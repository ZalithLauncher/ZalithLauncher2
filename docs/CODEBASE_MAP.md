# Aerix Launcher — Codebase Map

A full technical map of `entitybrian69-bit/Aerix-launcher`, written from a complete pass over
the tree at commit `71feb68` (branch `Aerix-launcher`, release 1.0.0 / version code 200043).

---

## 1. What this project is

| | |
| --- | --- |
| **Product** | Aerix Launcher — a Minecraft: Java Edition launcher for Android |
| **Upstream** | Fork of [`ZalithLauncher/ZalithLauncher2`](https://github.com/ZalithLauncher/ZalithLauncher2) by MovTery |
| **Package namespace** | `com.movtery.zalithlauncher` (upstream namespace kept) |
| **Release application ID** | `com.entitybrian69.mirailauncher.v2` |
| **Debug application ID** | `com.entitybrian69.mirailauncher.v2.debug` |
| **License** | GPL-3.0 (upstream MIT/other licenses preserved in `third_party/`) |
| **Default branch** | `Aerix-launcher` |
| **Releases** | 1.0.0, published 2026-10-04 |
| **Git history** | A single squashed commit — the fork's full history is not carried in this repo |

The namespace staying as `com.movtery.zalithlauncher` while the application ID changed is
deliberate: it keeps every source file, `R` reference, and upstream patch applicable without a
repo-wide rename.

---

## 2. Size and composition

| Metric | Value |
| --- | --- |
| Tracked files | 2,627 |
| Kotlin files | 944 |
| Java files | 119 |
| Kotlin + Java LOC | ~223,000 |
| C / C++ / header LOC (app `jni/` only) | ~21,000 |
| Vendored C in `third_party/` | ~1.91M LOC (mostly the NIR optimizer inside LTW) |
| Unit tests (`@Test`) | 224 |
| Registered settings | 119 |
| Git-tracked size | ~1 GB working tree, 369 MB `.git` |
| Languages (GitHub) | Kotlin 6.69 MB, Java 2.09 MB, C 1.17 MB, C++ 2 KB, Python 13 KB |

Largest single tracked files include `assets/runtimes/jre-25/universal.tar.xz` (32 MB),
`libs/spirv-cross-natives.aar` (31 MB), and `libOSMesa_2121.so` (18 MB).

---

## 3. Module graph

`settings.gradle.kts` declares ten modules under `rootProject.name = "MiraiLauncher"`:

```
:MiraiLauncher           the Android application — everything user-facing
:ltw                     third_party/LTW/ltw          → libltw.so       (MC 1.17+)
:ltwlegacy               third_party/LTWVLegacy/ltwlegacy → libltwlegacy.so (MC 1.8–1.16.5)
:LWJGL                   version resolution shell for LWJGL natives
  :LWJGL:lwjgl-3.3.3     LWJGL 3.3.3 packaging
  :LWJGL:lwjgl-3.4.1     LWJGL 3.4.1 packaging
  :LWJGL:patches         CursorRegistry / SdlCursorRegistry patches
:LayerController         in-game touch-control layer engine
:CardGrid                instance card grid layout engine
:ColorPicker             colour picker component
:Terracotta              Terracotta multiplayer/VPN bridge (libterracotta.so)
:InputMap                input mapping + keycode translation tables
:Guide                   onboarding/tip overlay system
```

`dependencyResolutionManagement` uses `FAIL_ON_PROJECT_REPOS`, so all repositories are declared
in settings only. Toolchain resolution uses the Foojay convention plugin 0.8.0.

### Support library responsibilities

| Module | LOC | Role |
| --- | --- | --- |
| `MiraiLauncher` | ~173,000 | The whole launcher |
| `LayerController` | 7,308 | Draggable/resizable on-screen controls, snap, layers |
| `CardGrid` | 3,425 | Grid geometry, resize edges, auto-scroll, perf-tested engine |
| `InputMap` | 2,438 | GLFW/LWJGL2/Minecraft keycode mapping |
| `Guide` | 2,208 | Placement solver, registry, tip scheduling |
| `ColorPicker` | 659 | HSV/alpha pickers with gesture handling |
| `Terracotta` | 499 | Terracotta session bridge |

---

## 4. Build system

**`MiraiLauncher/build.gradle.kts` (318 lines)** — the heart of the build.

| Setting | Value |
| --- | --- |
| `compileSdk` | 37 (minor API 2) |
| `minSdk` | 26 (Android 8.0) |
| `targetSdk` | 34 |
| Java / Kotlin target | JVM 17 |
| Kotlin opt-in | `ExperimentalMaterial3Api` |
| NDK | `25.2.9519653` (app), `28.2.13676358` (ltw / ltwlegacy) |
| Gradle | 9.5.0 |
| Gradle JVM args | `-Xmx4g -XX:MaxMetaspaceSize=512m` |
| Native build | `ndkBuild` via `src/main/jni/Android.mk` |
| Packaging | `useLegacyPackaging = true`, `pickFirsts += "**/libbytehook.so"` |
| Desugaring | enabled (for `sora-editor` textmate) |
| R8 | `isMinifyEnabled` + `isShrinkResources` on release |

### Build variants and ABI splits

`-Darch=` drives everything: `all` (default), `arm`, `arm64`, `x86`, `x86_64`. Five APKs are
produced. Two `afterEvaluate` hooks trim the merged asset tree:

1. **JRE cleanup** — for a single-arch build, delete every `runtimes/jre-*/` entry that is not
   `version`, `*universal*`, or `bin-<arch>.tar.xz`.
2. **LWJGL natives cleanup** — delete every non-target ABI directory under
   `app_runtime/lwjgl/<ver>/natives/`.

Output filenames are `Aerix.Launcher-<version>[-<abi>].apk` (spaces replaced by dots so the
update URLs resolve — the script layer explicitly rejects names containing spaces).

### Version and branding source of truth

`MiraiLauncher/gradle.properties`:

```
launcher_name=Aerix Launcher
launcher_app_name=Aerix Launcher
launcher_short_name=Aerix
url_home=https://github.com/entitybrian69-bit/Aerix-launcher
launcher_version_code=200043
launcher_version_name=1.0.0
```

These are surfaced into the app as `BuildKeys` constants (`LAUNCHER_NAME`,
`LAUNCHER_SHORT_NAME`, `LAUNCHER_IDENTIFIER`, `URL_HOME`, `OAUTH_CLIENT_ID`,
`CURSEFORGE_API`, `BUILD_ARCH`) by the `com.movtery.buildkeys` plugin. Secrets come from
environment variables first, then from gitignored files (`.store_password.txt`,
`.key_password.txt`, `.oauth_client_id.txt`, `.curseforge_api.txt`).

---

## 5. Runtime architecture

### Entry points

```
SplashActivity   → unpacks JRE + components, handles import intents
    ↓
MainActivity     → navigation backstack, game launch lifecycle
    ↓
MainScreen       → MiraiNavigationRail + content area
```

`ZLApplication` (`@HiltAndroidApp`) initialises, in order: `GlobalContext`, `TaskKeepAlive`,
a global `UncaughtExceptionHandler` (writes a crash report, kills the process), then Fishnet,
MMKV, `loadAllSettings`, the logger, `PathManager.DIR_FILES_PRIVATE`, and the device
architecture. Coil is configured with a 20 MB memory cache and a 512 MB disk cache, with GIF
and SVG decoders.

### Navigation

Navigation 3 (`androidx.navigation3`) with serializable keys in `NormalNavKey.kt` /
`NestedNavKey.kt`. The rail has five sections:

`HOME` · `LIBRARY` · `DISCOVER` · `WALLPAPERS` · `SETTINGS`

Settings nests: `Renderer`, `Game`, `Control`, `Gamepad`, `Launcher`, `JavaManager`,
`ControlManager`, `AboutInfo`, `Wallpapers`.
Versions nests: `OverView`, `Config`, `UpdateLoader`, `ModsManager`, `SavesManager`,
`ResourcePackManager`, `ShadersManager`, `ScreenshotsManager`, `ServerList`.

### Feature areas of `:MiraiLauncher`

| Package | Purpose |
| --- | --- |
| `game/renderer` | Renderer abstraction, registry, auto-picker |
| `game/launch` | JVM launch, args, handlers, `GameLauncher`, `LibSortFix` |
| `game/version` | Installed versions, mods, multiplayer status, download |
| `game/download` | MoJang/Modrinth/CurseForge/MCIM/MCBBS/MultiMC/modpack pipelines |
| `game/account` | Microsoft, Yggdrasil, auth-server, offline, wardrobe (skins/capes) |
| `game/addons` | Mirror sources (BMCLAPI), modloaders (Fabric/Quilt/Forge/NeoForge/Cleanroom/OptiFine) |
| `game/optimization` | The four Aerix headline features (see §8) |
| `filemanager` | Full in-launcher file manager: its own theme, viewmodel, trash, editor |
| `ui/control` | Touch controls, gamepad, gyroscope, joystick, mouse |
| `ui/theme` | Material 3, MaterialKolor, festival effects, wallpaper-dynamic theming |
| `crashlogs` | MCLogs upload + mirrored fallback |
| `bridge` | JNI surface: `ZLBridge`, `ZWNativeInvoker`, `NativeLibraryLoader`, `FliteTts` |
| `setting` | 119 typed settings over MMKV |

---

## 6. The renderer subsystem

The single most important part of the codebase, and the fork's main point of difference.

### Abstraction

`RendererInterface` declares a renderer's identity (`getRendererId`, `getUniqueIdentifier`,
`getRendererName`, `getRendererSummary`), its **own** Minecraft compatibility window
(`getMinMCVersion` / `getMaxMCVersion`, plus display overrides), and what it needs loaded:
`getRendererEnv()`, `getDlopenLibrary()`, `getRendererLibrary()`, `getRendererEGL()`.
All env maps are `lazy` and side-effect free, which is what makes the renderer unit tests
runnable on a plain JVM.

