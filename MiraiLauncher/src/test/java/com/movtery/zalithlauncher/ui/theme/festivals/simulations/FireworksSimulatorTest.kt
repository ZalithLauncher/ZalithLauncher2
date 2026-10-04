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

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class FireworksSimulatorTest {

    private val width = 1080f
    private val height = 2340f
    private val density = 2.75f

    private fun stepSeconds(simulator: FireworksSimulator, seconds: Float, delta: Float = 1f / 60f) {
        var remaining = seconds
        while (remaining > 0f) {
            val step = minOf(delta, remaining)
            simulator.step(step)
            remaining -= step
        }
    }

    @Test
    fun `rocket rises then explodes into burst particles`() {
        val simulator = FireworksSimulator(Random(42))
        simulator.resize(width, height, density)

        simulator.burstAt(x = width / 2f, targetY = height * 0.3f)
        assertEquals(1, simulator.particles.count { it.state == FireworksSimulator.STATE_ROCKET })

        val startY = simulator.particles.first().y
        repeat(30) { simulator.step(1f / 60f) }
        assertTrue(simulator.particles.first { it.state == FireworksSimulator.STATE_ROCKET }.y < startY)

        // 火箭应在此时间内到达目标高度并炸开
        // （环境发射与齐射持续进行，火箭不会清零；以出现爆炸光点为准）
        stepSeconds(simulator, 4f)
        assertTrue(simulator.particles.any { it.state == FireworksSimulator.STATE_BURST })

        // 环境火箭会持续自发发射，不清空粒子；但所有光点均有有限寿命，不会残留僵尸粒子
        stepSeconds(simulator, 5f)
        simulator.particles
            .filter { it.state != FireworksSimulator.STATE_ROCKET }
            .forEach {
                assertTrue(it.maxLife > 0f)
                assertTrue(it.life <= it.maxLife)
            }
        assertTrue(simulator.particles.size < 200)
    }

    @Test
    fun `burst particles stay bounded and fade out`() {
        val simulator = FireworksSimulator(Random(42))
        simulator.resize(width, height, density)
        simulator.burstAt(width / 2f, height * 0.25f)
        stepSeconds(simulator, 2.5f)

        val bursts = simulator.particles.filter { it.state == FireworksSimulator.STATE_BURST }
        assertTrue(bursts.isNotEmpty())
        bursts.forEach { particle ->
            assertTrue(particle.alpha in 0f..1f)
            assertTrue(particle.life <= particle.maxLife)
            assertTrue(particle.x > -width && particle.x < width * 2f)
            assertTrue(particle.y < height * 1.5f)
        }
    }

    @Test
    fun `concurrent rockets are capped`() {
        val simulator = FireworksSimulator(Random(42))
        simulator.resize(width, height, density)

        repeat(10) { simulator.burstAt(width / 2f, height * 0.3f) }
        assertTrue(simulator.particles.count { it.state == FireworksSimulator.STATE_ROCKET } <= 5)
    }

    @Test
    fun `ambient launches keep running`() {
        val simulator = FireworksSimulator(Random(7))
        simulator.resize(width, height, density)
        stepSeconds(simulator, 30f)

        assertTrue(simulator.particles.isNotEmpty())
    }

    @Test
    fun `salvos raise concurrent rockets occasionally but stay capped`() {
        val simulator = FireworksSimulator(Random(42))
        simulator.resize(width, height, density)

        var maxConcurrent = 0
        repeat(60 * 60) {
            simulator.step(1f / 60f)
            maxConcurrent = maxOf(maxConcurrent, simulator.particles.count { it.state == FireworksSimulator.STATE_ROCKET })
        }

        // 齐射使偶发多枚同屏；上限始终受 MAX_ROCKETS 约束
        assertTrue("expected salvo concurrency, max was $maxConcurrent", maxConcurrent >= 2)
        assertTrue("rockets exceeded cap, max was $maxConcurrent", maxConcurrent <= 5)
    }

    @Test
    fun `grand mode produces larger bursts`() {
        val simulator = FireworksSimulator(Random(42), grand = true)
        simulator.resize(width, height, density)
        simulator.burstAt(width / 2f, height * 0.25f)

        // 隆重模式的单次爆发规模显著更大；统计峰值以覆盖分叉/双层等不同形态
        var maxTotal = 0
        repeat(150) {
            simulator.step(1f / 60f)
            maxTotal = maxOf(maxTotal, simulator.particles.size)
        }
        assertTrue("expected grand burst peak >= 50, was $maxTotal", maxTotal >= 50)
    }

    @Test
    fun `varied trajectories keep rockets on screen`() {
        val simulator = FireworksSimulator(Random(42))
        simulator.resize(width, height, density)

        repeat(60 * 60) {
            simulator.step(1f / 60f)
            simulator.particles
                .filter { it.state == FireworksSimulator.STATE_ROCKET }
                .forEach { rocket ->
                    // 出生位置在屏幕底部下方 dp(6)，故 y 下界放宽
                    assertTrue("rocket x ${rocket.x} out of bounds", rocket.x in -width * 0.05f..width * 1.05f)
                    assertTrue("rocket y ${rocket.y} out of bounds", rocket.y in -1f..height + 30f)
                }
        }
    }

    @Test
    fun `crossette split stage appears over time`() {
        val simulator = FireworksSimulator(Random(42))
        simulator.resize(width, height, density)

        var sawSplit = false
        repeat(60 * 120) {
            simulator.step(1f / 60f)
            if (simulator.particles.any { it.state == FireworksSimulator.STATE_SPLIT }) sawSplit = true
        }
        assertTrue("expected crossette primaries within 120s", sawSplit)
    }
}
