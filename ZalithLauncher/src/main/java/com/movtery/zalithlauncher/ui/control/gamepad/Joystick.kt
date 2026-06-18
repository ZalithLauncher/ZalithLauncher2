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

package com.movtery.zalithlauncher.ui.control.gamepad

import androidx.compose.ui.geometry.Offset
import com.movtery.layer_controller.layout.JoystickDirection
import com.movtery.zalithlauncher.viewmodel.GamepadViewModel.Event
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.pow
import kotlin.math.sin

private const val MOUSE_MAX_ACCELERATION = 2.0

/**
 * 摇杆横轴、纵轴偏移量状态
 */
class Joystick(
    val type: JoystickType,
    var horizontalValue: Float = 0f,
    var verticalValue: Float = 0f
) {
    /**
     * 摇杆当前方向
     */
    var direction: JoystickDirection = JoystickDirection.None
        private set

    private var angleRadian: Double? = null
    private var acceleration: Double? = null

    fun isUsing(): Boolean = horizontalValue != 0f || verticalValue != 0f

    fun onTick(sendEvent: (Event) -> Unit) {
        val mouseAngle = angleRadian ?: getAngleRadian()
        val acceleration = acceleration ?: calculateAcceleration()

        val deltaX = (cos(mouseAngle) * acceleration).toFloat()
        val deltaY = (sin(mouseAngle) * acceleration).toFloat()

        val offset = Offset(deltaX, -deltaY)
        //偏移量为0的情况下，无论发不发送事件都是无意义的
        if (offset != Offset.Zero) {
            sendEvent(
                Event.StickOffset(type, offset)
            )
        }

        calculateDirection(mouseAngle).takeIf { it != direction }?.let { d ->
            direction = d
            sendEvent(
                Event.StickDirection(type, d)
            )
        }
    }

    fun updateState(
        horizontal: Float = horizontalValue,
        vertical: Float = verticalValue
    ) {
        this.horizontalValue = horizontal
        this.verticalValue = vertical

        angleRadian = getAngleRadian()
        acceleration = calculateAcceleration()
    }

    fun getAngleRadian(): Double {
        return -atan2(verticalValue.toDouble(), horizontalValue.toDouble())
    }

    fun calculateAcceleration(): Double {
        return getMagnitude().pow(MOUSE_MAX_ACCELERATION).coerceAtMost(1.0)
    }

    fun calculateDirection(
        angleRadian: Double = getAngleRadian()
    ): JoystickDirection {
        val magnitude = getMagnitude()
        if (magnitude == 0.0) return JoystickDirection.None

        val angleDeg = Math.toDegrees(angleRadian).let { if (it < 0) it + 360.0 else it }
        val index = (((angleDeg + 22.5) / 45).toInt() % 8 + 8) % 8

        return JoystickDirection.entries.getOrNull(index) ?: JoystickDirection.None
    }

    fun getMagnitude(): Double {
        val x = abs(horizontalValue)
        val y = abs(verticalValue)
        return hypot(x.toDouble(), y.toDouble())
    }
}