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
package com.movtery.zalithlauncher.game.versioninfo.models

import com.google.gson.annotations.SerializedName

data class VersionManifest(
    @SerializedName("latest")
    val latest: Latest,
    @SerializedName("versions")
    val versions: List<Version>
) {
    data class Latest(
        @SerializedName("release")
        val release: String,
        @SerializedName("snapshot")
        val snapshot: String
    )

    data class Version(
        @SerializedName("id")
        val id: String,
        @SerializedName("type")
        val type: String,
        @SerializedName("url")
        val url: String,
        @SerializedName("time")
        val time: String,
        @SerializedName("releaseTime")
        val releaseTime: String,
        @SerializedName("sha1")
        val sha1: String? = null,
        @SerializedName("complianceLevel")
        val complianceLevel: Int? = null
    )
}
