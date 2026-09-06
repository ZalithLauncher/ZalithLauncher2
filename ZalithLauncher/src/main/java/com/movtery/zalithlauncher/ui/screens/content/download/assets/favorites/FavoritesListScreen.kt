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

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.game.download.assets.favorites.AssetFavorite
import com.movtery.zalithlauncher.game.download.assets.favorites.AssetFavoriteManager
import com.movtery.zalithlauncher.game.download.assets.favorites.AssetFavoriteType
import com.movtery.zalithlauncher.game.download.assets.favorites.storageKey
import com.movtery.zalithlauncher.game.download.assets.platform.Platform
import com.movtery.zalithlauncher.game.download.assets.platform.PlatformClasses
import com.movtery.zalithlauncher.ui.base.BaseScreen
import com.movtery.zalithlauncher.ui.components.CheckChip
import com.movtery.zalithlauncher.ui.components.ScalingLabel
import com.movtery.zalithlauncher.ui.components.SimpleAlertDialog
import com.movtery.zalithlauncher.ui.components.SimpleTextInputField
import com.movtery.zalithlauncher.ui.components.lazyScrollWithBar
import com.movtery.zalithlauncher.ui.screens.NestedNavKey
import com.movtery.zalithlauncher.ui.screens.NormalNavKey
import com.movtery.zalithlauncher.ui.screens.TitledNavKey
import com.movtery.zalithlauncher.utils.animation.swapAnimateDpAsState
import com.movtery.zalithlauncher.utils.string.isEmptyOrBlank
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

/**
 * 收藏列表页操作状态
 */
private sealed interface FavoritesOperation {
    data object None : FavoritesOperation
    /** 删除某项目组的全部收藏（项目收藏 + 所有版本收藏） */
    data class RemoveGroup(val group: FavoriteGroup) : FavoritesOperation
}

@Composable
private fun FavoritesOperation(
    operation: FavoritesOperation,
    updateOperation: (FavoritesOperation) -> Unit
) {
    when (operation) {
        FavoritesOperation.None -> {}
        is FavoritesOperation.RemoveGroup -> SimpleAlertDialog(
            title = stringResource(R.string.download_favorites_remove_group),
            text = stringResource(
                R.string.download_favorites_remove_group_message,
                operation.group.versions.size
            ),
            onConfirm = {
                AssetFavoriteManager.removeAllByProject(
                    operation.group.platform,
                    operation.group.projectId
                )
                updateOperation(FavoritesOperation.None)
            },
            onDismiss = { updateOperation(FavoritesOperation.None) }
        )
    }
}

/**
 * 类别筛选 tab 项
 */
private data class FavoritesTabItem(
    val label: Int,
    val icon: Int,
    val classes: PlatformClasses?
)

private val FAVORITES_TABS = listOf(
    FavoritesTabItem(R.string.download_favorites_tab_all, R.drawable.ic_favorite_outlined, null),
    FavoritesTabItem(R.string.download_category_modpack, R.drawable.ic_package_2_outlined, PlatformClasses.MOD_PACK),
    FavoritesTabItem(R.string.download_category_mod, R.drawable.ic_extension_outlined, PlatformClasses.MOD),
    FavoritesTabItem(R.string.download_category_resource_pack, R.drawable.ic_format_paint_outlined, PlatformClasses.RESOURCE_PACK),
    FavoritesTabItem(R.string.download_category_saves, R.drawable.ic_public, PlatformClasses.SAVES),
    FavoritesTabItem(R.string.download_category_shaders, R.drawable.ic_lightbulb, PlatformClasses.SHADERS)
)

/**
 * 收藏列表页 ViewModel：持有类别筛选与搜索关键词状态，
 * 并负责收藏条目的分组（按项目聚合 PROJECT + VERSION）、过滤与排序
 */
private class FavoritesViewModel : ViewModel() {
    private val _selectedClasses = MutableStateFlow<PlatformClasses?>(null)
    /** 当前选中的类别筛选，null 表示全部 */
    val selectedClasses = _selectedClasses.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    /** 当前搜索关键词 */
    val searchQuery = _searchQuery.asStateFlow()

    /**
     * 分组、过滤、排序后的收藏组列表：
     * 按 (platform, projectId) 聚合为 [FavoriteGroup]，再按类别与搜索词过滤，
     * 按组内最近收藏时间降序排序
     */
    val favoriteGroups: StateFlow<List<FavoriteGroup>> = combine(
        AssetFavoriteManager.favorites,
        _selectedClasses,
        _searchQuery
    ) { favorites, classes, query ->
        favorites
            .filter { classes == null || it.classes == classes }
            .groupBy { it.platform to it.projectId }
            .map { (_, list) ->
                FavoriteGroup(
                    project = list.firstOrNull { it.type == AssetFavoriteType.PROJECT },
                    versions = list.filter { it.type == AssetFavoriteType.VERSION }
                )
            }
            .filter { group -> query.isEmptyOrBlank() || group.matchesQuery(query) }
            .sortedByDescending { it.latestSavedAt }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    /**
     * 切换类别筛选
     * @param classes 目标类别，null 表示全部
     */
    fun selectClasses(classes: PlatformClasses?) {
        _selectedClasses.value = classes
    }

    /**
     * 更新搜索关键词
     * @param query 新的搜索关键词
     */
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }
}

