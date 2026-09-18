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

package com.movtery.zalithlauncher.game.download.modpack.platform.modrinth

import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.coroutine.Task
import com.movtery.zalithlauncher.game.addons.modloader.ModLoader
import com.movtery.zalithlauncher.game.download.assets.platform.mcim.mapMCIMMirrorUrls
import com.movtery.zalithlauncher.game.download.modpack.install.ModFile
import com.movtery.zalithlauncher.game.download.modpack.install.ModPackInfo
import com.movtery.zalithlauncher.game.download.modpack.install.ModPackInfoTask
import com.movtery.zalithlauncher.game.download.modpack.platform.PackPlatform
import com.movtery.zalithlauncher.ui.androidText
import com.movtery.zalithlauncher.utils.file.copyDirectoryContents
import java.io.File

/**
 * Modrinth 整合包安装信息
 * @param manifest Modrinth 整合包清单
 */
class ModrinthPack(
    root: File,
    private val manifest: ModrinthManifest
) : ModPackInfoTask(
    root = root,
    platform = PackPlatform.Modrinth
) {
    /**
     * 将 Modrinth 的清单读取为 [ModPackInfo] 信息对象
     */
    suspend fun readModrinth(
        task: Task,
        targetFolder: File,
        extractFiles: suspend (internalPath: String, outputDir: File) -> Unit
    ): ModPackInfo {
        //获取所有需要下载的模组文件
        val files = manifest.files.mapNotNull { manifestFile ->
            //客户端不支持
            if (manifestFile.env?.client == "unsupported") return@mapNotNull null
            ModFile(
                outputFile = File(targetFolder, manifestFile.path),
                downloadUrls = manifestFile.downloads.mapMCIMMirrorUrls(),
                sha1 = manifestFile.hashes.sha1
            )
        }

        //获取加载器信息
        val loaders = manifest.dependencies.entries.mapNotNull { (id, version) ->
            when (id) {
                "forge" -> ModLoader.FORGE to version
                "neoforge" -> ModLoader.NEOFORGE to version
                "fabric-loader" -> ModLoader.FABRIC to version
                "quilt-loader" -> ModLoader.QUILT to version
                else -> null
            }
        }

        //提取覆盖包到目标目录
        task.updateProgress(-1f)
        task.updateMessage(androidText(R.string.download_modpack_install_overrides))
        extractFiles("overrides", targetFolder)
        extractFiles("client-overrides", targetFolder)

        return ModPackInfo(
            name = manifest.name,
            summary = manifest.summary,
            files = files,
            loaders = loaders,
            gameVersion = manifest.getGameVersion()
        )
    }

    override suspend fun readInfo(
        task: Task,
        versionFolder: File,
        root: File
    ): ModPackInfo {
        return readModrinth(
            task = task,
            targetFolder = versionFolder,
            extractFiles = { internalPath, outputDir ->
                val sourceDir = internalPath.takeIf { it.isNotBlank() }
                    ?.let { File(root, it) }
                    ?: root
                if (sourceDir.exists()) {
                    //提取文件
                    copyDirectoryContents(
                        from = sourceDir,
                        to = outputDir,
                        onProgress = { progress ->
                            task.updateProgress(progress)
                        }
                    )
                }
            }
        )
    }
}