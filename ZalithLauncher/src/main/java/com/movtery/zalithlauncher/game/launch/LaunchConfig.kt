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

import android.os.Build
import androidx.compose.ui.unit.IntSize
import com.movtery.zalithlauncher.BuildKeys
import com.movtery.zalithlauncher.game.multirt.Runtime
import com.movtery.zalithlauncher.game.versioninfo.models.GameManifest
import com.movtery.zalithlauncher.path.LibPath
import com.movtery.zalithlauncher.path.PathManager
import com.movtery.zalithlauncher.utils.logging.Logger
import java.io.File
import java.util.Locale
import java.util.TimeZone

private const val TAG = "LaunchProfile"

/**
 * 启动配置
 *
 * 通用 -D → [preJvmArgs] → cacio → 用户自定义参数 → purge/内存 → [jvmArgs] → [programArgs]
 */
interface LaunchProfile {
    /**
     * 用户自定义参数之前的 agent 段
     */
    fun preJvmArgs(): List<String>

    /**
     * 用户自定义参数之后的 JVM 选项段
     */
    fun jvmArgs(): List<String>

    /**
     * 主类与游戏参数
     */
    fun programArgs(): List<String>

    /**
     * 内存分配（单位：MB）
     */
    fun ramAllocation(): Int

    /**
     * 用户自定义 JVM 参数串
     */
    fun userJvmArgs(): String

    /**
     * 向通用 -D 键表追加或覆盖本路径特有键
     */
    fun putLaunchProperties(props: MutableMap<String, String>) {}

    /**
     * 向通用环境变量追加本路径特有键
     */
    fun onInitEnv(ctx: LaunchContext, env: MutableMap<String, String>) {}

    /**
     * 通用 openal 库加载完成后的补充加载
     */
    fun onDlopenEngine(ctx: LaunchContext) {}
}

data class LaunchContext(
    val runtime: Runtime,
    val javaHome: String,
    val screenSize: IntSize,
    val lwjglNativesDir: String,
    /** LD_LIBRARY_PATH 内容 */
    val libraryPath: String,
    val userHome: String,
    val useLocalLanguage: Boolean,
    val resolvFile: File,
    val minecraftPath: String
)

/**
 * 不允许用户指定的参数前缀
 */
private val PURGED_ARG_PREFIXES = listOf(
    "-Xms", "-Xmx", "-d32", "-d64", "-Xint",
    "-XX:+UseTransparentHugePages", "-XX:+UseLargePagesInMetaspace", "-XX:+UseLargePages",
    "-Dorg.lwjgl.opengl.libname",
    // Don't let the user specify a custom Freetype library (as the user is unlikely to specify a version compiled for Android)
    "-Dorg.lwjgl.freetype.libname",
    // Overridden by us to specify the exact number of cores that the android system has
    "-XX:ActiveProcessorCount"
)

/**
 * 排除不允许用户指定的参数前缀
 */
fun MutableList<String>.purgeRestrictedArgs() {
    PURGED_ARG_PREFIXES.forEach { prefix ->
        purgeArg(prefix)
    }
}

private fun MutableList<String>.purgeArg(argStart: String) {
    removeIf { arg: String -> arg.startsWith(argStart) }
}

/**
 * 组装启动器托管的 `-D` 参数，用户已显式声明的同名键会被跳过，交由用户参数生效
 */
