package com.movtery.guide

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.unit.IntSize
import kotlin.math.abs
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

internal data class GuideCalloutPlacement(
    val labelRect: Rect,
    val targetPoint: Offset,
    val labelPoint: Offset,
)

private enum class CalloutSide {
    Above, Below, Left, Right
}

private data class CalloutCandidate(
    val labelRect: Rect,
    val side: CalloutSide,
) {
    fun placement(target: Rect): GuideCalloutPlacement {
        val targetPoint: Offset
        val labelPoint: Offset
        when (side) {
            CalloutSide.Above -> {
                targetPoint = Offset(target.center.x, target.top)
                labelPoint = Offset(labelRect.center.x, labelRect.bottom)
            }
            CalloutSide.Below -> {
                targetPoint = Offset(target.center.x, target.bottom)
                labelPoint = Offset(labelRect.center.x, labelRect.top)
            }
            CalloutSide.Left -> {
                targetPoint = Offset(target.left, target.center.y)
                labelPoint = Offset(labelRect.right, labelRect.center.y)
            }
            CalloutSide.Right -> {
                targetPoint = Offset(target.right, target.center.y)
                labelPoint = Offset(labelRect.left, labelRect.center.y)
            }
        }
        return GuideCalloutPlacement(labelRect, targetPoint, labelPoint)
    }
}

internal fun solveGuideCallouts(
    targets: List<Rect>,
    labelSizes: List<IntSize>,
    containerSize: IntSize,
    paddingPx: Float,
    gapPx: Float,
    labelSpacingPx: Float,
): List<GuideCalloutPlacement> {
    require(targets.size == labelSizes.size)
    if (targets.isEmpty()) return emptyList()

    val candidates = targets.mapIndexed { index, target ->
        calloutCandidates(target, labelSizes[index], containerSize, paddingPx, gapPx)
    }
    var best: List<GuideCalloutPlacement>? = null
    var bestDistance = Double.MAX_VALUE

    fun search(index: Int, selected: List<GuideCalloutPlacement>, distance: Double) {
        if (distance >= bestDistance) return
        if (index == targets.size) {
            best = selected
            bestDistance = distance
            return
        }

        candidates[index].forEach { candidate ->
            val placement = candidate.placement(targets[index])
            if (targets.any { target -> placement.labelRect.overlaps(target) }) return@forEach
            if (targets.withIndex().any { (targetIndex, target) ->
                    targetIndex != index &&
                            segmentIntersectsRect(placement.targetPoint, placement.labelPoint, target)
                }
            ) {
                return@forEach
            }
            if (selected.any { other ->
                    placement.labelRect.inflate(labelSpacingPx / 2f).overlaps(other.labelRect) ||
                            segmentsIntersect(
                                placement.targetPoint,
                                placement.labelPoint,
                                other.targetPoint,
                                other.labelPoint,
                            ) ||
                            segmentIntersectsRect(
                                placement.targetPoint,
                                placement.labelPoint,
                                other.labelRect,
                            ) ||
                            segmentIntersectsRect(
                                other.targetPoint,
                                other.labelPoint,
                                placement.labelRect,
                            )
                }
            ) {
                return@forEach
            }
            search(
                index = index + 1,
                selected = selected + placement,
                distance = distance + placement.distanceTo(targets[index]),
            )
        }
    }
    search(0, emptyList(), 0.0)
    best?.let { return it }

    val fallback = mutableListOf<GuideCalloutPlacement>()
    candidates.forEachIndexed { index, candidatesForTarget ->
        val candidate = candidatesForTarget.firstOrNull { candidate0 ->
            fallback.none { other -> candidate0.labelRect.overlaps(other.labelRect) }
        } ?: candidatesForTarget.first()
        fallback += candidate.placement(targets[index])
    }
    return fallback
}

private fun GuideCalloutPlacement.distanceTo(target: Rect): Double = hypot(
    (labelRect.center.x - target.center.x).toDouble(),
    (labelRect.center.y - target.center.y).toDouble(),
)

