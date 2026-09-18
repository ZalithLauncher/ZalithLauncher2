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

package com.movtery.zalithlauncher.ui.screens.content.versions

import android.content.ContentResolver
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil3.compose.AsyncImage
import com.movtery.zalithlauncher.BuildKeys
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.game.version.installed.Version
import com.movtery.zalithlauncher.game.version.installed.VersionFolders
import com.movtery.zalithlauncher.ui.androidText
import com.movtery.zalithlauncher.ui.base.BaseScreen
import com.movtery.zalithlauncher.ui.components.CardTitleLayout
import com.movtery.zalithlauncher.ui.components.EdgeDirection
import com.movtery.zalithlauncher.ui.components.ScalingLabel
import com.movtery.zalithlauncher.ui.components.SimpleAlertDialog
import com.movtery.zalithlauncher.ui.components.SimpleTextInputField
import com.movtery.zalithlauncher.ui.components.fadeEdge
import com.movtery.zalithlauncher.ui.screens.NestedNavKey
import com.movtery.zalithlauncher.ui.screens.NormalNavKey
import com.movtery.zalithlauncher.ui.screens.TitledNavKey
import com.movtery.zalithlauncher.ui.screens.content.elements.SortByDropdownMenu
import com.movtery.zalithlauncher.ui.screens.content.elements.SortByEnum
import com.movtery.zalithlauncher.ui.screens.content.versions.elements.DeleteAllOperation
import com.movtery.zalithlauncher.ui.screens.content.versions.elements.LoadingState
import com.movtery.zalithlauncher.ui.screens.content.versions.elements.MinecraftColorTextNormal
import com.movtery.zalithlauncher.ui.screens.content.versions.layouts.VersionChunkBackground
import com.movtery.zalithlauncher.ui.theme.itemColor
import com.movtery.zalithlauncher.ui.theme.onItemColor
import com.movtery.zalithlauncher.utils.animation.getAnimateTween
import com.movtery.zalithlauncher.utils.animation.swapAnimateDpAsState
import com.movtery.zalithlauncher.utils.logging.Logger
import com.movtery.zalithlauncher.utils.string.getMessageOrToString
import com.movtery.zalithlauncher.viewmodel.ErrorViewModel
import com.movtery.zalithlauncher.viewmodel.EventViewModel
import com.movtery.zalithlauncher.viewmodel.sendToast
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import javax.inject.Inject

private const val TAG = "ScreenshotsManager"

/**
 * 游戏内截图信息
 */
data class ScreenshotInfo(
    val file: File,
    val name: String = file.nameWithoutExtension,
    val lastModified: Long = file.lastModified(),
    val size: Long = file.length()
)

data class ScreenshotFilter(val filterName: String)

sealed interface ExportOperation {
    data object None : ExportOperation
    data object Ask : ExportOperation
}

