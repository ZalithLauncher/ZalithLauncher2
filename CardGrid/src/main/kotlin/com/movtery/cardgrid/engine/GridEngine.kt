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
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * 卡片网格布局引擎：全部结算均为无副作用的纯函数。
 *
 * 缩放使用推箱式语义——沿被拖边把挡路的卡片推开成链，推不动时逐格收缩跨度；
 * 拖动使用开门让位语义——被压住的卡片沿指针反方向链式滑开，汇入沿途的空洞或网格尾部，
 * 指针侧轴线推不动时回退垂直轴开门；
 * 卡片间允许纵向留空，布局不做自动压实；最上最左贪心打包仅用于空闲落位与重排。
 */
object GridEngine {

    /** 推挤运动的主导轴 */
    internal enum class PushAxis { Horizontal, Vertical }

    /** 缩放结算结果：缩放卡的布局与被推开的卡片（id -> 新布局） */
    data class ResizeResult(
        val layout: CardRect,
        val pushed: Map<String, CardRect>
    )

    /** 卡片是否完全位于网格边界内（纵向不设限） */
    fun isInGrid(rect: CardRect, columns: Int): Boolean =
        rect.x >= 0 && rect.y >= 0 && rect.right <= columns

    /** 一组卡片之间是否存在重叠 */
    fun hasOverlap(cards: List<CardRect>): Boolean {
        for (i in cards.indices) {
            for (j in i + 1 until cards.size) {
                if (cards[i].intersects(cards[j])) return true
            }
        }
        return false
    }

    /** 网格当前占用的总行数（无卡片时为 0） */
    fun totalRows(cards: List<CardRect>): Int = cards.maxOfOrNull { it.bottom } ?: 0

    /**
     * 为尺寸 [width]×[height] 的卡片搜索距离 [origin] 最近的空闲位置。
     *
     * 以卡片中心间欧氏距离度量远近，距离相同者优先取更靠上、更靠左的位置；
     * [obstacles] 为需要避开的矩形集合，网格纵向无上限，
     * 搜索行数以障碍物最大底边为界（其下方整行必然空闲，解必定存在）。
     *
     * @return 最近的空闲位置，尺寸无法放入网格宽时返回 null
     */
    fun findNearestFreeSlot(
        width: Int,
        height: Int,
        origin: IntOffset,
        columns: Int,
        obstacles: List<CardRect>
    ): IntOffset? {
        if (width <= 0 || height <= 0 || width > columns) return null
        val maxRow = obstacles.maxOfOrNull { it.bottom } ?: 0
        val originCenterX = origin.x + width / 2f
        val originCenterY = origin.y + height / 2f
        var best: IntOffset? = null
        var bestDistance = Float.MAX_VALUE
        for (cy in 0..maxRow) {
            for (cx in 0..columns - width) {
                val candidate = CardRect("", cx, cy, width, height)
                if (obstacles.any { it.intersects(candidate) }) continue
                val dx = cx + width / 2f - originCenterX
                val dy = cy + height / 2f - originCenterY
                val distance = dx * dx + dy * dy
                if (distance < bestDistance) {
                    bestDistance = distance
                    best = IntOffset(cx, cy)
                }
            }
        }
        return best
    }

