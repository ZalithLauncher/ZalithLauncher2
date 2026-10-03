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

package com.movtery.cardgrid.state

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntOffset
import com.movtery.cardgrid.engine.GridEngine
import com.movtery.cardgrid.model.CardInteraction
import com.movtery.cardgrid.model.CardRect
import com.movtery.cardgrid.model.CardType
import com.movtery.cardgrid.model.ResizeEdge
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CardGridStateTest {

    // 动画协程挂起后随作用域废弃，状态断言只关心同步发生的布局结算
    private val scope = CoroutineScope(
        SupervisorJob() + CoroutineExceptionHandler { _, _ -> }
    )

    private val testType = CardType(
        typeId = "test",
        defaultSpan = IntOffset(4, 4),
        content = { _ -> }
    )

    /** 几何就绪的 10 列网格，单元格 20px */
    private fun state(): CardGridState {
        val state = CardGridState(scope)
        state.updateGeometry(200f, Density(1f))
        return state
    }

    private fun seededState(vararg layouts: CardRect): CardGridState {
        val state = state()
        state.seed(
            types = listOf(testType),
            seeds = layouts.map { CardSeed(it.id, "test", it) },
            storedColumns = 10
        )
        return state
    }

    private fun layoutOf(state: CardGridState, id: String): CardRect =
        state.cards.first { it.id == id }.layout

    @Test
    fun testViewportPositionedUpdatesState() {
        val state = state()
        state.onViewportPositioned(120f, 800f)
        assertEquals(120f, state.viewportTopPx, 0f)
        assertEquals(800f, state.viewportHeightPx, 0f)
    }

    // ---------- 播种 ----------

    @Test
    fun testSeedValidatesOverlappingLayouts() {
        val state = seededState(
            CardRect("A", 0, 0, 4, 4),
            CardRect("B", 0, 0, 4, 4)
        )
        assertEquals(2, state.cards.size)
        assertFalse(GridEngine.hasOverlap(state.cards.map { it.layout }))
        assertTrue(state.cards.all { GridEngine.isInGrid(it.layout, state.geometry.columns) })
    }

    @Test
    fun testSeedReflowsOnColumnMismatch() {
        val state = state()
        // 持久化时的列数为 5，与当前 10 列不一致：跨度按比例折算
        state.seed(
            types = listOf(testType),
            seeds = listOf(CardSeed("A", "test", CardRect("A", 0, 0, 2, 2))),
            storedColumns = 5
        )
        assertEquals(CardRect("A", 0, 0, 4, 4), layoutOf(state, "A"))
    }

    @Test
    fun testSeedPreservesVerticalGaps() {
        val state = seededState(
            CardRect("A", 0, 0, 4, 4),
            CardRect("B", 0, 8, 4, 4)
        )
        assertEquals(CardRect("B", 0, 8, 4, 4), layoutOf(state, "B"))
    }

    @Test
    fun testSeedDropsUnknownType() {
        val state = state()
        state.seed(
            types = listOf(testType),
            seeds = listOf(CardSeed("X", "unknown", CardRect("X", 0, 0, 4, 4))),
            storedColumns = 10
        )
        assertTrue(state.cards.isEmpty())
    }

    @Test
    fun testSeedBeforeFirstMeasurementSkipsInitialGeometryReflow() {
        // 播种早于首次有效测量：卡片按持久化布局落位，不按初始默认几何重排
        val state = CardGridState(scope)
        state.seed(
            types = listOf(testType),
            seeds = listOf(CardSeed("A", "test", CardRect("A", 0, 0, 2, 2))),
            storedColumns = 10
        )
        state.updateGeometry(200f, Density(1f))
        assertEquals(CardRect("A", 0, 0, 4, 4), layoutOf(state, "A"))
    }

    @Test
    fun testSeedWithUnknownStoredColumnsValidates() {
        // 存储列数未知时不做比例折算，仅校验修复
        val state = state()
        state.seed(
            types = listOf(testType),
            seeds = listOf(CardSeed("A", "test", CardRect("A", 0, 0, 2, 2))),
            storedColumns = 0
        )
        assertEquals(CardRect("A", 0, 0, 4, 4), layoutOf(state, "A"))
    }

    @Test
    fun testSeedReplacesPreMaterializedCard() {
        // 几何未就绪时补位逻辑先行加入同 id 卡片，播种应以持久化布局替换而非重复追加
        val state = CardGridState(scope)
        state.seed(
            types = listOf(testType),
            seeds = listOf(CardSeed("A", "test", CardRect("A", 2, 0, 4, 4))),
            storedColumns = 10
        )
        state.addCard(testType, "A")
        state.updateGeometry(200f, Density(1f))
        assertEquals(1, state.cards.size)
        assertEquals(CardRect("A", 2, 0, 4, 4), layoutOf(state, "A"))
    }

    @Test
    fun testZeroWidthMeasurementIgnored() {
        val state = seededState(CardRect("A", 0, 0, 4, 4))
        state.updateGeometry(0f, Density(1f))
        assertEquals(10, state.geometry.columns)
        assertEquals(CardRect("A", 0, 0, 4, 4), layoutOf(state, "A"))
    }

    @Test
    fun testReflowSpanGrowthNotEatenByPerStepRounding() {
        //小跨度卡片在逐级列数变化下跨度增长不应被取整吞噬（否则卡片宽度在超宽屏上停滞）
        val state = CardGridState(scope)
        state.updateGeometry(1280f, Density(1f)) // 64 列
        state.seed(
            types = listOf(testType),
            seeds = listOf(CardSeed("A", "test", CardRect("A", 0, 0, 10, 4))),
            storedColumns = 64
        )
        state.updateGeometry(1320f, Density(1f)) // 66 列
        state.updateGeometry(1360f, Density(1f)) // 68 列
        assertEquals(CardRect("A", 0, 0, 11, 4), layoutOf(state, "A"))
    }

    @Test
    fun testAddCardBeforeGeometryReadySkipsPersistence() {
        val state = CardGridState(scope)
        var committed = false
        state.onLayoutCommitted = { committed = true }
        state.addCard(testType, "A")
        assertFalse(committed)
    }

    // ---------- 卡片管理 ----------

    @Test
    fun testAddCardPlacesAtTopLeftFreeSlot() {
        val state = seededState(CardRect("A", 0, 0, 4, 4))
        val card = state.addCard(testType, "B")
        assertEquals(CardRect("B", 4, 0, 4, 4), card?.layout)
    }

    @Test
    fun testAddCardRejectsDuplicateId() {
        val state = seededState(CardRect("A", 0, 0, 4, 4))
        assertNull(state.addCard(testType, "A"))
    }

    @Test
    fun testRemoveCardLeavesHoleAndNotifies() {
        var removed: String? = null
        var committed = false
        val state = seededState(
            CardRect("A", 0, 0, 4, 4),
            CardRect("B", 0, 8, 4, 4)
        )
        state.onCardRemoved = { removed = it }
        state.onLayoutCommitted = { committed = true }

        state.removeCard("A")
        assertEquals("A", removed)
        assertTrue(committed)
        // 其余卡片保持原位，A 留下的空洞不上浮填补
        assertEquals(CardRect("B", 0, 8, 4, 4), layoutOf(state, "B"))
    }

    // ---------- 命中测试 ----------

    @Test
    fun testCardAtHitsPlacedCard() {
        val state = seededState(CardRect("A", 0, 0, 4, 4))
        assertEquals("A", state.cardAt(Offset(40f, 40f))?.id)
        assertNull(state.cardAt(Offset(200f, 200f)))
    }

    @Test
    fun testResizeEdgeAtRequiresAdjustingState() {
        val state = seededState(CardRect("A", 0, 0, 4, 4))
        assertNull(state.resizeEdgeAt(Offset(74f, 40f)))
    }

    // ---------- 拖动会话 ----------

    @Test
    fun testDragSessionDisplacesLiveAndCommits() {
        val state = seededState(
            CardRect("A", 0, 0, 4, 4),
            CardRect("B", 4, 0, 4, 4)
        )
        state.onCardDragStart(state.cards.first { it.id == "A" }, Offset(50f, 50f))
        // 指针拖到第 8 格：拖动中 B 实时让位预览（指针压在 B 中心右侧 → 向左滑开）
        state.onCardDrag(Offset(170f, 50f))

        assertEquals(CardRect("A", 6, 0, 4, 4), state.dragPreview)
        assertEquals(mapOf("B" to CardRect("B", 2, 0, 4, 4)), state.displaced)

        // 松手提交让位结果并持久化
        state.onCardDragEnd()
        assertEquals(CardRect("A", 6, 0, 4, 4), layoutOf(state, "A"))
        assertEquals(CardRect("B", 2, 0, 4, 4), layoutOf(state, "B"))
        assertFalse(state.hasSession)
    }

    @Test
    fun testDragCancelRestoresLayouts() {
        val state = seededState(
            CardRect("A", 0, 0, 4, 4),
            CardRect("B", 4, 0, 4, 4)
        )
        state.onCardDragStart(state.cards.first { it.id == "A" }, Offset(50f, 50f))
        state.onCardDrag(Offset(170f, 50f))
        state.onCardDragCancel()

        assertEquals(CardRect("A", 0, 0, 4, 4), layoutOf(state, "A"))
        assertEquals(CardRect("B", 4, 0, 4, 4), layoutOf(state, "B"))
        assertTrue(state.displaced.isEmpty())
    }

    @Test
    fun testDragDirectionLocksWhileCardStaysDisplaced() {
        // 20 列网格：向右的让位链条有足够的滑动空间，方向差异才能从结果上体现
        val state = CardGridState(scope)
        state.updateGeometry(400f, Density(1f))
        state.seed(
            types = listOf(testType),
            seeds = listOf(
                CardSeed("A", "test", CardRect("A", 0, 0, 4, 4)),
                CardSeed("B", "test", CardRect("B", 4, 0, 4, 4))
            ),
            storedColumns = 20
        )
        state.onCardDragStart(state.cards.first { it.id == "A" }, Offset(50f, 50f))
        // 首帧：指针压在 B 中心左侧 → B 向右滑让位
        state.onCardDrag(Offset(105f, 50f))
        assertEquals(CardRect("A", 3, 0, 4, 4), state.dragPreview)
        assertEquals(mapOf("B" to CardRect("B", 7, 0, 4, 4)), state.displaced)

        // 指针扫过 B 中心右侧：方向已锁定，B 继续向右滑而不是掉头向左
        state.onCardDrag(Offset(164f, 50f))
        assertEquals(CardRect("A", 6, 0, 4, 4), state.dragPreview)
        assertEquals(mapOf("B" to CardRect("B", 10, 0, 4, 4)), state.displaced)
    }

    @Test
    fun testDragOpensDoorDownwardWhenHorizontalBlocked() {
        val state = seededState(
            CardRect("A", 0, 8, 4, 4),
            CardRect("B", 0, 0, 4, 4),
            CardRect("C", 4, 0, 4, 4)
        )
        state.onCardDragStart(state.cards.first { it.id == "A" }, Offset(50f, 200f))
        // 指针压在 B 右半：横向让位两侧都被顶死（B 顶着左缘、C 顶死右缘），垂直轴向下开门
        state.onCardDrag(Offset(50f, 40f))
        assertEquals(CardRect("A", 0, 0, 4, 4), state.dragPreview)
        assertEquals(mapOf("B" to CardRect("B", 0, 4, 4, 4)), state.displaced)
        state.onCardDragEnd()
        assertEquals(CardRect("A", 0, 0, 4, 4), layoutOf(state, "A"))
        assertEquals(CardRect("B", 0, 4, 4, 4), layoutOf(state, "B"))
        assertEquals(CardRect("C", 4, 0, 4, 4), layoutOf(state, "C"))
    }

    // ---------- 缩放会话 ----------

    @Test
    fun testResizeSessionCommit() {
        val state = seededState(CardRect("A", 0, 0, 4, 4))
        state.onResizeStart(state.cards.first(), ResizeEdge.End, Offset(74f, 40f))
        // 指针拖到第 8 格右缘
        state.onResize(Offset(150f, 40f))

        assertEquals(CardRect("A", 0, 0, 8, 4), state.dragPreview)
        state.onResizeEnd()
        assertEquals(CardRect("A", 0, 0, 8, 4), layoutOf(state, "A"))
    }

    @Test
    fun testResizeBlockedByCardAtEdgeKeepsOriginal() {        val state = seededState(
            CardRect("A", 0, 0, 4, 4),
            CardRect("B", 4, 0, 6, 4)
        )
        state.onResizeStart(state.cards.first { it.id == "A" }, ResizeEdge.End, Offset(74f, 40f))
        state.onResize(Offset(190f, 40f))
        state.onResizeEnd()
        // B 被推到贴合右缘后链条推不动，A 跨度止步于原位
        assertEquals(CardRect("A", 0, 0, 4, 4), layoutOf(state, "A"))
    }

    // ---------- 调整态 ----------

    @Test
    fun testInteractionStates() {
        val state = seededState(CardRect("A", 0, 0, 4, 4))
        val card = state.cards.first()
        assertEquals(CardInteraction.Idle, state.interactionOf("A"))

        state.onCardDragStart(card, Offset(50f, 50f))
        assertEquals(CardInteraction.Dragging, state.interactionOf("A"))

        state.onCardDragCancel()
        assertTrue(state.isAdjusting)
        assertEquals(CardInteraction.Adjusting, state.interactionOf("A"))

        state.exitAdjusting()
        assertFalse(state.isAdjusting)
        assertEquals(CardInteraction.Idle, state.interactionOf("A"))
    }
}
