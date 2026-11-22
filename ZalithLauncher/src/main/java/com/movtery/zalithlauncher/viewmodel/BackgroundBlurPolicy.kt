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

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

//小半径优先保留纹理；只有大半径才利用高频信息不可见的特性增加降采样。
internal const val DYNAMIC_GPU_BLUR_MAX_DIMENSION = 1024
internal const val DYNAMIC_CPU_BLUR_MAX_DIMENSION = 512
internal const val GPU_MAX_SAMPLED_BLUR_RADIUS = 20f
internal const val CPU_MAX_SAMPLED_BLUR_RADIUS = 16f

/** 背景内容是否会在没有布局/设置变化时自行产生新帧。 */
internal enum class BackgroundContentKind {
    NONE,
    STATIC_IMAGE,
    ANIMATED_IMAGE,
    VIDEO
}

internal data class BackgroundBlurDecision(
    val enabled: Boolean,
    val cacheStaticImage: Boolean,
    val captureFrames: Boolean
)

/**
 * 静态图片只需要在媒体、区域或模糊参数变化时重新生成；GIF/视频才需要连续捕获。
 */
internal fun resolveBackgroundBlur(
    content: BackgroundContentKind,
    blur: Int,
    opacity: Int
): BackgroundBlurDecision {
    val enabled = content != BackgroundContentKind.NONE && blur > 0 && opacity < 100
    return BackgroundBlurDecision(
        enabled = enabled,
        cacheStaticImage = enabled && content == BackgroundContentKind.STATIC_IMAGE,
        captureFrames = enabled && (content == BackgroundContentKind.ANIMATED_IMAGE || content == BackgroundContentKind.VIDEO)
    )
}

internal data class BackgroundMediaConfig(
    val valid: Boolean,
    val isImage: Boolean,
    val isAnimatedImage: Boolean,
    val generation: Boolean,
    val targetWidth: Int,
    val targetHeight: Int
)

internal data class BackgroundBlurConfig(
    val blur: Int,
    val opacity: Int,
    val valid: Boolean,
    val isImage: Boolean,
    val isAnimatedImage: Boolean,
    val generation: Boolean,
    val targetWidth: Int,
    val targetHeight: Int
)

/**
 * 设置项是普通 StateFlow，必须显式合并；放在 snapshotFlow 中读取不会订阅其变化。
 * 媒体和布局状态则由调用方通过 snapshotFlow 提供。
 */
internal fun combineBackgroundBlurConfig(
    blur: Flow<Int>,
    opacity: Flow<Int>,
    media: Flow<BackgroundMediaConfig>
): Flow<BackgroundBlurConfig> = combine(blur, opacity, media) { blurValue, opacityValue, mediaValue ->
    BackgroundBlurConfig(
        blur = blurValue,
        opacity = opacityValue,
        valid = mediaValue.valid,
        isImage = mediaValue.isImage,
        isAnimatedImage = mediaValue.isAnimatedImage,
        generation = mediaValue.generation,
        targetWidth = mediaValue.targetWidth,
        targetHeight = mediaValue.targetHeight
    )
}

/**
 * 模糊采样的尺寸与半径。层先按 [scaleX]/[scaleY] 录制，再以倒数放大显示。
 * 模糊半径随采样一起缩小，避免视觉上改变用户设置的有效模糊范围。
 */
internal data class BlurSamplingPlan(
    val width: Int,
    val height: Int,
    val scaleX: Float,
    val scaleY: Float,
    val blurRadiusPx: Float
)

internal fun calculateBlurSamplingPlan(
    sourceWidth: Int,
    sourceHeight: Int,
    blurRadiusPx: Float,
    maxDimension: Int,
    maxSampledRadius: Float
): BlurSamplingPlan {
    require(sourceWidth > 0 && sourceHeight > 0) { "Source size must be positive" }
    require(maxDimension > 0) { "maxDimension must be positive" }
    require(maxSampledRadius > 0f) { "maxSampledRadius must be positive" }

    val radius = blurRadiusPx.coerceAtLeast(0.5f)
    val scale = min(
        1f,
        min(
            maxDimension.toFloat() / max(sourceWidth, sourceHeight).toFloat(),
            maxSampledRadius / radius
        )
    )
    val width = (sourceWidth * scale).roundToInt().coerceIn(1, sourceWidth)
    val height = (sourceHeight * scale).roundToInt().coerceIn(1, sourceHeight)
    val scaleX = width.toFloat() / sourceWidth.toFloat()
    val scaleY = height.toFloat() / sourceHeight.toFloat()
    val sampledRadius = (radius * min(scaleX, scaleY)).coerceIn(0.5f, maxSampledRadius)
    return BlurSamplingPlan(width, height, scaleX, scaleY, sampledRadius)
}

internal data class CenterCrop(
    val left: Int,
    val top: Int,
    val width: Int,
    val height: Int
)

/** 计算与 ContentScale.Crop 等价的居中裁剪区域。 */
internal fun calculateCenterCrop(
    sourceWidth: Int,
    sourceHeight: Int,
    targetWidth: Int,
    targetHeight: Int
): CenterCrop {
    require(sourceWidth > 0 && sourceHeight > 0 && targetWidth > 0 && targetHeight > 0) {
        "Source and target sizes must be positive"
    }
    val sourceAspect = sourceWidth.toFloat() / sourceHeight.toFloat()
    val targetAspect = targetWidth.toFloat() / targetHeight.toFloat()
    val cropWidth: Int
    val cropHeight: Int
    if (sourceAspect > targetAspect) {
        cropHeight = sourceHeight
        cropWidth = (sourceHeight * targetAspect).roundToInt().coerceIn(1, sourceWidth)
    } else {
        cropWidth = sourceWidth
        cropHeight = (sourceWidth / targetAspect).roundToInt().coerceIn(1, sourceHeight)
    }
    return CenterCrop(
        left = (sourceWidth - cropWidth) / 2,
        top = (sourceHeight - cropHeight) / 2,
        width = cropWidth,
        height = cropHeight
    )
}
