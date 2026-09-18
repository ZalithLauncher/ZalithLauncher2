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

package com.movtery.zalithlauncher.ui.screens.content.download.assets.favorites

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.drawscope.inset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.game.download.assets.favorites.FavoriteEntry
import com.movtery.zalithlauncher.game.download.assets.favorites.FavoriteProjectsRepository
import com.movtery.zalithlauncher.game.download.assets.platform.Platform
import com.movtery.zalithlauncher.game.download.assets.platform.PlatformClasses
import com.movtery.zalithlauncher.game.download.assets.platform.PlatformDisplayLabel
import com.movtery.zalithlauncher.game.download.assets.platform.curseforge.models.curseForgeModLoaderFilters
import com.movtery.zalithlauncher.game.download.assets.platform.modrinth.models.modrinthModLoaderFilters
import com.movtery.zalithlauncher.setting.AllSettings
import com.movtery.zalithlauncher.ui.base.BaseScreen
import com.movtery.zalithlauncher.ui.components.EdgeDirection
import com.movtery.zalithlauncher.ui.components.OwnOutlinedTextField
import com.movtery.zalithlauncher.ui.components.TextRailItem
import com.movtery.zalithlauncher.ui.components.fadeEdge
import com.movtery.zalithlauncher.ui.screens.NestedNavKey
import com.movtery.zalithlauncher.ui.screens.TitledNavKey
import com.movtery.zalithlauncher.ui.screens.content.download.assets.elements.FilterListLayout
import com.movtery.zalithlauncher.ui.screens.content.download.assets.elements.FilterSelectionMode
import com.movtery.zalithlauncher.ui.screens.content.elements.backgroundGlass
import com.movtery.zalithlauncher.ui.theme.cardColor
import com.movtery.zalithlauncher.ui.theme.onCardColor
import com.movtery.zalithlauncher.utils.animation.swapAnimateDpAsState
import com.movtery.zalithlauncher.viewmodel.backgroundVisible
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * 收藏排序方式
 */
private enum class FavoriteSortBy {
    /** 收藏时间，新到旧 */
    FOLLOW_TIME,
    /** 项目名称，字典序 */
    TITLE
}

/**
 * 收藏过滤器可用的模组加载器，两平台按显示名合并
 */
private val favoriteModLoaderFilters: List<PlatformDisplayLabel> =
    (modrinthModLoaderFilters + curseForgeModLoaderFilters).distinctBy { it.getDisplayName() }

private class FavoritesScreenViewModel : ViewModel() {
    /** 分类过滤器，null 表示全部 */
    var category by mutableStateOf<PlatformClasses?>(null)
        private set
    var searchName by mutableStateOf("")
    var platformFilter by mutableStateOf<Platform?>(null)
    var sortBy by mutableStateOf(FavoriteSortBy.FOLLOW_TIME)
    var modloaderFilter by mutableStateOf<List<PlatformDisplayLabel>>(emptyList())

    /**
     * 过滤后用于展示的收藏列表，仅从数据池中过滤，不触发任何加载
     */
    val items: List<FavoriteEntry> by derivedStateOf {
        val keyword = searchName.trim()
        val modloaderNames = modloaderFilter.map { it.getDisplayName() }.toSet()

        FavoriteProjectsRepository.projects.values
            .asSequence()
            .filter { entry -> category == null || entry.project.classes == category }
            .filter { entry ->
                keyword.isEmpty() || entry.displayTitle().contains(keyword, ignoreCase = true)
            }
            .filter { entry -> platformFilter == null || entry.platform == platformFilter }
            .filter { entry ->
                //加载器信息仅来自远端数据，未就绪的条目视为不匹配
                modloaderNames.isEmpty() ||
                        entry.remote?.platformModLoaders()
                            ?.any { it.getDisplayName() in modloaderNames } == true
            }
            .sortedWith(
                when (sortBy) {
                    FavoriteSortBy.FOLLOW_TIME -> compareByDescending { it.project.followTime }
                    FavoriteSortBy.TITLE -> compareBy { it.displayTitle().lowercase() }
                }
            )
            .toList()
    }

