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
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.coroutine.TitledTask
import com.movtery.zalithlauncher.coroutine.addTask
import com.movtery.zalithlauncher.game.addons.modloader.ModLoader
import com.movtery.zalithlauncher.game.download.modpack.platform.curseforge.CurseForgeManifest
import com.movtery.zalithlauncher.game.download.modpack.platform.mcbbs.MCBBSManifest
import com.movtery.zalithlauncher.game.version.export.AbstractExporter
import com.movtery.zalithlauncher.game.version.export.ExportInfo
import com.movtery.zalithlauncher.game.version.export.PackType
import com.movtery.zalithlauncher.game.version.installed.Version
import com.movtery.zalithlauncher.ui.androidText
import com.movtery.zalithlauncher.utils.GSON
import com.movtery.zalithlauncher.utils.string.tokenize
import org.jackhuang.hmcl.util.DigestUtils
import java.io.File

/**
 * MCBBS 整合包导出工具
 */
class MCBBSPackExporter: AbstractExporter(
    type = PackType.MCBBS
) {
    override fun MutableList<TitledTask>.buildTasks(
        context: Context,
        version: Version,
        info: ExportInfo,
        tempPath: File
    ) {
        addTask(
            id = "MCBBSPackExporter.PackManifest",
            title = androidText(R.string.versions_export_task_pack_manifest),
            icon = R.drawable.ic_build_filled
        ) {
            val gameName = info.gamePath.name
            val blackList = listOf(
                File(info.gamePath, "$gameName.json"),
                File(info.gamePath, "$gameName.jar")
            )

            val override = File(tempPath, "overrides")
            val addonFiles = info.selectedFiles.mapNotNull { file ->
                if (file !in blackList) {
                    val targetFile = generateTargetRoot(
                        file = file,
                        rootPath = info.gamePath.absolutePath,
                        targetPath = override.absolutePath
                    )
                    file.copyTo(targetFile)

                    MCBBSManifest.AddonFile(
                        force = true,
                        path = relativePath(file, info.gamePath.absolutePath),
                        hash = DigestUtils.digestToString("SHA-1", file.toPath())
                    )
                } else {
                    null
                }
            }

            val addons = buildList {
                add(MCBBSManifest.Addon(id = "game", version = info.mcVersion))
                info.loader?.let { loader ->
                    val patchId = when (loader.loader) {
                        ModLoader.OPTIFINE -> "optifine"
                        ModLoader.FORGE -> "forge"
                        ModLoader.NEOFORGE -> "neoforge"
                        ModLoader.FABRIC -> "fabric"
                        ModLoader.QUILT -> "quilt"
                        ModLoader.LITE_LOADER -> "liteloader"
                        ModLoader.CLEANROOM -> "cleanroom"
                        else -> null
                    } ?: return@let
                    add(MCBBSManifest.Addon(id = patchId, version = loader.version))
                }
            }

            val settings = MCBBSManifest.Settings(installMods = true, installResourcepack = true)
            val launchInfo = MCBBSManifest.LaunchInfo(
                minMemory = info.minMemory.takeIf { it > 0 } ?: version.getRamAllocation(context),
                supportJava = null,
                launchArguments = tokenize(info.jvmArgs),
                javaArguments = tokenize(info.javaArgs)
            )

            val manifest = MCBBSManifest(
                manifestType = "minecraftModpack",
                manifestVersion = 2,
                name = info.name,
                version = info.version,
                author = info.author,
                description = info.summary ?: "",
                fileApi = info.fileApi?.removeSuffix("/"),
                url = info.url,
                forceUpdate = info.forceUpdate,
                origins = emptyList(),
                addons = addons,
                libraries = emptyList(),
                files = addonFiles,
                settings = settings,
                launchInfo = launchInfo
            )

            val manifestFile = File(tempPath, "mcbbs.packmeta")
            val jsonString = GSON.toJson(manifest)
            manifestFile.writeText(jsonString)

            // CurseForge manifest
            val cfLoader = info.loader?.let { loader ->
                when (loader.loader) {
                    ModLoader.FORGE -> CurseForgeManifest.Minecraft.ModLoader(
                        "forge-${loader.version}", true
                    )
                    ModLoader.NEOFORGE -> CurseForgeManifest.Minecraft.ModLoader(
                        "neoforge-${loader.version}", true
                    )
                    ModLoader.FABRIC -> CurseForgeManifest.Minecraft.ModLoader(
                        "fabric-${loader.version}", true
                    )
                    else -> null
                }
            }

            val minecraft = CurseForgeManifest.Minecraft(
                gameVersion = info.mcVersion,
                modLoaders = listOfNotNull(cfLoader)
            )
            val curseManifest = CurseForgeManifest(
                manifestType = "minecraftModpack",
                manifestVersion = 1,
                name = info.name,
                version = info.version,
                author = info.author,
                overrides = "overrides",
                minecraft = minecraft,
                files = emptyList(),
            )

            val curseManifestFile = File(tempPath, "manifest.json")
            val curseJson = GSON.toJson(curseManifest)
            curseManifestFile.writeText(curseJson)
        }
    }

    override val fileSuffix: String
        get() = "zip"
}