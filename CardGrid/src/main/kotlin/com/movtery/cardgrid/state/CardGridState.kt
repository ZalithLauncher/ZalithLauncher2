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

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.AnimationVector4D
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.spring
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntOffset
import com.movtery.cardgrid.engine.GridEngine
import com.movtery.cardgrid.model.CardInteraction
import com.movtery.cardgrid.model.CardLimits
import com.movtery.cardgrid.model.CardRect
import com.movtery.cardgrid.model.CardSpacing
import com.movtery.cardgrid.model.CardState
import com.movtery.cardgrid.model.CardType
import com.movtery.cardgrid.model.GridGeometry
import com.movtery.cardgrid.model.MIN_GRID_COLUMNS
import com.movtery.cardgrid.model.ResizeEdge
import com.movtery.cardgrid.model.computeGridGeometry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.math.max
import kotlin.math.roundToInt

/** 已落位的网格卡片 */
data class GridCard(
    val id: String,
    val type: CardType,
    val layout: CardRect
)

/** 待播种的持久化卡片布局，[typeId] 须能在播种的类型表中找到 */
data class CardSeed(
    val id: String,
    val typeId: String,
    val layout: CardRect
)

/**
 * 卡片网格的状态持有者：
 * 统一持有布局结算的会话状态，所有指针坐标均为网格内容坐标系
 * （网格区域左上角为原点、像素单位），命中测试与会话结算均在此坐标系下进行，
 * 布局结算全部委托 [GridEngine]，卡片渲染矩形通过逐卡 [Animatable] 以弹簧动画过渡。
 */
