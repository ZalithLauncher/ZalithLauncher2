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

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.nonInteractiveScrollbar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.coroutine.TaskSystem
import com.movtery.zalithlauncher.game.version.installed.Version
import com.movtery.zalithlauncher.game.version.installed.VersionFolders
import com.movtery.zalithlauncher.ui.base.BaseScreen
import com.movtery.zalithlauncher.ui.components.CardTitleLayout
import com.movtery.zalithlauncher.ui.components.EdgeDirection
import com.movtery.zalithlauncher.ui.components.IconTextButton
import com.movtery.zalithlauncher.ui.components.ProgressDialog
import com.movtery.zalithlauncher.ui.components.ScalingLabel
import com.movtery.zalithlauncher.ui.components.SimpleAlertDialog
import com.movtery.zalithlauncher.ui.components.SimpleTextInputField
import com.movtery.zalithlauncher.ui.components.fadeEdge
import com.movtery.zalithlauncher.ui.screens.NestedNavKey
import com.movtery.zalithlauncher.ui.screens.NormalNavKey
import com.movtery.zalithlauncher.ui.screens.TitledNavKey
import com.movtery.zalithlauncher.ui.screens.content.elements.ImportMultipleFileButton
import com.movtery.zalithlauncher.ui.screens.content.elements.SortByDropdownMenu
import com.movtery.zalithlauncher.ui.screens.content.elements.SortByEnum
import com.movtery.zalithlauncher.ui.screens.content.elements.rememberMultipleUriImportTaskBuilder
import com.movtery.zalithlauncher.ui.screens.content.versions.elements.DeleteAllOperation
import com.movtery.zalithlauncher.ui.screens.content.versions.elements.FileNameInputDialog
import com.movtery.zalithlauncher.ui.screens.content.versions.elements.LoadingState
import com.movtery.zalithlauncher.ui.screens.content.versions.elements.ShaderOperation
import com.movtery.zalithlauncher.ui.screens.content.versions.elements.ShaderPackInfo
import com.movtery.zalithlauncher.ui.screens.content.versions.elements.filterShaders
import com.movtery.zalithlauncher.ui.screens.content.versions.layouts.VersionChunkBackground
import com.movtery.zalithlauncher.ui.theme.itemColor
import com.movtery.zalithlauncher.ui.theme.onItemColor
import com.movtery.zalithlauncher.utils.animation.getAnimateTween
import com.movtery.zalithlauncher.utils.animation.swapAnimateDpAsState
import com.movtery.zalithlauncher.utils.file.FolderFileCounter
import com.movtery.zalithlauncher.utils.file.formatFileSize
import com.movtery.zalithlauncher.viewmodel.ErrorViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.apache.commons.io.FileUtils
import java.io.File

