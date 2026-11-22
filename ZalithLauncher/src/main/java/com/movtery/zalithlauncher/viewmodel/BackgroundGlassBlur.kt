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
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.neverEqualPolicy
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.core.graphics.createBitmap
import com.movtery.zalithlauncher.context.GlobalContext
import com.movtery.zalithlauncher.utils.logging.Logger
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File
import java.util.concurrent.atomic.AtomicLong
import kotlin.coroutines.CoroutineContext
import kotlin.math.log2
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.roundToInt

private const val TAG = "BackgroundGlassBlur"
private const val STATIC_SAMPLING_MAX_DIM = 512
private const val STATIC_MAX_SAMPLED_BLUR_RADIUS = 24f

/** 每次静态发布都创建独立位图，调用方不得再修改返回对象。 */
internal fun createStaticBlurResultBitmap(pixels: IntArray, width: Int, height: Int): Bitmap =
    createBitmap(width, height).also { result ->
        result.setPixels(pixels, 0, width, 0, 0, width, height)
    }

/**
 * 背景玻璃模糊管线。
 * 静态图片按请求缓存，动态内容只处理已经降采样的捕获层。
 */
class BackgroundGlassBlur(private val backgroundFile: File) {
    var glassLayer by mutableStateOf<GraphicsLayer?>(null)
        private set

    var captureLayer by mutableStateOf<GraphicsLayer?>(null)
        private set

    //动态双缓冲在取消/重试后可能再次发布同一 Bitmap，必须始终通知 Compose 重绘。
    var blurredBackground by mutableStateOf<ImageBitmap?>(null, neverEqualPolicy())
        private set

    private var sampleResultA: Bitmap? = null
    private var sampleResultB: Bitmap? = null
    private var sampleResultUseA = false
    private var samplePixels: IntArray? = null
    private var sampleTemp: IntArray? = null
    private var sampleWorkspace: IntArray? = null

    private val staticMutex = Mutex()
    private val staticCacheEpoch = AtomicLong()
    private var staticSource: Bitmap? = null
    private var staticSourceKey: StaticSourceKey? = null
    private var staticResultRequest: StaticRequest? = null
    private var staticFrame: Bitmap? = null
    private var staticCanvas: Canvas? = null
    private var staticPixels: IntArray? = null
    private var staticTemp: IntArray? = null
    private var staticWorkspace: IntArray? = null
    private val staticSrcRect = Rect()
    private val staticDstRect = Rect()
    private val staticPaint = Paint(Paint.FILTER_BITMAP_FLAG)

    fun attachGlassLayer(layer: GraphicsLayer?) {
        glassLayer = layer
    }

    fun attachCaptureLayer(layer: GraphicsLayer?) {
        captureLayer = layer
    }

    fun clearBlurredBackground() {
        blurredBackground = null
    }

    /** 媒体文件变化后使解码源与尚未完成的静态请求失效。 */
    fun invalidateStaticCache() {
        staticCacheEpoch.incrementAndGet()
        blurredBackground = null
    }

    /**
     * 捕获当前低分辨率捕获层的一帧。
     * @param sampledRadiusPx 已在捕获层降采样时同步缩小的模糊半径
     */
    suspend fun captureFrame(sampledRadiusPx: Float) {
        val layer = captureLayer ?: return
        if (layer.size.width < 1 || layer.size.height < 1) return
        val snapshot = try {
            layer.toImageBitmap()
        } catch (c: CancellationException) {
            throw c
        } catch (t: Throwable) {
            logSampleFailure(t)
            return
        }

        val context = currentCoroutineContext()
        val result = withContext(Dispatchers.Default) {
            try {
                blurSnapshot(snapshot, sampledRadiusPx, context)
            } catch (c: CancellationException) {
                throw c
            } catch (t: Throwable) {
                logSampleFailure(t)
                null
            }
        }
        if (result != null) blurredBackground = result
    }

    /**
     * 生成静态显示位图。相同请求直接命中缓存，新请求完成前保留上一张结果供实时预览。
     */
    suspend fun refreshStaticDisplayBitmap(blur: Int, targetWidth: Int, targetHeight: Int) {
        if (targetWidth <= 0 || targetHeight <= 0) return
        withContext(Dispatchers.IO) {
            staticMutex.withLock {
                val epoch = staticCacheEpoch.get()
                val request = StaticRequest(
                    blur = blur,
                    targetWidth = targetWidth,
                    targetHeight = targetHeight,
                    epoch = epoch,
                    lastModified = backgroundFile.lastModified(),
                    fileLength = backgroundFile.length()
                )
                if (blurredBackground != null && staticResultRequest == request) return@withLock

                val context = currentCoroutineContext()
                try {
                    val result = regenerateStaticBitmap(request, context) ?: return@withLock
                    context.ensureActive()
                    ensureStaticEpoch(epoch)
                    blurredBackground = result.asImageBitmap()
                    staticResultRequest = request
                } catch (c: CancellationException) {
                    throw c
                } catch (t: Throwable) {
                    logSampleFailure(t)
                }
            }
        }
    }

    private fun blurSnapshot(
        snapshot: ImageBitmap,
        sampledRadiusPx: Float,
        context: CoroutineContext
    ): ImageBitmap {
        val androidBitmap = snapshot.asAndroidBitmap()
        // API 28+ 的层快照通常是 HARDWARE，CPU 读取前只需转换硬件位图。
        val source = if (androidBitmap.config == Bitmap.Config.HARDWARE) {
            androidBitmap.copy(Bitmap.Config.ARGB_8888, false) ?: androidBitmap
        } else {
            androidBitmap
        }
        //捕获层已经按当前模糊半径采样，这里不再进行第二次降分辨率。
        return blurSampledFrame(source, sampledRadiusPx, context).asImageBitmap()
    }

