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

package com.movtery.zalithlauncher.ui.theme

import android.os.Build
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.materialkolor.PaletteStyle
import com.materialkolor.dynamicColorScheme
import com.movtery.zalithlauncher.setting.AllSettings
import com.movtery.zalithlauncher.setting.enums.isLauncherInDarkTheme
import com.movtery.zalithlauncher.ui.theme.components.activeMaskView
import com.movtery.zalithlauncher.utils.festival.Festival
import com.movtery.zalithlauncher.utils.festival.LocalFestivals
import com.movtery.zalithlauncher.viewmodel.BackgroundViewModel
import com.movtery.zalithlauncher.viewmodel.LocalBackgroundViewModel

private val embermireLight = lightColorScheme(
    primary = primaryLight.embermire,
    onPrimary = onPrimaryLight.embermire,
    primaryContainer = primaryContainerLight.embermire,
    onPrimaryContainer = onPrimaryContainerLight.embermire,
    secondary = secondaryLight.embermire,
    onSecondary = onSecondaryLight.embermire,
    secondaryContainer = secondaryContainerLight.embermire,
    onSecondaryContainer = onSecondaryContainerLight.embermire,
    tertiary = tertiaryLight.embermire,
    onTertiary = onTertiaryLight.embermire,
    tertiaryContainer = tertiaryContainerLight.embermire,
    onTertiaryContainer = onTertiaryContainerLight.embermire,
    error = errorLight.embermire,
    onError = onErrorLight.embermire,
    errorContainer = errorContainerLight.embermire,
    onErrorContainer = onErrorContainerLight.embermire,
    background = backgroundLight.embermire,
    onBackground = onBackgroundLight.embermire,
    surface = surfaceLight.embermire,
    onSurface = onSurfaceLight.embermire,
    surfaceVariant = surfaceVariantLight.embermire,
    onSurfaceVariant = onSurfaceVariantLight.embermire,
    outline = outlineLight.embermire,
    outlineVariant = outlineVariantLight.embermire,
    scrim = scrimLight.embermire,
    inverseSurface = inverseSurfaceLight.embermire,
    inverseOnSurface = inverseOnSurfaceLight.embermire,
    inversePrimary = inversePrimaryLight.embermire,
    surfaceDim = surfaceDimLight.embermire,
    surfaceBright = surfaceBrightLight.embermire,
    surfaceContainerLowest = surfaceContainerLowestLight.embermire,
    surfaceContainerLow = surfaceContainerLowLight.embermire,
    surfaceContainer = surfaceContainerLight.embermire,
    surfaceContainerHigh = surfaceContainerHighLight.embermire,
    surfaceContainerHighest = surfaceContainerHighestLight.embermire,
)

private val embermireDark = darkColorScheme(
    primary = primaryDark.embermire,
    onPrimary = onPrimaryDark.embermire,
    primaryContainer = primaryContainerDark.embermire,
    onPrimaryContainer = onPrimaryContainerDark.embermire,
    secondary = secondaryDark.embermire,
    onSecondary = onSecondaryDark.embermire,
    secondaryContainer = secondaryContainerDark.embermire,
    onSecondaryContainer = onSecondaryContainerDark.embermire,
    tertiary = tertiaryDark.embermire,
    onTertiary = onTertiaryDark.embermire,
    tertiaryContainer = tertiaryContainerDark.embermire,
    onTertiaryContainer = onTertiaryContainerDark.embermire,
    error = errorDark.embermire,
    onError = onErrorDark.embermire,
    errorContainer = errorContainerDark.embermire,
    onErrorContainer = onErrorContainerDark.embermire,
    background = backgroundDark.embermire,
    onBackground = onBackgroundDark.embermire,
    surface = surfaceDark.embermire,
    onSurface = onSurfaceDark.embermire,
    surfaceVariant = surfaceVariantDark.embermire,
    onSurfaceVariant = onSurfaceVariantDark.embermire,
    outline = outlineDark.embermire,
    outlineVariant = outlineVariantDark.embermire,
    scrim = scrimDark.embermire,
    inverseSurface = inverseSurfaceDark.embermire,
    inverseOnSurface = inverseOnSurfaceDark.embermire,
    inversePrimary = inversePrimaryDark.embermire,
    surfaceDim = surfaceDimDark.embermire,
    surfaceBright = surfaceBrightDark.embermire,
    surfaceContainerLowest = surfaceContainerLowestDark.embermire,
    surfaceContainerLow = surfaceContainerLowDark.embermire,
    surfaceContainer = surfaceContainerDark.embermire,
    surfaceContainerHigh = surfaceContainerHighDark.embermire,
    surfaceContainerHighest = surfaceContainerHighestDark.embermire,
)