@Stable
class CardGridState internal constructor(
    private val scope: CoroutineScope
) {
    /** 拖动/缩放会话 */
    private data class AdjustSession(
        val card: GridCard,
        val grabOffset: Offset,
        val mode: Mode
    ) {
        sealed interface Mode {
            data object Move : Mode
            data class Resize(val edge: ResizeEdge) : Mode
        }
    }

    /** 网格几何（列数恒为偶数，单元格为正方形） */
    var geometry by mutableStateOf(GridGeometry(MIN_GRID_COLUMNS, 20f))
        private set

    /** 单元格边长（px） */
    var cellPx by mutableFloatStateOf(20f)
        private set

    /** 屏幕密度（dp → px 换算），随几何更新 */
    private var densityFactor by mutableFloatStateOf(1f)

    /** 卡片矩形在单元格内的收缩量（px） */
    var cardInsetPx by mutableIntStateOf(2)
        private set

    /** 全部已落位卡片，保持加入顺序 */
    var cards by mutableStateOf<List<GridCard>>(emptyList())
        private set

    /** 处于调整态（长按选中）的卡片 id */
    var adjustingCardId by mutableStateOf<String?>(null)
        private set

    /** 是否处于调整态 */
    val isAdjusting: Boolean get() = adjustingCardId != null

    /** 吸附后的预览布局（虚影位置），仅会话期间非空 */
    var dragPreview by mutableStateOf<CardRect?>(null)
        private set

    /** 跟随手指的原始矩形，仅会话期间非空 */
    var dragRawRect by mutableStateOf<Rect?>(null)
        private set

    /** 指针在网格内容坐标系中的位置，供网格光晕与自动滚动使用 */
    var pointerPosition by mutableStateOf<Offset?>(null)
        private set

    /** 网格区域在窗口坐标系中的偏移（随滚动变化） */
    internal var areaOffsetInRoot by mutableStateOf(Offset.Zero)
        private set

    /** 网格视口在窗口坐标系中的上缘（窗口坐标不受滚动影响），未上报时为 0 */
    var viewportTopPx by mutableFloatStateOf(0f)
        private set

    /** 网格视口高度（px），未上报时为 0 */
    var viewportHeightPx by mutableFloatStateOf(0f)
        private set

    /** 网格区域布局位置回调 */
    fun onAreaPositioned(offsetInRoot: Offset) {
        areaOffsetInRoot = offsetInRoot
    }

    /** 网格视口布局位置回调（窗口坐标系），供工具条放置判定与自动滚动使用 */
    fun onViewportPositioned(topPx: Float, heightPx: Float) {
        viewportTopPx = topPx
        viewportHeightPx = heightPx
    }

    /**
     * 手指在窗口坐标系中的锚点：窗口坐标不受滚动影响，
     * 指针的网格坐标始终由锚点与网格区域当前偏移整体换算得出，
     * 避免滚动增量与事件坐标之间的反馈振荡。
     */
    private var pointerAnchorInRoot: Offset? = null

    /** 被挤开让位的卡片（id -> 让位布局）：会话期间实时更新为预览态，松手提交后持久化 */
    var displaced by mutableStateOf<Map<String, CardRect>>(emptyMap())
        private set

    /** 布局发生结算后的回调（用于持久化） */
    var onLayoutCommitted: () -> Unit = {}

    /** 卡片被移除后的回调（用于同步外部与该卡片关联的数据） */
    var onCardRemoved: (cardId: String) -> Unit = {}

    /** 快速空间动画（拖动中的让位） */
    internal var fastSpec: AnimationSpec<Rect> = spring(
        dampingRatio = Spring.DampingRatioLowBouncy,
        stiffness = Spring.StiffnessMediumLow
    )

    /** 默认空间动画（结算、压实） */
    internal var defaultSpec: AnimationSpec<Rect> = spring(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMediumLow
    )

    private var session by mutableStateOf<AdjustSession?>(null)

    /** 是否存在进行中的拖动/缩放会话 */
    val hasSession: Boolean get() = session != null

    /** 待播种的持久化卡片，待网格几何就绪后生效 */
    private var pendingSeeds: List<CardSeed>? = null
    private var pendingColumns: Int = 0

    /** 已注册的卡片类型表 */
    private var typeById: Map<String, CardType> = emptyMap()

    /** 网格几何是否已经依据实际容器宽度完成过计算 */
    private var geometryReady = false

    private val animators = mutableMapOf<String, Animatable<Rect, AnimationVector4D>>()

    // ---------- 播种 ----------

    /**
     * 播种卡片类型与持久化的卡片布局：
     * 类型表即刻生效，卡片布局待网格几何就绪后校验修复（列数一致）或按阅读顺序重排（列数不一致）。
     * 重复 id 的卡片仅保留最先出现的一个，类型未知的卡片被丢弃。
     */
    fun seed(types: List<CardType>, seeds: List<CardSeed>, storedColumns: Int) {
        typeById = types.associateBy { it.typeId }
        val distinct = seeds.distinctBy { it.id }.filter { it.typeId in typeById }
        if (distinct.isEmpty()) return

        pendingSeeds = distinct
        pendingColumns = storedColumns
        if (geometryReady) materializePending(newColumns = geometry.columns)
    }

    private fun materializePending(newColumns: Int) {
        val pending = pendingSeeds ?: return
        pendingSeeds = null
        val typeIdById = pending.associate { it.id to it.typeId }
        val layouts = if (pendingColumns == newColumns) {
            GridEngine.validate(pending.map { it.layout }, newColumns) { rect ->
                typeById[typeIdById[rect.id]]?.limits ?: CardLimits.DEFAULT
            }
        } else {
            GridEngine.reflow(
                cards = pending.map { it.layout },
                oldColumns = pendingColumns,
                columns = newColumns
            ) { rect ->
                typeById[typeIdById[rect.id]]?.limits ?: CardLimits.DEFAULT
            }
        }.associateBy { it.id }

        val materialized = pending.mapNotNull { seed ->
            typeById[seed.typeId]?.let { type ->
                GridCard(id = seed.id, type = type, layout = layouts.getValue(seed.id))
            }
        }
        cards = cards + materialized
        materialized.forEach { animateTo(it, effectiveLayout(it), defaultSpec) }
    }

    // ---------- 几何 ----------

    /** 依据容器宽度更新网格；列数变化时触发整体重排 */
    fun updateGeometry(widthDp: Float, density: Density) {
        val oldColumns = geometry.columns
        val newGeometry = computeGridGeometry(widthDp)
        cellPx = newGeometry.cellSize * density.density
        cardInsetPx = with(density) {
            CardSpacing.roundToPx()
        }
        densityFactor = density.density
        geometry = newGeometry
        if (widthDp > 0f) {
            geometryReady = true
            materializePending(newColumns = newGeometry.columns)
        }
        if (newGeometry.columns != oldColumns && cards.isNotEmpty()) {
            reflowTo(newColumns = newGeometry.columns, oldColumns = oldColumns)
        }
    }

    /** 卡片布局对应的渲染矩形（px），锚点为网格左上角 */
    fun rectFor(layout: CardRect): Rect = Rect(
        left = layout.x * cellPx + cardInsetPx,
        top = layout.y * cellPx + cardInsetPx,
        right = layout.right * cellPx - cardInsetPx,
        bottom = layout.bottom * cellPx - cardInsetPx
    )

    /** 网格内容高度（px），含一行备用行供尾部推放 */
    fun gridHeightPx(): Float {
        val rows = max(
            GridEngine.totalRows(cards.map { it.layout }),
            dragPreview?.bottom ?: 0
        )
        return (rows + 1) * cellPx
    }

    // ---------- 卡片管理 ----------

    /** 追加一张卡片，落在最上最左的空闲位置 */
    fun addCard(type: CardType, id: String = UUID.randomUUID().toString()): GridCard? {
        if (cards.any { it.id == id }) return null
        val lim = type.limits.clampedFor(geometry.columns)
        val width = type.defaultSpan.x.coerceIn(lim.minWidth, lim.maxWidth)
        val height = type.defaultSpan.y.coerceIn(lim.minHeight, lim.maxHeight)
        val slot = GridEngine.findTopLeftFreeSlot(
            width = width,
            height = height,
            columns = geometry.columns,
            obstacles = layouts()
        )
        val card = GridCard(
            id = id,
            type = type,
            layout = CardRect(id = id, x = slot.x, y = slot.y, width = width, height = height)
        )
        cards = cards + card
        onLayoutCommitted()
        return card
    }

    /** 移除一张卡片并压实剩余布局 */
    fun removeCard(id: String) {
        val removed = cards.firstOrNull { it.id == id } ?: return
        val remaining = cards.filterNot { it.id == id }
        val compacted = GridEngine.compact(remaining.map { it.layout }).associateBy { it.id }
        cards = remaining.map { card -> card.copy(layout = compacted.getValue(card.id)) }
        animators.remove(removed.id)
        if (adjustingCardId == id) adjustingCardId = null
        if (session?.card?.id == id) endSession()
        cards.forEach { card -> animateTo(card, effectiveLayout(card), defaultSpec) }
        onLayoutCommitted()
        onCardRemoved(id)
    }

    // ---------- 命中测试（网格内容坐标系） ----------

    /** 命中测试卡片，z 序高的优先（调整态卡优先，其余列表靠后者在上，与绘制层叠顺序一致） */
    fun cardAt(position: Offset): GridCard? {
        val ordered = cards
            .withIndex()
            .sortedWith(
                compareByDescending<IndexedValue<GridCard>> { (_, card) ->
                    if (card.id == adjustingCardId) 1 else 0
                }.thenByDescending { it.index }
            )
        return ordered.firstOrNull { (_, card) ->
            rectFor(effectiveLayout(card)).contains(position)
        }?.value
    }

    /**
     * 命中测试调整态卡片的缩放手柄热区，
     * @return 卡片与命中的边，未命中返回 null
     */
    fun resizeEdgeAt(position: Offset): Pair<GridCard, ResizeEdge>? {
        val card = adjustingCardId?.let { id -> cards.firstOrNull { it.id == id } } ?: return null
        val rect = renderRectOf(card)
        val hitRadiusPx = EDGE_HIT_RADIUS_DP * densityFactor
        return ResizeEdge.entries
            .map { edge -> edge to edgeCenter(edge, rect) }
            .map { (edge, center) -> edge to (position - center).getDistance() }
            .filter { (_, distance) -> distance <= hitRadiusPx }
            .minByOrNull { (_, distance) -> distance }
            ?.let { (edge, _) -> card to edge }
    }

    private fun edgeCenter(edge: ResizeEdge, rect: Rect): Offset = when (edge) {
        ResizeEdge.Start -> Offset(rect.left, rect.center.y)
        ResizeEdge.Top -> Offset(rect.center.x, rect.top)
        ResizeEdge.End -> Offset(rect.right, rect.center.y)
        ResizeEdge.Bottom -> Offset(rect.center.x, rect.bottom)
    }

    // ---------- 调整会话：拖动 ----------

    /** 长按成功，卡片进入拖动（同时也进入了调整态） */
    fun onCardDragStart(card: GridCard, pointer: Offset) {
        val layout = effectiveLayout(card)
        session = AdjustSession(
            card = card,
            grabOffset = pointer - rectFor(layout).topLeft,
            mode = AdjustSession.Mode.Move
        )
        adjustingCardId = card.id
        pointerAnchorInRoot = areaOffsetInRoot + pointer
        dragRawRect = rectFor(layout)
        pointerPosition = pointer
        applyPreview(card.layout, emptyMap())
    }

    fun onCardDrag(pointer: Offset) {
        val current = session ?: return
        check(current.mode is AdjustSession.Mode.Move) { "Current session is not a drag session" }

        pointerAnchorInRoot = areaOffsetInRoot + pointer
        pointerPosition = pointer
        val size = rectFor(current.card.layout).size
        val topLeft = Offset(
            pointer.x - current.grabOffset.x,
            pointer.y - current.grabOffset.y
        )
        dragRawRect = Rect(offset = topLeft, size = size)
        // 吸附到单元格，横向钳制在网格内
        // 被压卡片依据手指位置实时做方向性让位预览
        val target = IntOffset(
            (topLeft.x / cellPx).roundToInt().coerceIn(0, geometry.columns - current.card.layout.width),
            (topLeft.y / cellPx).roundToInt().coerceAtLeast(0)
        )
        val preview = current.card.layout.positionAt(target)
        applyPreview(
            preview,
            GridEngine.resolveDisplacements(
                moving = preview,
                columns = geometry.columns,
                cards = layouts(),
                pointer = IntOffset(
                    (pointer.x / cellPx).roundToInt(),
                    (pointer.y / cellPx).roundToInt()
                )
            )
        )
    }

    /** 松手：结算落位与被挤开的卡片，压实并持久化 */
    fun onCardDragEnd() {
        val current = session ?: return
        commit(previewLayoutOf(current))
    }

    /** 拖动被取消：一切回到会话前的状态 */
    fun onCardDragCancel() {
        cancelSession()
    }

    // ---------- 调整会话：缩放 ----------

    /** 开始拖动某条边的手柄，单向调整跨度 */
    fun onResizeStart(card: GridCard, edge: ResizeEdge, pointer: Offset) {
        session = AdjustSession(
            card = card,
            grabOffset = pointer,
            mode = AdjustSession.Mode.Resize(edge)
        )
        adjustingCardId = card.id
        pointerAnchorInRoot = areaOffsetInRoot + pointer
        dragRawRect = rectFor(card.layout)
        pointerPosition = pointer
        applyPreview(card.layout, emptyMap())
    }

    fun onResize(pointer: Offset) {
        val current = session ?: return
        val edge = (current.mode as? AdjustSession.Mode.Resize)?.edge ?: return
        pointerAnchorInRoot = areaOffsetInRoot + pointer
        pointerPosition = pointer
        val result = GridEngine.resolveResize(
            current = current.card.layout,
            edge = edge,
            pointer = IntOffset(
                (pointer.x / cellPx).roundToInt(),
                (pointer.y / cellPx).roundToInt()
            ),
            columns = geometry.columns,
            limits = current.card.type.limits,
            obstacles = layouts().filterNot { it.id == current.card.id }
        )
        dragRawRect = rawRectForResize(current.card, edge, pointer, result.layout)
        applyPreview(result.layout, result.pushed)
    }

    fun onResizeEnd() {
        val current = session ?: return
        commit(previewLayoutOf(current))
    }

    fun onResizeCancel() {
        cancelSession()
    }

    /**
     * 自动滚动后重算指针位置：
     * 手指的窗口锚点不变，滚动改变了网格区域的窗口偏移，
     * 指针的网格坐标由两者整体换算（全量覆盖，无增量累积）。
     */
    internal fun onAutoScroll() {
        val anchor = pointerAnchorInRoot ?: return
        val local = anchor - areaOffsetInRoot
        when (session?.mode) {
            is AdjustSession.Mode.Move -> onCardDrag(local)
            is AdjustSession.Mode.Resize -> onResize(local)
            null -> Unit
        }
    }

    // ---------- 调整态 ----------

    /** 指定卡片当前的交互状态 */
    fun interactionOf(cardId: String): CardInteraction {
        val current = session
        return when {
            current?.card?.id == cardId && current.mode is AdjustSession.Mode.Resize -> CardInteraction.Resizing
            current?.card?.id == cardId && current.mode is AdjustSession.Mode.Move -> CardInteraction.Dragging
            adjustingCardId == cardId -> CardInteraction.Adjusting
            else -> CardInteraction.Idle
        }
    }

    /** 会话是否正作用于指定卡片（渲染时跟手矩形替换动画矩形） */
    fun isSessionCard(cardId: String): Boolean = session?.card?.id == cardId

    /** 退出调整态（点击空白或返回键） */
    fun exitAdjusting() {
        if (session != null) cancelSession()
        adjustingCardId = null
    }

    /** 提供给卡片内容的自身状态（缩放会话期间跟随吸附预览，实时感知尺寸变化） */
    fun cardStateOf(card: GridCard): CardState {
        val layout = if (isSessionCard(card.id)) dragPreview ?: card.layout else card.layout
        return CardState(
            spanWidth = layout.width,
            spanHeight = layout.height,
            columns = geometry.columns,
            interaction = interactionOf(card.id)
        )
    }

    // ---------- 渲染 ----------

    /** 指定卡片的渲染矩形动画器 */
    internal fun animatorFor(card: GridCard): Animatable<Rect, AnimationVector4D> =
        animators.getOrPut(card.id) {
            Animatable(rectFor(card.layout), Rect.VectorConverter)
        }

    /** 单张卡片的当前渲染矩形（会话卡跟手，其余取动画值） */
    internal fun renderRectOf(card: GridCard): Rect {
        if (isSessionCard(card.id)) return dragRawRect ?: animatorFor(card).value
        return animatorFor(card).value
    }

    private fun animateTo(card: GridCard, layout: CardRect, spec: AnimationSpec<Rect>) {
        val animatable = animatorFor(card)
        val target = rectFor(layout)
        scope.launch { animatable.animateTo(target, spec) }
    }

    private fun animateAll() {
        cards.forEach { animateTo(it, effectiveLayout(it), defaultSpec) }
    }

    /**
     * 会话卡结算：动画器先吸附到松手时的跟手矩形，再动画到最终布局，
     * 避免跟手渲染切换回动画渲染时发生瞬移。
     */
    private fun settleSessionCard(card: GridCard, rawRect: Rect?, target: CardRect) {
        val animatable = animatorFor(card)
        scope.launch {
            rawRect?.let { animatable.snapTo(it) }
            animatable.animateTo(rectFor(target), defaultSpec)
        }
    }

    // ---------- 内部：结算 ----------

    internal fun effectiveLayout(card: GridCard): CardRect =
        displaced[card.id] ?: card.layout

    private fun previewLayoutOf(session: AdjustSession): CardRect =
        dragPreview ?: session.card.layout

    private fun layouts(): List<CardRect> = cards.map { it.layout }

    /** 应用新的吸附预览：动画过渡让位中的卡片与刚脱离让位的卡片 */
    private fun applyPreview(preview: CardRect, displacements: Map<String, CardRect>) {
        if (dragPreview == preview && displaced.keys == displacements.keys) return
        dragPreview = preview
        val affected = displaced.keys + displacements.keys
        displaced = displacements
        affected.forEach { id ->
            cards.firstOrNull { it.id == id }?.let { card ->
                animateTo(card, displacements[id] ?: card.layout, fastSpec)
            }
        }
    }

    /** 提交会话结果：沿用会话过程中的让位结算，压实并持久化 */
    private fun commit(target: CardRect) {
        val current = session ?: return
        // endSession 会清空跟手矩形，必须先捕获供动画器吸附
        val rawRect = dragRawRect
        val settled = displaced
        cards = cards.map { card ->
            when {
                card.id == current.card.id -> card.copy(layout = target)
                else -> settled[card.id]?.let { card.copy(layout = it) } ?: card
            }
        }
        cards = compactCards(cards)
        // 压实可能改变会话卡的最终落位，以列表中的最终布局为准
        val finalLayout = cards.firstOrNull { it.id == current.card.id }?.layout ?: target
        endSession()
        settleSessionCard(current.card, rawRect, finalLayout)
        cards.filterNot { it.id == current.card.id }
            .forEach { animateTo(it, effectiveLayout(it), defaultSpec) }
        onLayoutCommitted()
    }

    /** 取消会话：让位卡与会话卡弹回原位，不产生任何结算 */
    private fun cancelSession() {
        val current = session ?: return
        val rawRect = dragRawRect
        val affected = displaced.keys
        endSession()
        affected.forEach { id ->
            cards.firstOrNull { it.id == id }?.let { animateTo(it, it.layout, defaultSpec) }
        }
        settleSessionCard(current.card, rawRect, current.card.layout)
    }

    private fun endSession() {
        session = null
        dragPreview = null
        dragRawRect = null
        pointerPosition = null
        displaced = emptyMap()
    }

    /** 垂直压实全部卡片，保持实例映射 */
    private fun compactCards(list: List<GridCard>): List<GridCard> {
        val compacted = GridEngine.compact(list.map { it.layout }).associateBy { it.id }
        return list.map { card -> card.copy(layout = compacted.getValue(card.id)) }
    }

    /** 列数变化：按阅读顺序重排并持久化 */
    private fun reflowTo(newColumns: Int, oldColumns: Int) {
        val layouts = GridEngine.reflow(
            cards = layouts(),
            oldColumns = oldColumns,
            columns = newColumns
        ) { rect -> cards.firstOrNull { it.id == rect.id }?.type?.limits ?: CardLimits.DEFAULT }
            .associateBy { it.id }
        cards = cards.map { card -> card.copy(layout = layouts.getValue(card.id)) }
        endSession()
        adjustingCardId = null
        animateAll()
        onLayoutCommitted()
    }

    // ---------- 内部：跟手矩形 ----------

    /** 依据指针位置计算缩放时跟手的原始矩形，被拖动边钳制在最小跨度与推挤结算的跨度之间 */
    private fun rawRectForResize(
        card: GridCard,
        edge: ResizeEdge,
        pointer: Offset,
        settled: CardRect
    ): Rect {
        val layout = card.layout
        val lim = card.type.limits.clampedFor(geometry.columns)
        val minSpan = if (edge == ResizeEdge.Start || edge == ResizeEdge.End) lim.minWidth else lim.minHeight
        val maxSpan = when (edge) {
            ResizeEdge.Start, ResizeEdge.End -> settled.width
            ResizeEdge.Top, ResizeEdge.Bottom -> settled.height
        }
        val range = minSpan..maxSpan
        val left = layout.x * cellPx + cardInsetPx
        val top = layout.y * cellPx + cardInsetPx
        val right = layout.right * cellPx - cardInsetPx
        val bottom = layout.bottom * cellPx - cardInsetPx
        // 各跨度对应的被拖动边像素位置
        fun edgePxStart(span: Int) = (layout.right - span) * cellPx + cardInsetPx
        fun edgePxTop(span: Int) = (layout.bottom - span) * cellPx + cardInsetPx
        fun edgePxEnd(span: Int) = (layout.x + span) * cellPx - cardInsetPx
        fun edgePxBottom(span: Int) = (layout.y + span) * cellPx - cardInsetPx
        return when (edge) {
            ResizeEdge.End -> Rect(left, top, pointer.x.coerceIn(edgePxEnd(range.first), edgePxEnd(range.last)), bottom)
            ResizeEdge.Start -> Rect(pointer.x.coerceIn(edgePxStart(range.last), edgePxStart(range.first)), top, right, bottom)
            ResizeEdge.Bottom -> Rect(left, top, right, pointer.y.coerceIn(edgePxBottom(range.first), edgePxBottom(range.last)))
            ResizeEdge.Top -> Rect(left, pointer.y.coerceIn(edgePxTop(range.last), edgePxTop(range.first)), right, bottom)
        }
    }

    companion object {
        /** 缩放手柄热区半径（dp） */
        private const val EDGE_HIT_RADIUS_DP = 24f
    }
}

/** 创建与组合生命周期绑定的 [CardGridState] */
@Composable
fun rememberCardGridState(): CardGridState {
    val scope = rememberCoroutineScope()
    return remember { CardGridState(scope) }
}
