"""
从骨架按名采样指定 NLA 轨道的关节世界矩阵，输出 JSON（glTF Y-up）。

关节帧校正：
Blender 骨骼坐标系的 Y 永远沿"头→尾"，而管线各处（基准 GLB 关节
节点、内置模型、导出器）用的都是关节枢轴帧。pose_bones[].matrix
给的是骨骼帧——骨尾取向若不沿关节帧 Y（旧版 build_rig 把骨尾指到
骨架 +Z），每关节会带一个恒定旋转（实测 +90°X），采样产物接入运行
时全部部件绕枢轴甩开。本脚本按基准 GLB（out/rig-base.glb，须先经
export_base.py 生成）的关节节点反推每根骨的"骨骼帧→关节帧"常量
校正，并在静止姿态下自检（校正后必须逐位回到基准静止世界）。

注意（Blender 5.0 后台模式实测）：仅 frame_set 不会使骨架姿态重新
求值，首帧之后的读取全部粘在首次求值结果上；每帧对采样轨道做一次
mute 抖动（mute→update→unmute→update）才能强制逐帧求值。预热
（unmute/求值/remute）不可用，会让姿态永久卡死。采样前姿态骨会被
归零：未打键的骨按模型契约回落立正 rest，而非残留在上次编辑的姿势。

用法：
blender -b --factory-startup rig.blend -P script/sample_clips.py -- <repo-root> <剪辑名> [剪辑名...]（在 rig/ 目录执行）
"""
import json
import math
import sys
from pathlib import Path

import bpy
from mathutils import Matrix, Quaternion

JOINTS = ("Orbit", "Body", "Head", "Left_Arm", "Right_Arm",
          "Left_Leg", "Right_Leg", "Cape")
FPS = 60

# blender(Z-up) -> glTF(Y-up)：p_g = (x, z, -y)
S = Matrix(((1, 0, 0, 0), (0, 0, 1, 0), (0, -1, 0, 0), (0, 0, 0, 1)))
S_INV = S.inverted()

# 纯 Python 侧的 S 共轭（GLB 文档数学用，与 merge_channels 同式）
S_PY = [[1.0, 0, 0, 0], [0, 0, 1.0, 0], [0, -1.0, 0, 0], [0, 0, 0, 1.0]]
S_INV_PY = [[1.0, 0, 0, 0], [0, 0, -1.0, 0], [0, 1.0, 0, 0], [0, 0, 0, 1.0]]


def to_gltf(m: Matrix) -> dict:
    g = S @ m @ S_INV
    q = g.to_quaternion()
    return {
        "t": [round(g.translation.x, 6), round(g.translation.y, 6),
              round(g.translation.z, 6)],
        "q": [round(q.x, 6), round(q.y, 6), round(q.z, 6), round(q.w, 6)],
    }


def eval_frame(track, f: int) -> None:
    """切到第 f 帧并强制骨架姿态按该帧 NLA 重新求值。"""
    bpy.context.scene.frame_set(f)
    track.mute = True
    bpy.context.view_layer.update()
    track.mute = False
    bpy.context.view_layer.update()


def sample_track(arm, track, corrections) -> dict:
    strip = track.strips[0]
    start, end = int(strip.frame_start), int(strip.frame_end)
    track.mute = False
    frames = []
    prev_q = {}
    max_dev = 0.0
    for f in range(start, end + 1):
        eval_frame(track, f)
        dg = bpy.context.evaluated_depsgraph_get()
        aev = arm.evaluated_get(dg)
        world = aev.matrix_world
        sample = {"time": round((f - start) / FPS, 6),
                  "joints": {}}
        for name in JOINTS:
            # 骨骼帧 -> 关节帧（校正常量，见模块 docstring）
            m = world @ aev.pose.bones[name].matrix @ corrections[name]
            g = to_gltf(m)
            if f > start:
                pq = prev_q[name]
                # 四元数双覆盖下取最短夹角；旧公式只看部分分量，
                # 会把纯 Z/Y 轴旋转误报成 FROZEN。
                dot = abs(sum(a * b for a, b in zip(g["q"], pq)))
                dev = 2.0 * math.acos(min(1.0, dot))
                max_dev = max(max_dev, dev)
            prev_q[name] = g["q"]
            sample["joints"][name] = g
        frames.append(sample)
    track.mute = True
    state = "ok" if max_dev > 1e-4 else "FROZEN?"
    print(f"sampled {track.name}: {start}-{end} max_dev={max_dev:.5f} [{state}]")
    return {"name": track.name, "frames": frames}


