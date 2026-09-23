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

import kotlin.math.roundToInt

/** 网格的最小列数 */
const val MIN_GRID_COLUMNS = 4

/** 单元格边长的设计目标值（dp） */
const val DEFAULT_TARGET_CELL_SIZE = 20f

/**
 * 网格几何信息。
 * [columns] 列数恒为偶数，保证卡片可严格对齐半宽等对称分割；
 * [cellSize] 为单个正方形单元格的边长（dp），
 * 网格首尾两端与容器边缘严格对齐。
 */
data class GridGeometry(
    val columns: Int,
    val cellSize: Float
)

/**
 * 依据容器宽度计算网格：
 * 以 [targetCellSize] 为目标细分出偶数列，宽度余数平摊进每个单元格，
 * 使单元格边长保持在目标值附近、网格边缘与容器边缘对齐。
 */
fun computeGridGeometry(
    widthDp: Float,
    targetCellSize: Float = DEFAULT_TARGET_CELL_SIZE
): GridGeometry {
    if (widthDp <= 0f) return GridGeometry(MIN_GRID_COLUMNS, targetCellSize)
    val evenColumns = (widthDp / targetCellSize / 2f)
        .roundToInt()
        .coerceAtLeast(MIN_GRID_COLUMNS / 2) * 2
    return GridGeometry(columns = evenColumns, cellSize = widthDp / evenColumns)
}
