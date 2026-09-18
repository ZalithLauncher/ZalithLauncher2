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

package com.movtery.zalithlauncher.ui.control.mouse

import android.view.InputDevice
import android.view.MotionEvent
import android.view.View
import android.view.ViewTreeObserver
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.PointerId
import androidx.compose.ui.input.pointer.PointerType
import androidx.compose.ui.input.pointer.changedToDown
import androidx.compose.ui.input.pointer.changedToUpIgnoreConsumed
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.movtery.zalithlauncher.game.sdl.SdlBridge
import com.movtery.zalithlauncher.setting.enums.MouseControlMode
import com.movtery.zalithlauncher.ui.components.FocusableBox
import com.movtery.zalithlauncher.ui.control.input.TouchCharInput
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.libsdl.app.SDLActivity
import kotlin.time.Duration.Companion.milliseconds

/**
 * 拖动状态数据类
 */
private data class DragState(
    var isDragging: Boolean = false,
    var longPressTriggered: Boolean = false,
    val startPosition: Offset
)

/**
 * 判定双指同时按下的最大时间间隔（ms）
 */
private const val SCROLL_GESTURE_DOWN_WINDOW_MILLIS = 200L
/**
 * 双指滑动滚动时，滑动该距离对应滚动一次滚轮
 */
private val SCROLL_GESTURE_SCROLL_DISTANCE = 6.dp

/**
 * 原始触摸控制模拟层
 * @param controlMode               控制模式：SLIDE（滑动控制）、CLICK（点击控制）
 * @param enableMouseClick          是否开启虚拟鼠标点击操作（仅适用于滑动控制）
 * @param longPressTimeoutMillis    长按触发检测时长
 * @param requestPointerCapture     是否使用鼠标抓取方案
 * @param pointerIcon               实体指针图标
 * @param onTouch                   触摸到鼠标层
 * @param onMouse                   实体鼠标交互事件
 * @param onTap                     点击回调，参数是触摸点在控件内的绝对坐标
 * @param onLongPress               长按开始回调
 * @param onLongPressEnd            长按结束回调
 * @param onPointerMove             指针移动回调，参数在 SLIDE 模式下是指针位置，CLICK 模式下是手指当前位置
 * @param onMouseMove               实体鼠标指针移动回调
 * @param onMouseScroll             实体鼠标指针滚轮滑动
 * @param onMouseButton             实体鼠标指针按钮按下反馈
 * @param isMoveOnlyPointer         指针是否被父级标记为仅可滑动指针
 * @param onOccupiedPointer         占用指针回调
 * @param onReleasePointer          释放指针回调
 * @param enableScrollGesture       是否启用双指滑动滚动手势
 * @param onScrollGesture           双指滑动滚动回调，参数为滚轮滚动的偏移量
 * @param inputChange               重新启动内部的 pointerInput 块，让触摸逻辑能够实时拿到最新的外部参数
 */
