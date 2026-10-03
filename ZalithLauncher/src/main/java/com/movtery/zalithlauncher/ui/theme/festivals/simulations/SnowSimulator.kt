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

class SnowSimulator(
    random: Random
) : ParticleSimulator(random) {
    private var wind = 0f
    private var windTarget = 0f
    private var nextGust = 0f

    override fun onUpdate(deltaSeconds: Float) {
        updateWind(deltaSeconds)

        if (particles.isEmpty()) {
            repeat(scaledCount(95)) {
                spawn {
                    initFlake(this)
                    // 首次生成时铺满整个屏幕高度
                    y = random.nextFloat() * height
                    py = y
                }
            }
        }

        for (particle in particles) {
            particle.px = particle.x
            particle.py = particle.y

            val sway = sin(elapsed * SWAY_FREQ + particle.phase) * SWAY_AMP * density
            particle.x += (sway + wind * particle.size * 8f) * deltaSeconds
            particle.y += particle.vy * deltaSeconds

            if (particle.x < -FLAKE_MARGIN * density) {
                particle.x = width + FLAKE_MARGIN * density
                particle.px = particle.x
            } else if (particle.x > width + FLAKE_MARGIN * density) {
                particle.x = -FLAKE_MARGIN * density
                particle.px = particle.x
            }

            if (particle.y > height + particle.size) {
                initFlake(particle)
                particle.y = -particle.size
                particle.py = particle.y
            }
        }
    }

    private fun updateWind(deltaSeconds: Float) {
        nextGust -= deltaSeconds
        if (nextGust <= 0f) {
            windTarget = random.nextFloat() * 30f - 15f
            nextGust = random.nextFloat() * 6f + 4f
        }
        wind += (windTarget - wind) * (deltaSeconds * 0.5f).coerceAtMost(1f)
    }

    private fun initFlake(particle: Particle) {
        particle.variant = random.nextFloat()
        particle.phase = random.nextFloat() * KT_PI2
        particle.maxLife = 0f
        particle.life = 0f
        particle.colorIndex = when {
            particle.variant < 0.34f -> 0
            particle.variant < 0.67f -> 1
            else -> 2
        }
        particle.size = dp(FLAKE_SIZE_DP[particle.colorIndex]) / 2f
        particle.vy = dp(45f + (1f - particle.variant) * 105f)
        particle.alpha = 0.35f + (1f - particle.variant) * 0.4f
        particle.x = random.nextFloat() * width
        particle.px = particle.x
    }

    companion object {
        private const val SWAY_FREQ = 1.4f
        private const val SWAY_AMP = 22f
        private const val FLAKE_MARGIN = 8f
        private const val KT_PI2 = (Math.PI * 2.0).toFloat()

        /** 三档雪片直径（dp） */
        val FLAKE_SIZE_DP = floatArrayOf(2.4f, 4.4f, 6.6f)
    }
}
