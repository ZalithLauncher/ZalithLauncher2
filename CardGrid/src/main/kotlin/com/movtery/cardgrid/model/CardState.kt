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

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** 卡片与用户的交互状态 */
enum class CardInteraction { Idle, Adjusting, Dragging, Resizing }

/**
 * 提供给卡片内容的自身状态，
 * 卡片依据尺寸形态与交互状态切换不同的显示形态。
 */
class CardState(
    val spanWidth: Int,
    val spanHeight: Int,
    val columns: Int,
    val interaction: CardInteraction
) {
    /** 依据跨度推导的形态分类 */
    val sizeClass: CardSize = deriveSizeClass(spanWidth, spanHeight, columns)
}

/** 卡片矩形在单元格内向四边的内缩量 */
val CardSpacing: Dp = 6.dp
