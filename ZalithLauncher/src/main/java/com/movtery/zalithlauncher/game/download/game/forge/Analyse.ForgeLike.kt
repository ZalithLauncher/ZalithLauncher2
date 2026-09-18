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

package com.movtery.zalithlauncher.game.download.game.forge

import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import com.movtery.zalithlauncher.coroutine.Task
import com.movtery.zalithlauncher.game.addons.mirror.mapBMCLMirrorUrls
import com.movtery.zalithlauncher.game.download.game.GameLibDownloader
import com.movtery.zalithlauncher.game.download.game.copyVanillaFiles
import com.movtery.zalithlauncher.game.download.game.getLibraryPath
import com.movtery.zalithlauncher.game.download.game.models.ForgeLikeInstallProcessor
import com.movtery.zalithlauncher.game.version.download.BaseMinecraftDownloader
import com.movtery.zalithlauncher.game.version.download.artifactToPath
import com.movtery.zalithlauncher.game.version.download.parseTo
import com.movtery.zalithlauncher.game.versioninfo.MinecraftVersions
import com.movtery.zalithlauncher.game.versioninfo.models.GameManifest
import com.movtery.zalithlauncher.utils.GSON
import com.movtery.zalithlauncher.utils.file.ensureDirectory
import com.movtery.zalithlauncher.utils.file.extractEntryToFile
import com.movtery.zalithlauncher.utils.file.readText
import com.movtery.zalithlauncher.utils.json.merge
import com.movtery.zalithlauncher.utils.json.parseToJson
import com.movtery.zalithlauncher.utils.logging.Logger
import com.movtery.zalithlauncher.utils.network.fetchStringFromUrls
import com.movtery.zalithlauncher.utils.network.withRetry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.util.zip.ZipFile

private const val TAG = "Analyse.ForgeLike"

const val FORGE_LIKE_ANALYSE_ID = "Analyse.ForgeLike"

/**
 * Forge Like 分析与安装支持库 (仅支持处理新版本 Forge、NeoForge)
 */
fun getForgeLikeAnalyseTask(
    downloader: BaseMinecraftDownloader,
    targetTempInstaller: File,
    removeFromDownload: String,
    tempMinecraftFolder: File,
    sourceInherit: String,
    processedInherit: String,
): Task {
    return Task.runTask(
        id = FORGE_LIKE_ANALYSE_ID,
        dispatcher = Dispatchers.IO,
        task = { task ->
            if (sourceInherit != processedInherit) {
                //准备安装环境
                //复制原版文件
                copyVanillaFiles(
                    sourceGameFolder = tempMinecraftFolder,
                    sourceVersion = sourceInherit,
                    destinationGameFolder = tempMinecraftFolder,
                    targetVersion = processedInherit
                )
            }

            analyseNewForge(
                task = task,
                downloader = downloader,
                removeFromDownload = removeFromDownload,
                installer = targetTempInstaller,
                tempMinecraftFolder = tempMinecraftFolder,
                inherit = processedInherit,
            )
        }
    )
}

/**
 * [Reference PCL2](https://github.com/Hex-Dragon/PCL2/blob/bf6fa718c89e8615b947d1c639ed16a72ce125e0/Plain%20Craft%20Launcher%202/Pages/PageDownload/ModDownloadLib.vb#L1324-L1411)
 * 处理新版 Forge、NeoForge
 */
private suspend fun analyseNewForge(
    task: Task,
    downloader: BaseMinecraftDownloader,
    removeFromDownload: String,
    installer: File,
    tempMinecraftFolder: File,
    inherit: String,
) {
    task.updateProgress(-1f)

    //解析 NeoForge 的支持库列表，并统一进行下载
    val (installProfile, versionString) = withContext(Dispatchers.IO) {
        ZipFile(installer).use { zip ->
            task.updateProgress(0.2f)

            val installProfileString = zip.readText("install_profile.json")
            val versionString = zip.readText("version.json")

            val installProfile = installProfileString.parseToJson()
            installProfile["libraries"]?.takeIf { it.isJsonArray }?.let { libraries ->
                val libraryList: List<GameManifest.Library> =
                    GSON.fromJson(libraries, object : TypeToken<List<GameManifest.Library>>() {}.type) ?: return@let

                for (library in libraryList) {
                    val path = artifactToPath(library) ?: continue
                    zip.getEntry("maven/$path")?.let { entry ->
                        val dest = File(tempMinecraftFolder, "libraries/$path")
                        zip.extractEntryToFile(entry, dest)
                    }
                }
            }

            installProfile["path"]?.takeIf { it.isJsonPrimitive }?.let { path ->
                val libraryPath = getLibraryPath(path.asString)
                zip.getEntry("maven/$libraryPath")?.let { entry ->
                    val dest = File(tempMinecraftFolder, "libraries/$libraryPath")
                    zip.extractEntryToFile(entry, dest)
                }
            }

            installProfile to versionString
        }
    }

    //合并为一个Json
    installProfile.merge(versionString.parseToJson())

    //计划下载 install_profile.json 内的所有支持库
    val libDownloader = GameLibDownloader(
        downloader = downloader,
        gameJson = installProfile.toString()
    )
    libDownloader.schedule(task, File(tempMinecraftFolder, "libraries").ensureDirectory(), false)

    //添加 Mojang Mappings 下载信息
    task.updateProgress(0.4f)
    scheduleMojangMappings(
        mergedJson = installProfile,
        tempMinecraftDir = tempMinecraftFolder,
        tempVanillaJar = File(tempMinecraftFolder, "versions/$inherit/$inherit.jar"),
        tempInstaller = installer
    ) { urls, sha1, targetFile, size ->
        libDownloader.scheduleDownload(
            urls = urls,
            sha1 = sha1,
            targetFile = targetFile,
            size = size
        )
    }

    task.updateProgress(0.8f)

    libDownloader.apply {
        removeDownload { lib ->
            (lib.targetFile.name.endsWith("$removeFromDownload.jar") ||
             lib.targetFile.name.endsWith("$removeFromDownload-client.jar")).also {
                if (it) {
                    Logger.info(TAG,
                        "The download task has been removed from the scheduled downloads: \n" +
                                "url: \n${lib.urls.joinToString("\n")}\n" +
                                "target path: ${lib.targetFile.absolutePath}"
                    )
                }
            }
        }
    }

    //开始下载 NeoForge 支持库
    libDownloader.download(task)

    task.updateProgress(1f)
}