private val velvetRoseLight = lightColorScheme(
    primary = primaryLight.velvetRose,
    onPrimary = onPrimaryLight.velvetRose,
    primaryContainer = primaryContainerLight.velvetRose,
    onPrimaryContainer = onPrimaryContainerLight.velvetRose,
    secondary = secondaryLight.velvetRose,
    onSecondary = onSecondaryLight.velvetRose,
    secondaryContainer = secondaryContainerLight.velvetRose,
    onSecondaryContainer = onSecondaryContainerLight.velvetRose,
    tertiary = tertiaryLight.velvetRose,
    onTertiary = onTertiaryLight.velvetRose,
    tertiaryContainer = tertiaryContainerLight.velvetRose,
    onTertiaryContainer = onTertiaryContainerLight.velvetRose,
    error = errorLight.velvetRose,
    onError = onErrorLight.velvetRose,
    errorContainer = errorContainerLight.velvetRose,
    onErrorContainer = onErrorContainerLight.velvetRose,
    background = backgroundLight.velvetRose,
    onBackground = onBackgroundLight.velvetRose,
    surface = surfaceLight.velvetRose,
    onSurface = onSurfaceLight.velvetRose,
    surfaceVariant = surfaceVariantLight.velvetRose,
    onSurfaceVariant = onSurfaceVariantLight.velvetRose,
    outline = outlineLight.velvetRose,
    outlineVariant = outlineVariantLight.velvetRose,
    scrim = scrimLight.velvetRose,
    inverseSurface = inverseSurfaceLight.velvetRose,
    inverseOnSurface = inverseOnSurfaceLight.velvetRose,
    inversePrimary = inversePrimaryLight.velvetRose,
    surfaceDim = surfaceDimLight.velvetRose,
    surfaceBright = surfaceBrightLight.velvetRose,
    surfaceContainerLowest = surfaceContainerLowestLight.velvetRose,
    surfaceContainerLow = surfaceContainerLowLight.velvetRose,
    surfaceContainer = surfaceContainerLight.velvetRose,
    surfaceContainerHigh = surfaceContainerHighLight.velvetRose,
    surfaceContainerHighest = surfaceContainerHighestLight.velvetRose,
)

private val velvetRoseDark = darkColorScheme(
    primary = primaryDark.velvetRose,
    onPrimary = onPrimaryDark.velvetRose,
    primaryContainer = primaryContainerDark.velvetRose,
    onPrimaryContainer = onPrimaryContainerDark.velvetRose,
    secondary = secondaryDark.velvetRose,
    onSecondary = onSecondaryDark.velvetRose,
    secondaryContainer = secondaryContainerDark.velvetRose,
    onSecondaryContainer = onSecondaryContainerDark.velvetRose,
    tertiary = tertiaryDark.velvetRose,
    onTertiary = onTertiaryDark.velvetRose,
    tertiaryContainer = tertiaryContainerDark.velvetRose,
    onTertiaryContainer = onTertiaryContainerDark.velvetRose,
    error = errorDark.velvetRose,
    onError = onErrorDark.velvetRose,
    errorContainer = errorContainerDark.velvetRose,
    onErrorContainer = onErrorContainerDark.velvetRose,
    background = backgroundDark.velvetRose,
    onBackground = onBackgroundDark.velvetRose,
    surface = surfaceDark.velvetRose,
    onSurface = onSurfaceDark.velvetRose,
    surfaceVariant = surfaceVariantDark.velvetRose,
    onSurfaceVariant = onSurfaceVariantDark.velvetRose,
    outline = outlineDark.velvetRose,
    outlineVariant = outlineVariantDark.velvetRose,
    scrim = scrimDark.velvetRose,
    inverseSurface = inverseSurfaceDark.velvetRose,
    inverseOnSurface = inverseOnSurfaceDark.velvetRose,
    inversePrimary = inversePrimaryDark.velvetRose,
    surfaceDim = surfaceDimDark.velvetRose,
    surfaceBright = surfaceBrightDark.velvetRose,
    surfaceContainerLowest = surfaceContainerLowestDark.velvetRose,
    surfaceContainerLow = surfaceContainerLowDark.velvetRose,
    surfaceContainer = surfaceContainerDark.velvetRose,
    surfaceContainerHigh = surfaceContainerHighDark.velvetRose,
    surfaceContainerHighest = surfaceContainerHighestDark.velvetRose,
)

