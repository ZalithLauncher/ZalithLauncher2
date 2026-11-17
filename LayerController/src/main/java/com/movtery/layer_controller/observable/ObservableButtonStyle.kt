/*
 * Zalith Launcher 2
 * Copyright (C) 2025-2026 MovTery <movtery228@qq.com> and contributors
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

package com.movtery.layer_controller.observable

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.movtery.layer_controller.data.ButtonStyle
import com.movtery.layer_controller.data.DefaultButtonStyle
import com.movtery.layer_controller.data.cloneNew

/**
 * 可观察的ButtonStyle包装类
 */
class ObservableButtonStyle(
    private val style: ButtonStyle
): Packable<ButtonStyle> {
    val uuid = style.uuid
    var name by mutableStateOf(style.name)
    var animateSwap by mutableStateOf(style.animateSwap)
    var commonStyle by mutableStateOf(style.commonStyle)
    var lightStyle = ObservableStyleConfig(style.lightStyle)
    var darkStyle = ObservableStyleConfig(style.darkStyle)

    override fun pack(): ButtonStyle {
        return ButtonStyle(
            name = this.name,
            uuid = this.uuid,
            animateSwap = this.animateSwap,
            commonStyle = this.commonStyle,
            lightStyle = this.lightStyle.pack(),
            darkStyle = this.darkStyle.pack()
        )
    }

    override fun isModified(): Boolean {
        return style.isModified(pack())
    }
}

val DefaultObservableButtonStyle = ObservableButtonStyle(DefaultButtonStyle)

fun ObservableButtonStyle.cloneNew(): ObservableButtonStyle {
    return ObservableButtonStyle(pack().cloneNew())
}