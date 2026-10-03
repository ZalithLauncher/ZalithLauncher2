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

import kotlin.math.roundToInt
import kotlin.random.Random

/** 粒子基础尺寸参考 */
private const val REFERENCE_AREA = 1080f * 2340f

class Particle {
    var x = 0f
    var y = 0f
    var px = 0f
    var py = 0f
    var vx = 0f
    var vy = 0f
    var size = 1f
    var alpha = 1f
    var life = 0f
    var maxLife = 0f
    var rotation = 0f
    var spin = 0f
    var phase = 0f
    var variant = 0f
    var colorIndex = 0
    var state = 0
    var data = FloatArray(0)
}

/**
 * 粒子模拟器
 */
abstract class ParticleSimulator(
    protected val random: Random
) {
    var width = 0f
        private set
    var height = 0f
        private set
    var density = 1f
        private set

    val particles = ArrayList<Particle>()

    /** 模拟累计时间（s） */
    protected var elapsed = 0f
        private set

    private val pool = ArrayDeque<Particle>()
    private val spawnQueue = ArrayDeque<(Particle) -> Unit>()

    /** 粒子数量随屏幕面积缩放 */
    protected val areaFactor: Float
        get() = if (width <= 0f || height <= 0f) 0f else (width * height / REFERENCE_AREA).coerceIn(0.5f, 1.5f)

    fun resize(width: Float, height: Float, density: Float) {
        this.width = width
        this.height = height
        this.density = density
    }

    fun step(deltaSeconds: Float) {
        if (deltaSeconds <= 0f || width <= 0f || height <= 0f) return
        elapsed += deltaSeconds
        onUpdate(deltaSeconds)
        drainSpawns()

        val iterator = particles.iterator()
        while (iterator.hasNext()) {
            val particle = iterator.next()
            if (isDead(particle)) {
                iterator.remove()
                pool.addLast(particle)
            }
        }
    }

    protected abstract fun onUpdate(deltaSeconds: Float)

    /**
     * 生成一个粒子
     */
    protected fun spawn(action: Particle.() -> Unit) {
        spawnQueue.addLast(action)
    }

    protected fun drainSpawns() {
        while (spawnQueue.isNotEmpty()) {
            val action = spawnQueue.removeFirst()
            val particle = pool.removeFirstOrNull() ?: Particle()
            resetParticle(particle)
            action(particle)
            particles.add(particle)
        }
    }

    protected open fun isDead(particle: Particle): Boolean {
        return particle.maxLife > 0f && particle.life >= particle.maxLife
    }

    /** 立即标记粒子为待回收 */
    protected fun markDead(particle: Particle) {
        particle.maxLife = 0.001f
        particle.life = 0.001f
    }

    protected fun scaledCount(base: Int): Int {
        return (base * areaFactor).roundToInt().coerceAtLeast(if (base >= 10) 8 else 2)
    }

    protected fun dp(value: Float): Float = value * density

    private fun resetParticle(particle: Particle) {
        particle.x = 0f
        particle.y = 0f
        particle.px = 0f
        particle.py = 0f
        particle.vx = 0f
        particle.vy = 0f
        particle.size = 1f
        particle.alpha = 1f
        particle.life = 0f
        particle.maxLife = 0f
        particle.rotation = 0f
        particle.spin = 0f
        particle.phase = 0f
        particle.variant = 0f
        particle.colorIndex = 0
        particle.state = 0
    }
}
