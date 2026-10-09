/*
 * Zalith Launcher 2
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

package com.movtery.zalithlauncher.game.launch

import java.io.File

/**
 * SDL_HINT_OPENGL_LIBRARY (SDL_OPENGL_LIBRARY) expects a loadable shared-library path,
 * not the launcher's renderer identifier (for example, `opengles3_mobileglues`).
 * Built-in renderer libraries are in the app's native library directory; plugins already
 * provide an absolute library path.
 */
internal fun resolveSdlOpenGlLibraryPath(rendererLibrary: String, nativeLibraryDirectory: String): String {
    return if (File(rendererLibrary).isAbsolute) {
        rendererLibrary
    } else {
        File(nativeLibraryDirectory, rendererLibrary).absolutePath
    }
}
