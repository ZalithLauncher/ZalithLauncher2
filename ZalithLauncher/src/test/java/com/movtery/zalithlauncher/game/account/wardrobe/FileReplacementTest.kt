/*
 * Zalith Launcher 2
 * Copyright (C) 2026 Mirai Launcher contributors.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.movtery.zalithlauncher.game.account.wardrobe

import java.io.IOException
import java.nio.file.Files
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FileReplacementTest {
    @Test
    fun successfulReplacementMovesStagedFileOverTarget() {
        val directory = Files.createTempDirectory("mirai-file-replacement").toFile()
        try {
            val target = directory.resolve("cape.png").apply { writeText("old cape") }
            val staged = directory.resolve("cape.staged.png").apply { writeText("new cape") }

            replaceFileKeepingOriginal(staged, target)

            assertEquals("new cape", target.readText())
            assertFalse(staged.exists())
            assertEquals(listOf(target.name), directory.listFiles()?.map { it.name })
        } finally {
            directory.deleteRecursively()
        }
    }

    @Test
    fun failedReplacementRestoresExistingTarget() {
        val directory = Files.createTempDirectory("mirai-file-replacement").toFile()
        try {
            val target = directory.resolve("cape.png").apply { writeText("existing cape") }
            val missingStagedFile = directory.resolve("missing.staged.png")

            try {
                replaceFileKeepingOriginal(missingStagedFile, target)
                throw AssertionError("Expected the staged-file move to fail")
            } catch (_: IOException) {
                // A missing staged file exercises the rollback path after the original is backed up.
            }

            assertTrue(target.exists())
            assertEquals("existing cape", target.readText())
            assertEquals(listOf(target.name), directory.listFiles()?.map { it.name })
        } finally {
            directory.deleteRecursively()
        }
    }
}
