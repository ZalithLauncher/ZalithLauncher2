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

package com.movtery.zalithlauncher.ui.screens.content

import android.content.Context
import android.os.Environment
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.game.path.GamePathManager
import com.movtery.zalithlauncher.game.version.installed.Version
import com.movtery.zalithlauncher.game.version.installed.VersionComparator
import com.movtery.zalithlauncher.game.version.installed.VersionType
import com.movtery.zalithlauncher.game.version.installed.VersionsManager
import com.movtery.zalithlauncher.game.version.installed.cleanup.GameAssetCleaner
import com.movtery.zalithlauncher.ui.activities.MainActivity
import com.movtery.zalithlauncher.ui.base.BaseScreen
import com.movtery.zalithlauncher.ui.components.BackgroundCard
import com.movtery.zalithlauncher.ui.components.CardTitleLayout
import com.movtery.zalithlauncher.ui.components.EdgeDirection
import com.movtery.zalithlauncher.ui.components.IconTextButton
import com.movtery.zalithlauncher.ui.components.MarqueeText
import com.movtery.zalithlauncher.ui.components.ScalingActionButton
import com.movtery.zalithlauncher.ui.components.ScalingLabel
import com.movtery.zalithlauncher.ui.components.fadeEdge
import com.movtery.zalithlauncher.ui.screens.NormalNavKey
import com.movtery.zalithlauncher.ui.screens.content.elements.CleanupOperation
import com.movtery.zalithlauncher.ui.screens.content.elements.GamePathItemLayout
import com.movtery.zalithlauncher.ui.screens.content.elements.GamePathOperation
import com.movtery.zalithlauncher.ui.screens.content.elements.VersionCategory
import com.movtery.zalithlauncher.ui.screens.content.elements.VersionCategoryItem
import com.movtery.zalithlauncher.ui.screens.content.elements.VersionItemLayout
import com.movtery.zalithlauncher.ui.screens.content.elements.VersionsOperation
import com.movtery.zalithlauncher.utils.animation.swapAnimateDpAsState
import com.movtery.zalithlauncher.utils.checkStoragePermissions
import com.movtery.zalithlauncher.viewmodel.ErrorViewModel
import com.movtery.zalithlauncher.viewmodel.EventViewModel
import com.movtery.zalithlauncher.viewmodel.ScreenBackStackViewModel
import com.movtery.zalithlauncher.viewmodel.sendKeepScreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

private class VersionsScreenViewModel : ViewModel() {
    /** 版本类别分类 */
    var versionCategory by mutableStateOf(VersionCategory.ALL)
        private set

    /** 游戏路径相关操作 */
    var gamePathOperation by mutableStateOf<GamePathOperation>(GamePathOperation.None)

    private val _versions = MutableStateFlow<List<Version>>(emptyList())
    val versions = _versions.asStateFlow()

    /** 全部版本的数量 */
    var allVersionsCount by mutableIntStateOf(0)
        private set
    /** 原版版本数量 */
    var vanillaVersionsCount by mutableIntStateOf(0)
        private set
    /** 模组加载器版本数量 */
    var modloaderVersionsCount by mutableIntStateOf(0)
        private set

    fun startRefreshVersions() {
        if (!VersionsManager.isRefreshing.value) {
            _versions.update { emptyList() }
            VersionsManager.refresh("VersionsScreenViewModel.startRefreshVersions")
        }
    }

    /**
     * 刷新当前版本列表
     */
    suspend fun refreshVersions(
        currentVersions: List<Version>,
        clearCurrent: Boolean = true
    ) {
        withContext(Dispatchers.Main) {
            if (clearCurrent) {
                _versions.update { emptyList() }
            }

            val filteredVersions = withContext(Dispatchers.Default) {
                allVersionsCount = currentVersions.size

                val vanillaVersions = currentVersions
                    .filter { ver -> ver.versionType == VersionType.VANILLA }
                    .also { vanillaVersionsCount = it.size }
                val modloaderVersions = currentVersions
                    .filter { ver -> ver.versionType == VersionType.MODLOADERS }
                    .also { modloaderVersionsCount = it.size }

                when (versionCategory) {
                    VersionCategory.ALL -> currentVersions
                    VersionCategory.VANILLA -> vanillaVersions
                    VersionCategory.MODLOADER -> modloaderVersions
                }
            }

            _versions.update {
                filteredVersions.sortedWith(VersionComparator)
            }
        }
    }

