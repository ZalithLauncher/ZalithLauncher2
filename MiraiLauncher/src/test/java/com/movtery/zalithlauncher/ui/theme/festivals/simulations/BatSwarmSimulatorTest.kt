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

class BatSwarmSimulatorTest {

    private val width = 1080f
    private val height = 2340f
    private val density = 2.75f

    private fun stepSeconds(simulator: BatSwarmSimulator, seconds: Float, delta: Float = 1f / 60f) {
        var remaining = seconds
        while (remaining > 0f) {
            val step = minOf(delta, remaining)
            simulator.step(step)
            remaining -= step
        }
    }

    @Test
    fun `bat count stays stable while crossing the screen`() {
        val simulator = BatSwarmSimulator(Random(42))
        simulator.resize(width, height, density)
        stepSeconds(simulator, 30f)

        assertEquals(7, simulator.particles.count { it.state == BatSwarmSimulator.STATE_BAT })
        assertEquals(9, simulator.particles.count { it.state == BatSwarmSimulator.STATE_WISP })
    }

    @Test
    fun `bats travel in varied directions`() {
        val simulator = BatSwarmSimulator(Random(42))
        simulator.resize(width, height, density)
        stepSeconds(simulator, 2f)

        val bats = simulator.particles.filter { it.state == BatSwarmSimulator.STATE_BAT }
        // 方向多样性：左右两个朝向都存在，且不全是水平飞行
        assertTrue(bats.any { it.vx > 0f })
        assertTrue(bats.any { it.vx < 0f })
        assertTrue(bats.any { kotlin.math.abs(it.vy) > 20f })
    }

    @Test
    fun `bats stay near the screen between respawns`() {
        val simulator = BatSwarmSimulator(Random(42))
        simulator.resize(width, height, density)
        stepSeconds(simulator, 30f)

        // 环境蝙蝠穿出边界后立即重生，位置只在边距范围内可见
        simulator.particles
            .filter { it.state == BatSwarmSimulator.STATE_BAT }
            .forEach { bat ->
                assertTrue("bat x ${bat.x} out of bounds", bat.x in -120f..width + 120f)
                assertTrue("bat y ${bat.y} out of bounds", bat.y in -120f..height + 120f)
            }
    }

    @Test
    fun `startled bats scatter in all directions and vanish`() {
        val simulator = BatSwarmSimulator(Random(42))
        simulator.resize(width, height, density)
        stepSeconds(simulator, 2f)

        simulator.startleAt(width * 0.5f, height * 0.5f)
        val startled = simulator.particles
            .filter { it.state == BatSwarmSimulator.STATE_STARTLED }
        assertTrue("expected startled bats, was ${startled.size}", startled.size in 3..5)

        // 四散：速度方向不全落在同一个象限
        val quadrants = startled.map { bat ->
            if (bat.vx >= 0f) if (bat.vy >= 0f) 0 else 3 else if (bat.vy >= 0f) 1 else 2
        }.toSet()
        assertTrue("expected scattered directions, got $quadrants", quadrants.size >= 2)

        // 飞出屏幕后消亡，环境蝙蝠数量回归 7 只
        stepSeconds(simulator, 10f)
        assertEquals(0, simulator.particles.count { it.state == BatSwarmSimulator.STATE_STARTLED })
        assertEquals(7, simulator.particles.count { it.state == BatSwarmSimulator.STATE_BAT })
    }

    @Test
    fun `wisps wander within the screen and blink`() {
        val simulator = BatSwarmSimulator(Random(42))
        simulator.resize(width, height, density)
        stepSeconds(simulator, 30f)

        simulator.particles
            .filter { it.state == BatSwarmSimulator.STATE_WISP }
            .forEach { wisp ->
                assertTrue("wisp x ${wisp.x} out of bounds", wisp.x in -1f..width + 1f)
                assertTrue("wisp y ${wisp.y} out of bounds", wisp.y in -1f..height + 1f)
                assertTrue(wisp.alpha in 0.54f..0.91f)
            }
    }

    @Test
    fun `wisps occasionally dash faster`() {
        val simulator = BatSwarmSimulator(Random(42))
        simulator.resize(width, height, density)

        var maxSpeed = 0f
        repeat(60 * 30) {
            simulator.step(1f / 60f)
            simulator.particles
                .filter { it.state == BatSwarmSimulator.STATE_WISP }
                .forEach { maxSpeed = maxOf(maxSpeed, it.data[3]) }
        }

        // 窜跑时速度应显著高于基础游走速度（26-66dp/s * 2.75 密度）
        assertTrue("expected dash speed, max was $maxSpeed", maxSpeed > 66f * density * 2f)
    }

    @Test
    fun `wisps leave transient trails`() {
        val simulator = BatSwarmSimulator(Random(42))
        simulator.resize(width, height, density)
        stepSeconds(simulator, 5f)

        val trails = simulator.particles.filter { it.state == BatSwarmSimulator.STATE_TRAIL }
        assertTrue("expected trail particles", trails.isNotEmpty())
        trails.forEach { trail ->
            assertTrue(trail.life <= trail.maxLife)
        }
    }

    @Test
    fun `embers rise from the bottom and expire`() {
        val simulator = BatSwarmSimulator(Random(42))
        simulator.resize(width, height, density)

        var seen = 0
        repeat(60 * 20) {
            simulator.step(1f / 60f)
            val embers = simulator.particles.filter { it.state == BatSwarmSimulator.STATE_EMBER }
            assertTrue("too many embers: ${embers.size}", embers.size <= 24)
            seen = maxOf(seen, embers.size)
        }
        assertTrue("expected embers within 20s, peak was $seen", seen > 0)

        // 仿真停止推进后所有余烬均已到寿
        simulator.particles
            .filter { it.state == BatSwarmSimulator.STATE_EMBER }
            .forEach { ember -> assertTrue(ember.life <= ember.maxLife) }
    }

    @Test
    fun `tapping scatters sparks alongside startled bats`() {
        val simulator = BatSwarmSimulator(Random(42))
        simulator.resize(width, height, density)
        stepSeconds(simulator, 2f)

        simulator.startleAt(width * 0.5f, height * 0.5f)

        val bats = simulator.particles.count { it.state == BatSwarmSimulator.STATE_STARTLED }
        val sparks = simulator.particles.count { it.state == BatSwarmSimulator.STATE_SPARK }
        assertTrue("expected startled bats, was $bats", bats in 3..5)
        assertTrue("expected sparks, was $sparks", sparks in 8..14)

        // 火花寿命有限，全部渐隐后不残留
        stepSeconds(simulator, 4f)
        assertEquals(0, simulator.particles.count { it.state == BatSwarmSimulator.STATE_SPARK })
    }
}
