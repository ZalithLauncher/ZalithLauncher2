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

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.material3.scrollbar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
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
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.coroutine.TaskSystem
import com.movtery.zalithlauncher.game.addons.modloader.ModLoader
import com.movtery.zalithlauncher.game.download.assets.platform.Platform
import com.movtery.zalithlauncher.game.download.assets.utils.getMcmodTitle
import com.movtery.zalithlauncher.game.version.installed.Version
import com.movtery.zalithlauncher.game.version.installed.VersionFolders
import com.movtery.zalithlauncher.game.version.mod.AllModReader
import com.movtery.zalithlauncher.game.version.mod.LocalMod
import com.movtery.zalithlauncher.game.version.mod.RemoteMod
import com.movtery.zalithlauncher.game.version.mod.isDisabled
import com.movtery.zalithlauncher.game.version.mod.isEnabled
import com.movtery.zalithlauncher.game.version.mod.update.ModManifest
import com.movtery.zalithlauncher.game.version.mod.update.ModUpdater
import com.movtery.zalithlauncher.game.version.mod.update.SelectableModManifest
import com.movtery.zalithlauncher.game.version.mod.update.toSelectableList
import com.movtery.zalithlauncher.ui.base.BaseScreen
import com.movtery.zalithlauncher.ui.components.CardTitleLayout
import com.movtery.zalithlauncher.ui.components.EdgeDirection
import com.movtery.zalithlauncher.ui.components.IconTextButton
import com.movtery.zalithlauncher.ui.components.LittleTextLabel
import com.movtery.zalithlauncher.ui.components.ScalingLabel
import com.movtery.zalithlauncher.ui.components.SimpleTextInputField
import com.movtery.zalithlauncher.ui.components.TooltipIconButton
import com.movtery.zalithlauncher.ui.components.fadeEdge
import com.movtery.zalithlauncher.ui.screens.NestedNavKey
import com.movtery.zalithlauncher.ui.screens.NormalNavKey
import com.movtery.zalithlauncher.ui.screens.TitledNavKey
import com.movtery.zalithlauncher.ui.screens.content.download.assets.elements.AssetsIcon
import com.movtery.zalithlauncher.ui.screens.content.elements.ImportMultipleFileButton
import com.movtery.zalithlauncher.ui.screens.content.elements.ModLoaderIcon
import com.movtery.zalithlauncher.ui.screens.content.elements.SortByDropdownMenu
import com.movtery.zalithlauncher.ui.screens.content.elements.SortByEnum
import com.movtery.zalithlauncher.ui.screens.content.elements.rememberMultipleUriImportTaskBuilder
import com.movtery.zalithlauncher.ui.screens.content.versions.elements.ByteArrayIcon
import com.movtery.zalithlauncher.ui.screens.content.versions.elements.DeleteAllOperation
import com.movtery.zalithlauncher.ui.screens.content.versions.elements.LoadingState
import com.movtery.zalithlauncher.ui.screens.content.versions.elements.ModStateFilter
import com.movtery.zalithlauncher.ui.screens.content.versions.elements.ModsConfirmOperation
import com.movtery.zalithlauncher.ui.screens.content.versions.elements.ModsOperation
import com.movtery.zalithlauncher.ui.screens.content.versions.elements.ModsUpdateOperation
import com.movtery.zalithlauncher.ui.screens.content.versions.elements.filterMods
import com.movtery.zalithlauncher.ui.screens.content.versions.layouts.VersionChunkBackground
import com.movtery.zalithlauncher.ui.theme.itemColor
import com.movtery.zalithlauncher.ui.theme.onItemColor
import com.movtery.zalithlauncher.utils.animation.getAnimateTween
import com.movtery.zalithlauncher.utils.animation.swapAnimateDpAsState
import com.movtery.zalithlauncher.utils.file.FolderFileCounter
import com.movtery.zalithlauncher.utils.file.formatFileSize
import com.movtery.zalithlauncher.utils.string.isNotEmptyOrBlank
import com.movtery.zalithlauncher.viewmodel.ErrorViewModel
import com.movtery.zalithlauncher.viewmodel.EventViewModel
import com.movtery.zalithlauncher.viewmodel.sendKeepScreen
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.apache.commons.io.FileUtils
import java.io.File
import java.util.LinkedList
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.time.Duration.Companion.milliseconds

