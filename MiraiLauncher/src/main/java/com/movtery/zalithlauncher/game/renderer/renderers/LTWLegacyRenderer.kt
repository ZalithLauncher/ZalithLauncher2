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
 * LTW Legacy, the legacy half of the LTW renderer family.
 *
 * Minecraft 1.8 through 1.16.5 drive OpenGL 1.x/2.1 with fixed-function state and the
 * legacy client-array draw path. [LTWRenderer] cannot serve those versions: it implements
 * the OpenGL 3.2 core profile, which has no fixed-function emulation at all. LTW Legacy
 * covers that gap with a dedicated GL 1.x/2.1 to OpenGL ES translation layer.
 *
 * The library is built from the vendored GL4ES snapshot in `third_party/LTWVLegacy`, so it
 * is reproducible from source for every ABI instead of shipping a prebuilt blob. See
 * `third_party/LTWVLegacy/UPSTREAM.md` for provenance and licensing.
 */
object LTWLegacyRenderer : RendererInterface {
    /**
     * Deliberately prefixed with `opengles`, and deliberately not `opengles3`.
     *
     * The launcher's native bridge routes anything starting with `opengles` onto the
     * GLES-backed context path (`egl_bridge.c`, `gl_bridge.c`), which is what a GL-to-GLES
     * translator needs. The exact string `opengles3` belongs to [NGGL4ESRenderer] and would
     * additionally advertise OpenGL 4.0 to the game, which is wrong for these versions.
     */
    override fun getRendererId(): String = "opengles2_ltwlegacy"

    override fun getUniqueIdentifier(): String = "5f2c9a41-8e3d-4b7a-9c16-7d0e4f8a2b53"

    override fun getRendererName(): String = "LTW Legacy"

    override fun getRendererSummary(): String =
        "OpenGL 1.x/2.1 translation layer for Minecraft 1.8 - 1.16.5, built from source."

    /** Minecraft 1.8 is the oldest version Aerix ships, and the oldest with an official launcher profile. */
    override fun getMinMCVersion(): String = "1.8"

    /**
     * 1.16.5 is the last release on the legacy OpenGL pipeline. 1.17 moved Minecraft to the
     * OpenGL 3.2 core profile, which [LTWRenderer] handles instead.
     */
    override fun getMaxMCVersion(): String = "1.16.5"

    override fun getRendererEnv(): Lazy<Map<String, String>> = lazy {
        buildMap {
            // OpenGL ES 2.0 is the floor every supported device meets, and it matches the
            // GL 2.1 feature set these Minecraft versions were written against.
            put("LIBGL_ES", "2")
            // Pin the version reported to the game. Without this the wrapper infers it, and
            // mods that branch on glGetString(GL_VERSION) would see inconsistent results.
            put("LIBGL_GL", "21")
            // The wrapper's startup banner is noise in the launcher's log view.
            put("LIBGL_NOBANNER", "1")
        }
    }

    override fun getDlopenLibrary(): Lazy<List<String>> = lazy { emptyList() }

    override fun getRendererLibrary(): String = "libltwlegacy.so"
}