### The registry

`Renderers.BUILT_IN` is the single source of truth — `Renderers.init()` loads it and
`RendererPicker` reads compatibility windows from it, so no range is ever written twice.
`addRenderer()` guards against duplicate unique identifiers.

Built-in renderers, in the order they are offered:

| # | Renderer | Library | MC range | Notes |
| --- | --- | --- | --- | --- |
| 1 | Krypton Wrapper (`NGGL4ESRenderer`) | `libng_gl4es.so` | ≤ `26.3-snapshot-3` (displays ≤ 26.2) | shipped via `libs/NG-GL4ES-release.aar`, all 4 ABIs |
| 2 | MobileGlues | `libmobileglues.so` | 1.17 – 26.3 | self-contained; own EGL provider |
| 3 | GL4ES | `libgl4es_114.so` | ≤ 1.21.4 | prebuilt blob in `jniLibs`, unchanged |
| 4 | vgpu (Glow-Worm) | `libvgpu.so` | ≤ 1.16.5 | shader converter + `DRAWBUFFERS` rewriter |
| 5 | vgpu 1.3.6β | `libvgpu_1368.so` | ≤ 1.16.5 | |
| 6 | **LTW Legacy** | `libltwlegacy.so` | **1.8 – 1.16.5** | **built from source** (§7) |
| 7 | **LTW** | `libltw.so` | **1.17+** | **built from source**, GL 3.2 core |
| 8 | Kopper Zink | `libglxshim.so` | ≤ `26.3-snapshot-3` | shipped via `libs/kopper-zink-release.aar`, all 4 ABIs |
| 9 | VirGL | `libOSMesa_2121.so` | ≤ `26.3-snapshot-3` | Mesa on virtual GPU |
| 10 | Freedreno | `libOSMesa_8.so` | ≤ `26.3-snapshot-3` | Mesa on Adreno |
| 11 | Panfrost | `libOSMesa_2300d.so` | ≤ 1.21.4 | Mesa on Mali |

