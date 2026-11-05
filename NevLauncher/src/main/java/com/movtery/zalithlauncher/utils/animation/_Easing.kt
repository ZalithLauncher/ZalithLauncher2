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

package com.movtery.zalithlauncher.utils.animation

import androidx.compose.animation.core.Easing
import kotlin.math.cos
import kotlin.math.pow

/**
 * 平滑结束缓动
 */
class EaseOutFluent(private val power: Int) : Easing {
    override fun transform(fraction: Float): Float = 1f - (1f - fraction).coerceIn(0f, 1f).pow(power)
}

/**
 * 回弹结束缓动
 */
class EaseOutBack(power: Int) : Easing {
    private val exponent = 3f - power * 0.5f

    override fun transform(fraction: Float): Float {
        val t = fraction.coerceIn(0f, 1f)
        return 1f - (1f - t).pow(exponent) * cos(1.5f * Math.PI * t).toFloat()
    }
}

/**
 * 进场位移合成曲线
 */
val EnterTranslateEasing = Easing { fraction ->
    //进场位移第一段曲线
    val first = 5f * EaseOutFluent(3).transform((fraction * 350f / 250f).coerceAtMost(1f))
    //进场位移第二段曲线
    val second = 11f * EaseOutBack(3).transform(fraction)
    (first + second) / 16f
}