/*
 * Zalith Launcher 2
 * Copyright (C) 2025 MovTery <movtery228@qq> and contributors
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

package com.movtery.zalithlauncher.game.plugin.renderer_v2

import android.content.Context
import android.content.pm.ApplicationInfo
import android.os.Bundle
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.game.plugin.ApkPlugin
import com.movtery.zalithlauncher.game.plugin.ApkPluginManager
import com.movtery.zalithlauncher.game.plugin.cacheAppIcon
import com.movtery.zalithlauncher.game.plugin.renderer_v2.data.RendererConfig
import com.movtery.zalithlauncher.game.plugin.renderer_v2.data.resolveNativePaths
import com.movtery.zalithlauncher.path.GLOBAL_JSON
import com.movtery.zalithlauncher.utils.logging.Logger

object RendererV2PluginManager : ApkPluginManager() {
    private const val TAG = "RendererV2Plugin"
    private val rendererPluginList: MutableList<RendererV2Data> = mutableListOf()

    fun getRendererList(): List<RendererV2Data> = rendererPluginList


    fun clearPlugin() {
        rendererPluginList.clear()
    }

    /**
     * 识别插件并暂存[ApplicationInfo]，不做加载
     */
    override fun parseApkPlugin(
        context: Context,
        info: ApplicationInfo,
        loaded: (ApkPlugin) -> Unit
    ) {
        if (info.flags and ApplicationInfo.FLAG_SYSTEM != 0) return
        val metaData = info.metaData ?: return

        // 读取启动器配置资源
        val configRes = metaData.getStringRes("fclPlugin_V2") ?: return
        val configString = context.getString(info, configRes) ?: return

        val pm = context.packageManager
        val packageName = info.packageName

        // 反序列化渲染器配置信息
        val config = runCatching {
            GLOBAL_JSON.decodeFromString<RendererConfig>(configString)
        }.onFailure { e ->
            Logger.error(TAG, "Failed to parse config JSON from $packageName", e)
        }.getOrNull() ?: return

        // 获取插件应用信息
        val appLabel = info.loadLabel(pm).toString()
        val appVersion = runCatching {
            pm.getPackageInfo(packageName, 0).versionName ?: ""
        }.getOrDefault("")

        rendererPluginList.add(
            RendererV2Data(
                packageName = packageName,
                nativePath = info.nativeLibraryDir,
                summary = context.getString(R.string.settings_renderer_from_plugins, appLabel),
                renderer = config.resolveNativePaths(info.nativeLibraryDir)
            ) { metaString ->
                context.getMetaString(info, metaString)
            }
        )

        // 已成功加载目标插件
        runCatching {
            cacheAppIcon(context, info)
            ApkPlugin(
                packageName = packageName,
                appName = appLabel,
                appVersion = appVersion
            )
        }.getOrNull()?.let { loaded(it) }
    }

    private fun Bundle.getStringRes(key: String): Int? {
        return runCatching {
            getInt(key, -1).takeIf { it > 0 }
        }.getOrNull()
    }

    private fun Context.getString(info: ApplicationInfo, path: Int): String? {
        return runCatching {
            packageManager.getResourcesForApplication(info).getString(path)
        }.getOrNull()
    }

    private fun Context.getMetaString(info: ApplicationInfo, key: String): String? {
        return runCatching {
            val metaData = info.metaData ?: return null
            val path = metaData.getStringRes(key) ?: return null
            packageManager.getResourcesForApplication(info).getString(path)
        }.getOrNull()
    }

    /**
     * 移除加载失败的渲染器
     */
    fun removeRenderer(failedToLoadList: List<RendererV2Data>) {
        rendererPluginList.removeAll { it in failedToLoadList }
    }
}
