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

import androidx.compose.foundation.OverscrollEffect
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.gestures.FlingBehavior
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.nonInteractiveScrollbar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.inset
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * 为可滚动的组件增加边缘渐隐的效果，可以很直观的提醒用户这里可以被滑动，
 * 效果类似元安卓View体系下的 fading edge
 * - 本实现依赖 `graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)`，
 *   该设置会让内容在单独的离屏缓冲区中绘制，以便后续通过混合模式（如 DstOut）实现“擦除”效果
 * - 如果将此修饰符放在 `padding()`、`scroll()` 或其他修饰符之后，
 *   那么离屏层可能只包裹部分内容，导致渐隐区域无法覆盖整个可见范围，从而看起来“没有效果”
 * @param direction 控制渐隐的方向，默认是垂直方向
 * @param position 指定在哪个边缘显示渐隐效果（上、下或两边）
 * @param style 控制渐隐的样式
 */
@Composable
fun Modifier.fadeEdge(
    state: ScrollState,
    length: Dp = 12.dp,
    direction: EdgeDirection = EdgeDirection.Vertical,
    position: EdgeSide = EdgeSide.Both,
    style: FadeStyle = createDefaultFadeStyle(direction)
): Modifier {
    val fadePx = with(LocalDensity.current) { length.toPx() }

    val topDistance = state.value.toFloat()
    val startFade = if (state.canScrollBackward) {
        (topDistance / fadePx).coerceIn(0f, 1f) * fadePx
    } else 0f

    val bottomDistance = (state.maxValue - state.value).toFloat()
    val endFade = if (state.canScrollForward) {
        (bottomDistance / fadePx).coerceIn(0f, 1f) * fadePx
    } else 0f

    return this
        //使用离屏合成，让混合模式只影响当前组件，不污染父级与同级元素
        .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
        .drawWithContent {
            drawContent()
            drawFadeEdges(startFade, endFade, direction, position, style)
        }
}

/**
 * 为可滚动的组件增加边缘渐隐的效果，可以很直观的提醒用户这里可以被滑动，
 * 效果类似元安卓View体系下的 fading edge
 * - 本实现依赖 `graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)`，
 *   该设置会让内容在单独的离屏缓冲区中绘制，以便后续通过混合模式（如 DstOut）实现“擦除”效果
 * - 如果将此修饰符放在 `padding()`、`scroll()` 或其他修饰符之后，
 *   那么离屏层可能只包裹部分内容，导致渐隐区域无法覆盖整个可见范围，从而看起来“没有效果”
 * @param direction 控制渐隐的方向，默认是垂直方向
 * @param position 指定在哪个边缘显示渐隐效果（上、下或两边）
 * @param style 控制渐隐的样式
 */
@Composable
fun Modifier.fadeEdge(
    state: LazyListState,
    length: Dp = 12.dp,
    direction: EdgeDirection = EdgeDirection.Vertical,
    position: EdgeSide = EdgeSide.Both,
    style: FadeStyle = createDefaultFadeStyle(direction)
): Modifier {
    val fadePx = with(LocalDensity.current) { length.toPx() }
    val layoutInfo = state.layoutInfo

    val firstItem = layoutInfo.visibleItemsInfo.firstOrNull()
    val topDistancePx = when {
        firstItem == null -> 0f
        state.firstVisibleItemIndex == 0 -> -firstItem.offset.toFloat()
        else -> fadePx //说明已经不在顶部了，直接满强度
    }
    val startFade = if (state.canScrollBackward) {
        (topDistancePx / fadePx).coerceIn(0f, 1f) * fadePx
    } else 0f

    val lastItem = layoutInfo.visibleItemsInfo.lastOrNull()
    val viewportHeight = layoutInfo.viewportEndOffset - layoutInfo.viewportStartOffset
    val bottomDistancePx = when {
        lastItem == null -> 0f
        state.canScrollForward.not() -> 0f
        state.firstVisibleItemIndex + layoutInfo.visibleItemsInfo.size == layoutInfo.totalItemsCount &&
                lastItem.offset + lastItem.size > viewportHeight ->
            (lastItem.offset + lastItem.size - viewportHeight).toFloat()
        else -> fadePx //还没到底部，直接满强度
    }
    val endFade = if (state.canScrollForward) {
        (bottomDistancePx / fadePx).coerceIn(0f, 1f) * fadePx
    } else 0f

    return this
        //使用离屏合成，让混合模式只影响当前组件，不污染父级与同级元素
        .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
        .drawWithContent {
            drawContent()
            drawFadeEdges(startFade, endFade, direction, position, style)
        }
}

