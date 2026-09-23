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

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.IntOffset

/** 卡片内容的 composable 类型，参数为卡片自身 id */
typealias CardContent = @Composable CardState.(cardId: String) -> Unit

/**
 * 用户卡片的类型声明，未声明形状时使用主题默认形状。
 * @param defaultSpan 默认跨度
 * @param limits 尺寸边界限制
 * @param shape 形状
 * @param content 该卡片的 UI 内容
 */
class CardType(
    val typeId: String,
    val defaultSpan: IntOffset,
    val limits: CardLimits = CardLimits.DEFAULT,
    val shape: Shape? = null,
    val content: CardContent
)
