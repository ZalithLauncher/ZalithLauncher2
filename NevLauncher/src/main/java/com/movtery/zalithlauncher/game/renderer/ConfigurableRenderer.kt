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

package com.movtery.zalithlauncher.game.renderer

import androidx.annotation.WorkerThread
import com.movtery.zalithlauncher.game.renderer.config.GlobalRendererConfigStore
import com.movtery.zalithlauncher.game.renderer.config.RendererConfigSpec
import com.movtery.zalithlauncher.game.renderer.config.ResolvedRendererConfig
import com.movtery.zalithlauncher.game.renderer.config.VersionRendererConfigStore
import com.movtery.zalithlauncher.game.renderer.config.removeStaleEntries
import com.movtery.zalithlauncher.game.renderer.config.resolveRendererConfig
import com.movtery.zalithlauncher.game.version.installed.Version

/**
 * 提供独立配置规范的渲染器
 */
interface ConfigurableRenderer : RendererInterface {
    val configSpec: RendererConfigSpec

    val legacyConfigKeys: List<String> get() = listOf(getUniqueIdentifier())

    @WorkerThread
    fun getConfiguredEnv(config: ResolvedRendererConfig): Map<String, String> = config.environment

    override fun getRendererEnv(): Lazy<Map<String, String>> = lazy {
        getConfiguredEnv(resolveRendererConfig(getUniqueIdentifier(), configSpec))
    }
}

/**
 * 一次启动的渲染器配置
 */
data class RendererLaunchConfig(
    /** 的环境变量 */
    val environment: Map<String, String>,
    /** SFPEW 兼容层开关，渲染器配置未声明时为 null */
    val sfpewLayerEnabled: Boolean?
)

@WorkerThread
fun prepareRendererLaunchConfig(renderer: ConfigurableRenderer, version: Version): RendererLaunchConfig {
    val rendererId = renderer.getUniqueIdentifier()
    GlobalRendererConfigStore.migrateLegacy(rendererId, renderer.legacyConfigKeys)
    GlobalRendererConfigStore.removeStaleEntries(rendererId, renderer.configSpec)
    val versionStore = if (version.getVersionConfig().renderer == rendererId) {
        VersionRendererConfigStore(version.getVersionPath(), Renderers.getLegacyRendererIds())
            .apply { removeStaleEntries(rendererId, renderer.configSpec) }
    } else null
    val config = resolveRendererConfig(rendererId, renderer.configSpec, versionStore)
    return RendererLaunchConfig(
        environment = renderer.getConfiguredEnv(config),
        sfpewLayerEnabled = config.sfpewLayerEnabled
    )
}