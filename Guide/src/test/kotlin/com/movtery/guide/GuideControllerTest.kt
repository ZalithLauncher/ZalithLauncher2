package com.movtery.guide

import androidx.compose.ui.Alignment
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GuideControllerTest {
    private object K1 : GuideKey
    private object K2 : GuideKey
    private object K3 : GuideKey

    @Before
    fun resetHub() {
        GuideHub.reset()
    }

    private fun controller(vararg keys: GuideKey): GuideController =
        GuideController(
            entries = keys.map { key ->
                GuideEntry(
                    key = key,
                    nodeClick = NodeClickMode.Intercept,
                    advanceOnScrimClick = true,
                    placement = GuidePlacement.Auto,
                    showNextTip = true,
                    content = { }
                )
            },
            colors = GuideColors(),
            holeRadius = 8.dp,
            holeBorderWidth = 0.dp,
            backBehavior = GuideBack.Block
        )

    private val anchors = listOf(Rect(0f, 0f, 100f, 100f))

    @Test
    fun `空引导流 start 失败`() {
        assertFalse(controller().start())
    }

    @Test
    fun `状态流转 start next 到结束`() {
        val flow = controller(K1, K2, K3)

        assertTrue(flow.start())
        assertEquals(GuideState.Active(0, flow.entries[0], emptyList()), flow.state.value)

        flow.next()
        assertEquals(GuideState.Active(1, flow.entries[1], emptyList()), flow.state.value)

        flow.next()
        assertEquals(GuideState.Active(2, flow.entries[2], emptyList()), flow.state.value)

        flow.next()
        assertEquals(GuideState.Finished, flow.state.value)
    }

    @Test
    fun `finish 直接结束且可重新启动`() {
        val flow = controller(K1, K2)

        assertTrue(flow.start())
        flow.finish()
        assertEquals(GuideState.Finished, flow.state.value)

        assertTrue(flow.start())
        assertEquals(GuideState.Active(0, flow.entries[0], emptyList()), flow.state.value)
    }

    @Test
    fun `并发拒绝 结束后释放`() {
        val first = controller(K1, K2)
        val second = controller(K1)

        assertTrue(first.start())
        assertFalse(second.start())

        first.finish()
        assertTrue(second.start())
        assertEquals(GuideState.Active(0, second.entries[0], emptyList()), second.state.value)
        second.finish()
    }

    @Test
    fun `next 越过末步时自动结束并释放激活位`() {
        val first = controller(K1)
        val second = controller(K1)

        assertTrue(first.start())
        first.next()
        assertEquals(GuideState.Finished, first.state.value)
        assertTrue(second.start())
        second.finish()
    }

    @Test
    fun `锚点回写仅限当前步骤且值不变时不重复发布`() {
        val flow = controller(K1, K2)
        assertTrue(flow.start())

        flow.updateAnchors(index = 1, rects = anchors)
        assertEquals(emptyList<Rect>(), (flow.state.value as GuideState.Active).anchors)

        flow.updateAnchors(index = 0, rects = anchors)
        assertEquals(anchors, (flow.state.value as GuideState.Active).anchors)

        val version = flow.state.value
        flow.updateAnchors(index = 0, rects = anchors)
        assertEquals(version, flow.state.value)

        flow.next()
        assertEquals(emptyList<Rect>(), (flow.state.value as GuideState.Active).anchors)
    }

    @Test
    fun `非激活状态的 next 与 finish 无副作用`() {
        val flow = controller(K1)
        flow.next()
        assertEquals(GuideState.Idle, flow.state.value)
        flow.finish()
        assertEquals(GuideState.Idle, flow.state.value)
        assertFalse(flow.state.value is GuideState.Active)
    }

    @Test
    fun `Active 的 isReady 由锚点决定`() {
        val flow = controller(K1)
        assertTrue(flow.start())
        val active = flow.state.value as GuideState.Active
        assertFalse(active.isReady)
        flow.updateAnchors(0, anchors)
        assertTrue((flow.state.value as GuideState.Active).isReady)
    }

    @Test
    fun `介绍步骤激活即就绪且整流可走完`() {
        val introEntry = GuideEntry(
            key = object : GuideKey {},
            nodeClick = NodeClickMode.Intercept,
            advanceOnScrimClick = true,
            placement = GuidePlacement.Fixed(Alignment.Center),
            showNextTip = true,
            content = { },
            isIntro = true
        )
        val flow = GuideController(
            entries = listOf(
                introEntry,
                GuideEntry(
                    key = K1,
                    nodeClick = NodeClickMode.Intercept,
                    advanceOnScrimClick = true,
                    placement = GuidePlacement.Auto,
                    showNextTip = true,
                    content = { }
                )
            ),
            colors = GuideColors(),
            holeRadius = 8.dp,
            holeBorderWidth = 0.dp,
            backBehavior = GuideBack.Block
        )

        assertTrue(flow.start())
        val active = flow.state.value as GuideState.Active
        assertTrue(active.entry.isIntro)
        assertTrue(active.isReady)

        flow.next()
        assertFalse((flow.state.value as GuideState.Active).isReady)

        flow.next()
        assertEquals(GuideState.Finished, flow.state.value)
    }
}
