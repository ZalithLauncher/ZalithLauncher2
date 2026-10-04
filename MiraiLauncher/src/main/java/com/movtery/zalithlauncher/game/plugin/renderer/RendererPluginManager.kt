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

package com.movtery.zalithlauncher.game.plugin.renderer

import android.content.Context
import android.content.pm.ApplicationInfo
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.game.plugin.ApkPlugin
import com.movtery.zalithlauncher.game.plugin.ApkPluginManager
import com.movtery.zalithlauncher.game.plugin.cacheAppIcon
import com.movtery.zalithlauncher.game.plugin.renderer_v2.RendererV2PluginManager
import com.movtery.zalithlauncher.game.renderer.Renderers

/**
 * FCL、ZalithLauncher 渲染器插件，同时支持使用本地渲染器插件
 * [FCL Renderer Plugin](https://github.com/FCL-Team/FCLRendererPlugin)
 */
object RendererPluginManager: ApkPluginManager() {
    private val rendererPluginList: MutableList<RendererPlugin> = mutableListOf()

    /**
     * 获取当前渲染器插件加载的所有渲染器
     */
    fun getRendererList(): List<RendererPlugin> = rendererPluginList

    /**
     * 移除某些已加载的渲染器
     */
    fun removeRenderer(rendererPlugins: Collection<RendererPlugin>) {
        rendererPluginList.removeAll(rendererPlugins)
    }

    /**
     * 当前选择的渲染器插件所加载的渲染器
     * 根据总渲染器管理者选择的渲染器的渲染器唯一标识符进行判断
     */
    val selectedRendererPlugin: RendererPlugin?
        get() {
            val currentRenderer = runCatching {
                Renderers.getCurrentRenderer().getUniqueIdentifier()
            }.getOrNull()
            return rendererPluginList.find { it.packageName == currentRenderer }
        }

    /**
     * 清除渲染器插件
     */
    fun clearPlugin() {
        rendererPluginList.clear()
    }

    /**
     * 当前渲染器插件是否带有配置项（软件式插件、白名单包名）
     */
    @JvmStatic
    fun isConfigurablePlugin(rendererUniqueIdentifier: String): Boolean {
        val renderer = rendererPluginList.find { it.packageName == rendererUniqueIdentifier }
        return renderer?.isConfigurable == true
    }

    /**
     * 解析 ZalithLauncher、FCL 渲染器插件
     */
    override fun parseApkPlugin(
        context: Context,
        info: ApplicationInfo,
        loaded: (ApkPlugin) -> Unit
    ) {
        if (info.flags and ApplicationInfo.FLAG_SYSTEM == 0) {
            val metaData = info.metaData ?: return
            if (
                metaData.getBoolean("fclPlugin", false) ||
                metaData.getBoolean("zalithRendererPlugin", false)
            ) {
                val packageManager = context.packageManager
                val packageName = info.packageName
                val appName = info.loadLabel(packageManager).toString()

                // 如果已加载新架构渲染器插件，此处不再继续加载其提供的旧架构
                if (
                    RendererV2PluginManager.getRendererList().any { v2Plugin ->
                        v2Plugin.packageName == packageName
                    }
                ) return

                val rendererString = metaData.getString("renderer") ?: return
                val des = metaData.getString("des") ?: return
                val pojavEnvString = metaData.getString("pojavEnv") ?: return
                val nativeLibraryDir = info.nativeLibraryDir
                val renderer = rendererString.split(":")

                var rendererId: String = renderer[0]
                val envList = mutableMapOf<String, String>()
                val dlopenList = mutableListOf<String>()
                pojavEnvString.split(":").forEach { envString ->
                    if (envString.contains("=")) {
                        val stringList = envString.split("=")
                        val key = stringList[0]
                        val value = stringList[1]
                        when (key) {
                            "POJAV_RENDERER" -> rendererId = value
                            "DLOPEN" -> {
                                value.split(",").forEach { lib ->
                                    dlopenList.add(lib)
                                }
                            }
                            "LIB_MESA_NAME", "MESA_LIBRARY" -> envList[key] = "$nativeLibraryDir/$value"
                            else -> envList[key] = value
                        }
                    }
                }

                val plugin = RendererPlugin(
                    packageName = packageName,
                    id = rendererId,
                    displayName = des,
                    summary = context.getString(R.string.settings_renderer_from_plugins, appName),
                    minMCVer = metaData.getVersionString("minMCVer"),
                    maxMCVer = metaData.getVersionString("maxMCVer"),
                    glName = renderer[1],
                    eglName = renderer[2].progressEglName(nativeLibraryDir),
                    path = nativeLibraryDir,
                    env = envList,
                    dlopen = dlopenList,
                    isConfigurable = packageName in setOf(
                        "com.bzlzhh.plugin.ngg",
                        "com.bzlzhh.plugin.ngg.angleless",
                        "com.fcl.plugin.mobileglues"
                    )
                )

                rendererPluginList.add(plugin)

                runCatching {
                    cacheAppIcon(context, info)
                    ApkPlugin(
                        packageName = packageName,
                        appName = appName,
                        appVersion = packageManager.getPackageInfo(packageName, 0).versionName ?: ""
                    )
                }.getOrNull()?.let { loaded(it) }
            }
        }
    }

    private fun String.progressEglName(libPath: String): String =
        if (startsWith("/")) "$libPath$this"
        else this
}