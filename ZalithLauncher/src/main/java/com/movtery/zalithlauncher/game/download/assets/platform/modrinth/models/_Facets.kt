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

package com.movtery.zalithlauncher.game.download.assets.platform.modrinth.models

import kotlinx.serialization.json.Json

/**
 * Modrinth搜索过滤器
 */
interface ModrinthFacet {
    /**
     * 过滤器名称
     */
    fun facetName(): String

    /**
     * 过滤器值
     */
    fun facetValue(): String

    /**
     * 转换为Modrinth接受的格式
     */
    fun describe(): String? = "${facetName()}:${facetValue()}"
}

/**
 * Minecraft 版本过滤器
 */
class VersionFacet(val version: String) : ModrinthFacet {
    override fun facetValue(): String = version
    override fun facetName(): String = "versions"
}

/**
 * 项目类型
 */
enum class ProjectTypeFacet : ModrinthFacet {
    MOD {
        override fun facetValue(): String = "mod"
    },

    MODPACK {
        override fun facetValue(): String = "modpack"
    },

    RESOURCE_PACK {
        override fun facetValue(): String = "resourcepack"
    },

    SHADER {
        override fun facetValue(): String = "shader"
    };

    override fun facetName(): String = "project_type"
}

/**
 * 转换为Modrinth接受的格式
 */
fun List<ModrinthFacet>.toFacetsString(): String {
    val rawFacets = this.map { listOfNotNull(it.describe()) }
    return Json.encodeToString(rawFacets)
}