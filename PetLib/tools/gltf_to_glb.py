#!/usr/bin/env python3
"""
把内嵌 data URI 的 .gltf 无损打包为 GLB 二进制容器。

用法：
python gltf_to_glb.py [输入.gltf ...]（缺省转换 models/ 下全部 .gltf）
"""

import base64
import json
import struct
import sys
from pathlib import Path

TOOLS_DIR = Path(__file__).resolve().parent
MODELS_DIR = TOOLS_DIR / "models"

GLB_MAGIC = 0x46546C67  # "glTF"
CHUNK_JSON = 0x4E4F534A  # "JSON"
CHUNK_BIN = 0x004E4942  # "BIN"


def pad4(data: bytes, fill: bytes) -> bytes:
    return data + fill * (-len(data) % 4)


def convert(path: Path) -> None:
    doc = json.loads(path.read_text(encoding="utf-8"))
    buffers = doc.get("buffers", [])
    if len(buffers) != 1:
        raise SystemExit(f"{path.name}: only single-buffer files are supported")
    uri = buffers[0].get("uri", "")
    if not uri.startswith("data:"):
        raise SystemExit(f"{path.name}: only embedded data URI buffers are supported")
    binary = base64.b64decode(uri.split(",", 1)[1])
    # BIN chunk 取代 data URI：从 JSON 中移除 uri，指向容器自带 buffer
    del buffers[0]["uri"]

    out_path = path.with_suffix(".glb")
    json_bytes = pad4(json.dumps(doc, ensure_ascii=False, separators=(",", ":"))
                      .encode("utf-8"), b" ")
    bin_bytes = pad4(binary, b"\x00")
    total = 12 + 8 + len(json_bytes) + 8 + len(bin_bytes)
    with out_path.open("wb") as f:
        f.write(struct.pack("<III", GLB_MAGIC, 2, total))
        f.write(struct.pack("<I", len(json_bytes)))
        f.write(CHUNK_JSON.to_bytes(4, "little"))
        f.write(json_bytes)
        f.write(struct.pack("<I", len(bin_bytes)))
        f.write(CHUNK_BIN.to_bytes(4, "little"))
        f.write(bin_bytes)
    print(f"{path.name} -> {out_path.name} ({total} bytes)")


def main() -> None:
    inputs = [Path(a) for a in sys.argv[1:]] or sorted(MODELS_DIR.glob("*.gltf"))
    for path in inputs:
        convert(path)


if __name__ == "__main__":
    main()