    /**
     * 拖动结算：[moving] 为拖动中的卡片预览（或落位），
     * 与其重叠的卡片沿让位方向被链式推开门——挡在路径上的卡片级联随动，
     * 直到链条完全脱离 [moving]，汇入沿途的空洞或网格尾部。
     * 让位方向依次尝试指针反方向、其反向与垂直轴的正反向，门朝哪边能开就朝哪边开；
     * 纵向向下开门必定成功，仅在无法确定让位方向时结算失败。
     *
     * @param pointer 指针所在的单元格，用于为未锁定方向的被压卡片推导让位方向
     * @param directions 卡片的让位方向锁定表，优先于 [pointer] 推导
     * @return 被重新安置的卡片（id -> 新布局）；无法确定让位方向时返回 null
     */
    fun resolveDisplacements(
        moving: CardRect,
        columns: Int,
        cards: List<CardRect>,
        pointer: IntOffset? = null,
        directions: Map<String, IntOffset> = emptyMap()
    ): Map<String, CardRect>? {
        val displaced = cards
            .filter { it.id != moving.id && it.intersects(moving) }
            .sortedWith(readingOrder())
        // 全部卡片都是链条的障碍：尚未结算的被压卡片以原位参与阻挡，
        // 先行链条的成果以新位参与阻挡，链条不得从任何卡片身上滑过
        val occupied = mutableListOf(moving)
        occupied.addAll(cards.filter { it.id != moving.id })
        val result = mutableMapOf<String, CardRect>()
        for (card in displaced) {
            val current = occupied.first { it.id == card.id }
            // 先行处理的链条可能已把该卡片带离 [moving]
            if (!current.intersects(moving)) continue
            val direction = directions[current.id]
                ?: pointer?.let { displacementDirection(it, current) }
                ?: return null
            if (direction.x == 0 && direction.y == 0) return null
            // 垂直轴候选朝向指针背离侧：指针压在轴的哪一侧，门就朝另一侧开
            val perpendicular = if (direction.x != 0) {
                IntOffset(0, if (pointer != null && pointer.y > current.y + current.height / 2f) -1 else 1)
            } else {
                IntOffset(if (pointer != null && pointer.x > current.x + current.width / 2f) -1 else 1, 0)
            }
            val chain = listOf(
                direction,
                IntOffset(-direction.x, -direction.y),
                perpendicular,
                IntOffset(-perpendicular.x, -perpendicular.y)
            ).firstNotNullOfOrNull { candidate ->
                slideChain(current, candidate, moving, columns, occupied)
            } ?: return null
            occupied.removeAll { it.id in chain }
            occupied.addAll(chain.values)
            result.putAll(chain)
        }
        return result
    }

    /**
     * 指针压到卡片的哪一侧，卡片就沿该轴向远离指针的一侧让开。
     * 偏移按卡片自身跨度归一化后比较主导轴：
     * 宽（高）卡片上另一轴的绝对偏移天然偏大，直接比较会系统性偏向该轴。
     */
    internal fun displacementDirection(pointer: IntOffset, card: CardRect): IntOffset {
        val dx = (pointer.x - (card.x + card.width / 2f)) / card.width
        val dy = (pointer.y - (card.y + card.height / 2f)) / card.height
        return if (abs(dx) >= abs(dy)) {
            IntOffset(if (dx > 0) -1 else 1, 0)
        } else {
            IntOffset(0, if (dy > 0) -1 else 1)
        }
    }

    /**
     * 链式滑动：把 [head] 沿 [direction]（单位轴向量）逐格推动，
     * 挡在路径上的卡片级联加入链条同步移动，
     * 直到 [head] 完全脱离 [mover]；任何成员越出网格边界（横向越界或纵向越过网格边缘）即推不动。
     *
     * @return 链条上全部被移动的卡片（id -> 新布局），推不动时返回 null
     */
    internal fun slideChain(
        head: CardRect,
        direction: IntOffset,
        mover: CardRect,
        columns: Int,
        obstacles: List<CardRect>
    ): Map<String, CardRect>? {
        val moved = linkedMapOf(head.id to head)
        while (moved.getValue(head.id).intersects(mover)) {
            // 链条生长：挡在任一成员前进一格路径上的卡片加入链条
            while (true) {
                val blockers = obstacles.filter { obstacle ->
                    obstacle.id != mover.id && obstacle.id !in moved &&
                        moved.values.any { member -> obstacle.intersects(member.shift(direction)) }
                }
                if (blockers.isEmpty()) break
                blockers.forEach { moved[it.id] = it }
            }
            // 边界结算：任一成员越界即整条链推不动
            val overflow = moved.values.any { member ->
                val next = member.shift(direction)
                (direction.x != 0 && (next.x < 0 || next.right > columns)) ||
                    (direction.y < 0 && next.y < 0)
            }
            if (overflow) return null
            // 整链同步前进一格
            moved.replaceAll { _, member -> member.shift(direction) }
        }
        return moved
    }

