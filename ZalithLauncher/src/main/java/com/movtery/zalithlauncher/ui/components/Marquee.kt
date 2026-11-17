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

package com.movtery.zalithlauncher.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.IntrinsicMeasurable
import androidx.compose.ui.layout.IntrinsicMeasureScope
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.node.DrawModifierNode
import androidx.compose.ui.node.LayoutModifierNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.constrainWidth
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.milliseconds

/** 滚动速度 */
private val MarqueeVelocity = 30.dp
/** 每轮滚动结束后的停留时长 */
private const val MarqueeDelayMillis = 1200
/** 淡化边缘长度 */
private val MarqueeFadeEdgeLength = 12.dp

/**
 * 通用跑马灯修饰符
 * @param iterations 剩余滚动次数，正数为次数，负数表示无限循环，0 不滚动
 * @param velocity 滚动速度，必须大于 0
 * @param repeatDelayMillis 每轮滚动结束后的停留时长
 * @param fadingEdges 是否绘制首尾淡化
 */
fun Modifier.marquee(
    iterations: Int = Int.MAX_VALUE,
    velocity: Dp = MarqueeVelocity,
    repeatDelayMillis: Int = MarqueeDelayMillis,
    fadingEdges: Boolean = true
): Modifier {
    require(velocity > 0.dp) { "velocity must be greater than zero." }
    require(repeatDelayMillis >= 0) { "repeatDelayMillis must not be negative." }
    val fadeLayer = if (fadingEdges) {
        Modifier.graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
    } else {
        Modifier
    }
    return this
        .then(fadeLayer)
        .then(MarqueeModifierElement(
            iterations = iterations,
            velocity = velocity,
            repeatDelayMillis = repeatDelayMillis,
            fadingEdges = fadingEdges
        ))
}

private data class MarqueeModifierElement(
    private val iterations: Int,
    private val velocity: Dp,
    private val repeatDelayMillis: Int,
    private val fadingEdges: Boolean
) : ModifierNodeElement<MarqueeModifierNode>() {
    override fun create() =
        MarqueeModifierNode(
            iterations = iterations,
            velocity = velocity,
            repeatDelayMillis = repeatDelayMillis,
            fadingEdges = fadingEdges,
        )
    override fun update(node: MarqueeModifierNode) =
        node.update(
            iterations = iterations,
            velocity = velocity,
            repeatDelayMillis = repeatDelayMillis,
            fadingEdges = fadingEdges,
        )
}

