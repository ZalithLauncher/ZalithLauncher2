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

package com.movtery.zalithlauncher.ui.control.mouse

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * 鼠标指针热点存储类型，保存热点的X、Y的百分比坐标
 */
@Parcelize
data class CursorHotspot(
    val xPercent: Int,
    val yPercent: Int
): Parcelable

/**
 * 默认：居中的指针热点
 */
val CENTER_HOTSPOT = CursorHotspot(xPercent = 50, yPercent = 50)

/**
 * 默认：左上角的指针热点
 */
val LEFT_TOP_HOTSPOT = CursorHotspot(xPercent = 0, yPercent = 0)