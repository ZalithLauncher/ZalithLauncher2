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

package com.movtery.cardgrid.engine

import androidx.compose.ui.unit.IntOffset
import com.movtery.cardgrid.model.CardLimits
import com.movtery.cardgrid.model.CardRect
import com.movtery.cardgrid.model.CardSizeClass
import com.movtery.cardgrid.model.ResizeEdge
import com.movtery.cardgrid.model.computeGridGeometry
import com.movtery.cardgrid.model.deriveSizeClass
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Random
import kotlin.math.abs

class GridEngineTest {

    private fun card(
        id: String,
        x: Int,
        y: Int,
        width: Int,
        height: Int
    ) = CardRect(id = id, x = x, y = y, width = width, height = height)

    // ---------- 网格几何 ----------

    @Test
    fun testGridColumnsAlwaysEven() {
        // 500dp / 20dp = 25 → 取最近的偶数 26
        assertEquals(26, computeGridGeometry(500f).columns)
        // 300dp / 20dp = 15 → 取最近的偶数 16
        assertEquals(16, computeGridGeometry(300f).columns)
        // 199dp / 20dp ≈ 10 → 保持偶数
        assertEquals(10, computeGridGeometry(199f).columns)
    }

    @Test
    fun testGridEdgesAlignToContainer() {
        val geometry = computeGridGeometry(500f)
        assertEquals(500f, geometry.columns * geometry.cellSize, 0.01f)
        // 单元格边长保持在 20dp 附近
        assertTrue(abs(geometry.cellSize - 20f) < 2f)
    }

    @Test
    fun testGridMinimumColumns() {
        assertEquals(4, computeGridGeometry(30f).columns)
        assertEquals(4, computeGridGeometry(0f).columns)
        assertEquals(4, computeGridGeometry(-10f).columns)
    }

    // ---------- 形态分类 ----------

    @Test
    fun testDeriveSizeClassHeightBySpan() {
        val columns = 16
        //高度：≤4 拥挤、=5 小、6..7 中、8..9 大、≥10 超大
        assertEquals(CardSizeClass.COMPACT, deriveSizeClass(10, 2, columns).height)
        assertEquals(CardSizeClass.COMPACT, deriveSizeClass(10, 4, columns).height)
        assertEquals(CardSizeClass.SMALL, deriveSizeClass(10, 5, columns).height)
        assertEquals(CardSizeClass.MEDIUM, deriveSizeClass(10, 6, columns).height)
        assertEquals(CardSizeClass.MEDIUM, deriveSizeClass(10, 7, columns).height)
        assertEquals(CardSizeClass.LARGE, deriveSizeClass(10, 8, columns).height)
        assertEquals(CardSizeClass.LARGE, deriveSizeClass(10, 9, columns).height)
        assertEquals(CardSizeClass.EXTRA_LARGE, deriveSizeClass(10, 10, columns).height)
        assertEquals(CardSizeClass.EXTRA_LARGE, deriveSizeClass(10, 20, columns).height)
    }

    @Test
    fun testDeriveSizeClassWidthByFraction() {
        val columns = 16
        //宽度按占网格宽度的比例划分：每档覆盖 16/5 列
        assertEquals(CardSizeClass.COMPACT, deriveSizeClass(3, 8, columns).width)
        assertEquals(CardSizeClass.SMALL, deriveSizeClass(4, 8, columns).width)
        assertEquals(CardSizeClass.SMALL, deriveSizeClass(6, 8, columns).width)
        assertEquals(CardSizeClass.MEDIUM, deriveSizeClass(7, 8, columns).width)
        assertEquals(CardSizeClass.LARGE, deriveSizeClass(10, 8, columns).width)
        assertEquals(CardSizeClass.LARGE, deriveSizeClass(12, 8, columns).width)
        //占比 ≥ 4/5 即为最高档
        assertEquals(CardSizeClass.EXTRA_LARGE, deriveSizeClass(13, 8, columns).width)
        //占满整行即为最高档
        assertEquals(CardSizeClass.EXTRA_LARGE, deriveSizeClass(16, 8, columns).width)
        //高度仍按绝对跨度划分
        assertEquals(CardSizeClass.MEDIUM, deriveSizeClass(16, 7, columns).height)
    }

