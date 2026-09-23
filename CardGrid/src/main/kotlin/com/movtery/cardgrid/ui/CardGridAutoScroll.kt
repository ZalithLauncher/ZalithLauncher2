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

package com.movtery.cardgrid.ui

import androidx.compose.foundation.ScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.movtery.cardgrid.state.CardGridState

/** 触发边缘自动滚动的带宽 */
private val AutoScrollEdge = 56.dp

/** 自动滚动的最大速度（dp/秒） */
private const val AutoScrollMaxSpeed = 900f

/**
 * 拖动/缩放会话期间的边缘自动滚动：
 * 指针接近视口的首尾边缘时驱动 [scrollState] 滚动，
 * 滚动后重算指针位置使会话跟随。
 * 需放置在提供视口坐标的组合中，与 [CardGrid] 同处一个滚动容器。
 */
@Composable
fun CardGridAutoScroll(
    state: CardGridState,
    scrollState: ScrollState
) {
    val density = LocalDensity.current
    LaunchedEffect(state.hasSession) {
        if (!state.hasSession) return@LaunchedEffect
        var lastFrameNanos = 0L
        val edgePx = with(density) { AutoScrollEdge.toPx() }
        val maxSpeedPx = with(density) { AutoScrollMaxSpeed.dp.toPx() }
        while (true) {
            val frameNanos = withFrameNanos { it }
            if (lastFrameNanos != 0L) {
                val dt = (frameNanos - lastFrameNanos) / 1_000_000_000f
                val pointer = state.pointerPosition
                if (pointer != null) {
                    val viewportY = state.areaOffsetInRoot.y + pointer.y - state.viewportTopPx
                    val bottomDistance = state.viewportHeightPx - viewportY
                    val dyScroll = when {
                        viewportY in 0f..edgePx ->
                            -maxSpeedPx * (1f - viewportY / edgePx) * dt
                        bottomDistance in 0f..edgePx ->
                            maxSpeedPx * (1f - bottomDistance / edgePx) * dt
                        else -> 0f
                    }
                    if (dyScroll != 0f) {
                        scrollState.dispatchRawDelta(dyScroll)
                        state.onAutoScroll()
                    }
                }
            }
            lastFrameNanos = frameNanos
        }
    }
}
