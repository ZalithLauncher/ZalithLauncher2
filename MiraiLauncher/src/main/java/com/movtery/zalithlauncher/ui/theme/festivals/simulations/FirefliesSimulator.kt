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

package com.movtery.zalithlauncher.ui.theme.festivals.simulations

import kotlin.math.sin
import kotlin.random.Random

class Firefly(
    val centerX: Float,
    val centerY: Float,
    val radiusX: Float,
    val radiusY: Float,
    val freqX: Float,
    val freqY: Float,
    val phase: Float,
    val blinkFreq: Float
) {
    var x = 0f
        private set
    var y = 0f
        private set
    var alpha = 0f
        private set

    fun update(elapsed: Float) {
        x = centerX + sin(elapsed * freqX + phase) * radiusX
        y = centerY + sin(elapsed * freqY + phase * 1.7f) * radiusY
        val blink = sin(elapsed * blinkFreq + phase * 11f)
        alpha = 0.22f + 0.78f * (if (blink > 0f) blink * blink * blink else 0f)
    }
}

class FirefliesSimulator(
    random: Random
) : ParticleSimulator(random) {

    val flies = ArrayList<Firefly>()

    var moonX = 0f
        private set
    var moonY = 0f
        private set
    var moonRadius = 0f
        private set

    /** 满月光晕呼吸系数 */
    val moonGlow: Float
        get() = 0.925f + sin(elapsed * MOON_BREATH_FREQ) * 0.075f

    override fun onUpdate(deltaSeconds: Float) {
        if (width <= 0f || height <= 0f) return

        moonRadius = dp(22f)
        moonX = width * 0.78f
        moonY = height * 0.16f

        if (flies.isEmpty()) {
            repeat(22) {
                val radiusX = dp(50f + random.nextFloat() * 120f)
                val radiusY = dp(30f + random.nextFloat() * 90f)
                flies.add(
                    Firefly(
                        centerX = radiusX + random.nextFloat() * (width - radiusX * 2f).coerceAtLeast(1f),
                        centerY = height * 0.2f + radiusY + random.nextFloat() * (height * 0.65f - radiusY * 2f).coerceAtLeast(1f),
                        radiusX = radiusX,
                        radiusY = radiusY,
                        freqX = 0.06f + random.nextFloat() * 0.11f,
                        freqY = 0.09f + random.nextFloat() * 0.15f,
                        phase = random.nextFloat() * KT_PI2,
                        blinkFreq = 0.7f + random.nextFloat() * 1.1f
                    )
                )
            }
        }

        for (fly in flies) {
            fly.update(elapsed)
        }
    }

    companion object {
        private const val MOON_BREATH_FREQ = 0.45f
        private const val KT_PI2 = (Math.PI * 2.0).toFloat()
    }
}
