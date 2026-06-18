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
import androidx.compose.foundation.layout.size
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.movtery.layer_controller.data.HideLayerWhen
import com.movtery.layer_controller.data.VisibilityType
import com.movtery.layer_controller.event.EventHandler
import com.movtery.layer_controller.layout.StyleableJoystick
import com.movtery.layer_controller.layout.TextButton
import com.movtery.layer_controller.observable.ObservableButtonStyle
import com.movtery.layer_controller.observable.ObservableControlLayer
import com.movtery.layer_controller.observable.ObservableControlLayout
import com.movtery.layer_controller.observable.ObservableWidget
import com.movtery.layer_controller.observable.TouchProcessor
import com.movtery.layer_controller.observable.TouchSession
import com.movtery.layer_controller.utils.buttonSize
import com.movtery.layer_controller.utils.getWidgetPosition
import androidx.compose.runtime.collectAsState

/**
 * 控制布局画布
 * @param observedLayout 需要监听并绘制的控制布局
 * @param eventHandler 处理控制布局事件用到的处理器
 * @param checkOccupiedPointers 检查已占用的指针，防止底层正在被使用的指针仍被控制布局画布处理
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
    enableJoystick: Boolean = true,
    isEditMode: Boolean = false,
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
                            isCursorGrabbing = isCursorGrabbing,
                            enableJoystick = enableJoystick,
                            isEditMode = isEditMode,
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
    isCursorGrabbing: Boolean,
    enableJoystick: Boolean,
    isEditMode: Boolean,
    hideLayerWhen: HideLayerWhen,
    isDark: Boolean,
    content: @Composable BoxScope.() -> Unit
) {
    val layers by observedLayout.layers.collectAsStateWithLifecycle()
    /** 渲染层级：底部最先渲染，顶部最后渲染 */
    val renderingOrderLayers = remember(layers) { layers.reversed() }

    val styles by observedLayout.styles.collectAsStateWithLifecycle()

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

    //触控管线
    val touchSession = remember { TouchSession() }
    val touchProcessor = remember(screenSize) {
        TouchProcessor(eventHandler) { widget ->
            getWidgetPosition(widget, widget.internalRenderSize, screenSize)
        }
    }

    Box(
        modifier = modifier
            .pointerInput(layers, hideLayerWhen, enableJoystick, isEditMode) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent(pass = PointerEventPass.Initial)

                        event.changes.forEach { change ->
                            val pointerId = change.id
                            //手指抬起，清理该指针所有状态
                            if (!change.pressed) {
                                touchSession.endPointer(pointerId).forEach { widget ->
                                    //释放该指针事件
                                    widget.onReleaseEvent(eventHandler, layers)
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
                                enableJoystick = enableJoystick,
                                isEditMode = isEditMode
                            )

                            touchProcessor.processFrame(
                                session = touchSession,
                                change = change,
                                visibleWidgets = visibleWidgets,
                                allLayers = layers,
                                consumeEvent = { it.consume() },
                                markPointerAsMoveOnly = markPointerAsMoveOnly,
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
            layers = renderingOrderLayers,
            styles = styles,
            screenSize = screenSize,
            eventHandler = eventHandler,
            isCursorGrabbing = currentIsCursorGrabbing,
            enableJoystick = enableJoystick,
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
    isCursorGrabbing: Boolean,
    enableJoystick: Boolean,
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
                    isCursorGrabbing = isCursorGrabbing,
                    visibilityType = layer.visibilityType
                )

                // 注意：这里必须和 layout 块中的顺序完全一致
                layer.textBoxes.collectAsState().value.forEach { data ->
                    val isVisible = layerVisibility && checkVisibility(
                        isCursorGrabbing,
                        data.onCheckVisibilityType()
                    )
                    TextButton(
                        isEditMode = false,
                        data = data,
                        allStyles = styles,
                        screenSize = screenSize,
                        isDark = isDark,
                        visible = isVisible,
                        getOtherWidgets = { emptyList() },
                        snapThresholdValue = 4.dp,
                        eventHandler = eventHandler,
                        isPressed = false
                    )
                }

                layer.normalButtons.collectAsState().value.forEach { data ->
                    val isVisible = layerVisibility && checkVisibility(
                        isCursorGrabbing,
                        data.onCheckVisibilityType()
                    )
                    val isPressed = data.isPressed
                    TextButton(
                        isEditMode = false,
                        data = data,
                        allStyles = styles,
                        screenSize = screenSize,
                        isDark = isDark,
                        visible = isVisible,
                        getOtherWidgets = { emptyList() },
                        snapThresholdValue = 4.dp,
                        eventHandler = eventHandler,
                        isPressed = isPressed
                    )
                }

                if (enableJoystick) {
                    layer.joysticks.collectAsState().value.forEach { data ->
                        val isVisible = layerVisibility && checkVisibility(
                            isCursorGrabbing,
                            data.onCheckVisibilityType()
                        )

                        key(data.uuid) {
                            LaunchedEffect(isVisible) {
                                if (!isVisible) data.resetState()
                            }

                            if (isVisible) {
                                StyleableJoystick(
                                    modifier = Modifier.buttonSize(data, screenSize),
                                    isDarkTheme = isDark,
                                    style = data.style,
                                    deadZoneRatio = data.deadZoneRatio,
                                    lockThreshold = data.lockThreshold,
                                    canLock = data.canLock,
                                    enablePointerInput = false,
                                    externalOffset = data.joystickOffset,
                                    externalIsLocked = data.isLockedState,
                                    externalCanLockInternal = data.canLockState
                                )
                            } else {
                                // 占位，否则索引会乱
                                Box(Modifier.size(0.dp))
                            }
                        }
                    }
                }
            }
        }
    ) { measurables, constraints ->
        val placeables = measurables.map { measurable ->
            measurable.measure(constraints)
        }

        var placeableIndex = 0

        layout(constraints.maxWidth, constraints.maxHeight) {
            layers.forEach { layer ->
                val layerVisibility = checkLayerVisibility(
                    layer = layer,
                    hideLayerWhen = hideLayerWhen,
                    isCursorGrabbing = isCursorGrabbing,
                    visibilityType = layer.visibilityType
                )

                // 摆放逻辑必须遍历所有列表（即使不可见也要消耗掉 placeableIndex）
                layer.textBoxes.value.forEach { widget ->
                    if (placeableIndex < placeables.size) {
                        val placeable = placeables[placeableIndex++]
                        val isVisible = layerVisibility && checkVisibility(
                            isCursorGrabbing,
                            widget.onCheckVisibilityType()
                        )
                        if (isVisible) {
                            val size = IntSize(placeable.width, placeable.height)
                            val position = getWidgetPosition(widget, size, screenSize)
                            // 更新位置供触控检测
                            widget.internalRenderSize = size
                            widget.internalRenderOffsetPx = position
                            placeable.place(position.x.toInt(), position.y.toInt())
                        }
                    }
                }

                layer.normalButtons.value.forEach { widget ->
                    if (placeableIndex < placeables.size) {
                        val placeable = placeables[placeableIndex++]
                        val isVisible = layerVisibility && checkVisibility(
                            isCursorGrabbing,
                            widget.onCheckVisibilityType()
                        )
                        if (isVisible) {
                            val size = IntSize(placeable.width, placeable.height)
                            val position = getWidgetPosition(widget, size, screenSize)
                            widget.internalRenderSize = size
                            widget.internalRenderOffsetPx = position
                            placeable.place(position.x.toInt(), position.y.toInt())
                        }
                    }
                }

                if (enableJoystick) {
                    layer.joysticks.value.forEach { widget ->
                        if (placeableIndex < placeables.size) {
                            val placeable = placeables[placeableIndex++]
                            val isVisible = layerVisibility && checkVisibility(
                                isCursorGrabbing,
                                widget.onCheckVisibilityType()
                            )
                            if (isVisible) {
                                val size = IntSize(placeable.width, placeable.height)
                                val position = getWidgetPosition(widget, size, screenSize)
                                widget.internalRenderSize = size
                                widget.internalRenderOffsetPx = position
                                placeable.place(position.x.toInt(), position.y.toInt())
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * 收集所有可见控件层中的可触控控件
 * 只做图层可见性、控件可见类型检查，命中和深度检测由 [TouchProcessor] 提供
 */
private fun collectVisibleWidgets(
    layers: List<ObservableControlLayer>,
    hideLayerWhen: HideLayerWhen,
    isCursorGrabbing: Boolean,
    enableJoystick: Boolean,
    isEditMode: Boolean
): List<ObservableWidget> {
    val list = mutableListOf<ObservableWidget>()

    // 从顶层到底层收集
    layers
        .filter { layer ->
            checkLayerVisibility(
                layer = layer,
                hideLayerWhen = hideLayerWhen,
                isCursorGrabbing = isCursorGrabbing,
                visibilityType = layer.visibilityType,
            )
        }
        .forEach { layer ->
            // 在同一层内，优先级：摇杆 > 普通按钮 > 文本框

            if (enableJoystick && !isEditMode) {
                list.addAll(layer.joysticks.value.reversed())
            }

            list.addAll(layer.normalButtons.value.reversed())
        }

    return list.filter { widget ->
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

    val hideConditionMet = when (hideLayerWhen) {
        HideLayerWhen.WhenMouse -> layer.hideWhenMouse
        HideLayerWhen.WhenGamepad -> layer.hideWhenGamepad
        HideLayerWhen.None -> false
    }

    return !hideConditionMet
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
