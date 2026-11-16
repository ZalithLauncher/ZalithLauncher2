/*
 * Zalith Launcher 2
 * Copyright (C) 2025-2026 MovTery <movtery228@qq.com> and contributors
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

package com.movtery.zalithlauncher.ui.components

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PetCameraFitTest {
    @Test
    fun `default framing keeps legacy distance`() {
        assertEquals(1f, calculatePetCameraDistance(440, 440, 0f), 1e-6f)
        assertEquals(1f, calculatePetCameraDistance(200, 1000, 0f), 1e-6f)
    }

    @Test
    fun `short viewport pulls camera back to fit height`() {
        assertEquals(5f, calculatePetCameraDistance(1000, 200, 0f), 1e-6f)
    }

    @Test
    fun `safe area reserves every edge`() {
        assertEquals(1.1f, calculatePetCameraDistance(440, 440, 20f), 1e-6f)
    }

    @Test
    fun `invalid sizes and margins stay finite`() {
        assertEquals(1f, calculatePetCameraDistance(0, 100, 8f), 1e-6f)
        assertTrue(calculatePetCameraDistance(100, 80, Float.NaN).isFinite())
        assertTrue(calculatePetCameraDistance(100, 80, 1000f).isFinite())
        assertEquals(1f, calculatePetCameraDistance(440, 440, -1f), 1e-6f)
    }
}
