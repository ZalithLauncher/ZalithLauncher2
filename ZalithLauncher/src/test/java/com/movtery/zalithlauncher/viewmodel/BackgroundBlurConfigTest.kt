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

import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.yield
import org.junit.Assert.assertEquals
import org.junit.Test

class BackgroundBlurConfigTest {
    @Test
    fun `slider flow regenerates static requests without media or layout changes`() = runBlocking {
        val blur = MutableStateFlow(1)
        val opacity = MutableStateFlow(40)
        val media = MutableStateFlow(
            BackgroundMediaConfig(
                valid = true,
                isImage = true,
                isAnimatedImage = false,
                generation = false,
                targetWidth = 1080,
                targetHeight = 2400
            )
        )
        val configs = mutableListOf<BackgroundBlurConfig>()
        val collector = launch(start = CoroutineStart.UNDISPATCHED) {
            combineBackgroundBlurConfig(blur, opacity, media).take(3).toList(configs)
        }
        yield()

        blur.value = 7
        yield()
        blur.value = 19
        yield()
        collector.join()

        assertEquals(listOf(1, 7, 19), configs.map { it.blur })
        assertEquals(listOf(1080, 1080, 1080), configs.map { it.targetWidth })
        assertEquals(listOf(false, false, false), configs.map { it.generation })
    }
}
