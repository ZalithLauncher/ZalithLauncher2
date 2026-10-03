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

package com.movtery.zalithlauncher.game.download.game.optifine

/**
 * OptiFine 与 Forge 1.17+ 不兼容时抛出（需要 H1 Pre2 之后的 OptiFine）
 */
class OptiFineForge17IncompatibleException(
    /** 当前 OptiFine 的构建号（buildof.txt） */
    val buildof: String
) : RuntimeException(
    "OptiFine (build $buildof) is incompatible with Forge 1.17+, " +
            "OptiFine H1 Pre2 (20210924-190833) or newer is required"
)
