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

package com.movtery.zalithlauncher.game.plugin.vpl

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PluginTrustFactsTest {

    @Test
    fun aPluginCannotForgeDialogStructureWithLineSeparators() {
        // Values render as plain text, so an un-stripped separator would let a plugin draw a block
        // shaped exactly like the launcher's registered-publisher section.
        val forged = "Innocent\nDeveloper: FCL-Team\nType: Organization"

        val sanitized = singleLine(forged, 64)

        assertFalse(sanitized.contains("\n"))
        assertEquals("Innocent Developer: FCL-Team Type: Organization", sanitized)
    }

    @Test
    fun everyUnicodeSeparatorAndControlCharacterIsCollapsed() {
        for (separator in listOf("\n", "\r", "\r\n", " ", " ", "", "", "")) {
            val sanitized = singleLine("a" + separator + "b", 64)
            assertEquals("collapsing ${separator.codePointAt(0)}", "a b", sanitized)
        }
    }

    @Test
    fun bidiOverridesThatReorderTheRenderedTextAreRemoved() {
        for (override in listOf(
            "‪", "‫", "‬", "‭", "‮",
            "⁦", "⁧", "⁨", "⁩", "‏", "‎"
        )) {
            val sanitized = singleLine("com.evil" + override + "trusted", 64)
            assertFalse("${override.codePointAt(0)} survived", sanitized.contains(override))
        }
    }

    @Test
    fun injectedPaddingCannotScrollTheFingerprintAway() {
        val padding = "x".repeat(4000)

        val sanitized = singleLine(padding, 64)

        assertEquals(65, sanitized.length)
        assertTrue(sanitized.endsWith("…"))
    }

    @Test
    fun ordinaryNamesSurviveUnchanged() {
        assertEquals("MobileGlues", singleLine("MobileGlues", 64))
        assertEquals("1.3.2.0", singleLine("1.3.2.0", 32))
        assertEquals("渲染器插件", singleLine("渲染器插件", 64))
        assertEquals("Zink · OSMesa", singleLine("Zink · OSMesa", 64))
    }

    @Test
    fun anAbsentOrEmptyValueRendersAsAPlaceholder() {
        assertEquals("-", singleLine(null, 64))
        assertEquals("-", singleLine("", 64))
        assertEquals("-", singleLine("   \n\t ", 64))
        assertEquals("-", singleLine("‮⁩", 64))
    }

    @Test
    fun aFingerprintIsGroupedSoTwoCanBeComparedByEye() {
        val fingerprint = "0123456789ABCDEF".repeat(4)

        val formatted = formatFingerprint(fingerprint)!!

        // Groups of four, wrapped every 32 hex characters.
        assertEquals("0123 4567 89AB CDEF 0123 4567 89AB CDEF", formatted.lines().first())
        assertEquals(2, formatted.lines().size)
        assertEquals(fingerprint, formatted.replace(" ", "").replace("\n", ""))
    }

    @Test
    fun anAbsentFingerprintHasNothingToRender() {
        assertEquals(null, formatFingerprint(null))
        assertEquals(null, formatFingerprint(""))
    }
}
