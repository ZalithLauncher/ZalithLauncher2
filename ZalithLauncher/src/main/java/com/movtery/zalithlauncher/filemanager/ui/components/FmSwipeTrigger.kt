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

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.movtery.zalithlauncher.filemanager.ui.theme.FmAnimations
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * 横向滑动触发手势的状态，记录拖动偏移以联动条目的视觉效果
 * @param triggerPx 触发距离（px）
 */
@Stable
class FmSwipeTriggerState(
    val triggerPx: Float
) {
    val offsetX = Animatable(0f)
    /** 是否正处于拖动手势中，回弹动画结束后复位 */
    var isDragging by mutableStateOf(false)
    /** 当前拖动偏移相对触发距离的比例，随拖动与回弹实时变化，范围 0..1 */
    val dragFraction: Float
        get() = (abs(offsetX.value) / triggerPx).coerceIn(0f, 1f)
}

/** 创建横向滑动触发手势的状态 */
@Composable
fun rememberFmSwipeTriggerState(triggerDistanceDp: Float = 80f): FmSwipeTriggerState {
    val triggerPx = with(LocalDensity.current) { triggerDistanceDp.dp.toPx() }
    return remember(triggerPx) { FmSwipeTriggerState(triggerPx) }
}

/** 为条目添加横向滑动触发手势 */
fun Modifier.fmSwipeTrigger(
    state: FmSwipeTriggerState,
    triggerable: Boolean,
    onTriggered: () -> Unit
): Modifier = composed {
    val maxDragPx = state.triggerPx * 1.5f
    val scope = rememberCoroutineScope()

    this
        .offset { IntOffset(state.offsetX.value.roundToInt(), 0) }
        .pointerInput(state, triggerable) {
            detectHorizontalDragGestures(
                onDragStart = {
                    state.isDragging = true
                    scope.launch { state.offsetX.snapTo(0f) }
                },
                onHorizontalDrag = { _, delta ->
                    scope.launch {
                        val next = (state.offsetX.value + delta).coerceIn(-maxDragPx, maxDragPx)
                        state.offsetX.snapTo(next)
                    }
                },
                onDragEnd = {
                    val reached = abs(state.offsetX.value) >= state.triggerPx
                    scope.launch {
                        if (reached && triggerable) onTriggered()
                        state.offsetX.animateTo(0f, tween(FmAnimations.SWIPE_BACK_MS))
                        state.isDragging = false
                    }
                },
                onDragCancel = {
                    scope.launch {
                        state.offsetX.animateTo(0f, tween(FmAnimations.SWIPE_BACK_MS))
                        state.isDragging = false
                    }
                }
            )
        }
}
