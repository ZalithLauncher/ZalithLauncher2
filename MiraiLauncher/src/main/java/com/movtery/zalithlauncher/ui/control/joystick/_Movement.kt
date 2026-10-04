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

import com.movtery.inputmap.keycodes.MOVEMENT_BACK
import com.movtery.inputmap.keycodes.MOVEMENT_BACK_VALUE
import com.movtery.inputmap.keycodes.MOVEMENT_FORWARD
import com.movtery.inputmap.keycodes.MOVEMENT_FORWARD_VALUE
import com.movtery.inputmap.keycodes.MOVEMENT_LEFT
import com.movtery.inputmap.keycodes.MOVEMENT_LEFT_VALUE
import com.movtery.inputmap.keycodes.MOVEMENT_RIGHT
import com.movtery.inputmap.keycodes.MOVEMENT_RIGHT_VALUE

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
