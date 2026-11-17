package com.movtery.guide

import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.IntOffset

/**
 * 引导内容相对锚点的方位
 */
enum class GuideSide {
    Above, Below, Start, End
}

/**
 * 引导内容相对锚点的摆放策略
 */
sealed class GuidePlacement {
    /**
     * 自动求解：不遮挡锚点、不出屏为硬约束，可行解中取最靠近锚点包围盒中心者
     */
    data object Auto : GuidePlacement()

    /**
     * 完全由对齐方式与偏移决定，不做约束检查
     */
    data class Fixed(
        val alignment: Alignment,
        val offset: IntOffset = IntOffset.Zero
    ) : GuidePlacement()
}

/**
 * 方向偏好：该方向可行时优先采用，否则回落自动求解；
 * 仅由单锚点节点的方向推荐驱动，不对外暴露
 */
internal data class PreferSide(val side: GuideSide) : GuidePlacement()
