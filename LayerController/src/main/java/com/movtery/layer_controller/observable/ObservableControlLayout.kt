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
import com.movtery.layer_controller.EDITOR_VERSION
import com.movtery.layer_controller.data.ButtonStyle
import com.movtery.layer_controller.layout.ControlLayer
import com.movtery.layer_controller.layout.ControlLayout
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * 可观察的ControlLayout包装类，用于监听变化
 */
class ObservableControlLayout(
    private val layout: ControlLayout
): Packable<ControlLayout> {
    val info = ObservableControlInfo(layout.info)

    private val _layers = MutableStateFlow(layout.layers.map { initLayer(it) })
    val layers = _layers.asStateFlow()

    /**
     * 全局唯一的摇杆（如果存在）
     */
    var globalJoystick by mutableStateOf(findGlobalJoystick())
        private set

    private fun findGlobalJoystick(): ObservableJoystickData? {
        _layers.value.forEach { layer ->
            val found = layer.joysticks.value.firstOrNull()
            if (found != null) return found
        }
        return null
    }

    private fun initLayer(layer: ControlLayer): ObservableControlLayer {
        return ObservableControlLayer(layer).apply {
            onJoysticksChanged = {
                globalJoystick = findGlobalJoystick()
            }
        }
    }

    private val _styles = MutableStateFlow(layout.styles.map { ObservableButtonStyle(it) })
    val styles = _styles.asStateFlow()

    private val _special = MutableStateFlow(ObservableSpecial(layout.special))
    val special = _special.asStateFlow()

    /**
     * 添加控件层
     * @return 新添加的可观察控件层
     */
    fun addLayer(layer: ControlLayer): ObservableControlLayer {
        val newLayer = initLayer(layer)
        // 在顶部添加
        _layers.update { oldList ->
            val newList = listOf(newLayer) + oldList
            globalJoystick = findGlobalJoystickInternal(newList)
            newList
        }
        return newLayer
    }

    private fun findGlobalJoystickInternal(currentLayers: List<ObservableControlLayer>): ObservableJoystickData? {
        for (layer in currentLayers) {
            val found = layer.joysticks.value.firstOrNull()
            if (found != null) return found
        }
        return null
    }

    /**
     * 移除控件层
     */
    fun removeLayer(uuid: String) {
        _layers.update { oldLayers ->
            val newList = oldLayers.filterNot { it.uuid == uuid }
            globalJoystick = findGlobalJoystickInternal(newList)
            newList
        }
    }

    /**
     * 合并至下层
     */
    fun mergeDownward(layer: ObservableControlLayer) {
        _layers.update { oldLayers ->
            if (oldLayers.isEmpty()) return@update oldLayers

            val layers = oldLayers.toMutableList()
            val index = layers.indexOf(layer)
            if (index == -1 || index + 1 >= layers.size) return@update oldLayers

            val downLayer = layers[index + 1]

            layer.normalButtons.value.takeIf {
                it.isNotEmpty()
            }?.let {
                downLayer.addAllNormalButton(it)
            }

            layer.textBoxes.value.takeIf {
                it.isNotEmpty()
            }?.let {
                downLayer.addAllTextBox(it)
            }

            layer.joysticks.value.takeIf { it.isNotEmpty() }?.let {
                downLayer.addAllJoystick(it)
            }

            layers.removeAt(index)
            globalJoystick = findGlobalJoystickInternal(layers)
            layers
        }
    }

    /**
     * 调换层级顺序
     */
    fun reorder(fromIndex: Int, toIndex: Int) {
        _layers.update { oldLayers ->
            oldLayers.toMutableList().apply {
                add(toIndex, removeAt(fromIndex))
                globalJoystick = findGlobalJoystickInternal(this)
            }
        }
    }

    /**
     * 添加新的按钮样式
     */
    fun addStyle(style: ButtonStyle) {
        _styles.update { it + ObservableButtonStyle(style) }
    }

    /**
     * 复制控件样式
     */
    fun cloneStyle(style: ObservableButtonStyle) {
        _styles.update { it + style.cloneNew() }
    }

    /**
     * 移除按钮样式
     */
    fun removeStyle(uuid: String) {
        _styles.update { oldStyles ->
            oldStyles.filterNot { it.uuid == uuid }
        }
        layers.value.forEach { layer ->
            layer.normalButtons.value.forEach { button ->
                if (button.buttonStyle == uuid) {
                    button.buttonStyle = null
                }
            }
            layer.textBoxes.value.forEach { textBox ->
                if (textBox.buttonStyle == uuid) {
                    textBox.buttonStyle = null
                }
            }
        }
    }

    /**
     * 将编辑器内层级隐藏状态同步到实际隐藏状态
     * 供预览模式下使用正确的隐藏状态
     */
    fun applyEditorHide() {
        layers.value.forEach { layer ->
            layer.hide = layer.editorHide
        }
    }

    override fun pack(): ControlLayout {
        return ControlLayout(
            info = info.pack(),
            layers = _layers.value.map { it.pack() },
            styles = _styles.value.map { it.pack() },
            special = _special.value.pack(),
            editorVersion = EDITOR_VERSION
        )
    }

    override fun isModified(): Boolean {
        return this.layout.isModified(pack())
    }
}
