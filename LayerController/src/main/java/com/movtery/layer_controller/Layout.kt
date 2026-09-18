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
import com.movtery.layer_controller.layout.JoystickWidgetRenderer
import com.movtery.layer_controller.layout.TextButton
import com.movtery.layer_controller.observable.ObservableButtonStyle
import com.movtery.layer_controller.observable.ObservableControlLayer
import com.movtery.layer_controller.observable.ObservableControlLayout
import com.movtery.layer_controller.observable.ObservableJoystickStyle
import com.movtery.layer_controller.observable.ObservableWidget
import com.movtery.layer_controller.observable.PointerEventBus
import com.movtery.layer_controller.observable.TouchProcessor
import com.movtery.layer_controller.utils.getWidgetPosition

/**
 * 控制布局画布
 * @param observedLayout 需要监听并绘制的控制布局
 * @param eventHandler 处理控制布局事件用到的处理器
 * @param checkOccupiedPointers 检查已占用的指针
 * @param opacity 控制布局画布整体不透明度 0f~1f
 * @param markPointerAsMoveOnly 标记指针为仅接受滑动处理
 * @param hideLayerWhen 根据情况决定是否隐藏指定控件层
 */
@Composable
fun ControlBoxLayout(
    modifier: Modifier = Modifier,
    observedLayout: ObservableControlLayout? = null,
    eventHandler: EventHandler = EventHandler(),
    isCursorGrabbing: Boolean,
    checkOccupiedPointers: (PointerId) -> Boolean,
    @FloatRange(0.0, 1.0) opacity: Float = 1f,
    markPointerAsMoveOnly: (PointerId) -> Unit = {},
    onOccupiedPointer: (PointerId) -> Unit = {},
    onReleasePointer: (PointerId) -> Unit = {},
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
                            onOccupiedPointer = onOccupiedPointer,
                            onReleasePointer = onReleasePointer,
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
    onOccupiedPointer: (PointerId) -> Unit,
    onReleasePointer: (PointerId) -> Unit,
    isCursorGrabbing: Boolean,
    hideLayerWhen: HideLayerWhen,
    isDark: Boolean,
    content: @Composable BoxScope.() -> Unit
) {
    val layers by observedLayout.layers.collectAsStateWithLifecycle()
    val reversedLayers = remember(layers) { layers.reversed() }
    val styles by observedLayout.styles.collectAsStateWithLifecycle()
    val joystickStyles by observedLayout.joystickStyles.collectAsStateWithLifecycle()

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

    // 共享的多指针状态管理器
    val pointerEventBus = remember { PointerEventBus() }
    pointerEventBus.checkOccupiedPointers = currentCheckOccupiedPointers
    pointerEventBus.markPointerAsMoveOnly = markPointerAsMoveOnly

    val touchProcessor = remember(screenSize) {
        TouchProcessor(eventHandler) { widget ->
            getWidgetPosition(widget, widget.internalRenderSize, screenSize)
        }
    }

    val currentTouchProcessor by rememberUpdatedState(touchProcessor)
    val currentMarkPointerAsMoveOnly by rememberUpdatedState(markPointerAsMoveOnly)

    Box(
        modifier = modifier
            .pointerInput(reversedLayers, hideLayerWhen) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent(pass = PointerEventPass.Initial)

                        event.changes.forEach { change ->
                            val pointerId = change.id
                            //手指抬起，清理该指针所有状态
                            if (!change.pressed) {
                                pointerEventBus.endPointer(pointerId).forEach { widget ->
                                    //释放该指针事件
                                    widget.onReleaseEvent(eventHandler, reversedLayers)
                                }
                                return@forEach
                            }

                            if (change.isConsumed || currentCheckOccupiedPointers(pointerId)) {
                                return@forEach //跳过已消费或被占用的指针
                            }

                            //收集可见控件
                            val visibleWidgets = collectVisibleWidgets(
                                layers = layers,
                                hideLayerWhen = currentHideLayerWhen,
                                isCursorGrabbing = currentIsCursorGrabbing,
                            )

                            currentTouchProcessor.processFrame(
                                session = pointerEventBus,
                                change = change,
                                visibleWidgets = visibleWidgets,
                                allLayers = reversedLayers,
                                consumeEvent = { it.consume() },
                                markPointerAsMoveOnly = currentMarkPointerAsMoveOnly,
                            )
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
            joystickStyles = joystickStyles,
            screenSize = screenSize,
            pointerEventBus = pointerEventBus,
            eventHandler = eventHandler,
            isCursorGrabbing = currentIsCursorGrabbing,
            hideLayerWhen = currentHideLayerWhen,
            onOccupiedPointer = onOccupiedPointer,
            onReleasePointer = onReleasePointer
        )
    }
}