@HiltViewModel
class ScreenshotsManageViewModel @Inject constructor(
    @param:ApplicationContext private val context: Context
) : ViewModel() {

    private var screenshotDir: File? = null

    var filter by mutableStateOf(ScreenshotFilter(""))
        private set

    var allScreenshots by mutableStateOf<List<ScreenshotInfo>>(emptyList())
        private set
    var filteredScreenshots by mutableStateOf<List<ScreenshotInfo>?>(null)
        private set
    var sortByEnum by mutableStateOf(SortByEnum.Name)
        private set
    var isAscending by mutableStateOf(false)
        private set
    var listState by mutableStateOf<LoadingState>(LoadingState.None)
        private set

    val selectedShots = mutableStateListOf<ScreenshotInfo>()

    var deleteAllOperation by mutableStateOf<DeleteAllOperation>(DeleteAllOperation.None)
    var exportOperation by mutableStateOf<ExportOperation>(ExportOperation.None)

    /**
     * 初始化截图文件夹
     */
    fun initDirectory(dir: File) {
        if (screenshotDir != dir) {
            screenshotDir = dir
            refresh()
        }
    }

    /**
     * 全选当前已过滤的结果
     */
    fun selectAllFiles() {
        filteredScreenshots?.forEach { shot ->
            if (!selectedShots.contains(shot)) selectedShots.add(shot)
        }
    }

    /**
     * 取消选择当前已过滤的结果
     */
    fun clearSelected() {
        filteredScreenshots?.let {
            selectedShots.removeAll(it)
        }
    }

    /**
     * 发起删除选择的截图的请求，先警告用户
     */
    fun requestDeleteSelected() {
        if (deleteAllOperation == DeleteAllOperation.None && selectedShots.isNotEmpty()) {
            deleteAllOperation = DeleteAllOperation.Warning(selectedShots.map { it.file })
        }
    }

    private var refreshJob: Job? = null

    fun refresh() {
        val dir = screenshotDir ?: return

        refreshJob = viewModelScope.launch {
            listState = LoadingState.Loading
            selectedShots.clear()

            withContext(Dispatchers.IO) {
                val tempList = mutableListOf<ScreenshotInfo>()
                try {
                    if (dir.exists() && dir.isDirectory) {
                        dir.listFiles { file ->
                            file.isFile && file.extension.lowercase() == "png"
                        }?.forEach { file ->
                            ensureActive()
                            tempList.add(ScreenshotInfo(file))
                        }
                    }
                } catch (_: CancellationException) {
                    return@withContext
                }
                allScreenshots = tempList
                applyFilterAndSort()
            }

            listState = LoadingState.None
            refreshJob = null
        }
    }

    private var exportsJob: Job? = null

    fun exports(
        onSuccess: suspend () -> Unit,
        onFailed: suspend (e: Exception) -> Unit
    ) {
        val infos = selectedShots.takeIf { it.isNotEmpty() } ?: allScreenshots
        exportsJob = viewModelScope.launch(Dispatchers.IO) {
            withContext(Dispatchers.Main) {
                exportOperation = ExportOperation.None
            }

            try {
                val resolver = context.contentResolver
                for (info in infos) {
                    exportSingleImage(resolver, info.file)
                }
                onSuccess()
            } catch (e: Exception) {
                Logger.error(TAG, "Failed to export screenshots!", e)
                onFailed(e)
            }
        }
    }

    /**
     * 将单个截图导出到公共目录，如果已存在，则覆盖目标截图
     */
    private fun exportSingleImage(
        resolver: ContentResolver,
        file: File
    ) {
        val fileName = file.name
        val relativePath = Environment.DIRECTORY_PICTURES + "/" + BuildKeys.LAUNCHER_IDENTIFIER + "/"

        //如果是已存在的文件，则Uri不为null
        val existingUri = queryExistingUri(resolver, fileName, relativePath)
        val isNewFile = (existingUri == null)

        val uri = if (isNewFile) {
            val contentValues = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
                put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.Images.Media.RELATIVE_PATH, relativePath)
                    put(MediaStore.Images.Media.IS_PENDING, 1)
                }
            }
            resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                ?: throw IOException("Failed to insert new MediaStore entry for $fileName")
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val pendingValues = ContentValues().apply {
                    put(MediaStore.Images.Media.IS_PENDING, 1)
                }
                resolver.update(existingUri, pendingValues, null, null)
            }
            existingUri
        }

        try {
            resolver.openOutputStream(uri)?.use { out ->
                file.inputStream().use { input ->
                    input.copyTo(out)
                }
            } ?: throw IOException("Failed to open output stream for $fileName")
        } catch (e: Exception) {
            //写入失败时清理 pending 状态
            if (isNewFile && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                resolver.delete(uri, null, null)
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val clearPending = ContentValues().apply {
                    put(MediaStore.Images.Media.IS_PENDING, 0)
                }
                resolver.update(uri, clearPending, null, null)
            }
            throw IOException("Failed to write image $fileName", e)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val finalValues = ContentValues().apply {
                put(MediaStore.Images.Media.IS_PENDING, 0)
            }
            resolver.update(uri, finalValues, null, null)
        }
    }

    private fun queryExistingUri(
        resolver: ContentResolver,
        fileName: String,
        relativePath: String
    ): Uri? {
        val projection = arrayOf(MediaStore.Images.Media._ID)
        val selection = "${MediaStore.Images.Media.DISPLAY_NAME} = ? AND ${MediaStore.Images.Media.RELATIVE_PATH} = ?"
        val selectionArgs = arrayOf(fileName, relativePath)

        resolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            null
        )?.use { cursor ->
            if (cursor.moveToFirst()) {
                val id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID))
                return ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
            }
        }
        return null
    }

    init {
        refresh()
    }

    fun updateFilter(newFilter: ScreenshotFilter) {
        this.filter = newFilter
        applyFilterAndSort()
    }

    fun updateSortBy(sortByEnum: SortByEnum) {
        this.sortByEnum = sortByEnum
        applyFilterAndSort()
    }

    fun updateSortOrder() {
        this.isAscending = !this.isAscending
        applyFilterAndSort()
    }

    val supportedSortByEnums = listOf(
        SortByEnum.Name, SortByEnum.FileModifiedTime
    )

    private fun applyFilterAndSort() {
        filteredScreenshots = allScreenshots
            .takeIf { it.isNotEmpty() }
            ?.filter { info ->
                if (filter.filterName.isBlank()) true
                else info.name.contains(filter.filterName, ignoreCase = true)
            }
            ?.sortedWith { o1, o2 ->
                val value = when (sortByEnum) {
                    SortByEnum.Name -> o1.name.compareTo(o2.name)
                    SortByEnum.FileModifiedTime -> o1.lastModified.compareTo(o2.lastModified)
                    else -> error("This sorting method is not supported: $sortByEnum")
                }
                if (isAscending) value else -value
            }
    }

    override fun onCleared() {
        refreshJob?.cancel()
        refreshJob = null
        exportsJob?.cancel()
        exportsJob = null
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ScreenshotsManagerScreen(
    mainScreenKey: TitledNavKey?,
    versionsScreenKey: TitledNavKey?,
    version: Version,
    backToMainScreen: () -> Unit,
    eventViewModel: EventViewModel,
    submitError: (ErrorViewModel.ThrowableMessage) -> Unit
) {
    if (!version.isValid()) {
        backToMainScreen()
        return
    }

    val screenshotDir = remember(version) {
        VersionFolders.SCREENSHOTS.getDir(version.getGameDir())
    }

    BaseScreen(
        levels1 = listOf(
            Pair(NestedNavKey.VersionSettings::class.java, mainScreenKey)
        ),
        Triple(NormalNavKey.Versions.ScreenshotsManager, versionsScreenKey, false)
    ) { isVisible ->
        val viewModel: ScreenshotsManageViewModel = hiltViewModel(
            key = version.toString() + "_" + VersionFolders.SCREENSHOTS.folderName
        )

        LaunchedEffect(screenshotDir) {
            //初始化截图文件夹
            viewModel.initDirectory(screenshotDir)
        }

        DeleteAllOperation(
            operation = viewModel.deleteAllOperation,
            changeOperation = { viewModel.deleteAllOperation = it },
            submitError = submitError,
            onRefresh = { viewModel.refresh() }
        )

        ExportDialogHandler(
            operation = viewModel.exportOperation,
            updateOperation = { viewModel.exportOperation = it },
            selectedShots = viewModel.selectedShots,
            onExport = {
                viewModel.exports(
                    onSuccess = {
                        withContext(Dispatchers.Main) {
                            eventViewModel.sendToast(androidText(R.string.generic_saved))
                        }
                    },
                    onFailed = { e ->
                        submitError(
                            ErrorViewModel.ThrowableMessage(
                                title = androidText(R.string.screenshots_manage_export_failed),
                                message = androidText(e.getMessageOrToString())
                            )
                        )
                    }
                )
            }
        )

        val yOffset by swapAnimateDpAsState(
            targetValue = (-40).dp,
            swapIn = isVisible
        )

        VersionChunkBackground(
            modifier = Modifier
                .fillMaxSize()
                .padding(all = 12.dp)
                .offset { IntOffset(x = 0, y = yOffset.roundToPx()) },
            paddingValues = PaddingValues()
        ) {
            when (viewModel.listState) {
                is LoadingState.None -> {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Column(modifier = Modifier.fillMaxSize()) {
                            ScreenshotHeader(
                                modifier = Modifier.fillMaxWidth(),
                                filter = viewModel.filter,
                                changeFilter = { viewModel.updateFilter(it) },
                                supportedSortByEnums = viewModel.supportedSortByEnums,
                                sortByEnum = viewModel.sortByEnum,
                                onSortByChanged = { viewModel.updateSortBy(it) },
                                isAscending = viewModel.isAscending,
                                onToggleSortOrder = { viewModel.updateSortOrder() },
                                onDeleteAll = { viewModel.requestDeleteSelected() },
                                isFilesSelected = viewModel.selectedShots.isNotEmpty(),
                                onSelectAll = { viewModel.selectAllFiles() },
                                onClearFilesSelected = { viewModel.clearSelected() },
                                onRefresh = { viewModel.refresh() }
                            )

                            ScreenshotGrid(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                list = viewModel.filteredScreenshots,
                                selected = viewModel.selectedShots,
                                removeFromSelected = { viewModel.selectedShots.remove(it) },
                                addToSelected = { viewModel.selectedShots.add(it) }
                            )
                        }

                        //导出图片悬浮按钮
                        if (viewModel.allScreenshots.isNotEmpty()) {
                            FloatingActionButton(
                                onClick = {
                                    if (viewModel.listState != LoadingState.Loading) {
                                        viewModel.exportOperation = ExportOperation.Ask
                                    }
                                },
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(16.dp),
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_file_export_outlined),
                                    contentDescription = stringResource(R.string.screenshots_manage_export)
                                )
                            }
                        }
                    }
                }

                is LoadingState.Loading -> {
                    Box(
                        Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        LoadingIndicator()
                    }
                }
            }
        }
    }
}

