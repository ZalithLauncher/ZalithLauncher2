package com.movtery.guide

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GuideEventArbiterTest {
    private object K1 : GuideKey
    private object K2 : GuideKey

    private val anchorRect = Rect(0f, 0f, 100f, 100f)
    private val inside = Offset(50f, 50f)
    private val outside = Offset(200f, 200f)
    private val contentRect = Rect(300f, 300f, 400f, 400f)

    @Before
    fun resetHub() {
        GuideHub.reset()
    }

    private fun entry(
        key: GuideKey,
        nodeClick: NodeClickMode = NodeClickMode.Intercept,
        waitForNextNode: Boolean = false,
        advanceOnScrimClick: Boolean = true,
        isIntro: Boolean = false
    ): GuideEntry = GuideEntry(
        key = key,
        nodeClick = nodeClick,
        waitForNextNode = waitForNextNode,
        advanceOnScrimClick = advanceOnScrimClick,
        placement = GuidePlacement.Auto,
        showNextTip = true,
        content = { },
        isIntro = isIntro
    )

    private fun controller(vararg entries: GuideEntry): GuideController = GuideController(
        entries = entries.toList(),
        colors = GuideColors(),
        holeRadius = 8.dp,
        holeBorderWidth = 0.dp,
        shape = GuideDefaults.contentShape,
        backBehavior = GuideBack.Block
    )

    private fun arbiter(controller: GuideController, registry: GuideRegistry) =
        GuideEventArbiter(listOf(controller), registry)

    private fun registerAnchors(registry: GuideRegistry, key: GuideKey) {
        val node = GuideNode(key)
        registry.attach(node)
        registry.updateBounds(node, anchorRect)
    }

    private fun active(controller: GuideController): GuideState.Active =
        controller.state.value as GuideState.Active

    @Test
    fun `无激活流时触摸全放行且点击不推进`() {
        val registry = GuideRegistry()
        val flow = controller(entry(K1))
        val a = arbiter(flow, registry)

        assertTrue(a.passThroughAt(inside))
        assertEquals(TapAction.None, a.resolveTap(flow, contentRect, inside))
    }

    @Test
    fun `拦截步骤不放行且点击推进`() {
        val registry = GuideRegistry()
        registerAnchors(registry, K1)
        val flow = controller(entry(K1))
        val a = arbiter(flow, registry)
        assertTrue(flow.start())

        assertFalse(a.passThroughAt(inside))
        assertEquals(TapAction.Advance, a.resolveTap(flow, contentRect, inside))
        assertEquals(TapAction.Advance, a.resolveTap(flow, contentRect, outside))
    }

    @Test
    fun `放行步骤仅锚点区域放行`() {
        val registry = GuideRegistry()
        registerAnchors(registry, K1)
        val flow = controller(entry(K1, nodeClick = NodeClickMode.PassThrough))
        val a = arbiter(flow, registry)
        assertTrue(flow.start())

        assertTrue(a.passThroughAt(inside))
        assertFalse(a.passThroughAt(outside))
    }

    @Test
    fun `介绍步骤任意点击推进且不放行`() {
        val registry = GuideRegistry()
        val flow = controller(entry(object : GuideKey {}, isIntro = true))
        val a = arbiter(flow, registry)
        assertTrue(flow.start())

        assertFalse(a.passThroughAt(inside))
        assertEquals(TapAction.Advance, a.resolveTap(flow, contentRect, inside))
    }

    @Test
    fun `引导内容区域点击不推进`() {
        val registry = GuideRegistry()
        registerAnchors(registry, K1)
        val flow = controller(entry(K1))
        val a = arbiter(flow, registry)
        assertTrue(flow.start())

        assertEquals(
            TapAction.None,
            a.resolveTap(flow, contentRect, Offset(350f, 350f))
        )
    }

    @Test
    fun `空白区域按推进配置裁决`() {
        val registry = GuideRegistry()
        registerAnchors(registry, K1)
        val flow = controller(entry(K1, advanceOnScrimClick = false))
        val a = arbiter(flow, registry)
        assertTrue(flow.start())

        assertEquals(TapAction.None, a.resolveTap(flow, contentRect, outside))
    }

    @Test
    fun `门控生效时点击改为表达推进意图且锚点就绪可观察`() {
        val registry = GuideRegistry()
        registerAnchors(registry, K1)
        val flow = controller(
            entry(K1, nodeClick = NodeClickMode.PassThrough, waitForNextNode = true),
            entry(K2)
        )
        val a = arbiter(flow, registry)
        assertTrue(flow.start())

        val state = active(flow)
        assertTrue(a.gateActive(flow, state))
        assertEquals(TapAction.GateArm, a.resolveTap(flow, contentRect, inside))
        assertFalse(a.nextAnchorReady(flow, state))

        registerAnchors(registry, K2)
        assertTrue(a.nextAnchorReady(flow, state))
    }

    @Test
    fun `末步无下一步时门控不生效`() {
        val registry = GuideRegistry()
        registerAnchors(registry, K1)
        val flow = controller(entry(K1, nodeClick = NodeClickMode.PassThrough, waitForNextNode = true))
        val a = arbiter(flow, registry)
        assertTrue(flow.start())

        assertFalse(a.gateActive(flow, active(flow)))
        assertEquals(TapAction.Advance, a.resolveTap(flow, contentRect, inside))
    }
}
