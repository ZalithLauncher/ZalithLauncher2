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

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.game.download.assets.platform.Platform
import com.movtery.zalithlauncher.game.download.assets.platform.PlatformClasses
import com.movtery.zalithlauncher.game.download.assets.platform.PlatformDisplayLabel
import com.movtery.zalithlauncher.game.download.assets.platform.PlatformFilterCode
import com.movtery.zalithlauncher.game.download.assets.platform.PlatformSearchFilter
import com.movtery.zalithlauncher.game.download.assets.platform.PlatformSearchResult
import com.movtery.zalithlauncher.game.download.assets.platform.PlatformSortField
import com.movtery.zalithlauncher.game.download.assets.platform.navigatePage
import com.movtery.zalithlauncher.game.download.assets.platform.nextPage
import com.movtery.zalithlauncher.game.download.assets.platform.previousPage
import com.movtery.zalithlauncher.game.download.assets.platform.searchAssets
import com.movtery.zalithlauncher.game.download.assets.utils.ModTranslations
import com.movtery.zalithlauncher.game.download.assets.utils.searchMcMods
import com.movtery.zalithlauncher.game.version.mod.InstalledMod
import com.movtery.zalithlauncher.game.versioninfo.MinecraftVersion
import com.movtery.zalithlauncher.game.versioninfo.MinecraftVersions
import com.movtery.zalithlauncher.game.versioninfo.popularVersions
import com.movtery.zalithlauncher.ui.base.BaseScreen
import com.movtery.zalithlauncher.ui.screens.NestedNavKey
import com.movtery.zalithlauncher.ui.screens.TitledNavKey
import com.movtery.zalithlauncher.ui.screens.content.download.assets.elements.AssetsPage
import com.movtery.zalithlauncher.ui.screens.content.download.assets.elements.ResultListLayout
import com.movtery.zalithlauncher.ui.screens.content.download.assets.elements.SearchAssetsState
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
            if (version.isNotEmpty()) delay(120)
            val allVersions = MinecraftVersions.allVersions.value
            val platform = searchPlatform
            val result: List<String> = withContext(Dispatchers.Default) {
                when {
                    version.isEmpty() -> popularVersions
                    allVersions.isEmpty() -> popularVersions.filter { ver ->
                        ver.contains(version)
                    }.take(20) //仅展示20个搜索结果
                    else -> allVersions.filter {
                        it.version.id.contains(version) &&
                                //CurseForge只能使用正式版进行过滤
                                (platform != Platform.CURSEFORGE || it.type == MinecraftVersion.Type.Release)
                    }.map { it.version.id }.take(20) //仅展示20个搜索结果
                }
            }
            if (searchFilter.gameVersion == version) {
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
        currentSearchVersionJob?.cancel()

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
                Logger.warning(TAG, "Failed to refresh Minecraft versions")
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
    getCategories: (Platform) -> List<PlatformFilterCode>,
    enableModLoader: Boolean = false,
    getModloaders: (Platform) -> List<PlatformDisplayLabel> = { emptyList() },
    mapCategories: (Platform, String) -> PlatformFilterCode?,
    swapToDownload: (Platform, projectId: String, iconUrl: String?) -> Unit = { _, _, _ -> },
    installedInfo: ((Platform, projectId: String) -> InstalledMod?)? = null,
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
        val yOffset by swapAnimateDpAsState(targetValue = (-30).dp, swapIn = isVisible)
        val searchedVersions by viewModel.searchedVersions.collectAsStateWithLifecycle()

        var showMcVersionMenu by remember { mutableStateOf(false) }
        var showLoaderMenu by remember { mutableStateOf(false) }
        var showSortMenu by remember { mutableStateOf(false) }
        var showPlatformMenu by remember { mutableStateOf(false) }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .offset { IntOffset(x = 0, y = yOffset.roundToPx()) },
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Compact Horizontal Search & Filter Bar (Mockup #4)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Search Box ("Search Modrinth & CurseForge...")
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF21242B))
                        .border(1.dp, Color(0xFF2E333E), RoundedCornerShape(10.dp))
                        .padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_search),
                        contentDescription = null,
                        tint = Color(0xFF9CA3AF),
                        modifier = Modifier
                            .size(16.dp)
                            .clickable { viewModel.resetSearch() }
                    )
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (viewModel.searchFilter.searchName.isEmpty()) {
                            Text(
                                text = "Search Modrinth & CurseForge...",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF8A909E),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        BasicTextField(
                            value = viewModel.searchFilter.searchName,
                            onValueChange = { viewModel.updateNameFilter(it) },
                            singleLine = true,
                            textStyle = TextStyle(
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            ),
                            cursorBrush = SolidColor(Color(0xFF1BD96A)),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                            keyboardActions = KeyboardActions(onSearch = { viewModel.resetSearch() }),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    if (viewModel.searchFilter.searchName.isNotEmpty()) {
                        Icon(
                            painter = painterResource(R.drawable.ic_close),
                            contentDescription = stringResource(R.string.generic_clear),
                            tint = Color(0xFF9CA3AF),
                            modifier = Modifier
                                .size(15.dp)
                                .clickable {
                                    viewModel.updateNameFilter("")
                                    viewModel.resetSearch()
                                }
                        )
                    }
                }

                // Platform Dropdown Pill (Modrinth / CurseForge)
                if (enablePlatform) {
                    Box {
                        DiscoverFilterDropdownPill(
                            label = viewModel.searchPlatform.displayName,
                            onClick = { showPlatformMenu = true }
                        )
                        DropdownMenu(
                            expanded = showPlatformMenu,
                            onDismissRequest = { showPlatformMenu = false }
                        ) {
                            Platform.entries.forEach { platform ->
                                DropdownMenuItem(
                                    text = { Text(platform.displayName) },
                                    onClick = {
                                        showPlatformMenu = false
                                        if (viewModel.searchPlatform != platform) {
                                            viewModel.searchPlatform = platform
                                            viewModel.researchWithFilter(
                                                viewModel.searchFilter.copy(categories = emptyList(), modloader = null)
                                            )
                                            onPlatformChange(platform)
                                        }
                                    }
                                )
                            }
                        }
                    }
                }

                // MC Version Dropdown Pill ("MC: 1.21.1 ▾")
                Box {
                    val mcLabel = viewModel.searchFilter.gameVersion.ifEmpty { "All" }
                    DiscoverFilterDropdownPill(
                        label = "MC: $mcLabel",
                        onClick = { showMcVersionMenu = true }
                    )
                    DropdownMenu(
                        expanded = showMcVersionMenu,
                        onDismissRequest = { showMcVersionMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("All Versions") },
                            onClick = {
                                showMcVersionMenu = false
                                viewModel.researchWithFilter(viewModel.searchFilter.copy(gameVersion = ""))
                            }
                        )
                        val versionOptions = searchedVersions.ifEmpty { popularVersions }
                        versionOptions.take(15).forEach { ver ->
                            DropdownMenuItem(
                                text = { Text(ver) },
                                onClick = {
                                    showMcVersionMenu = false
                                    viewModel.researchWithFilter(viewModel.searchFilter.copy(gameVersion = ver))
                                }
                            )
                        }
                    }
                }

                // Loader Dropdown Pill ("Loader: Fabric ▾")
                if (enableModLoader && modloaders.isNotEmpty()) {
                    Box {
                        val loaderLabel = viewModel.searchFilter.modloader?.getDisplayName() ?: "All"
                        DiscoverFilterDropdownPill(
                            label = "Loader: $loaderLabel",
                            onClick = { showLoaderMenu = true }
                        )
                        DropdownMenu(
                            expanded = showLoaderMenu,
                            onDismissRequest = { showLoaderMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("All Loaders") },
                                onClick = {
                                    showLoaderMenu = false
                                    viewModel.researchWithFilter(viewModel.searchFilter.copy(modloader = null))
                                }
                            )
                            modloaders.forEach { loader ->
                                DropdownMenuItem(
                                    text = { Text(loader.getDisplayName()) },
                                    onClick = {
                                        showLoaderMenu = false
                                        viewModel.researchWithFilter(viewModel.searchFilter.copy(modloader = loader))
                                    }
                                )
                            }
                        }
                    }
                }

                // Sort Dropdown Pill ("Sort: Downloads ▾")
                Box {
                    DiscoverFilterDropdownPill(
                        label = "Sort: ${stringResource(viewModel.searchFilter.sortField.displayNameRes)}",
                        onClick = { showSortMenu = true }
                    )
                    DropdownMenu(
                        expanded = showSortMenu,
                        onDismissRequest = { showSortMenu = false }
                    ) {
                        PlatformSortField.entries.forEach { sort ->
                            DropdownMenuItem(
                                text = { Text(stringResource(sort.displayNameRes)) },
                                onClick = {
                                    showSortMenu = false
                                    viewModel.researchWithFilter(viewModel.searchFilter.copy(sortField = sort))
                                }
                            )
                        }
                    }
                }
            }

            // Full-Width 2-Column Mobile Project Grid (Mockup #4)
            ResultListLayout(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
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
                            viewModel.search()
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
                            viewModel.search()
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
                            viewModel.search()
                        }
                    )
                }
            )
        }
    }
}

@Composable
private fun DiscoverFilterDropdownPill(
    label: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.height(36.dp),
        shape = RoundedCornerShape(10.dp),
        color = Color(0xFF21242B),
        border = BorderStroke(1.dp, Color(0xFF2E333E)),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFFE5E7EB),
                maxLines = 1
            )
            Icon(
                painter = painterResource(R.drawable.ic_arrow_drop_down_rounded),
                contentDescription = null,
                tint = Color(0xFF9CA3AF),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}