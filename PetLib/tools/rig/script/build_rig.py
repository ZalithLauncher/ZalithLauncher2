"""
从内置玩家模型 GLB 构建动画骨架 rig.blend

blender -b --factory-startup -P script/build_rig.py -- <repo-root>（在 rig/ 目录执行）
"""
import sys
from pathlib import Path

import bpy
from mathutils import Matrix, Vector

# 导入器伴随的残渣：C4D 相机节点与空剪辑
ARTIFACT_PREFIXES = ("CINEMA_4D",)
# 恒等透传节点：删除前把子级按世界变换提升，删除前后世界矩阵须不变
PASS_THROUGH_NODES = ("RootNode", "Node_18")
# 立正归零的关节：四肢 rest 旋转只是源模型的跨步定格；
# 披风/根节点的旋转是结构性约定（π 翻转），必须保留
STANCE_JOINTS = ("Left_Arm", "Right_Arm", "Left_Leg", "Right_Leg")
# 骨架层级（Orbit 为根骨；须与导入 empties 的父子关系一致）
BONES = ("Orbit", "Body", "Head", "Left_Arm", "Right_Arm",
         "Left_Leg", "Right_Leg", "Cape")
BONE_PARENT = {
    "Orbit": None,
    "Body": "Orbit", "Left_Leg": "Orbit", "Right_Leg": "Orbit",
    "Head": "Body", "Left_Arm": "Body", "Right_Arm": "Body", "Cape": "Body",
}
# 姿态骨视口配色（THEME 调色板）
BONE_COLORS = {
    "Body": "THEME04", "Head": "THEME01",
    "Left_Arm": "THEME02", "Right_Arm": "THEME03",
    "Left_Leg": "THEME06", "Right_Leg": "THEME07",
    "Cape": "THEME05",
}
JOINT_MESHES = {
    "Head": ("Head_2", "Hat_Layer"),
    "Body": ("Body_2", "Body_Layer"),
    "Left_Arm": ("Left_Arm_2", "Left_Arm_Layer"),
    "Right_Arm": ("Right_Arm_2", "Right_Arm_Layer"),
    "Left_Leg": ("Left_Leg_2", "Left_Leg_Layer"),
    "Right_Leg": ("Right_Leg_2", "Right_Leg_Layer"),
    "Cape": ("Cape_2",),
}
# 骨长仅影响 Pose Mode 观感；骨架空间 Z 与模型同轴，取部件高度的 2/3
BONE_LENGTH = {
    "Orbit": 0.3, "Body": 0.5, "Head": 0.33,
    "Left_Arm": 0.5, "Right_Arm": 0.5,
    "Left_Leg": 0.5, "Right_Leg": 0.5,
    "Cape": 0.67,
}
EPS = 1e-4


def snapshot_world() -> dict:
    bpy.context.view_layer.update()
    return {o.name: o.matrix_world.copy() for o in bpy.data.objects}


def check_world_unchanged(snap: dict, label: str) -> None:
    bpy.context.view_layer.update()
    for o in bpy.data.objects:
        old = snap.get(o.name)
        if old is None:
            continue
        diff = max(
            abs(a - b)
            for ra, rb in zip(old, o.matrix_world)
            for a, b in zip(ra, rb)
        )
        if diff > EPS:
            raise RuntimeError(f"{label}: {o.name} world changed by {diff}")


def snapshot_world_verts() -> dict:
    bpy.context.view_layer.update()
    return {
        o.name: [o.matrix_world @ v.co for v in o.data.vertices]
        for o in bpy.data.objects
        if o.type == "MESH"
    }


def check_verts_unchanged(snap: dict, label: str) -> None:
    bpy.context.view_layer.update()
    for o in bpy.data.objects:
        old = snap.get(o.name)
        if o.type != "MESH" or old is None:
            continue
        if len(old) != len(o.data.vertices):
            raise RuntimeError(f"{label}: {o.name} vertex count changed")
        for co, v in zip(old, o.data.vertices):
            if (co - o.matrix_world @ v.co).length > EPS:
                raise RuntimeError(f"{label}: {o.name} world vertex moved")


def remove_artifacts() -> None:
    """删除残渣"""
    for obj in list(bpy.data.objects):
        if obj.name.startswith(ARTIFACT_PREFIXES):
            bpy.data.objects.remove(obj, do_unlink=True)
    for name in PASS_THROUGH_NODES:
        obj = bpy.data.objects.get(name)
        if obj is None:
            continue
        parent = obj.parent
        for child in list(obj.children):
            world = child.matrix_world.copy()
            child.parent = parent
            child.matrix_parent_inverse = Matrix.Identity(4)
            child.matrix_world = world
        bpy.data.objects.remove(obj, do_unlink=True)


