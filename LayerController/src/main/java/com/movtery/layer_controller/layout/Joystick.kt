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

package com.movtery.layer_controller.layout

import android.graphics.Rect
import android.graphics.Region
import androidx.annotation.FloatRange
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.input.pointer.PointerId
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.changedToDown
import androidx.compose.ui.input.pointer.changedToUpIgnoreConsumed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import com.movtery.layer_controller.observable.ObservableJoystickData
import com.movtery.layer_controller.observable.ObservableJoystickStyle
import com.movtery.layer_controller.observable.ObservableWidget
import com.movtery.layer_controller.utils.buttonSize
import com.movtery.layer_controller.utils.editMode
import com.movtery.layer_controller.utils.snap.GuideLine
import com.movtery.layer_controller.utils.snap.SnapMode
import kotlinx.coroutines.coroutineScope
import kotlin.math.atan2
import kotlin.math.sqrt

/**
 * 摇杆、方向键通用方向
 */
enum class JoystickDirection {
    East,
    NorthEast,
    North,
    NorthWest,
    West,
    SouthWest,
    South,
    SouthEast,
    /**
     * 无方向
     */
    None
}

/**
 * 根据可观察的摇杆样式对象一键设置样式的移动摇杆控件
 * @param isDarkTheme 是否处于暗色模式中，用于选择样式
 * @param style 可观察的摇杆样式
 * @param deadZoneRatio 死区范围，作为半分比，根据组件整体大小计算
 * @param lockThreshold 前进锁判定范围（在组件的外部，正上方），作为百分比，根据组件整体大小计算
 * @param onDirectionChanged 当摇杆的方向变更时，使用这个函数回调
 * @param canLock 是否可以进行前进锁
 * @param onCanLock 当遥感可以进行前进锁定时，或者不能进行前进锁定时，使用这个函数回调
 * @param onLock 当摇杆触发前进锁，或者离开锁定状态时，使用这个函数回调
 */
@Composable
fun StyleableJoystick(
    modifier: Modifier = Modifier,
    isDarkTheme: Boolean,
    style: ObservableJoystickStyle,
    @FloatRange(from = 0.0, to = 1.0)
    deadZoneRatio: Float = 0.5f,
    @FloatRange(from = 0.0, to = 1.0)
    lockThreshold: Float = 0.3f,
    onDirectionChanged: (JoystickDirection) -> Unit = {},
    canLock: Boolean = true,
    onCanLock: (Boolean) -> Unit = {},
    onLock: (Boolean) -> Unit = {},
    enablePointerInput: Boolean = true,
    // 如果提供了外部状态，则使用外部状态，否则内部管理状态
    externalOffset: Offset? = null,
    externalIsLocked: Boolean? = null,
    externalCanLockInternal: Boolean? = null
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

    Joystick(
        modifier = modifier,
        alpha = theme.alpha,
        backgroundColor = theme.backgroundColor,
        joystickColor = theme.joystickColor,
        joystickCanLockColor = theme.joystickCanLockColor,
        joystickLockedColor = theme.joystickLockedColor,
        lockMarkColor = theme.lockMarkColor,
        borderColor = theme.borderColor,
        borderWidthRatio = borderWidthRatio,
        backgroundShape = backgroundShape,
        joystickShape = joystickShape,
        joystickSize = theme.joystickSize,
        deadZoneRatio = deadZoneRatio,
        lockThreshold = lockThreshold,
        onDirectionChanged = onDirectionChanged,
        canLock = canLock,
        onCanLock = onCanLock,
        onLock = onLock,
        enablePointerInput = enablePointerInput,
        externalOffset = externalOffset,
        externalIsLocked = externalIsLocked,
        externalCanLockInternal = externalCanLockInternal
    )
}

/**
 * 移动摇杆控件
 */