def joint_frame_corrections(arm, base_glb: Path) -> dict:
    """按基准 GLB 的关节节点（关节枢轴帧）反推每根骨的常量校正，
    并在静止姿态下自检：校正后的关节世界必须逐位回到基准静止世界。

    校正 = 骨骼rest帧⁻¹ × 关节帧(骨架空间)，对任意姿态恒定（骨骼帧
    随关节刚体运动）。基准 GLB 缺失或关节不齐时直接报错——先跑
    export_base.py。自检同时清掉姿态骨残留（未打键的骨按契约回落
    立正 rest，而非上次编辑的姿势）；只改内存，不保存工程。
    """
    sys.path.insert(0, str(Path(__file__).resolve().parent))
    from merge_channels import decompose, load_glb, matmul, node_maps, trs

    if not base_glb.is_file():
        raise SystemExit("base GLB not found: " + str(base_glb)
                         + "\nrun export_base.py first (step 1 of the pipeline)")
    doc, _ = load_glb(base_glb)
    names, parent = node_maps(doc)
    missing = [j for j in JOINTS if j not in names]
    if missing:
        raise SystemExit(f"base GLB misses joints {missing}: {base_glb}")

    cache = {}

    def static_world(idx: int):
        if idx in cache:
            return cache[idx]
        n = doc["nodes"][idx]
        m = trs(n.get("translation", [0.0, 0.0, 0.0]),
                n.get("rotation", [0.0, 0.0, 0.0, 1.0]))
        p = parent.get(idx)
        m = matmul(static_world(p), m) if p is not None else m
        cache[idx] = m
        return m

    arm_inv = arm.matrix_world.inverted()
    corrections = {}
    for j in JOINTS:
        m_gltf = matmul(matmul(S_INV_PY, static_world(names[j])), S_PY)
        t, q = decompose(m_gltf)
        frame = (Matrix.Translation(t)
                 @ Quaternion((q[3], q[0], q[1], q[2])).to_matrix().to_4x4())
        # 骨骼rest帧⁻¹ × 关节帧(骨架空间)：静止时 world @ rest @ 校正
        # = world @ rest @ rest⁻¹ @ 关节帧_arm = 关节帧世界（自检锚点）
        corrections[j] = (arm.data.bones[j].matrix_local.inverted()
                          @ (arm_inv @ frame))

    # 静止自检：全部轨道静音并抖动强制重估；随后清除求值写入的
    # 姿态属性，校正后的关节世界必须逐位回到基准静止世界。
    tracks = list(arm.animation_data.nla_tracks)
    for t in tracks:
        t.mute = True
    bpy.context.scene.frame_set(1)
    bpy.context.view_layer.update()
    if tracks:
        tracks[0].mute = False
        bpy.context.view_layer.update()
        tracks[0].mute = True
        bpy.context.view_layer.update()
    # NLA 求值会把剪辑值写进姿态属性，静音后仍可能残留；必须在
    # 抖动之后清零，否则非立正剪辑会被误判为 rest 不一致。
    for pb in arm.pose.bones:
        pb.matrix_basis = Matrix.Identity(4)
    bpy.context.view_layer.update()
    dg = bpy.context.evaluated_depsgraph_get()
    aev = arm.evaluated_get(dg)
    world = aev.matrix_world
    for j in JOINTS:
        g = to_gltf(world @ aev.pose.bones[j].matrix @ corrections[j])
        t, q = decompose(static_world(names[j]))
        dot = abs(sum(a * b for a, b in zip(g["q"], q)))
        ang = math.degrees(2 * math.acos(min(1.0, dot)))
        dt = math.dist(g["t"], t)
        if ang > 0.01 or dt > 1e-3:
            pb = aev.pose.bones[j]
            raise SystemExit(
                f"self-check failed for {j}: d_rot={ang:.3f}deg "
                f"d_trans={dt:.5f}\n"
                f"  pb.matrix:   t={tuple(round(v, 4) for v in pb.matrix.translation)} "
                f"q={tuple(round(v, 4) for v in pb.matrix.to_quaternion())}\n"
                f"  matrix_local:t={tuple(round(v, 4) for v in arm.data.bones[j].matrix_local.translation)} "
                f"q={tuple(round(v, 4) for v in arm.data.bones[j].matrix_local.to_quaternion())}\n"
                "the blend's rest diverges from the base GLB "
                "- re-run export_base.py")
    tilt = max(math.degrees(c.to_quaternion().angle)
               for c in corrections.values())
    print(f"joint-frame corrections: max tilt {tilt:.2f} deg "
          f"{'(bone/joint frames differ, corrected)' if tilt > 1 else '(bones aligned)'}")
    print("self-check: corrected rest matches base GLB (8 joints)")
    return corrections


def main() -> int:
    argv = sys.argv[sys.argv.index("--") + 1:]
    root = Path(argv[0]).resolve()
    names_arg = argv[1:]
    out = root / "PetLib/tools/rig/out/clip-samples.json"
    base_glb = root / "PetLib/tools/rig/out/rig-base.glb"

    arm = next(o for o in bpy.data.objects if o.type == "ARMATURE")
    ad = arm.animation_data or arm.animation_data_create()
    # 活动动作会污染后台 NLA 逐轨求值（非活动轨全部粘成首采样轨数据）。
    # Blender 5.0 slotted action 须先清 slot，再清 action。
    if getattr(ad, "action_slot", None) is not None:
        ad.action_slot = None
    ad.action = None

    # 静止自检会临时静音轨道；保存真正的初始状态供采样后恢复。
    original_mutes = {t.name: t.mute for t in ad.nla_tracks}
    corrections = joint_frame_corrections(arm, base_glb)

    available = [t.name for t in ad.nla_tracks]
    if not names_arg:
        hint = ("available tracks: " + str(available)
                if available else
                "available tracks: (none — rig ships no clips; "
                "see script/rebuild_actions.py to pull one in)")
        raise SystemExit(
            "usage: sample_clips.py -- <repo-root> <clip> [clip...]\n"
            + hint)
    missing = [n for n in names_arg if n not in available]
    if missing:
        raise SystemExit(
            f"track not found: {missing!r}\navailable tracks: {available}")

    # 轨道隔离：采样期间其余轨道全部静音，避免混入目标轨道的求值
    for t in ad.nla_tracks:
        t.mute = True

    clips = []
    try:
        for name in names_arg:
            track = next(t for t in ad.nla_tracks if t.name == name)
            clips.append(sample_track(arm, track, corrections))
    finally:
        for t in ad.nla_tracks:
            t.mute = original_mutes[t.name]

    out.parent.mkdir(parents=True, exist_ok=True)
    out.write_text(json.dumps({"root": to_gltf(arm.matrix_world),
                               "clips": clips}))
    print(f"wrote {out}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