def fcurve_count(action) -> int:
    """Blender 4.4+ 的分层动作没有顶层 fcurves，需经 channelbag 统计。"""
    if hasattr(action, "fcurves"):
        return len(action.fcurves)
    total = 0
    for layer in action.layers:
        for strip in layer.strips:
            bags = getattr(strip, "channelbags", None)
            if bags is not None:
                for bag in bags:
                    total += len(bag.fcurves)
    return total


def remove_empty_actions() -> None:
    for action in list(bpy.data.actions):
        if fcurve_count(action) == 0:
            bpy.data.actions.remove(action)


def reset_stance() -> None:
    for name in STANCE_JOINTS:
        bpy.data.objects[name].rotation_quaternion.identity()


def check_stance() -> None:
    bpy.context.view_layer.update()
    for name in STANCE_JOINTS:
        mesh = bpy.data.objects[name + "_2"]
        pts = [mesh.matrix_world @ Vector(c) for c in mesh.bound_box]
        skew = min(p.y for p in pts) + max(p.y for p in pts)
        if abs(skew) > 0.02:
            raise RuntimeError(f"stance: {name} still skewed by {skew}")


def build_armature(root_world: Matrix) -> bpy.types.Object:
    bpy.context.view_layer.update()
    old = bpy.data.objects["blockbench_export"]
    old.name = "bb_export_old"
    arm_data = bpy.data.armatures.new("PetLibRig")
    arm = bpy.data.objects.new("blockbench_export", arm_data)
    bpy.context.scene.collection.objects.link(arm)
    arm.matrix_world = root_world
    bpy.context.view_layer.objects.active = arm
    arm.select_set(True)
    bpy.ops.object.mode_set(mode="EDIT")
    base = root_world.inverted()
    for name in BONES:
        eb = arm_data.edit_bones.new(name)
        eb.matrix = base @ bpy.data.objects[name].matrix_world
        head = eb.matrix.translation.copy()
        eb.tail = head + Vector((0, 0, BONE_LENGTH[name]))
    bpy.context.view_layer.update()
    for name, parent in BONE_PARENT.items():
        if parent:
            arm_data.edit_bones[name].parent = arm_data.edit_bones[parent]
            arm_data.edit_bones[name].use_connect = False
    bpy.ops.object.mode_set(mode="OBJECT")
    arm.select_set(False)
    return arm


def reset_pose(arm: bpy.types.Object) -> None:
    for pb in arm.pose.bones:
        pb.location = (0., 0., 0.)
        pb.rotation_quaternion.identity()
        pb.scale = (1., 1., 1.)


def snap_world(obj: bpy.types.Object, target: Matrix) -> None:
    last = (0.0, 0.0)
    for _ in range(3):
        obj.matrix_world = target
        bpy.context.view_layer.update()
        err = obj.matrix_world.inverted() @ target
        last = (abs(err.to_quaternion().angle), err.translation.length)
        if last[0] < 1e-5 and last[1] < 1e-6:
            return
    raise RuntimeError(
        f"snap_world failed for {obj.name}: rot={last[0]:.6f} trans={last[1]:.6f}"
    )


def reparent_meshes(arm: bpy.types.Object, joint_frames: dict) -> None:
    """网格改父到关节骨并保持世界，随后原点归一到关节枢轴。"""
    reset_pose(arm)
    verts = snapshot_world_verts()
    bpy.context.view_layer.update()
    for joint, meshes in JOINT_MESHES.items():
        for mesh_name in meshes:
            mesh = bpy.data.objects[mesh_name]
            world = mesh.matrix_world.copy()
            mesh.parent = arm
            mesh.parent_type = "BONE"
            mesh.parent_bone = joint
            mesh.matrix_parent_inverse = Matrix.Identity(4)
            snap_world(mesh, world)
    bpy.context.view_layer.update()
    for joint, meshes in JOINT_MESHES.items():
        pivot = joint_frames[joint]
        for mesh_name in meshes:
            mesh = bpy.data.objects[mesh_name]
            world = mesh.matrix_world
            mesh.data.transform(pivot.inverted() @ world)
            mesh.matrix_parent_inverse = Matrix.Identity(4)
            mesh.matrix_basis = Matrix.Identity(4)
            snap_world(mesh, pivot)
    check_verts_unchanged(verts, "mesh re-origin")


def remove_empties() -> None:
    for obj in list(bpy.data.objects):
        if obj.type == "EMPTY":
            bpy.data.objects.remove(obj, do_unlink=True)


