# Changelog

All notable changes to Mirai Launcher are recorded here.

Versions follow the `launcher_version_code` / `launcher_version_name` pair in
`ZalithLauncher/gradle.properties`.

## Unreleased

### Added

- **LTW Legacy renderer** (`libltwlegacy.so`) — a new wrapper covering Minecraft **1.8 – 1.16.5**,
  the versions that drive OpenGL 1.x/2.1. It is built from a vendored, pinned snapshot of GL4ES
  (version 1.1.7, commit `a444cc94`) in `third_party/LTWVLegacy`, for every ABI. See
  [`third_party/LTWVLegacy/UPSTREAM.md`](third_party/LTWVLegacy/UPSTREAM.md).
- `Renderers.BUILT_IN` — the single source of truth for which renderers ship with the launcher.
  Both `Renderers.init()` and the selection logic now read from it.
- `RendererPicker.resolve()` — the entry point the launch path uses to pick a wrapper.
- `LTWLegacyRendererTest`, and new `RendererPickerTest` cases for the 1.16.5 → 1.17 boundary.
- [`docs/RENDERER_VALIDATION.md`](docs/RENDERER_VALIDATION.md) — the on-device test procedure
  and results template.
- `CHANGELOG.md` (this file).

### Changed

- **The legacy GL path is now built from source.** `libgl4es_114.so` remains in `jniLibs` for
  the GL4ES renderer, but the LTW Legacy wrapper is compiled from the vendored sources by the
  `:ltwlegacy` Gradle module instead of shipping another prebuilt blob.
- **Automatic wrapper selection is now actually wired into the launch path.** `RendererPicker`
  previously had no production caller, so an instance with no renderer configured fell back to
  the first loaded renderer for every Minecraft version. The launch path now asks the picker
  first, and an explicit per-instance choice still wins.
- `RendererPicker` no longer hardcodes renderer identifiers or version boundaries. It reads each
  renderer's declared `getMinMCVersion()` / `getMaxMCVersion()`, so a renderer cannot drift out
  of sync with its own compatibility window.
- An override that the target Minecraft version does not support is now rejected instead of
  being honoured and then failing the launcher's own support check moments later.
- `GameLauncher.setRendererEnv` no longer applies the Zink/Mesa overrides to `LTWLegacyRenderer`,
  which would have loaded a second GL implementation alongside `libltwlegacy.so`.

### Verified in CI

- `verify_ltw_apk.py` now checks **both** `libltw.so` and `libltwlegacy.so` for every requested
  ABI, and is wired into the release workflow as well as the normal build.
- The renderer unit tests that CI runs now include `LTWLegacyRendererTest` and
  `RendererPickerTest`; the latter was never executed by CI before.

### Not yet established

These are deliberately **not** claimed in this release, because they require a physical Android
device running the game and have not been measured:

- Performance (FPS, 1% lows, memory, startup time, chunk load speed) relative to GL4ES, LTW or
  any third-party renderer.
- Mod, resource pack and shader pack compatibility matrices.
- Whether the newer upstream GL4ES revision behaves identically to the 1.1.4 build it
  supersedes on legacy versions.

See [`docs/RENDERER_VALIDATION.md`](docs/RENDERER_VALIDATION.md) for how to produce those
results.

### Notes

- LTW Legacy is derived from GL4ES, which is MIT licensed; the upstream license and copyright
  notices are preserved in `third_party/LTWVLegacy`.
- No launcher version number was bumped by this change. Tag a release when the on-device
  validation above has been run.