@Composable
private fun ExportDialogHandler(
    operation: ExportOperation,
    updateOperation: (ExportOperation) -> Unit,
    selectedShots: List<ScreenshotInfo>,
    onExport: () -> Unit
) {
    when (operation) {
        is ExportOperation.None -> {}
        is ExportOperation.Ask -> {
            val isAll = selectedShots.isEmpty()
            SimpleAlertDialog(
                title = stringResource(R.string.screenshots_manage_export_title),
                text = {
                    Text(
                        text = if (isAll) {
                            stringResource(R.string.screenshots_manage_export_all_message)
                        } else {
                            stringResource(
                                R.string.screenshots_manage_export_selected_message,
                                selectedShots.size
                            )
                        }
                    )
                },
                confirmText = stringResource(R.string.screenshots_manage_export),
                dismissText = stringResource(R.string.generic_cancel),
                onConfirm = onExport,
                onCancel = { updateOperation(ExportOperation.None) },
                onDismissRequest = { updateOperation(ExportOperation.None) }
            )
        }
    }
}

@Composable
private fun ScreenshotHeader(
    modifier: Modifier = Modifier,
    filter: ScreenshotFilter,
    changeFilter: (ScreenshotFilter) -> Unit,
    supportedSortByEnums: List<SortByEnum>,
    sortByEnum: SortByEnum,
    onSortByChanged: (SortByEnum) -> Unit,
    isAscending: Boolean,
    onToggleSortOrder: () -> Unit,
    onDeleteAll: () -> Unit,
    isFilesSelected: Boolean,
    onSelectAll: () -> Unit,
    onClearFilesSelected: () -> Unit,
    onRefresh: () -> Unit,
    inputFieldColor: Color = itemColor(),
    inputFieldContentColor: Color = onItemColor()
) {
    CardTitleLayout(modifier = modifier) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
                .padding(top = 4.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box {
                    var expanded by remember { mutableStateOf(false) }
                    IconButton(onClick = { expanded = !expanded }) {
                        Icon(
                            painter = painterResource(R.drawable.ic_sort),
                            contentDescription = stringResource(R.string.sort_by)
                        )
                    }
                    SortByDropdownMenu(
                        expanded = expanded,
                        onClose = { expanded = false },
                        enums = supportedSortByEnums,
                        currentEnum = sortByEnum,
                        onEnumChanged = onSortByChanged,
                        isAscending = isAscending,
                        onToggleSortOrder = onToggleSortOrder
                    )
                }

                SimpleTextInputField(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 4.dp),
                    value = filter.filterName,
                    onValueChange = { changeFilter(ScreenshotFilter(it)) },
                    hint = {
                        Text(
                            text = stringResource(R.string.generic_search),
                            style = TextStyle(color = LocalContentColor.current).copy(fontSize = 12.sp)
                        )
                    },
                    color = inputFieldColor,
                    contentColor = inputFieldContentColor,
                    singleLine = true
                )

                val scrollState = rememberScrollState()
                AnimatedVisibility(
                    modifier = Modifier.height(IntrinsicSize.Min),
                    visible = isFilesSelected
                ) {
                    Row(
                        modifier = Modifier
                            .fadeEdge(
                                state = scrollState,
                                length = 32.dp,
                                direction = EdgeDirection.Horizontal
                            )
                            .widthIn(max = this@BoxWithConstraints.maxWidth / 2)
                            .horizontalScroll(scrollState),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onDeleteAll) {
                            Icon(
                                painterResource(R.drawable.ic_delete_outlined),
                                contentDescription = null
                            )
                        }
                        IconButton(onClick = onSelectAll) {
                            Icon(
                                painterResource(R.drawable.ic_select_all),
                                contentDescription = null
                            )
                        }
                        IconButton(onClick = { if (isFilesSelected) onClearFilesSelected() }) {
                            Icon(painterResource(R.drawable.ic_deselect), contentDescription = null)
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        VerticalDivider(
                            modifier = Modifier
                                .fillMaxHeight()
                                .padding(vertical = 12.dp),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                }

                IconButton(onClick = onRefresh) {
                    Icon(
                        painter = painterResource(R.drawable.ic_refresh),
                        contentDescription = stringResource(R.string.generic_refresh)
                    )
                }
            }
        }
    }
}

