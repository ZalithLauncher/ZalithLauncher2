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

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerId
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.movtery.zalithlauncher.bridge.CURSOR_DISABLED
import com.movtery.zalithlauncher.bridge.CURSOR_ENABLED
import com.movtery.zalithlauncher.bridge.ZLBridgeStates
import com.movtery.zalithlauncher.setting.AllSettings
import com.movtery.zalithlauncher.setting.enums.MouseControlMode
import com.movtery.zalithlauncher.ui.control.gamepad.GamepadStickCameraListener
import com.movtery.zalithlauncher.utils.device.PhysicalMouseChecker
import com.movtery.zalithlauncher.utils.file.ifExists
import com.movtery.zalithlauncher.viewmodel.GamepadViewModel

/**
 * 鼠标指针抓取模式
 */
typealias CursorMode = Int

/**
 * 可根据指针抓取模式，自切换的虚拟指针模拟层
 * @param cursorMode                当前指针抓取模式
 * @param controlMode               控制模式：SLIDE（滑动控制）、CLICK（点击控制）
 * @param enableMouseClick          是否开启虚拟鼠标点击操作（仅适用于滑动控制）
 * @param longPressTimeoutMillis    长按触发检测时长
 * @param requestPointerCapture     是否使用鼠标抓取方案
 * @param hideMouseInClickMode      是否在鼠标为点击控制模式时，隐藏鼠标指针
 * @param gamepadViewModel          更新手柄状态的ViewModel
 * @param onTouch                   触摸到鼠标层
 * @param onMouse                   实体鼠标交互事件
 * @param onTap                     点击回调
 * @param onCapturedTap             抓取模式点击回调，参数是触摸点在控件内的绝对坐标
 * @param onLongPress               长按开始回调
 * @param onLongPressEnd            长按结束回调
 * @param onCapturedLongPress       抓取模式长按开始回调
 * @param onCapturedLongPressEnd    抓取模式长按结束回调
 * @param onPointerMove             指针移动回调，参数在 SLIDE 模式下是指针位置，CLICK 模式下是手指当前位置
 * @param onCapturedMove            抓取模式指针移动回调，返回滑动偏移量
 * @param onMouseScroll             实体鼠标指针滚轮滑动
 * @param onMouseButton             实体鼠标指针按钮按下反馈
 * @param isMoveOnlyPointer         指针是否被父级标记为仅可滑动指针
 * @param onOccupiedPointer         占用指针回调
 * @param onReleasePointer          释放指针回调
 * @param enableScrollGesture       是否启用双指滑动滚动手势
 * @param onScrollGesture           双指滑动滚动回调，参数为滚轮滚动的偏移量
 * @param mouseSize                 指针大小
 * @param cursorSensitivity         指针灵敏度（滑动模式生效）
 */