    /**
     * 缩放结算：依据指针所在的单元格 [pointer] 计算缩放布局，
     * 锚定被拖动边 [edge] 的对侧，扩张方向上的网格边缘与 [limits] 为硬性界限。
     *
     * 扩张压到的卡片沿被拖边方向被推至与前沿齐平，并联动推开链条上的其他卡片；
     * 链条推不动时，跨度逐格回退至可推动的最远位置；收缩方向不受推挤影响。
     */
    fun resolveResize(
        current: CardRect,
        edge: ResizeEdge,
        pointer: IntOffset,
        columns: Int,
        limits: CardLimits,
        obstacles: List<CardRect>
    ): ResizeResult {
        val lim = limits.clampedFor(columns)
        val horizontal = edge == ResizeEdge.Start || edge == ResizeEdge.End
        val minSpan = if (horizontal) lim.minWidth else lim.minHeight
        val maxSpan = if (horizontal) lim.maxWidth else lim.maxHeight
        // 硬性界限下被拖动边的最大跨度，与阻挡卡片无关
        val hardMax = when (edge) {
            ResizeEdge.End -> columns - current.x
            ResizeEdge.Start -> current.right
            ResizeEdge.Bottom -> Int.MAX_VALUE
            ResizeEdge.Top -> current.bottom
        }.coerceAtLeast(1)
        val upper = minOf(maxSpan, hardMax).coerceAtLeast(1)
        val span = when (edge) {
            ResizeEdge.End -> pointer.x - current.x
            ResizeEdge.Start -> current.right - pointer.x
            ResizeEdge.Bottom -> pointer.y - current.y
            ResizeEdge.Top -> current.bottom - pointer.y
        }.coerceIn(minSpan.coerceAtMost(upper), upper)

        val desired = withSpan(current, edge, span)
        if (span <= spanOf(current, edge)) return ResizeResult(desired, emptyMap())

        val axis = if (horizontal) PushAxis.Horizontal else PushAxis.Vertical
        val forward = edge == ResizeEdge.End || edge == ResizeEdge.Bottom
        var candidate = desired
        while (true) {
            val pushed = push(candidate, axis, forward, columns, obstacles)
            if (pushed != null) return ResizeResult(candidate, pushed)
            val shrunk = spanOf(candidate, edge) - 1
            if (shrunk < spanOf(current, edge)) return ResizeResult(current, emptyMap())
            candidate = withSpan(candidate, edge, shrunk)
        }
    }

    /**
     * 推挤求解：沿 [axis] 的 [forward] 方向把与 [mover] 实际重叠的卡片
     * 依次推至与 mover 前沿齐平，被推入的新位压到的卡片随之联动。
     * 任何一张卡片被推离网格（横向越界或反向越过网格边缘）即整条链条推不动。
     *
     * @return 被推动的卡片（id -> 新布局），推不动时返回 null
     */
    internal fun push(
        mover: CardRect,
        axis: PushAxis,
        forward: Boolean,
        columns: Int,
        obstacles: List<CardRect>
    ): Map<String, CardRect>? {
        val horizontal = axis == PushAxis.Horizontal

        // 卡片朝向前沿的一侧
        fun leading(card: CardRect): Int = when {
            horizontal && forward -> card.x
            horizontal -> card.right
            forward -> card.y
            else -> card.bottom
        }
        fun span(card: CardRect): Int = if (horizontal) card.width else card.height
        fun placedAt(card: CardRect, front: Int): CardRect = when {
            horizontal && forward -> card.copy(x = front)
            horizontal -> card.copy(x = front - card.width)
            forward -> card.copy(y = front)
            else -> card.copy(y = front - card.height)
        }

        // 前沿从 mover 的推进侧出发，随推挤前进
        var front = when {
            horizontal && forward -> mover.right
            horizontal -> mover.x
            forward -> mover.bottom
            else -> mover.y
        }
        val pushed = mutableMapOf<String, CardRect>()
        // 只有与 mover 实际重叠的卡片才会被推；被推入的矩形可能压到扩张带之外的卡片，待推队列随之动态增长
        val queue = obstacles
            .filter { it.id != mover.id && it.intersects(mover) }
            .toMutableList()
        while (queue.isNotEmpty()) {
            queue.sortBy { if (forward) leading(it) else -leading(it) }
            val card = queue.removeAt(0)
            // 距前沿最近的卡片已被容纳到前沿之外，其后的卡片更远，推挤结束
            if (if (forward) leading(card) >= front else leading(card) <= front) break
            val placed = placedAt(card, front)
            front += if (forward) span(card) else -span(card)
            val blocked = when {
                horizontal && forward -> front > columns
                forward -> false
                else -> front < 0
            }
            if (blocked) return null
            pushed[card.id] = placed
            queue.addAll(
                obstacles.filter {
                    it.id != mover.id && it.id !in pushed && it.intersects(placed) && it !in queue
                }
            )
        }
        return pushed
    }