private class ShadersManageViewModel(
    val shadersDir: File
) : ViewModel() {
    var nameFilter by mutableStateOf("")

    var allShaders by mutableStateOf<List<ShaderPackInfo>>(emptyList())
        private set
    var filteredShaders by mutableStateOf<List<ShaderPackInfo>?>(null)
        private set
    var sortByEnum by mutableStateOf(SortByEnum.FileName)
        private set
    var isAscending by mutableStateOf(true)
        private set

    var shadersState by mutableStateOf<LoadingState>(LoadingState.None)
        private set

    /**
     * 已选择的文件
     */
    val selectedPacks = mutableStateListOf<ShaderPackInfo>()

    /**
     * 删除所有已选择文件的操作流程
     */
    var deleteAllOperation by mutableStateOf<DeleteAllOperation>(DeleteAllOperation.None)

    /** 临时记录的光影包数量 */
    private var packCount = FolderFileCounter(shadersDir)

    /**
     * 全选所有文件
     */
    fun selectAllFiles() {
        filteredShaders?.forEach { pack ->
            if (!selectedPacks.contains(pack)) selectedPacks.add(pack)
        }
    }

    fun clearSelected() {
        filteredShaders?.let {
            selectedPacks.removeAll(it)
        }
    }

    private var job: Job? = null
    /**
     * @param checkCount 刷新目录内文件数量记录
     */
    fun refresh(
        checkCount: Boolean = true
    ) {
        job = viewModelScope.launch {
            shadersState = LoadingState.Loading
            selectedPacks.clear()
            if (checkCount) packCount.checkDir()

            withContext(Dispatchers.IO) {
                try {
                    val list = shadersDir.listFiles()?.filter {
                        //光影包只能是后缀为.zip的压缩包
                        it.isFile && it.extension.equals("zip", true)
                    }?.map { file ->
                        ensureActive()
                        ShaderPackInfo(
                            file = file,
                            fileSize = FileUtils.sizeOf(file)
                        )
                    } ?: emptyList()
                    allShaders = list.sortedBy { it.file.name }
                    filterShaders()
                } catch (_: CancellationException) {
                    return@withContext
                }
            }

            shadersState = LoadingState.None
            job = null
        }
    }

    fun checkCountAndRefresh() {
        val isUnchecked = packCount.isUnchecked()
        if (packCount.checkDir() && !isUnchecked && job == null) {
            refresh(checkCount = false)
        }
    }

    init {
        refresh(checkCount = false)
    }

    fun updateFilter(name: String) {
        this.nameFilter = name
        filterShaders()
    }

    fun updateSortBy(sortByEnum: SortByEnum) {
        this.sortByEnum = sortByEnum
        filterShaders()
    }

    fun updateSortOrder() {
        this.isAscending = !this.isAscending
        filterShaders()
    }

    val supportedSortByEnums = listOf(
        SortByEnum.FileName, SortByEnum.FileModifiedTime
    )

    private fun filterShaders() {
        filteredShaders = allShaders
            .takeIf { it.isNotEmpty() }
            ?.filterShaders(nameFilter)
            ?.sortedWith { o1, o2 ->
                val file1 = o1.file
                val file2 = o2.file
                val value = when (sortByEnum) {
                    SortByEnum.FileName -> file1.name.compareTo(file2.name)
                    SortByEnum.FileModifiedTime -> file2.lastModified().compareTo(file1.lastModified())
                    else -> error("This sorting method is not supported: $sortByEnum")
                }
                if (isAscending) {
                    value
                } else {
                    -value
                }
            }
    }
}

