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

import androidx.annotation.FloatRange
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerId
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.movtery.layer_controller.data.HideLayerWhen
import com.movtery.layer_controller.data.VisibilityType
import com.movtery.layer_controller.event.EventHandler
import com.movtery.layer_controller.layout.TextButton
import com.movtery.layer_controller.observable.ObservableButtonStyle
import com.movtery.layer_controller.observable.ObservableControlLayer
import com.movtery.layer_controller.observable.ObservableControlLayout
import com.movtery.layer_controller.observable.ObservableWidget
import com.movtery.layer_controller.utils.getWidgetPosition

/**
 * 控制布局画布
 * @param observedLayout 需要监听并绘制的控制布局
 * @param eventHandler 处理控制布局事件用到的处理器
 * @param checkOccupiedPointers 检查已占用的指针，防止底层正在被使用的指针仍被控制布局画布处理
 * @param opacity 控制布局画布整体不透明度 0f~1f
 * @param markPointerAsMoveOnly 标记指针为仅接受滑动处理
 * @param hideLayerWhen 根据情况决定是否隐藏指定控件层
 * @param isUsingJoystick 是否正在使用摇杆组件
 */
@Composable
fun ControlBoxLayout(
    modifier: Modifier = Modifier,
    observedLayout: ObservableControlLayout? = null,
    eventHandler: EventHandler = EventHandler(),
    isUsingJoystick: Boolean,
    isCursorGrabbing: Boolean,
    checkOccupiedPointers: (PointerId) -> Boolean,
    @FloatRange(0.0, 1.0) opacity: Float = 1f,
    markPointerAsMoveOnly: (PointerId) -> Unit = {},
    hideLayerWhen: HideLayerWhen = HideLayerWhen.None,
    isDark: Boolean = isSystemInDarkTheme(),
    content: @Composable BoxScope.() -> Unit
) {
    when {
        observedLayout == null -> {
            Box(
                modifier = modifier,
                contentAlignment = Alignment.BottomCenter
            ) {
                //控件处于加载状态
                LinearProgressIndicator(
                    modifier = Modifier.padding(all = 16.dp)
                )
            }
        }
        else -> {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                key(observedLayout.hashCode()) {
                    BoxWithConstraints(
                        modifier = modifier
                    ) {
                        BaseControlBoxLayout(
                            modifier = Modifier.fillMaxSize(),
                            observedLayout = observedLayout,
                            eventHandler = eventHandler,
                            checkOccupiedPointers = checkOccupiedPointers,
                            opacity = opacity,
                            markPointerAsMoveOnly = markPointerAsMoveOnly,
                            isUsingJoystick = isUsingJoystick,
                            isCursorGrabbing = isCursorGrabbing,
                            hideLayerWhen = hideLayerWhen,
                            isDark = isDark,
                            content = content
                        )
                    }
                }
            }
        }
    }
}

/**
 * 控制布局画布
 */
