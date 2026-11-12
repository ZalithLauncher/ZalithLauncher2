/*
 * Zalith Launcher 2
 * Copyright (C) 2025-2026 MovTery <movtery228@qq.com> and contributors
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

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import androidx.core.content.ContextCompat
import com.movtery.zalithlauncher.game.launch.LogName
import com.movtery.zalithlauncher.utils.file.ensureDirectorySilently
import com.movtery.zalithlauncher.utils.logging.Logger
import org.apache.commons.io.FileUtils
import java.io.File

private const val TAG = "PathManager"

/** Nama folder Nev Launcher di beranda (penyimpanan utama), mis. /storage/emulated/0/NevLauncher */
private const val PUBLIC_DIR_NAME = "NevLauncher"
/** Folder game di dalam folder data launcher */
private const val GAME_DIR_NAME = ".minecraft"

class PathManager {
    companion object {
        /** Folder data eksternal yang sudah ditentukan untuk proses ini (ditentukan sekali agar tidak berubah di tengah jalan) */
        private var resolvedExternalDir: File? = null

        /** True bila folder data Nev Launcher berada di beranda (bukan di Android/data) */
        @Volatile
        var isUsingPublicStorage: Boolean = false
            private set

        /** True bila izin sudah ada tetapi folder di beranda gagal dipakai, sehingga kembali ke Android/data */
        @Volatile
        var publicStorageFailed: Boolean = false
            private set

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
        lateinit var DIR_CACHE_SAVE_EXPORTER: File
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
        lateinit var FILE_SPONSORS_CACHE: File

        fun refreshPaths(context: Context) {
            DIR_FILES_PRIVATE = context.filesDir
            DIR_FILES_EXTERNAL = resolveExternalDir(context)
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
            DIR_CACHE_SAVE_EXPORTER = File(DIR_CACHE, "temp_save_exporter")
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
            FILE_SPONSORS_CACHE = File(DIR_CACHE, "monthly_sponsors.json")

            createDirs()
            handleLegacy()
        }

        /**
         * Tentukan folder data eksternal Nev Launcher.
         * Bila izin akses semua file sudah diberikan, dipakai folder "NevLauncher" di beranda;
         * kalau belum (atau gagal), tetap memakai folder lama di Android/data.
         * Hasilnya ditentukan sekali per proses.
         */
        private fun resolveExternalDir(context: Context): File {
            resolvedExternalDir?.let { return it }

            val legacyDir = context.getExternalFilesDir(null) ?: run {
                //from FCL (commit 744156a)
                val externalDir = File(Environment.getExternalStorageDirectory(), "Android/data/${context.packageName}/files")
                if (externalDir.ensureDirectorySilently()) externalDir
                else {
                    Logger.warning(TAG, "External files dir is inaccessible, falling back to internal storage: ${externalDir.absolutePath}")
                    File(DIR_FILES_PRIVATE, "data_files")
                }
            }

            val resolved = tryPublicDir(context, legacyDir) ?: legacyDir
            resolvedExternalDir = resolved
            Logger.info(TAG, "Launcher data directory: ${resolved.absolutePath}")
            return resolved
        }

        private fun hasAllFilesAccess(context: Context): Boolean {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                Environment.isExternalStorageManager()
            } else {
                ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                        ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
            }
        }

        /**
         * @return folder di beranda, atau null bila izin belum ada / folder tidak bisa dipakai
         */
        private fun tryPublicDir(context: Context, legacyDir: File): File? {
            if (!hasAllFilesAccess(context)) return null

            val publicDir = File(Environment.getExternalStorageDirectory(), PUBLIC_DIR_NAME)
            if (!publicDir.ensureDirectorySilently()) {
                Logger.warning(TAG, "Unable to use home storage directory, using Android/data: ${publicDir.absolutePath}")
                publicStorageFailed = true
                return null
            }
            if (!migrateLegacyData(legacyDir, publicDir)) {
                publicStorageFailed = true
                return null
            }
            isUsingPublicStorage = true
            return publicDir
        }

        /**
         * Pindahkan data lama dari Android/data ke folder di beranda (dipindah, bukan disalin).
         * Folder game (.minecraft) dipindah lebih dulu; bila gagal, tidak ada yang dipindah
         * dan launcher tetap memakai folder lama agar data tidak terlihat hilang.
         * @return false bila folder game gagal dipindah
         */
        private fun migrateLegacyData(legacyDir: File, publicDir: File): Boolean {
            if (!legacyDir.isDirectory) return true

            val legacyGame = File(legacyDir, GAME_DIR_NAME)
            val publicGame = File(publicDir, GAME_DIR_NAME)
            if (legacyGame.exists() && !publicGame.exists()) {
                if (!legacyGame.renameTo(publicGame)) {
                    Logger.warning(TAG, "Failed to move game folder to ${publicGame.absolutePath}, keeping ${legacyDir.absolutePath}")
                    return false
                }
            }

            //sisanya (log, layout kontrol, dll.) dipindah semampunya, tidak menimpa file yang sudah ada
            legacyDir.listFiles()?.forEach { child ->
                if (child.name == GAME_DIR_NAME) return@forEach
                val dest = File(publicDir, child.name)
                if (dest.exists()) return@forEach
                if (!child.renameTo(dest)) {
                    Logger.warning(TAG, "Failed to move ${child.absolutePath} to ${dest.absolutePath}")
                }
            }
            return true
        }

        private fun createDirs() {
            listOfNotNull(
                DIR_RUNTIME_MOD, DIR_GAME, DIR_ACCOUNT_SKIN, DIR_ACCOUNT_CAPE, DIR_MULTIRT,
                DIR_JNA, DIR_COMPONENTS, DIR_MOUSE_POINTER, DIR_BACKGROUND,
                DIR_CACHE_GAME_DOWNLOADER, DIR_CACHE_MODPACK_DOWNLOADER, DIR_CACHE_MODPACK_EXPORTER,
                DIR_CACHE_SAVE_EXPORTER, DIR_CACHE_MOD_UPDATER, DIR_CACHE_APP_ICON, DIR_CACHE_HOME_PAGE,
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