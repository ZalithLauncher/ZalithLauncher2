package com.movtery.guide

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect

/**
 * 点击推进裁决
 */
internal enum class TapAction {
    /** 立即推进到下一步骤 */
    Advance,
    /** 待下一步骤锚点就绪后推进 */
    GateArm,
    /** 不推进 */
    None
}

/**
 * 点击位置相对当前步骤的区域
 */
private enum class TapRegion { Anchor, Content, Scrim }

/**
 * 引导触摸事件仲裁器
 */
internal class GuideEventArbiter internal constructor(
    private val guides: List<GuideController>,
    private val registry: GuideRegistry
) {
    /** [controller] 当前激活的引导状态，无激活流时为 null */
    private fun activeState(controller: GuideController): GuideState.Active? =
        controller.state.value as? GuideState.Active

    /**
     * position 处的触摸是否放行到宿主内容：
     * 无激活流时全放行；激活时仅放行模式的锚点区域放行
     */
    fun passThroughAt(position: Offset): Boolean {
        val state = guides.firstNotNullOfOrNull { activeState(it) } ?: return true
        if (state.entry.isIntro) return false
        if (state.entry.nodeClick != NodeClickMode.PassThrough) return false
        return registry.rectsFor(state.entry.key).any { it.contains(position) }
    }

    /** 门控推进是否对 [state] 生效：已开启且下一步骤存在可解析的锚点 */
    fun gateActive(controller: GuideController, state: GuideState.Active): Boolean {
        val next = controller.entries.getOrNull(state.index + 1) ?: return false
        return state.entry.waitForNextNode && !next.isIntro
    }

    /** 下一步骤的锚点是否已注册就绪 */
    fun nextAnchorReady(controller: GuideController, state: GuideState.Active): Boolean {
        val next = controller.entries.getOrNull(state.index + 1) ?: return false
        if (next.isIntro) return false
        return registry.nodesFor(next.key).isNotEmpty()
    }

    /**
     * 单次点击的推进裁决：
     * 介绍步骤任意点击推进；锚点与空白区域按推进配置推进，门控生效时改为表达推进意图；
     * 引导内容区域不推进
     */
    fun resolveTap(
        controller: GuideController,
        contentRect: Rect?,
        position: Offset
    ): TapAction {
        val state = activeState(controller) ?: return TapAction.None
        if (state.entry.isIntro) return TapAction.Advance
        return when (regionAt(state, contentRect, position)) {
            TapRegion.Anchor -> tapAction(controller, state)
            TapRegion.Content -> TapAction.None
            TapRegion.Scrim -> if (state.entry.advanceOnScrimClick) {
                tapAction(controller, state)
            } else {
                TapAction.None
            }
        }
    }

    private fun tapAction(
        controller: GuideController,
        state: GuideState.Active
    ): TapAction = if (gateActive(controller, state)) TapAction.GateArm else TapAction.Advance

    private fun regionAt(
        state: GuideState.Active,
        contentRect: Rect?,
        position: Offset
    ): TapRegion = when {
        registry.rectsFor(state.entry.key).any { it.contains(position) } -> TapRegion.Anchor
        contentRect?.contains(position) == true -> TapRegion.Content
        else -> TapRegion.Scrim
    }
}
