package com.movtery.guide

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.unit.Dp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * 进程内引导流激活仲裁；同一时刻最多一条引导流处于激活态，仅限主线程访问
 */
internal object GuideHub {
    private var active: GuideController? = null

    fun tryActivate(controller: GuideController): Boolean {
        val current = active
        if (current === controller) return true
        // 残留的已结束/未启动控制器不占用激活位（如宿主重建后未正常收尾）
        if (current != null && current.state.value is GuideState.Active) return false
        active = controller
        return true
    }

    fun deactivate(controller: GuideController) {
        if (active === controller) active = null
    }

    fun reset() {
        active = null
    }
}

/**
 * 一条引导流，持有步骤序列与运行状态
 */
class GuideController internal constructor(
    internal val entries: List<GuideEntry>,
    internal val colors: GuideColors,
    internal val holeRadius: Dp,
    internal val holeBorderWidth: Dp,
    internal val backBehavior: GuideBack
) {
    private val _state = MutableStateFlow<GuideState>(GuideState.Idle)
    val state: StateFlow<GuideState> = _state.asStateFlow()

    /**
     * 从第一个步骤启动引导流
     * @return 已有其他引导流激活时返回 false
     */
    fun start(): Boolean {
        if (entries.isEmpty()) return false
        if (!GuideHub.tryActivate(this)) return false
        _state.value = GuideState.Active(0, entries.first(), emptyList())
        return true
    }

    /**
     * 推进到下一步骤，已在最后一步时结束引导流
     */
    fun next() {
        val current = _state.value as? GuideState.Active ?: return
        val nextIndex = current.index + 1
        if (nextIndex < entries.size) {
            _state.value = GuideState.Active(nextIndex, entries[nextIndex], emptyList())
        } else {
            finish()
        }
    }

    /**
     * 立即结束引导流
     */
    fun finish() {
        if (_state.value is GuideState.Active) {
            GuideHub.deactivate(this)
            _state.value = GuideState.Finished
        }
    }

    internal fun updateAnchors(index: Int, rects: List<Rect>) {
        val current = _state.value as? GuideState.Active ?: return
        if (current.index == index && current.anchors != rects) {
            _state.value = current.copy(anchors = rects)
        }
    }
}
