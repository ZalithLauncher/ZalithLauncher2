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

class HeartsSimulator(
    random: Random
) : ParticleSimulator(random) {

    override fun onUpdate(deltaSeconds: Float) {
        if (particles.none { it.state == STATE_RISING }) {
            repeat(scaledCount(42)) {
                spawn { initHeart(this, random.nextFloat() * height) }
            }
        }

        val dragFactor = 1f - BURST_DRAG * deltaSeconds
        for (particle in particles) {
            particle.px = particle.x
            particle.py = particle.y

            when (particle.state) {
                STATE_RISING -> {
                    particle.x += sin(elapsed * SWAY_FREQ + particle.phase) * SWAY_AMP * density * deltaSeconds
                    particle.y += particle.vy * deltaSeconds
                    particle.rotation = sin(elapsed * ROCK_FREQ + particle.phase) * ROCK_ANGLE

                    if (particle.y < -particle.size * 2f) {
                        initHeart(particle, height + particle.size * 2f)
                    }
                }

                STATE_BURSTING -> {
                    particle.vx *= dragFactor
                    particle.vy = particle.vy * dragFactor + BURST_GRAVITY * density * deltaSeconds
                    particle.x += particle.vx * deltaSeconds
                    particle.y += particle.vy * deltaSeconds
                    particle.rotation += particle.spin * deltaSeconds
                    particle.life += deltaSeconds
                    if (particle.life >= particle.maxLife) continue
                    particle.alpha = 1f - particle.life / particle.maxLife
                }
            }
        }
    }

    /** 在点击处爆出一簇向上飘散的小爱心 */
    fun burstHearts(x: Float, y: Float) {
        if (width <= 0f || height <= 0f) return
        drainSpawns()
        val count = 6 + random.nextInt(5)
        repeat(count) {
            spawn {
                state = STATE_BURSTING
                this.x = x + (random.nextFloat() - 0.5f) * dp(20f)
                this.y = y + (random.nextFloat() - 0.5f) * dp(16f)
                px = this.x
                py = this.y
                // 扇形向上飘散
                val angle = (-150f + random.nextFloat() * 120f) * DEG_TO_RAD
                val speed = dp(120f + random.nextFloat() * 150f)
                vx = cos(angle) * speed
                vy = sin(angle) * speed
                size = dp(5f + random.nextFloat() * 4f)
                spin = (random.nextFloat() - 0.5f) * 2.4f
                rotation = 0f
                alpha = 1f
                life = 0f
                maxLife = 1.1f + random.nextFloat() * 0.9f
                colorIndex = 0
            }
        }
        drainSpawns()
    }

    /** [startY] 为爱心中心的初始纵坐标 */
    private fun initHeart(particle: Particle, startY: Float) {
        particle.state = STATE_RISING
        particle.variant = random.nextFloat()
        particle.phase = random.nextFloat() * KT_PI2
        // 大颗更淡（近景虚化），小颗略深（远景）
        particle.alpha = 0.55f - particle.variant * 0.25f
        particle.size = dp(8f + particle.variant * 8f)
        particle.vy = -dp(30f + (1f - particle.variant) * 40f)
        particle.rotation = 0f
        particle.spin = 0f
        particle.maxLife = 0f
        particle.life = 0f
        // 两侧留出摆动余量，避免爱心被正弦摆动送出屏幕
        val swayMargin = SWAY_AMP * density + dp(6f)
        particle.x = swayMargin + random.nextFloat() * (width - swayMargin * 2f).coerceAtLeast(1f)
        particle.px = particle.x
        particle.y = startY
        particle.py = particle.y
    }

    companion object {
        const val STATE_RISING = 0
        const val STATE_BURSTING = 1

        private const val SWAY_FREQ = 1.3f
        private const val SWAY_AMP = 20f
        private const val ROCK_FREQ = 0.9f
        private const val ROCK_ANGLE = 0.26f
        private const val BURST_DRAG = 1.4f
        private const val BURST_GRAVITY = 60f
        private const val KT_PI2 = (Math.PI * 2.0).toFloat()
        private const val DEG_TO_RAD = (Math.PI / 180.0).toFloat()
    }
}
