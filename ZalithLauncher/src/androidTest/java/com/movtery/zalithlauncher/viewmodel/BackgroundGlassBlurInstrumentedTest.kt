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
import android.graphics.Color
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class BackgroundGlassBlurInstrumentedTest {
    @Test
    fun staticOverlayIsPrecomposedExactlyOnce() {
        val width = 4
        val height = 1
        val pixels = intArrayOf(
            0xFF202020.toInt(),
            0xFF808080.toInt(),
            0xFFC08040.toInt(),
            0xFF000000.toInt()
        )
        val alpha = whiteOverlayAlpha(80)

        val withoutOverlay = createStaticBlurResultBitmap(pixels, width, height)
        assertArrayEquals(pixels, withoutOverlay.readPixels(width, height))

        val once = createStaticBlurResultBitmap(pixels, width, height, alpha)
        val oncePixels = once.readPixels(width, height)
        val originalRed = Color.red(pixels[1])
        val onceRed = Color.red(oncePixels[1])
        assertTrue("Softlight should brighten the mid-gray pixel", onceRed > originalRed)
        assertEquals(255, Color.alpha(oncePixels[1]))

        val twice = createStaticBlurResultBitmap(oncePixels, width, height, alpha)
        val twiceRed = Color.red(twice.readPixels(width, height)[1])
        assertTrue("A repeated overlay must not be treated as visually equivalent", twiceRed > onceRed)
    }

    @Test
    fun staticPublicationIncludesOverlayMetadataAndClearResetsIt() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val sourceFile = File.createTempFile("background-glass-test", ".png", context.cacheDir)
        try {
            val source = Bitmap.createBitmap(8, 8, Bitmap.Config.ARGB_8888).apply {
                eraseColor(0xFF526578.toInt())
            }
            sourceFile.outputStream().use { output ->
                assertTrue(source.compress(Bitmap.CompressFormat.PNG, 100, output))
            }

            val blur = BackgroundGlassBlur(sourceFile)
            runBlocking {
                blur.refreshStaticDisplayBitmap(blur = 20, targetWidth = 64, targetHeight = 32)
            }

            val frame = blur.blurredBackgroundFrame
            assertNotNull(frame)
            assertNotNull(frame?.image)
            assertTrue(frame?.whiteOverlayApplied == true)

            blur.clearBlurredBackground()
            assertNull(blur.blurredBackgroundFrame)
            assertNull(blur.blurredBackground)
        } finally {
            sourceFile.delete()
        }
    }

    private fun Bitmap.readPixels(width: Int, height: Int): IntArray =
        IntArray(width * height).also { pixels ->
            getPixels(pixels, 0, width, 0, 0, width, height)
        }
}
