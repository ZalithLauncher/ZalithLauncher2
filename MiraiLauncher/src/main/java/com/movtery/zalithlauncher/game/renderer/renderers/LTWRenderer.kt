/*
 * Zalith Launcher 2
 * Copyright (C) 2025 MovTery <movtery228@qq.com> and contributors
 * Copyright (C) 2026 Aerix Launcher contributors.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/gpl-3.0.txt>.
 */

package com.movtery.zalithlauncher.game.renderer.renderers

import com.movtery.zalithlauncher.game.renderer.RendererInterface

/**
 * Large Thin Wrapper, bundled from MojoLauncher/LTW as an Android library.
 * The upstream renderer implements an incomplete OpenGL 3.2 core profile on
 * top of OpenGL ES; compatibility depends on the game, mods, and device.
 */
object LTWRenderer : RendererInterface {
    override fun getRendererId(): String = "opengles3_ltw"

    override fun getUniqueIdentifier(): String = "a0a34376-5f5c-4be3-96d3-8c5afbbaf5bb"

    override fun getRendererName(): String = "LTW (Large Thin Wrapper)"

    override fun getRendererSummary(): String =
        "Incomplete OpenGL 3.2 core wrapper on OpenGL ES; game, mod, and device compatibility varies."

    /** LTW is available for Minecraft 1.17 and newer. */
    override fun getMinMCVersion(): String = "1.17"

    override fun getRendererEnv(): Lazy<Map<String, String>> = lazy {
        mapOf("LIBGL_ES" to "3")
    }

    override fun getDlopenLibrary(): Lazy<List<String>> = lazy { emptyList() }

    override fun getRendererLibrary(): String = "libltw.so"

    override fun getRendererEGL(): String = "libltw.so"
}
