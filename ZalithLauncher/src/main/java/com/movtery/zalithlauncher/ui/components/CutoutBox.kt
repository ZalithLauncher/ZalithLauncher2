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

package com.movtery.zalithlauncher.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp

private object CutoutContentSlot

private object CutoutOverlaySlot

/**
 * 保存上方覆盖组件相对于 [CutoutBox] 的裁切区域。
 * 在 SubcomposeLayout 的测量阶段写入，绘制阶段读取，因此不需要触发重组。
 */
private class CutoutAreaHolder {
    var area: Rect? = null
}

/**
 * 使用上方覆盖组件的边界反向裁切下方内容：
 * 下方内容保留“整体区域 - 圆角矩形区域”的路径。
 */
private class DeferredCutoutShape(
    private val areaHolder: CutoutAreaHolder,
    private val radius: Dp
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val cutout = areaHolder.area ?: return Outline.Rectangle(Rect(0f, 0f, size.width, size.height))

        val radiusPx = with(density) {
            radius.coerceAtLeast(0.dp).toPx()
        }
        val cornerRadius = CornerRadius(
            radiusPx.coerceAtMost(minOf(cutout.width, cutout.height) / 2f)
        )
        val cutoutPath = Path().apply {
            addRoundRect(RoundRect(rect = cutout, cornerRadius = cornerRadius))
        }
        val contentPath = Path().apply {
            addRect(Rect(0f, 0f, size.width, size.height))
            op(this, cutoutPath, PathOperation.Difference)
        }
        return Outline.Generic(contentPath)
    }
}

/**
 * 自动裁切 Box
 *
 * [content] 是下层组件，会被绘制为底层，并根据 [overlay] 的边界裁切出一个圆角缺口；
 * [overlay] 是上层组件，不会被裁切。两者都可以像普通 [Box] 一样在内部使用
 * `Modifier.align`，[overlayAlignment] 则控制整个覆盖层在当前组件中的位置。
 *
 * @param cutoutRadius 裁切缺口的圆角半径。
 */
@Composable
fun CutoutBox(
    modifier: Modifier = Modifier,
    cutoutRadius: Dp = 16.dp,
    overlayAlignment: Alignment = Alignment.TopCenter,
    overlay: @Composable BoxScope.() -> Unit,
    content: @Composable BoxScope.() -> Unit
) {
    val areaHolder = remember { CutoutAreaHolder() }
    val shape = remember(areaHolder, cutoutRadius) {
        DeferredCutoutShape(areaHolder, cutoutRadius)
    }

    SubcomposeLayout(modifier = modifier) { constraints ->
        val looseConstraints = constraints.copy(minWidth = 0, minHeight = 0)

        val contentMeasurables = subcompose(CutoutContentSlot) {
            Box(modifier = Modifier.clip(shape = shape), content = content)
        }

        val overlayMeasurables = subcompose(CutoutOverlaySlot) {
            Box(content = overlay)
        }

        val measuredContent = contentMeasurables.single().measure(looseConstraints)
        val measuredOverlay = overlayMeasurables.single().measure(looseConstraints)

        val width = maxOf(measuredContent.width, measuredOverlay.width)
            .coerceIn(constraints.minWidth, constraints.maxWidth)
        val height = maxOf(measuredContent.height, measuredOverlay.height)
            .coerceIn(constraints.minHeight, constraints.maxHeight)

        val content = contentMeasurables.single().measure(Constraints.fixed(width, height))

        val overlayOffset = overlayAlignment.align(
            size = IntSize(measuredOverlay.width, measuredOverlay.height),
            space = IntSize(width, height),
            layoutDirection = layoutDirection
        )

        areaHolder.area = Rect(
            left = overlayOffset.x.toFloat(),
            top = overlayOffset.y.toFloat(),
            right = (overlayOffset.x + measuredOverlay.width).toFloat(),
            bottom = (overlayOffset.y + measuredOverlay.height).toFloat()
        )

        layout(width = width, height = height) {
            content.place(IntOffset.Zero)
            measuredOverlay.placeRelative(x = overlayOffset.x, y = overlayOffset.y)
        }
    }
}