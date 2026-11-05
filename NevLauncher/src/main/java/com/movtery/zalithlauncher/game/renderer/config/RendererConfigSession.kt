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
 * 配置对话框的编辑会话，管理一个渲染器的全部配置项状态及整体重置；
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

    init {
        reload()
    }

    fun saveValue(newValue: ConfigScalar) {
        val coerced = entry.coerce(newValue)
        localStore.putValue(rendererId, entry.id, coerced)
        value = coerced
        updateOverride()
    }

    fun saveEnabled(newValue: Boolean) {
        if (newValue && entry is OrderedChoiceEntry) {
            reload()
            localStore.putValue(rendererId, entry.id, value)
        }
        localStore.putEnabled(rendererId, entry.id, newValue)
        enabled = newValue
        updateOverride()
    }

    fun reset() {
        localStore.removeEntry(rendererId, entry.id)
        reload()
    }

    fun reload() {
        val state = resolveEntry(rendererId, entry, localStore, globalStore, inheritedValues())
        value = state.value
        enabled = state.enabled
        updateOverride()
    }

    private fun updateOverride() {
        isOverridden = localStore.getValue(rendererId, entry.id) != null ||
                localStore.getEnabled(rendererId, entry.id) != null
    }
}
