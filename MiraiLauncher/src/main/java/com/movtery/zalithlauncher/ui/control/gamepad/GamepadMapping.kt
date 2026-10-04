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

package com.movtery.zalithlauncher.ui.control.gamepad

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * 手柄与键盘映射
 * @param key 手柄键值
 * @param targetsInGame 目标键盘映射（游戏内）
 * @param targetsInMenu 目标键盘映射（菜单内）
 */
@Parcelize
data class GamepadMapping(
    val key: Int,
    val dpadDirection: DpadDirection?,
    val targetsInGame: Set<String> = emptySet(),
    val targetsInMenu: Set<String> = emptySet()
): Parcelable