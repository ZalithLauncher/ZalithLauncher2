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

package com.movtery.zalithlauncher.filemanager.ui

import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.filemanager.logic.editor.isKnownTextFile
import com.movtery.zalithlauncher.filemanager.logic.entry.FmEntry
import com.movtery.zalithlauncher.filemanager.logic.ops.ConflictResolution
import com.movtery.zalithlauncher.filemanager.os.FmLog
import com.movtery.zalithlauncher.filemanager.ui.components.FmAlertDialog
import com.movtery.zalithlauncher.filemanager.ui.components.FmAppBar
import com.movtery.zalithlauncher.filemanager.ui.components.FmBottomBar
import com.movtery.zalithlauncher.filemanager.ui.components.FmCardPosition
import com.movtery.zalithlauncher.filemanager.ui.components.FmEntryItem
import com.movtery.zalithlauncher.filemanager.ui.components.FmNavRail
import com.movtery.zalithlauncher.filemanager.ui.dialogs.FmBulkActionsDialog
import com.movtery.zalithlauncher.filemanager.ui.dialogs.FmCompressDialog
import com.movtery.zalithlauncher.filemanager.ui.dialogs.FmCompressOutputChoiceDialog
import com.movtery.zalithlauncher.filemanager.ui.dialogs.FmConflictDialog
import com.movtery.zalithlauncher.filemanager.ui.dialogs.FmCreateDialog
import com.movtery.zalithlauncher.filemanager.ui.dialogs.FmDeleteConfirmDialog
import com.movtery.zalithlauncher.filemanager.ui.dialogs.FmExtractDialog
import com.movtery.zalithlauncher.filemanager.ui.dialogs.FmExtractPasswordDialog
import com.movtery.zalithlauncher.filemanager.ui.dialogs.FmJumpDialog
import com.movtery.zalithlauncher.filemanager.ui.dialogs.FmPropertyDialog
import com.movtery.zalithlauncher.filemanager.ui.dialogs.FmRenameDialog
import com.movtery.zalithlauncher.filemanager.ui.dialogs.FmSearchResultDialog
import com.movtery.zalithlauncher.filemanager.ui.dialogs.FmSearchSetupDialog
import com.movtery.zalithlauncher.filemanager.ui.dialogs.FmSearchTaskDialog
import com.movtery.zalithlauncher.filemanager.ui.theme.FmAnimations
import com.movtery.zalithlauncher.filemanager.ui.theme.fmBackgroundColor
import com.movtery.zalithlauncher.filemanager.ui.theme.fmOnBackgroundColor
import com.movtery.zalithlauncher.filemanager.ui.theme.fmSecondaryTextColor
import com.movtery.zalithlauncher.filemanager.viewmodel.DialogIntent
import com.movtery.zalithlauncher.filemanager.viewmodel.FileManagerUiState
import com.movtery.zalithlauncher.filemanager.viewmodel.FileManagerViewModel
import com.movtery.zalithlauncher.filemanager.viewmodel.SearchUiState
import com.movtery.zalithlauncher.filemanager.viewmodel.applyVisibility
import com.movtery.zalithlauncher.utils.file.formatFileSize
import com.movtery.zalithlauncher.utils.formatDate
import kotlinx.coroutines.delay
import java.nio.file.Path
import kotlin.math.floor
import kotlin.time.Duration.Companion.milliseconds

private sealed interface FmOperation {
    data object None : FmOperation
    data object Create : FmOperation
    data class Jump(val currentPath: String) : FmOperation
    data class Rename(val entry: FmEntry) : FmOperation
    data class Property(val entry: FmEntry) : FmOperation
    data class DeleteConfirm(val count: Int) : FmOperation
    /** 未知格式文件点击编辑时的确认 */
    data class EditConfirm(val entry: FmEntry) : FmOperation
    data object BulkActions : FmOperation
    data object RangeSelectHelp : FmOperation
}

