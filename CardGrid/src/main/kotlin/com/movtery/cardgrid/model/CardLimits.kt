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

import com.movtery.cardgrid.model.CardLimits.Companion.DEFAULT


/**
 * 卡片的尺寸边界（单元格跨度），由卡片类型自行声明，
 * 未声明的卡片使用 [DEFAULT] 默认值。
 */
data class CardLimits(
    val minWidth: Int = DEFAULT_MIN_SPAN,
    val minHeight: Int = DEFAULT_MIN_SPAN,
    val maxWidth: Int = Int.MAX_VALUE,
    val maxHeight: Int = Int.MAX_VALUE
) {
    /**
     * 依据实际网格宽度归一化边界：
     * 最大跨度不超过网格，最小跨度不超过最大跨度。
     */
    fun clampedFor(columns: Int): CardLimits {
        val maxW = maxWidth.coerceAtMost(columns)
        val minW = minWidth.coerceAtMost(maxW)
        val maxH = maxHeight
        val minH = minHeight.coerceAtMost(maxH)
        return CardLimits(minWidth = minW, minHeight = minH, maxWidth = maxW, maxHeight = maxH)
    }

    fun clampWidth(width: Int): Int = width.coerceIn(minWidth, maxWidth)

    fun clampHeight(height: Int): Int = height.coerceIn(minHeight, maxHeight)

    companion object {
        /** 默认最小跨度：4×4（约 80dp） */
        const val DEFAULT_MIN_SPAN = 4

        /** 默认最大高度跨度：12 行（约 240dp） */
        const val DEFAULT_MAX_HEIGHT = 12

        val DEFAULT = CardLimits(maxHeight = DEFAULT_MAX_HEIGHT)
    }
}