@Composable
private fun ScreenshotGrid(
    modifier: Modifier = Modifier,
    list: List<ScreenshotInfo>?,
    selected: List<ScreenshotInfo>,
    removeFromSelected: (ScreenshotInfo) -> Unit,
    addToSelected: (ScreenshotInfo) -> Unit
) {
    list?.let { items ->
        if (items.isNotEmpty()) {
            val context = LocalContext.current
            val isSelectionMode = selected.isNotEmpty()

            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 160.dp),
                modifier = modifier,
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(items) { info ->
                    ScreenshotItemLayout(
                        info = info,
                        selected = selected.contains(info),
                        isSelectionMode = isSelectionMode,
                        onToggleSelect = {
                            if (selected.contains(info)) {
                                removeFromSelected(info)
                            } else {
                                addToSelected(info)
                            }
                        },
                        onOpen = {
                            try {
                                // 唤起系统看图软件打开该图片
                                val uri = FileProvider.getUriForFile(
                                    context,
                                    "${context.packageName}.provider",
                                    info.file
                                )
                                val intent = Intent(Intent.ACTION_VIEW).apply {
                                    setDataAndType(uri, "image/png")
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                Logger.error(TAG, "Failed to open image", e)
                            }
                        }
                    )
                }
            }
        } else {
            Box(modifier = Modifier.fillMaxSize()) {
                ScalingLabel(
                    modifier = Modifier.align(Alignment.Center),
                    text = stringResource(R.string.generic_no_matching_items)
                )
            }
        }
    } ?: run {
        Box(modifier = Modifier.fillMaxSize()) {
            ScalingLabel(
                modifier = Modifier.align(Alignment.Center),
                text = stringResource(R.string.screenshots_manage_no_screenshots)
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ScreenshotItemLayout(
    modifier: Modifier = Modifier,
    info: ScreenshotInfo,
    selected: Boolean,
    isSelectionMode: Boolean,
    onToggleSelect: () -> Unit,
    onOpen: () -> Unit,
    itemColor: Color = itemColor(),
    itemContentColor: Color = onItemColor(),
    borderColor: Color = MaterialTheme.colorScheme.primary,
    shape: Shape = MaterialTheme.shapes.large,
) {
    val borderWidth by animateDpAsState(if (selected) 2.dp else (-1).dp)
    val scale = remember { Animatable(initialValue = 0.95f) }

    LaunchedEffect(Unit) {
        scale.animateTo(targetValue = 1f, animationSpec = getAnimateTween())
    }

    Surface(
        modifier = modifier
            .graphicsLayer(scaleY = scale.value, scaleX = scale.value)
            .border(width = borderWidth, color = borderColor, shape = shape),
        shape = shape,
        color = itemColor,
        contentColor = itemContentColor,
    ) {
        Box(
            modifier = Modifier
                .aspectRatio(16f / 9f)
                .combinedClickable(
                    onClick = {
                        if (isSelectionMode) {
                            onToggleSelect()
                        } else {
                            onOpen()
                        }
                    },
                    onLongClick = {
                        if (!isSelectionMode) {
                            onToggleSelect()
                        }
                    }
                )
        ) {
            AsyncImage(
                model = info.file,
                contentDescription = info.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.6f))
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            ) {
                MinecraftColorTextNormal(
                    modifier = Modifier.basicMarquee(iterations = Int.MAX_VALUE),
                    inputText = info.name,
                    style = MaterialTheme.typography.labelMedium.copy(color = Color.White),
                    maxLines = 1
                )
            }
        }
    }
}