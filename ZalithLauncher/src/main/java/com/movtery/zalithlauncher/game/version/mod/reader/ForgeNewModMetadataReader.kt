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

import com.moandjiezana.toml.Toml
import com.movtery.zalithlauncher.game.addons.modloader.ModLoader
import com.movtery.zalithlauncher.game.version.mod.LocalMod
import com.movtery.zalithlauncher.game.version.mod.ModMetadataReader
import com.movtery.zalithlauncher.game.version.mod.meta.ForgeNewModMetadata
import com.movtery.zalithlauncher.utils.GSON
import com.movtery.zalithlauncher.utils.file.UnpackZipException
import com.movtery.zalithlauncher.utils.logging.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.commons.compress.archivers.zip.ZipFile
import org.apache.commons.io.FileUtils
import java.io.File
import java.io.IOException
import java.util.jar.Attributes
import java.util.jar.Manifest
import java.util.zip.ZipFile as JDKZipFile

private const val TAG = "ForgeNewMetadata"

/**
 * [Reference HMCL](https://github.com/HMCL-dev/HMCL/blob/4650287/HMCLCore/src/main/java/org/jackhuang/hmcl/mod/modinfo/ForgeNewModMetadata.java)
 */
object ForgeNewModMetadataReader : ModMetadataReader {
    private const val ACC_FORGE = 0x01
    private const val ACC_NEO_FORGED = 0x02

    override suspend fun fromLocal(modFile: File): LocalMod = withContext(Dispatchers.IO) {
        try {
            JDKZipFile(modFile).use { zip ->
                //尝试 Forge
                runCatching {
                    readFromToml(
                        zip = zip,
                        tomlPath = "META-INF/mods.toml",
                        loaderACC = ACC_FORGE or ACC_NEO_FORGED,
                        defaultLoader = ModLoader.FORGE,
                        modFile = modFile
                    )
                }.onSuccess { return@withContext it }

                //尝试 NeoForge
                runCatching {
                    readFromToml(
                        zip = zip,
                        tomlPath = "META-INF/neoforge.mods.toml",
                        loaderACC = ACC_NEO_FORGED,
                        defaultLoader = ModLoader.NEOFORGE,
                        modFile = modFile
                    )
                }.onSuccess { return@withContext it }

                throw UnpackZipException("File $modFile is not a Forge 1.13+ or NeoForge mod.")
            }
        } catch (e: Exception) {
            if (e !is UnpackZipException) return@withContext readWithApacheZip(modFile)
            else throw e
        }
    }

    private fun readFromToml(
        zip: JDKZipFile,
        tomlPath: String,
        loaderACC: Int,
        defaultLoader: ModLoader,
        modFile: File
    ): LocalMod {
        val tomlEntry = zip.getEntry(tomlPath) ?: throw IOException("TOML file $tomlPath not found")

        zip.getInputStream(tomlEntry).bufferedReader().use { reader ->
            val toml = Toml().read(reader.readText())
            val tomlMap = toml.toMap() as MutableMap<String, Any?>

            fixAuthorsField(tomlMap)

            val json = GSON.toJsonTree(tomlMap)
            val metadata = GSON.fromJson(json, ForgeNewModMetadata::class.java)
                ?: throw IOException("Failed to parse TOML metadata, file = $modFile")

            if (metadata.mods.isEmpty()) {
                throw IOException("Mod $modFile `$tomlPath` is malformed")
            }

            val mod = metadata.mods[0]
            val jarVersion = readVersion(zip, modFile)
            val resolvedVersion = mod.version.replace("\${file.jarVersion}", jarVersion ?: "")
            val loaderType = analyzeLoader(toml, mod.modId, loaderACC, defaultLoader)
            val icon = zip.tryGetIcon(metadata.logoFile)

            return LocalMod(
                modFile = modFile,
                fileSize = FileUtils.sizeOf(modFile),
                id = mod.modId,
                loader = loaderType,
                name = mod.displayName,
                description = mod.description,
                version = resolvedVersion,
                authors = mod.authors ?: emptyList(),
                icon = icon
            )
        }
    }

    private fun readVersion(zip: JDKZipFile, modFile: File): String? {
        val manifestEntry = zip.getEntry("META-INF/MANIFEST.MF") ?: return null

        return try {
            zip.getInputStream(manifestEntry).use { stream ->
                Manifest(stream).mainAttributes.getValue(Attributes.Name.IMPLEMENTATION_VERSION)
            }
        } catch (e: Exception) {
            Logger.warning(TAG, "Failed to parse MANIFEST.MF in file $modFile", e)
            null
        }
    }

