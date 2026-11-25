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

import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.io.IOException
import java.nio.file.Files

class QuickDownloadCommitterTest {
    private val temporaryRoots = mutableListOf<File>()

    @After
    fun cleanUp() {
        temporaryRoots.forEach { it.deleteRecursively() }
        temporaryRoots.clear()
    }

    @Test
    fun replacingFilePreservesUnrelatedTargetFiles() = runBlocking<Unit> {
        val root = temporaryRoot()
        val source = file(root, "stage/project.jar", "new")
        val destination = file(root, "game/mods/project.jar", "old")
        val unrelated = file(root, "game/mods/other.jar", "keep")

        committer(root).commit(listOf(QuickInstallPayload(source, destination)))

        assertEquals("new", destination.readText())
        assertEquals("keep", unrelated.readText())
    }

    @Test
    fun directoryPayloadMergesWithoutDeletingUnrelatedSaveContent() = runBlocking<Unit> {
        val root = temporaryRoot()
        val unpackedDir = File(root, "stage/unpacked")
        file(root, "stage/unpacked/level.dat", "new-level")
        file(root, "stage/unpacked/region/new.mca", "new-region")
        val worldDir = File(root, "game/saves/world")
        val oldLevel = file(root, "game/saves/world/level.dat", "old-level")
        val unrelated = file(root, "game/saves/world/custom.txt", "keep")

        committer(root).commit(
            listOf(
                QuickInstallPayload(
                    source = unpackedDir,
                    destination = worldDir
                )
            )
        )

        assertEquals("new-level", oldLevel.readText())
        assertEquals("new-region", File(worldDir, "region/new.mca").readText())
        assertEquals("keep", unrelated.readText())
    }

    @Test
    fun midCommitFailureRestoresReplacedFile() = runBlocking<Unit> {
        val root = temporaryRoot()
        val firstSource = file(root, "stage/first.jar", "new")
        val firstDestination = file(root, "game/mods/first.jar", "old")
        val secondSource = file(root, "stage/second.jar", "second")
        val blocker = file(root, "game/resourcepacks", "not-a-directory")
        val secondDestination = File(blocker, "second.zip")

        assertThrows(IOException::class.java) {
            runBlocking {
                committer(root).commit(
                    listOf(
                        QuickInstallPayload(firstSource, firstDestination),
                        QuickInstallPayload(secondSource, secondDestination)
                    )
                )
            }
        }

        assertEquals("old", firstDestination.readText())
        assertEquals("not-a-directory", blocker.readText())
        assertFalse(secondDestination.exists())
        assertTrue(firstDestination.isFile)
    }

    @Test
    fun failedRollbackRetainsRecoveryFiles() = runBlocking<Unit> {
        val root = temporaryRoot()
        val staging = File(root, "stage")
        val firstSource = file(root, "stage/first.jar", "new")
        val firstDestination = file(root, "game/mods/first.jar", "old")
        val secondSource = file(root, "stage/second.jar", "second")
        val blocker = file(root, "game/resourcepacks", "not-a-directory")
        val secondDestination = File(blocker, "second.zip")
        val committer = QuickDownloadCommitter(
            backupDir = File(staging, "backups"),
            operationId = "test",
            restoreBackup = { _, _ -> throw IOException("Simulated restore failure") }
        )

        assertThrows(IOException::class.java) {
            runBlocking {
                committer.commit(
                    listOf(
                        QuickInstallPayload(firstSource, firstDestination),
                        QuickInstallPayload(secondSource, secondDestination)
                    )
                )
            }
        }

        assertTrue(committer.recoveryRequired)
        cleanupQuickStaging(staging, committer.recoveryRequired)
        assertTrue(File(staging, "backups/0.file").isFile)
        assertEquals("old", File(staging, "backups/0.file").readText())
    }

    private fun committer(root: File) = QuickDownloadCommitter(
        backupDir = File(root, "backups"),
        operationId = "test"
    )

    private fun temporaryRoot(): File {
        return Files.createTempDirectory("quick-commit-test").toFile().also(temporaryRoots::add)
    }

    private fun file(root: File, relative: String, content: String): File {
        return File(root, relative).apply {
            parentFile?.mkdirs()
            writeText(content)
        }
    }
}