/**
 * 解析并提交下载Mojang映射
 */
private suspend fun scheduleMojangMappings(
    mergedJson: JsonObject,
    tempMinecraftDir: File,
    tempVanillaJar: File,
    tempInstaller: File,
    schedule: (urls: List<String>, sha1: String?, targetFile: File, size: Long) -> Unit
) = withContext(Dispatchers.IO) {
    val tempDir = File(tempMinecraftDir, ".temp/forge_installer_cache").ensureDirectory()
    val vars = mutableMapOf<String, String>()

    ZipFile(tempInstaller).use { zip ->
        zip.readText("install_profile.json").parseToJson()["data"].asJsonObject?.let { data ->
            for ((key, value) in data.entrySet()) {
                if (value.isJsonObject) {
                    val client = value.asJsonObject["client"]
                    if (client != null && client.isJsonPrimitive) {
                        parseLiteral(
                            baseDir = tempMinecraftDir,
                            literal = client.asString,
                            plainConverter = { str ->
                                val dest: Path = Files.createTempFile(tempDir.toPath(), null, null)
                                val item = str
                                    .removePrefix("\\")
                                    .removePrefix("/")
                                    .replace("\\", "/")
                                zip.extractEntryToFile(item, dest.toFile())
                                dest.toString()
                            }
                        )?.let {
                            vars[key] = it
                        }
                    }
                }
            }
        }
    }

    vars += mapOf(
        "SIDE" to "client",
        "MINECRAFT_JAR" to tempVanillaJar.absolutePath,
        "MINECRAFT_VERSION" to tempVanillaJar.absolutePath,
        "ROOT" to tempMinecraftDir.absolutePath,
        "INSTALLER" to tempInstaller.absolutePath,
        "LIBRARY_DIR" to File(tempMinecraftDir, "libraries").absolutePath
    )

    parseProcessors(
        baseDir = tempMinecraftDir,
        jsonObject = mergedJson,
        vars = vars,
        schedule = schedule
    )
}

/**
 * [Reference HMCL](https://github.com/HMCL-dev/HMCL/blob/6e05b5ee58e67cd40e58c6f6002f3599897ca358/HMCLCore/src/main/java/org/jackhuang/hmcl/download/forge/ForgeNewInstallTask.java#L332-L360)
 */
private suspend fun parseProcessors(
    baseDir: File,
    jsonObject: JsonObject,
    vars: Map<String, String>,
    schedule: (urls: List<String>, sha1: String?, targetFile: File, size: Long) -> Unit
) = withContext(Dispatchers.IO) {
    val processors: List<ForgeLikeInstallProcessor> = jsonObject["processors"]?.asJsonArray?.let { processors ->
        val type = object : TypeToken<List<ForgeLikeInstallProcessor>>() {}.type
        GSON.fromJson(processors, type)
    } ?: return@withContext

    processors.map { processor ->
        parseOptions(baseDir, processor.getArgs(), vars)
    }.forEach { options ->
        if (options["task"] != "DOWNLOAD_MOJMAPS" || options["side"] != "client") return@forEach
        val version = options["version"] ?: return@forEach
        val output = options["output"] ?: return@forEach
        Logger.info(TAG, "Patching DOWNLOAD_MOJMAPS task")

        val versionManifest = MinecraftVersions.getVersionManifest()
        versionManifest.versions.find { it.id == version }?.let { vanilla ->
            val manifest = withRetry(FORGE_LIKE_ANALYSE_ID, maxRetries = 2) {
                fetchStringFromUrls(
                    vanilla.url.mapBMCLMirrorUrls()
                ).parseTo(GameManifest::class.java)
            }
            manifest.downloads?.clientMappings?.let { mappings ->
                schedule(mappings.url.mapBMCLMirrorUrls(), mappings.sha1, File(output), mappings.size)
                Logger.info(TAG, "Mappings: ${mappings.url} (SHA1: ${mappings.sha1})")
            } ?: throw Exception("client_mappings download info not found")
        }
    }
}
