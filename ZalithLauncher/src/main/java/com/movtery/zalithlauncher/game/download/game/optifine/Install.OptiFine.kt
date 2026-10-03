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

package com.movtery.zalithlauncher.game.download.game.optifine

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.components.jre.Jre
import com.movtery.zalithlauncher.context.GlobalContext
import com.movtery.zalithlauncher.coroutine.Task
import com.movtery.zalithlauncher.coroutine.TaskLogOutput
import com.movtery.zalithlauncher.coroutine.withTaskLogOutput
import com.movtery.zalithlauncher.game.addons.modloader.ModLoader
import com.movtery.zalithlauncher.game.addons.modloader.optifine.OptiFineVersion
import com.movtery.zalithlauncher.game.download.game.isOldVersion
import com.movtery.zalithlauncher.game.download.jvm_server.runJvmRetryRuntimes
import com.movtery.zalithlauncher.game.download.jvm_server.stopAllNonMainProcesses
import com.movtery.zalithlauncher.game.version.download.parseTo
import com.movtery.zalithlauncher.game.versioninfo.models.GameManifest
import com.movtery.zalithlauncher.path.LibPath
import com.movtery.zalithlauncher.ui.androidText
import com.movtery.zalithlauncher.utils.GSON
import com.movtery.zalithlauncher.utils.file.ensureParentDirectory
import com.movtery.zalithlauncher.utils.file.extractEntryToFile
import com.movtery.zalithlauncher.utils.file.readText
import com.movtery.zalithlauncher.utils.string.isLowerTo
import kotlinx.coroutines.flow.MutableStateFlow
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.time.format.DateTimeFormatter
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream

private const val TAG = "Install.OptiFine"

const val OPTIFINE_INSTALL_ID = "Install.OptiFine"

/** LaunchWrapper 主类，OptiFine 以 Tweaker 方式通过它加载 */
const val LAUNCH_WRAPPER_MAIN = "net.minecraft.launchwrapper.Launch"

const val OPTIFINE_TWEAKER = "optifine.OptiFineTweaker"
const val OPTIFINE_FORGE_TWEAKER = "optifine.OptiFineForgeTweaker"

/** HMCL TransformerDiscoveryService，供 Forge 1.13 ~ 1.16 的 ModLauncher 发现并加载 OptiFine */
const val TRANSFORMER_DISCOVERY_SERVICE_LIB = "org.jackhuang.hmcl:transformer-discovery-service:1.0"

/** 与 Forge 1.17 原生兼容的最低 OptiFine 构建号（H1 Pre2） */
private const val FORGE_17_MIN_BUILD = "20210924-190833"

/**
 * 判断 Minecraft 版本是否为 1.17 及以上（BootstrapLauncher时代）
 */
fun isMinecraft17Plus(version: String): Boolean {
    val minor = version.split(".").getOrNull(1)?.toIntOrNull() ?: return false
    return minor >= 17
}

