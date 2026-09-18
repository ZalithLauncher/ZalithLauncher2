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

package com.movtery.zalithlauncher.game.version.saves

import com.movtery.zalithlauncher.R

/**
 * @param levelCode 在 level.dat 中存储的值
 */
enum class Difficulty(val levelCode: Int, val nameRes: Int) {
    /** 和平 */
    PEACEFUL(0, R.string.saves_manage_difficulty_peaceful),
    /** 简单 */
    EASY(1, R.string.saves_manage_difficulty_easy),
    /** 普通 */
    NORMAL(2, R.string.saves_manage_difficulty_normal),
    /** 困难 */
    HARD(3, R.string.saves_manage_difficulty_hard)
}