@Composable
fun Joystick(
    modifier: Modifier = Modifier,
    @FloatRange(from = 0.0, to = 1.0)
    alpha: Float = 1.0f,
    backgroundColor: Color = Color.Black.copy(alpha = 0.5f),
    joystickColor: Color = Color.White.copy(alpha = 0.5f),
    joystickCanLockColor: Color = Color.Yellow.copy(alpha = 0.5f),
    joystickLockedColor: Color = Color.Green.copy(alpha = 0.5f),
    lockMarkColor: Color = Color.White,
    borderColor: Color = Color.White,
    @FloatRange(from = 0.0, to = 0.5)
    borderWidthRatio: Float = 0f,
    backgroundShape: Shape = CircleShape,
    joystickShape: Shape = CircleShape,
    @FloatRange(from = 0.0, to = 1.0)
    joystickSize: Float = 0.5f,
    @FloatRange(from = 0.0, to = 1.0)
    deadZoneRatio: Float = 0.5f,
    @FloatRange(from = 0.0, to = 1.0)
    lockThreshold: Float = 0.3f,
    onDirectionChanged: (JoystickDirection) -> Unit = {},
    canLock: Boolean = true,
    onCanLock: (Boolean) -> Unit = {},
    onLock: (Boolean) -> Unit = {},
    enablePointerInput: Boolean = true,
    externalOffset: Offset? = null,
    externalIsLocked: Boolean? = null,
    externalCanLockInternal: Boolean? = null
) {
    //使用这个标记来判断是否渲染摇杆组件，未完全初始化时，可能导致组件闪烁
    var initialized by remember { mutableStateOf(false) }

    val density = LocalDensity.current
    val layoutDirection = LocalLayoutDirection.current

    val currentBackgroundColor =
        remember(backgroundColor, alpha) { backgroundColor.applyAlpha(alpha) }
    val currentJoystickColor = remember(joystickColor, alpha) { joystickColor.applyAlpha(alpha) }
    val currentJoystickCanLockColor =
        remember(joystickCanLockColor, alpha) { joystickCanLockColor.applyAlpha(alpha) }
    val currentJoystickLockedColor =
        remember(joystickLockedColor, alpha) { joystickLockedColor.applyAlpha(alpha) }
    val currentLockMarkColor = remember(lockMarkColor, alpha) { lockMarkColor.applyAlpha(alpha) }
    val currentBorderColor = remember(borderColor, alpha) { borderColor.applyAlpha(alpha) }
    val currentCanLock by rememberUpdatedState(canLock)

    var backgroundSizePx by remember { mutableFloatStateOf(0f) }

    val backgroundRegion = remember(backgroundShape, backgroundSizePx) {
        backgroundShape.toRegion(
            size = Size(backgroundSizePx, backgroundSizePx),
            density = density,
            layoutDirection = layoutDirection
        )
    }
    val currentBackgroundRegion by rememberUpdatedState(backgroundRegion)

    val joystickSizePx = remember(backgroundSizePx, joystickSize) {
        backgroundSizePx * joystickSize.coerceIn(0.0f, 1.0f)
    }
    val currentJoystickSizePx by rememberUpdatedState(joystickSizePx)

    val centerPoint =
        remember(backgroundSizePx) { Offset(backgroundSizePx / 2, backgroundSizePx / 2) }
    val currentCenterPoint by rememberUpdatedState(centerPoint)

    val deadZoneRadius =
        remember(backgroundSizePx, deadZoneRatio) { backgroundSizePx * deadZoneRatio / 2 }
    val currentDeadZoneRadius by rememberUpdatedState(deadZoneRadius)

    val lockThresholdPx =
        remember(backgroundSizePx, lockThreshold) { backgroundSizePx * lockThreshold }
    val currentLockThresholdPx by rememberUpdatedState(lockThresholdPx)

    val lockPosition = remember(centerPoint) { Offset(centerPoint.x, 0f) }
    val currentLockPosition by rememberUpdatedState(lockPosition)

    // 内部状态
    var internalCanLock by remember { mutableStateOf(false) }
    var internalDirection by remember { mutableStateOf(JoystickDirection.None) }
    var internalJoystickPosition by remember { mutableStateOf(currentCenterPoint) }
    var internalIsLocked by remember { mutableStateOf(false) }
    var lastDragPosition by remember { mutableStateOf(Offset.Zero) }

    // 使用外部偏移量时，需要转换（外部是相对于中心的偏移，内部是绝对坐标）
    val joystickPosition =
        externalOffset?.let { currentCenterPoint + it } ?: internalJoystickPosition
    val isLocked = externalIsLocked ?: internalIsLocked
    val canLockInternal = externalCanLockInternal ?: internalCanLock

    fun updateJoystickState(position: Offset = currentCenterPoint) {
        val clampedPosition = position.clampToRegion(
            region = currentBackgroundRegion,
            center = currentCenterPoint
        )

        internalJoystickPosition = clampedPosition

        val direction = calculateDirection(
            joystickPosition = clampedPosition,
            backgroundCenter = currentCenterPoint,
            deadZoneRadius = currentDeadZoneRadius
        )
        internalDirection = direction

        internalCanLock =
            currentCanLock &&
                    direction == JoystickDirection.North &&
                    lastDragPosition.y < -currentLockThresholdPx
    }

    LaunchedEffect(backgroundSizePx) {
        initialized = false
        updateJoystickState()
        initialized = true
    }

    LaunchedEffect(backgroundShape) {
        updateJoystickState(internalJoystickPosition)
    }

    if (externalOffset == null) {
        LaunchedEffect(internalDirection) { onDirectionChanged(internalDirection) }
        LaunchedEffect(internalCanLock) { onCanLock(internalCanLock) }
        LaunchedEffect(internalIsLocked) { onLock(internalIsLocked) }
    }

    DisposableEffect(Unit) {
        onDispose {
            if (externalOffset == null) onDirectionChanged(JoystickDirection.None)
            initialized = false
        }
    }

    Box(
        modifier = modifier
            .onSizeChanged {
                backgroundSizePx = it.width.toFloat()
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .then(
                    if (enablePointerInput) {
                        Modifier.pointerInput(Unit) {
                            simpleDrag(
                                hitTest = { pos ->
                                    currentBackgroundRegion.contains(pos.x.toInt(), pos.y.toInt())
                                },
                                onPointerMove = { offset ->
                                    lastDragPosition = offset
                                    if (internalIsLocked) internalIsLocked = false
                                    updateJoystickState(offset)
                                },
                                onPointerRelease = {
                                    if (internalCanLock) {
                                        internalIsLocked = true
                                        updateJoystickState(currentLockPosition)
                                    } else {
                                        internalIsLocked = false
                                        updateJoystickState(currentCenterPoint)
                                    }
                                }
                            )
                        }
                    } else Modifier
                )
        ) {
            if (initialized) {
                val minSide = minOf(this@Canvas.size.width, this@Canvas.size.height)

                drawBackgroundLayer(
                    layoutDirection = layoutDirection,
                    size = this@Canvas.size,
                    shape = backgroundShape,
                    backgroundColor = currentBackgroundColor,
                    borderColor = currentBorderColor,
                    borderWidthPx = (minSide * borderWidthRatio).coerceAtLeast(0f)
                )

                drawJoystick(
                    layoutDirection = layoutDirection,
                    color = when {
                        isLocked -> currentJoystickLockedColor
                        canLockInternal -> currentJoystickCanLockColor
                        else -> currentJoystickColor
                    },
                    center = joystickPosition,
                    size = currentJoystickSizePx,
                    shape = joystickShape
                )

                if (isLocked) {
                    drawCircle(
                        color = currentLockMarkColor,
                        center = currentLockPosition,
                        radius = 4f
                    )
                }
            }
        }
    }
}

