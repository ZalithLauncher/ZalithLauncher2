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

package com.movtery.zalithlauncher.game.addons.modloader.forgelike

import com.movtery.zalithlauncher.utils.string.isVersionEqualTo

class ForgeBuildVersion private constructor(
    val major: Int,
    val minor: Int,
    val build: Int,
    val revision: Int
) : Comparable<ForgeBuildVersion> {
    companion object {
        fun parse(versionString: String): ForgeBuildVersion {
            val parts = versionString.split('.', '-').mapNotNull { it.toIntOrNull() }
            return ForgeBuildVersion(
                parts.getOrElse(0) { 0 },
                parts.getOrElse(1) { 0 },
                parts.getOrElse(2) { 0 },
                parts.getOrElse(3) { 0 }
            )
        }
    }

    fun compareOptiFineRequired(requiredVersion: String): Boolean {
        return if ('.' in requiredVersion) {
            toString().isVersionEqualTo(requiredVersion)
        } else {
            revision.toString() == requiredVersion
        }
    }

    override fun compareTo(other: ForgeBuildVersion): Int {
        return compareValuesBy(
            this, other,
            { it.major },
            { it.minor },
            { it.build },
            { it.revision }
        )
    }

    override fun toString(): String {
        return "$major.$minor.$build.$revision"
    }
}