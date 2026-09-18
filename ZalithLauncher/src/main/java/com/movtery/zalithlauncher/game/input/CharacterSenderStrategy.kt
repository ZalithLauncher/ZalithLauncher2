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
 * along with this program.  If not, see <https://www.gnu.org/licenses/gpl-3.0.txt>.
 */

package com.movtery.zalithlauncher.game.input

import android.view.KeyEvent

/**
 * Simple interface for sending chars through whatever bridge will be necessary
 * [Modified from PojavLauncher](https://github.com/PojavLauncherTeam/PojavLauncher/blob/v3_openjdk/app_pojavlauncher/src/main/java/net/kdt/pojavlaunch/customcontrols/keyboard/CharacterSenderStrategy.java)
 */
interface CharacterSenderStrategy {
    /** Called when there is a character to delete, may be called multiple times in a row  */
    fun sendBackspace()

    /** Called when we want to send enter specifically  */
    fun sendEnter()

    /** Called when the Tab key is pressed */
    fun sendTab()

    /** Called when the left arrow key is pressed */
    fun sendLeft()

    /** Called when the right arrow key is pressed */
    fun sendRight()

    /** Called when the up arrow key is pressed */
    fun sendUp()

    /** Called when the down arrow key is pressed */
    fun sendDown()

    /** Called when there is a character to send, may be called multiple times in a row  */
    fun sendChar(character: Char)

    /**
     * Called when a non-character and non-arrow key event needs to be sent
     * @param key the KeyEvent representing the pressed key
     */
    fun sendOther(key: KeyEvent)

    /**
     * Called when the "Copy" action needs to be triggered, typically copies the selected content to the clipboard
     */
    fun sendCopy()

    /**
     * Called when the "Cut" action needs to be triggered, typically removes the selected content and copies it to the clipboard
     */
    fun sendCut()

    /**
     * Called when the "Paste" action needs to be triggered, typically pastes the content from the clipboard
     */
    fun sendPaste()

    /**
     * Called when the "Select All" action needs to be triggered, typically selects all available text or content
     */
    fun sendSelectAll()

    /**
     * Called when the Shift modifier key needs to be sent or toggled
     * Typically used to modify the behavior of subsequent key or character inputs
     */
    fun sendModifierShift(press: Boolean)

    /**
     * Called when the Control (Ctrl) modifier key needs to be sent or toggled
     * Typically used for shortcut combinations such as copy, paste, or other control commands
     */
    fun sendModifierCtrl(press: Boolean)
}