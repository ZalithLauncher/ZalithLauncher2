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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Screen density breakpoints
 */
data class CompactModeThresholds(
    val phoneSmall: Dp = 360.dp,
    val phoneMedium: Dp = 480.dp,
    val phoneLarge: Dp = 600.dp,
    val tablet: Dp = 800.dp
)

/**
 * Responsive spacing for compact mode
 */
data class CompactSpacing(
    val extraSmall: Dp = 2.dp,
    val small: Dp = 4.dp,
    val medium: Dp = 8.dp,
    val large: Dp = 12.dp,
    val extraLarge: Dp = 16.dp
)

val LocalCompactMode = compositionLocalOf { false }
val LocalCompactSpacing = compositionLocalOf { CompactSpacing() }

/**
 * Responsive padding function
 */
fun getResponsivePadding(compact: Boolean): CompactSpacing {
    return if (compact) {
        CompactSpacing(
            extraSmall = 1.dp,
            small = 2.dp,
            medium = 4.dp,
            large = 6.dp,
            extraLarge = 8.dp
        )
    } else {
        CompactSpacing()
    }
}
