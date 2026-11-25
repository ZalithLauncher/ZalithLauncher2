/*
 * Zalith Launcher 2
 * Copyright (C) 2025-2026 MovTery <movtery228@qq.com> and contributors
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

package com.movtery.zalithlauncher.game.download.addon

import com.movtery.zalithlauncher.game.download.addon.platform.PlatformClasses
import com.movtery.zalithlauncher.game.download.addon.platform.PlatformDisplayLabel
import com.movtery.zalithlauncher.game.download.addon.platform.PlatformReleaseType
import com.movtery.zalithlauncher.game.download.addon.platform.PlatformVersion
import com.movtery.zalithlauncher.game.download.addon.platform.curseforge.models.CurseForgeFile
import com.movtery.zalithlauncher.game.download.addon.platform.curseforge.models.CurseForgeModLoader
import com.movtery.zalithlauncher.game.download.addon.platform.curseforge.models.hasUsablePrimaryMetadata
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.JsonArray
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DownloadCompatibilityTest {
    @Test
    fun curseForgeAnyMatchesRecognizedLocalLoaders() {
        val rawFile = initializedFile("1.20.1", "Any")
        assertFalse(rawFile.platformLoaders().contains(CurseForgeModLoader.ANY))
        assertTrue(rawFile.platformLoadersStrict().contains(CurseForgeModLoader.ANY))
        val file = withoutVersionCatalog(rawFile)

        assertTrue(file.isCompatibleWith("1.20.1", listOf("Fabric"), PlatformClasses.MOD, true))
        assertTrue(file.isCompatibleWith("1.20.1", listOf("Forge"), PlatformClasses.MOD, true))
        assertTrue(file.isCompatibleWith("1.20.1", listOf("Fabric"), PlatformClasses.MOD, false))
    }

    @Test
    fun curseForgeAnyStillRequiresGameVersionAndRecognizedLocalLoader() {
        val file = withoutVersionCatalog(initializedFile("1.20.1", "Any"))

        assertFalse(file.isCompatibleWith("1.20.1", emptyList(), PlatformClasses.MOD, true))
        assertFalse(file.isCompatibleWith("1.20.1", listOf("OptiFine"), PlatformClasses.MOD, true))
        assertFalse(file.isCompatibleWith("1.21", listOf("Fabric"), PlatformClasses.MOD, true))
    }

    @Test
    fun curseForgeSpecificLoaderDoesNotBecomeWildcard() {
        val file = withoutVersionCatalog(initializedFile("1.20.1", "Forge"))

        assertTrue(file.isCompatibleWith("1.20.1", listOf("Forge"), PlatformClasses.MOD, true))
        assertFalse(file.isCompatibleWith("1.20.1", listOf("Fabric"), PlatformClasses.MOD, true))
        assertFalse(file.isCompatibleWith("1.20.1", emptyList(), PlatformClasses.MOD, true))
    }

    @Test
    fun primaryMetadataRejectsMissingOrBlankFileNameEvenWithDownloadUrl() {
        assertFalse(curseForgeFile("1.20.1", fileName = null).hasUsablePrimaryMetadata())
        assertFalse(curseForgeFile("1.20.1", fileName = " ").hasUsablePrimaryMetadata())
        assertTrue(curseForgeFile("1.20.1").hasUsablePrimaryMetadata())
    }

    private fun initializedFile(vararg gameVersions: String): CurseForgeFile =
        curseForgeFile(*gameVersions).also { file ->
            runBlocking { assertTrue(file.initFileStrict("10")) }
        }

    /** 避免 JVM 测试依赖 Android 资源中的 Minecraft 版本目录，只替换游戏版本读取。 */
    private fun withoutVersionCatalog(file: CurseForgeFile): PlatformVersion =
        object : PlatformVersion by file {
            override fun platformGameVersion(): Array<String> = arrayOf("1.20.1")
            override fun platformLoaders(): List<PlatformDisplayLabel> = file.platformLoaders()
            override fun platformLoadersStrict(): List<PlatformDisplayLabel> = file.platformLoadersStrict()
        }

    private fun curseForgeFile(
        vararg gameVersions: String,
        fileName: String? = "test.jar",
        downloadUrl: String? = "https://example.invalid/test.jar"
    ) = CurseForgeFile(
        id = 1,
        gameId = 432,
        modId = 10,
        isAvailable = true,
        displayName = "Test file",
        fileName = fileName,
        releaseType = PlatformReleaseType.RELEASE,
        fileStatus = 4,
        hashes = emptyArray(),
        fileDate = "2025-01-01T00:00:00Z",
        fileLength = 1L,
        downloadCount = 0L,
        downloadUrl = downloadUrl,
        gameVersions = arrayOf(*gameVersions),
        sortableGameVersions = JsonArray(emptyList()),
        dependencies = emptyArray(),
        fileFingerprint = 1L
    )
}
