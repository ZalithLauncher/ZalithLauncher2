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

package com.movtery.zalithlauncher.utils.file

import java.io.File

/**
 * 记录文件夹内文件数量
 */
class FolderFileCounter(
    private val dir: File
) {
    private var counts: Int? = null

    /**
     * 记录目录内的文件数量，并检查是否有变化
     * @return 当前目录
     */
    fun checkDir(): Boolean {
        val tempCount = if (dir.isFile) {
            0
        } else {
            dir.list()?.size ?: 0
        }
        val result = getRecordedCount() != tempCount
        counts = tempCount
        return result
    }

    /**
     * 获取上一次记录的目录内的文件数量
     */
    fun getRecordedCount(): Int = counts ?: 0

    /**
     * 当前是否从未检查过文件数量
     */
    fun isUnchecked(): Boolean = counts == null
}