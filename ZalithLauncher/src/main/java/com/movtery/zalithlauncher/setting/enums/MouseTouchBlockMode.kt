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

package com.movtery.zalithlauncher.setting.enums

import com.movtery.zalithlauncher.R

/**
 * 虚拟鼠标的半屏触摸阻止模式
 */
enum class MouseTouchBlockMode(val nameRes: Int) {
    NONE(R.string.generic_close),
    /** 阻止左半屏的触摸输入 */
    LEFT(R.string.settings_control_mouse_block_touch_left),
    /** 阻止右半屏的触摸输入 */
    RIGHT(R.string.settings_control_mouse_block_touch_right)
}