private val mistwaveLight = lightColorScheme(
    primary = primaryLight.mistwave,
    onPrimary = onPrimaryLight.mistwave,
    primaryContainer = primaryContainerLight.mistwave,
    onPrimaryContainer = onPrimaryContainerLight.mistwave,
    secondary = secondaryLight.mistwave,
    onSecondary = onSecondaryLight.mistwave,
    secondaryContainer = secondaryContainerLight.mistwave,
    onSecondaryContainer = onSecondaryContainerLight.mistwave,
    tertiary = tertiaryLight.mistwave,
    onTertiary = onTertiaryLight.mistwave,
    tertiaryContainer = tertiaryContainerLight.mistwave,
    onTertiaryContainer = onTertiaryContainerLight.mistwave,
    error = errorLight.mistwave,
    onError = onErrorLight.mistwave,
    errorContainer = errorContainerLight.mistwave,
    onErrorContainer = onErrorContainerLight.mistwave,
    background = backgroundLight.mistwave,
    onBackground = onBackgroundLight.mistwave,
    surface = surfaceLight.mistwave,
    onSurface = onSurfaceLight.mistwave,
    surfaceVariant = surfaceVariantLight.mistwave,
    onSurfaceVariant = onSurfaceVariantLight.mistwave,
    outline = outlineLight.mistwave,
    outlineVariant = outlineVariantLight.mistwave,
    scrim = scrimLight.mistwave,
    inverseSurface = inverseSurfaceLight.mistwave,
    inverseOnSurface = inverseOnSurfaceLight.mistwave,
    inversePrimary = inversePrimaryLight.mistwave,
    surfaceDim = surfaceDimLight.mistwave,
    surfaceBright = surfaceBrightLight.mistwave,
    surfaceContainerLowest = surfaceContainerLowestLight.mistwave,
    surfaceContainerLow = surfaceContainerLowLight.mistwave,
    surfaceContainer = surfaceContainerLight.mistwave,
    surfaceContainerHigh = surfaceContainerHighLight.mistwave,
    surfaceContainerHighest = surfaceContainerHighestLight.mistwave,
)

private val mistwaveDark = darkColorScheme(
    primary = primaryDark.mistwave,
    onPrimary = onPrimaryDark.mistwave,
    primaryContainer = primaryContainerDark.mistwave,
    onPrimaryContainer = onPrimaryContainerDark.mistwave,
    secondary = secondaryDark.mistwave,
    onSecondary = onSecondaryDark.mistwave,
    secondaryContainer = secondaryContainerDark.mistwave,
    onSecondaryContainer = onSecondaryContainerDark.mistwave,
    tertiary = tertiaryDark.mistwave,
    onTertiary = onTertiaryDark.mistwave,
    tertiaryContainer = tertiaryContainerDark.mistwave,
    onTertiaryContainer = onTertiaryContainerDark.mistwave,
    error = errorDark.mistwave,
    onError = onErrorDark.mistwave,
    errorContainer = errorContainerDark.mistwave,
    onErrorContainer = onErrorContainerDark.mistwave,
    background = backgroundDark.mistwave,
    onBackground = onBackgroundDark.mistwave,
    surface = surfaceDark.mistwave,
    onSurface = onSurfaceDark.mistwave,
    surfaceVariant = surfaceVariantDark.mistwave,
    onSurfaceVariant = onSurfaceVariantDark.mistwave,
    outline = outlineDark.mistwave,
    outlineVariant = outlineVariantDark.mistwave,
    scrim = scrimDark.mistwave,
    inverseSurface = inverseSurfaceDark.mistwave,
    inverseOnSurface = inverseOnSurfaceDark.mistwave,
    inversePrimary = inversePrimaryDark.mistwave,
    surfaceDim = surfaceDimDark.mistwave,
    surfaceBright = surfaceBrightDark.mistwave,
    surfaceContainerLowest = surfaceContainerLowestDark.mistwave,
    surfaceContainerLow = surfaceContainerLowDark.mistwave,
    surfaceContainer = surfaceContainerDark.mistwave,
    surfaceContainerHigh = surfaceContainerHighDark.mistwave,
    surfaceContainerHighest = surfaceContainerHighestDark.mistwave,
)

