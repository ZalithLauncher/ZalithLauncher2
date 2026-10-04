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

package com.movtery.zalithlauncher.game.download.modpack.install

import com.movtery.zalithlauncher.coroutine.Task
import com.movtery.zalithlauncher.game.download.assets.platform.Platform
import com.movtery.zalithlauncher.game.download.modpack.platform.AbstractPack
import com.movtery.zalithlauncher.game.download.modpack.platform.curseforge.CurseForgeManifest
import com.movtery.zalithlauncher.game.download.modpack.platform.curseforge.CurseForgePack
import com.movtery.zalithlauncher.game.download.modpack.platform.modrinth.ModrinthManifest
import com.movtery.zalithlauncher.game.download.modpack.platform.modrinth.ModrinthPack
import com.movtery.zalithlauncher.utils.GSON
import com.movtery.zalithlauncher.utils.file.extractFromZip
import com.movtery.zalithlauncher.utils.file.readText
import com.movtery.zalithlauncher.utils.logging.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import org.apache.commons.compress.archivers.zip.ZipFile as ApacheZipFile
import java.util.zip.ZipFile as JdkZipFile

private const val TAG = "ParserModPack"

/**
 * 用于统一解析流程的整合包解析配置
 * @param manifestPath 清单文件在压缩包内的位置
 * @param manifestType 清单文件类型（用于反序列化的类）
 * @param createPack 通过清单创建通用整合包格式
 * @param readPack 使用创建的通用整合包格式生成 [ModPackInfo]
 */
private data class PackParserConfig<M, P : AbstractPack>(
    val manifestPath: String,
    val manifestType: Class<M>,
    val createPack: (manifest: M) -> P,
    val readPack: suspend P.(Task, File, suspend (String, File) -> Unit) -> ModPackInfo
)

/**
 * 仅适用于内置在线下载，通过 [Platform] 解析不同类型的整合包
 * @return 整合包在线下载模组信息
 */
suspend fun parserModPack(
    file: File,
    platform: Platform,
    targetFolder: File,
    task: Task
): ModPackInfo = withContext(Dispatchers.IO) {
    //此处不需要使用root，因为仅仅只是获取ModPackInfo对象
    //所以使用emptyFile填充，创建对象而已
    val emptyFile = File("")

    when (platform) {
        Platform.CURSEFORGE -> {
            val config = PackParserConfig(
                manifestPath = "manifest.json",
                manifestType = CurseForgeManifest::class.java,
                createPack = { CurseForgePack(root = emptyFile, it) },
                readPack = { task, target, extract ->
                    readCurseForge(task, target, extract)
                }
            )
            parseModPackGeneric(file, targetFolder, task, config)
        }
        Platform.MODRINTH -> {
            val config = PackParserConfig(
                manifestPath = "modrinth.index.json",
                manifestType = ModrinthManifest::class.java,
                createPack = { ModrinthPack(root = emptyFile, it) },
                readPack = { task, target, extract ->
                    readModrinth(task, target, extract)
                }
            )
            parseModPackGeneric(file, targetFolder, task, config)
        }
    }
}

/**
 * 使用 [JdkZipFile] 工具尝试解析，如果出现问题，将使用 [ApacheZipFile] 工具兜底解析
 */
suspend fun <T> withZipFile(
    file: File,
    loaderJdk: suspend (JdkZipFile) -> T,
    loaderApache: suspend (ApacheZipFile) -> T
): T {
    return withContext(Dispatchers.IO) {
        try {
            JdkZipFile(file).use { loaderJdk(it) }
        } catch (e: Exception) {
            Logger.warning(TAG, "JDK ZipFile failed to parse ${file.name}, fallback to Apache ZipFile.", e)
            ApacheZipFile.builder().setFile(file).get().use { loaderApache(it) }
        }
    }
}

/**
 * 通用整合包解析逻辑
 */
private suspend fun <M, P : AbstractPack> parseModPackGeneric(
    file: File,
    targetFolder: File,
    task: Task,
    config: PackParserConfig<M, P>
): ModPackInfo = withZipFile(
    file = file,
    loaderJdk = { zip ->
        task.updateProgress(-1f)

        val json = zip.readText(config.manifestPath)
        val manifest = GSON.fromJson(json, config.manifestType)
        val pack = config.createPack(manifest)

        config.readPack(pack, task, targetFolder) { internal, out ->
            zip.extractFromZip(internal, out)
        }
    },
    loaderApache = { zip ->
        task.updateProgress(-1f)

        val entry = zip.getEntry(config.manifestPath)
            ?: throw IOException("${config.manifestPath} not found in ${file.name}")

        val json = zip.getInputStream(entry).bufferedReader().readText()
        val manifest = GSON.fromJson(json, config.manifestType)
        val pack = config.createPack(manifest)

        config.readPack(pack, task, targetFolder) { internal, out ->
            zip.extractFromZip(internal, out)
        }
    }
)