@Composable
private fun rememberFavoritesViewModel(
    navKey: TitledNavKey
): FavoritesViewModel {
    return viewModel(key = navKey.toString() + "_favorites") {
        FavoritesViewModel()
    }
}

/**
 * 收藏列表屏幕
 * @param mainScreenKey 主屏幕Key
 * @param downloadScreenKey 下载屏幕Key
 * @param downloadFavoritesScreenKey 下载收藏屏幕Key
 * @param downloadFavoritesScreenCurrentKey 下载收藏屏幕当前Key
 * @param swapToDownload 跳转资源详情页
 * @param onDownloadVersion 下载指定版本收藏（在线重取失败时由调用方回退快照）
 * @param onCopyLink 复制指定版本收藏的下载直链
 */
@Composable
fun FavoritesListScreen(
    mainScreenKey: TitledNavKey?,
    downloadScreenKey: TitledNavKey?,
    downloadFavoritesScreenKey: TitledNavKey,
    downloadFavoritesScreenCurrentKey: TitledNavKey?,
    swapToDownload: (platform: Platform, projectId: String, classes: PlatformClasses, iconUrl: String?) -> Unit = { _, _, _, _ -> },
    onDownloadVersion: (favorite: AssetFavorite) -> Unit = {},
    onCopyLink: (favorite: AssetFavorite) -> Unit = {}
) {
    val viewModel = rememberFavoritesViewModel(downloadFavoritesScreenKey)
    val selectedClasses by viewModel.selectedClasses.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val groups by viewModel.favoriteGroups.collectAsStateWithLifecycle()
    //原始收藏列表，用于区分"完全无收藏"与"搜索/筛选无结果"两种空态
    val favorites by AssetFavoriteManager.favorites.collectAsStateWithLifecycle()

    var operation by remember { mutableStateOf<FavoritesOperation>(FavoritesOperation.None) }
    FavoritesOperation(
        operation = operation,
        updateOperation = { operation = it }
    )

    BaseScreen(
        levels1 = listOf(
            Pair(NestedNavKey.Download::class.java, mainScreenKey)
        ),
        Triple(downloadFavoritesScreenKey, downloadScreenKey, false),
        Triple(NormalNavKey.FavoritesList, downloadFavoritesScreenCurrentKey, false)
    ) { isVisible ->
        val yOffset by swapAnimateDpAsState(targetValue = (-40).dp, swapIn = isVisible)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .offset { IntOffset(0, yOffset.roundToPx()) }
        ) {
            //搜索框：命中项目标题/别名/作者/描述/版本名
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SimpleTextInputField(
                    modifier = Modifier.weight(1f),
                    value = searchQuery,
                    onValueChange = { viewModel.updateSearchQuery(it) },
                    hint = {
                        Text(
                            text = stringResource(R.string.download_favorites_search_hint),
                            style = TextStyle(color = LocalContentColor.current).copy(fontSize = 12.sp)
                        )
                    },
                    singleLine = true
                )
                //一键清除搜索内容
                AnimatedVisibility(visible = searchQuery.isNotEmpty()) {
                    IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                        Icon(
                            painter = painterResource(R.drawable.ic_close),
                            contentDescription = stringResource(R.string.generic_clear)
                        )
                    }
                }
            }

            //类别筛选 tab
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FAVORITES_TABS.forEach { tab ->
                    CheckChip(
                        selected = selectedClasses == tab.classes,
                        onClick = { viewModel.selectClasses(tab.classes) },
                        label = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    modifier = Modifier.size(FilterChipDefaults.IconSize),
                                    painter = painterResource(tab.icon),
                                    contentDescription = null
                                )
                                Text(
                                    text = stringResource(tab.label),
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                        }
                    )
                }
            }

            if (groups.isEmpty()) {
                //空态分场景：完全无收藏 / 搜索或筛选无结果
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(all = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    ScalingLabel(
                        text = stringResource(
                            if (favorites.isEmpty()) R.string.download_favorites_empty
                            else R.string.download_favorites_no_results
                        )
                    )
                }
            } else {
                val listState = rememberLazyListState()
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .lazyScrollWithBar(listState),
                    state = listState,
                    contentPadding = PaddingValues(all = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        items = groups,
                        key = { "${it.platform}:${it.projectId}" }
                    ) { group ->
                        FavoritesCard(
                            modifier = Modifier.fillMaxWidth(),
                            group = group,
                            onClick = {
                                swapToDownload(
                                    group.platform,
                                    group.projectId,
                                    group.classes,
                                    group.iconUrl
                                )
                            },
                            onDownloadVersion = onDownloadVersion,
                            onCopyLink = onCopyLink,
                            onRemoveVersion = { favorite ->
                                //单版本删除属轻操作，直接执行
                                AssetFavoriteManager.remove(favorite.storageKey)
                            },
                            onRemoveAll = {
                                operation = FavoritesOperation.RemoveGroup(group)
                            }
                        )
                    }
                }
            }
        }
    }
}

/**
 * 收藏组是否命中搜索词（项目标题/别名/作者/描述/版本名，忽略大小写）
 */
private fun FavoriteGroup.matchesQuery(query: String): Boolean {
    val entries = listOfNotNull(project) + versions
    return title.contains(query, ignoreCase = true) ||
        author?.contains(query, ignoreCase = true) == true ||
        description?.contains(query, ignoreCase = true) == true ||
        entries.any { it.slug?.contains(query, ignoreCase = true) == true } ||
        versions.any { it.versionName?.contains(query, ignoreCase = true) == true }
}
