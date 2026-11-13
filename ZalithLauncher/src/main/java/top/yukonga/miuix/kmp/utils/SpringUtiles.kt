// Copyright 2025, compose-miuix-ui contributors
// SPDX-License-Identifier: Apache-2.0

package top.yukonga.miuix.kmp.utils

import androidx.compose.runtime.withFrameNanos
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.isActive
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sign

internal object SpringMath {
    const val MAX_FRAME_DELTA_SECONDS = 0.016f
    const val MIN_FRAME_DELTA_SECONDS = 0.001f
    const val HIGH_VELOCITY_THRESHOLD = 5000.0
    const val CRITICAL_DAMPING_RATIO = 1.0f
    const val STANDARD_SPRING_PERIOD = 0.4f
    const val SLOWER_SPRING_PERIOD_FOR_HIGH_VELOCITY = 0.55f

    /**
     * Obtain the movable distance after converting damping
     *
     * Damping formula: x - x^2 + x^3/3
     * */
    fun obtainDampingDistance(normalizedInput: Float, range: Float): Float {
        val x = max(0.0f, min(normalizedInput, 1.0f)).toDouble()
        val dampedFactor = x - x.pow(2.0) + (x.pow(3.0) / 3.0)
        return (dampedFactor * range).toFloat()
    }

    /**
     * Retrieve the damping distance and restore it to the original movement distance
     *
     * Restore formula: range - (range^(2/3)) * (range - 3 * absPixelOffset)^(1/3)
     * */
    fun obtainTouchDistance(currentPixelOffset: Float, range: Float): Float {
        var absPixelOffset = abs(currentPixelOffset)
        val absMaxOffset = abs(obtainDampingDistance(1.0f, range))

        if (absPixelOffset <= 0f) return 0f
        if (absPixelOffset >= absMaxOffset) {
            absPixelOffset = absMaxOffset
        }

        val base = range - (3.0 * absPixelOffset)
        val part2 = range.toDouble().pow(2.0 / 3.0) * sign(base) * abs(base).pow(1.0 / 3.0)
        return (range - part2).toFloat()
    }
}

private class SpringOperator(dampingRatio: Float, naturalPeriod: Float) {
    private val dampingCoefficient: Double
    private val stiffnessOverMass: Double

    init {
        val angularFrequency = (2.0 * PI) / naturalPeriod
        stiffnessOverMass = angularFrequency * angularFrequency // k/m = ω^2
        dampingCoefficient = 2.0 * dampingRatio * angularFrequency // c/m = 2 * ζ * ω
    }

    /**
     * Calculate the new velocity using Euler's formula
     * */
    fun updateVelocity(
        currentVelocity: Double,
        deltaTime: Float,
        currentPosition: Double,
        targetPosition: Double,
    ): Double {
        val velocityDecayFactor = 1.0 - dampingCoefficient * deltaTime
        val velocityIncreaseFromSpring = stiffnessOverMass * (targetPosition - currentPosition) * deltaTime
        return currentVelocity * velocityDecayFactor + velocityIncreaseFromSpring
    }
}

internal class SpringEngine {
    private var springOperator: SpringOperator? = null
    var velocity: Double = 0.0
    var currentPos: Double = 0.0
    private var targetPos: Double = 0.0
    private var initialPos: Double = 0.0
    private var initialVelocity: Double = 0.0

    private fun isAtEquilibrium(): Boolean {
        if (initialPos < targetPos && currentPos > targetPos) {
            return true
        }
        if (initialPos <= targetPos || currentPos >= targetPos) {
            return (initialPos == targetPos && sign(initialVelocity) != sign(currentPos)) || abs(currentPos - targetPos) < 1.0
        }
        return true
    }

    fun start(startValue: Float, targetValue: Float, initialVel: Float) {
        currentPos = startValue.toDouble()
        initialPos = startValue.toDouble()
        targetPos = targetValue.toDouble()
        velocity = initialVel.toDouble()
        initialVelocity = initialVel.toDouble()

        springOperator = SpringOperator(
            SpringMath.CRITICAL_DAMPING_RATIO,
            if (abs(initialVel) > SpringMath.HIGH_VELOCITY_THRESHOLD) {
                SpringMath.SLOWER_SPRING_PERIOD_FOR_HIGH_VELOCITY
            } else {
                SpringMath.STANDARD_SPRING_PERIOD
            },
        )
    }

    fun step(deltaTime: Float): Boolean {
        val operator = springOperator ?: return false
        val dt = deltaTime.coerceIn(SpringMath.MIN_FRAME_DELTA_SECONDS, SpringMath.MAX_FRAME_DELTA_SECONDS)
        velocity = operator.updateVelocity(velocity, dt, currentPos, targetPos)
        currentPos += dt * velocity

        if (isAtEquilibrium()) {
            currentPos = targetPos
            velocity = 0.0
            return true
        }

        return false
    }
}

/**
 * Drive this spring from [startValue] to [targetValue] with [initialVelocity], calling [onFrame] each
 * frame until it settles (or is cancelled), then [onSettle]. Shared by both overscroll implementations.
 */
internal suspend fun SpringEngine.runSettleAnimation(
    startValue: Float,
    targetValue: Float = 0f,
    initialVelocity: Float,
    onFrame: (currentPos: Float) -> Unit,
    onSettle: () -> Unit,
) {
    start(startValue = startValue, targetValue = targetValue, initialVel = initialVelocity)
    var lastFrameTimeNanos = -1L
    var isFinished = false
    try {
        while (!isFinished && currentCoroutineContext().isActive) {
            isFinished = withFrameNanos { frameTimeNanos ->
                if (lastFrameTimeNanos == -1L) {
                    lastFrameTimeNanos = frameTimeNanos
                    return@withFrameNanos false
                }
                val dt = (frameTimeNanos - lastFrameTimeNanos) / 1_000_000_000f
                lastFrameTimeNanos = frameTimeNanos
                val finished = step(dt)
                onFrame(currentPos.toFloat())
                finished
            }
        }
    } finally {
        onSettle()
    }
}
