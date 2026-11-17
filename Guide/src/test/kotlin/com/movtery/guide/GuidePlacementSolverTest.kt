package com.movtery.guide

import androidx.compose.ui.Alignment
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GuidePlacementSolverTest {
    private val container = IntSize(1080, 1920)
    private val content = IntSize(400, 200)
    private val gap = 12f
    private val padding = 16f

    private fun PlacementResult.rect(contentSize: IntSize = content): Rect =
        Rect(
            offset.x.toFloat(),
            offset.y.toFloat(),
            (offset.x + contentSize.width).toFloat(),
            (offset.y + contentSize.height).toFloat()
        )

    private fun solve(
        anchors: List<Rect>,
        placement: GuidePlacement = GuidePlacement.Auto,
        contentSize: IntSize = content,
        layoutDirection: LayoutDirection = LayoutDirection.Ltr
    ): PlacementResult = solvePlacement(
        placement = placement,
        anchors = anchors,
        contentSize = contentSize,
        containerSize = container,
        layoutDirection = layoutDirection,
        gapPx = gap,
        paddingPx = padding
    )

    @Test
    fun `居中锚点放置在贴邻且不遮挡的一侧`() {
        val anchor = Rect(440f, 860f, 640f, 1060f)
        val result = solve(listOf(anchor))
        val rect = result.rect()

        assertFalse(rect.overlaps(anchor))
        assertTrue(result.side == GuideSide.Below || result.side == GuideSide.Above)
        assertTrue(rect.left >= padding)
        assertTrue(rect.right <= container.width - padding)
        assertTrue(rect.top >= padding)
        assertTrue(rect.bottom <= container.height - padding)
    }

    @Test
    fun `底部锚点回退到上方`() {
        val anchor = Rect(340f, 1800f, 740f, 1880f)
        val result = solve(listOf(anchor))
        val rect = result.rect()

        assertEquals(GuideSide.Above, result.side)
        assertFalse(rect.overlaps(anchor))
    }

    @Test
    fun `可行解中取距锚点中心最近者`() {
        val anchor = Rect(0f, 0f, 200f, 100f)
        val result = solve(listOf(anchor))
        val rect = result.rect()
        val anchorCenterX = anchor.left + anchor.width / 2f
        val anchorCenterY = anchor.top + anchor.height / 2f
        val distance = hypot(rect.center.x - anchorCenterX, rect.center.y - anchorCenterY)

        assertFalse(rect.overlaps(anchor))
        // 可行解的距离应不劣于任一方向贴边候选
        val sides = listOf(GuideSide.Below, GuideSide.Above, GuideSide.Start, GuideSide.End)
        sides.forEach { side ->
            val candidateRect = candidateRect(anchor, side)
            val overlaps = candidateRect.overlaps(anchor)
            if (!overlaps && insideScreen(candidateRect)) {
                val candidateDistance = hypot(
                    candidateRect.center.x - anchorCenterX,
                    candidateRect.center.y - anchorCenterY
                )
                assertTrue(distance <= candidateDistance + 0.5f)
            }
        }
    }

    @Test
    fun `多锚点以包围盒中心为参照且不遮挡任一锚点`() {
        val anchors = listOf(
            Rect(0f, 0f, 200f, 100f),
            Rect(880f, 1820f, 1080f, 1920f)
        )
        val result = solve(anchors)
        val rect = result.rect()

        anchors.forEach { assertFalse(rect.overlaps(it)) }
        // 包围盒中心 (540, 960)，内容应贴在包围盒内部空隙（Start 侧）
        assertEquals(GuideSide.Start, result.side)
    }

    @Test
    fun `内容大于屏幕时兜底到留白内且不崩溃`() {
        val anchor = Rect(0f, 0f, 1080f, 1920f)
        val result = solve(listOf(anchor), contentSize = IntSize(1200, 2000))

        // 全部候选不可行，按交叠面积→出屏量兜底取第一个最不坏候选
        assertEquals(IntOffset(16, 16), result.offset)
        assertEquals(GuideSide.Below, result.side)
    }

    @Test
    fun `方向偏好可行时优先采用`() {
        val anchor = Rect(440f, 860f, 640f, 1060f)
        val result = solve(listOf(anchor), placement = PreferSide(GuideSide.End))

        assertEquals(GuideSide.End, result.side)
        val rect = result.rect()
        assertTrue(rect.left >= anchor.right + gap - 0.5f)
    }

    @Test
    fun `Fixed 完全遵循对齐与偏移`() {
        val anchor = Rect(440f, 860f, 640f, 1060f)
        val result = solve(
            listOf(anchor),
            placement = GuidePlacement.Fixed(Alignment.TopStart, IntOffset(10, 20))
        )

        assertEquals(IntOffset(10, 20), result.offset)
        assertNull(result.side)
    }

    @Test
    fun `RTL 下 Start 与 End 镜像`() {
        val anchor = Rect(440f, 860f, 640f, 1060f)
        val start = solve(listOf(anchor), placement = PreferSide(GuideSide.Start), layoutDirection = LayoutDirection.Rtl)
        val end = solve(listOf(anchor), placement = PreferSide(GuideSide.End), layoutDirection = LayoutDirection.Rtl)

        // RTL 中 Start 在锚点右侧，End 在锚点左侧
        assertTrue(start.rect().left >= anchor.right + gap - 0.5f)
        assertTrue(end.rect().right <= anchor.left - gap + 0.5f)
    }

    @Test
    fun `单锚点节点推荐映射为方向偏好`() {
        val result = resolvePlacement(GuidePlacement.Auto, GuideSide.Below, listOf(Rect(0f, 0f, 100f, 100f)))

        assertEquals(PreferSide(GuideSide.Below), result)
    }

    @Test
    fun `多锚点忽略节点推荐`() {
        val anchors = listOf(
            Rect(0f, 0f, 100f, 100f),
            Rect(200f, 0f, 300f, 100f)
        )

        assertEquals(GuidePlacement.Auto, resolvePlacement(GuidePlacement.Auto, GuideSide.Below, anchors))
    }

    @Test
    fun `显式摆放策略优先于节点推荐`() {
        val fixed = GuidePlacement.Fixed(Alignment.Center)

        assertEquals(fixed, resolvePlacement(fixed, GuideSide.Below, listOf(Rect(0f, 0f, 100f, 100f))))
        assertEquals(
            GuidePlacement.Auto,
            resolvePlacement(GuidePlacement.Auto, null, listOf(Rect(0f, 0f, 100f, 100f)))
        )
    }

    @Test
    fun `空锚点视为不可见`() {
        assertFalse(anchorsVisibleIn(emptyList(), container))
    }

    @Test
    fun `完全入视口才可见`() {
        val visible = listOf(Rect(100f, 100f, 300f, 200f))
        assertTrue(anchorsVisibleIn(visible, container))

        // 部分出屏
        assertFalse(anchorsVisibleIn(listOf(Rect(-10f, 100f, 300f, 200f)), container))
        // 完全出屏
        assertFalse(anchorsVisibleIn(listOf(Rect(0f, container.height + 10f, 300f, container.height + 100f)), container))
        // 多锚点时要求全部可见
        assertFalse(anchorsVisibleIn(visible + listOf(Rect(0f, -50f, 100f, 0f)), container))
    }

    @Test
    fun `包围盒覆盖多锚点`() {
        val union = listOf(
            Rect(0f, 0f, 100f, 50f),
            Rect(300f, 200f, 500f, 300f)
        ).boundsUnion()

        assertEquals(Rect(0f, 0f, 500f, 300f), union)
    }

    private fun candidateRect(anchor: Rect, side: GuideSide): Rect {
        val centerX = anchor.left + (anchor.width - content.width) / 2f
        val centerY = anchor.top + (anchor.height - content.height) / 2f
        return when (side) {
            GuideSide.Above -> Rect(centerX, anchor.top - gap - content.height, centerX + content.width, anchor.top - gap)
            GuideSide.Below -> Rect(centerX, anchor.bottom + gap, centerX + content.width, anchor.bottom + gap + content.height)
            GuideSide.Start -> Rect(anchor.left - gap - content.width, centerY, anchor.left - gap, centerY + content.height)
            GuideSide.End -> Rect(anchor.right + gap, centerY, anchor.right + gap + content.width, centerY + content.height)
        }
    }

    private fun insideScreen(rect: Rect): Boolean =
        rect.left >= 0f && rect.top >= 0f && rect.right <= container.width && rect.bottom <= container.height

    private fun hypot(x: Float, y: Float): Float = kotlin.math.hypot(x, y)
}
