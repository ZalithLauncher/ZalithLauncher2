package com.movtery.guide

import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.Dp

/**
 * 引导锚点注册表，由 [GuideHost] 持有；
 * 记录当前组合中各 [GuideKey] 的锚点边界（根坐标），版本号随任意锚点变化递增
 */
internal class GuideRegistry {
    private val nodes = HashMap<GuideKey, MutableList<GuideNode>>()
    private val scrollHandlers = mutableListOf<GuideScrollHandler>()
    private val versionState = mutableIntStateOf(0)

    /**
     * 注册表版本号，引导层读取它以感知锚点变化
     */
    val version: Int
        get() = versionState.intValue

    fun attach(node: GuideNode) {
        nodes.getOrPut(node.key) { mutableListOf() }.add(node)
        bump()
    }

    fun detach(node: GuideNode) {
        val list = nodes[node.key] ?: return
        if (list.remove(node)) {
            if (list.isEmpty()) nodes.remove(node.key)
            bump()
        }
    }

    fun updateBounds(node: GuideNode, bounds: Rect) {
        if (node.bounds != bounds) {
            node.bounds = bounds
            bump()
        }
    }

    fun updatePreferSide(node: GuideNode, side: GuideSide?) {
        if (node.preferSide != side) {
            node.preferSide = side
            bump()
        }
    }

    /**
     * 更新锚点的镂空样式声明
     */
    fun updateStyle(node: GuideNode, radius: Dp?, borderWidth: Dp?, borderColor: Color?) {
        if (node.holeRadius != radius || node.holeBorderWidth != borderWidth || node.holeBorderColor != borderColor) {
            node.holeRadius = radius
            node.holeBorderWidth = borderWidth
            node.holeBorderColor = borderColor
            bump()
        }
    }

    /**
     * 指定 key 的全部锚点节点
     */
    internal fun nodesFor(key: GuideKey): List<GuideNode> = nodes[key].orEmpty()

    /**
     * 指定 key 的全部锚点边界
     */
    fun rectsFor(key: GuideKey): List<Rect> = nodesFor(key).map(GuideNode::bounds)

    /**
     * 指定 key 的方向推荐，仅恰好一个锚点节点声明推荐时生效；多锚点时忽略推荐
     */
    fun sideHintFor(key: GuideKey): GuideSide? =
        nodes[key]?.singleOrNull()?.preferSide

    /**
     * 依次询问滚动容器能否定位到该 key 的锚点
     * @return 是否有容器接管
     */
    suspend fun requestScroll(key: GuideKey): Boolean {
        scrollHandlers.toList().forEach { handler ->
            if (handler(key)) return true
        }
        return false
    }

    /**
     * 请求将 key 的锚点带入视口
     */
    suspend fun bringIntoView(key: GuideKey) {
        nodes[key].orEmpty().forEach { node ->
            node.bringIntoViewRequester.bringIntoView(Rect.Zero)
        }
    }

    fun addScrollHandler(handler: GuideScrollHandler) {
        scrollHandlers.add(handler)
    }

    fun removeScrollHandler(handler: GuideScrollHandler) {
        scrollHandlers.remove(handler)
    }

    private fun bump() {
        versionState.intValue++
    }
}

/**
 * 滚动容器的锚点定位回调
 * @return true 表示已处理该 key 的定位
 */
internal typealias GuideScrollHandler = suspend (GuideKey) -> Boolean

/**
 * 一个被标记的引导锚点
 */
internal class GuideNode internal constructor(internal val key: GuideKey) {
    internal var bounds: Rect = Rect.Zero
    internal var preferSide: GuideSide? = null
    internal var holeRadius: Dp? = null
    internal var holeBorderWidth: Dp? = null
    internal var holeBorderColor: Color? = null
    internal val bringIntoViewRequester = BringIntoViewRequester()
}

/**
 * 引导锚点注册表，由 [GuideHost] 向其内容树提供；无画布时为 null
 */
internal val LocalGuideRegistry = compositionLocalOf<GuideRegistry?> { null }

/**
 * 将组件标记为引导锚点；多个组件标记同一个 [key] 时，作为同一步骤的一组锚点
 * @param preferSide 方向推荐：引导内容优先展示在组件该侧；多锚点或该侧装不下时回落自动求解
 * @param holeRadius 镂空圆角半径，null 时回落引导流的全局配置
 * @param holeBorderWidth 镂空描边宽度，null 时回落引导流的全局配置
 * @param holeBorderColor 镂空描边颜色，null 时使用库默认颜色
 */
fun Modifier.guideNode(
    key: GuideKey,
    preferSide: GuideSide? = null,
    holeRadius: Dp? = null,
    holeBorderWidth: Dp? = null,
    holeBorderColor: Color? = null
): Modifier = composed {
    val registry = LocalGuideRegistry.current ?: return@composed this
    val node = remember(key) { GuideNode(key) }
    DisposableEffect(key, registry) {
        registry.attach(node)
        onDispose { registry.detach(node) }
    }
    SideEffect {
        registry.updatePreferSide(node, preferSide)
        registry.updateStyle(node, holeRadius, holeBorderWidth, holeBorderColor)
    }
    this
        .bringIntoViewRequester(node.bringIntoViewRequester)
        .onGloballyPositioned { coordinates ->
            registry.updateBounds(node, coordinates.boundsInRoot())
        }
}

/**
 * 登记滚动容器的锚点定位能力
 * 引导步骤的锚点缺失时库会依次询问各容器，[scroll] 返回 true 表示已处理该 key 的定位
 */
fun Modifier.guideScrollTo(scroll: suspend (GuideKey) -> Boolean): Modifier = composed {
    val registry = LocalGuideRegistry.current ?: return@composed this
    val current by rememberUpdatedState(scroll)
    DisposableEffect(registry) {
        val handler: GuideScrollHandler = { key -> current(key) }
        registry.addScrollHandler(handler)
        onDispose { registry.removeScrollHandler(handler) }
    }
    this
}