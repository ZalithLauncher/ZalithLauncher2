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

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import kotlin.math.roundToInt

@Composable
fun FloatingBall(
    position: Offset,
    onPositionChanged: (Offset) -> Unit,
    modifier: Modifier = Modifier,
    onSavePos: () -> Unit = {},
    onClick: () -> Unit = {},
    alpha: Float = 1f,
    color: Color = Color.Black.copy(alpha = 0.25f),
    contentColor: Color = Color.White.copy(alpha = 0.95f),
    shape: Shape = MaterialTheme.shapes.medium,
    content: @Composable () -> Unit
) {
    var ballSize by remember { mutableStateOf(IntSize.Zero) }
    val currentPosition by rememberUpdatedState(position)
    val currentOnClick by rememberUpdatedState(onClick)
    val currentOnSavePos by rememberUpdatedState(onSavePos)

    //在首次启动时，将悬浮球放到屏幕的 TopCenter
    //确保这个行为只触发一次
    var isInitialized by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(Unit) { isInitialized = true }

    //检查是否是RTL布局，需要做初始位置适配
    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    BoxWithConstraints(
        modifier = Modifier
            .alpha(alpha)
            .fillMaxSize()
            .onSizeChanged { size ->
                if (currentPosition != Offset.Zero) {
                    val maxX = (size.width - ballSize.width).toFloat().coerceAtLeast(0f)
                    val maxY = (size.height - ballSize.height).toFloat().coerceAtLeast(0f)

                    val newX = currentPosition.x.coerceIn(0f, maxX)
                    val newY = currentPosition.y.coerceIn(0f, maxY)

                    if (newX != currentPosition.x || newY != currentPosition.y) {
                        onPositionChanged(Offset(newX, newY))
                    }
                }
            }
    ) {
        val parentWidth by rememberUpdatedState(constraints.maxWidth)
        val parentHeight by rememberUpdatedState(constraints.maxHeight)

        Surface(
            modifier = modifier
                .onSizeChanged { size ->
                    ballSize = size
                    if (isInitialized || currentPosition != Offset.Zero) return@onSizeChanged
                    val x = ((parentWidth - ballSize.width) / 2f) //默认位置 TopCenter
                    val positionX = x.coerceIn(0f, (parentWidth - ballSize.width).toFloat())
                    val positionY = 0f.coerceIn(0f, (parentHeight - ballSize.height).toFloat())
                    onPositionChanged(Offset(positionX, positionY))
                }
                .offset {
                    val maxX = (parentWidth - ballSize.width).coerceAtLeast(0)
                    val maxY = (parentHeight - ballSize.height).coerceAtLeast(0)
                    IntOffset(
                        currentPosition.x.roundToInt().coerceIn(0, maxX),
                        currentPosition.y.roundToInt().coerceIn(0, maxY)
                    )
                }
                .pointerInput(Unit) {
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)

                        val startPosition = down.position
                        var isDragging = false

                        val clampedX = currentPosition.x
                            .coerceIn(0f, (parentWidth - ballSize.width).toFloat().coerceAtLeast(0f))
                        val clampedY = currentPosition.y
                            .coerceIn(0f, (parentHeight - ballSize.height).toFloat().coerceAtLeast(0f))
                        if (clampedX != currentPosition.x || clampedY != currentPosition.y) {
                            onPositionChanged(Offset(clampedX, clampedY))
                        }

                        drag(down.id) { change ->
                            val delta = change.positionChange()
                            val distanceFromStart = (change.position - startPosition).getDistance()

                            if (!isDragging && distanceFromStart > viewConfiguration.touchSlop) {
                                //超出了拖动检测距离，说明是真的在进行拖动
                                //标记当前为拖动，避免松开手指后，判定为点击事件
                                isDragging = true
                            }

                            if (isDragging) { //只有在拖动的情况下，才会变更位置
                                val deltaX = if (isRtl) -delta.x else delta.x
                                val newX = currentPosition.x + deltaX
                                val newY = currentPosition.y + delta.y
                                val positionX =
                                    newX.coerceIn(0f, (parentWidth - ballSize.width).toFloat())
                                val positionY =
                                    newY.coerceIn(0f, (parentHeight - ballSize.height).toFloat())
                                onPositionChanged(Offset(positionX, positionY))
                            }
                            change.consume()
                        }

                        if (isDragging) {
                            currentOnSavePos()
                        } else {
                            //非拖动事件，判定为一次点击
                            currentOnClick()
                        }
                    }
                },
            color = color,
            contentColor = contentColor,
            shape = shape
        ) {
            content()
        }
    }
}