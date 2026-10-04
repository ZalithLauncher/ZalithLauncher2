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

package com.movtery.zalithlauncher.game.download.game.models

/**
 * [Reference HMCL](https://github.com/HMCL-dev/HMCL/blob/6e05b5ee58e67cd40e58c6f6002f3599897ca358/HMCLCore/src/main/java/org/jackhuang/hmcl/game/Artifact.java#L75-L91)
 */
fun fromDescriptor(descriptor: String): LibraryComponents {
    val arr = descriptor.split(":", limit = 4).toMutableList()
    if (arr.size != 3 && arr.size != 4) {
        throw IllegalArgumentException("Artifact name is malformed")
    }

    var extension: String? = null
    val last = arr.size - 1
    val splitted = arr[last].split("@")
    when (splitted.size) {
        2 -> {
            arr[last] = splitted[0]
            extension = splitted[1]
        }
        in 3..Int.MAX_VALUE -> throw IllegalArgumentException("Artifact name is malformed")
    }

    return LibraryComponents(
        arr[0].replace('\\', '/'),
        arr[1],
        arr[2],
        if (arr.size >= 4) arr[3] else "",
        extension ?: "jar"
    )
}

fun LibraryComponents.toPath(): String {
    val rawName = "$artifactId-$version".let { raw ->
        if (classifier.isNullOrEmpty()) raw
        else "$raw-$classifier"
    }
    val fileName = "$rawName.$extension"

    return "${groupId.replace('.', '/')}/$artifactId/$version/$fileName"
}