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

package com.movtery.zalithlauncher.viewmodel

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.RectF
import android.os.Build
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.core.graphics.createBitmap
import com.movtery.zalithlauncher.context.GlobalContext
import com.movtery.zalithlauncher.setting.AllSettings
import com.movtery.zalithlauncher.utils.logging.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.math.log2
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.roundToInt

private const val TAG = "BackgroundGlassBlur"

/**
 * 背景玻璃模糊管线
 */
class BackgroundGlassBlur(private val backgroundFile: File) {
    var glassLayer by mutableStateOf<GraphicsLayer?>(null)
        private set

    var captureLayer by mutableStateOf<GraphicsLayer?>(null)
        private set

    var blurredBackground by mutableStateOf<ImageBitmap?>(null)
        private set

    private var sampleFrame: Bitmap? = null
    private var sampleResultA: Bitmap? = null
    private var sampleResultB: Bitmap? = null
    private var sampleResultUseA = false
    private var samplePixels: IntArray? = null
    private var sampleTemp: IntArray? = null

    fun attachGlassLayer(layer: GraphicsLayer?) {
        glassLayer = layer
    }

    fun attachCaptureLayer(layer: GraphicsLayer?) {
        captureLayer = layer
    }

    fun clearBlurredBackground() {
        blurredBackground = null
    }

    suspend fun captureFrame() {
        val layer = captureLayer ?: return
        //层尚未完成首次录制时尺寸为零，跳过本帧
        if (layer.size.width < 1 || layer.size.height < 1) return
        val snapshot = runCatching { layer.toImageBitmap() }
            .onFailure { logSampleFailure(it) }
            .getOrNull() ?: return
        blurredBackground = withContext(Dispatchers.Default) {
            runCatching { downscaleAndBlurSnapshot(snapshot) }
                .onFailure { logSampleFailure(it) }
                .getOrNull()
        }
    }

    private var sampleFailureLogged = false

    private fun logSampleFailure(t: Throwable) {
        if (sampleFailureLogged) return
        sampleFailureLogged = true
        Logger.warning(TAG, "background glass sampling failed, subsequent failed frames will be skipped", t)
    }

    suspend fun refreshStaticDisplayBitmap(blur: Int) {
        blurredBackground = withContext(Dispatchers.IO) {
            runCatching { regenerateStaticBitmap(blur) }.getOrNull()
        }
    }