private val glacierLight = lightColorScheme(
    primary = primaryLight.glacier,
    onPrimary = onPrimaryLight.glacier,
    primaryContainer = primaryContainerLight.glacier,
    onPrimaryContainer = onPrimaryContainerLight.glacier,
    secondary = secondaryLight.glacier,
    onSecondary = onSecondaryLight.glacier,
    secondaryContainer = secondaryContainerLight.glacier,
    onSecondaryContainer = onSecondaryContainerLight.glacier,
    tertiary = tertiaryLight.glacier,
    onTertiary = onTertiaryLight.glacier,
    tertiaryContainer = tertiaryContainerLight.glacier,
    onTertiaryContainer = onTertiaryContainerLight.glacier,
    error = errorLight.glacier,
    onError = onErrorLight.glacier,
    errorContainer = errorContainerLight.glacier,
    onErrorContainer = onErrorContainerLight.glacier,
    background = backgroundLight.glacier,
    onBackground = onBackgroundLight.glacier,
    surface = surfaceLight.glacier,
    onSurface = onSurfaceLight.glacier,
    surfaceVariant = surfaceVariantLight.glacier,
    onSurfaceVariant = onSurfaceVariantLight.glacier,
    outline = outlineLight.glacier,
    outlineVariant = outlineVariantLight.glacier,
    scrim = scrimLight.glacier,
    inverseSurface = inverseSurfaceLight.glacier,
    inverseOnSurface = inverseOnSurfaceLight.glacier,
    inversePrimary = inversePrimaryLight.glacier,
    surfaceDim = surfaceDimLight.glacier,
    surfaceBright = surfaceBrightLight.glacier,
    surfaceContainerLowest = surfaceContainerLowestLight.glacier,
    surfaceContainerLow = surfaceContainerLowLight.glacier,
    surfaceContainer = surfaceContainerLight.glacier,
    surfaceContainerHigh = surfaceContainerHighLight.glacier,
    surfaceContainerHighest = surfaceContainerHighestLight.glacier,
)

private val glacierDark = darkColorScheme(
    primary = primaryDark.glacier,
    onPrimary = onPrimaryDark.glacier,
    primaryContainer = primaryContainerDark.glacier,
    onPrimaryContainer = onPrimaryContainerDark.glacier,
    secondary = secondaryDark.glacier,
    onSecondary = onSecondaryDark.glacier,
    secondaryContainer = secondaryContainerDark.glacier,
    onSecondaryContainer = onSecondaryContainerDark.glacier,
    tertiary = tertiaryDark.glacier,
    onTertiary = onTertiaryDark.glacier,
    tertiaryContainer = tertiaryContainerDark.glacier,
    onTertiaryContainer = onTertiaryContainerDark.glacier,
    error = errorDark.glacier,
    onError = onErrorDark.glacier,
    errorContainer = errorContainerDark.glacier,
    onErrorContainer = onErrorContainerDark.glacier,
    background = backgroundDark.glacier,
    onBackground = onBackgroundDark.glacier,
    surface = surfaceDark.glacier,
    onSurface = onSurfaceDark.glacier,
    surfaceVariant = surfaceVariantDark.glacier,
    onSurfaceVariant = onSurfaceVariantDark.glacier,
    outline = outlineDark.glacier,
    outlineVariant = outlineVariantDark.glacier,
    scrim = scrimDark.glacier,
    inverseSurface = inverseSurfaceDark.glacier,
    inverseOnSurface = inverseOnSurfaceDark.glacier,
    inversePrimary = inversePrimaryDark.glacier,
    surfaceDim = surfaceDimDark.glacier,
    surfaceBright = surfaceBrightDark.glacier,
    surfaceContainerLowest = surfaceContainerLowestDark.glacier,
    surfaceContainerLow = surfaceContainerLowDark.glacier,
    surfaceContainer = surfaceContainerDark.glacier,
    surfaceContainerHigh = surfaceContainerHighDark.glacier,
    surfaceContainerHighest = surfaceContainerHighestDark.glacier,
)

