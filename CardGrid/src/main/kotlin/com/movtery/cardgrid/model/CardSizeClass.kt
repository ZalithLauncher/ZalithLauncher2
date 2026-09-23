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

/**
 * 卡片依据自身跨度推导出的形态分类，供卡片内容按形态切换显示，
 * 高度档位按跨度（格）的绝对数值划分
 */
enum class CardSizeClass(val maxSpan: Int) {
    COMPACT(4),
    SMALL(5),
    MEDIUM(7),
    LARGE(9),
    EXTRA_LARGE(Int.MAX_VALUE);

    companion object {
        /** 依据高度跨度（格）推导所处档位 */
        fun fromSpan(span: Int): CardSizeClass =
            entries.first { span <= it.maxSpan }

        /** 依据宽度占网格宽度的比例推导所处档位 */
        fun fromFraction(width: Int, columns: Int): CardSizeClass {
            val classes = entries
            if (columns <= 0 || width <= 0) return COMPACT
            val fraction = width / columns.toFloat()
            val index = (fraction * classes.size).toInt().coerceIn(0, classes.lastIndex)
            return classes[index]
        }
    }
}

/** 卡片形态记录 */
data class CardSize(
    val width: CardSizeClass,
    val height: CardSizeClass
)

/** 由卡片跨度推导宽、高各自所处的形态档位 */
fun deriveSizeClass(
    width: Int,
    height: Int,
    columns: Int
): CardSize = CardSize(
    width = CardSizeClass.fromFraction(width, columns),
    height = CardSizeClass.fromSpan(height)
)
