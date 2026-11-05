/*
 * Zalith Launcher 2
 * Copyright (C) 2025-2026 MovTery <movtery228@qq.com> and contributors
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

package com.movtery.zalithlauncher.ui.control.input

import android.graphics.Rect
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateCentroid
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.setting.AllSettings
import com.movtery.zalithlauncher.ui.screens.content.elements.DisabledAlpha
import kotlin.math.roundToInt

private const val MinMagnifierZoom = 1.0f
private const val MaxMagnifierZoom = 4.0f

class InputMagnifierState {
    var isVisible by mutableStateOf(false)
    var dismissedForInput by mutableStateOf(false)
        private set
    var zoom by mutableFloatStateOf(MinMagnifierZoom)
        private set
    var sourceCenter by mutableStateOf(Offset(0f, 1f))
        private set

    fun close() {
        isVisible = false
        dismissedForInput = true
    }

    fun resetInputSession() {
        isVisible = false
        dismissedForInput = false
    }

    fun saveViewport(zoom: Float, sourceCenter: Offset) {
        this.zoom = zoom.coerceIn(MinMagnifierZoom, MaxMagnifierZoom)
        this.sourceCenter = sourceCenter
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun InputMagnifier(
    inputEnabled: Boolean,
    state: InputMagnifierState,
    frame: () -> ImageBitmap?,
    frameVersion: () -> Long,
    modifier: Modifier = Modifier,
    floatingContent: (@Composable BoxScope.() -> Unit)? = null
) {
    val density = LocalDensity.current
    val imeVisible = WindowInsets.isImeVisible
    val imeHeight = WindowInsets.ime.getBottom(density)
    val rootView = LocalView.current.rootView
    val visibleFrame = Rect().also { rootView.getWindowVisibleDisplayFrame(it) }
    val inputOccludesGame = inputEnabled && imeVisible && rootView.height > 0 &&
            rootView.height * 2 / 3 > visibleFrame.bottom

    LaunchedEffect(inputEnabled) {
        if (!inputEnabled) state.resetInputSession()
    }
    LaunchedEffect(inputOccludesGame) {
        if (!inputOccludesGame) {
            state.isVisible = false
        } else if (!state.dismissedForInput && AllSettings.imeMagnifierAutoShow.state) {
            state.isVisible = true
        }
    }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val availableHeight = (constraints.maxHeight - imeHeight).coerceAtLeast(0)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(with(density) { availableHeight.toDp() })
        ) {
            if (inputOccludesGame) {
                if (state.isVisible) {
                    MagnifierWindow(
                        modifier = Modifier.align(Alignment.Center),
                        state = state,
                        frame = frame,
                        frameVersion = frameVersion,
                        floatingContent = floatingContent,
                    )
                } else if (!state.isVisible) {
                    IconButton(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(all = 16.dp)
                            .size(40.dp)
                            .focusProperties { canFocus = false },
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = Color.Black.copy(alpha = 0.55f),
                            contentColor = Color.White,
                            disabledContainerColor = Color.Black.copy(alpha = 0.2f),
                            disabledContentColor = Color.White.copy(alpha = DisabledAlpha)
                        ),
                        onClick = { state.isVisible = true }
                    ) {
                        Icon(
                            modifier = Modifier.size(18.dp),
                            painter = painterResource(R.drawable.ic_search),
                            contentDescription = stringResource(R.string.game_magnifier_show)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MagnifierWindow(
    state: InputMagnifierState,
    frame: () -> ImageBitmap?,
    frameVersion: () -> Long,
    modifier: Modifier,
    floatingContent: (@Composable BoxScope.() -> Unit)? = null
) {
    var zoom by remember { mutableFloatStateOf(state.zoom) }
    var sourceCenter by remember { mutableStateOf(state.sourceCenter) }

    Surface(
        modifier = modifier
            .padding(vertical = 12.dp)
            .fillMaxWidth(0.6f)
            .fillMaxHeight(),
        shape = MaterialTheme.shapes.medium,
        color = Color.Black,
        border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary),
        shadowElevation = 6.dp
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            MagnifierViewport(
                frame = frame,
                frameVersion = frameVersion,
                zoom = zoom,
                sourceCenter = sourceCenter,
                onViewportChange = { newZoom, newCenter ->
                    zoom = newZoom
                    sourceCenter = newCenter
                },
                onGestureEnd = { newZoom, newCenter ->
                    zoom = newZoom
                    sourceCenter = newCenter
                    state.saveViewport(newZoom, newCenter)
                }
            )

            floatingContent?.invoke(this@Box)
        }
    }
}

@Composable
private fun MagnifierViewport(
    frame: () -> ImageBitmap?,
    frameVersion: () -> Long,
    zoom: Float,
    sourceCenter: Offset,
    onViewportChange: (Float, Offset) -> Unit,
    onGestureEnd: (Float, Offset) -> Unit
) {
    val frame0 = frame() ?: return
    val currentFrame by rememberUpdatedState(frame0)
    val currentZoom by rememberUpdatedState(zoom)
    val currentSourceCenter by rememberUpdatedState(sourceCenter)

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                awaitEachGesture {
                    awaitFirstDown(requireUnconsumed = false)
                    var gestureZoom = currentZoom
                    var gestureCenter = currentSourceCenter
                    do {
                        val event = awaitPointerEvent()
                        val bitmap = currentFrame
                        if (event.changes.any { it.pressed } &&
                            bitmap.width > 0 && bitmap.height > 0 && size.width > 0 && size.height > 0
                        ) {
                            val oldZoom = gestureZoom
                            val newZoom = (oldZoom * event.calculateZoom()).coerceIn(
                                MinMagnifierZoom,
                                MaxMagnifierZoom
                            )
                            val oldSourceSize = sourceSize(
                                frame = bitmap,
                                viewportSize = size,
                                zoom = oldZoom
                            )
                            val oldCenter = clampedSourceCenter(
                                normalizedCenter = gestureCenter,
                                frame = bitmap,
                                sourceSize = oldSourceSize
                            )
                            val centroid = event.calculateCentroid()
                            val focusOffset = centroid - Offset(size.width / 2f, size.height / 2f)
                            val newCenter = oldCenter +
                                    focusOffset / oldZoom -
                                    focusOffset / newZoom -
                                    event.calculatePan() / newZoom
                            gestureZoom = newZoom
                            gestureCenter = Offset(
                                x = (newCenter.x / bitmap.width).coerceIn(0f, 1f),
                                y = (newCenter.y / bitmap.height).coerceIn(0f, 1f)
                            )
                            onViewportChange(gestureZoom, gestureCenter)
                        }
                        event.changes.forEach { change ->
                            if (change.positionChanged()) change.consume()
                        }
                    } while (event.changes.any { it.pressed })

                    val bitmap = currentFrame
                    if (bitmap.width > 0 && bitmap.height > 0 && size.width > 0 && size.height > 0) {
                        val savedSourceSize = sourceSize(
                            frame = bitmap,
                            viewportSize = size,
                            zoom = gestureZoom
                        )
                        val savedCenter = clampedSourceCenter(
                            gestureCenter,
                            bitmap,
                            savedSourceSize
                        )
                        onGestureEnd(
                            gestureZoom,
                            Offset(
                                x = savedCenter.x / bitmap.width,
                                y = savedCenter.y / bitmap.height
                            )
                        )
                    }
                }
            }
    ) {
        frameVersion()
        if (size.width <= 0f || size.height <= 0f) return@Canvas
        val viewportSize = IntSize(size.width.roundToInt(), size.height.roundToInt())
        val sourceSize = sourceSize(
            frame = frame0,
            viewportSize = viewportSize,
            zoom = zoom
        )
        val center = clampedSourceCenter(sourceCenter, frame0, sourceSize)
        val sourceOffset = IntOffset(
            x = (center.x - sourceSize.width / 2f).roundToInt()
                .coerceIn(0, frame0.width - sourceSize.width),
            y = (center.y - sourceSize.height / 2f).roundToInt()
                .coerceIn(0, frame0.height - sourceSize.height)
        )
        drawImage(
            image = frame0,
            srcOffset = sourceOffset,
            srcSize = sourceSize,
            dstSize = viewportSize
        )
    }
}

private fun sourceSize(
    frame: ImageBitmap,
    viewportSize: IntSize,
    zoom: Float
): IntSize {
    val width = viewportSize.width / zoom
    val height = viewportSize.height / zoom
    val scale = minOf(1f, frame.width / width, frame.height / height)
    return IntSize(
        width = (width * scale).roundToInt().coerceIn(1, frame.width),
        height = (height * scale).roundToInt().coerceIn(1, frame.height)
    )
}

private fun clampedSourceCenter(
    normalizedCenter: Offset,
    frame: ImageBitmap,
    sourceSize: IntSize
): Offset {
    val halfWidth = sourceSize.width / 2f
    val halfHeight = sourceSize.height / 2f
    return Offset(
        x = (normalizedCenter.x * frame.width).coerceIn(
            halfWidth,
            (frame.width - halfWidth).coerceAtLeast(halfWidth)
        ),
        y = (normalizedCenter.y * frame.height).coerceIn(
            halfHeight,
            (frame.height - halfHeight).coerceAtLeast(halfHeight)
        )
    )
}

