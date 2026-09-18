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

package com.movtery.zalithlauncher.game.addons.modloader.forgelike.neoforge.models

import com.movtery.zalithlauncher.game.addons.modloader.forgelike.neoforge.NeoForgeVersion

interface NeoForgeMergeableMaven<E>  {
    /**
     * 将自己的版本数据与其他的版本数据进行合并
     */
    operator fun plus(maven: E): List<NeoForgeVersion>

    fun isVersionInvalid(versionId: String): Boolean {
        val cantDownload = versionId == "47.1.82" //这个版本虽然在版本列表中，但不能下载
        val isAlpha = versionId.contains("-alpha") //Alpha版本不太稳定，避免下载
        return cantDownload || isAlpha
    }
}