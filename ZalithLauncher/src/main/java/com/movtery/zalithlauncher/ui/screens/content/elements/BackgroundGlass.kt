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

package com.movtery.zalithlauncher.ui.screens.content.elements

import android.graphics.Matrix
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.BlurEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.GraphicsContext
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.Matrix as ComposeMatrix
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.node.CompositionLocalConsumerModifierNode
import androidx.compose.ui.node.DrawModifierNode
import androidx.compose.ui.node.GlobalPositionAwareModifierNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.node.currentValueOf
import androidx.compose.ui.node.invalidateDraw
import androidx.compose.ui.platform.LocalGraphicsContext
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.util.lerp
import androidx.core.graphics.withSave
import com.movtery.zalithlauncher.setting.LauncherSettings
import com.movtery.zalithlauncher.setting.enums.BackgroundBlur
import com.movtery.zalithlauncher.viewmodel.BackgroundViewModel
import com.movtery.zalithlauncher.viewmodel.BlurSamplingPlan
import com.movtery.zalithlauncher.viewmodel.CPU_MAX_SAMPLED_BLUR_RADIUS
import com.movtery.zalithlauncher.viewmodel.DYNAMIC_CPU_BLUR_MAX_DIMENSION
import com.movtery.zalithlauncher.viewmodel.DYNAMIC_GPU_BLUR_MAX_DIMENSION
import com.movtery.zalithlauncher.viewmodel.GPU_MAX_SAMPLED_BLUR_RADIUS
import com.movtery.zalithlauncher.viewmodel.LocalBackgroundViewModel
import com.movtery.zalithlauncher.viewmodel.calculateBlurSamplingPlan
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import kotlin.math.sqrt

/**
 * 背景毛玻璃效果：在元素内容下方对齐绘制启动器背景的预模糊结果
 * @param enabled 是否应用模糊效果
 */
@Composable
fun Modifier.backgroundGlass(
    blur: Int,
    color: Color,
    enabled: Boolean = true,
): Modifier {
    if (LauncherSettings.backgroundBlurType.state == BackgroundBlur.Background) return this
    if (!enabled) return this
    val background = LocalBackgroundViewModel.current?.takeIf { it.isValid } ?: return this
    if (blur <= 0 || LauncherSettings.launcherBackgroundOpacity.state >= 100) return this
    return this then GlassElement(blur, color, background)
}

/**
 * 启动器背景捕获：由 Background 挂载，发布背景内容与屏幕区域供毛玻璃效果采样
 * @param recordContent 是否捕获动态内容（静态图片直接使用位图缓存）
 * @param displayBlurred 背景模糊模式下，是否以模糊结果替代清晰背景显示
 * @param blurRadiusPx 模糊半径（屏幕像素）
 * @param whiteOverlayAlpha 白色提亮层透明度（0 表示不绘制）
 */
internal fun Modifier.backgroundCapture(
    store: BackgroundViewModel,
    recordContent: Boolean,
    displayBlurred: Boolean,
    blurRadiusPx: Float,
    whiteOverlayAlpha: Float,
): Modifier = this then BackgroundCaptureElement(
    store = store,
    recordContent = recordContent,
    displayBlurred = displayBlurred,
    blurRadiusPx = blurRadiusPx,
    whiteOverlayAlpha = whiteOverlayAlpha
)

internal fun whiteOverlayAlpha(blur: Int): Float {
    val t = (blur / 80f).coerceIn(0f, 1f)
    return lerp(start = 0f, stop = 0.25f, fraction = sqrt(t))
}