    private fun spanOf(rect: CardRect, edge: ResizeEdge): Int = when (edge) {
        ResizeEdge.Start, ResizeEdge.End -> rect.width
        ResizeEdge.Top, ResizeEdge.Bottom -> rect.height
    }

    private fun withSpan(rect: CardRect, edge: ResizeEdge, span: Int): CardRect = when (edge) {
        ResizeEdge.End -> rect.copy(width = span)
        ResizeEdge.Start -> rect.copy(x = rect.right - span, width = span)
        ResizeEdge.Bottom -> rect.copy(height = span)
        ResizeEdge.Top -> rect.copy(y = rect.bottom - span, height = span)
    }

    /**
     * 为尺寸 [width]×[height] 的卡片寻找最上、最左的空闲位置（贪心打包）。
     */
    fun findTopLeftFreeSlot(
        width: Int,
        height: Int,
        columns: Int,
        obstacles: List<CardRect>
    ): IntOffset {
        val maxRow = obstacles.maxOfOrNull { it.bottom } ?: 0
        for (cy in 0..maxRow) {
            for (cx in 0..columns - width) {
                val candidate = CardRect("", cx, cy, width, height)
                if (obstacles.none { it.intersects(candidate) }) {
                    return IntOffset(cx, cy)
                }
            }
        }
        return IntOffset(0, maxRow)
    }

    /**
     * 按阅读顺序（先上后下、先左后右）贪心重排，
     * 用于网格宽度变化后的布局迁移：卡片宽高按新旧列数比例折算，
     * 以 [limits] 声明的边界钳制，再逐个放入最上最左的空位。
     */
    fun reflow(
        cards: List<CardRect>,
        oldColumns: Int,
        columns: Int,
        limits: (CardRect) -> CardLimits = { CardLimits.DEFAULT }
    ): List<CardRect> {
        if (cards.isEmpty()) return cards
        val scale = columns.toFloat() / oldColumns.coerceAtLeast(1)
        val placed = mutableListOf<CardRect>()
        for (card in cards.sortedWith(readingOrder())) {
            val lim = limits(card).clampedFor(columns)
            val width = (card.width * scale).roundToInt().let { lim.clampWidth(it) }
            val height = (card.height * scale).roundToInt().let { lim.clampHeight(it) }
            val slot = findTopLeftFreeSlot(width, height, columns, placed)
            placed.add(card.copy(x = slot.x, y = slot.y, width = width, height = height))
        }
        return placed
    }

    /**
     * 加载校验：钳制越界与非法的卡片、化解卡片间的重叠，其余布局原样保留，
     * 按阅读顺序返回。重复 id 的卡片仅保留最先出现的一个。
     */
    fun validate(
        cards: List<CardRect>,
        columns: Int,
        limits: (CardRect) -> CardLimits = { CardLimits.DEFAULT }
    ): List<CardRect> {
        val seen = mutableSetOf<String>()
        val clamped = mutableListOf<CardRect>()
        for (card in cards) {
            if (!seen.add(card.id)) continue
            val lim = limits(card).clampedFor(columns)
            val width = lim.clampWidth(card.width.coerceAtLeast(1))
            val height = lim.clampHeight(card.height.coerceAtLeast(1))
            val x = card.x.coerceIn(0, columns - width)
            val y = card.y.coerceAtLeast(0)
            clamped.add(card.copy(x = x, y = y, width = width, height = height))
        }
        val settled = mutableListOf<CardRect>()
        for (card in clamped.sortedWith(readingOrder())) {
            val position = if (settled.any { it.intersects(card) }) {
                findNearestFreeSlot(
                    width = card.width,
                    height = card.height,
                    origin = IntOffset(card.x, card.y),
                    columns = columns,
                    obstacles = settled
                ) ?: IntOffset(0, totalRows(settled))
            } else {
                IntOffset(card.x, card.y)
            }
            settled.add(card.positionAt(position))
        }
        return settled.sortedWith(readingOrder())
    }

    /** 矩形沿单位轴向量平移一格 */
    private fun CardRect.shift(direction: IntOffset): CardRect =
        positionAt(IntOffset(x + direction.x, y + direction.y))

    private fun readingOrder() = compareBy<CardRect>({ it.y }, { it.x })
}
