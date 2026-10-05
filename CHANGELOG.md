# Changelog

All notable changes to Aerix Launcher are recorded here.

Versions follow the `launcher_version_code` / `launcher_version_name` pair in
`MiraiLauncher/gradle.properties`.

## 1.0.0 - 2026-10-04

First public release.

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
- **MobileGlues renderer** (`libmobileglues.so`) — a modern OpenGL-on-GLES wrapper by
  MobileGL-Dev covering Minecraft **1.17 – 26.3**, bundled for all four ABIs. The launcher
  writes its tuned MG-ES `config.json` at startup (error checking off for the fastest path,
  upstream defaults otherwise), keeps it out of the Zink/Mesa env forcing, adds it to the
  auto-picker's modern order (LTW → MobileGlues → Zink), and gives it a FASTEST quick-pick
  card in renderer settings.
- **VGPU renderers** (`libvgpu.so`, `libvgpu_1368.so`) — Pojav Glow-Worm vgpu and VGPU 1.3.6β
  options for Minecraft **1.16.5 and older**, bundled for all four ABIs with Sodium and shader
  support, plus picker tests.
- **1-Tap FPS Booster** — one tap applies the full mobile FPS preset; the selection persists
  across restarts.
- **JRE & GC auto-tuner** — automatically tunes the Java runtime and garbage collector for the
  device; the selection persists across restarts.
- **Smart Crash Doctor** — reads a crash log and suggests the fix.
- **Smart Mod Dependency & Conflict Resolver** — resolves missing mod dependencies and
  conflicts automatically.
- **Interface overhaul** — redesigned launcher screens plus dedicated landscape layouts with
  animations, a new home dashboard with quick-action pills, a streamlined navigation rail, a
  Discover category filter, and a dynamic theme that follows the wallpaper.
- **20 HD Minecraft wallpapers** built in, with dim control, Solid Dark mode and custom imports.
- **Rebuilt download pages** — hero card, screenshots rail directly under the hero, and
  auto-detection that reads a file's MC versions/loaders, shows a Detected line and pre-checks
  compatible installed versions.
- **Full Minecraft version catalog in Discover** — the MC filter dropdown now lists all 69
  modding-relevant releases from 26.3 back to 1.4.7 (newest first, patch-level), replacing
  the old 14-item capped list. Versions seen in results stay pinned on top.
- **Minimizable modpack install dialog** — the Installing Modpack window has a Minimize button
  (left of Cancel) that shrinks it to a small floating reopen button while the install keeps
  running in the background.
- **In-game Aerix Pill HUD** and world/screenshot quick actions.
- **Interactive 3D paper doll** — drag to spin 360°, tap to open account management.

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
- **Platform search now falls back sequentially across sources** instead of racing them
  concurrently: each source gets a 15s budget inside a 30s overall cap, and a total failure
  reports every dead source by name instead of a bogus error.
- **Wallpaper blur is now a pre-rendered static image on all Android versions** instead of a
  live fullscreen GPU blur, removing the per-frame cost that janked tab switches, scrolling
  and dialogs. The frosted look is unchanged.
- **Settings and wallpaper backgrounds now decode off the UI thread** and share a
  process-wide cached bitmap instead of re-decoding the JPEG on every visit.
- The auto-picker's modern order is now LTW → MobileGlues → Zink, and the Zink/Mesa env
  forcing is skipped for MobileGlues the same way it already was for the other
  self-contained GLES wrappers.

### Fixed

- **`GameVersionNumber` could not be used in a plain JVM unit test.** Its static initialiser
  opens the version list with `Class.getResourceAsStream("/assets/game/versions.txt")`. On a
  device the class loader finds that file inside the APK, but a JVM test has no APK, the stream
  came back `null`, and the initialiser threw. Every later call to compare two game versions
  then failed with `NoClassDefFoundError`. A missing stream now degrades to an empty version
  list, which is a no-op on a device and makes the version-comparison stack testable off it.
  This is why `RendererPickerTest` could never run in CI before.
- A failing unit test reported only "Process completed with exit code 1".
  `.github/scripts/publish_unit_test_failures.py` now publishes each failing test, its assertion
  message and its stack trace as check annotations, and falls back to the Gradle log when the
  test source set fails to compile.
- **Discover search failed with "This job has not completed yet"** on Modrinth and CurseForge.
  Concurrent requests through the shared HTTP client produced the spurious failure; the
  sequential fallback above fixes it.
- **CurseForge outages no longer kill search** — requests fall back to a mirror backup source.
- **Sodium / Embedium / Rubidium crashed instantly after joining a world** on every renderer
  except Krypton Wrapper. The launcher now applies the crash-free chunk-backend patch
  (`use_chunk_multidraw = false`, safe `GL30` backend) to all three mods' config files on
  **every** renderer — detected from the installed mods, covering both existing configs and
  first-run defaults — so 1.16.5 + Sodium-family mods runs anywhere.
- **Visual lag when opening any tab** is fixed by the pre-rendered blur and cached background
  decoding described above.
- **Startup/animation jank** from the JellyBounce animation and software bitmap path is gone.

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
- MobileGlues is LGPL-2.1 licensed by MobileGL-Dev; only its prebuilt native libraries are
  bundled, unmodified.
- Released as **1.0.0** (version code 200043, kept monotonic with pre-release builds).
