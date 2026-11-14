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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/gpl-3.0.txt>.
 */

package com.movtery.zalithlauncher.ui.theme

import androidx.compose.ui.unit.dp

/** Shared spacing scale for DEXOR launcher surfaces and controls. */
object DexorSpacing {
    val xSmall = 4.dp
    val small = 8.dp
    val medium = 12.dp
    val large = 16.dp
    val xLarge = 24.dp
    val xxLarge = 32.dp
}

/** Shape dimensions used by future DEXOR components; existing APIs stay intact. */
object DexorShapeTokens {
    val cardCorner = 20.dp
    val controlCorner = 14.dp
    val compactCorner = 10.dp
}

/** Short, restrained timing tokens for state transitions. */
object DexorMotionTokens {
    const val quickMillis = 140
    const val standardMillis = 220
    const val emphasizedMillis = 320
}
