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

package com.movtery.zalithlauncher.filemanager.viewmodel.controllers

import com.movtery.zalithlauncher.filemanager.logic.entry.FmEntry
import com.movtery.zalithlauncher.filemanager.viewmodel.FmStateStore
import com.movtery.zalithlauncher.filemanager.viewmodel.entryPathKey

/** 主列表选择 / 多选控制器 */
class SelectionController(private val store: FmStateStore) {
    /**
     * 进入多选并选中该条目
     */
    fun enterMultiSelectWith(entry: FmEntry) {
        val key = entryPathKey(entry)
        store.rangeAnchorKey = key
        store.setSelection(store.selection + key, true)
    }

    /**
     * 在多选模式下点击条目，切换选中
     */
    fun toggleSelection(entry: FmEntry) {
        val key = entryPathKey(entry)
        val newSelection = if (key in store.selection) {
            store.selection - key
        } else {
            store.selection + key
        }
        if (newSelection.isEmpty()) {
            store.rangeAnchorKey = null
            store.setSelection(newSelection, false)
        } else {
            store.setSelection(newSelection, true)
        }
    }

    /**
     * 滑动连选
     * 以锚点与滑动项为边界，选中两者之间的未选中项
     */
    fun swipeRangeSelect(entry: FmEntry) {
        val list = store.stateValue().visibleEntries
        val swipeKey = entryPathKey(entry)
        val swipeIndex = list.indexOfFirst { entryPathKey(it) == swipeKey }
        if (swipeIndex < 0) return

        if (!store.stateValue().multiSelect) {
            enterMultiSelectWith(entry)
            return
        }

        val anchorIndex = store.rangeAnchorKey?.let { a ->
            list.indexOfFirst { entryPathKey(it) == a }
        } ?: -1
        if (anchorIndex < 0) {
            // 无锚点（框选已结束 / 锚点失效）
            // 以本次滑动项为新锚点，新一轮框选开始
            store.rangeAnchorKey = swipeKey
            store.setSelection(store.selection + swipeKey, true)
            return
        }

        val from = minOf(anchorIndex, swipeIndex)
        val to = maxOf(anchorIndex, swipeIndex)
        val newSelection = store.selection + list.subList(from, to + 1).map { entryPathKey(it) }
        // 本次框选完成，清除锚点，以便下一次滑动开始新一轮框选
        store.rangeAnchorKey = null
        store.setSelection(newSelection, true)
    }

    fun selectAll() {
        store.rangeAnchorKey = null
        store.setSelection(store.stateValue().visibleEntries.map { entryPathKey(it) }.toSet(), true)
    }

    fun clearSelection() {
        store.rangeAnchorKey = null
        store.setSelection(emptySet(), false)
    }
}