    private var currentJob: Job? = null
    private var mutex: Mutex = Mutex()

    /**
     * 变更当前版本列表的过滤类型
     */
    fun changeCategory(category: VersionCategory) {
        currentJob?.cancel()
        currentJob = viewModelScope.launch {
            mutex.withLock {
                this@VersionsScreenViewModel.versionCategory = category
                refreshVersions(VersionsManager.versions, false)
            }
        }
    }

    /**
     * 重新排序当前版本列表
     */
    fun resortVersions() {
        _versions.update {
            it.sortedWith(VersionComparator)
        }
    }

    /** 清理游戏文件操作 */
    var cleanupOperation by mutableStateOf<CleanupOperation>(CleanupOperation.None)

    /** 游戏无用资源清理者 */
    var cleaner by mutableStateOf<GameAssetCleaner?>(null)

    fun cleanUnusedFiles(
        context: Context,
        onStart: () -> Unit = {},
        onStop: () -> Unit = {}
    ) {
        cleaner = GameAssetCleaner(
            context = context,
            scope = viewModelScope
        ).also {
            cleanupOperation = CleanupOperation.Clean
            it.start(
                onEnd = { count, size ->
                    cleaner = null
                    cleanupOperation = CleanupOperation.Success(count, size)
                    onStop()
                },
                onThrowable = { th ->
                    cleaner = null
                    cleanupOperation = CleanupOperation.Error(th)
                    onStop()
                }
            )
        }
        onStart()
    }

    fun cancelCleaner() {
        cleaner?.cancel()
        cleaner = null
        cleanupOperation = CleanupOperation.None
    }

    private val listener: suspend (List<Version>) -> Unit = { versions ->
        refreshVersions(versions)
    }

    init {
        viewModelScope.launch {
            //初始化时刷新一次版本
            refreshVersions(VersionsManager.versions)
        }

        VersionsManager.registerListener(listener)
    }

    override fun onCleared() {
        cancelCleaner()
        VersionsManager.unregisterListener(listener)
        currentJob?.cancel()
    }
}

@Composable
private fun rememberVersionViewModel() : VersionsScreenViewModel {
    return viewModel(
        key = NormalNavKey.VersionsManager.toString()
    ) {
        VersionsScreenViewModel()
    }
}

@Composable
fun VersionsManageScreen(
    backScreenViewModel: ScreenBackStackViewModel,
    navigateToVersions: (Version) -> Unit,
    navigateToExport: (Version) -> Unit,
    eventViewModel: EventViewModel,
    submitError: (ErrorViewModel.ThrowableMessage) -> Unit
) {
    val viewModel = rememberVersionViewModel()
    val context = LocalContext.current

    val versions by viewModel.versions.collectAsStateWithLifecycle()
    val currentVersion by VersionsManager.currentVersion.collectAsStateWithLifecycle()
    val isRefreshing by VersionsManager.isRefreshing.collectAsStateWithLifecycle()

    GamePathOperation(
        gamePathOperation = viewModel.gamePathOperation,
        changeState = { viewModel.gamePathOperation = it },
        submitError = submitError
    )

    BaseScreen(
        screenKey = NormalNavKey.VersionsManager,
        currentKey = backScreenViewModel.mainScreen.currentKey
    ) { isVisible ->
        Row {
            LeftMenu(
                isVisible = isVisible,
                isRefreshing = isRefreshing,
                swapToFileSelector = { path ->
                    backScreenViewModel.mainScreen.backStack.navigateToFileSelector(
                        startPath = path,
                        selectFile = false,
                        saveKey = NormalNavKey.VersionsManager
                    ) { path ->
                        viewModel.gamePathOperation = GamePathOperation.AddNewPath(path)
                    }
                },
                onCleanupGameFiles = {
                    if (viewModel.cleanupOperation == CleanupOperation.None) {
                        viewModel.cleanupOperation = CleanupOperation.Tip
                    }
                },
                changePathOperation = {
                    viewModel.gamePathOperation = it
                },
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(2.5f)
            )

            VersionsLayout(
                isVisible = isVisible,
                isRefreshing = isRefreshing,
                versions = versions,
                currentVersion = currentVersion,
                versionCategory = viewModel.versionCategory,
                onCategoryChange = { viewModel.changeCategory(it) },
                allVersionsCount = viewModel.allVersionsCount,
                vanillaVersionsCount = viewModel.vanillaVersionsCount,
                modloaderVersionsCount = viewModel.modloaderVersionsCount,
                navigateToVersions = navigateToVersions,
                navigateToExport = navigateToExport,
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(7.5f)
                    .padding(vertical = 12.dp)
                    .padding(end = 12.dp),
                submitError = submitError,
                onRefresh = {
                    viewModel.startRefreshVersions()
                },
                onVersionPinned = {
                    viewModel.resortVersions()
                },
                onInstall = {
                    backScreenViewModel.navigateToDownload()
                }
            )

            CleanupOperation(
                operation = viewModel.cleanupOperation,
                changeOperation = { viewModel.cleanupOperation = it },
                cleaner = viewModel.cleaner,
                onClean = {
                    viewModel.cleanUnusedFiles(
                        context = context,
                        onStart = {
                            eventViewModel.sendKeepScreen(true)
                        },
                        onStop = {
                            eventViewModel.sendKeepScreen(false)
                        }
                    )
                },
                onCancel = {
                    viewModel.cancelCleaner()
                    eventViewModel.sendKeepScreen(false)
                },
                submitError = submitError
            )
        }
    }
}

