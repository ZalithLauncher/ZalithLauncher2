# LTW Legacy — provenance and fork notes

`LTW Legacy` is Mirai's legacy OpenGL compatibility renderer for Minecraft **1.8 through 1.16.4**.

It is built from a vendored snapshot of **GL4ES**, patched and packaged by Mirai as a single
native library, `libltwlegacy.so`.

## Upstream

| Field | Value |
| --- | --- |
| Project | GL4ES — OpenGL 1.x/2.x to OpenGL ES translation layer |
| Upstream | <https://github.com/ptitSeb/gl4es> |
| Upstream commit | `a444cc94b17c672c66c3e6ce07428dc603034db1` (2026-09-28) |
| Upstream version | 1.1.7 (`version.h`) |
| License | MIT — see [`LICENSE`](LICENSE) |
| Vendored on | 2026-10-03 |

## Why this exists

Before this module, Mirai's legacy (1.8–1.16.4) renderer path depended on
`MiraiLauncher/src/main/jniLibs/*/libgl4es_114.so` — a **prebuilt binary with no source in
tree**. That binary is GL4ES **1.1.4**; it cannot be audited, patched, rebuilt for a new ABI, or
updated, and it silently drifts from the NDK and platform it is linked against.

Vendoring the sources and building from CMake fixes that: the legacy renderer is now
reproducible from source for every ABI, on the pinned upstream revision, and can be patched.

## What was vendored

Only the parts required to build the `libltwlegacy.so` shared library:

```
include/            Khronos GL/EGL/GLES headers plus gl4esinit.h and gl4eshint.h
src/config.h        compile-time limits (MAX_TEX, MAX_STACK_* etc.)
src/gl/             the translation layer itself (including gl/wrap and gl/math)
src/glx/hardext.c   hardware capability detection, used on non-X11 targets
src/glx/*.h         headers that hardext.c includes
src/egl/            EGL wrapper sources, kept for reference (not compiled into the library)
version.h           upstream version macros
LICENSE             upstream MIT license
```

Deliberately **not** vendored, to keep the tree small: `traces/` (46 MB of captured API traces),
`refs/`, `spec/`, `media/`, `tests/`, `debian/`, `Android.mk`, and the upstream top-level
`CMakeLists.txt`. Mirai supplies its own CMake project instead, in
`ltwlegacy/src/main/cpp/CMakeLists.txt`.

## What Mirai changed

1. **Build system.** A dedicated CMake project replaces upstream's multi-platform top-level
   `CMakeLists.txt`. It compiles `src/gl/**` plus `src/glx/hardext.c` and emits a single
   `libltwlegacy.so` with the `ANDROID`, `NOX11`, `NO_GBM` and `DEFAULT_ES=2` defines upstream
   uses for Android targets.
2. **No EGL wrapper.** Upstream can build a second `libEGL.so` shim. Mirai does not use it: the
   launcher loads `libltwlegacy.so` purely as a GL translation layer and lets the platform
   supply EGL, exactly as it already does for the `GL4ES` and `Krypton Wrapper` renderers. This
   avoids a second library competing for `eglGetProcAddress`.
3. **Library name.** `libltwlegacy.so`, so it can coexist with `libgl4es_114.so` and
   `libng_gl4es.so` without clashing.

No behavioural patch to the C sources is applied on top of the pinned commit. The upstream
sources in this directory are unmodified.

## Relationship to the other Mirai renderers

| Renderer | Library | Emulates | Target versions |
| --- | --- | --- | --- |
| LTW | `libltw.so` | OpenGL 3.2 **core** → ES 3 | 1.17+ |
| **LTW Legacy** | `libltwlegacy.so` | OpenGL **1.x/2.1** → ES 2/3 | **1.8 – 1.16.4** |
| GL4ES | `libgl4es_114.so` | OpenGL 1.x/2.1 → ES 2 | fallback, 1.8 – 1.21.4 |
| Krypton Wrapper | `libng_gl4es.so` | OpenGL 3.1 → ES 3 | fallback |

LTW Legacy and GL4ES are the same class of translator, which is expected: a legacy GL 2.1
pipeline cannot be served by LTW's core-profile wrapper, because upstream LTW implements no
fixed-function emulation at all. LTW Legacy is the maintained, from-source build of that legacy
path; GL4ES stays selectable as a fallback for devices where the newer upstream revision
regresses.

## License compliance

GL4ES is MIT licensed. The upstream `LICENSE` file is preserved verbatim at
[`LICENSE`](LICENSE), and upstream copyright notices remain in every source file. MIT is
compatible with Mirai's GPL-3.0 licensing; the combined work is distributed under GPL-3.0 with
the upstream MIT notice retained.