private class MarqueeModifierNode(
    var iterations: Int,
    var velocity: Dp,
    var repeatDelayMillis: Int,
    fadingEdges: Boolean
) : Modifier.Node(), LayoutModifierNode, DrawModifierNode {
    var fadingEdges by mutableStateOf(fadingEdges)

    private var contentWidth by mutableIntStateOf(0)
    private var containerWidth by mutableIntStateOf(0)
    private var isRunning by mutableStateOf(false)
    private var density = 1f

    private val offset = Animatable(0f)
    private var animationJob: Job? = null

    fun update(iterations: Int, velocity: Dp, repeatDelayMillis: Int, fadingEdges: Boolean) {
        this.iterations = iterations
        this.velocity = velocity
        this.repeatDelayMillis = repeatDelayMillis
        this.fadingEdges = fadingEdges
        restartAnimation()
    }

    override fun onAttach() {
        restartAnimation()
    }

    override fun onDetach() {
        animationJob?.cancel()
    }

    private fun restartAnimation() {
        val oldJob = animationJob
        oldJob?.cancel()
        if (isAttached) {
            animationJob = coroutineScope.launch {
                oldJob?.join()
                runMarquee()
            }
        }
    }

    private suspend fun runMarquee() {
        snapshotFlow {
            if (contentWidth <= containerWidth) null
            else contentWidth.toFloat() + containerWidth / 3f
        }.collectLatest { maxScroll ->
            if (maxScroll == null) {
                offset.snapTo(0f)
                isRunning = false
                return@collectLatest
            }
            try {
                isRunning = true
                delay(repeatDelayMillis.milliseconds)
                var remaining = iterations
                while (remaining != 0) {
                    if (remaining > 0) remaining--
                    val velocityPxPerMs = velocity.value * density / 1000f
                    offset.snapTo(0f)
                    offset.animateTo(
                        targetValue = maxScroll,
                        animationSpec = tween(
                            durationMillis = (maxScroll / velocityPxPerMs).roundToInt().coerceAtLeast(1),
                            easing = LinearEasing
                        )
                    )
                    offset.snapTo(0f)
                    if (remaining != 0) delay(repeatDelayMillis.milliseconds)
                }
            } finally {
                offset.snapTo(0f)
                isRunning = false
            }
        }
    }

    override fun MeasureScope.measure(
        measurable: Measurable,
        constraints: Constraints
    ): MeasureResult {
        val placeable = measurable.measure(constraints.copy(maxWidth = Constraints.Infinity))
        contentWidth = placeable.width
        this@MarqueeModifierNode.density = this@measure.density
        val container = constraints.constrainWidth(placeable.width)
        containerWidth = container
        return layout(container, placeable.height) {
            placeable.placeRelative(0, 0)
        }
    }

    override fun IntrinsicMeasureScope.minIntrinsicWidth(
        measurable: IntrinsicMeasurable,
        height: Int
    ): Int = 0

    override fun IntrinsicMeasureScope.maxIntrinsicWidth(
        measurable: IntrinsicMeasurable,
        height: Int
    ): Int = measurable.maxIntrinsicWidth(height)

    override fun IntrinsicMeasureScope.minIntrinsicHeight(
        measurable: IntrinsicMeasurable,
        width: Int
    ): Int = measurable.minIntrinsicHeight(Constraints.Infinity)

    override fun IntrinsicMeasureScope.maxIntrinsicHeight(
        measurable: IntrinsicMeasurable,
        width: Int
    ): Int = measurable.maxIntrinsicHeight(Constraints.Infinity)

    override fun ContentDrawScope.draw() {
        val contentWidthPx = contentWidth.toFloat()
        val containerWidthPx = containerWidth.toFloat()
        val offsetPx = offset.value
        val direction = if (layoutDirection == LayoutDirection.Rtl) -1f else 1f

        clipRect(0f, 0f, containerWidthPx, size.height) {
            translate(left = -direction * offsetPx) { this@draw.drawContent() }
            val gap = containerWidthPx / 3f
            val ghostStart = contentWidthPx - containerWidthPx + gap
            if (isRunning && offsetPx > ghostStart) {
                val ghostOffset = contentWidthPx + gap
                translate(left = direction * (ghostOffset - offsetPx)) {
                    this@draw.drawContent()
                }
            }
        }

        if (!fadingEdges) return

        val fadeLength = MarqueeFadeEdgeLength.toPx()
        if (fadeLength <= 0f || containerWidthPx <= 0f) return

        val maxLength = containerWidthPx / 2f
        val fadeStop = contentWidthPx + containerWidthPx / 6f

        val leftStrength = if (offsetPx <= fadeStop) (offsetPx / fadeLength).coerceIn(0f, 1f) else 0f
        val rightStrength = if (isRunning) {
            val maxFadeScroll = contentWidthPx - containerWidthPx + containerWidthPx / 3f + 2 * contentWidthPx
            ((maxFadeScroll - offsetPx) / fadeLength).coerceIn(0f, 1f)
        } else {
            ((contentWidthPx - containerWidthPx) / fadeLength).coerceIn(0f, 1f)
        }

        val leftWidth = leftStrength * fadeLength
        if (leftWidth > 1f) {
            drawRect(
                brush = Brush.horizontalGradient(
                    colors = listOf(Color.Black, Color.Transparent),
                    startX = 0f,
                    endX = leftWidth
                ),
                topLeft = Offset.Zero,
                size = Size(leftWidth.coerceAtMost(maxLength), size.height),
                blendMode = BlendMode.DstOut
            )
        }
        val rightWidth = rightStrength * fadeLength
        if (rightWidth > 1f) {
            drawRect(
                brush = Brush.horizontalGradient(
                    colors = listOf(Color.Transparent, Color.Black),
                    startX = containerWidthPx - rightWidth,
                    endX = containerWidthPx
                ),
                topLeft = Offset(containerWidthPx - rightWidth.coerceAtMost(maxLength), 0f),
                size = Size(rightWidth.coerceAtMost(maxLength), size.height),
                blendMode = BlendMode.DstOut
            )
        }
    }
}
