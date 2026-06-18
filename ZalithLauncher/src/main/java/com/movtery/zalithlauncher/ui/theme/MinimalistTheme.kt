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

package com.movtery.zalithlauncher.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * Minimalist monochrome color palette
 * Optimized for accessibility and reduced visual complexity
 */
object MinimalistPalette {
    // Core monochrome grays
    val Black = Color(0xFF000000)
    val DarkGray1 = Color(0xFF1A1A1A)
    val DarkGray2 = Color(0xFF2D2D2D)
    val DarkGray3 = Color(0xFF404040)
    val MediumGray = Color(0xFF595959)
    val LightGray1 = Color(0xFF7F7F7F)
    val LightGray2 = Color(0xFFA6A6A6)
    val LightGray3 = Color(0xFFCCCCCC)
    val VeryLightGray = Color(0xFFE0E0E0)
    val White = Color(0xFFFFFFFF)

    // Semantic colors (minimal, mostly grayscale)
    val Success = Color(0xFF404040)  // Dark gray for success
    val Warning = Color(0xFF595959)  // Medium gray for warning
    val Error = Color(0xFF2D2D2D)    // Dark gray for error
    val Info = Color(0xFF7F7F7F)     // Light gray for info
}

/**
 * Minimalist dark theme with monochrome palette
 */
val MinimalistDarkColorScheme = darkColorScheme(
    primary = MinimalistPalette.LightGray1,
    onPrimary = MinimalistPalette.Black,
    primaryContainer = MinimalistPalette.DarkGray2,
    onPrimaryContainer = MinimalistPalette.LightGray3,
    secondary = MinimalistPalette.MediumGray,
    onSecondary = MinimalistPalette.White,
    secondaryContainer = MinimalistPalette.DarkGray3,
    onSecondaryContainer = MinimalistPalette.LightGray2,
    tertiary = MinimalistPalette.LightGray1,
    onTertiary = MinimalistPalette.Black,
    tertiaryContainer = MinimalistPalette.DarkGray2,
    onTertiaryContainer = MinimalistPalette.LightGray3,
    error = MinimalistPalette.DarkGray1,
    onError = MinimalistPalette.White,
    errorContainer = MinimalistPalette.DarkGray2,
    onErrorContainer = MinimalistPalette.VeryLightGray,
    background = MinimalistPalette.Black,
    onBackground = MinimalistPalette.White,
    surface = MinimalistPalette.DarkGray1,
    onSurface = MinimalistPalette.LightGray3,
    surfaceVariant = MinimalistPalette.DarkGray2,
    onSurfaceVariant = MinimalistPalette.LightGray2,
    outline = MinimalistPalette.MediumGray,
    outlineVariant = MinimalistPalette.DarkGray3,
    scrim = MinimalistPalette.Black
)

@Composable
fun MinimalistTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = MinimalistDarkColorScheme,
        typography = MinimalistTypography,
        shapes = MinimalistShapes,
        content = content
    )
}
