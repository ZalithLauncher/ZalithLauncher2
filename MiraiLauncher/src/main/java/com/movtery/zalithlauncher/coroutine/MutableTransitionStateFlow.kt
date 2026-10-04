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

package com.movtery.zalithlauncher.coroutine

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map

/**
 * 一个扩展型的状态容器，用于在状态更新时同时提供旧值与新值
 * @param initial 初始状态，允许为 `null`，以便在初始化阶段逐步构建状态
 */
class MutableTransitionStateFlow<T>(initial: T?) {
    /**
     * 表示一次状态变化（旧值 → 新值）
     */
    data class Transition<T>(
        val old: T?,
        val new: T
    )

    private val internal = MutableStateFlow(initial)
    private var lastValue: T? = initial

    val value
        get () = internal.value

    /**
     * 面向 UI 层暴露的状态流
     */
    val stateFlow: StateFlow<T?> = internal.asStateFlow()

    /**
     * 状态变化流，包含旧值与新值，
     * 每当调用 [set] 时，都会向该流发送一个 [Transition] 对象
     */
    val changes: Flow<Transition<T>> = internal
        .filterNotNull()
        .map { new ->
            val transition = Transition(lastValue, new)
            lastValue = new
            transition
        }

    /**
     * 设置新的状态值
     * @param value 新的状态值
     */
    fun set(value: T) {
        internal.value = value
    }
}