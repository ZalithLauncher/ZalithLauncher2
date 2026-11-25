"""
合并 rig 导出

用法：
python script/merge_channels.py <repo-root>（在 rig/ 目录执行）
"""
import json
import struct
import sys
from pathlib import Path


def load_glb(path: Path):
    data = path.read_bytes()
    assert data[:4] == b"glTF", path
    off = 12
    doc = bin_chunk = None
    while off < len(data):
        length, ctype = struct.unpack_from("<II", data, off)
        chunk = data[off + 8: off + 8 + length]
        if ctype == 0x4E4F534A:
            doc = json.loads(chunk.decode("utf-8"))
        elif ctype == 0x004E4942:
            bin_chunk = bytearray(chunk)
        off += 8 + length
    return doc, bin_chunk


def save_glb(path: Path, doc: dict, bin_chunk: bytes) -> None:
    payload = json.dumps(doc, separators=(",", ":")).encode("utf-8")
    payload += b" " * ((4 - len(payload) % 4) % 4)
    out = bytearray()
    out += b"glTF" + struct.pack(
        "<II", 2, 12 + 8 + len(payload) + (8 + len(bin_chunk) if bin_chunk else 0))
    out += struct.pack("<II", len(payload), 0x4E4F534A) + payload
    if bin_chunk is not None:
        pad = (4 - len(bin_chunk) % 4) % 4
        out += struct.pack("<II", len(bin_chunk) + pad, 0x004E4942)
        out += bytes(bin_chunk) + b"\x00" * pad
    path.write_bytes(bytes(out))


def write_accessor(doc, bin_chunk, values: list, comp: int,
                   component_type: int = 5126) -> int:
    pack = {5126: ("f", 4), 5123: ("H", 2), 5121: ("B", 1),
            5125: ("I", 4)}[component_type]
    flat = list(values)
    raw = b"".join(struct.pack("<" + pack[0], v) for v in flat)
    while len(bin_chunk) % 4:
        bin_chunk.append(0)
    off = len(bin_chunk)
    bin_chunk.extend(raw)
    doc["bufferViews"].append({"buffer": 0, "byteOffset": off, "byteLength": len(raw)})
    doc["accessors"].append({
        "bufferView": len(doc["bufferViews"]) - 1,
        "componentType": component_type,
        "count": len(flat) // comp,
        "type": {1: "SCALAR", 2: "VEC2", 3: "VEC3", 4: "VEC4"}[comp],
    })
    return len(doc["accessors"]) - 1


def quat_to_mat(q):
    x, y, z, w = q
    n = (x * x + y * y + z * z + w * w) ** 0.5
    x, y, z, w = x / n, y / n, z / n, w / n
    return [
        [1 - 2 * (y * y + z * z), 2 * (x * y - z * w), 2 * (x * z + y * w)],
        [2 * (x * y + z * w), 1 - 2 * (x * x + z * z), 2 * (y * z - x * w)],
        [2 * (x * z - y * w), 2 * (y * z + x * w), 1 - 2 * (x * x + y * y)],
    ]


def trs(t, q):
    r = quat_to_mat(q)
    return [[r[0][0], r[0][1], r[0][2], t[0]],
            [r[1][0], r[1][1], r[1][2], t[1]],
            [r[2][0], r[2][1], r[2][2], t[2]],
            [0.0, 0.0, 0.0, 1.0]]


def matmul(a, b):
    return [[sum(a[r][k] * b[k][c] for k in range(4)) for c in range(4)]
            for r in range(4)]


def mat_inv(m):
    # 刚体 TRS 矩阵：R⁻¹ = 转置，t' = -R⁻¹t
    r = [[m[c][r] for c in range(3)] for r in range(3)]
    t = [m[0][3], m[1][3], m[2][3]]
    ti = [-(r[i][0] * t[0] + r[i][1] * t[1] + r[i][2] * t[2]) for i in range(3)]
    return [[r[0][0], r[0][1], r[0][2], ti[0]],
            [r[1][0], r[1][1], r[1][2], ti[1]],
            [r[2][0], r[2][1], r[2][2], ti[2]],
            [0.0, 0.0, 0.0, 1.0]]


def decompose(m):
    t = [m[0][3], m[1][3], m[2][3]]
    r00, r01, r02 = m[0][0], m[0][1], m[0][2]
    r10, r11, r12 = m[1][0], m[1][1], m[1][2]
    r20, r21, r22 = m[2][0], m[2][1], m[2][2]
    tr = r00 + r11 + r22
    if tr > 0:
        s = (tr + 1.0) ** 0.5 * 2
        q = [(r21 - r12) / s, (r02 - r20) / s, (r10 - r01) / s, 0.25 * s]
    elif r00 > r11 and r00 > r22:
        s = (1.0 + r00 - r11 - r22) ** 0.5 * 2
        q = [0.25 * s, (r01 + r10) / s, (r02 + r20) / s, (r21 - r12) / s]
    elif r11 > r22:
        s = (1.0 + r11 - r00 - r22) ** 0.5 * 2
        q = [(r01 + r10) / s, 0.25 * s, (r12 + r21) / s, (r02 - r20) / s]
    else:
        s = (1.0 + r22 - r00 - r11) ** 0.5 * 2
        q = [(r02 + r20) / s, (r12 + r21) / s, 0.25 * s, (r10 - r01) / s]
    n = sum(v * v for v in q) ** 0.5
    return t, [v / n for v in q]


