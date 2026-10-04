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

package com.movtery.zalithlauncher.ui.screens.content.home

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.animate
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import com.movtery.zalithlauncher.setting.enums.ActionMenuSide
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * 提起 ActionMenu 时的放大倍率
 */
private const val PickUpScale = 1.08f

interface ActionMenuDragHandler {
    fun onDragStart(position: Offset)
    fun onDrag(position: Offset)
    fun onDragEnd()
    fun onDragCancel()

    /** 登记自行处理长按的内部区域 */
    fun addExclusion(exclusion: ActionMenuDragExclusion)
    /** 移除已登记的内部区域 */
    fun removeExclusion(exclusion: ActionMenuDragExclusion)
}

/**
 * 自行处理长按的内部区域（根坐标系矩形，随布局更新）
 */
class ActionMenuDragExclusion {
    internal var bounds: Rect? = null
}

/**
 * 当前生效的 ActionMenu 长按拖拽处理器
 */
val LocalActionMenuDrag = staticCompositionLocalOf<ActionMenuDragHandler?> { null }

/**
 * 标记背景板为 ActionMenu 的长按拖拽锚点
 */
@Composable
fun Modifier.actionMenuDragAnchor(): Modifier {
    val handler = LocalActionMenuDrag.current ?: return this
    var anchorCoordinates by remember { mutableStateOf<LayoutCoordinates?>(null) }
    return this
        .onGloballyPositioned { anchorCoordinates = it }
        .pointerInput(handler) {
            detectDragGesturesAfterLongPress(
                onDragStart = { position ->
                    anchorCoordinates?.let { coordinates ->
                        handler.onDragStart(coordinates.positionInRoot() + position)
                    }
                },
                onDragEnd = { handler.onDragEnd() },
                onDragCancel = { handler.onDragCancel() },
                onDrag = { change, _ ->
                    anchorCoordinates?.let { coordinates ->
                        handler.onDrag(coordinates.positionInRoot() + change.position)
                    }
                }
            )
        }
}

/**
 * 声明该元素区域自行处理长按
 */
@Composable
fun Modifier.actionMenuDragExclusion(): Modifier {
    val handler = LocalActionMenuDrag.current ?: return this
    val exclusion = remember { ActionMenuDragExclusion() }
    DisposableEffect(handler) {
        handler.addExclusion(exclusion)
        onDispose {
            handler.removeExclusion(exclusion)
        }
    }
    return this.onGloballyPositioned { coordinates ->
        exclusion.bounds = if (coordinates.isAttached) coordinates.boundsInRoot() else null
    }
}

/**
 * 创建 [ActionMenuDragState]
 * @param onCommit 松手后提交最终停泊侧
 */
@Composable
fun rememberActionMenuDragState(
    onCommit: (ActionMenuSide) -> Unit
): ActionMenuDragState {
    val scope = rememberCoroutineScope()
    val settleSpec = MaterialTheme.motionScheme.fastSpatialSpec<Offset>()
    val scaleSpec = MaterialTheme.motionScheme.fastSpatialSpec<Float>()
    val previewSpec = MaterialTheme.motionScheme.fastSpatialSpec<Float>()
    val currentOnCommit by rememberUpdatedState(onCommit)
    return remember {
        ActionMenuDragState(
            scope = scope,
            settleSpec = settleSpec,
            scaleSpec = scaleSpec,
            previewSpec = previewSpec,
            onCommit = { currentOnCommit(it) }
        )
    }
}

/**
 * ActionMenu 长按拖拽的状态持有者
 */
