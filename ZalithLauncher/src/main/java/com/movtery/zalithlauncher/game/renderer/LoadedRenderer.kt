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
import com.movtery.zalithlauncher.game.renderer.config.ResolvedRendererConfig
import com.movtery.zalithlauncher.game.renderer.config.VersionRendererConfigStore
import com.movtery.zalithlauncher.game.renderer.config.resolveRendererConfig
import com.movtery.zalithlauncher.game.version.installed.Version
import java.io.File

/**
 * 渲染器与其已解析配置的绑定，版本兼容边界可依赖配置解析结果
 */
class LoadedRenderer(
    val renderer: RendererInterface,
    /** 非 [ConfigurableRenderer] 时为 null */
    val config: ResolvedRendererConfig?,
) {
    /** SFPEW 兼容层开关，渲染器配置规范未声明该项时为 null */
    val sfpewLayerEnabled: Boolean? get() = config?.sfpewLayerEnabled

    fun getMinMCVersion(): String? = renderer.getMinMCVersion(config)

    fun getMaxMCVersion(): String? = renderer.getMaxMCVersion(config)

    fun getDisplayMinMCVersion(): String? = renderer.getDisplayMinMCVersion(config)

    fun getDisplayMaxMCVersion(): String? = renderer.getDisplayMaxMCVersion(config)
}

/**
 * 加载渲染器并解析其配置：全局配置必读，[version] 非空时叠加版本独立覆盖
 */
@WorkerThread
fun loadRenderer(renderer: RendererInterface, version: Version?): LoadedRenderer =
    loadRendererInternal(renderer, version?.getVersionPath())

/**
 * 同 [loadRenderer]，直接以版本目录为配置上下文
 */
@WorkerThread
fun loadRenderer(renderer: RendererInterface, versionPath: File?): LoadedRenderer =
    loadRendererInternal(renderer, versionPath)

private fun loadRendererInternal(renderer: RendererInterface, versionPath: File?): LoadedRenderer {
    if (renderer !is ConfigurableRenderer) return LoadedRenderer(renderer, null)
    return LoadedRenderer(renderer, loadResolvedConfig(renderer, versionPath))
}

/**
 * 迁移旧配置键后，按 全局配置、版本独立覆盖、规范默认值 的优先级解析渲染器配置
 */
@WorkerThread
private fun loadResolvedConfig(renderer: ConfigurableRenderer, versionPath: File?): ResolvedRendererConfig {
    val rendererId = renderer.getUniqueIdentifier()
    GlobalRendererConfigStore.migrateLegacy(rendererId, renderer.legacyConfigKeys)
    val versionStore = versionPath?.let {
        VersionRendererConfigStore(it, Renderers.getLegacyRendererIds())
    }
    return resolveRendererConfig(rendererId, renderer.configSpec, versionStore)
}
