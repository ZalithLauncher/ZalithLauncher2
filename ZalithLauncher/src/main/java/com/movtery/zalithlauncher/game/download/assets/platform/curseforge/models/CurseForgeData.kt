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

import com.movtery.zalithlauncher.game.download.assets.platform.Platform
import com.movtery.zalithlauncher.game.download.assets.platform.PlatformClasses
import com.movtery.zalithlauncher.game.download.assets.platform.PlatformDisplayLabel
import com.movtery.zalithlauncher.game.download.assets.platform.PlatformFilterCode
import com.movtery.zalithlauncher.game.download.assets.platform.PlatformSearchData
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class CurseForgeData(
    /**
     * 项目 ID
     */
    @SerialName("id")
    val id: Int,

    /**
     * 项目所属游戏 ID
     */
    @SerialName("gameId")
    val gameId: Int,

    /**
     * 项目的名称
     */
    @SerialName("name")
    val name: String,

    /**
     * 出现在 URL 中的项目 slug
     */
    @SerialName("slug")
    val slug: String,

    /**
     * 该项目的相关链接
     */
    @SerialName("links")
    val links: Links,

    /**
     * 该项目的描述
     */
    @SerialName("summary")
    val summary: String,

    /**
     * 该项目的状态
     */
    @SerialName("status")
    val status: Int,

    /**
     * 该项目的总下载量
     */
    @SerialName("downloadCount")
    val downloadCount: Long,

    /**
     * 该项目是否包含在精选项目列表中
     */
    @SerialName("isFeatured")
    val isFeatured: Boolean,

    /**
     * 由项目作者指定的主要类别
     */
    @SerialName("primaryCategoryId")
    val primaryCategoryId: Int,

    /**
     * 项目相关的类别列表
     */
    @SerialName("categories")
    val categories: Array<Category>,

    /**
     * 这个项目所属的类 id
     */
    @SerialName("classId")
    val classId: Int? = null,

    /**
     * 项目的作者名单
     */
    @SerialName("authors")
    val authors: Array<Author>,

    /**
     * 项目的 Logo
     */
    @SerialName("logo")
    val logo: Asset? = null,

    /**
     * 项目的截图
     */
    @SerialName("screenshots")
    val screenshots: Array<Asset> = emptyArray(),

    /**
     * 项目的主文件 ID
     */
    @SerialName("mainFileId")
    val mainFileId: Int,

    /**
     * 项目的最新文件列表
     */
    @SerialName("latestFiles")
    val latestFiles: Array<CurseForgeFile>,

    /**
     * 项目的最新文件的文件相关详细信息列表
     */
    @SerialName("latestFilesIndexes")
    val latestFilesIndexes: Array<CurseForgeFileIndex>,

    /**
     * 该项目的最新抢先体验文件的文件相关详细信息列表
     */
    @SerialName("latestEarlyAccessFilesIndexes")
    val latestEarlyAccessFilesIndexes: Array<CurseForgeFileIndex>? = null,

    /**
     * 项目的创建日期
     */
    @SerialName("dateCreated")
    val dateCreated: String,

    /**
     * 上次修改项目的时间
     */
    @SerialName("dateModified")
    val dateModified: String,

    /**
     * 项目的发布日期
     */
    @SerialName("dateReleased")
    val dateReleased: String,

    /**
     * 是否允许分发项目
     */
    @SerialName("allowModDistribution")
    val allowModDistribution: Boolean? = null,

    /**
     * 游戏的 Mod 人气排名
     */
    @SerialName("gamePopularityRank")
    val gamePopularityRank: Int,

    /**
     * 该项目是否可供搜索
     * 当项目处于实验性状态、处于已删除状态或只有 Alpha 文件时，此值为 false
     */
    @SerialName("isAvailable")
    val isAvailable: Boolean,

    /**
     * 项目的支持数
     */
    @SerialName("thumbsUpCount")
    val thumbsUpCount: Int,

    /**
     * 项目的评分
     */
    @SerialName("rating")
    val rating: Double? = null
) : PlatformSearchData {
    @Serializable
    class Links(
        /**
         * 官方网站 URL
         */
        @SerialName("websiteUrl")
        val websiteUrl: String? = null,

        /**
         * WIKI URL
         */
        @SerialName("wikiUrl")
        val wikiUrl: String? = null,

        /**
         * 议题 URL
         */
        @SerialName("issuesUrl")
        val issuesUrl: String? = null,

        /**
         * 源代码 URL
         */
        @SerialName("sourceUrl")
        val sourceUrl: String? = null
    )

    @Serializable
    class Category(
        /**
         * 类别的 ID
         */
        @SerialName("id")
        val id: Int,

        /**
         * 类别所属的游戏 ID
         */
        @SerialName("gameId")
        val gameId: Int,

        /**
         * 类别的名称
         */
        @SerialName("name")
        val name: String,

        /**
         * URL 中显示的类别 slug
         */
        @SerialName("slug")
        val slug: String,

        /**
         * 类别的 URL
         */
        @SerialName("url")
        val url: String,

        /**
         * 类别的图标 URL
         */
        @SerialName("iconUrl")
        val iconUrl: String,

        /**
         * 类别的上次修改日期
         */
        @SerialName("dateModified")
        val dateModified: String,

        /**
         * 其他类别的顶级类别
         */
        @SerialName("isClass")
        val isClass: Boolean,

        /**
         * 类别的类 ID，即此类别所属的类
         */
        @SerialName("classId")
        val classId: Int? = null,

        /**
         * 此类别的父类别
         */
        @SerialName("parentCategoryId")
        val parentCategoryId: Int? = null,

        /**
         * 此类别的显示索引
         */
        @SerialName("displayIndex")
        val displayIndex: Int? = null
    )

    @Serializable
    class Author(
        @SerialName("id")
        val id: Int,

        @SerialName("name")
        val name: String,

        @SerialName("url")
        val url: String,

        @SerialName("avatarUrl")
        val avatarUrl: String? = null
    )

    @Serializable
    class Asset(
        @SerialName("id")
        val id: Int,

        @SerialName("modId")
        val modId: Int,

        @SerialName("title")
        val title: String,

        @SerialName("description")
        val description: String,

        @SerialName("thumbnailUrl")
        val thumbnailUrl: String,

        @SerialName("url")
        val url: String
    )

    override fun platform(): Platform = Platform.CURSEFORGE

    override fun platformId(): String = id.toString()

    override fun platformTitle(): String = name

    override fun platformDescription(): String = summary

    override fun platformAuthor(): String = authors[0].name

    override fun platformIconUrl(): String? = logo?.url

    override fun platformDownloadCount(): Long = downloadCount

    override fun platformFollows(): Long? = null

    override fun platformModLoaders(): List<PlatformDisplayLabel>? {
        return latestFilesIndexes.mapNotNull {
            it.modLoader //通过最新文件获取模组加载器信息
        }.toSet()
            .takeIf { it.isNotEmpty() }
            ?.sortedWith { o1, o2 -> o1.index() - o2.index() }
    }

    override fun platformCategories(classes: PlatformClasses): List<PlatformFilterCode>? {
        return categories.mapNotNull {
            it.id.toString().mapCurseForgeCategory(classes)
        }.toSet().takeIf { it.isNotEmpty() }
            ?.sortedWith { o1, o2 -> o1.index() - o2.index() }
    }
}

/**
 * @return 该模组是否可见
 */
fun CurseForgeData.isApproved(): Boolean = this.status == 4