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

package com.movtery.zalithlauncher.ui.control.event

import com.movtery.inputmap.keycodes.ControlEventKeycode
import org.lwjgl.glfw.CallbackBridge

/**
 * 点击按键时，处理LWJGL按键事件
 */
fun lwjglEvent(
    eventKey: String,
    isMouse: Boolean,
    isPressed: Boolean
) {
    val keycode: Int = ControlEventKeycode.getKeycodeFromEvent(eventKey)?.toInt() ?: return

    if (isMouse) {
        CallbackBridge.sendMouseButton(keycode, isPressed)
    } else {
        CallbackBridge.sendKeyPress(keycode, CallbackBridge.getCurrentMods(), isPressed)
        CallbackBridge.setModifiers(keycode, isPressed)
    }
}

//启动器点击事件

/** 切换输入法 */
const val LAUNCHER_EVENT_SWITCH_IME = "launcher.event.switch_ime"
/** 切换菜单 */
const val LAUNCHER_EVENT_SWITCH_MENU = "launcher.event.switch_menu"
/** 控制虚拟鼠标滚轮上-长按一直触发 */
const val LAUNCHER_EVENT_SCROLL_UP = "launcher.event.scroll_up"
/** 控制虚拟鼠标滚轮上-单次点击 */
const val LAUNCHER_EVENT_SCROLL_UP_SINGLE = "launcher.event.scroll_up.single"
/** 控制虚拟鼠标滚轮下-长按一直触发 */
const val LAUNCHER_EVENT_SCROLL_DOWN = "launcher.event.scroll_down"
/** 控制虚拟鼠标滚轮下-单次点击 */
const val LAUNCHER_EVENT_SCROLL_DOWN_SINGLE = "launcher.event.scroll_down.single"

/**
 * 点击按键时，处理启动器事件
 */
fun launcherEvent(
    eventKey: String,
    isPressed: Boolean,
    onSwitchIME: () -> Unit,
    onSwitchMenu: () -> Unit,
    onSingleScrollUp: () -> Unit,
    onSingleScrollDown: () -> Unit,
    onLongScrollUp: () -> Unit,
    onLongScrollUpCancel: () -> Unit,
    onLongScrollDown: () -> Unit,
    onLongScrollDownCancel: () -> Unit
) {
    if (eventKey.startsWith("GLFW_MOUSE_", false)) {
        //处理鼠标事件
        lwjglEvent(eventKey = eventKey, isMouse = true, isPressed = isPressed)
    } else {
        if (isPressed) {
            when (eventKey) {
                LAUNCHER_EVENT_SWITCH_IME -> onSwitchIME()
                LAUNCHER_EVENT_SWITCH_MENU -> onSwitchMenu()
                LAUNCHER_EVENT_SCROLL_UP_SINGLE -> onSingleScrollUp()
                LAUNCHER_EVENT_SCROLL_DOWN_SINGLE -> onSingleScrollDown()
            }
        }
        when (eventKey) {
            LAUNCHER_EVENT_SCROLL_UP -> {
                if (isPressed) {
                    onLongScrollUp()
                } else {
                    onLongScrollUpCancel()
                }
            }
            LAUNCHER_EVENT_SCROLL_DOWN -> {
                if (isPressed) {
                    onLongScrollDown()
                } else {
                    onLongScrollDownCancel()
                }
            }
        }
    }
}