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

/**
 * NeoForged Maven
 */
@Serializable
data class NeoForgedMaven(
    @SerialName("isSnapshot")
    val isSnapshot: Boolean,
    @SerialName("versions")
    val versions: List<String>,
    @Transient
    val isLegacy: Boolean = false
): NeoForgeMergeableMaven<NeoForgedMaven> {

    override fun plus(maven: NeoForgedMaven): List<NeoForgeVersion> {
        return this.versions.mapNotNull { versionId ->
            if (isVersionInvalid(versionId)) return@mapNotNull null
            NeoForgeVersion(versionId, this.isLegacy)
        } + maven.versions.mapNotNull { versionId ->
            if (isVersionInvalid(versionId)) return@mapNotNull null
            NeoForgeVersion(versionId, maven.isLegacy)
        }
    }
}