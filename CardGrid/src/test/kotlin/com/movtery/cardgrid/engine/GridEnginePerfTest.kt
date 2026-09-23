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
import com.movtery.cardgrid.model.ResizeEdge
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Random

/**
 * 布局引擎压力测试：重度使用规模（上百张卡片）下热路径的耗时与结果正确性。
 * 耗时断言采用宽松预算，仅拦截复杂度退化（意外的指数级或死循环），
 * 真实耗时通过标准输出记录。
 */
class GridEnginePerfTest {

    private fun card(
        id: String,
        x: Int,
        y: Int,
        width: Int,
        height: Int
    ) = CardRect(id = id, x = x, y = y, width = width, height = height)

    /** 用引擎自身的贪心打包生成一张填满的网格，模拟重度自定义的用户主页 */
    private fun generateGrid(count: Int, columns: Int, seed: Long): List<CardRect> {
        val random = Random(seed)
        val placed = mutableListOf<CardRect>()
        for (index in 0 until count) {
            val width = (2 + random.nextInt(5)) * 2
            val height = (2 + random.nextInt(5)) * 2
            val slot = GridEngine.findTopLeftFreeSlot(width, height, columns, placed)
            placed.add(card("card$index", slot.x, slot.y, width, height))
        }
        assertTrue(GridEngine.validate(placed, columns).size == count)
        return placed
    }

    private fun assertNoOverlap(all: List<CardRect>) {
        assertFalse("Cards must not overlap at stress scale", GridEngine.hasOverlap(all))
    }

    private inline fun measureMs(block: () -> Unit): Long {
        val start = System.nanoTime()
        block()
        return (System.nanoTime() - start) / 1_000_000
    }

    // ---------- 拖动热路径 ----------

    @Test
    fun testResolveDisplacementsUnderLongDragPath() {
        val columns = 16
        // 80 张卡 ≈ 2400+ 单元格，网格纵深约百行，远超正常使用规模
        val cards = generateGrid(count = 80, columns = columns, seed = 42L)
        val mover = cards[0]
        val others = cards.filter { it.id != mover.id }

        // 预热 JIT
        repeat(50) {
            GridEngine.resolveDisplacements(mover, columns, others)
        }

        // 模拟一次贯穿整张网格的拖动：从左上到右下逐步换格
        val steps = 300
        val maxY = GridEngine.totalRows(cards)
        val warm = measureMs {
            for (step in 0 until steps) {
                val t = step / steps.toFloat()
                val preview = mover.copy(
                    x = ((columns - mover.width) * t).toInt(),
                    y = ((maxY - mover.height) * t).toInt()
                )
                val displaced = GridEngine.resolveDisplacements(preview, columns, others)
                val relocated = others.map { displaced[it.id] ?: it }
                assertNoOverlap(relocated + preview)
            }
        }
        val perCallMs = warm.toDouble() / steps
        println("resolveDisplacements: ${warm}ms / $steps steps = ${"%.3f".format(perCallMs)}ms per call (80 cards, 16 cols)")
        // 宽松预算：单次结算在桌面 JVM 上应远低于 10ms
        assertTrue("resolveDisplacements took too long: $perCallMs ms", perCallMs < 10.0)
    }

    // ---------- 缩放热路径 ----------

    @Test
    fun testResolveResizeUnderDenseGrid() {
        val columns = 16
        val cards = generateGrid(count = 80, columns = columns, seed = 13L)
        val mover = cards[0]
        val others = cards.filter { it.id != mover.id }

        repeat(50) {
            GridEngine.resolveResize(mover, ResizeEdge.End, IntOffset(columns, 0), columns, CardLimits.DEFAULT, others)
        }

        val steps = 200
        val elapsed = measureMs {
            for (step in 0 until steps) {
                val span = 4 + (step % (columns - 4))
                val result = GridEngine.resolveResize(
                    mover, ResizeEdge.End, IntOffset(mover.x + span, mover.y),
                    columns, CardLimits.DEFAULT, others
                )
                val relocated = others.map { result.pushed[it.id] ?: it }
                assertNoOverlap(relocated + result.layout)
            }
        }
        val perCallMs = elapsed.toDouble() / steps
        println("resolveResize: ${elapsed}ms / $steps steps = ${"%.3f".format(perCallMs)}ms per call (80 cards, 16 cols)")
        assertTrue("resolveResize took too long: $perCallMs ms", perCallMs < 10.0)
    }

