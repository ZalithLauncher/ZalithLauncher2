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

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.movtery.zalithlauncher.setting.unit.AbstractSettingUnit

/**
 * 渲染器可配置环境变量的设置单元（sealed）
 * @param summary 该配置项的描述文本，由插件提供
 */
sealed class EnvSettingUnit(
    mmkvKey: String,
    defaultValue: String,
    val summary: String?,
) : AbstractSettingUnit<String>(mmkvKey, defaultValue) {

    override fun getValue(): String {
        return rendererEnvMMKV().getString(key, defaultValue)!!
            .also { state = it }
    }

    override fun saveValue(v: String): String {
        rendererEnvMMKV().putString(key, v).apply()
        return v
    }

    /**
     * 选项式环境变量：从预设列表中选择一个值
     * @param rawEnv 原始环境变量配置
     * @param values 所有可选值（含默认值）
     */
    class Selectable(
        mmkvKey: String,
        val rawEnv: RendererConfig.Env.SelectableEnv,
        defaultValue: String,
        val values: List<String>,
        summary: String? = null,
    ) : EnvSettingUnit(mmkvKey, defaultValue, summary) {
        private val checkKey = "${mmkvKey}:check"

        /**
         * 当前是否启用此环境变量
         */
        var isEnabled by mutableStateOf(rawEnv.check != false)
            private set

        fun initCheck() {
            val mmkv = rendererEnvMMKV()
            val pluginDefault = rawEnv.check != false

            if (rawEnv.check == null) {
                isEnabled = true
            } else if (mmkv.containsKey(checkKey)) {
                isEnabled = mmkv.getBoolean(checkKey, pluginDefault)
            } else {
                isEnabled = pluginDefault
            }
        }

        fun saveCheck(enabled: Boolean) {
            isEnabled = enabled
            rendererEnvMMKV().putBoolean(checkKey, enabled).apply()
        }
    }

    /**
     * 自由填写式环境变量：用户可自行输入任意值
     * @param rawEnv 原始环境变量配置
     */
    class Customizable(
        mmkvKey: String,
        val rawEnv: RendererConfig.Env.CustomizableEnv,
        defaultValue: String,
        summary: String? = null,
    ) : EnvSettingUnit(mmkvKey, defaultValue, summary)

    /**
     * 开关式环境变量：启用/禁用该环境变量
     * 启用时环境变量值为 [RendererConfig.Env.ToggleableEnv.value]，禁用时不设置
     * @param rawEnv 原始环境变量配置
     * @param envValue 该环境变量的实际值（toggle=true 时使用）
     */
    class Toggleable(
        mmkvKey: String,
        val rawEnv: RendererConfig.Env.ToggleableEnv,
        defaultValue: String,
        val envValue: String,
        summary: String? = null,
    ) : EnvSettingUnit(mmkvKey, defaultValue, summary) {
        /** 当前开关是否启用 */
        val isEnabled: Boolean get() = state.isNotEmpty()
    }
}
