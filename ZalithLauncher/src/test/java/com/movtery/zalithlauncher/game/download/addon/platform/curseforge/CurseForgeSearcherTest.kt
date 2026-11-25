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
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/gpl-3.0.txt>.
 */

package com.movtery.zalithlauncher.game.download.addon.platform.curseforge

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.concurrent.atomic.AtomicInteger

class CurseForgeSearcherTest {
    @Test
    fun fullPageAtPaginationLimitDoesNotRequestBeyondLimit() = runBlocking {
        val requestCount = AtomicInteger()
        val maximumIndex = AtomicInteger()

        val versions = getAllVersions<Int, String>(
            pageSize = CURSEFORGE_MAX_PAGE_SIZE,
            chunkSize = 20,
            maxConcurrent = 10,
            checkNotEmpty = { true },
            asyncVersions = { index, pageSize ->
                require(index <= CURSEFORGE_MAX_PAGINATION_RESULTS - pageSize)
                requestCount.incrementAndGet()
                maximumIndex.accumulateAndGet(index, ::maxOf)
                index
            },
            processVersions = {
                // 始终模拟满页，覆盖“第 10000 条之后仍尝试下一页”的边界。
                emptyList<String>() to CURSEFORGE_MAX_PAGE_SIZE
            }
        )

        assertTrue(versions.isEmpty())
        assertEquals(CURSEFORGE_MAX_PAGINATION_RESULTS / CURSEFORGE_MAX_PAGE_SIZE, requestCount.get())
        assertEquals(CURSEFORGE_MAX_PAGINATION_RESULTS - CURSEFORGE_MAX_PAGE_SIZE, maximumIndex.get())
    }
}