/** 将 Compose 的列主序 4×4 矩阵转换为 Android Canvas 的 3×3 矩阵（局部 z 固定为 0）。 */
internal fun Matrix.setFromComposeMatrix(source: ComposeMatrix, target: FloatArray) {
    require(target.size >= 9) { "A 3x3 matrix requires 9 floats" }
    val values = source.values
    target[Matrix.MSCALE_X] = values[0]
    target[Matrix.MSKEW_X] = values[4]
    target[Matrix.MTRANS_X] = values[12]
    target[Matrix.MSKEW_Y] = values[1]
    target[Matrix.MSCALE_Y] = values[5]
    target[Matrix.MTRANS_Y] = values[13]
    target[Matrix.MPERSP_0] = values[3]
    target[Matrix.MPERSP_1] = values[7]
    target[Matrix.MPERSP_2] = values[15]
    setValues(target)
}

private data class GlassElement(
    val blur: Int,
    val color: Color,
    val store: BackgroundViewModel,
) : ModifierNodeElement<GlassNode>() {
    override fun create() = GlassNode(blur, color, store)

    override fun update(node: GlassNode) {
        node.blur = blur
        node.color = color
    }
}

/**
 * 毛玻璃绘制节点：绘制期间读取的状态变化只触发重绘，不触发重组
 */
private class GlassNode(
    blur: Int,
    color: Color,
    val store: BackgroundViewModel,
) : Modifier.Node(), DrawModifierNode, GlobalPositionAwareModifierNode {
    var blur: Int = blur
        set(value) {
            if (field == value) return
            field = value
            invalidateDraw()
        }

    var color: Color = color
        set(value) {
            if (field == value) return
            field = value
            invalidateDraw()
        }

    //完整的外层 graphicsLayer 变换只在合成时生效，录制画布的 matrix 无法代表它。
    private var layoutCoordinates: LayoutCoordinates? = null

    //绘制时复用的临时对象，避免每次绘制都重新分配
    private val localToScreen = ComposeMatrix()
    private val androidMatrix = Matrix()
    private val invertedMatrix = Matrix()
    private val matrixValues = FloatArray(9)

    override fun onGloballyPositioned(coordinates: LayoutCoordinates) {
        layoutCoordinates = coordinates
        invalidateDraw()
    }

    override fun onDetach() {
        layoutCoordinates = null
    }

    override fun ContentDrawScope.draw() {
        drawGlass()
        drawContent()
    }

    private fun DrawScope.drawGlass() {
        if (blur <= 0 || LauncherSettings.launcherBackgroundOpacity.flow.value >= 100) return
        val bounds = store.backgroundBounds
        val coordinates = layoutCoordinates
        if (bounds.isEmpty || coordinates == null || !coordinates.isAttached) return

        //graphicsLayer 在内容录制后才应用变换；仅反转 Canvas matrix 会让背景再次跟随外层缩放。
        //这里使用完整的 local -> screen 矩阵，绘制时预先抵消它，使合成后的背景仍固定在屏幕上。
        localToScreen.reset()
        coordinates.transformToScreen(localToScreen)
        androidMatrix.setFromComposeMatrix(localToScreen, matrixValues)
        if (!androidMatrix.invert(invertedMatrix)) return

        val nativeCanvas = drawContext.canvas.nativeCanvas
        nativeCanvas.withSave {
            concat(invertedMatrix)
            val bitmap = store.blurredBackground
            if (bitmap != null) {
                this@drawGlass.drawImage(
                    image = bitmap,
                    dstOffset = IntOffset(bounds.left.roundToInt(), bounds.top.roundToInt()),
                    dstSize = IntSize(bounds.width.roundToInt(), bounds.height.roundToInt())
                )
            } else {
                store.glassLayer?.let { layer ->
                    this@drawGlass.translate(bounds.left, bounds.top) {
                        drawLayer(layer)
                    }
                }
            }
        }

        drawRect(color = color, blendMode = BlendMode.SrcOver)
        drawRect(
            color = Color.White.copy(alpha = whiteOverlayAlpha(blur)),
            blendMode = BlendMode.Softlight
        )
    }
}