    @Test
    fun testDeriveSizeClassDimensionsIndependent() {
        val columns = 16
        //宽度与高度独立分级，互不影响
        val size = deriveSizeClass(width = 3, height = 10, columns = columns)
        assertEquals(CardSizeClass.COMPACT, size.width)
        assertEquals(CardSizeClass.EXTRA_LARGE, size.height)

        val square = deriveSizeClass(width = 7, height = 7, columns = columns)
        assertEquals(CardSizeClass.MEDIUM, square.width)
        assertEquals(CardSizeClass.MEDIUM, square.height)
    }

    // ---------- 最近空位搜索 ----------

    @Test
    fun testFindNearestSlotPrefersSidewaysWhenBlockedBelow() {
        // 原位与正下方均被占用，左侧 (0,0) 成为最近空位
        val obstacles = listOf(
            card("block1", 8, 0, 8, 4),
            card("block2", 8, 4, 8, 4)
        )
        val slot = GridEngine.findNearestFreeSlot(8, 4, IntOffset(8, 0), 16, obstacles)
        assertEquals(IntOffset(0, 0), slot)
    }

    @Test
    fun testFindNearestSlotFallsBelowFullRow() {
        // 整行被占用，只能落到下一行
        val obstacles = listOf(card("block", 0, 0, 16, 4))
        val slot = GridEngine.findNearestFreeSlot(16, 4, IntOffset(0, 0), 16, obstacles)
        assertEquals(IntOffset(0, 4), slot)
    }

    @Test
    fun testFindNearestSlotGuaranteedBelowAllObstacles() {
        // 纵向堆满的障碍，解落在所有障碍物下方
        val obstacles = listOf(
            card("a", 0, 0, 4, 4),
            card("b", 0, 4, 4, 4)
        )
        val slot = GridEngine.findNearestFreeSlot(4, 4, IntOffset(0, 4), 4, obstacles)
        assertEquals(IntOffset(0, 8), slot)
    }

    @Test
    fun testFindNearestSlotImpossibleWidth() {
        assertNull(
            GridEngine.findNearestFreeSlot(20, 4, IntOffset(0, 0), 16, emptyList())
        )
    }

    // ---------- 挤压结算 ----------

    @Test
    fun testDisplacedCardSlidesSidewaysWithoutCascade() {
        val columns = 16
        val moving = card("A", 0, 0, 8, 4)
        val others = listOf(
            card("B", 4, 0, 8, 4),   // 与 A 重叠，将被挤压
            card("C", 0, 4, 8, 4)    // 未被重叠，不允许被级联影响
        )
        val result = GridEngine.resolveDisplacements(moving, columns, others)
        // B 滑到 A 右侧，尺寸不变
        assertEquals(card("B", 8, 0, 8, 4), result["B"])
        // C 完全不受影响
        assertFalse(result.containsKey("C"))
    }

    @Test
    fun testDisplacedCardFallsToNextRowWhenRowFull() {
        val columns = 16
        val moving = card("A", 0, 0, 16, 4)
        val others = listOf(card("B", 0, 0, 8, 4))
        val result = GridEngine.resolveDisplacements(moving, columns, others)
        // 行内无处可去，B 落到下一行
        assertEquals(card("B", 0, 4, 8, 4), result["B"])
    }

    @Test
    fun testDragOverlapsTwoCardsDisplacesBoth() {
        val columns = 16
        // 拖动挤压同时压到左右两张卡
        val moving = card("B", 4, 0, 8, 4)
        val others = listOf(
            card("L", 0, 0, 6, 4),
            card("R", 10, 0, 6, 4)
        )
        val result = GridEngine.resolveDisplacements(moving, columns, others)
        assertEquals(2, result.size)
        val settled = others.map { result[it.id] ?: it } + moving
        assertFalse(GridEngine.hasOverlap(settled))
    }