### Automatic selection (`RendererPicker`)

The boundary is the **OpenGL profile**, not a version number:

- **1.8 – 1.16.5** → legacy pipeline → `LEGACY_ORDER = [LTW_LEGACY, VGPU, VGPU_1368, GL4ES, VIRGL]`
- **1.17+** → GL 3.2 core → `MODERN_ORDER = [LTW, MOBILEGLUES, ZINK]`

`resolve(mcVersion, manualIdentifier)` is called on the launch path and returns `null` for a
blank version so nothing changes for an unknown version string. Precedence:

1. **Explicit per-instance choice wins** — but only if the renderer is loaded *and* declares
   support for the target version. An unsupported override is rejected rather than honoured and
   then failing the launcher's own compatibility check moments later.
2. **Version decides** — first compatible entry in the matching order list.
3. **Nothing declares support** — the first loaded renderer is used and the choice is logged with
   the reason, rather than refusing to launch.

Renderer plugin identifiers with no built-in metadata are treated as compatible rather than
blocked. Every decision is written to the launch log with a human-readable reason.

Verified in CI by `LTWRendererTest`, `LTWLegacyRendererTest`, `VGPURendererTest`, and
`RendererPickerTest` (including the 1.16.5 → 1.17 boundary).

---

## 7. Native layer

