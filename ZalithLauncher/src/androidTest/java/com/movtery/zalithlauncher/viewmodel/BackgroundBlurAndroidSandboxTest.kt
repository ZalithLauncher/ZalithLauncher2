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

import android.graphics.Bitmap
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/** 在真实 ART 与 Android Bitmap 上运行；模拟器耗时仍不能视为 ARM 真机耗时。 */
@RunWith(AndroidJUnit4::class)
class BackgroundBlurAndroidSandboxTest {
    @Test
    fun constrainedVideoAndStaticBitmapPipeline() {
        runVideoFrames()
        runStaticSliderPreviews()
    }

    private fun runVideoFrames() {
        val plan = calculateBlurSamplingPlan(
            1080, 2400, 90f, DYNAMIC_CPU_BLUR_MAX_DIMENSION, CPU_MAX_SAMPLED_BLUR_RADIUS
        )
        val source = Bitmap.createBitmap(plan.width, plan.height, Bitmap.Config.ARGB_8888)
        val framePixels = IntArray(plan.width * plan.height) { index ->
            0xFF000000.toInt() or (index * 31 and 0x00FFFFFF)
        }
        val blurPixels = IntArray(framePixels.size)
        val temp = IntArray(framePixels.size)
        val workspace = IntArray(TripleBoxBlur.workspaceSize(plan.width, plan.height, plan.blurRadiusPx))
        val results = arrayOf(
            Bitmap.createBitmap(plan.width, plan.height, Bitmap.Config.ARGB_8888),
            Bitmap.createBitmap(plan.width, plan.height, Bitmap.Config.ARGB_8888)
        )
        var checksum = 0
        val started = System.nanoTime()

        repeat(120) { frame ->
            framePixels[frame % framePixels.size] = framePixels[frame % framePixels.size] xor 0x00112233
            source.setPixels(framePixels, 0, plan.width, 0, 0, plan.width, plan.height)
            source.getPixels(blurPixels, 0, plan.width, 0, 0, plan.width, plan.height)
            TripleBoxBlur.blurInPlace(
                blurPixels, temp, plan.width, plan.height, plan.blurRadiusPx, workspace
            )
            results[frame and 1].setPixels(blurPixels, 0, plan.width, 0, 0, plan.width, plan.height)
            checksum = checksum xor blurPixels[frame % blurPixels.size]
        }

        val elapsedMs = (System.nanoTime() - started) / 1_000_000.0
        println(
            "Android constrained sandbox: ${plan.width}x${plan.height}, 120/120 frames, " +
                "${"%.2f".format(elapsedMs)} ms total, ${"%.2f".format(elapsedMs / 120)} ms/frame, " +
                "checksum=$checksum, vm=${System.getProperty("java.vm.name")}"
        )
        assertTrue(elapsedMs > 0.0)
    }

    private fun runStaticSliderPreviews() {
        var previous: Bitmap? = null
        var checksum = 0
        val started = System.nanoTime()

        for (blur in 1..40) {
            val plan = calculateBlurSamplingPlan(1080, 2400, blur * 3f, 512, 24f)
            val pixels = IntArray(plan.width * plan.height) { index ->
                0xFF000000.toInt() or (index * 31 and 0x00FFFFFF)
            }
            val workspace = IntArray(TripleBoxBlur.workspaceSize(plan.width, plan.height, plan.blurRadiusPx))
            TripleBoxBlur.blurInPlace(
                pixels, IntArray(pixels.size), plan.width, plan.height, plan.blurRadiusPx, workspace
            )
            val published = createStaticBlurResultBitmap(pixels, plan.width, plan.height)
            assertNotSame("A published static bitmap must never be mutated by the next preview", previous, published)
            previous = published
            checksum = checksum xor pixels[pixels.size / 2]
        }

        val elapsedMs = (System.nanoTime() - started) / 1_000_000.0
        println("Android static slider sandbox: 40 previews, ${"%.2f".format(elapsedMs)} ms total, checksum=$checksum")
        assertTrue(previous != null)
    }
}
