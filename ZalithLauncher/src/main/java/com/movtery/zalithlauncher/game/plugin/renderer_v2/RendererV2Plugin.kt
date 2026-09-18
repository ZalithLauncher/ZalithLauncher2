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

package com.movtery.zalithlauncher.game.plugin.renderer_v2

import com.movtery.zalithlauncher.game.plugin.Plugin
import com.movtery.zalithlauncher.game.plugin.renderer_v2.data.RendererConfig
import com.movtery.zalithlauncher.game.plugin.renderer_v2.data.RendererEnv
import com.movtery.zalithlauncher.game.renderer.RendererInterface

/**
 * V2 渲染器插件项
 * @param packageName 插件包名
 * @param nativePath 插件的原生库路径
 * @param renderer 外部插件导入的渲染器配置
 */
class RendererV2Data(
    val packageName: String,
    val nativePath: String,
    val summary: String,
    val renderer: RendererConfig,
    genSummary: (metaString: String) -> String?,
): RendererInterface, Plugin {
    val env = RendererEnv(
        packageName = packageName,
        envs = renderer.env,
        genSummary = genSummary,
    )

    override fun getIdentifier(): String = packageName
    override fun getNativeLibPath(): String = nativePath

    override fun getRendererId(): String = renderer.rendererId
    override fun getUniqueIdentifier(): String = packageName
    override fun getRendererName(): String = renderer.displayName
    override fun getRendererSummary(): String = summary
    override fun getMinMCVersion(): String? = renderer.minMCVer
    override fun getMaxMCVersion(): String? = renderer.maxMCVer
    override fun getRendererEnv(): Lazy<Map<String, String>> = lazy { env.getEnv() }
    override fun getDlopenLibrary(): Lazy<List<String>> = lazy { renderer.dlopenLibPaths }
    override fun getRendererLibrary(): String = renderer.rendererGLPath
    override fun getRendererEGL(): String = renderer.rendererEGLPath
}