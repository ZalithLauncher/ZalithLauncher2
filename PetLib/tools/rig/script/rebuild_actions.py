"""
从 classic GLB 通道直接重建骨架姿态动作。

rig.blend 的姿态动作源自 glTF 导入器写到 empties 的参考剪辑，
导入器按场景 fps=30 落键，而骨架按 60fps 播放/烘焙，导致剪辑以 2 倍速
采样源数据（idle 呼吸相位在 t=0.5 处漂移 1.3cm，渲染门禁 6% 像素差）。

用法（须先运行 export_base.py 生成实际 Rig 的静止基准）：
blender -b --factory-startup rig.blend -P script/rebuild_actions.py -- <repo-root>（在 rig/ 目录执行）
"""
import json
import struct
import sys
from pathlib import Path

import bpy
from mathutils import Matrix

BONES = ("Orbit", "Body", "Head", "Left_Arm", "Right_Arm",
         "Left_Leg", "Right_Leg", "Cape")
BONE_PARENT = {
    "Orbit": None,
    "Body": "Orbit", "Left_Leg": "Orbit", "Right_Leg": "Orbit",
    "Head": "Body", "Left_Arm": "Body", "Right_Arm": "Body", "Cape": "Body",
}
FPS = 60

# blender(Z-up) <-> glTF(Y-up)：p_g = (x, z, -y)
S = Matrix(((1, 0, 0, 0), (0, 0, 1, 0), (0, -1, 0, 0), (0, 0, 0, 1)))
S_INV = S.inverted()


# ---------- glTF 解析 ----------

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
            bin_chunk = chunk
        off += 8 + length
    return doc, bin_chunk


def acc_read(doc, bc, idx):
    a = doc["accessors"][idx]
    bv = doc["bufferViews"][a["bufferView"]]
    start = bv.get("byteOffset", 0) + a.get("byteOffset", 0)
    comp = {"VEC3": 3, "VEC4": 4, "SCALAR": 1}[a["type"]]
    stride = bv.get("byteStride", 4 * comp)
    return [struct.unpack_from("<" + "f" * comp, bc, start + i * stride)
            for i in range(a["count"])]


def quat_mat(q):
    x, y, z, w = q
    return [[1 - 2 * (y * y + z * z), 2 * (x * y - z * w), 2 * (x * z + y * w)],
            [2 * (x * y + z * w), 1 - 2 * (x * x + z * z), 2 * (y * z - x * w)],
            [2 * (x * z - y * w), 2 * (y * z + x * w), 1 - 2 * (x * x + y * y)]]


def trs(t, q):
    r = quat_mat(q)
    return [[r[0][0], r[0][1], r[0][2], t[0]],
            [r[1][0], r[1][1], r[1][2], t[1]],
            [r[2][0], r[2][1], r[2][2], t[2]],
            [0.0, 0.0, 0.0, 1.0]]


def matmul(a, b):
    return [[sum(a[r][k] * b[k][c] for k in range(4)) for c in range(4)]
            for r in range(4)]


def mat_inv(m):
    r = [[m[c][r] for c in range(3)] for r in range(3)]
    t = [m[0][3], m[1][3], m[2][3]]
    ti = [-(r[i][0] * t[0] + r[i][1] * t[1] + r[i][2] * t[2]) for i in range(3)]
    return [[r[0][0], r[0][1], r[0][2], ti[0]],
            [r[1][0], r[1][1], r[1][2], ti[1]],
            [r[2][0], r[2][1], r[2][2], ti[2]],
            [0.0, 0.0, 0.0, 1.0]]


def to_mathutils(m):
    return Matrix((m[0], m[1], m[2], m[3]))


# ---------- 通道采样 ----------

