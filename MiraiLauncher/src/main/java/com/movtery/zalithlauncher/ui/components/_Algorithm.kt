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

package com.movtery.zalithlauncher.ui.components

import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize

/**
 * 根据屏幕高度，以特定比例计算最大高度
 */
@Composable
fun rememberMaxHeight(fraction: Float = 3f / 5f): Dp {
    val windowInfo = LocalWindowInfo.current
    val density = LocalDensity.current
    val heightPx = windowInfo.containerSize.height
    return remember(heightPx, density, fraction) {
        with(density) {
            (heightPx * fraction).toDp()
        }
    }
}

/**
 * 根据屏幕宽度，以特定比例计算最大宽度
 */
@Composable
fun rememberMaxWidth(fraction: Float = 3f / 4f): Dp {
    val windowInfo = LocalWindowInfo.current
    val density = LocalDensity.current
    val widthPx = windowInfo.containerSize.width
    return remember(widthPx, density, fraction) {
        with(density) {
            (widthPx * fraction).toDp()
        }
    }
}

@Composable
fun BoxWithConstraintsScope.rememberBoxSize(): IntSize {
    val density = LocalDensity.current
    return remember(maxWidth, maxHeight) {
        with(density) {
            IntSize(
                width = maxWidth.roundToPx(),
                height = maxHeight.roundToPx()
            )
        }
    }
}