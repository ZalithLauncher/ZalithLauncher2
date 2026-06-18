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
 * along with this program. If not, see <https://www.gnu.org/licenses/gpl-3.0.txt>.
 */

package com.movtery.zalithlauncher.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Get adaptive padding based on screen size
 */
@Composable
fun getAdaptivePadding(windowSizeClass: WindowSizeClass): PaddingValues {
    return when (windowSizeClass.widthSizeClass) {
        WindowWidthSizeClass.Compact -> PaddingValues(
            horizontal = 4.dp,
            vertical = 4.dp
        )
        WindowWidthSizeClass.Medium -> PaddingValues(
            horizontal = 8.dp,
            vertical = 6.dp
        )
        else -> PaddingValues(
            horizontal = 12.dp,
            vertical = 8.dp
        )
    }
}

/**
 * Get adaptive spacing value
 */
fun getAdaptiveSpacing(windowSizeClass: WindowSizeClass): Dp {
    return when (windowSizeClass.widthSizeClass) {
        WindowWidthSizeClass.Compact -> 4.dp
        WindowWidthSizeClass.Medium -> 6.dp
        else -> 8.dp
    }
}
