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

import com.google.gson.annotations.SerializedName

data class LibraryComponents(
    @SerializedName("groupId")
    val groupId: String,
    @SerializedName("artifactId")
    val artifactId: String,
    @SerializedName("version")
    val version: String,
    @SerializedName("classifier")
    val classifier: String? = null,
    @SerializedName("extension")
    val extension: String = "jar"
) {
    val descriptor: String by lazy {
        // groupId:artifactId:version:classifier@extension
        "$groupId:$artifactId:$version".let {
            if (classifier.isNullOrEmpty()) it
            else "$it:$classifier"
        }.let {
            if (extension != "jar") "$it@$extension"
            else it
        }
    }

    override fun toString(): String {
        return descriptor
    }
}