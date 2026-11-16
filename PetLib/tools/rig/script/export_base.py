"""
从 rig.blend 导出"基准 GLB"

blender -b --factory-startup rig.blend -P script/export_base.py -- <repo-root> <文件名.glb>（在 rig/ 目录执行）
"""
import sys
from pathlib import Path

import bpy


def main() -> int:
    argv = sys.argv[sys.argv.index("--") + 1:]
    root = Path(argv[0]).resolve()
    name = argv[1] if len(argv) > 1 else "rig-base.glb"
    out = root / "PetLib/tools/rig/out" / name
    out.parent.mkdir(parents=True, exist_ok=True)

    bpy.ops.export_scene.gltf(
        filepath=str(out),
        export_format="GLB",
        export_animation_mode="ACTIONS",
        export_image_format="NONE",
        export_yup=True,
        export_apply=False,
        export_cameras=False,
        export_lights=False,
        export_extras=False,
        export_skins=False,
        export_morph=False,
        use_visible=True,
        export_optimize_animation_size=False,
    )
    print(f"exported base {out}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