fun buildOverridableJavaArgs(
    ctx: LaunchContext,
    profile: LaunchProfile,
    userArguments: List<String>
): List<String> {
    val overridableArguments = mutableMapOf<String, String>().apply {
        put("java.home", ctx.javaHome)
        put("java.io.tmpdir", PathManager.DIR_CACHE.absolutePath)
        put("jna.boot.library.path", PathManager.DIR_NATIVE_LIB)
        put("user.home", ctx.userHome)
        if (ctx.useLocalLanguage) {
            put("user.language", System.getProperty("user.language") ?: "en")
            put("user.country", Locale.getDefault().country)
        }
        put("user.timezone", TimeZone.getDefault().id)
        put("os.name", "Linux")
        put("os.version", "Android-${Build.VERSION.RELEASE}")
        put("pojav.path.minecraft", ctx.minecraftPath)
        put("pojav.path.private.account", PathManager.DIR_DATA_BASES.absolutePath)
        put("org.lwjgl.vulkan.libname", "libvulkan.so")
        // LWJGL 3.4 的 Library.loadSystem 通过该属性定位 native 库。
        // 指向 per-version natives 目录，保证 3.4.x 游戏加载对应版本的 liblwjgl.so 等。
        put("org.lwjgl.librarypath", ctx.lwjglNativesDir)
        put("glfwstub.windowWidth", ctx.screenSize.width.toString())
        put("glfwstub.windowHeight", ctx.screenSize.height.toString())
        put("glfwstub.initEgl", "false")
        put("ext.net.resolvPath", ctx.resolvFile.absolutePath)

        put("log4j2.formatMsgNoLookups", "true")
        // Fix RCE vulnerability of log4j2
        put("java.rmi.server.useCodebaseOnly", "true")
        put("com.sun.jndi.rmi.object.trustURLCodebase", "false")
        put("com.sun.jndi.cosnaming.object.trustURLCodebase", "false")

        put("net.minecraft.clientmodname", BuildKeys.LAUNCHER_NAME)

        // fml
        put("fml.earlyprogresswindow", "false")
        put("fml.ignoreInvalidMinecraftCertificates", "true")
        put("fml.ignorePatchDiscrepancies", "true")

        put("loader.disable_forked_guis", "true")
        put("jdk.lang.Process.launchMechanism", "FORK")

        put("sodium.checks.issue2561", "false")

        put("file.encoding", "UTF-8")
        put("sun.stdout.encoding", "UTF-8")
        put("sun.stderr.encoding", "UTF-8")

        put("cpu.name", getSocName())

        profile.putLaunchProperties(this)
    }.map { entry ->
        "-D${entry.key}=${entry.value}"
    }

    return overridableArguments.filter { arg ->
        val stripped = arg.substringBefore('=')
        val overridden = userArguments.any { it.startsWith(stripped) }
        if (overridden) {
            Logger.info(TAG, "Arg skipped: $arg")
        }
        !overridden
    }
}

/**
 * 在 LD_LIBRARY_PATH 中检索 native 库的绝对路径
 * @param libName 检索的库名，检索不到时返回原名交给系统解析
 */
fun findInLdLibPath(libraryPath: String, libName: String): String {
    return libraryPath.split(":").find { path ->
        val file = File(path, libName)
        file.exists() && file.isFile
    }?.let {
        File(it, libName).absolutePath
    } ?: libName
}

