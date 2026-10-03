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
import org.junit.Assert.assertFalse
import org.junit.Test
import kotlin.random.Random

class RainSimulatorTest {

    private val width = 1080f
    private val height = 2340f
    private val density = 2.75f

    private fun stepSeconds(simulator: RainSimulator, seconds: Float, delta: Float = 1f / 60f) {
        var remaining = seconds
        while (remaining > 0f) {
            val step = minOf(delta, remaining)
            simulator.step(step)
            remaining -= step
        }
    }

    @Test
    fun `rain drops fill screen and stay bounded`() {
        val simulator = RainSimulator(Random(42))
        simulator.resize(width, height, density)
        stepSeconds(simulator, 10f)

        val drops = simulator.particles.filter { it.state == RainSimulator.STATE_DROP }
        assertEquals(110, drops.size)
        simulator.particles.forEach { particle ->
            assertFalse(particle.x.isNaN() || particle.y.isNaN())
            assertFalse(particle.vx.isNaN() || particle.vy.isNaN())
        }
        drops.forEach { drop ->
            assert(drop.y <= height + drop.size)
            assert(drop.x > -width * 0.3f && drop.x < width * 1.3f)
        }
    }

    @Test
    fun `splashes expire over time`() {
        val simulator = RainSimulator(Random(7))
        simulator.resize(width, height, density)
        stepSeconds(simulator, 5f)

        val splash = simulator.particles.firstOrNull { it.state == RainSimulator.STATE_SPLASH }
        if (splash != null) {
            assert(splash.maxLife > 0f)
            assert(splash.life <= splash.maxLife)
        }
        // 足够长的时间后不应有超龄粒子残留
        stepSeconds(simulator, 5f)
        simulator.particles
            .filter { it.state == RainSimulator.STATE_SPLASH }
            .forEach { assert(it.life <= it.maxLife) }
    }

}
