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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RichTooltip
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
import androidx.compose.ui.draw.clip
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
import com.movtery.zalithlauncher.game.version.resource_pack.ResourcePackInfo
import com.movtery.zalithlauncher.game.version.resource_pack.parseResourcePack
import com.movtery.zalithlauncher.ui.base.BaseScreen
import com.movtery.zalithlauncher.ui.components.CardTitleLayout
import com.movtery.zalithlauncher.ui.components.ContentCheckBox
import com.movtery.zalithlauncher.ui.components.EdgeDirection
import com.movtery.zalithlauncher.ui.components.IconTextButton
import com.movtery.zalithlauncher.ui.components.ProgressDialog
import com.movtery.zalithlauncher.ui.components.ScalingLabel
import com.movtery.zalithlauncher.ui.components.SimpleAlertDialog
import com.movtery.zalithlauncher.ui.components.SimpleTextInputField
import com.movtery.zalithlauncher.ui.components.TooltipIconButton
import com.movtery.zalithlauncher.ui.components.fadeEdge
import com.movtery.zalithlauncher.ui.screens.NestedNavKey
import com.movtery.zalithlauncher.ui.screens.NormalNavKey
import com.movtery.zalithlauncher.ui.screens.TitledNavKey
import com.movtery.zalithlauncher.ui.screens.content.elements.ImportMultipleFileButton
import com.movtery.zalithlauncher.ui.screens.content.elements.SortByDropdownMenu
import com.movtery.zalithlauncher.ui.screens.content.elements.SortByEnum
import com.movtery.zalithlauncher.ui.screens.content.elements.rememberMultipleUriImportTaskBuilder
import com.movtery.zalithlauncher.ui.screens.content.versions.elements.ByteArrayIcon
import com.movtery.zalithlauncher.ui.screens.content.versions.elements.DeleteAllOperation
import com.movtery.zalithlauncher.ui.screens.content.versions.elements.FileNameInputDialog
import com.movtery.zalithlauncher.ui.screens.content.versions.elements.LoadingState
import com.movtery.zalithlauncher.ui.screens.content.versions.elements.MinecraftColorTextNormal
import com.movtery.zalithlauncher.ui.screens.content.versions.elements.ResourcePackFilter
import com.movtery.zalithlauncher.ui.screens.content.versions.elements.ResourcePackOperation
import com.movtery.zalithlauncher.ui.screens.content.versions.elements.filterPacks
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

