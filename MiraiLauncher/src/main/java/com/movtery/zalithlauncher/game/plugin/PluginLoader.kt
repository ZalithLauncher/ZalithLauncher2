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

package com.movtery.zalithlauncher.game.plugin

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import com.movtery.zalithlauncher.game.plugin.driver.DriverPluginManager
import com.movtery.zalithlauncher.game.plugin.ffmpeg.FFmpegPluginManager
import com.movtery.zalithlauncher.game.plugin.natives.NativePluginManager
import com.movtery.zalithlauncher.game.plugin.renderer.RendererPluginManager
import com.movtery.zalithlauncher.game.plugin.renderer_v2.RendererV2PluginManager
import com.movtery.zalithlauncher.game.renderer.Renderers
import com.movtery.zalithlauncher.utils.logging.Logger

/**
 * 统一插件的加载，保证仅获取一次应用列表
 */
object PluginLoader {
    private var isInitialized: Boolean = false
    private const val PACKAGE_FLAGS =
        PackageManager.GET_META_DATA or PackageManager.GET_SHARED_LIBRARY_FILES

    /**
     * 所有已加载的应用插件
     */
    var allPlugins: List<ApkPlugin> = emptyList()
        private set

    @JvmStatic
    @SuppressLint("QueryPermissionsNeeded")
    fun loadAllPlugins(context: Context, force: Boolean = false) {
        if (isInitialized && !force) return
        isInitialized = true

        val apkPluginList: MutableList<ApkPlugin> = mutableListOf()

        DriverPluginManager.initDriver(context)
        RendererPluginManager.clearPlugin()
        RendererV2PluginManager.clearPlugin()
        NativePluginManager.clearPlugin()

        val queryIntentActivities =
            context.packageManager.queryIntentActivities(
                Intent(Intent.ACTION_MAIN),
                PACKAGE_FLAGS
            )
        queryIntentActivities.forEach { resolve ->
            val applicationInfo = resolve.activityInfo.applicationInfo
            runCatching {
                DriverPluginManager.parseApkPlugin(context, applicationInfo) { apkPluginList.add(it) }
                RendererV2PluginManager.parseApkPlugin(context, applicationInfo) { apkPluginList.add(it) }
                RendererPluginManager.parseApkPlugin(context, applicationInfo) { apkPluginList.add(it) }
                NativePluginManager.parseApkPlugin(context, applicationInfo) { apkPluginList.add(it) }
            }.onFailure { e ->
                Logger.error("PluginLoader", "An exception was encountered while importing the software plugin ${applicationInfo.packageName}", e)
            }
        }
        FFmpegPluginManager.loadPlugin(context) { apkPluginList.add(it) }

        // 加载旧架构渲染器插件
        RendererPluginManager.getRendererList().filter { plugin ->
            !Renderers.addRenderer(plugin)
        }.takeIf {
            it.isNotEmpty()
        }?.let { failedToLoadList ->
            RendererPluginManager.removeRenderer(failedToLoadList)
        }
        // 加载新架构渲染器插件
        RendererV2PluginManager.getRendererList().filter { plugin ->
            !Renderers.addRenderer(plugin)
        }.takeIf {
            it.isNotEmpty()
        }?.let { failedToLoadList ->
            RendererV2PluginManager.removeRenderer(failedToLoadList)
        }

        // 去重已加载的插件
        val seenPackages = mutableSetOf<String>()
        apkPluginList.removeAll { !seenPackages.add(it.packageName) }

        //全部已加载的插件
        allPlugins = apkPluginList.sortedBy { it.appName }
    }
}