@Composable
fun SwitchableMouseLayout(
    modifier: Modifier = Modifier,
    screenSize: IntSize,
    cursorMode: CursorMode,
    controlMode: MouseControlMode = AllSettings.mouseControlMode.state,
    enableMouseClick: Boolean = AllSettings.enableMouseClick.state,
    longPressTimeoutMillis: Long = AllSettings.mouseLongPressDelay.state.toLong(),
    requestPointerCapture: Boolean = !AllSettings.physicalMouseMode.state,
    hideMouseInClickMode: Boolean = AllSettings.hideMouse.state,
    gamepadViewModel: GamepadViewModel? = null,
    onTouch: () -> Unit = {},
    onMouse: () -> Unit = {},
    onTap: (Offset) -> Unit = {},
    onCapturedTap: (Offset) -> Unit = {},
    onLongPress: () -> Unit = {},
    onLongPressEnd: () -> Unit = {},
    onCapturedLongPress: () -> Unit = {},
    onCapturedLongPressEnd: () -> Unit = {},
    onPointerMove: (Offset) -> Unit = {},
    onCapturedMove: (Offset) -> Unit = {},
    onMouseScroll: (Offset) -> Unit = {},
    onMouseButton: (button: Int, pressed: Boolean) -> Unit = { _, _ -> },
    isMoveOnlyPointer: (PointerId) -> Boolean = { false },
    onOccupiedPointer: (PointerId) -> Unit = {},
    onReleasePointer: (PointerId) -> Unit = {},
    enableScrollGesture: Boolean = false,
    onScrollGesture: (Offset) -> Unit = {},
    mouseSize: Dp = AllSettings.mouseSize.state.dp,
    cursorSensitivity: Int = AllSettings.cursorSensitivity.state,
    gamepadCursorSensitivity: Int = AllSettings.gamepadCursorSensitivity.state,
    gamepadCameraSensitivity: Int = AllSettings.gamepadCameraSensitivity.state
) {
    val screenWidth: Float = screenSize.width.toFloat()
    val screenHeight: Float = screenSize.height.toFloat()
    val centerPos = Offset(screenWidth / 2f, screenHeight / 2f)

    val speedFactor = remember(cursorSensitivity) { cursorSensitivity / 100f }
    val gamepadCursorSpeedFactor = remember(gamepadCursorSensitivity) { 19f * (gamepadCursorSensitivity / 100f) }
    val gamepadCameraSpeedFactor = remember(gamepadCameraSensitivity) { 18f * (gamepadCameraSensitivity / 100f) }

    val lastVirtualMousePos = remember { object { var value: Offset? = null } }

    //判断鼠标是否正在被抓取
    val isCaptured by remember(cursorMode) {
        mutableStateOf(
            value = cursorMode == CURSOR_DISABLED
        )
    }

    //当前是否为物理鼠标模式
    var isPhysicalMouseMode by remember {
        mutableStateOf(
            if (PhysicalMouseChecker.physicalMouseConnected) { //物理鼠标已连接
                !requestPointerCapture //根据是否是抓取模式（虚拟鼠标控制模式）判断物理鼠标是否显示
            } else {
                false
            }
        )
    }
    //检查并应用当前物理鼠标模式
    //若未捕获的情况下，正在使用，则标记为物理鼠标模式
    fun checkPhysicalMouseMode(using: Boolean) {
        isPhysicalMouseMode = !requestPointerCapture && using
    }

    var showMousePointer by remember {
        mutableStateOf(requestPointerCapture)
    }
    fun updateMousePointer(show: Boolean) {
        showMousePointer = show
    }
    LaunchedEffect(cursorMode, hideMouseInClickMode) {
        updateMousePointer(
            show = if (cursorMode == CURSOR_ENABLED) {
                when {
                    //物理鼠标已连接 && 当前为物理鼠标模式：是否为抓获控制模式
                    PhysicalMouseChecker.physicalMouseConnected && isPhysicalMouseMode -> requestPointerCapture
                    //点击控制模式：由隐藏虚拟鼠标设置决定
                    controlMode == MouseControlMode.CLICK -> !hideMouseInClickMode
                    //滑动控制始终显示
                    else -> controlMode == MouseControlMode.SLIDE
                }
            } else false
        )
    }

    val requestPointerCapture1 by remember(isCaptured) {
        mutableStateOf(
            value = if (isCaptured) true //被抓取时，开启实体鼠标指针抓取模式
            else requestPointerCapture
        )
    }

    fun updatePointerPos(pos: Offset) {
        lastVirtualMousePos.value = pos
        onPointerMove(pos)
    }
    var pointerPosition by remember {
        mutableStateOf(centerPos)
    }
    LaunchedEffect(isCaptured) {
        val pos = lastVirtualMousePos.value?.takeIf {
            //如果当前正在使用物理鼠标，则使用上次虚拟鼠标的位置
            //否则默认将鼠标放到屏幕正中心
            isPhysicalMouseMode
        } ?: centerPos
        if (!isCaptured) updatePointerPos(pos)
        pointerPosition = pos
    }

    gamepadViewModel?.let { viewModel ->
        val isGrabbing = cursorMode == CURSOR_DISABLED
        GamepadStickCameraListener(
            gamepadViewModel = viewModel,
            isGrabbing = isGrabbing,
            onOffsetEvent = { offset ->
                if (isGrabbing) {
                    updateMousePointer(false)
                    onCapturedMove(
                        Offset(
                            x = offset.x * gamepadCameraSpeedFactor,
                            y = offset.y * gamepadCameraSpeedFactor
                        )
                    )
                } else {
                    updateMousePointer(true)
                    val newOffset = Offset(
                        x = (pointerPosition.x + (offset.x * gamepadCursorSpeedFactor)).coerceIn(0f, screenWidth),
                        y = (pointerPosition.y + (offset.y * gamepadCursorSpeedFactor)).coerceIn(0f, screenHeight)
                    )
                    pointerPosition = newOffset
                    updatePointerPos(newOffset)
                }
            }
        )
    }

    Box(modifier = modifier) {
        val cursorShape by ZLBridgeStates.cursorShape.collectAsStateWithLifecycle()

        if (showMousePointer) {
            MousePointer(
                modifier = Modifier.mouseFixedPosition(
                    mouseSize = mouseSize,
                    cursorShape = cursorShape,
                    pointerPosition = pointerPosition
                ),
                cursorShape = cursorShape,
                mouseSize = mouseSize,
                mouseFile = getMouseFile(cursorShape).ifExists(),
            )
        }

        TouchpadLayout(
            modifier = Modifier.fillMaxSize(),
            controlMode = if (cursorMode == CURSOR_ENABLED) {
                controlMode
            } else {
                //捕获模式下，只有滑动控制模式才能获取到滑动偏移量
                MouseControlMode.SLIDE
            },
            enableMouseClick = enableMouseClick,
            longPressTimeoutMillis = longPressTimeoutMillis,
            requestPointerCapture = requestPointerCapture1,
            pointerIcon = cursorShape.composeIcon,
            onTouch = {
                onTouch()
                checkPhysicalMouseMode(false)
            },
            onMouse = {
                onMouse()
                checkPhysicalMouseMode(true)
            },
            onTap = { fingerPos ->
                when (cursorMode) {
                    CURSOR_DISABLED -> {
                        onCapturedTap(fingerPos)
                    }
                    CURSOR_ENABLED -> {
                        onTap(
                            if (controlMode == MouseControlMode.CLICK) {
                                updateMousePointer(!isCaptured && !hideMouseInClickMode)
                                //当前手指的绝对坐标
                                pointerPosition = fingerPos
                                fingerPos
                            } else {
                                pointerPosition
                            }
                        )
                    }
                }
            },
            onLongPress = {
                when (cursorMode) {
                    CURSOR_DISABLED -> {
                        onCapturedLongPress()
                    }
                    CURSOR_ENABLED -> {
                        onLongPress()
                    }
                }
            },
            onLongPressEnd = {
                when (cursorMode) {
                    CURSOR_DISABLED -> {
                        onCapturedLongPressEnd()
                    }
                    CURSOR_ENABLED -> {
                        onLongPressEnd()
                    }
                }
            },
            onPointerMove = { offset, isMoveOnly ->
                when (cursorMode) {
                    CURSOR_DISABLED -> {
                        updateMousePointer(false)
                        onCapturedMove(offset)
                    }
                    CURSOR_ENABLED -> {
                        pointerPosition = if (isMoveOnly || controlMode == MouseControlMode.SLIDE) {
                            updateMousePointer(true)
                            Offset(
                                x = (pointerPosition.x + offset.x * speedFactor).coerceIn(0f, screenWidth),
                                y = (pointerPosition.y + offset.y * speedFactor).coerceIn(0f, screenHeight)
                            )
                        } else {
                            updateMousePointer(!hideMouseInClickMode)
                            //当前手指的绝对坐标
                            offset
                        }
                        updatePointerPos(pointerPosition)
                    }
                }
            },
            onMouseMove = { offset ->
                when (cursorMode) {
                    CURSOR_DISABLED -> {
                        updateMousePointer(false)
                        onCapturedMove(offset)
                    }
                    CURSOR_ENABLED -> {
                        if (requestPointerCapture) {
                            updateMousePointer(true)
                            pointerPosition = Offset(
                                x = (pointerPosition.x + offset.x * speedFactor).coerceIn(0f, screenWidth),
                                y = (pointerPosition.y + offset.y * speedFactor).coerceIn(0f, screenHeight)
                            )
                            updatePointerPos(pointerPosition)
                        } else {
                            //非鼠标抓取模式
                            updateMousePointer(false)
                            pointerPosition = offset
                            updatePointerPos(pointerPosition)
                        }
                    }
                }
            },
            onMouseScroll = onMouseScroll,
            onMouseButton = onMouseButton,
            isMoveOnlyPointer = isMoveOnlyPointer,
            onOccupiedPointer = onOccupiedPointer,
            onReleasePointer = onReleasePointer,
            enableScrollGesture = enableScrollGesture && cursorMode == CURSOR_ENABLED,
            onScrollGesture = onScrollGesture,
            requestFocusKey = cursorMode
        )
    }
}