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

import android.content.Context
import android.os.LocaleList
import android.system.Os
import android.util.ArrayMap
import androidx.compose.ui.unit.IntSize
import com.movtery.zalithlauncher.bridge.LoggerBridge
import com.movtery.zalithlauncher.bridge.ZLBridge
import com.movtery.zalithlauncher.bridge.ZLNativeInvoker
import com.movtery.zalithlauncher.components.Components
import com.movtery.zalithlauncher.components.InstallableItem
import com.movtery.zalithlauncher.components.UnpackComponentsTask
import com.movtery.zalithlauncher.context.GlobalContext
import com.movtery.zalithlauncher.game.multirt.Runtime
import com.movtery.zalithlauncher.game.multirt.RuntimesManager
import com.movtery.zalithlauncher.game.path.getGameHome
import com.movtery.zalithlauncher.game.plugin.ffmpeg.FFmpegPluginManager
import com.movtery.zalithlauncher.game.plugin.natives.NativePluginManager
import com.movtery.zalithlauncher.game.plugin.renderer.RendererPluginManager
import com.movtery.zalithlauncher.path.LibPath
import com.movtery.zalithlauncher.path.PathManager
import com.movtery.zalithlauncher.setting.AllSettings
import com.movtery.zalithlauncher.utils.device.Architecture
import com.movtery.zalithlauncher.utils.device.Architecture.ARCH_X86
import com.movtery.zalithlauncher.utils.device.Architecture.is64BitsDevice
import com.movtery.zalithlauncher.utils.logging.Logger
import com.movtery.zalithlauncher.utils.network.getSystemDnsServerAddresses
import com.movtery.zalithlauncher.utils.string.splitPreservingQuotes
import com.oracle.dalvik.VMLauncher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.commons.io.FileUtils
import java.io.File
import java.io.IOException
import java.util.Locale

private const val TAG = "Launcher"