/**
 * 内部统一渲染渐隐的函数
 */
private fun DrawScope.drawFadeEdges(
    startFade: Float,
    endFade: Float,
    direction: EdgeDirection,
    position: EdgeSide,
    style: FadeStyle
) {
    val totalSize = if (direction == EdgeDirection.Vertical) size.height else size.width

    if (position.includeStart && startFade > 0f) {
        drawFadeEdge(
            insetStart = 0f,
            insetEnd = totalSize - startFade,
            direction = direction,
            reverse = false,
            style = style
        )
    }

    if (position.includeEnd && endFade > 0f) {
        drawFadeEdge(
            insetStart = totalSize - endFade,
            insetEnd = 0f,
            direction = direction,
            reverse = true,
            style = style
        )
    }
}

private fun DrawScope.drawFadeEdge(
    insetStart: Float,
    insetEnd: Float,
    direction: EdgeDirection,
    reverse: Boolean,
    style: FadeStyle
) {
    val isVertical = direction == EdgeDirection.Vertical
    inset(
        left = if (!isVertical) insetStart else 0f,
        top = if (isVertical) insetStart else 0f,
        right = if (!isVertical) insetEnd else 0f,
        bottom = if (isVertical) insetEnd else 0f
    ) {
        rotate(if (reverse) 180f else 0f) {
            drawRect(brush = style.brush, blendMode = style.blendMode)
        }
    }
}

/**
 * 控制渐隐的方向
 */
@Immutable
enum class EdgeDirection {
    Vertical, Horizontal
}

/**
 * 指定在哪一侧显示渐隐效果
 */
@Immutable
enum class EdgeSide(val includeStart: Boolean, val includeEnd: Boolean) {
    /** 上/左 */
    Start(true, false),
    /** 下/右 */
    End(false, true),
    /** 两边 */
    Both(true, true)
}

/**
 * 渐隐样式定义,通常情况下使用默认的 [BlendMode.DstOut] 即可
 */
@Immutable
data class FadeStyle(
    val brush: Brush,
    val blendMode: BlendMode
)

/**
 * 黑色 -> 透明 的线性渐变，结合 [BlendMode.DstOut] 实现淡化效果
 * @param direction 决定渐变的方向
 */
fun createDefaultFadeStyle(
    direction: EdgeDirection
): FadeStyle {
    //黑色 -> 透明
    val colorStops = arrayOf(
        0f to Color.Black,
        1f to Color.Transparent
    )

    return FadeStyle(
        brush = when (direction) {
            EdgeDirection.Vertical -> Brush.verticalGradient(*colorStops)
            EdgeDirection.Horizontal -> Brush.horizontalGradient(*colorStops)
        },
        blendMode = BlendMode.DstOut
    )
}


@Composable
fun Modifier.verticalScrollWithBar(
    state: ScrollState,
    enabled: Boolean = true,
    flingBehavior: FlingBehavior? = null,
    reverseScrolling: Boolean = false
): Modifier = this.nonInteractiveScrollbar(
    state = state.scrollIndicatorState!!,
    orientation = Orientation.Vertical
).verticalScroll(
    state = state,
    enabled = enabled,
    flingBehavior = flingBehavior,
    reverseScrolling = reverseScrolling,
)

@Composable
fun Modifier.verticalScrollWithBar(
    state: ScrollState,
    overscrollEffect: OverscrollEffect?,
    enabled: Boolean = true,
    flingBehavior: FlingBehavior? = null,
    reverseScrolling: Boolean = false
): Modifier = this.nonInteractiveScrollbar(
    state = state.scrollIndicatorState!!,
    orientation = Orientation.Vertical
).verticalScroll(
    state = state,
    overscrollEffect = overscrollEffect,
    enabled = enabled,
    flingBehavior = flingBehavior,
    reverseScrolling = reverseScrolling,
)

@Composable
fun Modifier.lazyScrollWithBar(
    state: LazyListState,
    orientation: Orientation = Orientation.Vertical
): Modifier = this.nonInteractiveScrollbar(
    state = state.scrollIndicatorState!!,
    orientation = orientation
)