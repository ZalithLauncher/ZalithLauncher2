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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/gpl-3.0.txt>.
 */

package com.movtery.zalithlauncher.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight

private val systemSans = FontFamily.SansSerif
private val defaults = Typography()

private fun TextStyle.miraiText(weight: FontWeight = fontWeight ?: FontWeight.Normal) = copy(
    fontFamily = systemSans,
    fontWeight = weight
)

/**
 * Compact, clean sans-serif type scale for the Aerix interface. It uses Android's
 * system sans face so text remains crisp and accessible without copying a
 * third-party app's bundled brand assets.
 */
val AppTypography = Typography(
    displayLarge = defaults.displayLarge.miraiText(FontWeight.Bold),
    displayMedium = defaults.displayMedium.miraiText(FontWeight.Bold),
    displaySmall = defaults.displaySmall.miraiText(FontWeight.Bold),
    headlineLarge = defaults.headlineLarge.miraiText(FontWeight.Bold),
    headlineMedium = defaults.headlineMedium.miraiText(FontWeight.SemiBold),
    headlineSmall = defaults.headlineSmall.miraiText(FontWeight.SemiBold),
    titleLarge = defaults.titleLarge.miraiText(FontWeight.SemiBold),
    titleMedium = defaults.titleMedium.miraiText(FontWeight.Medium),
    titleSmall = defaults.titleSmall.miraiText(FontWeight.Medium),
    bodyLarge = defaults.bodyLarge.miraiText(),
    bodyMedium = defaults.bodyMedium.miraiText(),
    bodySmall = defaults.bodySmall.miraiText(),
    labelLarge = defaults.labelLarge.miraiText(FontWeight.Medium),
    labelMedium = defaults.labelMedium.miraiText(FontWeight.Medium),
    labelSmall = defaults.labelSmall.miraiText(FontWeight.Medium),
)
