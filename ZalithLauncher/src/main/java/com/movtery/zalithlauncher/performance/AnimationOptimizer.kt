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

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween

/**
 * Get optimized animation spec based on device memory pressure
 */
fun getOptimizedAnimationSpec(
    duration: Int = 300,
    memoryPressure: Float = 0f
): AnimationSpec<Float> {
    // On high memory pressure, use snap animations
    return if (memoryPressure > 0.8f) {
        snap()
    } else if (memoryPressure > 0.6f) {
        // Medium pressure: shorter duration
        tween(durationMillis = 100)
    } else {
        // Low pressure: normal animation
        tween(durationMillis = duration)
    }
}

/**
 * Animation performance hints
 */
object AnimationOptimizationConfig {
    var disableAnimationsOnLowMemory: Boolean = true
    var reduceAnimationDurationOnMediumPressure: Boolean = true
    var memoryPressureThreshold: Float = 0.6f
}
