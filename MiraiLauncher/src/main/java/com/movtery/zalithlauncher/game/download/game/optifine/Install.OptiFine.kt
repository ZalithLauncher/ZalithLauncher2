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
import com.movtery.zalithlauncher.utils.file.extractEntryToFile
import com.movtery.zalithlauncher.utils.file.readText
import com.movtery.zalithlauncher.utils.logging.Logger
import kotlinx.coroutines.flow.MutableStateFlow
import java.io.File
import java.time.format.DateTimeFormatter
import java.util.zip.ZipFile

private const val TAG = "Install.OptiFine"

const val OPTIFINE_INSTALL_ID = "Install.OptiFine"

fun getOptiFineInstallTask(
    tempGameDir: File,
    tempMinecraftDir: File,
    tempInstallerJar: File,
    isNewVersion: Boolean,
    optifineVersion: OptiFineVersion,
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

            if (isNewVersion) {
                stopAllNonMainProcesses(GlobalContext)
                withTaskLogOutput(
                    holder = logOutputHolder,
                    title = androidText(
                        R.string.download_game_install_base_install,
                        ModLoader.OPTIFINE.displayName
                    )
                ) { output ->
                    runJvmRetryRuntimes(
                        OPTIFINE_INSTALL_ID,
                        jvmArgs =
                            "-javaagent:" +
                                    //使用 AWTBlockerAgent 禁用 AWT GUI 类调用
                                    LibPath.AWT_BLOCKER_AGENT.absolutePath + " " +
                                    "-cp" + " " +
                                    //使用 JarExceptionCatcher 捕获异常并退出
                                    LibPath.JAR_EXCEPTION_CATCHER.absolutePath + ":" +
                                    tempInstallerJar.absolutePath + " " +
                                    "movtery.JarExceptionCatcher" + " " +
                                    "optifine.Installer",
                        prefixArgs = { jre ->
                            if (jre.majorVersion >= 9) {
                                "--add-exports cpw.mods.bootstraplauncher/cpw.mods.bootstraplauncher=ALL-UNNAMED"
                            } else {
                                null
                            }
                        },
                        jre = Jre.JRE_8,
                        userHome = tempGameDir.absolutePath.trimEnd('\\'),
                        logOutput = output
                    )
                }

                //检查 launchwrapper 是否正常安装
                ZipFile(tempInstallerJar).use { zip ->
                    zip.getEntry("launchwrapper-of.txt")
                        ?.readText(zip)
                        ?.takeIf { it.matches("[0-9.]+".toRegex()) }
                        ?.let { version ->
                            checkOFLaunchWrapper(version, zip, tempLibrariesFolder)
                        }
                }
            } else {
                val tempMcFolder = File(tempVersionFolder, optifineVersion.inherit)
                val tempMcJar = File(tempMcFolder, "${optifineVersion.inherit}.jar")
                val tempMcJson = File(tempMcFolder, "${optifineVersion.inherit}.json")

                val tempOfFolder = File(tempVersionFolder, optifineVersion.version)
                val tempOfJar = File(tempOfFolder, "${optifineVersion.version}.jar")
                val tempOfJson = File(tempOfFolder, "${optifineVersion.version}.json")

                //复制原版Jar文件
                tempMcJar.copyTo(tempOfJar, overwrite = true)

                //建立Json
                val jsonString = createOldOptiFineJson(
                    vanillaJson = tempMcJson,
                    optifineVersion = optifineVersion
                )
                tempOfJson.writeText(jsonString)
            }
        }
    )
}

/**
 * 确保 launchwrapper-of 库被正常安装，如果未正常安装，则自行尝试解压
 */
private fun checkOFLaunchWrapper(version: String, installer: ZipFile, libFolder: File) {
    val fileName = "launchwrapper-of-$version.jar"
    val lwTargetFolder = File(libFolder, "optifine/launchwrapper-of/$version")
    val lwTargetFile = File(lwTargetFolder, fileName)

    if (!lwTargetFile.exists()) {
        //安装出现神秘问题导致该文件未解压，自行尝试解压
        Logger.info(TAG, "$fileName is not exists! try extract it by self.")
        installer.extractEntryToFile(fileName, lwTargetFile)
    }
}

private fun createOldOptiFineJson(
    vanillaJson: File,
    optifineVersion: OptiFineVersion
): String {
    val vanillaVersion = vanillaJson.readText().parseTo(GameManifest::class.java)
    val releaseTime = if (optifineVersion.releaseDate.isEmpty()) {
        vanillaVersion.releaseTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
    } else {
        optifineVersion.releaseDate.replace("/", "-")
    }

    val jsonBuilder = StringBuilder().apply {
        appendLine("{")
        appendLine("  \"id\": \"${optifineVersion.version}\",")
        appendLine("  \"inheritsFrom\": \"${optifineVersion.inherit}\",")
        appendLine("  \"time\": \"${releaseTime}T23:33:33+08:00\",")
        appendLine("  \"releaseTime\": \"${releaseTime}T23:33:33+08:00\",")
        appendLine("  \"type\": \"release\",")
        appendLine("  \"libraries\": [")
        appendLine("    {\"name\": \"optifine:OptiFine:${optifineVersion.fileName.replace("OptiFine_", "").replace(".jar", "").replace("preview_", "")}\"},")
        appendLine("    {\"name\": \"net.minecraft:launchwrapper:1.12\"}")
        appendLine("  ],")
        appendLine("  \"mainClass\": \"net.minecraft.launchwrapper.Launch\",")

        if (vanillaVersion.isOldVersion()) {
            appendLine("  \"minimumLauncherVersion\": 18,")
            appendLine("  \"minecraftArguments\": \"${vanillaVersion.minecraftArguments}  --tweakClass optifine.OptiFineTweaker\"")
            appendLine("}")
        } else {
            appendLine("  \"minimumLauncherVersion\": \"21\",")
            appendLine("  \"arguments\": {")
            appendLine("    \"game\": [")
            appendLine("      \"--tweakClass\",")
            appendLine("      \"optifine.OptiFineTweaker\"")
            appendLine("    ]")
            appendLine("  }")
            appendLine("}")
        }
    }

    return jsonBuilder.toString()
}