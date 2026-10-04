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

package com.movtery.zalithlauncher.utils.device

import android.app.Activity
import android.content.Context
import android.hardware.input.InputManager
import android.view.InputDevice
import com.movtery.zalithlauncher.utils.logging.Logger

private const val TAG = "PhysicalMouseChecker"

object PhysicalMouseChecker {
    /**
     * 当前是否有实体鼠标连接
     */
    var physicalMouseConnected = false
        private set

    fun initChecker(activity: Activity) {
        //粗检测，因为启动软件前可能已经连接实体鼠标了
        physicalMouseConnected = isPhysicalMouseConnected()
        Logger.info(TAG, "Initialization complete, physical mouse connection status: $physicalMouseConnected")

        val listener = object : InputManager.InputDeviceListener {
            override fun onInputDeviceAdded(deviceId: Int) {
                if (deviceId.isMouseId()) {
                    Logger.info(TAG, "Physical mouse connected, deviceId: $deviceId")
                    physicalMouseConnected = true
                }
            }

            override fun onInputDeviceRemoved(deviceId: Int) {
                if (deviceId.isMouseId()) {
                    Logger.info(TAG, "Physical mouse disconnected, deviceId: $deviceId")
                    physicalMouseConnected = false
                } else {
                    physicalMouseConnected = isPhysicalMouseConnected()
                    Logger.info(TAG, "Fallback check for physical mouse connection status: $physicalMouseConnected")
                }
            }

            override fun onInputDeviceChanged(deviceId: Int) {
                //ignore
            }
        }

        val inputManager = activity.getSystemService(Context.INPUT_SERVICE) as InputManager
        inputManager.registerInputDeviceListener(listener, null)
    }
}

/**
 * 确认这个deviceId是否为实体鼠标
 */
private fun Int.isMouseId(): Boolean {
    return InputDevice.getDevice(this)?.let { device ->
        device.sources and InputDevice.SOURCE_MOUSE == InputDevice.SOURCE_MOUSE
    } ?: false
}

/**
 * 粗略检查是否接入实体鼠标
 */
private fun isPhysicalMouseConnected(): Boolean {
    return InputDevice.getDeviceIds()
        .takeIf { it.isNotEmpty() }
        ?.any { it.isMouseId() }
        ?: false
}