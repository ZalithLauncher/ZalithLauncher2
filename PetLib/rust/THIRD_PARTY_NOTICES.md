# Third-party notices

本 crate 为 [Minecraft-Desktop-Pet](https://github.com/DLmember/Minecraft-Desktop-Pet)
渲染核心的移植与改造版本，原项目以 MIT 协议分发，其许可全文见同目录
[LICENSE](LICENSE)

## 依赖

| 组件 | 许可 | 说明                                                                                                  |
| --- | --- |-----------------------------------------------------------------------------------------------------|
| glam | MIT / Apache-2.0 | 线性代数                                                                                                |
| image | MIT / Apache-2.0 | PNG 解码，其依赖链（png、flate2、miniz_oxide、fdeflate、crc32fast、simd-adler32、adler2、zlib-rs 等）各按其自身许可分发       |
| skinview3d | MIT | Walking 动画与 slim 臂型检测的数学来源（[animation.ts / process.ts](https://github.com/bs-community/skinview3d)） |