def configure_materials() -> None:
    """配置像素皮肤预览，并清理导入器留下的失效贴图引用。"""
    for mat in bpy.data.materials:
        # 皮肤外层含有大量透明像素。混合透明既依赖排序，又会绘制透明背面，
        # 特定视角下因此能透过外侧表面看到另一侧；抖动透明与 Minecraft
        # 的镂空皮肤更匹配，关闭透明背面后只显示朝向观察者的表面。
        mat.use_backface_culling = True
        mat.show_transparent_back = False
        mat.surface_render_method = "DITHERED"
        if mat.node_tree is None:
            continue
        for node in mat.node_tree.nodes:
            if node.type != "TEX_IMAGE":
                continue
            node.interpolation = "Closest"
            img = node.image
            if img is not None and not img.packed_file:
                path = bpy.path.abspath(img.filepath)
                if not Path(path).is_file():
                    node.image = None
    for img in list(bpy.data.images):
        if img.users == 0:
            bpy.data.images.remove(img)


def check_materials() -> None:
    """确保所有玩家网格都使用不会发生透明背面穿透的材质。"""
    materials = {
        mat
        for mesh_names in JOINT_MESHES.values()
        for mesh_name in mesh_names
        for mat in bpy.data.objects[mesh_name].data.materials
        if mat is not None
    }
    if not materials:
        raise RuntimeError("materials: player meshes have no materials")
    for mat in materials:
        if not mat.use_backface_culling:
            raise RuntimeError(f"materials: {mat.name} must cull back faces")
        if mat.show_transparent_back:
            raise RuntimeError(f"materials: {mat.name} shows transparent back faces")
        if mat.surface_render_method != "DITHERED":
            raise RuntimeError(f"materials: {mat.name} must use DITHERED transparency")
        if mat.node_tree is None:
            raise RuntimeError(f"materials: {mat.name} has no shader nodes")
        for node in mat.node_tree.nodes:
            if node.type == "TEX_IMAGE" and node.interpolation != "Closest":
                raise RuntimeError(f"materials: {mat.name} texture filtering must be Closest")


def bone_style(arm: bpy.types.Object) -> None:
    try:
        arm.data.show_names = True
    except AttributeError:
        pass
    for name, theme in BONE_COLORS.items():
        bone = arm.data.bones.get(name)
        pb = arm.pose.bones.get(name)
        for holder in (bone, pb):
            if holder is not None and hasattr(holder, "color"):
                try:
                    holder.color.palette = theme
                except (AttributeError, TypeError):
                    pass


def setup_animation_workspace() -> None:
    bpy.context.scene.tool_settings.use_keyframe_insert_auto = True
    try:
        bpy.context.window.workspace = bpy.data.workspaces["Animation"]
    except (AttributeError, TypeError):
        pass  # 后台上下文无窗口时保持默认


def check_orientation() -> None:
    bpy.context.view_layer.update()
    cape = bpy.data.objects["Cape_2"]
    cape_pts = [cape.matrix_world @ Vector(c) for c in cape.bound_box]
    cape_y = sum(p.y for p in cape_pts) / len(cape_pts)
    if cape_y < 0.05:
        raise RuntimeError(f"orientation: cape on the front side (y={cape_y})")
    arm = bpy.data.objects["Left_Arm_2"]
    arm_pts = [arm.matrix_world @ Vector(c) for c in arm.bound_box]
    arm_x = sum(p.x for p in arm_pts) / len(arm_pts)
    if arm_x < 0:
        raise RuntimeError(f"orientation: Left_Arm on -x (x={arm_x}); rig is flipped")


def main() -> int:
    argv = sys.argv[sys.argv.index("--") + 1:]
    root = Path(argv[0]).resolve()
    src = root / "PetLib/rust/src/assets/classic-player.glb"
    out = root / "PetLib/tools/rig/rig.blend"

    bpy.ops.wm.read_factory_settings(use_empty=True)
    bpy.context.scene.render.fps = 60  # 采样器按 60fps 映射时间
    bpy.context.scene.frame_start = 1
    bpy.context.scene.frame_end = 250
    bpy.ops.import_scene.gltf(filepath=str(src))

    snap = snapshot_world()
    remove_artifacts()
    check_world_unchanged(snap, "artifact removal")

    remove_empty_actions()

    for action in list(bpy.data.actions):
        bpy.data.actions.remove(action)
    reset_stance()
    check_stance()

    joint_frames = {n: bpy.data.objects[n].matrix_world.copy() for n in BONES}
    root_world = bpy.data.objects["blockbench_export"].matrix_world.copy()

    arm = build_armature(root_world)
    reparent_meshes(arm, joint_frames)
    remove_empties()
    bone_style(arm)
    configure_materials()
    check_materials()

    setup_animation_workspace()

    check_orientation()
    check_stance()

    if bpy.data.actions:
        raise RuntimeError("rig must not ship any clips")

    out.parent.mkdir(parents=True, exist_ok=True)
    bpy.ops.wm.save_as_mainfile(filepath=str(out))
    print(f"saved {out}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
