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

import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.filemanager.config.FmConfig
import com.movtery.zalithlauncher.filemanager.events.FileManagerEvent
import com.movtery.zalithlauncher.filemanager.events.FileManagerEventBus
import com.movtery.zalithlauncher.filemanager.logic.FileManagerLogic
import com.movtery.zalithlauncher.filemanager.logic.FmResult
import com.movtery.zalithlauncher.filemanager.logic.ops.ConflictResolution
import com.movtery.zalithlauncher.filemanager.logic.trash.RestoreResult
import com.movtery.zalithlauncher.filemanager.logic.trash.TrashItem
import com.movtery.zalithlauncher.filemanager.viewmodel.DialogIntent
import com.movtery.zalithlauncher.filemanager.viewmodel.FmSnackbar
import com.movtery.zalithlauncher.filemanager.viewmodel.FmStateStore
import com.movtery.zalithlauncher.filemanager.viewmodel.TrashItemView
import com.movtery.zalithlauncher.filemanager.viewmodel.TrashListView
import com.movtery.zalithlauncher.filemanager.viewmodel.TrashSortConfig
import com.movtery.zalithlauncher.filemanager.viewmodel.TrashViewState
import com.movtery.zalithlauncher.filemanager.viewmodel.persist
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield

/** 回收站控制器 */
class TrashController(
    private val logic: FileManagerLogic,
    private val store: FmStateStore,
    private val browse: BrowseController,
    private val coroutineScope: CoroutineScope
) {
    /** 回收站多选集合 */
    private var trashSelection: Set<String> = emptySet()
    /** 回收站多选选区锚点 */
    private var trashRangeAnchorKey: String? = null

    /** 加载回收站列表 */
    fun loadTrashList() {
        if ((store.stateValue().trashView as? TrashViewState.Opened)?.trashListView?.loading == false) {
            // 已加载过，不再重复加载
            return
        }
        store.updateState {
            it.copy(
                trashView = TrashViewState.Opened(
                    emptyList(), TrashListView(loading = true)
                )
            )
        }
        coroutineScope.launch(Dispatchers.IO) { reloadTrash() }
    }

    fun refreshTrashList() {
        store.updateState {
            it.copy(
                trashView = TrashViewState.Opened(
                    emptyList(), TrashListView(loading = true)
                )
            )
        }
        coroutineScope.launch(Dispatchers.IO) { reloadTrash() }
    }

    fun closeTrash() {
        store.clearSelectionAndExitMulti()
        trashRangeAnchorKey = null
        trashSelection = emptySet()
        store.updateState { it.copy(trashView = TrashViewState.Idle) }
        browse.refreshDir(store.history.currentPath)
    }

    suspend fun reloadTrash() {
        var result = logic.trashList()

        while (result is FmResult.Rejected && currentCoroutineContext().isActive) {
            yield()
            result = logic.trashList()
        }
        when (result) {
            is FmResult.Ok -> updateTrashListView(result.value)
            is FmResult.Failed -> {
                store.emitSnackbar(FmSnackbar(result.error.message ?: store.stringResolver(R.string.fm_error_trash_load_failed)))
            }
            FmResult.Rejected -> {
                store.emitSnackbar(FmSnackbar(store.stringResolver(R.string.fm_task_busy)))
            }
            FmResult.Cancelled -> {}
        }
    }

    /** 设置回收站专用排序 */
    fun setTrashSortConfig(config: TrashSortConfig) {
        config.persist()
        store.updateState { it.copy(trashSortConfig = config) }
        (store.stateValue().trashView as? TrashViewState.Opened)?.let {
            updateTrashListView(it.rawItems)
        }
    }

    fun trashRestore(items: List<TrashItem>, resolutions: Map<String, ConflictResolution>) {
        coroutineScope.launch(Dispatchers.IO) {
            doTrashRestore(items, resolutions)
        }
    }

    /** 执行回收站恢复 */
    private suspend fun doTrashRestore(items: List<TrashItem>, resolutions: Map<String, ConflictResolution>) {
        when (val r = logic.trashRestore(items, resolutions)) {
            is FmResult.Ok -> {
                FileManagerEventBus.dispatch(FileManagerEvent(FileManagerEvent.Type.TRASH_RESTORE, r.value.mapNotNull {
                    (it as? RestoreResult.Ok)?.target?.parent?.toString()
                }.distinct()))
                reloadTrash()
            }
            is FmResult.Failed -> {
                store.emitSnackbar(FmSnackbar(r.error.message ?: store.stringResolver(R.string.fm_error_restore_failed)))
                reloadTrash()
            }
            FmResult.Rejected -> store.emitSnackbar(FmSnackbar(store.stringResolver(R.string.fm_task_busy)))
            FmResult.Cancelled -> {}
        }
    }

    /** 发起恢复流程 */
    fun beginTrashRestore(items: List<TrashItem>) {
        coroutineScope.launch(Dispatchers.IO) {
            val conflicts = runCatching {
                logic.trashRestoreConflicts(items)
            }.getOrDefault(emptyList())

            if (conflicts.isEmpty()) {
                doTrashRestore(items, emptyMap())
            } else {
                store.updateState {
                    it.copy(
                        dialogIntent = DialogIntent.TrashRestoreConflict(
                            trashItems = items,
                            conflictItems = conflicts,
                            resolutions = emptyMap(),
                            pendingIndex = 0
                        )
                    )
                }
            }
        }
    }

    /** 回收站恢复冲突决策 */
    fun resolveTrashRestoreConflict(resolution: ConflictResolution) {
        val cur = store.stateValue().dialogIntent as? DialogIntent.TrashRestoreConflict ?: return

        val conflictItems = cur.conflictItems
        val total = conflictItems.size
        val updated = cur.resolutions.toMutableMap()
        val (item, _) = conflictItems[cur.pendingIndex]
        updated[item.uuid] = resolution

        val next = cur.pendingIndex + 1
        if (next < total) {
            store.updateState {
                it.copy(
                    dialogIntent = DialogIntent.TrashRestoreConflict(
                        trashItems = cur.trashItems,
                        conflictItems = conflictItems,
                        resolutions = updated,
                        pendingIndex = next
                    )
                )
            }
        } else {
            // 全部决策完成
            store.dismissDialog()
            coroutineScope.launch(Dispatchers.IO) {
                doTrashRestore(cur.trashItems, updated)
            }
        }
    }

    fun restoreTrashItem(item: TrashItem) {
        beginTrashRestore(listOf(item))
    }

    fun trashRestoreAll() {
        val items = selectedTrashItems().takeIf { it.isNotEmpty() }
            ?: (store.stateValue().trashView as? TrashViewState.Opened)?.rawItems ?: emptyList()
        if (items.isEmpty()) return
        beginTrashRestore(items)
    }

    fun purgeTrashItem(item: TrashItem) {
        trashPurge(listOf(item))
    }

    fun trashPurge(items: List<TrashItem>) {
        coroutineScope.launch(Dispatchers.IO) {
            when (val r = logic.trashPurge(items)) {
                is FmResult.Ok -> {
                    FileManagerEventBus.dispatch(FileManagerEvent(FileManagerEvent.Type.TRASH_PURGE, items.map { it.meta.originalPath }))
                    reloadTrash()
                }
                is FmResult.Failed -> {
                    store.emitSnackbar(FmSnackbar(r.error.message ?: store.stringResolver(R.string.fm_error_purge_failed)))
                    reloadTrash()
                }
                FmResult.Rejected -> store.emitSnackbar(FmSnackbar(store.stringResolver(R.string.fm_task_busy)))
                FmResult.Cancelled -> {}
            }
        }
    }

    fun trashClear() {
        coroutineScope.launch(Dispatchers.IO) {
            when (val r = logic.trashClear()) {
                is FmResult.Ok -> {
                    FileManagerEventBus.dispatch(FileManagerEvent(FileManagerEvent.Type.TRASH_CLEAR, emptyList()))
                    reloadTrash()
                }
                is FmResult.Failed -> {
                    store.emitSnackbar(FmSnackbar(r.error.message ?: store.stringResolver(R.string.fm_error_clear_failed)))
                    reloadTrash()
                }
                FmResult.Rejected -> store.emitSnackbar(FmSnackbar(store.stringResolver(R.string.fm_task_busy)))
                FmResult.Cancelled -> {}
            }
        }
    }

    fun toggleTrashSelection(uuid: String) {
        trashSelection = if (uuid in trashSelection) {
            trashSelection - uuid
        } else {
            trashSelection + uuid
        }
        if (trashSelection.isEmpty()) trashRangeAnchorKey = null
        syncTrashSelection()
    }

    fun selectAllTrash() {
        val items = (store.stateValue().trashView as? TrashViewState.Opened)?.rawItems ?: emptyList()
        trashRangeAnchorKey = null
        trashSelection = items.map { it.uuid }.toSet()
        syncTrashSelection()
    }

    fun clearTrashSelection() {
        trashRangeAnchorKey = null
        trashSelection = emptySet()
        syncTrashSelection()
    }

    /** 回收站滑动连选 */
    fun trashRangeSelect(swipeItem: TrashItem) {
        val view = (store.stateValue().trashView as? TrashViewState.Opened) ?: return
        // 基于排序后的可见列表计算区间，与界面所见顺序一致
        val list = view.trashListView.items
        val swipeIndex = list.indexOfFirst { it.uuid == swipeItem.uuid }
        if (swipeIndex < 0) return

        if (trashSelection.isEmpty()) {
            trashRangeAnchorKey = swipeItem.uuid
            trashSelection = trashSelection + swipeItem.uuid
        } else {
            val anchorIndex = trashRangeAnchorKey?.let { a ->
                list.indexOfFirst { it.uuid == a }
            } ?: -1

            if (anchorIndex < 0) {
                trashRangeAnchorKey = swipeItem.uuid
                trashSelection = trashSelection + swipeItem.uuid
            } else {
                val from = minOf(anchorIndex, swipeIndex)
                val to = maxOf(anchorIndex, swipeIndex)
                trashSelection = trashSelection + list.subList(from, to + 1).map { it.uuid }
                // 本次框选完成，清除锚点
                trashRangeAnchorKey = null
            }
        }
        syncTrashSelection()
    }

    fun selectedTrashItems(): List<TrashItem> {
        val all = (store.stateValue().trashView as? TrashViewState.Opened)?.rawItems ?: emptyList()
        return all.filter { it.uuid in trashSelection }
    }

    private fun syncTrashSelection() {
        val cur = store.stateValue().trashView as? TrashViewState.Opened ?: return
        store.updateState {
            it.copy(
                trashView = cur.copy(
                    trashListView = cur.trashListView.copy(
                        selection = trashSelection,
                        multiSelect = trashSelection.isNotEmpty()
                    )
                )
            )
        }
    }

    private fun updateTrashListView(raw: List<TrashItem>) {
        val views = applyTrashSort(raw, store.stateValue().trashSortConfig)
        // 数据刷新后剔除已消失项的选中
        // 选中因此清空时退出多选并重置锚点
        val present = views.mapTo(mutableSetOf()) { it.uuid }
        if (trashSelection.any { it !in present }) {
            trashSelection = trashSelection.intersect(present)
            if (trashSelection.isEmpty()) trashRangeAnchorKey = null
        }
        val totalSize = views.sumOf { it.size }
        store.updateState {
            it.copy(
                trashView = TrashViewState.Opened(
                    raw,
                    TrashListView(
                        items = views,
                        totalSize = totalSize,
                        total = views.size,
                        loading = false,
                        selection = trashSelection,
                        multiSelect = trashSelection.isNotEmpty()
                    )
                )
            )
        }
    }

    private fun applyTrashSort(raw: List<TrashItem>, config: TrashSortConfig): List<TrashItemView> {
        val views = raw.map { it.toView() }
        val comparator: Comparator<TrashItemView> = when (config.field) {
            FmConfig.TrashSortField.NAME -> compareBy { it.name.lowercase() }
            FmConfig.TrashSortField.DELETED -> compareBy { it.deletedAt }
        }
        val ordered = if (config.ascending) {
            views.sortedWith(comparator)
        } else {
            views.sortedWith(comparator.reversed())
        }
        if (!config.folderFirst) return ordered
        val dirs = ordered.filter { it.isFolder }
        val files = ordered.filter { !it.isFolder }
        return dirs + files
    }

    private fun TrashItem.toView(): TrashItemView = TrashItemView(
        uuid = uuid,
        name = meta.name,
        deletedAt = meta.deletedAt,
        isFolder = meta.isFolder,
        size = size,
        corrupted = meta.corrupted
    )
}
