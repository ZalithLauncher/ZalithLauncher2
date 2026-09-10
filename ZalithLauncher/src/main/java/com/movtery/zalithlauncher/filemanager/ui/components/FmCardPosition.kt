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

package com.movtery.zalithlauncher.filemanager.ui.components

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * 卡片在列表组中所处的方位，用于决定四个角各自使用大圆角还是小圆角
 */
enum class FmCardPosition {
    Top, TopStart, TopEnd,
    MiddleStart,
    MiddleEnd,
    Middle, Bottom, BottomStart, BottomEnd,
    MiddleBottomEnd,
    Single;

    companion object {
        /**
         * 根据条目在列表组中的位置推导方位
         * @param index 条目下标
         * @param count 组内条目总数
         * @param columns 列数，单列列表使用默认值
         */
        fun of(index: Int, count: Int, columns: Int = 1): FmCardPosition {
            if (count <= 1) return Single
            val cols = columns.coerceAtLeast(1)
            // 网格只有一行时，条目的上下边缘与组端暴露，邻接边使用小圆角
            if (count <= cols) {
                return when (index) {
                    0 -> MiddleStart
                    count - 1 -> MiddleEnd
                    else -> Middle
                }
            }
            val row = index / cols
            val col = index % cols
            val lastRow = (count - 1) / cols
            val lastCol = (count - 1) % cols

            val top = row == 0
            val bottom = row == lastRow
            val start = col == 0
            // 末行未占满时，由组内最后一个条目收拢右边缘
            val end = col == cols - 1 || (row == lastRow && col == lastCol)
            // 末行的前一排中，超出末行占有列的条目底部暴露在组边缘
            val gapBelow = row == lastRow - 1 && col > lastCol

            return when {
                top && start && end -> Top
                top && start -> TopStart
                top && end -> TopEnd
                top -> Top
                bottom && start && end -> Bottom
                bottom && start -> BottomStart
                bottom && end -> BottomEnd
                bottom -> Bottom
                gapBelow && end -> MiddleBottomEnd
                else -> Middle
            }
        }
    }
}

/**
 * 根据卡片方位组合四个角的圆角
 */
@Composable
fun rememberFmCardShape(
    position: FmCardPosition,
    outerShape: Dp = 28.dp,
    innerShape: Dp = 4.dp
): Shape {
    return remember(position, outerShape, innerShape) {
        when (position) {
            FmCardPosition.Top -> RoundedCornerShape(
                topStart = outerShape,
                topEnd = outerShape,
                bottomStart = innerShape,
                bottomEnd = innerShape
            )
            FmCardPosition.TopStart -> RoundedCornerShape(
                topStart = outerShape,
                topEnd = innerShape,
                bottomStart = innerShape,
                bottomEnd = innerShape
            )
            FmCardPosition.TopEnd -> RoundedCornerShape(
                topStart = innerShape,
                topEnd = outerShape,
                bottomStart = innerShape,
                bottomEnd = innerShape
            )
            FmCardPosition.Middle -> RoundedCornerShape(innerShape)
            FmCardPosition.MiddleStart -> RoundedCornerShape(
                topStart = outerShape,
                topEnd = innerShape,
                bottomStart = outerShape,
                bottomEnd = innerShape
            )
            FmCardPosition.MiddleEnd -> RoundedCornerShape(
                topStart = innerShape,
                topEnd = outerShape,
                bottomStart = innerShape,
                bottomEnd = outerShape
            )
            FmCardPosition.MiddleBottomEnd -> RoundedCornerShape(
                topStart = innerShape,
                topEnd = innerShape,
                bottomStart = innerShape,
                bottomEnd = outerShape
            )
            FmCardPosition.Bottom -> RoundedCornerShape(
                topStart = innerShape,
                topEnd = innerShape,
                bottomStart = outerShape,
                bottomEnd = outerShape
            )
            FmCardPosition.BottomStart -> RoundedCornerShape(
                topStart = innerShape,
                topEnd = innerShape,
                bottomStart = outerShape,
                bottomEnd = innerShape
            )
            FmCardPosition.BottomEnd -> RoundedCornerShape(
                topStart = innerShape,
                topEnd = innerShape,
                bottomStart = innerShape,
                bottomEnd = outerShape
            )
            FmCardPosition.Single -> RoundedCornerShape(outerShape)
        }
    }
}
