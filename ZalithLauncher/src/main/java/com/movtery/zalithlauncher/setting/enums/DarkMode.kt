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

package com.movtery.zalithlauncher.setting.enums

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.setting.AllSettings

/**
 * 启动器UI深色主题
 */
enum class DarkMode(val textRes: Int) {
    Enable(R.string.generic_enable),
    Disable(R.string.generic_disable),

    /**
     * 跟随系统变化
     */
    FollowSystem(R.string.generic_follow_system)
}

/**
 * 当前启动器是否处于深色主题模式中，和 [isSystemInDarkTheme] 函数使用方法一致，
 * 但引入了启动器的设置系统的干预
 */
@Composable
fun isLauncherInDarkTheme(): Boolean {
    val value = AllSettings.launcherDarkMode.state
    return key(value) {
        when (value) {
            DarkMode.Enable -> true
            DarkMode.Disable -> false
            DarkMode.FollowSystem -> isSystemInDarkTheme()
        }
    }
}