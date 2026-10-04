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

import java.io.File

/**
 * 平台资源搜索抽象类
 * @param platform 目标平台类型（仅作标识）
 * @param source 源（区分官方源、镜像源）名称，仅日志需要
 */
abstract class AbstractPlatformSearcher(
    val platform: Platform,
    val source: String
) {
    /**
     * 搜索资源结果列表
     */
    abstract suspend fun searchAssets(
        query: String,
        searchFilter: PlatformSearchFilter,
        platformClasses: PlatformClasses
    ): PlatformSearchResult

    /**
     * 获取单个项目的信息
     */
    abstract suspend fun getProject(
        projectID: String,
    ): PlatformProject

    /**
     * 获取单个项目的所有版本信息
     */
    abstract suspend fun getVersions(
        projectID: String,
        pageCallback: (chunk: Int, page: Int) -> Unit = { _, _ -> },
    ): List<PlatformVersion>

    /**
     * 通过本地文件的sha值尝试找到对应的版本信息
     */
    abstract suspend fun getVersionByLocalFile(
        file: File,
        sha1: String,
    ): PlatformVersion?
}