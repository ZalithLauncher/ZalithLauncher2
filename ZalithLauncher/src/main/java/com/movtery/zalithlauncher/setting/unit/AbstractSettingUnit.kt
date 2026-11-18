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

import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

abstract class AbstractSettingUnit<V>(
    val key: String,
    val defaultValue: V
) {
    private val _flow: Lazy<MutableStateFlow<V>> = lazy {
        MutableStateFlow(runCatching { readValue() }.getOrDefault(defaultValue))
    }

    /**
     * 状态是否已从存储中实际读取过
     */
    val isMaterialized: Boolean
        get() = _flow.isInitialized()

    /**
     * 可观察的状态流
     */
    val flow: StateFlow<V>
        get() = _flow.value

    /**
     * 可观察的状态
     */
    val state: V
        @Composable get() = _flow.value.collectAsStateWithLifecycle().value

    private fun setStateInternal(value: V) {
        _flow.value.value = value
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
        if (!_flow.isInitialized()) {
            return _flow.value.value
        }
        val flow = _flow.value
        flow.value = readValue()
        return flow.value
    }

    /**
     * 保存当前状态值
     */
    fun save() {
        saveValue(_flow.value.value)
    }

    /**
     * 存入值
     */
    fun save(value: V) {
        setStateInternal(saveValue(value))
    }

    /**
     * **仅更新状态**，不保存值
     */
    open fun updateState(value: V) {
        setStateInternal(value)
    }

    /**
     * 重置当前设置单元为默认值
     */
    fun reset() {
        setStateInternal(saveValue(defaultValue))
    }
}
