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

import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material3.Typography
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.movtery.zalithlauncher.R

/** NusaLauncher design tokens. Lihat docs/design/DESIGN.md. */
object NusaColors {
    val Background = Color(0xFF0E0E10)
    val Surface = Color(0xFF17181A)
    val SurfaceRaised = Color(0xFF212326)
    val Border = Color(0xFF2E3033)
    val TextPrimary = Color(0xFFE8E8EA)
    val TextSecondary = Color(0xFF9A9CA1)

    val AccentStart = Color(0xFFFFB20D)
    val AccentEnd = Color(0xFFFF4412)
    /** Oranye untuk teks dan tautan di atas latar gelap */
    val AccentText = Color(0xFFFF9A2E)
    val OnAccent = Color(0xFF0E0E10)

    val ShimmerBase = Color(0xFF1E2023)
    val ShimmerHighlight = Color(0xFF2E3034)

    val AccentBrush: Brush
        get() = Brush.verticalGradient(listOf(AccentStart, AccentEnd))
}

/** Sudut kanan atas dan kiri bawah dipotong miring, meniru potongan pada logo NL. */
object NusaShapes {
    val Large = CutCornerShape(topStart = 0.dp, topEnd = 24.dp, bottomEnd = 0.dp, bottomStart = 24.dp)
    val Medium = CutCornerShape(topStart = 0.dp, topEnd = 16.dp, bottomEnd = 0.dp, bottomStart = 16.dp)
    val Small = CutCornerShape(topStart = 0.dp, topEnd = 10.dp, bottomEnd = 0.dp, bottomStart = 10.dp)
}

object NusaMotion {
    const val PRESS_DOWN_MS = 80
    const val COLOR_MS = 90
    const val SHIMMER_MS = 1300
    const val BUTTON_PRESSED_SCALE = 0.96f
    const val CARD_PRESSED_SCALE = 0.985f
}

object NusaType {
    /** Chakra Petch, lisensi SIL OFL 1.1 (docs/licenses/ChakraPetch-OFL.txt) */
    val Display = FontFamily(
        Font(R.font.chakra_petch_semibold, FontWeight.SemiBold),
        Font(R.font.chakra_petch_bold, FontWeight.Bold)
    )

    val DisplayLarge = TextStyle(fontFamily = Display, fontWeight = FontWeight.Bold, fontSize = 34.sp, lineHeight = 38.sp, color = NusaColors.TextPrimary)
    val Title = TextStyle(fontFamily = Display, fontWeight = FontWeight.SemiBold, fontSize = 20.sp, lineHeight = 26.sp, color = NusaColors.TextPrimary)
    val Button = TextStyle(fontFamily = Display, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, lineHeight = 20.sp)
    val Body = TextStyle(fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 20.sp, color = NusaColors.TextPrimary)
    val Caption = TextStyle(fontWeight = FontWeight.Normal, fontSize = 12.sp, lineHeight = 16.sp, color = NusaColors.TextSecondary)

    val Material = Typography(
        headlineLarge = DisplayLarge,
        titleLarge = Title,
        labelLarge = Button,
        bodyLarge = Body.copy(fontSize = 16.sp, lineHeight = 22.sp),
        bodyMedium = Body,
        bodySmall = Caption
    )
}
