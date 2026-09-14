# BedroomLauncher
![Downloads](https://img.shields.io/github/downloads/voltageStudiosOfficial/BedroomLauncher/total)

> [!IMPORTANT]
> This project is **completely separate** from [ZalithLauncher2](https://github.com/ZalithLauncher2), [LeviLaunchroid](https://github.com/LiteLDev/LeviLaunchroid), and [PojavLauncher](https://github.com/PojavLauncherTeam/PojavLauncher).

**Bedroom Launcher** = Minecraft Java on your phone, but prettier. 

We took PojavLauncher’s engine, slapped on a clean Material 3 UI, and made it actually fun to use. Java works right now. Bedrock support is coming later via LeviLaunchroid so you can play both worlds from one app.

**Play nice:** Use Bedroom Launcher with Mojang’s EULA in mind. You need to own Minecraft to play. We also include a **Dev Mode** toggle that bypasses some restrictions for testing/modding — use it responsibly and only with accounts you own.

Official website coming soon.  
Heads up: If you find a “Bedroom Launcher” site that isn’t linked here, it’s fake. We don’t run ads or sketchy mirrors. Stay safe!

## ✨ What works now

- **Java Edition**: Full Microsoft login, mods, resource packs, custom JVM flags
- **Modern UI**: Jetpack Compose + Material You. Dark mode, smooth animations, no clutter
- **Instances**: Separate profiles for each modpack/setup

## 🗺️ Roadmap for 1.0 Stable

| Feature | Status |
| --- | --- |
| **Java Edition Support** | ✅ Ready to play |
| **Bedrock Edition Support** | 🔜 Coming via LeviLaunchroid |
| **ely.by Support** | 🔜 Planned |

Java first, Bedrock next. ely.by login is on the list too.

## 📦 Build it yourself

### Requirements

* Android Studio **Bumblebee** or newer
* Android SDK:
    * **Minimum:** Android 8.0 Oreo (API 26)
    * **Target:** Android 16 Baklava (API 36)
* JDK 17

### Build Steps

```bash
git clone https://github.com/voltageStudiosOfficial/BedroomLauncher
cd BedroomLauncher
./gradlew assembleRelease
```

## 📜 License

**GPL-3.0** - Use it, fork it, mod it. Just follow the rules below.

### Fork Rules (GPLv3 Section 7)

1. Change the name. Your fork **cannot** be called "BedroomLauncher", "BL", or anything confusingly similar.
2. Say it’s unofficial. Put “Unofficial Modified Version” on the startup/main screen.
3. Don’t remove copyright notices.

## ⚖️ Disclaimer

Unofficial launcher. Not made by or endorsed by Mojang, Microsoft, Levi, LeviLaunchroid, ZalithLauncher2, or ely.by. You need to own Minecraft to play. Dev Mode exists for testing only — you are responsible for complying with Mojang’s EULA. [LeviLaunchroid](https://github.com/LiteLDev/LeviLaunchroid) handles our future Bedrock support and is its own project.

---

**License:** GPL-3.0  
**Maintainer:** @voltageStudiosOfficial  

## Open Source Libraries and Licenses

This software uses the following open source libraries:

| Library | Copyright | License | Official Link |
| --- | --- | --- | --- |
| androidx-constraintlayout-compose | Copyright © The Android Open Source Project | Apache 2.0 | [Link](https://developer.android.com/develop/ui/compose/layouts/constraintlayout) |
| androidx-material-icons-core | Copyright © The Android Open Source Project | Apache 2.0 | [Link](https://developer.android.com/jetpack/androidx/releases/compose-material) |
| androidx-material-icons-extended | Copyright © The Android Open Source Project | Apache 2.0 | [Link](https://developer.android.com/jetpack/androidx/releases/compose-material) |
| ANGLE | Copyright 2018 The ANGLE Project Authors | BSD 3-Clause License | [Link](http://angleproject.org/) |
| Apache Commons Codec | - | Apache 2.0 | [Link](https://commons.apache.org/proper/commons-codec) |
| Apache Commons Compress | - | Apache 2.0 | [Link](https://commons.apache.org/proper/commons-compress) |
| Apache Commons IO | - | Apache 2.0 | [Link](https://commons.apache.org/proper/commons-io) |
| ByteHook | Copyright © 2020-2024 ByteDance, Inc. | MIT License | [Link](https://github.com/bytedance/bhook) |
| BuildKeys | Copyright © 2026 MovTery | Apache 2.0 | [Link](https://github.com/MovTery/BuildKeys) |
| Coil Compose | Copyright © 2025 Coil Contributors | Apache 2.0 | [Link](https://github.com/coil-kt/coil) |
| Coil Gifs | Copyright © 2025 Coil Contributors | Apache 2.0 | [Link](https://github.com/coil-kt/coil) |
| Coil SVG | Copyright © 2025 Coil Contributors | Apache 2.0 | [Link](https://github.com/coil-kt/coil) |
| Fishnet | Copyright © 2025 Kyant | Apache 2.0 | [Link](https://github.com/Kyant0/Fishnet) |
| gl4es_extra_extra | Copyright © 2016-2018 Sebastien Chevalier; Copyright (c) 2013-2016 Ryan Hileman | MIT License | [Link](https://github.com/PojavLauncherTeam/gl4es_extra_extra) |
| Gson | Copyright © 2008 Google Inc. | Apache 2.0 | [Link](https://github.com/google/gson) |
| kotlinx.coroutines | Copyright © 2000-2020 JetBrains s.r.o. | Apache 2.0 | [Link](https://github.com/Kotlin/kotlinx.coroutines) |
| ktor-client-cio | Copyright © 2000-2023 JetBrains s.r.o. | Apache 2.0 | [Link](https://ktor.io) |
| ktor-client-content-negotiation | Copyright © 2000-2023 JetBrains s.r.o. | Apache 2.0 | [Link](https://ktor.io) |
| ktor-client-core | Copyright © 2000-2023 JetBrains s.r.o. | Apache 2.0 | [Link](https://ktor.io) |
| ktor-http | Copyright © 2000-2023 JetBrains s.r.o. | Apache 2.0 | [Link](https://ktor.io) |
| ktor-serialization-kotlinx-json | Copyright © 2000-2023 JetBrains s.r.o. | Apache 2.0 | [Link](https://ktor.io) |
| LWJGL - Lightweight Java Game Library | Copyright © 2012-present Lightweight Java Game Library All rights reserved. | BSD 3-Clause License | [Link](https://github.com/LWJGL/lwjgl3) |
| material-color-utilities | Copyright 2021 Google LLC | Apache 2.0 | [Link](https://github.com/material-foundation/material-color-utilities) |
| Maven Artifact | Copyright © The Apache Software Foundation | Apache 2.0 | [Link](https://github.com/apache/maven/tree/maven-3.9.9/maven-artifact) |
| Media3 | Copyright © The Android Open Source Project | Apache 2.0 | [Link](https://developer.android.com/jetpack/androidx/releases/media3) |
| Mesa | Copyright © The Mesa Authors | MIT License | [Link](https://mesa3d.org/) |
| MMKV | Copyright © 2018 THL A29 Limited, a Tencent company. | BSD 3-Clause License | [Link](https://github.com/Tencent/MMKV) |
| Navigation 3 | Copyright © The Android Open Source Project | Apache 2.0 | [Link](https://developer.android.com/jetpack/androidx/releases/navigation3) |
| NBT | Copyright © 2016 - 2020 Querz | MIT License | [Link](https://github.com/Querz/NBT) |
| NG-GL4ES | Copyright © 2016-2018 Sebastien Chevalier; Copyright © 2013-2016 Ryan Hileman; Copyright (c) 2025-2026 BZLZHH | MIT License | [Link](https://github.com/BZLZHH/NG-GL4ES) |
| OkHttp | Copyright © 2019 Square, Inc. | Apache 2.0 | [Link](https://github.com/square/okhttp) |
| Okio | Copyright © 2013 Square, Inc. | Apache 2.0 | [Link](https://square.github.io/okio/) |
| Process Phoenix | Copyright © 2015 Jake Wharton | Apache 2.0 | [Link](https://github.com/JakeWharton/ProcessPhoenix) |
| proxy-client-android | - | LGPL-3.0 License | [Link](https://github.com/TouchController/TouchController) |
| Reorderable | Copyright © 2023 Calvin Liang | Apache 2.0 | [Link](https://github.com/Calvin-LL/Reorderable) |
| skinview3d | Copyright © 2014-2018 Kent Rasmussen; Copyright © 2017-2022 Haowei Wen, Sean Boult and contributors | MIT License | [Link](https://github.com/bs-community/skinview3d) |
| sora-editor | Copyright © 1991, 1999 Free Software Foundation, Inc. | LGPL-2.1 License | [Link](https://github.com/Rosemoe/sora-editor) |
| StringFog | Copyright © 2016-2023, Megatron King | Apache 2.0 | [Link](https://github.com/MegatronKing/StringFog) |
| XZ for Java | Copyright © The XZ for Java authors and contributors | 0BSD License | [Link](https://tukaani.org/xz/java.html) |
