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

package com.movtery.zalithlauncher.game.launch

import android.app.Activity
import android.os.Build
import android.os.Parcelable
import android.widget.Toast
import androidx.annotation.Keep
import androidx.compose.ui.unit.IntSize
import com.movtery.zalithlauncher.BuildConfig
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.ZLApplication
import com.movtery.zalithlauncher.bridge.LoggerBridge.append
import com.movtery.zalithlauncher.bridge.LoggerBridge.appendTitle
import com.movtery.zalithlauncher.bridge.ZLBridge
import com.movtery.zalithlauncher.context.readAssetFile
import com.movtery.zalithlauncher.game.account.Account
import com.movtery.zalithlauncher.game.account.AccountType
import com.movtery.zalithlauncher.game.account.offline.OfflineYggdrasilServer
import com.movtery.zalithlauncher.game.addons.modloader.ModLoader
import com.movtery.zalithlauncher.game.download.game.parseLibraryComponents
import com.movtery.zalithlauncher.game.multirt.Runtime
import com.movtery.zalithlauncher.game.multirt.RuntimesManager
import com.movtery.zalithlauncher.game.path.GamePathManager
import com.movtery.zalithlauncher.game.plugin.driver.DriverPluginManager
import com.movtery.zalithlauncher.game.plugin.renderer.RendererPluginManager
import com.movtery.zalithlauncher.game.renderer.Renderers
import com.movtery.zalithlauncher.game.renderer.renderers.GL4ESRenderer
import com.movtery.zalithlauncher.game.renderer.renderers.NGGL4ESRenderer
import com.movtery.zalithlauncher.game.support.touch_controller.ControllerProxy
import com.movtery.zalithlauncher.game.version.installed.Version
import com.movtery.zalithlauncher.game.version.installed.VersionInfoParser
import com.movtery.zalithlauncher.game.version.installed.VersionsManager
import com.movtery.zalithlauncher.game.versioninfo.models.GameManifest
import com.movtery.zalithlauncher.path.LibPath
import com.movtery.zalithlauncher.path.PathManager
import com.movtery.zalithlauncher.setting.AllSettings
import com.movtery.zalithlauncher.utils.GSON
import com.movtery.zalithlauncher.utils.device.Architecture
import com.movtery.zalithlauncher.utils.file.child
import com.movtery.zalithlauncher.utils.file.ensureDirectorySilently
import com.movtery.zalithlauncher.utils.logging.Logger
import com.movtery.zalithlauncher.utils.string.isBiggerTo
import com.movtery.zalithlauncher.utils.string.isEqualTo
import kotlinx.parcelize.Parcelize
import org.lwjgl.glfw.CallbackBridge
import java.io.File
import javax.microedition.khronos.egl.EGL10
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.egl.EGLContext

private const val TAG = "GameLauncher"

@Keep
@Parcelize
class LaunchConfig(
    val version: Version,
    val account: Account,
): Parcelable

