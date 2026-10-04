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

class FirefliesSimulatorTest {

    private val width = 1080f
    private val height = 2340f
    private val density = 2.75f

    @Test
    fun `flies stay on screen and blink`() {
        val simulator = FirefliesSimulator(Random(42))
        simulator.resize(width, height, density)
        repeat(60 * 120) { simulator.step(1f / 60f) }

        assertEquals(22, simulator.flies.size)
        simulator.flies.forEach { fly ->
            assertTrue("fly x ${fly.x} out of bounds", fly.x in -1f..width + 1f)
            assertTrue("fly y ${fly.y} out of bounds", fly.y in -1f..height + 1f)
            assertTrue(fly.alpha in 0.22f..1f)
        }
    }

    @Test
    fun `moon follows canvas size and breathes`() {
        val simulator = FirefliesSimulator(Random(42))
        simulator.resize(width, height, density)
        repeat(60) { simulator.step(1f / 60f) }

        assertEquals(width * 0.78f, simulator.moonX, 0.01f)
        assertEquals(height * 0.16f, simulator.moonY, 0.01f)
        assertTrue(simulator.moonRadius > 0f)
        assertTrue(simulator.moonGlow in 0.85f..1f)
    }

}
