"""
校验 GLB 是否满足 PetLib 模型契约

用法：
python script/validate_glb.py <file.glb> [--json]（在 rig/ 目录执行）
"""
import json
import struct
import sys
from pathlib import Path

JOINTS = {"Body", "Head", "Left_Arm", "Right_Arm", "Left_Leg", "Right_Leg", "Cape"}
CLIP_PREFIXES = ("idle", "idle_sub_", "interact")


def read_glb(path: Path) -> dict:
    data = path.read_bytes()
    if data[:4] != b"glTF":
        raise SystemExit("not a GLB file")
    version, _ = struct.unpack_from("<II", data, 4)
    if version != 2:
        raise SystemExit(f"unsupported GLB version: {version}")
    off = 12
    gltf = None
    buffers = []
    while off < len(data):
        length, ctype = struct.unpack_from("<II", data, off)
        chunk = data[off + 8 : off + 8 + length]
        if ctype == 0x4E4F534A:
            gltf = json.loads(chunk.decode("utf-8"))
        elif ctype == 0x004E4942:
            buffers.append(chunk)
        off += 8 + length
    if gltf is None:
        raise SystemExit("missing JSON chunk")
    if len(buffers) > 1:
        raise SystemExit(f"contract requires a single buffer, got {len(buffers)}")
    if "buffers" in gltf and len(gltf["buffers"]) != 1:
        raise SystemExit(f"contract requires a single buffer, got {len(gltf['buffers'])}")
    return gltf


def check(gltf: dict) -> list[str]:
    errors: list[str] = []
    nodes = gltf.get("nodes", [])

    for i, node in enumerate(nodes):
        if "matrix" in node:
            errors.append(f"node {i} ({node.get('name', '?')}) uses matrix; contract requires TRS")

    names = {n.get("name") for n in nodes}
    missing = JOINTS - names
    if missing:
        errors.append(f"missing joints: {sorted(missing)}")

    has_rotation_root = any(
        abs(r[1]) > 0.99 for n in nodes if (r := n.get("rotation", [0, 0, 0, 1]))
    )
    if not has_rotation_root:
        errors.append("no ~180° Y rotation root node found")

    animations = gltf.get("animations", [])
    if not animations:
        errors.append("no animations")
    for anim in animations:
        name = anim.get("name", "?")
        if not name.startswith(CLIP_PREFIXES):
            errors.append(f"animation {name!r} does not follow clip naming ({CLIP_PREFIXES})")
        for ch in anim.get("channels", []):
            path = ch["target"]["path"]
            if path not in ("rotation", "translation"):
                errors.append(f"animation {name!r}: unsupported channel path {path!r}")
            sampler = anim["samplers"][ch["sampler"]]
            interp = sampler.get("interpolation", "LINEAR")
            if interp != "LINEAR":
                errors.append(f"animation {name!r}: interpolation {interp!r} is not LINEAR")

    for mesh in gltf.get("meshes", []):
        for prim in mesh.get("primitives", []):
            if prim.get("mode", 4) != 4:
                errors.append(f"mesh {mesh.get('name', '?')}: mode {prim.get('mode')} is not triangles")
            for attr in ("POSITION", "NORMAL", "TEXCOORD_0"):
                if attr not in prim.get("attributes", {}):
                    errors.append(f"mesh {mesh.get('name', '?')}: missing attribute {attr}")
    return errors


def main() -> int:
    if len(sys.argv) < 2:
        raise SystemExit(__doc__)
    path = Path(sys.argv[1])
    gltf = read_glb(path)
    errors = check(gltf)
    clips = [a.get("name", "?") for a in gltf.get("animations", [])]
    print(f"{path.name}: {len(gltf.get('nodes', []))} nodes, "
          f"{len(gltf.get('meshes', []))} meshes, clips = {clips}")
    if errors:
        for e in errors:
            print(f"FAIL: {e}")
        return 1
    print("OK: contract satisfied")
    return 0


if __name__ == "__main__":
    sys.exit(main())
