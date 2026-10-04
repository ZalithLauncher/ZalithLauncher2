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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.movtery.zalithlauncher.utils.animation.swapAnimateDpAsState

/**
 * 支持连锁动画效果的垂直布局容器
 * 通过 [AnimatedColumnScope.AnimatedItem] 构建子项，自动为每个子项应用递增的动画延迟
 * @param isVisible 控制容器内所有子项的动画触发状态
 * @param baseDelay 动画基础延迟时间(ms)，所有子项在此基础值上递增
 * @param delayIncrement 相邻子项间的动画延迟增量(ms)
 */
@Composable
fun AnimatedColumn(
    modifier: Modifier = Modifier,
    isVisible: Boolean,
    baseDelay: Int = 0,
    delayIncrement: Int = 50,
    verticalArrangement: Arrangement.Vertical = Arrangement.spacedBy(12.dp),
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    content: @Composable AnimatedColumnScope.(ColumnScope) -> Unit
) {
    Column(
        modifier = modifier,
        verticalArrangement = verticalArrangement,
        horizontalAlignment = horizontalAlignment
    ) {
        AnimatedColumnScopeImpl(isVisible, baseDelay, delayIncrement).content(this@Column)
    }
}

interface AnimatedColumnScope {
    /**
     * 在 [AnimatedColumn] 中声明一个具有动画效果的子项
     * @param delay 指定当前子项的动画延迟时间(ms)，如设置将覆盖自动计算的延迟值
     * @param targetValue 初始偏移距离(Dp)
     */
    @Composable
    fun AnimatedItem(
        columnScope: ColumnScope,
        modifier: Modifier = Modifier,
        delay: Int? = null,
        targetValue: Int = -40,
        content: @Composable ColumnScope.(yOffset: Dp) -> Unit
    )
}

private class AnimatedColumnScopeImpl(
    private val isVisible: Boolean,
    private val baseDelay: Int,
    private val delayIncrement: Int
) : AnimatedColumnScope {
    private var itemIndex = 0

    @Composable
    override fun AnimatedItem(
        columnScope: ColumnScope,
        modifier: Modifier,
        delay: Int?,
        targetValue: Int,
        content: @Composable ColumnScope.(yOffset: Dp) -> Unit
    ) {
        val currentIndex = itemIndex++
        //优先使用显式设置的延迟，否则基于索引自动计算递增延迟
        val actualDelay = delay ?: (baseDelay + currentIndex * delayIncrement)

        val yOffset by swapAnimateDpAsState(
            targetValue = targetValue.dp,
            swapIn = isVisible,
            delayMillis = actualDelay
        )

        columnScope.content(yOffset)
    }
}


// Lazy Column



/**
 * 支持连锁动画效果的懒加载垂直列表容器
 * 通过 [AnimatedLazyListScope.animatedItems] 或 [AnimatedLazyListScope.animatedItem] 构建子项，自动为每个子项应用递增的动画延迟
 * @param isVisible 控制列表内所有子项的动画触发状态
 * @param baseDelay 动画基础延迟时间(ms)，所有子项在此基础值上递增
 * @param delayIncrement 相邻子项间的动画延迟增量(ms)
 */
@Composable
fun AnimatedLazyColumn(
    modifier: Modifier = Modifier,
    state: LazyListState = rememberLazyListState(),
    isVisible: Boolean,
    baseDelay: Int = 0,
    delayIncrement: Int = 50,
    verticalArrangement: Arrangement.Vertical = Arrangement.spacedBy(12.dp),
    contentPadding: PaddingValues = PaddingValues(0.dp),
    content: AnimatedLazyListScope.(LazyListScope) -> Unit
) {
    val scope = remember(isVisible, baseDelay, delayIncrement) {
        AnimatedLazyListScopeImpl(isVisible, baseDelay, delayIncrement)
    }
    LazyColumn(
        modifier = modifier,
        state = state,
        verticalArrangement = verticalArrangement,
        contentPadding = contentPadding
    ) {
        scope.content(this@LazyColumn)
    }
}

interface AnimatedLazyListScope {
    /**
     * 在 [LazyListScope] 中添加单个动画列表项
     * @param delay 指定当前项的动画延迟时间(ms)，如设置将覆盖自动计算的延迟值
     * @param targetValue 初始偏移距离(Dp)
     */
    fun animatedItem(
        lazyListScope: LazyListScope,
        key: Any? = null,
        delay: Int? = null,
        targetValue: Int = -40,
        content: @Composable LazyItemScope.(yOffset: Dp) -> Unit
    )

    /**
     * 在 [LazyListScope] 中批量添加动画列表项
     * @param delay 基础延迟时间(ms)，每个项在此基础值上根据索引递增
     * @param targetValue 初始偏移距离(Dp)
     */
    fun <T> animatedItems(
        lazyListScope: LazyListScope,
        items: List<T>,
        delay: Int = 0,
        key: ((item: T) -> Any)? = null,
        targetValue: Int = -40,
        content: @Composable LazyItemScope.(index: Int, item: T, yOffset: Dp) -> Unit
    )
}

private class AnimatedLazyListScopeImpl(
    private val isVisible: Boolean,
    private val baseDelay: Int,
    private val delayIncrement: Int
) : AnimatedLazyListScope {
    private var itemIndex = 0

    override fun animatedItem(
        lazyListScope: LazyListScope,
        key: Any?,
        delay: Int?,
        targetValue: Int,
        content: @Composable LazyItemScope.(yOffset: Dp) -> Unit
    ) {
        val currentIndex = itemIndex++
        //优先使用显式设置的延迟，否则基于索引自动计算递增延迟
        val actualDelay = delay ?: (baseDelay + currentIndex * delayIncrement)

        lazyListScope.item(key = key) {
            val yOffset by swapAnimateDpAsState(
                targetValue = targetValue.dp,
                swapIn = isVisible,
                delayMillis = actualDelay
            )
            content(yOffset)
        }
    }

    override fun <T> animatedItems(
        lazyListScope: LazyListScope,
        items: List<T>,
        delay: Int,
        key: ((item: T) -> Any)?,
        targetValue: Int,
        content: @Composable LazyItemScope.(index: Int, item: T, yOffset: Dp) -> Unit
    ) {
        lazyListScope.itemsIndexed(
            items = items,
            key = if (key != null) { _, item -> key(item) } else null
        ) { index, item ->
            val actualDelay = delay + baseDelay + index * delayIncrement
            val yOffset by swapAnimateDpAsState(
                targetValue = targetValue.dp,
                swapIn = isVisible,
                delayMillis = actualDelay
            )
            content(index, item, yOffset)
        }
    }
}