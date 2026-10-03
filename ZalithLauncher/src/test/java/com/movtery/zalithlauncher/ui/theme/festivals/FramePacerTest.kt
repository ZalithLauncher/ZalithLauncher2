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

package com.movtery.zalithlauncher.ui.theme.festivals

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs

class FramePacerTest {

    private val frame60 = 16_666_667L

    @Test
    fun `renders every frame at 60hz`() {
        val pacer = FramePacer()
        var time = 0L
        var renders = 0

        repeat(600) {
            time += frame60
            if (pacer.onVsync(time) != null) renders++
        }

        assertEquals(600, renders)
    }

    @Test
    fun `caps at 60fps on 120hz`() {
        val pacer = FramePacer()
        val frame = 8_333_333L
        var time = 0L
        var renders = 0

        repeat(1200) {
            time += frame
            val delta = pacer.onVsync(time)
            if (delta != null) {
                renders++
                if (delta > 0L) {
                    // 每次步进间隔应接近 16.67ms
                    assertTrue("dt was $delta", abs(delta - frame60) < 3_000_000L)
                }
            }
        }

        // 首帧 + 之后隔帧渲染，共 600 次
        assertEquals(600, renders)
    }

    @Test
    fun `averages 60fps on 90hz`() {
        val pacer = FramePacer()
        val frame = 11_111_111L
        var time = 0L
        var renders = 0
        var deltaSum = 0L

        // 20 秒的 90Hz vsync：期望约 1200 次步进（60fps 平均）
        repeat(1800) {
            time += frame
            val delta = pacer.onVsync(time)
            if (delta != null && delta > 0L) {
                renders++
                deltaSum += delta
            }
        }

        assertTrue("expected ~1200 renders, was $renders", renders in 1188..1212)
        val averageDelta = deltaSum / renders.coerceAtLeast(1)
        assertTrue("average dt was $averageDelta", abs(averageDelta - frame60) < 1_500_000L)
    }

    @Test
    fun `first frame renders with zero delta`() {
        val pacer = FramePacer()
        val delta = pacer.onVsync(100_000_000L)
        assertNotNull(delta)
        assertEquals(0L, delta)
    }

    @Test
    fun `skips frames below the target interval`() {
        val pacer = FramePacer()
        pacer.onVsync(0L)
        // 距上一帧仅 8.33ms，应跳过
        assertNull(pacer.onVsync(8_333_333L))
        // 距首帧 16.67ms，应步进
        assertNotNull(pacer.onVsync(frame60))
    }

    @Test
    fun `reset clears accumulated credit`() {
        val pacer = FramePacer()
        pacer.onVsync(0L)
        pacer.onVsync(16_666_667L)
        // 正常情况下下一帧 33.33ms 处会因余量跳过与否取决于携带值；重置后首帧必定步进
        pacer.reset()
        val delta = pacer.onVsync(20_000_000L)
        assertNotNull(delta)
        assertEquals(0L, delta)
    }
}
