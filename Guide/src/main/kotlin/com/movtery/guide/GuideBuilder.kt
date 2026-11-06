package com.movtery.guide

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * [rememberGuide] 的步骤收集器
 */
class GuideBuilder internal constructor() {
    internal val entries = mutableListOf<GuideEntry>()

    /**
     * 声明一个引导步骤，声明顺序即步骤顺序
     * @param showNextTip 是否显示下一步提示
     * @param waitForNextNode 门控推进，仅在锚点点击放行模式下默认开启
     */
    fun entry(
        key: GuideKey,
        nodeClick: NodeClickMode = NodeClickMode.Intercept,
        waitForNextNode: Boolean = nodeClick == NodeClickMode.PassThrough,
        advanceOnScrimClick: Boolean = true,
        placement: GuidePlacement = GuidePlacement.Auto,
        showNextTip: Boolean = true,
        content: @Composable (GuideScope) -> Unit
    ) {
        entries += GuideEntry(
            key = key,
            nodeClick = nodeClick,
            waitForNextNode = waitForNextNode,
            advanceOnScrimClick = advanceOnScrimClick,
            placement = placement,
            showNextTip = showNextTip,
            content = content
        )
    }

    /**
     * 声明一个介绍步骤
     * @param showNextTip 是否显示下一步提示
     */
    fun intro(showNextTip: Boolean = true, content: @Composable (GuideScope) -> Unit) {
        entries += GuideEntry(
            key = object : GuideKey {},
            nodeClick = NodeClickMode.Intercept,
            advanceOnScrimClick = true,
            placement = GuidePlacement.Fixed(Alignment.Center),
            showNextTip = showNextTip,
            content = content,
            isIntro = true
        )
    }
}

/**
 * 构建并记住一条引导流
 *
 * @param colors 引导层配色：遮罩、内容默认色
 * @param holeRadius 锚点未声明镂空圆角时的全局默认值
 * @param holeBorderWidth 锚点未声明镂空描边时的全局默认值，0 表示无边框
 * @param shape 锚点未声明内容裁剪形状时的全局默认值
 */
@Composable
fun rememberGuide(
    colors: GuideColors = GuideDefaults.colors,
    holeRadius: Dp = GuideDefaults.holeRadius,
    holeBorderWidth: Dp = 0.dp,
    shape: Shape = GuideDefaults.contentShape,
    backBehavior: GuideBack = GuideBack.Block,
    builder: GuideBuilder.() -> Unit
): GuideController = remember(colors, holeRadius, holeBorderWidth, shape, backBehavior) {
    val result = GuideBuilder().apply(builder)
    GuideController(result.entries.toList(), colors, holeRadius, holeBorderWidth, shape, backBehavior)
}