    @Test
    fun testDisplacementDirectionPicksDominantSide() {
        // 指针压到卡片中心的哪一侧，卡片就向相反方向让开
        val b = card("B", 4, 4, 4, 4)
        // 指针在中心左侧 → 向右让
        assertEquals(IntOffset(1, 0), GridEngine.displacementDirection(IntOffset(3, 6), b))
        // 指针在中心右侧 → 向左让
        assertEquals(IntOffset(-1, 0), GridEngine.displacementDirection(IntOffset(9, 6), b))
        // 指针在中心上方 → 向下让
        assertEquals(IntOffset(0, 1), GridEngine.displacementDirection(IntOffset(6, 3), b))
        // 指针在中心下方 → 向上让
        assertEquals(IntOffset(0, -1), GridEngine.displacementDirection(IntOffset(6, 9), b))
        // 主导轴并列时取横向
        assertEquals(IntOffset(-1, 0), GridEngine.displacementDirection(IntOffset(8, 8), b))
    }

    @Test
    fun testDirectionalFreeSlotSlidesSidewaysFirst() {
        // 竖向让步过于积极的回归：指针压在 B 左半 → B 向右横滑贴住拖动卡，而不是掉到下一行
        val others = listOf(card("B", 4, 0, 4, 4))
        val result = GridEngine.resolveDisplacements(card("A", 0, 0, 6, 4), 16, others, IntOffset(5, 1))
        assertEquals(mapOf("B" to card("B", 6, 0, 4, 4)), result)

        // 指针压在 B 上半 → B 向下滑让位
        val downward = GridEngine.resolveDisplacements(card("A", 0, 0, 16, 6), 16, listOf(card("B", 0, 0, 4, 4)), IntOffset(2, 1))
        assertEquals(mapOf("B" to card("B", 0, 6, 4, 4)), downward)
    }

    @Test
    fun testDirectionalFreeSlotFallsBackWhenBlocked() {
        // 让位方向被堵死时退化为最近空位
        val a = card("A", 0, 0, 4, 4)
        val others = listOf(
            card("B", 4, 0, 4, 4),
            card("C", 8, 0, 4, 4),
            card("D", 12, 0, 4, 4)
        )
        // A 缩放扩到 8 宽压到 B；指针在 B 左半 → 向右，但 C/D 堵死右侧 → 退化最近空位
        val result = GridEngine.resolveDisplacements(card("A", 0, 0, 8, 4), 16, others, IntOffset(5, 1))
        val settled = others.map { result[it.id] ?: it } + card("A", 0, 0, 8, 4)
        assertFalse(GridEngine.hasOverlap(settled))
        // B 让位到下方（右侧无空位）
        assertTrue(result.getValue("B").y > 0)
    }

    @Test
    fun testDirectionalFreeSlotScansAlongAxis() {
        val obstacles = listOf(
            card("A", 0, 0, 4, 4),
            card("B", 4, 0, 4, 4),
            card("C", 8, 0, 4, 4)
        )
        // B 从 (4,0) 向右逐格扫描，(5,0) 撞 C、到 (12,0) 无阻挡
        val slot = GridEngine.findDirectionalFreeSlot(card("B", 4, 0, 4, 4), IntOffset(1, 0), 16, obstacles)
        assertEquals(IntOffset(12, 0), slot)
        // 向上越出网格顶部返回 null
        assertNull(GridEngine.findDirectionalFreeSlot(card("B", 4, 0, 4, 4), IntOffset(0, -1), 16, obstacles))
        // 向下到障碍下方
        val down = GridEngine.findDirectionalFreeSlot(card("B", 4, 0, 4, 4), IntOffset(0, 1), 16, obstacles)
        assertEquals(IntOffset(4, 4), down)
    }

    // ---------- 缩放结算 ----------