private class ModsManageViewModel(
    modsDir: File
) : ViewModel() {
    val modReader = AllModReader(modsDir)

    var nameFilter by mutableStateOf("")
        private set
    var stateFilter by mutableStateOf(ModStateFilter.All)
        private set
    var sortByEnum by mutableStateOf(SortByEnum.FileName)
        private set
    var isAscending by mutableStateOf(true)
        private set

    var allMods by mutableStateOf<List<RemoteMod>>(emptyList())
        private set
    var filteredMods by mutableStateOf<List<RemoteMod>?>(null)
        private set

    /** 已启用的模组数量 */
    var enabledCount by mutableIntStateOf(-1)
        private set
    /** 已禁用的模组数量 */
    var disabledCount by mutableIntStateOf(-1)
        private set

    /**
     * 已选择的模组
     */
    val selectedMods = mutableStateListOf<RemoteMod>()

    /**
     * 是否可以更新选中的模组
     */
    var canUpdate by mutableStateOf(false)
        private set

    /**
     * 删除所有已选择模组的操作流程
     */
    var deleteAllOperation by mutableStateOf<DeleteAllOperation>(DeleteAllOperation.None)

    /** 作为标记，记录哪些模组已被加载 */
    private val modsToLoad = mutableListOf<RemoteMod>()
    private val loadQueue = LinkedList<Pair<RemoteMod, Boolean>>()
    private val semaphore = Semaphore(8) //一次最多允许同时加载8个模组
    private var initialQueueSize = 0
    private val queueMutex = Mutex()

    var modsState by mutableStateOf<LoadingState>(LoadingState.None)


    /** 临时记录的模组数量 */
    private var modsCount = FolderFileCounter(modsDir)

    private var job: Job? = null
    /**
     * @param checkCount 刷新目录内文件数量记录
     */
    fun refresh(
        context: Context? = null,
        checkCount: Boolean = true
    ) {
        job?.cancel()
        job = viewModelScope.launch {
            withContext(Dispatchers.Main) {
                enabledCount = -1
                disabledCount = -1
            }
            modsState = LoadingState.Loading
            selectedMods.clear() //清空所有已选择的模组
            canUpdate = false

            if (checkCount) modsCount.checkDir()
            try {
                allMods = modReader.readAllForRemote()
                filterMods(context)
            } catch (_: CancellationException) {
                //已取消
            }
            modsState = LoadingState.None
            job = null
        }
    }

    fun checkCountAndRefresh(
        context: Context? = null,
    ) {
        val isUnchecked = modsCount.isUnchecked()
        if (modsCount.checkDir() && !isUnchecked && job == null) {
            refresh(context = context, checkCount = false)
        }
    }

    /**
     * 刷新模组计数
     */
    fun refreshCounter() {
        allMods.also { list ->
            val counts = list.fold(Pair(0, 0)) { (enabled, disabled), mod ->
                when {
                    mod.localMod.file.isEnabled() -> Pair(enabled + 1, disabled)
                    mod.localMod.file.isDisabled() -> Pair(enabled, disabled + 1)
                    else -> Pair(enabled, disabled)
                }
            }
            enabledCount = counts.first
            disabledCount = counts.second
        }
    }

    init {
        refresh(checkCount = false)
        startQueueProcessor()
    }

    fun updateFilter(name: String, context: Context? = null) {
        this.nameFilter = name
        filterMods(context)
    }

    fun updateStateFilter(filter: ModStateFilter, context: Context? = null) {
        this.stateFilter = filter
        filterMods(context)
    }

    fun updateSortBy(sortByEnum: SortByEnum, context: Context? = null) {
        this.sortByEnum = sortByEnum
        filterMods(context)
    }

    fun updateSortOrder(context: Context? = null) {
        this.isAscending = !this.isAscending
        filterMods(context)
    }

    val supportedSortByEnums = listOf(
        SortByEnum.FileName, SortByEnum.FileModifiedTime
    )

    private fun filterMods(context: Context? = null) {
        refreshCounter()
        filteredMods = allMods
            .takeIf { it.isNotEmpty() }
            ?.filterMods(nameFilter, stateFilter, context)
            ?.sortedWith { o1, o2 ->
                val file1 = o1.localMod.file
                val file2 = o2.localMod.file
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

    fun selectAllMods() {
        filteredMods?.forEach { mod ->
            if (!selectedMods.contains(mod)) {
                selectedMods.add(mod)
            }
        }
        checkCanUpdate()
    }

    fun clearSelected() {
        filteredMods?.let {
            selectedMods.removeAll(it)
        }
        checkCanUpdate()
    }

    fun checkCanUpdate() {
        // 寻找列表中是否存在能够检查远端的模组
        canUpdate = selectedMods.any { it.localMod.checkRemote }
    }

    /** 在ViewModel的生命周期协程内调用 */
    fun doInScope(block: suspend () -> Unit) {
        viewModelScope.launch {
            block()
        }
    }

    private fun startQueueProcessor() {
        viewModelScope.launch {
            while (true) {
                try {
                    ensureActive()
                } catch (_: Exception) {
                    break //取消
                }

                val task = queueMutex.withLock {
                    loadQueue.poll()
                } ?: run {
                    delay(100.milliseconds)
                    continue
                }

                val (mod, loadFromCache) = task
                semaphore.acquire()

                launch {
                    try {
                        mod.load(loadFromCache)
                    } finally {
                        semaphore.release()
                    }
                }
            }
        }
    }

    /** 加载模组远端信息 */
    fun loadMod(mod: RemoteMod, loadFromCache: Boolean = true) {
        //强制刷新：直接加入队列头部并清除旧任务
        if (!loadFromCache) {
            doInScope {
                queueMutex.withLock {
                    loadQueue.removeAll { it.first == mod }
                    loadQueue.addFirst(mod to false) //加入队头优先执行
                }
            }
            if (modsToLoad.contains(mod)) return //已在加载列表
            modsToLoad.add(mod)
            return
        }

        if (modsToLoad.contains(mod)) return

        modsToLoad.add(mod)
        doInScope {
            queueMutex.withLock {
                val canJoin = loadQueue.size <= (initialQueueSize / 2)
                if (canJoin || loadQueue.none { it.first == mod }) {
                    loadQueue.add(mod to true)
                    modsToLoad.add(mod)
                    //若当前是新一轮任务，更新初始队列总数
                    if (initialQueueSize == 0 || canJoin) {
                        initialQueueSize = loadQueue.size
                    }
                }
            }
        }
    }

    override fun onCleared() {
        viewModelScope.cancel()
    }
}


// ------------
// 模组更新
// ------------
private class ModsUpdaterViewModel(
    private val modsDir: File,
    private val version: Version
) : ViewModel() {
    var modsUpdateOperation by mutableStateOf<ModsUpdateOperation>(ModsUpdateOperation.None)
    var modsConfirmOperation by mutableStateOf<ModsConfirmOperation>(ModsConfirmOperation.None)
        private set

    //等待用户确认模组更新
    private var waitingUserContinuation: (Continuation<List<SelectableModManifest>>)? = null
    suspend fun waitingForUserConfirm(list: List<ModManifest>): List<SelectableModManifest> {
        return suspendCancellableCoroutine { cont ->
            waitingUserContinuation = cont
            modsConfirmOperation = ModsConfirmOperation.WaitingConfirm(list.toSelectableList())
        }
    }

    /**
     * 用户确认更新模组
     */
    fun modsUserConfirm(manifests: List<SelectableModManifest>) {
        waitingUserContinuation?.resume(manifests)
        waitingUserContinuation = null
        modsConfirmOperation = ModsConfirmOperation.None
    }

    /**
     * 模组更新器
     */
    var modsUpdater by mutableStateOf<ModUpdater?>(null)

    fun update(
        context: Context,
        mods: List<RemoteMod>,
        refreshMods: () -> Unit,
        onStart: () -> Unit = {},
        onStop: () -> Unit = {}
    ) {
        val minecraftVer = version.getVersionInfo()!!.minecraftVersion
        val modLoader = version.getVersionInfo()!!.loaderInfo!!.loader

        modsUpdater = ModUpdater(
            context = context,
            mods = mods,
            modsDir = modsDir,
            minecraft = minecraftVer,
            modLoader = modLoader,
            scope = viewModelScope,
            waitForUserConfirm = ::waitingForUserConfirm
        ).also {
            modsUpdateOperation = ModsUpdateOperation.Update
            it.updateAll(
                onUpdated = {
                    modsUpdater = null
                    refreshMods()
                    modsUpdateOperation = ModsUpdateOperation.Success
                    onStop()
                },
                onNoModUpdates = {
                    viewModelScope.launch(Dispatchers.Main) {
                        Toast.makeText(context, context.getString(R.string.mods_update_no_mods_update), Toast.LENGTH_SHORT).show()
                    }
                    modsUpdater = null
                    modsUpdateOperation = ModsUpdateOperation.None
                    onStop()
                },
                onCancelled = {
                    modsUpdater = null
                    modsUpdateOperation = ModsUpdateOperation.None
                    onStop()
                },
                onError = { th ->
                    modsUpdater = null
                    refreshMods()
                    modsUpdateOperation = ModsUpdateOperation.Error(th)
                    onStop()
                }
            )
        }
        onStart()
    }

    fun cancel() {
        modsUpdater?.cancel()
        modsUpdater = null
        modsUpdateOperation = ModsUpdateOperation.None
    }

    override fun onCleared() {
        cancel()
    }
}

@Composable
private fun rememberModsManageViewModel(
    version: Version,
    modsDir: File
): ModsManageViewModel {
    return viewModel(
        key = version.toString() + "_" + VersionFolders.MOD.folderName
    ) {
        ModsManageViewModel(modsDir)
    }
}

@Composable
private fun rememberModsUpdaterViewModel(
    version: Version,
    modsDir: File
): ModsUpdaterViewModel {
    return viewModel(
        key = version.toString() + "_ModsUpdater"
    ) {
        ModsUpdaterViewModel(modsDir = modsDir, version = version)
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ModsManagerScreen(
    mainScreenKey: TitledNavKey?,
    versionsScreenKey: TitledNavKey?,
    version: Version,
    backToMainScreen: () -> Unit,
    swapToDownload: () -> Unit,
    onSwapMoreInfo: (id: String, Platform) -> Unit,
    eventViewModel: EventViewModel,
    submitError: (ErrorViewModel.ThrowableMessage) -> Unit
) {
    val context = LocalContext.current

    if (!version.isValid()) {
        backToMainScreen()
        return
    }

    //是否拥有模组加载器
    val hasModLoader = remember(version) {
        version.getVersionInfo()?.loaderInfo?.loader?.isLoader == true
    }
    val modsDir = remember(version) {
        VersionFolders.MOD.getDir(version.getGameDir())
    }

    BaseScreen(
        levels1 = listOf(
            Pair(NestedNavKey.VersionSettings::class.java, mainScreenKey)
        ),
        Triple(NormalNavKey.Versions.ModsManager, versionsScreenKey, false)
    ) { isVisible ->
        val viewModel = rememberModsManageViewModel(version, modsDir)
        val updaterViewModel = rememberModsUpdaterViewModel(version, modsDir)

        //页面创建时，检查一次模组数量，如果不同，则说明有增删
        //可自动刷新一次模组列表
        LaunchedEffect(Unit) {
            viewModel.checkCountAndRefresh(context)
        }

        DeleteAllOperation(
            operation = viewModel.deleteAllOperation,
            changeOperation = { viewModel.deleteAllOperation = it },
            submitError = submitError,
            onRefresh = { viewModel.refresh(context) }
        )

        ModsUpdateOperation(
            operation = updaterViewModel.modsUpdateOperation,
            changeOperation = { updaterViewModel.modsUpdateOperation = it },
            modsUpdater = updaterViewModel.modsUpdater,
            onUpdate = { mods ->
                updaterViewModel.update(
                    context = context,
                    mods = mods,
                    refreshMods = {
                        //刷新模组
                        viewModel.refresh(context)
                    },
                    onStart = {
                        eventViewModel.sendKeepScreen(true)
                    },
                    onStop = {
                        eventViewModel.sendKeepScreen(false)
                    }
                )
            },
            onCancel = {
                updaterViewModel.cancel()
                eventViewModel.sendKeepScreen(false)
            }
        )

        ModsConfirmOperation(
            operation = updaterViewModel.modsConfirmOperation,
            onCancel = {
                updaterViewModel.modsUserConfirm(emptyList())
            },
            onConfirm = { manifests ->
                updaterViewModel.modsUserConfirm(manifests)
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
            when (viewModel.modsState) {
                LoadingState.None -> {
                    var modsOperation by remember { mutableStateOf<ModsOperation>(ModsOperation.None) }
                    /** 运行任务并刷新模组列表 */
                    fun runProgress(task: () -> Unit) {
                        viewModel.doInScope {
                            withContext(Dispatchers.IO) {
                                modsOperation = ModsOperation.Progress
                                task()
                                modsOperation = ModsOperation.None
                                viewModel.refresh(context)
                            }
                        }
                    }

                    ModsOperation(
                        modsOperation = modsOperation,
                        updateOperation = { modsOperation = it },
                        onDelete = { mod ->
                            runProgress {
                                FileUtils.deleteQuietly(mod.file)
                            }
                        }
                    )

                    Column {
                        ModsActionsHeader(
                            modifier = Modifier.fillMaxWidth(),
                            nameFilter = viewModel.nameFilter,
                            onNameFilterChange = { viewModel.updateFilter(it, context) },
                            stateFilter = viewModel.stateFilter,
                            onStateFilterChange = { viewModel.updateStateFilter(it, context) },
                            allModsCount = viewModel.allMods.size,
                            enabledModsCount = viewModel.enabledCount.takeIf { it >= 0 },
                            disabledModsCount = viewModel.disabledCount.takeIf { it >= 0 },
                            supportedSortByEnums = viewModel.supportedSortByEnums,
                            sortByEnum = viewModel.sortByEnum,
                            onSortByChanged = { viewModel.updateSortBy(it, context) },
                            isAscending = viewModel.isAscending,
                            onToggleSortOrder = { viewModel.updateSortOrder(context) },
                            hasModLoader = hasModLoader,
                            onUpdateMods = {
                                if (
                                    updaterViewModel.modsUpdateOperation == ModsUpdateOperation.None &&
                                    viewModel.deleteAllOperation == DeleteAllOperation.None
                                ) {
                                    updaterViewModel.modsUpdateOperation = ModsUpdateOperation.Warning(viewModel.selectedMods)
                                }
                            },
                            modsDir = modsDir,
                            onDeleteAll = {
                                if (
                                    updaterViewModel.modsUpdateOperation == ModsUpdateOperation.None &&
                                    viewModel.deleteAllOperation == DeleteAllOperation.None &&
                                    viewModel.selectedMods.isNotEmpty()
                                ) {
                                    viewModel.deleteAllOperation = DeleteAllOperation.Warning(
                                        files = viewModel.selectedMods.map { mod ->
                                            mod.localMod.file
                                        }
                                    )
                                }
                            },
                            isModsSelected = viewModel.selectedMods.isNotEmpty(),
                            canUpdate = viewModel.canUpdate,
                            onSelectAll = {
                                viewModel.selectAllMods()
                            },
                            onClearModsSelected = {
                                viewModel.clearSelected()
                            },
                            swapToDownload = swapToDownload,
                            refresh = { viewModel.refresh(context) },
                            submitError = submitError
                        )

                        ModsList(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            hasModLoader = hasModLoader,
                            modsList = viewModel.filteredMods,
                            selectedMods = viewModel.selectedMods,
                            removeFromSelected = { mod ->
                                viewModel.selectedMods.remove(mod)
                                viewModel.checkCanUpdate()
                            },
                            addToSelected = { mod ->
                                viewModel.selectedMods.add(mod)
                                viewModel.checkCanUpdate()
                            },
                            onLoad = { mod ->
                                viewModel.loadMod(mod)
                            },
                            onForceRefresh = { mod ->
                                viewModel.loadMod(mod, loadFromCache = false)
                            },
                            onEnable = { mod ->
                                //启用和禁用模组应该避免刷新所有模组，否则将会极度影响体验
                                viewModel.doInScope {
                                    withContext(Dispatchers.IO) {
                                        mod.localMod.enable()
                                    }
                                    withContext(Dispatchers.Main) {
                                        viewModel.refreshCounter()
                                    }
                                }
                            },
                            onDisable = { mod ->
                                viewModel.doInScope {
                                    withContext(Dispatchers.IO) {
                                        mod.localMod.disable()
                                    }
                                    withContext(Dispatchers.Main) {
                                        viewModel.refreshCounter()
                                    }
                                }
                            },
                            onSwapMoreInfo = onSwapMoreInfo,
                            onDelete = { mod ->
                                modsOperation = ModsOperation.Delete(mod.localMod)
                            }
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
private fun ModsActionsHeader(
    modifier: Modifier,
    nameFilter: String,
    onNameFilterChange: (String) -> Unit,
    stateFilter: ModStateFilter,
    onStateFilterChange: (ModStateFilter) -> Unit,
    allModsCount: Int,
    enabledModsCount: Int?,
    disabledModsCount: Int?,
    supportedSortByEnums: List<SortByEnum>,
    sortByEnum: SortByEnum,
    onSortByChanged: (SortByEnum) -> Unit,
    isAscending: Boolean,
    onToggleSortOrder: () -> Unit,
    hasModLoader: Boolean,
    onUpdateMods: () -> Unit,
    modsDir: File,
    onDeleteAll: () -> Unit,
    isModsSelected: Boolean,
    canUpdate: Boolean,
    onSelectAll: () -> Unit,
    onClearModsSelected: () -> Unit,
    swapToDownload: () -> Unit,
    refresh: () -> Unit,
    submitError: (ErrorViewModel.ThrowableMessage) -> Unit = {},
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
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box {
                    var expanded by remember { mutableStateOf(false) }
                    IconButton(
                        onClick = { expanded = !expanded }
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_filter_alt_outlined),
                            contentDescription = stringResource(R.string.mods_update_task_filter)
                        )
                    }
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        shape = MaterialTheme.shapes.large
                    ) {
                        ModStateFilter.entries.forEach { filter ->
                            val count = when (filter) {
                                ModStateFilter.Enabled -> enabledModsCount
                                ModStateFilter.Disabled -> disabledModsCount
                                else -> allModsCount
                            }

                            DropdownMenuItem(
                                text = {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(text = stringResource(filter.textRes))
                                        if (count != null) {
                                            Text(text = "($count)")
                                        }
                                    }
                                },
                                onClick = {
                                    onStateFilterChange(filter)
                                    expanded = false
                                },
                                trailingIcon = if (filter == stateFilter) {
                                    {
                                        Icon(
                                            painter = painterResource(R.drawable.ic_check),
                                            contentDescription = null
                                        )
                                    }
                                } else null
                            )
                        }
                    }
                }

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
                    visible = isModsSelected
                ) {
                    Row {
                        if (hasModLoader && canUpdate) {
                            IconButton(
                                onClick = onUpdateMods
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_update),
                                    contentDescription = null
                                )
                            }
                        }

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
                                if (isModsSelected) onClearModsSelected()
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

                Spacer(modifier = Modifier.width(6.dp))

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
                    val taskBuilder = rememberMultipleUriImportTaskBuilder(
                        id = "ContentManager.Mods.Import",
                        targetDir = modsDir,
                        checkExtension = listOf("jar"),
                        submitError = submitError,
                        onImported = refresh
                    )
                    ImportMultipleFileButton(
                        extension = "jar",
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
private fun ModsList(
    modifier: Modifier = Modifier,
    hasModLoader: Boolean,
    modsList: List<RemoteMod>?,
    selectedMods: List<RemoteMod>,
    removeFromSelected: (RemoteMod) -> Unit,
    addToSelected: (RemoteMod) -> Unit,
    onLoad: (RemoteMod) -> Unit,
    onForceRefresh: (RemoteMod) -> Unit,
    onEnable: (RemoteMod) -> Unit,
    onDisable: (RemoteMod) -> Unit,
    onSwapMoreInfo: (id: String, Platform) -> Unit,
    onDelete: (RemoteMod) -> Unit
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        val scrollState = rememberLazyListState()
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .scrollbar(
                    state = scrollState.scrollIndicatorState,
                    orientation = Orientation.Vertical,
                ),
            contentPadding = PaddingValues(all = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            state = scrollState,
        ) {
            if (!hasModLoader) {
                item(key = "warning_no_modloader") {
                    WarningItem(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = stringResource(R.string.mods_manage_no_loader),
                            style = MaterialTheme.typography.titleSmall,
                        )
                    }
                }
            }

            val list = modsList ?: emptyList()
            items(list) { mod ->
                ModItemLayout(
                    modifier = Modifier.fillMaxWidth(),
                    mod = mod,
                    onLoad = {
                        onLoad(mod)
                    },
                    onForceRefresh = {
                        onForceRefresh(mod)
                    },
                    onClick = {
                        //仅加载了项目信息的模组允许被选择
                        if (selectedMods.contains(mod)) {
                            removeFromSelected(mod)
                        } else {
                            addToSelected(mod)
                        }
                    },
                    onEnable = {
                        onEnable(mod)
                    },
                    onDisable = {
                        onDisable(mod)
                    },
                    onSwapMoreInfo = onSwapMoreInfo,
                    onDelete = {
                        onDelete(mod)
                    },
                    selected = selectedMods.contains(mod)
                )
            }
        }

        //一些重要的标签
        if (modsList == null) {
            //如果为null，则代表本身就没有模组可以展示
            ScalingLabel(
                text = stringResource(R.string.mods_manage_no_mods)
            )
        } else if (modsList.isEmpty()) {
            //如果列表是空的，则是由搜索导致的
            //展示“无匹配项”文本
            ScalingLabel(
                text = stringResource(R.string.generic_no_matching_items)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ModItemLayout(
    modifier: Modifier = Modifier,
    mod: RemoteMod,
    onLoad: () -> Unit = {},
    onForceRefresh: () -> Unit = {},
    onClick: () -> Unit = {},
    onEnable: () -> Unit,
    onDisable: () -> Unit,
    onSwapMoreInfo: (id: String, Platform) -> Unit,
    onDelete: () -> Unit,
    selected: Boolean,
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
    val context = LocalContext.current

    val projectInfo = mod.projectInfo

    LaunchedEffect(mod) {
        //尝试加载该模组文件在平台上所属的项目
        onLoad()
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
            //模组的封面图标
            ModIcon(
                modifier = Modifier.clip(shape = RoundedCornerShape(10.dp)),
                mod = mod,
                iconSize = 48.dp
            )

            //模组简要信息
            Crossfade(
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .weight(1f),
                //在本地是否为未知文件
                targetState = mod.localMod.notMod && projectInfo == null,
                label = "ModItemInfoCrossfade"
            ) { isUnknown ->
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val localMod = mod.localMod
                    when {
                        isUnknown -> {
                            //非模组，只展示文件名称
                            Text(
                                modifier = Modifier.basicMarquee(iterations = Int.MAX_VALUE),
                                text = localMod.file.name,
                                style = MaterialTheme.typography.titleSmall,
                                maxLines = 1
                            )
                            if (localMod.loader != ModLoader.UNKNOWN) {
                                LittleTextLabel(
                                    text = localMod.loader.displayName,
                                    shape = MaterialTheme.shapes.small
                                )
                            }
                        }
                        else -> {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val displayTitle = if (projectInfo != null) {
                                    val title = projectInfo.title
                                    mod.mcMod?.getMcmodTitle(title, context) ?: title
                                } else {
                                    localMod.name
                                }
                                Text(
                                    modifier = Modifier
                                        .weight(1f, fill = false)
                                        .basicMarquee(iterations = Int.MAX_VALUE)
                                        .animateContentSize(),
                                    text = displayTitle,
                                    style = MaterialTheme.typography.titleSmall,
                                    maxLines = 1
                                )
                                Row(
                                    modifier = Modifier
                                        .basicMarquee(iterations = Int.MAX_VALUE)
                                        .animateContentSize(),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    val remoteLoaders = mod.remoteFile?.loaders
                                    if (remoteLoaders != null && remoteLoaders.isNotEmpty()) {
                                        remoteLoaders.forEach { loader ->
                                            LittleTextLabel(
                                                text = loader.getDisplayName(),
                                                shape = MaterialTheme.shapes.small
                                            )
                                        }
                                    } else if (localMod.loader != ModLoader.UNKNOWN) {
                                        LittleTextLabel(
                                            text = localMod.loader.displayName,
                                            shape = MaterialTheme.shapes.small
                                        )
                                    }
                                }
                            }

                            Text(
                                modifier = Modifier
                                    .alpha(0.7f)
                                    .basicMarquee(iterations = Int.MAX_VALUE),
                                text = stringResource(R.string.generic_file_name, localMod.file.name),
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.align(Alignment.CenterVertically),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (mod.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .size(18.dp)
                            .alpha(0.7f),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                } else if (mod.isLoaded) {
                    IconButton(
                        modifier = Modifier.size(38.dp),
                        onClick = onForceRefresh
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_refresh),
                            contentDescription = stringResource(R.string.generic_refresh)
                        )
                    }
                }

                //启用/禁用
                Checkbox(
                    checked = mod.localMod.file.isEnabled(),
                    onCheckedChange = { checked ->
                        if (checked) onEnable()
                        else onDisable()
                    }
                )

                //详细信息展示
                if (projectInfo == null) {
                    if (!mod.localMod.notMod) {
                        LocalModInfoTooltip(mod.localMod)
                    }
                } else {
                    IconButton(
                        modifier = Modifier.size(38.dp),
                        onClick = {
                            onSwapMoreInfo(projectInfo.id, projectInfo.platform)
                        }
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_info_outlined),
                            contentDescription = stringResource(R.string.mods_manage_info)
                        )
                    }
                }

                IconButton(
                    modifier = Modifier.size(38.dp),
                    onClick = onDelete
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_delete_outlined),
                        contentDescription = stringResource(R.string.generic_delete)
                    )
                }
            }
        }
    }
}

@Composable
private fun ModIcon(
    modifier: Modifier = Modifier,
    mod: RemoteMod,
    iconSize: Dp,
    disableContainerSize: Dp = 28.dp
) {
    Box(modifier = modifier) {
        val colorMatrix = remember(mod, mod.localMod.file) { ColorMatrix() }
        colorMatrix.setToSaturation(
            if (mod.localMod.file.isDisabled()) 0f
            else 1f
        )

        val projectInfo = mod.projectInfo
        if (projectInfo == null) {
            if (mod.localMod.icon == null) {
                ModLoaderIcon(
                    modifier = Modifier.size(iconSize),
                    modloader = mod.localMod.loader,
                    defaultIcon = R.drawable.ic_unknown_pack,
                    colorFilter = ColorFilter.colorMatrix(colorMatrix),

                )
            } else {
                ByteArrayIcon(
                    modifier = Modifier.size(iconSize),
                    triggerRefresh = mod,
                    icon = mod.localMod.icon,
                    colorFilter = ColorFilter.colorMatrix(colorMatrix),
                )
            }
        } else {
            AssetsIcon(
                iconUrl = projectInfo.iconUrl,
                size = iconSize,
                colorFilter = ColorFilter.colorMatrix(colorMatrix)
            )
        }

        AnimatedVisibility(
            modifier = Modifier.align(Alignment.Center),
            visible = mod.localMod.file.isDisabled(),
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Surface(
                modifier = Modifier
                    .padding(all = 4.dp)
                    .size(disableContainerSize),
                color = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                shape = CircleShape,
                shadowElevation = 4.dp
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_block_outlined),
                    contentDescription = null
                )
            }
        }
    }
}

/**
 * 在模组列表中穿插的警告文本项
 */
@Composable
private fun WarningItem(
    modifier: Modifier = Modifier,
    itemColor: Color = itemColor(),
    itemContentColor: Color = onItemColor(),
    shape: Shape = MaterialTheme.shapes.large,
    content: @Composable ColumnScope.() -> Unit
) {
    val scale = remember { Animatable(initialValue = 0.95f) }
    LaunchedEffect(Unit) {
        scale.animateTo(targetValue = 1f, animationSpec = getAnimateTween())
    }

    Surface(
        modifier = modifier
            .graphicsLayer(scaleY = scale.value, scaleX = scale.value),
        onClick = {},
        shape = shape,
        color = itemColor,
        contentColor = itemContentColor,
    ) {
        Row(
            modifier = Modifier.padding(all = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(48.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(all = 8.dp),
                    painter = painterResource(R.drawable.ic_warning_filled),
                    contentDescription = stringResource(R.string.generic_warning),
                    tint = MaterialTheme.colorScheme.tertiary
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                content = content
            )
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun LocalModInfoTooltip(
    mod: LocalMod
) {
    TooltipIconButton(
        modifier = Modifier.size(38.dp),
        tooltip = {
            RichTooltip(
                modifier = Modifier.padding(all = 3.dp),
                title = { Text(text = stringResource(R.string.mods_manage_info)) },
                shadowElevation = 3.dp
            ) {
                Column {
                    //文件大小
                    Text(text = stringResource(R.string.generic_file_size, formatFileSize(mod.fileSize)))
                    //模组版本
                    mod.version?.let { version ->
                        Text(text = stringResource(R.string.mods_manage_version, version))
                    }
                    //作者
                    Row {
                        Text(text = stringResource(R.string.mods_manage_authors))
                        FlowRow(
                            modifier = Modifier.weight(1f, fill = false),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            mod.authors.forEach { author ->
                                Text(text = author)
                            }
                        }
                    }
                    //模组描述
                    mod.description?.takeIf { it.isNotEmptyOrBlank() }?.let { description ->
                        Row {
                            Text(text = stringResource(R.string.mods_manage_description))
                            Text(
                                modifier = Modifier.weight(1f, fill = false),
                                text = description
                            )
                        }
                    }
                }
            }
        }
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_info_outlined),
            contentDescription = stringResource(R.string.mods_manage_info)
        )
    }
}

