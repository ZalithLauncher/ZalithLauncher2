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

package com.movtery.zalithlauncher.crashlogs.analysis

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CrashAnalyzerTest {
    @Test
    fun shaderFailureIsPrimaryAndOfflineAuthIsOnlyInfo() {
        val log = """
            [21:26:21] [Download-2/ERROR]: Failed to fetch user properties
            com.mojang.authlib.exceptions.InvalidCredentialsException: Status: 401
            [21:26:26] [Render thread/ERROR]: Couldn't compile fragment shader for pipeline (minecraft:core/terrain): 0:209: S0015
            java.lang.RuntimeException: Failed to load required shader programs:
        """.trimIndent()
        val primary = CrashAnalyzer.primary(log)
        assertNotNull(primary)
        assertEquals("shader_compile", primary!!.id)
        assertTrue(CrashAnalyzer.analyze(log).any { it.id == "offline_auth_normal" && it.severity == DiagnosisSeverity.INFO })
    }

    @Test
    fun outOfMemoryHasHigherPriorityThanNativeCrash() {
        val log = "SIGSEGV somewhere\njava.lang.OutOfMemoryError: Java heap space"
        assertEquals("out_of_memory", CrashAnalyzer.primary(log)!!.id)
    }

    @Test
    fun cleanLogHasNoCause() {
        assertNull(CrashAnalyzer.primary("[INFO]: Setting user: Steve\n[INFO]: Stopping!"))
    }

    @Test
    fun sanitizerHidesLaunchArgsTokensEmailsAndIps() {
        val log = "▷ --uuid\n▷ 3a41224679e039fe949f2b7c789aa6fb\n▷ --accessToken\n▷ eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ4In0.abc123\n" +
            "contact me@example.com from 192.168.1.20 and 127.0.0.1\nbearer ABCDEFGHIJKLMNOP1234\nlwjgl 3.4.1"
        val out = LogSanitizer.sanitize(log)
        assertFalse(out.contains("3a41224679e039fe949f2b7c789aa6fb"))
        assertFalse(out.contains("eyJhbGci"))
        assertFalse(out.contains("me@example.com"))
        assertFalse(out.contains("192.168.1.20"))
        assertFalse(out.contains("ABCDEFGHIJKLMNOP1234"))
        assertTrue(out.contains("127.0.0.1"))
        assertTrue(out.contains("lwjgl 3.4.1"))
    }
}
