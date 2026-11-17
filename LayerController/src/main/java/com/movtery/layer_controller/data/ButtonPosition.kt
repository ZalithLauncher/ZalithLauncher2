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

package com.movtery.layer_controller.data

import com.movtery.layer_controller.observable.Modifiable
import com.movtery.layer_controller.utils.checkInRange
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 按钮位置存储取值范围
 */
val POSITION_RANGE: IntRange = 0..10000

/**
 * 按钮的位置
 * @param x 0~10000
 * @param y 0~10000
 */
@Serializable
data class ButtonPosition(
    @SerialName("x")
    val x: Int,
    @SerialName("y")
    val y: Int
): Modifiable<ButtonPosition> {
    init {
        checkInRange("x", x, POSITION_RANGE)
        checkInRange("y", y, POSITION_RANGE)
    }

    /**
     * 计算x坐标百分比
     */
    fun xPercentage(): Float {
        return (x / 10000f).coerceAtMost(1f).coerceAtLeast(0f)
    }

    /**
     * 计算y坐标百分比
     */
    fun yPercentage(): Float {
        return (y / 10000f).coerceAtMost(1f).coerceAtLeast(0f)
    }

    override fun isModified(other: ButtonPosition): Boolean {
        return this.x != other.x ||
                this.y != other.y
    }
}

/**
 * 位于屏幕左上角
 */
val TopStartPosition = ButtonPosition(0, 0)

/**
 * 位于屏幕右上角
 */
val TopEndPosition = ButtonPosition(10000, 0)

/**
 * 位于屏幕中心
 */
val CenterPosition = ButtonPosition(5000, 5000)

/**
 * 位于屏幕左下角
 */
val BottomStartPosition = ButtonPosition(0, 10000)

/**
 * 位于屏幕右下角
 */
val BottomEndPosition = ButtonPosition(10000, 10000)
