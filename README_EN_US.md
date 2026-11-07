<p align="center">
  <img src="brand/banner.png" alt="NusaLauncher" width="720">
</p>

<p align="center">
  A Minecraft: Java Edition launcher for Android with offline accounts, crash analysis, and a performance monitor.
</p>

[Bahasa Indonesia](README.md) | [简体中文 (upstream docs)](README_ZH_CN.md) | [繁體中文 (upstream docs)](README_ZH_TW.md)

> [!NOTE]
> **NusaLauncher** is an **unofficial** community fork of [Zalith Launcher 2](https://github.com/ZalithLauncher/ZalithLauncher2) by MovTery and contributors. It is **not affiliated with or endorsed by** the Zalith Launcher team. Licensed under GPL-3.0, same as upstream.
>
> Not an official Minecraft product. Not approved by or associated with Mojang or Microsoft. Minecraft is a trademark of Mojang Synergies AB.

## Status

NusaLauncher is in a **testing phase**. Automatic updates are not available yet: the "Check for updates" button opens the [Releases](https://github.com/PickoCore/NusaLauncher/releases) page. There is no official release yet, and the app is currently a debug build. Features may change and bugs are expected.

## Features

### Inherited from Zalith Launcher 2

- Runs Minecraft: Java Edition on Android devices (arm64, Android 8.0 and up).
- Several renderers, including GL4ES, MobileGlues, and Zink.
- Fabric and modpack installation, version and Java management, and touch controls.

### Added by NusaLauncher

- **Offline accounts** without having to sign in with Microsoft first.
- **Local crash analysis.** After a crash, the launcher recognizes 12 common problems (out of memory, wrong Java version, mod conflicts, shader failures, native crashes, and more) and shows the likely cause and advice in English or Indonesian. Works without internet.
- **Share logs to mclo.gs** with automatic redaction (UUIDs, tokens, emails, and IP addresses are masked before upload).
- **Automatic log archive.** The log of each game session is saved when you exit, named like `NusaLogs-2026-10-06-19.41.MobileGlues.log`, and kept for 30 days. See [docs/logs.md](docs/logs.md).
- **Frame-time monitor.** Every 5 seconds the game log gets a `[NusaFrameStats]` line with average FPS, 1% low, jitter, and stutter count, so "high FPS but not smooth" can be measured. See [docs/performance-testing.md](docs/performance-testing.md).
- New identity: its own name, icon, and package (`com.pickocore.nusalauncher`).

### Planned (not available yet)

- An in-app **log history** screen (list, view, share, delete).
- **Play together** over a local network (same WiFi), with a cross-version option.
- A **renderer hub** and benchmark to pick the best renderer for your phone.
- A black and orange interface theme.
- Fully tested Microsoft sign-in. The base code comes from Zalith, but we have not tested it.

## Download

There is no release yet. To try a test build:

1. Open the **Actions** tab of this repository and pick the latest green **Build APK** run.
2. Under **Artifacts**, download the APK for your phone's architecture (most modern phones are **arm64**). GitHub login is required.
3. Extract the zip and install the APK inside (allow installing from unknown sources).

## Usage notes

- **Offline accounts** only work in single-player and on servers running in offline-mode. Online-mode servers (most large servers) reject them and require a genuine Microsoft account. If you enjoy the game, please buy it to support Mojang.
- **Choosing a renderer.** In an early test on one phone (Dimensity 8500, Mali-G720), GL4ES was the most stable for vanilla Minecraft, MobileGlues came next, and Zink stuttered the most. GL4ES does not support Sodium and can crash when used with it. This is a single device, so treat it as a hint, not a guarantee.
- After a crash, open the log menu once the game closes to see the diagnosis, or attach the `NusaLogs-...` file when reporting a problem.

## Documentation

- [Log system](docs/logs.md)
- [Performance testing guide](docs/performance-testing.md)
- [Brand assets](brand/README.md)

## Build from source

Requirements (following the [CI workflow](.github/workflows/build.yml)):

- JDK 21
- Android SDK and NDK `27.0.12077973`
- Rust (stable) and [cargo-ndk](https://github.com/bbqsrc/cargo-ndk), for the `PetLib` module
- The repository's Gradle wrapper (AGP 9.3, Kotlin 2.4)
- minSdk 26, targetSdk 34

```bash
git clone https://github.com/PickoCore/NusaLauncher.git
# Open in Android Studio and build, or follow the steps in .github/workflows/build.yml
```

## Reporting issues

Open an [issue](https://github.com/PickoCore/NusaLauncher/issues). Include your phone model, Minecraft version, renderer, and the archived log file (`NusaLogs-...log`). Never paste tokens or personal data.

Problems that come from the upstream project (not NusaLauncher features) are best also reported to [Zalith Launcher 2](https://github.com/ZalithLauncher/ZalithLauncher2/issues).

## 📜 License

This project is licensed under the **[GPL-3.0 license](LICENSE)**.

### Additional Terms (Pursuant to Section 7 of the GPLv3 License)

1. When distributing a modified version of this program, you must reasonably modify the program's name or version number to distinguish it from the original version. (According to [GPLv3, 7(c)](https://github.com/ZalithLauncher/ZalithLauncher2/blob/969827b/LICENSE#L372-L374))
    - Modified versions **must not include the original program name "ZalithLauncher" or its abbreviation "ZL" in their name, nor use any name that is similar enough to cause confusion with the official name**.
    - All modified versions **must clearly indicate that they are “Unofficial Modified Versions” on the program’s startup screen or main interface**.
    - The application name of the program can be modified in [gradle.properties](./ZalithLauncher/gradle.properties).

2. You must not remove the copyright notices displayed by the program. (According to [GPLv3, 7(b)](https://github.com/ZalithLauncher/ZalithLauncher2/blob/969827b/LICENSE#L368-L370))

## Open Source Libraries and Licenses

This software uses the following open source libraries:

| Library                               | Copyright                                                                                                     | License              | Official Link                                                                      |
|---------------------------------------|---------------------------------------------------------------------------------------------------------------|----------------------|------------------------------------------------------------------------------------|
| androidx-appcompat                    | Copyright © The Android Open Source Project                                                                   | Apache 2.0           | [Link↗](https://developer.android.com/jetpack/androidx/releases/appcompat)         |
| androidx-constraintlayout-compose     | Copyright © The Android Open Source Project                                                                   | Apache 2.0           | [Link↗](https://developer.android.com/develop/ui/compose/layouts/constraintlayout) |
| androidx-webkit                       | Copyright © The Android Open Source Project                                                                   | Apache 2.0           | [Link↗](https://developer.android.com/jetpack/androidx/releases/webkit)            |
| ANGLE                                 | Copyright 2018 The ANGLE Project Authors                                                                      | BSD 3-Clause License | [Link↗](http://angleproject.org/)                                                  |
| Apache Commons Codec                  | -                                                                                                             | Apache 2.0           | [Link↗](https://commons.apache.org/proper/commons-codec)                           |
| Apache Commons Compress               | -                                                                                                             | Apache 2.0           | [Link↗](https://commons.apache.org/proper/commons-compress)                        |
| Apache Commons IO                     | -                                                                                                             | Apache 2.0           | [Link↗](https://commons.apache.org/proper/commons-io)                              |
| ByteHook                              | Copyright © 2020-2024 ByteDance, Inc.                                                                         | MIT License          | [Link↗](https://github.com/bytedance/bhook)                                        |
| BuildKeys                             | Copyright © 2026 MovTery                                                                                      | Aoache 2.0           | [Link↗](https://github.com/MovTery/BuildKeys)                                      |
| Coil Compose                          | Copyright © 2025 Coil Contributors                                                                            | Apache 2.0           | [Link↗](https://github.com/coil-kt/coil)                                           |
| Coil Gifs                             | Copyright © 2025 Coil Contributors                                                                            | Apache 2.0           | [Link↗](https://github.com/coil-kt/coil)                                           |
| Coil SVG                              | Copyright © 2025 Coil Contributors                                                                            | Apache 2.0           | [Link↗](https://github.com/coil-kt/coil)                                           |
| Fishnet                               | Copyright © 2025 Kyant                                                                                        | Apache 2.0           | [Link↗](https://github.com/Kyant0/Fishnet)                                         |
| gl4es_extra_extra                     | Copyright © 2016-2018 Sebastien Chevalier; Copyright (c) 2013-2016 Ryan Hileman                               | MIT License          | [Link↗](https://github.com/PojavLauncherTeam/gl4es_extra_extra)                    |
| Gson                                  | Copyright © 2008 Google Inc.                                                                                  | Apache 2.0           | [Link↗](https://github.com/google/gson)                                            |
| kotlinx.coroutines                    | Copyright © 2000-2020 JetBrains s.r.o.                                                                        | Apache 2.0           | [Link↗](https://github.com/Kotlin/kotlinx.coroutines)                              |
| ktor-client-content-negotiation       | Copyright © 2000-2023 JetBrains s.r.o.                                                                        | Apache 2.0           | [Link↗](https://ktor.io)                                                           |
| ktor-client-core                      | Copyright © 2000-2023 JetBrains s.r.o.                                                                        | Apache 2.0           | [Link↗](https://ktor.io)                                                           |
| ktor-client-okhttp                    | Copyright © 2000-2023 JetBrains s.r.o.                                                                        | Apache 2.0           | [Link↗](https://ktor.io)                                                           |
| ktor-http                             | Copyright © 2000-2023 JetBrains s.r.o.                                                                        | Apache 2.0           | [Link↗](https://ktor.io)                                                           |
| ktor-serialization-kotlinx-json       | Copyright © 2000-2023 JetBrains s.r.o.                                                                        | Apache 2.0           | [Link↗](https://ktor.io)                                                           |
| LWJGL - Lightweight Java Game Library | Copyright © 2012-present Lightweight Java Game Library All rights reserved.                                   | BSD 3-Clause License | [Link↗](https://github.com/LWJGL/lwjgl3)                                           |
| material-color-utilities              | Copyright 2021 Google LLC                                                                                     | Apache 2.0           | [Link↗](https://github.com/material-foundation/material-color-utilities)           |
| Maven Artifact                        | Copyright © The Apache Software Foundation                                                                    | Apache 2.0           | [Link↗](https://github.com/apache/maven/tree/maven-3.9.9/maven-artifact)           |
| Media3                                | Copyright © The Android Open Source Project                                                                   | Apache 2.0           | [Link↗](https://developer.android.com/jetpack/androidx/releases/media3)            |
| Mesa                                  | Copyright © The Mesa Authors                                                                                  | MIT License          | [Link↗](https://mesa3d.org/)                                                       |
| MobileGlues                           | Copyright © 2025-2026 MobileGL-Dev                                                                           | LGPL-2.1 License     | [Link↗](https://github.com/MobileGL-Dev/MobileGlues)                             |
| MMKV                                  | Copyright © 2018 THL A29 Limited, a Tencent company.                                                          | BSD 3-Clause License | [Link↗](https://github.com/Tencent/MMKV)                                           |
| Navigation 3                          | Copyright © The Android Open Source Project                                                                   | Apache 2.0           | [Link↗](https://developer.android.com/jetpack/androidx/releases/navigation3)       |
| NG-GL4ES                              | Copyright © 2016-2018 Sebastien Chevalier; Copyright © 2013-2016 Ryan Hileman; Copyright (c) 2025-2026 BZLZHH | MIT License          | [Link↗](https://github.com/BZLZHH/NG-GL4ES)                                        |
| OkHttp                                | Copyright © 2019 Square, Inc.                                                                                 | Apache 2.0           | [Link↗](https://github.com/square/okhttp)                                          |
| Okio                                  | Copyright © 2013 Square, Inc.                                                                                 | Apache 2.0           | [Link↗](https://square.github.io/okio/)                                            |
| OpenNBT                               | Copyright © 2013-2021 Steveice10.                                                                             | MIT License          | [Link↗](https://github.com/GeyserMC/OpenNBT)                                       |
| Process Phoenix                       | Copyright © 2015 Jake Wharton                                                                                 | Apache 2.0           | [Link↗](https://github.com/JakeWharton/ProcessPhoenix)                             |
| proxy-client-android                  | -                                                                                                             | LGPL-3.0 License     | [Link↗](https://github.com/TouchController/TouchController)                        |
| Reorderable                           | Copyright © 2023 Calvin Liang                                                                                 | Apache 2.0           | [Link↗](https://github.com/Calvin-LL/Reorderable)                                  |
| sdl2-compat                           | Copyright (C) 2026 Sam Lantinga <slouken@libsdl.org>                                                          | Zlib License         | [Link↗](https://github.com/libsdl-org/sdl2-compat)                                 |
| SDL3                                  | Copyright (C) 1997-2026 Sam Lantinga <slouken@libsdl.org>                                                     | Zlib License         | [Link↗](https://github.com/libsdl-org/SDL)                                         |
| SimpleFPEWrapper                      | Copyright © 2026 MobileGL-Dev                                                                                | LGPL-3.0 License     | [Link↗](https://github.com/MobileGL-Dev/SimpleFPEWrapper)                        |
| sora-editor                           | Copyright (C) 2020-2026  Rosemoe                                                                              | LGPL-2.1 License     | [Link↗](https://github.com/Rosemoe/sora-editor)                                    |
| StringFog                             | Copyright © 2016-2023, Megatron King                                                                          | Apache 2.0           | [Link↗](https://github.com/MegatronKing/StringFog)                                 |
| tm4e (TextMate for Eclipse)           | Copyright © Eclipse Foundation                                                                                | EPL-2.0 License      | [Link↗](https://github.com/eclipse-tm4e/tm4e)                                      |
| XZ for Java                           | Copyright © The XZ for Java authors and contributors                                                          | 0BSD License         | [Link↗](https://tukaani.org/xz/java.html)                                          |
