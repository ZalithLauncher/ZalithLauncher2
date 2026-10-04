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

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * 万圣节蝙蝠群与鬼火
 */
class BatSwarmSimulator(
    random: Random
) : ParticleSimulator(random) {

    /** 模拟时间（秒），供绘制层计算起伏姿态与火焰带形状 */
    val clock: Float
        get() = elapsed

    private var emberTimer = 0.8f

    override fun onUpdate(deltaSeconds: Float) {
        ensureBats()
        ensureWisps()
        spawnEmbers(deltaSeconds)

        for (particle in particles) {
            particle.px = particle.x
            particle.py = particle.y

            when (particle.state) {
                STATE_BAT, STATE_STARTLED -> updateBat(particle, deltaSeconds)
                STATE_WISP -> updateWisp(particle, deltaSeconds)
                STATE_TRAIL -> updateTrail(particle, deltaSeconds)
                STATE_SPARK -> updateSparkLike(particle, deltaSeconds)
                STATE_EMBER -> updateEmber(particle, deltaSeconds)
            }
        }
    }

    private fun updateBat(particle: Particle, deltaSeconds: Float) {
        val bob = sin(elapsed * BOB_FREQ + particle.phase) *
            BOB_AMP * density * (0.6f + particle.variant * 0.8f)
        particle.x += particle.vx * deltaSeconds
        particle.y += (particle.vy + bob) * deltaSeconds
        particle.rotation += particle.spin * deltaSeconds

        val margin = BAT_MARGIN * density
        val off = particle.x < -margin || particle.x > width + margin ||
            particle.y < -margin || particle.y > height + margin
        when {
            particle.state == STATE_BAT && off -> spawnEdgeBat(particle)
            // 惊飞的蝙蝠飞出屏幕后直接消亡，环境蝙蝠数量回归常态
            particle.state == STATE_STARTLED && off -> markDead(particle)
        }
    }

    private fun updateWisp(particle: Particle, deltaSeconds: Float) {
        // 窜跑状态机：冷却结束触发短时加速，速度平滑过渡
        particle.data[1] -= deltaSeconds
        val dashing = particle.data[2] > 0f
        if (dashing) {
            particle.data[2] -= deltaSeconds
        } else if (particle.data[1] <= 0f) {
            particle.data[2] = DASH_TIME_MIN + random.nextFloat() * (DASH_TIME_MAX - DASH_TIME_MIN)
            particle.data[1] = DASH_COOLDOWN_MIN + random.nextFloat() * (DASH_COOLDOWN_MAX - DASH_COOLDOWN_MIN)
        }
        val targetSpeed = particle.data[5] * if (dashing) DASH_SPEED_MULT else 1f
        particle.data[3] += (targetSpeed - particle.data[3]) * (deltaSeconds * SPEED_LERP).coerceAtMost(1f)

        // 曲率漂移：曲率值缓慢随机演化并被缓慢振荡的偏置拉回，
        // 转向量正比于速度，轨迹呈连绵变化的 C 形/S 形弧线；窜跑时弧线拉直一些
        var curvature = particle.data[6]
        curvature += (random.nextFloat() - 0.5f) * CURV_DRIFT * deltaSeconds
        val bias = sin(elapsed * CURV_BIAS_FREQ + particle.rotation) * MAX_CURVATURE * 0.6f
        curvature += (bias - curvature) * (deltaSeconds * CURV_BIAS_LERP).coerceAtMost(1f)
        curvature = curvature.coerceIn(-MAX_CURVATURE, MAX_CURVATURE)
        particle.data[6] = curvature

        val turn = curvature * particle.data[3] * if (dashing) DASH_TURN_SCALE else 1f
        var angle = particle.phase + turn * deltaSeconds

        // 靠近边缘时平滑转向屏幕中心，避免冲出边界
        val steerMargin = WISP_STEER_MARGIN * density
        val nearEdge = particle.x < steerMargin || particle.x > width - steerMargin ||
            particle.y < steerMargin || particle.y > height - steerMargin
        if (nearEdge) {
            val toCenter = atan2(height * 0.5f - particle.y, width * 0.5f - particle.x)
            angle = angle + shortestAngle(angle, toCenter) * (deltaSeconds * 4f).coerceAtMost(1f)
        }

        particle.phase = angle
        particle.x += cos(angle) * particle.data[3] * deltaSeconds
        particle.y += sin(angle) * particle.data[3] * deltaSeconds

        particle.life += deltaSeconds
        val blink = sin(elapsed * particle.data[0] + particle.rotation)
        particle.alpha = 0.55f + 0.35f * (if (blink > 0f) blink * blink else 0f)

        // 拖尾粒子：钉在生成位置，仅随寿命渐隐，构成纯粹的历史移动尾迹
        particle.data[4] -= deltaSeconds
        if (particle.data[4] <= 0f) {
            particle.data[4] = TRAIL_INTERVAL
            spawn {
                state = STATE_TRAIL
                x = particle.x - cos(particle.phase) * particle.size * 0.6f
                y = particle.y - sin(particle.phase) * particle.size * 0.6f
                px = x
                py = y
                vx = 0f
                vy = 0f
                size = particle.size * (0.5f + random.nextFloat() * 0.2f)
                alpha = 0.8f
                life = 0f
                maxLife = TRAIL_LIFE_MIN + random.nextFloat() * (TRAIL_LIFE_MAX - TRAIL_LIFE_MIN)
                colorIndex = WISP_COLOR
            }
        }
    }

    private fun updateTrail(particle: Particle, deltaSeconds: Float) {
        particle.life += deltaSeconds
        if (particle.life >= particle.maxLife) return
        particle.alpha = (1f - particle.life / particle.maxLife) * 0.8f
    }

    private fun updateSparkLike(particle: Particle, deltaSeconds: Float) {
        val drag = 1f - SPARK_DRAG * deltaSeconds
        particle.vx *= drag
        particle.vy = particle.vy * drag + SPARK_GRAVITY * density * deltaSeconds
        particle.x += particle.vx * deltaSeconds
        particle.y += particle.vy * deltaSeconds
        particle.life += deltaSeconds
        if (particle.life >= particle.maxLife) return
        val progress = particle.life / particle.maxLife
        // 尾段闪烁渐隐
        val flicker = if (progress > 0.55f) 0.4f + random.nextFloat() * 0.6f else 1f
        particle.alpha = (1f - progress) * flicker
    }

    private fun updateEmber(particle: Particle, deltaSeconds: Float) {
        particle.life += deltaSeconds
        if (particle.life >= particle.maxLife) return

        val progress = particle.life / particle.maxLife
        // 螺旋飘忽：横向速度按正弦摆动，半径随高度增大；上升逐渐减速
        val sway = sin(particle.life * particle.data[0] + particle.phase) *
            particle.data[1] * particle.data[0] * (0.4f + progress)
        particle.x += sway * deltaSeconds
        particle.y -= particle.data[2] * (1f - progress * 0.55f) * deltaSeconds

        particle.size = particle.data[3] * (1f - progress * 0.55f)
        val flicker = 0.7f + 0.3f * sin(particle.life * EMBER_FLICKER_FREQ + particle.rotation)
        particle.alpha = (1f - progress) * flicker.coerceIn(0f, 1f)
    }

    /** 点击交互：惊飞一小群蝙蝠 + 四溅一簇鬼火火花 */
    fun startleAt(x: Float, y: Float) {
        if (width <= 0f || height <= 0f) return
        drainSpawns()

        val startled = particles.count { it.state == STATE_STARTLED }
        if (startled < MAX_STARTLED) {
            val count = (3 + random.nextInt(3)).coerceAtMost(MAX_STARTLED - startled)
            repeat(count) {
                spawn {
                    state = STATE_STARTLED
                    this.x = x + (random.nextFloat() - 0.5f) * dp(24f)
                    this.y = (y + (random.nextFloat() - 0.5f) * dp(24f)).coerceIn(dp(10f), height - dp(10f))
                    px = this.x
                    py = this.y
                    val angle = random.nextFloat() * KT_PI2
                    val speed = dp(190f + random.nextFloat() * 140f)
                    vx = cos(angle) * speed
                    vy = sin(angle) * speed
                    variant = 0.25f + random.nextFloat() * 0.45f
                    size = dp(8f + variant * 8f)
                    spin = FLAP_BASE + random.nextFloat() * FLAP_RANGE
                    rotation = random.nextFloat() * KT_PI2
                    phase = random.nextFloat() * KT_PI2
                    alpha = 1f
                    maxLife = 0f
                }
            }
        }

        val sparks = (8 + random.nextInt(7)).coerceAtMost(MAX_SPARKS - particles.count { it.state == STATE_SPARK })
        repeat(sparks.coerceAtLeast(0)) {
            spawn {
                state = STATE_SPARK
                this.x = x + (random.nextFloat() - 0.5f) * dp(12f)
                this.y = y + (random.nextFloat() - 0.5f) * dp(12f)
                px = this.x
                py = this.y
                val angle = random.nextFloat() * KT_PI2
                val speed = dp(170f + random.nextFloat() * 190f)
                vx = cos(angle) * speed
                vy = sin(angle) * speed
                size = dp(1.4f + random.nextFloat() * 1.2f)
                alpha = 1f
                life = 0f
                maxLife = 0.5f + random.nextFloat() * 0.5f
                colorIndex = WISP_COLOR
            }
        }
        drainSpawns()
    }

    private fun spawnEmbers(deltaSeconds: Float) {
        emberTimer -= deltaSeconds
        if (emberTimer > 0f) return
        emberTimer = EMBER_INTERVAL_MIN + random.nextFloat() * (EMBER_INTERVAL_MAX - EMBER_INTERVAL_MIN)
        if (particles.count { it.state == STATE_EMBER } >= MAX_EMBERS) return

        repeat(1 + random.nextInt(2)) {
            spawn {
                state = STATE_EMBER
                val baseSize = dp(2.6f + random.nextFloat() * 2.8f)
                x = random.nextFloat() * width
                y = height + baseSize
                px = x
                py = y
                phase = random.nextFloat() * KT_PI2
                rotation = random.nextFloat() * KT_PI2
                data = floatArrayOf(
                    EMBER_SPIRAL_FREQ_MIN + random.nextFloat() * (EMBER_SPIRAL_FREQ_MAX - EMBER_SPIRAL_FREQ_MIN),
                    dp(30f + random.nextFloat() * 60f),
                    dp(60f + random.nextFloat() * 70f),
                    baseSize
                )
                size = baseSize
                alpha = 1f
                life = 0f
                maxLife = EMBER_LIFE_MIN + random.nextFloat() * (EMBER_LIFE_MAX - EMBER_LIFE_MIN)
                colorIndex = WISP_COLOR
            }
        }
    }

    private fun ensureBats() {
        if (particles.any { it.state == STATE_BAT }) return
        repeat(scaledCount(7)) {
            spawn {
                initBatBody(this)
                spawnEdgeBat(this)
            }
        }
    }

    private fun spawnEdgeBat(particle: Particle) {
        val margin = BAT_MARGIN * density
        val speed = dp(60f + random.nextFloat() * 90f)
        val angleDeg: Float
        when (random.nextInt(4)) {
            0 -> {
                particle.x = -margin
                particle.y = random.nextFloat() * height
                angleDeg = -35f + random.nextFloat() * 70f
            }

            1 -> {
                particle.x = width + margin
                particle.y = random.nextFloat() * height
                angleDeg = 145f + random.nextFloat() * 70f
            }

            2 -> {
                particle.x = random.nextFloat() * width
                particle.y = -margin
                angleDeg = 55f + random.nextFloat() * 70f
            }

            else -> {
                particle.x = random.nextFloat() * width
                particle.y = height + margin
                angleDeg = 235f + random.nextFloat() * 70f
            }
        }
        val angle = Math.toRadians(angleDeg.toDouble()).toFloat()
        particle.vx = cos(angle) * speed
        particle.vy = sin(angle) * speed
        particle.px = particle.x
        particle.py = particle.y
        particle.state = STATE_BAT
        particle.maxLife = 0f
        particle.alpha = 1f
    }

    private fun initBatBody(particle: Particle) {
        particle.variant = random.nextFloat()
        particle.size = dp(8f + particle.variant * 8f)
        // 少量大蝙蝠制造前后层次
        if (random.nextFloat() < 0.2f) {
            particle.size = dp(16f + random.nextFloat() * 4f)
        }
        particle.spin = FLAP_BASE + random.nextFloat() * FLAP_RANGE
        particle.rotation = random.nextFloat() * KT_PI2
        particle.phase = random.nextFloat() * KT_PI2
    }

    private fun ensureWisps() {
        if (particles.any { it.state == STATE_WISP }) return
        repeat(scaledCount(9)) {
            spawn {
                state = STATE_WISP
                val margin = WISP_STEER_MARGIN * density
                x = margin + random.nextFloat() * (width - margin * 2f).coerceAtLeast(1f)
                y = margin + random.nextFloat() * (height * 0.8f - margin).coerceAtLeast(1f)
                px = x
                py = y
                size = dp(3f + random.nextFloat() * 2.4f)
                data = floatArrayOf(
                    1.4f + random.nextFloat() * 1.1f,
                    random.nextFloat() * DASH_COOLDOWN_MAX,
                    0f,
                    0f,
                    0f,
                    dp(60f + random.nextFloat() * 50f),
                    0f
                )
                data[3] = data[5]
                phase = random.nextFloat() * KT_PI2
                rotation = random.nextFloat() * KT_PI2
                alpha = 0.7f
                maxLife = 0f
                life = 0f
                colorIndex = WISP_COLOR
            }
        }
    }

    /** [from] 到 [to] 的最短角差（弧度，-π..π） */
    private fun shortestAngle(from: Float, to: Float): Float {
        val diff = (to - from) % KT_PI2
        return when {
            diff > Math.PI.toFloat() -> diff - KT_PI2
            diff < -Math.PI.toFloat() -> diff + KT_PI2
            else -> diff
        }
    }

    companion object {
        const val STATE_BAT = 0
        const val STATE_WISP = 1
        /** 被点击惊飞的蝙蝠：飞出屏幕后消亡而非重生 */
        const val STATE_STARTLED = 2
        /** 鬼火拖尾小光点 */
        const val STATE_TRAIL = 3
        /** 自底部螺旋升起的火焰余烬 */
        const val STATE_EMBER = 4
        /** 点击四溅的鬼火火花 */
        const val STATE_SPARK = 5
        const val WISP_COLOR = 0

        private const val BAT_MARGIN = 30f
        private const val BOB_FREQ = 2.2f
        private const val BOB_AMP = 14f
        private const val FLAP_BASE = 34f
        private const val FLAP_RANGE = 18f
        private const val MAX_STARTLED = 12
        private const val KT_PI2 = (Math.PI * 2.0).toFloat()

        /** 鬼火游走与窜跑：曲率漂移形成连绵弧线路径 */
        private const val MAX_CURVATURE = 0.004f
        private const val CURV_DRIFT = 0.02f
        private const val CURV_BIAS_FREQ = 0.3f
        private const val CURV_BIAS_LERP = 0.6f
        private const val DASH_SPEED_MULT = 3.2f
        private const val DASH_TURN_SCALE = 0.55f
        private const val DASH_TIME_MIN = 0.25f
        private const val DASH_TIME_MAX = 0.55f
        private const val DASH_COOLDOWN_MIN = 0.9f
        private const val DASH_COOLDOWN_MAX = 2.2f
        private const val SPEED_LERP = 6f
        private const val WISP_STEER_MARGIN = 48f

        /** 拖尾 */
        private const val TRAIL_INTERVAL = 0.12f
        private const val TRAIL_LIFE_MIN = 0.28f
        private const val TRAIL_LIFE_MAX = 0.5f

        /** 余烬 */
        private const val EMBER_INTERVAL_MIN = 0.4f
        private const val EMBER_INTERVAL_MAX = 0.85f
        private const val EMBER_LIFE_MIN = 2f
        private const val EMBER_LIFE_MAX = 4f
        private const val EMBER_SPIRAL_FREQ_MIN = 3.5f
        private const val EMBER_SPIRAL_FREQ_MAX = 7f
        private const val EMBER_FLICKER_FREQ = 17f
        private const val MAX_EMBERS = 24

        /** 火花 */
        private const val SPARK_DRAG = 2.2f
        private const val SPARK_GRAVITY = 380f
        private const val MAX_SPARKS = 40

        /** 振翅的瞬时翼尖摆动系数（-1..1），供绘制层计算翼形 */
        fun wingAngle(particle: Particle): Float = sin(particle.rotation)

        /** 蝙蝠纵向瞬时速度（px/s），供绘制层估算俯仰姿态 */
        fun bobVelocity(particle: Particle, elapsed: Float, density: Float): Float {
            return cos(elapsed * BOB_FREQ + particle.phase) * BOB_AMP * density *
                (0.6f + particle.variant * 0.8f)
        }
    }
}