fun getOptiFineInstallTask(
    tempGameDir: File,
    tempMinecraftDir: File,
    tempInstallerJar: File,
    optifineVersion: OptiFineVersion,
    hasBootstrapForge: Boolean,
    logOutputHolder: MutableStateFlow<TaskLogOutput?>
): Task {
    val tempVersionFolder = File(tempMinecraftDir, "versions")
    val tempLibrariesFolder = File(tempMinecraftDir, "libraries")

    return Task.runTask(
        id = OPTIFINE_INSTALL_ID,
        task = { task ->
            task.updateProgress(-1f)
            task.updateMessage(androidText(
                R.string.download_game_install_base_installing, ModLoader.OPTIFINE.displayName
            ))

            checkBootstrapForgeCompatibility(tempInstallerJar, hasBootstrapForge)

            val mavenVersion = optifineVersion.mavenVersion()
            val optifineLibFolder = File(tempLibrariesFolder, "optifine/OptiFine/$mavenVersion")

            //installer 归位至 libraries（Maven 布局），并移除 mods.toml，防止被 Forge 当作 Mod 扫描
            val installerLibFile = File(optifineLibFolder, "OptiFine-$mavenVersion-installer.jar")
            copyZipSkippingEntries(tempInstallerJar, installerLibFile, "META-INF/mods.toml")

            //生成 OptiFine 本体库：新版通过 Patcher 对原版 Jar 打补丁，旧版直接以 installer 充当
            val patchedLibFile = File(optifineLibFolder, "OptiFine-$mavenVersion.jar")
            val launchWrapperLibs: List<String> = ZipFile(tempInstallerJar).use { zip ->
                if (zip.getEntry("optifine/Patcher.class") != null) {
                    stopAllNonMainProcesses(GlobalContext)
                    withTaskLogOutput(
                        holder = logOutputHolder,
                        title = androidText(
                            R.string.download_game_install_base_installing,
                            ModLoader.OPTIFINE.displayName
                        )
                    ) { output ->
                        runJvmRetryRuntimes(
                            OPTIFINE_INSTALL_ID,
                            jvmArgs = "-cp" + " " + tempInstallerJar.absolutePath + " " +
                                    "optifine.Patcher" + " " +
                                    File(tempVersionFolder, "${optifineVersion.inherit}/${optifineVersion.inherit}.jar").absolutePath + " " +
                                    tempInstallerJar.absolutePath + " " +
                                    patchedLibFile.absolutePath,
                            prefixArgs = { null },
                            jre = Jre.JRE_8,
                            userHome = tempGameDir.absolutePath.trimEnd('\\'),
                            logOutput = output
                        )
                    }
                    //Patcher 产物中同样移除 mods.toml，防止 Forge 扫描冲突
                    stripZipEntries(patchedLibFile, "META-INF/mods.toml")
                } else {
                    copyZipSkippingEntries(tempInstallerJar, patchedLibFile, "META-INF/mods.toml")
                }

                installLaunchWrapper(zip, tempLibrariesFolder)
            }

            //释放 TransformerDiscoveryService 到 libraries，供 Forge 1.13 ~ 1.16 组合时加载 OptiFine
            val discoveryServiceFile = File(
                tempLibrariesFolder,
                "org/jackhuang/hmcl/transformer-discovery-service/1.0/transformer-discovery-service-1.0.jar"
            ).apply { ensureParentDirectory() }
            LibPath.TRANSFORMER_DISCOVERY_SERVICE.copyTo(discoveryServiceFile, overwrite = true)

            //建立 OptiFine 版本 Json，交由 GameJsonMerger 与其余组件合并
            val tempOfFolder = File(tempVersionFolder, optifineVersion.version)
            val tempOfJson = File(tempOfFolder, "${optifineVersion.version}.json")
            val tempMcJson = File(tempVersionFolder, "${optifineVersion.inherit}/${optifineVersion.inherit}.json")
            tempOfJson.writeText(
                createOptiFineJson(
                    vanillaJson = tempMcJson,
                    optifineVersion = optifineVersion,
                    mavenVersion = mavenVersion,
                    launchWrapperLibs = launchWrapperLibs
                )
            )
        }
    )
}

/**
 * 校验 Forge 1.17+ 与 OptiFine 的兼容性：
 * 只有 H1 Pre2 之后的 OptiFine 才与 Forge 1.17 原生兼容，不兼容时提前终止安装
 */
private fun checkBootstrapForgeCompatibility(installer: File, hasBootstrapForge: Boolean) {
    if (!hasBootstrapForge) return
    ZipFile(installer).use { zip ->
        val buildof = zip.getEntry("buildof.txt")?.readText(zip)?.trim() ?: return
        if (buildof.isLowerTo(FORGE_17_MIN_BUILD)) {
            throw OptiFineForge17IncompatibleException(buildof)
        }
    }
}

/**
 * 从 installer 中提取 OptiFine 自带的 launchwrapper
 * @return 需要写入版本 Json 的 launchwrapper 库坐标，都没有时回退到官方 launchwrapper
 */
