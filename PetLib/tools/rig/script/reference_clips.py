"""
从 classic GLB 直接采样参考剪辑的关节世界矩阵 → clip-samples.json。

rig.blend 是纯骨架工作区、不附带剪辑；本脚本在纯 glTF 空间从
classic-player.glb 采样全部（或指定）参考动画，产物与
sample_clips.py 完全同构，供 merge_channels.py 生成守门样例
（rig-export.glb，与内置模型渲染逐位一致）或取参考数据。

用法：
python script/reference_clips.py <repo-root> [剪辑名...]（在 rig/ 目录执行）
"""
import json
import struct
import sys
from pathlib import Path

from merge_channels import decompose, load_glb, matmul, node_maps, trs

FPS = 60
JOINTS = ("Orbit", "Body", "Head", "Left_Arm", "Right_Arm",
          "Left_Leg", "Right_Leg", "Cape")
SRC = "PetLib/rust/src/assets/classic-player.glb"
OUT = "PetLib/tools/rig/out/clip-samples.json"


def acc_read(doc, bin_chunk, idx):
    a = doc["accessors"][idx]
    bv = doc["bufferViews"][a["bufferView"]]
    start = bv.get("byteOffset", 0) + a.get("byteOffset", 0)
    comp = {"SCALAR": 1, "VEC2": 2, "VEC3": 3, "VEC4": 4}[a["type"]]
    stride = bv.get("byteStride", 4 * comp)
    return [list(struct.unpack_from("<" + "f" * comp, bin_chunk,
                                    start + i * stride))
            for i in range(a["count"])]


class Anim:
    """一个 glTF 动画：按时间求各节点本地 TRS（LINEAR，端点钳制）。"""

    def __init__(self, doc, bin_chunk, anim):
        self.doc = doc
        self.bin = bin_chunk
        self.channels = {}  # node_idx -> {path: (times, vals)}
        for ch in anim["channels"]:
            node = ch["target"]["node"]
            s = anim["samplers"][ch["sampler"]]
            times = [v[0] for v in acc_read(doc, bin_chunk, s["input"])]
            vals = acc_read(doc, bin_chunk, s["output"])
            self.channels.setdefault(node, {})[ch["target"]["path"]] = (times, vals)
        self.t_end = max((times[-1] for chans in self.channels.values()
                          for times, _ in chans.values()), default=0.0)

    def local(self, node_idx, t):
        n = self.doc["nodes"][node_idx]
        tr = list(n.get("translation", [0.0, 0.0, 0.0]))
        qr = list(n.get("rotation", [0.0, 0.0, 0.0, 1.0]))
        for path, arr in (("translation", tr), ("rotation", qr)):
            if path not in self.channels.get(node_idx, {}):
                continue
            times, vals = self.channels[node_idx][path]
            if t <= times[0]:
                v = vals[0]
            elif t >= times[-1]:
                v = vals[-1]
            else:
                k = next(i for i in range(1, len(times)) if times[i] >= t)
                t0, t1 = times[k - 1], times[k]
                w = 0.0 if t1 == t0 else (t - t0) / (t1 - t0)
                a, b = vals[k - 1], vals[k]
                if path == "rotation" and sum(x * y for x, y in zip(a, b)) < 0:
                    b = [-x for x in b]
                v = [a[i] * (1 - w) + b[i] * w for i in range(len(a))]
            arr[:] = v
        return trs(tr, qr)


def main() -> int:
    argv = sys.argv[sys.argv.index("--") + 1:] if "--" in sys.argv else sys.argv[1:]
    root = Path(argv[0]).resolve()
    names = argv[1:]

    doc, bin_chunk = load_glb(root / SRC)
    names_map, parent = node_maps(doc)
    joints = [j for j in JOINTS if j in names_map]
    arm_idx = names_map["blockbench_export"]

    def world_at(idx, anim, t, memo):
        if idx in memo:
            return memo[idx]
        lm = anim.local(idx, t)
        p = parent.get(idx)
        w = matmul(world_at(p, anim, t, memo), lm) if p is not None else lm
        memo[idx] = w
        return w

    clips = []
    for anim_gltf in doc["animations"]:
        clip = anim_gltf["name"]
        # 跳过不驱动任何关节的残渣动画（C4D 相机等）
        if not any(doc["nodes"][ch["target"]["node"]].get("name") in joints
                   for ch in anim_gltf["channels"]):
            print(f"skip {clip}: no joint channels")
            continue
        if names and clip not in names:
            continue
        anim = Anim(doc, bin_chunk, anim_gltf)
        n_frames = int(round(anim.t_end * FPS)) + 1
        frames = []
        for f in range(1, n_frames + 1):
            t = (f - 1) / FPS
            memo = {}
            sample = {"time": round(t, 6), "joints": {}}
            for j in joints:
                w = world_at(names_map[j], anim, t, memo)
                tt, qq = decompose(w)
                sample["joints"][j] = {
                    "t": [round(v, 6) for v in tt],
                    "q": [round(v, 6) for v in qq],
                }
            frames.append(sample)
        clips.append({"name": clip, "frames": frames})
        print(f"sampled {clip}: 1-{n_frames} ({anim.t_end:.3f}s)")

    if not clips:
        raise SystemExit(f"no matching animations (wanted: {names or 'all'})")

    rt, rq = decompose(world_at(arm_idx, Anim(doc, bin_chunk, doc["animations"][0]),
                                0.0, {}))
    out = root / OUT
    out.parent.mkdir(parents=True, exist_ok=True)
    out.write_text(json.dumps({
        "root": {"t": [round(v, 6) for v in rt],
                 "q": [round(v, 6) for v in rq]},
        "clips": clips,
    }))
    print(f"wrote {out}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
