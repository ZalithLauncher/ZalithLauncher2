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

import android.content.Context
import com.movtery.zalithlauncher.game.launch.LogName
import com.movtery.zalithlauncher.utils.logging.Logger
import org.apache.commons.io.FileUtils
import java.io.File

private const val TAG = "PathManager"

class PathManager {
    companion object {
        lateinit var DIR_FILES_PRIVATE: File
        lateinit var DIR_FILES_EXTERNAL: File
        lateinit var DIR_CACHE: File
        lateinit var DIR_NATIVE_LIB: String
        var DIR_RUNTIME_MOD: File? = null

        lateinit var DIR_GAME: File
        lateinit var DIR_DATA_BASES: File
        lateinit var DIR_ACCOUNT_SKIN: File
        lateinit var DIR_ACCOUNT_CAPE: File
        lateinit var DIR_MULTIRT: File
        lateinit var DIR_JNA: File
        lateinit var DIR_COMPONENTS: File
        lateinit var DIR_MOUSE_POINTER: File
        lateinit var DIR_BACKGROUND: File
        lateinit var DIR_CACHE_GAME_DOWNLOADER: File
        lateinit var DIR_CACHE_MODPACK_DOWNLOADER: File
        lateinit var DIR_CACHE_MODPACK_EXPORTER: File
        lateinit var DIR_CACHE_MOD_UPDATER: File
        lateinit var DIR_CACHE_APP_ICON: File
        lateinit var DIR_CACHE_HOME_PAGE: File
        lateinit var DIR_LAUNCHER_LOGS: File
        lateinit var DIR_NATIVE_LOGS: File
        lateinit var DIR_IMAGE_CACHE: File
        lateinit var DIR_CONTROL_LAYOUTS: File
        lateinit var DIR_TERRACOTTA: File
        lateinit var DIR_STYLES: File

        lateinit var FILE_CRASH_REPORT: File
        lateinit var FILE_SETTINGS: File
        lateinit var FILE_MINECRAFT_VERSIONS: File
        lateinit var FILE_LAUNCHER_BACKGROUND: File
        lateinit var FILE_TERRACOTTA_LOG: File

        fun refreshPaths(context: Context) {
            DIR_FILES_PRIVATE = context.filesDir
            DIR_FILES_EXTERNAL = context.getExternalFilesDir(null) ?: run {
                // 外部存储不可用时，改用内部存储
                Logger.warning(TAG, "External files dir is unavailable, falling back to internal storage")
                File(DIR_FILES_PRIVATE, "data_files")
            }
            DIR_CACHE = context.cacheDir
            DIR_NATIVE_LIB = context.applicationInfo.nativeLibraryDir
            DIR_RUNTIME_MOD = context.getDir("runtime_mod", 0)

            DIR_DATA_BASES = File(DIR_FILES_PRIVATE.parentFile, "databases")
            DIR_GAME = File(DIR_FILES_PRIVATE, "games")
            DIR_ACCOUNT_SKIN = File(DIR_GAME, "account_skins")
            DIR_ACCOUNT_CAPE = File(DIR_GAME, "account_capes")
            DIR_MULTIRT = File(DIR_GAME, "runtimes")
            DIR_JNA = File(DIR_GAME, "jna_dir")
            DIR_COMPONENTS = File(DIR_FILES_PRIVATE, "components")
            DIR_MOUSE_POINTER = File(DIR_FILES_PRIVATE, "mouse_pointer")
            DIR_BACKGROUND = File(DIR_FILES_PRIVATE, "background")
            DIR_CACHE_GAME_DOWNLOADER = File(DIR_CACHE, "temp_game")
            DIR_CACHE_MODPACK_DOWNLOADER = File(DIR_CACHE, "temp_modpack")
            DIR_CACHE_MODPACK_EXPORTER = File(DIR_CACHE, "temp_modpack_exporter")
            DIR_CACHE_MOD_UPDATER = File(DIR_CACHE, "temp_mod_updater")
            DIR_CACHE_APP_ICON = File(DIR_CACHE, "app_icons")
            DIR_CACHE_HOME_PAGE = File(DIR_CACHE, "remote_homepage")
            DIR_LAUNCHER_LOGS = File(DIR_FILES_EXTERNAL, "logs")
            DIR_NATIVE_LOGS = File(DIR_LAUNCHER_LOGS, "native")
            DIR_IMAGE_CACHE = File(DIR_CACHE, "images")
            DIR_CONTROL_LAYOUTS = File(DIR_FILES_EXTERNAL, "control_layouts")
            DIR_TERRACOTTA = File(DIR_FILES_PRIVATE, "net.burningtnt.terracotta")
            DIR_STYLES = File(DIR_FILES_PRIVATE, "special_styles")

            FILE_CRASH_REPORT = File(DIR_LAUNCHER_LOGS, "launcher_crash.log")
            FILE_SETTINGS = File(DIR_FILES_PRIVATE, "settings.json")
            FILE_MINECRAFT_VERSIONS = File(DIR_GAME, "minecraft_versions.json")
            FILE_LAUNCHER_BACKGROUND = File(DIR_BACKGROUND, "background01.file")
            FILE_TERRACOTTA_LOG = File(DIR_FILES_EXTERNAL, "terracotta.log")

            createDirs()
            handleLegacy()
        }

        private fun createDirs() {
            listOfNotNull(
                DIR_RUNTIME_MOD, DIR_GAME, DIR_ACCOUNT_SKIN, DIR_ACCOUNT_CAPE, DIR_MULTIRT,
                DIR_JNA, DIR_COMPONENTS, DIR_MOUSE_POINTER, DIR_BACKGROUND,
                DIR_CACHE_GAME_DOWNLOADER, DIR_CACHE_MODPACK_DOWNLOADER, DIR_CACHE_MODPACK_EXPORTER,
                DIR_CACHE_MOD_UPDATER, DIR_CACHE_APP_ICON, DIR_CACHE_HOME_PAGE,
                DIR_LAUNCHER_LOGS, DIR_NATIVE_LOGS, DIR_IMAGE_CACHE, DIR_CONTROL_LAYOUTS,
                DIR_TERRACOTTA, DIR_STYLES
            ).forEach { dir ->
                if (!dir.exists() && !dir.mkdirs()) {
                    Logger.warning(TAG, "Failed to create directory: ${dir.absolutePath}")
                }
            }
        }

        /**
         * 处理历史遗留的旧文件
         */
        private fun handleLegacy() {
            //不再支持的共用游戏运行日志
            File(DIR_FILES_EXTERNAL, LogName.GAME.fileName).takeIf { it.exists() }?.let {
                FileUtils.deleteQuietly(it)
            }
        }
    }
}