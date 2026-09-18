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

package com.movtery.zalithlauncher.library

import com.movtery.zalithlauncher.R

/**
 * 库使用的协议信息
 * @param name 名称
 * @param raw 协议文本
 */
data class License(
    val name: String,
    val raw: Int
)

/**
 * Apache License 2.0
 * @see <a href="http://www.apache.org/licenses/LICENSE-2.0.txt">Apache License 2.0</a>
 */
val LICENSE_APACHE_2 = License("Apache License 2.0", R.raw.apache_license_2)