@Stable
class ActionMenuDragState(
    private val scope: CoroutineScope,
    private val settleSpec: AnimationSpec<Offset>,
    private val scaleSpec: AnimationSpec<Float>,
    private val previewSpec: AnimationSpec<Float>,
    private val onCommit: (ActionMenuSide) -> Unit
) : ActionMenuDragHandler {
    /**
     * ActionMenu 是否处于提起状态
     */
    var floating by mutableStateOf(false)
        private set

    /**
     * 拖拽期间预览的停泊侧，null 表示未在拖拽
     */
    var previewSide by mutableStateOf<ActionMenuSide?>(null)
        private set

    /**
     * 提起状态的放大比例
     */
    var scale by mutableFloatStateOf(1f)
        private set

    /**
     * 提起后卡片左上角在父容器坐标系下的位置
     */
    var cardPosition by mutableStateOf(Offset.Zero)
        private set

    /**
     * 松手后的归位动画位移，叠加在停泊位置上
     */
    val settleOffset = Animatable(Offset.Zero, Offset.VectorConverter)

    /**
     * 预览让位时内容区的水平位移（布局方向坐标，向末尾侧为正），
     * 停泊侧切换预览时内容区向停泊侧平移一个槽位
     */
    val previewShift = Animatable(0f, Float.VectorConverter)

    private var settleJob: Job? = null
    private var scaleJob: Job? = null
    private var fingerAtGrab = Offset.Zero
    private var cardPositionAtGrab = Offset.Zero

    private val exclusions = mutableListOf<ActionMenuDragExclusion>()

    /**
     * 已停泊侧（持久化状态）
     */
    var dockedSide = ActionMenuSide.END

    /**
     * 布局方向是否为从右到左
     */
    var isRtl = false

    /**
     * 父容器在根坐标系下的位置，用于把手指位置换算到父容器坐标
     */
    var parentOrigin = Offset.Zero

    /**
     * 父容器宽度（像素），用于换算屏幕中线与停泊位置
     */
    var parentWidthPx = 0f

    /**
     * 停泊槽内容区与屏幕边缘的间距（像素）
     */
    var outerPaddingPx = 0f

    /**
     * 停泊槽宽度（像素），即换边时内容区与卡片的位移量
     */
    var menuSpanPx = 0f

    /**
     * @return 指定侧停泊槽内容区域的位置（父容器坐标）
     */
    fun landingOf(side: ActionMenuSide): Offset {
        val physicallyLeft = if (isRtl) side == ActionMenuSide.END else side == ActionMenuSide.START
        val x = if (physicallyLeft) outerPaddingPx else parentWidthPx - menuSpanPx
        return Offset(x, outerPaddingPx)
    }

    override fun addExclusion(exclusion: ActionMenuDragExclusion) {
        exclusions.add(exclusion)
    }

    override fun removeExclusion(exclusion: ActionMenuDragExclusion) {
        exclusions.remove(exclusion)
    }

    override fun onDragStart(position: Offset) {
        //落点位于自行处理长按的内部区域时不接管，避免与内部长按手势同时触发
        if (exclusions.any { it.bounds?.contains(position) == true }) return

        settleJob?.cancel()
        fingerAtGrab = position - parentOrigin
        //以当前渲染位置（停泊位叠加进行中的归位偏移）为基准，提起瞬间不产生位移
        cardPositionAtGrab = landingOf(dockedSide) + settleOffset.value
        cardPosition = cardPositionAtGrab
        previewSide = dockedSide
        floating = true
        animatePreviewTo(0f)
        animateScaleTo(PickUpScale)
    }

    override fun onDrag(position: Offset) {
        if (!floating) return
        val finger = position - parentOrigin
        cardPosition = cardPositionAtGrab + (finger - fingerAtGrab)
        //以手指位置是否处于屏幕物理左半侧来判断预览停泊侧
        val physicalLeft = finger.x < parentWidthPx / 2f
        val side = if (isRtl) {
            if (physicalLeft) ActionMenuSide.END else ActionMenuSide.START
        } else {
            if (physicalLeft) ActionMenuSide.START else ActionMenuSide.END
        }
        if (side != previewSide) {
            previewSide = side
            animatePreviewTo(previewShiftTargetOf(side))
        }
    }

    override fun onDragEnd() {
        if (!floating) return
        val target = previewSide ?: dockedSide
        settle(target)
    }

    override fun onDragCancel() {
        if (!floating) return
        settle(dockedSide)
    }

    /**
     * 松开卡片，从当前位置动画归位到目标停泊槽
     */
    private fun settle(target: ActionMenuSide) {
        settleJob?.cancel()
        val releasePosition = cardPosition
        settleJob = scope.launch {
            if (target != dockedSide) {
                onCommit(target)
                val shiftTarget = previewShiftTargetOf(target)
                previewShift.snapTo(previewShift.value - shiftTarget)
            }
            launch {
                previewShift.animateTo(0f, previewSpec)
            }
            settleOffset.snapTo(releasePosition - landingOf(target))
            previewSide = null
            floating = false
            launch { animateScaleTo(1f) }
            settleOffset.animateTo(Offset.Zero, settleSpec)
        }
    }

    private fun animateScaleTo(target: Float) {
        scaleJob?.cancel()
        scaleJob = scope.launch {
            animate(
                initialValue = scale,
                targetValue = target,
                animationSpec = scaleSpec
            ) { value, _ -> scale = value }
        }
    }

    /**
     * @return 预览指定侧时内容区需要的水平位移
     */
    private fun previewShiftTargetOf(side: ActionMenuSide): Float = when {
        side == dockedSide -> 0f
        dockedSide == ActionMenuSide.END -> menuSpanPx
        else -> -menuSpanPx
    }

    private fun animatePreviewTo(target: Float) {
        scope.launch {
            previewShift.animateTo(target, previewSpec)
        }
    }
}