def node_maps(doc):
    by_name = {}
    parent = {}
    for i, n in enumerate(doc["nodes"]):
        if "name" in n:
            by_name[n["name"]] = i
        for c in n.get("children", []):
            parent[c] = i
    return by_name, parent


def read_acc(doc, bin_chunk, idx):
    a = doc["accessors"][idx]
    bv = doc["bufferViews"][a["bufferView"]]
    start = bv.get("byteOffset", 0) + a.get("byteOffset", 0)
    comp = {"SCALAR": 1, "VEC2": 2, "VEC3": 3, "VEC4": 4}[a["type"]]
    fmt = {5126: "f", 5123: "H", 5121: "B", 5125: "I"}[a["componentType"]]
    stride = bv.get("byteStride", struct.calcsize("<" + fmt) * comp)
    return [list(struct.unpack_from("<" + fmt * comp, bin_chunk,
                                    start + i * stride))
            for i in range(a["count"])]


def compensate_meshes(base_doc, base_bin, classic_doc, classic_bin) -> None:
    classic_names, _ = node_maps(classic_doc)
    copied, skipped = 0, []
    for n in base_doc["nodes"]:
        if "mesh" not in n or "name" not in n:
            continue
        c_idx = classic_names.get(n["name"])
        if c_idx is None:
            continue
        c_node = classic_doc["nodes"][c_idx]
        mesh = base_doc["meshes"][n["mesh"]]
        c_mesh = classic_doc["meshes"][c_node["mesh"]]
        prim, c_prim = mesh["primitives"][0], c_mesh["primitives"][0]
        pos_idx = prim["attributes"].get("POSITION")
        c_pos_idx = c_prim["attributes"].get("POSITION")
        if (pos_idx is None or c_pos_idx is None
                or base_doc["accessors"][pos_idx]["count"]
                != classic_doc["accessors"][c_pos_idx]["count"]):
            skipped.append(n["name"])
            continue
        for attr, c_acc_idx in c_prim["attributes"].items():
            vals = read_acc(classic_doc, classic_bin, c_acc_idx)
            a = classic_doc["accessors"][c_acc_idx]
            comp = {"SCALAR": 1, "VEC2": 2, "VEC3": 3, "VEC4": 4}[a["type"]]
            prim["attributes"][attr] = write_accessor(
                base_doc, base_bin, [v for row in vals for v in row], comp,
                a["componentType"])
        if "indices" in c_prim:
            a = classic_doc["accessors"][c_prim["indices"]]
            vals = read_acc(classic_doc, classic_bin, c_prim["indices"])
            prim["indices"] = write_accessor(
                base_doc, base_bin, [v[0] for v in vals], 1,
                a["componentType"])
        for key in ("translation", "rotation", "scale"):
            if key in c_node:
                n[key] = list(c_node[key])
            else:
                n.pop(key, None)
        copied += 1
    print(f"mesh compensation: {copied} copied, kept {skipped}")


def main() -> int:
    root = Path(sys.argv[1]).resolve()
    out_dir = root / "PetLib/tools/rig/out"
    base_doc, base_bin = load_glb(out_dir / "rig-base.glb")
    samples = json.loads((out_dir / "clip-samples.json").read_text())
    classic_doc, classic_bin = load_glb(
        root / "PetLib/rust/src/assets/classic-player.glb")
    compensate_meshes(base_doc, base_bin, classic_doc, classic_bin)

    base_names, base_parent = node_maps(base_doc)
    root_mat = trs(samples["root"]["t"], samples["root"]["q"])

    joints = [n for n in ("Orbit", "Body", "Head", "Left_Arm", "Right_Arm",
                          "Left_Leg", "Right_Leg", "Cape")
              if n in base_names]

    new_anims = []
    for clip in samples["clips"]:
        frames = clip["frames"]
        times = [f["time"] for f in frames]
        world = {j: [trs(f["joints"][j]["t"], f["joints"][j]["q"])
                     for f in frames] for j in joints}
        channels, samplers = [], []
        for jname in joints:
            b_idx = base_names[jname]
            p_idx = base_parent.get(b_idx)
            p_name = base_doc["nodes"][p_idx].get("name") if p_idx is not None else None
            loc_out, rot_out = [], []
            for k in range(len(frames)):
                w_j = world[jname][k]
                if p_name in world:
                    w_p = world[p_name][k]
                else:
                    w_p = root_mat
                lt, lq = decompose(matmul(mat_inv(w_p), w_j))
                loc_out.extend(lt)
                rot_out.extend(lq)
            time_acc = write_accessor(base_doc, base_bin, list(times), 1)
            loc_acc = write_accessor(base_doc, base_bin, loc_out, 3)
            rot_acc = write_accessor(base_doc, base_bin, rot_out, 4)
            for path, acc in (("translation", loc_acc), ("rotation", rot_acc)):
                samplers.append({"input": time_acc, "output": acc,
                                 "interpolation": "LINEAR"})
                channels.append({"sampler": len(samplers) - 1,
                                 "target": {"node": b_idx, "path": path}})
        new_anims.append({"name": clip["name"],
                          "channels": channels, "samplers": samplers})

    base_doc["animations"] = new_anims
    save_glb(out_dir / "rig-export.glb", base_doc, bytes(base_bin))
    print(f"merged {len(new_anims)} clips -> {out_dir / 'rig-export.glb'}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
