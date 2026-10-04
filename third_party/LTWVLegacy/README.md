# LTW Legacy

Mirai's legacy OpenGL renderer for Minecraft **1.8 through 1.16.4**.

LTW Legacy translates the OpenGL 1.x/2.1 pipeline that these Minecraft versions use onto the
device's OpenGL ES 2.0/3.x driver, producing a single shared library:

```
libltwlegacy.so
```

It is the legacy counterpart to [`../LTW`](../LTW/README-upstream.md), which handles the OpenGL
3.2 **core** profile used by Minecraft 1.17 and newer. The two do not overlap: upstream LTW
implements no fixed-function emulation, so it cannot serve the older versions, and LTW Legacy's
GL 2.1 translator is not needed once Minecraft moves to the core profile.

## Provenance

Built from a vendored snapshot of [GL4ES](https://github.com/ptitSeb/gl4es) (MIT), pinned at
commit `a444cc94b17c672c66c3e6ce07428dc603034db1`, version 1.1.7. Full details, including what was
vendored, what Mirai changed, and license compliance, are in [`../UPSTREAM.md`](../UPSTREAM.md).

## Building

The library is built by the `:ltwlegacy` Gradle module as part of the normal APK build. There is
no separate step:

```
./gradlew MiraiLauncher:assembleDebug
```

To build only the native library:

```
./gradlew :ltwlegacy:assembleDebug
```

The Android CMake project is at `ltwlegacy/src/main/cpp/CMakeLists.txt`. It compiles the vendored
sources with the same defines upstream GL4ES uses for Android targets and emits
`libltwlegacy.so` for every ABI in the build.

`.github/scripts/verify_ltw_apk.py` asserts that the library is present and is a valid ELF shared
object for each requested ABI, so a packaging regression fails CI rather than shipping.

## Requirements

- Android 8.0+ (minSdk 26 in the app; the library itself declares minSdk 21).
- An OpenGL ES 2.0 driver, which every supported device has. On ES 3.x devices the wrapper can
  raise its feature level at runtime through `LIBGL_ES`.

## Runtime configuration

LTW Legacy honours the standard GL4ES hints. Mirai sets defaults for Minecraft in
`GameLauncher.setRendererEnv`, and an instance can override any of them. The most useful ones:

| Variable | Meaning |
| --- | --- |
| `LIBGL_ES` | Target OpenGL ES level (`2` or `3`). |
| `LIBGL_GL` | OpenGL version reported to the game. |
| `LIBGL_MIPMAP` | Mipmap generation mode. |
| `LIBGL_SHRINK` | Texture shrinking for low-memory devices. |
| `LIBGL_AVOID16BITS` / `LIBGL_AVOID24BITS` | Pixel format workarounds. |
| `LIBGL_BATCH` | Batched drawing. |
| `LIBGL_NOBANNER` | Suppress the startup banner. |
| `LIBGL_LOGSHADERERROR` | Log shader conversion failures. |

The full list is in [`include/gl4eshint.h`](../include/gl4eshint.h) and in upstream's `USAGE.md`.
