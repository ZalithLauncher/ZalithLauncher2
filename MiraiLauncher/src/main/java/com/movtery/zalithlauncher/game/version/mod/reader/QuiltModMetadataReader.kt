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

import com.google.gson.JsonObject
import com.google.gson.JsonParseException
import com.movtery.zalithlauncher.game.addons.modloader.ModLoader
import com.movtery.zalithlauncher.game.version.mod.LocalMod
import com.movtery.zalithlauncher.game.version.mod.ModMetadataReader
import com.movtery.zalithlauncher.game.version.mod.meta.QuiltModMetadata
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
 * [Reference HMCL](https://github.com/HMCL-dev/HMCL/blob/4650287/HMCLCore/src/main/java/org/jackhuang/hmcl/mod/modinfo/QuiltModMetadata.java)
 */
object QuiltModMetadataReader : ModMetadataReader {
    override suspend fun fromLocal(modFile: File): LocalMod = withContext(Dispatchers.IO) {
        try {
            JDKZipFile(modFile).use { zip ->
                try {
                    val entry = zip.getEntry("quilt.mod.json")
                        ?: throw IOException("File $modFile is not a Quilt mod.")

                    zip.getInputStream(entry).bufferedReader().use { reader ->
                        val metadata = GSON.fromJson(
                            reader.readText(),
                            QuiltModMetadata::class.java
                        ) ?: throw JsonParseException("Json object cannot be null.")

                        if (metadata.schemaVersion != 1) {
                            throw IOException("File $modFile is not a supported Quilt mod (schema version ${metadata.schemaVersion}).")
                        }

                        val quiltLoader = metadata.quiltLoader
                        val contributors = parseContributors(quiltLoader.metadata.contributors)
                        val icon = zip.tryGetIcon(quiltLoader.metadata.icon)

                        return@withContext LocalMod(
                            modFile = modFile,
                            fileSize = FileUtils.sizeOf(modFile),
                            id = quiltLoader.id,
                            loader = ModLoader.QUILT,
                            name = quiltLoader.metadata.name,
                            description = quiltLoader.metadata.description,
                            version = quiltLoader.version,
                            authors = contributors,
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
            val quiltModEntry = zip.getEntry("quilt.mod.json")
                ?: throw IOException("File $modFile is not a Quilt mod.")

            zip.getInputStream(quiltModEntry).bufferedReader().use { reader ->
                val metadata = GSON.fromJson(
                    reader.readText(),
                    QuiltModMetadata::class.java
                ) ?: throw JsonParseException("Json object cannot be null.")

                if (metadata.schemaVersion != 1) {
                    throw IOException("File $modFile is not a supported Quilt mod (schema version ${metadata.schemaVersion}).")
                }

                val quiltLoader = metadata.quiltLoader
                val contributors = parseContributors(quiltLoader.metadata.contributors)
                val icon = zip.tryGetIcon(quiltLoader.metadata.icon)

                return LocalMod(
                    modFile = modFile,
                    fileSize = FileUtils.sizeOf(modFile),
                    id = quiltLoader.id,
                    loader = ModLoader.QUILT,
                    name = quiltLoader.metadata.name,
                    description = quiltLoader.metadata.description,
                    version = quiltLoader.version,
                    authors = contributors,
                    icon = icon
                )
            }
        }
    }

    private fun parseContributors(contributors: JsonObject?): List<String> {
        if (contributors == null) return emptyList()

        return contributors.entrySet().map { (name, role) ->
            val roleText = role.asJsonPrimitive?.asString?.takeIf { it.isNotBlank() }
            if (roleText != null) "$name ($roleText)" else name
        }
    }
}