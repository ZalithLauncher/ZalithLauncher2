/*
 * Nev Launcher
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

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

/**
 * Tema desain "Cryogenic": biru es, cyan, turquoise, dan navy pekat,
 * dengan permukaan gelap dan bernuansa futuristik.
 * Palet ini dibuat sendiri mengikuti arah visual yang dijelaskan di
 * https://github.com/RussoViller/Cryogenic-Launcher (repo itu tidak berisi kode, hanya deskripsi).
 */
internal val cryogenicDark: ColorScheme = darkColorScheme(
    primary = Color(0xFF4DD8F0),
    onPrimary = Color(0xFF00363F),
    primaryContainer = Color(0xFF0E4F63),
    onPrimaryContainer = Color(0xFFB8EEFF),
    secondary = Color(0xFF7FD1DE),
    onSecondary = Color(0xFF00363C),
    secondaryContainer = Color(0xFF12424C),
    onSecondaryContainer = Color(0xFFBDEFF7),
    tertiary = Color(0xFF9CCBFF),
    onTertiary = Color(0xFF00315A),
    tertiaryContainer = Color(0xFF16477A),
    onTertiaryContainer = Color(0xFFD2E4FF),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    background = Color(0xFF08131E),
    onBackground = Color(0xFFD9E8F2),
    surface = Color(0xFF0A1622),
    onSurface = Color(0xFFD9E8F2),
    surfaceVariant = Color(0xFF1C3342),
    onSurfaceVariant = Color(0xFFA9C4D3),
    outline = Color(0xFF4F7A8E),
    outlineVariant = Color(0xFF24424F),
    scrim = Color(0xFF000000),
    inverseSurface = Color(0xFFD9E8F2),
    inverseOnSurface = Color(0xFF10222E),
    inversePrimary = Color(0xFF006879),
    surfaceDim = Color(0xFF08131E),
    surfaceBright = Color(0xFF2A4254),
    surfaceContainerLowest = Color(0xFF050E17),
    surfaceContainerLow = Color(0xFF0D1B28),
    surfaceContainer = Color(0xFF112332),
    surfaceContainerHigh = Color(0xFF182E3F),
    surfaceContainerHighest = Color(0xFF203849),
)

internal val cryogenicLight: ColorScheme = lightColorScheme(
    primary = Color(0xFF006879),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFA6EEFF),
    onPrimaryContainer = Color(0xFF001F26),
    secondary = Color(0xFF2F6B77),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFB7EBF7),
    onSecondaryContainer = Color(0xFF001F25),
    tertiary = Color(0xFF2C5F9B),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFD3E4FF),
    onTertiaryContainer = Color(0xFF001C3A),
    error = Color(0xFFBA1A1A),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    background = Color(0xFFF1FAFD),
    onBackground = Color(0xFF0C1F28),
    surface = Color(0xFFF1FAFD),
    onSurface = Color(0xFF0C1F28),
    surfaceVariant = Color(0xFFD6E7EE),
    onSurfaceVariant = Color(0xFF3A4F59),
    outline = Color(0xFF69808B),
    outlineVariant = Color(0xFFBBD0D9),
    scrim = Color(0xFF000000),
    inverseSurface = Color(0xFF21343E),
    inverseOnSurface = Color(0xFFE4F2F8),
    inversePrimary = Color(0xFF4DD8F0),
    surfaceDim = Color(0xFFCFDFE6),
    surfaceBright = Color(0xFFF1FAFD),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFEAF5F9),
    surfaceContainer = Color(0xFFE3F0F5),
    surfaceContainerHigh = Color(0xFFDCEAF0),
    surfaceContainerHighest = Color(0xFFD5E4EB),
)
