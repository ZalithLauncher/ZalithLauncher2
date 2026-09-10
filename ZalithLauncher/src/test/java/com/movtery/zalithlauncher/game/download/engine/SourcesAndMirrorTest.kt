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

import com.movtery.zalithlauncher.game.addons.mirror.MirrorPriority
import com.movtery.zalithlauncher.game.addons.mirror.orderCandidates
import com.movtery.zalithlauncher.game.addons.mirror.resolveMirrorPriority
import com.movtery.zalithlauncher.setting.enums.MirrorSourceType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

private fun fault(code: Int): Throwable = when (code) {
    -1 -> UnknownHost()
    else -> HttpResultException(code, "synthetic")
}

private class UnknownHost : java.net.UnknownHostException("synthetic")

class SourceSetTest {

    @Test
    fun `acquire keeps preference order while every host is healthy`() {
        val sources = SourceSet(listOf("a", "b", "c"))
        assertEquals("a", sources.acquire(requireRange = false)!!.url)
        assertEquals("a", sources.acquire(requireRange = false)!!.url)
    }

    @Test
    fun `acquire prefers the host with fewer recent failures`() {
        val health = HostHealth(tripThreshold = 10)
        repeat(2) { health.recordFailure("https://mirror.example/f1", fault(500)) }

        val sources = SourceSet(listOf("https://mirror.example/f", "https://official.example/f"), health)
        assertEquals("https://official.example/f", sources.acquire(false)!!.url)
    }

    @Test
    fun `soft failures accumulate until disabled then revive on degrade`() {
        val sources = SourceSet(listOf("a", "b"))
        val first = sources.acquire(false)!!

        repeat(SourceSet.SOFT_FAILURE_LIMIT - 1) {
            assertTrue(first.recordFailure(fault(500)))
        }
        assertFalse(first.recordFailure(fault(500)))
        assertEquals("b", sources.acquire(false)!!.url)

        sources.degrade()
        //软禁用的 a 复活，但它的主机失败计数更高，健康排序仍先给出 b
        assertEquals("b", sources.acquire(false)!!.url)
    }

    @Test
    fun `fatal statuses disable immediately and survive degrade`() {
        val sources = SourceSet(listOf("fatal", "healthy"))
        val fatalSource = sources.acquire(false)!!
        assertFalse(fatalSource.recordFailure(fault(404)))
        sources.degrade()
        assertEquals("healthy", sources.acquire(false)!!.url)
    }

    @Test
    fun `dns failure counts as fatal`() {
        val sources = SourceSet(listOf("gone"))
        assertFalse(sources.acquire(false)!!.recordFailure(fault(-1)))
    }

    @Test
    fun `server overload statuses stay soft instead of fatal`() {
        val sources = SourceSet(listOf("a", "b"))
        val first = sources.acquire(false)!!

        //502 属于源过载的常见形态，第一次失败后源仍可用
        assertTrue(first.recordFailure(fault(502)))
    }

    @Test
    fun `range requirement skips no-range sources`() {
        val sources = SourceSet(listOf("noRange", "ranged"))
        sources.acquire(false)!!.markNoRangeSupport()
        assertEquals("ranged", sources.acquire(requireRange = true)!!.url)
    }

    @Test
    fun `range exhausted returns null while plain mode still works`() {
        val sources = SourceSet(listOf("only"))
        sources.acquire(false)!!.markNoRangeSupport()
        assertNull(sources.acquire(requireRange = true))
        assertNotNull(sources.acquire(requireRange = false))
    }
}

class MirrorOrderTest {

    @Test
    fun `auto resolves by mainland flag`() {
        assertEquals(MirrorPriority.MIRROR_FIRST, resolveMirrorPriority(MirrorSourceType.AUTO, mainland = true))
        assertEquals(MirrorPriority.OFFICIAL_FIRST, resolveMirrorPriority(MirrorSourceType.AUTO, mainland = false))
    }

    @Test
    fun `explicit choices always win`() {
        assertEquals(MirrorPriority.OFFICIAL_FIRST, resolveMirrorPriority(MirrorSourceType.OFFICIAL_FIRST, mainland = true))
        assertEquals(MirrorPriority.MIRROR_FIRST, resolveMirrorPriority(MirrorSourceType.MIRROR_FIRST, mainland = false))
    }

    @Test
    fun `candidate ordering`() {
        assertEquals(listOf("o", "m"), orderCandidates("o", "m", MirrorPriority.OFFICIAL_FIRST))
        assertEquals(listOf("m", "o"), orderCandidates("o", "m", MirrorPriority.MIRROR_FIRST))
        assertEquals(listOf("o"), orderCandidates("o", null, MirrorPriority.MIRROR_FIRST))
    }
}