### `MiraiLauncher/src/main/jni` — built by ndkBuild (`Android.mk`), ~21k LOC

| Module | Purpose |
| --- | --- |
| `pojavexec` | Pojav-style JVM bootstrap (`jre_launcher.c`, `egl_bridge.c`, `input_bridge_v3.c`) |
| `driver_helper` | GL driver selection + `nsbypass` |
| `linkerhook` | Linker interception, `-z global` |
| `exithook` | `exit_hook.c`, `sdl_hook.c`, `sdl_dlopen_hook.c` (bytehook-based) |
| `vulkan_check` | Vulkan capability probe |
| `awt_headless`, `awt_xawt`, `pojavexec_awt` | AWT shims (fake libs deleted after linking) |
| `flite`, `fliteWrapper`, `flite_cmu_us_kal16` | Text-to-speech |

`ctxbridges/` contains the per-API context bridges: `gl_bridge.c`, `osm_bridge.c`,
`virgl_bridge.c`, `egl_loader.c`, `osmesa_loader.c`, `swap_interval_no_egl.c`.
`Application.mk` targets `android-21` with `c++_shared`.

### `third_party/` — the two from-source renderers

**`third_party/LTW`** — Large Thin Wrapper (`git.artdeell.ltw`), the OpenGL 3.2 core wrapper.
Builds `libltw.so` via `src/main/tinywrapper/CMakeLists.txt`.

**`third_party/LTWVLegacy`** — Aerix's legacy wrapper, **a vendored GL4ES snapshot**:

| Field | Value |
| --- | --- |
| Upstream | `https://github.com/ptitSeb/gl4es` |
| Commit | `a444cc94b17c672c66c3e6ce07428dc603034db1` (2026-09-28) |
| Version | 1.1.7 (`version.h`) |
| License | MIT (preserved) |
| Vendored | 2026-10-03 |

Aerix's three deliberate changes, documented in `UPSTREAM.md`:

1. **Own CMake project** replacing upstream's multi-platform `CMakeLists.txt`. Compiles
   `src/gl/**` + `src/glx/hardext.c` with `ANDROID`, `NOX11`, `NO_GBM`, `DEFAULT_ES=2`.
2. **No EGL wrapper.** Upstream can build a second `libEGL.so` shim; Aerix does not, so it never
   competes for `eglGetProcAddress`. The platform supplies EGL.
3. **Renamed** to `libltwlegacy.so` so it coexists with `libgl4es_114.so` and `libng_gl4es.so`.

Deliberately not vendored: `traces/` (46 MB), `refs/`, `spec/`, `media/`, `tests/`, `debian/`.

This replaced a **prebuilt GL4ES 1.1.4 blob** that could not be audited, patched, or rebuilt.

### Native libraries actually packaged

From `src/main/jniLibs/<abi>/` — ARM64 contents, ~87 MB; Armeabi-v7a 65 MB, x86 25 MB,
x86_64 77 MB:

`libEGL_angle.so`, `libGLESv2_angle.so`, `libOSMesa_{8,2121,2300d}.so`,
`libVkLayer_khronos_timeline_semaphore.so`, `libgl4es_114.so`, `libjnidispatch.so`,
`libmobileglues.so`, `libmobileglues_info_getter.so`, `libshaderconv.so`, `libunpack200.so`,
`libvgpu.so`, `libvgpu_1368.so`, `libvirgl_test_server.so`, `libvulkan_freedreno.so`.

