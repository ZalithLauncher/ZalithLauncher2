# AEROO Launcher: build and provenance

AEROO is an unofficial, separately branded modification of **Zalith Launcher 2**. The app remains based on the original launcher implementation; its Android application ID and user-facing branding are configured independently. It is not affiliated with or endorsed by the Zalith Launcher developers. This fork retains the upstream GPL-3.0 license, copyright statements, additional GPLv3 terms, bundled license texts, and third-party attributions. Do not remove those notices when redistributing builds.

## Original framework and project architecture

The upstream framework is Zalith Launcher 2, an Android Minecraft: Java Edition launcher. The repository uses Gradle Kotlin DSL with an Android application module (`ZalithLauncher`), Kotlin and Jetpack Compose/Material 3, plus local Android library modules (`LayerController`, `ColorPicker`, `CardGrid`, `Terracotta`, `InputMap`, `Guide`, and `PetLib`). `LWJGL` contains locally patched LWJGL 3.3.3 and 3.4.1 modules and bundled jars/native assets. `PetLib` builds Rust native libraries for Android ABIs through `cargo-ndk`. The app also has an `ndk-build` native project under `ZalithLauncher/src/main/jni`.

The root project includes the original Gradle wrapper (Gradle 9.5.0), Android Gradle Plugin 9.3.0, Kotlin 2.4.21, Java 17 source/target, and compile SDK 37 (minor API level 2); the configured app NDK is 25.2.9519653. Cloud builds use Temurin JDK 21 and do not require Android Studio on the contributor's phone.

## Build the first debug APK in GitHub Actions

1. Push this branch to GitHub (or open a pull request after pushing it).
2. The existing **Build CI** workflow runs the reusable **Build APK** workflow. Until the workflow change noted below is permitted and pushed, it builds five architecture jobs; the `all` job produces the universal APK.
3. Open the run in the repository's **Actions** tab and download the artifact named `debug apk (all)`. It contains `AEROO-debug.apk`.

A CI workflow hardening change is included in the local working tree: it limits Debug to the single universal build, pins NDK 25.2.9519653 to match the app configuration, adds a selectable manual build variant, and uploads a stable named artifact while failing on missing APKs. It could not be pushed because GitHub rejected the update to `.github/workflows/build.yml` for missing `workflows` permission on the connected GitHub App. After that permission is granted and the workflow change is pushed, the artifact will be named `AEROO-debug-all`. The existing workflow uses the checked-in Gradle wrapper and Rust Android targets. Debug builds need no signing secrets; the existing debug signing configuration is retained. Optional OAuth and CurseForge build values remain optional.

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

The Gradle output name includes the original launcher version and is written under `ZalithLauncher/build/outputs/apk/debug/`. GitHub Actions copies the generated universal debug APK to the stable artifact filename `AEROO-debug.apk`.

## Fork branding and notices

AEROO-specific values are configured in `ZalithLauncher/gradle.properties` for this fork, so the existing cloud workflow builds the AEROO identity without requiring credentials or changes to workflow secrets. The application ID is `com.aeroo.launcher` (with the existing `.debug` suffix on debug builds), keeping AEROO installs separate from the upstream app. The upstream Android namespace and source package names are intentionally retained to avoid a risky architecture-wide package migration.

The splash welcome screen and main interface identify the app as an unofficial modified version, state that AEROO is not affiliated with or endorsed by the upstream project, and preserve the upstream copyright attribution. The app label/build identifier use AEROO. The existing launcher icon artwork is unchanged in this first build; replace it only when approved AEROO artwork is available.

## Modifications made for AEROO's first build

- `.github/workflows/build.yml`: added a selectable manual build variant (Debug by default), optimized Debug to one universal APK, pinned the CI NDK to the version already requested by the Android module, and made the required debug artifact upload fail if the APK is missing. GitHub rejected pushing this workflow change because the connected GitHub App lacks the `workflows` permission; it remains a local change until that permission is granted.
- `ZalithLauncher/build.gradle.kts`: made the Android application ID configurable while preserving the original default package identity and source namespace, and set the universal debug APK filename to `AEROO-debug.apk`.
- `ZalithLauncher/src/main/java/com/movtery/zalithlauncher/ui/screens/main/MainScreen.kt`: added a visible fork disclaimer to the main interface for branded builds only.
- `ZalithLauncher/src/main/java/com/movtery/zalithlauncher/ui/screens/splash/WelcomeScreen.kt`: added the same attribution/disclaimer to the startup welcome screen for branded builds only.
- `ZalithLauncher/src/main/res/values/strings.xml`: added the English attribution and unofficial-modification notice, falling back for locales without an override.
- `AEROO_BUILD.md`: recorded the framework, cloud build path, legal/provenance notes, and repository modifications.

The original `LICENSE`, source headers, upstream build defaults, user functionality, and architecture have not been removed or globally renamed. No APK is checked into source control; the CI artifact is the distribution for this initial build.