private data class BackgroundCaptureElement(
    val store: BackgroundViewModel,
    val recordContent: Boolean,
    val displayBlurred: Boolean,
    val blurRadiusPx: Float,
    val whiteOverlayAlpha: Float,
) : ModifierNodeElement<BackgroundCaptureNode>() {
    override fun create() = BackgroundCaptureNode(
        store, recordContent, displayBlurred, blurRadiusPx, whiteOverlayAlpha
    )

    override fun update(node: BackgroundCaptureNode) {
        node.store = store
        node.recordContent = recordContent
        node.displayBlurred = displayBlurred
        node.blurRadiusPx = blurRadiusPx
        node.whiteOverlayAlpha = whiteOverlayAlpha
        node.sync()
        node.invalidateDraw()
    }
}

/**
 * 背景捕获节点：清晰源层保持原始尺寸，模糊/读取层先降采样再处理。
 * Android 12+ 在 GPU 层应用模糊，Android 12- 从同一低分辨率层读取位图。
 */
private class BackgroundCaptureNode(
    var store: BackgroundViewModel,
    var recordContent: Boolean,
    var displayBlurred: Boolean,
    blurRadiusPx: Float,
    var whiteOverlayAlpha: Float,
) : Modifier.Node(), DrawModifierNode, GlobalPositionAwareModifierNode,
    CompositionLocalConsumerModifierNode {
    var blurRadiusPx: Float = blurRadiusPx
        set(value) {
            if (field == value) return
            field = value
        }

    private var graphicsContext: GraphicsContext? = null
    private var sourceLayer: GraphicsLayer? = null
    private var sampledLayer: GraphicsLayer? = null
    private var blurredEffectRadius = 0f
    private var captureJob: Job? = null
    private var sampledBlurRadiusPx = 0.5f

    private var cachedPlan: BlurSamplingPlan? = null
    private var planWidth = -1
    private var planHeight = -1
    private var planRadius = Float.NaN

    override fun onAttach() {
        sync()
    }

    override fun onDetach() {
        captureJob?.cancel()
        captureJob = null
        releaseLayers()
        graphicsContext = null
    }

    override fun onGloballyPositioned(coordinates: LayoutCoordinates) {
        store.updateBackgroundBounds(coordinates)
    }

    override fun ContentDrawScope.draw() {
        val source = sourceLayer
        val sampled = sampledLayer
        val sourceWidth = size.width.roundToInt()
        val sourceHeight = size.height.roundToInt()
        if (recordContent && source != null && sourceWidth > 0 && sourceHeight > 0) {
            if (Build.VERSION.SDK_INT >= 31) {
                if (sampled != null) {
                    //动态内容跟随实际绘制帧更新，不再通过独立限频器丢弃视频/GIF帧。
                    source.record { this@draw.drawContent() }
                    recordSampledLayer(source, sampled, sourceWidth, sourceHeight, upscale = true)
                    if (displayBlurred) drawLayer(sampled) else drawLayer(source)
                } else {
                    drawLayer(source)
                }
            } else {
                source.record { this@draw.drawContent() }
                if (sampled != null) {
                    //低分辨率层直接用于位图快照，杜绝全屏 GPU -> CPU 读取
                    recordSampledLayer(source, sampled, sourceWidth, sourceHeight, upscale = false)
                    if (displayBlurred) {
                        val bitmap = store.blurredBackground
                        if (bitmap != null) {
                            drawImage(
                                image = bitmap,
                                dstSize = IntSize(sourceWidth, sourceHeight)
                            )
                        } else {
                            drawLayer(source)
                        }
                    } else {
                        drawLayer(source)
                    }
                } else {
                    drawLayer(source)
                }
            }
        } else {
            drawContent()
        }
        if (whiteOverlayAlpha > 0f) {
            drawRect(
                color = Color.White.copy(alpha = whiteOverlayAlpha),
                blendMode = BlendMode.Softlight
            )
        }
    }

    private fun ContentDrawScope.recordSampledLayer(
        source: GraphicsLayer,
        target: GraphicsLayer,
        sourceWidth: Int,
        sourceHeight: Int,
        upscale: Boolean
    ) {
        val plan = samplingPlan(sourceWidth, sourceHeight)
        sampledBlurRadiusPx = plan.blurRadiusPx
        target.pivotOffset = Offset.Zero
        target.scaleX = if (upscale) 1f / plan.scaleX else 1f
        target.scaleY = if (upscale) 1f / plan.scaleY else 1f
        if (Build.VERSION.SDK_INT >= 31 && blurredEffectRadius != plan.blurRadiusPx) {
            target.renderEffect = BlurEffect(
                plan.blurRadiusPx,
                plan.blurRadiusPx,
                TileMode.Clamp
            )
            blurredEffectRadius = plan.blurRadiusPx
        }
        target.record(
            density = this,
            layoutDirection = layoutDirection,
            size = IntSize(plan.width, plan.height)
        ) {
            scale(plan.scaleX, plan.scaleY, pivot = Offset.Zero) {
                drawLayer(source)
            }
        }
    }

    private fun samplingPlan(width: Int, height: Int): BlurSamplingPlan {
        cachedPlan?.let { plan ->
            if (planWidth == width && planHeight == height && planRadius == blurRadiusPx) return plan
        }
        return calculateBlurSamplingPlan(
            sourceWidth = width,
            sourceHeight = height,
            blurRadiusPx = blurRadiusPx,
            maxDimension = if (Build.VERSION.SDK_INT >= 31) {
                DYNAMIC_GPU_BLUR_MAX_DIMENSION
            } else {
                DYNAMIC_CPU_BLUR_MAX_DIMENSION
            },
            maxSampledRadius = if (Build.VERSION.SDK_INT >= 31) {
                GPU_MAX_SAMPLED_BLUR_RADIUS
            } else {
                CPU_MAX_SAMPLED_BLUR_RADIUS
            }
        ).also { plan ->
            cachedPlan = plan
            planWidth = width
            planHeight = height
            planRadius = blurRadiusPx
        }
    }

    /** 依据当前参数调和共享层的发布与捕获循环的启停。 */
    fun sync() {
        syncLayers()
        syncCaptureLoop()
    }

    private fun syncLayers() {
        if (recordContent) {
            sourceLayer ?: createLayer()?.also { sourceLayer = it }
            val sampled = ensureSampledLayer()
            if (Build.VERSION.SDK_INT >= 31) {
                store.attachGlassLayer(sampled)
                store.attachCaptureLayer(null)
            } else {
                store.attachCaptureLayer(sampled)
                store.attachGlassLayer(null)
            }
        } else {
            releaseLayers()
        }
    }

    private fun ensureSampledLayer(): GraphicsLayer? {
        sampledLayer?.let { return it }
        return createLayer()?.also { sampledLayer = it }
    }

    private fun createLayer(): GraphicsLayer? {
        //release 必须归还给创建它的同一 context，故在创建时捕获
        val context = currentValueOf(LocalGraphicsContext)
        graphicsContext = context
        return context.createGraphicsLayer()
    }

    private fun syncCaptureLoop() {
        if (recordContent && Build.VERSION.SDK_INT < 31) {
            if (captureJob?.isActive == true) return
            captureJob = coroutineScope.launch {
                while (currentCoroutineContext().isActive) {
                    //与界面帧时钟同步；耗时工作自然跳过中间帧，但不主动降低目标帧率。
                    withFrameNanos { }
                    if (store.isValid && store.captureLayer != null) {
                        store.captureGlassFrame(sampledBlurRadiusPx)
                    }
                }
            }
        } else {
            captureJob?.cancel()
            captureJob = null
        }
    }

    private fun releaseLayers() {
        store.attachGlassLayer(null)
        store.attachCaptureLayer(null)
        val context = graphicsContext
        sourceLayer?.let { context?.releaseGraphicsLayer(it) }
        sourceLayer = null
        sampledLayer?.let { context?.releaseGraphicsLayer(it) }
        sampledLayer = null
        blurredEffectRadius = 0f
        cachedPlan = null
    }
}