@Composable
private fun ControlsRendererLayer(
    isDark: Boolean,
    @FloatRange(0.0, 1.0) opacity: Float,
    layers: List<ObservableControlLayer>,
    styles: List<ObservableButtonStyle>,
    joystickStyles: List<ObservableJoystickStyle>,
    screenSize: IntSize,
    pointerEventBus: PointerEventBus,
    eventHandler: EventHandler,
    isCursorGrabbing: Boolean,
    hideLayerWhen: HideLayerWhen,
    onOccupiedPointer: (PointerId) -> Unit,
    onReleasePointer: (PointerId) -> Unit
) {
    Layout(
        modifier = Modifier.alpha(alpha = opacity),
        content = {
            //按图层顺序渲染所有可见的控件
            layers.forEach { layer ->
                val layerVisibility = checkLayerVisibility(
                    layer = layer,
                    hideLayerWhen = hideLayerWhen,
                    isCursorGrabbing = isCursorGrabbing,
                    visibilityType = layer.visibilityType
                )
                val normalButtons by layer.normalButtons.collectAsStateWithLifecycle()
                val textBoxes by layer.textBoxes.collectAsStateWithLifecycle()
                val joystickButtons by layer.joystickButtons.collectAsStateWithLifecycle()

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

                joystickButtons.forEach { data ->
                    JoystickWidgetRenderer(
                        data = data,
                        joystickStyles = joystickStyles,
                        screenSize = screenSize,
                        isDark = isDark,
                        visible = layerVisibility && checkVisibility(isCursorGrabbing, data.visibilityType),
                        pointerEventBus = pointerEventBus,
                        eventHandler = eventHandler,
                        reversedLayers = layers,
                        onOccupiedPointer = onOccupiedPointer,
                        onReleasePointer = onReleasePointer
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
            layer.joystickButtons.value.fastForEach { it.putSize() }
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
                layer.joystickButtons.value.fastForEach { it.place() }
            }
        }
    }
}

/**
 * 收集所有可见控件层中的可触控控件
 */
private fun collectVisibleWidgets(
    layers: List<ObservableControlLayer>,
    hideLayerWhen: HideLayerWhen,
    isCursorGrabbing: Boolean,
): List<ObservableWidget> {
    return layers
        .filter { layer ->
            checkLayerVisibility(
                layer = layer,
                hideLayerWhen = hideLayerWhen,
                isCursorGrabbing = isCursorGrabbing,
                visibilityType = layer.visibilityType,
            )
        }
        .flatMap { layer ->
            //反转，从顶到底
            layer.normalButtons.value.reversed()
        }
        .filter { widget ->
            widget.canTouch() && checkVisibility(
                isCursorGrabbing = isCursorGrabbing,
                visibilityType = widget.onCheckVisibilityType()
            )
        }
}

private fun checkLayerVisibility(
    layer: ObservableControlLayer,
    hideLayerWhen: HideLayerWhen,
    isCursorGrabbing: Boolean,
    visibilityType: VisibilityType
): Boolean {
    if (layer.hide || !checkVisibility(isCursorGrabbing, visibilityType)) {
        return false
    }

    return !when (hideLayerWhen) {
        HideLayerWhen.WhenMouse -> layer.hideWhenMouse
        HideLayerWhen.WhenGamepad -> layer.hideWhenGamepad
        HideLayerWhen.None -> false
    }
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
