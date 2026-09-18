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

package com.movtery.zalithlauncher.game.addons.modloader.forgelike.neoforge.models

import com.movtery.zalithlauncher.game.addons.modloader.forgelike.neoforge.NeoForgeVersion
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class BMCLAPIMaven(
    @SerialName("files")
    val files: List<File>,
    @SerialName("name")
    val name: String = "",
    @SerialName("type")
    val type: String = "",
    @Transient
    val isLegacy: Boolean = false
): NeoForgeMergeableMaven<BMCLAPIMaven> {
    @Serializable
    data class File(
        @SerialName("contentLength")
        val contentLength: Int? = null,
        @SerialName("contentType")
        val contentType: String? = null,
        @SerialName("lastModifiedTime")
        val lastModifiedTime: Double? = null,
        @SerialName("name")
        val name: String,
        @SerialName("type")
        val type: String
    )

    override fun plus(maven: BMCLAPIMaven): List<NeoForgeVersion> {
        return this.files.mapNotNull { file ->
            val versionId = file.name
            if (file.type != "DIRECTORY" || versionId.contains("maven", true)) return@mapNotNull null
            if (isVersionInvalid(versionId)) return@mapNotNull null
            NeoForgeVersion(versionId, this.isLegacy)
        } + maven.files.mapNotNull { file ->
            val versionId = file.name
            if (file.type != "DIRECTORY" || versionId.contains("maven", true)) return@mapNotNull null
            if (isVersionInvalid(versionId)) return@mapNotNull null
            NeoForgeVersion(versionId, maven.isLegacy)
        }
    }
}