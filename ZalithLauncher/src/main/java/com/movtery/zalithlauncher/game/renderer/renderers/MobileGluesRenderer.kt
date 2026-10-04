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

package com.movtery.zalithlauncher.game.renderer.renderers

import com.movtery.zalithlauncher.game.renderer.RendererInterface
import com.movtery.zalithlauncher.path.PathManager
import java.io.File

/**
 * MobileGlues (MG-ES): modern OpenGL-on-GLES translator by MobileGL-Dev.
 * A self-contained wrapper for MC 1.17+; it is configured via
 * MG_DIR_PATH/config.json (written at launch) rather than LIBGL_* variables.
 */
object MobileGluesRenderer : RendererInterface {
    override fun getRendererId(): String = "opengles3_mobileglues"

    override fun getUniqueIdentifier(): String = "0c2477e3-efaa-49fd-a060-6700d7b9f14a"

    override fun getRendererName(): String = "MobileGlues"

    override fun getMinMCVersion(): String = "1.17"

    override fun getMaxMCVersion(): String = "26.3"

    override fun getRendererEnv(): Lazy<Map<String, String>> = lazy {
        buildMap {
            //MG reads <dir>/config.json (written at launch); no LIBGL_* needed.
            put("MG_DIR_PATH", File(PathManager.DIR_FILES_PRIVATE, "MobileGlues").absolutePath)
            //MG targets GLES 3.x. Set explicitly: the launcher's auto-detect would
            //otherwise derive garbage from this renderer's id suffix.
            put("LIBGL_ES", "3")
        }
    }

    //MG is its own EGL provider (same hook LTW uses): feeds POJAVEXEC_EGL and SDL.
    override fun getRendererEGL(): String = "libmobileglues.so"

    override fun getDlopenLibrary(): Lazy<List<String>> = lazy { emptyList() }

    override fun getRendererLibrary(): String = "libmobileglues.so"
}
