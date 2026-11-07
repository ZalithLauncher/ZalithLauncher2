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

package com.movtery.zalithlauncher.utils.logging

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.util.Calendar

class GameLogArchiverTest {
    @get:Rule
    val tmp = TemporaryFolder()

    private fun date(y: Int, mo: Int, d: Int, h: Int, mi: Int) =
        Calendar.getInstance().apply { clear(); set(y, mo - 1, d, h, mi, 0) }.time

    @Test
    fun fileNameFollowsNusaLogsFormat() {
        val t = date(2026, 10, 6, 19, 41)
        assertEquals("NusaLogs-2026-10-06-19.41.MobileGlues.log", GameLogArchiver.fileName(t, "MobileGlues"))
        assertEquals("NusaLogs-2026-10-06-19.41.KopperZink.log", GameLogArchiver.fileName(t, "Kopper Zink"))
        assertEquals("NusaLogs-2026-10-06-19.41.GL4ES-2.log", GameLogArchiver.fileName(t, "GL4ES", 2))
    }

    @Test
    fun rendererNameIsMadeFileSafe() {
        assertEquals("UnknownRenderer", GameLogArchiver.sanitizeRenderer("  /\\:  "))
        assertEquals("AB", GameLogArchiver.sanitizeRenderer("A/B:"))
    }

    @Test
    fun archiveCopiesLogWithHeaderAndDoesNotOverwriteSameMinute() {
        val src = tmp.newFile("latest_game.log").apply { writeText("hello log\n") }
        val dir = File(tmp.root, "game")
        val t = date(2026, 10, 6, 19, 41)
        val a = GameLogArchiver.archive(src, dir, "MobileGlues", GameLogArchiver.buildHeader(mapOf("Minecraft" to "1.21.1"), t), t)
        val b = GameLogArchiver.archive(src, dir, "MobileGlues", "", t)
        assertNotNull(a); assertNotNull(b)
        assertEquals("NusaLogs-2026-10-06-19.41.MobileGlues.log", a!!.name)
        assertEquals("NusaLogs-2026-10-06-19.41.MobileGlues-2.log", b!!.name)
        assertTrue(a.readText().contains("Minecraft: 1.21.1"))
        assertTrue(a.readText().endsWith("hello log\n"))
        assertEquals("hello log\n", src.readText()) // original stays
    }

    @Test
    fun emptyOrMissingSourceIsNotArchived() {
        val empty = tmp.newFile("empty.log")
        assertNull(GameLogArchiver.archive(empty, File(tmp.root, "g"), "X", ""))
        assertNull(GameLogArchiver.archive(File(tmp.root, "nope.log"), File(tmp.root, "g"), "X", ""))
    }

    @Test
    fun cleanupRemovesOnlyOldArchives() {
        val dir = tmp.newFolder("game")
        val now = date(2026, 11, 6, 12, 0).time
        val day = 86400000L
        val old = File(dir, "NusaLogs-2026-09-01-10.00.GL4ES.log").apply { writeText("x"); setLastModified(now - 31 * day) }
        val fresh = File(dir, "NusaLogs-2026-10-20-10.00.GL4ES.log").apply { writeText("x"); setLastModified(now - 29 * day) }
        val other = File(dir, "notes.txt").apply { writeText("x"); setLastModified(now - 90 * day) }
        assertEquals(1, GameLogArchiver.cleanup(dir, 30, now))
        assertFalse(old.exists()); assertTrue(fresh.exists()); assertTrue(other.exists())
    }

    @Test
    fun parseNameReadsTimeAndRenderer() {
        val info = GameLogArchiver.parseName("NusaLogs-2026-10-06-19.41.MobileGlues.log")
        assertNotNull(info)
        assertEquals(date(2026, 10, 6, 19, 41).time, info!!.timeMillis)
        assertEquals("MobileGlues", info.renderer)
        // duplicate counter is not part of the renderer name
        assertEquals("GL4ES", GameLogArchiver.parseName("NusaLogs-2026-10-06-19.41.GL4ES-2.log")!!.renderer)
        assertNull(GameLogArchiver.parseName("log_2026-11-06T19-38-49_main.log"))
        assertNull(GameLogArchiver.parseName("NusaLogs-garbage.log"))
    }

    @Test
    fun parseNameRoundTripsGeneratedNames() {
        val t = date(2026, 1, 2, 3, 4)
        val info = GameLogArchiver.parseName(GameLogArchiver.fileName(t, "Kopper Zink"))
        assertEquals(t.time, info!!.timeMillis)
        assertEquals("KopperZink", info.renderer)
    }
}
