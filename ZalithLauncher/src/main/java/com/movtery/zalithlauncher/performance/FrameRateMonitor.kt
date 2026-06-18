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

import android.view.WindowManager
import kotlin.math.roundToInt

data class FrameStats(
    val currentFPS: Int,
    val averageFPS: Double,
    val droppedFrames: Int,
    val totalFrames: Long
)

/**
 * Frame rate monitoring for performance analysis
 */
class FrameRateMonitor {
    private var lastFrameTime = System.nanoTime()
    private val frameTimings = mutableListOf<Long>()
    private var droppedFrames = 0
    private var totalFrames = 0L
    private val maxSamples = 120

    fun recordFrame() {
        val currentTime = System.nanoTime()
        val frameDuration = currentTime - lastFrameTime
        lastFrameTime = currentTime

        frameTimings.add(frameDuration)
        totalFrames++

        if (frameTimings.size > maxSamples) {
            frameTimings.removeAt(0)
        }

        // Detect dropped frames (> 16.67ms for 60fps)
        if (frameDuration > 16_670_000) {
            droppedFrames++
        }
    }

    fun getStats(): FrameStats {
        val averageFrameTime = if (frameTimings.isEmpty()) {
            16_670_000.0
        } else {
            frameTimings.average()
        }

        val currentFPS = (1_000_000_000.0 / averageFrameTime).roundToInt().coerceAtLeast(1)

        return FrameStats(
            currentFPS = currentFPS,
            averageFPS = 1_000_000_000.0 / averageFrameTime,
            droppedFrames = droppedFrames,
            totalFrames = totalFrames
        )
    }

    fun reset() {
        frameTimings.clear()
        droppedFrames = 0
        totalFrames = 0L
    }
}
