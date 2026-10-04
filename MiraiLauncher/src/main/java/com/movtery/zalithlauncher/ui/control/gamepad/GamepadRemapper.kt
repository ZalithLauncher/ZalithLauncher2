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

import android.os.Parcelable
import android.util.SparseArray
import android.view.InputDevice
import android.view.KeyEvent
import android.view.MotionEvent
import com.movtery.zalithlauncher.setting.AllSettings
import com.movtery.zalithlauncher.utils.logging.Logger
import com.movtery.zalithlauncher.viewmodel.GamepadViewModel
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import kotlin.math.abs
import kotlin.math.hypot
import kotlin.math.max

private const val TAG = "GamepadRemapper"

private const val AXIS_TO_KEY_ACTIVATION_THRESHOLD = 0.6f
private const val AXIS_TO_KEY_RESET_THRESHOLD = 0.4f

/**
 * 当前重映射版本号
 */
private const val REMAPPER_VERSION = 2

/**
 * 手柄事件重映射信息保存类
 * @param motionMapping [MotionEvent]类事件映射
 * @param keyMapping [KeyEvent]类事件映射
 * @param version 当前映射信息版本号，会根据本地版本号进行比较
 */
@Parcelize
data class GamepadRemapper(
    val motionMapping: Map<Int, Int>,
    val keyMapping: Map<Int, Int>,
    val version: Int = 0
): Parcelable {
    constructor(
        motionMapping: Map<Int, Int>,
        keyMapping: Map<Int, Int>
    ): this(motionMapping, keyMapping, REMAPPER_VERSION)

    @IgnoredOnParcel private val reverseMotionMap by lazy {
        motionMapping.entries.associate { (key, value) ->
            value to key
        }
    }

    @IgnoredOnParcel private val currentKeyValues = SparseArray<Float>()
    @IgnoredOnParcel private val currentMotionValues = SparseArray<Float>()

    /**
     * 比较重映射版本号
     * @return 是否过旧
     */
    fun isOldVersion(): Boolean {
        return version < REMAPPER_VERSION
    }

    /**
     * 如果事件是有效的手柄事件，则调用 [GamepadViewModel] 发送事件
     * 注意：如果值没有变化，处理器不会被调用
     *
     * @param event 当前的 MotionEvent
     * @return 输入是否被处理
     */
    fun handleMotionEventInput(event: MotionEvent, gamepadViewModel: GamepadViewModel): Boolean {
        if (!event.isJoystickMoving()) return false

        handleMotionIfDifferent(MotionEvent.AXIS_HAT_X, getRemappedValue(MotionEvent.AXIS_HAT_X, event), gamepadViewModel)
        handleMotionIfDifferent(MotionEvent.AXIS_HAT_Y, getRemappedValue(MotionEvent.AXIS_HAT_Y, event), gamepadViewModel)
        handleMotionIfDifferent(MotionEvent.AXIS_RTRIGGER, getRemappedValue(MotionEvent.AXIS_RTRIGGER, event), gamepadViewModel)
        handleMotionIfDifferent(MotionEvent.AXIS_LTRIGGER, getRemappedValue(MotionEvent.AXIS_LTRIGGER, event), gamepadViewModel)

        handleJoystickInput(event, gamepadViewModel, MotionEvent.AXIS_X, MotionEvent.AXIS_Y)
        handleJoystickInput(event, gamepadViewModel, MotionEvent.AXIS_Z, MotionEvent.AXIS_RZ)
        return true
    }

    /**
     * 如果事件是有效的手柄按键事件，则调用 [GamepadViewModel] 发送事件
     *
     * @param event 当前的 KeyEvent
     * @return 输入是否被处理
     */
    fun handleKeyEventInput(
        event: KeyEvent,
        gamepadViewModel: GamepadViewModel
    ): Boolean {
        if (!event.isGamepadKeyEvent()) return false
        if (event.keyCode == KeyEvent.KEYCODE_UNKNOWN) return false
        if (event.repeatCount > 0) return false

        val mappedSource = getRemappedSource(event)
        val currentValue = getRemappedValue(mappedSource, event)
        val lastValue = currentKeyValues[mappedSource]

        if (lastValue == null || currentValue != lastValue) {
            currentValue?.let { value ->
                gamepadViewModel.updateButton(mappedSource, value > 0f)
            }
            currentKeyValues[mappedSource] = currentValue
        }
        return true
    }

    private fun handleJoystickInput(
        event: MotionEvent,
        gamepadViewModel: GamepadViewModel,
        horizontalAxis: Int,
        verticalAxis: Int
    ) {
        var x = getRemappedValue(horizontalAxis, event)
        var y = getRemappedValue(verticalAxis, event)

        val magnitude = getMagnitude(x, y)
        val deadzone = getDeadzone(event, getRemappedSource(horizontalAxis))

        if (magnitude < deadzone) {
            x = 0f
            y = 0f
        } else {
            //对死区进行补偿
            x = ((x / magnitude) * ((magnitude - deadzone) / (1 - deadzone))).toFloat()
            y = ((y / magnitude) * ((magnitude - deadzone) / (1 - deadzone))).toFloat()
        }

        handleMotionIfDifferent(horizontalAxis, x, gamepadViewModel)
        handleMotionIfDifferent(verticalAxis, y, gamepadViewModel)
    }

    private fun handleMotionIfDifferent(
        mappedSource: Int,
        value: Float,
        gamepadViewModel: GamepadViewModel
    ) {
        val lastValue = currentMotionValues[mappedSource]
        if (lastValue == null || lastValue != value) {
            gamepadViewModel.updateMotion(mappedSource, value)
            currentMotionValues[mappedSource] = value
        }
    }

    /**
     * 获取两个坐标点 (0,0) 与 (|x|,|y|) 之间的距离（即向量的模）
     */
    private fun getMagnitude(x: Float, y: Float): Double {
        val dx = abs(x)
        val dy = abs(y)
        return hypot(dx.toDouble(), dy.toDouble())
    }

    private fun getDeadzone(event: MotionEvent, axis: Int): Float {
        return try {
            val range = event.device?.getMotionRange(axis, InputDevice.SOURCE_JOYSTICK)
            val deadzoneScale = AllSettings.gamepadDeadZoneScale.state / 100f
            val deadzone = (range?.flat ?: 0f) * deadzoneScale
            max(deadzone, 0.1f * deadzoneScale)
        } catch (e: Exception) {
            Logger.error(TAG, "Dynamic Deadzone is not supported", e)
            0.2f
        }
    }

    private fun getRemappedSource(axisSource: Int): Int {
        return reverseMotionMap[axisSource] ?: axisSource
    }

    private fun getRemappedValue(originalSource: Int, motionEvent: MotionEvent): Float {
        val mappedSource = getRemappedSource(originalSource)

        return if (supportedAxis.any { it == mappedSource }) {
            motionEvent.getAxisValue(mappedSource)
        } else {
            // 否则，将其转换为按键事件
            // 假设只有一个按钮被映射到最终值
            // 因为事件被转换回“KeyEvent”，所以取值为 0 或 1
            val isEnabled = (currentMotionValues[originalSource] ?: 0.0f) == 1.0f
            val absoluteValue = abs(motionEvent.getAxisValue(mappedSource))

            if (isEnabled) {
                if (absoluteValue >= AXIS_TO_KEY_RESET_THRESHOLD) 1f else 0f
            } else {
                if (absoluteValue >= AXIS_TO_KEY_ACTIVATION_THRESHOLD) 1f else 0f
            }
        }
    }

    /**
     * 将按键码（keycode）转换为对应的轴（axis）
     * 这是因为某些轴和方向键（DPAD）的按键码在功能上是相同的
     */
    private fun transformKeyEventInput(keycode: Int): Int {
        return when (keycode) {
            KeyEvent.KEYCODE_BUTTON_L2 -> MotionEvent.AXIS_LTRIGGER
            KeyEvent.KEYCODE_BUTTON_R2 -> MotionEvent.AXIS_RTRIGGER
            KeyEvent.KEYCODE_DPAD_UP, KeyEvent.KEYCODE_DPAD_DOWN -> MotionEvent.AXIS_HAT_Y
            KeyEvent.KEYCODE_DPAD_LEFT, KeyEvent.KEYCODE_DPAD_RIGHT -> MotionEvent.AXIS_HAT_X
            else -> keycode
        }
    }

    private fun getRemappedSource(event: KeyEvent): Int {
        val translatedSource = transformKeyEventInput(event.keyCode)
        return keyMapping[translatedSource] ?: translatedSource
    }

    private fun getRemappedValue(mappedSource: Int, keyEvent: KeyEvent): Float? {
        //DPAD 和触发器特殊处理，永远映射为null
        val isDpad = (mappedSource == MotionEvent.AXIS_HAT_Y && keyEvent.keyCode == KeyEvent.KEYCODE_DPAD_UP) ||
                (mappedSource == MotionEvent.AXIS_HAT_X && keyEvent.keyCode == KeyEvent.KEYCODE_DPAD_LEFT)
        val isTrigger = (mappedSource == MotionEvent.AXIS_LTRIGGER || keyEvent.keyCode == KeyEvent.KEYCODE_BUTTON_L2) ||
                (mappedSource == MotionEvent.AXIS_RTRIGGER || keyEvent.keyCode == KeyEvent.KEYCODE_BUTTON_R2)
        if (isDpad || isTrigger) return null

        return when (keyEvent.action) {
            KeyEvent.ACTION_DOWN, KeyEvent.ACTION_MULTIPLE -> 1f
            else -> 0f
        }
    }
}
