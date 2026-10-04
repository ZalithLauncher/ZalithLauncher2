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

package com.movtery.zalithlauncher.game.download.modpack.platform.curseforge

import com.google.gson.annotations.SerializedName
import com.movtery.zalithlauncher.game.download.modpack.platform.PackManifest

class CurseForgeManifest(
    @SerializedName("manifestType")
    val manifestType: String,
    @SerializedName("manifestVersion")
    val manifestVersion: Int,
    @SerializedName("name")
    val name: String,
    @SerializedName("version")
    val version: String,
    @SerializedName("author")
    val author: String,
    @SerializedName("overrides")
    val overrides: String? = null,
    @SerializedName("minecraft")
    val minecraft: Minecraft,
    @SerializedName("files")
    val files: List<ManifestFile>
): PackManifest {
    data class Minecraft(
        @SerializedName("version")
        val gameVersion: String,
        @SerializedName("modLoaders")
        val modLoaders: List<ModLoader>,
        @SerializedName("recommendedRam")
        val recommendedRam: Int? = null
    ) {
        data class ModLoader(
            @SerializedName("id")
            val id: String,
            @SerializedName("primary")
            val primary: Boolean
        )
    }

    data class ManifestFile(
        @SerializedName("projectID")
        val projectID: Int,
        @SerializedName("fileID")
        val fileID: Int,
        @SerializedName("fileName")
        val fileName: String? = null,
        @SerializedName("url")
        val url: String? = null,
        @SerializedName("required")
        val required: Boolean
    ) {
        fun getFileUrl(): String? {
            return url ?: fileName?.let {
                "https://edge.forgecdn.net/files/${fileID / 1000}/${fileID % 1000}/$it"
            }
        }
    }
}