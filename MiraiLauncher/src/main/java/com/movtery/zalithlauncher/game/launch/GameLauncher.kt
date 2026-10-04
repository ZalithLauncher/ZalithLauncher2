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
import com.movtery.zalithlauncher.bridge.LoggerBridge.appendInfo
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
import com.movtery.zalithlauncher.game.optimization.JvmGcAutoTuner
import com.movtery.zalithlauncher.game.plugin.Plugin
import com.movtery.zalithlauncher.game.plugin.driver.DriverPluginManager
import com.movtery.zalithlauncher.game.plugin.renderer.RendererPluginManager
import com.movtery.zalithlauncher.game.renderer.Renderers
import com.movtery.zalithlauncher.game.renderer.renderers.GL4ESRenderer
import com.movtery.zalithlauncher.game.renderer.renderers.LTWLegacyRenderer
import com.movtery.zalithlauncher.game.renderer.renderers.LTWRenderer
import com.movtery.zalithlauncher.game.renderer.renderers.MobileGluesRenderer
import com.movtery.zalithlauncher.game.renderer.renderers.NGGL4ESRenderer
import com.movtery.zalithlauncher.game.renderer.renderers.VGPU1368Renderer
import com.movtery.zalithlauncher.game.renderer.renderers.VGPURenderer
import com.movtery.zalithlauncher.game.support.touch_controller.ControllerProxy
import com.movtery.zalithlauncher.game.version.installed.Version
import com.movtery.zalithlauncher.game.version.installed.VersionInfoParser
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
            version.getInheritedClientJar(inheritsFrom)
        } ?: version.getClientJar()

        gameManifest = version.launchManifest?.let { json ->
            runCatching {
                GSON.fromJson(json, GameManifest::class.java)
            }.onFailure {
                Logger.warning(TAG, "Failed to parse the carried launch manifest", it)
            }.getOrNull()
        } ?: VersionInfoParser(version)
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

        initLwjglComponent(activity, detectLwjglVersion(gameManifest))

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

    override fun getMinecraftPath(): String = version.getGameHome()

    override fun getLogFile(): File = version.getLatestLog()

    override fun initEnv(screenSize: IntSize): MutableMap<String, String> {
        val envMap = super.initEnv(screenSize)

        envMap["DRIVER_PATH"] = DriverPluginManager.getDriver(version.getDriver()).path

        checkAndUsedJSPH(envMap, runtime)
        version.getVersionInfo()?.loaderInfo?.getLoaderEnvKey()?.let { loaderKey ->
            envMap[loaderKey] = "1"
        }
        if (Renderers.isCurrentRendererValid()) {
            setRendererEnv(envMap)
        }
        envMap["ZALITH_VERSION_CODE"] = BuildConfig.VERSION_CODE.toString()

        //lwjgl3ify 实例：注入 XDG 数据目录兜底，避免其向不存在的 ~/.local/share
        //写桌面快捷方式时抛出异常杀死 RFB 主线程，导致游戏静默退出
        if (File(version.getGameDir(), "config/lwjgl3ify.cfg").isFile()) {
            val xdgDataHome = File(version.getGameDir(), ".local/share")
            if (!xdgDataHome.isDirectory) xdgDataHome.mkdirs()
            envMap["XDG_DATA_HOME"] = xdgDataHome.absolutePath
        }
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

        val rendererLib = getRendererLibrary() ?: return
        if (!ZLBridge.dlopen(rendererLib) && !ZLBridge.dlopen(findInLdLibPath(rendererLib))) {
            Logger.error(TAG, "Failed to load renderer $rendererLib")
        }
    }

    override fun progressFinalUserArgs(args: MutableList<String>, ramAllocation: Int) {
        val allocMb = version.getRamAllocation(activity)
        super.progressFinalUserArgs(args, allocMb)
        JvmGcAutoTuner.sanitizeAndInjectGcArgs(
            args = args,
            javaMajor = runtime.javaVersion,
            ramAllocationMb = allocMb
        )
        if (Renderers.isCurrentRendererValid()) {
            args.add("-Dorg.lwjgl.opengl.libname=${getRendererLibrary()}")
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
        configureVgpuAndLegacyCompatibility(gameDirPath)
        writeMobileGluesConfig()

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
            lwjglVersion = lwjglVersion,
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
            userHome = version.getGameHome(),
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
        appendInfo("Launcher version: ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})")
        appendInfo("Architecture: ${Architecture.archAsString(ZLApplication.DEVICE_ARCHITECTURE)}")
        appendInfo("Device model: ${Build.MANUFACTURER}, ${Build.MODEL}")
        appendInfo("API version: ${Build.VERSION.SDK_INT}")
        appendInfo("Renderer: ${renderer.getRendererName()}")
        renderer.getRendererSummary()?.let { summary ->
            appendInfo("Renderer Summary: $summary")
        }
        appendInfo("Selected Minecraft version: ${version.getVersionName()}")
        appendInfo("Minecraft Info: $mcInfo")
        appendInfo("Game Path: ${version.getGameDir().absolutePath} (Isolation: ${version.isIsolation()})")
        appendInfo("Custom Java arguments: $javaArguments")
        appendInfo("Java Runtime: $javaRuntime")
        appendInfo("Account: ${usingAccount.username} (${usingAccount.accountType})")
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
            JvmGcAutoTuner.resolveOptimalRuntimeForLaunch(version, gameManifest)?.let { optimalRuntime ->
                return optimalRuntime
            }
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

    /**
     * Ensures 1.16.5 + Fabric/Forge + Sodium-family mods (Sodium, Embedium, Rubidium)
     * and OptiFine/Iris shaderpacks run smoothly without errors on EVERY renderer:
     * 1. Patches `use_chunk_multidraw = false` (plus the safe `chunk_renderer_backend`)
     *    in the installed mods' options files (`sodium-options.json`,
     *    `embedium-options.json`, `rubidium-options.json`) so the chunk renderer uses
     *    the Oneshot backend instead of calling `glMultiDrawArraysIndirect(GL_QUADS)` —
     *    the call that crashes instantly after joining a world on renderers without
     *    full multidraw support. Applied for all renderers: it only affects the game
     *    when a Sodium-family mod is actually installed.
     * 2. Disables OptiFine `ofFastRender` / `ofAaLevel` in `optionsof.txt` if present so FBO
     *    shaderpacks render cleanly (legacy GLES renderers only).
     * 3. Downgrades `graphicsMode:2` (Fabulous) to `graphicsMode:1` (Fancy) in `options.txt` if
     *    present so vanilla Fabulous depth-layer FBOs do not conflict with VGPU/shaders
     *    (legacy GLES renderers only).
     */
    private fun configureVgpuAndLegacyCompatibility(dir: File) {
        //Sodium-family patch first and always: it is renderer-independent and only
        //touches families that are installed (or left a config behind).
        runCatching {
            patchSodiumFamilyOptions(dir)
        }.onFailure {
            Logger.warning(TAG, "Failed to apply Sodium compatibility configuration", it)
        }

        if (!Renderers.isCurrentRendererValid()) return
        val renderer = Renderers.getCurrentRenderer()
        val isVgpuOrLegacyGles = renderer == VGPURenderer ||
            renderer == VGPU1368Renderer ||
            renderer == LTWLegacyRenderer ||
            renderer == GL4ESRenderer ||
            renderer == NGGL4ESRenderer
        if (!isVgpuOrLegacyGles) return


        runCatching {
            val optionsOfFile = File(dir, "optionsof.txt")
            if (optionsOfFile.exists() && optionsOfFile.isFile) {
                val ofText = optionsOfFile.readText()
                val updatedOfText = ofText
                    .replace("ofFastRender:true", "ofFastRender:false")
                    .replace(Regex("ofAaLevel:[1-9]\\d*"), "ofAaLevel:0")
                if (updatedOfText != ofText) {
                    optionsOfFile.writeText(updatedOfText)
                }
            }

            val optionsFile = File(dir, "options.txt")
            if (optionsFile.exists() && optionsFile.isFile) {
                val text = optionsFile.readText()
                if (text.contains("graphicsMode:2")) {
                    optionsFile.writeText(text.replace("graphicsMode:2", "graphicsMode:1"))
                }
            }
        }.onFailure {
            Logger.warning(TAG, "Failed to apply VGPU/Sodium compatibility configuration", it)
        }
    }

    /**
     * Patches every installed Sodium-family mod (Sodium, Embedium, Rubidium) so its
     * chunk renderer uses the crash-free Oneshot backend on ALL renderers. Only families
     * that are installed (or left a config behind) are touched.
     */
    private fun patchSodiumFamilyOptions(dir: File) {
        val configDir = File(dir, "config")
        if (!configDir.ensureDirectorySilently()) return
        //Scanning mods/ avoids writing junk configs for mods the player never had.
        val modJars = runCatching {
            File(dir, "mods").listFiles()?.map { it.name.lowercase() }.orEmpty()
        }.getOrDefault(emptyList())
        val targets = listOf(
            "sodium-options.json" to "sodium",
            "embedium-options.json" to "embedium",
            "rubidium-options.json" to "rubidium"
        ).filter { (configName, fragment) ->
            modJars.any { fragment in it } || File(configDir, configName).exists()
        }
        if (targets.isEmpty()) return
        targets.forEach { (configName, _) ->
            val optionsFile = File(configDir, configName)
            if (optionsFile.exists() && optionsFile.isFile) {
                patchExistingSodiumOptions(optionsFile)
            } else {
                writeSafeSodiumOptions(optionsFile)
            }
        }
    }

    /**
     * Forces the crash-free chunk backend in an existing Sodium-family options file.
     */
    private fun patchExistingSodiumOptions(optionsFile: File) {
        var content = optionsFile.readText()
        var modified = false
        if (content.contains(Regex("\"use_chunk_multidraw\"\\s*:\\s*true"))) {
            content = content.replace(
                Regex("\"use_chunk_multidraw\"\\s*:\\s*true"),
                "\"use_chunk_multidraw\": false"
            )
            modified = true
        } else if (!content.contains(Regex("\"use_chunk_multidraw\"\\s*:"))) {
            //Key missing entirely: inject the safe value into the "advanced" block
            //when there is one, otherwise Sodium falls back to multidraw and crashes
            //right after joining the world on weaker renderers.
            val advancedBlock = Regex("\"advanced\"\\s*:\\s*\\{")
            if (advancedBlock.containsMatchIn(content)) {
                content = advancedBlock.replaceFirst(
                    content,
                    "$0\n    \"use_chunk_multidraw\": false,"
                )
                modified = true
            }
        }
        if (content.contains(Regex("\"chunk_renderer_backend\"\\s*:\\s*\"GL43\""))) {
            content = content.replace(
                Regex("\"chunk_renderer_backend\"\\s*:\\s*\"GL43\""),
                "\"chunk_renderer_backend\": \"GL30\""
            )
            modified = true
        }
        if (modified) {
            optionsFile.writeText(content)
        }
    }

    /**
     * Writes a fresh crash-free Sodium-family options file (first run, no config yet).
     */
    private fun writeSafeSodiumOptions(optionsFile: File) {
        optionsFile.writeText(
            """
            {
              "quality": {
                "cloud_quality": "FAST",
                "weather_quality": "DEFAULT",
                "enable_vignette": false,
                "enable_clouds": true,
                "smooth_lighting": "HIGH"
              },
              "advanced": {
                "use_vertex_array_objects": true,
                "use_chunk_multidraw": false,
                "chunk_renderer_backend": "GL30",
                "animate_only_visible_textures": true,
                "use_entity_culling": true,
                "use_particle_culling": true,
                "use_fog_occlusion": true,
                "use_compact_vertex_format": true,
                "use_block_face_culling": true,
                "allow_direct_memory_access": true,
                "ignore_driver_blacklist": false
              },
              "notifications": {
                "hide_donation_button": true
              }
            }
            """.trimIndent() + "\n"
        )
    }

    /**
     * Writes the MobileGlues MG-ES config.json when MobileGlues is the active renderer.
     * MG reads <MG_DIR_PATH>/config.json at startup; writing it explicitly pins the
     * tested performance profile: error checking off (fastest path), everything else
     * at upstream defaults (no ANGLE, no FSR, stock multidraw and extension set).
     */
    private fun writeMobileGluesConfig() {
        if (!Renderers.isCurrentRendererValid()) return
        if (Renderers.getCurrentRenderer() != MobileGluesRenderer) return
        runCatching {
            val mgDir = File(PathManager.DIR_FILES_PRIVATE, "MobileGlues")
            if (!mgDir.exists() && !mgDir.mkdirs()) return
            File(mgDir, "config.json").writeText(
                """
                {"enableANGLE":0,"enableNoError":1,"fsr1Setting":0,"enableExtComputeShader":0,"angleDepthClearFixMode":0,"enableExtTimerQuery":0,"enableExtDirectStateAccess":0,"multidrawMode":0,"maxGlslCacheSize":128}
                """.trimIndent() + "\n"
            )
        }.onFailure {
            Logger.warning(TAG, "Failed to write MobileGlues config.json", it)
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

    // SDL 环境变量
    envMap["SDL_OPENGL_LIBRARY"] = rendererId

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

        // 指定 SDL EGL
        val nativeLibPath = if (renderer is Plugin) {
            renderer.getNativeLibPath()
        } else {
            PathManager.DIR_NATIVE_LIB
        }
        envMap["SDL_EGL_LIBRARY"] = "$nativeLibPath/$eglName"
    }

    envMap["POJAV_RENDERER"] = rendererId

    if (RendererPluginManager.selectedRendererPlugin != null) return

    // LTW, LTW Legacy, VGPU and MobileGlues are self-contained GLES-backed wrappers that
    // bring their own GL implementation. Forcing the Zink/Mesa path here would load a second
    // GL implementation beside them and the game would render through the wrong one.
    if (renderer != GL4ESRenderer && renderer != NGGL4ESRenderer &&
        renderer != LTWRenderer && renderer != LTWLegacyRenderer &&
        renderer != VGPURenderer && renderer != VGPU1368Renderer &&
        renderer != MobileGluesRenderer) {
        envMap["MESA_LOADER_DRIVER_OVERRIDE"] = "zink"
        envMap["MESA_GLSL_CACHE_DIR"] = PathManager.DIR_CACHE.absolutePath
        envMap["MESA_GL_VERSION_OVERRIDE"] = "4.6"
        envMap["MESA_GLSL_VERSION_OVERRIDE"] = "460"
        envMap["force_glsl_extensions_warn"] = "true"
        envMap["allow_higher_compat_version"] = "true"
        envMap["allow_glsl_extension_directive_midshader"] = "true"
        envMap["LIB_MESA_NAME"] = getRendererLibrary() ?: "null"
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