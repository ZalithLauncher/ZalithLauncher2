# Nev Launcher

**Nev Launcher** adalah launcher untuk menjalankan [Minecraft: Java Edition](https://www.minecraft.net/) di **perangkat Android**, dibuat oleh **NestMC**. Tampilannya bertema merah bergaya gaming, dibangun dengan **Jetpack Compose** dan **Material Design 3**, dan memakai [PojavLauncher](https://github.com/PojavLauncherTeam/PojavLauncher/tree/v3_openjdk/app_pojavlauncher/src/main/jni) sebagai inti peluncur.

> [!IMPORTANT]
> Nev Launcher adalah **versi modifikasi tidak resmi** dari [Zalith Launcher 2](https://github.com/ZalithLauncher/ZalithLauncher2) (oleh MovTery dan kontributor). Proyek ini tidak berafiliasi dengan Zalith Launcher, PojavLauncher, Mojang, maupun Microsoft.


## ✨ Fitur

* **Tampilan baru:** tema merah, menu samping di layar utama, dan menu tab di bagian atas pada layar Unduh, Pengaturan, dan Kelola Versi.
* **Nev Server Hub:** daftar server dengan pencarian, filter lokasi, jumlah pemain online, dan mode permainan, serta Favorit dan penambahan server sendiri.
* **Menu dalam game:** overlay FPS, koordinat X/Y/Z, arah mata angin, dan Menu Game yang bisa dibesarkan atau dikecilkan.
* **Perekaman layar:** rekam, jeda, lanjut, dan simpan, dengan suara game dan mikrofon, serta Noise Cancelling.
* **Crash Analyzer:** membantu membaca penyebab crash.
* **Render Distance Smooth:** membantu menjaga performa saat render distance tinggi.
* **Lemari pakaian dan cape:** pasang cape pada skin untuk akun offline maupun Microsoft.
* **Layout kontrol bawaan** dan berbagai gaya kontrol siap pakai.
* Fitur bawaan Zalith Launcher 2 lainnya tetap tersedia.


## 🌐 Bahasa dan Terjemahan

Terjemahan bawaan berasal dari proyek Zalith Launcher 2 yang dikelola lewat [Weblate](https://hosted.weblate.org/projects/zalithlauncher2). Terima kasih kepada semua kontributor terjemahan.


## 📦 Cara Membangun (Pengembang)

> Bagian ini untuk yang ingin mengembangkan atau membangun sendiri aplikasinya.

### Persyaratan

* Android Studio Bumblebee atau lebih baru
* Android SDK:
    * **API minimum**: 26
    * **API target**: 35
* JDK 11

### Langkah

```bash
git clone https://github.com/NestMCC/NevLauncher.git
# Buka proyek dengan Android Studio lalu build,
# atau jalankan workflow build di tab Actions pada GitHub.
```


## 📜 Lisensi

Kode proyek ini berlisensi **[GPL-3.0](LICENSE)**.

Nev Launcher adalah turunan dari Zalith Launcher 2 dan tetap mengikuti ketentuan tambahan lisensinya (GPLv3 pasal 7):

1. Versi modifikasi harus memakai nama yang berbeda dari program asli dan ditandai sebagai **versi modifikasi tidak resmi**. Nama aplikasi dapat diubah di [gradle.properties](./NevLauncher/gradle.properties).
2. Pemberitahuan hak cipta yang ditampilkan program tidak boleh dihapus.

Hak cipta kode asli: © MovTery dan kontributor Zalith Launcher 2.


## 🙏 Kredit

* **NestMC** — pembuat Nev Launcher
* **MovTery dan kontributor** — [Zalith Launcher 2](https://github.com/ZalithLauncher/ZalithLauncher2)
* **PojavLauncher Team** — inti peluncur


## Proyek Open Source yang Digunakan

Perangkat lunak ini memakai pustaka open source berikut:

| Library                               | Copyright                                                                                                     | License              | Link                                                                             |
|---------------------------------------|---------------------------------------------------------------------------------------------------------------|----------------------|----------------------------------------------------------------------------------|
| androidx-appcompat                    | Copyright © The Android Open Source Project                                                                   | Apache 2.0           | [Tautan↗](https://developer.android.com/jetpack/androidx/releases/appcompat)         |
| androidx-constraintlayout-compose     | Copyright © The Android Open Source Project                                                                   | Apache 2.0           | [Tautan↗](https://developer.android.com/develop/ui/compose/layouts/constraintlayout) |
| androidx-webkit                       | Copyright © The Android Open Source Project                                                                   | Apache 2.0           | [Tautan↗](https://developer.android.com/jetpack/androidx/releases/webkit)            |
| ANGLE                                 | Copyright 2018 The ANGLE Project Authors                                                                      | BSD 3-Clause License | [Tautan↗](http://angleproject.org/)                                                  |
| Apache Commons Codec                  | -                                                                                                             | Apache 2.0           | [Tautan↗](https://commons.apache.org/proper/commons-codec)                           |
| Apache Commons Compress               | -                                                                                                             | Apache 2.0           | [Tautan↗](https://commons.apache.org/proper/commons-compress)                        |
| Apache Commons IO                     | -                                                                                                             | Apache 2.0           | [Tautan↗](https://commons.apache.org/proper/commons-io)                              |
| ByteHook                              | Copyright © 2020-2024 ByteDance, Inc.                                                                         | MIT License          | [Tautan↗](https://github.com/bytedance/bhook)                                        |
| BuildKeys                             | Copyright © 2026 MovTery                                                                                      | Apache 2.0           | [Tautan↗](https://github.com/MovTery/BuildKeys)                                      |
| Coil Compose                          | Copyright © 2025 Coil Contributors                                                                            | Apache 2.0           | [Tautan↗](https://github.com/coil-kt/coil)                                           |
| Coil Gifs                             | Copyright © 2025 Coil Contributors                                                                            | Apache 2.0           | [Tautan↗](https://github.com/coil-kt/coil)                                           |
| Coil SVG                              | Copyright © 2025 Coil Contributors                                                                            | Apache 2.0           | [Tautan↗](https://github.com/coil-kt/coil)                                           |
| Fishnet                               | Copyright © 2025 Kyant                                                                                        | Apache 2.0           | [Tautan↗](https://github.com/Kyant0/Fishnet)                                         |
| gl4es_extra_extra                     | Copyright © 2016-2018 Sebastien Chevalier; Copyright (c) 2013-2016 Ryan Hileman                               | MIT License          | [Tautan↗](https://github.com/PojavLauncherTeam/gl4es_extra_extra)                    |
| Gson                                  | Copyright © 2008 Google Inc.                                                                                  | Apache 2.0           | [Tautan↗](https://github.com/google/gson)                                            |
| kotlinx.coroutines                    | Copyright © 2000-2020 JetBrains s.r.o.                                                                        | Apache 2.0           | [Tautan↗](https://github.com/Kotlin/kotlinx.coroutines)                              |
| ktor-client-content-negotiation       | Copyright © 2000-2023 JetBrains s.r.o.                                                                        | Apache 2.0           | [Tautan↗](https://ktor.io)                                                           |
| ktor-client-core                      | Copyright © 2000-2023 JetBrains s.r.o.                                                                        | Apache 2.0           | [Tautan↗](https://ktor.io)                                                           |
| ktor-client-okhttp                    | Copyright © 2000-2023 JetBrains s.r.o.                                                                        | Apache 2.0           | [Tautan↗](https://ktor.io)                                                           |
| ktor-http                             | Copyright © 2000-2023 JetBrains s.r.o.                                                                        | Apache 2.0           | [Tautan↗](https://ktor.io)                                                           |
| ktor-serialization-kotlinx-json       | Copyright © 2000-2023 JetBrains s.r.o.                                                                        | Apache 2.0           | [Tautan↗](https://ktor.io)                                                           |
| LWJGL - Lightweight Java Game Library | Copyright © 2012-present Lightweight Java Game Library All rights reserved.                                   | BSD 3-Clause License | [Tautan↗](https://github.com/LWJGL/lwjgl3)                                           |
| material-color-utilities              | Copyright 2021 Google LLC                                                                                     | Apache 2.0           | [Tautan↗](https://github.com/material-foundation/material-color-utilities)           |
| Maven Artifact                        | Copyright © The Apache Software Foundation                                                                    | Apache 2.0           | [Tautan↗](https://github.com/apache/maven/tree/maven-3.9.9/maven-artifact)           |
| Media3                                | Copyright © The Android Open Source Project                                                                   | Apache 2.0           | [Tautan↗](https://developer.android.com/jetpack/androidx/releases/media3)            |
| Mesa                                  | Copyright © The Mesa Authors                                                                                  | MIT License          | [Tautan↗](https://mesa3d.org/)                                                       |
| MobileGlues                           | Copyright © 2025-2026 MobileGL-Dev                                                                           | LGPL-2.1 License     | [Tautan↗](https://github.com/MobileGL-Dev/MobileGlues)                         |
| MMKV                                  | Copyright © 2018 THL A29 Limited, a Tencent company.                                                          | BSD 3-Clause License | [Tautan↗](https://github.com/Tencent/MMKV)                                           |
| Navigation 3                          | Copyright © The Android Open Source Project                                                                   | Apache 2.0           | [Tautan↗](https://developer.android.com/jetpack/androidx/releases/navigation3)       |
| NG-GL4ES                              | Copyright © 2016-2018 Sebastien Chevalier; Copyright © 2013-2016 Ryan Hileman; Copyright (c) 2025-2026 BZLZHH | MIT License          | [Tautan↗](https://github.com/BZLZHH/NG-GL4ES)                                        |
| OkHttp                                | Copyright © 2019 Square, Inc.                                                                                 | Apache 2.0           | [Tautan↗](https://github.com/square/okhttp)                                          |
| Okio                                  | Copyright © 2013 Square, Inc.                                                                                 | Apache 2.0           | [Tautan↗](https://square.github.io/okio/)                                            |
| OpenNBT                               | Copyright © 2013-2021 Steveice10.                                                                             | MIT License          | [Tautan↗](https://github.com/GeyserMC/OpenNBT)                                       |
| Process Phoenix                       | Copyright © 2015 Jake Wharton                                                                                 | Apache 2.0           | [Tautan↗](https://github.com/JakeWharton/ProcessPhoenix)                             |
| proxy-client-android                  | -                                                                                                             | LGPL-3.0 License     | [Tautan↗](https://github.com/TouchController/TouchController)                        |
| Reorderable                           | Copyright © 2023 Calvin Liang                                                                                 | Apache 2.0           | [Tautan↗](https://github.com/Calvin-LL/Reorderable)                                  |
| sdl2-compat                           | Copyright (C) 2026 Sam Lantinga <slouken@libsdl.org>                                                          | Zlib License         | [Tautan↗](https://github.com/libsdl-org/sdl2-compat)                                 |
| SDL3                                  | Copyright (C) 1997-2026 Sam Lantinga <slouken@libsdl.org>                                                     | Zlib License         | [Tautan↗](https://github.com/libsdl-org/SDL)                                         |
| SimpleFPEWrapper                      | Copyright © 2026 MobileGL-Dev                                                                                | LGPL-3.0 License     | [Tautan↗](https://github.com/MobileGL-Dev/SimpleFPEWrapper)                    |
| sora-editor                           | Copyright (C) 2020-2026  Rosemoe                                                                              | LGPL-2.1 License     | [Tautan↗](https://github.com/Rosemoe/sora-editor)                                    |
| StringFog                             | Copyright © 2016-2023, Megatron King                                                                          | Apache 2.0           | [Tautan↗](https://github.com/MegatronKing/StringFog)                                 |
| tm4e (TextMate for Eclipse)           | Copyright © Eclipse Foundation                                                                                | EPL-2.0 License      | [Tautan↗](https://github.com/eclipse-tm4e/tm4e)                                      |
| XZ for Java                           | Copyright © The XZ for Java authors and contributors                                                          | 0BSD License         | [Tautan↗](https://tukaani.org/xz/java.html)                                          |