    @Test
    fun testResizeEndPushesOverlappedCard() {
        // 扩张压到 B：B 被推至与前沿齐平，直到贴合网格右缘、推不动为止
        val a = card("A", 0, 0, 4, 4)
        val obstacles = listOf(card("B", 4, 0, 8, 4))
        val result = GridEngine.resolveResize(a, ResizeEdge.End, IntOffset(16, 0), 16, CardLimits.DEFAULT, obstacles)
        assertEquals(card("A", 0, 0, 8, 4), result.layout)
        assertEquals(mapOf("B" to card("B", 8, 0, 8, 4)), result.pushed)
    }

    @Test
    fun testResizeEndBlockedWhenSideFull() {
        // B 占满 A 右侧并贴合网格右缘：完全推不动，跨度止步于原位
        val a = card("A", 0, 0, 4, 4)
        val obstacles = listOf(card("B", 4, 0, 12, 4))
        val result = GridEngine.resolveResize(a, ResizeEdge.End, IntOffset(16, 0), 16, CardLimits.DEFAULT, obstacles)
        assertEquals(card("A", 0, 0, 4, 4), result.layout)
        assertTrue(result.pushed.isEmpty())
    }

    @Test
    fun testResizeEndGrowsIntoFreeSpaceWithLeftNeighbor() {
        // 回归：同行的左侧卡片不在扩张路径上，右侧空旷时必须能一路扩到网格右缘
        val a = card("A", 4, 0, 3, 4)
        val obstacles = listOf(card("B", 0, 0, 4, 4))
        val result = GridEngine.resolveResize(a, ResizeEdge.End, IntOffset(10, 0), 10, CardLimits.DEFAULT, obstacles)
        assertEquals(card("A", 4, 0, 6, 4), result.layout)
        assertTrue(result.pushed.isEmpty())
    }

    @Test
    fun testResizeBottomKeepsCardAbove() {
        // 回归：纵向扩张不受上方卡片影响，上方卡片不得被瞬移
        val a = card("A", 0, 4, 4, 4)
        val obstacles = listOf(card("B", 0, 0, 8, 4))
        val result = GridEngine.resolveResize(a, ResizeEdge.Bottom, IntOffset(0, 100), 16, CardLimits.DEFAULT, obstacles)
        assertEquals(card("A", 0, 4, 4, 12), result.layout)
        assertTrue(result.pushed.isEmpty())
    }

    @Test
    fun testResizeEndPushesChainOfCards() {
        // 推箱链条：B 被推开后联动顶开 C，直到 C 贴合网格右缘
        val a = card("A", 0, 0, 4, 4)
        val obstacles = listOf(
            card("B", 4, 0, 4, 4),
            card("C", 8, 0, 4, 4)
        )
        val result = GridEngine.resolveResize(a, ResizeEdge.End, IntOffset(10, 0), 16, CardLimits.DEFAULT, obstacles)
        assertEquals(card("A", 0, 0, 8, 4), result.layout)
        assertEquals(
            mapOf(
                "B" to card("B", 8, 0, 4, 4),
                "C" to card("C", 12, 0, 4, 4)
            ),
            result.pushed
        )
    }

    @Test
    fun testResizeStartPushesOverlappedCard() {
        // 左缘扩张：B 被推向网格左缘
        val a = card("A", 4, 0, 4, 4)
        val obstacles = listOf(card("B", 2, 0, 2, 4))
        val result = GridEngine.resolveResize(a, ResizeEdge.Start, IntOffset(0, 0), 16, CardLimits.DEFAULT, obstacles)
        assertEquals(card("A", 2, 0, 6, 4), result.layout)
        assertEquals(mapOf("B" to card("B", 0, 0, 2, 4)), result.pushed)
    }

    @Test
    fun testResizeStartBlockedByFlushCard() {
        // 左侧卡片贴合网格左缘：左缘无法扩张
        val a = card("A", 4, 0, 4, 4)
        val obstacles = listOf(card("B", 0, 0, 4, 4))
        val result = GridEngine.resolveResize(a, ResizeEdge.Start, IntOffset(0, 0), 16, CardLimits.DEFAULT, obstacles)
        assertEquals(card("A", 4, 0, 4, 4), result.layout)
        assertTrue(result.pushed.isEmpty())
    }

