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

package com.movtery.zalithlauncher.game.version.mod.meta

import com.google.gson.annotations.SerializedName

data class LiteModMetadata(
    @SerializedName("name")
    val name: String,
    @SerializedName("version")
    val version: String,
    @SerializedName("mcversion")
    val mcversion: String,
    @SerializedName("revision")
    val revision: String? = null,
    @SerializedName("author")
    val author: String? = null,
    @SerializedName("classTransformerClasses")
    val classTransformerClasses: Array<String>? = null,
    @SerializedName("description")
    val description: String? = null,
    @SerializedName("modpackName")
    val modpackName: String? = null,
    @SerializedName("modpackVersion")
    val modpackVersion: String? = null,
    @SerializedName("checkUpdateUrl")
    val checkUpdateUrl: String? = null,
    @SerializedName("updateURI")
    val updateURI: String? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as LiteModMetadata
        return classTransformerClasses.contentEquals(other.classTransformerClasses)
    }

    override fun hashCode(): Int {
        return classTransformerClasses?.contentHashCode() ?: 0
    }
}