class GameLauncher(
    private val activity: Activity,
    config: LaunchConfig,
    onExit: (code: Int, isSignal: Boolean) -> Unit,
    openPath: (folder: File) -> Unit
) : Launcher(onExit, openPath) {
    private lateinit var gameManifest: GameManifest
    private var jnaDir: File? = null
    private val offlineServer = OfflineYggdrasilServer(0)

    private val version = config.version
    private val usingAccount = if (version.offlineAccountLogin) {
        //使用临时离线账号启动游戏
        config.account.copy(
            accountType = AccountType.LOCAL.tag
        )
    } else {
        config.account
    }

    override fun exit() {
        offlineServer.stop()
    }

    override suspend fun launch(screenSize: IntSize): Int {
        if (!Renderers.isCurrentRendererValid()) {
            Renderers.setCurrentRenderer(version.getRenderer())
        }

        val manifest = GSON.fromJson(File(version.getVersionPath(), "${version.getVersionName()}.json").readText(), GameManifest::class.java)
        val clientJar = manifest.inheritsFrom?.let { inheritsFrom ->
            //FIXME: 依赖的是一个原版ID的版本，但这个版本可能是用户自行安装的，只是版本名称与ID一致，不保证客户端真的是对应版本
            VersionsManager.getVersion(inheritsFrom)?.getClientJar()
        } ?: version.getClientJar()

        gameManifest = VersionInfoParser(version)
            .setManifest(manifest)
            .setInheriting()
            .build()

        //jna
        jnaDir = gameManifest.libraries?.find { library ->
            library.name.startsWith("net.java.dev.jna:jna:")
        }?.let { library ->
            parseLibraryComponents(library.name).version
        }?.let { jnaVersion ->
            File(LibPath.JNA, jnaVersion)
        }?.takeIf { it.exists() }

        CallbackBridge.nativeSetUseInputStackQueue(gameManifest.arguments != null)

        val customArgs = version.getJvmArgs().takeIf { it.isNotBlank() } ?: AllSettings.jvmArgs.getValue()
        val javaRuntime = getRuntime()

        printLauncherInfo(
            javaArguments = customArgs.takeIf { it.isNotEmpty() } ?: "NONE",
            javaRuntime = javaRuntime,
        )

        return launchGame(
            screenSize = screenSize,
            clientJar = clientJar,
            javaRuntime = javaRuntime,
            customArgs = customArgs,
        )
    }

    override fun MutableMap<String, String>.putJavaArgs() {
        val versionInfo = version.getVersionInfo()
        //Fix Forge 1.7.2
        val is172 = (versionInfo?.minecraftVersion ?: "0.0").isEqualTo("1.7.2")
        if (is172 && (versionInfo?.loaderInfo?.loader == ModLoader.FORGE)) {
            Logger.debug(TAG, "Is Forge 1.7.2, use the patched sorting method.")
            put("sort.patch", "true")
        }

        //Jna
        jnaDir?.let { dir ->
            val dirPath = dir.absolutePath
            put("jna.boot.library.path", dirPath) //覆盖父类添加的jna路径
        }
    }

    override fun chdir(): String {
        return version.getGameDir().absolutePath
    }

    override fun getLogFile(): File = VersionsManager.getLatestLog(version)

    override fun initEnv(screenSize: IntSize): MutableMap<String, String> {
        val envMap = super.initEnv(screenSize)

        DriverPluginManager.setDriverById(version.getDriver())
        envMap["DRIVER_PATH"] = DriverPluginManager.getDriver().path

        checkAndUsedJSPH(envMap, runtime)
        version.getVersionInfo()?.loaderInfo?.getLoaderEnvKey()?.let { loaderKey ->
            envMap[loaderKey] = "1"
        }
        if (Renderers.isCurrentRendererValid()) {
            setRendererEnv(envMap)
        }
        envMap["ZALITH_VERSION_CODE"] = BuildConfig.VERSION_CODE.toString()
        return envMap
    }

    override fun dlopenEngine() {
        super.dlopenEngine()
        appendTitle("DLOPEN Renderer")

        //声音引擎加载后，dlopen渲染器的库
        RendererPluginManager.selectedRendererPlugin?.let { renderer ->
            val libs by renderer.getDlopenLibrary()
            libs.forEach { libPath ->
                ZLBridge.dlopen(libPath)
            }
        }

        val rendererLib = loadGraphicsLibrary() ?: return
        if (!ZLBridge.dlopen(rendererLib) && !ZLBridge.dlopen(findInLdLibPath(rendererLib))) {
            Logger.error(TAG, "Failed to load renderer $rendererLib")
        }
    }

    override fun progressFinalUserArgs(args: MutableList<String>, ramAllocation: Int) {
        super.progressFinalUserArgs(args, version.getRamAllocation(activity))
        if (Renderers.isCurrentRendererValid()) {
            args.add("-Dorg.lwjgl.opengl.libname=${loadGraphicsLibrary()}")
        }
    }

    private suspend fun launchGame(
        screenSize: IntSize,
        clientJar: File,
        javaRuntime: String,
        customArgs: String
    ): Int {
        val runtime = RuntimesManager.forceReload(javaRuntime)

        val gameDirPath = version.getGameDir()

        disableSplash(gameDirPath)

        //初始化运行环境
        this.runtime = runtime
        val launchArgs = LaunchArgs(
            runtimeLibraryPath = getRuntimeLibraryPath(),
            account = usingAccount,
            offlineServer = offlineServer,
            gameDirPath = gameDirPath,
            version = version,
            clientJar = clientJar,
            gameManifest = gameManifest,
            runtime = runtime,
            readAssetsFile = { path -> activity.readAssetFile(path) },
            getCacioJavaArgs = { isJava8 ->
                getCacioJavaArgs(screenSize, isJava8)
            }
        ).getAllArgs()

        tryStartTouchProxy()

        return launchJvm(
            context = activity,
            jvmArgs = launchArgs,
            userHome = GamePathManager.getCurrentPath(),
            userArgs = customArgs,
            screenSize = screenSize
        )
    }

    override fun getRuntimeLibraryPath(): String {
        val parent = super.getRuntimeLibraryPath()
        return jnaDir?.absolutePath?.let { dirPath ->
            "$parent:$dirPath"
        } ?: parent
    }

    private fun tryStartTouchProxy() {
        if (version.enableTouchProxy) {
            ControllerProxy.startProxy(
                context = activity,
                vibrateDuration = version.getTouchVibrateDuration(),
                vibrateKind = version.getTouchVibrateKind(),
            )
        }
    }

    private fun printLauncherInfo(
        javaArguments: String,
        javaRuntime: String,
    ) {
        var mcInfo = version.getVersionName()
        version.getVersionInfo()?.let { info -> mcInfo = info.getInfoString() }
        val renderer = Renderers.getCurrentRenderer()

        appendTitle("Launch Minecraft")
        append("▷ Launcher version: ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})")
        append("▷ Architecture: ${Architecture.archAsString(ZLApplication.DEVICE_ARCHITECTURE)}")
        append("▷ Device model: ${Build.MANUFACTURER}, ${Build.MODEL}")
        append("▷ API version: ${Build.VERSION.SDK_INT}")
        append("▷ Renderer: ${renderer.getRendererName()}")
        renderer.getRendererSummary()?.let { summary ->
            append("▷ Renderer Summary: $summary")
        }
        append("▷ Selected Minecraft version: ${version.getVersionName()}")
        append("▷ Minecraft Info: $mcInfo")
        append("▷ Game Path: ${version.getGameDir().absolutePath} (Isolation: ${version.isIsolation()})")
        append("▷ Custom Java arguments: $javaArguments")
        append("▷ Java Runtime: $javaRuntime")
        append("▷ Account: ${usingAccount.username} (${usingAccount.accountType})")
    }

    /**
     * 获取Java运行环境名称，
     * 如果版本独立设置了运行环境，则直接选定它；
     * 如果版本未设置，则根据全局设置或自动选择
     */
    private fun getRuntime(): String {
        val versionRuntime = version.getJavaRuntime().takeIf { it.isNotEmpty() } ?: ""
        if (versionRuntime.isNotEmpty()) return versionRuntime

        val runtime = AllSettings.javaRuntime.getValue()
        val pickedRuntime = RuntimesManager.loadRuntime(runtime)

        if (AllSettings.autoPickJavaRuntime.getValue()) {
            val loaderInfo = version.getVersionInfo()?.loaderInfo
            //开启了自动选择，根据游戏需求的版本做选择
            val targetJavaVersion = when (loaderInfo?.loader) {
                ModLoader.BABRIC -> 17 //Babric 推荐使用 17
                ModLoader.CLEANROOM -> {
                    if (loaderInfo.version.isBiggerTo("0.4.4-alpha")) {
                        25 //0.5.0-alpha 及以上要求使用 25
                    } else {
                        21 //0.4.4-alpha 及以下要求使用 21
                    }
                }
                else -> gameManifest.javaVersion?.majorVersion ?: 8
            }
            if (pickedRuntime.javaVersion == 0 || pickedRuntime.javaVersion < targetJavaVersion) {
                val runtime0 = RuntimesManager.getNearestJreName(targetJavaVersion)
                if (runtime0 != null) {
                    return runtime0
                } else {
                    activity.runOnUiThread {
                        Toast.makeText(activity, activity.getString(R.string.game_auto_pick_runtime_failed), Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
        return runtime
    }

    /**
     * 禁用Forge的启动屏幕
     * [Modified from PojavLauncher](https://github.com/PojavLauncherTeam/PojavLauncher/blob/a6f3fc0/app_pojavlauncher/src/main/java/net/kdt/pojavlaunch/Tools.java#L372-L391)
     */
    private fun disableSplash(dir: File) {
        File(dir, "config").let { configDir ->
            if (configDir.ensureDirectorySilently()) {
                val forgeSplashFile = configDir.child("splash.properties")
                runCatching {
                    var forgeSplashContent = "enabled=true"
                    if (forgeSplashFile.exists()) {
                        forgeSplashContent = forgeSplashFile.readText()
                    }
                    if (forgeSplashContent.contains("enabled=true")) {
                        forgeSplashFile.writeText(
                            forgeSplashContent.replace("enabled=true", "enabled=false")
                        )
                    }
                }.onFailure {
                    Logger.warning(TAG, "Could not disable Forge 1.12.2 and below splash screen!", it)
                }
            } else {
                Logger.warning(TAG, "Failed to create the configuration directory")
            }
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

private fun setRendererEnv(envMap: MutableMap<String, String>) {
    val renderer = Renderers.getCurrentRenderer()
    val rendererId = renderer.getRendererId()

    if (rendererId.startsWith("opengles2")) {
        envMap["LIBGL_ES"] = "2"
        envMap["LIBGL_MIPMAP"] = "3"
        envMap["LIBGL_NOERROR"] = "1"
        envMap["LIBGL_NOINTOVLHACK"] = "1"
        envMap["LIBGL_NORMALIZE"] = "1"
    }

    envMap += renderer.getRendererEnv().value

    renderer.getRendererEGL()?.let { eglName ->
        envMap["POJAVEXEC_EGL"] = eglName
    }

    envMap["POJAV_RENDERER"] = rendererId

    if (RendererPluginManager.selectedRendererPlugin != null) return

    if (renderer != GL4ESRenderer && renderer != NGGL4ESRenderer) {
        envMap["MESA_LOADER_DRIVER_OVERRIDE"] = "zink"
        envMap["MESA_GLSL_CACHE_DIR"] = PathManager.DIR_CACHE.absolutePath
        envMap["MESA_GL_VERSION_OVERRIDE"] = "4.6"
        envMap["MESA_GLSL_VERSION_OVERRIDE"] = "460"
        envMap["force_glsl_extensions_warn"] = "true"
        envMap["allow_higher_compat_version"] = "true"
        envMap["allow_glsl_extension_directive_midshader"] = "true"
        envMap["LIB_MESA_NAME"] = loadGraphicsLibrary() ?: "null"
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

/**
 * Open the render library in accordance to the settings.
 * It will fallback if it fails to load the library.
 * @return The name of the loaded library
 */
private fun loadGraphicsLibrary(): String? {
    return if (!Renderers.isCurrentRendererValid()) null
    else Renderers.getCurrentRenderer().getRendererLibrary()
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