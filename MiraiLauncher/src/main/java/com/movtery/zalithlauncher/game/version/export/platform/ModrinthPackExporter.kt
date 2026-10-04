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
import com.movtery.zalithlauncher.game.download.assets.platform.curseforge.models.fixedFileUrl
import com.movtery.zalithlauncher.game.download.assets.platform.mirroredCurseForgeSource
import com.movtery.zalithlauncher.game.download.assets.platform.mirroredModrinthSource
import com.movtery.zalithlauncher.game.download.assets.platform.mirroredPlatformSearcher
import com.movtery.zalithlauncher.game.download.assets.platform.modrinth.models.getPrimary
import com.movtery.zalithlauncher.game.download.modpack.platform.modrinth.ModrinthManifest
import com.movtery.zalithlauncher.game.version.export.AbstractExporter
import com.movtery.zalithlauncher.game.version.export.ExportInfo
import com.movtery.zalithlauncher.game.version.export.PackType
import com.movtery.zalithlauncher.game.version.installed.Version
import com.movtery.zalithlauncher.game.version.mod.enabledMod
import com.movtery.zalithlauncher.game.version.mod.isDisabled
import com.movtery.zalithlauncher.ui.androidText
import com.movtery.zalithlauncher.utils.GSON
import com.movtery.zalithlauncher.utils.logging.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import org.jackhuang.hmcl.util.DigestUtils
import java.io.File
import java.nio.file.Files

private const val TAG = "ModrinthPackExporter"

/**
 * Modrinth 整合包导出工具
 */
class ModrinthPackExporter: AbstractExporter(
    type = PackType.Modrinth
) {
    private val files = mutableListOf<ModrinthManifest.ManifestFile>()
    private val filesInManifest = mutableListOf<File>()

    override fun MutableList<TitledTask>.buildTasks(
        context: Context,
        version: Version,
        info: ExportInfo,
        tempPath: File
    ) {
        if (info.packModrinth) {
            addTask(
                id = "ModrinthPackExporter.FetchRemote",
                title = androidText(R.string.versions_export_task_fetch_remote),
                icon = R.drawable.ic_search
            ) { task ->
                //获取远端数据
                packRemote(
                    packCurseForge = info.packCurseForge,
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
            id = "ModrinthPackExporter.PackManifest",
            title = androidText(R.string.versions_export_task_pack_manifest),
            icon = R.drawable.ic_build_filled
        ) {
            val gameName = info.gamePath.name
            val blackList = listOf(
                File(info.gamePath, "$gameName.json"),
                File(info.gamePath, "$gameName.jar")
            )

            val override = File(tempPath, "client-overrides")
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

            val dependencies = buildMap {
                put("minecraft", info.mcVersion)

                info.loader?.let { loader ->
                    val identifier = when (loader.loader) {
                        ModLoader.FORGE -> "forge"
                        ModLoader.NEOFORGE -> "neoforge"
                        ModLoader.FABRIC -> "fabric-loader"
                        ModLoader.QUILT -> "quilt-loader"
                        else -> null
                    } ?: return@let
                    put(identifier, loader.version)
                }
            }

            val manifest = ModrinthManifest(
                game = "minecraft",
                formatVersion = 1,
                versionId = info.version,
                name = info.name,
                summary = info.summary,
                files = files.toTypedArray(),
                dependencies = dependencies
            )

            val index = File(tempPath, "modrinth.index.json")
            val jsonString = GSON.toJson(manifest)
            //写入整合包清单信息
            index.writeText(jsonString)
        }
    }

    override val fileSuffix: String
        get() = "mrpack"

    private suspend fun packRemote(
        packCurseForge: Boolean,
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

                        val inManifest = runCatching {
                            val path = file.toPath()

                            val sha1 = DigestUtils.digestToString("SHA-1", path)
                            val sha512 = DigestUtils.digestToString("SHA-512", path)

                            val modrinthDeferred = async(Dispatchers.IO) {
                                val version = runCatching {
                                    mirroredPlatformSearcher(
                                        searchers = mirroredModrinthSource()
                                    ) { searcher ->
                                        searcher.getVersionByLocalFile(file, sha1)
                                    }
                                }.getOrNull() ?: return@async null

                                val modrinthFile = version.files.getPrimary() ?: return@async null
                                modrinthFile.url
                            }

                            val curseForgeDeferred = async(Dispatchers.IO) {
                                val version = runCatching {
                                    mirroredPlatformSearcher(
                                        searchers = mirroredCurseForgeSource()
                                    ) { searcher ->
                                        searcher.getVersionByLocalFile(file, sha1)
                                    }
                                }.getOrNull() ?: return@async null

                                version.fixedFileUrl() ?: return@async null
                            }

                            val links = listOfNotNull(
                                modrinthDeferred,
                                curseForgeDeferred.takeIf { packCurseForge }
                            ).awaitAll().filterNotNull().takeIf {
                                it.isNotEmpty()
                            } ?: return@runCatching false

                            val resourceFile = ModrinthManifest.ManifestFile(
                                path = relativePath(
                                    file = enabledMod(file),
                                    rootPath = gamePath.absolutePath
                                ),
                                hashes = ModrinthManifest.ManifestFile.Hashes(sha1, sha512),
                                env = if (file.isDisabled()) {
                                    ModrinthManifest.ManifestFile.Env(client = "optional")
                                } else null,
                                downloads = links.toTypedArray(),
                                fileSize = Files.size(path)
                            )

                            files.add(resourceFile)
                            true
                        }.onFailure {
                            Logger.warning(TAG, "Failed to obtain remote data for ${file.name}!", it)
                        }.getOrDefault(false)

                        if (inManifest) {
                            filesInManifest.add(file)
                        }
                    }

                    onProgress(null)
                }
            }
        }
    }
}