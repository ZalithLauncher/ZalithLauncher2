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

package com.movtery.layer_controller.layout

import com.movtery.layer_controller.data.JoystickData
import com.movtery.layer_controller.data.NormalData
import com.movtery.layer_controller.data.TextData
import com.movtery.layer_controller.data.VisibilityType
import com.movtery.layer_controller.observable.Modifiable
import com.movtery.layer_controller.observable.isModified
import com.movtery.layer_controller.utils.randomUUID
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 控制布局单个层级，像图层一样存储控制组件
 * @param name 层级的名称
 * @param hide 是否隐藏层级
 * @param hideWhenMouse 是否在实体鼠标操控后隐藏
 * @param hideWhenGamepad 是否在手柄操控后隐藏
 * @param visibilityType 层级的可见场景
 * @param normalButtons 普通的按钮列表
 * @param textBoxes 文本显示框列表
 * @param joystickButtons 摇杆控件列表
 */
@Serializable
data class ControlLayer(
    @SerialName("name")
    val name: String,
    @SerialName("uuid")
    val uuid: String,
    @SerialName("hide")
    val hide: Boolean,
    @SerialName("hideWhenMouse")
    val hideWhenMouse: Boolean = true,
    @SerialName("hideWhenGamepad")
    val hideWhenGamepad: Boolean = true,
    @SerialName("visibilityType")
    val visibilityType: VisibilityType,
    @SerialName("normalButtons")
    val normalButtons: List<NormalData> = emptyList(),
    @SerialName("textBoxes")
    val textBoxes: List<TextData> = emptyList(),
    @SerialName("joystickButtons")
    val joystickButtons: List<JoystickData> = emptyList()
): Modifiable<ControlLayer> {
    override fun isModified(other: ControlLayer): Boolean {
        return this.name != other.name ||
                this.uuid != other.uuid ||
                this.hide != other.hide ||
                this.hideWhenMouse != other.hideWhenMouse ||
                this.hideWhenGamepad != other.hideWhenGamepad ||
                this.visibilityType != other.visibilityType ||
                this.normalButtons.isModified(other.normalButtons) ||
                this.textBoxes.isModified(other.textBoxes) ||
                this.joystickButtons.isModified(other.joystickButtons)
    }
}

fun createNewLayer(defaultLayerName: String = ""): ControlLayer {
    return ControlLayer(
        name = defaultLayerName,
        uuid = randomUUID(),
        hide = false,
        hideWhenMouse = true,
        hideWhenGamepad = true,
        visibilityType = VisibilityType.ALWAYS
    )
}