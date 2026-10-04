/*
 * Zalith Launcher 2
 * Copyright (C) 2026 Mirai Launcher contributors.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.movtery.zalithlauncher.game.optimization

import com.movtery.zalithlauncher.game.addons.modloader.ModLoader
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class JvmGcAutoTunerTest {

    @Test
    fun recommendsExpectedJavaVersionAcrossMinecraftReleases() {
        assertEquals(8, JvmGcAutoTuner.recommendJavaMajorVersion("1.12.2", ModLoader.FORGE))
        assertEquals(8, JvmGcAutoTuner.recommendJavaMajorVersion("1.16.5", ModLoader.FABRIC))
        assertEquals(17, JvmGcAutoTuner.recommendJavaMajorVersion("1.18.2", ModLoader.FABRIC))
        assertEquals(17, JvmGcAutoTuner.recommendJavaMajorVersion("1.20.1", ModLoader.FORGE))
        assertEquals(21, JvmGcAutoTuner.recommendJavaMajorVersion("1.20.5", ModLoader.FABRIC))
        assertEquals(21, JvmGcAutoTuner.recommendJavaMajorVersion("1.21.4", ModLoader.NEOFORGE))
    }

    @Test
    fun sanitizesZgcOnJava8AndInjectsLowPauseG1Gc() {
        val args = mutableListOf(
            "-Xms2048M",
            "-Xmx2048M",
            "-XX:+UseZGC",
            "-XX:+ZGenerational"
        )
        JvmGcAutoTuner.sanitizeAndInjectGcArgs(args, javaMajor = 8, ramAllocationMb = 2048)

        assertFalse(args.contains("-XX:+UseZGC"))
        assertFalse(args.contains("-XX:+ZGenerational"))
        assertTrue(args.contains("-XX:+UseG1GC"))
        assertTrue(args.contains("-XX:MaxGCPauseMillis=50"))
    }

    @Test
    fun sanitizesCmsOnJava17AndInjectsG1Gc() {
        val args = mutableListOf(
            "-XX:+UseConcMarkSweepGC",
            "-XX:+CMSIncrementalMode"
        )
        JvmGcAutoTuner.sanitizeAndInjectGcArgs(args, javaMajor = 17, ramAllocationMb = 3072)

        assertFalse(args.contains("-XX:+UseConcMarkSweepGC"))
        assertFalse(args.contains("-XX:+CMSIncrementalMode"))
        assertTrue(args.contains("-XX:+UseG1GC"))
    }

    @Test
    fun buildsGenerationalZgcOnJava21AndFallsBackOnJava17() {
        val java21Flags = JvmGcAutoTuner.buildJvmFlags(
            preset = GcTuningPreset.GENERATIONAL_ZGC_TURBO,
            javaMajor = 21,
            ramAllocationMb = 4096
        )
        assertTrue(java21Flags.contains("-XX:+UseZGC"))
        assertTrue(java21Flags.contains("-XX:+ZGenerational"))

        val java17Flags = JvmGcAutoTuner.buildJvmFlags(
            preset = GcTuningPreset.GENERATIONAL_ZGC_TURBO,
            javaMajor = 17,
            ramAllocationMb = 4096
        )
        assertFalse(java17Flags.contains("-XX:+ZGenerational"))
        assertTrue(java17Flags.contains("-XX:+UseG1GC"))
    }
}
