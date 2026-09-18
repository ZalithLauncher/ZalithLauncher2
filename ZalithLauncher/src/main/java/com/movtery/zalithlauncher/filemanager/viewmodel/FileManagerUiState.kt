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

import com.movtery.zalithlauncher.filemanager.logic.entry.FmEntry
import com.movtery.zalithlauncher.filemanager.logic.entry.FmListResult
import com.movtery.zalithlauncher.filemanager.logic.ops.ConflictResolution
import com.movtery.zalithlauncher.filemanager.logic.ops.DirStats
import com.movtery.zalithlauncher.filemanager.logic.ops.PasteRequest
import com.movtery.zalithlauncher.filemanager.logic.task.TaskProgress
import com.movtery.zalithlauncher.filemanager.logic.task.TaskState
import com.movtery.zalithlauncher.filemanager.logic.trash.TrashItem
import com.movtery.zalithlauncher.ui.code_editor.EditorState
import java.nio.file.Path

/** 目录属性扫描状态 */
data class DirScanUiState(
    val running: Boolean,
    val stats: DirStats?
)

/** 文本编辑器状态 */
data class EditorUiState(
    /** 正在编辑的文件路径 */
    val path: Path? = null,
    /** 文件内容状态（加载中 / 加载完成） */
    val state: EditorState = EditorState.Loading,
    /** 文件是否可写，不可写时以只读方式打开 */
    val writable: Boolean = true,
    /** 是否存在未保存的修改 */
    val dirty: Boolean = false,
    /** 是否正在保存 */
    val saving: Boolean = false,
    /** 是否请求显示退出确认弹窗 */
    val exitConfirm: Boolean = false,
    /** 打开 / 加载失败的错误提示 */
    val error: String? = null
)

/** 搜索状态 */
data class SearchUiState(
    /** 是否正在搜索 */
    val running: Boolean = false,
    /** 当前正在扫描的目录 */
    val currentDir: Path? = null,
    /** 搜索结果列表 */
    val hits: List<SearchHitView> = emptyList(),
    /** 最近一次搜索的关键词 */
    val lastKeyword: String = ""
)

/** 搜索命中结果视图 */
data class SearchHitView(
    val path: Path,
    val name: String,
    val isDirectory: Boolean,
    val size: Long
)

/** 文件管理器状态集合 */
data class FileManagerUiState(
    val currentDir: Path? = null,
    /** 刷新流水线是否运行中 */
    val refreshing: Boolean = false,
    val visibleEntries: List<FmEntry> = emptyList(),
    val rawList: RawList? = null,
    val folderCount: Int = 0,
    val fileCount: Int = 0,
    val selection: Set<String> = emptySet(),
    val multiSelect: Boolean = false,
    val clipboard: FmClipboard? = null,
    val sortConfig: SortConfig = SortConfig(),
    val showHidden: Boolean = false,
    val trashSortConfig: TrashSortConfig = TrashSortConfig(),
    val taskState: TaskState = TaskState.Idle,
    val taskProgress: TaskProgress? = null,
    val trashView: TrashViewState = TrashViewState.Idle,
    val snackbar: FmSnackbar? = null,
    val locateHighlightPath: Path? = null,
    val canNavigateBack: Boolean = false,
    val canNavigateForward: Boolean = false,
    /** 当前打开的对话框意图；为空表示无对话框 */
    val dialogIntent: DialogIntent? = null
) {
    val canBack: Boolean get() = rawList?.let { it.currentDir != it.rootDir } ?: false
}

/** 原始浏览结果 */
data class RawList(
    val currentDir: Path,
    val rootDir: Path,
    val ancestors: List<Path>,
    val entries: List<FmEntry>,
    val hasSubdirectory: Boolean,
    val writable: Boolean
) {
    companion object {
        fun of(result: FmListResult): RawList = RawList(
            currentDir = result.currentDir,
            rootDir = result.rootDir,
            ancestors = result.ancestors,
            entries = result.entries,
            hasSubdirectory = result.hasSubdirectory,
            writable = result.writable
        )
    }
}

/** 回收站视图 */
sealed interface TrashViewState {
    data object Idle : TrashViewState
    /** 回收站已打开 */
    data class Opened(
        val rawItems: List<TrashItem>,
        val trashListView: TrashListView
    ) : TrashViewState
}

data class TrashItemView(
    val uuid: String,
    val name: String,
    val deletedAt: Long,
    val isFolder: Boolean,
    val size: Long,
    val corrupted: Boolean
)

/** 回收站内部加载视图 */
data class TrashListView(
    val items: List<TrashItemView> = emptyList(),
    val totalSize: Long = 0L,
    val total: Int = 0,
    val loading: Boolean = false,
    val selection: Set<String> = emptySet(),
    val multiSelect: Boolean = false
)

/** Snackbar 消息 */
data class FmSnackbar(
    val text: String,
    val long: Boolean = true
)

/** 对话框意图 */
sealed interface DialogIntent {
    /** 搜索设置对话框 */
    data object Search : DialogIntent
    /** 搜索任务对话框 */
    data object SearchTask : DialogIntent
    /** 搜索结果列表对话框 */
    data object SearchResult : DialogIntent

    /**
     * 压缩设置对话框
     * @param defaultName 默认压缩包名（不含后缀）
     * @param sources 待压缩条目的绝对路径
     */
    data class CompressSetup(
        val defaultName: String,
        val sources: List<Path>
    ) : DialogIntent
    /** 压缩输出位置选择 */
    data object CompressOutputChoice : DialogIntent
    /** 压缩输出位置标记，通过 SAF 选择输出目录。 */
    data object CompressOutputPick : DialogIntent
    /** 输出目录已存在同名压缩包时的冲突对话框 */
    data class CompressConflict(
        val fileName: String
    ) : DialogIntent

    /**
     * 解压设置对话框
     * @param archivePath 压缩包绝对路径
     * @param archiveName 压缩包文件名
     */
    data class ExtractSetup(
        val archivePath: Path,
        val archiveName: String
    ) : DialogIntent
    /** 解压输出位置选择 */
    data object ExtractOutputChoice : DialogIntent
    /** 解压输出位置标记，通过 SAF 选择输出目录 */
    data object ExtractOutputPick : DialogIntent
    /** 解压目标已存在同名顶层内容时的冲突对话框 */
    data class ExtractConflict(
        val name: String
    ) : DialogIntent
    /** 解压密码输入对话框 */
    data class ExtractPassword(
        val errorText: String? = null
    ) : DialogIntent

    /** 通过 SAF 多选文件 */
    data object ImportFiles : DialogIntent
    /** 通过 SAF 选择目录 */
    data object ImportDir : DialogIntent

    /** 粘贴冲突流程 */
    data class PasteConflict(
        val request: PasteRequest.ResolveRequest,
        val decidedResolutions: List<ConflictResolution> = emptyList(),
        val currentIndex: Int = 0
    ) : DialogIntent

    /** 回收站恢复冲突流程 */
    data class TrashRestoreConflict(
        val trashItems: List<TrashItem>,
        val conflictItems: List<Pair<TrashItem, Int>>,
        val resolutions: Map<String, ConflictResolution> = emptyMap(),
        val pendingIndex: Int = 0
    ) : DialogIntent
}