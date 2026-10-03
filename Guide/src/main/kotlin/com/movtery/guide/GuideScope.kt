package com.movtery.guide

import androidx.compose.runtime.State
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.unit.IntSize

/**
 * 引导内容布局时可读取的上下文
 */
class GuideScope internal constructor(
    /**
     * 当前步骤全部锚点的边界（根坐标）
     */
    val anchors: List<Rect>,

    /**
     * 引导层容器尺寸
     */
    val containerSize: IntSize,
    private val sideState: State<GuideSide?>,

    /**
     * 所属引导流控制器，可通过 [GuideController.next] 等驱动步骤
     */
    val controller: GuideController
) {
    /**
     * 全部锚点的包围盒
     */
    val bounds: Rect = anchors.boundsUnion()

    /**
     * 自动求解出的方位；[GuidePlacement.Fixed] 或屏幕居中兜底时为 null
     */
    val side: GuideSide?
        get() = sideState.value
}
