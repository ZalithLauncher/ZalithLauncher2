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
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.movtery.zalithlauncher.game.addons.modloader.ModLoader
import com.movtery.zalithlauncher.game.version.mod.LocalMod
import com.movtery.zalithlauncher.game.version.mod.ModMetadataReader
import com.movtery.zalithlauncher.game.version.mod.meta.ForgeOldModMetadata
import com.movtery.zalithlauncher.game.version.mod.meta.ForgeOldModMetadataList
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
 * [Reference HMCL](https://github.com/HMCL-dev/HMCL/blob/3eddfa2/HMCLCore/src/main/java/org/jackhuang/hmcl/mod/modinfo/ForgeOldModMetadata.java)
 */
object ForgeOldModMetadataReader : ModMetadataReader {
    override suspend fun fromLocal(modFile: File): LocalMod = withContext(Dispatchers.IO) {
        try {
            JDKZipFile(modFile).use { zip ->
                try {
                    val mcmodEntry = zip.getEntry("mcmod.info")
                        ?: throw IOException("File $modFile is not a Forge mod.")

                    zip.getInputStream(mcmodEntry).bufferedReader().use { reader ->
                        // new changed from https://github.com/HMCL-dev/HMCL/commit/2c428faa9540e8666ce3cec07ff284cb53eab34a
                        val jsonReader = JsonReader(reader)
                        val firstToken: JsonToken = jsonReader.peek()

                        val modList: List<ForgeOldModMetadata> = when (firstToken) {
                            JsonToken.BEGIN_ARRAY -> {
                                GSON.fromJson(jsonReader, object : TypeToken<List<ForgeOldModMetadata>>() {}.type)
                            }
                            JsonToken.BEGIN_OBJECT -> {
                                val list: ForgeOldModMetadataList = GSON.fromJson(jsonReader, ForgeOldModMetadataList::class.java)
                                list.modList ?: throw IOException("Mod $modFile `mcmod.info` is malformed.")
                            }
                            else -> throw JsonParseException("Unexpected first token: $firstToken")
                        }

                        if (modList.isEmpty()) {
                            throw IOException("Mod $modFile `mcmod.info` is malformed.")
                        }

                        val metadata = modList[0]
                        val authors = determineAuthors(metadata)
                        val icon = zip.tryGetIcon(metadata.logoFile)

                        return@withContext LocalMod(
                            modFile = modFile,
                            fileSize = FileUtils.sizeOf(modFile),
                            id = metadata.modId,
                            loader = ModLoader.FORGE,
                            name = metadata.name,
                            description = metadata.description,
                            version = metadata.version,
                            authors = authors,
                            icon = icon
                        )
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

        zipFile.use { zip ->
            val mcmodEntry = zip.getEntry("mcmod.info")
                ?: throw IOException("File $modFile is not a Forge mod.")

            zip.getInputStream(mcmodEntry).bufferedReader().use { reader ->
                val modList: List<ForgeOldModMetadata> = GSON.fromJson(
                    reader,
                    object : TypeToken<List<ForgeOldModMetadata>>() {}.type
                )

                if (modList.isEmpty()) {
                    throw IOException("Mod $modFile `mcmod.info` is malformed.")
                }

                val metadata = modList[0]
                val authors = determineAuthors(metadata)
                val icon = zip.tryGetIcon(metadata.logoFile)

                return LocalMod(
                    modFile = modFile,
                    fileSize = FileUtils.sizeOf(modFile),
                    id = metadata.modId,
                    loader = ModLoader.FORGE,
                    name = metadata.name,
                    description = metadata.description,
                    version = metadata.version,
                    authors = authors,
                    icon = icon
                )
            }
        }
    }

    private fun determineAuthors(metadata: ForgeOldModMetadata): List<String> {
        return when {
            metadata.authors.isNotEmpty() -> metadata.authors.toList()
            metadata.authorList.isNotEmpty() -> metadata.authorList.toList()
            metadata.author.isNotBlank() -> listOf(metadata.author)
            metadata.credits.isNotBlank() -> listOf(metadata.credits)
            else -> emptyList()
        }
    }
}