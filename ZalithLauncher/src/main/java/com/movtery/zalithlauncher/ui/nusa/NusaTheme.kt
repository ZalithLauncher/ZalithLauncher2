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

package com.movtery.zalithlauncher.ui.nusa

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

/** Tema Nusa Dark. Belum dipasang ke layar mana pun; dipakai bertahap sesuai rencana UI. */
@Composable
fun NusaTheme(content: @Composable () -> Unit) {
    val scheme = darkColorScheme(
        primary = NusaColors.AccentStart,
        onPrimary = NusaColors.OnAccent,
        background = NusaColors.Background,
        onBackground = NusaColors.TextPrimary,
        surface = NusaColors.Surface,
        onSurface = NusaColors.TextPrimary,
        surfaceVariant = NusaColors.SurfaceRaised,
        onSurfaceVariant = NusaColors.TextSecondary,
        outline = NusaColors.Border
    )
    MaterialTheme(
        colorScheme = scheme,
        typography = NusaType.Material,
        content = content
    )
}
