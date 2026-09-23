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
    fun testSeedDropsUnknownType() {
        val state = state()
        state.seed(
            types = listOf(testType),
            seeds = listOf(CardSeed("X", "unknown", CardRect("X", 0, 0, 4, 4))),
            storedColumns = 10
        )
        assertTrue(state.cards.isEmpty())
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
    fun testRemoveCardCompactsAndNotifies() {
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
        assertEquals(CardRect("B", 0, 0, 4, 4), layoutOf(state, "B"))
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
    fun testResizeBlockedByCardAtEdgeKeepsOriginal() {
        val state = seededState(
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
