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
 * Pojav Glow-Worm VGPU 1.3.6β (`VGPU 1.3.6β`).
 *
 * Backed by `libvgpu_1368.so` (vgpu 1.3.68), which reports OpenGL 3.0 / GLSL 1.30
 * and includes the `shaderconv` pipeline for Minecraft up to 1.16.5, Fabric + Sodium
 * (Oneshot GL 2.0/3.0 backend), and OptiFine / Iris shaderpacks.
 */
object VGPU1368Renderer : RendererInterface {
    override fun getRendererId(): String = "opengles2_vgpu_1"

    override fun getUniqueIdentifier(): String = "c7e9b23f-9a4d-4c8e-b162-3d6a0e4f2c51"

    override fun getRendererName(): String = "VGPU 1.3.6β"

    override fun getRendererSummary(): String =
        "(Compat with all version and very reliable)"

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

    override fun getRendererLibrary(): String = "libvgpu_1368.so"
}
