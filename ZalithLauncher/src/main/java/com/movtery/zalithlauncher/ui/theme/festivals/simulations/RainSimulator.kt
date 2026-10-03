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

import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

class RainSimulator(
    random: Random
) : ParticleSimulator(random) {
    private var wind = 0f
    private var windTarget = 0f
    private var nextGust = 0f
    private var dropCount = 0

    override fun onUpdate(deltaSeconds: Float) {
        updateWind(deltaSeconds)
        ensureDrops()

        val iterator = particles.iterator()
        while (iterator.hasNext()) {
            val particle = iterator.next()
            particle.px = particle.x
            particle.py = particle.y

            if (particle.state == STATE_DROP) {
                particle.vx += (wind * particle.vy - particle.vx) * (deltaSeconds * 1.5f).coerceAtMost(1f)
                particle.x += particle.vx * deltaSeconds
                particle.y += particle.vy * deltaSeconds

                if (particle.x < -width * 0.2f) {
                    particle.x += width * 1.4f
                } else if (particle.x > width * 1.2f) {
                    particle.x -= width * 1.4f
                }

                if (particle.y > height + particle.size) {
                    if (random.nextFloat() < 0.55f) {
                        spawnSplash(particle.x, height)
                    }
                    resetDrop(particle)
                }
            } else {
                particle.vy += SPLASH_GRAVITY * density * deltaSeconds
                particle.x += particle.vx * deltaSeconds
                particle.y += particle.vy * deltaSeconds
                particle.life += deltaSeconds
            }
        }
    }

    private fun updateWind(deltaSeconds: Float) {
        nextGust -= deltaSeconds
        if (nextGust <= 0f) {
            windTarget = random.nextFloat() * 0.55f - 0.15f
            nextGust = random.nextFloat() * 5f + 3f
        }
        wind += (windTarget - wind) * (deltaSeconds * 0.8f).coerceAtMost(1f)
    }

    private fun ensureDrops() {
        if (dropCount > 0) return
        val target = scaledCount(110)
        repeat(target) {
            spawn {
                state = STATE_DROP
                initDrop(this)
                y = random.nextFloat() * height
                py = y
            }
        }
        dropCount = target
    }

    private fun resetDrop(particle: Particle) {
        initDrop(particle)
        particle.y = -random.nextFloat() * height * 0.2f
        particle.py = particle.y
    }

    private fun initDrop(particle: Particle) {
        particle.state = STATE_DROP
        particle.life = 0f
        particle.maxLife = 0f
        particle.variant = random.nextFloat()
        val speed = dp(900f + random.nextFloat() * 480f) * (0.75f + particle.variant * 0.45f)
        particle.size = speed * 0.042f
        particle.alpha = 0.3f + particle.variant * 0.3f
        particle.vx = speed * wind
        particle.vy = speed
        particle.x = random.nextFloat() * width * 1.3f - width * 0.15f
    }

    private fun spawnSplash(x: Float, y: Float) {
        val count = 2 + random.nextInt(3)
        repeat(count) {
            spawn {
                state = STATE_SPLASH
                val angle = -PI_HALF + (random.nextFloat() - 0.5f) * 1.6f
                val speed = dp(140f + random.nextFloat() * 180f)
                this.x = x
                this.y = y
                px = x
                py = y
                vx = cos(angle) * speed
                vy = sin(angle) * speed
                size = dp(1.8f)
                alpha = 0.55f
                maxLife = 0.22f + random.nextFloat() * 0.18f
            }
        }
    }

    companion object {
        const val STATE_DROP = 0
        const val STATE_SPLASH = 1
        private const val SPLASH_GRAVITY = 2400f
        private const val PI_HALF = (Math.PI / 2.0).toFloat()
    }
}
