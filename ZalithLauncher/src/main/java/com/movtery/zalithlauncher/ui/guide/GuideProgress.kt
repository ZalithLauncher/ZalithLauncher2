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

package com.movtery.zalithlauncher.ui.guide

import com.movtery.zalithlauncher.BuildKeys
import com.tencent.mmkv.MMKV

/**
 * 引导进度存储
 */
object GuideProgress {
    private val mmkv = MMKV.mmkvWithID(
        "${BuildKeys.LAUNCHER_IDENTIFIER}_guide",
        MMKV.MULTI_PROCESS_MODE
    )

    /**
     * 该组引导的持久化键
     */
    fun keyOf(group: GuideKeys.Keys) = when (group) {
        GuideKeys.Main -> "started_Main"
        GuideKeys.Editor -> "started_Editor"
    }

    /**
     * @return 该组引导是否已经播放过
     */
    fun isPlayed(group: GuideKeys.Keys): Boolean =
        mmkv.getBoolean(keyOf(group), false)

    /**
     * 标记该组引导已经播放
     */
    fun markPlayed(group: GuideKeys.Keys) {
        mmkv.putBoolean(keyOf(group), true).apply()
    }
}
