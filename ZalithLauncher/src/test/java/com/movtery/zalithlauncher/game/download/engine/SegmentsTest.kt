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

package com.movtery.zalithlauncher.game.download.engine

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SegmentMathTest {

    @Test
    fun `split refuses when remaining cannot fit a compliant tail`() {
        //剩余量恰好等于最小尾段：不可拆（旧段将一无所有）
        assertNull(SegmentChain.splitOffset(position = 0, endExclusive = 512L * 1024L, minTailSize = 512L * 1024L))
        assertNull(SegmentChain.splitOffset(position = 1_000_000, endExclusive = 1_000_000, minTailSize = 1024))
    }

    @Test
    fun `split point guarantees tail at least min size and keeps position before it`() {
        val end = 10L * 1024L * 1024L
        val cut = SegmentChain.splitOffset(position = 0, endExclusive = end, minTailSize = 256L * 1024L)
        assertNotNull(cut)
        cut!!
        assertTrue(cut > 0)
        assertEquals(end - (end * SegmentChain.TAIL_FRACTION).toLong(), cut)
    }

    @Test
    fun `split respects already downloaded progress as boundary`() {
        val end = 1L shl 20
        val progress = 600L * 1024L
        val cut = SegmentChain.splitOffset(position = progress, endExclusive = end, minTailSize = 256L * 1024L)!!
        assertTrue(cut > progress && cut < end)
    }
}

class SegmentChainTest {

    @Test
    fun `chain covers whole range and completion requires all segments done`() {
        val chain = SegmentChain(1000)
        assertFalse(chain.isComplete())

        val first = chain.first
        first.done.set(400)
        val second = chain.split(first, 500)
        assertEquals(500L, first.endOr(chain.tailEnd()))

        first.done.set(500)
        second.done.set(300)
        assertFalse(chain.isComplete())
        second.done.set(500)
        assertTrue(chain.isComplete())
    }

    @Test
    fun `pending excludes finished and honours eof for unknown size`() {
        val chain = SegmentChain(-1)
        assertTrue(chain.pendingSegments().isNotEmpty())
        chain.eofReached()
        assertTrue(chain.pendingSegments().isEmpty())
        assertTrue(chain.isComplete())

        chain.resetForSingleStream()
        assertFalse(chain.isComplete())
        assertEquals(listOf(0L), chain.snapshot().map { it.start })
    }

    @Test
    fun `latest declared total always wins over stale expectation`() {
        val unknown = SegmentChain(-1)
        unknown.adoptTotal(2048)
        unknown.adoptTotal(4096)
        assertEquals(4096L, unknown.totalSize)

        //陈旧的期望值会被远端声明覆盖，完成判定随之刷新
        val stale = SegmentChain(947865).also { it.first.done.set(947865) }
        assertTrue(stale.isComplete())
        stale.adoptTotal(957945)
        assertFalse(stale.isComplete())
        stale.first.done.set(957945)
        assertTrue(stale.isComplete())
    }

    @Test
    fun `reset for single stream collapses splits and zeroes progress`() {
        val chain = SegmentChain(1000)
        val head = chain.first
        head.done.set(300)
        chain.split(head, 600).done.set(150)

        chain.resetForSingleStream()
        assertEquals(1, chain.snapshot().size)
        assertEquals(0L, chain.first.done.get())
        assertFalse(chain.isComplete())
    }
}
