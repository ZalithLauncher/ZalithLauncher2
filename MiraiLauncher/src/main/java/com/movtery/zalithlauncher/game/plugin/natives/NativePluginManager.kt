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

package com.movtery.zalithlauncher.game.plugin.natives

import android.content.Context
import android.content.pm.ApplicationInfo
import com.movtery.zalithlauncher.game.plugin.ApkPlugin
import com.movtery.zalithlauncher.game.plugin.ApkPluginManager
import com.movtery.zalithlauncher.game.plugin.cacheAppIcon
import com.movtery.zalithlauncher.setting.AllSettings
import java.nio.file.Path
import java.nio.file.Paths

object NativePluginManager: ApkPluginManager() {
    private val nativePlugins = mutableListOf<NativePlugin>()

    private val disabledPlugins: List<String>
        get() = AllSettings.disableNativeLibPlugins.getValue()

    /**
     * 获取全部已加载的原生库插件
     */
    fun getPlugins(): List<NativePlugin> = nativePlugins.toList()

    /**
     * 获取所有未禁用的原生库插件
     */
    fun getCheckedPlugins(): List<NativePlugin> =
        nativePlugins.filter { it.packageName !in disabledPlugins }

    /**
     * 获取所有未禁用的原生库插件的 native lib dir
     */
    fun getPaths(): List<String> {
        return buildList {
            nativePlugins.forEach { plugin ->
                if (plugin.packageName in disabledPlugins) return@forEach
                add(plugin.path)
            }
        }
    }

    /**
     * 获取所有未禁用的原生库插件的 JVM 环境参数
     */
    fun getJVMEnv(): List<String> {
        return buildList {
            nativePlugins.forEach { plugin ->
                if (plugin.packageName in disabledPlugins) return@forEach
                addAll(plugin.envList)
            }
        }
    }

    fun clearPlugin() {
        nativePlugins.clear()
    }

    override fun parseApkPlugin(
        context: Context,
        info: ApplicationInfo,
        loaded: (ApkPlugin) -> Unit
    ) {
        if (info.flags and ApplicationInfo.FLAG_SYSTEM == 0) {
            val metaData = info.metaData ?: return
            if (
                metaData.getBoolean("FCLNativePlugin", false)
            ) {
                val nativeLibraryDir = info.nativeLibraryDir
                val packageManager = context.packageManager
                val packageName = info.packageName
                val appName = info.loadLabel(packageManager).toString()
                val appVersion = packageManager.getPackageInfo(packageName, 0).versionName ?: ""

                val environment = metaData.getString("environment") ?: return
                val des = metaData.getString("des") ?: ""

                val envList = if (environment.isNotEmpty()) {
                    val entries = environment.split(" ")
                    buildList {
                        entries.forEach { entry ->
                            add(parseEntry(entry, nativeLibraryDir))
                        }
                    }
                } else {
                    emptyList()
                }

                val plugin = NativePlugin(
                    packageName = packageName,
                    appName = appName,
                    appVersion = appVersion,
                    displayName = des,
                    minMCVer = metaData.getVersionString("minMCVer"),
                    maxMCVer = metaData.getVersionString("maxMCVer"),
                    path = nativeLibraryDir,
                    envList = envList
                )
                nativePlugins.add(plugin)

                runCatching {
                    cacheAppIcon(context, info)
                    loaded(plugin)
                }
            }
        }
    }

    private const val NATIVE_LIB_DIR_PLACEHOLDER = "{nativeLibraryDir}"

    private fun parseEntry(
        entry: String,
        nativeLibraryDir: String
    ): String {
        var (key, value) = entry.split("=")

        if (value.startsWith(NATIVE_LIB_DIR_PLACEHOLDER)) {
            if (value == NATIVE_LIB_DIR_PLACEHOLDER) {
                value = nativeLibraryDir
            } else {
                val path = safePath(
                    baseDir = nativeLibraryDir,
                    input = value.removePrefix(NATIVE_LIB_DIR_PLACEHOLDER)
                )
                value = path?.toAbsolutePath()?.toString() ?: nativeLibraryDir
            }
        }

        return "$key=$value"
    }

    private fun safePath(baseDir: String, input: String): Path? {
        return try {
            val basePath = Paths.get(baseDir).normalize().toAbsolutePath()
            val resolvedPath = basePath.resolve(input).normalize().toAbsolutePath()

            if (resolvedPath.startsWith(basePath)) {
                resolvedPath
            } else {
                null //阻止路径穿越
            }
        } catch (_: Exception) {
            null //无效的路径
        }
    }
}