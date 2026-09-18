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

package com.movtery.layer_controller.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 摇杆操控方式
 */
@Serializable
enum class JoystickTriggerMode {
    /**
     * 拖动触发
     * 手指需在摇杆上拖动才能使用
     */
    @SerialName("drag")
    DRAG,
    /**
     * 触碰触发
     * 手指碰到摇杆的可触摸范围即可触发并使用
     */
    @SerialName("touch")
    TOUCH
}
