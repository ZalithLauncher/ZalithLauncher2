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

package com.movtery.zalithlauncher.game.download.modpack.install

import java.io.File

/**
 * 整合包可下载模组文件
 * @param getFile 如果无法临时构建模组下载链接，或者构建模组下载链接过于耗时，则可以在这里进行构建
 */
data class ModFile(
    val outputFile: File? = null,
    val downloadUrls: List<String>? = null,
    val sha1: String? = null,
    val getFile: (suspend () -> ModFile)? = null
)
