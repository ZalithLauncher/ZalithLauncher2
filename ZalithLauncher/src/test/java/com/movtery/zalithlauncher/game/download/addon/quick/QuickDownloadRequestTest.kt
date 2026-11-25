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

package com.movtery.zalithlauncher.game.download.addon.quick

import com.movtery.zalithlauncher.game.download.addon.favorites.FavoriteEntry
import com.movtery.zalithlauncher.game.download.addon.favorites.FavoriteProject
import com.movtery.zalithlauncher.game.download.addon.platform.Platform
import com.movtery.zalithlauncher.game.download.addon.platform.PlatformClasses
import com.movtery.zalithlauncher.game.download.addon.platform.curseforge.models.CurseForgeModLoader
import com.movtery.zalithlauncher.game.download.addon.recognizedPlatformLoaders
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class QuickDownloadRequestTest {
    @Test
    fun modrinthChecksGameAndLoaderTogether() {
        val request = request(
            QuickCompatibilitySummary.Modrinth(
                gameVersions = setOf("1.20.1"),
                loaders = setOf("Fabric")
            )
        )

        assertEquals(QuickCompatibility.COMPATIBLE, request.compatibilityWith("1.20.1", listOf("Fabric")))
        assertEquals(QuickCompatibility.INCOMPATIBLE, request.compatibilityWith("1.20.1", listOf("Forge")))
        assertEquals(QuickCompatibility.INCOMPATIBLE, request.compatibilityWith("1.21", listOf("Fabric")))
    }

    @Test
    fun missingSummaryOrLocalInformationIsUnknown() {
        val unknownGame = request(
            QuickCompatibilitySummary.Modrinth(gameVersions = null, loaders = setOf("Fabric"))
        )
        val unknownLoaders = request(
            QuickCompatibilitySummary.Modrinth(gameVersions = setOf("1.20.1"), loaders = null)
        )

        assertEquals(QuickCompatibility.UNKNOWN, unknownGame.compatibilityWith("1.20.1", listOf("Fabric")))
        assertEquals(QuickCompatibility.UNKNOWN, unknownLoaders.compatibilityWith("1.20.1", listOf("Fabric")))
        assertEquals(QuickCompatibility.UNKNOWN, unknownLoaders.compatibilityWith(null, listOf("Fabric")))
        assertEquals(QuickCompatibility.UNKNOWN, unknownLoaders.compatibilityWith("1.20.1", null))
    }

    @Test
    fun curseForgeDoesNotPairGameAndLoaderAcrossDifferentFiles() {
        val request = request(
            QuickCompatibilitySummary.CurseForge(
                files = listOf(
                    QuickSummaryFile("1.20.1", CurseForgeModLoader.FORGE),
                    QuickSummaryFile("1.21", CurseForgeModLoader.FABRIC)
                )
            )
        )

        assertEquals(QuickCompatibility.COMPATIBLE, request.compatibilityWith("1.20.1", listOf("Forge")))
        assertEquals(QuickCompatibility.INCOMPATIBLE, request.compatibilityWith("1.20.1", listOf("Fabric")))
        assertEquals(QuickCompatibility.INCOMPATIBLE, request.compatibilityWith("1.19.2", listOf("Forge")))
    }

    @Test
    fun curseForgeAnyMissingAndMixedIndexesAreDistinct() {
        val anyLoader = request(
            QuickCompatibilitySummary.CurseForge(
                listOf(QuickSummaryFile("1.20.1", CurseForgeModLoader.ANY))
            )
        )
        val missingLoader = request(
            QuickCompatibilitySummary.CurseForge(listOf(QuickSummaryFile("1.20.1", null)))
        )
        val mixed = request(
            QuickCompatibilitySummary.CurseForge(
                listOf(
                    QuickSummaryFile("1.20.1", CurseForgeModLoader.FORGE),
                    QuickSummaryFile("1.20.1", null)
                )
            )
        )
        val empty = request(QuickCompatibilitySummary.CurseForge(emptyList()))

        assertEquals(QuickCompatibility.COMPATIBLE, anyLoader.compatibilityWith("1.20.1", listOf("Fabric")))
        assertEquals(QuickCompatibility.INCOMPATIBLE, anyLoader.compatibilityWith("1.20.1", emptyList()))
        assertEquals(QuickCompatibility.UNKNOWN, missingLoader.compatibilityWith("1.20.1", listOf("Fabric")))
        assertEquals(QuickCompatibility.COMPATIBLE, mixed.compatibilityWith("1.20.1", listOf("Forge")))
        assertEquals(QuickCompatibility.UNKNOWN, mixed.compatibilityWith("1.20.1", listOf("Fabric")))
        assertEquals(QuickCompatibility.UNKNOWN, empty.compatibilityWith("1.20.1", listOf("Fabric")))
    }

    @Test
    fun emptyRecognizedLocalLoadersAreIncompatibleForMods() {
        val modrinth = request(
            QuickCompatibilitySummary.Modrinth(
                gameVersions = setOf("1.20.1"),
                loaders = setOf("Fabric")
            )
        )
        val curseForge = request(
            QuickCompatibilitySummary.CurseForge(
                files = listOf(QuickSummaryFile("1.20.1", CurseForgeModLoader.FORGE))
            )
        )
        val noRemoteLoader = request(
            QuickCompatibilitySummary.Modrinth(
                gameVersions = setOf("1.20.1"),
                loaders = emptySet()
            )
        )

        assertEquals(QuickCompatibility.INCOMPATIBLE, modrinth.compatibilityWith("1.20.1", emptyList()))
        assertEquals(QuickCompatibility.INCOMPATIBLE, curseForge.compatibilityWith("1.20.1", emptyList()))
        assertEquals(QuickCompatibility.INCOMPATIBLE, noRemoteLoader.compatibilityWith("1.20.1", listOf("Fabric")))
        assertEquals(
            emptyList<String>(),
            recognizedPlatformLoaders(Platform.CURSEFORGE, listOf("", "Any"))
        )
    }

    @Test
    fun nonModsDoNotUseModLoaderSummary() {
        val request = request(
            summary = QuickCompatibilitySummary.Modrinth(
                gameVersions = setOf("1.20.1"),
                loaders = setOf("Fabric")
            ),
            classes = PlatformClasses.RESOURCE_PACK
        )

        assertEquals(QuickCompatibility.COMPATIBLE, request.compatibilityWith("1.20.1", listOf("Forge")))
    }

    @Test
    fun unrecognizedModrinthLoadersMakeCompatibilityUnknown() {
        assertNull(modrinthLoaders(arrayOf("java-agent")))
        assertNull(modrinthLoaders(arrayOf("fabric", "java-agent")))
        assertEquals(setOf("Fabric"), modrinthLoaders(arrayOf("fabric")))
        assertEquals(
            setOf("Fabric"),
            modrinthLoaders(arrayOf("fabric", "adventure"), PlatformClasses.MOD)
        )
        assertNull(modrinthLoaders(arrayOf("fabric", "new-loader"), PlatformClasses.MOD))
    }

    @Test
    fun favoriteWithoutRemoteDataIsUnknownButStillInstallable() {
        val request = favorite(PlatformClasses.MOD).toQuickDownloadRequest()

        assertEquals("Favorite", request?.title)
        assertEquals(QuickCompatibility.UNKNOWN, request?.compatibilityWith("1.20.1", listOf("Fabric")))
    }

    @Test
    fun favoriteModPackAndInvalidEntryAreExcluded() {
        assertNull(favorite(PlatformClasses.MOD_PACK).toQuickDownloadRequest())
        assertNull(favorite(PlatformClasses.MOD, invalid = true).toQuickDownloadRequest())
    }

    private fun favorite(
        classes: PlatformClasses,
        invalid: Boolean = false
    ) = FavoriteEntry(
        platform = Platform.MODRINTH,
        project = FavoriteProject(
            projectId = "favorite",
            title = "Favorite",
            classes = classes,
            followTime = 0L
        ),
        invalid = invalid
    )

    private fun request(
        summary: QuickCompatibilitySummary,
        classes: PlatformClasses = PlatformClasses.MOD
    ) = QuickDownloadRequest(
        platform = Platform.MODRINTH,
        projectId = "project",
        title = "Project",
        classes = classes,
        summary = summary
    )
}
