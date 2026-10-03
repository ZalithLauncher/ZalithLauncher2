package com.movtery.guide

import androidx.compose.animation.core.Spring
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * 引导库默认值
 */
object GuideDefaults {
    /**
     * 默认引导层配色
     */
    val colors = GuideColors()
    /**
     * 镂空圆角半径
     */
    val holeRadius: Dp = 8.dp
    /**
     * 镂空描边默认颜色
     */
    val holeBorder: Color = Color.White
    /**
     * 下一步提示与屏幕边缘的留白
     */
    val nextTipPadding: Dp = 24.dp
    /**
     * 下一步提示与障碍物的判定边距
     */
    val nextTipMargin: Dp = 16.dp
    /**
     * 引导内容与锚点的间距
     */
    val contentGap: Dp = 12.dp
    /**
     * 引导内容与屏幕边缘的最小留白
     */
    val screenPadding: Dp = 16.dp
    /**
     * MD3 Expressive 风格的空间弹性
     */
    val expressiveSpatial = GuideSpring(dampingRatio = 0.9f, stiffness = 380f)
    /**
     * MD3 Expressive 风格的透明度过渡
     */
    val expressiveFade = GuideSpring(dampingRatio = 1f, stiffness = Spring.StiffnessLow)
}

@Immutable
data class GuideSpring(
    /** 阻尼比 */
    val dampingRatio: Float,
    /** 刚度 */
    val stiffness: Float
)

/**
 * 引导层动画配置
 */
@Immutable
class GuideAnimations(
    /**
     * 是否启用动画
     */
    val enabled: Boolean = true,
    /**
     * 空间过渡弹性
     */
    val spatialSpring: GuideSpring = GuideDefaults.expressiveSpatial,
    /**
     * 透明度过渡弹性
     */
    val fadeSpring: GuideSpring = GuideDefaults.expressiveFade
) {
    companion object {
        val Default = GuideAnimations()
        /**
         * 关闭全部动画
         */
        val None = GuideAnimations(enabled = false)
    }
}
