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

package com.movtery.layer_controller.utils

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.AnimationVector4D
import androidx.compose.animation.core.TwoWayConverter
import androidx.compose.animation.core.animateValueAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * 在RoundedCornerShape之间创建动画
 */
@Composable
fun animateShapeAsState(
    targetShape: RoundedCornerShape,
    animationSpec: AnimationSpec<RoundedCornerShape> = spring(),
    label: String = "RoundedCornerShapeAnimation"
): State<RoundedCornerShape> {
    val density = LocalDensity.current
    val converter = remember(density) {
        RoundedCornerShapeConverter(density)
    }

    return animateValueAsState(
        targetValue = targetShape,
        typeConverter = converter,
        animationSpec = animationSpec,
        label = label
    )
}

/**
 * 简化的动画函数，支持从当前值到目标圆角的动画
 */
@Composable
fun animateShapeAsState(
    targetCornerSize: Dp,
    animationSpec: AnimationSpec<RoundedCornerShape> = spring(),
    label: String = "RoundedCornerShapeAnimation"
): State<RoundedCornerShape> {
    val targetShape = remember(targetCornerSize) {
        RoundedCornerShape(targetCornerSize)
    }
    return animateShapeAsState(targetShape, animationSpec, label)
}

/**
 * 简化的动画函数，支持独立控制四个角的圆角动画
 */
@Composable
fun animateShapeAsState(
    topStart: Dp = 0.dp,
    topEnd: Dp = 0.dp,
    bottomEnd: Dp = 0.dp,
    bottomStart: Dp = 0.dp,
    animationSpec: AnimationSpec<RoundedCornerShape> = spring(),
    label: String = "RoundedCornerShapeAnimation"
): State<RoundedCornerShape> {
    val targetShape = remember(topStart, topEnd, bottomEnd, bottomStart) {
        RoundedCornerShape(topStart, topEnd, bottomEnd, bottomStart)
    }
    return animateShapeAsState(targetShape, animationSpec, label)
}

private class RoundedCornerShapeConverter(
    private val density: Density,
) : TwoWayConverter<RoundedCornerShape, AnimationVector4D> {
    override val convertFromVector: (AnimationVector4D) -> RoundedCornerShape = { vector ->
        RoundedCornerShape(
            topStart = vector.v1.pxToDp(density),
            topEnd = vector.v2.pxToDp(density),
            bottomEnd = vector.v3.pxToDp(density),
            bottomStart = vector.v4.pxToDp(density)
        )
    }

    override val convertToVector: (RoundedCornerShape) -> AnimationVector4D = { shape ->
        AnimationVector4D(
            v1 = shape.topStart.toPx(Size.Zero, density),
            v2 = shape.topEnd.toPx(Size.Zero, density),
            v3 = shape.bottomEnd.toPx(Size.Zero, density),
            v4 = shape.bottomStart.toPx(Size.Zero, density)
        )
    }
}

private fun Float.pxToDp(density: Density): Dp {
    return with(density) { this@pxToDp.toDp() }
}
