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

package com.movtery.zalithlauncher.viewmodel

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.min

class BackgroundBlurPolicyTest {
    @Test
    fun `static image uses cache and never starts realtime capture`() {
        val decision = resolveBackgroundBlur(BackgroundContentKind.STATIC_IMAGE, blur = 20, opacity = 40)

        assertTrue(decision.enabled)
        assertTrue(decision.cacheStaticImage)
        assertFalse(decision.captureFrames)
    }

    @Test
    fun `gif and video use realtime capture`() {
        for (kind in arrayOf(BackgroundContentKind.ANIMATED_IMAGE, BackgroundContentKind.VIDEO)) {
            val decision = resolveBackgroundBlur(kind, blur = 20, opacity = 40)
            assertTrue(decision.enabled)
            assertFalse(decision.cacheStaticImage)
            assertTrue(decision.captureFrames)
        }
    }

    @Test
    fun `disabled blur never decodes or captures`() {
        for (kind in BackgroundContentKind.entries) {
            for (config in arrayOf(0 to 40, 20 to 100)) {
                val decision = resolveBackgroundBlur(kind, config.first, config.second)
                if (kind == BackgroundContentKind.NONE || config.first == 0 || config.second == 100) {
                    assertFalse(decision.enabled)
                    assertFalse(decision.cacheStaticImage)
                    assertFalse(decision.captureFrames)
                }
            }
        }
    }

    @Test
    fun `small radius preserves video detail instead of forcing 256px sampling`() {
        val plan = calculateBlurSamplingPlan(
            sourceWidth = 1080,
            sourceHeight = 2400,
            blurRadiusPx = 12f,
            maxDimension = DYNAMIC_GPU_BLUR_MAX_DIMENSION,
            maxSampledRadius = GPU_MAX_SAMPLED_BLUR_RADIUS
        )

        assertEquals(461, plan.width)
        assertEquals(1024, plan.height)
        assertTrue(plan.width > 256 && plan.height > 256)
        assertEquals(12f, plan.blurRadiusPx / min(plan.scaleX, plan.scaleY), 0.01f)
    }

    @Test
    fun `large radius reduces sampling instead of running a huge blur kernel`() {
        val plan = calculateBlurSamplingPlan(
            sourceWidth = 1200,
            sourceHeight = 1200,
            blurRadiusPx = 240f,
            maxDimension = DYNAMIC_CPU_BLUR_MAX_DIMENSION,
            maxSampledRadius = GPU_MAX_SAMPLED_BLUR_RADIUS
        )

        assertEquals(100, plan.width)
        assertEquals(100, plan.height)
        assertEquals(GPU_MAX_SAMPLED_BLUR_RADIUS, plan.blurRadiusPx, 0f)
        assertEquals(240f, plan.blurRadiusPx / min(plan.scaleX, plan.scaleY), 0.01f)
    }

    @Test
    fun `small sources are never upscaled for processing`() {
        val plan = calculateBlurSamplingPlan(120, 80, 4f, 256, 24f)

        assertEquals(120, plan.width)
        assertEquals(80, plan.height)
        assertEquals(1f, plan.scaleX, 0f)
        assertEquals(1f, plan.scaleY, 0f)
    }

    @Test
    fun `slider radius changes next sampling plan immediately`() {
        val first = calculateBlurSamplingPlan(1080, 2400, 12f, 256, 24f)
        val second = calculateBlurSamplingPlan(1080, 2400, 24f, 256, 24f)

        assertNotEquals(first.blurRadiusPx, second.blurRadiusPx)
        assertEquals(12f, first.blurRadiusPx / min(first.scaleX, first.scaleY), 0.01f)
        assertEquals(24f, second.blurRadiusPx / min(second.scaleX, second.scaleY), 0.01f)
    }

    @Test
    fun `center crop matches portrait and square destinations`() {
        val portrait = calculateCenterCrop(4000, 3000, 1080, 2400)
        assertEquals(1350, portrait.width)
        assertEquals(3000, portrait.height)
        assertEquals(1325, portrait.left)
        assertEquals(0, portrait.top)

        val square = calculateCenterCrop(2000, 1000, 1000, 1000)
        assertEquals(1000, square.width)
        assertEquals(1000, square.height)
        assertEquals(500, square.left)
        assertEquals(0, square.top)
    }
}