fun getCacioJavaArgs(
    screenSize: IntSize,
    isJava8: Boolean
): List<String> {
    val argsList: MutableList<String> = ArrayList()

    // Caciocavallo config AWT-enabled version
    argsList.add("-Djava.awt.headless=false")
    argsList.add("-Dcacio.managed.screensize=${screenSize.width}x${screenSize.height}")
    argsList.add("-Dcacio.font.fontmanager=sun.awt.X11FontManager")
    argsList.add("-Dcacio.font.fontscaler=sun.font.FreetypeFontScaler")
    argsList.add("-Dswing.defaultlaf=javax.swing.plaf.nimbus.NimbusLookAndFeel")
    if (isJava8) {
        argsList.add("-Dawt.toolkit=net.java.openjdk.cacio.ctc.CTCToolkit")
        argsList.add("-Djava.awt.graphicsenv=net.java.openjdk.cacio.ctc.CTCGraphicsEnvironment")
    } else {
        argsList.add("-Dawt.toolkit=com.github.caciocavallosilano.cacio.ctc.CTCToolkit")
        argsList.add("-Djava.awt.graphicsenv=com.github.caciocavallosilano.cacio.ctc.CTCGraphicsEnvironment")
        argsList.add("-javaagent:${LibPath.CACIO_17_AGENT.absolutePath}")

        argsList.add("--add-exports=java.desktop/java.awt=ALL-UNNAMED")
        argsList.add("--add-exports=java.desktop/java.awt.peer=ALL-UNNAMED")
        argsList.add("--add-exports=java.desktop/sun.awt.image=ALL-UNNAMED")
        argsList.add("--add-exports=java.desktop/sun.java2d=ALL-UNNAMED")
        argsList.add("--add-exports=java.desktop/java.awt.dnd.peer=ALL-UNNAMED")
        argsList.add("--add-exports=java.desktop/sun.awt=ALL-UNNAMED")
        argsList.add("--add-exports=java.desktop/sun.awt.event=ALL-UNNAMED")
        argsList.add("--add-exports=java.desktop/sun.awt.datatransfer=ALL-UNNAMED")
        argsList.add("--add-exports=java.desktop/sun.font=ALL-UNNAMED")
        argsList.add("--add-exports=java.base/sun.security.action=ALL-UNNAMED")
        argsList.add("--add-opens=java.base/java.util=ALL-UNNAMED")
        argsList.add("--add-opens=java.desktop/java.awt=ALL-UNNAMED")
        argsList.add("--add-opens=java.desktop/sun.font=ALL-UNNAMED")
        argsList.add("--add-opens=java.desktop/sun.java2d=ALL-UNNAMED")
        argsList.add("--add-opens=java.base/java.lang.reflect=ALL-UNNAMED")

        // Opens the java.net package to Arc DNS injector on Java 9+
        argsList.add("--add-opens=java.base/java.net=ALL-UNNAMED")
    }

    val cacioClassPath = StringBuilder()
    cacioClassPath.append("-Xbootclasspath/").append(if (isJava8) "p" else "a")
    val cacioFiles = if (isJava8) LibPath.CACIO_8 else LibPath.CACIO_17
    cacioFiles.listFiles()?.onEach {
        if (it.name.endsWith(".jar")) cacioClassPath.append(":").append(it.absolutePath)
    }

    argsList.add(cacioClassPath.toString())

    return argsList
}

/**
 * 获取设备 SoC 名称，在 API 31+ 读取系统属性 ro.soc.model
 *
 * 失败则返回 [Build.HARDWARE]
 */
fun getSocName(): String {
    return runCatching {
        ProcessBuilder("getprop", "ro.soc.model")
            .start()
            .inputStream
            .bufferedReader()
            .use { reader ->
                reader.readLine()
            }
    }.getOrNull()?.takeIf {
        it.isNotBlank()
    } ?: Build.HARDWARE
}

/**
 * 从版本清单中探测要求的 LWJGL 主版本
 * 解析 `org.lwjgl:lwjgl:X.Y.Z` / `org.lwjgl.lwjgl:lwjgl:X.Y.Z` 坐标，
 * 返回去掉句点后的整数（如 3.3.3→333、3.4.1→341、2.9.9→299）
 * @return 无法确定时返回 0（默认按 LWJGL3 处理）
 */
fun detectLwjglVersion(manifest: GameManifest): Int {
    for (lib in manifest.libraries) {
        val name = lib.name ?: continue
        val versionPrefix = when {
            name.startsWith("org.lwjgl.lwjgl:lwjgl:") -> "org.lwjgl.lwjgl:lwjgl:"
            name.startsWith("org.lwjgl:lwjgl:") -> "org.lwjgl:lwjgl:"
            else -> continue
        }
        val intVersion = name.substring(versionPrefix.length)
            .takeWhile { it.isDigit() || it == '.' }
            .filter { it != '.' }
            .toIntOrNull()
        if (intVersion != null && intVersion in 200..999) return intVersion
    }
    return 0
}

/**
 * LWJGL 版本整数 -> 组件目录名。
 * 版本 >= 3.4.1 -> 3.4.1 组件；否则（含 LWJGL2 桥接场景）-> 3.3.3 组件
 */
fun lwjglVersionDir(lwjglVersion: Int): String = if (lwjglVersion >= 341) "3.4.1" else "3.3.3"