/**
 * 编辑模式下的摇杆控件
 */
@Composable
internal fun JoystickButton(
    data: ObservableWidget,
    screenSize: IntSize,
    isDark: Boolean,
    enableSnap: Boolean,
    snapMode: SnapMode,
    localSnapRange: Dp,
    getOtherWidgets: () -> List<ObservableWidget>,
    snapThresholdValue: Dp,
    drawLine: (ObservableWidget, List<GuideLine>) -> Unit,
    onLineCancel: (ObservableWidget) -> Unit,
    onTapInEditMode: () -> Unit
) {
    val joystickData = data as? ObservableJoystickData ?: return

    StyleableJoystick(
        modifier = Modifier
            .buttonSize(data, screenSize)
            .editMode(
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
                onTapInEditMode = onTapInEditMode
            ),
        isDarkTheme = isDark,
        style = joystickData.style,
        deadZoneRatio = joystickData.deadZoneRatio,
        lockThreshold = joystickData.lockThreshold,
        canLock = joystickData.canLock,
        //编辑模式下禁用真实交互
        onDirectionChanged = {},
        onCanLock = {},
        onLock = {},
        enablePointerInput = false
    )
}

/**
 * 以下代码块从 ZalithLauncher 迁移，略作调整以适应库环境
 */

private suspend fun PointerInputScope.simpleDrag(
    hitTest: (Offset) -> Boolean,
    onPointerMove: (position: Offset) -> Unit,
    onPointerRelease: () -> Unit
) {
    coroutineScope {
        var activePointer: PointerId? = null
        awaitPointerEventScope {
            while (true) {
                val event = awaitPointerEvent()
                event.changes
                    .filter { it.changedToDown() }
                    .forEach { change ->
                        val pointerId = change.id
                        if (activePointer == null) {
                            val pos = change.position
                            if (hitTest(pos)) {
                                activePointer = pointerId
                                onPointerMove(pos)
                            }
                        }
                    }
                activePointer?.let { pointerId ->
                    event.changes
                        .firstOrNull { it.id == pointerId && it.positionChanged() && !it.isConsumed }
                        ?.let { moveChange ->
                            onPointerMove(moveChange.position)
                            moveChange.consume()
                        }
                }
                event.changes
                    .filter { it.changedToUpIgnoreConsumed() }
                    .forEach { change ->
                        val pointerId = change.id
                        if (pointerId == activePointer) {
                            onPointerRelease()
                            activePointer = null
                        }
                    }
            }
        }
    }
}

