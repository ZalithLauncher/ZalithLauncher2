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

package com.movtery.cardgrid.model

import androidx.compose.ui.unit.IntOffset

/**
 * 网格卡片的布局矩形。
 * 坐标与尺寸均以网格单元格为单位，锚点为卡片左上角，
 * y 轴向下为正，纵向（行数）不设上限。
 */
data class CardRect(
    val id: String,
    val x: Int,
    val y: Int,
    val width: Int,
    val height: Int
) {
    val right: Int get() = x + width
    val bottom: Int get() = y + height

    fun positionAt(position: IntOffset): CardRect = copy(x = position.x, y = position.y)

    fun intersects(other: CardRect): Boolean =
        x < other.right && other.x < right && y < other.bottom && other.y < bottom
}
