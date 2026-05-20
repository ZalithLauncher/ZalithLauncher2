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
import com.movtery.zalithlauncher.game.download.assets.platform.navigatePage
import com.movtery.zalithlauncher.game.download.assets.platform.nextPage
import com.movtery.zalithlauncher.game.download.assets.platform.previousPage
import com.movtery.zalithlauncher.game.download.assets.platform.searchAssets
import com.movtery.zalithlauncher.game.download.assets.utils.ModTranslations
import com.movtery.zalithlauncher.game.download.assets.utils.searchMcMods
import com.movtery.zalithlauncher.game.versioninfo.MinecraftVersion
import com.movtery.zalithlauncher.game.versioninfo.MinecraftVersions
import com.movtery.zalithlauncher.game.versioninfo.popularVersions
import com.movtery.zalithlauncher.ui.base.BaseScreen
import com.movtery.zalithlauncher.ui.screens.NestedNavKey
import com.movtery.zalithlauncher.ui.screens.TitledNavKey
import com.movtery.zalithlauncher.ui.screens.content.download.assets.elements.AssetsPage
import com.movtery.zalithlauncher.ui.screens.content.download.assets.elements.ResultListLayout
import com.movtery.zalithlauncher.ui.screens.content.download.assets.elements.SearchAssetsState
import com.movtery.zalithlauncher.ui.screens.content.download.assets.elements.SearchFilter
import com.movtery.zalithlauncher.utils.animation.swapAnimateDpAsState
import com.movtery.zalithlauncher.utils.logging.Logger.lInfo
import com.movtery.zalithlauncher.utils.logging.Logger.lWarning
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * 资源搜索屏幕的 view model
 * @param initialPlatform 初始设定的平台
 * @param platformClasses 资源搜索的类型
 */
private class SearchScreenViewModel(
    initialPlatform: Platform,
    private val platformClasses: PlatformClasses
): ViewModel() {
    var searchResult by mutableStateOf<SearchAssetsState>(SearchAssetsState.Searching)
    val pages = mutableStateListOf<AssetsPage?>()

    var searchPlatform by mutableStateOf(initialPlatform)
    var searchFilter by mutableStateOf(PlatformSearchFilter())

    private val _searchedMcMods = MutableStateFlow<List<ModTranslations.McMod>>(emptyList())
    /** 搜索得到的所有 MCMOD 项目 */
    val searchedMcMods = _searchedMcMods.asStateFlow()
    private val _searchedVersions = MutableStateFlow<List<String>>(emptyList())
    /** 搜索得到的所有Minecraft版本 */
    val searchedVersions = _searchedVersions.asStateFlow()

    var currentSearchJob: Job? = null
    var currentSearchMCMODSJob: Job? = null
    var currentSearchVersionJob: Job? = null

    /**
     * 仅更新搜索名称
     */
    fun updateNameFilter(searchName: String) {
        searchFilter = searchFilter.copy(searchName = searchName)
        currentSearchMCMODSJob?.cancel()
        currentSearchMCMODSJob = viewModelScope.launch {
            val result = try {
                searchName.searchMcMods(classes = platformClasses) ?: emptyList()
            } catch (_: CancellationException) {
                emptyList()
            }.take(20) //仅展示20个搜索结果
            withContext(Dispatchers.Main) {
                _searchedMcMods.update { result }
            }
            currentSearchMCMODSJob = null
        }
    }

    /**
     * 仅更新版本名称
     */
    fun updateVersionFilter(version: String) {
        searchFilter = searchFilter.copy(gameVersion = version)
        refreshVerSuggestions(version)
    }

    private fun refreshVerSuggestions(
        version: String
    ) {
        currentSearchVersionJob?.cancel()
        currentSearchVersionJob = viewModelScope.launch {
            val allVersions = MinecraftVersions.allVersions.value
            val result: List<String> = when {
                version.isEmpty() -> popularVersions
                allVersions.isEmpty() -> popularVersions.filter { ver ->
                    ver.contains(version)
                }.take(20) //仅展示20个搜索结果
                else -> allVersions.filter {
                    it.version.id.contains(version) &&
                            //CurseForge只能使用正式版进行过滤
                            (searchPlatform != Platform.CURSEFORGE || it.type == MinecraftVersion.Type.Release)
                }.map { it.version.id }.take(20) //仅展示20个搜索结果
            }
            withContext(Dispatchers.Main) {
                _searchedVersions.update { result }
            }
            currentSearchVersionJob = null
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
        search()
    }

    private fun putResult(result: PlatformSearchResult) {
        result.getAssetsPage(platformClasses).also { page ->
            lInfo("Searched page info: {pageNumber: ${page.pageNumber}, pageIndex: ${page.pageIndex}, totalPage: ${page.totalPage}, isLastPage: ${page.isLastPage}}")

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
    }

    fun search() {
        currentSearchJob?.cancel() //取消上一个搜索

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
        //初始化后，执行一次搜索
        search()
        refreshVerSuggestions("")
        viewModelScope.launch {
            runCatching {
                MinecraftVersions.refreshVersions(force = false)
            }.onFailure {
                lWarning("Failed to refresh Minecraft versions")
            }
        }
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
    platformClasses: PlatformClasses
): SearchScreenViewModel {
    val screenKey = navKey.toString()
    return viewModel(
        key = "${screenKey}_search"
    ) {
        SearchScreenViewModel(initialPlatform, platformClasses)
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
 * @param getCategories 根据平台获取可用的资源类别过滤器
 * @param enableModLoader 是否允许更改模组加载器
 * @param getModloaders 根据平台获取可用的模组加载器过滤器
 * @param mapCategories 通过平台获取类别本地化信息
 * @param swapToDownload 跳转到下载详情页
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
    getCategories: (Platform) -> List<PlatformFilterCode>,
    enableModLoader: Boolean = false,
    getModloaders: (Platform) -> List<PlatformDisplayLabel> = { emptyList() },
    mapCategories: (Platform, String) -> PlatformFilterCode?,
    swapToDownload: (Platform, projectId: String, iconUrl: String?) -> Unit = { _, _, _ -> },
    extraFilter: (LazyListScope.() -> Unit)? = null
) {
    val viewModel: SearchScreenViewModel = rememberSearchAssetsViewModel(
        navKey = screenKey,
        initialPlatform = initialPlatform,
        platformClasses = platformClasses
    )

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
            val searchedVersions by viewModel.searchedVersions.collectAsStateWithLifecycle()
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
                    viewModel.updateNameFilter(it)
                },
                onSearch = {
                    viewModel.resetSearch()
                },
                searchedMcMods = searchedMcMods,
                searchedVersions = searchedVersions,
                gameVersion = viewModel.searchFilter.gameVersion,
                onGameVersionChange = {
                    viewModel.updateVersionFilter(it)
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