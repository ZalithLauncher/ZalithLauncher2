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

class HeartsSimulatorTest {

    private val width = 1080f
    private val height = 2340f
    private val density = 2.75f

    @Test
    fun `rising heart count stays stable while recycling`() {
        val simulator = HeartsSimulator(Random(42))
        simulator.resize(width, height, density)
        repeat(60) { simulator.step(1f / 60f) }
        assertEquals(42, simulator.particles.count { it.state == HeartsSimulator.STATE_RISING })

        repeat(60 * 120) { simulator.step(1f / 60f) }
        assertEquals(42, simulator.particles.count { it.state == HeartsSimulator.STATE_RISING })
    }

    @Test
    fun `hearts rise and stay within horizontal bounds`() {
        val simulator = HeartsSimulator(Random(42))
        simulator.resize(width, height, density)
        repeat(60) { simulator.step(1f / 60f) }

        val before = simulator.particles.filter { it.state == HeartsSimulator.STATE_RISING }.map { it.y }
        repeat(30) { simulator.step(1f / 60f) }
        val after = simulator.particles.filter { it.state == HeartsSimulator.STATE_RISING }.map { it.y }

        // 爱心整体上升
        assertTrue(before.zip(after).count { (y0, y1) -> y1 < y0 } > before.size / 2)
        simulator.particles.forEach { heart ->
            assertTrue("heart x ${heart.x} out of bounds", heart.x in -1f..width + 1f)
        }
    }

    @Test
    fun `tapped burst hearts appear then fade out`() {
        val simulator = HeartsSimulator(Random(42))
        simulator.resize(width, height, density)
        repeat(60) { simulator.step(1f / 60f) }

        simulator.burstHearts(width * 0.5f, height * 0.5f)
        val burst = simulator.particles.count { it.state == HeartsSimulator.STATE_BURSTING }
        assertTrue("expected burst hearts, was $burst", burst in 6..10)

        // 爆散的爱心寿命有限，全部渐隐后不残留
        repeat(60 * 4) { simulator.step(1f / 60f) }
        assertEquals(0, simulator.particles.count { it.state == HeartsSimulator.STATE_BURSTING })
        assertEquals(42, simulator.particles.count { it.state == HeartsSimulator.STATE_RISING })
    }
}
