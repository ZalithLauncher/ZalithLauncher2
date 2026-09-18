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

package com.movtery.zalithlauncher.setting.enums

import com.movtery.inputmap.keycodes.LwjglGlfwKeycode
import com.movtery.zalithlauncher.R

/**
 * 手势控制点击时触发的按钮
 */
enum class GestureActionType(val nameRes: Int) {
    MOUSE_RIGHT(R.string.settings_control_gesture_trigger_mouse_right),
    MOUSE_LEFT(R.string.settings_control_gesture_trigger_mouse_left)
}

/**
 * 转换为实际的Lwjgl键值
 */
fun GestureActionType.toAction(): Int =
    when (this) {
        GestureActionType.MOUSE_RIGHT -> LwjglGlfwKeycode.GLFW_MOUSE_BUTTON_RIGHT
        GestureActionType.MOUSE_LEFT -> LwjglGlfwKeycode.GLFW_MOUSE_BUTTON_LEFT
    }.toInt()