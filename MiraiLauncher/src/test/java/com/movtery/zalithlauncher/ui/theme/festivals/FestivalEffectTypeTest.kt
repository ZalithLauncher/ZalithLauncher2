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

package com.movtery.zalithlauncher.ui.theme.festivals

import com.movtery.zalithlauncher.utils.festival.Festival
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FestivalEffectTypeTest {

    @Test
    fun `festivals map to their registered effects`() {
        assertEquals(
            listOf(FestivalEffectType.RAIN),
            FestivalEffectType.of(listOf(Festival.QING_MING))
        )
        assertEquals(
            listOf(FestivalEffectType.SNOW),
            FestivalEffectType.of(listOf(Festival.CHRISTMAS))
        )
        assertEquals(
            listOf(FestivalEffectType.FIREWORKS),
            FestivalEffectType.of(listOf(Festival.NEW_YEAR, Festival.SPRING_FESTIVAL))
        )
        assertEquals(
            listOf(FestivalEffectType.FIREWORKS_NATIONAL),
            FestivalEffectType.of(listOf(Festival.NATIONAL_DAY))
        )
        assertEquals(
            listOf(FestivalEffectType.FIREFLIES),
            FestivalEffectType.of(listOf(Festival.MID_AUTUMN))
        )
        assertEquals(
            listOf(FestivalEffectType.BAT_SWARM),
            FestivalEffectType.of(listOf(Festival.HALLOWEEN))
        )
        assertEquals(
            listOf(FestivalEffectType.HEARTS),
            FestivalEffectType.of(listOf(Festival.VALENTINES, Festival.QIXI))
        )
    }

    @Test
    fun `repeated effect types are deduplicated`() {
        val types = FestivalEffectType.of(
            listOf(Festival.QING_MING, Festival.CHRISTMAS, Festival.QING_MING)
        )
        assertEquals(listOf(FestivalEffectType.RAIN, FestivalEffectType.SNOW), types)
    }

    @Test
    fun `festivals without effects produce no result`() {
        assertTrue(FestivalEffectType.of(listOf(Festival.DRAGON_BOAT, Festival.APRIL_FOOLS)).isEmpty())
        assertTrue(FestivalEffectType.of(emptyList()).isEmpty())
    }
}
