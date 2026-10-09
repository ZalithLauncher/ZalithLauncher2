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

package com.movtery.zalithlauncher.ui.screens.content.download.assets.search

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.movtery.zalithlauncher.game.download.assets.platform.Platform
import com.movtery.zalithlauncher.game.download.assets.platform.PlatformClasses
import com.movtery.zalithlauncher.game.download.assets.platform.PlatformDisplayLabel
import com.movtery.zalithlauncher.game.download.assets.platform.PlatformFilterCode
import com.movtery.zalithlauncher.game.download.assets.platform.PlatformSearchFilter
import com.movtery.zalithlauncher.game.download.assets.platform.PlatformSearchResult
import com.movtery.zalithlauncher.game.download.assets.platform.loadRawPersistedData
import com.movtery.zalithlauncher.game.download.assets.platform.loadSearchFilter
import com.movtery.zalithlauncher.game.download.assets.platform.resolvePersistedCategories
import com.movtery.zalithlauncher.game.download.assets.platform.resolvePersistedModloader
import com.movtery.zalithlauncher.game.download.assets.platform.saveSearchFilter
import com.movtery.zalithlauncher.game.download.assets.platform.navigatePage
import com.movtery.zalithlauncher.game.download.assets.platform.nextPage
import com.movtery.zalithlauncher.game.download.assets.platform.previousPage
import com.movtery.zalithlauncher.game.download.assets.platform.searchAssets
import com.movtery.zalithlauncher.game.download.assets.utils.ModTranslations
import com.movtery.zalithlauncher.game.download.assets.utils.searchMcMods
import com.movtery.zalithlauncher.game.version.installed.VersionsManager
import com.movtery.zalithlauncher.game.version.mod.InstalledMod
import com.movtery.zalithlauncher.ui.base.BaseScreen
import com.movtery.zalithlauncher.ui.screens.NestedNavKey
import com.movtery.zalithlauncher.ui.screens.TitledNavKey
import com.movtery.zalithlauncher.ui.screens.content.download.assets.elements.AssetsPage
import com.movtery.zalithlauncher.ui.screens.content.download.assets.elements.ResultListLayout
import com.movtery.zalithlauncher.ui.screens.content.download.assets.elements.SearchAssetsState
import com.movtery.zalithlauncher.ui.screens.content.download.assets.elements.SearchFilter
import com.movtery.zalithlauncher.utils.animation.swapAnimateDpAsState
import com.movtery.zalithlauncher.utils.string.containsChinese
import com.movtery.zalithlauncher.utils.logging.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val TAG = "SearchAssetsScreen"

/**
 * 资源搜索屏幕的 view model
 * @param initialPlatform 初始设定的平台
 * @param platformClasses 资源搜索的类型
 * @param filterPersistenceKey MMKV中保存过滤器状态的键，为空则不持久化
 * @param getCategories 根据平台获取可用的资源类别过滤器（用于恢复持久化的类别选择）
 * @param getModloaders 根据平台获取可用的模组加载器过滤器（用于恢复持久化的模组加载器）
 * @param autoSelectEnabled 是否允许在进入屏幕时自动预选游戏版本（由"自动选择下载内容"设置控制）
 */
