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

package com.movtery.zalithlauncher.ui.control

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerId
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.movtery.inputmap.keycodes.DROP
import com.movtery.inputmap.keycodes.DROP_VALUE
import com.movtery.inputmap.keycodes.HOTBAR_1
import com.movtery.inputmap.keycodes.HOTBAR_1_VALUE
import com.movtery.inputmap.keycodes.HOTBAR_2
import com.movtery.inputmap.keycodes.HOTBAR_2_VALUE
import com.movtery.inputmap.keycodes.HOTBAR_3
import com.movtery.inputmap.keycodes.HOTBAR_3_VALUE
import com.movtery.inputmap.keycodes.HOTBAR_4
import com.movtery.inputmap.keycodes.HOTBAR_4_VALUE
import com.movtery.inputmap.keycodes.HOTBAR_5
import com.movtery.inputmap.keycodes.HOTBAR_5_VALUE
import com.movtery.inputmap.keycodes.HOTBAR_6
import com.movtery.inputmap.keycodes.HOTBAR_6_VALUE
import com.movtery.inputmap.keycodes.HOTBAR_7
import com.movtery.inputmap.keycodes.HOTBAR_7_VALUE
import com.movtery.inputmap.keycodes.HOTBAR_8
import com.movtery.inputmap.keycodes.HOTBAR_8_VALUE
import com.movtery.inputmap.keycodes.HOTBAR_9
import com.movtery.inputmap.keycodes.HOTBAR_9_VALUE
import com.movtery.inputmap.keycodes.LwjglGlfwKeycode
import com.movtery.inputmap.keycodes.SWAP_OFFHAND
import com.movtery.inputmap.keycodes.SWAP_OFFHAND_VALUE
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.bridge.ZLBridgeStates
import com.movtery.zalithlauncher.game.keycodes.mapToKeycode
import com.movtery.zalithlauncher.game.launch.MCOptions
import com.movtery.zalithlauncher.setting.AllSettings
import com.movtery.zalithlauncher.utils.rememberGameRenderSize
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

enum class HotbarRule(val nameRes: Int) {
    /**
     * 自动计算(一些情况下并不精准)
     */
    Auto(R.string.game_menu_option_hotbar_rule_auto),

    /**
     * 完全自定义大小
     */
    Custom(R.string.game_menu_option_hotbar_rule_custom)
}

/**
 * 自定义大小：0~1000比例下，计算百分比值
 */
fun Int.hotbarPercentage() = this / 1000f

/**
 * 快捷栏按键绑定键
 */
private val hotbarList = listOf(
    HOTBAR_1 to HOTBAR_1_VALUE,
    HOTBAR_2 to HOTBAR_2_VALUE,
    HOTBAR_3 to HOTBAR_3_VALUE,
    HOTBAR_4 to HOTBAR_4_VALUE,
    HOTBAR_5 to HOTBAR_5_VALUE,
    HOTBAR_6 to HOTBAR_6_VALUE,
    HOTBAR_7 to HOTBAR_7_VALUE,
    HOTBAR_8 to HOTBAR_8_VALUE,
    HOTBAR_9 to HOTBAR_9_VALUE,
)

private val keyList = listOf(
    LwjglGlfwKeycode.GLFW_KEY_1,
    LwjglGlfwKeycode.GLFW_KEY_2,
    LwjglGlfwKeycode.GLFW_KEY_3,
    LwjglGlfwKeycode.GLFW_KEY_4,
    LwjglGlfwKeycode.GLFW_KEY_5,
    LwjglGlfwKeycode.GLFW_KEY_6,
    LwjglGlfwKeycode.GLFW_KEY_7,
    LwjglGlfwKeycode.GLFW_KEY_8,
    LwjglGlfwKeycode.GLFW_KEY_9
)

/**
 * Minecraft 快捷栏判定箱
 * 根据屏幕分辨率定位 MC 的快捷栏位置
 * 点击、滑动快捷栏，会计算指针处于哪个槽位中，并触发 [sendKeycode] 回调
 *
 * @param isGrabbing 处于鼠标抓获模式下，才会开启判定箱
 * @param displayOffset 游戏画面的黑边偏移，用于对齐自定义分辨率下的快捷栏位置
 */