    private fun regenerateStaticBitmap(request: StaticRequest, context: CoroutineContext): Bitmap? {
        val source = decodeStaticSource(request) ?: return null
        context.ensureActive()
        ensureStaticEpoch(request.epoch)

        val density = GlobalContext.resources.displayMetrics.density
        val plan = calculateBlurSamplingPlan(
            sourceWidth = request.targetWidth,
            sourceHeight = request.targetHeight,
            blurRadiusPx = request.blur * density,
            maxDimension = STATIC_SAMPLING_MAX_DIM,
            maxSampledRadius = STATIC_MAX_SAMPLED_BLUR_RADIUS
        )
        val crop = calculateCenterCrop(
            sourceWidth = source.width,
            sourceHeight = source.height,
            targetWidth = request.targetWidth,
            targetHeight = request.targetHeight
        )
        val frame = staticFrame?.takeIf { it.width == plan.width && it.height == plan.height }
            ?: createBitmap(plan.width, plan.height).also { staticFrame = it }
        frame.eraseColor(0)
        val canvas = staticCanvas ?: Canvas().also { staticCanvas = it }
        canvas.setBitmap(frame)
        staticSrcRect.set(crop.left, crop.top, crop.left + crop.width, crop.top + crop.height)
        staticDstRect.set(0, 0, plan.width, plan.height)
        canvas.drawBitmap(source, staticSrcRect, staticDstRect, staticPaint)

        val size = plan.width * plan.height
        val pixels = staticPixels?.takeIf { it.size == size }
            ?: IntArray(size).also { staticPixels = it }
        val temp = staticTemp?.takeIf { it.size == size }
            ?: IntArray(size).also { staticTemp = it }
        frame.getPixels(pixels, 0, plan.width, 0, 0, plan.width, plan.height)
        val workspaceSize = TripleBoxBlur.workspaceSize(plan.width, plan.height, plan.blurRadiusPx)
        val workspace = staticWorkspace?.takeIf { it.size >= workspaceSize }
            ?: IntArray(workspaceSize).also { staticWorkspace = it }
        TripleBoxBlur.blurInPlace(
            pixels = pixels,
            temp = temp,
            width = plan.width,
            height = plan.height,
            radiusPx = plan.blurRadiusPx,
            workspace = workspace,
            checkpoint = {
                context.ensureActive()
                ensureStaticEpoch(request.epoch)
            }
        )

        //已发布的 ImageBitmap 必须视为不可变；静态预览始终用新对象替换旧图。
        return createStaticBlurResultBitmap(pixels, plan.width, plan.height)
    }

    private fun decodeStaticSource(request: StaticRequest): Bitmap? {
        //拖动滑杆时媒体与代次均未变化，直接复用解码源，连 bounds 头也不重复读取
        staticSource?.let { source ->
            val key = staticSourceKey
            if (key != null && key.epoch == request.epoch &&
                key.lastModified == request.lastModified && key.fileLength == request.fileLength
            ) {
                return source
            }
        }

        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(backgroundFile.absolutePath, bounds)
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null

        val sampleSize = downscaleFactor(max(bounds.outWidth, bounds.outHeight).toFloat())
        val key = StaticSourceKey(
            epoch = request.epoch,
            lastModified = request.lastModified,
            fileLength = request.fileLength,
            sampleSize = sampleSize
        )
        staticSource?.takeIf { staticSourceKey == key }?.let { return it }

        val decoded = BitmapFactory.decodeFile(
            backgroundFile.absolutePath,
            BitmapFactory.Options().apply { inSampleSize = sampleSize }
        ) ?: return null
        ensureStaticEpoch(request.epoch)
        staticSource = decoded
        staticSourceKey = key
        return decoded
    }

    private fun ensureStaticEpoch(epoch: Long) {
        if (staticCacheEpoch.get() != epoch) {
            throw CancellationException("Background changed while generating static blur")
        }
    }

    private fun blurSampledFrame(
        source: Bitmap,
        radiusPx: Float,
        context: CoroutineContext
    ): Bitmap {
        val width = source.width
        val height = source.height
        val size = width * height
        val pixels = samplePixels?.takeIf { it.size == size }
            ?: IntArray(size).also { samplePixels = it }
        val temp = sampleTemp?.takeIf { it.size == size }
            ?: IntArray(size).also { sampleTemp = it }
        val workspaceSize = TripleBoxBlur.workspaceSize(width, height, radiusPx)
        val workspace = sampleWorkspace?.takeIf { it.size >= workspaceSize }
            ?: IntArray(workspaceSize).also { sampleWorkspace = it }
        source.getPixels(pixels, 0, width, 0, 0, width, height)
        TripleBoxBlur.blurInPlace(pixels, temp, width, height, radiusPx, workspace) {
            context.ensureActive()
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

    private var sampleFailureLogged = false

    private fun logSampleFailure(t: Throwable) {
        if (sampleFailureLogged) return
        sampleFailureLogged = true
        Logger.warning(TAG, "background glass sampling failed, subsequent failed frames will be skipped", t)
    }

    private fun downscaleFactor(maxDim: Float): Int {
        val target = maxDim / STATIC_SAMPLING_MAX_DIM.toFloat()
        if (target <= 1f) return 1
        return 2f.pow(log2(target).roundToInt()).toInt().coerceIn(1, 64)
    }
}

private data class StaticSourceKey(
    val epoch: Long,
    val lastModified: Long,
    val fileLength: Long,
    val sampleSize: Int
)

private data class StaticRequest(
    val blur: Int,
    val targetWidth: Int,
    val targetHeight: Int,
    val epoch: Long,
    val lastModified: Long,
    val fileLength: Long
)
