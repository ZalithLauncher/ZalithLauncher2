package com.movtery.guide

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.unit.IntSize
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.max
import kotlin.math.min

class GuideCalloutSolverTest {
    @Test
    fun `右侧底部入口的文字与引线互不重叠`() {
        assertValid(
            containerSize = IntSize(1280, 720),
            targets = Rect(900f, 580f, 1016f, 650f) to Rect(1018f, 580f, 1240f, 650f),
            labelSizes = IntSize(300, 90) to IntSize(340, 100),
        )
    }

    @Test
    fun `左侧底部入口自动换向且保持连接`() {
        assertValid(
            containerSize = IntSize(1280, 720),
            targets = Rect(40f, 580f, 156f, 650f) to Rect(158f, 580f, 380f, 650f),
            labelSizes = IntSize(300, 90) to IntSize(340, 100),
        )
    }

    @Test
    fun `紧凑屏幕使用分区候选避免遮挡`() {
        assertValid(
            containerSize = IntSize(800, 480),
            targets = Rect(500f, 340f, 558f, 400f) to Rect(560f, 340f, 760f, 400f),
            labelSizes = IntSize(240, 80) to IntSize(260, 90),
        )
    }

    @Test
    fun `单个标注同样连接目标外轮廓`() {
        val target = Rect(500f, 340f, 650f, 400f)
        val placement = solveGuideCallouts(
            targets = listOf(target),
            labelSizes = listOf(IntSize(240, 80)),
            containerSize = IntSize(800, 480),
            paddingPx = 16f,
            gapPx = 20f,
            labelSpacingPx = 12f,
        ).single()

        assertTrue(placement.targetPoint onBorderOf target)
        assertTrue(placement.labelPoint onBorderOf placement.labelRect)
        assertFalse(placement.labelRect.overlaps(target))
    }

    private fun assertValid(
        containerSize: IntSize,
        targets: Pair<Rect, Rect>,
        labelSizes: Pair<IntSize, IntSize>,
    ) {
        val placements = solveGuideCallouts(
            targets = targets.toList(),
            labelSizes = labelSizes.toList(),
            containerSize = containerSize,
            paddingPx = 16f,
            gapPx = 20f,
            labelSpacingPx = 12f,
        )
        val targetList = targets.toList()

        placements.forEachIndexed { index, placement ->
            assertTrue(placement.labelRect.left >= 16f)
            assertTrue(placement.labelRect.top >= 16f)
            assertTrue(placement.labelRect.right <= containerSize.width - 16f)
            assertTrue(placement.labelRect.bottom <= containerSize.height - 16f)
            assertFalse(placement.labelRect.overlaps(targetList[1 - index]))
            assertTrue(placement.targetPoint onBorderOf targetList[index])
            assertTrue(placement.labelPoint onBorderOf placement.labelRect)
            assertFalse(
                segmentIntersectsRect(
                    placement.targetPoint,
                    placement.labelPoint,
                    targetList[1 - index],
                )
            )
            assertFalse(
                segmentIntersectsRect(
                    placement.targetPoint,
                    placement.labelPoint,
                    placements[1 - index].labelRect,
                )
            )
        }
        assertFalse(placements.first().labelRect.overlaps(placements.last().labelRect))
        assertFalse(
            segmentsIntersect(
                placements.first().targetPoint,
                placements.first().labelPoint,
                placements.last().targetPoint,
                placements.last().labelPoint,
            )
        )
    }

    private infix fun Offset.onBorderOf(rect: Rect): Boolean =
        x >= rect.left && x <= rect.right && y >= rect.top && y <= rect.bottom &&
                (x == rect.left || x == rect.right || y == rect.top || y == rect.bottom)

    private fun segmentIntersectsRect(start: Offset, end: Offset, rect: Rect): Boolean {
        if (start in rect || end in rect) return true
        return segmentsIntersect(start, end, rect.topLeft, rect.topRight) ||
                segmentsIntersect(start, end, rect.topRight, rect.bottomRight) ||
                segmentsIntersect(start, end, rect.bottomRight, rect.bottomLeft) ||
                segmentsIntersect(start, end, rect.bottomLeft, rect.topLeft)
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

    private fun cross(a: Offset, b: Offset, point: Offset): Float =
        (b.x - a.x) * (point.y - a.y) - (b.y - a.y) * (point.x - a.x)
}