private class ResourcePackManageViewModel(
    val resourcePackDir: File
) : ViewModel() {
    var packFilter by mutableStateOf(ResourcePackFilter(false, ""))
        private set

    var allPacks by mutableStateOf<List<ResourcePackInfo>>(emptyList())
        private set
    var filteredPacks by mutableStateOf<List<ResourcePackInfo>?>(null)
        private set
    var sortByEnum by mutableStateOf(SortByEnum.Name)
        private set
    var isAscending by mutableStateOf(true)
        private set

    var packState by mutableStateOf<LoadingState>(LoadingState.None)
        private set

    /**
     * 已选择的文件
     */
    val selectedPacks = mutableStateListOf<ResourcePackInfo>()

    /**
     * 删除所有已选择文件的操作流程
     */
    var deleteAllOperation by mutableStateOf<DeleteAllOperation>(DeleteAllOperation.None)

    /** 临时记录的资源包数量 */
    private var packCount = FolderFileCounter(resourcePackDir)

    /**
     * 全选所有文件
     */
    fun selectAllFiles() {
        filteredPacks?.forEach { pack ->
            if (!selectedPacks.contains(pack)) selectedPacks.add(pack)
        }
    }

    fun clearSelected() {
        filteredPacks?.let {
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
            packState = LoadingState.Loading
            selectedPacks.clear()
            if (checkCount) packCount.checkDir()

            withContext(Dispatchers.IO) {
                val tempList = mutableListOf<ResourcePackInfo>()
                try {
                    resourcePackDir.listFiles()?.forEach { file ->
                        parseResourcePack(file)?.let {
                            ensureActive()
                            tempList.add(it)
                        }
                    }
                } catch (_: CancellationException) {
                    return@withContext
                }
                allPacks = tempList.sortedBy { it.rawName }
                filterPacks()
            }

            packState = LoadingState.None
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

    fun updateFilter(filter: ResourcePackFilter) {
        this.packFilter = filter
        filterPacks()
    }

    fun updateSortBy(sortByEnum: SortByEnum) {
        this.sortByEnum = sortByEnum
        filterPacks()
    }

    fun updateSortOrder() {
        this.isAscending = !this.isAscending
        filterPacks()
    }

    val supportedSortByEnums = listOf(
        SortByEnum.Name, SortByEnum.FileModifiedTime
    )

    private fun filterPacks() {
        filteredPacks = allPacks
            .takeIf { it.isNotEmpty() }
            ?.filterPacks(packFilter)
            ?.sortedWith { o1, o2 ->
                val value = when (sortByEnum) {
                    SortByEnum.Name -> o1.displayName.compareTo(o2.displayName)
                    SortByEnum.FileModifiedTime -> o2.file.lastModified().compareTo(o1.file.lastModified())
                    else -> error("This sorting method is not supported: $sortByEnum")
                }
                if (isAscending) {
                    value
                } else {
                    -value
                }
            }
    }

    override fun onCleared() {
        super.onCleared()
        job?.cancel()
        job = null
    }
}

@Composable
private fun rememberResourcePackManageViewModel(
    resourcePackDir: File,
    version: Version
) = viewModel(
    key = version.toString() + "_" + VersionFolders.RESOURCE_PACK.folderName
) {
    ResourcePackManageViewModel(resourcePackDir)
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ResourcePackManageScreen(
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

    val resourcePackDir = remember(version) {
        VersionFolders.RESOURCE_PACK.getDir(version.getGameDir())
    }

    BaseScreen(
        levels1 = listOf(
            Pair(NestedNavKey.VersionSettings::class.java, mainScreenKey)
        ),
        Triple(NormalNavKey.Versions.ResourcePackManager, versionsScreenKey, false)
    ) { isVisible ->
        val viewModel = rememberResourcePackManageViewModel(resourcePackDir, version)

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

            when (viewModel.packState) {
                is LoadingState.None -> {
                    var resourcePackOperation by remember { mutableStateOf<ResourcePackOperation>(ResourcePackOperation.None) }
                    fun runProgress(task: () -> Unit) {
                        operationScope.launch(Dispatchers.IO) {
                            resourcePackOperation = ResourcePackOperation.Progress
                            task()
                            resourcePackOperation = ResourcePackOperation.None
                            viewModel.refresh()
                        }
                    }
                    ResourcePackOperation(
                        resourcePackDir = resourcePackDir,
                        resourcePackOperation = resourcePackOperation,
                        updateOperation = { resourcePackOperation = it },
                        onRename = { newName, packInfo ->
                            runProgress {
                                val extension = if (packInfo.file.isFile) {
                                    ".${packInfo.file.extension}"
                                } else ""
                                packInfo.file.renameTo(File(resourcePackDir, "$newName$extension"))
                            }
                        },
                        onDelete = { packInfo ->
                            runProgress {
                                FileUtils.deleteQuietly(packInfo.file)
                            }
                        }
                    )

                    Column {
                        ResourcePackHeader(
                            modifier = Modifier.fillMaxWidth(),
                            packFilter = viewModel.packFilter,
                            changePackFilter = { viewModel.updateFilter(it) },
                            supportedSortByEnums = viewModel.supportedSortByEnums,
                            sortByEnum = viewModel.sortByEnum,
                            onSortByChanged = { viewModel.updateSortBy(it) },
                            isAscending = viewModel.isAscending,
                            onToggleSortOrder = { viewModel.updateSortOrder() },
                            resourcePackDir = resourcePackDir,
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
                            onRefresh = {
                                viewModel.refresh()
                            },
                            submitError = submitError,
                        )

                        ResourcePackList(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            packList = viewModel.filteredPacks,
                            selectedPacks = viewModel.selectedPacks,
                            removeFromSelected = { viewModel.selectedPacks.remove(it) },
                            addToSelected = { viewModel.selectedPacks.add(it) },
                            updateOperation = { resourcePackOperation = it }
                        )
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
private fun ResourcePackHeader(
    modifier: Modifier = Modifier,
    packFilter: ResourcePackFilter,
    changePackFilter: (ResourcePackFilter) -> Unit,
    supportedSortByEnums: List<SortByEnum>,
    sortByEnum: SortByEnum,
    onSortByChanged: (SortByEnum) -> Unit,
    isAscending: Boolean,
    onToggleSortOrder: () -> Unit,
    resourcePackDir: File,
    onDeleteAll: () -> Unit,
    isFilesSelected: Boolean,
    onSelectAll: () -> Unit,
    onClearFilesSelected: () -> Unit,
    swapToDownload: () -> Unit,
    onRefresh: () -> Unit,
    submitError: (ErrorViewModel.ThrowableMessage) -> Unit,
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
                    value = packFilter.filterName,
                    onValueChange = { changePackFilter(packFilter.copy(filterName = it)) },
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

                        Spacer(modifier = Modifier.width(6.dp))
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
                    ContentCheckBox(
                        checked = packFilter.onlyShowValid,
                        onCheckedChange = { changePackFilter(packFilter.copy(onlyShowValid = it)) }
                    ) {
                        Text(
                            text = stringResource(R.string.manage_only_show_valid),
                            style = MaterialTheme.typography.labelMedium
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    val taskBuilder = rememberMultipleUriImportTaskBuilder(
                        id = "ContentManager.ResourcePacks.Import",
                        targetDir = resourcePackDir,
                        checkExtension = listOf("zip"),
                        submitError = submitError,
                        onImported = onRefresh
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
                        onClick = onRefresh
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
private fun ResourcePackList(
    modifier: Modifier = Modifier,
    packList: List<ResourcePackInfo>?,
    selectedPacks: List<ResourcePackInfo>,
    removeFromSelected: (ResourcePackInfo) -> Unit,
    addToSelected: (ResourcePackInfo) -> Unit,
    updateOperation: (ResourcePackOperation) -> Unit
) {
    packList?.let { list ->
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
                items(list) { pack ->
                    ResourcePackItemLayout(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        resourcePackInfo = pack,
                        selected = selectedPacks.contains(pack),
                        onClick = {
                            if (selectedPacks.contains(pack)) {
                                removeFromSelected(pack)
                            } else {
                                addToSelected(pack)
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
        //如果为null，则代表本身就没有资源包可以展示
        Box(modifier = Modifier.fillMaxSize()) {
            ScalingLabel(
                modifier = Modifier.align(Alignment.Center),
                text = stringResource(R.string.resource_pack_manage_no_packs)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ResourcePackItemLayout(
    modifier: Modifier = Modifier,
    resourcePackInfo: ResourcePackInfo,
    selected: Boolean,
    onClick: () -> Unit = {},
    itemColor: Color = itemColor(),
    itemContentColor: Color = onItemColor(),
    borderColor: Color = MaterialTheme.colorScheme.primary,
    shape: Shape = MaterialTheme.shapes.large,
    updateOperation: (ResourcePackOperation) -> Unit
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
            ByteArrayIcon(
                modifier = Modifier
                    .size(48.dp)
                    .clip(shape = RoundedCornerShape(10.dp)),
                triggerRefresh = resourcePackInfo,
                icon = resourcePackInfo.icon
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                MinecraftColorTextNormal(
                    modifier = Modifier.basicMarquee(iterations = Int.MAX_VALUE),
                    inputText = resourcePackInfo.displayName,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1
                )
                resourcePackInfo.description?.let { description ->
                    MinecraftColorTextNormal(
                        inputText = description,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 2
                    )
                }
            }

            Row(
                modifier = Modifier.align(Alignment.CenterVertically),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (resourcePackInfo.isValid) {
                    //详细信息展示
                    TooltipIconButton(
                        modifier = Modifier.size(38.dp),
                        tooltip = {
                            RichTooltip(
                                modifier = Modifier.padding(all = 3.dp),
                                title = { Text(text = stringResource(R.string.resource_pack_manage_info)) },
                                shadowElevation = 3.dp
                            ) {
                                ResourcePackInfoTooltip(resourcePackInfo)
                            }
                        }
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_info_outlined),
                            contentDescription = stringResource(R.string.saves_manage_info)
                        )
                    }
                } else {
                    Text(
                        text = stringResource(R.string.resource_pack_manage_invalid),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                //更多操作
                ResourcePackOperationMenu(
                    resourcePackInfo = resourcePackInfo,
                    buttonSize = 38.dp,
                    iconSize = 26.dp,
                    onRenameClick = {
                        updateOperation(ResourcePackOperation.RenamePack(resourcePackInfo))
                    },
                    onDeleteClick = {
                        updateOperation(ResourcePackOperation.DeletePack(resourcePackInfo))
                    }
                )
            }
        }
    }
}

@Composable
private fun ResourcePackOperationMenu(
    resourcePackInfo: ResourcePackInfo,
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
                enabled = resourcePackInfo.isValid,
                text = {
                    Text(text = stringResource(R.string.generic_rename))
                },
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
                text = {
                    Text(text = stringResource(R.string.generic_delete))
                },
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
private fun ResourcePackOperation(
    resourcePackDir: File,
    resourcePackOperation: ResourcePackOperation,
    updateOperation: (ResourcePackOperation) -> Unit,
    onRename: (String, ResourcePackInfo) -> Unit,
    onDelete: (ResourcePackInfo) -> Unit
) {
    when (resourcePackOperation) {
        is ResourcePackOperation.None -> {}
        is ResourcePackOperation.Progress -> {
            ProgressDialog()
        }
        is ResourcePackOperation.RenamePack -> {
            val packInfo = resourcePackOperation.packInfo
            FileNameInputDialog(
                initValue = packInfo.displayName,
                existsCheck = { value ->
                    val fileName = if (packInfo.file.isDirectory) {
                        value //文件夹类型，不做扩展名处理
                    } else {
                        "$value.${packInfo.file.extension}"
                    }

                    if (File(resourcePackDir, fileName).exists()) {
                        stringResource(R.string.resource_pack_manage_exists)
                    } else {
                        null
                    }
                },
                title = stringResource(R.string.generic_rename),
                label = stringResource(R.string.resource_pack_manage_name),
                onDismissRequest = {
                    updateOperation(ResourcePackOperation.None)
                },
                onConfirm = { value ->
                    onRename(value, packInfo)
                }
            )
        }
        is ResourcePackOperation.DeletePack -> {
            val packInfo = resourcePackOperation.packInfo
            SimpleAlertDialog(
                title = stringResource(R.string.generic_warning),
                text = stringResource(R.string.resource_pack_manage_delete_warning, packInfo.file.name),
                onDismiss = {
                    updateOperation(ResourcePackOperation.None)
                },
                onConfirm = {
                    onDelete(packInfo)
                    updateOperation(ResourcePackOperation.None)
                }
            )
        }
    }
}

@Composable
private fun ResourcePackInfoTooltip(
    resourcePackInfo: ResourcePackInfo
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        //资源包类型
        Text(
            text = stringResource(
                R.string.resource_pack_manage_type,
                if (resourcePackInfo.file.isDirectory) {
                    stringResource(R.string.resource_pack_manage_type_folder)
                } else {
                    stringResource(R.string.resource_pack_manage_type_zip)
                }
            )
        )
        //文件名称
        Text(text = stringResource(R.string.generic_file_name, resourcePackInfo.file.name))
        //文件大小
        resourcePackInfo.fileSize?.let { fileSize ->
            Text(text = stringResource(R.string.generic_file_size, formatFileSize(fileSize)))
        }
        //格式版本
        resourcePackInfo.packFormat?.let { packFormat ->
            Text(text = stringResource(R.string.resource_pack_manage_formats, packFormat.toString()))
        }
    }
}