    fun onCategoryChange(value: PlatformClasses?) {
        category = value
        //分类不再需要模组加载器过滤时，重置过滤器，避免过滤器继续对列表生效
        if (value != PlatformClasses.MOD && value != PlatformClasses.MOD_PACK) {
            modloaderFilter = emptyList()
        }
    }

    fun onScreenEntered() {
        viewModelScope.launch {
            //先完成数据同步，再基于最新数据池刷新远端数据
            FavoriteProjectsRepository.reload()
            FavoriteProjectsRepository.refreshRemote()
        }
    }
}

private fun FavoriteEntry.displayTitle(): String {
    return remote?.platformTitle() ?: project.title
}

@Composable
private fun rememberFavoritesScreenViewModel(): FavoritesScreenViewModel = viewModel {
    FavoritesScreenViewModel()
}

/**
 * @param swapToDownload 跳转到资源类型对应的下载分类屏幕详情页
 */
@Composable
fun FavoritesScreen(
    mainScreenKey: TitledNavKey?,
    parentScreenKey: TitledNavKey,
    parentCurrentKey: TitledNavKey?,
    screenKey: TitledNavKey,
    currentKey: TitledNavKey?,
    swapToDownload: (Platform, PlatformClasses, projectId: String, iconUrl: String?) -> Unit
) {
    val viewModel = rememberFavoritesScreenViewModel()

    BaseScreen(
        levels1 = listOf(
            Pair(NestedNavKey.Download::class.java, mainScreenKey)
        ),
        Triple(parentScreenKey, parentCurrentKey, false),
        Triple(screenKey, currentKey, false)
    ) { isVisible ->
        //每进入一次屏幕，重载收藏数据并刷新远端数据
        LaunchedEffect(Unit) {
            viewModel.onScreenEntered()
        }

        Row(modifier = Modifier.fillMaxSize()) {
            val yOffset by swapAnimateDpAsState(targetValue = (-40).dp, swapIn = isVisible)
            FavoritesContent(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(7f)
                    .offset { IntOffset(x = 0, y = yOffset.roundToPx()) },
                viewModel = viewModel,
                swapToDownload = swapToDownload
            )

            val xOffset by swapAnimateDpAsState(
                targetValue = 40.dp,
                swapIn = isVisible,
                isHorizontal = true
            )
            FavoritesFilter(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(3f)
                    .offset { IntOffset(x = xOffset.roundToPx(), y = 0) },
                viewModel = viewModel
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FavoritesContent(
    modifier: Modifier = Modifier,
    viewModel: FavoritesScreenViewModel,
    swapToDownload: (Platform, PlatformClasses, projectId: String, iconUrl: String?) -> Unit
) {
    val repositoryLoaded = FavoriteProjectsRepository.initialized
    val repositoryEmpty = FavoriteProjectsRepository.projects.isEmpty()

    Box(modifier = modifier) {
        if (!repositoryLoaded) {
            //收藏数据装载中
        } else if (repositoryEmpty) {
            //暂无任何收藏
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    modifier = Modifier.size(68.dp),
                    painter = painterResource(R.drawable.ic_box),
                    contentDescription = null
                )
                Text(
                    text = stringResource(R.string.favorites_empty),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            //分类操作栏滚动吸附
            val density = LocalDensity.current
            val topAppBarState = rememberTopAppBarState()
            val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(topAppBarState)
            val headerTopPaddingPx = with(density) { 12.dp.toPx() }
            var headerHeightPx by remember { mutableIntStateOf(0) }

            //操作栏阴影跟随滚动线性过渡
            val listState = rememberLazyListState()
            val listScrolledFraction = remember(listState, headerTopPaddingPx) {
                derivedStateOf {
                    if (listState.firstVisibleItemIndex > 0) 1f
                    else {
                        val rampPx = headerTopPaddingPx + (listState.layoutInfo.visibleItemsInfo.firstOrNull()?.size ?: 0)
                        (listState.firstVisibleItemScrollOffset / rampPx.coerceAtLeast(1f)).coerceIn(0f, 1f)
                    }
                }
            }
            val barShownFraction = remember(topAppBarState) {
                derivedStateOf { 1f - topAppBarState.collapsedFraction }
            }
            val actionBarShadowElevation = if (backgroundVisible()) {
                0.dp //背景可见时不使用阴影，因为卡片会半透明化
            } else {
                5.dp * barShownFraction.value * listScrolledFraction.value
            }

            //列表顶部淡化，凸显悬浮分类操作栏的视觉层级
            val listTopFadePx = (headerHeightPx + headerTopPaddingPx + 80f) *
                    listScrolledFraction.value *
                    barShownFraction.value

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .nestedScroll(scrollBehavior.nestedScrollConnection)
                    .clipToBounds()
                    .topFade(listTopFadePx),
                state = listState,
                contentPadding = PaddingValues(
                    start = 12.dp,
                    end = 12.dp,
                    bottom = 12.dp,
                    top = with(density) {
                        (headerHeightPx + topAppBarState.heightOffset).coerceAtLeast(0f).toDp()
                    }
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val entries = viewModel.items
                if (entries.isEmpty()) {
                    //过滤后无结果
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 80.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = stringResource(R.string.favorites_empty_filtered),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
                items(entries, key = { "${it.platform}:${it.project.projectId}" }) { entry ->
                    FavoriteProjectLayout(
                        modifier = Modifier
                            .fillMaxWidth()
                            .animateItem(),
                        entry = entry,
                        onClick = {
                            swapToDownload(
                                entry.platform,
                                entry.project.classes,
                                entry.project.projectId,
                                entry.remote?.platformIconUrl() ?: entry.project.iconUrl
                            )
                        },
                        onUnfavorite = {
                            FavoriteProjectsRepository.unfavorite(entry.platform, entry.project.projectId)
                        }
                    )
                }
            }

            //悬浮分类操作栏
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .zIndex(1f)
                    .onSizeChanged {
                        headerHeightPx = it.height
                        topAppBarState.heightOffsetLimit = -it.height.toFloat()
                    }
                    .offset { IntOffset(x = 0, y = topAppBarState.heightOffset.roundToInt()) }
            ) {
                val blur = AllSettings.backgroundBlur.state
                val barColor = cardColor()
                Surface(
                    modifier = Modifier.padding(all = 12.dp),
                    color = barColor,
                    contentColor = onCardColor(),
                    shape = MaterialTheme.shapes.large,
                    shadowElevation = actionBarShadowElevation
                ) {
                    val scrollState = rememberScrollState()
                    Row(
                        modifier = Modifier
                            .backgroundGlass(blur, barColor)
                            .fadeEdge(
                                state = scrollState,
                                length = 32.dp,
                                direction = EdgeDirection.Horizontal
                            )
                            .horizontalScroll(state = scrollState)
                            .padding(all = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FavoriteCategoryItem(
                            text = stringResource(R.string.generic_all),
                            selected = viewModel.category == null,
                            onClick = { viewModel.onCategoryChange(null) }
                        )
                        PlatformClasses.entries.forEach { classes ->
                            FavoriteCategoryItem(
                                text = stringResource(classes.categoryText()),
                                selected = viewModel.category == classes,
                                onClick = { viewModel.onCategoryChange(classes) }
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun PlatformClasses.categoryText(): Int = when (this) {
    PlatformClasses.MOD -> R.string.download_category_mod
    PlatformClasses.MOD_PACK -> R.string.download_category_modpack
    PlatformClasses.RESOURCE_PACK -> R.string.download_category_resource_pack
    PlatformClasses.SAVES -> R.string.download_category_saves
    PlatformClasses.SHADERS -> R.string.download_category_shaders
}

@Composable
private fun FavoriteCategoryItem(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    TextRailItem(
        text = {
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium
            )
        },
        onClick = onClick,
        selected = selected,
        shape = MaterialTheme.shapes.large
    )
}

@Composable
private fun FavoritesFilter(
    modifier: Modifier = Modifier,
    viewModel: FavoritesScreenViewModel
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(top = 12.dp, end = 12.dp, bottom = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        //名称搜索，直接从数据池过滤，无需主动触发
        item {
            OwnOutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = viewModel.searchName,
                onValueChange = { viewModel.searchName = it },
                singleLine = true,
                label = {
                    Text(text = stringResource(R.string.download_assets_filter_search_name))
                },
                trailingIcon = {
                    Icon(
                        painter = painterResource(R.drawable.ic_search),
                        contentDescription = stringResource(R.string.generic_search)
                    )
                },
                shape = MaterialTheme.shapes.large
            )
        }

        //目标平台过滤
        item {
            FilterListLayout(
                modifier = Modifier.fillMaxWidth(),
                items = listOf<Platform?>(null) + Platform.entries,
                selectionMode = FilterSelectionMode.Single,
                selectedItems = listOf(viewModel.platformFilter),
                onSelectionChange = { new ->
                    val value = new.firstOrNull()
                    if (value != viewModel.platformFilter) {
                        viewModel.platformFilter = value
                    }
                },
                getItemLabel = { item ->
                    item?.displayName ?: stringResource(R.string.generic_all)
                },
                title = stringResource(R.string.download_assets_filter_search_platform),
                cancelable = false
            )
        }

        //排序方式过滤
        item {
            FilterListLayout(
                modifier = Modifier.fillMaxWidth(),
                items = FavoriteSortBy.entries,
                selectionMode = FilterSelectionMode.Single,
                selectedItems = listOf(viewModel.sortBy),
                onSelectionChange = { new ->
                    new.firstOrNull()?.takeIf { it != viewModel.sortBy }?.let { value ->
                        viewModel.sortBy = value
                    }
                },
                getItemLabel = { item ->
                    stringResource(
                        when (item) {
                            FavoriteSortBy.FOLLOW_TIME -> R.string.favorites_sort_follow_time
                            FavoriteSortBy.TITLE -> R.string.favorites_sort_title
                        }
                    )
                },
                title = stringResource(R.string.sort_by),
                cancelable = false
            )
        }

        //模组加载器，仅模组、整合包分类提供
        val enableModLoader = viewModel.category == PlatformClasses.MOD ||
                viewModel.category == PlatformClasses.MOD_PACK
        if (enableModLoader) {
            item {
                FilterListLayout(
                    modifier = Modifier.fillMaxWidth(),
                    items = favoriteModLoaderFilters,
                    selectionMode = FilterSelectionMode.Multiple,
                    selectedItems = viewModel.modloaderFilter,
                    onSelectionChange = { new ->
                        viewModel.modloaderFilter = new
                    },
                    getItemLabel = { item ->
                        item.getDisplayName()
                    },
                    title = stringResource(R.string.download_assets_filter_modloader)
                )
            }
        }
    }
}

/**
 * 在组件顶部绘制指定像素高度的线性渐隐
 */
private fun Modifier.topFade(heightPx: Float): Modifier = this
    .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
    .drawWithContent {
        drawContent()
        if (heightPx <= 0f) return@drawWithContent
        inset(
            left = 0f,
            top = 0f,
            right = 0f,
            bottom = (size.height - heightPx).coerceAtLeast(0f)
        ) {
            drawRect(
                brush = Brush.verticalGradient(0f to Color.Black, 1f to Color.Transparent),
                blendMode = BlendMode.DstOut
            )
        }
    }