private class SearchScreenViewModel(
    initialPlatform: Platform,
    private val platformClasses: PlatformClasses,
    private val filterPersistenceKey: String? = null,
    private val getCategories: ((Platform) -> List<PlatformFilterCode>)? = null,
    private val getModloaders: ((Platform) -> List<PlatformDisplayLabel>)? = null,
    private val autoSelectEnabled: Boolean = true
): ViewModel() {
    var searchResult by mutableStateOf<SearchAssetsState>(SearchAssetsState.Searching)
    val pages = mutableStateListOf<AssetsPage?>()

    var searchPlatform by mutableStateOf(initialPlatform)
    var searchFilter by mutableStateOf(PlatformSearchFilter())

    /** 过滤器变更时的回调，由外部设置以持久化保存过滤器 */
    var onFilterChange: (PlatformSearchFilter) -> Unit = {}

    private val _searchedMcMods = MutableStateFlow<List<ModTranslations.McMod>>(emptyList())
    /** 搜索得到的所有 MCMOD 项目 */
    val searchedMcMods = _searchedMcMods.asStateFlow()
    var currentSearchJob: Job? = null
    var currentSearchMCMODSJob: Job? = null

    /**
     * Issue #9: 已安装的Minecraft版本号（用于在版本列表顶部显示，并渲染已安装星标）。
     * 持续订阅 [VersionsManager.versions]，因此安装/删除版本、导入/移除实例后会自动刷新，
     * 无需重启启动器；缓存为一个已 distinct 的 List，滚动时不会重复触发文件系统扫描——
     * 真正的扫描仍然只发生在 VersionsManager.refresh() 内部（由安装/删除/导入等操作触发）。
     */
    var installedVersionIds: List<String> by mutableStateOf(
        VersionsManager.versions.value.mapNotNull { it.getVersionInfo()?.minecraftVersion }.distinct()
    )
        private set

    init {
        viewModelScope.launch {
            VersionsManager.versions.collect { versions ->
                installedVersionIds = versions.mapNotNull { it.getVersionInfo()?.minecraftVersion }.distinct()
            }
        }
    }

    /**
     * 仅更新搜索名称
     */
    fun updateFilter(searchName: String) {
        searchFilter = searchFilter.copy(searchName = searchName)
        currentSearchMCMODSJob?.cancel()
        currentSearchMCMODSJob = viewModelScope.launch {
            if (!searchName.containsChinese()) {
                _searchedMcMods.update { emptyList() }
                currentSearchMCMODSJob = null
                return@launch
            }
            // Chinese mod-name matching runs an LCS search over the local translation index.
            // Wait for a brief pause in typing so rapid edits don't repeat that work per keystroke.
            delay(180)
            val result = searchName.searchMcMods(classes = platformClasses)
                .orEmpty()
                .take(20) //仅展示20个搜索结果
            if (searchFilter.searchName == searchName) {
                _searchedMcMods.update { result }
            }
            currentSearchMCMODSJob = null
        }
    }

    /**
     * 重置并重新搜索
     */
    fun resetSearch() {
        pages.clear()
        searchFilter = searchFilter.copy(index = 0) //重置索引到起始处
        search()
    }

    /**
     * 更新过滤器时，重置已有结果，重新触发搜索
     */
    fun researchWithFilter(filter: PlatformSearchFilter) {
        pages.clear()
        searchFilter = filter.copy(index = 0) //重置索引到起始处
        persistFilter()
        search()
    }

    /**
     * 将当前过滤器状态持久化到 MMKV
     */
    private fun persistFilter() {
        if (filterPersistenceKey != null) {
            saveSearchFilter(filterPersistenceKey, searchFilter)
        }
    }

    private suspend fun putResult(result: PlatformSearchResult) {
        val page = withContext(Dispatchers.Default) {
            result.getAssetsPage(platformClasses)
        }
        Logger.info(TAG, "Searched page info: {pageNumber: ${page.pageNumber}, pageIndex: ${page.pageIndex}, totalPage: ${page.totalPage}, isLastPage: ${page.isLastPage}}")

        val targetIndex = page.pageNumber - 1
        if (pages.size > targetIndex) {
            pages[targetIndex] = page //替换已有页
        } else {
            while (pages.size < targetIndex) {
                pages += null
            }
            pages += page
        }

        searchResult = SearchAssetsState.Success(page)
    }

    fun search() {
        currentSearchJob?.cancel() //取消上一个搜索
        currentSearchMCMODSJob?.cancel()

        currentSearchJob = viewModelScope.launch {
            searchResult = SearchAssetsState.Searching
            searchAssets(
                searchPlatform = searchPlatform,
                searchFilter = searchFilter,
                platformClasses = platformClasses,
                onSuccess = { result ->
                    putResult(result)
                },
                onError = {
                    searchResult = it
                }
            )
        }
    }

    init {
        //从 MMKV 恢复持久化的过滤器状态（游戏版本为会话状态，不从持久化恢复）
        if (filterPersistenceKey != null) {
            val filter = loadSearchFilter(filterPersistenceKey)
            if (filter != null) {
                searchFilter = searchFilter.copy(
                    // gameVersion is session-only — intentionally not restored from persistence
                    sortField = filter.sortField
                )
            }

            //恢复类别和模组加载器
            val raw = loadRawPersistedData(filterPersistenceKey)
            if (raw != null) {
                if (getCategories != null) {
                    val resolvedCategories = resolvePersistedCategories(
                        names = raw.categories,
                        getCategories = getCategories,
                        platform = searchPlatform
                    )
                    searchFilter = searchFilter.copy(categories = resolvedCategories)
                }
                if (getModloaders != null) {
                    val resolvedModloader = resolvePersistedModloader(
                        name = raw.modloader,
                        getModloaders = getModloaders,
                        platform = searchPlatform
                    )
                    searchFilter = searchFilter.copy(modloader = resolvedModloader)
                }
            }
        }

        // Issue #9: 如果"自动选择下载内容"已启用，且只有一个已安装版本，则自动预选该版本
        // Game Version auto-selection is controlled solely by the autoSelectEnabled setting.
        if (autoSelectEnabled && searchFilter.gameVersion == null && installedVersionIds.size == 1) {
            searchFilter = searchFilter.copy(gameVersion = installedVersionIds.first())
        }

        //初始化后，执行一次搜索
        search()
    }

    override fun onCleared() {
        currentSearchJob?.cancel()
        currentSearchMCMODSJob?.cancel()
    }
}

