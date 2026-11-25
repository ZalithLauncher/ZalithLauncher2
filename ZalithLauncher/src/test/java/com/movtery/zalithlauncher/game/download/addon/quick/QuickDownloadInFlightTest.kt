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

import com.movtery.zalithlauncher.game.download.addon.platform.Platform
import com.movtery.zalithlauncher.game.download.addon.platform.PlatformClasses
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class QuickDownloadInFlightTest {
    @Test
    fun concurrentAcquiresOnlyRetainOneCopy() = runBlocking {
        val registry = QuickInFlightRegistry()
        val item = item(targets = listOf(target("a")))

        val leases = coroutineScope {
            (1..20).map {
                async(Dispatchers.Default) { registry.acquire(listOf(item)) }
            }.awaitAll()
        }

        assertEquals(1, leases.sumOf { lease -> lease.items.sumOf { it.targets.size } })
        assertEquals(19, leases.sumOf { it.skippedTargets })
        leases.forEach { it.release() }

        val afterRelease = registry.acquire(listOf(item))
        assertEquals(1, afterRelease.items.single().targets.size)
        afterRelease.release()
    }

    @Test
    fun partialOverlapAndReleaseAreTrackedPerTarget() = runBlocking {
        val registry = QuickInFlightRegistry()
        val targetA = target("a")
        val targetB = target("b")
        val first = registry.acquire(listOf(item(targets = listOf(targetA))))

        val second = registry.acquire(listOf(item(targets = listOf(targetA, targetB))))
        assertEquals(1, second.skippedTargets)
        assertEquals(listOf(targetB.installDir), second.items.single().targets.map { it.installDir })

        first.release()
        val third = registry.acquire(listOf(item(targets = listOf(targetA, targetB))))
        assertEquals(listOf(targetA.installDir), third.items.single().targets.map { it.installDir })

        second.release()
        third.release()
        val all = registry.acquire(listOf(item(targets = listOf(targetA, targetB))))
        assertEquals(2, all.items.single().targets.size)
        all.release()
        all.release()
        assertTrue(all.released)
    }

    @Test
    fun differentProjectsCannotCommitToTheSameDestination() = runBlocking {
        val registry = QuickInFlightRegistry()
        val targetA = target("a")
        val targetB = target("b")
        val first = registry.acquire(
            listOf(item(targets = listOf(targetA), projectId = "first", fileName = "shared.jar"))
        )

        assertThrows(QuickPlanningException::class.java) {
            registry.acquire(
                listOf(
                    item(
                        targets = listOf(targetB, targetA),
                        projectId = "second",
                        fileName = "shared.jar"
                    )
                )
            )
        }

        // 冲突发生在登记前，目标 B 也不能被失败的任务残留占用。
        val otherDestination = registry.acquire(
            listOf(item(targets = listOf(targetB), projectId = "second", fileName = "other.jar"))
        )
        assertEquals(1, otherDestination.items.single().targets.size)

        first.release()
        otherDestination.release()
        val retry = registry.acquire(
            listOf(item(targets = listOf(targetA), projectId = "second", fileName = "shared.jar"))
        )
        assertEquals(1, retry.items.single().targets.size)
        retry.release()
    }

    @Test
    fun differentFileNamesMayShareAnInstallDirectory() = runBlocking {
        val registry = QuickInFlightRegistry()
        val sharedTarget = target("shared")
        val first = registry.acquire(
            listOf(item(targets = listOf(sharedTarget), projectId = "first", fileName = "first.jar"))
        )

        val second = registry.acquire(
            listOf(item(targets = listOf(sharedTarget), projectId = "second", fileName = "second.jar"))
        )

        assertEquals(1, second.items.single().targets.size)
        first.release()
        second.release()
    }

    private fun item(
        targets: List<QuickInstallTarget>,
        projectId: String = "project",
        fileName: String = "project.jar"
    ) = QuickDownloadItem(
        key = QuickItemKey(Platform.MODRINTH, projectId, "version", PlatformClasses.MOD),
        projectTitle = projectId,
        file = QuickResolvedFile(
            versionId = "version",
            displayName = "Version",
            fileName = fileName,
            downloadUrl = "https://example.invalid/$fileName",
            sha1 = null,
            size = 1L
        ),
        targets = targets
    )

    private fun target(name: String): QuickInstallTarget {
        val game = QuickGameTarget(name, File("build/quick-in-flight/$name").canonicalFile, "1.20.1", listOf("Fabric"))
        return game.installTo(PlatformClasses.MOD)
    }
}