    @Test
    fun testResizeBottomPushesCardBelow() {
        // 下方纵向不设限：B 一直被推到 A 的下缘之下
        val a = card("A", 0, 0, 4, 4)
        val obstacles = listOf(card("B", 0, 8, 4, 4))
        val result = GridEngine.resolveResize(a, ResizeEdge.Bottom, IntOffset(0, 100), 16, CardLimits.DEFAULT, obstacles)
        assertEquals(card("A", 0, 0, 4, 12), result.layout)
        assertEquals(mapOf("B" to card("B", 0, 12, 4, 4)), result.pushed)
    }

    @Test
    fun testResizeTopPushesCardAbove() {
        // 上缘扩张：B 被推向网格顶部，A 止步于 B 的原位
        val a = card("A", 0, 4, 4, 4)
        val obstacles = listOf(card("B", 0, 1, 4, 2))
        val result = GridEngine.resolveResize(a, ResizeEdge.Top, IntOffset(0, 0), 16, CardLimits.DEFAULT, obstacles)
        assertEquals(card("A", 0, 2, 4, 6), result.layout)
        assertEquals(mapOf("B" to card("B", 0, 0, 4, 2)), result.pushed)
    }

    @Test
    fun testResizeTopBlockedByGridEdge() {
        // 无阻挡时上缘止步于网格顶部
        val a = card("A", 0, 4, 4, 4)
        val result = GridEngine.resolveResize(a, ResizeEdge.Top, IntOffset(0, 0), 16, CardLimits.DEFAULT, emptyList())
        assertEquals(card("A", 0, 0, 4, 8), result.layout)
    }

    @Test
    fun testResizeShrinkUnaffectedByPush() {
        // 收缩方向不受推挤影响，仅受最小跨度约束
        val a = card("A", 0, 0, 8, 4)
        val obstacles = listOf(card("B", 8, 0, 4, 4))
        val result = GridEngine.resolveResize(a, ResizeEdge.End, IntOffset(0, 0), 16, CardLimits.DEFAULT, obstacles)
        assertEquals(card("A", 0, 0, 4, 4), result.layout)
        assertTrue(result.pushed.isEmpty())
    }

    @Test
    fun testResizePushResolvesPerpendicularOverlap() {
        // B 的行范围超出 A：被推开后会压到行范围之外的 C，C 需要联动让开
        val a = card("A", 0, 0, 4, 4)
        val obstacles = listOf(
            card("B", 4, 0, 4, 6),
            card("C", 10, 4, 4, 4)
        )
        val result = GridEngine.resolveResize(a, ResizeEdge.End, IntOffset(12, 0), 16, CardLimits.DEFAULT, obstacles)
        assertEquals(card("A", 0, 0, 8, 4), result.layout)
        assertEquals(
            mapOf(
                "B" to card("B", 8, 0, 4, 6),
                "C" to card("C", 12, 4, 4, 4)
            ),
            result.pushed
        )
    }

    @Test
    fun testResizeIgnoresCardsOutsideSpan() {
        // 行范围不重叠的卡片不参与结算
        val a = card("A", 0, 0, 4, 4)
        val obstacles = listOf(card("B", 6, 4, 4, 4))   // B 位于 A 下方
        val result = GridEngine.resolveResize(a, ResizeEdge.End, IntOffset(16, 0), 16, CardLimits.DEFAULT, obstacles)
        assertEquals(card("A", 0, 0, 16, 4), result.layout)

        // 部分行重叠的卡片被推开而非构成阻挡
        val tall = card("A", 0, 0, 4, 8)
        val partial = listOf(card("C", 6, 2, 4, 2))     // 与 A 的行范围 2..4 重叠
        val partialResult = GridEngine.resolveResize(tall, ResizeEdge.End, IntOffset(16, 0), 16, CardLimits.DEFAULT, partial)
        assertEquals(card("A", 0, 0, 12, 8), partialResult.layout)
        assertEquals(mapOf("C" to card("C", 12, 2, 4, 2)), partialResult.pushed)
    }

