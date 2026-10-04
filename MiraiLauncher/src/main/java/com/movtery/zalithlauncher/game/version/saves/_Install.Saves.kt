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

package com.movtery.zalithlauncher.game.version.saves

import com.movtery.zalithlauncher.utils.file.CompressZipEntryAdapter
import com.movtery.zalithlauncher.utils.file.JavaZipEntryAdapter
import com.movtery.zalithlauncher.utils.file.UnpackZipException
import com.movtery.zalithlauncher.utils.file.ZipEntryBase
import com.movtery.zalithlauncher.utils.file.extractFromZip
import com.movtery.zalithlauncher.utils.logging.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.commons.compress.archivers.zip.ZipFile
import org.apache.commons.io.FileUtils
import java.io.File
import java.io.IOException
import java.util.zip.ZipFile as JDKZipFile

private const val TAG = "InstallSaves"

/**
 * 解压存档压缩包
 */
suspend fun unpackSaveZip(zipFile: File, targetPath: File) = withContext(Dispatchers.IO) {
    val path = extractLevelPath(zipFile) ?: throw IOException("Unable to locate the level where the level.dat file is stored.")
    Logger.info(TAG, "Found the level of the level.data file: $path")
    val target = File(targetPath, zipFile.nameWithoutExtension)
    try {
        JDKZipFile(zipFile).use { zip ->
            zip.extractFromZip(path, target)
            Logger.info(TAG, "Decompression is complete")
        }
    } catch (e: Exception) {
        if (e !is UnpackZipException) {
            tryApacheZip(zipFile, path, target)
        } else {
            throw e
        }
    }
    FileUtils.deleteQuietly(zipFile)
}

private suspend fun tryApacheZip(zipFile: File, path: String, target: File) {
    FileUtils.deleteQuietly(target) //清除一次目标文件夹（如果之前解压出错）
    val zipFile1 = ZipFile.Builder()
        .setFile(zipFile)
        .get()

    zipFile1.use { zip ->
        zip.extractFromZip(path, target)
        Logger.info(TAG, "Decompression is complete")
    }
}

/**
 * 读取zip文件，并找到level.data文件所在的路径
 * @param file 压缩包文件
 */
private fun extractLevelPath(file: File): String? {
    if (!file.exists() || !file.isFile) {
        return null
    }

    if (!file.name.endsWith(".zip", ignoreCase = true)) {
        return null
    }

    try {
        JDKZipFile(file).use { zip ->
            val entries = zip.entries().asSequence()
                .map { entry ->
                    JavaZipEntryAdapter(entry)
                }
            return findLevelEntryName(entries)
        }
    } catch (e: Exception) {
        if (e !is UnpackZipException) {
            return extractLevelPathCompress(file)
        } else {
            throw e
        }
    }
}

private fun extractLevelPathCompress(file: File): String? {
    val zipFile = ZipFile.Builder()
        .setFile(file)
        .get()

    zipFile.use { zip ->
        val entries = zip.entries.asSequence()
            .map { entry ->
                CompressZipEntryAdapter(entry)
            }
        return findLevelEntryName(entries)
    }
}

private fun <T : ZipEntryBase> findLevelEntryName(
    entries: Sequence<T>
): String? {
    val allEntries = entries.toList()
    var currentPrefix = ""

    while (true) {
        val inLayer = allEntries.filter {
            //过滤当前层级内的所有内容
            it.name.startsWith(currentPrefix) && it.name != currentPrefix
        }
        if (inLayer.isEmpty()) {
            //当前层无任何有效内容，这是一个无效的存档格式
            return null
        }

        //检查当前层级是否存在 level.dat
        val hasLevelDat = inLayer.any {
            val relative = it.name.removePrefix(currentPrefix)
            !it.isDirectory && relative.equals("level.dat", ignoreCase = true)
        }

        if (hasLevelDat) {
            //存在 level.dat 文件时，必须至少有一个文件夹
            //否则认为这是一个无效的存档格式
            val hasFolder = inLayer.any {
                val relative = it.name.removePrefix(currentPrefix)
                it.isDirectory || relative.contains("/")
            }
            return if (hasFolder) currentPrefix.removeSuffix("/") else null
        }

        //不存在 level.dat 时，只有在当前层级只有一个目录且没有文件的情况下才深入
        val relativeNames = inLayer.map {
            it.name.removePrefix(currentPrefix)
        }

        val topLevelNames = relativeNames.map {
            it.substringBefore("/")
        }.distinct()

        if (topLevelNames.size == 1) {
            val name = topLevelNames[0]
            val isFolder = inLayer.any {
                val rel = it.name.removePrefix(currentPrefix)
                rel.startsWith("$name/") || (rel == name && it.isDirectory)
            }
            val hasFileWithSameName = inLayer.any {
                val rel = it.name.removePrefix(currentPrefix)
                !it.isDirectory && rel == name
            }

            if (isFolder && !hasFileWithSameName) {
                currentPrefix = "$currentPrefix$name/"
                continue
            }
        }
        return null
    }
}
