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

package com.movtery.layer_controller

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.movtery.layer_controller.data.ButtonSize
import com.movtery.layer_controller.data.MAX_SIZE_PERCENTAGE
import com.movtery.layer_controller.data.MIN_SIZE_DP
import com.movtery.layer_controller.data.MIN_SIZE_PERCENTAGE
import com.movtery.layer_controller.layout.JoystickButton
import com.movtery.layer_controller.layout.TextButton
import com.movtery.layer_controller.observable.ObservableButtonStyle
import com.movtery.layer_controller.observable.ObservableControlLayer
import com.movtery.layer_controller.observable.ObservableControlLayout
import com.movtery.layer_controller.observable.ObservableJoystickData
import com.movtery.layer_controller.observable.ObservableWidget
import com.movtery.layer_controller.utils.getWidgetPosition
import com.movtery.layer_controller.utils.snap.GuideLine
import com.movtery.layer_controller.utils.snap.LineDirection
import com.movtery.layer_controller.utils.snap.SnapMode
import com.movtery.layer_controller.utils.toPercentagePosition
import kotlin.math.roundToInt

/**
 * 控制布局编辑器渲染层
 * @param selectedWidget 当前选中的控件，编辑器将会标注它
 * @param floatingButtons 选中控件后，在控件下方悬浮的按钮栏
 * @param enableSnap 是否开启吸附
 * @param snapInAllLayers 是否在全控制层范围内吸附
 * @param snapMode 吸附模式
 * @param focusedLayer 聚焦的层级，需要针对性对某一层级进行编辑时
 * @param localSnapRange 局部吸附范围（仅在Local模式下有效）
 * @param snapThresholdValue 吸附距离阈值
 */
