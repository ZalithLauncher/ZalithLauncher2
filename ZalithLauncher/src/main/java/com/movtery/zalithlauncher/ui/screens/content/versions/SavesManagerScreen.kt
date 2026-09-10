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

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.material3.nonInteractiveScrollbar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
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
import coil3.compose.AsyncImage
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.context.COPY_LABEL_SAVE_SEED
import com.movtery.zalithlauncher.coroutine.TaskSystem
import com.movtery.zalithlauncher.game.version.installed.Version
import com.movtery.zalithlauncher.game.version.installed.VersionFolders
import com.movtery.zalithlauncher.game.version.installed.VersionInfo
import com.movtery.zalithlauncher.game.version.saves.SaveData
import com.movtery.zalithlauncher.game.version.saves.isCompatible
import com.movtery.zalithlauncher.game.version.saves.parseLevelDatFile
import com.movtery.zalithlauncher.game.version.saves.unpackSaveZip
import com.movtery.zalithlauncher.ui.androidText
import com.movtery.zalithlauncher.ui.base.BaseScreen
import com.movtery.zalithlauncher.ui.components.CardTitleLayout
import com.movtery.zalithlauncher.ui.components.ContentCheckBox
import com.movtery.zalithlauncher.ui.components.EdgeDirection
import com.movtery.zalithlauncher.ui.components.IconTextButton
import com.movtery.zalithlauncher.ui.components.LittleTextLabel
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
import com.movtery.zalithlauncher.ui.screens.content.versions.elements.FileNameInputDialog
import com.movtery.zalithlauncher.ui.screens.content.versions.elements.LoadingState
import com.movtery.zalithlauncher.ui.screens.content.versions.elements.MinecraftColorTextNormal
import com.movtery.zalithlauncher.ui.screens.content.versions.elements.SavesFilter
import com.movtery.zalithlauncher.ui.screens.content.versions.elements.SavesOperation
import com.movtery.zalithlauncher.ui.screens.content.versions.elements.filterSaves
import com.movtery.zalithlauncher.ui.screens.content.versions.layouts.VersionChunkBackground
import com.movtery.zalithlauncher.ui.theme.itemColor
import com.movtery.zalithlauncher.ui.theme.onItemColor
import com.movtery.zalithlauncher.utils.animation.getAnimateTween
import com.movtery.zalithlauncher.utils.animation.swapAnimateDpAsState
import com.movtery.zalithlauncher.utils.copyText
import com.movtery.zalithlauncher.utils.formatDate
import com.movtery.zalithlauncher.viewmodel.ErrorViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.apache.commons.io.FileUtils
import java.io.File
import java.util.Date
import kotlin.time.DurationUnit
import kotlin.time.toDuration