From AARs in `MiraiLauncher/libs/` (all four ABIs each): `NG-GL4ES-release.aar`,
`kopper-zink-release.aar`, `SDL-release.aar`, `lwjgl-3.3.3-natives-release.aar`,
`lwjgl-3.4.1-natives-release.aar`, `openal-soft-release.aar`, `spirv-cross-natives.aar`
(the last one is notably larger than everything else at 31 MB).

### Asset payload

`src/main/assets/` carries the runtime the launcher unpacks on first run:

- `runtimes/jre-{8,17,21,25}/` — `universal.tar.xz` plus per-ABI `bin-<arch>.tar.xz`
- `runtimes/jna/`
- `app_runtime/lwjgl/{3.3.3,3.4.1}/` — LWJGL modules + per-ABI natives
- `components/{auth_libs,caciocavallo,caciocavallo17,launcher}/`
- `game/` — including `versions.txt` (the version catalog `GameVersionNumber` reads)
- `skinview/`, `wallpapers/`, `textmate/{grammars,themes}/`

---

## 8. The four Aerix-differentiated features

All under `game/optimization/`, all persisted through `AllSettings`:

| File | LOC | What it does |
| --- | --- | --- |
| `MobileFpsBooster.kt` | ~27.6k chars | One-tap full mobile FPS preset; selection persists across restarts (`FpsBoostPreset`) |
| `JvmGcAutoTuner.kt` | ~29.9k chars | Tunes the Java runtime and garbage collector per device; persists (`GcTuningPreset`); unit-tested by `JvmGcAutoTunerTest` |
| `SmartCrashDoctor.kt` | ~22.5k chars | Reads a crash log and suggests a fix |
| `ModDependencyResolver.kt` | ~48.5k chars | Resolves missing mod dependencies and conflicts automatically |

Around them: a Aerix-branded UI layer (`MiraiHomeDashboard`, `MiraiPlayPage`, `MiraiDetailPages`,
`MiraiRedrawnPages`, `MiraiRemainingPages`, `MiraiSectionPages`, `MiraiToolPages`,
`MiraiNavigationRail`, `MiraiThemeManager`), 20 built-in HD wallpapers with dim control and
custom imports, a pre-rendered blur (replacing a live GPU blur), an in-game Aerix Pill HUD, and
an interactive 3D paper doll.

---

## 9. Settings architecture

`AllSettings : SettingsRegistry` — **119 typed settings** over MMKV. Types are provided by
`setting/unit/` classes: `Boolean`, `Int`, `Float`, `Long`, `String`, `StringList`,
`NullableInt`, `Enum`, `Parcelable`, `Offset`, each with an `AbstractSettingUnit` base and
default/range helpers (`_DefaultRange.kt`, `_Numbers.kt`).

Nine enums in `setting/enums/`: `ActionMenuSide`, `AppLanguage`, `BackgroundBlur`, `DarkMode`,
`GamepadInputMode`, `GestureActionType`, `MirrorSourceType`, `MouseControlMode`,
`ResolutionRule`.

Notable defaults: `renderer = ""` (empty → the picker decides), `vulkanDriver = "default turnip"`,
`resolutionRatio = 70`, `autoPickJavaRuntime = true`, `versionIsolation = true`,
`launcherColorTheme = AERIX`, `gameDownloadSource = AUTO`, `curseForgeApiKey = ""`.

---

## 10. Localisation

18 `values-*` directories: Arabic, Spanish, Estonian, Filipino, Indonesian, Italian, Japanese,
Korean, Portuguese, Brazilian Portuguese, Russian, Thai, Turkish, Uyghur, Vietnamese,
Simplified Chinese, Traditional Chinese, plus `values-night`.

---

## 11. Networking and downloads

`path/UrlManager.kt` centralises endpoints and HTTP clients.

