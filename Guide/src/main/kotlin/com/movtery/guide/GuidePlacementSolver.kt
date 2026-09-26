package com.movtery.guide

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * 放置策略求解结果
 */
internal data class PlacementResult(
    val offset: IntOffset,
    val side: GuideSide?
)

/**
 * 全部锚点的包围盒
 */
internal fun List<Rect>.boundsUnion(): Rect {
    if (isEmpty()) return Rect.Zero
    var left = Float.MAX_VALUE
    var top = Float.MAX_VALUE
    var right = -Float.MAX_VALUE
    var bottom = -Float.MAX_VALUE
    forEach { rect ->
        left = min(left, rect.left)
        top = min(top, rect.top)
        right = max(right, rect.right)
        bottom = max(bottom, rect.bottom)
    }
    return Rect(left, top, right, bottom)
}

/**
 * 合成有效摆放策略：显式摆放策略优先，其次单锚点节点的方向推荐（映射为方向偏好），无推荐时自动求解
 */
internal fun resolvePlacement(
    placement: GuidePlacement,
    sideHint: GuideSide?,
    anchors: List<Rect>
): GuidePlacement = when {
    placement !is GuidePlacement.Auto -> placement
    anchors.size == 1 && sideHint != null -> PreferSide(sideHint)
    else -> GuidePlacement.Auto
}

/**
 * 全部锚点是否完全落在视口内
 */
internal fun anchorsVisibleIn(anchors: List<Rect>, containerSize: IntSize): Boolean =
    anchors.isNotEmpty() && anchors.all { anchor ->
        anchor.left >= 0f && anchor.top >= 0f &&
                anchor.right <= containerSize.width && anchor.bottom <= containerSize.height
    }

/**
 * 求解引导内容的摆放位置：
 * 以锚点包围盒的中心（多个锚点时即其中心点）为参照，内容尽可能贴近该点；
 * 不遮挡锚点、不出屏为硬约束，可行解中取内容中心距参照点最近者；
 * 全部不可行时按「锚点交叠面积 → 出屏量 → 距参照点距离」字典序兜底
 */
internal fun solvePlacement(
    placement: GuidePlacement,
    anchors: List<Rect>,
    contentSize: IntSize,
    containerSize: IntSize,
    layoutDirection: LayoutDirection,
    gapPx: Float,
    paddingPx: Float
): PlacementResult {
    if (placement is GuidePlacement.Fixed) {
        val base = placement.alignment.align(contentSize, containerSize, layoutDirection)
        return PlacementResult(base + placement.offset, side = null)
    }

    val width = contentSize.width.toFloat()
    val height = contentSize.height.toFloat()
    val bounds = anchors.boundsUnion()
    val anchorCenterX = bounds.left + bounds.width / 2f
    val anchorCenterY = bounds.top + bounds.height / 2f

    fun clamp(x: Float, y: Float): Pair<Float, Float> {
        val maxX = (containerSize.width - paddingPx - width).coerceAtLeast(paddingPx)
        val maxY = (containerSize.height - paddingPx - height).coerceAtLeast(paddingPx)
        return x.coerceIn(paddingPx, maxX) to y.coerceIn(paddingPx, maxY)
    }

    fun candidate(side: GuideSide): Pair<Float, Float> {
        val centerX = bounds.left + (bounds.width - width) / 2f
        val centerY = bounds.top + (bounds.height - height) / 2f
        val startLeft = layoutDirection == LayoutDirection.Ltr
        return when (side) {
            GuideSide.Above -> centerX to (bounds.top - gapPx - height)
            GuideSide.Below -> centerX to (bounds.bottom + gapPx)
            GuideSide.Start -> {
                val x = if (startLeft) bounds.left - gapPx - width else bounds.right + gapPx
                x to centerY
            }
            GuideSide.End -> {
                val x = if (startLeft) bounds.right + gapPx else bounds.left - gapPx - width
                x to centerY
            }
        }
    }

    val sides = listOf(GuideSide.Below, GuideSide.Above, GuideSide.Start, GuideSide.End)
    val ordered: List<GuideSide> = when (val p = placement) {
        is PreferSide -> listOf(p.side) + (sides - p.side)
        else -> sides
    }

    fun evaluate(side: GuideSide): Evaluated {
        val (x, y) = clamp(candidate(side).first, candidate(side).second)
        val rect = Rect(x, y, x + width, y + height)
        val overlap = anchors.fold(0f) { acc, anchor -> acc + anchor.overlapArea(rect) }
        val out = max(0f, paddingPx - x) + max(0f, x + width - (containerSize.width - paddingPx)) +
                max(0f, paddingPx - y) + max(0f, y + height - (containerSize.height - paddingPx))
        val distance = hypot(rect.center.x - anchorCenterX, rect.center.y - anchorCenterY).toDouble()
        return Evaluated(side, x, y, overlap, out, distance)
    }

    val candidates = ordered.map(::evaluate)
    val feasible = candidates.filter { it.overlap <= 0f && it.out <= 0f }
    val chosen = when {
        placement is PreferSide &&
                candidates.first().overlap <= 0f && candidates.first().out <= 0f -> candidates.first()
        feasible.isNotEmpty() -> feasible.minBy { it.distance }
        else -> candidates.minWith(compareBy({ it.overlap }, { it.out }, { it.distance }))
    }
    return PlacementResult(IntOffset(chosen.x.roundToInt(), chosen.y.roundToInt()), chosen.side)
}

