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

package com.movtery.layer_controller.data.legacy

import com.movtery.layer_controller.data.JoystickStyle
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 旧版编辑器布局（版本 8~11）中 special 字段
 */
@Serializable
data class LegacySpecial(
    @SerialName("joystickStyle")
    val joystickStyle: LegacyJoystickStyle? = null
)

/**
 * 旧版 [JoystickStyle]
 */
@Serializable
data class LegacyJoystickStyle(
    @SerialName("uuid")
    val uuid: String,
    @SerialName("lightStyle")
    val lightStyle: JoystickStyle.StyleConfig,
    @SerialName("darkStyle")
    val darkStyle: JoystickStyle.StyleConfig
) {
    fun toJoystickStyle(): JoystickStyle = JoystickStyle(
        name = "Legacy",
        uuid = uuid,
        commonStyle = true,
        lightStyle = lightStyle,
        darkStyle = darkStyle
    )
}
