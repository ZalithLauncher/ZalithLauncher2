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

package com.movtery.layer_controller.observable

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.movtery.layer_controller.data.JoystickData
import com.movtery.layer_controller.data.NormalData
import com.movtery.layer_controller.data.TextData
import com.movtery.layer_controller.layout.ControlLayer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * 可观察的ControlLayer包装类
 */
class ObservableControlLayer(
    private val layer: ControlLayer
): Packable<ControlLayer> {
    val uuid: String = layer.uuid

    var editorHide by mutableStateOf(layer.hide)

    var name by mutableStateOf(layer.name)
    var hide by mutableStateOf(editorHide)
    var hideWhenMouse by mutableStateOf(layer.hideWhenMouse)
    var hideWhenGamepad by mutableStateOf(layer.hideWhenGamepad)
    var visibilityType by mutableStateOf(layer.visibilityType)
    
    private val _normalButtons = MutableStateFlow(layer.normalButtons.map { ObservableNormalData(it) })
    val normalButtons = _normalButtons.asStateFlow()
    
    private val _textBoxes = MutableStateFlow(layer.textBoxes.map { ObservableTextData(it) })
    val textBoxes = _textBoxes.asStateFlow()

    private val _joysticks = MutableStateFlow(layer.joysticks.map { ObservableJoystickData(it) })
    val joysticks = _joysticks.asStateFlow()

    /**
     * 添加一个普通的按钮
     */
    fun addNormalButton(button: NormalData) {
        addNormalButton(ObservableNormalData(button))
    }

    /**
     * 添加一个普通的按钮
     */
    fun addNormalButton(button: ObservableNormalData) {
        _normalButtons.update { it + button }
    }

    /**
     * 批量添加普通的按钮
     */
    fun addAllNormalButton(buttons: List<ObservableNormalData>) {
        _normalButtons.update { it + buttons }
    }

    /**
     * 移除一个普通的按钮
     */
    fun removeNormalButton(uuid: String) {
        _normalButtons.update { oldList ->
            oldList.filterNot { it.uuid == uuid }
        }
    }

    /**
     * 添加文本展示框
     */
    fun addTextBox(textBox: TextData) {
        addTextBox(ObservableTextData(textBox))
    }

    /**
     * 添加文本展示框
     */
    fun addTextBox(textBox: ObservableTextData) {
        _textBoxes.update { it + textBox }
    }

    /**
     * 批量添加文本展示框
     */
    fun addAllTextBox(textBoxes: List<ObservableTextData>) {
        _textBoxes.update { it + textBoxes }
    }

    /**
     * 移除文本展示框
     */
    fun removeTextBox(uuid: String) {
        _textBoxes.update { oldList ->
            oldList.filterNot { it.uuid == uuid }
        }
    }

    var onJoysticksChanged: () -> Unit = {}

    /**
     * 添加摇杆
     */
    fun addJoystick(joystick: JoystickData) {
        addJoystick(ObservableJoystickData(joystick))
    }

    /**
     * 添加摇杆
     */
    fun addJoystick(joystick: ObservableJoystickData) {
        _joysticks.update { it + joystick }
        onJoysticksChanged()
    }

    /**
     * 批量添加摇杆
     */
    fun addAllJoystick(joysticks: List<ObservableJoystickData>) {
        _joysticks.update { it + joysticks }
        onJoysticksChanged()
    }

    /**
     * 移除摇杆
     */
    fun removeJoystick(uuid: String) {
        _joysticks.update { oldList ->
            oldList.filterNot { it.uuid == uuid }
        }
        onJoysticksChanged()
    }

    override fun pack(): ControlLayer {
        return ControlLayer(
            name = name,
            uuid = uuid,
            hide = editorHide,
            hideWhenMouse = hideWhenMouse,
            hideWhenGamepad = hideWhenGamepad,
            visibilityType = visibilityType,
            normalButtons = _normalButtons.value.map { it.packNormal() },
            textBoxes = _textBoxes.value.map { it.packText() },
            joysticks = _joysticks.value.map { it.packJoystick() }
        )
    }

    override fun isModified(): Boolean {
        return this.layer.isModified(pack())
    }
}