    @Test
    fun testResizeEndClampsToGridEdge() {
        // 无阻挡卡片时，扩张止步于网格右缘
        val a = card("A", 0, 0, 4, 4)
        val result = GridEngine.resolveResize(a, ResizeEdge.End, IntOffset(16, 0), 6, CardLimits.DEFAULT, emptyList())
        assertEquals(card("A", 0, 0, 6, 4), result.layout)
    }

    // ---------- 垂直压实 ----------

    @Test
    fun testCompactFillsVerticalGap() {
        val cards = listOf(
            card("A", 0, 0, 8, 4),
            card("B", 0, 8, 8, 4)
        )
        val compacted = GridEngine.compact(cards)
        // B 上浮填补 A 与 B 之间的空隙
        assertEquals(4, compacted.first { it.id == "B" }.y)
    }

    @Test
    fun testCompactBlockedByOtherCard() {
        val cards = listOf(
            card("A", 0, 0, 8, 4),
            card("B", 0, 8, 8, 4),
            card("C", 0, 4, 8, 4)
        )
        val compacted = GridEngine.compact(cards)
        val a = compacted.first { it.id == "A" }
        val b = compacted.first { it.id == "B" }
        val c = compacted.first { it.id == "C" }
        assertEquals(0, a.y)
        assertEquals(4, c.y)
        // C 挡在中间，B 只能压在 C 下方
        assertEquals(8, b.y)
    }

    @Test
    fun testCompactPreservesHorizontalPosition() {
        val cards = listOf(card("D", 8, 8, 8, 4))
        val compacted = GridEngine.compact(cards)
        assertEquals(card("D", 8, 0, 8, 4), compacted.first())
    }

    @Test
    fun testCompactAlreadyCompactedIsStable() {
        val cards = listOf(
            card("A", 0, 0, 8, 4),
            card("B", 8, 0, 8, 4),
            card("C", 0, 4, 8, 4)
        )
        assertEquals(cards.sortedWith(compareBy({ it.y }, { it.x })), GridEngine.compact(cards))
    }

    // ---------- 重排 ----------

    @Test
    fun testReflowScalesSpansProportionally() {
        val cards = listOf(card("A", 0, 0, 4, 4))
        val result = GridEngine.reflow(cards, oldColumns = 8, columns = 16)
        assertEquals(card("A", 0, 0, 8, 8), result.first())
    }

    @Test
    fun testReflowKeepsReadingOrder() {
        val cards = listOf(
            card("A", 0, 0, 8, 4),
            card("B", 0, 4, 8, 4)
        )
        val result = GridEngine.reflow(cards, oldColumns = 8, columns = 16)
        val a = result.first { it.id == "A" }
        val b = result.first { it.id == "B" }
        assertTrue(a.y <= b.y)
        assertFalse(GridEngine.hasOverlap(result))
    }

    @Test
    fun testReflowClampsToLimits() {
        val cards = listOf(card("A", 0, 0, 16, 16))
        val limits: (CardRect) -> CardLimits = { CardLimits(maxWidth = 8, maxHeight = 8) }
        val result = GridEngine.reflow(cards, oldColumns = 16, columns = 16, limits = limits)
        assertEquals(card("A", 0, 0, 8, 8), result.first())
    }

    @Test
    fun testReflowShrinkingColumnsNeverOverflows() {
        val cards = listOf(
            card("A", 0, 0, 16, 4),
            card("B", 0, 4, 16, 4)
        )
        val result = GridEngine.reflow(cards, oldColumns = 16, columns = 8)
        assertTrue(result.all { GridEngine.isInGrid(it, 8) })
        assertFalse(GridEngine.hasOverlap(result))
    }

    // ---------- 加载校验 ----------

    @Test
    fun testValidateClampsOutOfBounds() {
        val result = GridEngine.validate(
            listOf(card("A", 14, -2, 8, 4)),
            columns = 16
        )
        assertEquals(card("A", 8, 0, 8, 4), result.first())
    }

