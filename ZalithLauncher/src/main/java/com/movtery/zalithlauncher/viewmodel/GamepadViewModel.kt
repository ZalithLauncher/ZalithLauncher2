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

package com.movtery.zalithlauncher.viewmodel

import android.util.SparseBooleanArray
import android.view.KeyEvent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.core.util.set
import androidx.lifecycle.ViewModel
import com.movtery.layer_controller.layout.JoystickDirection
import com.movtery.zalithlauncher.setting.AllSettings
import com.movtery.zalithlauncher.ui.control.gamepad.DpadDirection
import com.movtery.zalithlauncher.ui.control.gamepad.GamepadMap
import com.movtery.zalithlauncher.ui.control.gamepad.GamepadMapping
import com.movtery.zalithlauncher.ui.control.gamepad.GamepadMappingList
import com.movtery.zalithlauncher.ui.control.gamepad.GamepadRemap
import com.movtery.zalithlauncher.ui.control.gamepad.Joystick
import com.movtery.zalithlauncher.ui.control.gamepad.JoystickType
import com.movtery.zalithlauncher.ui.control.gamepad.keyMappingListMMKV
import com.movtery.zalithlauncher.ui.control.gamepad.keyMappingMMKV
import io.ktor.util.collections.ConcurrentSet

private const val BUTTON_PRESS_THRESHOLD = 0.85f
const val GAMEPAD_CONFIG_NAME_LENGTH = 16

private const val AXIS_ACTIVATION_THRESHOLD = 0.6f
private const val AXIS_RESET_THRESHOLD = 0.4f

class GamepadViewModel : ViewModel() {
    private val keyListeners = mutableListOf<(KeyEvent) -> Unit>()

    /**
     * 发送一个按键事件
     */
    fun sendKeyEvent(event: KeyEvent) {
        keyListeners.forEach { listener ->
            listener(event)
        }
    }

    /**
     * 添加一个原生按键事件监听器
     */
    fun registerKeyListener(listener: (KeyEvent) -> Unit) {
        this.keyListeners.add(listener)
    }

    /**
     * 移除一个原生按键事件监听器
     */
    fun unregisterKeyListener(listener: (KeyEvent) -> Unit) {
        this.keyListeners.remove(listener)
    }

    private val eventListeners = mutableListOf<(Event) -> Unit>()

    private val listMMKV = keyMappingListMMKV()
    private val oldMMKV = keyMappingMMKV()

    private val mappingLists = ConcurrentSet<GamepadMappingList>()
    var currentMapping: GamepadMappingList? = null
        private set

    /** 左摇杆状态 */
    private val leftJoystick = Joystick(JoystickType.Left)
    /** 右摇杆状态 */
    private val rightJoystick = Joystick(JoystickType.Right)

    /**
     * 手柄活动状态控制
     */
    var gamepadEngaged by mutableStateOf(false)
        private set

    private var lastActivityTime = System.nanoTime()
    private var pollLevel = PollLevel.Close

    init {
        reloadAllMappings()
    }

    /**
     * 检查并更新手柄是否活动中
     * @return 当前轮询频率等级
     */
    fun checkGamepadActive(): PollLevel {
        val now = System.nanoTime()

        if (
            leftJoystick.isUsing() ||
            rightJoystick.isUsing()
        ) {
            lastActivityTime = now
        }

        pollLevel = if (now - lastActivityTime < 10_000_000_000L) PollLevel.High else PollLevel.Close
        gamepadEngaged = pollLevel != PollLevel.Close

        return pollLevel
    }

    /** 激活状态更新 */
    private fun onActive() {
        val wasInactive = !gamepadEngaged
        lastActivityTime = System.nanoTime()
        if (wasInactive) {
            gamepadEngaged = true
            pollLevel = PollLevel.High
        }
    }

    fun reloadAllMappings() {
        mappingLists.clear()

        var movedOldData = false
        val defaultName = "default"

        if (oldMMKV.count() > 0) {
            val defaultMappings = mutableListOf<GamepadMapping>()
            GamepadMap.entries.forEach { entry ->
                val mapping = oldMMKV.decodeParcelable(entry.identifier, GamepadMapping::class.java)
                    ?: GamepadMapping(
                        key = entry.gamepad,
                        dpadDirection = entry.dpadDirection,
                        targetsInGame = entry.defaultKeysInGame,
                        targetsInMenu = entry.defaultKeysInMenu
                    )
                defaultMappings.add(mapping)
            }
            val list = GamepadMappingList(
                name = defaultName,
                list = defaultMappings
            )
            oldMMKV.clearAll()
            listMMKV.encode(defaultName, list)

            mappingLists.add(list)
            movedOldData = true
            AllSettings.gamepadMappingConfig.save(defaultName)
        }

        if (!movedOldData && listMMKV.count() == 0L) {
            //当前没有任何的配置
            val list = createDefaultMapping(defaultName)
            AllSettings.gamepadMappingConfig.save(defaultName)
            listMMKV.encode(defaultName, list)
            mappingLists.add(list)
        } else {
            listMMKV.allKeys()?.forEach { key ->
                if (movedOldData && defaultName == key) return@forEach
                listMMKV.decodeParcelable(key, GamepadMappingList::class.java)?.let {
                    mappingLists.add(it)
                }
            }
        }

        refreshLists()
    }

    private fun refreshLists() {
        mappingLists.forEach { list ->
            list.load()
        }
        currentMapping = loadCurrentConfig()
    }

    private fun loadCurrentConfig(): GamepadMappingList? {
        val config = AllSettings.gamepadMappingConfig.getValue()
        return mappingLists.find {
            it.name == config
        } ?: mappingLists.firstOrNull()?.also { config ->
            AllSettings.gamepadMappingConfig.save(config.name)
        }
    }

