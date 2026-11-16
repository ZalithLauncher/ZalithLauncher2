"""
校验 rig.blend 的玩家网格材质与基础骨架结构

blender -b rig.blend -P script/validate_rig.py（在 rig/ 目录执行）
"""
import sys
from pathlib import Path

import bpy

sys.path.insert(0, str(Path(__file__).resolve().parent))
from build_rig import BONES, JOINT_MESHES, check_materials


def main() -> int:
    arm = bpy.data.objects.get("blockbench_export")
    if arm is None or arm.type != "ARMATURE":
        raise RuntimeError("rig: blockbench_export armature is missing")
    missing_bones = [name for name in BONES if name not in arm.data.bones]
    if missing_bones:
        raise RuntimeError(f"rig: missing bones: {missing_bones}")
    missing_meshes = [
        name for names in JOINT_MESHES.values() for name in names
        if bpy.data.objects.get(name) is None
    ]
    if missing_meshes:
        raise RuntimeError(f"rig: missing meshes: {missing_meshes}")
    check_materials()
    print(f"OK: {len(BONES)} bones, player materials use DITHERED without transparent backs")
    return 0


if __name__ == "__main__":
    sys.exit(main())
