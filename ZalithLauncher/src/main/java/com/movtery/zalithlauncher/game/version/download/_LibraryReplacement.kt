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

package com.movtery.zalithlauncher.game.version.download

import com.movtery.zalithlauncher.game.versioninfo.models.GameManifest

data class LibraryReplacement(
    val newName: String,
    val newPath: String,
    val newSha1: String,
    val newUrl: String
)

fun getLibraryReplacement(libraryName: String, versionParts: List<String>): LibraryReplacement? {
    val major = versionParts.getOrNull(0)?.toIntOrNull() ?: 0
    val minor = versionParts.getOrNull(1)?.toIntOrNull() ?: 0

    return when {
        libraryName.startsWith("net.java.dev.jna:jna:") -> {
            //如果版本已经达到5.13.0及以上，则不做处理
            if (major >= 5 && minor >= 13) null
            else LibraryReplacement(
                newName = "net.java.dev.jna:jna:5.13.0",
                newPath = "net/java/dev/jna/jna/5.13.0/jna-5.13.0.jar",
                newSha1 = "1200e7ebeedbe0d10062093f32925a912020e747",
                newUrl = "https://repo1.maven.org/maven2/net/java/dev/jna/jna/5.13.0/jna-5.13.0.jar"
            )
        }
        libraryName.startsWith("com.github.oshi:oshi-core:") -> {
            //仅对版本 6.2.0 进行修改
            if (major != 6 || minor != 2) null
            else LibraryReplacement(
                newName = "com.github.oshi:oshi-core:6.3.0",
                newPath = "com/github/oshi/oshi-core/6.3.0/oshi-core-6.3.0.jar",
                newSha1 = "9e98cf55be371cafdb9c70c35d04ec2a8c2b42ac",
                newUrl = "https://repo1.maven.org/maven2/com/github/oshi/oshi-core/6.3.0/oshi-core-6.3.0.jar"
            )
        }
        libraryName.startsWith("org.ow2.asm:asm-all:") -> {
            //如果主版本号不低于5，则不做处理
            if (major >= 5) null
            else LibraryReplacement(
                newName = "org.ow2.asm:asm-all:5.0.4",
                newPath = "org/ow2/asm/asm-all/5.0.4/asm-all-5.0.4.jar",
                newSha1 = "e6244859997b3d4237a552669279780876228909",
                newUrl = "https://repo1.maven.org/maven2/org/ow2/asm/asm-all/5.0.4/asm-all-5.0.4.jar"
            )
        }
        else -> null
    }
}

/**
 * 新版 SDL 绑定依赖与其同版本的 LWJGL Core。
 */
internal fun List<GameManifest.Library>.usesLwjglSdl(): Boolean = any { library ->
    library.name?.split(":")?.let { coordinates ->
        coordinates.size == 3 &&
            coordinates[0] == "org.lwjgl" &&
            coordinates[1] == "lwjgl-sdl"
    } == true
}

/**
 * @return 是否需要被过滤
 */
fun GameManifest.Library.filterLibrary(allowLwjglSdlClasses: Boolean = false): Boolean {
    val libraryName = name ?: return false
    val coordinates = libraryName.split(":")
    val isLwjglSdlClasses = coordinates.size == 3 &&
        coordinates[0] == "org.lwjgl" &&
        (coordinates[1] == "lwjgl" || coordinates[1] == "lwjgl-sdl") &&
        !isNative

    return when {
        coordinates.firstOrNull() == "org.lwjgl" -> !(allowLwjglSdlClasses && isLwjglSdlClasses)
        libraryName.contains("jinput-platform") -> true
        libraryName.contains("twitch-platform") -> true
        else -> false
    }
}
