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

package com.movtery.zalithlauncher.game.download.assets.platform.modrinth

import com.movtery.zalithlauncher.game.download.assets.platform.PlatformSortField
import com.movtery.zalithlauncher.game.download.assets.platform.modrinth.models.ModrinthFacet
import com.movtery.zalithlauncher.game.download.assets.platform.modrinth.models.ProjectTypeFacet
import com.movtery.zalithlauncher.game.download.assets.platform.modrinth.models.toFacetsString
import io.ktor.http.Parameters

/**
 * [Modrinth api](https://docs.modrinth.com/api/operations/searchprojects/)
 */
data class ModrinthSearchRequest(
    /** 搜索词条 */
    val query: String = "",

    /** 应用于搜索的过滤器列表 */
    val facets: List<ModrinthFacet> = listOf(ProjectTypeFacet.MOD),

    /** 排序方式 */
    val index: PlatformSortField = PlatformSortField.RELEVANCE,

    /** 要跳过的结果页数（用于分页） */
    val offset: Int = 0,

    /** 要返回的结果页数，最大值为 100 */
    val limit: Int = 20
) {
    /**
     * 转换为 GET 参数
     */
    fun toParameters(): Parameters = Parameters.build {
        append("query", query)
        append("facets", facets.toFacetsString())
        append("limit", limit.toString())
        append("index", index.modrinth)
        append("offset", offset.toString())
    }
}