@Composable
private fun BoxWithConstraintsScope.BaseControlBoxLayout(
    modifier: Modifier = Modifier,
    observedLayout: ObservableControlLayout,
    eventHandler: EventHandler,
    checkOccupiedPointers: (PointerId) -> Boolean,
    @FloatRange(0.0, 1.0) opacity: Float,
    markPointerAsMoveOnly: (PointerId) -> Unit,
    isUsingJoystick: Boolean,
    isCursorGrabbing: Boolean,
    hideLayerWhen: HideLayerWhen,
    isDark: Boolean,
    content: @Composable BoxScope.() -> Unit
) {
//    val isDarkMode by rememberUpdatedState(isSystemInDarkTheme())

    val layers by observedLayout.layers.collectAsStateWithLifecycle()
    val reversedLayers = remember(layers) { layers.reversed() }
    val styles by observedLayout.styles.collectAsStateWithLifecycle()

    val allActiveWidgets = remember { mutableStateMapOf<PointerId, List<ObservableWidget>>() }
    val currentCheckOccupiedPointers by rememberUpdatedState(checkOccupiedPointers)
    val currentIsCursorGrabbing by rememberUpdatedState(isCursorGrabbing)
    val currentHideLayerWhen by rememberUpdatedState(hideLayerWhen)

    val density = LocalDensity.current
    val screenSize = remember(maxWidth, maxHeight) {
        with(density) {
            IntSize(
                width = maxWidth.roundToPx(),
                height = maxHeight.roundToPx()
            )
        }
    }

    Box(
        modifier = modifier
            .pointerInput(reversedLayers, hideLayerWhen) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent(pass = PointerEventPass.Initial)

                        event.changes.forEach { change ->
                            val position = change.position
                            val pointerId = change.id
                            val isPressed = change.pressed

                            //抬起时，总是尝试释放该指针下活跃的按钮
                            //避免子级占用了指针后，导致按钮状态无法被释放
                            if (!isPressed) {
                                allActiveWidgets.remove(pointerId)?.forEach { widget ->
                                    widget.onReleaseEvent(eventHandler, reversedLayers)
                                }
                                return@forEach
                            }

                            if (
                                change.isConsumed ||
                                //不处理被子级占用的指针
                                currentCheckOccupiedPointers(pointerId)
                            ) {
                                return@forEach
                            }

                            //在可见控件层中，收集所有可见的按钮
                            val visibleWidgets: List<ObservableWidget> = layers //使用原始控件层顺序，保证触摸逻辑正常
                                .filter { layer ->
                                    checkLayerVisibility(
                                        layer = layer,
                                        hideLayerWhen = currentHideLayerWhen,
                                        isUsingJoystick = isUsingJoystick,
                                        isCursorGrabbing = currentIsCursorGrabbing,
                                        visibilityType = layer.visibilityType
                                    )
                                }
                                .flatMap { layer ->
                                    //顶向下的顺序影响控件层的处理优先级
                                    layer.normalButtons.value.reversed()
                                }

                            //查找当前指针在哪些按钮上
                            val targetWidgets = visibleWidgets
                                .filter { widget ->
                                    if (!widget.canTouch()) return@filter false

                                    if (!checkVisibility(currentIsCursorGrabbing, widget.onCheckVisibilityType())) {
                                        //隐藏了，不响应事件
                                        return@filter false
                                    }

                                    val size = widget.internalRenderSize
                                    val offset = getWidgetPosition(widget, size, screenSize)

                                    val x = position.x
                                    val y = position.y
                                    x >= offset.x && x <= offset.x + size.width &&
                                            y >= offset.y && y <= offset.y + size.height
                                }.let { list ->
                                    if (list.isEmpty()) return@let list

                                    val firstDeepWidget = list.firstOrNull { it.supportsDeepTouchDetection() }

                                    when {
                                        firstDeepWidget == null -> list
                                        else -> {
                                            val topIndex = list.indexOf(firstDeepWidget)
                                            list.subList(0, topIndex + 1).filter { widget ->
                                                !widget.canProcess()
                                            }
                                        }
                                    }
                                }

                            var activeWidgets = allActiveWidgets[pointerId] ?: emptyList()

                            //检查是否移出边界
                            if (activeWidgets.isNotEmpty()) {
                                val backInBounds = mutableListOf<ObservableWidget>()
                                val releasedWidgets = mutableListOf<ObservableWidget>()
                                for (widget in activeWidgets) {
                                    //检查组件是否可以响应移除边界即松开
                                    if (!widget.isReleaseOnOutOfBounds()) continue

                                    val size = widget.internalRenderSize
                                    val offset = getWidgetPosition(widget, size, screenSize)
                                    val isOutOfBounds = position.x !in offset.x..(offset.x + size.width) ||
                                            position.y !in offset.y..(offset.y + size.height)

                                    if (isOutOfBounds) {
                                        widget.onReleaseEvent(eventHandler, reversedLayers)
                                        releasedWidgets.add(widget)
                                    } else {
                                        backInBounds.add(widget)
                                    }
                                }
                                //fix: 移出边界时应移出组，否则滑动回已释放的按钮无法再次触发
                                if (releasedWidgets.isNotEmpty()) {
                                    activeWidgets = activeWidgets - releasedWidgets
                                    allActiveWidgets[pointerId] = activeWidgets
                                }
                                //fix: 应该在抬起事件全部处理完成后再处理 #941
                                if (backInBounds.isNotEmpty()) {
                                    for (widget in backInBounds) {
                                        widget.onPointerBackInBounds(eventHandler, reversedLayers)
                                    }
                                }
                            }

                            when {
                                targetWidgets.isEmpty() -> {}
                                else -> {
                                    for (targetWidget in targetWidgets) {
                                        if (targetWidget.canProcess()) {
                                            return@forEach //拒绝处理该事件
                                        }

                                        targetWidget.onTouchEvent(
                                            eventHandler = eventHandler,
                                            allLayers = reversedLayers,
                                            change = change,
                                            activeWidgets = activeWidgets,
                                            addThis = {
                                                allActiveWidgets[pointerId] = activeWidgets + listOf(targetWidget)
                                            },
                                            consumeEvent = { value ->
                                                if (value) {
                                                    change.consume()
                                                } else {
                                                    //将指针标记为仅接受滑动处理
                                                    //期望子级不对点击事件等进行处理
                                                    markPointerAsMoveOnly(pointerId)
                                                }
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
    ) {
        content()

        ControlsRendererLayer(
            isDark = isDark,
            opacity = opacity,
            layers = reversedLayers,
            styles = styles,
            screenSize = screenSize,
            eventHandler = eventHandler,
            isUsingJoystick = isUsingJoystick,
            isCursorGrabbing = currentIsCursorGrabbing,
            hideLayerWhen = currentHideLayerWhen
        )
    }
}

@Composable
private fun ControlsRendererLayer(
    isDark: Boolean,
    @FloatRange(0.0, 1.0) opacity: Float,
    layers: List<ObservableControlLayer>,
    styles: List<ObservableButtonStyle>,
    screenSize: IntSize,
    eventHandler: EventHandler,
    isUsingJoystick: Boolean,
    isCursorGrabbing: Boolean,
    hideLayerWhen: HideLayerWhen
) {
    Layout(
        modifier = Modifier.alpha(alpha = opacity),
        content = {
            //按图层顺序渲染所有可见的控件
            layers.forEach { layer ->
                val layerVisibility = checkLayerVisibility(
                    layer = layer,
                    hideLayerWhen = hideLayerWhen,
                    isUsingJoystick = isUsingJoystick,
                    isCursorGrabbing = isCursorGrabbing,
                    visibilityType = layer.visibilityType
                )
                val normalButtons by layer.normalButtons.collectAsStateWithLifecycle()
                val textBoxes by layer.textBoxes.collectAsStateWithLifecycle()

                textBoxes.forEach { data ->
                    TextButton(
                        isEditMode = false,
                        data = data,
                        allStyles = styles,
                        screenSize = screenSize,
                        isDark = isDark,
                        visible = layerVisibility && checkVisibility(isCursorGrabbing, data.visibilityType),
                        getOtherWidgets = { emptyList() }, //不需要计算吸附
                        snapThresholdValue = 4.dp,
                        eventHandler = eventHandler,
                        isPressed = false //文本框不需要按压状态
                    )
                }

                normalButtons.forEach { data ->
                    TextButton(
                        isEditMode = false,
                        data = data,
                        allStyles = styles,
                        screenSize = screenSize,
                        isDark = isDark,
                        visible = layerVisibility && checkVisibility(isCursorGrabbing, data.visibilityType),
                        getOtherWidgets = { emptyList() }, //不需要计算吸附
                        snapThresholdValue = 4.dp,
                        eventHandler = eventHandler,
                        isPressed = data.isPressed
                    )
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

        layers.fastForEach { layer ->
            layer.textBoxes.value.fastForEach { it.putSize() }
            layer.normalButtons.value.fastForEach { it.putSize() }
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

            layers.fastForEach { layer ->
                layer.textBoxes.value.fastForEach { it.place() }
                layer.normalButtons.value.fastForEach { it.place() }
            }
        }
    }
}

private fun checkLayerVisibility(
    layer: ObservableControlLayer,
    hideLayerWhen: HideLayerWhen,
    isUsingJoystick: Boolean,
    isCursorGrabbing: Boolean,
    visibilityType: VisibilityType
): Boolean {
    if (layer.hide || !checkVisibility(isCursorGrabbing, visibilityType)) {
        return false
    }

    val hideConditionMet = when (hideLayerWhen) {
        HideLayerWhen.WhenMouse -> layer.hideWhenMouse
        HideLayerWhen.WhenGamepad -> layer.hideWhenGamepad
        HideLayerWhen.None -> false
    }

    return !(hideConditionMet || (isUsingJoystick && layer.hideWhenJoystick))
}

/**
 * 通过虚拟鼠标抓获情况，判断当前是否应当展示控件
 */
private fun checkVisibility(
    isCursorGrabbing: Boolean,
    visibilityType: VisibilityType
): Boolean {
    return when (visibilityType) {
        VisibilityType.ALWAYS -> true
        VisibilityType.IN_GAME -> isCursorGrabbing
        VisibilityType.IN_MENU -> !isCursorGrabbing
    }
}
