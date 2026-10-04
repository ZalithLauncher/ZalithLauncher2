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

package com.movtery.zalithlauncher.ui.control.event

import java.util.concurrent.ConcurrentHashMap

/**
 * 处理启动器按键事件标识
 */
@Deprecated("怀疑 KeyEventHandler 会导致按键事件混乱，导致游戏乱操作，暂时移除 #894")
class KeyEventHandler(
    private val handle: (key: String, pressed: Boolean) -> Unit
) {
    /**
     * 当前按键总共按住的数量（也许有同一个按键同时按下的情况）
     */
    private val keyEvents = ConcurrentHashMap<String, Int>()

    /**
     * 按下按键
     */
    fun pressKey(key: String) {
        var shouldPress = false
        keyEvents.compute(key) { _, old ->
            val oldCount = old ?: 0
            shouldPress = oldCount == 0
            oldCount + 1
        }
        if (shouldPress) {
            handle(key, true)
        }
    }

    fun releaseKey(key: String) {
        var shouldRelease = false
        keyEvents.compute(key) { _, old ->
            when {
                old == null || old <= 0 -> {
                    shouldRelease = false
                    null
                }
                old == 1 -> {
                    shouldRelease = true
                    null
                }
                else -> {
                    shouldRelease = false
                    old - 1
                }
            }
        }
        if (shouldRelease) {
            handle(key, false)
        }
    }

    fun clearEvent() {
        val allKeys = keyEvents.keys.toSet()
        keyEvents.clear()
        allKeys.forEach {
            handle(it, false)
        }
    }
}