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

package com.movtery.zalithlauncher.game.plugin.renderer_v2.data

/**
 * 渲染器配置项状态、存储
 * @param packageName 插件包名，用于 MMKV 存储命名空间隔离
 * @param envs 渲染器环境变量配置项
 * @param genSummary 将 [RendererConfig.MetaString] 转换为本地化文本
 */
class RendererEnv(
    val packageName: String,
    val envs: List<RendererConfig.Env>,
    genSummary: (metaString: String) -> String?,
) {
    private val settingUnits: Map<String, EnvSettingUnit>

    init {
        val mmkv = rendererEnvMMKV()
        val prefix = "$packageName:"

        // 收集所有可配置环境变量键（Selectable / Customizable / Toggleable）
        val currentConfigurableKeys = envs.mapNotNull { env ->
            when (env) {
                is RendererConfig.Env.NormalEnv -> null
                is RendererConfig.Env.SelectableEnv -> env.key
                is RendererConfig.Env.CustomizableEnv -> env.key
                is RendererConfig.Env.ToggleableEnv -> env.key
            }
        }.toSet()

        // 清理插件更新后不再受支持的环境变量
        mmkv.allKeys()
            ?.filter { it.startsWith(prefix) }
            ?.forEach { storedKey ->
                val envKey = storedKey.removePrefix(prefix)
                val baseKey = envKey.removeSuffix(":check")
                if (baseKey !in currentConfigurableKeys) {
                    mmkv.remove(storedKey)
                }
        }

        // 为每个可配置环境变量创建设置单元
        val units = mutableMapOf<String, EnvSettingUnit>()
        for (env in envs) {
            when (env) {
                is RendererConfig.Env.NormalEnv -> {}

                is RendererConfig.Env.SelectableEnv -> {
                    val mmkvKey = "$prefix${env.key}"
                    val summary = env.getTitleMetaString()?.let { genSummary(it) }
                    val unit = EnvSettingUnit.Selectable(
                        mmkvKey = mmkvKey,
                        rawEnv = env,
                        defaultValue = env.items.defaultValue,
                        values = buildList {
                            add(env.items.defaultValue)
                            addAll(env.items.values)
                        },
                        summary = summary
                    )
                    unit.init()
                    unit.initCheck()

                    // 校验已保存的值是否仍在当前可选值列表中，不在则重置为默认值
                    if (unit.state !in env.items.values) {
                        unit.save(env.items.defaultValue)
                    }

                    units[env.key] = unit
                }

                is RendererConfig.Env.CustomizableEnv -> {
                    val mmkvKey = "$prefix${env.key}"
                    val summary = env.getTitleMetaString()?.let { genSummary(it) }
                    val default = env.defaultValue ?: ""
                    val unit = EnvSettingUnit.Customizable(
                        mmkvKey = mmkvKey,
                        rawEnv = env,
                        defaultValue = default,
                        summary = summary
                    )
                    unit.init()
                    units[env.key] = unit
                }

                is RendererConfig.Env.ToggleableEnv -> {
                    val mmkvKey = "$prefix${env.key}"
                    val summary = env.getTitleMetaString()?.let { genSummary(it) }
                    val default = if (env.toggle) env.value else ""
                    val unit = EnvSettingUnit.Toggleable(
                        mmkvKey = mmkvKey,
                        rawEnv = env,
                        defaultValue = default,
                        envValue = env.value,
                        summary = summary
                    )
                    unit.init()
                    units[env.key] = unit
                }
            }
        }
        settingUnits = units
    }

    /**
     * 获取该渲染器当前的环境变量配置
     */
    fun getEnv(): Map<String, String> {
        val result = mutableMapOf<String, String>()
        for (env in envs) {
            when (env) {
                is RendererConfig.Env.NormalEnv -> {
                    result[env.key] = env.value
                }
                is RendererConfig.Env.SelectableEnv -> {
                    val unit = settingUnits[env.key] as? EnvSettingUnit.Selectable
                    if (unit != null && unit.isEnabled) {
                        result[env.key] = unit.state
                    }
                }
                is RendererConfig.Env.CustomizableEnv -> {
                    val unit = settingUnits[env.key] as? EnvSettingUnit.Customizable
                    if (unit != null && unit.state.isNotEmpty()) {
                        result[env.key] = unit.state
                    }
                }
                is RendererConfig.Env.ToggleableEnv -> {
                    val unit = settingUnits[env.key] as? EnvSettingUnit.Toggleable
                    if (unit != null && unit.isEnabled) {
                        result[env.key] = unit.envValue
                    }
                }
            }
        }
        return result
    }

    /**
     * 获取所有可配置环境变量的设置单元列表
     */
    fun getConfigurableUnits(): List<EnvSettingUnit> = settingUnits.values.toList()

    /**
     * 从 [RendererConfig.Env] 中提取 [RendererConfig.MetaString] 的 key
     */
    private fun RendererConfig.Env.getTitleMetaString(): String? {
        return when (this) {
            is RendererConfig.Env.NormalEnv -> null
            is RendererConfig.Env.SelectableEnv -> title?.key
            is RendererConfig.Env.CustomizableEnv -> title?.key
            is RendererConfig.Env.ToggleableEnv -> title?.key
        }
    }
}