    private fun downscaleAndBlurSnapshot(snapshot: ImageBitmap): ImageBitmap {
        val androidBitmap = snapshot.asAndroidBitmap()
        val source = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            androidBitmap.copy(Bitmap.Config.ARGB_8888, false) ?: androidBitmap
        } else {
            androidBitmap
        }
        val scale = SAMPLING_MAX_DIM / max(source.width, source.height).coerceAtLeast(1)
        val width = (source.width * scale).roundToInt().coerceAtLeast(1)
        val height = (source.height * scale).roundToInt().coerceAtLeast(1)
        val frame = ensureSampleFrame(width, height)
        frame.eraseColor(0)
        val canvas = Canvas(frame)
        canvas.drawBitmap(source, null, RectF(0f, 0f, width.toFloat(), height.toFloat()), null)
        return blurSampledFrame(frame, frameRadius(scale)).asImageBitmap()
    }

    private suspend fun regenerateStaticBitmap(blur: Int): ImageBitmap? = withContext(Dispatchers.IO) {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(backgroundFile.absolutePath, bounds)
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return@withContext null

        val density = GlobalContext.resources.displayMetrics.density
        val radiusPx = blur * density
        val maxDim = max(bounds.outWidth, bounds.outHeight).toFloat()
        val factor = downscaleFactor(radiusPx, maxDim).toInt().coerceAtLeast(1)

        val bitmap = BitmapFactory.decodeFile(
            backgroundFile.absolutePath,
            BitmapFactory.Options().apply { inSampleSize = factor }
        ) ?: return@withContext null

        blurBitmap(bitmap, (radiusPx / factor).coerceIn(0.5f, 25f)).asImageBitmap()
    }

    private fun ensureSampleFrame(width: Int, height: Int): Bitmap =
        sampleFrame?.takeIf { it.width == width && it.height == height }
            ?: createBitmap(width, height).also { sampleFrame = it }

    private fun blurSampledFrame(source: Bitmap, radiusPx: Float): Bitmap {
        val width = source.width
        val height = source.height
        val size = width * height
        val radius = radiusPx.roundToInt()
            .coerceIn(1, 25)
            .coerceAtMost(minOf(width, height) - 1)

        val pixels = samplePixels?.takeIf { it.size == size }
            ?: IntArray(size).also { samplePixels = it }
        val temp = sampleTemp?.takeIf { it.size == size }
            ?: IntArray(size).also { sampleTemp = it }
        source.getPixels(pixels, 0, width, 0, 0, width, height)

        if (radius >= 1) {
            repeat(3) {
                boxBlur(pixels, temp, width, height, radius, horizontal = true)
                boxBlur(temp, pixels, width, height, radius, horizontal = false)
            }
        }

        sampleResultUseA = !sampleResultUseA
        val result = (if (sampleResultUseA) sampleResultA else sampleResultB)
            ?.takeIf { it.width == width && it.height == height }
            ?: createBitmap(width, height).also {
                if (sampleResultUseA) sampleResultA = it else sampleResultB = it
            }
        result.setPixels(pixels, 0, width, 0, 0, width, height)
        return result
    }

    private fun frameRadius(scale: Float): Float =
        (AllSettings.backgroundBlur.state * GlobalContext.resources.displayMetrics.density * scale)
            .coerceIn(0.5f, 25f)

    private fun downscaleFactor(radiusPx: Float, maxDim: Float): Float {
        val target = max(radiusPx / 20f, maxDim / 512f)
        if (target <= 1f) return 1f
        return 2f.pow(log2(target).roundToInt()).coerceIn(1f, 64f)
    }

    private fun blurBitmap(source: Bitmap, radiusPx: Float): Bitmap {
        val width = source.width
        val height = source.height
        val radius = radiusPx.roundToInt()
            .coerceIn(1, 25)
            .coerceAtMost(minOf(width, height) - 1)
        if (radius < 1) return source

        val pixels = IntArray(width * height)
        source.getPixels(pixels, 0, width, 0, 0, width, height)

        val temp = IntArray(pixels.size)
        repeat(3) {
            boxBlur(pixels, temp, width, height, radius, horizontal = true)
            boxBlur(temp, pixels, width, height, radius, horizontal = false)
        }

        val result = createBitmap(width, height)
        result.setPixels(pixels, 0, width, 0, 0, width, height)
        return result
    }

    private fun boxBlur(
        src: IntArray,
        dst: IntArray,
        width: Int,
        height: Int,
        radius: Int,
        horizontal: Boolean
    ) {
        val window = radius * 2 + 1
        if (horizontal) {
            for (y in 0 until height) {
                val row = y * width
                var sumA = 0
                var sumR = 0
                var sumG = 0
                var sumB = 0
                for (i in -radius..radius) {
                    val p = src[row + i.coerceIn(0, width - 1)]
                    sumA += p ushr 24 and 0xFF
                    sumR += p ushr 16 and 0xFF
                    sumG += p ushr 8 and 0xFF
                    sumB += p and 0xFF
                }
                for (x in 0 until width) {
                    dst[row + x] = (sumA / window shl 24) or
                        (sumR / window shl 16) or
                        (sumG / window shl 8) or
                        (sumB / window)
                    val outP = src[row + (x - radius).coerceIn(0, width - 1)]
                    val inP = src[row + (x + radius + 1).coerceIn(0, width - 1)]
                    sumA += (inP ushr 24 and 0xFF) - (outP ushr 24 and 0xFF)
                    sumR += (inP ushr 16 and 0xFF) - (outP ushr 16 and 0xFF)
                    sumG += (inP ushr 8 and 0xFF) - (outP ushr 8 and 0xFF)
                    sumB += (inP and 0xFF) - (outP and 0xFF)
                }
            }
        } else {
            for (x in 0 until width) {
                var sumA = 0
                var sumR = 0
                var sumG = 0
                var sumB = 0
                for (i in -radius..radius) {
                    val p = src[i.coerceIn(0, height - 1) * width + x]
                    sumA += p ushr 24 and 0xFF
                    sumR += p ushr 16 and 0xFF
                    sumG += p ushr 8 and 0xFF
                    sumB += p and 0xFF
                }
                for (y in 0 until height) {
                    dst[y * width + x] = (sumA / window shl 24) or
                        (sumR / window shl 16) or
                        (sumG / window shl 8) or
                        (sumB / window)
                    val outP = src[(y - radius).coerceIn(0, height - 1) * width + x]
                    val inP = src[(y + radius + 1).coerceIn(0, height - 1) * width + x]
                    sumA += (inP ushr 24 and 0xFF) - (outP ushr 24 and 0xFF)
                    sumR += (inP ushr 16 and 0xFF) - (outP ushr 16 and 0xFF)
                    sumG += (inP ushr 8 and 0xFF) - (outP ushr 8 and 0xFF)
                    sumB += (inP and 0xFF) - (outP and 0xFF)
                }
            }
        }
    }
}

private const val SAMPLING_MAX_DIM = 384f