    private fun readWithApacheZip(modFile: File): LocalMod {
        val zipFile = ZipFile.builder()
            .setFile(modFile)
            .get()

        zipFile.use { zip ->
            runCatching {
                readFromTomlApache(
                    zip,
                    "META-INF/mods.toml",
                    ACC_FORGE or ACC_NEO_FORGED,
                    ModLoader.FORGE,
                    modFile
                )
            }.onSuccess { return it }

            runCatching {
                readFromTomlApache(
                    zip,
                    "META-INF/neoforge.mods.toml",
                    ACC_NEO_FORGED,
                    ModLoader.NEOFORGE,
                    modFile
                )
            }.onSuccess { return it }

            throw IOException("File $modFile is not a Forge 1.13+ or NeoForge mod.")
        }
    }

    private fun readFromTomlApache(
        zip: ZipFile,
        tomlPath: String,
        loaderACC: Int,
        defaultLoader: ModLoader,
        modFile: File
    ): LocalMod {
        val tomlEntry = zip.getEntry(tomlPath) ?: throw IOException("TOML file $tomlPath not found")

        zip.getInputStream(tomlEntry).bufferedReader().use { reader ->
            val toml = Toml().read(reader.readText())
            val tomlMap = toml.toMap() as MutableMap<String, Any?>

            fixAuthorsField(tomlMap)

            val json = GSON.toJsonTree(tomlMap)
            val metadata = GSON.fromJson(json, ForgeNewModMetadata::class.java)
                ?: throw IOException("Failed to parse TOML metadata, file = $modFile")

            if (metadata.mods.isEmpty()) {
                throw IOException("Mod $modFile `$tomlPath` is malformed")
            }

            val mod = metadata.mods[0]
            val jarVersion = readVersionApache(zip, modFile)
            val resolvedVersion = mod.version.replace("\${file.jarVersion}", jarVersion ?: "")
            val loaderType = analyzeLoader(toml, mod.modId, loaderACC, defaultLoader)
            val icon = zip.tryGetIcon(metadata.logoFile)

            return LocalMod(
                modFile = modFile,
                fileSize = FileUtils.sizeOf(modFile),
                id = mod.modId,
                loader = loaderType,
                name = mod.displayName,
                description = mod.description,
                version = resolvedVersion,
                authors = mod.authors ?: emptyList(),
                icon = icon
            )
        }
    }

    private fun readVersionApache(zip: ZipFile, modFile: File): String? {
        val manifestEntry = zip.getEntry("META-INF/MANIFEST.MF") ?: return null

        return try {
            zip.getInputStream(manifestEntry).use { stream ->
                Manifest(stream).mainAttributes.getValue(Attributes.Name.IMPLEMENTATION_VERSION)
            }
        } catch (e: Exception) {
            Logger.warning(TAG, "Failed to parse MANIFEST.MF in file $modFile", e)
            null
        }
    }

    /**
     * 修复作者列表
     * 有的模组作者名称只是单个字符串；
     * 有的模组则是作者名称列表
     */
    @Suppress("UNCHECKED_CAST")
    private fun fixAuthorsField(map: MutableMap<String, Any?>) {
        val mods = map["mods"] as? List<MutableMap<String, Any?>> ?: return
        for (mod in mods) {
            val authors = mod["authors"]
            when (authors) {
                is String -> mod["authors"] = parseAuthors(authors)
                is List<*> -> {}
                else -> mod["authors"] = emptyList<String>()
            }
        }
    }

    private fun parseAuthors(authors: String?): List<String> {
        return authors?.split(',', ';', '&')?.map {
            it.trim()
        }?.filter {
            it.isNotBlank()
        } ?: emptyList()
    }

    /**
     * 分析模组加载器类型
     */
    private fun analyzeLoader(
        toml: Toml,
        modID: String,
        loaderACC: Int,
        defaultLoader: ModLoader
    ): ModLoader {
        val depsArray = runCatching {
            toml.getTables("dependencies")
        }.getOrNull()

        val dependencies: List<Map<String, Any>>? = when (depsArray) {
            null -> { //尝试 dependencies.{modID}
                val depsTable = toml.getTable("dependencies")
                val modDepsArray = depsTable?.getTables(modID)
                modDepsArray?.map { it.toMap() as Map<String, Any> }
            }

            else -> {
                depsArray.map { it.toMap() as Map<String, Any> }
            }
        }

        fun checkLoaderACC(current: Int, target: Int, res: ModLoader): ModLoader {
            return if (target and current != 0) res else throw IOException("Mismatched loader")
        }

        dependencies?.forEach { dependency ->
            when (dependency["modId"] as? String) {
                "forge" -> return checkLoaderACC(loaderACC, ACC_FORGE, ModLoader.FORGE)
                "neoforge" -> return checkLoaderACC(loaderACC, ACC_NEO_FORGED, ModLoader.NEOFORGE)
            }
        }

        return defaultLoader
    }
}