private val verdantFieldLight = lightColorScheme(
    primary = primaryLight.verdantField,
    onPrimary = onPrimaryLight.verdantField,
    primaryContainer = primaryContainerLight.verdantField,
    onPrimaryContainer = onPrimaryContainerLight.verdantField,
    secondary = secondaryLight.verdantField,
    onSecondary = onSecondaryLight.verdantField,
    secondaryContainer = secondaryContainerLight.verdantField,
    onSecondaryContainer = onSecondaryContainerLight.verdantField,
    tertiary = tertiaryLight.verdantField,
    onTertiary = onTertiaryLight.verdantField,
    tertiaryContainer = tertiaryContainerLight.verdantField,
    onTertiaryContainer = onTertiaryContainerLight.verdantField,
    error = errorLight.verdantField,
    onError = onErrorLight.verdantField,
    errorContainer = errorContainerLight.verdantField,
    onErrorContainer = onErrorContainerLight.verdantField,
    background = backgroundLight.verdantField,
    onBackground = onBackgroundLight.verdantField,
    surface = surfaceLight.verdantField,
    onSurface = onSurfaceLight.verdantField,
    surfaceVariant = surfaceVariantLight.verdantField,
    onSurfaceVariant = onSurfaceVariantLight.verdantField,
    outline = outlineLight.verdantField,
    outlineVariant = outlineVariantLight.verdantField,
    scrim = scrimLight.verdantField,
    inverseSurface = inverseSurfaceLight.verdantField,
    inverseOnSurface = inverseOnSurfaceLight.verdantField,
    inversePrimary = inversePrimaryLight.verdantField,
    surfaceDim = surfaceDimLight.verdantField,
    surfaceBright = surfaceBrightLight.verdantField,
    surfaceContainerLowest = surfaceContainerLowestLight.verdantField,
    surfaceContainerLow = surfaceContainerLowLight.verdantField,
    surfaceContainer = surfaceContainerLight.verdantField,
    surfaceContainerHigh = surfaceContainerHighLight.verdantField,
    surfaceContainerHighest = surfaceContainerHighestLight.verdantField,
)

private val verdantFieldDark = darkColorScheme(
    primary = primaryDark.verdantField,
    onPrimary = onPrimaryDark.verdantField,
    primaryContainer = primaryContainerDark.verdantField,
    onPrimaryContainer = onPrimaryContainerDark.verdantField,
    secondary = secondaryDark.verdantField,
    onSecondary = onSecondaryDark.verdantField,
    secondaryContainer = secondaryContainerDark.verdantField,
    onSecondaryContainer = onSecondaryContainerDark.verdantField,
    tertiary = tertiaryDark.verdantField,
    onTertiary = onTertiaryDark.verdantField,
    tertiaryContainer = tertiaryContainerDark.verdantField,
    onTertiaryContainer = onTertiaryContainerDark.verdantField,
    error = errorDark.verdantField,
    onError = onErrorDark.verdantField,
    errorContainer = errorContainerDark.verdantField,
    onErrorContainer = onErrorContainerDark.verdantField,
    background = backgroundDark.verdantField,
    onBackground = onBackgroundDark.verdantField,
    surface = surfaceDark.verdantField,
    onSurface = onSurfaceDark.verdantField,
    surfaceVariant = surfaceVariantDark.verdantField,
    onSurfaceVariant = onSurfaceVariantDark.verdantField,
    outline = outlineDark.verdantField,
    outlineVariant = outlineVariantDark.verdantField,
    scrim = scrimDark.verdantField,
    inverseSurface = inverseSurfaceDark.verdantField,
    inverseOnSurface = inverseOnSurfaceDark.verdantField,
    inversePrimary = inversePrimaryDark.verdantField,
    surfaceDim = surfaceDimDark.verdantField,
    surfaceBright = surfaceBrightDark.verdantField,
    surfaceContainerLowest = surfaceContainerLowestDark.verdantField,
    surfaceContainerLow = surfaceContainerLowDark.verdantField,
    surfaceContainer = surfaceContainerDark.verdantField,
    surfaceContainerHigh = surfaceContainerHighDark.verdantField,
    surfaceContainerHighest = surfaceContainerHighestDark.verdantField,
)

