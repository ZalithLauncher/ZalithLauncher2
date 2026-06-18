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

import com.movtery.zalithlauncher.game.download.assets.platform.Platform
import com.movtery.zalithlauncher.game.download.assets.platform.PlatformClasses
import com.movtery.zalithlauncher.game.download.assets.platform.PlatformDisplayLabel
import com.movtery.zalithlauncher.game.download.assets.platform.PlatformFilterCode
import com.movtery.zalithlauncher.game.download.assets.platform.PlatformProject
import com.movtery.zalithlauncher.game.download.assets.platform.UnsupportedClassesException
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class ModrinthSingleProject(
    /** 项目简洁字符串标识符 */
    @SerialName("slug")
    val slug: String,

    /** 项目的标题或名称 */
    @SerialName("title")
    val title: String,

    /** 项目简介 */
    @SerialName("description")
    val description: String,

    /** 项目具有的类别的列表 */
    @SerialName("categories")
    val categories: Array<String>,

    /** 项目的客户端支持 */
    @SerialName("client_side")
    val clientSide: ModrinthSide,

    /** 项目的服务器端支持 */
    @SerialName("server_side")
    val serverSide: ModrinthSide,

    /** 项目的长篇描述 */
    @SerialName("body")
    val body: String,

    /** 项目状态 */
    @SerialName("status")
    val status: String,

    /** 提交以供审核或安排项目发布时的请求状态 */
    @SerialName("requested_status")
    val requestedStatus: String? = null,

    /** 可搜索但非主要类别的列表 */
    @SerialName("additional_categories")
    val additionalCategories: Array<String>,

    /** 一个可选链接，指向在何处提交项目的 bug 或 issue */
    @SerialName("issues_url")
    val issuesUrl: String? = null,

    /** 指向项目源代码的可选链接 */
    @SerialName("source_url")
    val sourceUrl: String? = null,

    /** 指向项目 Wiki 页面或其他相关信息的可选链接 */
    @SerialName("wiki_url")
    val wikiUrl: String? = null,

    /** 项目 Discord 的可选邀请链接 */
    @SerialName("discord_url")
    val discordUrl: String? = null,

    /** 该项目的捐赠链接列表 */
    @SerialName("donation_urls")
    val donationUrls: Array<DonationUrl>,

    /** 项目的项目类型 */
    @SerialName("project_type")
    val projectType: String,

    /** 项目的下载总数 */
    @SerialName("downloads")
    val downloads: Long,

    /** 项目图标的 URL */
    @SerialName("icon_url")
    val iconUrl: String? = null,

    /** 项目的 RGB 颜色，从项目图标提取 */
    @SerialName("color")
    val color: Int? = null,

    /** 与此项目关联的审核线程的 ID */
    @SerialName("thread_id")
    val threadId: String,

    @SerialName("monetization_status")
    val monetizationStatus: MonetizationStatus,

    /** 项目的 ID，编码为 base62 字符串 */
    @SerialName("id")
    val id: String,

    /** 拥有此项目所有权的团队的 ID */
    @SerialName("team")
    val team: String? = null,

    /** 拥有此项目所有权的组织的 ID */
    @SerialName("organization")
    val organization: String? = null,

    /** 指向项目详细描述的链接。始终为 null，仅用于旧版兼容性。 */
    @SerialName("body_url")
    val bodyUrl: String? = null,

    /** 审核者发送的有关项目的消息 */
    @SerialName("moderator_message")
    val moderatorMessage: ModeratorMessage? = null,

    /** 项目的发布日期 */
    @SerialName("published")
    val published: String,

    /** 上次更新项目的日期 */
    @SerialName("updated")
    val updated: String,

    /** 项目状态设置为已批准状态的日期 */
    @SerialName("approved")
    val approved: String? = null,

    /** 项目状态提交给审核者的日期 */
    @SerialName("queued")
    val queued: String? = null,

    /** 关注项目的用户总数 */
    @SerialName("followers")
    val followers: Long,

    /** 项目的许可证 */
    @SerialName("license")
    val license: License,

    /** 项目的版本 ID 列表（除非 draft 状态，否则永远不会为空） */
    @SerialName("versions")
    val versions: Array<String>,

    /** 项目支持的所有游戏版本的列表 */
    @SerialName("game_versions")
    val gameVersions: Array<String>,

    /** 项目支持的所有加载器的列表 */
    @SerialName("loaders")
    val loaders: Array<String>,

    /** 已上传到项目图库的图像列表 */
    @SerialName("gallery")
    val gallery: Array<Gallery>
): PlatformProject {
    @Serializable
    class DonationUrl(
        /** 捐赠平台的 ID */
        @SerialName("id")
        val id: String,

        /** 此链接指向的捐赠平台 */
        @SerialName("platform")
        val platform: String,

        /** 捐赠平台和用户的 URL */
        @SerialName("url")
        val url: String,
    )

    @Serializable
    class ModeratorMessage(
        /** 审核者为项目留下的消息 */
        @SerialName("message")
        val message: String,

        /** 审核者为项目留下的消息的较长正文 */
        @SerialName("body")
        val body: String? = null
    )

    @Serializable
    class License(
        /** 项目的 SPDX 许可证 ID */
        @SerialName("id")
        val id: String,

        /** 许可证的长名称 */
        @SerialName("name")
        val name: String,

        /** 此许可证的 URL */
        @SerialName("url")
        val url: String? = null
    )

    @Serializable
    class Gallery(
        /** 图库图像的 URL */
        @SerialName("url")
        val url: String,

        /** 这张图片是否在画廊中被推荐展示 */
        @SerialName("featured")
        val featured: Boolean,

        /** 图库图像的标题 */
        @SerialName("title")
        val title: String? = null,

        /** 图库图像的描述 */
        @SerialName("description")
        val description: String? = null,

        /** 创建图库图像的日期和时间 */
        @SerialName("created")
        val created: String,

        /** 图库图像的顺序 */
        @SerialName("ordering")
        val ordering: Int
    )

    override fun platform(): Platform = Platform.MODRINTH

    override fun platformId(): String = id

    override fun platformClasses(defaultClasses: PlatformClasses): PlatformClasses {
        return projectType.mapModrinthType()?.platform ?: defaultClasses
    }

    override fun platformSlug(): String = slug

    override fun platformIconUrl(): String? = iconUrl

    override fun platformTitle(): String = title

    override fun platformSummary(): String = description

    override fun platformAuthor(): String? = null

    override fun platformDownloadCount(): Long = downloads

    override fun platformFollows(): Long = followers

    override fun platformModLoaders(): List<PlatformDisplayLabel>? {
        val modloaders = loaders.mapNotNull { string ->
                ModrinthModLoaderCategory.entries.find { it.facetValue() == string }
            }.toSet().takeIf { it.isNotEmpty() }

        return modloaders?.sortedWith { o1, o2 -> o1.index() - o2.index() }
    }

    override fun checkClasses() {
        //fixme: 插件类、数据包类项目被标为模组类别
        if (projectType.mapModrinthType() == null) throw UnsupportedClassesException(projectType)
    }

    override fun platformCategories(classes: PlatformClasses): List<PlatformFilterCode>? {
        return categories.take(4) //没有主要类别，则展示前4个
            .mapNotNull { string ->
                string.mapModrinthCategory(classes)
            }
            .toSet()
            .takeIf { it.isNotEmpty() }
            ?.sortedWith { o1, o2 -> o1.index() - o2.index() }
    }

    override fun platformUrls(defaultClasses: PlatformClasses): PlatformProject.Urls {
        val classes = projectType.mapModrinthType()?.platform ?: defaultClasses
        return PlatformProject.Urls(
            projectUrl = "https://modrinth.com/${classes.modrinth!!.facetValue()}/${slug}",
            sourceUrl = sourceUrl,
            issuesUrl = issuesUrl,
            wikiUrl = wikiUrl
        )
    }

    override fun platformScreenshots(): List<PlatformProject.Screenshot> {
        return gallery.map { gallery ->
            PlatformProject.Screenshot(
                imageUrl = gallery.url,
                title = gallery.title,
                description = gallery.description
            )
        }
    }
}

/**
 * @return 该项目是否公开可见
 */
fun ModrinthSingleProject.isPublic(): Boolean {
    return when (this.status) {
        "approved", "archived" -> true
        else -> false
    }
}