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

package com.movtery.zalithlauncher.game.launch

import android.app.Activity
import androidx.collection.ArrayMap
import com.movtery.zalithlauncher.BuildConfig
import com.movtery.zalithlauncher.BuildKeys
import com.movtery.zalithlauncher.bridge.LoggerBridge
import com.movtery.zalithlauncher.bridge.ZLBridge
import com.movtery.zalithlauncher.context.readAssetFile
import com.movtery.zalithlauncher.game.account.Account
import com.movtery.zalithlauncher.game.account.isAuthServerAccount
import com.movtery.zalithlauncher.game.account.isLocalAccount
import com.movtery.zalithlauncher.game.account.offline.OfflineYggdrasilServer
import com.movtery.zalithlauncher.game.addons.modloader.ModLoader
import com.movtery.zalithlauncher.game.multirt.Runtime
import com.movtery.zalithlauncher.game.path.getAssetsHome
import com.movtery.zalithlauncher.game.path.getLibrariesHome
import com.movtery.zalithlauncher.game.plugin.Plugin
import com.movtery.zalithlauncher.game.plugin.driver.DriverPluginManager
import com.movtery.zalithlauncher.game.plugin.natives.NativePluginManager
import com.movtery.zalithlauncher.game.plugin.renderer.RendererPluginManager
import com.movtery.zalithlauncher.game.renderer.ConfigurableRenderer
import com.movtery.zalithlauncher.game.renderer.RendererLaunchConfig
import com.movtery.zalithlauncher.game.renderer.Renderers
import com.movtery.zalithlauncher.game.renderer.getSfpewLibraryPath
import com.movtery.zalithlauncher.game.renderer.isSfpewEnabledFor
import com.movtery.zalithlauncher.game.version.download.artifactToPath
import com.movtery.zalithlauncher.game.version.download.filterLibrary
import com.movtery.zalithlauncher.game.version.download.getLibraryReplacement
import com.movtery.zalithlauncher.game.version.installed.Version
import com.movtery.zalithlauncher.game.version.installed.VersionInfo
import com.movtery.zalithlauncher.game.versioninfo.models.GameManifest
import com.movtery.zalithlauncher.path.LibPath
import com.movtery.zalithlauncher.path.PathManager
import com.movtery.zalithlauncher.setting.AllSettings
import com.movtery.zalithlauncher.ui.screens.content.elements.QuickPlay
import com.movtery.zalithlauncher.utils.file.child
import com.movtery.zalithlauncher.utils.logging.Logger
import com.movtery.zalithlauncher.utils.network.ServerAddress
import com.movtery.zalithlauncher.utils.string.insertJSONValueList
import com.movtery.zalithlauncher.utils.string.isEmptyOrBlank
import com.movtery.zalithlauncher.utils.string.isEqualTo
import com.movtery.zalithlauncher.utils.string.isLowerTo
import com.movtery.zalithlauncher.utils.string.isNotEmptyOrBlank
import com.movtery.zalithlauncher.utils.string.splitPreservingQuotes
import com.movtery.zalithlauncher.utils.string.toUnicodeEscaped
import java.io.File
import javax.microedition.khronos.egl.EGL10
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.egl.EGLContext

private const val TAG = "GameLaunchProfile"

