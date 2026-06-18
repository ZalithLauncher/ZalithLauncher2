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

import com.movtery.layer_controller.observable.Modifiable
import com.movtery.layer_controller.utils.randomUUID
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 摇杆数据
 * @param uuid 唯一标识符
 * @param position 位置
 * @param size 大小
 * @param style 样式
 * @param deadZoneRatio 死区范围，作为半分比，根据组件整体大小计算
 * @param lockThreshold 前进锁判定范围（在组件的外部，正上方），作为百分比，根据组件整体大小计算
 * @param canLock 是否可以进行前进锁
 */
@Serializable
data class JoystickData(
    @SerialName("uuid")
    val uuid: String,
    @SerialName("position")
    val position: ButtonPosition,
    @SerialName("size")
    val size: ButtonSize,
    @SerialName("style")
    val style: JoystickStyle,
    @SerialName("deadZoneRatio")
    val deadZoneRatio: Float = 0.3f,
    @SerialName("lockThreshold")
    val lockThreshold: Float = 0.3f,
    @SerialName("canLock")
    val canLock: Boolean = true,
    @SerialName("lockSpring")
    val lockSpring: Boolean = true
) : Widget, Modifiable<JoystickData> {
    override fun isModified(other: JoystickData): Boolean {
        return this.uuid != other.uuid ||
                this.position.isModified(other.position) ||
                this.size.isModified(other.size) ||
                this.style.isModified(other.style) ||
                this.deadZoneRatio != other.deadZoneRatio ||
                this.lockThreshold != other.lockThreshold ||
                this.canLock != other.canLock ||
                this.lockSpring != other.lockSpring
    }
}

/**
 * 默认摇杆数据
 */
val DefaultJoystickData = JoystickData(
    uuid = randomUUID(),
    position = BottomStartPosition.copy(x = 1500, y = 8500),
    size = DefaultSize.copy(widthPercentage = 2500, heightPercentage = 2500),
    style = DefaultJoystickStyle,
    deadZoneRatio = 0.3f,
    lockThreshold = 0.3f,
    canLock = true,
    lockSpring = true
)