- `URL_PROJECT` / `URL_OWNER` / `URL_RELEASES` → the Aerix repo (fork-corrected)
- `URL_LATEST_RELEASE_INFO` → `.../releases/latest/download/aerix-update.json`
- `URL_SUPPORT` → **still points at MovTery** (upstream leftover)
- `URL_WEBLATE` → **still points at `zalithlauncher2`** (upstream leftover)
- `URL_MINECRAFT_VERSION_REPOS` → `piston-meta.mojang.com/.../version_manifest_v2.json`
- CurseForge host detection + automatic `x-api-key` injection; a user-supplied key in settings
  overrides the build-time key
- `ResilientDns` — falls back to DoH when system DNS fails
- Two OkHttp clients: `GLOBAL_CLIENT` (30 s timeout, pinned HTTP/1.1) and
  `DOWNLOAD_OKHTTP_CLIENT` (no `callTimeout`, 64-connection pool, HTTP/2 by default)
- API **and** asset searches fall back **sequentially** across platforms (15 s per source,
  30 s overall) — changed from concurrent racing, which was producing spurious
  "This job has not completed yet" failures on Modrinth and CurseForge

Platform integrations: Modrinth, CurseForge (with a mirror backup), MCIM, MCBBS, MultiMC.
Mirror sources live in `game/addons/mirror/` (BMCLAPI, `SourceType`, `MirrorPreferences`).

---

## 12. Update mechanism

`LauncherUpgradeViewModel` fetches a **self-hosted JSON manifest**, not the GitHub API
directly. `RemoteData` is a Kotlin-serializable model with `code`, `version`, `created_at`,
`files[]` (per-ABI APK URLs + sizes), `default_body`, `bodies[]` (per-language changelogs),
and optional cloud-drive links.

- **On app start** — rate-limited to one check per hour; the user's last-ignored version is honoured.
- **Manually** (Settings → About) — rate-limited to one check per 5 s, throws
  `TooFrequentOperationException`; bypasses the ignore list.
- HTTP 404 is handled as `ManifestUnavailable` ("No Aerix update manifest is published yet")
  rather than an error, so a fork without a published release degrades gracefully.
- Comparison is `BuildConfig.VERSION_CODE < data.code`.

`.github/scripts/mirai_release.py` generates that manifest at release time (§14).

---

## 13. CI/CD

Ten workflows in `.github/workflows/`:

| Workflow | Trigger | Purpose |
| --- | --- | --- |
| `build.yml` | `workflow_call` / manual | **The reusable build.** Matrix over 5 arches, JDK 21 Temurin, Gradle setup with failure summaries |
| `debug_ci.yml` | push (any branch), PR | Debug build |
| `push_ci.yml` | push (any branch), PR | Debug build (duplicate of the above) |
| `mirai_fixed_ci.yml` | push to `Aerix-launcher` | Debug build (third duplicate) |
| `release_ci.yml` | release published | Release build + collect, verify, sign-check, manifest, upload |
| `add_home_features.yml` | manual | No-op stub |
| `fix_back_button.yml` | manual | Scripted commit pushing to `Aerix-launcher` |
| `remove_plus_tab.yml` | manual | Scripted commit pushing to `Aerix-launcher` |
| `remove_warning.yml` | manual | Scripted commit |
| `restore_classic_shell.yml`, `show_skin_stage.yml` | manual | Scripted commits |

`debug_ci.yml`, `push_ci.yml`, and `mirai_fixed_ci.yml` are three near-identical debug-build
workflows. Both `debug_ci.yml` and `push_ci.yml` trigger on `push` to `**` — i.e. every branch —
and on every pull request, so any push runs two full builds; a push to `Aerix-launcher`
additionally runs `mirai_fixed_ci.yml` for a third. Recent run history confirms it (7–15 minutes
each). Consolidating to one would cut CI time by roughly two-thirds with no loss of coverage.

### What `build.yml` does

1. Checkout with submodules, JDK 21, Gradle caching
2. Release only: assert `STORE_PASSWORD` / `KEY_PASSWORD` are set and the keystore is present
3. `./gradlew MiraiLauncher:assemble<Variant> -Darch=<arch>` with the log teed to a file
4. On failure: a Python step extracts the failure summary, relevant compiler diagnostics, and
   the stack ending, writes them to the job summary, and emits an `::error::` annotation
