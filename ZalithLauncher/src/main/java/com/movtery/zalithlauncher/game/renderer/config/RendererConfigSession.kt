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

package com.movtery.zalithlauncher.game.renderer.config

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * 配置对话框的编辑会话，管理一个渲染器的全部配置项状态、延迟保存及整体重置；
 * 构建时清理存储中已不在规范内的失效配置项
 */
class RendererConfigSession(
    private val rendererId: String,
    spec: RendererConfigSpec,
    private val localStore: RendererConfigStore,
    private val globalStore: RendererConfigStore = GlobalRendererConfigStore
) {
    init {
        localStore.removeStaleEntries(rendererId, spec)
        if (globalStore !== localStore) globalStore.removeStaleEntries(rendererId, spec)
    }

    val states: List<ConfigEntryState> = mutableListOf<ConfigEntryState>().also { states ->
        spec.entries.forEach { entry ->
            states += ConfigEntryState(rendererId, entry, localStore, globalStore) {
                states.associate { it.entry.id to it.value }
            }
        }
    }

    val hasOverrides: Boolean get() = states.any { it.isOverridden }

    fun isModified(): Boolean = states.any { it.isModified() }

    fun save() {
        states.forEach { it.save() }
    }

    fun resetAll() {
        localStore.removeRenderer(rendererId)
        states.forEach { it.reload() }
    }
}

/**
 * 单个配置项的可观察状态，按 本地覆盖、全局配置、规范默认值 解析当前值
 */
class ConfigEntryState(
    private val rendererId: String,
    val entry: ConfigEntry,
    private val localStore: RendererConfigStore,
    private val globalStore: RendererConfigStore,
    private val inheritedValues: () -> Map<String, ConfigScalar> = { emptyMap() }
) {
    var value by mutableStateOf(entry.defaultValue)
        private set
    var enabled by mutableStateOf(entry.defaultEnabled)
        private set
    var isOverridden by mutableStateOf(false)
        private set

    private var storedValue: ConfigScalar? = null
    private var storedEnabled: Boolean? = null
    private var valueModified = false
    private var pendingEnabled: Boolean? = null

    init {
        reload()
    }

    fun saveValue(newValue: ConfigScalar) {
        value = entry.coerce(newValue)
        valueModified = true
        isOverridden = true
    }

    fun saveEnabled(newValue: Boolean) {
        if (newValue && entry is OrderedChoiceEntry) {
            if (!valueModified) reload()
            valueModified = true
        }
        enabled = newValue
        pendingEnabled = newValue
        isOverridden = true
    }

    fun isModified(): Boolean =
        (valueModified && storedValue != value) ||
                (pendingEnabled != null && storedEnabled != pendingEnabled)

    fun save() {
        if (valueModified && storedValue != value) {
            localStore.putValue(rendererId, entry.id, value)
            storedValue = value
        }
        val newEnabled = pendingEnabled
        if (newEnabled != null && storedEnabled != newEnabled) {
            localStore.putEnabled(rendererId, entry.id, newEnabled)
            storedEnabled = newEnabled
        }
        valueModified = false
        pendingEnabled = null
        updateOverride()
    }

    fun reset() {
        localStore.removeEntry(rendererId, entry.id)
        reload()
    }

    fun reload() {
        storedValue = localStore.getValue(rendererId, entry.id)
        storedEnabled = localStore.getEnabled(rendererId, entry.id)
        val state = resolveEntry(rendererId, entry, localStore, globalStore, inheritedValues())
        value = state.value
        enabled = state.enabled
        valueModified = false
        pendingEnabled = null
        updateOverride()
    }

    private fun updateOverride() {
        isOverridden = storedValue != null || storedEnabled != null
    }
}