@Composable
private fun LeftMenu(
    isVisible: Boolean,
    isRefreshing: Boolean,
    swapToFileSelector: (path: String) -> Unit,
    onCleanupGameFiles: () -> Unit,
    changePathOperation: (GamePathOperation) -> Unit,
    modifier: Modifier = Modifier
) {
    val surfaceXOffset by swapAnimateDpAsState(
        targetValue = (-40).dp,
        swapIn = isVisible,
        isHorizontal = true
    )

    Column(
        modifier = modifier.offset { IntOffset(x = surfaceXOffset.roundToPx(), y = 0) },
    ) {
        val gamePaths by GamePathManager.gamePathData.collectAsStateWithLifecycle()
        val currentPath by GamePathManager.currentPath.collectAsStateWithLifecycle()
        val context = LocalContext.current

        LazyColumn(
            modifier = Modifier
                .padding(all = 12.dp)
                .fillMaxWidth()
                .weight(1f)
        ) {
            items(gamePaths, key = { it.id }) { pathItem ->
                GamePathItemLayout(
                    item = pathItem,
                    selected = currentPath == pathItem.path,
                    onClick = {
                        if (!isRefreshing) { //避免频繁刷新，防止currentGameInfo意外重置
                            if (pathItem.id == GamePathManager.DEFAULT_ID) {
                                GamePathManager.saveDefaultPath()
                            } else {
                                (context as? MainActivity)?.let { activity ->
                                    checkStoragePermissions(
                                        activity = activity,
                                        message = activity.getString(R.string.versions_manage_game_storage_permissions),
                                        messageSdk30 = activity.getString(R.string.versions_manage_game_storage_permissions_sdk30),
                                        hasPermission = {
                                            GamePathManager.saveCurrentPath(pathItem.id)
                                        }
                                    )
                                }
                            }
                        }
                    },
                    onDelete = {
                        changePathOperation(GamePathOperation.DeletePath(pathItem))
                    },
                    onRename = {
                        changePathOperation(GamePathOperation.RenamePath(pathItem))
                    }
                )
            }
        }

        ScalingActionButton(
            modifier = Modifier
                .padding(horizontal = 12.dp)
                .padding(top = 8.dp)
                .fillMaxWidth(),
            onClick = {
                (context as? MainActivity)?.let { activity ->
                    checkStoragePermissions(
                        activity = activity,
                        message = activity.getString(R.string.versions_manage_game_path_storage_permissions),
                        messageSdk30 = activity.getString(R.string.versions_manage_game_path_storage_permissions_sdk30),
                        hasPermission = {
                            swapToFileSelector(Environment.getExternalStorageDirectory().absolutePath)
                        }
                    )
                }
            }
        ) {
            MarqueeText(text = stringResource(R.string.versions_manage_game_path_add_new))
        }

        ScalingActionButton(
            modifier = Modifier
                .padding(PaddingValues(horizontal = 12.dp, vertical = 8.dp))
                .fillMaxWidth(),
            onClick = onCleanupGameFiles
        ) {
            MarqueeText(text = stringResource(R.string.versions_manage_cleanup))
        }
    }
}