    /**
     * 获取所有的手柄映射配置名称
     */
    fun getAllConfigKeys(): List<String> {
        return mappingLists.map { it.name }
    }

    /**
     * 该名称是否已被保存的配置使用
     */
    fun containsConfig(name: String): Boolean = listMMKV.containsKey(name)

    /**
     * 创建新的手柄映射配置
     */
    fun createNewConfig(
        name: String,
        onContainsConfig: () -> Unit,
        onFinished: () -> Unit = {}
    ) {
        val name0 = name.take(GAMEPAD_CONFIG_NAME_LENGTH)
        if (containsConfig(name0)) onContainsConfig()

        val list = createDefaultMapping(name0)

        mappingLists.add(list)
        listMMKV.encode(name0, list)

        AllSettings.gamepadMappingConfig.save(name0)
        refreshLists()

        onFinished()
    }

    /**
     * 创建一个默认的映射配置
     */
    private fun createDefaultMapping(name: String): GamepadMappingList {
        val defaultMappings = mutableListOf<GamepadMapping>()
        GamepadMap.entries.forEach { entry ->
            defaultMappings.add(
                GamepadMapping(
                    key = entry.gamepad,
                    dpadDirection = entry.dpadDirection,
                    targetsInGame = entry.defaultKeysInGame,
                    targetsInMenu = entry.defaultKeysInMenu
                )
            )
        }
        return GamepadMappingList(
            name = name,
            list = defaultMappings
        )
    }

    /**
     * 删除一个手柄映射配置
     */
    fun deleteConfig(
        name: String,
        onFinished: () -> Unit = {}
    ) {
        mappingLists.removeIf { it.name == name }
        listMMKV.remove(name)
        refreshLists()

        onFinished()
    }

    fun updateButton(code: Int, pressed: Boolean) {
        onActive()
        sendEvent(Event.Button(code, pressed))
    }

    fun updateMotion(axisCode: Int, value: Float) {
        onActive()
        when (axisCode) {
            //更新摇杆状态
            GamepadRemap.MotionX.code -> leftJoystick.updateState(horizontal = value)
            GamepadRemap.MotionY.code -> leftJoystick.updateState(vertical = value)
            GamepadRemap.MotionZ.code -> rightJoystick.updateState(horizontal = value)
            GamepadRemap.MotionRZ.code -> rightJoystick.updateState(vertical = value)
        }

        when (axisCode) {
            //更新左右触发器状态
            GamepadRemap.MotionLeftTrigger.code,
            GamepadRemap.MotionRightTrigger.code -> {
                checkAxisPress(
                    axisCode, value,
                    onEvent = { isPressed ->
                        updateButton(axisCode, isPressed)
                    }
                )
            }

            //更新方向键状态
            GamepadRemap.MotionHatX.code -> {
                updateDpad(DpadDirection.Left, value < -BUTTON_PRESS_THRESHOLD)
                updateDpad(DpadDirection.Right, value > BUTTON_PRESS_THRESHOLD)
            }
            GamepadRemap.MotionHatY.code -> {
                updateDpad(DpadDirection.Up, value < -BUTTON_PRESS_THRESHOLD)
                updateDpad(DpadDirection.Down, value > BUTTON_PRESS_THRESHOLD)
            }
        }
    }

    private val axisStates = SparseBooleanArray()
    private fun checkAxisPress(
        axisCode: Int, value: Float,
        onEvent: (isPressed: Boolean) -> Unit,
        activation: Float = AXIS_ACTIVATION_THRESHOLD,
        reset: Float = AXIS_RESET_THRESHOLD
    ) {
        val isPressed = axisStates[axisCode]

        val press = if (isPressed) {
            value >= reset
        } else {
            value >= activation
        }
        axisStates[axisCode] = press

        if (isPressed != press) {
            onEvent(press)
        }
    }

    private fun updateDpad(direction: DpadDirection, pressed: Boolean) {
        onActive()
        sendEvent(Event.Dpad(direction, pressed))
    }

    /**
     * 轮询调用，持续发送当前拥有的摇杆状态
     */
    fun pollJoystick() {
        leftJoystick.onTick(::sendEvent)
        rightJoystick.onTick(::sendEvent)
    }

    private fun sendEvent(event: Event) {
        eventListeners.forEach { listener ->
            listener(event)
        }
    }

    /**
     * 添加一个事件监听者，在事件发送时立即回调
     */
    fun addEventListener(listener: (Event) -> Unit) {
        eventListeners.add(listener)
    }

    /**
     * 移除已添加的事件监听者
     */
    fun removeEventListener(listener: (Event) -> Unit) {
        eventListeners.remove(listener)
    }

    sealed interface Event {
        /**
         * 手柄按钮按下/松开事件
         * @param code 经过映射转化后的标准按钮键值
         */
        data class Button(val code: Int, val pressed: Boolean) : Event

        /**
         * 手柄摇杆偏移量事件
         * @param joystickType 摇杆类型（左、右）
         */
        data class StickOffset(val joystickType: JoystickType, val offset: Offset) : Event

        /**
         * 手柄摇杆方向变更事件
         * @param joystickType 摇杆类型（左、右）
         */
        data class StickDirection(val joystickType: JoystickType, val direction: JoystickDirection) : Event

        /**
         * 手柄方向键按下/松开事件
         * @param direction 方向
         */
        data class Dpad(val direction: DpadDirection, val pressed: Boolean) : Event
    }

    enum class PollLevel(val delayMs: Long) {
        /**
         * 高轮询等级：16ms延迟 ≈ 60fps
         */
        High(16L),

        /**
         * 不进行轮询
         */
        Close(10_000L)
    }
}