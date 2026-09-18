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

package com.movtery.zalithlauncher.game.download.modpack.platform.curseforge

import com.google.gson.JsonParseException
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.coroutine.Task
import com.movtery.zalithlauncher.game.addons.modloader.ModLoader
import com.movtery.zalithlauncher.game.download.assets.platform.curseforge.models.fixedFileUrl
import com.movtery.zalithlauncher.game.download.assets.platform.curseforge.models.getPlatformClassesOrNull
import com.movtery.zalithlauncher.game.download.assets.platform.curseforge.models.getSHA1
import com.movtery.zalithlauncher.game.download.assets.platform.mcim.mapMCIMMirrorUrls
import com.movtery.zalithlauncher.game.download.assets.platform.mirroredCurseForgeSource
import com.movtery.zalithlauncher.game.download.assets.platform.mirroredPlatformSearcher
import com.movtery.zalithlauncher.game.download.modpack.install.ModFile
import com.movtery.zalithlauncher.game.download.modpack.install.ModPackInfo
import com.movtery.zalithlauncher.game.download.modpack.install.ModPackInfoTask
import com.movtery.zalithlauncher.game.download.modpack.platform.PackPlatform
import com.movtery.zalithlauncher.game.version.installed.VersionFolders
import com.movtery.zalithlauncher.ui.androidText
import com.movtery.zalithlauncher.utils.file.copyDirectoryContents
import com.movtery.zalithlauncher.utils.logging.Logger
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException

private const val TAG = "CurseForgePack"

/**
 * CurseForge 整合包安装信息
 * @param manifest CurseForge 整合包清单
 */
class CurseForgePack(
    root: File,
    private val manifest: CurseForgeManifest
) : ModPackInfoTask(
    root = root,
    platform = PackPlatform.CurseForge
) {
    /**
     * 将 CurseForge 的清单读取为 [ModPackInfo] 信息对象
     */
    suspend fun readCurseForge(
        task: Task,
        targetFolder: File,
        extractFiles: suspend (internalPath: String, outputDir: File) -> Unit
    ): ModPackInfo {
        val modsFolder = VersionFolders.MOD.getDir(targetFolder)

        //获取全部需要下载的模组文件
        val totalCount = manifest.files.size
        val files = manifest.files.mapIndexed { index, manifestFile ->
            val modFile = if (manifestFile.fileName.isNullOrBlank() || manifestFile.getFileUrl() == null) {
                ModFile(
                    getFile = {
                        runCatching {
                            val version = mirroredPlatformSearcher(
                                searchers = mirroredCurseForgeSource()
                            ) { searcher ->
                                searcher.getVersion(
                                    projectID = manifestFile.projectID.toString(),
                                    fileID = manifestFile.fileID.toString()
                                )
                            }.data
                            val url = version.fixedFileUrl() ?: throw IOException("Can't get the file url")
                            val fileName = version.fileName ?: throw IOException("Can't get the file name")

                            //获取项目
                            val project = mirroredPlatformSearcher(
                                searchers = mirroredCurseForgeSource()
                            ) { searcher ->
                                searcher.getProject(
                                    projectID = manifestFile.projectID.toString()
                                )
                            }.data
                            //通过项目类型指定目标下载目录
                            val folder = project.getPlatformClassesOrNull()
                                ?.versionFolder?.folderName
                                ?.let { folderName ->
                                    File(targetFolder, folderName)
                                } ?: modsFolder

                            ModFile(
                                outputFile = File(folder, fileName),
                                downloadUrls = url.mapMCIMMirrorUrls(),
                                sha1 = version.getSHA1()
                            )
                        }.onFailure { e ->
                            when (e) {
                                is FileNotFoundException -> Logger.warning(TAG, "Could not query api.curseforge.com for deleted mods: ${manifestFile.projectID}, ${manifestFile.fileID}", e)
                                is IOException, is JsonParseException -> Logger.warning(TAG, "Unable to fetch the file name projectID=${manifestFile.projectID}, fileID=${manifestFile.fileID}", e)
                                else -> Logger.warning(TAG, "Unable to fetch the file name projectID=${manifestFile.projectID}, fileID=${manifestFile.fileID}", e)
                            }
                        }.getOrThrow()
                    }
                )
            } else {
                ModFile(
                    outputFile = File(modsFolder, manifestFile.fileName),
                    downloadUrls = listOf(manifestFile.getFileUrl()!!)
                )
            }
            task.updateProgress(
                index.toFloat() / totalCount.toFloat()
            )
            task.updateMessage(
                androidText(
                    R.string.download_modpack_install_get_mod_url,
                    index, totalCount
                )
            )
            modFile
        }

        //获取模组加载器信息
        val loaders = manifest.minecraft.modLoaders.mapNotNull { modloader ->
            val id = modloader.id
            when {
                id.startsWith("forge-") -> ModLoader.FORGE to id.removePrefix("forge-")
                id.startsWith("fabric-") -> ModLoader.FABRIC to id.removePrefix("fabric-")
                id.startsWith("neoforge-") -> ModLoader.NEOFORGE to id.removePrefix("neoforge-")
                id.startsWith("quilt-") -> ModLoader.QUILT to id.removePrefix("quilt-")
                else -> null
            }
        }

        //提取覆盖包到目标目录
        task.updateProgress(-1f)
        task.updateMessage(androidText(R.string.download_modpack_install_overrides))
        extractFiles(manifest.overrides ?: "overrides", targetFolder)

        return ModPackInfo(
            name = manifest.name,
            ram = manifest.minecraft.recommendedRam,
            files = files,
            loaders = loaders,
            gameVersion = manifest.minecraft.gameVersion
        )
    }

    override suspend fun readInfo(
        task: Task,
        versionFolder: File,
        root: File
    ): ModPackInfo {
        return readCurseForge(
            task = task,
            targetFolder = versionFolder,
            extractFiles = { internal, out ->
                val sourceDir = internal.takeIf { it.isNotBlank() }
                    ?.let { File(root, it) }
                    ?: root
                //提取文件
                copyDirectoryContents(
                    from = sourceDir,
                    to = out,
                    onProgress = { progress ->
                        task.updateProgress(progress)
                    }
                )
            }
        )
    }
}