@Composable
fun ControlEditorLayer(
    observedLayout: ObservableControlLayout,
    selectedWidget: ObservableWidget?,
    onButtonTap: (data: ObservableWidget, layer: ObservableControlLayer?) -> Unit,
    onBackgroundClick: () -> Unit,
    floatingButtons: @Composable RowScope.() -> Unit,
    enableSnap: Boolean,
    snapInAllLayers: Boolean,
    snapMode: SnapMode,
    focusedLayer: ObservableControlLayer? = null,
    isDark: Boolean = isSystemInDarkTheme(),
    localSnapRange: Dp = 20.dp,
    snapThresholdValue: Dp = 4.dp
) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        val primaryColor = MaterialTheme.colorScheme.primary

        val layers by observedLayout.layers.collectAsStateWithLifecycle()
        val styles by observedLayout.styles.collectAsStateWithLifecycle()

        val guideLines = remember { mutableStateMapOf<ObservableWidget, List<GuideLine>>() }

        val renderingLayers = when (focusedLayer) {
            null -> layers
                //仅渲染编辑器可见层
                .filter { !it.editorHide }
                //反转：将最后一层视为底层，逐步向上渲染
                .reversed()
            //开启聚焦模式
            else -> listOf(focusedLayer)
        }

        BoxWithConstraints(
            modifier = Modifier.fillMaxSize()
        ) {
            //这里临时记录正在调整大小的状态，消除抖动
            var resizingWidget by remember { mutableStateOf<ObservableWidget?>(null) }
            /** 拖动中的左上角的手柄位置 TopLeft */
            var dragTL by remember { mutableStateOf(Offset.Zero) }
            /** 拖动中的右下角的手柄位置 BottomRight */
            var dragBR by remember { mutableStateOf(Offset.Zero) }

            //空白可点击层，点击背景清除选中的按钮
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(0f)
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() },
                        onClick = onBackgroundClick
                    )
            )

            val density = LocalDensity.current
            val screenSize = remember(maxWidth, maxHeight) {
                with(density) {
                    IntSize(
                        width = maxWidth.roundToPx(),
                        height = maxHeight.roundToPx()
                    )
                }
            }

            //计算选中控件的像素边界，优先使用拖拽中的实时坐标
            val selectedWidgetBounds by remember(
                selectedWidget, resizingWidget, dragTL, dragBR, screenSize
            ) {
                derivedStateOf {
                    val widget = selectedWidget ?: return@derivedStateOf null
                    val widgetSize = widget.internalRenderSize
                    if (widgetSize == IntSize.Zero) return@derivedStateOf null

                    if (resizingWidget == widget) {
                        dragTL to dragBR
                    } else {
                        val position = getWidgetPosition(widget, widgetSize, screenSize)
                        position to Offset(position.x + widgetSize.width, position.y + widgetSize.height)
                    }
                }
            }

            ControlWidgetRenderer(
                screenSize = screenSize,
                isDark = isDark,
                renderingLayers = renderingLayers,
                styles = styles,
                enableSnap = enableSnap,
                snapInAllLayers = snapInAllLayers,
                snapMode = snapMode,
                localSnapRange = localSnapRange,
                snapThresholdValue = snapThresholdValue,
                onButtonTap = onButtonTap,
                drawLine = { data, line ->
                    guideLines[data] = line
                },
                onLineCancel = { data ->
                    guideLines.remove(data)
                }
            )
            //绘制参考线与选中框
            Canvas(modifier = Modifier.fillMaxSize()) {
                guideLines.values.forEach { guidelines ->
                    guidelines.forEach { guideline ->
                        drawLine(
                            guideline = guideline,
                            color = primaryColor
                        )
                    }
                }

                //绘制选中控件的红色方框
                selectedWidgetBounds?.let { (drawTL, drawBR) ->
                    //稍微留出点空隙
                    val padding = 4.dp.toPx()
                    drawRect(
                        color = primaryColor,
                        topLeft = Offset(drawTL.x - padding, drawTL.y - padding),
                        size = Size(
                            (drawBR.x - drawTL.x) + padding * 2,
                            (drawBR.y - drawTL.y) + padding * 2
                        ),
                        style = Stroke(width = 1.dp.toPx())
                    )
                }
            }

            //绘制调整大小的手柄
            selectedWidget?.takeIf { widget ->
                //控件的大小类型为包裹内容时，调整大小是无意义的
                widget.widgetSize.type != ButtonSize.Type.WrapContent
            }?.let { widget ->
                selectedWidgetBounds?.let { (drawTL, drawBR) ->
                    //获取尺寸约束的像素值
                    val minSizePx = with(density) { MIN_SIZE_DP.dp.toPx() }
                    val oldSize = widget.widgetSize

                    val (minWidth, maxWidth) = when (oldSize.type) {
                        ButtonSize.Type.Dp -> minSizePx to screenSize.width.toFloat()
                        ButtonSize.Type.Percentage -> {
                            val reference = if (oldSize.widthReference == ButtonSize.Reference.ScreenWidth) {
                                screenSize.width
                            } else {
                                screenSize.height
                            }
                            (reference * 0.01f) to (reference * 1.0f)
                        }
                        else -> minSizePx to screenSize.width.toFloat()
                    }

                    val (minHeight, maxHeight) = when (oldSize.type) {
                        ButtonSize.Type.Dp -> minSizePx to screenSize.height.toFloat()
                        ButtonSize.Type.Percentage -> {
                            val reference = if (oldSize.heightReference == ButtonSize.Reference.ScreenWidth) {
                                screenSize.width
                            } else {
                                screenSize.height
                            }
                            (reference * 0.01f) to (reference * 1.0f)
                        }
                        else -> minSizePx to screenSize.height.toFloat()
                    }

                    /**
                     * 拖动手柄时更新控件的位置和尺寸
                     */
                    val updateSizeAndPos = { newTopLeft: Offset, newSize: IntSize ->
                        val newPosPercentage = newTopLeft.toPercentagePosition(newSize, screenSize)
                        widget.putRenderPosition(newPosPercentage)

                        val oldSize = widget.widgetSize
                        val newWidgetSize = when (oldSize.type) {
                            ButtonSize.Type.Dp -> {
                                oldSize.copy(
                                    widthDp = with(density) { newSize.width.toDp().value },
                                    heightDp = with(density) { newSize.height.toDp().value }
                                )
                            }

                            ButtonSize.Type.Percentage -> {
                                val widthRef = when (oldSize.widthReference) {
                                    ButtonSize.Reference.ScreenWidth -> screenSize.width
                                    ButtonSize.Reference.ScreenHeight -> screenSize.height
                                }
                                val heightRef = when (oldSize.heightReference) {
                                    ButtonSize.Reference.ScreenWidth -> screenSize.width
                                    ButtonSize.Reference.ScreenHeight -> screenSize.height
                                }
                                oldSize.copy(
                                    widthPercentage = (newSize.width.toFloat() / widthRef * MAX_SIZE_PERCENTAGE)
                                        .roundToInt()
                                        .coerceIn(MIN_SIZE_PERCENTAGE, MAX_SIZE_PERCENTAGE),
                                    heightPercentage = (newSize.height.toFloat() / heightRef * MAX_SIZE_PERCENTAGE)
                                        .roundToInt()
                                        .coerceIn(MIN_SIZE_PERCENTAGE, MAX_SIZE_PERCENTAGE)
                                )
                            }
                            else -> oldSize
                        }
                        widget.putWidgetSize(newWidgetSize)
                    }

                    @Composable
                    fun ResizeHandle(
                        isTopLeft: Boolean,
                        currentPos: Offset,
                        touchSize: Dp = 30.dp,
                        visualSize: Dp = 14.dp
                    ) {
                        var activeHandleCount by remember { mutableIntStateOf(0) }
                        val touchSizePx = with(density) { touchSize.toPx() }
                        val visualSizePx = with(density) { visualSize.toPx() }

                        Box(
                            modifier = Modifier
                                .offset {
                                    IntOffset(
                                        (currentPos.x - touchSizePx / 2).roundToInt(),
                                        (currentPos.y - touchSizePx / 2).roundToInt()
                                    )
                                }
                                .size(touchSize)
                                .drawBehind {
                                    drawCircle(
                                        color = primaryColor,
                                        radius = visualSizePx / 2,
                                        center = center
                                    )
                                }
                                .pointerInput(widget, screenSize, minWidth, maxWidth, minHeight, maxHeight) {
                                    detectDragGestures(
                                        onDragStart = {
                                            if (activeHandleCount == 0) {
                                                val currentWidgetSize = widget.internalRenderSize
                                                val pos = getWidgetPosition(widget, currentWidgetSize, screenSize)
                                                dragTL = pos
                                                dragBR = Offset(pos.x + currentWidgetSize.width, pos.y + currentWidgetSize.height)
                                            }
                                            activeHandleCount++
                                            resizingWidget = widget
                                            widget.movingOffset = dragTL
                                            widget.isEditingPos = true
                                        },
                                        onDrag = { change, dragAmount ->
                                            change.consume()
                                            if (isTopLeft) {
                                                val newTL = dragTL + dragAmount
                                                var finalTL = Offset(
                                                    newTL.x.coerceIn(maxOf(0f, dragBR.x - maxWidth), dragBR.x - minWidth),
                                                    newTL.y.coerceIn(maxOf(0f, dragBR.y - maxHeight), dragBR.y - minHeight)
                                                )
                                                if (widget is ObservableJoystickData) {
                                                    val size = minOf(dragBR.x - finalTL.x, dragBR.y - finalTL.y)
                                                    finalTL = Offset(dragBR.x - size, dragBR.y - size)
                                                }
                                                dragTL = finalTL
                                                widget.movingOffset = finalTL
                                                val finalSize = IntSize(
                                                    (dragBR.x - finalTL.x).roundToInt(),
                                                    (dragBR.y - finalTL.y).roundToInt()
                                                )
                                                updateSizeAndPos(finalTL, finalSize)
                                            } else {
                                                val newBR = dragBR + dragAmount
                                                var finalBR = Offset(
                                                    newBR.x.coerceIn(dragTL.x + minWidth, minOf(screenSize.width.toFloat(), dragTL.x + maxWidth)),
                                                    newBR.y.coerceIn(dragTL.y + minHeight, minOf(screenSize.height.toFloat(), dragTL.y + maxHeight))
                                                )
                                                if (widget is ObservableJoystickData) {
                                                    val size = minOf(finalBR.x - dragTL.x, finalBR.y - dragTL.y)
                                                    finalBR = Offset(dragTL.x + size, dragTL.y + size)
                                                }
                                                dragBR = finalBR
                                                val finalSize = IntSize(
                                                    (finalBR.x - dragTL.x).roundToInt(),
                                                    (finalBR.y - dragTL.y).roundToInt()
                                                )
                                                updateSizeAndPos(dragTL, finalSize)
                                            }
                                        },
                                        onDragEnd = {
                                            activeHandleCount = (activeHandleCount - 1).coerceAtLeast(0)
                                            if (activeHandleCount == 0) {
                                                resizingWidget = null
                                                widget.isEditingPos = false
                                            }
                                        },
                                        onDragCancel = {
                                            activeHandleCount = (activeHandleCount - 1).coerceAtLeast(0)
                                            if (activeHandleCount == 0) {
                                                resizingWidget = null
                                                widget.isEditingPos = false
                                            }
                                        }
                                    )
                                }
                        )
                    }

                    //左上角手柄
                    ResizeHandle(isTopLeft = true, currentPos = drawTL)
                    //右下角手柄
                    ResizeHandle(isTopLeft = false, currentPos = drawBR)
                }
            }

            //悬浮功能按钮栏
            selectedWidget?.let {
                selectedWidgetBounds?.let { (drawTL, drawBR) ->
                    var barSize by remember { mutableStateOf(IntSize.Zero) }

                    val centerX = (drawTL.x + drawBR.x) / 2
                    val targetY = drawBR.y + with(density) { 8.dp.toPx() }

                    //居中显示，但不能超出屏幕左右边界
                    val xPos = (centerX - barSize.width / 2)
                        .coerceIn(0f, maxOf(0f, screenSize.width.toFloat() - barSize.width))
                    val yPos = targetY
                        .coerceAtMost(maxOf(0f, screenSize.height.toFloat() - barSize.height))

                    Row(
                        modifier = Modifier
                            .onSizeChanged { barSize = it }
                            //加载好位置之后再显示，否则有点影响体验
                            .alpha(if (barSize != IntSize.Zero) 1f else 0f)
                            .offset {
                                IntOffset(xPos.roundToInt(), yPos.roundToInt())
                            },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        content = floatingButtons
                    )
                }
            }
        }
    }
}