private val urbanAshLight = lightColorScheme(
    primary = primaryLight.urbanAsh,
    onPrimary = onPrimaryLight.urbanAsh,
    primaryContainer = primaryContainerLight.urbanAsh,
    onPrimaryContainer = onPrimaryContainerLight.urbanAsh,
    secondary = secondaryLight.urbanAsh,
    onSecondary = onSecondaryLight.urbanAsh,
    secondaryContainer = secondaryContainerLight.urbanAsh,
    onSecondaryContainer = onSecondaryContainerLight.urbanAsh,
    tertiary = tertiaryLight.urbanAsh,
    onTertiary = onTertiaryLight.urbanAsh,
    tertiaryContainer = tertiaryContainerLight.urbanAsh,
    onTertiaryContainer = onTertiaryContainerLight.urbanAsh,
    error = errorLight.urbanAsh,
    onError = onErrorLight.urbanAsh,
    errorContainer = errorContainerLight.urbanAsh,
    onErrorContainer = onErrorContainerLight.urbanAsh,
    background = backgroundLight.urbanAsh,
    onBackground = onBackgroundLight.urbanAsh,
    surface = surfaceLight.urbanAsh,
    onSurface = onSurfaceLight.urbanAsh,
    surfaceVariant = surfaceVariantLight.urbanAsh,
    onSurfaceVariant = onSurfaceVariantLight.urbanAsh,
    outline = outlineLight.urbanAsh,
    outlineVariant = outlineVariantLight.urbanAsh,
    scrim = scrimLight.urbanAsh,
    inverseSurface = inverseSurfaceLight.urbanAsh,
    inverseOnSurface = inverseOnSurfaceLight.urbanAsh,
    inversePrimary = inversePrimaryLight.urbanAsh,
    surfaceDim = surfaceDimLight.urbanAsh,
    surfaceBright = surfaceBrightLight.urbanAsh,
    surfaceContainerLowest = surfaceContainerLowestLight.urbanAsh,
    surfaceContainerLow = surfaceContainerLowLight.urbanAsh,
    surfaceContainer = surfaceContainerLight.urbanAsh,
    surfaceContainerHigh = surfaceContainerHighLight.urbanAsh,
    surfaceContainerHighest = surfaceContainerHighestLight.urbanAsh,
)

private val urbanAshDark = darkColorScheme(
    primary = primaryDark.urbanAsh,
    onPrimary = onPrimaryDark.urbanAsh,
    primaryContainer = primaryContainerDark.urbanAsh,
    onPrimaryContainer = onPrimaryContainerDark.urbanAsh,
    secondary = secondaryDark.urbanAsh,
    onSecondary = onSecondaryDark.urbanAsh,
    secondaryContainer = secondaryContainerDark.urbanAsh,
    onSecondaryContainer = onSecondaryContainerDark.urbanAsh,
    tertiary = tertiaryDark.urbanAsh,
    onTertiary = onTertiaryDark.urbanAsh,
    tertiaryContainer = tertiaryContainerDark.urbanAsh,
    onTertiaryContainer = onTertiaryContainerDark.urbanAsh,
    error = errorDark.urbanAsh,
    onError = onErrorDark.urbanAsh,
    errorContainer = errorContainerDark.urbanAsh,
    onErrorContainer = onErrorContainerDark.urbanAsh,
    background = backgroundDark.urbanAsh,
    onBackground = onBackgroundDark.urbanAsh,
    surface = surfaceDark.urbanAsh,
    onSurface = onSurfaceDark.urbanAsh,
    surfaceVariant = surfaceVariantDark.urbanAsh,
    onSurfaceVariant = onSurfaceVariantDark.urbanAsh,
    outline = outlineDark.urbanAsh,
    outlineVariant = outlineVariantDark.urbanAsh,
    scrim = scrimDark.urbanAsh,
    inverseSurface = inverseSurfaceDark.urbanAsh,
    inverseOnSurface = inverseOnSurfaceDark.urbanAsh,
    inversePrimary = inversePrimaryDark.urbanAsh,
    surfaceDim = surfaceDimDark.urbanAsh,
    surfaceBright = surfaceBrightDark.urbanAsh,
    surfaceContainerLowest = surfaceContainerLowestDark.urbanAsh,
    surfaceContainerLow = surfaceContainerLowDark.urbanAsh,
    surfaceContainer = surfaceContainerDark.urbanAsh,
    surfaceContainerHigh = surfaceContainerHighDark.urbanAsh,
    surfaceContainerHighest = surfaceContainerHighestDark.urbanAsh,
)

