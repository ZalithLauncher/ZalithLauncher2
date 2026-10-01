# Large Thin Wrapper integration

The `ltw/` Android library is vendored from [MojoLauncher/LTW](https://github.com/MojoLauncher/LTW), pinned at upstream commit `11f1b36f701e69731d685e94c6788e60d829ea35` (2026-09-28).

Upstream copyright and source notices are preserved in the vendored files. The upstream project is licensed under LGPL-3.0; see [`LICENSE`](LICENSE) and the upstream [README](README-upstream.md). The unused prebuilt host `glsl_compiler` helper was omitted; the Android CMake build compiles from the preserved sources.

Mirai's renderer adapter lives in `ZalithLauncher/src/main/java/com/movtery/zalithlauncher/game/renderer/renderers/LTWRenderer.kt`. Gradle builds the native `libltw.so` for each requested Android ABI from the upstream CMake sources.
