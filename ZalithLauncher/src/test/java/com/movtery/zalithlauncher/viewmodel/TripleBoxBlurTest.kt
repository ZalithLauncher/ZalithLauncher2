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

package com.movtery.zalithlauncher.viewmodel

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class TripleBoxBlurTest {
    @Test
    fun `solid pixels and alpha remain unchanged`() {
        val width = 32
        val height = 24
        val color = 0x7F3456AB.toInt()
        val pixels = IntArray(width * height) { color }
        val temp = IntArray(pixels.size)

        TripleBoxBlur.blurInPlace(pixels, temp, width, height, 8f)

        assertTrue(pixels.all { it == color })
    }

    @Test
    fun `transparent input stays transparent`() {
        val pixels = IntArray(20 * 20)
        TripleBoxBlur.blurInPlace(pixels, IntArray(pixels.size), 20, 20, 6f)

        assertTrue(pixels.all { it == 0 })
    }

    @Test
    fun `bright region spreads symmetrically without leaving source buffer stale`() {
        val width = 31
        val height = 31
        val pixels = IntArray(width * height) { 0xFF000000.toInt() }
        val center = width / 2
        for (y in center - 4..center + 4) {
            for (x in center - 4..center + 4) {
                pixels[y * width + x] = 0xFFFFFFFF.toInt()
            }
        }

        TripleBoxBlur.blurInPlace(pixels, IntArray(pixels.size), width, height, 4f)

        assertTrue((pixels[center * width + center] and 0xFF) > 0)
        assertTrue((pixels[center * width + center - 6] and 0xFF) > 0)
        assertEquals(pixels[center * width + center - 2], pixels[center * width + center + 2])
        assertEquals(pixels[(center - 2) * width + center], pixels[(center + 2) * width + center])
    }

    @Test
    fun `row scheduled vertical pass matches the straightforward box blur reference`() {
        val width = 37
        val height = 29
        val random = Random(7123)
        val source = IntArray(width * height) { random.nextInt() }
        val actual = source.copyOf()

        TripleBoxBlur.blurInPlace(actual, IntArray(actual.size), width, height, 5f)

        var expected = source
        repeat(3) {
            expected = referenceBoxPass(expected, width, height, 5, horizontal = true)
            expected = referenceBoxPass(expected, width, height, 5, horizontal = false)
        }
        assertTrue(expected.contentEquals(actual))
    }

    @Test
    fun `pixel loop is cooperatively cancellable`() {
        val pixels = IntArray(256 * 256) { 0xFF123456.toInt() }
        var checkpoints = 0
        val failure = IllegalStateException("cancelled")

        val thrown = runCatching {
            TripleBoxBlur.blurInPlace(pixels, IntArray(pixels.size), 256, 256, 12f) {
                checkpoints++
                if (checkpoints == 4) throw failure
            }
        }.exceptionOrNull()

        assertTrue(thrown === failure)
        assertEquals(4, checkpoints)
    }

    @Test
    fun `ten seconds of synthetic video processes every frame and reports a mobile budget`() {
        val plan = calculateBlurSamplingPlan(
            1080, 2400, 90f, DYNAMIC_CPU_BLUR_MAX_DIMENSION, CPU_MAX_SAMPLED_BLUR_RADIUS
        )
        val pixels = IntArray(plan.width * plan.height) { index ->
            0xFF000000.toInt() or (index * 31 and 0x00FFFFFF)
        }
        val temp = IntArray(pixels.size)
        val workspace = IntArray(TripleBoxBlur.workspaceSize(plan.width, plan.height, plan.blurRadiusPx))
        var captures = 0
        var checksum = 0
        val started = System.nanoTime()

        //600 个源帧全部执行真实模糊，不再用调度器把 60Hz 视频减半。
        repeat(600) { frame ->
            pixels[frame % pixels.size] = pixels[frame % pixels.size] xor 0x00112233
            TripleBoxBlur.blurInPlace(
                pixels, temp, plan.width, plan.height, plan.blurRadiusPx, workspace
            )
            checksum = checksum xor pixels[frame % pixels.size]
            captures++
        }
        val elapsedMs = (System.nanoTime() - started) / 1_000_000.0
        val visitsPerFrame = pixels.size * 6L
        val assumedMobileVisitsPerSecond = 25_000_000L
        val mobileBudgetMs = visitsPerFrame * 1000.0 / assumedMobileVisitsPerSecond

        println(
            "Dynamic blur sandbox: ${plan.width}x${plan.height}, " +
                "$captures blurred frames / 600 source frames, ${"%.2f".format(elapsedMs)} ms total, checksum=$checksum"
        )
        println(
            "Mobile budget model (25M pixel visits/s, not device timing): " +
                "${"%.2f".format(mobileBudgetMs)} ms/blur, $visitsPerFrame visits/frame"
        )
        assertEquals(600, captures)
        assertTrue(plan.height > 256)
        assertTrue(mobileBudgetMs > 0.0)
    }

    @Test
    fun `all slider values execute a real static blur preview`() {
        var checksum = 0
        var pixelVisits = 0L
        val started = System.nanoTime()

        for (blur in 1..40) {
            val plan = calculateBlurSamplingPlan(1080, 2400, blur * 3f, 512, 24f)
            val pixels = IntArray(plan.width * plan.height) { index ->
                0xFF000000.toInt() or (index * 31 and 0x00FFFFFF)
            }
            TripleBoxBlur.blurInPlace(pixels, IntArray(pixels.size), plan.width, plan.height, plan.blurRadiusPx)
            checksum = checksum xor pixels[plan.width * plan.height / 2]
            pixelVisits += pixels.size * 6L
        }
        val elapsedMs = (System.nanoTime() - started) / 1_000_000.0
        println("Static slider sandbox: 40 real previews, $pixelVisits pixel visits, ${"%.2f".format(elapsedMs)} ms total")

        assertNotEquals(0, checksum)
        assertTrue(pixelVisits > 0L)
    }

    private fun referenceBoxPass(
        src: IntArray,
        width: Int,
        height: Int,
        radius: Int,
        horizontal: Boolean
    ): IntArray {
        val dst = IntArray(src.size)
        val window = radius * 2 + 1
        for (y in 0 until height) {
            for (x in 0 until width) {
                var sumA = 0
                var sumR = 0
                var sumG = 0
                var sumB = 0
                for (offset in -radius..radius) {
                    val sampleX = if (horizontal) (x + offset).coerceIn(0, width - 1) else x
                    val sampleY = if (horizontal) y else (y + offset).coerceIn(0, height - 1)
                    val p = src[sampleY * width + sampleX]
                    sumA += p ushr 24 and 0xFF
                    sumR += p ushr 16 and 0xFF
                    sumG += p ushr 8 and 0xFF
                    sumB += p and 0xFF
                }
                dst[y * width + x] = (sumA / window shl 24) or
                    (sumR / window shl 16) or
                    (sumG / window shl 8) or
                    (sumB / window)
            }
        }
        return dst
    }
}
