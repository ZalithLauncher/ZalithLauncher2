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

class SnowSimulatorTest {

    private val width = 1080f
    private val height = 2340f
    private val density = 2.75f

    @Test
    fun `flake count stays stable while recycling`() {
        val simulator = SnowSimulator(Random(42))
        simulator.resize(width, height, density)
        val delta = 1f / 60f

        repeat(60 * 60) { simulator.step(delta) }
        assertEquals(95, simulator.particles.size)

        repeat(60 * 60) { simulator.step(delta) }
        assertEquals(95, simulator.particles.size)
    }

    @Test
    fun `flakes fall downwards`() {
        val simulator = SnowSimulator(Random(42))
        simulator.resize(width, height, density)
        repeat(60) { simulator.step(1f / 60f) }

        val before = simulator.particles.map { it.y }
        repeat(15) { simulator.step(1f / 60f) }
        val after = simulator.particles.map { it.y }

        // 相同索引下雪片总体下落（正弦摆动不改变整体趋势）
        assertTrue(before.zip(after).count { (y0, y1) -> y1 > y0 } > before.size / 2)
    }

}