@Composable
fun TouchpadLayout(
    modifier: Modifier = Modifier,
    controlMode: MouseControlMode = MouseControlMode.SLIDE,
    enableMouseClick: Boolean = true,
    longPressTimeoutMillis: Long = -1L,
    requestPointerCapture: Boolean = true,
    pointerIcon: PointerIcon = PointerIcon.Default,
    onTouch: () -> Unit = {},
    onMouse: () -> Unit = {},
    onTap: (Offset) -> Unit = {},
    onLongPress: () -> Unit = {},
    onLongPressEnd: () -> Unit = {},
    onPointerMove: (Offset, isMoveOnly: Boolean) -> Unit = { _, _ -> },
    onMouseMove: (Offset) -> Unit = {},
    onMouseScroll: (Offset) -> Unit = {},
    onMouseButton: (button: Int, pressed: Boolean) -> Unit = { _, _ -> },
    isMoveOnlyPointer: (PointerId) -> Boolean = { false },
    onOccupiedPointer: (PointerId) -> Unit = {},
    onReleasePointer: (PointerId) -> Unit = {},
    enableScrollGesture: Boolean = false,
    onScrollGesture: (Offset) -> Unit = {},
    inputChange: Array<out Any> = arrayOf(Unit),
    requestFocusKey: Any? = null
) {
    val interactionSource = remember { MutableInteractionSource() }

    val composeFocusCount by SdlBridge.composeFocus.collectAsStateWithLifecycle()

    //确保 pointerInput 中总是调用到最新的回调，避免闭包捕获旧值
    val currentOnTouch by rememberUpdatedState(onTouch)
    val currentControlMode by rememberUpdatedState(controlMode)
    val currentEnableMouseClick by rememberUpdatedState(enableMouseClick)
    val currentLongPressTimeoutMillis by rememberUpdatedState(longPressTimeoutMillis)
    val currentOnTap by rememberUpdatedState(onTap)
    val currentOnLongPress by rememberUpdatedState(onLongPress)
    val currentOnLongPressEnd by rememberUpdatedState(onLongPressEnd)
    val currentOnPointerMove by rememberUpdatedState(onPointerMove)
    val currentEnableScrollGesture by rememberUpdatedState(enableScrollGesture)
    val currentOnScrollGesture by rememberUpdatedState(onScrollGesture)
    val currentScrollGestureDistancePx by rememberUpdatedState(
        with(LocalDensity.current) { SCROLL_GESTURE_SCROLL_DISTANCE.toPx() }
    )

    FocusableBox(
        modifier = modifier
            .hoverable(interactionSource)
            .pointerHoverIcon(pointerIcon)
            .pointerInput(*inputChange) {
                coroutineScope {
                    /** 所有被占用的指针 */
                    val occupiedPointers = mutableSetOf<PointerId>()

                    /** 当前正在被处理的指针 */
                    var activePointer: PointerId? = null
                    val longPressJobs = mutableMapOf<PointerId, Job>()

                    /** 每个指针的拖动状态 */
                    val dragStates = mutableMapOf<PointerId, DragState>()

                    /** moveOnly 指针集合，用于处理滑动事件 */
                    val moveOnlyPointers = mutableSetOf<PointerId>()
                    /** 双指滑动滚动手势占用的指针 */
                    val scrollGesturePointers = mutableSetOf<PointerId>()
                    /** 双指滑动滚动手势是否正在进行 */
                    var scrollGestureActive = false
                    /** 双指滑动滚动手势中，未满一次滚动的位移余量 */
                    var scrollGestureRemainder = Offset.Zero
                    /** 活跃指针的按下时间，用于判定双指是否几乎同时按下 */
                    var activePointerDownTime = 0L

                    /** 清除鼠标触摸层的状态 */
                    fun resetTouchState() {
                        activePointer = null
                        dragStates.clear()
                        longPressJobs.values.forEach { it.cancel() }
                        longPressJobs.clear()
                        occupiedPointers.forEach { onReleasePointer(it) }
                        occupiedPointers.clear()
                        moveOnlyPointers.clear()
                        scrollGesturePointers.clear()
                        scrollGestureActive = false
                        scrollGestureRemainder = Offset.Zero
                    }

                    awaitPointerEventScope {
                        try {
                            while (true) {
                                val event = awaitPointerEvent()
                                event.changes
                                    .filter { it.changedToDown() }
                                    .forEach { change ->
                                        if (change.type != PointerType.Touch) {
                                            return@forEach
                                        }

                                        //刚触摸到屏幕时，触发触摸事件回调
                                        currentOnTouch()

                                        val pointerId = change.id
                                        //是否被父级标记为仅处理滑动
                                        val isMoveOnly = isMoveOnlyPointer(pointerId)

                                        //如果是 moveOnly 指针
                                        if (isMoveOnly) {
                                            //如果当前没有活跃指针，则成为活跃指针
                                            //这样当第一根手指被标记为 moveOnly 后，第二根手指可以接管
                                            if (activePointer == null) {
                                                activePointer = pointerId
                                                dragStates[pointerId] = DragState(startPosition = change.position)
                                            } else {
                                                //如果已有活跃指针，仅处理滑动
                                                moveOnlyPointers.add(pointerId)
                                            }
                                        } else if (activePointer == null && !change.isConsumed) {
                                            //如果没有活跃指针，且当前指针未被消费，则开始处理这个指针
                                            //fix: 只有真正成为 activePointer 的指针，才标记为已占用
                                            if (pointerId !in occupiedPointers) {
                                                onOccupiedPointer(pointerId)
                                                occupiedPointers.add(pointerId)
                                            }

                                            activePointer = pointerId
                                            activePointerDownTime = change.uptimeMillis

                                            dragStates[pointerId] =
                                                DragState(startPosition = change.position)

                                            if (currentControlMode == MouseControlMode.SLIDE && currentEnableMouseClick) {
                                                longPressJobs[pointerId] = launch {
                                                    //只在滑动点击模式下进行长按计时
                                                    val timeout =
                                                        if (currentLongPressTimeoutMillis > 0) {
                                                            currentLongPressTimeoutMillis
                                                        } else {
                                                            viewConfiguration.longPressTimeoutMillis
                                                        }
                                                    delay(timeout.milliseconds)

                                                    //检查是否仍在处理此指针且未开始拖动
                                                    if (activePointer == pointerId && dragStates[pointerId]?.isDragging != true) {
                                                        dragStates[pointerId]?.longPressTriggered = true
                                                        currentOnLongPress()
                                                    }
                                                }
                                            }

                                            if (currentControlMode == MouseControlMode.CLICK) {
                                                //点击模式下，如果触摸，无论如何都应该更新指针位置
                                                currentOnPointerMove(change.position, false)
                                            }
                                        } else if (currentEnableScrollGesture && scrollGesturePointers.isEmpty() && !change.isConsumed) {
                                            //尝试与已按下的指针组成双指滑动滚动手势
                                            //两根指针都不能来源于控制布局，需几乎同时按下，且第一根指针尚未开始拖动虚拟鼠标
                                            activePointer?.takeIf { it != pointerId }?.let { first ->
                                                if (!isMoveOnlyPointer(first)
                                                    && change.uptimeMillis - activePointerDownTime <= SCROLL_GESTURE_DOWN_WINDOW_MILLIS
                                                    && dragStates[first]?.let { !it.isDragging && !it.longPressTriggered } == true
                                                ) {
                                                    longPressJobs.remove(first)?.cancel()
                                                    //标记第一根指针为拖动状态，避免手势结束后误触点击
                                                    dragStates[first]?.isDragging = true
                                                    //第二根指针同样注册为占用，避免被控制布局抢占
                                                    if (pointerId !in occupiedPointers) {
                                                        onOccupiedPointer(pointerId)
                                                        occupiedPointers.add(pointerId)
                                                    }
                                                    scrollGesturePointers.add(first)
                                                    scrollGesturePointers.add(pointerId)
                                                    scrollGestureActive = true
                                                    scrollGestureRemainder = Offset.Zero
                                                }
                                            }
                                        }
                                    }

                                //处理双指滑动滚动手势的移动，取双指位移的平均值，
                                //滑动距离累计满一次阈值时，发送一次滚轮滚动事件
                                if (scrollGestureActive) {
                                    var deltaSum = Offset.Zero
                                    event.changes
                                        .filter { it.id in scrollGesturePointers && it.positionChanged() && !it.isConsumed }
                                        .forEach { change ->
                                            deltaSum += change.positionChange()
                                            change.consume()
                                        }

                                    if (deltaSum != Offset.Zero) {
                                        val scaled = deltaSum / scrollGesturePointers.size.coerceAtLeast(1).toFloat()
                                        val hScroll = scaled.x / currentScrollGestureDistancePx + scrollGestureRemainder.x
                                        val vScroll = scaled.y / currentScrollGestureDistancePx + scrollGestureRemainder.y
                                        val hRound = hScroll.toInt()
                                        val vRound = vScroll.toInt()

                                        if (hRound != 0 || vRound != 0) {
                                            currentOnScrollGesture(Offset(hScroll, vScroll))
                                        }
                                        scrollGestureRemainder = Offset(
                                            x = hScroll - hRound,
                                            y = vScroll - vRound
                                        )
                                    }
                                }

                                //处理移动事件，处理活跃指针的移动
                                activePointer?.takeIf { it !in scrollGesturePointers }?.let { pointerId ->
                                    event.changes
                                        .firstOrNull { it.id == pointerId && it.positionChanged() && !it.isConsumed }
                                        ?.let { moveChange ->
                                            val dragState = dragStates[pointerId] ?: return@let
                                            //是否被父级标记为仅处理滑动
                                            val isMoveOnly = isMoveOnlyPointer(pointerId)

                                            if (isMoveOnly) {
                                                dragState.isDragging = true
                                                val delta = moveChange.positionChange()
                                                currentOnPointerMove(delta, true)
                                            } else {
                                                when (currentControlMode) {
                                                    MouseControlMode.SLIDE -> {
                                                        if (currentEnableMouseClick) {
                                                            val distanceFromStart =
                                                                (moveChange.position - dragState.startPosition).getDistance()

                                                            if (distanceFromStart > viewConfiguration.touchSlop && !dragState.isDragging) {
                                                                //超出了滑动检测距离，说明是真的在进行滑动
                                                                dragState.isDragging = true
                                                                longPressJobs.remove(pointerId)
                                                                    ?.cancel() //取消长按计时
                                                            }

                                                            if (dragState.isDragging || dragState.longPressTriggered) {
                                                                val delta = moveChange.positionChange()
                                                                currentOnPointerMove(delta, false)
                                                            }
                                                        } else {
                                                            dragState.isDragging = true
                                                            val delta = moveChange.positionChange()
                                                            currentOnPointerMove(delta, false)
                                                        }
                                                    }

                                                    MouseControlMode.CLICK -> {
                                                        if (!dragState.longPressTriggered) {
                                                            dragState.longPressTriggered = true
                                                            longPressJobs.remove(pointerId)?.cancel()
                                                            currentOnLongPress()
                                                        }
                                                        currentOnPointerMove(moveChange.position, false)
                                                    }
                                                }
                                            }

                                            moveChange.consume()
                                        }
                                }

                                //处理 moveOnly 指针的移动
                                event.changes
                                    .filter { moveOnlyPointers.contains(it.id) && it.positionChanged() && !it.isConsumed }
                                    .forEach { moveChange ->
                                        val pointerId = moveChange.id
                                        val dragState = dragStates[pointerId]
                                        if (dragState != null) {
                                            dragState.isDragging = true
                                            val delta = moveChange.positionChange()
                                            currentOnPointerMove(delta, true)
                                            moveChange.consume()
                                        }
                                    }

                                //释放
                                event.changes
                                    .filter { it.changedToUpIgnoreConsumed() }
                                    .forEach { change ->
                                        val pointerId = change.id
                                        //是否被父级标记为仅处理滑动
                                        val isMoveOnly = isMoveOnlyPointer(pointerId)

                                        longPressJobs.remove(pointerId)?.cancel()
                                        val dragState = dragStates.remove(pointerId)

                                        if (pointerId in scrollGesturePointers) {
                                            //滚动手势的指针抬起，直接结束手势，不触发点击
                                            scrollGesturePointers.remove(pointerId)
                                            scrollGestureActive = false
                                            if (pointerId == activePointer) {
                                                activePointer = null
                                            }
                                        } else if (pointerId == activePointer) {
                                            if (!isMoveOnly) {
                                                if (dragState?.longPressTriggered == true) {
                                                    currentOnLongPressEnd()
                                                } else {
                                                    when (currentControlMode) {
                                                        MouseControlMode.SLIDE -> {
                                                            if (currentEnableMouseClick && dragState?.isDragging != true) {
                                                                currentOnTap(change.position)
                                                            }
                                                        }

                                                        MouseControlMode.CLICK -> {
                                                            //未进入长按，算一次点击事件
                                                            currentOnTap(change.position)
                                                        }
                                                    }
                                                }
                                            }

                                            activePointer = null
                                        }

                                        //从 moveOnly 指针集合中移除
                                        moveOnlyPointers.remove(pointerId)

                                        if (!isMoveOnly && pointerId in occupiedPointers) {
                                            occupiedPointers.remove(pointerId)
                                            onReleasePointer(pointerId)
                                        }
                                    }

                                if (!event.changes.any { it.pressed }) {
                                    resetTouchState()
                                }
                            }
                        } finally {
                            resetTouchState()
                        }
                    }
                }
            }
            .then(
                Modifier.mouseEventModifier(
                    disabled = requestPointerCapture,
                    inputChange = inputChange,
                    onMouse = onMouse,
                    onMouseMove = onMouseMove,
                    onMouseScroll = onMouseScroll,
                    onMouseButton = onMouseButton
                )
            ),
        requestKey = requestFocusKey to composeFocusCount,
        canRequestFocus = { !SDLActivity.isUsingSDLTextEdit() && !TouchCharInput.isActive() }
    )

    SimpleMouseCapture(
        enabled = requestPointerCapture,
        onMouse = onMouse,
        onMouseMove = onMouseMove,
        onMouseScroll = onMouseScroll,
        onMouseButton = onMouseButton
    )
}

