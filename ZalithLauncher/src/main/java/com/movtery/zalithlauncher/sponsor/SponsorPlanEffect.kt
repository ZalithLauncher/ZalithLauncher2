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

enum class SponsorPlanEffect {
    LAPIS,
    RED_CAT_HAT,
    GOLDEN_TOKEN,
    EMERALD;

    companion object {
        private const val LAPIS_PREFIX = "c1e7e62e"
        private const val RED_CAT_HAT_PREFIX = "c3a32536"
        private const val GOLDEN_TOKEN_PREFIX = "c4b309b8"

        fun of(planId: String): SponsorPlanEffect = when {
            planId.startsWith(LAPIS_PREFIX, ignoreCase = true) -> LAPIS
            planId.startsWith(RED_CAT_HAT_PREFIX, ignoreCase = true) -> RED_CAT_HAT
            planId.startsWith(GOLDEN_TOKEN_PREFIX, ignoreCase = true) -> GOLDEN_TOKEN
            else -> EMERALD
        }
    }
}
