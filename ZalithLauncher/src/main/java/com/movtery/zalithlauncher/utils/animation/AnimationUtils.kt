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

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.movtery.zalithlauncher.setting.AllSettings

/**
 * 获取动画的持续时长
 */
fun getAnimateSpeed(): Int = calculateAnimationTime(
    AllSettings.launcherAnimateSpeed.state,
    1500,
    0.1f
)

/**
 * 获取根据动画倍速调整后的 delayMillis
 */
fun getAdjustedDelayMillis(baseDelayMillis: Int): Int {
    if (baseDelayMillis == 0) return 0
    val adjustedAnimationTime = calculateAnimationTime(
        AllSettings.launcherAnimateSpeed.state,
        baseDelayMillis
    )
    return adjustedAnimationTime
}

/**
 * 页面切换动画是否关闭
 */
fun isSwapAnimateClosed() = AllSettings.launcherSwapAnimateType.state == TransitionAnimationType.CLOSE

fun <E> getAnimateTween(
    delayMillis: Int = 0
): FiniteAnimationSpec<E> = getAnimateTween(
    durationMillis = getAnimateSpeed(),
    delayMillis = delayMillis
)

fun <E> getAnimateTween(
    durationMillis: Int,
    delayMillis: Int = 0
): FiniteAnimationSpec<E> = tween(
    durationMillis = durationMillis,
    delayMillis = delayMillis
)

fun <E> getAnimateTweenBounce(
    delayMillis: Int = 0
): FiniteAnimationSpec<E> = getAnimateTweenBounce(
    durationMillis = getAnimateSpeed(),
    delayMillis = delayMillis
)

fun <E> getAnimateTweenBounce(
    durationMillis: Int,
    delayMillis: Int = 0
): FiniteAnimationSpec<E> = tween(
    durationMillis = durationMillis,
    delayMillis = delayMillis,
    easing = BounceEasing
)

fun <E> getAnimateTweenJellyBounce(
    delayMillis: Int = 0
): FiniteAnimationSpec<E> = getAnimateTweenJellyBounce(
    durationMillis = getAnimateSpeed(),
    delayMillis = delayMillis
)

fun <E> getAnimateTweenJellyBounce(
    durationMillis: Int,
    delayMillis: Int = 0
): FiniteAnimationSpec<E> = tween(
    durationMillis = durationMillis,
    delayMillis = delayMillis,
    easing = JellyBounce
)

/**
 * 获取页面切换动画
 */
fun <E> getSwapAnimateTween(
    swapIn: Boolean,
    delayMillis: Int = 0
): FiniteAnimationSpec<E> {
    val adjustedDelayMillis = getAdjustedDelayMillis(delayMillis)
    return if (swapIn) {
        when (AllSettings.launcherSwapAnimateType.state) {
            TransitionAnimationType.CLOSE -> snap()
            TransitionAnimationType.BOUNCE -> getAnimateTweenBounce(adjustedDelayMillis)
            TransitionAnimationType.JELLY_BOUNCE -> getAnimateTweenJellyBounce(adjustedDelayMillis)
            else -> getAnimateTween(adjustedDelayMillis)
        }
    } else {
        getAnimateTween(adjustedDelayMillis)
    }
}

/**
 * 计算动画的幅度（计算targetValue）
 * 以5为基准，5对应targetValue本身
 *
 * 幅度0，大小为 targetValue * 0.5
 * 幅度5，大小为 targetValue * 1.0
 * 幅度10，大小为 targetValue * 1.5
 */
fun getTargetValueByAmplitude(
    targetValue: Dp,
    amplitude: Int = 5
): Dp {
    val safeAmplitude = amplitude.coerceIn(0, 10)

    val minScale = 0.5f
    val maxScale = 1.5f
    val baseAmplitude = 5

    val scale = if (safeAmplitude == baseAmplitude) {
        1.0f
    } else if (safeAmplitude < baseAmplitude) {
        minScale + (safeAmplitude.toFloat() / baseAmplitude) * (1.0f - minScale)
    } else {
        1.0f + ((safeAmplitude - baseAmplitude).toFloat() / (10 - baseAmplitude)) * (maxScale - 1.0f)
    }

    return (targetValue.value * scale).dp
}

@Composable
fun swapAnimateDpAsState(
    targetValue: Dp,
    swapIn: Boolean,
    amplitude: Int = AllSettings.launcherAnimateExtent.state,
    isHorizontal: Boolean = false,
    delayMillis: Int = 0
): State<Dp> {
    return if (!isSwapAnimateClosed()) {
        swapAnimateDpAsState(
            targetValue = targetValue,
            swapIn = swapIn,
            amplitude = amplitude,
            isHorizontal = isHorizontal,
            animationSpec = getSwapAnimateTween(swapIn, delayMillis = delayMillis)
        )
    } else {
        rememberUpdatedState(newValue = 0.dp)
    }
}

@Composable
fun swapAnimateDpAsState(
    targetValue: Dp,
    swapIn: Boolean,
    amplitude: Int = AllSettings.launcherAnimateExtent.state,
    isHorizontal: Boolean = false,
    animationSpec: AnimationSpec<Dp>
): State<Dp> {
    val value = if (swapIn) 0.dp
    else {
        getTargetValueByAmplitude(
            if (isHorizontal) targetValue / 2
            else targetValue,
            amplitude
        )
    }
    return animateDpAsState(
        targetValue = value,
        animationSpec = animationSpec
    )
}

/**
 * 计算根据倍速调整后的时间（毫秒）
 * @param minFactor 最快时相对于 baseTime 的缩放比例（0.25 = 最快时是 1/4 时间）
 * @return 根据倍速调整后的时间
 */
fun calculateAnimationTime(speed: Int, baseTime: Int, minFactor: Float = 0.25f): Int {
    val factor = 1f - (speed / 10f) * (1f - minFactor)
    return (baseTime * factor).toInt()
}