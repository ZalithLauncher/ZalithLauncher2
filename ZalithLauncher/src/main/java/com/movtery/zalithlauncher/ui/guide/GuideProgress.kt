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

    private fun legacyKeyOf(group: GuideKeys.Keys) = when (group) {
        GuideKeys.Main -> "started_Main"
        GuideKeys.Editor -> "started_Editor"
        else -> null
    }

    /**
     * @return 该组引导已播放版本的持久化键
     */
    fun keyOf(group: GuideKeys.Keys) = when (group) {
        GuideKeys.Main -> "version_Main"
        GuideKeys.Editor -> "version_Editor"
        GuideKeys.JoystickEvents -> "version_JoystickEvents"
    }

    /**
     * @return 该组引导已播放的版本，0 表示从未播放
     */
    fun playedVersion(group: GuideKeys.Keys): Int {
        val version = mmkv.getInt(keyOf(group), 0)
        if (version != 0) return version

        // 兼容旧版布尔存储
        val legacyKey = legacyKeyOf(group)
        return if (legacyKey != null && mmkv.getBoolean(legacyKey, false)) {
            1 // 旧版已播放时，视为版本号 1
        } else {
            0
        }
    }

    /**
     * @return 该组引导是否已播放过当前版本
     */
    fun isPlayed(group: GuideKeys.Keys): Boolean {
        return playedVersion(group) >= group.version
    }

    /**
     * 标记该组引导已经播放到指定版本
     */
    fun markPlayed(group: GuideKeys.Keys, version: Int) {
        mmkv.putInt(keyOf(group), version).apply()
    }
}