@Composable
fun BoxScope.MinecraftHotbar(
    screenSize: IntSize,
    rule: HotbarRule,
    widthPercentage: Float,
    heightPercentage: Float,
    sendKeycode: (key: Int) -> Unit,
    isGrabbing: Boolean = false,
    displayOffset: IntOffset = IntOffset.Zero,
    onOccupiedPointer: (PointerId) -> Unit,
    onReleasePointer: (PointerId) -> Unit
) {
    val density = LocalDensity.current

    var hotbarSize by remember { mutableStateOf(DpSize(0.dp, 0.dp)) }
    val hotbarUpdateAnim = remember { Animatable(0f) }

    when (rule) {
        HotbarRule.Auto -> {
            val optionsChangeKey by MCOptions.refreshKey.collectAsStateWithLifecycle()
            val windowChangeKey by ZLBridgeStates.windowChangeKey.collectAsStateWithLifecycle()
            val renderSize = rememberGameRenderSize(screenSize)
            LaunchedEffect(
                isGrabbing, optionsChangeKey, screenSize, density,
                renderSize, windowChangeKey
            ) {
                val guiScale = getMCGuiScale(renderSize.width, renderSize.height)
                val slotSize = guiScale * 20

                with(density) {
                    hotbarSize = DpSize((slotSize * hotbarList.size).toDp(), slotSize.toDp())
                }
            }
        }
        HotbarRule.Custom -> {
            var isInitialized by remember { mutableStateOf(false) }

            LaunchedEffect(
                widthPercentage, heightPercentage
            ) {
                val width = (screenSize.width * widthPercentage).toInt()
                val height = (screenSize.height * heightPercentage).toInt()

                with(density) {
                    hotbarSize = DpSize(width.toDp(), height.toDp())
                }

                if (isInitialized) {
                    hotbarUpdateAnim.snapTo(0.5f)
                    delay(1000L.milliseconds)
                    hotbarUpdateAnim.animateTo(0f, tween(800))
                } else {
                    isInitialized = true
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .size(hotbarSize)
            .align(Alignment.BottomCenter)
            .offset {
                //跟随游戏画面的显示区域，对齐黑边偏移
                IntOffset(x = 0, y = -displayOffset.y)
            }
            .then(
                if (rule == HotbarRule.Custom) Modifier.background(Color.Red.copy(alpha = hotbarUpdateAnim.value))
                else Modifier
            )
    ) {
        if (isGrabbing) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .mainTouchLogic(
                        slotCount = hotbarList.size,
                        hotbarSize = hotbarSize,
                        density = density,
                        longClickDelay = AllSettings.hotbarLongClickDelay.state.toLong(),
                        onClick = { index ->
                            val keycode = calculateSlotKeycode(index)
                            sendKeycode(keycode)
                        },
                        enableDoubleClick = AllSettings.hotbarDoubleClick.state,
                        onDoubleClick = {
                            //发送切换副手按键键值
                            val swapKeycode = getKeycode(
                                optionKey = SWAP_OFFHAND,
                                optionValue = SWAP_OFFHAND_VALUE,
                                defaultValue = LwjglGlfwKeycode.GLFW_KEY_F
                            )
                            sendKeycode(swapKeycode)
                        },
                        enableLongClick = AllSettings.hotbarLongClick.state,
                        onLongClick = {
                            //发送丢弃按键键值
                            val dropKeycode = getKeycode(
                                optionKey = DROP,
                                optionValue = DROP_VALUE,
                                defaultValue = LwjglGlfwKeycode.GLFW_KEY_Q
                            )
                            sendKeycode(dropKeycode)
                        },
                        onOccupiedPointer = onOccupiedPointer,
                        onReleasePointer = onReleasePointer
                    )
            )
        }
    }
}

private data class PointerState(
    val initialPosition: Offset,
    val initialSlotIndex: Int,
    var currentSlotIndex: Int,
    var isMovedBeyondSlop: Boolean = false,
    var isLongPressedTriggered: Boolean = false,
    var longPressJob: Job? = null,
    var isPressed: Boolean = true,
)

private data class DownSlot(
    val slot: Int,
    val downTime: Long,
)

private fun Modifier.mainTouchLogic(
    slotCount: Int,
    hotbarSize: DpSize,
    density: Density,
    longClickDelay: Long,
    enableDoubleClick: Boolean,
    enableLongClick: Boolean,
    onClick: (index: Int) -> Unit,
    onDoubleClick: () -> Unit,
    onLongClick: () -> Unit,
    onOccupiedPointer: (PointerId) -> Unit,
    onReleasePointer: (PointerId) -> Unit
): Modifier = this.pointerInput(
    slotCount, hotbarSize, density, longClickDelay, enableDoubleClick, enableLongClick
) {
    val touchSlop = viewConfiguration.touchSlop
    val doubleTapTimeout = viewConfiguration.doubleTapTimeoutMillis

    val states = mutableMapOf<PointerId, PointerState>()
    val occupiedPointers = mutableSetOf<PointerId>()
    var lastSlot: DownSlot? = null

    coroutineScope {
        awaitPointerEventScope {
            while (true) {
                val event = awaitPointerEvent(PointerEventPass.Initial)
                event.changes.forEach { change ->
                    if (change.isConsumed) return@forEach

                    val pointerId = change.id
                    val currentTime = change.uptimeMillis

                    when {
                        //手指刚按下
                        change.pressed && !change.previousPressed -> {
                            if (pointerId !in occupiedPointers) {
                                onOccupiedPointer(pointerId)
                                occupiedPointers.add(pointerId)
                            }

                            val x = change.position.x
                            val slotIndex = calculateSlotIndex(x, hotbarSize, slotCount, density)
                            //碰到就视为点击，避免后续逻辑临时切物品栏导致游戏状态不同步
                            onClick(slotIndex)

                            val state = PointerState(
                                initialPosition = change.position,
                                initialSlotIndex = slotIndex,
                                currentSlotIndex = slotIndex,
                                isPressed = true
                            )

                            //仅启用长按时启动检测
                            if (enableLongClick) {
                                state.longPressJob = launch {
                                    delay(longClickDelay.milliseconds)
                                    if (state.isPressed && !state.isMovedBeyondSlop && !state.isLongPressedTriggered) {
                                        state.isLongPressedTriggered = true
                                        //触发长按，使用当前所在的栏位进行回调
                                        onLongClick()
                                        if (enableDoubleClick) {
                                            lastSlot = null
                                        }
                                    }
                                }
                            }

                            states[pointerId] = state
                            change.consume()
                        }

                        //按下、滑动
                        change.pressed && change.previousPressed -> {
                            val state = states[pointerId] ?: return@forEach
                            //滑动时实时计算并更新当前槽位
                            state.currentSlotIndex = calculateSlotIndex(change.position.x, hotbarSize, slotCount, density)

                            if (enableLongClick) {
                                val distance = (change.position - state.initialPosition).getDistance()
                                if (!state.isMovedBeyondSlop && distance > touchSlop) {
                                    state.isMovedBeyondSlop = true
                                    state.longPressJob?.cancel()
                                    state.longPressJob = null
                                }
                            }

                            change.consume()
                        }

                        //松开手指
                        !change.pressed && change.previousPressed -> {
                            val state = states.remove(pointerId) ?: return@forEach
                            state.isPressed = false
                            state.longPressJob?.cancel()
                            state.longPressJob = null

                            if (pointerId in occupiedPointers) {
                                occupiedPointers.remove(pointerId)
                                onReleasePointer(pointerId)
                            }

                            //长按已触发，不再处理点击
                            if (state.isLongPressedTriggered) {
                                change.consume()
                                return@forEach
                            }

                            val finalSlotIndex = state.currentSlotIndex
                            if (enableDoubleClick) {
                                val isDoubleTap = lastSlot?.let { last ->
                                    //检查是当前点击的栏位
                                    val isSlot = last.slot == finalSlotIndex
                                    //检查双击时间间隔
                                    val inTime = currentTime - last.downTime < doubleTapTimeout
                                    (isSlot && inTime).also { result ->
                                        //都不满足条件时，清除上一次点击，避免误判
                                        if (!result) lastSlot = null
                                    }
                                } ?: false

                                if (isDoubleTap) {
                                    onDoubleClick()
                                    lastSlot = null
                                } else {
                                    //完成单击后，记录上一次点击的槽位
                                    lastSlot = DownSlot(
                                        slot = finalSlotIndex,
                                        downTime = currentTime,
                                    )
                                }
                            } else {
                                onClick(finalSlotIndex)
                            }

                            change.consume()
                        }
                    }
                }
            }
        }
    }
}

private fun getMCGuiScale(width: Int, height: Int): Int {
    val guiScale = MCOptions.get("guiScale")?.toIntOrNull() ?: 4
    val scale = minOf(width / 320, height / 240).coerceAtLeast(1)
    return if (scale < guiScale || guiScale == 0) scale else guiScale
}

private fun calculateSlotIndex(
    x: Float,
    hotbarSize: DpSize,
    slotCount: Int,
    density: Density
): Int {
    val totalWidth = with(density) { hotbarSize.width.toPx() }
    val slotWidth = totalWidth / slotCount
    return (x / slotWidth).toInt().coerceIn(0, slotCount - 1)
}

private fun calculateSlotKeycode(
    slotIndex: Int
): Int {
    val pair = hotbarList[slotIndex]
    val keyCode = getKeycode(
        optionKey = pair.first,
        optionValue = pair.second,
        defaultValue = keyList[slotIndex]
    )
    return keyCode
}

private fun getKeycode(
    optionKey: String,
    optionValue: String,
    defaultValue: Short
): Int {
    return mapToKeycode(optionKey, optionValue) ?: defaultValue.toInt()
}