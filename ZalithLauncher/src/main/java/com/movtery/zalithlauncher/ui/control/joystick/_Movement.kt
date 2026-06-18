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

package com.movtery.zalithlauncher.ui.control.joystick

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateSetOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import com.movtery.layer_controller.event.ClickEvent
import com.movtery.layer_controller.layout.JoystickDirection
import com.movtery.zalithlauncher.game.keycodes.MOVEMENT_BACK
import com.movtery.zalithlauncher.game.keycodes.MOVEMENT_BACK_VALUE
import com.movtery.zalithlauncher.game.keycodes.MOVEMENT_FORWARD
import com.movtery.zalithlauncher.game.keycodes.MOVEMENT_FORWARD_VALUE
import com.movtery.zalithlauncher.game.keycodes.MOVEMENT_LEFT
import com.movtery.zalithlauncher.game.keycodes.MOVEMENT_LEFT_VALUE
import com.movtery.zalithlauncher.game.keycodes.MOVEMENT_RIGHT
import com.movtery.zalithlauncher.game.keycodes.MOVEMENT_RIGHT_VALUE
import com.movtery.zalithlauncher.game.keycodes.mapToControlEvent
import com.movtery.zalithlauncher.viewmodel.JoystickMovementViewModel

/**
 * 摇杆的每个方向代表的移动键键值
 */
val directionMapping = mapOf(
    JoystickDirection.East to listOf(
        MOVEMENT_RIGHT to MOVEMENT_RIGHT_VALUE
    ),
    JoystickDirection.NorthEast to listOf(
        MOVEMENT_FORWARD to MOVEMENT_FORWARD_VALUE,
        MOVEMENT_RIGHT to MOVEMENT_RIGHT_VALUE
    ),
    JoystickDirection.North to listOf(
        MOVEMENT_FORWARD to MOVEMENT_FORWARD_VALUE
    ),
    JoystickDirection.NorthWest to listOf(
        MOVEMENT_FORWARD to MOVEMENT_FORWARD_VALUE,
        MOVEMENT_LEFT to MOVEMENT_LEFT_VALUE
    ),
    JoystickDirection.West to listOf(
        MOVEMENT_LEFT to MOVEMENT_LEFT_VALUE
    ),
    JoystickDirection.SouthWest to listOf(
        MOVEMENT_BACK to MOVEMENT_BACK_VALUE,
        MOVEMENT_LEFT to MOVEMENT_LEFT_VALUE
    ),
    JoystickDirection.South to listOf(
        MOVEMENT_BACK to MOVEMENT_BACK_VALUE
    ),
    JoystickDirection.SouthEast to listOf(
        MOVEMENT_BACK to MOVEMENT_BACK_VALUE,
        MOVEMENT_RIGHT to MOVEMENT_RIGHT_VALUE
    ),
    JoystickDirection.None to emptyList()
)

/**
 * 所有的移动键键值
 */
val allAction = listOf(
    MOVEMENT_FORWARD to MOVEMENT_FORWARD_VALUE,
    MOVEMENT_BACK to MOVEMENT_BACK_VALUE,
    MOVEMENT_LEFT to MOVEMENT_LEFT_VALUE,
    MOVEMENT_RIGHT to MOVEMENT_RIGHT_VALUE
)

/**
 * 摇杆、方向键的方向处理监听器注册器
 */
@Composable
private fun ListenerRegister(
    viewModel: JoystickMovementViewModel,
    listener: (JoystickDirection) -> Unit,
    onDisposeCallback: (() -> Unit)? = null
) {
    DisposableEffect(Unit) {
        viewModel.registerListener(listener)
        onDispose {
            onDisposeCallback?.invoke()
            viewModel.unregisterListener(listener)
        }
    }
}

/**
 * 摇杆、方向键组件控制玩家移动的事件监听器
 * @param isGrabbing 判断当前是否在游戏中，若在游戏内，控制玩家移动
 * @param onKeyEvent 回调根据 options.txt 内保存的移动键键值转化的控制事件
 */
@Composable
fun JoystickDirectionListener(
    viewModel: JoystickMovementViewModel,
    isGrabbing: Boolean,
    onKeyEvent: (event: ClickEvent, pressed: Boolean) -> Unit
) {
    val currentIsGrabbing by rememberUpdatedState(isGrabbing)
    val currentOnKeyEvent by rememberUpdatedState(onKeyEvent)

    //缓存已按下的事件，目的是当游戏进入菜单后，能够清除状态
    //避免回到游戏时出现一直移动的问题
    val allPressEvent = remember { mutableStateSetOf<String>() }

    fun sendKeyEvent(
        mcKey: String,
        defaultValue: String,
        pressed: Boolean
    ) {
        mapToControlEvent(mcKey, defaultValue)?.let { event ->
            if (pressed) {
                allPressEvent.add(event)
            } else {
                allPressEvent.remove(event)
            }

            currentOnKeyEvent(
                ClickEvent(type = ClickEvent.Type.Key, event),
                pressed
            )
        }
    }

    fun clearPressedEvent() {
        if (allPressEvent.isNotEmpty()) {
            allPressEvent.forEach { event ->
                currentOnKeyEvent(
                    ClickEvent(type = ClickEvent.Type.Key, event),
                    false
                )
            }
            allPressEvent.clear()
        }
    }

    ListenerRegister(
        viewModel = viewModel,
        listener = { direction ->
            if (!currentIsGrabbing) {
                clearPressedEvent()
                return@ListenerRegister
            }

            allAction.forEach { (key, defaultValue) ->
                sendKeyEvent(key, defaultValue, false)
            }

            directionMapping[direction]?.forEach { (key, defaultValue) ->
                sendKeyEvent(key, defaultValue, true)
            }
        },
        onDisposeCallback = {
            clearPressedEvent()
        }
    )
}