/**
 * 简单的实体鼠标捕获层
 * @param enabled                   是否使用鼠标抓取方案
 * @param onMouse                   实体鼠标开始响应事件的回调
 * @param onMouseMove               实体鼠标指针移动回调
 * @param onMouseScroll             实体鼠标指针滚轮滑动
 * @param onMouseButton             实体鼠标指针按钮按下反馈
 */
@Composable
private fun SimpleMouseCapture(
    enabled: Boolean,
    onMouse: () -> Unit,
    onMouseMove: (Offset) -> Unit,
    onMouseScroll: (Offset) -> Unit,
    onMouseButton: (button: Int, pressed: Boolean) -> Unit
) {
    val view by rememberUpdatedState(LocalView.current)
    val currentOnMouse by rememberUpdatedState(onMouse)
    val currentOnMouseMove by rememberUpdatedState(onMouseMove)
    val currentOnMouseScroll by rememberUpdatedState(onMouseScroll)
    val currentOnMouseButton by rememberUpdatedState(onMouseButton)

    fun syncCaptureState() {
        if (enabled) {
            if (view.hasWindowFocus()) {
                view.requestPointerCapture()
            }
        } else {
            view.releasePointerCapture()
        }
    }

    val composeFocus by SdlBridge.composeFocus.collectAsStateWithLifecycle()
    LaunchedEffect(composeFocus) {
        syncCaptureState()
    }

    DisposableEffect(view, enabled) {
        view.setOnCapturedPointerListener(null)

        val focusListener = ViewTreeObserver.OnWindowFocusChangeListener { hasFocus ->
            if (enabled && hasFocus) {
                view.requestPointerCapture()
            }
        }
        view.viewTreeObserver.addOnWindowFocusChangeListener(focusListener)

        syncCaptureState()

        if (enabled) {
            val pointerListener = View.OnCapturedPointerListener { _, event ->
                when (event.actionMasked) {
                    MotionEvent.ACTION_HOVER_MOVE, MotionEvent.ACTION_MOVE -> {
                        var deltaX = 0f
                        var deltaY = 0f

                        val relX = event.getAxisValue(MotionEvent.AXIS_RELATIVE_X)
                        val relY = event.getAxisValue(MotionEvent.AXIS_RELATIVE_Y)
                        deltaX += if (relX != 0f) relX else event.x
                        deltaY += if (relY != 0f) relY else event.y

                        val historySize = event.historySize
                        for (i in 0 until historySize) {
                            deltaX += event.getHistoricalAxisValue(MotionEvent.AXIS_RELATIVE_X, i)
                            deltaY += event.getHistoricalAxisValue(MotionEvent.AXIS_RELATIVE_Y, i)
                        }

                        currentOnMouse()
                        currentOnMouseMove(Offset(deltaX, deltaY))
                        true
                    }
                    MotionEvent.ACTION_SCROLL -> {
                        currentOnMouseScroll(
                            Offset(
                                event.getAxisValue(MotionEvent.AXIS_HSCROLL),
                                event.getAxisValue(MotionEvent.AXIS_VSCROLL)
                            )
                        )
                        true
                    }
                    MotionEvent.ACTION_DOWN, MotionEvent.ACTION_BUTTON_PRESS -> {
                        currentOnMouseButton(event.actionButton, true)
                        true
                    }
                    MotionEvent.ACTION_UP, MotionEvent.ACTION_BUTTON_RELEASE -> {
                        currentOnMouseButton(event.actionButton, false)
                        true
                    }
                    else -> false
                }
            }

            view.setOnCapturedPointerListener(pointerListener)
        } else {
            view.setOnCapturedPointerListener(null)
        }

        onDispose {
            view.viewTreeObserver.removeOnWindowFocusChangeListener(focusListener)
            view.setOnCapturedPointerListener(null)
        }
    }
}