/**
 * 文件管理器主页面
 * @param vm 文件管理器视图模型
 * @param snackHost 全局 Snackbar 宿主
 * @param onOpenTrash 打开回收站回调
 * @param onOpenEditor 打开文本编辑器回调
 * @param onExit 退出文件管理器回调
 * @param onToggleOrientation 横竖屏切换回调
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FmMainPage(
    vm: FileManagerViewModel,
    snackHost: SnackbarHostState,
    /** 返回手势透明度（1 = 完全可见），仅作用于内容区，不随顶栏 / 底栏 */
    contentAlpha: Animatable<Float, AnimationVector1D>,
    onOpenTrash: () -> Unit,
    onOpenEditor: (Path) -> Unit,
    onExit: () -> Unit,
    onToggleOrientation: () -> Unit
) {
    val uiState by vm.state.collectAsStateWithLifecycle()
    val searchUi by vm.searchUi.collectAsStateWithLifecycle()
    var operation by remember { mutableStateOf<FmOperation>(FmOperation.None) }
    val updateOperation: (FmOperation) -> Unit = { operation = it }
    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE

    val listState = rememberLazyListState()
    val gridState = rememberLazyGridState()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            FmAppBar(
                currentPath = uiState.currentDir?.toString() ?: "",
                folderCount = uiState.folderCount,
                fileCount = uiState.fileCount,
                selectedCount = uiState.selection.size,
                multiSelect = uiState.multiSelect,
                clipboardPresent = uiState.clipboard != null,
                showHidden = uiState.showHidden,
                onJump = { updateOperation(FmOperation.Jump(uiState.currentDir?.toString() ?: "")) },
                onPaste = { vm.requestPaste() },
                onRefresh = { vm.refresh() },
                onImportFile = { vm.showImportFilesDialog() },
                onImportDir = { vm.showImportDirDialog() },
                onSearch = { vm.showSearchDialog() },
                searchEnabled = !searchUi.running,
                onSortChange = { vm.setSortConfig(it) },
                sortConfig = uiState.sortConfig,
                onToggleHidden = { vm.toggleHidden() },
                onOpenTrash = onOpenTrash,
                onExit = onExit,
                onToggleOrientation = onToggleOrientation
            )
        },
        bottomBar = {
            if (!isLandscape) {
                FmBottomBar(
                    multiSelect = uiState.multiSelect,
                    canParent = uiState.canBack,
                    onBack = { vm.back() },
                    onForward = { vm.forward() },
                    onNew = { updateOperation(FmOperation.Create) },
                    onParent = { vm.goParent() },
                    onSelectAll = { vm.selectAll() },
                    onClearSelection = { vm.clearSelection() },
                    onRangeSelectHelp = { updateOperation(FmOperation.RangeSelectHelp) },
                    canBack = uiState.canNavigateBack,
                    canForward = uiState.canNavigateForward,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        snackbarHost = { SnackbarHost(snackHost) },
        containerColor = fmBackgroundColor(),
        contentColor = fmOnBackgroundColor(),
    ) { inner ->
        if (isLandscape) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(inner)
            ) {
                // 侧边操作栏
                FmNavRail(
                    multiSelect = uiState.multiSelect,
                    canParent = uiState.canBack,
                    onBack = { vm.back() },
                    onForward = { vm.forward() },
                    onNew = { updateOperation(FmOperation.Create) },
                    onParent = { vm.goParent() },
                    onSelectAll = { vm.selectAll() },
                    onClearSelection = { vm.clearSelection() },
                    onRangeSelectHelp = { updateOperation(FmOperation.RangeSelectHelp) },
                    canBack = uiState.canNavigateBack,
                    canForward = uiState.canNavigateForward
                )
                // 文件项列表
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                ) {
                    MainContent(
                        uiState = uiState,
                        vm = vm,
                        updateOperation = updateOperation,
                        onOpenEditor = onOpenEditor,
                        listState = listState,
                        gridState = gridState,
                        contentAlpha = contentAlpha
                    )
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(inner)
            ) {
                MainContent(
                    uiState = uiState,
                    vm = vm,
                    updateOperation = updateOperation,
                    onOpenEditor = onOpenEditor,
                    listState = listState,
                    gridState = gridState,
                    contentAlpha = contentAlpha
                )
            }
        }
    }

    // 对话框宿主
    FmMainDialogs(
        vm = vm,
        uiState = uiState,
        searchUi = searchUi,
        operation = operation,
        updateOperation = updateOperation,
        onOpenEditor = onOpenEditor
    )
}