@Composable
private fun rememberSearchAssetsViewModel(
    navKey: TitledNavKey,
    initialPlatform: Platform,
    platformClasses: PlatformClasses,
    filterPersistenceKey: String? = null,
    getCategories: ((Platform) -> List<PlatformFilterCode>)? = null,
    getModloaders: ((Platform) -> List<PlatformDisplayLabel>)? = null,
    autoSelectEnabled: Boolean = true
): SearchScreenViewModel {
    val screenKey = navKey.toString()
    return viewModel(
        key = "${screenKey}_search"
    ) {
        SearchScreenViewModel(initialPlatform, platformClasses, filterPersistenceKey, getCategories, getModloaders, autoSelectEnabled)
    }
}

/**
 * @param parentScreenKey 父屏幕Key
 * @param parentCurrentKey 父屏幕当前Key
 * @param screenKey 屏幕的Key
 * @param currentKey 当前的Key
 * @param platformClasses 搜索资源的分类
 * @param initialPlatform 初始搜索平台
 * @param onPlatformChange 搜索平台变更
 * @param enablePlatform 是否允许更改平台
 * @param initialFilter 初始过滤器，从持久化存储中恢复（不含搜索文本和分页参数）
 * @param onFilterChange 过滤器（排序/版本/分类/加载器）变更时的回调，用于持久化保存
 * @param getCategories 根据平台获取可用的资源类别过滤器
 * @param enableModLoader 是否允许更改模组加载器
 * @param getModloaders 根据平台获取可用的模组加载器过滤器
 * @param mapCategories 通过平台获取类别本地化信息
 * @param filterPersistenceKey 持久化过滤器状态的 MMKV 键，为空则不保存
 * @param swapToDownload 跳转到下载详情页
 * @param installedInfo 查询项目本地是否已安装，键为平台与平台项目ID
 * @param extraFilter 额外的过滤器UI
 */
