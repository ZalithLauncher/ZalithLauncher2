#!/usr/bin/env python3
"""从 Modrinth 玩家模型 GLTF 生成剪辑数据(rust/src/clip_data.rs)。

只依赖标准库。键值为 GLTF 原始数据:四元数直采,平移按
1 GLTF 单位 = 16 模型像素缩放(基变换由渲染端的根共轭承担)。
"""

import base64
import json
import struct
from pathlib import Path

TOOLS_DIR = Path(__file__).resolve().parent
GLTF_PATH = TOOLS_DIR / "models" / "classic-player.gltf"
OUT_PATH = TOOLS_DIR.parent / "rust" / "src" / "clip_data.rs"

PIXELS_PER_UNIT = 16.0

# GLTF 动画名 → (Rust 静态名, 文档注释)
CLIPS = [
    ("idle", "CLIP_IDLE", "基础待机循环"),
    ("idle_sub_1", "CLIP_SUB_1", "随机待机子动画 idle_sub_1"),
    ("idle_sub_2", "CLIP_SUB_2", "随机待机子动画 idle_sub_2"),
    ("idle_sub_3", "CLIP_SUB_3", "随机待机子动画 idle_sub_3"),
    ("interact", "CLIP_INTERACT", "点击反应动画"),
]

# GLTF 节点名 → Joint 变体(列表顺序即输出顺序)
JOINTS = [
    ("Body", "Body"),
    ("Head", "Head"),
    ("Right_Arm", "ArmRight"),
    ("Left_Arm", "ArmLeft"),
    ("Right_Leg", "LegRight"),
    ("Left_Leg", "LegLeft"),
    ("Cape", "Cape"),
]


def fmt(value: float) -> str:
    if abs(value) < 5e-7:
        value = 0.0
    text = f"{value:.6f}".rstrip("0").rstrip(".")
    if text in ("", "-"):
        text = "0"
    if "." not in text:
        text += ".0"
    return text


def read_accessor(gltf: dict, buffer: bytes, index: int) -> list:
    acc = gltf["accessors"][index]
    assert acc["componentType"] == 5126, "仅支持 float32 通道"
    n = {"SCALAR": 1, "VEC3": 3, "VEC4": 4}[acc["type"]]
    view = gltf["bufferViews"][acc["bufferView"]]
    offset = view.get("byteOffset", 0) + acc.get("byteOffset", 0)
    stride = view.get("byteStride") or 4 * n
    out = []
    for i in range(acc["count"]):
        out.append(struct.unpack_from(f"<{n}f", buffer, offset + i * stride))
    return out


def load_gltf() -> dict:
    gltf = json.loads(GLTF_PATH.read_text(encoding="utf-8"))
    uri = gltf["buffers"][0]["uri"]
    assert uri.startswith("data:"), "缓冲区须为内嵌 data URI"
    gltf["_buffer"] = base64.b64decode(uri.split(",", 1)[1])
    return gltf


def extract_clip(gltf: dict, name: str) -> dict:
    """返回 {节点名: {"rotation": [...], "translation": [...]}}"""
    anim = next(a for a in gltf["animations"] if a["name"] == name)
    channels: dict = {}
    for channel in anim["channels"]:
        target = channel["target"]
        if target["path"] not in ("rotation", "translation"):
            continue
        sampler = anim["samplers"][channel["sampler"]]
        keys = read_accessor(gltf, gltf["_buffer"], sampler["output"])
        times = read_accessor(gltf, gltf["_buffer"], sampler["input"])
        assert len(times) == len(keys), f"{name}: time and value key count mismatch"
        step = times[1][0] - times[0][0] if len(times) > 1 else 0.0
        if len(times) > 1:
            assert abs(step - 1.0 / 30.0) < 1e-4, f"{name}: key spacing {step} off the 30fps grid"
        entry = channels.setdefault(gltf["nodes"][target["node"]]["name"], {})
        entry[target["path"]] = keys
    return channels


def rust_rotation(keys: list) -> str:
    quat = (f"[{fmt(x)}, {fmt(y)}, {fmt(z)}, {fmt(w)}]" for x, y, z, w in keys)
    return "&[" + ", ".join(quat) + "]"


def rust_translation(keys: list) -> str:
    px = (f"[{fmt(x * PIXELS_PER_UNIT)}, {fmt(y * PIXELS_PER_UNIT)}, {fmt(z * PIXELS_PER_UNIT)}]"
          for x, y, z in keys)
    return "&[" + ", ".join(px) + "]"


def emit_channel(joint: str, data: dict) -> str:
    rotation = rust_rotation(data["rotation"]) if "rotation" in data else "&[]"
    translation = rust_translation(data["translation"]) if "translation" in data else "&[]"
    return (
        "        Channel {\n"
        f"            joint: Joint::{joint},\n"
        f"            rotation: {rotation},\n"
        f"            translation: {translation},\n"
        "        },"
    )


def emit_clip(anim_name: str, static_name: str, doc: str, channels: dict) -> str:
    body = "\n".join(
        emit_channel(joint, channels[node])
        for node, joint in JOINTS
        if node in channels
    )
    return (
        f"/// {doc}\n"
        "#[rustfmt::skip]\n"
        f"pub(crate) static {static_name}: Clip = Clip {{\n"
        "    channels: &[\n"
        f"{body}\n"
        "    ],\n"
        "};"
    )


HEADER = """\
//! Modrinth 启动器玩家模型关键帧动画数据。
//!
//! 本文件由 tools/gen_clip_data.py 从 tools/models/classic-player.gltf 生成,勿手动编辑。
//! 参考 https://github.com/modrinth/code/tree/main/packages/assets/models(GPL-3)
"""

TYPES = """\
/// 关键帧采样率(每秒帧数)
pub(crate) const CLIP_FPS: f32 = 30.0;

#[derive(Clone, Copy, PartialEq, Eq, Debug)]
pub(crate) enum Joint {
    Body,
    Head,
    ArmLeft,
    ArmRight,
    LegLeft,
    LegRight,
    Cape,
}

#[derive(Clone, Copy)]
pub(crate) struct Channel {
    pub joint: Joint,
    /// 旋转四元数键帧 (x, y, z, w),GLTF 原始键
    pub rotation: &'static [[f32; 4]],
    /// 平移键帧(像素,GLTF 单位 × 16);空表示无通道
    pub translation: &'static [[f32; 3]],
}

#[derive(Clone, Copy)]
pub(crate) struct Clip {
    pub channels: &'static [Channel],
}
"""

FOOTER = """\
/// 随机待机子动画集合
pub(crate) static CLIP_SUBS: &[Clip] = &[CLIP_SUB_1, CLIP_SUB_2, CLIP_SUB_3];
"""


def main() -> None:
    gltf = load_gltf()
    parts = [HEADER, TYPES]
    for anim_name, static_name, doc in CLIPS:
        parts.append(emit_clip(anim_name, static_name, doc, extract_clip(gltf, anim_name)))
    parts.append(FOOTER)
    OUT_PATH.write_text("\n".join(parts) + "\n", encoding="utf-8", newline="\n")
    print(f"written {OUT_PATH}")


if __name__ == "__main__":
    main()
