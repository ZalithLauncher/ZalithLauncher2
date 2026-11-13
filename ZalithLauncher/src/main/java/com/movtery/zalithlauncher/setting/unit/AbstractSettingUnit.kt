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

package com.movtery.zalithlauncher.setting.unit

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf

abstract class AbstractSettingUnit<V>(
    val key: String,
    val defaultValue: V
) {
    private val _state: Lazy<MutableState<V>> = lazy { mutableStateOf(readValue()) }

    /**
     * 状态是否已从存储中实际读取过
     */
    val isMaterialized: Boolean
        get() = _state.isInitialized()

    /**
     * 可观察的状态
     */
    var state: V
        get() = _state.value.value
        protected set(value) {
            _state.value.value = value
        }

    /**
     * @return 从存储中读取当前的设置值
     */
    protected abstract fun readValue(): V

    /**
     * 保存设置值
     */
    protected abstract fun saveValue(v: V): V

    /**
     * 刷新处理：从存储中重新读取实际值同步到状态
     */
    fun init() {
        getValue()
    }

    /**
     * @return 每次都从存储中直读当前的设置值，并同步到状态
     */
    fun getValue(): V {
        if (!_state.isInitialized()) {
            return _state.value.value
        }
        val state = _state.value
        state.value = readValue()
        return state.value
    }

    /**
     * 保存当前状态值
     */
    fun save() {
        saveValue(_state.value.value)
    }

    /**
     * 存入值
     */
    fun save(value: V) {
        this.state = saveValue(value)
    }

    /**
     * **仅更新状态**，不保存值
     */
    open fun updateState(value: V) {
        this.state = value
    }

    /**
     * 重置当前设置单元为默认值
     */
    fun reset() {
        this.state = saveValue(defaultValue)
    }
}