@Composable
fun SearchAssetsScreen(
    mainScreenKey: TitledNavKey?,
    parentScreenKey: TitledNavKey,
    parentCurrentKey: TitledNavKey?,
    screenKey: TitledNavKey,
    currentKey: TitledNavKey?,
    platformClasses: PlatformClasses,
    initialPlatform: Platform,
    onPlatformChange: (Platform) -> Unit = {},
    enablePlatform: Boolean = true,
    initialFilter: PlatformSearchFilter = PlatformSearchFilter(),
    onFilterChange: ((Platform, PlatformSearchFilter) -> Unit)? = null,
    getCategories: (Platform) -> List<PlatformFilterCode>,
    enableModLoader: Boolean = false,
    getModloaders: (Platform) -> List<PlatformDisplayLabel> = { emptyList() },
    mapCategories: (Platform, String) -> PlatformFilterCode?,
    filterPersistenceKey: String? = null,
    /** 是否允许"自动选择下载内容"功能在进入屏幕时预选游戏版本（由各内容类型的设置开关控制） */
    autoSelectEnabled: Boolean = true,
    swapToDownload: (Platform, projectId: String, iconUrl: String?) -> Unit = { _, _, _ -> },
    installedInfo: ((Platform, projectId: String) -> InstalledMod?)? = null,
    extraFilter: (LazyListScope.() -> Unit)? = null
) {
    val viewModel: SearchScreenViewModel = rememberSearchAssetsViewModel(
        navKey = screenKey,
        initialPlatform = initialPlatform,
        platformClasses = platformClasses,
        filterPersistenceKey = filterPersistenceKey,
        getCategories = getCategories,
        getModloaders = getModloaders,
        autoSelectEnabled = autoSelectEnabled
    )

    // 每次重组时更新 ViewModel 的过滤器变更回调
    viewModel.onFilterChange = if (onFilterChange != null) {
        { filter -> onFilterChange(viewModel.searchPlatform, filter) }
    } else {
        {}
    }

    //跟随平台自动变更的内容
    val categories = remember(viewModel.searchPlatform) {
        getCategories(viewModel.searchPlatform)
    }
    val modloaders = remember(viewModel.searchPlatform) {
        getModloaders(viewModel.searchPlatform)
    }

    BaseScreen(
        levels1 = listOf(
            Pair(NestedNavKey.Download::class.java, mainScreenKey)
        ),
        Triple(parentScreenKey, parentCurrentKey, false),
        Triple(screenKey, currentKey, false)
    ) { isVisible ->
        Row {
            val yOffset by swapAnimateDpAsState(targetValue = (-40).dp, swapIn = isVisible)
            ResultListLayout(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(7f)
                    .offset { IntOffset(x = 0, y = yOffset.roundToPx()) },
                classes = platformClasses,
                searchState = viewModel.searchResult,
                onReload = {
                    viewModel.search()
                },
                swapToDownload = swapToDownload,
                installedInfo = installedInfo,
                onPreviousPage = { pageNumber ->
                    previousPage(
                        pageNumber = pageNumber,
                        pages = viewModel.pages,
                        index = viewModel.searchFilter.index,
                        limit = viewModel.searchFilter.limit,
                        onSuccess = { previousPage ->
                            viewModel.searchResult = SearchAssetsState.Success(previousPage)
                        },
                        onSearch = { newIndex ->
                            viewModel.searchFilter = viewModel.searchFilter.copy(index = newIndex)
                            viewModel.search() //搜索上一页
                        }
                    )
                },
                onNextPage = { pageNumber, isLastPage ->
                    nextPage(
                        pageNumber = pageNumber,
                        isLastPage = isLastPage,
                        pages = viewModel.pages,
                        index = viewModel.searchFilter.index,
                        limit = viewModel.searchFilter.limit,
                        onSuccess = { nextPage ->
                            viewModel.searchResult = SearchAssetsState.Success(nextPage)
                        },
                        onSearch = { newIndex ->
                            viewModel.searchFilter = viewModel.searchFilter.copy(index = newIndex)
                            viewModel.search() //搜索下一页
                        }
                    )
                },
                onNavigatePage = { pageNumber ->
                    navigatePage(
                        pageNumber = pageNumber,
                        pages = viewModel.pages,
                        limit = viewModel.searchFilter.limit,
                        onSuccess = { nextPage ->
                            viewModel.searchResult = SearchAssetsState.Success(nextPage)
                        },
                        onSearch = { newIndex ->
                            viewModel.searchFilter = viewModel.searchFilter.copy(index = newIndex)
                            viewModel.search() //搜索目标页
                        }
                    )
                }
            )

            val xOffset by swapAnimateDpAsState(
                targetValue = 40.dp,
                swapIn = isVisible,
                isHorizontal = true
            )
            val searchedMcMods by viewModel.searchedMcMods.collectAsStateWithLifecycle()
            SearchFilter(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(3f)
                    .offset { IntOffset(x = xOffset.roundToPx(), y = 0) },
                contentPadding = PaddingValues(top = 12.dp, bottom = 12.dp, end = 12.dp),
                enablePlatform = enablePlatform,
                searchPlatform = viewModel.searchPlatform,
                onPlatformChange = {
                    viewModel.searchPlatform = it
                    viewModel.researchWithFilter(
                        viewModel.searchFilter.copy(categories = emptyList(), modloader = null)
                    )
                    onPlatformChange(it)
                },
                searchName = viewModel.searchFilter.searchName,
                onSearchNameChange = {
                    viewModel.updateFilter(it)
                },
                onSearch = {
                    viewModel.resetSearch()
                },
                searchedMcMods = searchedMcMods,
                installedVersions = viewModel.installedVersionIds,
                gameVersion = viewModel.searchFilter.gameVersion,
                onGameVersionChange = {
                    viewModel.researchWithFilter(
                        viewModel.searchFilter.copy(gameVersion = it)
                    )
                },
                sortField = viewModel.searchFilter.sortField,
                onSortFieldChange = {
                    viewModel.researchWithFilter(
                        viewModel.searchFilter.copy(sortField = it)
                    )
                },
                allCategories = categories,
                categories = viewModel.searchFilter.categories,
                onCategoryChanged = { categories ->
                    viewModel.researchWithFilter(
                        viewModel.searchFilter.copy(categories = categories)
                    )
                },
                enableModLoader = enableModLoader,
                modloaders = modloaders,
                modloader = viewModel.searchFilter.modloader,
                onModLoaderChange = {
                    viewModel.researchWithFilter(
                        viewModel.searchFilter.copy(modloader = it)
                    )
                },
                extraFilter = extraFilter
            )
        }
    }
}
