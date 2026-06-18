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

import androidx.lifecycle.ViewModel
import com.movtery.layer_controller.layout.JoystickDirection
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * 摇杆、方向键组件处理 ViewModel
 */
@HiltViewModel
class JoystickMovementViewModel @Inject constructor() : ViewModel() {
    private val listeners = mutableListOf<(JoystickDirection) -> Unit>()

    /**
     * 注册组件方向处理监听器
     */
    fun registerListener(listener: (JoystickDirection) -> Unit) {
        this.listeners.add(listener)
    }

    /**
     * 移除组件方向处理监听器
     */
    fun unregisterListener(listener: (JoystickDirection) -> Unit) {
        this.listeners.remove(listener)
    }

    /**
     * 处理方向
     */
    fun onListen(direction: JoystickDirection) {
        this.listeners.forEach { listener ->
            listener(direction)
        }
    }
}