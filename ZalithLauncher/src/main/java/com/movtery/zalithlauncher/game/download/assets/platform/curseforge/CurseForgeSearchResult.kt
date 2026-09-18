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

package com.movtery.zalithlauncher.game.download.assets.platform.curseforge

import com.movtery.zalithlauncher.game.download.assets.platform.PlatformClasses
import com.movtery.zalithlauncher.game.download.assets.platform.PlatformSearchResult
import com.movtery.zalithlauncher.game.download.assets.platform.curseforge.models.CurseForgeData
import com.movtery.zalithlauncher.game.download.assets.platform.curseforge.models.CurseForgePagination
import com.movtery.zalithlauncher.game.download.assets.platform.curseforge.models.isApproved
import com.movtery.zalithlauncher.game.download.assets.platform.searchRankWithChineseBias
import com.movtery.zalithlauncher.game.download.assets.utils.getTranslations
import com.movtery.zalithlauncher.ui.screens.content.download.assets.elements.AssetsPage
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class CurseForgeSearchResult(
    /**
     * 响应数据
     */
    @SerialName("data")
    private val data: Array<CurseForgeData>,

    /**
     * 响应分页信息
     */
    @SerialName("pagination")
    private val pagination: CurseForgePagination
): PlatformSearchResult {
    override fun getAssetsPage(classes: PlatformClasses): AssetsPage {
        val mcmodData = data.mapNotNull { data0 ->
            if (!data0.isApproved()) return@mapNotNull null
            data0 to classes.getTranslations().getModBySlugId(data0.slug)
        }
        val pageSize = pagination.pageSize
        val isLastPage = pagination.resultCount < pageSize ||
                (pagination.index + pagination.resultCount) >= pagination.totalCount

        return AssetsPage(
            pageNumber = pagination.index / pageSize + 1,
            pageIndex = pagination.index,
            totalPage = ((pagination.totalCount + pageSize - 1) / pageSize).toInt(),
            isLastPage = isLastPage,
            data = mcmodData
        )
    }

    override fun processChineseSearchResults(
        searchFilter: String,
        classes: PlatformClasses
    ): PlatformSearchResult {
        val newData = data.filter { it.isApproved() }
            .searchRankWithChineseBias(searchFilter, classes) { it.slug }
            .toTypedArray()
        return CurseForgeSearchResult(newData, pagination)
    }
}