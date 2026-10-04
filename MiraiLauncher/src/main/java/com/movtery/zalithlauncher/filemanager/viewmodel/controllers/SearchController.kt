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
import com.movtery.zalithlauncher.filemanager.logic.FileManagerLogic
import com.movtery.zalithlauncher.filemanager.logic.SearchResult
import com.movtery.zalithlauncher.filemanager.logic.task.RunResult
import com.movtery.zalithlauncher.filemanager.logic.task.TaskKind
import com.movtery.zalithlauncher.filemanager.logic.task.TaskManager
import com.movtery.zalithlauncher.filemanager.viewmodel.DialogIntent
import com.movtery.zalithlauncher.filemanager.viewmodel.FmSnackbar
import com.movtery.zalithlauncher.filemanager.viewmodel.FmStateStore
import com.movtery.zalithlauncher.filemanager.viewmodel.SearchHitView
import com.movtery.zalithlauncher.filemanager.viewmodel.SearchUiState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/** 搜索控制器 */
class SearchController(
    private val logic: FileManagerLogic,
    private val taskManager: TaskManager,
    private val store: FmStateStore,
    private val coroutineScope: CoroutineScope
) {
    /**
     * 打开搜索入口
     * 本次会话已有结果时直接显示结果列表，否则显示搜索设置
     */
    fun showSearchDialog() {
        store.dismissDialog()
        val hasResult = store.searchUiValue().lastKeyword.isNotBlank()
        store.updateState {
            it.copy(dialogIntent = if (hasResult) DialogIntent.SearchResult else DialogIntent.Search)
        }
    }

    /**
     * 提交搜索
     * 非空校验后进入搜索任务，完成后展示结果列表
     */
    fun submitSearch(keyword: String, caseSensitive: Boolean) {
        if (keyword.isBlank()) return
        val startDir = store.history.currentPath
        store.setSearchUi(
            SearchUiState(
                running = true,
                currentDir = startDir,
                hits = emptyList(),
                lastKeyword = keyword
            )
        )
        store.updateState {
            it.copy(dialogIntent = DialogIntent.SearchTask)
        }

        coroutineScope.launch(Dispatchers.IO) {
            val result = taskManager.run(TaskKind.SEARCH) {
                logic.search(startDir, keyword, caseSensitive) { dir ->
                    report(currentName = dir.toString())
                    store.updateSearchUi { it.copy(currentDir = dir) }
                }
            }
            when (result) {
                is RunResult.Ok -> {
                    when (val search = result.value) {
                        is SearchResult.Ok -> {
                            // 隐藏过滤由数据控制层完成
                            val showHidden = FmConfig.showHidden()
                            val hits = search.hits
                                .filterNot { !showHidden && it.hidden }
                                .map { SearchHitView(it.path, it.name, it.isDirectory, it.size) }

                            store.setSearchUi(SearchUiState(running = false, currentDir = null, hits = hits, lastKeyword = keyword))
                            store.updateState { it.copy(dialogIntent = DialogIntent.SearchResult) }
                        }
                        is SearchResult.Failed -> {
                            store.setSearchUi(SearchUiState(running = false, currentDir = null, lastKeyword = keyword))
                            store.emitSnackbar(FmSnackbar(store.stringResolver(R.string.fm_error_search_failed)))
                            store.updateState { it.copy(dialogIntent = DialogIntent.SearchResult) }
                        }
                    }
                }
                is RunResult.Failed -> {
                    store.setSearchUi(SearchUiState(running = false, currentDir = null, lastKeyword = keyword))
                    store.emitSnackbar(FmSnackbar(store.stringResolver(R.string.fm_error_search_failed)))
                    store.updateState { it.copy(dialogIntent = DialogIntent.SearchResult) }
                }
                RunResult.Cancelled -> {
                    store.updateSearchUi { it.copy(running = false, currentDir = null) }
                    store.updateState { it.copy(dialogIntent = DialogIntent.SearchResult) }
                }
                is RunResult.Rejected -> {
                    store.setSearchUi(SearchUiState(running = false, currentDir = null, lastKeyword = keyword))
                    store.emitSnackbar(FmSnackbar(store.stringResolver(R.string.fm_task_busy)))
                    store.updateState { it.copy(dialogIntent = DialogIntent.SearchResult) }
                }
            }
        }
    }

    /** 清除搜索结果，返回搜索设置对话框 */
    fun clearSearch() {
        store.setSearchUi(SearchUiState())
        store.updateState {
            it.copy(dialogIntent = DialogIntent.Search)
        }
    }

    /** 从搜索结果列表返回搜索设置对话框（发起新搜索） */
    fun backToSearchSetup() {
        store.updateState {
            it.copy(dialogIntent = DialogIntent.Search)
        }
    }
}