5. Release only: `apksigner verify` every APK
6. `verify_ltw_apk.py <arch> <apk-dir>` — ELF-checks `libltw.so`, `libltwlegacy.so`,
   `libvgpu.so`, `libvgpu_1368.so` are present with the correct class, endianness, and machine
7. Debug + `all` only: `testDebugUnitTest` filtered to `LTWRendererTest`,
   `LTWLegacyRendererTest`, `VGPURendererTest`, `RendererPickerTest`, `JvmGcAutoTunerTest`,
   `MirrorPreferencesTest`, `FileReplacementTest`; failures published as annotations by
   `publish_unit_test_failures.py`

The ELF verifier is genuinely good work — a wrong-architecture renderer library would otherwise
be invisible until someone launched the matching Minecraft version on a device.

---

## 14. Release pipeline

`release_ci.yml` fires on `release: published` and:

1. Calls `build.yml` with `variant: Release`
2. Checks out the release tag
3. Collects all five APKs, determines each arch from its filename suffix, runs
   `verify_ltw_apk.py` per APK, then `mirai_release.py verify` to confirm exactly one APK per
   arch across `{all, arm, arm64, x86, x86_64}`
4. Reads `launcher_version_code` / `launcher_version_name` from `MiraiLauncher/gradle.properties`
5. `mirai_release.py metadata` writes `aerix-update.json` — rejecting names with spaces (GitHub
   would rename them and break the URLs) and requiring the `MiraiLauncher-` / `Aerix.Launcher-`
   prefix
6. `softprops/action-gh-release@v3` uploads the APKs and the manifest with
   `fail_on_unmatched_files: true`

This closes the loop for §12: the app polls `releases/latest/download/aerix-update.json`, which
this workflow generates and attaches to the tag.

---

## 15. Testing

224 `@Test` methods across three modules:

- **`:MiraiLauncher`** (`src/test/java/...`, ~36 files) — renderers (4), download engine
  (`FetcherHttpTest`, `FetcherResumeTest`, `ResumeContextTest`, `DownloadStatsTest`,
  `BatchDownloaderE2ETest`), JVM process exclusivity, version comparison, multiplayer status
  serialization, keep-alive, festivals (6 simulators), file utils, MurmurHash2 incremental,
  JVM GC tuner, mirror preferences, wardrobe file replacement
- **`:CardGrid`** — `GridEngineTest`, `GridEnginePerfTest`, `CardGridStateTest`
- **`:Guide`** — `GuideControllerTest`, `GuidePlacementSolverTest`, `GuideRegistryTest`,
  `NextTipSolverTest`

`testOptions.unitTests` sets `isIncludeAndroidResources = true` and
`isReturnDefaultValues = true`. Note that **only seven test classes are actually run by CI** —
the rest exist but are not wired into `build.yml`. The CHANGELOG also records a real fix in this
area: `GameVersionNumber`'s static initialiser used to throw `NoClassDefFoundError` on a plain
JVM because `getResourceAsStream("/assets/game/versions.txt")` returned `null`; it now degrades
to an empty list, which is what made `RendererPickerTest` runnable in CI at all.

---

## 16. Observations, drift, and risks

### Documentation drift

- `docs/RENDERER_SELECTION.md` describes the legacy fallback as "LTW Legacy → GL4ES → VirGL"
  but the code is `LEGACY_ORDER = [LTW_LEGACY, VGPU, VGPU_1368, GL4ES, VIRGL]`. The two vgpu
  entries are omitted from the doc.
- The same doc's renderer table lists Krypton Wrapper as `libng_gl4es.so` (correct) and Kopper
  Zink as `libglxshim.so` (correct) — both are shipped inside AARs, not `jniLibs`, so grepping
  `jniLibs` for them comes up empty. Worth a sentence in the doc.
- `AUDIT.md` is dated 2026-10-01, describes itself as "Prepared by: Copilot", and several of its
  open questions ("URL_LATEST_RELEASE_INFO needs verification", About-tab owner display) are now
  resolved in the code. It reads as a historical planning artefact rather than current state.

### Leftover upstream references

- `URL_SUPPORT = "https://ifdian.net/a/MovTery"` — the in-app support/donation link still sends
  users to the upstream author, not to Aerix.
