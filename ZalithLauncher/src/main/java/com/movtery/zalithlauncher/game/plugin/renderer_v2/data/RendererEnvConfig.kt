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

package com.movtery.zalithlauncher.game.plugin.renderer_v2.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 旧版渲染器环境变量配置，仅用于迁移版本文件夹下的 renderer.config
 */
@Serializable
data class RendererEnvConfig(
    /** 各渲染器插件的环境变量配置，键为插件包名 */
    @SerialName("renderers")
    val renderers: MutableMap<String, RendererEnvs> = mutableMapOf()
) {
    companion object {
        const val CONFIG_FILE_NAME = "renderer.config"
    }
}

/**
 * 单个渲染器插件的环境变量配置
 */
@Serializable
data class RendererEnvs(
    /** 环境变量取值，键为环境变量名 */
    @SerialName("envs")
    val envs: MutableMap<String, String> = mutableMapOf(),
    /** 环境变量的启用状态，键为环境变量名 */
    @SerialName("checks")
    val checks: MutableMap<String, Boolean> = mutableMapOf()
)