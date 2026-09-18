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

package com.movtery.zalithlauncher.game.version.export.platform

import android.content.Context
import com.movtery.zalithlauncher.BuildKeys
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.coroutine.TitledTask
import com.movtery.zalithlauncher.coroutine.addTask
import com.movtery.zalithlauncher.game.addons.modloader.ModLoader
import com.movtery.zalithlauncher.game.download.modpack.platform.multimc.MultiMCConfiguration
import com.movtery.zalithlauncher.game.download.modpack.platform.multimc.MultiMCManifest
import com.movtery.zalithlauncher.game.download.modpack.platform.multimc.UID_FABRIC
import com.movtery.zalithlauncher.game.download.modpack.platform.multimc.UID_FORGE
import com.movtery.zalithlauncher.game.download.modpack.platform.multimc.UID_LITELOADER
import com.movtery.zalithlauncher.game.download.modpack.platform.multimc.UID_MINECRAFT
import com.movtery.zalithlauncher.game.download.modpack.platform.multimc.UID_NEOFORGE
import com.movtery.zalithlauncher.game.download.modpack.platform.multimc.UID_QUILT
import com.movtery.zalithlauncher.game.version.export.AbstractExporter
import com.movtery.zalithlauncher.game.version.export.ExportInfo
import com.movtery.zalithlauncher.game.version.export.PackType
import com.movtery.zalithlauncher.game.version.installed.Version
import com.movtery.zalithlauncher.ui.androidText
import com.movtery.zalithlauncher.utils.GSON
import java.io.File
import java.io.StringWriter

/**
 * MultiMC 整合包导出器
 */
class MultiMCPackExporter: AbstractExporter(
    type = PackType.MultiMC
) {
    override fun MutableList<TitledTask>.buildTasks(
        context: Context,
        version: Version,
        info: ExportInfo,
        tempPath: File
    ) {
        addTask(
            title = androidText(R.string.versions_export_task_pack_manifest),
            icon = R.drawable.ic_build_filled
        ) {
            val gameName = info.gamePath.name
            val blackList = listOf(
                File(info.gamePath, "$gameName.json"),
                File(info.gamePath, "$gameName.jar")
            )

            val minecraftDir = File(tempPath, ".minecraft")
            info.selectedFiles.forEach { file ->
                if (file !in blackList) {
                    val targetFile = generateTargetRoot(
                        file = file,
                        rootPath = info.gamePath.absolutePath,
                        targetPath = minecraftDir.absolutePath
                    )
                    file.copyTo(targetFile)
                }
            }

            val components = buildList {
                //Minecraft 原版组件
                add(
                    MultiMCManifest.Component(
                        cachedName = null,
                        cachedRequires = null,
                        cachedVersion = null,
                        isImportant = true,
                        isDependencyOnly = false,
                        uid = UID_MINECRAFT,
                        version = info.mcVersion
                    )
                )

                info.loader?.let { loader ->
                    when (loader.loader) {
                        ModLoader.FORGE -> UID_FORGE
                        ModLoader.NEOFORGE -> UID_NEOFORGE
                        ModLoader.FABRIC -> UID_FABRIC
                        ModLoader.QUILT -> UID_QUILT
                        ModLoader.LITE_LOADER -> UID_LITELOADER
                        else -> null
                    }?.let { uid ->
                        uid to loader.version
                    }
                }?.let { (componentUid, version) ->
                    add(
                        MultiMCManifest.Component(
                            cachedName = null,
                            cachedRequires = null,
                            cachedVersion = null,
                            isImportant = false,
                            isDependencyOnly = false,
                            uid = componentUid,
                            version = version
                        )
                    )
                }
            }

            //生成 mmc-pack.json
            val manifest = MultiMCManifest(
                formatVersion = 1,
                components = components
            )
            val manifestFile = File(tempPath, "mmc-pack.json")
            val manifestJson = GSON.toJson(manifest)
            manifestFile.writeText(manifestJson)

            //生成实例配置文件
            val configuration = MultiMCConfiguration(
                instanceType = "OneSix",
                name = "${info.name}-${info.version}",
                gameVersion = null,
                permGen = null,
                wrapperCommand = "",
                preLaunchCommand = "",
                postExitCommand = null,
                notes = info.summary,
                javaPath = null,
                jvmArgs = info.jvmArgs,
                isFullscreen = false,
                width = null,
                height = null,
                maxMemory = info.maxMemory.takeIf { it > 0 } ?: version.getRamAllocation(context),
                minMemory = info.minMemory.takeIf { it > 0 } ?: version.getRamAllocation(context),
                joinServerOnLaunch = null,
                isShowConsole = false,
                isShowConsoleOnError = true,
                isAutoCloseConsole = false,
                isOverrideMemory = true,
                isOverrideJavaLocation = false,
                isOverrideJavaArgs = true,
                isOverrideConsole = true,
                isOverrideCommands = true,
                isOverrideWindow = true,
                iconKey = null
            ).toProperties()

            val cfgText = StringWriter().use { sw ->
                configuration.store(sw, "Auto generated by ${BuildKeys.LAUNCHER_NAME}")
                sw.toString()
            }
            val cfgFile = File(tempPath, "instance.cfg")
            cfgFile.writeText(cfgText)

            File(tempPath, ".packignore").createNewFile()
        }
    }

    override val fileSuffix: String = "zip"
}