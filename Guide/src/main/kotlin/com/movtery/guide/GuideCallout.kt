package com.movtery.guide

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.milliseconds

/**
 * 一个标注目标
 * @param target 与引导层同坐标系的部件外轮廓
 * @param label 标注内容
 */
data class GuideCallout(
    val target: Rect,
    val label: @Composable () -> Unit,
)

/**
 * 标注式引导层
 *
 * 在 [content] 上放置任意数量的标注。
 * 引线由目标外轮廓边中点延伸至标注外轮廓边中点，入场时标注沿引线滑出。
 *
 * @param animations 引导动画配置，禁用时直接展示最终布局
 * @param screenPadding 标注与屏幕边缘的最小留白
 * @param targetGap 标注与目标部件的贴邻间距
 * @param labelSpacing 不同标注之间的最小间距
 * @param labelMaxWidth 单个标注的最大测量宽度
 * @param content 可选的演示内容，需要绝对定位时可使用根坐标偏移
 */
@Composable
fun GuideCalloutOverlay(
    callouts: List<GuideCallout>,
    modifier: Modifier = Modifier,
    animations: GuideAnimations = GuideAnimations.Default,
    screenPadding: Dp = GuideDefaults.screenPadding,
    targetGap: Dp = GuideDefaults.calloutGap,
    labelSpacing: Dp = GuideDefaults.calloutLabelSpacing,
    labelMaxWidth: Dp = GuideDefaults.calloutLabelMaxWidth,
    lineColor: Color = Color.White.copy(alpha = 0.9f),
    pointColor: Color = Color.White,
    lineWidth: Dp = GuideDefaults.calloutLineWidth,
    pointRadius: Dp = GuideDefaults.calloutPointRadius,
    content: @Composable () -> Unit = {},
) {
    val density = LocalDensity.current
    val paddingPx = with(density) { screenPadding.toPx() }
    val gapPx = with(density) { targetGap.toPx() }
    val labelSpacingPx = with(density) { labelSpacing.toPx() }
    val labelMaxWidthPx = with(density) { labelMaxWidth.toPx() }
    val lineWidthPx = with(density) { lineWidth.toPx() }
    val pointRadiusPx = with(density) { pointRadius.toPx() }
    var placements by remember { mutableStateOf<List<GuideCalloutPlacement>>(emptyList()) }

    val initialProgress = if (animations.enabled) 0f else 1f
    val calloutProgress = remember(callouts.size, animations.enabled) {
        List(callouts.size) {
            Animatable(initialProgress)
        }
    }
    val progressSpec: FiniteAnimationSpec<Float> = if (animations.enabled) {
        spring(
            dampingRatio = animations.spatialSpring.dampingRatio,
            stiffness = animations.spatialSpring.stiffness,
        )
    } else {
        snap()
    }
    LaunchedEffect(animations, callouts.size) {
        if (!animations.enabled) {
            calloutProgress.forEach { it.snapTo(1f) }
        } else {
            calloutProgress.forEachIndexed { index, progress ->
                launch {
                    delay((index * 60L).milliseconds) // 引导标注点间隔 60ms
                    progress.animateTo(
                        targetValue = 1f,
                        animationSpec = progressSpec
                    )
                }
            }
        }
    }

    Layout(
        modifier = modifier
            .fillMaxSize()
            .drawBehind {
                if (placements.size == calloutProgress.size) {
                    placements.forEachIndexed { index, placement ->
                        val progress = calloutProgress[index].value
                        val visibleProgress = progress.coerceIn(0f, 1f)
                        val lineEnd = Offset(
                            x = placement.targetPoint.x +
                                    (placement.labelPoint.x - placement.targetPoint.x) * progress,
                            y = placement.targetPoint.y +
                                    (placement.labelPoint.y - placement.targetPoint.y) * progress,
                        )
                        drawLine(
                            color = lineColor.copy(alpha = lineColor.alpha * visibleProgress),
                            start = placement.targetPoint,
                            end = lineEnd,
                            strokeWidth = lineWidthPx,
                            cap = StrokeCap.Round,
                        )
                        drawCircle(
                            color = pointColor.copy(alpha = pointColor.alpha * visibleProgress),
                            radius = pointRadiusPx * visibleProgress,
                            center = placement.targetPoint,
                        )
                        drawCircle(
                            color = pointColor.copy(alpha = pointColor.alpha * visibleProgress),
                            radius = pointRadiusPx * visibleProgress,
                            center = lineEnd,
                        )
                    }
                }
            },
        content = {
            Box { content() }
            callouts.forEach { callout ->
                callout.label()
            }
        },
    ) { measurables, constraints ->
        val demo = measurables[0].measure(
            Constraints(maxWidth = constraints.maxWidth, maxHeight = constraints.maxHeight)
        )
        val labelConstraints = Constraints(
            maxWidth = minOf(
                labelMaxWidthPx.roundToInt(),
                (constraints.maxWidth - paddingPx * 2).roundToInt().coerceAtLeast(0),
            ),
            maxHeight = (constraints.maxHeight - paddingPx * 2).roundToInt().coerceAtLeast(0),
        )
        val labels = measurables.drop(1).map { it.measure(labelConstraints) }
        val solved = solveGuideCallouts(
            targets = callouts.map(GuideCallout::target),
            labelSizes = labels.map { IntSize(it.width, it.height) },
            containerSize = IntSize(constraints.maxWidth, constraints.maxHeight),
            paddingPx = paddingPx,
            gapPx = gapPx,
            labelSpacingPx = labelSpacingPx,
        )
        if (placements != solved) placements = solved

        layout(constraints.maxWidth, constraints.maxHeight) {
            demo.place(0, 0)
            labels.forEachIndexed { index, label ->
                val placement = solved[index]
                label.placeWithLayer(
                    x = placement.labelRect.left.roundToInt(),
                    y = placement.labelRect.top.roundToInt(),
                ) {
                    val progress = calloutProgress[index].value
                    val remaining = 1f - progress
                    alpha = progress.coerceIn(0f, 1f)
                    translationX = (placement.targetPoint.x - placement.labelPoint.x) * remaining
                    translationY = (placement.targetPoint.y - placement.labelPoint.y) * remaining
                }
            }
        }
    }
}
