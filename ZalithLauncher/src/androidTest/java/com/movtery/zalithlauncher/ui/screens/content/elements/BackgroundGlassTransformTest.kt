/*
 * Zalith Launcher 2
 * Copyright (C) 2025-2026 MovTery <movtery228@qq.com> and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/gpl-3.0.txt>.
 */

package com.movtery.zalithlauncher.ui.screens.content.elements

import android.graphics.Matrix
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Matrix as ComposeMatrix
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BackgroundGlassTransformTest {
    @Test
    fun versionItemScaleKeepsBackgroundFixedInScreenSpace() {
        for (scale in floatArrayOf(0.95f, 0.975f, 1f)) {
            //与 VersionItemLayout 一致：外层 graphicsLayer 围绕卡片中心缩放。
            val compose = ComposeMatrix()
            compose.translate(146f + 180f, 92f + 48f)
            compose.scale(scale, scale)
            compose.translate(-180f, -48f)

            val localToScreen = Matrix()
            localToScreen.setFromComposeMatrix(compose, FloatArray(9))
            val expected = compose.map(Offset(37f, 58f))
            val localPoint = floatArrayOf(37f, 58f)
            localToScreen.mapPoints(localPoint)
            assertEquals("scale=$scale", expected.x, localPoint[0], 0.02f)
            assertEquals("scale=$scale", expected.y, localPoint[1], 0.02f)

            //背景按 screen -> local 的逆矩阵预补偿，外层合成后必须回到同一屏幕点。
            val screenToLocal = Matrix()
            assertTrue(localToScreen.invert(screenToLocal))
            val backgroundPoint = floatArrayOf(412f, 736f)
            screenToLocal.mapPoints(backgroundPoint)
            localToScreen.mapPoints(backgroundPoint)
            assertEquals("scale=$scale", 412f, backgroundPoint[0], 0.02f)
            assertEquals("scale=$scale", 736f, backgroundPoint[1], 0.02f)
        }
    }
}
