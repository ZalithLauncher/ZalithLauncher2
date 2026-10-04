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

package com.movtery.zalithlauncher.ui.theme.festivals

import android.view.MotionEvent

/**
 * 点击彩蛋的输入观察器
 */
object FestivalTapObserver {
    private val listeners = ArrayList<(Float, Float) -> Unit>()

    /**
     * 订阅按下事件
     */
    fun subscribe(onTap: (x: Float, y: Float) -> Unit): () -> Unit {
        listeners.add(onTap)
        return { listeners.remove(onTap) }
    }

    fun observe(event: MotionEvent) {
        if (event.actionMasked != MotionEvent.ACTION_DOWN) return
        val x = event.x
        val y = event.y
        listeners.toList().forEach { listener ->
            listener(x, y)
        }
    }
}
