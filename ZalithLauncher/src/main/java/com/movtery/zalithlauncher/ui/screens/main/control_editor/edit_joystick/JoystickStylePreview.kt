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

package com.movtery.zalithlauncher.ui.screens.main.control_editor.edit_joystick

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.LayoutDirection
import com.movtery.layer_controller.observable.ObservableJoystickStyle
import com.movtery.layer_controller.observable.ObservableJoystickStyleConfig

private fun Color.applyAlpha(multiplier: Float): Color {
    val a = (this.alpha * multiplier).coerceIn(0f, 1f)
    return this.copy(alpha = a)
}

/**
 * 决定使用哪个主题配置
 */
fun resolveThemeConfig(
    style: ObservableJoystickStyle,
    isDark: Boolean
): ObservableJoystickStyleConfig {
    return if (style.commonStyle || !isDark) {
        style.lightStyle
    } else {
        style.darkStyle
    }
}

private fun percentToShape(percent: Int): Shape {
    return RoundedCornerShape(percent = percent)
}

private fun DrawScope.drawBackgroundLayer(
    layoutDirection: LayoutDirection,
    size: Size,
    shape: Shape,
    backgroundColor: Color,
    borderColor: Color,
    borderWidthPx: Float
) {
    val outline = shape.createOutline(size, layoutDirection, density = this)
    val clipPath = outlineToPath(outline)

    clipPath(clipPath) {
        drawOutline(outline = outline, color = backgroundColor)
        if (borderWidthPx > 0f) {
            drawOutline(outline = outline, color = borderColor, style = Stroke(width = borderWidthPx))
        }
    }
}

private fun DrawScope.drawJoystick(
    layoutDirection: LayoutDirection,
    color: Color,
    center: Offset,
    size: Float,
    shape: Shape
) {
    val halfSize = size / 2
    val topLeftX = center.x - halfSize
    val topLeftY = center.y - halfSize

    val outline = shape.createOutline(Size(size, size), layoutDirection, density = this)

    withTransform({
        translate(left = topLeftX, top = topLeftY)
    }) {
        drawOutline(outline = outline, color = color)
    }
}

private fun outlineToPath(outline: Outline): Path {
    return when (outline) {
        is Outline.Generic -> outline.path
        is Outline.Rounded -> Path().apply { addRoundRect(outline.roundRect) }
        is Outline.Rectangle -> Path().apply { addRect(outline.rect) }
    }
}

/**
 * 简单的摇杆样式预览渲染，不包含任何交互逻辑
 */
@Composable
fun JoystickStylePreview(
    config: ObservableJoystickStyleConfig,
    modifier: Modifier = Modifier,
) {
    val alpha = config.alpha
    val bgShape = remember(config.backgroundShape) { percentToShape(config.backgroundShape) }
    val jsShape = remember(config.joystickShape) { percentToShape(config.joystickShape) }
    val borderRatio = remember(config.borderWidthRatio) {
        (config.borderWidthRatio.toFloat() / 100f).coerceIn(0.0f, 0.5f)
    }
    val jsSizeRatio = config.joystickSize

    val backgroundColor = remember(config.backgroundColor, alpha) { config.backgroundColor.applyAlpha(alpha) }
    val borderColor = remember(config.borderColor, alpha) { config.borderColor.applyAlpha(alpha) }
    val joystickColor = remember(config.joystickColor, alpha) { config.joystickColor.applyAlpha(alpha) }

    val layoutDirection = LocalDensity.current.let { LayoutDirection.Ltr }

    Canvas(modifier = modifier.fillMaxSize()) {
        val canvasSize = this.size
        val minSide = minOf(canvasSize.width, canvasSize.height)

        // 背景层
        drawBackgroundLayer(
            layoutDirection = layoutDirection,
            size = canvasSize,
            shape = bgShape,
            backgroundColor = backgroundColor,
            borderColor = borderColor,
            borderWidthPx = (minSide * borderRatio).coerceAtLeast(0f)
        )

        // 摇杆头
        val joystickSizePx = minSide * jsSizeRatio
        val center = Offset(canvasSize.width / 2, canvasSize.height / 2)
        drawJoystick(
            layoutDirection = layoutDirection,
            color = joystickColor,
            center = center,
            size = joystickSizePx,
            shape = jsShape
        )
    }
}
