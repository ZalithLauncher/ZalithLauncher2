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

package com.movtery.zalithlauncher.setting.enums

import androidx.annotation.StringRes
import com.movtery.zalithlauncher.R

/**
 * 启动器主页类型
 */
enum class HomePageType(
    @field:StringRes
    val textRes: Int
) {
    /**
     * 空白主页
     */
    Blank(R.string.settings_launcher_home_page_type_blank),

    /**
     * 从本地加载
     */
    FromLocal(R.string.settings_launcher_home_page_type_local),

    /**
     * 从网络加载
     */
    FromURL(R.string.settings_launcher_home_page_type_url)
}