/**
 * 根据吸附参考线绘制线条
 */
private fun DrawScope.drawLine(
    guideline: GuideLine,
    color: Color/* = Color(0xFFFF5252)*/,
    strokeWidth: Float = 2f
) {
    when (guideline.direction) {
        LineDirection.Vertical -> {
            drawLine(
                color = color,
                start = Offset(guideline.coordinate, 0f),
                end = Offset(guideline.coordinate, size.height),
                strokeWidth = strokeWidth
            )
        }
        LineDirection.Horizontal -> {
            drawLine(
                color = color,
                start = Offset(0f, guideline.coordinate),
                end = Offset(size.width, guideline.coordinate),
                strokeWidth = strokeWidth
            )
        }
    }
}

/**
 * @param enableSnap 是否开启吸附功能
 * @param snapMode 吸附模式
 * @param snapInAllLayers 是否在全控制层范围内吸附
 * @param localSnapRange 局部吸附范围（仅在Local模式下有效）
 * @param snapThresholdValue 吸附距离阈值
 * @param drawLine 绘制吸附参考线
 * @param onLineCancel 取消吸附参考线
 */
@Composable
private fun ControlWidgetRenderer(
    screenSize: IntSize,
    isDark: Boolean,
    renderingLayers: List<ObservableControlLayer>,
    styles: List<ObservableButtonStyle>,
    enableSnap: Boolean,
    snapInAllLayers: Boolean,
    snapMode: SnapMode,
    localSnapRange: Dp,
    snapThresholdValue: Dp,
    onButtonTap: (data: ObservableWidget, layer: ObservableControlLayer?) -> Unit,
    drawLine: (ObservableWidget, List<GuideLine>) -> Unit,
    onLineCancel: (ObservableWidget) -> Unit
) {
    val allWidgetsMap = remember { mutableStateMapOf<ObservableControlLayer?, List<ObservableWidget>>() }
    val snapInAllLayers1 by rememberUpdatedState(snapInAllLayers)

    @Composable
    fun RenderWidget(
        data: ObservableWidget,
        layer: ObservableControlLayer,
        isPressed: Boolean
    ) {
        when (data) {
            is ObservableJoystickData -> {
                JoystickButton(
                    data = data,
                    screenSize = screenSize,
                    isDark = isDark,
                    enableSnap = enableSnap,
                    snapMode = snapMode,
                    localSnapRange = localSnapRange,
                    getOtherWidgets = {
                        allWidgetsMap
                            .filter { (layer1, _) ->
                                snapInAllLayers1 || layer1 == layer
                            }
                            .values.flatten().filter { it != data }
                    },
                    snapThresholdValue = snapThresholdValue,
                    drawLine = drawLine,
                    onLineCancel = onLineCancel,
                    onTapInEditMode = {
                        onButtonTap(data, layer)
                    }
                )
            }
            else -> {
                TextButton(
                    isEditMode = true,
                    data = data,
                    allStyles = styles,
                    screenSize = screenSize,
                    isDark = isDark,
                    enableSnap = enableSnap,
                    snapMode = snapMode,
                    localSnapRange = localSnapRange,
                    getOtherWidgets = {
                        allWidgetsMap
                            .filter { (layer1, _) ->
                                snapInAllLayers1 || layer1 == layer
                            }
                            .values.flatten().filter { it != data }
                    },
                    snapThresholdValue = snapThresholdValue,
                    drawLine = drawLine,
                    onLineCancel = onLineCancel,
                    isPressed = isPressed,
                    onTapInEditMode = {
                        onButtonTap(data, layer)
                    }
                )
            }
        }
    }

    Layout(
        content = {
            //按图层顺序渲染所有可见的控件
            renderingLayers.forEach { layer ->
                val normalButtons by layer.normalButtons.collectAsStateWithLifecycle()
                val textBoxes by layer.textBoxes.collectAsStateWithLifecycle()
                val joysticks by layer.joysticks.collectAsStateWithLifecycle()

                val widgetsInLayer = normalButtons + textBoxes + joysticks
                allWidgetsMap[layer] = widgetsInLayer

                textBoxes.forEach { data ->
                    RenderWidget(data, layer, isPressed = false)
                }

                normalButtons.forEach { data ->
                    RenderWidget(data, layer, data.isPressed)
                }

                joysticks.forEach { data ->
                    RenderWidget(data, layer, isPressed = false)
                }
            }
        }
    ) { measurables, constraints ->
        val placeables = measurables.map { measurable ->
            measurable.measure(constraints)
        }

        var index = 0
        fun ObservableWidget.putSize() {
            if (index < placeables.size) {
                val placeable = placeables[index]
                this.internalRenderSize = IntSize(placeable.width, placeable.height)
                index++
            }
        }

        renderingLayers.fastForEach { layer ->
            layer.textBoxes.value.fastForEach { it.putSize() }
            layer.normalButtons.value.fastForEach { it.putSize() }
            layer.joysticks.value.fastForEach { it.putSize() }
        }

        layout(constraints.maxWidth, constraints.maxHeight) {
            var placeableIndex = 0
            fun ObservableWidget.place() {
                if (placeableIndex < placeables.size) {
                    val placeable = placeables[placeableIndex]
                    val position = getWidgetPosition(
                        data = this,
                        widgetSize = IntSize(placeable.width, placeable.height),
                        screenSize = screenSize
                    )
                    placeable.place(position.x.toInt(), position.y.toInt())
                    placeableIndex++
                }
            }

            renderingLayers.fastForEach { layer ->
                layer.textBoxes.value.fastForEach { it.place() }
                layer.normalButtons.value.fastForEach { it.place() }
                layer.joysticks.value.fastForEach { it.place() }
            }
        }
    }
}