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

package com.movtery.zalithlauncher.game.version.mod.reader

import com.movtery.zalithlauncher.game.addons.modloader.ModLoader
import com.movtery.zalithlauncher.game.version.mod.LocalMod
import com.movtery.zalithlauncher.game.version.mod.ModMetadataReader
import com.movtery.zalithlauncher.game.version.mod.isDisabled
import com.movtery.zalithlauncher.game.version.mod.meta.PackMcMeta
import com.movtery.zalithlauncher.utils.GSON
import com.movtery.zalithlauncher.utils.file.UnpackZipException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.commons.compress.archivers.zip.ZipFile
import org.apache.commons.io.FileUtils
import java.io.File
import java.io.IOException
import java.io.InputStreamReader
import java.util.zip.ZipFile as JDKZipFile

/**
 * [Reference HMCL](https://github.com/HMCL-dev/HMCL/blob/4650287/HMCLCore/src/main/java/org/jackhuang/hmcl/mod/modinfo/PackMcMeta.java)
 */
object PackMcMetadataReader : ModMetadataReader {
    override suspend fun fromLocal(modFile: File): LocalMod = withContext(Dispatchers.IO) {
        try {
            JDKZipFile(modFile).use { zip ->
                try {
                    val entry = zip.getEntry("pack.mcmeta")
                        ?: throw IOException("pack.mcmeta not found in resource pack $modFile")

                    zip.getInputStream(entry).use { inputStream ->
                        InputStreamReader(inputStream).use { reader ->
                            val meta = GSON.fromJson(reader, PackMcMeta::class.java)

                            return@withContext LocalMod(
                                modFile = modFile,
                                fileSize = FileUtils.sizeOf(modFile),
                                id = getRawFileName(modFile),
                                loader = ModLoader.PACK,
                                name = getRawFileName(modFile),
                                description = meta.pack.description.toPlainText(),
                                version = "",
                                authors = emptyList(),
                                icon = null,
                                notMod = false
                            )
                        }
                    }
                } catch (e: Exception) {
                    throw UnpackZipException(e)
                }
            }
        } catch (e: Exception) {
            if (e !is UnpackZipException) return@withContext readWithApacheZip(modFile)
            else throw e
        }
    }

    private fun readWithApacheZip(modFile: File): LocalMod {
        val zipFile = ZipFile.Builder()
            .setFile(modFile)
            .get()

        val rawName = getRawFileName(modFile)

        zipFile.use { zip ->
            val entry = zip.getEntry("pack.mcmeta")
                ?: throw IOException("pack.mcmeta not found in resource pack $modFile")

            zip.getInputStream(entry).use { inputStream ->
                InputStreamReader(inputStream).use { reader ->
                    val meta = GSON.fromJson(reader, PackMcMeta::class.java)

                    return LocalMod(
                        modFile = modFile,
                        fileSize = FileUtils.sizeOf(modFile),
                        id = rawName,
                        loader = ModLoader.PACK,
                        name = rawName,
                        description = meta.pack.description.toPlainText(),
                        version = "",
                        authors = emptyList(),
                        icon = null,
                        notMod = false
                    )
                }
            }
        }
    }

    /**
     * 获取原始文件名，移除 .disabled 后缀
     */
    private fun getRawFileName(file: File): String {
        val fileName = file.name
        return if (file.isDisabled()) fileName.removeSuffix(".disabled")
        else fileName
    }
}
