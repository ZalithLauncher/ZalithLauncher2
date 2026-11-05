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

package com.movtery.zalithlauncher.ui.control.mouse

import android.view.MotionEvent
import org.junit.Assert.assertEquals
import org.junit.Test

class CapturedMouseButtonsTest {
    @Test
    fun `down and up with zero action button preserve physical clicks`() {
        for (button in listOf(MotionEvent.BUTTON_PRIMARY, MotionEvent.BUTTON_SECONDARY, MotionEvent.BUTTON_TERTIARY)) {
            val events = mutableListOf<Pair<Int, Boolean>>()
            val buttons = CapturedMouseButtons { changed, pressed -> events += changed to pressed }

            buttons.onEvent(MotionEvent.ACTION_DOWN, 0, button)
            buttons.onEvent(MotionEvent.ACTION_UP, 0, 0)

            assertEquals(listOf(button to true, button to false), events)
        }
    }

    @Test
    fun `explicit button events and repeated snapshots do not duplicate clicks`() {
        val events = mutableListOf<Pair<Int, Boolean>>()
        val buttons = CapturedMouseButtons { button, pressed -> events += button to pressed }
        val primary = MotionEvent.BUTTON_PRIMARY

        buttons.onEvent(MotionEvent.ACTION_DOWN, 0, primary)
        buttons.onEvent(MotionEvent.ACTION_BUTTON_PRESS, primary, primary)
        buttons.onEvent(MotionEvent.ACTION_MOVE, 0, primary)
        buttons.onEvent(MotionEvent.ACTION_BUTTON_RELEASE, primary, 0)
        buttons.onEvent(MotionEvent.ACTION_UP, 0, 0)
        buttons.onEvent(MotionEvent.ACTION_HOVER_MOVE, 0, 0)
        buttons.onEvent(MotionEvent.ACTION_SCROLL, 0, 0)

        assertEquals(listOf(primary to true, primary to false), events)
    }

    @Test
    fun `secondary transitions preserve a held primary and emit individual buttons`() {
        val events = mutableListOf<Pair<Int, Boolean>>()
        val buttons = CapturedMouseButtons { button, pressed -> events += button to pressed }
        val primary = MotionEvent.BUTTON_PRIMARY
        val secondary = MotionEvent.BUTTON_SECONDARY

        buttons.onEvent(MotionEvent.ACTION_DOWN, 0, primary)
        buttons.onEvent(MotionEvent.ACTION_MOVE, 0, primary or secondary)
        buttons.onEvent(MotionEvent.ACTION_MOVE, 0, primary)
        buttons.onEvent(MotionEvent.ACTION_UP, 0, 0)

        assertEquals(listOf(primary to true, secondary to true, secondary to false, primary to false), events)
    }

    @Test
    fun `explicit action button supplies its transition`() {
        val events = mutableListOf<Pair<Int, Boolean>>()
        val buttons = CapturedMouseButtons { button, pressed -> events += button to pressed }
        val secondary = MotionEvent.BUTTON_SECONDARY

        buttons.onEvent(MotionEvent.ACTION_BUTTON_PRESS, secondary, 0)
        buttons.onEvent(MotionEvent.ACTION_BUTTON_PRESS, secondary, 0)
        buttons.onEvent(MotionEvent.ACTION_BUTTON_RELEASE, secondary, secondary)
        buttons.onEvent(MotionEvent.ACTION_BUTTON_RELEASE, secondary, secondary)

        assertEquals(listOf(secondary to true, secondary to false), events)
    }

    @Test
    fun `cancellation and disposal reset release held buttons once and allow new input`() {
        val events = mutableListOf<Pair<Int, Boolean>>()
        val buttons = CapturedMouseButtons { button, pressed -> events += button to pressed }
        val primary = MotionEvent.BUTTON_PRIMARY
        val secondary = MotionEvent.BUTTON_SECONDARY

        buttons.onEvent(MotionEvent.ACTION_DOWN, 0, primary or secondary)
        buttons.onEvent(MotionEvent.ACTION_CANCEL, 0, primary or secondary)
        buttons.reset()
        buttons.onEvent(MotionEvent.ACTION_DOWN, 0, primary)
        buttons.reset()
        buttons.reset()
        buttons.onEvent(MotionEvent.ACTION_DOWN, 0, primary)
        buttons.onEvent(MotionEvent.ACTION_UP, 0, 0)

        assertEquals(
            listOf(primary to true, secondary to true, primary to false, secondary to false,
                primary to true, primary to false, primary to true, primary to false),
            events
        )
    }
}
