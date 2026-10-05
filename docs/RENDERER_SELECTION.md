# Renderer selection

Aerix ships several OpenGL wrappers. A wrapper translates the desktop OpenGL calls Minecraft
makes into whatever the device actually offers (OpenGL ES, or Vulkan through Mesa). Choosing the
wrong one is the single most common reason a version fails to start, so the launcher picks one
for you and lets you override it per instance.

## The wrappers

| Renderer | Library | Translates | Minecraft versions |
| --- | --- | --- | --- |
| **LTW Legacy** | `libltwlegacy.so` | OpenGL 1.x/2.1 → OpenGL ES 2/3 | 1.8 – 1.16.5 |
| **LTW** | `libltw.so` | OpenGL 3.2 core → OpenGL ES 3 | 1.17+ |
| GL4ES | `libgl4es_114.so` | OpenGL 1.x/2.1 → OpenGL ES 2 | fallback for 1.8 – 1.21.4 |
| Krypton Wrapper | `libng_gl4es.so` | OpenGL 3.1 → OpenGL ES 3 | fallback |
| Kopper Zink | `libglxshim.so` | OpenGL → Vulkan through Mesa | fallback |
| VirGL | `libOSMesa_2121.so` | OpenGL through Mesa on a virtual GPU | fallback |
| Freedreno / Panfrost | `libOSMesa_8.so` / `libOSMesa_2300d.so` | Mesa on Adreno / Mali | fallback |

`LTW` and `LTW Legacy` are both built from source in this repository, for every ABI. See
[`third_party/LTW/UPSTREAM.md`](../third_party/LTW/UPSTREAM.md) and
[`third_party/LTWVLegacy/UPSTREAM.md`](../third_party/LTWVLegacy/UPSTREAM.md).

## Why there are two LTW renderers

The split follows Minecraft's own OpenGL profile change, not a version number someone chose:

- **1.8 – 1.16.5** drive OpenGL 1.x/2.1 with fixed-function state (`glAlphaFunc`,
  `glShadeModel`, the matrix stack) and the legacy client-array draw path.
- **1.17 and newer** require the OpenGL 3.2 **core** profile, which removes all of that.

LTW implements the core profile and has no fixed-function emulation, so it cannot run the older
versions. LTW Legacy implements the legacy pipeline, and is not needed once the game uses the
core profile. Neither can stand in for the other.

GL4ES covers the same ground as LTW Legacy, and that is expected — both are GL 1.x/2.1
translators. LTW Legacy is the maintained, from-source build; GL4ES stays selectable as a
fallback for devices where the newer upstream revision regresses.

## How the launcher chooses

Selection lives in `game/renderer/RendererPicker.kt`. It reads each renderer's declared
compatibility window (`getMinMCVersion` / `getMaxMCVersion`) rather than hardcoding one, so a
renderer only has to declare its own range to take part:

1. **Your explicit choice wins.** An instance's configured renderer is used as long as it is
   loaded *and* declares support for the version being launched.
2. **Otherwise the version decides.**
   - 1.16.5 and older → LTW Legacy, falling back to GL4ES, then VirGL.
   - 1.17 and newer → LTW, falling back to Zink.
3. **If nothing declares support**, the first loaded wrapper is used and the choice is logged
   with the reason, rather than refusing to launch.

Every decision is written to the launch log with its reason, so a surprising choice can always
be traced:

```
Renderer: LTW Legacy
Renderer Summary: OpenGL 1.x/2.1 translation layer for Minecraft 1.8 - 1.16.5, built from source.
```

The user-facing renderer list is built from `Renderers.BUILT_IN`, which is the single source of
truth for which wrappers ship with the launcher.

## What a wrapper is not

Wrappers and performance/visual mods live on different layers, and it is worth being precise
about this:

- **Sodium, Lithium, Phosphor, Iris, OptiFine** are Minecraft mods. They run inside the game and
  call whichever wrapper is loaded. A wrapper cannot replace them, and running Sodium on top of
  LTW or LTW Legacy is the normal arrangement, not a conflict.
- **Chunk meshing, occlusion culling, LOD, dynamic resolution and GPU-driven rendering** are
  implemented inside the game or by those mods. A launcher cannot add them to arbitrary
  Minecraft versions.
- **Shader packs** are executed by the game through Iris/OptiFine/Canvas. The wrapper only
  provides the GL entry points those mods need, such as the core-profile and extension surface
  that LTW exposes.

So Aerix does not claim "Sodium parity" or "Iris parity" for any wrapper. What the wrappers are
responsible for is faithfully exposing the OpenGL surface each Minecraft version and its mods
expect — and that is what the version ranges above are about.

## Testing a wrapper

Renderer behaviour is covered by JVM unit tests that run in CI:

```
./gradlew MiraiLauncher:testDebugUnitTest \
  --tests "com.movtery.zalithlauncher.game.renderer.*"
```

`LTWRendererTest` and `LTWLegacyRendererTest` assert each wrapper's identity, library name and
environment, including the invariants the native bridge depends on. `RendererPickerTest` pins
the selection rules, including the 1.16.5 → 1.17 boundary and the rejection of an override that
the target version does not support.

`.github/scripts/verify_ltw_apk.py` separately asserts that both `libltw.so` and
`libltwlegacy.so` are present in a built APK and really are shared objects for each requested
ABI, so a packaging regression fails the build instead of reaching a device.

See [`RENDERER_VALIDATION.md`](RENDERER_VALIDATION.md) for the on-device procedure and the
results template.
