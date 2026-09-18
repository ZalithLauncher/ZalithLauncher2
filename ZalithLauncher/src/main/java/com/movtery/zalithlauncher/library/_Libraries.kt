/*
 * Zalith Launcher 2
 * Copyright (C) 2025 MovTery <movtery228@qq.com> and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/gpl-3.0.txt>.
 */

package com.movtery.zalithlauncher.library

import com.movtery.zalithlauncher.R

/**
 * Android Open Source Project
 * @see <a href="https://developer.android.com/topic/libraries/support-library">Android Open Source Project</a>
 */
private const val COPYRIGHT_AOSP = "Copyright © The Android Open Source Project"

/**
 * Ktor
 * @see <a href="https://ktor.io">Ktor</a>
 */
private const val COPYRIGHT_KTOR = "Copyright © 2000-2023 JetBrains s.r.o."

private const val LICENSE_MIT = "MIT License"
private const val LICENSE_LGPL_3 = "LGPL-3.0 License"
private const val LICENSE_AGPL_3 = "AGPL-3.0 License"
private const val LICENSE_BSD_3_CLAUSE = "BSD 3-Clause License"

private const val URL_KTOR = "https://ktor.io"

val libraryData = listOf(
    LibraryInfo("androidx-appcompat", COPYRIGHT_AOSP, LICENSE_APACHE_2, "https://developer.android.com/jetpack/androidx/releases/appcompat"),
    LibraryInfo("androidx-constraintlayout-compose", COPYRIGHT_AOSP, LICENSE_APACHE_2, "https://developer.android.com/develop/ui/compose/layouts/constraintlayout"),
    LibraryInfo("androidx-webkit", COPYRIGHT_AOSP, LICENSE_APACHE_2, "https://developer.android.com/jetpack/androidx/releases/webkit"),
    LibraryInfo("ANGLE", "Copyright 2018 The ANGLE Project Authors", License(LICENSE_BSD_3_CLAUSE, R.raw.angle_license), "http://angleproject.org/"),
    LibraryInfo("Apache Commons Codec", null, LICENSE_APACHE_2, "https://commons.apache.org/proper/commons-codec"),
    LibraryInfo("Apache Commons Compress", null, LICENSE_APACHE_2, "https://commons.apache.org/proper/commons-compress"),
    LibraryInfo("Apache Commons IO", null, LICENSE_APACHE_2, "https://commons.apache.org/proper/commons-io"),
    LibraryInfo("ByteHook", "Copyright © 2020-2024 ByteDance, Inc.", License(LICENSE_MIT, R.raw.bhook_license), "https://github.com/bytedance/bhook"),
    LibraryInfo("BuildKeys", "Copyright © 2026 MovTery", LICENSE_APACHE_2, "https://github.com/MovTery/BuildKeys"),
    LibraryInfo("Coil Compose", "Copyright © 2025 Coil Contributors", LICENSE_APACHE_2, "https://github.com/coil-kt/coil"),
    LibraryInfo("Coil Gifs", "Copyright © 2025 Coil Contributors", LICENSE_APACHE_2, "https://github.com/coil-kt/coil"),
    LibraryInfo("Coil SVG", "Copyright © 2025 Coil Contributors", LICENSE_APACHE_2, "https://github.com/coil-kt/coil"),
    LibraryInfo("Fishnet", "Copyright © 2025 Kyant", LICENSE_APACHE_2, "https://github.com/Kyant0/Fishnet"),
    LibraryInfo("gl4es_extra_extra", "Copyright © 2016-2018 Sebastien Chevalier; Copyright © 2013-2016 Ryan Hileman", License(LICENSE_MIT, R.raw.gl4es_license), "https://github.com/PojavLauncherTeam/gl4es_extra_extra"),
    LibraryInfo("Gson", "Copyright © 2008 Google Inc.", LICENSE_APACHE_2, "https://github.com/google/gson"),
    LibraryInfo("kotlinx.coroutines", "Copyright © 2000-2020 JetBrains s.r.o.", LICENSE_APACHE_2, "https://github.com/Kotlin/kotlinx.coroutines"),
    LibraryInfo("ktor-client-content-negotiation", COPYRIGHT_KTOR, LICENSE_APACHE_2, URL_KTOR),
    LibraryInfo("ktor-client-core", COPYRIGHT_KTOR, LICENSE_APACHE_2, URL_KTOR),
    LibraryInfo("ktor-client-okhttp", COPYRIGHT_KTOR, LICENSE_APACHE_2, URL_KTOR),
    LibraryInfo("ktor-http", COPYRIGHT_KTOR, LICENSE_APACHE_2, URL_KTOR),
    LibraryInfo("ktor-serialization-kotlinx-json", COPYRIGHT_KTOR, LICENSE_APACHE_2, URL_KTOR),
    LibraryInfo("language-textmate", "Copyright © 2020-2024 Rosemoe", License("LGPL-2.1 License", R.raw.sora_editor_license), "https://github.com/Rosemoe/sora-editor/tree/main/language-textmate"),
    LibraryInfo("LWJGL - Lightweight Java Game Library", "Copyright © 2012-present Lightweight Java Game Library All rights reserved.", License(LICENSE_BSD_3_CLAUSE, R.raw.lwjgl_license), "https://github.com/LWJGL/lwjgl3"),
    LibraryInfo("material-color-utilities", "Copyright 2021 Google LLC", LICENSE_APACHE_2, "https://github.com/material-foundation/material-color-utilities"),
    LibraryInfo("Maven Artifact", "Copyright © The Apache Software Foundation", LICENSE_APACHE_2, "https://github.com/apache/maven/tree/maven-3.9.9/maven-artifact"),
    LibraryInfo("Media3", COPYRIGHT_AOSP, LICENSE_APACHE_2, "https://developer.android.com/jetpack/androidx/releases/media3"),
    LibraryInfo("Mesa", "Copyright © The Mesa Authors", License(LICENSE_MIT, R.raw.mesa_license), "https://mesa3d.org/"),
    LibraryInfo("MMKV", "Copyright © 2018 THL A29 Limited, a Tencent company.", License(LICENSE_BSD_3_CLAUSE, R.raw.mmkv_license), "https://github.com/Tencent/MMKV"),
    LibraryInfo("Navigation 3", COPYRIGHT_AOSP, LICENSE_APACHE_2, "https://developer.android.com/jetpack/androidx/releases/navigation3"),
    LibraryInfo("NG-GL4ES", "Copyright © 2016-2018 Sebastien Chevalier; Copyright © 2013-2016 Ryan Hileman; Copyright © 2025-2026 BZLZHH", License(LICENSE_MIT, R.raw.ng_gl4es_license), "https://github.com/BZLZHH/NG-GL4ES"),
    LibraryInfo("OkHttp", "Copyright © 2019 Square, Inc.", LICENSE_APACHE_2, "https://github.com/square/okhttp"),
    LibraryInfo("Okio", "Copyright © 2013 Square, Inc.", LICENSE_APACHE_2, "https://square.github.io/okio/"),
    LibraryInfo("OpenNBT", "Copyright © 2013-2021 Steveice10.", License(LICENSE_MIT, R.raw.opennbt_license), "https://github.com/GeyserMC/OpenNBT"),
    LibraryInfo("Process Phoenix", "Copyright © 2015 Jake Wharton", LICENSE_APACHE_2, "https://github.com/JakeWharton/ProcessPhoenix"),
    LibraryInfo("proxy-client-android", null, License(LICENSE_LGPL_3, R.raw.lgpl_3_license), "https://github.com/TouchController/TouchController"),
    LibraryInfo("Reorderable", "Copyright © 2023 Calvin Liang", LICENSE_APACHE_2, "https://github.com/Calvin-LL/Reorderable"),
    LibraryInfo("sdl2-compat", "Copyright (C) 2026 Sam Lantinga <slouken@libsdl.org>", License("Zlib License", R.raw.sdl2_compat_license), "https://github.com/libsdl-org/sdl2-compat"),
    LibraryInfo("SDL3", "Copyright (C) 1997-2026 Sam Lantinga <slouken@libsdl.org>", License("Zlib License", R.raw.sdl3_license), "https://github.com/libsdl-org/SDL"),
    LibraryInfo("skinview3d", "Copyright © 2014-2018 Kent Rasmussen; Copyright © 2017-2022 Haowei Wen, Sean Boult and contributors", License(LICENSE_MIT, R.raw.skinview3d_license), "https://github.com/bs-community/skinview3d"),
    LibraryInfo("sora-editor", "Copyright (C) 2020-2026  Rosemoe", License("LGPL-2.1 License", R.raw.sora_editor_license), "https://github.com/Rosemoe/sora-editor"),
    LibraryInfo("Terracotta", "Copyright © 2025 Burning_TNT", License(LICENSE_AGPL_3, net.burningtnt.terracotta.R.raw.terracotta_license), "https://github.com/burningtnt/Terracotta"),
    LibraryInfo("tm4e (TextMate for Eclipse)", "Copyright © Eclipse Foundation", License("EPL-2.0 License", R.raw.tm4e_license), "https://github.com/eclipse-tm4e/tm4e"),
    LibraryInfo("XZ for Java", "Copyright © The XZ for Java authors and contributors", License("0BSD License", R.raw.xz_java_license), "https://tukaani.org/xz/java.html")
)
