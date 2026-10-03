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

/** 持续型彩蛋效果种类 */
enum class FestivalEffectType {
    RAIN,
    SNOW,
    FIREWORKS,
    FIREWORKS_NATIONAL,
    FIREFLIES,
    BAT_SWARM,
    HEARTS;

    companion object {
        private val REGISTRY: Map<Festival, List<FestivalEffectType>> = mapOf(
            Festival.QING_MING to listOf(RAIN),
            Festival.CHRISTMAS to listOf(SNOW),
            Festival.NEW_YEAR to listOf(FIREWORKS),
            Festival.SPRING_FESTIVAL to listOf(FIREWORKS),
            Festival.NATIONAL_DAY to listOf(FIREWORKS_NATIONAL),
            Festival.MID_AUTUMN to listOf(FIREFLIES),
            Festival.HALLOWEEN to listOf(BAT_SWARM),
            Festival.VALENTINES to listOf(HEARTS),
            Festival.QIXI to listOf(HEARTS)
        )

        fun of(festivals: List<Festival>): List<FestivalEffectType> {
            return festivals
                .flatMap { festival -> REGISTRY[festival].orEmpty() }
                .distinct()
        }

        /** 该效果种类是否响应点击交互 */
        fun supportsTapBurst(type: FestivalEffectType): Boolean {
            return when (type) {
                FIREWORKS, FIREWORKS_NATIONAL, BAT_SWARM, HEARTS -> true
                else -> false
            }
        }
    }
}