@Composable
private fun rememberShadersManageViewModel(
    shadersDir: File,
    version: Version
) = viewModel(
    key = version.toString() + "_" + VersionFolders.SHADERS.folderName
) {
    ShadersManageViewModel(shadersDir)
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ShadersManagerScreen(
    mainScreenKey: TitledNavKey?,
    versionsScreenKey: TitledNavKey?,
    version: Version,
    backToMainScreen: () -> Unit,
    swapToDownload: () -> Unit,
    submitError: (ErrorViewModel.ThrowableMessage) -> Unit
) {
    if (!version.isValid()) {
        backToMainScreen()
        return
    }

    val shadersDir = remember(version) {
        VersionFolders.SHADERS.getDir(version.getGameDir())
    }

    BaseScreen(
        levels1 = listOf(
            Pair(NestedNavKey.VersionSettings::class.java, mainScreenKey)
        ),
        Triple(NormalNavKey.Versions.ShadersManager, versionsScreenKey, false),
    ) { isVisible ->
        val viewModel = rememberShadersManageViewModel(shadersDir, version)

        LaunchedEffect(Unit) {
            viewModel.checkCountAndRefresh()
        }

        DeleteAllOperation(
            operation = viewModel.deleteAllOperation,
            changeOperation = { viewModel.deleteAllOperation = it },
            submitError = submitError,
            onRefresh = { viewModel.refresh() }
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
            val operationScope = rememberCoroutineScope()

            when (viewModel.shadersState) {
                LoadingState.None -> {
                    var shaderOperation by remember { mutableStateOf<ShaderOperation>(ShaderOperation.None) }
                    fun runProgress(task: () -> Unit) {
                        operationScope.launch(Dispatchers.IO) {
                            shaderOperation = ShaderOperation.Progress
                            task()
                            shaderOperation = ShaderOperation.None
                            viewModel.refresh()
                        }
                    }
                    ShaderOperation(
                        shaderOperation = shaderOperation,
                        updateOperation = { shaderOperation = it },
                        shadersDir = shadersDir,
                        renameShaderPack = { info, newName ->
                            runProgress {
                                val file = info.file
                                file.renameTo(File(shadersDir, "$newName.${file.extension}"))
                            }
                        },
                        deleteShaderPack = { info ->
                            runProgress {
                                FileUtils.deleteQuietly(info.file)
                            }
                        }
                    )

                    Column {
                        ShadersActionsHeader(
                            modifier = Modifier.fillMaxWidth(),
                            nameFilter = viewModel.nameFilter,
                            onNameFilterChange = { viewModel.updateFilter(it) },
                            supportedSortByEnums = viewModel.supportedSortByEnums,
                            sortByEnum = viewModel.sortByEnum,
                            onSortByChanged = { viewModel.updateSortBy(it) },
                            isAscending = viewModel.isAscending,
                            onToggleSortOrder = { viewModel.updateSortOrder() },
                            shadersDir = shadersDir,
                            onDeleteAll = {
                                val selected = viewModel.selectedPacks
                                if (
                                    viewModel.deleteAllOperation == DeleteAllOperation.None &&
                                    selected.isNotEmpty()
                                ) {
                                    viewModel.deleteAllOperation = DeleteAllOperation.Warning(
                                        files = selected.map { pack ->
                                            pack.file
                                        }
                                    )
                                }
                            },
                            isFilesSelected = viewModel.selectedPacks.isNotEmpty(),
                            onSelectAll = { viewModel.selectAllFiles() },
                            onClearFilesSelected = { viewModel.clearSelected() },
                            swapToDownload = swapToDownload,
                            refresh = { viewModel.refresh() },
                            submitError = submitError
                        )

                        ShadersList(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            shadersList = viewModel.filteredShaders,
                            selectedPacks = viewModel.selectedPacks,
                            removeFromSelected = { viewModel.selectedPacks.remove(it) },
                            addToSelected = { viewModel.selectedPacks.add(it) },
                            updateOperation = { shaderOperation = it }
                        )
                    }
                }
                LoadingState.Loading -> {
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
private fun ShadersActionsHeader(
    modifier: Modifier,
    nameFilter: String,
    onNameFilterChange: (String) -> Unit,
    supportedSortByEnums: List<SortByEnum>,
    sortByEnum: SortByEnum,
    onSortByChanged: (SortByEnum) -> Unit,
    isAscending: Boolean,
    onToggleSortOrder: () -> Unit,
    shadersDir: File,
    onDeleteAll: () -> Unit,
    isFilesSelected: Boolean,
    onSelectAll: () -> Unit,
    onClearFilesSelected: () -> Unit,
    swapToDownload: () -> Unit,
    refresh: () -> Unit,
    submitError: (ErrorViewModel.ThrowableMessage) -> Unit,
    inputFieldColor: Color = itemColor(),
    inputFieldContentColor: Color = onItemColor(),
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
                    IconButton(
                        onClick = { expanded = !expanded }
                    ) {
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
                    value = nameFilter,
                    onValueChange = { onNameFilterChange(it) },
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

                AnimatedVisibility(
                    modifier = Modifier.height(IntrinsicSize.Min),
                    visible = isFilesSelected
                ) {
                    Row {
                        IconButton(
                            onClick = onDeleteAll
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_delete_outlined),
                                contentDescription = null
                            )
                        }

                        IconButton(
                            onClick = onSelectAll
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_select_all),
                                contentDescription = null
                            )
                        }

                        IconButton(
                            onClick = {
                                if (isFilesSelected) onClearFilesSelected()
                            }
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_deselect),
                                contentDescription = null
                            )
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        VerticalDivider(
                            modifier = Modifier
                                .fillMaxHeight()
                                .padding(vertical = 12.dp),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                        )
                    }
                }

                val scrollState = rememberScrollState()
                LaunchedEffect(Unit) {
                    scrollState.scrollTo(scrollState.maxValue)
                }
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
                    Spacer(modifier = Modifier.width(6.dp))

                    val taskBuilder = rememberMultipleUriImportTaskBuilder(
                        id = "ContentManager.Shaders.Import",
                        targetDir = shadersDir,
                        checkExtension = listOf("zip"),
                        submitError = submitError,
                        onImported = refresh
                    )
                    ImportMultipleFileButton(
                        extension = "zip",
                        progressUris = { uris ->
                            TaskSystem.submitTask(
                                taskBuilder(uris)
                            )
                        }
                    )

                    IconTextButton(
                        onClick = swapToDownload,
                        painter = painterResource(R.drawable.ic_download_2_filled),
                        text = stringResource(R.string.generic_download)
                    )

                    IconButton(
                        onClick = refresh
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_refresh),
                            contentDescription = stringResource(R.string.generic_refresh)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ShadersList(
    modifier: Modifier = Modifier,
    shadersList: List<ShaderPackInfo>?,
    selectedPacks: List<ShaderPackInfo>,
    removeFromSelected: (ShaderPackInfo) -> Unit,
    addToSelected: (ShaderPackInfo) -> Unit,
    updateOperation: (ShaderOperation) -> Unit
) {
    shadersList?.let { list ->
        if (list.isNotEmpty()) {
            val scrollState = rememberLazyListState()
            LazyColumn(
                modifier = modifier.nonInteractiveScrollbar(
                    state = scrollState.scrollIndicatorState!!,
                    orientation = Orientation.Vertical,
                ),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                state = scrollState,
            ) {
                items(list) { info ->
                    ShaderPackItem(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        shaderPackInfo = info,
                        selected = selectedPacks.contains(info),
                        onClick = {
                            if (selectedPacks.contains(info)) {
                                removeFromSelected(info)
                            } else {
                                addToSelected(info)
                            }
                        },
                        updateOperation = updateOperation
                    )
                }
            }
        } else {
            //如果列表是空的，则是由搜索导致的
            //展示“无匹配项”文本
            Box(modifier = Modifier.fillMaxSize()) {
                ScalingLabel(
                    modifier = Modifier.align(Alignment.Center),
                    text = stringResource(R.string.generic_no_matching_items)
                )
            }
        }
    } ?: run {
        //如果为null，则代表本身就没有光影包可以展示
        Box(modifier = Modifier.fillMaxSize()) {
            ScalingLabel(
                modifier = Modifier.align(Alignment.Center),
                text = stringResource(R.string.shader_pack_manage_no_packs)
            )
        }
    }
}

@Composable
private fun ShaderPackItem(
    modifier: Modifier = Modifier,
    shaderPackInfo: ShaderPackInfo,
    selected: Boolean,
    onClick: () -> Unit = {},
    updateOperation: (ShaderOperation) -> Unit,
    itemColor: Color = itemColor(),
    itemContentColor: Color = onItemColor(),
    borderColor: Color = MaterialTheme.colorScheme.primary,
    shape: Shape = MaterialTheme.shapes.large,
) {
    val borderWidth by animateDpAsState(
        if (selected) 2.dp
        else (-1).dp
    )

    val scale = remember { Animatable(initialValue = 0.95f) }
    LaunchedEffect(Unit) {
        scale.animateTo(targetValue = 1f, animationSpec = getAnimateTween())
    }

    Surface(
        modifier = modifier
            .graphicsLayer(scaleY = scale.value, scaleX = scale.value)
            .border(
                width = borderWidth,
                color = borderColor,
                shape = shape
            ),
        onClick = onClick,
        shape = shape,
        color = itemColor,
        contentColor = itemContentColor,
    ) {
        Row(
            modifier = Modifier.padding(all = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .weight(1f),
            ) {
                //文件名称
                Text(
                    modifier = Modifier.basicMarquee(iterations = Int.MAX_VALUE),
                    text = shaderPackInfo.file.name,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1
                )
                //文件大小
                Text(
                    modifier = Modifier.alpha(0.7f),
                    text = stringResource(
                        R.string.generic_file_size,
                        formatFileSize(shaderPackInfo.fileSize)
                    ),
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Row(
                modifier = Modifier.align(Alignment.CenterVertically),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ShadersOperationMenu(
                    buttonSize = 38.dp,
                    iconSize = 26.dp,
                    onRenameClick = {
                        updateOperation(ShaderOperation.Rename(shaderPackInfo))
                    },
                    onDeleteClick = {
                        updateOperation(ShaderOperation.Delete(shaderPackInfo))
                    }
                )
            }
        }
    }
}

@Composable
private fun ShadersOperationMenu(
    buttonSize: Dp,
    iconSize: Dp = buttonSize,
    onRenameClick: () -> Unit = {},
    onDeleteClick: () -> Unit = {}
) {
    Row {
        var menuExpanded by remember { mutableStateOf(false) }

        IconButton(
            modifier = Modifier.size(buttonSize),
            onClick = { menuExpanded = !menuExpanded }
        ) {
            Icon(
                modifier = Modifier.size(iconSize),
                painter = painterResource(R.drawable.ic_more_horiz),
                contentDescription = stringResource(R.string.generic_more)
            )
        }

        DropdownMenu(
            expanded = menuExpanded,
            shape = MaterialTheme.shapes.large,
            shadowElevation = 3.dp,
            onDismissRequest = { menuExpanded = false }
        ) {
            DropdownMenuItem(
                text = { Text(text = stringResource(R.string.generic_rename)) },
                leadingIcon = {
                    Icon(
                        modifier = Modifier.size(20.dp),
                        painter = painterResource(R.drawable.ic_edit_filled),
                        contentDescription = stringResource(R.string.generic_rename)
                    )
                },
                onClick = {
                    onRenameClick()
                    menuExpanded = false
                }
            )
            DropdownMenuItem(
                text = { Text(text = stringResource(R.string.generic_delete)) },
                leadingIcon = {
                    Icon(
                        modifier = Modifier.size(20.dp),
                        painter = painterResource(R.drawable.ic_delete_filled),
                        contentDescription = stringResource(R.string.generic_delete)
                    )
                },
                onClick = {
                    onDeleteClick()
                    menuExpanded = false
                }
            )
        }
    }
}

@Composable
private fun ShaderOperation(
    shaderOperation: ShaderOperation,
    updateOperation: (ShaderOperation) -> Unit,
    shadersDir: File,
    renameShaderPack: (ShaderPackInfo, String) -> Unit,
    deleteShaderPack: (ShaderPackInfo) -> Unit
) {
    when (shaderOperation) {
        is ShaderOperation.None -> {}
        is ShaderOperation.Progress -> {
            ProgressDialog()
        }
        is ShaderOperation.Rename -> {
            val info = shaderOperation.info
            FileNameInputDialog(
                initValue = info.file.nameWithoutExtension,
                existsCheck = { value ->
                    if (File(shadersDir, "$value.${info.file.extension}").exists()) {
                        stringResource(R.string.shader_pack_manage_exists)
                    } else {
                        null
                    }
                },
                title = stringResource(R.string.generic_rename),
                label = stringResource(R.string.shader_pack_manage_name),
                onDismissRequest = {
                    updateOperation(ShaderOperation.None)
                },
                onConfirm = { newName ->
                    renameShaderPack(info, newName)
                    updateOperation(ShaderOperation.None)
                }
            )
        }
        is ShaderOperation.Delete -> {
            val info = shaderOperation.info
            SimpleAlertDialog(
                title = stringResource(R.string.generic_warning),
                text = stringResource(R.string.shader_pack_manage_delete_warning, info.file.name),
                onDismiss = {
                    updateOperation(ShaderOperation.None)
                },
                onConfirm = {
                    deleteShaderPack(info)
                    updateOperation(ShaderOperation.None)
                }
            )
        }
    }
}