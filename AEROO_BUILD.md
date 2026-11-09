# AEROO Launcher: build and provenance

AEROO is an unofficial, separately branded modification of **Zalith Launcher 2**. The app remains based on the original launcher implementation; its Android application ID and user-facing branding are configured independently. It is not affiliated with or endorsed by the Zalith Launcher developers. This fork retains the upstream GPL-3.0 license, copyright statements, additional GPLv3 terms, bundled license texts, and third-party attributions. Do not remove those notices when redistributing builds.

## Original framework and project architecture

The upstream framework is Zalith Launcher 2, an Android Minecraft: Java Edition launcher. The repository uses Gradle Kotlin DSL with an Android application module (`ZalithLauncher`), Kotlin and Jetpack Compose/Material 3, plus local Android library modules (`LayerController`, `ColorPicker`, `CardGrid`, `Terracotta`, `InputMap`, `Guide`, and `PetLib`). `LWJGL` contains locally patched LWJGL 3.3.3 and 3.4.1 modules and bundled jars/native assets. `PetLib` builds Rust native libraries for Android ABIs through `cargo-ndk`. The app also has an `ndk-build` native project under `ZalithLauncher/src/main/jni`.

The root project includes the original Gradle wrapper (Gradle 9.5.0), Android Gradle Plugin 9.3.0, Kotlin 2.4.21, Java 17 source/target, and compile SDK 37 (minor API level 2); the configured app NDK is 25.2.9519653. Cloud builds use Temurin JDK 21 and do not require Android Studio on the contributor's phone.

## Build the first debug APK in GitHub Actions

1. The **Build APK** workflow runs on pushes and can also be started from **Actions → Build APK → Run workflow**; select `Debug` (the default).
2. Debug is built as one universal (`all`) APK. Download the artifact named `AEROO-debug-all`; it contains `AEROO-debug.apk`.
3. The first successful cloud build is [run 42710276280](https://github.com/zerobore/AEROO-LAUNCHER/actions/runs/42710276280), commit `bb86b66`. Its `build (all)` job completed successfully and uploaded the `AEROO-debug-all` artifact (about 397 MiB).

The workflow uses Temurin JDK 21, installs `cargo-ndk` and Rust Android targets, resolves `sdkmanager` from PATH or the Android SDK command-line tools, installs the exact NDK 25.2.9519653 requested by the app, then builds and uploads the universal Debug APK. It fails if the APK is missing. Debug builds need no signing secrets: the existing debug signing configuration is retained. Optional OAuth and CurseForge build values remain optional.

For a local Linux/macOS build with JDK 21, Android SDK/NDK 25.2.9519653, Rust Android targets, and `cargo-ndk` installed:

```sh
./gradlew --no-daemon ZalithLauncher:assembleDebug \
  -Darch=all \
  -Plauncher_name=AEROO \
  -Plauncher_app_name="AEROO Launcher" \
  -Plauncher_short_name=AEROO \
  -Purl_home=https://github.com/zerobore/AEROO-LAUNCHER \
  -Papplication_id=com.aeroo.launcher
```

The Gradle output is named `AEROO-debug.apk` under `ZalithLauncher/build/outputs/apk/debug/`. GitHub Actions stages it at `ZalithLauncher/build/outputs/aeroo/AEROO-debug.apk` before uploading it as the `AEROO-debug-all` artifact.

## Fork branding and notices

AEROO-specific values are configured in `ZalithLauncher/gradle.properties` for this fork, so the existing cloud workflow builds the AEROO identity without requiring credentials or changes to workflow secrets. The application ID is `com.aeroo.launcher` (with the existing `.debug` suffix on debug builds), keeping AEROO installs separate from the upstream app. The upstream Android namespace and source package names are intentionally retained to avoid a risky architecture-wide package migration.

The splash welcome screen and main interface identify the app as an unofficial modified version, state that AEROO is not affiliated with or endorsed by the upstream project, and preserve the upstream copyright attribution. The app label/build identifier use AEROO. The existing launcher icon artwork is unchanged in this first build; replace it only when approved AEROO artwork is available.

## Modifications made for AEROO's first build

- `.github/workflows/build.yml`: added a selectable manual build variant (Debug by default), limited Debug to one universal APK while retaining per-ABI Release builds, resolved the SDK manager reliably from PATH or Android command-line tools, pinned/installed NDK 25.2.9519653, passed AEROO Gradle properties, and made the Debug artifact upload fail if the APK is missing. The changes are pushed and the workflow has completed successfully.
- `ZalithLauncher/build.gradle.kts`: made the Android application ID configurable while preserving the original default package identity and source namespace, and set the universal debug APK filename to `AEROO-debug.apk`.
- `ZalithLauncher/gradle.properties`: set this fork's app name, build identifier, short name, home URL, and application ID to AEROO values.
- `ZalithLauncher/src/main/java/com/movtery/zalithlauncher/ui/screens/main/MainScreen.kt`: added a visible fork disclaimer to the main interface for branded builds only.
- `ZalithLauncher/src/main/java/com/movtery/zalithlauncher/ui/screens/splash/WelcomeScreen.kt`: added the same attribution/disclaimer to the startup welcome screen for branded builds only.
- `ZalithLauncher/src/main/res/values/strings.xml`: added the English attribution and unofficial-modification notice, falling back for locales without an override.
- `README.md` and `README_EN_US.md`: identified this repository as the AEROO modification and linked to its build/provenance documentation.
- `gradlew`: made the checked-in Gradle wrapper executable for local Unix command-line use.
- `AEROO_BUILD.md`: recorded the original framework, cloud build path and result, legal/provenance notes, and repository modifications.

The original `LICENSE`, source headers, upstream source namespace, launcher features, and project architecture have not been removed or globally renamed. This fork changes its own build branding/configuration. No APK is checked into source control; the successful CI artifact is the distribution for this initial build.
