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

package com.movtery.zalithlauncher.performance

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalInspectionMode

/**
 * Track and log unnecessary recompositions
 * Only active in debug builds
 */
object RecompositionTracker {
    private val recompositionCounts = mutableMapOf<String, Int>()

    fun track(composableName: String) {
        if (!isDebug()) return
        recompositionCounts[composableName] = (recompositionCounts[composableName] ?: 0) + 1
    }

    fun getMetrics(): Map<String, Int> = recompositionCounts.toMap()

    fun reset() {
        recompositionCounts.clear()
    }

    private fun isDebug(): Boolean {
        return android.os.Build.DEBUG || android.util.Log.isLoggable(
            "RecompositionTracker",
            android.util.Log.DEBUG
        )
    }
}

@Composable
inline fun trackRecomposition(composableName: String, content: @Composable () -> Unit) {
    RecompositionTracker.track(composableName)
    content()
}
