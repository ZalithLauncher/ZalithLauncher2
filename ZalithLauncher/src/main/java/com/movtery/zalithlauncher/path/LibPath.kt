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

package com.movtery.zalithlauncher.path

import com.movtery.zalithlauncher.path.PathManager.Companion.DIR_COMPONENTS
import com.movtery.zalithlauncher.path.PathManager.Companion.DIR_JNA
import java.io.File

class LibPath {
    companion object {
        private val LAUNCHER_COMPONENTS = File(DIR_COMPONENTS, "launcher")
        private val AUTH_LIBS_DIR = File(DIR_COMPONENTS, "auth_libs")

        @JvmField val CACIO_8 = File(DIR_COMPONENTS, "caciocavallo")
        @JvmField val CACIO_17 = File(DIR_COMPONENTS, "caciocavallo17")
        @JvmField val CACIO_17_AGENT = File(CACIO_17, "cacio-agent.jar")

        @JvmField val JNA = File(DIR_JNA, "jna")

        @JvmField val MIO_LIB_PATCHER = File(LAUNCHER_COMPONENTS, "MioLibPatcher.jar")
        /**
         * [Github](https://github.com/bangbang93/forge-install-bootstrapper)
         */
        @JvmField val FORGE_INSTALLER = File(LAUNCHER_COMPONENTS, "forge_installer.jar")
        @JvmField val JAR_EXCEPTION_CATCHER = File(LAUNCHER_COMPONENTS, "JarExceptionCatcher.jar")
        @JvmField val AWT_BLOCKER_AGENT = File(LAUNCHER_COMPONENTS, "AWTBlockerAgent.jar")

        @JvmField val AUTHLIB_INJECTOR = File(AUTH_LIBS_DIR, "authlib-injector.jar")
        @JvmField val NIDE_8_AUTH = File(AUTH_LIBS_DIR, "nide8auth.jar")
    }
}