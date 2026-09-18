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

package com.movtery.zalithlauncher.game.plugin.renderer

import com.movtery.zalithlauncher.game.plugin.Plugin
import com.movtery.zalithlauncher.game.renderer.RendererInterface

class RendererPlugin(
    val packageName: String,
    val id: String,
    val displayName: String,
    val summary: String? = null,
    val minMCVer: String? = null,
    val maxMCVer: String? = null,
    private val glName: String,
    private val eglName: String,
    val path: String,
    private val env: Map<String, String>,
    private val dlopen: List<String>,
    val isConfigurable: Boolean = false,
): RendererInterface, Plugin {
    override fun getIdentifier(): String = packageName
    override fun getNativeLibPath(): String = path

    override fun getRendererId(): String = id
    override fun getUniqueIdentifier(): String = packageName
    override fun getRendererName(): String = displayName
    override fun getRendererSummary(): String? = summary
    override fun getMinMCVersion(): String? = minMCVer
    override fun getMaxMCVersion(): String? = maxMCVer
    override fun getRendererEnv(): Lazy<Map<String, String>> = lazy { env }
    override fun getDlopenLibrary(): Lazy<List<String>> = lazy {
        dlopen.map { lib -> "$path/$lib" }
    }
    override fun getRendererLibrary(): String = "$path/$glName"
    override fun getRendererEGL(): String = eglName
}
