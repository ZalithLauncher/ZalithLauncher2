/*
 * Zalith Launcher 2
 * Copyright (C) 2026 Aerix Launcher contributors.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.movtery.zalithlauncher.game.addons.mirror

import com.movtery.zalithlauncher.setting.enums.MirrorSourceType
import org.junit.Assert.assertEquals
import org.junit.Test

class MirrorPreferencesTest {
    @Test
    fun autoUsesOfficialFirstOutsideMainlandWithMirrorFallback() {
        assertEquals(
            MirrorPriority.OFFICIAL,
            resolveMirrorPriority(MirrorSourceType.AUTO, mainland = false)
        )
        assertEquals(
            listOf("official", "mirror"),
            orderSourceCandidates(
                official = "official",
                mirror = "mirror",
                preference = MirrorSourceType.AUTO,
                mainland = false
            )
        )
    }

    @Test
    fun autoUsesMirrorFirstInMainlandAndRetainsOfficialFallback() {
        assertEquals(
            MirrorPriority.MIRROR_FIRST,
            resolveMirrorPriority(MirrorSourceType.AUTO, mainland = true)
        )
        assertEquals(
            listOf("mirror", "official"),
            orderSourceCandidates(
                official = "official",
                mirror = "mirror",
                preference = MirrorSourceType.AUTO,
                mainland = true
            )
        )
    }

    @Test
    fun explicitMirrorIsMirrorFirstRegardlessOfRegion() {
        for (mainland in listOf(false, true)) {
            assertEquals(
                listOf("mirror", "official"),
                orderSourceCandidates(
                    official = "official",
                    mirror = "mirror",
                    preference = MirrorSourceType.MIRROR,
                    mainland = mainland
                )
            )
        }
    }

    @Test
    fun explicitOfficialRemainsOfficialOnlyInMainland() {
        assertEquals(
            MirrorPriority.OFFICIAL,
            resolveMirrorPriority(MirrorSourceType.OFFICIAL, mainland = true)
        )
        assertEquals(
            listOf("official"),
            orderSourceCandidates(
                official = "official",
                mirror = "mirror",
                preference = MirrorSourceType.OFFICIAL,
                mainland = true
            )
        )
    }
}
