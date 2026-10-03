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

import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

class FireworksSimulator(
    random: Random,
    private val grand: Boolean = false
) : ParticleSimulator(random) {
    private var nextLaunch = FIRST_LAUNCH_DELAY
    private var burstHue = 0
    private var salvosLeft = 0
    private var salvoDelay = 0f

    override fun onUpdate(deltaSeconds: Float) {
        nextLaunch -= deltaSeconds
        if (nextLaunch <= 0f) {
            launch(
                x = width * (0.15f + random.nextFloat() * 0.7f),
                targetY = height * (0.12f + random.nextFloat() * 0.3f)
            )
            nextLaunch = if (grand) {
                random.nextFloat() * 2.4f + 2.6f
            } else {
                random.nextFloat() * 2.0f + 1.4f
            }
        }

        // 短时间差内逐枚升空，可链式触发
        if (salvosLeft > 0) {
            salvoDelay -= deltaSeconds
            if (salvoDelay <= 0f) {
                salvosLeft--
                launch(
                    x = width * (0.15f + random.nextFloat() * 0.7f),
                    targetY = height * (0.12f + random.nextFloat() * 0.3f)
                )
                salvoDelay = SALVO_MIN_DELAY + random.nextFloat() * (SALVO_MAX_DELAY - SALVO_MIN_DELAY)
            }
        }

        val dragFactor = exp(-DRAG * deltaSeconds)
        for (particle in particles) {
            particle.px = particle.x
            particle.py = particle.y

            when (particle.state) {
                STATE_ROCKET -> {
                    particle.vy += GRAVITY * density * deltaSeconds
                    // 弯道火箭：横向正弦加速度画出小幅 S 形
                    if (particle.spin != 0f) {
                        particle.vx += particle.spin * sin(particle.life * CURVE_FREQ + particle.phase) * deltaSeconds
                    }
                    particle.x += particle.vx * deltaSeconds
                    particle.y += particle.vy * deltaSeconds
                    particle.life += deltaSeconds
                    if (particle.vy >= -RELEASE_VELOCITY * density || particle.y <= particle.size) {
                        explode(particle)
                    }
                }

                STATE_BURST, STATE_SPLIT, STATE_FLASH -> {
                    particle.vx *= dragFactor
                    particle.vy = particle.vy * dragFactor + GRAVITY * density * deltaSeconds * 0.55f
                    particle.x += particle.vx * deltaSeconds
                    particle.y += particle.vy * deltaSeconds
                    particle.life += deltaSeconds

                    // 分叉烟花
                    // 主光点在存活的某个时刻再次炸开成小光点
                    if (particle.state == STATE_SPLIT && particle.life >= particle.rotation) {
                        splitBurst(particle)
                        continue
                    }
                    if (particle.life >= particle.maxLife) continue

                    val progress = particle.life / particle.maxLife
                    // 尾段随机闪烁
                    val flicker = if (progress > 0.75f) {
                        0.35f + random.nextFloat() * 0.65f
                    } else {
                        1f
                    }
                    particle.alpha = (1f - progress) * flicker
                }
            }
        }
    }

    fun burstAt(x: Float, targetY: Float) {
        if (width <= 0f || height <= 0f) return
        drainSpawns()
        if (particles.count { it.state == STATE_ROCKET } >= MAX_ROCKETS) return
        launchRocket(
            x = x.coerceIn(width * 0.05f, width * 0.95f),
            targetY = targetY.coerceIn(height * 0.08f, height * 0.85f)
        )
        drainSpawns()
    }

    private fun launch(x: Float, targetY: Float) {
        if (width <= 0f || height <= 0f) return
        drainSpawns()
        if (particles.count { it.state == STATE_ROCKET } >= MAX_ROCKETS) return
        launchRocket(x, targetY)
        val salvoChance = if (grand) SALVO_CHANCE_GRAND else SALVO_CHANCE
        if (random.nextFloat() < salvoChance) {
            salvosLeft += 1 + random.nextInt(2)
            if (salvoDelay <= 0f) {
                salvoDelay = SALVO_MIN_DELAY + random.nextFloat() * (SALVO_MAX_DELAY - SALVO_MIN_DELAY)
            }
        }
    }

    private fun launchRocket(x: Float, targetY: Float) {
        spawn {
            state = STATE_ROCKET
            this.x = x
            this.y = height + dp(6f)
            px = x
            py = y
            val rise = this.y - targetY
            vy = -sqrt(2f * GRAVITY * density * rise)

            // 控制轨迹形态
            val trajectory = random.nextFloat()
            val flightTime =
                ((abs(vy) - RELEASE_VELOCITY * density) / (GRAVITY * density)).coerceAtLeast(0.1f)
            // 水平位移不得越过屏幕左右 8% 的安全区
            val minX = width * 0.08f
            val maxX = width * 0.92f
            when {
                // 直线
                trajectory < TRAJECTORY_TILTED_FROM -> {
                    vx = (random.nextFloat() - 0.5f) * dp(30f)
                    val finalX = (x + vx * flightTime).coerceIn(minX, maxX)
                    vx = (finalX - x) / flightTime
                }
                // 斜向
                trajectory < TRAJECTORY_CURVE_FROM -> {
                    val towardCenter = if (x < width * 0.5f) 1f else -1f
                    val tiltFactor = 0.14f + random.nextFloat() * 0.16f
                    val drift = abs(vy) * tiltFactor * flightTime
                    val finalX = if (towardCenter > 0f) {
                        minOf(x + drift, maxX)
                    } else {
                        maxOf(x - drift, minX)
                    }
                    vx = (finalX - x) / flightTime
                }
                // 小幅弯道
                else -> {
                    vx = (random.nextFloat() - 0.5f) * dp(30f)
                    val finalX = (x + vx * flightTime).coerceIn(minX, maxX)
                    vx = (finalX - x) / flightTime
                    // 弯道位移上限为 2·spin/ω²，据此限制强度，漂移不越过安全区
                    val available = minOf(x - minX, maxX - x) * 0.6f
                    val maxSpin = available * CURVE_FREQ * CURVE_FREQ / 2f
                    spin = (if (random.nextBoolean()) 1f else -1f) *
                        minOf(dp(40f + random.nextFloat() * 50f), maxSpin)
                }
            }

            phase = random.nextFloat() * KT_PI2
            rotation = nextBurstStyle().toFloat()
            size = dp(2f)
            alpha = 1f
            life = 0f
            maxLife = 0f
            colorIndex = nextBurstColor()
        }
    }

    private fun explode(rocket: Particle) {
        when (rocket.rotation.toInt()) {
            STYLE_CROSSETTE -> explodeCrossette(rocket)
            STYLE_DOUBLE -> explodeDouble(rocket)
            else -> explodeRing(rocket)
        }

        repeat(FLASH_COUNT) {
            spawn {
                state = STATE_FLASH
                x = rocket.x
                y = rocket.y
                px = x
                py = y
                vx = (random.nextFloat() - 0.5f) * dp(60f)
                vy = (random.nextFloat() - 0.5f) * dp(60f)
                size = dp(3.2f)
                alpha = 1f
                life = 0f
                maxLife = 0.16f + random.nextFloat() * 0.08f
                colorIndex = FLASH_COLOR
            }
        }

        markDead(rocket)
    }

    private fun explodeRing(rocket: Particle) {
        // 粒子量克制，视觉密度由绘制层的光晕补偿
        val count = if (grand) 44 + random.nextInt(20) else 30 + random.nextInt(14)
        repeat(count) {
            spawnBurstParticle(rocket, splitAt = 0f)
        }
    }

    private fun explodeDouble(rocket: Particle) {
        val count = if (grand) 40 + random.nextInt(18) else 28 + random.nextInt(12)
        repeat(count) { index ->
            val outer = index % 5 < 3
            spawnBurstParticle(
                rocket,
                splitAt = 0f,
                speedScale = if (outer) 1f else 0.45f,
                sizeScale = if (outer) 1f else 0.7f,
                lifeScale = if (outer) 1f else 0.75f
            )
        }
    }

    private fun explodeCrossette(rocket: Particle) {
        val primaries = if (grand) 12 + random.nextInt(5) else 10 + random.nextInt(4)
        repeat(primaries) {
            val maxLife = (if (grand) 1.4f else 1.2f) + random.nextFloat() * 0.6f
            spawnBurstParticle(rocket, splitAt = maxLife * (0.45f + random.nextFloat() * 0.25f), lifeOverride = maxLife)
        }
    }

    private fun spawnBurstParticle(
        rocket: Particle,
        splitAt: Float,
        speedScale: Float = 1f,
        sizeScale: Float = 1f,
        lifeScale: Float = 1f,
        lifeOverride: Float = 0f
    ) {
        spawn {
            state = if (splitAt > 0f) STATE_SPLIT else STATE_BURST
            val angle = random.nextFloat() * KT_PI2
            val baseSpeed = if (grand) 190f + random.nextFloat() * 330f else 160f + random.nextFloat() * 280f
            val speed = dp(baseSpeed) * (0.55f + random.nextFloat() * 0.45f) * speedScale
            x = rocket.x
            y = rocket.y
            px = x
            py = y
            vx = cos(angle) * speed
            vy = sin(angle) * speed
            size = dp(2f + random.nextFloat() * 1.6f) * sizeScale
            alpha = 1f
            life = 0f
            maxLife = (if (lifeOverride > 0f) lifeOverride else if (grand) 1.3f + random.nextFloat() * 1.2f else 1.1f + random.nextFloat() * 1.1f) * lifeScale
            colorIndex = rocket.colorIndex
            phase = random.nextFloat() * KT_PI2
            rotation = splitAt
        }
    }

    private fun splitBurst(primary: Particle) {
        val count = 3 + random.nextInt(2)
        repeat(count) {
            val angle = random.nextFloat() * KT_PI2
            val speed = dp(80f + random.nextFloat() * 110f)
            spawn {
                state = STATE_BURST
                x = primary.x
                y = primary.y
                px = x
                py = y
                vx = primary.vx * 0.25f + cos(angle) * speed
                vy = primary.vy * 0.25f + sin(angle) * speed
                size = dp(1.2f + random.nextFloat() * 0.9f)
                alpha = 1f
                life = 0f
                maxLife = 0.5f + random.nextFloat() * 0.4f
                colorIndex = primary.colorIndex
                phase = random.nextFloat() * KT_PI2
            }
        }
        markDead(primary)
    }

    private fun nextBurstColor(): Int {
        burstHue = (burstHue + 1) % BURST_COLOR_COUNT
        return burstHue
    }

    private fun nextBurstStyle(): Int {
        val roll = random.nextFloat()
        val crossette = if (grand) 0.40f else 0.30f
        val double = if (grand) 0.30f else 0.25f
        return when {
            roll < crossette -> STYLE_CROSSETTE
            roll < crossette + double -> STYLE_DOUBLE
            else -> STYLE_RING
        }
    }

    companion object {
        const val STATE_ROCKET = 0
        const val STATE_BURST = 1
        const val STATE_FLASH = 2
        const val STATE_SPLIT = 3
        const val BURST_COLOR_COUNT = 5
        const val FLASH_COLOR = BURST_COLOR_COUNT

        private const val GRAVITY = 420f
        private const val DRAG = 1.7f
        private const val RELEASE_VELOCITY = 55f
        private const val MAX_ROCKETS = 5
        private const val FLASH_COUNT = 2
        private const val KT_PI2 = (Math.PI * 2.0).toFloat()

        const val SALVO_CHANCE = 0.35f
        const val SALVO_CHANCE_GRAND = 0.45f
        private const val SALVO_MIN_DELAY = 0.3f
        private const val SALVO_MAX_DELAY = 0.7f

        private const val TRAJECTORY_TILTED_FROM = 0.40f
        private const val TRAJECTORY_CURVE_FROM = 0.70f
        private const val CURVE_FREQ = 1.6f

        private const val STYLE_RING = 0
        private const val STYLE_CROSSETTE = 1
        private const val STYLE_DOUBLE = 2

        private const val FIRST_LAUNCH_DELAY = 0.9f
    }
}
