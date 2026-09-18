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

package com.movtery.zalithlauncher.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.movtery.zalithlauncher.utils.animation.swapAnimateDpAsState

/**
 * 支持连锁动画效果的水平布局容器
 * 通过 [AnimatedRowScope.AnimatedItem] 构建子项，自动为每个子项应用递增的动画延迟
 * @param isVisible 控制容器内所有子项的动画触发状态
 * @param baseDelay 动画基础延迟时间(ms)，所有子项在此基础值上递增
 * @param delayIncrement 相邻子项间的动画延迟增量(ms)
 */
@Composable
fun AnimatedRow(
    modifier: Modifier = Modifier,
    isVisible: Boolean,
    baseDelay: Int = 0,
    delayIncrement: Int = 50,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.spacedBy(12.dp),
    content: @Composable AnimatedRowScope.(RowScope) -> Unit
) {
    Row(
        modifier = modifier,
        horizontalArrangement = horizontalArrangement
    ) {
        AnimatedRowScopeImpl(isVisible, baseDelay, delayIncrement).content(this@Row)
    }
}

interface AnimatedRowScope {
    /**
     * 在 [AnimatedRow] 中声明一个具有动画效果的子项
     * @param delay 指定当前子项的动画延迟时间(ms)，如设置将覆盖自动计算的延迟值
     * @param targetValue 初始偏移距离(Dp)
     */
    @Composable
    fun AnimatedItem(
        rowScope: RowScope,
        modifier: Modifier = Modifier,
        delay: Int? = null,
        targetValue: Int = 40,
        content: @Composable RowScope.(yOffset: Dp) -> Unit
    )
}

private class AnimatedRowScopeImpl(
    private val isVisible: Boolean,
    private val baseDelay: Int,
    private val delayIncrement: Int
) : AnimatedRowScope {
    private var itemIndex = 0

    @Composable
    override fun AnimatedItem(
        rowScope: RowScope,
        modifier: Modifier,
        delay: Int?,
        targetValue: Int,
        content: @Composable RowScope.(yOffset: Dp) -> Unit
    ) {
        val currentIndex = itemIndex++
        //优先使用显式设置的延迟，否则基于索引自动计算递增延迟
        val actualDelay = delay ?: (baseDelay + currentIndex * delayIncrement)

        val yOffset by swapAnimateDpAsState(
            targetValue = targetValue.dp,
            swapIn = isVisible,
            isHorizontal = true,
            delayMillis = actualDelay
        )

        rowScope.content(yOffset)
    }
}
