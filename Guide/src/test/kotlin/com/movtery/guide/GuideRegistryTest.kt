package com.movtery.guide

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GuideRegistryTest {
    private object K1 : GuideKey

    @Test
    fun `节点镂空样式更新随版本递增，值未变时不递增`() {
        val registry = GuideRegistry()
        val node = GuideNode(K1)
        registry.attach(node)
        val before = registry.version

        registry.updateStyle(node, 12.dp, 2.dp, Color.Red)
        assertEquals(12.dp, node.holeRadius)
        assertEquals(2.dp, node.holeBorderWidth)
        assertEquals(Color.Red, node.holeBorderColor)
        assertTrue(registry.version > before)

        val stable = registry.version
        registry.updateStyle(node, 12.dp, 2.dp, Color.Red)
        assertEquals(stable, registry.version)
    }

    @Test
    fun `锚点节点随挂载与卸载增删`() {
        val registry = GuideRegistry()
        val node = GuideNode(K1)
        registry.attach(node)
        assertEquals(listOf(node), registry.nodesFor(K1))
        assertEquals(listOf(node.bounds), registry.rectsFor(K1))

        registry.detach(node)
        assertTrue(registry.nodesFor(K1).isEmpty())
        assertTrue(registry.rectsFor(K1).isEmpty())
    }
}
