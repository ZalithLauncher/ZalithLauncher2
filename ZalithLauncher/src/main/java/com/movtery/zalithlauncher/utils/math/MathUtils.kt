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

package com.movtery.zalithlauncher.utils.math

import java.math.BigDecimal

/**
 * Find the object T with the closest (or higher) value compared to targetValue
 * [Modified from PojavLauncher](https://github.com/PojavLauncherTeam/PojavLauncher/blob/045018f/app_pojavlauncher/src/main/java/net/kdt/pojavlaunch/utils/MathUtils.java#L28-L44)
 * @param targetValue the target value
 * @param objects the list of objects that the search will be performed on
 * @param valueProvider the provider for each values
 * @return the RankedValue that wraps the object which has the closest value to targetValue, or null if values of all
 *         objects are less than targetValue
 * @param <T> the object type that is used for the search.
 */
fun <T> findNearestPositive(
    targetValue: Int,
    objects: List<T>,
    valueProvider: (T) -> Int
): RankedValue<T>? {
    var minDelta = Int.MAX_VALUE
    var selectedObject: T? = null

    for (obj in objects) {
        val value = valueProvider(obj)
        if (value < targetValue) continue

        val delta = value - targetValue
        if (delta == 0) return RankedValue(obj, 0)
        if (delta < minDelta) {
            minDelta = delta
            selectedObject = obj
        }
    }

    return selectedObject?.let { RankedValue(it, minDelta) }
}

fun Float.addBigDecimal(other: Float): Float =
    BigDecimal(this.toDouble()).add(BigDecimal(other.toDouble())).toFloat()

fun Float.subtractBigDecimal(other: Float): Float =
    BigDecimal(this.toDouble()).subtract(BigDecimal(other.toDouble())).toFloat()

fun Float.multiplyBigDecimal(other: Float): Float =
    BigDecimal(this.toDouble()).multiply(BigDecimal(other.toDouble())).toFloat()

fun Float.divideBigDecimal(other: Float): Float =
    BigDecimal(this.toDouble()).divide(BigDecimal(other.toDouble())).toFloat()