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

package com.movtery.zalithlauncher.game.download.assets.favorites

import android.os.Parcelable
import com.movtery.zalithlauncher.game.download.assets.platform.PlatformClasses
import kotlinx.parcelize.Parcelize

/**
 * 收藏的项目本地缓存数据
 * @param projectId 项目在平台上的Id
 * @param iconUrl 项目图标链接
 * @param title 项目名
 * @param description 项目描述
 * @param authors 作者列表
 * @param classes 项目类型
 * @param followTime 收藏时间戳（ms）
 */
@Parcelize
class FavoriteProject(
    val projectId: String,
    val iconUrl: String? = null,
    val title: String,
    val description: String = "",
    val authors: List<String> = emptyList(),
    val classes: PlatformClasses,
    val followTime: Long
) : Parcelable
