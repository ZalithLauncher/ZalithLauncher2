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

import android.provider.Settings
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

/** True jika pengguna mematikan animasi di pengaturan Android (skala durasi animator = 0). */
@Composable
fun rememberReducedMotion(): Boolean {
    val context = LocalContext.current
    return remember {
        runCatching {
            Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f
        }.getOrDefault(false)
    }
}

/**
 * Skala saat ditekan: turun cepat saat jari menekan, lalu kembali dengan pantulan kecil saat dilepas.
 * Tanpa animasi jika pengguna mematikan animasi sistem.
 */
@Composable
fun rememberPressScale(pressed: Boolean, pressedScale: Float): State<Float> {
    val reduced = rememberReducedMotion()
    return animateFloatAsState(
        targetValue = if (pressed) pressedScale else 1f,
        animationSpec = when {
            reduced -> snap()
            pressed -> tween(durationMillis = NusaMotion.PRESS_DOWN_MS, easing = FastOutSlowInEasing)
            else -> spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessMedium)
        },
        label = "nusaPressScale"
    )
}
