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

package com.movtery.zalithlauncher.ui.screens.game.elements

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import com.movtery.zalithlauncher.ui.control.Keyboard
import com.movtery.zalithlauncher.ui.control.event.lwjglEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

sealed interface SendKeycodeState {
    data object None : SendKeycodeState
    data object ShowDialog : SendKeycodeState
}

@Composable
fun SendKeycodeOperation(
    operation: SendKeycodeState,
    onChange: (SendKeycodeState) -> Unit,
    lifecycleScope: CoroutineScope
) {
    when (operation) {
        is SendKeycodeState.None -> {}
        is SendKeycodeState.ShowDialog -> {
            val pressedEvents = remember { mutableStateListOf<String>() }
            Keyboard(
                onDismissRequest = {
                    onChange(SendKeycodeState.None)
                },
                onSwitch = { keyString, pressed ->
                    lifecycleScope.launch {
                        if (pressed) pressedEvents.add(keyString)
                        else pressedEvents.remove(keyString)
                        lwjglEvent(keyString, isMouse = false, isPressed = pressed)
                    }
                }
            )
            DisposableEffect(Unit) {
                onDispose {
                    //停止所有的按键事件
                    pressedEvents.forEach { keyString ->
                        lwjglEvent(keyString, isMouse = false, isPressed = false)
                    }
                }
            }
        }
    }
}