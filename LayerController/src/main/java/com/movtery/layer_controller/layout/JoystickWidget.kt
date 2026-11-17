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

package com.movtery.layer_controller.layout

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.input.pointer.PointerId
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.movtery.layer_controller.data.JoystickDirection
import com.movtery.layer_controller.event.EventHandler
import com.movtery.layer_controller.observable.DefaultObservableJoystickStyle
import com.movtery.layer_controller.observable.ObservableControlLayer
import com.movtery.layer_controller.observable.ObservableJoystickData
import com.movtery.layer_controller.observable.ObservableJoystickData.Companion.calculateDirection
import com.movtery.layer_controller.observable.ObservableJoystickData.Companion.clampToRegion
import com.movtery.layer_controller.observable.ObservableJoystickData.Companion.toRegion
import com.movtery.layer_controller.observable.ObservableJoystickStyle
import com.movtery.layer_controller.observable.ObservableWidget
import com.movtery.layer_controller.observable.PointerEventBus
import com.movtery.layer_controller.utils.buttonSize
import com.movtery.layer_controller.utils.editMode
import com.movtery.layer_controller.utils.snap.GuideLine
import com.movtery.layer_controller.utils.snap.SnapMode

/**
 * 摇杆控件渲染组件
 */
@Composable
internal fun JoystickWidgetRenderer(
    data: ObservableJoystickData,
    joystickStyles: List<ObservableJoystickStyle>,
    screenSize: IntSize,
    isDark: Boolean,
    visible: Boolean = true,
    pointerEventBus: PointerEventBus? = null,
    eventHandler: EventHandler? = null,
    reversedLayers: List<ObservableControlLayer>? = null,
    onOccupiedPointer: (PointerId) -> Unit = {},
    onReleasePointer: (PointerId) -> Unit = {},
    isEditMode: Boolean = false,
    enableSnap: Boolean = false,
    snapMode: SnapMode = SnapMode.Local,
    localSnapRange: Dp = 20.dp,
    getOtherWidgets: () -> List<ObservableWidget> = { emptyList() },
    snapThresholdValue: Dp = 4.dp,
    drawLine: (ObservableWidget, List<GuideLine>) -> Unit = { _, _ -> },
    onLineCancel: (ObservableWidget) -> Unit = {},
    onTapInEditMode: (() -> Unit)? = null
) {
    val density = LocalDensity.current
    val layoutDirection = LocalLayoutDirection.current

    // 获取样式
    val joystickStyle = data.joystickStyleId?.let { id ->
        joystickStyles.find { it.uuid == id }
    }

    val themeConfig = if (joystickStyle?.commonStyle == true || !isDark) {
        joystickStyle?.lightStyle ?: DefaultObservableJoystickStyle.lightStyle
    } else {
        joystickStyle?.darkStyle ?: DefaultObservableJoystickStyle.darkStyle
    }

    //已经经过验证，如果使用Modifier.alpha设置不透明度，会导致摇杆强制裁切超出范围的内容
    //graphicsLayer(alpha = alpha, clip = false)也一样
    //这里暂时只能统一修改颜色的alpha
    val alpha = themeConfig.alpha
    val currentBackgroundColor = remember(themeConfig.backgroundColor, alpha) {
        themeConfig.backgroundColor.applyAlpha(alpha)
    }
    val currentJoystickColor = remember(themeConfig.joystickColor, alpha) {
        themeConfig.joystickColor.applyAlpha(alpha)
    }
    val currentJoystickCanLockColor = remember(themeConfig.joystickCanLockColor, alpha) {
        themeConfig.joystickCanLockColor.applyAlpha(alpha)
    }
    val currentJoystickLockedColor = remember(themeConfig.joystickLockedColor, alpha) {
        themeConfig.joystickLockedColor.applyAlpha(alpha)
    }
    val currentLockMarkColor = remember(themeConfig.lockMarkColor, alpha) {
        themeConfig.lockMarkColor.applyAlpha(alpha)
    }
    val currentBorderColor = remember(themeConfig.borderColor, alpha) {
        themeConfig.borderColor.applyAlpha(alpha)
    }

    // 形状
    val backgroundShape = remember(themeConfig.backgroundShape) {
        if (themeConfig.backgroundShape == 50) CircleShape
        else RoundedCornerShape(percent = themeConfig.backgroundShape)
    }
    val joystickShape = remember(themeConfig.joystickShape) {
        if (themeConfig.joystickShape == 50) CircleShape
        else RoundedCornerShape(percent = themeConfig.joystickShape)
    }

    // 边框宽度比例
    val borderWidthRatio = remember(themeConfig.borderWidthRatio) {
        (themeConfig.borderWidthRatio.toFloat() / 100f).coerceIn(0.0f, 0.5f)
    }

    // 摇杆头大小
    val joystickSizeRatio = remember(themeConfig.joystickSize) {
        themeConfig.joystickSize.coerceIn(0.0f, 1.0f)
    }

    // 当大小变化时重新初始化
    var currentSize by remember { mutableStateOf(IntSize.Zero) }

    // 缓存绘制对象，拖动摇杆导致的连续重绘不再重复创建轮廓与路径
    val drawCache = remember(
        backgroundShape, joystickShape, currentSize,
        borderWidthRatio, joystickSizeRatio, layoutDirection, density
    ) {
        if (currentSize == IntSize.Zero) null
        else {
            val sizePx = Size(currentSize.width.toFloat(), currentSize.height.toFloat())
            val minSide = minOf(sizePx.width, sizePx.height)
            createDrawCache(
                backgroundShape = backgroundShape,
                joystickShape = joystickShape,
                sizePx = sizePx,
                knobSizePx = minSide * joystickSizeRatio,
                borderWidthPx = (minSide * borderWidthRatio).coerceAtLeast(0f),
                layoutDirection = layoutDirection,
                density = density
            )
        }
    }

    // 当形状变化时重新计算区域
    LaunchedEffect(backgroundShape) {
        if (visible && currentSize != IntSize.Zero) {
            val sizePx = Size(currentSize.width.toFloat(), currentSize.height.toFloat())
            data.backgroundRegion = backgroundShape.toRegion(
                size = sizePx,
                density = density,
                layoutDirection = layoutDirection
            )
        }
    }

    if (visible) {
        Box(
            modifier = Modifier
                .onSizeChanged { size ->
                    currentSize = size
                    if (size != IntSize.Zero) {
                        // 计算并设置背景区域
                        val sizePx = Size(size.width.toFloat(), size.height.toFloat())
                        data.backgroundRegion = backgroundShape.toRegion(
                            size = sizePx,
                            density = density,
                            layoutDirection = layoutDirection
                        )
                        data.knobOffset = Offset.Zero
                    }
                }
                .buttonSize(data, screenSize)
                .let { modifier ->
                    if (isEditMode) {
                        modifier.editMode(
                            isEditMode = true,
                            data = data,
                            screenSize = screenSize,
                            enableSnap = enableSnap,
                            snapMode = snapMode,
                            localSnapRange = localSnapRange,
                            getOtherWidgets = getOtherWidgets,
                            snapThresholdValue = snapThresholdValue,
                            drawLine = drawLine,
                            onLineCancel = onLineCancel,
                            onTapInEditMode = onTapInEditMode ?: {}
                        )
                    } else if (pointerEventBus != null && eventHandler != null && reversedLayers != null) {
                        with(data) {
                            modifier.touchModifier(
                                pointerEventBus = pointerEventBus,
                                eventHandler = eventHandler,
                                allLayers = reversedLayers,
                                screenSize = screenSize,
                                onOccupiedPointer = onOccupiedPointer,
                                onReleasePointer = onReleasePointer
                            )
                        }
                    } else modifier
                },
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val cache = drawCache ?: return@Canvas
                val bgCenter = Offset(size.width / 2f, size.height / 2f)

                // 背景层
                drawBackgroundLayer(
                    cache = cache,
                    backgroundColor = currentBackgroundColor,
                    borderColor = currentBorderColor
                )

                // 摇杆头
                drawJoystick(
                    outline = cache.knobOutline,
                    color = when {
                        data.isLocked -> currentJoystickLockedColor
                        data.canLockState -> currentJoystickCanLockColor
                        else -> currentJoystickColor
                    },
                    center = Offset(
                        bgCenter.x + data.knobOffset.x,
                        bgCenter.y + data.knobOffset.y
                    ),
                    size = cache.knobSize
                )

                // 绘制锁定标记
                if (data.isLocked) {
                    drawCircle(
                        color = currentLockMarkColor,
                        center = Offset(bgCenter.x, 0f),
                        radius = 4f
                    )
                }
            }

            DisposableEffect(Unit) {
                data.onCompositionStart(eventHandler)
                onDispose {
                    data.onCompositionDispose(eventHandler)
                    data.activePointer?.let { pointerId ->
                        onReleasePointer(pointerId)
                        data.activePointer = null
                    }
                }
            }
        }
    } else {
        Spacer(
            modifier = Modifier.buttonSize(data, screenSize)
        )
    }
}