/**
 * 实体鼠标指针事件监听
 * @param disabled 是否禁用
 */
private fun Modifier.mouseEventModifier(
    disabled: Boolean,
    inputChange: Array<out Any> = arrayOf(Unit),
    onMouse: () -> Unit = {},
    onMouseMove: (Offset) -> Unit = {},
    onMouseScroll: (Offset) -> Unit = {},
    onMouseButton: (Int, Boolean) -> Unit = { _, _ -> },
) = composed(
    inspectorInfo = {
        name = "mouseEventModifier"
        properties["keys"] = inputChange
    }
) {
    val currentDisabled by rememberUpdatedState(disabled)
    val currentOnMouse by rememberUpdatedState(onMouse)
    val currentOnMouseMove by rememberUpdatedState(onMouseMove)
    val currentOnMouseScroll by rememberUpdatedState(onMouseScroll)
    val currentOnMouseButton by rememberUpdatedState(onMouseButton)

    var lastButtons by remember(*inputChange) {
        //位掩码存储鼠标按键按下状态
        mutableIntStateOf(0)
    }

    pointerInteropFilter { event ->
        if (currentDisabled) {
            return@pointerInteropFilter false
        }

        val isMouse = event.isFromSource(InputDevice.SOURCE_MOUSE)
        val isStylus = event.isFromSource(InputDevice.SOURCE_STYLUS)
        //过滤掉不是鼠标或者触控笔的类型
        //触控笔（Chromebook、三星等）
        if (!isMouse && !isStylus) {
            return@pointerInteropFilter false
        }

        currentOnMouse()

        val buttons = event.buttonState
        val changed = lastButtons xor buttons

        fun dispatchButton(button: Int) {
            if (changed and button != 0) {
                val pressed = buttons and button != 0
                currentOnMouseButton(button, pressed)
            }
        }

        dispatchButton(MotionEvent.BUTTON_PRIMARY)
        dispatchButton(MotionEvent.BUTTON_SECONDARY)
        dispatchButton(MotionEvent.BUTTON_TERTIARY)
        dispatchButton(MotionEvent.BUTTON_BACK)
        dispatchButton(MotionEvent.BUTTON_FORWARD)
        dispatchButton(MotionEvent.BUTTON_STYLUS_SECONDARY)

        lastButtons = buttons

        when (event.actionMasked) {
            MotionEvent.ACTION_HOVER_MOVE,
            MotionEvent.ACTION_MOVE -> {
                currentOnMouseMove(
                    Offset(x = event.x, y = event.y)
                )
            }

            //检查并处理触控笔按下
            MotionEvent.ACTION_DOWN,
            MotionEvent.ACTION_POINTER_DOWN -> {
                if (isStylus) {
                    currentOnMouseButton(MotionEvent.BUTTON_PRIMARY, true)
                }
            }
            //检查并处理触控笔抬起
            MotionEvent.ACTION_UP,
            MotionEvent.ACTION_POINTER_UP -> {
                if (isStylus) {
                    currentOnMouseButton(MotionEvent.BUTTON_PRIMARY, false)
                }
            }

            MotionEvent.ACTION_SCROLL -> {
                currentOnMouseScroll(
                    Offset(
                        x = event.getAxisValue(MotionEvent.AXIS_HSCROLL),
                        y = event.getAxisValue(MotionEvent.AXIS_VSCROLL),
                    )
                )
            }
        }

        true
    }
}