private fun calloutCandidates(
    target: Rect,
    labelSize: IntSize,
    containerSize: IntSize,
    paddingPx: Float,
    gapPx: Float,
): List<CalloutCandidate> {
    val width = labelSize.width.toFloat()
    val height = labelSize.height.toFloat()
    val minX = paddingPx
    val minY = paddingPx
    val maxX = (containerSize.width - paddingPx - width).coerceAtLeast(minX)
    val maxY = (containerSize.height - paddingPx - height).coerceAtLeast(minY)

    fun rect(x: Float, y: Float): Rect {
        val left = x.coerceIn(minX, maxX)
        val top = y.coerceIn(minY, maxY)
        return Rect(left, top, left + width, top + height)
    }

    val nearby = listOf(
        CalloutSide.Above to rect(target.center.x - width / 2f, target.top - gapPx - height),
        CalloutSide.Below to rect(target.center.x - width / 2f, target.bottom + gapPx),
        CalloutSide.Left to rect(target.left - gapPx - width, target.center.y - height / 2f),
        CalloutSide.Right to rect(target.right + gapPx, target.center.y - height / 2f),
    ).map { (side, labelRect) ->
        CalloutCandidate(labelRect, side)
    }.filter { candidate ->
        !candidate.labelRect.overlaps(target)
    }

    val xPositions = listOf(
        minX,
        minX + (maxX - minX) / 4f,
        minX + (maxX - minX) / 2f,
        minX + (maxX - minX) * 3f / 4f,
        maxX,
        target.center.x - width / 2f,
    ).map { it.coerceIn(minX, maxX) }
    val yPositions = listOf(
        minY,
        minY + (maxY - minY) / 4f,
        minY + (maxY - minY) / 2f,
        minY + (maxY - minY) * 3f / 4f,
        maxY,
        target.center.y - height / 2f,
    ).map { it.coerceIn(minY, maxY) }
    val distributed = xPositions.flatMap { x ->
        yPositions.map { y -> rect(x, y) }
    }.filter { labelRect ->
        !labelRect.overlaps(target)
    }.map { labelRect ->
        CalloutCandidate(labelRect, sideBetween(target, labelRect))
    }

    return (nearby + distributed).distinctBy { candidate ->
        listOf(
            candidate.labelRect.left.roundToInt(),
            candidate.labelRect.top.roundToInt(),
            candidate.side,
        )
    }
}

private fun sideBetween(target: Rect, labelRect: Rect): CalloutSide {
    val candidates = buildList {
        if (labelRect.bottom <= target.top) add(CalloutSide.Above)
        if (labelRect.top >= target.bottom) add(CalloutSide.Below)
        if (labelRect.right <= target.left) add(CalloutSide.Left)
        if (labelRect.left >= target.right) add(CalloutSide.Right)
    }
    return candidates.minByOrNull { side ->
        when (side) {
            CalloutSide.Above -> abs(labelRect.center.x - target.center.x)
            CalloutSide.Below -> abs(labelRect.center.x - target.center.x)
            CalloutSide.Left -> abs(labelRect.center.y - target.center.y)
            CalloutSide.Right -> abs(labelRect.center.y - target.center.y)
        }
    } ?: CalloutSide.Above
}

private fun segmentsIntersect(a: Offset, b: Offset, c: Offset, d: Offset): Boolean {
    if (max(a.x, b.x) < min(c.x, d.x) || max(c.x, d.x) < min(a.x, b.x) ||
        max(a.y, b.y) < min(c.y, d.y) || max(c.y, d.y) < min(a.y, b.y)
    ) {
        return false
    }
    val abC = cross(a, b, c)
    val abD = cross(a, b, d)
    val cdA = cross(c, d, a)
    val cdB = cross(c, d, b)
    return ((abC <= 0f && abD >= 0f) || (abC >= 0f && abD <= 0f)) &&
            ((cdA <= 0f && cdB >= 0f) || (cdA >= 0f && cdB <= 0f))
}

private fun segmentIntersectsRect(start: Offset, end: Offset, rect: Rect): Boolean {
    if (start in rect || end in rect) return true
    val topLeft = rect.topLeft
    val topRight = rect.topRight
    val bottomLeft = rect.bottomLeft
    val bottomRight = rect.bottomRight
    return segmentsIntersect(start, end, topLeft, topRight) ||
            segmentsIntersect(start, end, topRight, bottomRight) ||
            segmentsIntersect(start, end, bottomRight, bottomLeft) ||
            segmentsIntersect(start, end, bottomLeft, topLeft)
}

private fun cross(a: Offset, b: Offset, point: Offset): Float =
    (b.x - a.x) * (point.y - a.y) - (b.y - a.y) * (point.x - a.x)
