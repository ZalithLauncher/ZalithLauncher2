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

package com.movtery.zalithlauncher.game.download.game.models

import com.google.gson.annotations.SerializedName

/**
 * 当前版本的版本信息，在安装过程中写入，为启动器提供更好的版本识别
 */
class LaunchFor(
    @SerializedName("infos")
    val infos: Array<Info>
) {
    class Info(
        /**
         * 版本
         * Minecraft 版本如：1.21.4
         * NeoForge 版本如：21.4.136
         */
        @SerializedName("version")
        val version: String,
        /**
         * 名称
         * 如 Minecraft、NeoForge
         */
        @SerializedName("name")
        val name: String
    )
}