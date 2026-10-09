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

package com.movtery.zalithlauncher.game.sdl

import android.view.KeyEvent
import com.movtery.zalithlauncher.game.input.EfficientAndroidLWJGLKeycode
import com.movtery.zalithlauncher.utils.logging.Logger
import kotlinx.coroutines.delay
import org.libsdl.app.SDLActivity
import kotlin.time.Duration.Companion.milliseconds

object SdlTextSender {
    @JvmStatic
    fun sendChar(character: Char) {
        if (!SdlBridge.sdlEnabled) return
        SDLActivity.onNativeTextInput(character.toString())
    }

    @JvmStatic
    fun sendEnter() {
        if (!SdlBridge.sdlEnabled) return
        SDLActivity.onNativeKeyDown(KeyEvent.KEYCODE_ENTER)
        SDLActivity.onNativeKeyUp(KeyEvent.KEYCODE_ENTER)
    }

    @JvmStatic
    fun sendKey(lwjglGlfwKeycode: Int) {
        if (!SdlBridge.sdlEnabled) return
        val keyCode = EfficientAndroidLWJGLKeycode.getSdlAndroidKeycode(lwjglGlfwKeycode)
        if (keyCode == KeyEvent.KEYCODE_UNKNOWN) return
        SDLActivity.onNativeKeyDown(keyCode)
        SDLActivity.onNativeKeyUp(keyCode)
    }

    /**
     * 等待游戏侧激活 SDL 文本输入通道（聊天栏打开后游戏异步激活），
     * 通道未激活时经文本通道提交的内容会被 SDL 丢弃
     */
    suspend fun awaitTextInputActive(): Boolean {
        var waited = 0L
        while (!SdlBridge.isNativeTextInputActive()) {
            if (waited >= CHANNEL_WAIT_TIMEOUT_MS) {
                Logger.warning(TAG, "SDL text input channel not active after ${waited}ms, text dropped")
                return false
            }
            delay(CHANNEL_POLL_INTERVAL_MS.milliseconds)
            waited += CHANNEL_POLL_INTERVAL_MS
        }
        return true
    }

    private const val TAG = "SdlTextSender"
    private const val CHANNEL_WAIT_TIMEOUT_MS = 1000L
    private const val CHANNEL_POLL_INTERVAL_MS = 16L
}