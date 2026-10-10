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
import android.view.MotionEvent
import com.movtery.inputmap.keycodes.LwjglGlfwKeycode
import org.lwjgl.glfw.CallbackBridge

object LWJGLCharSender : CharacterSenderStrategy {
    override fun sendBackspace() {
        CallbackBridge.sendKeycode(LwjglGlfwKeycode.GLFW_KEY_BACKSPACE.toInt(), '\u0008', 0, 0, true)
        CallbackBridge.sendKeycode(LwjglGlfwKeycode.GLFW_KEY_BACKSPACE.toInt(), '\u0008', 0, 0, false)
    }

    override fun sendEnter() {
        CallbackBridge.sendKeyPress(LwjglGlfwKeycode.GLFW_KEY_ENTER.toInt())
    }

    override fun sendTab() {
        CallbackBridge.sendKeyPress(LwjglGlfwKeycode.GLFW_KEY_TAB.toInt())
    }

    override fun sendLeft() {
        CallbackBridge.sendKeyPress(LwjglGlfwKeycode.GLFW_KEY_LEFT.toInt())
    }

    override fun sendRight() {
        CallbackBridge.sendKeyPress(LwjglGlfwKeycode.GLFW_KEY_RIGHT.toInt())
    }

    override fun sendUp() {
        CallbackBridge.sendKeyPress(LwjglGlfwKeycode.GLFW_KEY_UP.toInt())
    }

    override fun sendDown() {
        CallbackBridge.sendKeyPress(LwjglGlfwKeycode.GLFW_KEY_DOWN.toInt())
    }

    override fun sendChar(character: Char) {
        CallbackBridge.sendChar(character, 0)
    }

    override fun sendOther(key: KeyEvent) {
        EfficientAndroidLWJGLKeycode.getIndexByKey(key.keyCode)
            .takeIf { it >= 0 }
            ?.let { index ->
                EfficientAndroidLWJGLKeycode.execKey(key, index)
            }
    }

    override fun sendCopy() {
        // Ignore
    }

    override fun sendCut() {
        // Ignore
    }

    override fun sendPaste() {
        // Ignore
    }

    override fun sendSelectAll() {
        // Ignore
    }

    override fun sendModifierShift(press: Boolean) {
        val keycode = LwjglGlfwKeycode.GLFW_KEY_LEFT_SHIFT.toInt()
        CallbackBridge.sendKeyPress(keycode, CallbackBridge.getCurrentMods(), press)
        CallbackBridge.setModifiers(keycode, press)
    }

    override fun sendModifierCtrl(press: Boolean) {
        val keycode = LwjglGlfwKeycode.GLFW_KEY_LEFT_CONTROL.toInt()
        CallbackBridge.sendKeyPress(keycode, CallbackBridge.getCurrentMods(), press)
        CallbackBridge.setModifiers(keycode, press)
    }

    /**
     * 获取 LWJGL 鼠标点击事件
     */
    fun getMouseButton(button: Int): Short? {
        return when (button) {
            MotionEvent.BUTTON_PRIMARY -> LwjglGlfwKeycode.GLFW_MOUSE_BUTTON_LEFT
            MotionEvent.BUTTON_SECONDARY, MotionEvent.BUTTON_STYLUS_SECONDARY -> LwjglGlfwKeycode.GLFW_MOUSE_BUTTON_RIGHT
            MotionEvent.BUTTON_TERTIARY -> LwjglGlfwKeycode.GLFW_MOUSE_BUTTON_MIDDLE
            // 处理鼠标侧键
            // 部分设备将物理鼠标侧键以 MotionEvent 的按钮状态上报
            // 而另一些设备会重映射为 KEYCODE_BACK/FORWARD
            MotionEvent.BUTTON_BACK -> LwjglGlfwKeycode.GLFW_MOUSE_BUTTON_4
            MotionEvent.BUTTON_FORWARD -> LwjglGlfwKeycode.GLFW_MOUSE_BUTTON_5
            else -> null
        }
    }
}