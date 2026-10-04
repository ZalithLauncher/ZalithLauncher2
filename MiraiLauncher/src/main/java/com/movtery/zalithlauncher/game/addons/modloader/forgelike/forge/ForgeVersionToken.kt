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

package com.movtery.zalithlauncher.game.addons.modloader.forgelike.forge

import com.movtery.zalithlauncher.game.addons.modloader.forgelike.forge.ForgeVersionToken.ForgeFile
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ForgeVersionToken(
    @SerialName("branch")
    val branch: String? = null,
    @SerialName("version")
    val version: String,
    @SerialName("modified")
    val modified: String,
    @SerialName("files")
    val files: List<ForgeFile>
) {
    @Serializable
    data class ForgeFile(
        @SerialName("category")
        val category: String,
        @SerialName("format")
        val format: String,
        @SerialName("hash")
        val hash: String? = null
    )
}

fun ForgeFile.isInstallerJar() = category == "installer" && format == "jar"
fun ForgeFile.isUniversalZip() = category == "universal" && format == "zip"
fun ForgeFile.isClientZip() = category == "client" && format == "zip"