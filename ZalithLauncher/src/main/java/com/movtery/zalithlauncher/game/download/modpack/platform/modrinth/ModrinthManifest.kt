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

package com.movtery.zalithlauncher.game.download.modpack.platform.modrinth

import com.google.gson.annotations.SerializedName
import com.movtery.zalithlauncher.game.download.modpack.platform.PackManifest

class ModrinthManifest(
    @SerializedName("game")
    val game: String,
    @SerializedName("formatVersion")
    val formatVersion: Int,
    @SerializedName("versionId")
    val versionId: String,
    @SerializedName("name")
    val name: String,
    /** optional */
    @SerializedName("summary")
    val summary: String? = null,
    @SerializedName("files")
    val files: Array<ManifestFile>,
    @SerializedName("dependencies")
    val dependencies: Map<String, String>
): PackManifest {
    class ManifestFile(
        @SerializedName("path")
        val path: String,
        @SerializedName("hashes")
        val hashes: Hashes,
        /** optional */
        @SerializedName("env")
        val env: Env? = null,
        @SerializedName("downloads")
        val downloads: Array<String>,
        @SerializedName("fileSize")
        val fileSize: Long
    ) {
        class Hashes(
            @SerializedName("sha1")
            val sha1: String,
            @SerializedName("sha512")
            val sha512: String
        )

        class Env(
            @SerializedName("client")
            val client: String? = null,
            @SerializedName("server")
            val server: String? = null
        )
    }
}