/**
 * 绘制背景层
 */
private fun DrawScope.drawBackgroundLayer(
    cache: DrawCache,
    backgroundColor: Color,
    borderColor: Color
) {
    clipPath(cache.backgroundClipPath) {
        drawOutline(
            outline = cache.backgroundOutline,
            color = backgroundColor
        )

        cache.borderStroke?.let { stroke ->
            drawOutline(
                outline = cache.backgroundOutline,
                color = borderColor,
                style = stroke
            )
        }
    }
}

/**
 * 绘制摇杆层
 */
private fun DrawScope.drawJoystick(
    outline: Outline,
    color: Color,
    center: Offset,
    size: Float
) {
    translate(
        left = center.x - size / 2f,
        top = center.y - size / 2f
    ) {
        drawOutline(
            outline = outline,
            color = color
        )
    }
}

/**
 * 缓存的绘制对象
 */
private class DrawCache(
    val backgroundOutline: Outline,
    val backgroundClipPath: Path,
    val borderStroke: Stroke?,
    val knobOutline: Outline,
    val knobSize: Float
)

private fun Outline.toClipPath(): Path = when (this) {
    is Outline.Generic -> path
    is Outline.Rounded -> Path().apply {
        addRoundRect(roundRect)
    }
    is Outline.Rectangle -> Path().apply {
        addRect(rect)
    }
}

