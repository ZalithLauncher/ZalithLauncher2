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
 * Pojav Glow-Worm VGPU (`vgpu - (up to 1.16.5, fast)`).
 *
 * Backed by `libvgpu.so` (vgpu 1.4.0), which provides fast OpenGL-to-GLES translation
 * with a built-in GLSL 120–440 to 300–320 ES shader converter and `DRAWBUFFERS` /
 * `gl_FragData` rewriter for Minecraft up to 1.16.5 (including Fabric + Sodium and
 * OptiFine / Iris shaders).
 */
object VGPURenderer : RendererInterface {
    override fun getRendererId(): String = "opengles2_vgpu"

    override fun getUniqueIdentifier(): String = "b4f8a12e-6d3c-4e9b-8a71-2c5f9d3e1b40"

    override fun getRendererName(): String = "vgpu - (up to 1.16.5, fast)"

    override fun getRendererSummary(): String =
        "( Performative and balance for version above 1.16.5)"

    override fun getMaxMCVersion(): String = "1.16.5"

    override fun getRendererEnv(): Lazy<Map<String, String>> = lazy {
        buildMap {
            put("LIBGL_ES", "2")
            put("LIBGL_MIPMAP", "3")
            put("LIBGL_NOERROR", "1")
            put("LIBGL_NOINTOVLHACK", "1")
            put("LIBGL_NORMALIZE", "1")
        }
    }

    override fun getDlopenLibrary(): Lazy<List<String>> = lazy { emptyList() }

    override fun getRendererLibrary(): String = "libvgpu.so"
}
