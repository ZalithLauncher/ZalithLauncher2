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

import android.os.Debug
import kotlin.math.round

data class MemoryStats(
    val nativeHeap: Long,
    val javaHeap: Long,
    val graphics: Long,
    val total: Long
)

/**
 * Memory optimization and monitoring
 */
object MemoryOptimizer {
    /**
     * Get current memory usage
     */
    fun getMemoryStats(): MemoryStats {
        val runtime = Runtime.getRuntime()
        val nativeHeap = Debug.getNativeHeap().map { it.totalSize }.sum()
        val javaHeap = runtime.totalMemory() - runtime.freeMemory()
        val graphics = 0L  // Platform-specific, would need native bridge
        val total = nativeHeap + javaHeap

        return MemoryStats(
            nativeHeap = nativeHeap,
            javaHeap = javaHeap,
            graphics = graphics,
            total = total
        )
    }

    /**
     * Get memory usage in MB
     */
    fun getMemoryUsageMB(): Double {
        return round(getMemoryStats().total / 1024.0 / 1024.0 * 100) / 100
    }

    /**
     * Trigger garbage collection (use sparingly)
     */
    fun triggerGC() {
        System.gc()
        System.runFinalization()
    }

    /**
     * Estimate memory pressure (0.0 to 1.0)
     */
    fun getMemoryPressure(): Float {
        val runtime = Runtime.getRuntime()
        val used = runtime.totalMemory() - runtime.freeMemory()
        val max = runtime.maxMemory()
        return (used.toFloat() / max).coerceIn(0f, 1f)
    }
}
