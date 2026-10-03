package com.movtery.guide

import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

/**
 * 引导层配色
 */
@Immutable
data class GuideColors(
    /**
     * 遮罩颜色
     */
    val scrim: Color = Color.Black.copy(alpha = 0.5f),

    /**
     * 引导内容的默认颜色
     * @see LocalContentColor
     */
    val content: Color = Color.White
)