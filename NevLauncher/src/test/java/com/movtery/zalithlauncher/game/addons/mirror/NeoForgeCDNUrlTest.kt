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

package com.movtery.zalithlauncher.game.addons.mirror

import junit.framework.TestCase.assertEquals
import org.junit.Test

class NeoForgeCDNUrlTest {
    private val installerUrl = "https://maven.neoforged.net/releases/net/neoforged/neoforge/21.1.215/neoforge-21.1.215-installer.jar"
    private val cdnInstallerUrl = "https://neoforged.forgecdn.net/releases/net/neoforged/neoforge/21.1.215/neoforge-21.1.215-installer.jar"

    @Test
    fun testMirrorFirstCandidates() {
        assertEquals(
            listOf(
                "https://bmclapi2.bangbang93.com/maven/net/neoforged/neoforge/21.1.215/neoforge-21.1.215-installer.jar",
                installerUrl,
                cdnInstallerUrl
            ),
            installerUrl.mapBMCLMirrorUrls(mainland = true, priority = MirrorPriority.MIRROR_FIRST)
        )
    }

    @Test
    fun testOfficialFirstCandidates() {
        assertEquals(
            listOf(installerUrl, cdnInstallerUrl),
            installerUrl.mapBMCLMirrorUrls(mainland = true, priority = MirrorPriority.OFFICIAL)
        )
    }

    @Test
    fun testMetadataFallbackOutsideMainland() {
        val metadataUrl = "https://maven.neoforged.net/api/maven/versions/releases/net/neoforged/neoforge"
        assertEquals(
            listOf(
                metadataUrl,
                "https://neoforged.forgecdn.net/api/maven/versions/releases/net/neoforged/neoforge"
            ),
            metadataUrl.mapBMCLMirrorUrls(mainland = false, priority = MirrorPriority.OFFICIAL)
        )
    }

    @Test
    fun testUnrelatedUrlOutsideMainland() {
        val url = "https://piston-meta.mojang.com/mc/game/version_manifest_v2.json"
        assertEquals(
            listOf(url),
            url.mapBMCLMirrorUrls(mainland = false, priority = MirrorPriority.OFFICIAL)
        )
    }
}
