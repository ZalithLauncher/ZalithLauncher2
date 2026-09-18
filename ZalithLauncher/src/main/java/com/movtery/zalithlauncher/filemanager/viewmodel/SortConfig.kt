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

package com.movtery.zalithlauncher.filemanager.viewmodel

import com.movtery.zalithlauncher.filemanager.config.FmConfig
import com.movtery.zalithlauncher.filemanager.logic.entry.FmEntry

/** 目录列表排序配置 */
data class SortConfig(
    val field: FmConfig.SortField = FmConfig.SortField.NAME,
    val ascending: Boolean = true,
    /** 目录在前 */
    val folderFirst: Boolean = true
) {
    companion object {
        fun load(): SortConfig = SortConfig(
            field = runCatching {
                FmConfig.SortField.valueOf(FmConfig.sortField())
            }.getOrDefault(FmConfig.SortField.NAME),
            ascending = FmConfig.sortAscending(),
            folderFirst = FmConfig.folderFirst()
        )
    }
}

/** 回收站专用排序配置 */
data class TrashSortConfig(
    val field: FmConfig.TrashSortField = FmConfig.TrashSortField.DELETED,
    /** 默认降序 删除时间越新越靠前 */
    val ascending: Boolean = false,
    /** 目录在前 */
    val folderFirst: Boolean = true
) {
    companion object {
        fun load(): TrashSortConfig = TrashSortConfig(
            field = runCatching {
                FmConfig.TrashSortField.valueOf(FmConfig.trashSortField())
            }.getOrDefault(FmConfig.TrashSortField.DELETED),
            ascending = FmConfig.trashSortAscending(),
            folderFirst = FmConfig.trashFolderFirst()
        )
    }
}

fun TrashSortConfig.persist() {
    FmConfig.setTrashSortField(field.name)
    FmConfig.setTrashSortAscending(ascending)
    FmConfig.setTrashFolderFirst(folderFirst)
}

fun SortConfig.persist() {
    FmConfig.setSortField(field.name)
    FmConfig.setSortAscending(ascending)
    FmConfig.setFolderFirst(folderFirst)
}

fun applyVisibility(
    entries: List<FmEntry>,
    config: SortConfig,
    showHidden: Boolean
): List<FmEntry> {
    var list = entries
    if (!showHidden) {
        list = list.filterNot { it.hidden }
    }
    val comparator: Comparator<FmEntry> = when (config.field) {
        FmConfig.SortField.NAME -> compareBy { it.name.lowercase() }

        // 目录无大小，按大小排序时目录回退为按名称排序
        FmConfig.SortField.SIZE -> compareBy<FmEntry> {
            if (it.isDirectory) 0L else it.size
        }.thenBy { it.name.lowercase() }

        FmConfig.SortField.MODIFIED -> compareBy { it.modifiedMs }
    }
    val ordered = if (config.ascending) {
        list.sortedWith(comparator)
    } else {
        list.sortedWith(comparator.reversed())
    }

    if (!config.folderFirst) return ordered

    // 保持排序中的“目录在前 / 文件在后”，目录间维持当前顺序
    val dirs = ordered.filter { it.isDirectory }
    val files = ordered.filter { it.isFile }
    return dirs + files
}