    @Test
    fun testFindNearestFreeSlotDenseGrid() {
        val columns = 16
        val cards = generateGrid(count = 80, columns = columns, seed = 7L)
        // 预热
        repeat(50) {
            GridEngine.findNearestFreeSlot(8, 8, IntOffset(columns / 2, GridEngine.totalRows(cards) / 2), columns, cards)
        }
        val rows = GridEngine.totalRows(cards)
        val elapsed = measureMs {
            repeat(1000) {
                val slot = GridEngine.findNearestFreeSlot(8, 8, IntOffset(columns / 2, rows / 2), columns, cards)
                assertNotNull("A free slot must exist in a dense grid", slot)
            }
        }
        println("findNearestFreeSlot: ${elapsed}ms / 1000 calls = ${"%.3f".format(elapsed / 1000.0)}ms per call (dense 16x$rows)")
        assertTrue("findNearestFreeSlot took too long: ${elapsed / 1000.0} ms", elapsed / 1000.0 < 10.0)
    }

    // ---------- 冷路径 ----------

    @Test
    fun testValidateOnLargeDirtyLayout() {
        val columns = 16
        // 构造脏数据：越界、重叠、非法尺寸、重复 id
        val random = Random(99L)
        val dirty = (0 until 120).map { index ->
            card(
                id = "card${index % 100}", // 末尾 20 张与前 20 张 id 重复
                x = random.nextInt(columns * 2) - columns / 2,
                y = random.nextInt(60) - 10,
                width = 1 + random.nextInt(columns),
                height = 1 + random.nextInt(16)
            )
        }
        val elapsed = measureMs {
            repeat(100) {
                GridEngine.validate(dirty, columns)
            }
        }
        val result = GridEngine.validate(dirty, columns)
        assertEquals("Duplicate ids must be dropped", 100, result.size)
        assertNoOverlap(result)
        assertTrue(result.all { GridEngine.isInGrid(it, columns) })
        assertTrue("Validation result must be compacted", result == GridEngine.compact(result))
        println("validate: ${elapsed}ms / 100 calls = ${"%.3f".format(elapsed / 100.0)}ms per call (120 dirty cards)")
        assertTrue("validate took too long: ${elapsed / 100.0} ms", elapsed / 100.0 < 50.0)
    }

    @Test
    fun testReflowOnLargeGrid() {
        val cards = generateGrid(count = 100, columns = 16, seed = 5L)
        val limits: (CardRect) -> CardLimits = { CardLimits.DEFAULT }
        val elapsed = measureMs {
            repeat(200) {
                val result = GridEngine.reflow(cards, oldColumns = 16, columns = 24, limits = limits)
                assertEquals(100, result.size)
                assertTrue(result.all { GridEngine.isInGrid(it, 24) })
                assertNoOverlap(result)

                val back = GridEngine.reflow(result, oldColumns = 24, columns = 16, limits = limits)
                assertTrue(back.all { GridEngine.isInGrid(it, 16) })
                assertNoOverlap(back)
            }
        }
        println("reflow (16->24 + 24->16): ${elapsed}ms / 200 round trips = ${"%.3f".format(elapsed / 400.0)}ms per call (100 cards)")
        assertTrue("reflow took too long: ${elapsed / 400.0} ms", elapsed / 400.0 < 50.0)
    }

    @Test
    fun testCompactOnTallGridIsStableAndFast() {
        val cards = generateGrid(count = 80, columns = 16, seed = 11L)
        val elapsed = measureMs {
            repeat(1000) {
                val once = GridEngine.compact(cards)
                val twice = GridEngine.compact(once)
                assertEquals("compact must be idempotent", once, twice)
            }
        }
        println("compact x2 (idempotency): ${elapsed}ms / 1000 iterations (80 cards)")
        assertTrue("compact took too long: ${elapsed / 1000.0} ms", elapsed / 1000.0 < 20.0)
    }
}
