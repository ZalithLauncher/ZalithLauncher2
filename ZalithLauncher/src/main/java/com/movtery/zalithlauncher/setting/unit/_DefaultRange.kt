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

package com.movtery.zalithlauncher.setting.unit

val DEFAULT_INT_RANGE: IntRange = Int.MIN_VALUE..Int.MAX_VALUE

val DEFAULT_FLOAT_RANGE: ClosedFloatingPointRange<Float> = Float.MIN_VALUE..Float.MAX_VALUE

val DEFAULT_LONG_RANGE: LongRange = Long.MIN_VALUE..Long.MAX_VALUE

fun IntRange.toFloatRange(): ClosedFloatingPointRange<Float> = start.toFloat()..endInclusive.toFloat()

val IntSettingUnit.floatRange: ClosedFloatingPointRange<Float>
    get() = valueRange.toFloatRange()

val NullableIntSettingUnit.floatRange: ClosedFloatingPointRange<Float>
    get() = valueRange.toFloatRange()