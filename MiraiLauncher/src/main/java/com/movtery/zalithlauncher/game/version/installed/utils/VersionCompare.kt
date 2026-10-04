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

package com.movtery.zalithlauncher.game.version.installed.utils

import org.jackhuang.hmcl.util.versioning.GameVersionNumber

/**
 * 判断版本是否大于某个版本
 */
fun String.isBiggerVer(other: String): Boolean {
    return GameVersionNumber.compare(this, other) > 0
}

/**
 * 判断版本是否大于等于某个版本
 */
fun String.isBiggerOrEqualVer(other: String): Boolean {
    return GameVersionNumber.compare(this, other) >= 0
}

/**
 * 判断版本是否小于某个版本
 */
fun String.isLowerVer(other: String): Boolean {
    return GameVersionNumber.compare(this, other) < 0
}

/**
 * 判断版本是否小于等于某个版本
 */
fun String.isLowerOrEqualVer(other: String): Boolean {
    return GameVersionNumber.compare(this, other) <= 0
}