abstract class Launcher(
    val onExit: (code: Int, isSignal: Boolean) -> Unit,
    val openPath: (folder: File) -> Unit
) {
    lateinit var runtime: Runtime
        protected set

    /** 当前启动版本要求的 LWJGL 版本（见 [detectLwjglVersion]），0 = 未探测/默认 */
    protected var lwjglVersion: Int = 0
        private set

    /**
     * 当前启动版本的 LWJGL natives 目录
     */
    protected lateinit var lwjglNativesDir: String
        private set

    /**
     * 初始化 LWJGL 组件
     */
    protected suspend fun initLwjglComponent(context: Context, version: Int) {
        check(!::lwjglNativesDir.isInitialized) { "LWJGL component has already been initialized" }
        lwjglVersion = version
        lwjglNativesDir = File(
            PathManager.DIR_COMPONENTS,
            "lwjgl/${lwjglVersionDir(version)}/natives/${Architecture.archAsStringAndroid(Architecture.getDeviceArchitecture())}"
        ).absolutePath
        verifyLwjglNatives(context)
    }

    private suspend fun verifyLwjglNatives(context: Context) {
        val versionDir = lwjglVersionDir(lwjglVersion)
        val component = Components.entries.firstOrNull {
            it.component == "lwjgl/$versionDir"
        } ?: error("Unrecognized LWJGL component: lwjgl/$versionDir")
        val task = UnpackComponentsTask(context, component)

        withContext(Dispatchers.IO) {
            if (task.checkState() == InstallableItem.State.FINISHED) {
                LoggerBridge.appendInfo("LWJGL: LWJGL $versionDir passed SHA-1 verification")
                return@withContext
            }

            LoggerBridge.appendInfo("LWJGL: LWJGL $versionDir failed verification, re-unpacking the component now")
            task.run()
            check(task.checkState() == InstallableItem.State.FINISHED) {
                "LWJGL component failed SHA-1 verification after re-unpack: $versionDir"
            }
            LoggerBridge.appendInfo("LWJGL: LWJGL $versionDir passed SHA-1 verification after re-unpack")
        }
    }

    private val runtimeHome: String by lazy {
        RuntimesManager.getRuntimeHome(runtime.name).absolutePath
    }

    private fun getJavaHome() = if (runtime.isJDK8) "$runtimeHome/jre" else runtimeHome

    abstract suspend fun launch(screenSize: IntSize): Int
    abstract fun chdir(): String
    abstract fun getLogFile(): File
    abstract fun exit()

    /**
     * 游戏目录（.minecraft），默认为当前选择的游戏目录
     */
    protected open fun getMinecraftPath(): String = getGameHome()

    protected suspend fun launchJvm(
        context: Context,
        profile: LaunchProfile,
        userHome: String,
        screenSize: IntSize,
        useLocalLanguage: Boolean = true
    ): Int {
        ZLNativeInvoker.staticLauncher = this

        ZLBridge.setLdLibraryPath(getRuntimeLibraryPath())

        val ctx = LaunchContext(
            runtime = runtime,
            javaHome = getJavaHome(),
            screenSize = screenSize,
            lwjglNativesDir = lwjglNativesDir,
            libraryPath = getLibraryPath(),
            userHome = userHome,
            useLocalLanguage = useLocalLanguage,
            resolvFile = ensureDNSConfig(),
            minecraftPath = getMinecraftPath()
        )

        LoggerBridge.appendTitle("Env Map")
        setEnv(ctx, profile)

        LoggerBridge.appendTitle("DLOPEN Java Runtime")
        dlopenJavaRuntime()

        dlopenEngine(ctx, profile)

        return launchJavaVM(context, profile, ctx, screenSize)
    }

    //伪 suspend 函数，等待 JVM 的退出代码
    private suspend fun launchJavaVM(
        context: Context,
        profile: LaunchProfile,
        ctx: LaunchContext,
        screenSize: IntSize
    ): Int {
        val userArguments = profile.userJvmArgs().splitPreservingQuotes()
        val args = buildOverridableJavaArgs(ctx, profile, userArguments).toMutableList()

        args.addAll(profile.preJvmArgs())
        args.addAll(getCacioJavaArgs(screenSize, runtime.javaVersion == 8))
        args.addAll(userArguments)
        applyFinalArgs(args, profile.ramAllocation())

        args.addAll(profile.jvmArgs())
        args.addAll(profile.programArgs())
        args.add(0, "$runtimeHome/bin/java")

        LoggerBridge.appendTitle("JVM Args")
        val iterator = args.iterator()
        while (iterator.hasNext()) {
            val arg = iterator.next()
            if (arg.startsWith("--accessToken") && iterator.hasNext()) {
                iterator.next()
                LoggerBridge.appendInfo(arg)
                LoggerBridge.appendInfo("********************")
                continue
            }
            LoggerBridge.appendInfo(arg)
        }

        ZLBridge.setupExitMethod(context.applicationContext)
        ZLBridge.initializeGameExitHook()
        ZLBridge.chdir(chdir())

        val exitCode = VMLauncher.launchJVM(args.toTypedArray())
        LoggerBridge.append("Java Exit code: $exitCode")
        return exitCode
    }

    /**
     * 清除用户指定的受限参数
     */
    private fun applyFinalArgs(args: MutableList<String>, ramAllocation: Int) {
        args.purgeRestrictedArgs()

        //Add automatically generated args
        val ramAllocationString = ramAllocation.toString()
        args.add("-Xms${ramAllocationString}M")
        args.add("-Xmx${ramAllocationString}M")

        args.add("-Dorg.lwjgl.openal.libname=${PathManager.DIR_NATIVE_LIB}/libopenal.so")

        // Force LWJGL to use the Freetype library intended for it, instead of using the one
        // that we ship with Java (since it may be older than what's needed).
        // 始终指向 LWJGL 组件 natives 目录内的库，禁止回退到应用原生 libs 目录（其中不含 LWJGL 系库）
        args.add("-Dorg.lwjgl.freetype.libname=${File(lwjglNativesDir, "libfreetype.so").absolutePath}")

        // Our spirv-cross is compiled shared, so it gets named shared.
        args.add("-Dorg.lwjgl.spvc.libname=spirv-cross-c-shared")

        // We don't have jemalloc for our LWJGL so set the allocator to system to avoid error logs
        args.add("-Dorg.lwjgl.system.allocator=system")

        // Some phones are not using the right number of cores, fix that
        args.add("-XX:ActiveProcessorCount=${java.lang.Runtime.getRuntime().availableProcessors()}")
    }

    /**
     * 确保 DNS 配置文件存在
     */
    private fun ensureDNSConfig(): File {
        val resolvFile = File(PathManager.DIR_GAME, "resolv.conf")
        val servers = buildResolvConfSet()
        Logger.info(TAG, "Using DNS servers for game: $servers")
        val configText = servers.joinToString(separator = "\n") { "nameserver $it" }
        runCatching {
            // 配置文件不存在或内容不一致时覆写一次
            if (!resolvFile.exists() || resolvFile.readText().trim() != configText.trim()) {
                resolvFile.writeText(configText)
            }
        }.onFailure {
            Logger.warning(TAG, "Failed to create resolv.conf", it)
            FileUtils.deleteQuietly(resolvFile)
        }
        return resolvFile
    }

    private fun buildResolvConfSet(): Set<String> {
        return buildSet {
            getSystemDnsServerAddresses()
                // JNDI DNS 的 nameserver 解析无法处理裸 IPv6 地址，仅保留 IPv4
                ?.filterNot { it.contains(':') }
                ?.let { addAll(it) }

            // 按地区获取公共 DNS
            if (LocaleList.getDefault().get(0).displayName != Locale.CHINA.displayName) {
                add("1.1.1.1")
                add("1.0.0.1")
            } else {
                add("223.5.5.5")
                add("119.29.29.29")
            }
        }
    }

    protected fun getJavaLibDir(): String {
        val architecture = runtime.arch?.let { arch ->
            if (Architecture.archAsInt(arch) == ARCH_X86) "i386/i486/i586"
            else arch
        } ?: throw IOException("Unsupported runtime environment: ${runtime.name}, arch is null!")

        var libDir = "/lib"
        architecture.split("/").forEach { arch ->
            val file = File(runtimeHome, "lib/$arch")
            if (file.exists() && file.isDirectory()) {
                libDir = "/lib/$arch"
            }
        }
        return libDir
    }

    private fun getJvmLibDir(): String {
        val jvmLibDir: String
        val path = (if (RuntimesManager.isJDK8(runtimeHome)) "/jre" else "") + getJavaLibDir()
        val jvmFile = File("$runtimeHome$path/server/libjvm.so")
        jvmLibDir = if (jvmFile.exists()) "/server" else "/client"
        return jvmLibDir
    }

    protected open fun getRuntimeLibraryPath(): String {
        val javaLibDir = getJavaLibDir()
        val jvmLibDir = getJvmLibDir()

        val libName = if (is64BitsDevice) "lib64" else "lib"
        val paths = buildList {
            FFmpegPluginManager.takeIf { it.isAvailable }?.libraryPath?.let { add(it) }
            RendererPluginManager.selectedRendererPlugin?.path?.let { add(it) }
            addAll(NativePluginManager.getPaths())
            add("$runtimeHome$javaLibDir/jli")
            if (runtime.isJDK8) {
                add("$runtimeHome/jre$javaLibDir$jvmLibDir:$runtimeHome/jre$javaLibDir")
            } else {
                add("$runtimeHome$javaLibDir$jvmLibDir")
            }
            add("/system/$libName")
            add("/vendor/$libName")
            add("/vendor/$libName/hw")
            add("/system_ext/$libName")
            add(LibPath.JNA.absolutePath)
            PathManager.DIR_RUNTIME_MOD?.absolutePath?.let { add(it) }
            add(lwjglNativesDir)
            add(PathManager.DIR_NATIVE_LIB)
        }
        return paths.joinToString(":")
    }

    protected fun getLibraryPath(): String {
        val libDirName = if (is64BitsDevice) "lib64" else "lib"
        val path = listOfNotNull(
            // per-version LWJGL natives 优先，避免 APK 内旧版 native 抢占
            lwjglNativesDir,
            "/system/$libDirName",
            "/vendor/$libDirName",
            "/vendor/$libDirName/hw",
            "/system_ext/$libDirName",
            RendererPluginManager.selectedRendererPlugin?.path,
            PathManager.DIR_RUNTIME_MOD?.absolutePath,
            PathManager.DIR_NATIVE_LIB
        )
        return path.joinToString(":")
    }

    private fun locateLibs(path: File): List<File> {
        val children = path.listFiles() ?: return emptyList()
        return children.flatMap { file ->
            when {
                file.isFile && file.name.endsWith(".so") -> listOf(file)
                file.isDirectory -> locateLibs(file)
                else -> emptyList()
            }
        }
    }

    private fun setEnv(ctx: LaunchContext, profile: LaunchProfile) {
        val envMap = runCatching {
            initEnv(ctx, profile)
        }.onFailure {
            LoggerBridge.appendTitle("Init Env Failed")
            LoggerBridge.append(it.stackTraceToString())
        }.getOrThrow()
        envMap.forEach { (key, value) ->
            LoggerBridge.appendInfo("$key = $value")
            runCatching {
                Os.setenv(key, value, true)
            }.onFailure {
                Logger.error(TAG, "Unable to set environment variable.", it)
            }
        }
    }

    private fun initEnv(ctx: LaunchContext, profile: LaunchProfile): MutableMap<String, String> {
        val envMap: MutableMap<String, String> = ArrayMap()
        setJavaEnv(
            screenSize = ctx.screenSize,
            envMap = { envMap }
        )
        profile.onInitEnv(ctx, envMap)
        return envMap
    }

    private fun setJavaEnv(
        screenSize: IntSize,
        envMap: () -> MutableMap<String, String>
    ) {
        val path = listOfNotNull("$runtimeHome/bin", Os.getenv("PATH"))

        envMap().let { map ->
            map["POJAV_NATIVEDIR"] = PathManager.DIR_NATIVE_LIB
            map["JAVA_HOME"] = getJavaHome()
            map["HOME"] = PathManager.DIR_FILES_EXTERNAL.absolutePath
            map["TMPDIR"] = PathManager.DIR_CACHE.absolutePath
            map["LD_LIBRARY_PATH"] = getLibraryPath()
            map["PATH"] = path.joinToString(":")
            map["AWTSTUB_WIDTH"] = screenSize.width.toString()
            map["AWTSTUB_HEIGHT"] = screenSize.height.toString()
            map["MOD_ANDROID_RUNTIME"] = PathManager.DIR_RUNTIME_MOD?.absolutePath ?: ""
            map["DALVIK_JAVAVM"] = ZLBridge.getJavaVMPointer().toString()
            map["DALVIK_APPLICATION"] = ZLBridge.jObjectToString(GlobalContext.applicationContext)
            map["ALSOFT_DRIVERS"] = "opensl"

            if (AllSettings.dumpShaders.getValue()) map["LIBGL_VGPU_DUMP"] = "1"
            if (AllSettings.zinkPreferSystemDriver.getValue()) map["POJAV_ZINK_PREFER_SYSTEM_DRIVER"] = "1"
            if (AllSettings.vsyncInZink.getValue()) map["POJAV_VSYNC_IN_ZINK"] = "1"

            if (FFmpegPluginManager.isAvailable) map["POJAV_FFMPEG_PATH"] = FFmpegPluginManager.executablePath!!
        }
    }

    private fun dlopenJavaRuntime() {
        var javaLibDir = "$runtimeHome${getJavaLibDir()}"
        val jliLibDir = if (File("$javaLibDir/jli/libjli.so").exists()) "$javaLibDir/jli" else javaLibDir

        if (runtime.isJDK8) {
            javaLibDir = "$runtimeHome/jre${getJavaLibDir()}"
        }
        val jvmLibDir = "$javaLibDir${getJvmLibDir()}"
        ZLBridge.dlopen("$jliLibDir/libjli.so")
        ZLBridge.dlopen("$jvmLibDir/libjvm.so")
        ZLBridge.dlopen("$javaLibDir/libfreetype.so")
        ZLBridge.dlopen("$javaLibDir/libverify.so")
        ZLBridge.dlopen("$javaLibDir/libjava.so")
        ZLBridge.dlopen("$javaLibDir/libnet.so")
        ZLBridge.dlopen("$javaLibDir/libnio.so")
        ZLBridge.dlopen("$javaLibDir/libawt.so")
        ZLBridge.dlopen("$javaLibDir/libawt_headless.so")
        ZLBridge.dlopen("$javaLibDir/libfontmanager.so")
        locateLibs(File(runtimeHome)).forEach { file ->
            ZLBridge.dlopen(file.absolutePath)
        }
    }

    private fun dlopenEngine(ctx: LaunchContext, profile: LaunchProfile) {
        ZLBridge.dlopen("${PathManager.DIR_NATIVE_LIB}/libopenal.so")
        profile.onDlopenEngine(ctx)
    }
}
