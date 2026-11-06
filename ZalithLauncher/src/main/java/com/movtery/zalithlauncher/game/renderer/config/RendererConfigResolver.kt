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

data class ResolvedConfigEntry(
    val entry: ConfigEntry,
    val value: ConfigScalar,
    val enabled: Boolean?,
)

/**
 * 一次启动所需的完整配置解析结果
 */
data class ResolvedRendererConfig(
    val fixedEnv: Map<String, String>,
    val entries: List<ResolvedConfigEntry>
) {
    /** 统一生成环境变量 */
    val environment: Map<String, String> = buildMap {
        putAll(fixedEnv)
        this@ResolvedRendererConfig.entries.forEach { resolved ->
            val target = resolved.entry.target as? ConfigTarget.Environment ?: return@forEach
            when (val entry = resolved.entry) {
                is SwitchEntry -> {
                    if ((resolved.value as? ConfigScalar.BooleanValue)?.value == true) {
                        put(target.key, entry.enabledValue.asString())
                    }
                }
                is SingleChoiceEntry -> {
                    if (resolved.enabled != false) put(target.key, resolved.value.asString())
                }
                is OrderedChoiceEntry -> {
                    if (resolved.enabled != false) put(target.key, resolved.value.asString())
                }
                is NumberEntry -> effectiveNumberValue(entry, resolved.value)?.let {
                    put(target.key, it.asString())
                }
                is TextEntry -> {
                    val value = resolved.value.asString()
                    if (value.isNotEmpty()) put(target.key, value)
                }
            }
        }
    }

    /** SFPEW 兼容层开关，渲染器规范未声明该项时为 null */
    val sfpewLayerEnabled: Boolean? = entries
        .firstOrNull { it.entry.target is ConfigTarget.SfpewLayer }
        ?.let { (it.value as? ConfigScalar.BooleanValue)?.value }

    /** 显式 JSON 字段 */
    val jsonFields: Map<String, ConfigScalar> = buildMap {
        this@ResolvedRendererConfig.entries.forEach { resolved ->
            val target = resolved.entry.target as? ConfigTarget.JsonField ?: return@forEach
            when (val entry = resolved.entry) {
                is SwitchEntry -> {
                    val enabled = (resolved.value as? ConfigScalar.BooleanValue)?.value == true
                    put(target.key, ConfigScalar.IntValue(if (enabled) 1 else 0))
                }
                is SingleChoiceEntry -> {
                    if (resolved.enabled != false) put(target.key, resolved.value)
                }
                is OrderedChoiceEntry -> {
                    if (resolved.enabled != false) put(target.key, resolved.value)
                }
                is NumberEntry -> {
                    effectiveNumberValue(entry, resolved.value)?.let { put(target.key, it) }
                }
                is TextEntry -> {
                    put(target.key, resolved.value)
                }
            }
        }
    }
}

/**
 * 单个配置项的值解析结果
 */
data class ResolvedEntryState(
    val value: ConfigScalar,
    val enabled: Boolean?,
)

/**
 * 按本地覆盖、全局配置、规范默认值 的优先级解析单个配置项；
 * 全局存储与本地存储相同时不再重复读取
 */
fun resolveEntry(
    rendererId: String,
    entry: ConfigEntry,
    localStore: RendererConfigStore?,
    globalStore: RendererConfigStore? = null,
    inheritedValues: Map<String, ConfigScalar> = emptyMap()
): ResolvedEntryState {
    val localValue = localStore?.getValue(rendererId, entry.id)
    val globalValue = globalStore?.takeIf { it !== localStore }?.getValue(rendererId, entry.id)
    val localEnabled = localStore?.getEnabled(rendererId, entry.id)
    val globalEnabled = globalStore?.takeIf { it !== localStore }?.getEnabled(rendererId, entry.id)
    val enabled = if (entry.defaultEnabled != null) {
        localEnabled ?: globalEnabled ?: entry.defaultEnabled
    } else null
    val orderedEntry = entry as? OrderedChoiceEntry
    val defaultValue = orderedEntry?.defaultValue(inheritedValues[orderedEntry.inheritFrom])
        ?: entry.defaultValue
    return ResolvedEntryState(
        value = entry.coerce(localValue ?: globalValue ?: defaultValue),
        enabled = enabled,
    )
}

/**
 * 按版本显式覆盖、全局显式配置、规范默认值 的优先级解析启动配置
 */
fun resolveRendererConfig(
    rendererId: String,
    spec: RendererConfigSpec,
    versionStore: VersionRendererConfigStore? = null,
    globalStore: RendererConfigStore = GlobalRendererConfigStore
): ResolvedRendererConfig {
    val inheritedValues = mutableMapOf<String, ConfigScalar>()
    return ResolvedRendererConfig(
        fixedEnv = spec.fixedEnv,
        entries = spec.entries.map { entry ->
            val state = resolveEntry(rendererId, entry, versionStore, globalStore, inheritedValues)
            inheritedValues[entry.id] = state.value
            ResolvedConfigEntry(
                entry = entry,
                value = state.value,
                enabled = state.enabled,
            )
        }
    )
}

private fun effectiveNumberValue(entry: NumberEntry, value: ConfigScalar): ConfigScalar? =
    if ((value as? ConfigScalar.StringValue)?.value?.isEmpty() == true) entry.emptyValue else value
