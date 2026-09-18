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

import android.content.Context
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.filemanager.logic.FmFilenameError
import com.movtery.zalithlauncher.filemanager.logic.FmFilenameException
import com.movtery.zalithlauncher.filemanager.logic.entry.FmEntry
import com.movtery.zalithlauncher.filemanager.logic.extract.NotArchiveException
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import java.nio.file.AccessDeniedException
import java.nio.file.NoSuchFileException

/** 文件名最大长度 */
private const val MAX_FILENAME_LENGTH = 255

/** 选中集合的元素键：归一化路径字符串，规避 Path 实例 hashCode 差异 */
fun entryPathKey(entry: FmEntry): String = entry.path.normalize().toAbsolutePath().toString()

/**
 * 文件管理器共享状态
 */
class FmStateStore(private val context: Context) {

    /** 当前目录导航历史 */
    lateinit var history: NavHistory

    var clipboard: FmClipboard? = null
        private set

    /** 选中集合（归一化路径字符串） */
    var selection: Set<String> = emptySet()
    var rangeAnchorKey: String? = null

    /** 单条目删除确认暂存的条目键 */
    var stagedSingleDeleteKey: String? = null

    /** 待清理的导入临时目录 */
    var pendingImportTempDir: java.nio.file.Path? = null

    private val _state = MutableStateFlow(FileManagerUiState())
    val state: StateFlow<FileManagerUiState> = _state.asStateFlow()

    private val _searchUi = MutableStateFlow(SearchUiState())
    val searchUi: StateFlow<SearchUiState> = _searchUi.asStateFlow()

    private val _dirScan = MutableStateFlow<DirScanUiState?>(null)
    val dirScan: StateFlow<DirScanUiState?> = _dirScan.asStateFlow()

    private val _editorUi = MutableStateFlow(EditorUiState())
    val editorUi: StateFlow<EditorUiState> = _editorUi.asStateFlow()

    private val _errorEvents = MutableSharedFlow<String>(extraBufferCapacity = 8)
    val errorEvents: SharedFlow<String> = _errorEvents.asSharedFlow()

    fun stateValue(): FileManagerUiState = _state.value

    fun updateState(transform: (FileManagerUiState) -> FileManagerUiState) {
        _state.value = transform(_state.value)
    }

    fun searchUiValue(): SearchUiState = _searchUi.value

    fun updateSearchUi(transform: (SearchUiState) -> SearchUiState) {
        _searchUi.value = transform(_searchUi.value)
    }

    fun setSearchUi(value: SearchUiState) {
        _searchUi.value = value
    }

    fun setDirScan(value: DirScanUiState?) {
        _dirScan.value = value
    }

    fun editorUiValue(): EditorUiState = _editorUi.value

    fun updateEditorUi(transform: (EditorUiState) -> EditorUiState) {
        _editorUi.value = transform(_editorUi.value)
    }

    fun emitSnackbar(s: FmSnackbar) {
        _state.value = _state.value.copy(snackbar = s)
    }

    fun dismissDialog() {
        if (_state.value.dialogIntent != null) _state.value = _state.value.copy(dialogIntent = null)
    }

    fun emitError(message: String) {
        _errorEvents.tryEmit(message)
    }

    fun setSelection(newSelection: Set<String>, multiSelect: Boolean) {
        selection = newSelection
        _state.value = _state.value.copy(
            selection = newSelection,
            multiSelect = if (newSelection.isEmpty()) false else multiSelect
        )
    }

    /** 清空选中并退出多选 */
    fun clearSelectionAndExitMulti() {
        rangeAnchorKey = null
        setSelection(emptySet(), false)
    }

    fun setClipboard(cb: FmClipboard?) {
        clipboard = cb
        _state.value = _state.value.copy(clipboard = cb)
    }

    fun selectedEntries(): List<FmEntry> {
        val all = _state.value.rawList?.entries ?: return emptyList()
        return all.filter { entryPathKey(it) in selection }
    }

    fun stringResolver(resId: Int): String = context.getString(resId)

    fun filenameErrorText(e: FmFilenameException): String = when (e.type) {
        FmFilenameError.ILLEGAL_CHARACTERS -> context.getString(
            R.string.generic_input_invalid_character,
            e.illegalCharacters ?: ""
        )
        FmFilenameError.INVALID_LENGTH -> context.getString(R.string.file_invalid_length, e.invalidLength, MAX_FILENAME_LENGTH)
        FmFilenameError.LEADING_OR_TRAILING_SPACE -> stringResolver(R.string.file_invalid_leading_or_trailing_space)
        FmFilenameError.NAME_CONFLICT -> stringResolver(R.string.fm_name_conflict)
    }

    fun operationErrorText(e: Exception, fallbackRes: Int): String =
        if (e is FmFilenameException) filenameErrorText(e) else stringResolver(fallbackRes)

    fun fileOpErrorText(e: Throwable, fallbackRes: Int): String = when (e) {
        is NoSuchFileException -> stringResolver(R.string.fm_error_file_not_found)
        is AccessDeniedException -> stringResolver(R.string.fm_error_access_denied)
        is NotArchiveException -> stringResolver(R.string.fm_error_not_archive)
        else -> e.message?.takeIf { it.isNotBlank() } ?: stringResolver(fallbackRes)
    }
}