    @Test
    fun testValidateResolvesOverlap() {
        val result = GridEngine.validate(
            listOf(
                card("A", 0, 0, 8, 4),
                card("B", 0, 0, 8, 4)
            ),
            columns = 16
        )
        assertFalse(GridEngine.hasOverlap(result))
        assertEquals(2, result.size)
    }

    @Test
    fun testValidateDeduplicatesIds() {
        val result = GridEngine.validate(
            listOf(
                card("A", 0, 0, 8, 4),
                card("A", 0, 0, 8, 4)
            ),
            columns = 16
        )
        assertEquals(1, result.size)
    }

    @Test
    fun testValidateClampsSizeToLimits() {
        val limits: (CardRect) -> CardLimits = { CardLimits(minWidth = 4, minHeight = 4, maxWidth = 8, maxHeight = 8) }
        val result = GridEngine.validate(
            listOf(card("A", 0, 0, 16, 1)),
            columns = 16,
            limits = limits
        )
        assertEquals(card("A", 0, 0, 8, 4), result.first())
    }

    @Test
    fun testValidateEndsCompacted() {
        val result = GridEngine.validate(
            listOf(card("A", 4, 8, 8, 4)),
            columns = 16
        )
        assertEquals(0, result.first().y)
    }

    // ---------- 汇总 ----------

    @Test
    fun testTotalRowsAndOverlap() {
        val cards = listOf(
            card("A", 0, 0, 8, 4),
            card("B", 0, 4, 8, 4)
        )
        assertEquals(8, GridEngine.totalRows(cards))
        assertFalse(GridEngine.hasOverlap(cards))
        assertTrue(GridEngine.hasOverlap(cards + card("C", 4, 2, 8, 4)))
    }

    // ---------- 随机不变式 ----------

    /**
     * 随机布局上执行大量随机拖动与缩放，
     * 断言每次结算后布局不变式成立：互不重叠、横向不出界、纵向不越顶。
     */
    @Test
    fun testRandomOperationsPreserveInvariants() {
        val random = Random(2026L)
        repeat(200) { round ->
            val columns = if (round % 2 == 0) 8 else 12
            val cards = buildList {
                repeat(10) { index ->
                    val width = (2 + random.nextInt(3)) * 2
                    val height = (2 + random.nextInt(3)) * 2
                    val slot = GridEngine.findTopLeftFreeSlot(width, height, columns, this)
                    add(card("c$index", slot.x, slot.y, width, height))
                }
            }

            repeat(20) {
                val mover = cards[random.nextInt(cards.size)]
                val others = cards.filter { it.id != mover.id }
                val settled: List<CardRect> = if (random.nextBoolean()) {
                    val edge = ResizeEdge.entries[random.nextInt(ResizeEdge.entries.size)]
                    val pointer = when (edge) {
                        ResizeEdge.End -> IntOffset(mover.x + random.nextInt(columns * 2), mover.y + random.nextInt(4))
                        ResizeEdge.Start -> IntOffset(random.nextInt(columns + 8) - 4, mover.y + random.nextInt(4))
                        ResizeEdge.Bottom -> IntOffset(mover.x, mover.y + random.nextInt(30) - 5)
                        ResizeEdge.Top -> IntOffset(mover.x, random.nextInt(20) - 5)
                    }
                    val resized = GridEngine.resolveResize(mover, edge, pointer, columns, CardLimits.DEFAULT, others)
                    others.map { resized.pushed[it.id] ?: it } + resized.layout
                } else {
                    // 拖动挤压：预览落位加上被挤开的卡片
                    val target = IntOffset(random.nextInt(columns - mover.width + 1), random.nextInt(40))
                    val preview = mover.positionAt(target)
                    val displaced = GridEngine.resolveDisplacements(preview, columns, others)
                    others.map { displaced[it.id] ?: it } + preview
                }
                assertFalse(
                    "Round $round: settled layout must not overlap: $settled",
                    GridEngine.hasOverlap(settled)
                )
                assertTrue(
                    "Round $round: settled layout must stay in grid: $settled",
                    settled.all { GridEngine.isInGrid(it, columns) }
                )
            }
        }
    }
}
