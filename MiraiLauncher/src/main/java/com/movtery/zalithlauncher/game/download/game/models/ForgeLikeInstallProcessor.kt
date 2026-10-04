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

class ForgeLikeInstallProcessor(
    @SerializedName("sides")
    private val sides: List<String>?,
    @SerializedName("jar")
    private val jar: String,
    @SerializedName("classpath")
    private val classpath: List<String>?,
    @SerializedName("args")
    private val args: List<String>?,
    @SerializedName("outputs")
    private val outputs: Map<String, String>?
) {
    fun isSide(side: String): Boolean {
        return sides == null || sides.contains(side)
    }

    fun getJar(): LibraryComponents {
        return fromDescriptor(this.jar)
    }

    fun getClasspath(): List<LibraryComponents> {
        return classpath?.map { fromDescriptor(it) } ?: emptyList()
    }

    fun getArgs(): List<String> {
        return args ?: emptyList()
    }

    fun getOutputs(): Map<String, String> {
        return outputs ?: emptyMap()
    }
}