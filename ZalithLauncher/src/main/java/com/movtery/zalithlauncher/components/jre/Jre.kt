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

package com.movtery.zalithlauncher.components.jre

import com.movtery.zalithlauncher.R

enum class Jre(val jreName: String, val jrePath: String, val summary: Int, val majorVersion: Int) {
    JRE_8("Internal-8", "runtimes/jre-8", R.string.unpack_screen_jre8, 8),
    JRE_17("Internal-17", "runtimes/jre-17", R.string.unpack_screen_jre17, 17),
    JRE_21("Internal-21", "runtimes/jre-21", R.string.unpack_screen_jre21, 21),
    JRE_25("Internal-25", "runtimes/jre-25", R.string.unpack_screen_jre25, 25)
}