@Composable
private fun MainContent(
    uiState: FileManagerUiState,
    vm: FileManagerViewModel,
    updateOperation: (FmOperation) -> Unit,
    onOpenEditor: (Path) -> Unit,
    listState: LazyListState,
    gridState: LazyGridState,
    contentAlpha: Animatable<Float, AnimationVector1D>
) {
    val rawList = uiState.rawList
    val contentState = if (uiState.refreshing &&
        (rawList == null || rawList.currentDir != uiState.currentDir)
    ) {
        null
    } else {
        rawList
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer { alpha = contentAlpha.value }
    ) {
        AnimatedContent(
            targetState = contentState,
            transitionSpec = {
                fadeIn(tween(FmAnimations.FADE_IN_MS)) togetherWith
                    fadeOut(tween(FmAnimations.FADE_OUT_MS))
            },
            label = "fm-content"
        ) { state ->
            when {
                state == null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                else -> {
                    // 按各帧自己的内容渲染
                    val visible = remember(state, uiState.sortConfig, uiState.showHidden) {
                        applyVisibility(state.entries, uiState.sortConfig, uiState.showHidden)
                    }
                    if (visible.isEmpty()) {
                        EmptyBox(stringResource(R.string.fm_empty_dir))
                    } else {
                        EntryList(
                            entries = visible,
                            uiState = uiState,
                            vm = vm,
                            updateOperation = updateOperation,
                            onOpenEditor = onOpenEditor,
                            listState = listState,
                            gridState = gridState
                        )
                    }
                }
            }
        }

        // 刷新进行中，全屏透明输入拦截层
        if (uiState.refreshing) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        awaitPointerEventScope {
                            while (true) {
                                awaitPointerEvent().changes.forEach { it.consume() }
                            }
                        }
                    }
            )
        }
    }
}

@Composable
private fun EntryList(
    entries: List<FmEntry>,
    uiState: FileManagerUiState,
    vm: FileManagerViewModel,
    updateOperation: (FmOperation) -> Unit,
    onOpenEditor: (Path) -> Unit,
    listState: LazyListState,
    gridState: LazyGridState
) {
    val cutPaths by remember(uiState.clipboard) {
        derivedStateOf { uiState.clipboard?.sources?.map { it.normalize().toAbsolutePath().toString() }?.toSet() ?: emptySet() }
    }
    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE

    fun selectionKey(entry: FmEntry): String =
        entry.path.normalize().toAbsolutePath().toString()

    // 定位高亮：自动滚动到目标项，短暂闪烁后清除
    val highlightKey = uiState.locateHighlightPath?.normalize()?.toAbsolutePath()?.toString()
    val highlightIndex = highlightKey?.let { key -> entries.indexOfFirst { selectionKey(it) == key } }
    LaunchedEffect(highlightKey, highlightIndex) {
        if (highlightIndex != null && highlightIndex >= 0) {
            if (isLandscape) {
                gridState.scrollToItem(highlightIndex)
            } else {
                listState.animateScrollToItem(highlightIndex)
            }
            delay(FmAnimations.LOCATE_HIGHLIGHT_MS.milliseconds)
            vm.consumeLocateHighlight()
        }
    }

    val onClick: (FmEntry) -> Unit = { entry ->
        if (uiState.multiSelect) {
            vm.toggleSelection(entry)
        } else if (entry.isDirectory) {
            vm.enterDirectory(entry)
        }
    }
    val onLongClick: (FmEntry) -> Unit = { entry ->
        if (uiState.multiSelect) {
            updateOperation(FmOperation.BulkActions)
        }
    }
    val onSwipeTrigger: (FmEntry) -> Unit = { vm.swipeRangeSelect(it) }
    val onEntryDelete: (FmEntry) -> Unit = { entry ->
        // 单条目删除：临时加入选中集合后进入删除确认
        vm.stageSingleDelete(entry)
        updateOperation(FmOperation.DeleteConfirm(1))
    }
    val onEntryEdit: (FmEntry) -> Unit = { entry ->
        // 已知文本格式直接进入编辑器，未知格式先弹警告确认
        if (isKnownTextFile(entry.name)) {
            onOpenEditor(entry.path)
        } else {
            updateOperation(FmOperation.EditConfirm(entry))
        }
    }

    val itemContent: @Composable (FmEntry, FmCardPosition) -> Unit = { entry, position ->
        val key = selectionKey(entry)
        FmEntryItem(
            entry = entry,
            position = position,
            multiSelect = uiState.multiSelect,
            selected = key in uiState.selection,
            cutMarked = key in cutPaths && (uiState.clipboard?.isCut == true),
            highlighted = highlightKey == key,
            onClick = { onClick(entry) },
            onLongClick = { onLongClick(entry) },
            onSwipeTrigger = { onSwipeTrigger(entry) },
            onRename = { updateOperation(FmOperation.Rename(entry)) },
            onProperty = { updateOperation(FmOperation.Property(entry)) },
            onEdit = { onEntryEdit(entry) },
            onExtract = { vm.showExtract(entry) },
            onShare = { vm.showShare(entry) },
            onCompress = { vm.compressEntry(entry) },
            onCopy = { vm.copyEntry(entry) },
            onCut = { vm.cutEntry(entry) },
            onDelete = { onEntryDelete(entry) }
        )
    }

    if (isLandscape) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val gap = 2.dp
            val minColumnSize = 280.dp
            // 与 GridCells.Adaptive 相同的列数公式，保证条目方位与实际网格一致
            val columns = floor(
                (maxWidth - 24.dp + gap).value / (minColumnSize + gap).value
            ).toInt().coerceAtLeast(1)
            LazyVerticalGrid(
                state = gridState,
                columns = GridCells.Fixed(columns),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 8.dp, bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(gap),
                verticalArrangement = Arrangement.spacedBy(gap)
            ) {
                itemsIndexed(entries, key = { _, entry -> entry.path.toString() }) { index, entry ->
                    itemContent(entry, FmCardPosition.of(index, entries.size, columns))
                }
            }
        }
    } else {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            itemsIndexed(entries, key = { _, entry -> entry.path.toString() }) { index, entry ->
                itemContent(entry, FmCardPosition.of(index, entries.size))
            }
        }
    }
}

