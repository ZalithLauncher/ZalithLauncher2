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

class AssetsSourceOrderTest {
    private val assetUrl = "https://resources.download.minecraft.net/ab/abcdef0123456789"
    private val assetMirrorUrl = "https://bmclapi2.bangbang93.com/assets/ab/abcdef0123456789"

    @Test
    fun mirrorFirstStillPrefersOfficialForAssets() {
        assertEquals(
            listOf(assetUrl, assetMirrorUrl),
            assetUrl.mapBMCLMirrorUrls(mainland = true, priority = MirrorPriority.MIRROR_FIRST)
        )
    }

    @Test
    fun mirrorFirstKeepsMirrorFirstForOtherSources() {
        val libraryUrl = "https://libraries.minecraft.net/org/lwjgl/lwjgl/3.3.3/lwjgl-3.3.3.jar"
        assertEquals(
            listOf(
                "https://bmclapi2.bangbang93.com/libraries/org/lwjgl/lwjgl/3.3.3/lwjgl-3.3.3.jar",
                libraryUrl
            ),
            libraryUrl.mapBMCLMirrorUrls(mainland = true, priority = MirrorPriority.MIRROR_FIRST)
        )
    }

    @Test
    fun officialPriorityKeepsSingleCandidate() {
        assertEquals(
            listOf(assetUrl),
            assetUrl.mapBMCLMirrorUrls(mainland = true, priority = MirrorPriority.OFFICIAL)
        )
    }
}
