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

package com.movtery.zalithlauncher.game.download.assets.platform.curseforge.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class CurseForgeFingerprintsMatches(
    @SerialName("data")
    val data: Result
) {
    @Serializable
    data class Result(
        @SerialName("isCacheBuilt")
        val isCacheBuilt: Boolean,
        @SerialName("exactMatches")
        val exactMatches: List<FingerprintMatch>? = null,
        @SerialName("exactFingerprints")
        val exactFingerprints: List<Long>? = null,
        @SerialName("partialMatches")
        val partialMatches: List<FingerprintMatch>? = null,
        @SerialName("partialMatchFingerprints")
        val partialMatchFingerprints: JsonElement? = null,
        @SerialName("additionalProperties")
        val additionalProperties: List<Long>? = null,
        @SerialName("installedFingerprints")
        val installedFingerprints: List<Long>? = null,
        @SerialName("unmatchedFingerprints")
        val unmatchedFingerprints: List<Long>? = null
    ) {
        @Serializable
        data class FingerprintMatch(
            @SerialName("id")
            val id: Int,
            @SerialName("file")
            val file: CurseForgeFile,
            @SerialName("latestFiles")
            val latestFiles: List<CurseForgeFile>
        )
    }
}