/**
 * 单个候选位置
 */
private data class Evaluated(
    val side: GuideSide,
    val x: Float,
    val y: Float,
    val overlap: Float,
    val out: Float,
    val distance: Double
)

private fun Rect.overlapArea(other: Rect): Float {
    val left = maxOf(left, other.left)
    val top = maxOf(top, other.top)
    val right = minOf(right, other.right)
    val bottom = minOf(bottom, other.bottom)
    if (right <= left || bottom <= top) return 0f
    return (right - left) * (bottom - top)
}

private enum class Corner { BottomStart, BottomEnd, TopEnd, TopStart }

/**
 * 求解下一步提示的位置
 */
internal fun solveNextTip(
    tipSize: IntSize,
    containerSize: IntSize,
    paddingPx: Float,
    marginPx: Float,
    obstacles: List<Rect>
): IntOffset {
    if (tipSize.width <= 0 || tipSize.height <= 0) return IntOffset.Zero
    if (containerSize.width <= 0 || containerSize.height <= 0) return IntOffset.Zero

    fun cornerOffset(corner: Corner): IntOffset {
        val x = when (corner) {
            Corner.BottomStart, Corner.TopStart -> paddingPx
            Corner.BottomEnd, Corner.TopEnd -> containerSize.width - paddingPx - tipSize.width
        }
        val y = when (corner) {
            Corner.BottomStart, Corner.BottomEnd -> containerSize.height - paddingPx - tipSize.height
            Corner.TopEnd, Corner.TopStart -> paddingPx
        }
        return IntOffset(
            x.coerceIn(paddingPx, (containerSize.width - tipSize.width).toFloat().coerceAtLeast(paddingPx)).roundToInt(),
            y.coerceIn(paddingPx, (containerSize.height - tipSize.height).toFloat().coerceAtLeast(paddingPx)).roundToInt()
        )
    }

    fun occupied(offset: IntOffset): Boolean {
        val tipRect = Rect(
            offset.x.toFloat(),
            offset.y.toFloat(),
            (offset.x + tipSize.width).toFloat(),
            (offset.y + tipSize.height).toFloat()
        ).inflate(marginPx)
        return obstacles.any { obstacle -> tipRect.overlapArea(obstacle) > 0f }
    }

    val ordered = listOf(Corner.BottomStart, Corner.BottomEnd, Corner.TopEnd, Corner.TopStart)
    ordered.firstOrNull { !occupied(cornerOffset(it)) }?.let { return cornerOffset(it) }

    // 网格采样：步长为提示尺寸的一半，取中心离屏幕中心最远的空白位置
    val minX = paddingPx.roundToInt()
    val minY = paddingPx.roundToInt()
    val maxX = cornerOffset(Corner.BottomEnd).x
    val maxY = cornerOffset(Corner.BottomStart).y
    val stepX = (tipSize.width / 2).coerceAtLeast(1)
    val stepY = (tipSize.height / 2).coerceAtLeast(1)
    val centerX = containerSize.width / 2f
    val centerY = containerSize.height / 2f

    var best: IntOffset? = null
    var bestDistance = -1.0f
    var y = minY
    while (y <= maxY) {
        var x = minX
        while (x <= maxX) {
            val offset = IntOffset(x, y)
            if (!occupied(offset)) {
                val distance = hypot(
                    x + tipSize.width / 2f - centerX,
                    y + tipSize.height / 2f - centerY
                )
                if (distance > bestDistance) {
                    bestDistance = distance
                    best = offset
                }
            }
            x += stepX
        }
        y += stepY
    }
    return best ?: cornerOffset(Corner.BottomStart)
}