private fun DrawScope.drawBackgroundLayer(
    layoutDirection: LayoutDirection,
    size: Size,
    shape: Shape,
    backgroundColor: Color,
    borderColor: Color,
    borderWidthPx: Float
) {
    val outline = shape.createOutline(
        size = size,
        layoutDirection = layoutDirection,
        density = this
    )
    val clipPath = when (outline) {
        is Outline.Generic -> outline.path
        is Outline.Rounded -> Path().apply { addRoundRect(outline.roundRect) }
        is Outline.Rectangle -> Path().apply { addRect(outline.rect) }
    }
    clipPath(clipPath) {
        drawOutline(outline = outline, color = backgroundColor)
        if (borderWidthPx > 0f) {
            drawOutline(
                outline = outline,
                color = borderColor,
                style = Stroke(width = borderWidthPx)
            )
        }
    }
}

private fun DrawScope.drawJoystick(
    layoutDirection: LayoutDirection,
    color: Color,
    center: Offset,
    size: Float,
    shape: Shape
) {
    val halfSize = size / 2
    val topLeftX = center.x - halfSize
    val topLeftY = center.y - halfSize
    val outline = shape.createOutline(
        size = Size(size, size),
        layoutDirection = layoutDirection,
        density = this
    )
    translate(left = topLeftX, top = topLeftY) {
        drawOutline(outline = outline, color = color)
    }
}

fun Shape.toRegion(size: Size, density: Density, layoutDirection: LayoutDirection): Region {
    val outline: Outline = this.createOutline(size, layoutDirection, density)
    val composePath: Path = when (outline) {
        is Outline.Rectangle -> Path().apply { addRect(outline.rect) }
        is Outline.Rounded -> Path().apply { addRoundRect(outline.roundRect) }
        is Outline.Generic -> outline.path
    }
    val androidPath = composePath.asAndroidPath()
    val region = Region()
    val rect = Rect(0, 0, size.width.toInt(), size.height.toInt())
    region.setPath(androidPath, Region(rect))
    return region
}

fun Offset.clampToRegion(region: Region, center: Offset): Offset {
    if (region.contains(x.toInt(), y.toInt())) return this
    var low = 0f
    var high = 1f
    var result = center
    repeat(10) {
        val mid = (low + high) / 2
        val testPoint = center + (this - center) * mid
        if (region.contains(testPoint.x.toInt(), testPoint.y.toInt())) {
            result = testPoint
            low = mid
        } else {
            high = mid
        }
    }
    return result
}

private fun calculateDirection(
    joystickPosition: Offset,
    backgroundCenter: Offset,
    deadZoneRadius: Float
): JoystickDirection {
    if (joystickPosition == backgroundCenter) return JoystickDirection.None
    val vector = joystickPosition - backgroundCenter
    val distance = sqrt(vector.x * vector.x + vector.y * vector.y)
    if (distance < deadZoneRadius) return JoystickDirection.None
    val angle = Math.toDegrees(atan2(vector.y.toDouble(), vector.x.toDouble())).toFloat()
    return when {
        angle >= -22.5f && angle < 22.5f -> JoystickDirection.East
        angle in 22.5f..<67.5f -> JoystickDirection.SouthEast
        angle in 67.5f..<112.5f -> JoystickDirection.South
        angle in 112.5f..<157.5f -> JoystickDirection.SouthWest
        angle >= 157.5f || angle < -157.5f -> JoystickDirection.West
        angle >= -157.5f && angle < -112.5f -> JoystickDirection.NorthWest
        angle >= -112.5f && angle < -67.5f -> JoystickDirection.North
        angle >= -67.5f && angle < -22.5f -> JoystickDirection.NorthEast
        else -> JoystickDirection.None
    }
}

private fun Color.applyAlpha(multiplier: Float): Color {
    return copy(alpha = this.alpha * multiplier)
}