private class SavesManageViewModel(
    val minecraftVersion: String,
    val savesDir: File
) : ViewModel() {
    var savesFilter by mutableStateOf(SavesFilter(onlyShowCompatible = false, saveName = ""))
        private set

    var allSaves by mutableStateOf<List<SaveData>>(emptyList())
        private set
    var filteredSaves by mutableStateOf<List<SaveData>?>(null)
        private set
    var sortByEnum by mutableStateOf(SortByEnum.Name)
        private set
    var isAscending by mutableStateOf(true)
        private set

    var savesState by mutableStateOf<LoadingState>(LoadingState.Loading)
        private set

    private var job: Job? = null

    fun refresh() {
        job?.cancel()
        job = viewModelScope.launch {
            savesState = LoadingState.Loading

            withContext(Dispatchers.IO) {
                val tempList = mutableListOf<SaveData>()
                savesDir.listFiles()?.filter { it.isDirectory }?.takeIf { it.isNotEmpty() }?.let { dirs ->
                    try {
                        dirs.forEach { dir ->
                            ensureActive()
                            //解析存档 level.dat，读取必要数据
                            val data = parseLevelDatFile(
                                saveFile = dir,
                                levelDatFile = File(dir, "level.dat"),
                                worldGenDatFile = File(dir, "data/minecraft/world_gen_settings.dat")
                                    .takeIf { it.isFile && it.exists() }
                            )
                            tempList.add(data)
                        }
                    } catch (_: CancellationException) {
                        return@withContext
                    }
                }
                allSaves = tempList.sortedBy { it.saveFile.name }
                filterSaves()
            }

            savesState = LoadingState.None
        }
    }

    init {
        refresh()
    }

    fun updateFilter(filter: SavesFilter) {
        this.savesFilter = filter
        filterSaves()
    }

    fun updateSortBy(sortByEnum: SortByEnum) {
        this.sortByEnum = sortByEnum
        filterSaves()
    }

    fun updateSortOrder() {
        this.isAscending = !this.isAscending
        filterSaves()
    }

    val supportedSortByEnums = listOf(
        SortByEnum.Name, SortByEnum.FileName, SortByEnum.LastPlayed
    )

    private fun filterSaves() {
        filteredSaves = allSaves
            .takeIf { it.isNotEmpty() }
            ?.filterSaves(minecraftVersion, savesFilter)
            ?.sortedWith { o1, o2 ->
                val file1 = o1.saveFile
                val file2 = o2.saveFile
                val lastPlayed1 = o1.lastPlayed ?: file1.lastModified()
                val lastPlayed2 = o2.lastPlayed ?: file2.lastModified()
                val value = when (sortByEnum) {
                    SortByEnum.Name -> (o1.levelName ?: file1.name).compareTo(o2.levelName ?: file2.name)
                    SortByEnum.FileName -> file1.name.compareTo(file2.name)
                    SortByEnum.LastPlayed -> lastPlayed2.compareTo(lastPlayed1)
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
private fun rememberSavesManageViewModel(
    minecraftVersion: String,
    savesDir: File,
    version: Version
) = viewModel(
    key = version.toString() + "_" + VersionFolders.SAVES.folderName
) {
    SavesManageViewModel(
        minecraftVersion = minecraftVersion,
        savesDir = savesDir
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SavesManagerScreen(
    mainScreenKey: TitledNavKey?,
    versionsScreenKey: TitledNavKey?,
    version: Version,
    onQuickPlay: (Version, String) -> Unit,
    backToMainScreen: () -> Unit,
    swapToDownload: () -> Unit,
    submitError: (ErrorViewModel.ThrowableMessage) -> Unit
) {
    if (!version.isValid()) {
        backToMainScreen()
        return
    }

    val savesDir = remember(version) {
        VersionFolders.SAVES.getDir(version.getGameDir())
    }
    val versionInfo = remember(version) {
        version.getVersionInfo()!!
    }
    val minecraftVersion = remember(versionInfo) {
        versionInfo.minecraftVersion
    }
    val quickPlay = remember(versionInfo) {
        versionInfo.quickPlay
    }

    BaseScreen(
        levels1 = listOf(
            Pair(NestedNavKey.VersionSettings::class.java, mainScreenKey)
        ),
        Triple(NormalNavKey.Versions.SavesManager, versionsScreenKey, false),
    ) { isVisible ->
        val viewModel = rememberSavesManageViewModel(minecraftVersion, savesDir, version)

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

            when (viewModel.savesState) {
                is LoadingState.None -> {
                    var savesOperation by remember { mutableStateOf<SavesOperation>(SavesOperation.None) }
                    fun runProgress(task: () -> Unit) {
                        operationScope.launch(Dispatchers.IO) {
                            savesOperation = SavesOperation.Progress
                            task()
                            savesOperation = SavesOperation.None
                            viewModel.refresh()
                        }
                    }
                    SaveOperation(
                        savesOperation = savesOperation,
                        savesDir = savesDir,
                        updateOperation = { savesOperation = it },
                        quickPlay = { saveName ->
                            onQuickPlay(version, saveName)
                        },
                        renameSave = { saveData, newName ->
                            runProgress {
                                saveData.saveFile.renameTo(File(savesDir, newName))
                            }
                        },
                        backupSave = { saveData, name ->
                            runProgress {
                                FileUtils.copyDirectory(saveData.saveFile, File(savesDir, name))
                            }
                        },
                        deleteSave = { saveData ->
                            runProgress {
                                FileUtils.deleteQuietly(saveData.saveFile)
                            }
                        }
                    )

                    Column {
                        SavesActionsHeader(
                            modifier = Modifier.fillMaxWidth(),
                            savesFilter = viewModel.savesFilter,
                            onSavesFilterChange = { viewModel.updateFilter(it) },
                            supportedSortByEnums = viewModel.supportedSortByEnums,
                            sortByEnum = viewModel.sortByEnum,
                            onSortByChanged = { viewModel.updateSortBy(it) },
                            isAscending = viewModel.isAscending,
                            onToggleSortOrder = { viewModel.updateSortOrder() },
                            savesDir = savesDir,
                            swapToDownload = swapToDownload,
                            refreshSaves = { viewModel.refresh() },
                            submitError = submitError
                        )

                        SavesList(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            savesList = viewModel.filteredSaves,
                            quickPlay = quickPlay,
                            minecraftVersion = minecraftVersion,
                            updateOperation = { savesOperation = it }
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
private fun SavesActionsHeader(
    modifier: Modifier,
    savesFilter: SavesFilter,
    onSavesFilterChange: (SavesFilter) -> Unit,
    supportedSortByEnums: List<SortByEnum>,
    sortByEnum: SortByEnum,
    onSortByChanged: (SortByEnum) -> Unit,
    isAscending: Boolean,
    onToggleSortOrder: () -> Unit,
    savesDir: File,
    swapToDownload: () -> Unit,
    refreshSaves: () -> Unit,
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
                    value = savesFilter.saveName,
                    onValueChange = { onSavesFilterChange(savesFilter.copy(saveName = it)) },
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
                        checked = savesFilter.onlyShowCompatible,
                        onCheckedChange = { onSavesFilterChange(savesFilter.copy(onlyShowCompatible = it)) }
                    ) {
                        Text(
                            text = stringResource(R.string.manage_only_show_valid),
                            style = MaterialTheme.typography.labelMedium
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    val taskBuilder = rememberMultipleUriImportTaskBuilder(
                        id = "ContentManager.Saves.Import",
                        targetDir = savesDir,
                        checkExtension = listOf("zip"),
                        errorMessage = stringResource(R.string.saves_manage_import_failed),
                        submitError = submitError,
                        onImported = refreshSaves,
                        onFileCopied = { task, file ->
                            task.updateProgress(-1f)
                            task.updateMessage(androidText(
                                R.string.saves_manage_import_unpacking, file.name
                            ))
                            unpackSaveZip(file, savesDir)
                        }
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
                        onClick = refreshSaves
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

/**
 * @param minecraftVersion 当前版本的 Minecraft 版本
 */
@Composable
private fun SavesList(
    modifier: Modifier = Modifier,
    savesList: List<SaveData>?,
    quickPlay: VersionInfo.QuickPlay,
    minecraftVersion: String,
    updateOperation: (SavesOperation) -> Unit
) {
    savesList?.let { list ->
        if (list.isNotEmpty()) {
            val scrollState = rememberLazyListState()
            LazyColumn(
                modifier = modifier.nonInteractiveScrollbar(
                    state = scrollState.scrollIndicatorState!!,
                    orientation = Orientation.Vertical,
                ),
                contentPadding = PaddingValues(all = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                state = scrollState,
            ) {
                items(list) { saveData ->
                    SaveItemLayout(
                        modifier = Modifier.fillMaxWidth(),
                        saveData = saveData,
                        quickPlay = quickPlay,
                        minecraftVersion = minecraftVersion,
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
        //如果为null，则代表本身就没有存档可以展示
        Box(modifier = Modifier.fillMaxSize()) {
            ScalingLabel(
                modifier = Modifier.align(Alignment.Center),
                text = stringResource(R.string.saves_manage_no_saves)
            )
        }
    }
}

/**
 * @param saveData 存档信息
 * @param minecraftVersion 当前版本的 Minecraft 版本
 */
@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun SaveItemLayout(
    modifier: Modifier = Modifier,
    saveData: SaveData,
    quickPlay: VersionInfo.QuickPlay,
    minecraftVersion: String,
    onClick: () -> Unit = {},
    updateOperation: (SavesOperation) -> Unit = {},
    shape: Shape = MaterialTheme.shapes.large,
    itemColor: Color = itemColor(),
    itemContentColor: Color = onItemColor(),
) {
    //存档是否与当前 MC 版本兼容
    val isCompatible = saveData.isCompatible(minecraftVersion)

    val context = LocalContext.current

    val scale = remember { Animatable(initialValue = 0.95f) }
    LaunchedEffect(Unit) {
        scale.animateTo(targetValue = 1f, animationSpec = getAnimateTween())
    }

    Surface(
        modifier = modifier.graphicsLayer(scaleY = scale.value, scaleX = scale.value),
        onClick = onClick,
        shape = shape,
        color = itemColor,
        contentColor = itemContentColor,
    ) {
        Row(
            modifier = Modifier.padding(all = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            //存档的封面图标
            SaveIcon(
                modifier = Modifier
                    .size(42.dp)
                    .clip(shape = RoundedCornerShape(10.dp)),
                saveData = saveData
            )

            Column(
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                val levelName = saveData.levelName
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MinecraftColorTextNormal(
                        modifier = Modifier.basicMarquee(iterations = Int.MAX_VALUE),
                        inputText = (levelName ?: saveData.saveFile.name),
                        style = MaterialTheme.typography.titleSmall,
                        maxLines = 1
                    )

                    if (saveData.isValid) {
                        saveData.levelMCVersion?.takeIf { it.isNotEmpty() }?.let { levelMCVer ->
                            if (isCompatible) {
                                LittleTextLabel(
                                    text = levelMCVer
                                )
                            } else {
                                LittleTextLabel(
                                    text = levelMCVer,
                                    color = MaterialTheme.colorScheme.error,
                                    contentColor = MaterialTheme.colorScheme.onError
                                )
                            }
                        }

                        //虽然极限模式与 gameMode 是分离开的
                        //不过它可以算作是一种游戏模式，毕竟创建世界时，极限模式就是在游戏模式里面选择的
                        if (saveData.hardcoreMode == true) {
                            LittleTextLabel(text = stringResource(R.string.saves_manage_hardcore))
                        } else {
                            saveData.gameMode?.let { gameMode ->
                                LittleTextLabel(text = stringResource(gameMode.nameRes))
                            }
                        }
                    }
                }

                if (saveData.isValid) {
                    Row(
                        modifier = Modifier.alpha(0.7f),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        val timeString = formatDate(
                            date = Date(saveData.lastPlayed ?: saveData.saveFile.lastModified()),
                            pattern = stringResource(R.string.date_format)
                        )
                        Text(
                            text = stringResource(R.string.saves_manage_last_played, timeString),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.align(Alignment.CenterVertically),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (saveData.isValid) {
                    //详细信息展示
                    TooltipIconButton(
                        modifier = Modifier.size(38.dp),
                        tooltip = {
                            RichTooltip(
                                modifier = Modifier.padding(all = 3.dp),
                                title = { Text(text = stringResource(R.string.saves_manage_info)) },
                                shadowElevation = 3.dp
                            ) {
                                SaveInfoTooltip(saveData) { seed ->
                                    copyText(COPY_LABEL_SAVE_SEED, seed, context)
                                }
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
                        text = stringResource(R.string.saves_manage_invalid),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                //更多存档操作
                SaveOperationMenu(
                    saveValid = saveData.isValid,
                    buttonSize = 38.dp,
                    iconSize = 26.dp,
                    canQuickPlay = quickPlay.isQuickPlaySingleplayer,
                    onQuickPlayClick = {
                        updateOperation(SavesOperation.QuickPlay(saveData))
                    },
                    onRenameClick = {
                        updateOperation(SavesOperation.RenameSave(saveData))
                    },
                    onBackupClick = {
                        updateOperation(SavesOperation.BackupSave(saveData))
                    },
                    onDeleteClick = {
                        updateOperation(SavesOperation.DeleteSave(saveData))
                    }
                )
            }
        }
    }
}

/**
 * 存档的封面图标
 * @param triggerRefresh 强制刷新
 */
@Composable
private fun SaveIcon(
    modifier: Modifier = Modifier,
    saveData: SaveData,
    triggerRefresh: Any? = null
) {
    val iconFile = remember(saveData) {
        File(saveData.saveFile, "icon.png")
    }

    val model = remember(iconFile, triggerRefresh) {
        iconFile.takeIf { it.exists() && it.isFile } ?: R.drawable.ic_unknown_save
    }

    AsyncImage(
        model = model,
        contentDescription = null,
        alignment = Alignment.Center,
        contentScale = ContentScale.Fit,
        modifier = modifier
    )
}

@Composable
private fun SaveInfoTooltip(
    saveData: SaveData,
    copySeed: (String) -> Unit = {}
) {
    Column {
        //文件名
        Text(text = stringResource(R.string.generic_file_name, saveData.saveFile.name))
//        //存档大小
//        Text(text = stringResource(R.string.generic_file_size, formatFileSize(saveData.saveSize)))
        //游戏模式，不存在则展示为未知
        Text(
            text = stringResource(
                R.string.saves_manage_gamemode,
                stringResource(
                    if (saveData.hardcoreMode == true) {
                        //极限模式
                        R.string.saves_manage_hardcore
                    } else {
                        saveData.gameMode?.nameRes ?: R.string.generic_unknown
                    }
                )
            )
        )
        //游戏难度
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            //游戏难度，不存在则展示为未知
            Text(
                text = stringResource(
                    R.string.saves_manage_difficulty,
                    stringResource(saveData.difficulty?.nameRes ?: R.string.generic_unknown)
                )
            )
            if (saveData.difficultyLocked == true) {
                Text(text = stringResource(R.string.saves_manage_difficulty_locked))
            }
        }
        //是否使用指令
        if (saveData.allowCommands == true) {
            Text(text = stringResource(R.string.saves_manage_allow_commands))
        }
        //游戏时长
        if (saveData.playTime != null) {
            val duration = (saveData.playTime / 20).toDuration(DurationUnit.SECONDS)
            val playtime = duration.toComponents { days, hours, minutes, _, _ ->
                stringResource(R.string.duration_format, days, hours, minutes)
            }
            Text(text = stringResource(R.string.saves_manage_playtime, playtime))
        }
        //世界种子
        val worldSeed = saveData.worldSeed?.toString()
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = stringResource(
                    R.string.saves_manage_world_seed,
                    worldSeed ?: stringResource(R.string.generic_unknown)
                )
            )
            //不为未知时，允许复制种子码
            worldSeed?.let { seed ->
                IconButton(
                    modifier = Modifier.size(24.dp),
                    onClick = {
                        copySeed(seed)
                    }
                ) {
                    Icon(
                        modifier = Modifier.size(18.dp),
                        painter = painterResource(R.drawable.ic_copy_all_outlined),
                        contentDescription = stringResource(R.string.generic_copy)
                    )
                }
            }
        }
    }
}

@Composable
private fun SaveOperationMenu(
    saveValid: Boolean,
    buttonSize: Dp,
    iconSize: Dp = buttonSize,
    canQuickPlay: Boolean,
    onQuickPlayClick: () -> Unit = {},
    onRenameClick: () -> Unit = {},
    onBackupClick: () -> Unit = {},
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
                enabled = saveValid && canQuickPlay,
                text = {
                    Text(
                        text = if (canQuickPlay) {
                            stringResource(R.string.saves_manage_quick_play)
                        } else {
                            stringResource(R.string.saves_manage_quick_play_disabled)
                        }
                    )
                },
                leadingIcon = {
                    Icon(
                        modifier = Modifier.size(20.dp),
                        painter = painterResource(R.drawable.ic_play_arrow_filled),
                        contentDescription = stringResource(R.string.saves_manage_quick_play)
                    )
                },
                onClick = {
                    onQuickPlayClick()
                    menuExpanded = false
                }
            )
            DropdownMenuItem(
                enabled = saveValid,
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
                enabled = saveValid,
                text = { Text(text = stringResource(R.string.saves_manage_backup)) },
                leadingIcon = {
                    Icon(
                        modifier = Modifier.size(20.dp),
                        painter = painterResource(R.drawable.ic_save_filled),
                        contentDescription = stringResource(R.string.saves_manage_backup)
                    )
                },
                onClick = {
                    onBackupClick()
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
private fun SaveOperation(
    savesOperation: SavesOperation,
    savesDir: File,
    updateOperation: (SavesOperation) -> Unit,
    quickPlay: (saveName: String) -> Unit,
    renameSave: (SaveData, String) -> Unit,
    backupSave: (SaveData, String) -> Unit,
    deleteSave: (SaveData) -> Unit
) {
    when (savesOperation) {
        is SavesOperation.None -> {}
        is SavesOperation.Progress -> {
            ProgressDialog()
        }
        is SavesOperation.QuickPlay -> {
            val saveData = savesOperation.saveData
            quickPlay(saveData.saveFile.name)
            updateOperation(SavesOperation.None)
        }
        is SavesOperation.RenameSave -> {
            val saveData = savesOperation.saveData
            SaveNameInputDialog(
                saveData = saveData,
                savesDir = savesDir,
                title = stringResource(R.string.generic_rename),
                onDismissRequest = {
                    updateOperation(SavesOperation.None)
                },
                onConfirm = { value ->
                    renameSave(saveData, value)
                    updateOperation(SavesOperation.None)
                }
            )
        }
        is SavesOperation.BackupSave -> {
            val saveData = savesOperation.saveData
            SaveNameInputDialog(
                saveData = saveData,
                savesDir = savesDir,
                title = stringResource(R.string.saves_manage_backup),
                onDismissRequest = {
                    updateOperation(SavesOperation.None)
                },
                onConfirm = { value ->
                    backupSave(saveData, value)
                    updateOperation(SavesOperation.None)
                }
            )
        }
        is SavesOperation.DeleteSave -> {
            val saveData = savesOperation.saveData
            SimpleAlertDialog(
                title = stringResource(R.string.generic_warning),
                text = stringResource(R.string.saves_manage_delete_warning, saveData.saveFile.name),
                onDismiss = {
                    updateOperation(SavesOperation.None)
                },
                onConfirm = {
                    deleteSave(saveData)
                    updateOperation(SavesOperation.None)
                }
            )
        }
    }
}

@Composable
private fun SaveNameInputDialog(
    saveData: SaveData,
    savesDir: File,
    title: String,
    onDismissRequest: () -> Unit = {},
    onConfirm: (vale: String) -> Unit = {}
) {
    FileNameInputDialog(
        initValue = saveData.saveFile.name,
        existsCheck = { value ->
            if (File(savesDir, value).exists()) {
                stringResource(R.string.saves_manage_exists)
            } else {
                null
            }
        },
        title = title,
        label = stringResource(R.string.saves_manage_save_name),
        onDismissRequest = onDismissRequest,
        onConfirm = onConfirm
    )
}
