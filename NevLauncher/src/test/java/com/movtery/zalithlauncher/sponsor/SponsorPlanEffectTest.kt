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

package com.movtery.zalithlauncher.sponsor

import org.junit.Assert.assertEquals
import org.junit.Test

class SponsorPlanEffectTest {

    @Test
    fun `known plan prefixes map to their effects`() {
        assertEquals(SponsorPlanEffect.LAPIS, SponsorPlanEffect.of("c1e7e62e"))
        assertEquals(SponsorPlanEffect.RED_CAT_HAT, SponsorPlanEffect.of("c3a32536"))
        assertEquals(SponsorPlanEffect.GOLDEN_TOKEN, SponsorPlanEffect.of("c4b309b8"))
    }

    @Test
    fun `only the first eight characters are needed`() {
        assertEquals(SponsorPlanEffect.LAPIS, SponsorPlanEffect.of("c1e7e62e-arbitrary-suffix"))
        assertEquals(SponsorPlanEffect.RED_CAT_HAT, SponsorPlanEffect.of("c3a32536-arbitrary-suffix"))
        assertEquals(SponsorPlanEffect.GOLDEN_TOKEN, SponsorPlanEffect.of("c4b309b8-arbitrary-suffix"))
    }

    @Test
    fun `known prefixes are case insensitive`() {
        assertEquals(SponsorPlanEffect.LAPIS, SponsorPlanEffect.of("C1E7E62E-suffix"))
        assertEquals(SponsorPlanEffect.RED_CAT_HAT, SponsorPlanEffect.of("C3A32536-suffix"))
        assertEquals(SponsorPlanEffect.GOLDEN_TOKEN, SponsorPlanEffect.of("C4B309B8-suffix"))
    }

    @Test
    fun `custom and unknown plans use emerald`() {
        assertEquals(SponsorPlanEffect.EMERALD, SponsorPlanEffect.of(""))
        assertEquals(SponsorPlanEffect.EMERALD, SponsorPlanEffect.of("unknown-plan"))
        assertEquals(SponsorPlanEffect.EMERALD, SponsorPlanEffect.of("c1e7e62"))
    }
}
