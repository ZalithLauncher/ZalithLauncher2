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
package com.movtery.zalithlauncher.game.launch

import com.movtery.zalithlauncher.game.multirt.Runtime
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class Base64IntrinsicsTest {

    private fun runtime(versionString: String?, arch: String?, javaVersion: Int) = Runtime(
        name = "test",
        versionString = versionString,
        arch = arch,
        javaVersion = javaVersion,
        isProvidedByLauncher = true,
        isJDK8 = javaVersion == 8
    )

    @Test
    fun `bundled runtimes on x86_64 match HMCL's affected ranges`() {
        assertTrue(runtime("17.0.10", "x86_64", 17).shouldDisableBase64Intrinsics())
        assertTrue(runtime("21.0.1", "x86_64", 21).shouldDisableBase64Intrinsics())
        assertTrue(runtime("25.0.5", "x86_64", 25).shouldDisableBase64Intrinsics())
    }

    @Test
    fun `jdk 8 never gets the flag`() {
        assertFalse(runtime("1.8.0_442", "amd64", 8).shouldDisableBase64Intrinsics())
    }

    @Test
    fun `java 17 boundaries`() {
        assertFalse(runtime("17.0.5", "x86_64", 17).shouldDisableBase64Intrinsics())
        assertTrue(runtime("17.0.6", "x86_64", 17).shouldDisableBase64Intrinsics())
        assertTrue(runtime("17.0.21", "x86_64", 17).shouldDisableBase64Intrinsics())

        assertFalse(runtime("17.0.21.1", "x86_64", 17).shouldDisableBase64Intrinsics())
        assertFalse(runtime("17.0.22", "x86_64", 17).shouldDisableBase64Intrinsics())
    }

    @Test
    fun `java 21 25 27 boundaries`() {
        assertTrue(runtime("21.0.13", "x86_64", 21).shouldDisableBase64Intrinsics())
        assertFalse(runtime("21.0.13.1", "x86_64", 21).shouldDisableBase64Intrinsics())

        assertTrue(runtime("25.0.5", "x86_64", 25).shouldDisableBase64Intrinsics())
        assertFalse(runtime("25.0.5.1", "x86_64", 25).shouldDisableBase64Intrinsics())

        assertTrue(runtime("27.0.1", "x86_64", 27).shouldDisableBase64Intrinsics())
        assertFalse(runtime("27.0.1.1", "x86_64", 27).shouldDisableBase64Intrinsics())
    }

    @Test
    fun `unpatched version lines are fully affected`() {
        for (major in listOf(18, 19, 20, 22, 23, 24, 26)) {
            assertTrue(
                runtime("$major.0.1", "x86_64", major).shouldDisableBase64Intrinsics()
            )
        }
    }

    @Test
    fun `non x86_64 architectures are excluded`() {
        assertFalse(runtime("21.0.1", "aarch64", 21).shouldDisableBase64Intrinsics())
        assertFalse(runtime("17.0.10", "arm", 17).shouldDisableBase64Intrinsics())

        assertFalse(runtime("17.0.10", "x86", 17).shouldDisableBase64Intrinsics())
    }

    @Test
    fun `versions without the intrinsic are unaffected`() {
        assertFalse(runtime("11.0.28", "x86_64", 11).shouldDisableBase64Intrinsics())
    }
}