@Composable
private fun FmMainDialogs(
    vm: FileManagerViewModel,
    uiState: FileManagerUiState,
    searchUi: SearchUiState,
    operation: FmOperation,
    updateOperation: (FmOperation) -> Unit,
    onOpenEditor: (Path) -> Unit
) {
    val intent = uiState.dialogIntent

    fun grantReadPermissions(uris: List<Uri>) {
        val resolver = vm.appContext().contentResolver
        for (uri in uris) {
            runCatching {
                resolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
        }
    }

    // 处理 SAF 选择导入完整目录
    val safDirLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val uri = result.data?.data
        if (uri != null) {
            runCatching {
                val flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                vm.appContext().contentResolver.takePersistableUriPermission(uri, flags)
            }.onFailure {
                FmLog.warn("FmMainDialogs", "takePersistableUriPermission failed for $uri", it)
            }
            when (intent) {
                DialogIntent.CompressOutputPick -> vm.onCompressOutputPicked(uri)
                DialogIntent.ExtractOutputPick -> vm.onExtractOutputPicked(uri)
                DialogIntent.ImportDir -> vm.onImportDir(uri)
                else -> Unit
            }
        } else {
            when (intent) {
                DialogIntent.CompressOutputPick -> vm.onCompressOutputPickedCancelled()
                DialogIntent.ExtractOutputPick -> vm.onExtractOutputPickedCancelled()
                DialogIntent.ImportDir -> vm.onImportCancelled()
                else -> Unit
            }
        }
    }

    // 处理 SAF 导入文件
    val safFilesLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val data = result.data
        if (data != null) {
            val uris = ArrayList<Uri>()
            val clipData = data.clipData
            if (clipData != null) {
                for (i in 0 until clipData.itemCount) {
                    clipData.getItemAt(i)?.uri?.let { uris.add(it) }
                }
            } else {
                data.data?.let { uris.add(it) }
            }
            grantReadPermissions(uris)
            vm.onImportFiles(uris)
        } else {
            vm.onImportCancelled()
        }
    }
    LaunchedEffect(intent) {
        when (intent) {
            DialogIntent.CompressOutputPick,
            DialogIntent.ExtractOutputPick,
            DialogIntent.ImportDir -> {
                val pickIntent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
                    addFlags(
                        Intent.FLAG_GRANT_READ_URI_PERMISSION or
                            Intent.FLAG_GRANT_WRITE_URI_PERMISSION or
                            Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
                    )
                }
                safDirLauncher.launch(pickIntent)
            }
            DialogIntent.ImportFiles -> {
                val pickIntent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "*/*"
                    putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                safFilesLauncher.launch(pickIntent)
            }
            else -> Unit
        }
    }

    when (intent) {
        is DialogIntent.Search -> FmSearchSetupDialog(
            initialKeyword = searchUi.lastKeyword,
            onDismiss = { vm.dismissDialog() },
            onSearch = { keyword, caseSensitive -> vm.submitSearch(keyword, caseSensitive) }
        )

        is DialogIntent.SearchTask -> FmSearchTaskDialog(
            searchUi = searchUi,
            onCancel = { vm.cancelCurrentTask() }
        )

        is DialogIntent.SearchResult -> FmSearchResultDialog(
            searchUi = searchUi,
            onDismiss = { vm.dismissDialog() },
            onClear = { vm.clearSearch() },
            onResumeSearch = { vm.backToSearchSetup() },
            onSelect = { hit -> vm.navigateToSearchHit(hit) }
        )

        is DialogIntent.CompressSetup -> FmCompressDialog(
            defaultName = intent.defaultName,
            onDismiss = { vm.dismissDialog() },
            onConfirm = { name, options -> vm.onCompressSetupConfirmed(name, intent.sources, options) }
        )

        DialogIntent.CompressOutputChoice -> FmCompressOutputChoiceDialog(
            onDismiss = { vm.dismissDialog() },
            onCurrentDir = { vm.onCompressOutputChoiceCurrent() },
            onSaf = { vm.onCompressOutputChoiceSaf() }
        )

        DialogIntent.CompressOutputPick -> {
            // SAF 选择器已由 LaunchedEffect 启动
        }

        is DialogIntent.CompressConflict -> FmConflictDialog(
            conflictName = intent.fileName,
            totalConflicts = 1,
            onSkip = { vm.resolveCompressConflict(ConflictResolution.SKIP) },
            onOverwrite = { vm.resolveCompressConflict(ConflictResolution.OVERWRITE) },
            onKeepBoth = { vm.resolveCompressConflict(ConflictResolution.KEEP_BOTH) }
        )

        is DialogIntent.ExtractSetup -> FmExtractDialog(
            archiveName = intent.archiveName,
            onDismiss = { vm.dismissDialog() },
            onConfirm = { independent -> vm.onExtractSetupConfirmed(independent) }
        )

        is DialogIntent.ExtractPassword -> FmExtractPasswordDialog(
            errorText = intent.errorText,
            onDismiss = { vm.dismissDialog() },
            onConfirm = { password -> vm.onExtractPasswordConfirmed(password) }
        )

        DialogIntent.ExtractOutputChoice -> FmCompressOutputChoiceDialog(
            onDismiss = { vm.dismissDialog() },
            onCurrentDir = { vm.onExtractOutputChoiceCurrent() },
            onSaf = { vm.onExtractOutputChoiceSaf() }
        )

        DialogIntent.ExtractOutputPick -> {
            // SAF 选择器已由 LaunchedEffect 启动
        }

        is DialogIntent.ExtractConflict -> FmConflictDialog(
            conflictName = intent.name,
            totalConflicts = 1,
            onSkip = { vm.resolveExtractConflict(ConflictResolution.SKIP) },
            onOverwrite = { vm.resolveExtractConflict(ConflictResolution.OVERWRITE) },
            onKeepBoth = { vm.resolveExtractConflict(ConflictResolution.KEEP_BOTH) }
        )

        DialogIntent.ImportFiles -> {
            // SAF 选择器已由 LaunchedEffect 启动
        }
        DialogIntent.ImportDir -> {
            // SAF 选择器已由 LaunchedEffect 启动
        }

        is DialogIntent.PasteConflict -> {
            val req = intent.request
            val idx = intent.currentIndex
            val conflict = req.conflicts.getOrNull(idx)
            if (conflict == null) {
                vm.dismissDialog()
            } else {
                FmConflictDialog(
                    conflictName = conflict.source.fileName?.toString() ?: "",
                    totalConflicts = req.conflicts.count { it != null },
                    onSkip = { vm.resolvePasteConflict(ConflictResolution.SKIP) },
                    onOverwrite = { vm.resolvePasteConflict(ConflictResolution.OVERWRITE) },
                    onKeepBoth = { vm.resolvePasteConflict(ConflictResolution.KEEP_BOTH) }
                )
            }
        }

        is DialogIntent.TrashRestoreConflict -> {
            // 由回收站页分派
        }

        else -> {}
    }

    when (operation) {
        FmOperation.None -> Unit

        FmOperation.Create -> FmCreateDialog(
            onDismiss = { updateOperation(FmOperation.None) },
            onConfirmFile = { name ->
                vm.submitCreate(name, isFolder = false) { success ->
                    if (success) updateOperation(FmOperation.None)
                }
            },
            onConfirmFolder = { name ->
                vm.submitCreate(name, isFolder = true) { success ->
                    if (success) updateOperation(FmOperation.None)
                }
            }
        )

        is FmOperation.Jump -> FmJumpDialog(
            currentPath = operation.currentPath,
            onDismiss = { updateOperation(FmOperation.None) },
            onConfirm = { target ->
                updateOperation(FmOperation.None)
                vm.submitJump(target)
            }
        )

        is FmOperation.Rename -> FmRenameDialog(
            entry = operation.entry,
            initialName = operation.entry.name,
            isFile = operation.entry.isDirectory.not(),
            onDismiss = { updateOperation(FmOperation.None) },
            onConfirm = { newName ->
                vm.submitRename(operation.entry, newName) {
                    updateOperation(FmOperation.None)
                }
            },
            validate = { _, candidate -> vm.validateRename(operation.entry, candidate) }
        )

        is FmOperation.Property -> {
            val entry = operation.entry
            if (entry.isDirectory) {
                // 启动异步扫描，对话框关闭时停止
                LaunchedEffect(entry.path) {
                    vm.startDirectoryScan(entry.path)
                }
            }
            DisposableEffect(entry.path) {
                onDispose { vm.stopDirectoryScan() }
            }
            val dirScan by vm.dirScan.collectAsStateWithLifecycle()
            FmPropertyDialog(
                name = entry.name,
                path = entry.path.toString(),
                isDirectory = entry.isDirectory,
                sizeText = formatFileSize(entry.size),
                modifiedText = formatDate(entry.modifiedMs),
                dirScan = if (entry.isDirectory) dirScan else null,
                onDismiss = { updateOperation(FmOperation.None) }
            )
        }

        is FmOperation.DeleteConfirm -> FmDeleteConfirmDialog(
            count = operation.count,
            onDismiss = {
                vm.cancelStagedDelete()
                updateOperation(FmOperation.None)
            },
            onConfirm = { toTrash ->
                vm.deleteSelected(toTrash)
                updateOperation(FmOperation.None)
            }
        )

        FmOperation.BulkActions -> FmBulkActionsDialog(
            selectedCount = uiState.selection.size,
            onDismiss = { updateOperation(FmOperation.None) },
            onCopy = {
                vm.bulkCopy()
                updateOperation(FmOperation.None)
            },
            onCut = {
                vm.bulkCut()
                updateOperation(FmOperation.None)
            },
            onDelete = {
                // 批量对话框先关闭，再弹删除确认
                updateOperation(FmOperation.DeleteConfirm(uiState.selection.size))
            },
            onCompress = {
                vm.bulkCompress()
                updateOperation(FmOperation.None)
            }
        )

        FmOperation.RangeSelectHelp -> FmAlertDialog(
            title = stringResource(R.string.fm_range_select_help),
            text = stringResource(R.string.fm_range_select_help_text),
            onDismiss = { updateOperation(FmOperation.None) }
        )

        is FmOperation.EditConfirm -> FmAlertDialog(
            title = stringResource(R.string.fm_edit_unknown_title),
            text = stringResource(R.string.fm_edit_unknown_message),
            onConfirm = {
                updateOperation(FmOperation.None)
                onOpenEditor(operation.entry.path)
            },
            onDismiss = { updateOperation(FmOperation.None) }
        )
    }
}

@Composable
private fun EmptyBox(text: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = fmSecondaryTextColor(),
            textAlign = TextAlign.Center
        )
    }
}
