/*
 * Zalith Launcher 2
 * Copyright (C) 2025 MovTery <movtery228@qq.com> and contributors
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
 * along with this program. If not, see <https://www.gnu.org/licenses/gpl-3.0.txt>.
 */

package com.movtery.zalithlauncher.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type

/**
 * System for managing keyboard shortcuts
 */
data class KeyboardShortcut(
    val key: Key,
    val ctrlPressed: Boolean = false,
    val shiftPressed: Boolean = false,
    val altPressed: Boolean = false,
    val action: () -> Unit
)

class KeyboardShortcutManager {
    private val shortcuts = mutableListOf<KeyboardShortcut>()

    fun register(shortcut: KeyboardShortcut) {
        shortcuts.add(shortcut)
    }

    fun unregister(shortcut: KeyboardShortcut) {
        shortcuts.remove(shortcut)
    }

    fun handleKeyEvent(event: KeyEvent): Boolean {
        if (event.type != KeyEventType.KeyDown) return false

        val matching = shortcuts.find { shortcut ->
            event.key == shortcut.key &&
            event.isCtrlPressed == shortcut.ctrlPressed &&
            event.isShiftPressed == shortcut.shiftPressed &&
            event.isAltPressed == shortcut.altPressed
        }

        return if (matching != null) {
            matching.action()
            true
        } else {
            false
        }
    }
}

@Composable
fun rememberKeyboardShortcutManager(): KeyboardShortcutManager {
    return remember { KeyboardShortcutManager() }
}