class Anim:
    """一个 glTF 动画：按时间求各节点本地 TRS（LINEAR，端点钳制）。"""

    def __init__(self, doc, bc, anim):
        self.channels = {}  # node_idx -> {path: (times, vals)}
        for ch in anim["channels"]:
            node = ch["target"]["node"]
            s = anim["samplers"][ch["sampler"]]
            times = [v[0] for v in acc_read(doc, bc, s["input"])]
            vals = acc_read(doc, bc, s["output"])
            self.channels.setdefault(node, {})[ch["target"]["path"]] = (times, vals)

    def local(self, doc, node_idx, t):
        n = doc["nodes"][node_idx]
        tr = list(n.get("translation", [0.0, 0.0, 0.0]))
        qr = list(n.get("rotation", [0.0, 0.0, 0.0, 1.0]))
        chans = self.channels.get(node_idx, {})
        for path, arr in (("translation", tr), ("rotation", qr)):
            if path not in chans:
                continue
            times, vals = chans[path]
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
    argv = sys.argv[sys.argv.index("--") + 1:]
    root = Path(argv[0]).resolve()
    doc, bc = load_glb(root / "PetLib/rust/src/assets/classic-player.glb")

    names = {n.get("name"): i for i, n in enumerate(doc["nodes"])}
    parent = {}
    for i, n in enumerate(doc["nodes"]):
        for c in n.get("children", []):
            parent[c] = i

    arm = bpy.data.objects["blockbench_export"]
    ad = arm.animation_data or arm.animation_data_create()
    rest_local = {b: arm.data.bones[b].matrix_local.copy() for b in BONES}

    # classic 根链世界（blockbench_export 的 glTF 世界 = 骨架对象的 glTF 世界）
    def world_gltf(node_idx, anim, t, memo):
        if node_idx in memo:
            return memo[node_idx]
        lm = anim.local(doc, node_idx, t)
        p = parent.get(node_idx)
        w = matmul(world_gltf(p, anim, t, memo), lm) if p is not None else lm
        memo[node_idx] = w
        return w

    arm_idx = names["blockbench_export"]
    joints = [b for b in BONES if b in names]
    order = [b for b in ("Orbit", "Body", "Head", "Left_Arm", "Right_Arm",
                         "Left_Leg", "Right_Leg", "Cape") if b in joints]

    # 与采样器共用实际 Rig 的静止关节帧。classic 的原始 rest 带有
    # 四肢跨步和披风姿态，不能代替 build_rig 立正归零后的 rest。
    # 采样器以 joint = bone @ correction 输出，这里走逆变换写骨骼帧。
    sys.path.insert(0, str(Path(__file__).resolve().parent))
    from sample_clips import joint_frame_corrections
    joint_corrections = joint_frame_corrections(
        arm, root / "PetLib/tools/rig/out/rig-base.glb")

    # 清旧轨道与动作
    old_actions = [s.action for t in ad.nla_tracks for s in t.strips if s.action]
    for t in list(ad.nla_tracks):
        ad.nla_tracks.remove(t)
    for a in old_actions:
        bpy.data.actions.remove(a)

    for anim_gltf in doc["animations"]:
        clip = anim_gltf["name"]
        # 跳过不驱动任何关节的残渣动画（C4D 相机等）
        if not any(doc["nodes"][ch["target"]["node"]].get("name") in joints
                   for ch in anim_gltf["channels"]):
            print(f"skip {clip}: no joint channels")
            continue
        anim = Anim(doc, bc, anim_gltf)
        # 时长：所有关节通道的最大末键时间
        t_end = 0.0
        for chans in anim.channels.values():
            for times, _ in chans.values():
                t_end = max(t_end, times[-1])
        n_frames = int(round(t_end * FPS)) + 1

        act = bpy.data.actions.new(clip)
        ad.action = act
        slot = getattr(ad, "action_slot", None)
        if slot is None and hasattr(act, "slots"):
            slot = act.slots.new(name=clip, id_type="OBJECT")
            ad.action_slot = slot

        # 预解每帧每关节 basis
        for f in range(1, n_frames + 1):
            t = (f - 1) / FPS
            memo = {}
            w_arm = world_gltf(arm_idx, anim, t, memo)
            desired = {}   # bone -> mathutils Matrix（骨架空间）
            for b in order:
                wj = world_gltf(names[b], anim, t, memo)
                joint = S_INV @ to_mathutils(mat_inv(w_arm)) @ to_mathutils(wj) @ S
                desired[b] = joint @ joint_corrections[b].inverted()
            for b in order:
                p = BONE_PARENT[b]
                if p is None:
                    basis = rest_local[b].inverted() @ desired[b]
                else:
                    basis = (rest_local[b].inverted() @ rest_local[p]
                             @ desired[p].inverted() @ desired[b])
                pb = arm.pose.bones[b]
                pb.location = basis.translation
                pb.rotation_mode = "QUATERNION"
                pb.rotation_quaternion = basis.to_quaternion()
                pb.keyframe_insert("location", frame=f)
                pb.keyframe_insert("rotation_quaternion", frame=f)

        # LINEAR 插值 + 上 NLA
        curves = (fc for layer in act.layers for strip in layer.strips
                  for bag in (getattr(strip, "channelbags", None) or [])
                  for fc in bag.fcurves)
        for fc in curves:
            for kp in fc.keyframe_points:
                kp.interpolation = "LINEAR"
        track = ad.nla_tracks.new()
        track.name = clip
        track.mute = True
        strip = track.strips.new(clip, 1, act)
        strip.name = clip
        print(f"rebuilt {clip}: 1-{n_frames} ({t_end:.3f}s)")

    # 姿态归零并保存；清空活动动作（残留会导致后台逐轨求值粘连首轨数据）
    ad.action = None
    if getattr(ad, "action_slot", None) is not None:
        ad.action_slot = None
    for pb in arm.pose.bones:
        pb.location = (0., 0., 0.)
        pb.rotation_mode = "QUATERNION"
        pb.rotation_quaternion.identity()
    bpy.context.scene.frame_set(1)
    bpy.ops.wm.save_mainfile()
    print("saved rig.blend")
    return 0


if __name__ == "__main__":
    sys.exit(main())
