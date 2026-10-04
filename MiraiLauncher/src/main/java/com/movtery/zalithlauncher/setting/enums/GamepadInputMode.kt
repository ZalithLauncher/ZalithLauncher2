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

import com.movtery.zalithlauncher.R

/**
 * 手柄输入模式
 */
enum class GamepadInputMode(val titleRes: Int, val summaryRes: Int) {
    /**
     * 映射模式：手柄按键/摇杆映射为虚拟键盘鼠标事件（旧版本游戏、无手柄支持的游戏）
     */
    Mapped(
        titleRes = R.string.settings_gamepad_input_mode_mapped,
        summaryRes = R.string.settings_gamepad_input_mode_mapped_summary
    ),

    /**
     * SDL 直通模式：手柄输入原样交给 SDL/GLFW gamepad API（Minecraft 26.3+ 原生手柄）
     */
    SdlDirect(
        titleRes = R.string.settings_gamepad_input_mode_sdl,
        summaryRes = R.string.settings_gamepad_input_mode_sdl_summary
    )
}