class GameLaunchProfile(
    private val activity: Activity,
    private val version: Version,
    private val account: Account,
    private val offlineServer: OfflineYggdrasilServer,
    private val runtime: Runtime,
    private val gameManifest: GameManifest,
    private val clientJar: File,
    private val gameDirPath: File,
    private val runtimeLibraryPath: String,
    private val lwjglVersion: Int,
    private val jnaDir: File?,
    private val configuredRenderer: RendererLaunchConfig?
) : LaunchProfile {

    override fun preJvmArgs(): List<String> {
        val argsList: MutableList<String> = ArrayList()

        if (account.isLocalAccount()) {
            if (account.hasSkinFile) {
                //该离线账号拥有本地皮肤，启用离线yggdrasil服务器
                offlineServer.start()
                offlineServer.addCharacter(account)
                offlineServer.getPort()?.let { port ->
                    val msg = "Using offline Yggdrasil server on port $port"
                    LoggerBridge.append(msg)
                    Logger.info(TAG, msg)
                    argsList.add("-javaagent:${LibPath.AUTHLIB_INJECTOR.absolutePath}=http://localhost:$port")
                    argsList.add("-Dauthlibinjector.side=client")
                } ?: run {
                    //无法获取端口号，说明服务器未成功启动
                    val msg = "Failed to start offline Yggdrasil server!"
                    LoggerBridge.append(msg)
                    Logger.warning(TAG, msg)
                    //本次启动将被忽略，为避免浪费性能，关停服务器
                    offlineServer.stop()
                }
            }
        } else if (account.isAuthServerAccount()) {
            if (account.otherBaseUrl!!.contains("auth.mc-user.com")) {
                argsList.add("-javaagent:${LibPath.NIDE_8_AUTH.absolutePath}=${account.otherBaseUrl!!.replace("https://auth.mc-user.com:233/", "")}")
                argsList.add("-Dnide8auth.client=true")
            } else {
                argsList.add("-javaagent:${LibPath.AUTHLIB_INJECTOR.absolutePath}=${account.otherBaseUrl}")
                argsList.add("-Dauthlibinjector.side=client")
            }
        }

        return argsList
    }

    override fun jvmArgs(): List<String> {
        val argsList: MutableList<String> = ArrayList()

        //启动器选定的渲染器库，purge 会清掉用户与清单中的同名键，此处兜底
        if (Renderers.isCurrentRendererValid()) {
            argsList.add("-Dorg.lwjgl.opengl.libname=${getGameRendererLibrary(version, configuredRenderer)}")
        }

        //MioLibPatcher 需作为 JVM 选项注册在 cacio agent 之后、-cp 之前
        argsList.add("-javaagent:${LibPath.MIO_LIB_PATCHER.absolutePath}")

        val configFilePath = version.getVersionPath().child("log4j2.xml")
        if (!configFilePath.exists()) {
            val is7 = (version.getVersionInfo()?.minecraftVersion ?: "0.0").isLowerTo("1.12")
            runCatching {
                val content = if (is7) {
                    readAssetsFile("components/log4j-1.7.xml")
                } else {
                    readAssetsFile("components/log4j-1.12.xml")
                }
                configFilePath.writeText(content)
            }.onFailure {
                Logger.warning(TAG, "Failed to write fallback Log4j configuration autonomously!", it)
            }
        }
        argsList.add("-Dlog4j.configurationFile=${configFilePath.absolutePath}")
        argsList.add("-Dminecraft.client.jar=${clientJar.absolutePath}")
        argsList.add("-Dminecraft.launcher.brand=${BuildKeys.LAUNCHER_NAME}")
        argsList.add("-Dminecraft.launcher.version=${BuildConfig.VERSION_NAME}")

        argsList.addAll(getMinecraftJVMArgs())
        argsList.addAll(NativePluginManager.getJVMEnv())

        if (runtime.javaVersion > 8) {
            argsList.add("--add-exports")
            val pkg: String = gameManifest.mainClass.substring(0, gameManifest.mainClass.lastIndexOf("."))
            argsList.add("$pkg/$pkg=ALL-UNNAMED")
        }

        return argsList
    }

    override fun programArgs(): List<String> {
        val argsList: MutableList<String> = ArrayList()

        argsList.add("mio.Wrapper")
        argsList.add(gameManifest.mainClass)
        argsList.addAll(getMinecraftClientArgs())

        version.getVersionInfo()?.let { info ->
            val quickPlay = version.quickPlaySingle
            if (quickPlay != null) {
                when (quickPlay) {
                    is QuickPlay.Save -> {
                        if (quickPlay.saveName.isEmptyOrBlank()) return@let

                        if (info.quickPlay.isQuickPlaySingleplayer) {
                            //将不受支持的字符转换为Unicode
                            val saveName = quickPlay.saveName.toUnicodeEscaped()
                            argsList.apply {
                                add("--quickPlaySingleplayer")
                                add(saveName)
                            }
                        } else {
                            val msg = "Quick Play for singleplayer is not supported and has been skipped."
                            LoggerBridge.append(msg)
                            Logger.warning(TAG, msg)
                        }
                    }
                    is QuickPlay.Server -> {
                        argsList.addQuickPlayServer(
                            address = quickPlay.serverAddress,
                            quickPlay = info.quickPlay
                        )
                    }
                }
            } else {
                version.getServerIp()?.let { address ->
                    argsList.addQuickPlayServer(
                        address = address,
                        quickPlay = info.quickPlay
                    )
                }
            }
        }

        //追加版本配置的游戏参数，置于参数列表末尾
        argsList.addAll(version.getGameArgs().splitPreservingQuotes())

        return argsList
    }

    override fun ramAllocation(): Int = version.getRamAllocation(activity)

    override fun userJvmArgs(): String {
        return version.getJvmArgs().takeIf { it.isNotBlank() } ?: AllSettings.jvmArgs.getValue()
    }

    override fun putLaunchProperties(props: MutableMap<String, String>) {
        val versionInfo = version.getVersionInfo()
        //Fix Forge 1.7.2
        val is172 = (versionInfo?.minecraftVersion ?: "0.0").isEqualTo("1.7.2")
        if (is172 && versionInfo?.hasLoader(ModLoader.FORGE) == true) {
            Logger.debug(TAG, "Is Forge 1.7.2, use the patched sorting method.")
            props["sort.patch"] = "true"
        }

        //Jna
        jnaDir?.let { dir ->
            props["jna.boot.library.path"] = dir.absolutePath //覆盖通用键表中添加的jna路径
        }
    }

    override fun onInitEnv(ctx: LaunchContext, env: MutableMap<String, String>) {
        env["DRIVER_PATH"] = DriverPluginManager.getDriver(version.getDriver()).path

        checkAndUsedJSPH(env, ctx.runtime)
        version.getVersionInfo()?.loaderInfos?.forEach { info ->
            info.getLoaderEnvKey()?.let { loaderKey ->
                env[loaderKey] = "1"
            }
        }
        if (Renderers.isCurrentRendererValid()) {
            setRendererEnv(env, version, configuredRenderer)
        }
        env["ZALITH_VERSION_CODE"] = BuildConfig.VERSION_CODE.toString()

        //lwjgl3ify 实例：注入 XDG 数据目录兜底，避免其向不存在的 ~/.local/share
        //写桌面快捷方式时抛出异常杀死 RFB 主线程，导致游戏静默退出
        if (File(version.getGameDir(), "config/lwjgl3ify.cfg").isFile()) {
            val xdgDataHome = File(version.getGameDir(), ".local/share")
            if (!xdgDataHome.isDirectory) xdgDataHome.mkdirs()
            env["XDG_DATA_HOME"] = xdgDataHome.absolutePath
        }
    }

    override fun onDlopenEngine(ctx: LaunchContext) {
        LoggerBridge.appendTitle("DLOPEN Renderer")

        //声音引擎加载后，dlopen渲染器的库
        RendererPluginManager.selectedRendererPlugin?.let { renderer ->
            val libs by renderer.getDlopenLibrary()
            libs.forEach { libPath ->
                ZLBridge.dlopen(libPath)
            }
        }

        val rendererLib = getRendererLibrary() ?: return
        if (!ZLBridge.dlopen(rendererLib) && !ZLBridge.dlopen(findInLdLibPath(ctx.libraryPath, rendererLib))) {
            Logger.error(TAG, "Failed to load renderer $rendererLib")
        }
    }

    private fun readAssetsFile(path: String): String = activity.readAssetFile(path)

    /**
     * 组装 LWJGL 组件 classpath
     * 版本 >= 3.4.1 -> 使用 3.4.1 组件；否则使用 3.3.3 组件。
     * LWJGL2 时代（版本 <= 299）额外加入 lwjgl-lwjglx.jar 桥接层。
     * lwjgl.jar 核心优先 -> merged-modules -> 其余模块。
     */
    private fun getLWJGL3ClassPath(): String {
        val versionDir = lwjglVersionDir(lwjglVersion)
        val dir = File(PathManager.DIR_COMPONENTS, "lwjgl/$versionDir")
        val isLwjgl2 = lwjglVersion in 1..299
        return dir.listFiles { file -> file.name.endsWith(".jar") }
            ?.sortedBy { file -> lwjglJarOrder(file.name, versionDir, isLwjgl2) }
            ?.filter { file -> isLwjgl2 || file.name != "lwjgl-lwjglx.jar" }
            ?.joinToString(":") { it.absolutePath }
            ?: ""
    }

    private fun lwjglJarOrder(name: String, versionDir: String, isLwjgl2: Boolean): Int = when (name) {
        "lwjgl.jar" -> 0
        "lwjgl-$versionDir-merged-modules.jar" -> 1
        "lwjgl-lwjglx.jar" -> 3 // 桥接层放最后，仅 LWJGL2 使用
        else -> 2
    }

    private fun getMinecraftJVMArgs(): Array<String> {
//        // Parse Forge 1.17+ additional JVM Arguments
//        if (versionInfo.inheritsFrom == null || versionInfo.arguments == null || versionInfo.arguments.jvm == null) {
//            return emptyArray()
//        }

        val varArgMap: MutableMap<String, String> = android.util.ArrayMap()
        val launchClassPath = buildList {
            add(getLWJGL3ClassPath())
            add(LibPath.MIO_LAUNCH_WRAPPER.absolutePath)
            putLaunchClassPath(gameManifest)
        }.joinToString(":")
        var hasClasspath = false //是否已经在jvm参数中包含 ${classpath} 配置

        varArgMap["classpath_separator"] = ":"
        varArgMap["library_directory"] = getLibrariesHome(version.getGameHome())
        varArgMap["version_name"] = gameManifest.id
        varArgMap["natives_directory"] = runtimeLibraryPath
        setLauncherInfo(varArgMap)

        fun Any.processJvmArg(): String? = (this as? String)?.let { argument ->
            if (argument.startsWith("-Djava.library.path=")) {
                //26.2+ Mojang 更改到了具体的路径，需要手动重定向
                return@let $$"-Djava.library.path=${natives_directory}"
            }
            when {
                argument.startsWith("-DignoreList=") -> {
                    "$argument,${version.getVersionName()}.jar"
                }
                argument.contains("-Dio.netty.native.workdir") ||
                argument.contains("-Djna.tmpdir") ||
                argument.contains("-Dorg.lwjgl.system.SharedLibraryExtractPath") -> {
                    //使用一个可读的目录
                    argument.replace($$"${natives_directory}", PathManager.DIR_CACHE.absolutePath)
                }
                argument == $$"${classpath}" -> {
                    hasClasspath = true
                    launchClassPath
                }
                else -> argument
            }
        }

        val jvmArgs = gameManifest.arguments?.jvm
            ?.mapNotNull { it.processJvmArg() }
            ?.toTypedArray()
            ?: emptyArray()

        val replacedArgs = insertJSONValueList(jvmArgs, varArgMap)
        return if (hasClasspath) {
            replacedArgs
        } else {
            //不包含 ${classpath} 配置，则需要手动添加
            replacedArgs + arrayOf("-cp", launchClassPath)
        }
    }

    /**
     * [Modified from PojavLauncher](https://github.com/PojavLauncherTeam/PojavLauncher/blob/a6f3fc0/app_pojavlauncher/src/main/java/net/kdt/pojavlaunch/Tools.java#L572-L592)
     */
    private fun MutableList<String>.putLaunchClassPath(gameManifest: GameManifest) {
        val classpath: Array<String> = generateLibClasspath(gameManifest)

        for (jarFile in classpath) {
            val jarFileObj = File(jarFile)
            if (!jarFileObj.exists()) {
                Logger.debug(TAG, "Ignored non-exists file: $jarFile")
                continue
            }
            add(jarFile)
        }
        if (clientJar.exists()) {
            add(clientJar.absolutePath)
        }
    }

    /**
     * [Modified from PojavLauncher](https://github.com/PojavLauncherTeam/PojavLauncher/blob/a6f3fc0/app_pojavlauncher/src/main/java/net/kdt/pojavlaunch/Tools.java#L871-L882)
     */
    private fun generateLibClasspath(gameManifest: GameManifest): Array<String> {
        val libSortFix = LibSortFix()
        val libs = LinkedHashMap<GameManifest.Library, String>()

        for (libItem in gameManifest.libraries) {
            if (!(GameManifest.Rule.checkRules(libItem.rules) && !libItem.isNative)) continue
            val path = libItem.progressLibrary() ?: continue
            with(libSortFix) {
                libs.insertLib(libItem, getLibrariesHome(version.getGameHome()) + "/" + path)
            }
        }

        return libs.values.toTypedArray<String>()
    }

    /**
     * @return 库相对路径
     */
    private fun GameManifest.Library.progressLibrary(): String? {
        if (filterLibrary()) return null

        var path = artifactToPath(this)

        val versionSegment = name.split(":").getOrNull(2) ?: return path
        val versionParts = versionSegment.split(".")

        getLibraryReplacement(name, versionParts)?.let { replacement ->
            Logger.debug(TAG, "Library ${this.name} has been changed to version ${replacement.newName.split(":").last()}")
            path = replacement.newPath
        }

        return path
    }

    private fun getMinecraftClientArgs(): Array<String> {
        val varArgMap: MutableMap<String, String> = ArrayMap()
        varArgMap["auth_session"] = account.accessToken
        varArgMap["auth_access_token"] = account.accessToken
        varArgMap["auth_player_name"] = account.username
        varArgMap["auth_uuid"] = account.profileId.replace("-", "")
        varArgMap["auth_xuid"] = account.xUid ?: ""
        varArgMap["assets_root"] = getAssetsHome(version.getGameHome())
        varArgMap["assets_index_name"] = gameManifest.assetIndex.id
        varArgMap["game_assets"] = getAssetsHome(version.getGameHome())
        varArgMap["game_directory"] = gameDirPath.absolutePath
        varArgMap["user_properties"] = "{}"
        varArgMap["user_type"] = "msa"
        varArgMap["version_name"] = gameManifest.id

        setLauncherInfo(varArgMap)

        val minecraftArgs: MutableList<String> = ArrayList()
        gameManifest.arguments?.apply {
            // Support Minecraft 1.13+
            game.forEach { if (it is String) minecraftArgs.add(it) }
        }

        return insertJSONValueList(
            splitAndFilterEmpty(
                gameManifest.minecraftArguments ?:
                minecraftArgs.toTypedArray().joinToString(" ")
            ), varArgMap
        )
    }

    private fun setLauncherInfo(verArgMap: MutableMap<String, String>) {
        verArgMap["launcher_name"] = BuildKeys.LAUNCHER_NAME
        verArgMap["launcher_version"] = BuildConfig.VERSION_NAME
        verArgMap["version_type"] = version.getCustomInfo()
            .takeIf { it.isNotEmptyOrBlank() }
            ?: gameManifest.type
    }

    private fun splitAndFilterEmpty(arg: String): Array<String> {
        val list: MutableList<String> = ArrayList()
        arg.split(" ").forEach {
            if (it.isNotEmpty()) list.add(it)
        }
        return list.toTypedArray()
    }

    private fun MutableList<String>.addQuickPlayServer(
        address: String,
        quickPlay: VersionInfo.QuickPlay
    ) {
        runCatching {
            ServerAddress.parse(address)
        }.onFailure {
            val msg = "Unable to resolve the server address: $address. The automatic server join feature is unavailable."
            LoggerBridge.append(msg)
            Logger.warning(TAG, msg, it)
        }.getOrNull()?.let { parsed ->
            val args = if (quickPlay.isQuickPlayMultiplayer) {
                val port = if (parsed.port < 0) {
                    ServerAddress.DEFAULT_PORT
                } else {
                    parsed.port
                }

                listOf(
                    "--quickPlayMultiplayer",
                    "${parsed.getASCIIHost()}:$port"
                )
            } else {
                val port = parsed.port.takeIf { it >= 0 } ?: ServerAddress.DEFAULT_PORT
                listOf("--server", parsed.getASCIIHost(), "--port", port.toString())
            }

            addAll(args)
        }
    }
}

private fun checkAndUsedJSPH(envMap: MutableMap<String, String>, runtime: Runtime) {
    if (runtime.javaVersion < 11) return //onUseJSPH
    val dir = File(PathManager.DIR_NATIVE_LIB).takeIf { it.isDirectory } ?: return
    val jsphHome = if (runtime.javaVersion == 17) "libjsph17" else "libjsph21"
    dir.listFiles { _, name -> name.startsWith(jsphHome) }?.takeIf { it.isNotEmpty() }?.let {
        val libName = "${PathManager.DIR_NATIVE_LIB}/$jsphHome.so"
        envMap["JSP"] = libName
    }
}

private fun setRendererEnv(
    envMap: MutableMap<String, String>,
    version: Version,
    configuredRenderer: RendererLaunchConfig?,
) {
    val renderer = Renderers.getCurrentRenderer()
    val rendererId = renderer.getRendererId()

    // SDL 环境变量
    envMap["SDL_OPENGL_LIBRARY"] = rendererId

    if (rendererId.startsWith("opengles2")) {
        envMap["LIBGL_ES"] = "2"
        envMap["LIBGL_MIPMAP"] = "3"
        envMap["LIBGL_NOERROR"] = "1"
        envMap["LIBGL_NOINTOVLHACK"] = "1"
        envMap["LIBGL_NORMALIZE"] = "1"
    }

    if (renderer is ConfigurableRenderer) {
        envMap += checkNotNull(configuredRenderer) {
            "Renderer configuration has not been prepared"
        }.environment
    } else {
        envMap += renderer.getRendererEnv().value
    }

    renderer.getRendererEGL()?.let { eglName ->
        envMap["POJAVEXEC_EGL"] = eglName

        val eglPath = if (eglName.startsWith("/")) {
            // 路径不存在时不设置，交给 SDL 按自身默认解析
            eglName
        } else {
            // eglName 已是绝对路径时直接使用，仅裸名拼接库目录
            val nativeLibPath = if (renderer is Plugin) {
                renderer.getNativeLibPath()
            } else {
                PathManager.DIR_NATIVE_LIB
            }
            "$nativeLibPath/$eglName"
        }
        if (eglPath.isNotEmpty() && File(eglPath).isFile()) {
            envMap["SDL_EGL_LIBRARY"] = eglPath
        }
    }

    envMap["POJAV_RENDERER"] = rendererId

    if (isSfpewEnabledFor(renderer, version, configuredRenderer)) {
        envMap["SFPEW_LAYER"] = "1"
        envMap["SFPEW_EGL"] = envMap["SDL_EGL_LIBRARY"]
            ?: envMap["POJAVEXEC_EGL"]
            ?: "libEGL.so"
        if (version.isSfpewRelaxedContextEnabled()) {
            envMap["SFPEW_RELAXED_CONTEXT"] = "1"
        }
        val sfpewLibraryPath = getSfpewLibraryPath()
        envMap["SDL_OPENGL_LIBRARY"] = sfpewLibraryPath
        envMap["SDL_EGL_LIBRARY"] = sfpewLibraryPath
    }

    if (RendererPluginManager.selectedRendererPlugin != null) return

    if (renderer.mesaBased) {
        // Mesa 系渲染器的 OSMesa 桥依据该变量定位 Mesa 库
        envMap["LIB_MESA_NAME"] = getRendererLibrary() ?: "null"
        envMap["MESA_GLSL_CACHE_DIR"] = PathManager.DIR_CACHE.absolutePath
    }

    if (renderer.needsZinkDriverOverride) {
        envMap["MESA_LOADER_DRIVER_OVERRIDE"] = "zink"
        envMap["MESA_GL_VERSION_OVERRIDE"] = "4.6"
        envMap["MESA_GLSL_VERSION_OVERRIDE"] = "460"
        envMap["force_glsl_extensions_warn"] = "true"
        envMap["allow_higher_compat_version"] = "true"
        envMap["allow_glsl_extension_directive_midshader"] = "true"
    }

    if (!envMap.containsKey("LIBGL_ES")) {
        val glesMajor = getDetectedVersion()
        Logger.info(TAG, "GLES version detected: $glesMajor")

        envMap["LIBGL_ES"] = if (glesMajor < 3) {
            //fallback to 2 since it's the minimum for the entire app
            "2"
        } else if (rendererId.startsWith("opengles")) {
            rendererId.replace("opengles", "").replace("_5", "")
        } else {
            // TODO if can: other backends such as Vulkan.
            // Sure, they should provide GLES 3 support.
            "3"
        }
    }
}

private fun getRendererLibrary(): String? {
    return if (!Renderers.isCurrentRendererValid()) null
    else Renderers.getCurrentRenderer().getRendererLibrary()
}

private fun getGameRendererLibrary(version: Version, configuredRenderer: RendererLaunchConfig?): String? {
    if (!Renderers.isCurrentRendererValid()) return null
    val renderer = Renderers.getCurrentRenderer()
    return if (isSfpewEnabledFor(renderer, version, configuredRenderer)) {
        getSfpewLibraryPath()
    } else {
        renderer.getRendererLibrary()
    }
}

/**
 * [Modified from PojavLauncher](https://github.com/PojavLauncherTeam/PojavLauncher/blob/98947f2/app_pojavlauncher/src/main/java/net/kdt/pojavlaunch/utils/JREUtils.java#L505-L516)
 */
private fun hasExtension(extensions: String, name: String): Boolean {
    var start = extensions.indexOf(name)
    while (start >= 0) {
        // check that we didn't find a prefix of a longer extension name
        val end = start + name.length
        if (end == extensions.length || extensions[end] == ' ') {
            return true
        }
        start = extensions.indexOf(name, end)
    }
    return false
}

private const val EGL_OPENGL_ES_BIT: Int = 0x0001
private const val EGL_OPENGL_ES2_BIT: Int = 0x0004
private const val EGL_OPENGL_ES3_BIT_KHR: Int = 0x0040

private fun getDetectedVersion(): Int {
    val egl = EGLContext.getEGL() as EGL10
    val display = egl.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY)
    val numConfigs = IntArray(1)
    if (egl.eglInitialize(display, null)) {
        try {
            val checkES3: Boolean = hasExtension(egl.eglQueryString(display, EGL10.EGL_EXTENSIONS), "EGL_KHR_create_context")
            if (egl.eglGetConfigs(display, null, 0, numConfigs)) {
                val configs = arrayOfNulls<EGLConfig>(
                    numConfigs[0]
                )
                if (egl.eglGetConfigs(display, configs, numConfigs[0], numConfigs)) {
                    var highestEsVersion = 0
                    val value = IntArray(1)
                    for (i in 0..<numConfigs[0]) {
                        if (egl.eglGetConfigAttrib(
                                display, configs[i],
                                EGL10.EGL_RENDERABLE_TYPE, value
                            )
                        ) {
                            if (checkES3 && ((value[0] and EGL_OPENGL_ES3_BIT_KHR) == EGL_OPENGL_ES3_BIT_KHR)) {
                                if (highestEsVersion < 3) highestEsVersion = 3
                            } else if ((value[0] and EGL_OPENGL_ES2_BIT) == EGL_OPENGL_ES2_BIT) {
                                if (highestEsVersion < 2) highestEsVersion = 2
                            } else if ((value[0] and EGL_OPENGL_ES_BIT) == EGL_OPENGL_ES_BIT) {
                                if (highestEsVersion < 1) highestEsVersion = 1
                            }
                        } else {
                            Logger.warning(TAG,
                                ("Getting config attribute with "
                                        + "EGL10#eglGetConfigAttrib failed "
                                        + "(" + i + "/" + numConfigs[0] + "): "
                                        + egl.eglGetError())
                            )
                        }
                    }
                    return highestEsVersion
                } else {
                    Logger.error(TAG,
                        "Getting configs with EGL10#eglGetConfigs failed: "
                                + egl.eglGetError()
                    )
                    return -1
                }
            } else {
                Logger.error(TAG,
                    "Getting number of configs with EGL10#eglGetConfigs failed: "
                            + egl.eglGetError()
                )
                return -2
            }
        } finally {
            egl.eglTerminate(display)
        }
    } else {
        Logger.error(TAG, "Couldn't initialize EGL.")
        return -3
    }
}