@Composable
private fun VersionsLayout(
    modifier: Modifier = Modifier,
    isVisible: Boolean,
    isRefreshing: Boolean,
    versions: List<Version>,
    currentVersion: Version?,
    versionCategory: VersionCategory,
    onCategoryChange: (VersionCategory) -> Unit,
    allVersionsCount: Int,
    vanillaVersionsCount: Int,
    modloaderVersionsCount: Int,
    navigateToVersions: (Version) -> Unit,
    navigateToExport: (Version) -> Unit,
    submitError: (ErrorViewModel.ThrowableMessage) -> Unit,
    onRefresh: () -> Unit,
    onVersionPinned: () -> Unit,
    onInstall: () -> Unit,
) {
    val surfaceYOffset by swapAnimateDpAsState(
        targetValue = (-40).dp,
        swapIn = isVisible
    )

    BackgroundCard(
        modifier = modifier.offset { IntOffset(x = 0, y = surfaceYOffset.roundToPx()) },
        shape = MaterialTheme.shapes.extraLarge
    ) {
        if (isRefreshing) { //版本正在刷新中
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                LoadingIndicator()
            }
        } else {
            var versionsOperation by remember { mutableStateOf<VersionsOperation>(VersionsOperation.None) }
            VersionsOperation(
                versionsOperation = versionsOperation,
                updateVersionsOperation = { versionsOperation = it },
                submitError = submitError
            )

            Column(modifier = Modifier.fillMaxSize()) {
                CardTitleLayout {
                    val scrollState = rememberScrollState()
                    Row(
                        modifier = Modifier
                            .fadeEdge(
                                state = scrollState,
                                length = 32.dp,
                                direction = EdgeDirection.Horizontal
                            )
                            .fillMaxWidth()
                            .horizontalScroll(state = scrollState)
                            .padding(all = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconTextButton(
                            onClick = onRefresh,
                            painter = painterResource(R.drawable.ic_refresh),
                            contentDescription = stringResource(R.string.generic_refresh),
                            text = stringResource(R.string.generic_refresh)
                        )
                        IconTextButton(
                            onClick = onInstall,
                            painter = painterResource(R.drawable.ic_download),
                            contentDescription = stringResource(R.string.versions_manage_install_new),
                            text = stringResource(R.string.versions_manage_install_new),
                        )
                        //版本分类
                        VersionCategoryItem(
                            value = VersionCategory.ALL,
                            versionsCount = allVersionsCount,
                            selected = versionCategory == VersionCategory.ALL,
                            onClick = { onCategoryChange(VersionCategory.ALL) }
                        )
                        VersionCategoryItem(
                            value = VersionCategory.VANILLA,
                            versionsCount = vanillaVersionsCount,
                            selected = versionCategory == VersionCategory.VANILLA,
                            onClick = { onCategoryChange(VersionCategory.VANILLA) }
                        )
                        VersionCategoryItem(
                            value = VersionCategory.MODLOADER,
                            versionsCount = modloaderVersionsCount,
                            selected = versionCategory == VersionCategory.MODLOADER,
                            onClick = { onCategoryChange(VersionCategory.MODLOADER) }
                        )
                    }
                }

                if (versions.isNotEmpty()) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .clipToBounds(),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        items(versions, key = { it.toString() }) { version ->
                            VersionItemLayout(
                                version = version,
                                selected = version == currentVersion,
                                submitError = submitError,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp)
                                    .animateItem(),
                                onSelected = {
                                    if (version == currentVersion) return@VersionItemLayout
                                    if (!VersionsManager.saveVersion(version)) {
                                        //不允许选择无效版本
                                        versionsOperation = VersionsOperation.InvalidDelete(version)
                                    }
                                },
                                onSettingsClick = {
                                    navigateToVersions(version)
                                },
                                onRenameClick = { versionsOperation = VersionsOperation.Rename(version) },
                                onCopyClick = { versionsOperation = VersionsOperation.Copy(version) },
                                onExportClick = { navigateToExport(version) },
                                onDeleteClick = { versionsOperation = VersionsOperation.Delete(version) },
                                onPinned = onVersionPinned
                            )
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        ScalingLabel(
                            modifier = Modifier.align(Alignment.Center),
                            text = stringResource(R.string.versions_manage_no_versions)
                        )
                    }
                }
            }
        }
    }
}