private val verdantDawnLight = lightColorScheme(
    primary = primaryLight.verdantDawn,
    onPrimary = onPrimaryLight.verdantDawn,
    primaryContainer = primaryContainerLight.verdantDawn,
    onPrimaryContainer = onPrimaryContainerLight.verdantDawn,
    secondary = secondaryLight.verdantDawn,
    onSecondary = onSecondaryLight.verdantDawn,
    secondaryContainer = secondaryContainerLight.verdantDawn,
    onSecondaryContainer = onSecondaryContainerLight.verdantDawn,
    tertiary = tertiaryLight.verdantDawn,
    onTertiary = onTertiaryLight.verdantDawn,
    tertiaryContainer = tertiaryContainerLight.verdantDawn,
    onTertiaryContainer = onTertiaryContainerLight.verdantDawn,
    error = errorLight.verdantDawn,
    onError = onErrorLight.verdantDawn,
    errorContainer = errorContainerLight.verdantDawn,
    onErrorContainer = onErrorContainerLight.verdantDawn,
    background = backgroundLight.verdantDawn,
    onBackground = onBackgroundLight.verdantDawn,
    surface = surfaceLight.verdantDawn,
    onSurface = onSurfaceLight.verdantDawn,
    surfaceVariant = surfaceVariantLight.verdantDawn,
    onSurfaceVariant = onSurfaceVariantLight.verdantDawn,
    outline = outlineLight.verdantDawn,
    outlineVariant = outlineVariantLight.verdantDawn,
    scrim = scrimLight.verdantDawn,
    inverseSurface = inverseSurfaceLight.verdantDawn,
    inverseOnSurface = inverseOnSurfaceLight.verdantDawn,
    inversePrimary = inversePrimaryLight.verdantDawn,
    surfaceDim = surfaceDimLight.verdantDawn,
    surfaceBright = surfaceBrightLight.verdantDawn,
    surfaceContainerLowest = surfaceContainerLowestLight.verdantDawn,
    surfaceContainerLow = surfaceContainerLowLight.verdantDawn,
    surfaceContainer = surfaceContainerLight.verdantDawn,
    surfaceContainerHigh = surfaceContainerHighLight.verdantDawn,
    surfaceContainerHighest = surfaceContainerHighestLight.verdantDawn,
)

private val verdantDawnDark = darkColorScheme(
    primary = primaryDark.verdantDawn,
    onPrimary = onPrimaryDark.verdantDawn,
    primaryContainer = primaryContainerDark.verdantDawn,
    onPrimaryContainer = onPrimaryContainerDark.verdantDawn,
    secondary = secondaryDark.verdantDawn,
    onSecondary = onSecondaryDark.verdantDawn,
    secondaryContainer = secondaryContainerDark.verdantDawn,
    onSecondaryContainer = onSecondaryContainerDark.verdantDawn,
    tertiary = tertiaryDark.verdantDawn,
    onTertiary = onTertiaryDark.verdantDawn,
    tertiaryContainer = tertiaryContainerDark.verdantDawn,
    onTertiaryContainer = onTertiaryContainerDark.verdantDawn,
    error = errorDark.verdantDawn,
    onError = onErrorDark.verdantDawn,
    errorContainer = errorContainerDark.verdantDawn,
    onErrorContainer = onErrorContainerDark.verdantDawn,
    background = backgroundDark.verdantDawn,
    onBackground = onBackgroundDark.verdantDawn,
    surface = surfaceDark.verdantDawn,
    onSurface = onSurfaceDark.verdantDawn,
    surfaceVariant = surfaceVariantDark.verdantDawn,
    onSurfaceVariant = onSurfaceVariantDark.verdantDawn,
    outline = outlineDark.verdantDawn,
    outlineVariant = outlineVariantDark.verdantDawn,
    scrim = scrimDark.verdantDawn,
    inverseSurface = inverseSurfaceDark.verdantDawn,
    inverseOnSurface = inverseOnSurfaceDark.verdantDawn,
    inversePrimary = inversePrimaryDark.verdantDawn,
    surfaceDim = surfaceDimDark.verdantDawn,
    surfaceBright = surfaceBrightDark.verdantDawn,
    surfaceContainerLowest = surfaceContainerLowestDark.verdantDawn,
    surfaceContainerLow = surfaceContainerLowDark.verdantDawn,
    surfaceContainer = surfaceContainerDark.verdantDawn,
    surfaceContainerHigh = surfaceContainerHighDark.verdantDawn,
    surfaceContainerHighest = surfaceContainerHighestDark.verdantDawn,
)

