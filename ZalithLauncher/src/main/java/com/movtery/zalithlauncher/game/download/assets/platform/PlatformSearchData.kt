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
 * 平台的搜索结果单项数据实现
 */
interface PlatformSearchData {
    /**
     * 所属平台
     */
    fun platform(): Platform

    /**
     * 项目Id
     */
    fun platformId(): String

    /**
     * 在平台上的标题
     */
    fun platformTitle(): String

    /**
     * 在平台上的描述
     */
    fun platformDescription(): String

    /**
     * 在平台上的主要作者
     */
    fun platformAuthor(): String

    /**
     * 在平台上的作者列表
     */
    fun platformAuthors(): List<String> = listOfNotNull(platformAuthor().takeIf { it.isNotBlank() })

    /**
     * 图标链接
     */
    fun platformIconUrl(): String?

    /**
     * 在平台上的下载数量
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
     * 在平台上标注的类别信息
     */
    fun platformCategories(classes: PlatformClasses): List<PlatformFilterCode>?
}