private fun installLaunchWrapper(zip: ZipFile, libFolder: File): List<String> {
    val libs = mutableListOf<String>()

    //launchwrapper-2.0.jar -> optifine:launchwrapper:2.0
    if (zip.getEntry("launchwrapper-2.0.jar") != null) {
        val target = File(libFolder, "optifine/launchwrapper/2.0/launchwrapper-2.0.jar")
        zip.extractEntryToFile("launchwrapper-2.0.jar", target)
        libs.add("optifine:launchwrapper:2.0")
    }

    //launchwrapper-of-<版本>.jar -> optifine:launchwrapper-of:<版本>
    val lwVersion = zip.getEntry("launchwrapper-of.txt")?.readText(zip)?.trim()
    if (lwVersion != null && zip.getEntry("launchwrapper-of-$lwVersion.jar") != null) {
        val target = File(libFolder, "optifine/launchwrapper-of/$lwVersion/launchwrapper-of-$lwVersion.jar")
        zip.extractEntryToFile("launchwrapper-of-$lwVersion.jar", target)
        libs.add("optifine:launchwrapper-of:$lwVersion")
    }

    if (libs.isEmpty()) libs.add("net.minecraft:launchwrapper:1.12")
    return libs
}

/**
 * 复制 ZIP 内全部条目，跳过指定条目
 */
private fun copyZipSkippingEntries(source: File, target: File, vararg skipEntries: String) {
    target.ensureParentDirectory()
    ZipFile(source).use { zip ->
        writeZip(target) { output ->
            zip.entries().asSequence().forEach { entry ->
                if (entry.name in skipEntries) return@forEach
                output.putNextEntry(ZipEntry(entry.name))
                if (!entry.isDirectory) zip.getInputStream(entry).copyTo(output)
                output.closeEntry()
            }
        }
    }
}

/**
 * 移除 ZIP 内的指定条目（原地重写）
 */
private fun stripZipEntries(source: File, vararg stripEntries: String) {
    if (!source.exists()) return
    val stripped = File(source.parentFile, "${source.name}.strip")
    copyZipSkippingEntries(source, stripped, *stripEntries)
    check(stripped.renameTo(source)) { "Failed to replace stripped zip file: ${source.absolutePath}" }
}

private inline fun writeZip(target: File, block: (ZipOutputStream) -> Unit) {
    ZipOutputStream(BufferedOutputStream(FileOutputStream(target))).use(block)
}

/**
 * 建立独立安装用的 OptiFine 版本 Json
 * 纯净环境（无 Forge/NeoForge）下以 LaunchWrapper + OptiFineTweaker 方式启动
 */
private fun createOptiFineJson(
    vanillaJson: File,
    optifineVersion: OptiFineVersion,
    mavenVersion: String,
    launchWrapperLibs: List<String>
): String {
    val vanillaVersion = vanillaJson.readText().parseTo(GameManifest::class.java)
    val releaseTime = if (optifineVersion.releaseDate.isEmpty()) {
        vanillaVersion.releaseTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
    } else {
        optifineVersion.releaseDate.replace("/", "-")
    }

    val root = JsonObject().apply {
        addProperty("id", optifineVersion.version)
        addProperty("inheritsFrom", optifineVersion.inherit)
        addProperty("time", "${releaseTime}T23:33:33+08:00")
        addProperty("releaseTime", "${releaseTime}T23:33:33+08:00")
        addProperty("type", "release")
        add("libraries", JsonArray().apply {
            add(JsonObject().apply { addProperty("name", "optifine:OptiFine:$mavenVersion") })
            launchWrapperLibs.forEach { lib ->
                add(JsonObject().apply { addProperty("name", lib) })
            }
        })
        addProperty("mainClass", LAUNCH_WRAPPER_MAIN)
    }

    if (vanillaVersion.isOldVersion()) {
        root.addProperty("minecraftArguments", "--tweakClass $OPTIFINE_TWEAKER")
    } else {
        root.add("arguments", JsonObject().apply {
            add("game", JsonArray().apply {
                add(JsonPrimitive("--tweakClass"))
                add(JsonPrimitive(OPTIFINE_TWEAKER))
            })
        })
    }

    return GSON.toJson(root)
}