private fun customLight(
    color: Color,
    style: PaletteStyle,
): ColorScheme {
    return dynamicColorScheme(
        primary = color,
        isDark = false,
        style = style,
    )
}

private fun customDark(
    color: Color,
    style: PaletteStyle,
): ColorScheme {
    return dynamicColorScheme(
        primary = color,
        isDark = true,
        style = style,
    )
}

@Composable
fun ZalithLauncherTheme(
    darkTheme: Boolean = isLauncherInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    backgroundViewModel: BackgroundViewModel? = null,
    festivals: List<Festival> = emptyList(),
    content: @Composable () -> Unit
) {
    val colorTheme = AllSettings.launcherColorTheme.state
    val customColorInt = AllSettings.launcherCustomColor.state
    val customColor = Color(customColorInt)
    val customPaletteStyle = AllSettings.launcherCustomPaletteStyle.state

    val context = LocalContext.current

    val targetColorScheme = remember(darkTheme, dynamicColor, colorTheme, customColor, customPaletteStyle) {
        when {
            dynamicColor && colorTheme == ColorThemeType.DYNAMIC && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            }

            darkTheme -> when (colorTheme) {
                ColorThemeType.EMBERMIRE -> embermireDark
                ColorThemeType.VELVET_ROSE -> velvetRoseDark
                ColorThemeType.MISTWAVE -> mistwaveDark
                ColorThemeType.GLACIER -> glacierDark
                ColorThemeType.VERDANTFIELD -> verdantFieldDark
                ColorThemeType.URBAN_ASH -> urbanAshDark
                ColorThemeType.VERDANT_DAWN -> verdantDawnDark
                ColorThemeType.CUSTOM -> customDark(
                    color = customColor,
                    style = customPaletteStyle
                )
                else -> embermireDark
            }

            else -> when (colorTheme) {
                ColorThemeType.EMBERMIRE -> embermireLight
                ColorThemeType.VELVET_ROSE -> velvetRoseLight
                ColorThemeType.MISTWAVE -> mistwaveLight
                ColorThemeType.GLACIER -> glacierLight
                ColorThemeType.VERDANTFIELD -> verdantFieldLight
                ColorThemeType.URBAN_ASH -> urbanAshLight
                ColorThemeType.VERDANT_DAWN -> verdantDawnLight
                ColorThemeType.CUSTOM -> customLight(
                    color = customColor,
                    style = customPaletteStyle
                )
                else -> embermireLight
            }
        }
    }

    var currentDarkTheme by remember { mutableStateOf(darkTheme) }
    var currentDisplayScheme by remember { mutableStateOf(targetColorScheme) }

    LaunchedEffect(darkTheme, targetColorScheme) {
        if (darkTheme != currentDarkTheme) {
            context.activeMaskView(
                maskComplete = {
                    currentDarkTheme = darkTheme
                    currentDisplayScheme = targetColorScheme
                },
                maskAnimFinish = {

                }
            )
        } else {
            currentDarkTheme = darkTheme
            currentDisplayScheme = targetColorScheme
        }
    }

    CompositionLocalProvider(
        LocalBackgroundViewModel provides backgroundViewModel,
        LocalFestivals provides festivals
    ) {
        MaterialExpressiveTheme(
            colorScheme = currentDisplayScheme,
            motionScheme = MotionScheme.expressive(),
            typography = AppTypography,
            content = content
        )
    }
}
