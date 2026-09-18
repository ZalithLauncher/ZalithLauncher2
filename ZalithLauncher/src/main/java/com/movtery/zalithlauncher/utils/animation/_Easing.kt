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

package com.movtery.zalithlauncher.utils.animation

import androidx.compose.animation.core.Easing
import kotlin.math.cos
import kotlin.math.exp

val JellyBounce = Easing { t ->
    // 计算基于阻尼余弦模型的动画插值值
    // (1 - ...) ：让动画最终收敛到 1（目标位置）
    // 0.6f       ：振幅 A = 0.6，决定初始弹性强度，0.6 即 60% 弹性
    // exp(-8 * t)：指数衰减项，控制震荡随时间快速减弱
    // cos(6 * π * t)：余弦波形，制造回弹感，共约 3 个完整震荡周期
    // 最终返回值在 [0,1] 附近轻微过冲
    (1 - 0.6f * exp(-8 * t) * cos(6 * Math.PI * t)).toFloat()
}

/**
 * @see android.view.animation.BounceInterpolator
 */
val BounceEasing: Easing = Easing { t ->
    fun bounce(x: Float): Float = x * x * 8.0f

    var input = t * 1.1226f
    when {
        input < 0.3535f -> bounce(input)
        input < 0.7408f -> bounce(input - 0.54719f) + 0.7f
        input < 0.9644f -> bounce(input - 0.8526f) + 0.9f
        else -> bounce(input - 1.0435f) + 0.95f
    }
}