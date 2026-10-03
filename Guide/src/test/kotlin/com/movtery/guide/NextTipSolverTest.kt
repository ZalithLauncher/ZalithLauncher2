package com.movtery.guide

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class NextTipSolverTest {
    private val tip = IntSize(200, 40)
    private val container = IntSize(1000, 800)

    private fun tipRect(offset: IntOffset, margin: Float = 0f): Rect =
        Rect(
            offset.x.toFloat(),
            offset.y.toFloat(),
            (offset.x + tip.width).toFloat(),
            (offset.y + tip.height).toFloat()
        ).inflate(margin)

    private fun Rect.overlaps(other: Rect): Boolean =
        left < other.right && right > other.left && top < other.bottom && bottom > other.top

    @Test
    fun `无障碍物时落在左下角`() {
        val offset = solveNextTip(
            tipSize = tip,
            containerSize = container,
            paddingPx = 24f,
            marginPx = 16f,
            obstacles = emptyList()
        )

        assertEquals(24, offset.x)
        assertEquals(800 - 24 - 40, offset.y)
    }

    @Test
    fun `左下被占时依次回落右下右上左上`() {
        val padding = 24f
        val bottomLeft = IntOffset(24, 800 - 24 - 40)
        val bottomRight = IntOffset(1000 - 24 - 200, 800 - 24 - 40)
        val topRight = IntOffset(1000 - 24 - 200, 24)
        val topLeft = IntOffset(24, 24)
        val margin = 16f

        val onlyBottomLeft = solveNextTip(
            tip, container, padding, margin, listOf(tipRect(bottomLeft, margin))
        )
        assertEquals(bottomRight, onlyBottomLeft)

        val exceptTopLeft = solveNextTip(
            tip, container, padding, margin,
            listOf(tipRect(bottomLeft, margin), tipRect(bottomRight, margin), tipRect(topRight, margin))
        )
        assertEquals(topLeft, exceptTopLeft)
    }

    @Test
    fun `四角全占时网格取离中心最远的空白位置`() {
        val margin = 16f
        val padding = 24f
        val obstacles = listOf(
            tipRect(IntOffset(24, 800 - 24 - 40), margin),
            tipRect(IntOffset(1000 - 24 - 200, 800 - 24 - 40), margin),
            tipRect(IntOffset(1000 - 24 - 200, 24), margin),
            tipRect(IntOffset(24, 24), margin)
        )

        val offset = solveNextTip(tip, container, padding, margin, obstacles)

        val rect = tipRect(offset, margin)
        assertFalse(obstacles.any { rect.overlaps(it) })
        // 网格采样（步长 = 提示尺寸一半）：左下角占用带上方的 (24, 664)
        // 是全部空白候选中提示中心离屏幕中心 (500, 400) 最远者
        assertEquals(IntOffset(24, 664), offset)
    }

    @Test
    fun `全部不可用时回落左下角`() {
        val obstacles = listOf(
            Rect(0f, 0f, 1000f, 800f) // 全屏障碍
        )

        val offset = solveNextTip(tip, container, 24f, 16f, obstacles)

        assertEquals(IntOffset(24, 800 - 24 - 40), offset)
    }

    @Test
    fun `外扩边距计入占用判定`() {
        // 障碍物与左下角提示仅相距 8dp，小于 16dp 边距，视为占用
        val obstacle = Rect(24f, 700f, 400f, 704f)
        val offset = solveNextTip(tip, container, 24f, 16f, listOf(obstacle))

        assertFalse(tipRect(offset, 16f).overlaps(obstacle))
    }
}
