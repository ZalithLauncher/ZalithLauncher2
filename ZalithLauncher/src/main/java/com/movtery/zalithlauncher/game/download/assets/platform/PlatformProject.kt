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

package com.movtery.zalithlauncher.game.download.assets.platform

/**
 * 平台项目实现
 */
interface PlatformProject {
    /**
     * 所属平台
     */
    fun platform(): Platform

    /**
     * 该项目在平台上的Id
     */
    fun platformId(): String

    /**
     * 该项目的类型
     */
    fun platformClasses(defaultClasses: PlatformClasses): PlatformClasses

    /**
     * 该项目的别名
     */
    fun platformSlug(): String

    /**
     * 该项目的图标链接
     */
    fun platformIconUrl(): String?

    /**
     * 该项目在平台上的标题名称
     */
    fun platformTitle(): String

    /**
     * 该项目在平台上的描述
     */
    fun platformSummary(): String?

    /**
     * 该项目在平台上的主要作者
     */
    fun platformAuthor(): String?

    /**
     * 该项目在平台上的作者列表
     */
    fun platformAuthors(): List<String> = listOfNotNull(platformAuthor()?.takeIf { it.isNotBlank() })

    /**
     * 该项目在平台上的总下载量
     */
    fun platformDownloadCount(): Long

    /**
     * 在平台上的收藏数量（Modrinth）
     */
    fun platformFollows(): Long?

    /**
     * 在平台上标注的模组加载器信息
     */
    fun platformModLoaders(): List<PlatformDisplayLabel>?

    /**
     * 该项目在平台上是否仍然可用
     */
    fun platformAvailable(): Boolean = true

    /**
     * 检查项目类别是否受支持
     */
    fun checkClasses()

    /**
     * 在平台上标注的类别信息
     */
    fun platformCategories(classes: PlatformClasses): List<PlatformFilterCode>?

    /**
     * 该项目的所有相关链接
     */
    fun platformUrls(defaultClasses: PlatformClasses): Urls

    /**
     * 该项目在平台上上传的所有截图
     */
    fun platformScreenshots(): List<Screenshot>

    /**
     * 资源项目各类外链
     * @param projectUrl 平台项目链接
     * @param sourceUrl 源代码仓库链接
     * @param issuesUrl 议题链接
     * @param wikiUrl wiki链接
     */
    class Urls(
        val projectUrl: String? = null,
        val sourceUrl: String? = null,
        val issuesUrl: String? = null,
        val wikiUrl: String? = null
    )

    /**
     * 屏幕截图
     * @param imageUrl 图片链接
     * @param title 截图标题
     * @param description 截图描述
     */
    class Screenshot(
        val imageUrl: String,
        val title: String? = null,
        val description: String? = null
    )
}

/**
 * 所有的链接是否都为null
 */
fun PlatformProject.Urls.isAllNull(): Boolean {
    return projectUrl == null &&
            sourceUrl == null &&
            issuesUrl == null &&
            wikiUrl   == null
}