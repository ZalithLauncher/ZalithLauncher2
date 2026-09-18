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

import com.google.gson.JsonParseException
import com.movtery.zalithlauncher.game.addons.modloader.ModLoader
import com.movtery.zalithlauncher.game.version.mod.LocalMod
import com.movtery.zalithlauncher.game.version.mod.ModMetadataReader
import com.movtery.zalithlauncher.game.version.mod.meta.FabricModMetadata
import com.movtery.zalithlauncher.utils.GSON
import com.movtery.zalithlauncher.utils.file.UnpackZipException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.commons.compress.archivers.zip.ZipFile
import org.apache.commons.io.FileUtils
import java.io.File
import java.io.IOException
import java.util.zip.ZipFile as JDKZipFile

/**
 * [Reference HMCL](https://github.com/HMCL-dev/HMCL/blob/4650287/HMCLCore/src/main/java/org/jackhuang/hmcl/mod/modinfo/FabricModMetadata.java)
 */
object FabricModMetadataReader : ModMetadataReader {
    override suspend fun fromLocal(modFile: File): LocalMod = withContext(Dispatchers.IO) {
        try {
            JDKZipFile(modFile).use { zip ->
                try {
                    val entry = zip.getEntry("fabric.mod.json")
                        ?: throw IOException("File $modFile is not a Fabric mod (fabric.mod.json not found).")

                    zip.getInputStream(entry).bufferedReader().use { reader ->
                        return@withContext parseFabricModMetadata(zip, modFile, reader.readText())
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

    private fun parseFabricModMetadata(
        zip: JDKZipFile,
        modFile: File,
        jsonText: String
    ): LocalMod {
        val metadata = GSON.fromJson(jsonText, FabricModMetadata::class.java)
            ?: throw JsonParseException("Json object cannot be null.")

        val authors = parseAuthors(metadata.authors)
        val icon = zip.tryGetIcon(metadata.icon)

        return LocalMod(
            modFile = modFile,
            fileSize = FileUtils.sizeOf(modFile),
            id = metadata.id,
            loader = ModLoader.FABRIC,
            name = metadata.name,
            description = metadata.description,
            version = metadata.version,
            authors = authors,
            icon = icon
        )
    }

    private fun readWithApacheZip(modFile: File): LocalMod {
        val zipFile = ZipFile.builder()
            .setFile(modFile)
            .get()

        zipFile.use { zip ->
            val fabricModEntry = zip.getEntry("fabric.mod.json")
                ?: throw IOException("File $modFile is not a Fabric mod (fabric.mod.json not found).")

            zip.getInputStream(fabricModEntry).bufferedReader().use { reader ->
                val metadata = GSON.fromJson(reader.readText(), FabricModMetadata::class.java)
                    ?: throw JsonParseException("Json object cannot be null.")

                val authors = parseAuthors(metadata.authors)
                val icon = zip.tryGetIcon(metadata.icon)

                return LocalMod(
                    modFile = modFile,
                    fileSize = FileUtils.sizeOf(modFile),
                    id = metadata.id,
                    loader = ModLoader.FABRIC,
                    name = metadata.name,
                    description = metadata.description,
                    version = metadata.version,
                    authors = authors,
                    icon = icon
                )
            }
        }
    }

    private fun parseAuthors(authors: List<FabricModMetadata.FabricModAuthor>?): List<String> {
        return authors?.mapNotNull { it.name } ?: emptyList()
    }
}