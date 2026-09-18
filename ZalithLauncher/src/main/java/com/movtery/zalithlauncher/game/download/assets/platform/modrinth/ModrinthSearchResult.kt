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

import com.movtery.zalithlauncher.game.download.assets.platform.Platform
import com.movtery.zalithlauncher.game.download.assets.platform.PlatformClasses
import com.movtery.zalithlauncher.game.download.assets.platform.PlatformDisplayLabel
import com.movtery.zalithlauncher.game.download.assets.platform.PlatformFilterCode
import com.movtery.zalithlauncher.game.download.assets.platform.PlatformSearchData
import com.movtery.zalithlauncher.game.download.assets.platform.PlatformSearchResult
import com.movtery.zalithlauncher.game.download.assets.platform.modrinth.models.ModrinthModLoaderCategory
import com.movtery.zalithlauncher.game.download.assets.platform.modrinth.models.ModrinthSide
import com.movtery.zalithlauncher.game.download.assets.platform.modrinth.models.MonetizationStatus
import com.movtery.zalithlauncher.game.download.assets.platform.modrinth.models.mapModrinthCategory
import com.movtery.zalithlauncher.game.download.assets.platform.searchRankWithChineseBias
import com.movtery.zalithlauncher.game.download.assets.utils.getTranslations
import com.movtery.zalithlauncher.ui.screens.content.download.assets.elements.AssetsPage
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Modrinth 搜索得到的项目返回值
 */
@Serializable
class ModrinthSearchResult(
    /**
     * 搜索得到的项目 **required**
     */
    @SerialName("hits")
    val hits: Array<ModrinthProject>,

    /**
     * 查询跳过的结果数 **required**
     */
    @SerialName("offset")
    val offset: Int,

    /**
     * 查询返回的结果数 **required**
     */
    @SerialName("limit")
    val limit: Int,

    /**
     * 与查询匹配的结果总数 **required**
     */
    @SerialName("total_hits")
    val totalHits: Int
): PlatformSearchResult {
    @Serializable
    class ModrinthProject(
        /**
         * 项目唯一标识 ID **required**
         */
        @SerialName("project_id")
        val projectId: String,

        /**
         * 项目类型 **required**
         */
        @SerialName("project_type")
        val projectType: String,

        /**
         * 项目简洁字符串标识符 **un-required**
         */
        @SerialName("slug")
        val slug: String? = null,

        /**
         * 项目的作者的用户名 **required**
         */
        @SerialName("author")
        val author: String,

        /**
         * 项目的标题 **un-required**
         */
        @SerialName("title")
        val title: String? = null,

        /**
         * 项目的描述介绍 **un-required**
         */
        @SerialName("description")
        val description: String? = null,

        /**
         * 项目具有的类别的列表 **un-required**
         */
        @SerialName("categories")
        val categories: Array<String>? = null,

        /**
         * 项目具有的非次要类别的列表 **un-required**
         */
        @SerialName("display_categories")
        val displayCategories: Array<String>? = null,

        /**
         * 项目支持的 Minecraft 版本列表  **required**
         */
        @SerialName("versions")
        val versions: Array<String>,

        /**
         * 项目的下载总数 **required**
         */
        @SerialName("downloads")
        val downloads: Long,

        /**
         * 关注项目的用户总数 **required**
         */
        @SerialName("follows")
        val follows: Long,

        /**
         * 项目图标的 URL **un-required**
         */
        @SerialName("icon_url")
        val iconUrl: String? = null,

        /**
         * 将项目添加到搜索的日期 **required**
         */
        @SerialName("date_created")
        val dateCreated: String,

        /**
         * 上次修改项目的日期 **required**
         */
        @SerialName("date_modified")
        val dateModified: String,

        /**
         * **un-required**
         */
        @SerialName("latest_version")
        val latestVersion: String? = null,

        /**
         * 项目的 SPDX 许可证 ID **required**
         */
        @SerialName("license")
        val license: String,

        /**
         * 项目的客户端支持 **un-required**
         */
        @SerialName("client_side")
        val clientSide: ModrinthSide? = null,

        /**
         * 项目的服务器端支持 **un-required**
         */
        @SerialName("server_side")
        val serverSide: ModrinthSide? = null,

        /**
         * 附加到项目的所有图库图像 **un-required**
         */
        @SerialName("gallery")
        val gallery: Array<String>? = null,

        /**
         * 项目的特色图库图片 **un-required**
         */
        @SerialName("featured_gallery")
        val featuredGallery: String? = null,

        /**
         * 项目的 RGB 颜色，从项目图标提取 **un-required**
         */
        @SerialName("color")
        val color: Int? = null,

        /**
         * 与此项目关联的审核线程的 ID **un-required**
         */
        @SerialName("thread_id")
        val threadId: String? = null,

        /**
         * **un-required**
         */
        @SerialName("monetization_status")
        val monetizationStatus: MonetizationStatus? = null,
    ) : PlatformSearchData {
        override fun platform(): Platform = Platform.MODRINTH

        override fun platformId(): String = projectId

        override fun platformTitle(): String = title ?: ""

        override fun platformDescription(): String = description ?: ""

        override fun platformAuthor(): String = author

        override fun platformIconUrl(): String? = iconUrl

        override fun platformDownloadCount(): Long = downloads

        override fun platformFollows(): Long = follows

        override fun platformModLoaders(): List<PlatformDisplayLabel>? {
            val modloaders = displayCategories
                ?.mapNotNull { string ->
                    ModrinthModLoaderCategory.entries.find { it.facetValue() == string }
                }
                ?.toSet()
                ?.takeIf { it.isNotEmpty() }

            return modloaders?.sortedWith { o1, o2 -> o1.index() - o2.index() }
        }

        override fun platformCategories(classes: PlatformClasses): List<PlatformFilterCode>? {
            val categories = displayCategories
                ?.mapNotNull { string ->
                    string.mapModrinthCategory(classes)
                }
                ?.toSet()
                ?.takeIf { it.isNotEmpty() }
                ?: categories
                    ?.take(4) //没有主要类别，则展示前4个
                    ?.mapNotNull { string ->
                        string.mapModrinthCategory(classes)
                    }
                    ?.toSet()
                    ?.takeIf { it.isNotEmpty() }

            return categories?.sortedWith { o1, o2 -> o1.index() - o2.index() }
        }
    }

    override fun getAssetsPage(classes: PlatformClasses): AssetsPage {
        val mcmodData = hits.map {
            it to classes.getTranslations().getModBySlugId(it.slug)
        }

        return AssetsPage(
            pageNumber = this.offset / this.limit + 1,
            pageIndex = this.offset,
            totalPage = (this.totalHits + this.limit - 1) / this.limit,
            isLastPage = (this.offset + this.limit) >= this.totalHits,
            data = mcmodData
        )
    }

    override fun processChineseSearchResults(
        searchFilter: String,
        classes: PlatformClasses
    ): PlatformSearchResult {
        val newHits = hits.toList()
            .searchRankWithChineseBias(searchFilter, classes) { it.slug }
            .toTypedArray()
        return ModrinthSearchResult(newHits, offset, limit, totalHits)
    }
}
