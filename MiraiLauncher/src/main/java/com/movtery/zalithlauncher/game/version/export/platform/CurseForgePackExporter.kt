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
import com.movtery.zalithlauncher.game.download.assets.platform.curseforge.models.CurseForgeData
import com.movtery.zalithlauncher.game.download.assets.platform.mirroredCurseForgeSource
import com.movtery.zalithlauncher.game.download.assets.platform.mirroredPlatformSearcher
import com.movtery.zalithlauncher.game.download.modpack.platform.curseforge.CurseForgeManifest
import com.movtery.zalithlauncher.game.version.export.AbstractExporter
import com.movtery.zalithlauncher.game.version.export.ExportInfo
import com.movtery.zalithlauncher.game.version.export.PackType
import com.movtery.zalithlauncher.game.version.installed.Version
import com.movtery.zalithlauncher.game.version.mod.isDisabled
import com.movtery.zalithlauncher.ui.androidText
import com.movtery.zalithlauncher.utils.GSON
import com.movtery.zalithlauncher.utils.logging.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jackhuang.hmcl.util.DigestUtils
import java.io.File

private const val TAG = "CurseForgePackExporter"

class CurseForgePackExporter: AbstractExporter(
    type = PackType.CurseForge
) {
    private val files = mutableListOf<CurseForgeManifest.ManifestFile>()
    private val remoteMods = mutableListOf<CurseForgeData>()
    private val filesInManifest = mutableListOf<File>()

    override fun MutableList<TitledTask>.buildTasks(
        context: Context,
        version: Version,
        info: ExportInfo,
        tempPath: File
    ) {
        if (info.packCurseForge) {
            addTask(
                id = "CurseForgePackExporter.FetchRemote",
                title = androidText(R.string.versions_export_task_fetch_remote),
                icon = R.drawable.ic_search
            ) { task ->
                //获取远端数据
                packRemote(
                    gamePath = info.gamePath,
                    selectedFiles = info.selectedFiles,
                    onProgress = { file ->
                        if (file != null) {
                            task.updateMessage(androidText(file.nameWithoutExtension))
                        } else {
                            task.updateMessage(null)
                        }
                    }
                )
            }
        }

        addTask(
            id = "CurseForgePackExporter.PackManifest",
            title = androidText(R.string.versions_export_task_pack_manifest),
            icon = R.drawable.ic_build_filled
        ) {
            val gameName = info.gamePath.name
            val blackList = listOf(
                File(info.gamePath, "$gameName.json"),
                File(info.gamePath, "$gameName.jar")
            )

            val override = File(tempPath, "overrides")
            info.selectedFiles.forEach { file ->
                if (file !in filesInManifest && file !in blackList) {
                    val targetFile = generateTargetRoot(
                        file = file,
                        rootPath = info.gamePath.absolutePath,
                        targetPath = override.absolutePath
                    )
                    file.copyTo(targetFile)
                }
            }

            val loaders = buildList {
                info.loader?.let { loader ->
                    val identifier = when (loader.loader) {
                        ModLoader.FORGE -> "forge"
                        ModLoader.NEOFORGE -> "neoforge"
                        ModLoader.FABRIC -> "fabric"
                        ModLoader.QUILT -> "quilt"
                        else -> null
                    } ?: return@let
                    add(
                        CurseForgeManifest.Minecraft.ModLoader(
                            id = "$identifier-${loader.version}",
                            primary = true
                        )
                    )
                }
            }

            val manifest = CurseForgeManifest(
                manifestType = "minecraftModpack",
                manifestVersion = 1,
                name = info.name,
                version = info.version,
                author = info.author,
                overrides = "overrides",
                minecraft = CurseForgeManifest.Minecraft(
                    gameVersion = info.mcVersion,
                    modLoaders = loaders,
                    recommendedRam = info.minMemory.takeIf { it > 0 }
                ),
                files = files
            )

            val manifestFile = File(tempPath, "manifest.json")
            val jsonString = GSON.toJson(manifest)
            //写入整合包清单信息
            manifestFile.writeText(jsonString)

            writeModList(tempPath)
        }
    }

    private suspend fun packRemote(
        gamePath: File,
        selectedFiles: List<File>,
        onProgress: (File?) -> Unit,
    ) = withContext(Dispatchers.IO) {
        for (dir in listOf("resourcepacks", "shaderpacks", "mods")) {
            val resourceDir = File(gamePath, dir)
            if (resourceDir.exists()) {
                resourceDir.listFiles()?.forEach { file ->
                    if (file in selectedFiles) {
                        onProgress(file)
                        if (file.isDisabled()) return@forEach

                        runCatching {
                            val path = file.toPath()

                            val modFile = mirroredPlatformSearcher(
                                searchers = mirroredCurseForgeSource()
                            ) { searcher ->
                                val sha1 = DigestUtils.digestToString("SHA-1", path)
                                searcher.getVersionByLocalFile(file, sha1)
                            }?.takeIf { mod ->
                                mod.isAvailable
                            } ?: return@runCatching

                            val project = mirroredPlatformSearcher(
                                searchers = mirroredCurseForgeSource()
                            ) { searcher ->
                                searcher.getProject(modFile.modId.toString())
                            }.data

                            files.add(
                                CurseForgeManifest.ManifestFile(
                                    projectID = modFile.modId,
                                    fileID = modFile.id,
                                    required = true
                                )
                            )
                            remoteMods.add(project)
                            filesInManifest.add(file)
                        }.onFailure {
                            Logger.warning(TAG, "Failed to obtain remote data for ${file.name}!", it)
                        }
                    }

                    onProgress(null)
                }
            }
        }
    }

    private suspend fun writeModList(tempPath: File) {
        val modListFile = File(tempPath, "modlist.html")
        withContext(Dispatchers.IO) {
            if (remoteMods.isNotEmpty()) {
                val modListHtml = buildString {
                    append("<ul>")
                    append("\n")
                    remoteMods.forEach { data ->
                        if (data.links.websiteUrl != null) {
                            append("<li><a href=\"")
                            append(data.links.websiteUrl)
                            append("\">")

                            append(data.name)
                            append("</a></li>")
                            append("\n")
                        }
                    }
                    append("</ul>")
                }
                modListFile.writeText(modListHtml)
            } else {
                //默认创建一个空文件
                modListFile.createNewFile()
            }
        }
    }

    override val fileSuffix: String
        get() = "zip"
}