private fun createDrawCache(
    backgroundShape: Shape,
    joystickShape: Shape,
    sizePx: Size,
    knobSizePx: Float,
    borderWidthPx: Float,
    layoutDirection: LayoutDirection,
    density: Density
): DrawCache {
    val outline = backgroundShape.createOutline(
        size = sizePx,
        layoutDirection = layoutDirection,
        density = density
    )

    return DrawCache(
        backgroundOutline = outline,
        backgroundClipPath = outline.toClipPath(),
        borderStroke = if (borderWidthPx > 0f) Stroke(width = borderWidthPx) else null,
        knobOutline = joystickShape.createOutline(
            size = Size(knobSizePx, knobSizePx),
            layoutDirection = layoutDirection,
            density = density
        ),
        knobSize = knobSizePx
    )
}

private fun Color.applyAlpha(multiplier: Float): Color {
    return copy(alpha = this.alpha * multiplier)
}

@Composable
fun JoystickStyleWidget(
    modifier: Modifier = Modifier,
    isDarkTheme: Boolean,
    style: ObservableJoystickStyle,
    widgetSize: Dp = 120.dp,
    deadZoneRatio: Float = 0.5f,
    lockThreshold: Float = 0.3f,
) {
    val theme = if (isDarkTheme) style.darkStyle else style.lightStyle

    val backgroundShape = remember(theme.backgroundShape) {
        RoundedCornerShape(percent = theme.backgroundShape)
    }

    val joystickShape = remember(theme.joystickShape) {
        RoundedCornerShape(percent = theme.joystickShape)
    }

    val borderWidthRatio = remember(theme.borderWidthRatio) {
        (theme.borderWidthRatio.toFloat() / 100f).coerceIn(0.0f, 0.5f)
    }

    val density = LocalDensity.current
    val layoutDirection = LocalLayoutDirection.current

    val backgroundSizePx = remember(widgetSize) {
        with(density) { widgetSize.toPx() }
    }

    val backgroundRegion = remember(backgroundShape, backgroundSizePx) {
        backgroundShape.toRegion(
            size = Size(backgroundSizePx, backgroundSizePx),
            density = density,
            layoutDirection = layoutDirection
        )
    }
    val currentBackgroundRegion by rememberUpdatedState(backgroundRegion)

    val joystickSizePx = remember(backgroundSizePx, theme.joystickSize) {
        backgroundSizePx * theme.joystickSize.coerceIn(0.0f, 1.0f)
    }

    val drawCache = remember(
        backgroundShape, joystickShape, backgroundSizePx, joystickSizePx,
        borderWidthRatio, layoutDirection, density
    ) {
        createDrawCache(
            backgroundShape = backgroundShape,
            joystickShape = joystickShape,
            sizePx = Size(backgroundSizePx, backgroundSizePx),
            knobSizePx = joystickSizePx,
            borderWidthPx = (backgroundSizePx * borderWidthRatio).coerceAtLeast(0f),
            layoutDirection = layoutDirection,
            density = density
        )
    }

    val centerPoint = remember(backgroundSizePx) {
        Offset(backgroundSizePx / 2, backgroundSizePx / 2)
    }

    val deadZoneRadius = remember(backgroundSizePx, deadZoneRatio) {
        backgroundSizePx * deadZoneRatio / 2
    }

    val lockThresholdPx = remember(backgroundSizePx, lockThreshold) {
        backgroundSizePx * lockThreshold
    }

    val lockPosition = remember(centerPoint) {
        Offset(centerPoint.x, 0f)
    }

    var joystickPosition by remember { mutableStateOf(centerPoint) }
    var isLocked by remember { mutableStateOf(false) }
    var internalCanLock by remember { mutableStateOf(false) }
    var lastDragPosition by remember { mutableStateOf(Offset.Zero) }

    fun updateJoystickState(position: Offset) {
        val clampedPosition = position.clampToRegion(
            region = currentBackgroundRegion,
            center = centerPoint
        )
        joystickPosition = clampedPosition

        val direction = calculateDirection(
            joystickPosition = clampedPosition,
            backgroundCenter = centerPoint,
            deadZoneRadius = deadZoneRadius
        )

        internalCanLock =
            direction == JoystickDirection.North &&
            lastDragPosition.y < -lockThresholdPx
    }

    val currentBackgroundColor = remember(theme.backgroundColor, theme.alpha) {
        theme.backgroundColor.applyAlpha(theme.alpha)
    }
    val currentJoystickColor = remember(theme.joystickColor, theme.alpha) {
        theme.joystickColor.applyAlpha(theme.alpha)
    }
    val currentJoystickCanLockColor = remember(theme.joystickCanLockColor, theme.alpha) {
        theme.joystickCanLockColor.applyAlpha(theme.alpha)
    }
    val currentJoystickLockedColor = remember(theme.joystickLockedColor, theme.alpha) {
        theme.joystickLockedColor.applyAlpha(theme.alpha)
    }
    val currentLockMarkColor = remember(theme.lockMarkColor, theme.alpha) {
        theme.lockMarkColor.applyAlpha(theme.alpha)
    }
    val currentBorderColor = remember(theme.borderColor, theme.alpha) {
        theme.borderColor.applyAlpha(theme.alpha)
    }

    Box(
        modifier = modifier.size(widgetSize),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            if (currentBackgroundRegion.contains(offset.x.toInt(), offset.y.toInt())) {
                                lastDragPosition = offset
                                updateJoystickState(offset)
                            }
                        },
                        onDrag = { change, _ ->
                            change.consume()
                            lastDragPosition = change.position
                            if (isLocked) isLocked = false
                            updateJoystickState(change.position)
                        },
                        onDragEnd = {
                            if (internalCanLock) {
                                isLocked = true
                                updateJoystickState(lockPosition)
                            } else {
                                isLocked = false
                                updateJoystickState(centerPoint)
                            }
                        },
                        onDragCancel = {
                            isLocked = false
                            updateJoystickState(centerPoint)
                        }
                    )
                }
        ) {
            drawBackgroundLayer(
                cache = drawCache,
                backgroundColor = currentBackgroundColor,
                borderColor = currentBorderColor
            )

            drawJoystick(
                outline = drawCache.knobOutline,
                color = when {
                    isLocked -> currentJoystickLockedColor
                    internalCanLock -> currentJoystickCanLockColor
                    else -> currentJoystickColor
                },
                center = joystickPosition,
                size = drawCache.knobSize
            )

            if (isLocked) {
                drawCircle(
                    color = currentLockMarkColor,
                    center = lockPosition,
                    radius = 4f
                )
            }
        }
    }
}