- `URL_WEBLATE = "https://hosted.weblate.org/projects/zalithlauncher2"` — translations link to
  the upstream Weblate project.
- The namespace `com.movtery.zalithlauncher` and most GPL headers credit MovTery (correct and
  required). `RendererPicker.kt` is one of the few files with the added
  "Copyright (C) 2026 Aerix Launcher contributors" line.

### CI cost

Two or three identical debug builds per push (see §13), because three separate workflow files
were added and the earlier two still trigger on every branch and every PR.

### Signing material in a public repository

`MiraiLauncher/zalith_launcher.jks` (the **release** keystore) and
`zalith_launcher_debug.jks` are both git-tracked. The release keystore's password is *not* in the
repo — it comes from the `STORE_PASSWORD` / `KEY_PASSWORD` secrets or gitignored local files —
so this is not an immediate compromise. It is still worth knowing that anyone who obtains those
passwords can produce an APK that Android will accept as a legitimate update over an installed
Aerix build. Rotation would require the key to leave version control.

The debug keystore's passwords *are* in `MiraiLauncher/gradle.properties` as plain text, which
is normal and harmless for a debug key.

### Version numbering

`launcher_version_code = 200043` while `launcher_version_name = 1.0.0`. The CHANGELOG explains
this is deliberate — kept monotonic with pre-release builds so existing installs see the update.
Anyone bumping the version must not "fix" the code down to `10000`.

### Prebuilt blobs that remain

`libgl4es_114.so` (GL4ES 1.1.4), the three `libOSMesa_*.so` builds, `libvulkan_freedreno.so`,
`libmobileglues.so`, `libvgpu*.so`, `libvirgl_test_server.so`, `libshaderconv.so`, and the AARs
in `libs/` are all still binary-only. That is a deliberate scope decision recorded in the
CHANGELOG — LTW and LTW Legacy are the two that were brought into source.

---

## 17. Where to touch what

| Task | File(s) |
| --- | --- |
| Add a renderer | New `RendererInterface` in `game/renderer/renderers/`, add to `Renderers.BUILT_IN`, add its lib to `jniLibs` or an AAR, extend `verify_ltw_apk.py` if built from source |
| Change auto-selection | `game/renderer/RendererPicker.kt` — the order lists and `usesCoreProfile` |
| Change a version range | The renderer's own `getMinMCVersion` / `getMaxMCVersion`; nothing else hardcodes it |
| Change the version | `MiraiLauncher/gradle.properties` (both `_code` and `_name`), then `CHANGELOG.md` |
| Change branding | `MiraiLauncher/gradle.properties` (`launcher_*`, `url_home`) |
| Change the update endpoint | `URL_LATEST_RELEASE_INFO` in `path/UrlManager.kt` — the manifest shape is `upgrade/RemoteData.kt` |
| Add a setting | `setting/AllSettings.kt` using a `setting/unit/` builder |
| Change nav structure | `ui/screens/NormalNavKey.kt`, `ui/screens/main/MiraiNavigationRail.kt` |
| Change the rail's look | `ui/screens/main/MiraiNavigationRail.kt`, `ui/theme/MiraiThemeManager.kt` |
| Home dashboard | `ui/screens/content/home/MiraiHomeDashboard.kt`, `MiraiPlayPage.kt` |
| Native changes | `MiraiLauncher/src/main/jni/Android.mk` |
| LTW Legacy changes | `third_party/LTWVLegacy/` + `ltwlegacy/src/main/cpp/CMakeLists.txt` |
| Release process | `.github/workflows/release_ci.yml`, `.github/scripts/mirai_release.py` |

---

## 18. Provenance

- **Upstream:** [ZalithLauncher/ZalithLauncher2](https://github.com/ZalithLauncher/ZalithLauncher2) — this repo is a fork, `isFork: true`
- **Vendored:** GL4ES (MIT) in `third_party/LTWVLegacy`; LTW in `third_party/LTW`
- **Bundled unmodified:** MobileGlues (LGPL-2.1, prebuilt natives only), VGPU, OSMesa builds, ANGLE, SPIRV-Cross
- **Issues:** disabled on the GitHub repo
- **History:** squashed to one commit; PRs #1–#5 visible on GitHub record the fork's actual work
