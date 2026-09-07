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

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.game.download.assets.favorites.AssetFavoriteManager
import com.movtery.zalithlauncher.game.download.assets.favorites.toAssetFavorite
import com.movtery.zalithlauncher.game.download.assets.mapExceptionToMessage
import com.movtery.zalithlauncher.game.download.assets.platform.Platform
import com.movtery.zalithlauncher.game.download.assets.platform.PlatformClasses
import com.movtery.zalithlauncher.game.download.assets.platform.PlatformProject
import com.movtery.zalithlauncher.game.download.assets.platform.UnsupportedClassesException
import com.movtery.zalithlauncher.game.download.assets.platform.getProjectByVersion
import com.movtery.zalithlauncher.game.download.assets.platform.isAllNull
import com.movtery.zalithlauncher.game.download.assets.utils.ModTranslations
import com.movtery.zalithlauncher.game.download.assets.utils.getMcMod
import com.movtery.zalithlauncher.game.download.assets.utils.getMcmodTitle
import com.movtery.zalithlauncher.game.download.assets.utils.getTranslations
import com.movtery.zalithlauncher.ui.AndroidStringText
import com.movtery.zalithlauncher.ui.androidText
import com.movtery.zalithlauncher.ui.base.BaseScreen
import com.movtery.zalithlauncher.ui.components.BackgroundCard
import com.movtery.zalithlauncher.ui.components.OwnOutlinedTextField
import com.movtery.zalithlauncher.ui.components.ScalingLabel
import com.movtery.zalithlauncher.ui.screens.NestedNavKey
import com.movtery.zalithlauncher.ui.screens.NormalNavKey
import com.movtery.zalithlauncher.ui.screens.TitledNavKey
import com.movtery.zalithlauncher.ui.screens.content.download.assets.elements.FavoriteToggleButton
import com.movtery.zalithlauncher.ui.screens.content.download.assets.elements.PlatformListLayout
import com.movtery.zalithlauncher.ui.screens.content.download.assets.elements.ProjectUrlsContent
import com.movtery.zalithlauncher.ui.screens.content.download.assets.elements.ResultProjectLayout
import com.movtery.zalithlauncher.ui.screens.content.download.assets.elements.ScreenshotItemLayout
import com.movtery.zalithlauncher.utils.animation.getAnimateTween
import com.movtery.zalithlauncher.utils.animation.swapAnimateDpAsState
import com.movtery.zalithlauncher.utils.logging.Logger
import com.movtery.zalithlauncher.utils.string.isEmptyOrBlank
import io.ktor.client.plugins.ClientRequestException
import io.ktor.http.HttpStatusCode
import io.ktor.server.plugins.NotFoundException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

private const val TAG = "SearchIdScreen"

private sealed interface SearchIdOperation {
    data object None : SearchIdOperation
    /** 加载项目中 */
    data object Loading : SearchIdOperation
    /** 已获得搜索结果 */
    data class Result(
        val project: PlatformProject,
        val mcMod: ModTranslations.McMod?,
        val mod: ModTranslations,
    ) : SearchIdOperation

    /** 未找到该项目 */
    data object NotFound : SearchIdOperation
    /** 项目类别不受支持 */
    data object Unsupported : SearchIdOperation

    /** 获取过程中出现异常 */
    data class Error(val message: AndroidStringText) : SearchIdOperation
}

private class SearchIdViewModel: ViewModel() {
    //fixme: 默认视为模组，通常情况下，获取到的项目会带有类别
    val defaultClasses = PlatformClasses.MOD

    var projectId by mutableStateOf("")
    var platform by mutableStateOf(Platform.CURSEFORGE)

    /** 搜索页面操作状态 */
    var operation by mutableStateOf<SearchIdOperation>(SearchIdOperation.None)

    private var searchJob: Job? = null
    /** 开始按照ID搜索 */
    fun search() {
        val id = projectId
        val platform0 = platform
        if (id.isEmptyOrBlank()) return

        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            operation = SearchIdOperation.Loading
            runCatching {
                val project = getProjectByVersion(
                    projectId = id,
                    platform = platform0
                )
                project.checkClasses()
                val classes = project.platformClasses(defaultClasses)
                val translations = classes.getTranslations()
                operation = SearchIdOperation.Result(
                    project = project,
                    mcMod = project.getMcMod(translations),
                    mod = translations,
                )
            }.onFailure { e ->
                operation = when (e) {
                    is CancellationException -> {
                        Logger.debug(TAG, "The search task has been cancelled.")
                        SearchIdOperation.None
                    }
                    is UnsupportedClassesException -> SearchIdOperation.Unsupported
                    is NotFoundException -> SearchIdOperation.NotFound
                    is ClientRequestException -> {
                        if (e.response.status == HttpStatusCode.NotFound) {
                            SearchIdOperation.NotFound
                        } else {
                            onError(e)
                            return@onFailure
                        }
                    }
                    else -> {
                        onError(e)
                        return@onFailure
                    }
                }
            }
        }
    }

    private fun onError(throwable: Throwable) {
        Logger.error(TAG, "An exception occurred while searching for assets.", throwable)
        operation = SearchIdOperation.Error(mapExceptionToMessage(throwable))
    }
}

@Composable
private fun rememberSearchIdViewModel() = viewModel {
    SearchIdViewModel()
}

@Composable
fun SearchIdScreen(
    mainScreenKey: TitledNavKey?,
    downloadScreenKey: TitledNavKey?,
    openLink: (String) -> Unit,
    swapToDownload: (Platform, PlatformClasses, projectId: String, iconUrl: String?) -> Unit,
) {
    BaseScreen(
        levels1 = listOf(
            Pair(NestedNavKey.Download::class.java, mainScreenKey)
        ),
        Triple(NormalNavKey.SearchId, downloadScreenKey, false)
    ) { isVisible ->
        val viewModel = rememberSearchIdViewModel()
        Content(
            modifier = Modifier.fillMaxSize(),
            isVisible = isVisible,
            onSearch = { viewModel.search() },
            onView = { platform, classes, id, iconUrl ->
                swapToDownload(platform, classes, id, iconUrl)
            },
            openLink = openLink,
            operation = viewModel.operation,
            defaultClasses = viewModel.defaultClasses,
            projectId = viewModel.projectId,
            onProjectIdChange = { viewModel.projectId = it },
            searchPlatform = viewModel.platform,
            onPlatformChange = { viewModel.platform = it }
        )
    }
}

@Composable
private fun Content(
    isVisible: Boolean,
    operation: SearchIdOperation,
    defaultClasses: PlatformClasses,
    onSearch: () -> Unit,
    onView: (Platform, PlatformClasses, id: String, iconUrl: String?) -> Unit,
    openLink: (String) -> Unit,
    projectId: String,
    onProjectIdChange: (String) -> Unit,
    searchPlatform: Platform,
    onPlatformChange: (Platform) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        ContentResult(
            modifier = Modifier
                .weight(0.7f)
                .fillMaxHeight(),
            isVisible = isVisible,
            operation = operation,
            onReload = onSearch,
            onView = onView,
            openLink = openLink,
            defaultClasses = defaultClasses,
            contentPadding = PaddingValues(start = 12.dp, top = 12.dp, bottom = 12.dp)
        )

        ContentFilter(
            modifier = Modifier
                .weight(0.3f)
                .fillMaxHeight()
                .padding(top = 12.dp, end = 12.dp, bottom = 12.dp),
            isVisible = isVisible,
            onSearch = onSearch,
            projectId = projectId,
            onProjectIdChange = onProjectIdChange,
            searchPlatform = searchPlatform,
            onPlatformChange = onPlatformChange,
        )
    }
}

@Composable
private fun ContentResult(
    isVisible: Boolean,
    operation: SearchIdOperation,
    defaultClasses: PlatformClasses,
    onReload: () -> Unit,
    onView: (Platform, PlatformClasses, id: String, iconUrl: String?) -> Unit,
    openLink: (String) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues()
) {
    val yOffset by swapAnimateDpAsState(
        targetValue = (-40).dp,
        swapIn = isVisible
    )

    Box(
        modifier = modifier.offset { IntOffset(x = 0, y = yOffset.roundToPx()) },
        contentAlignment = Alignment.Center
    ) {
        when (operation) {
            is SearchIdOperation.None -> {
                IconTip(
                    icon = painterResource(R.drawable.ic_search),
                    text = stringResource(R.string.download_assets_id_tip)
                )
            }
            is SearchIdOperation.Loading -> {
                LinearWavyProgressIndicator(
                    modifier = Modifier.width(168.dp),
                    wavelength = 32.dp
                )
            }
            is SearchIdOperation.Result -> {
                ResultLayout(
                    project = operation.project,
                    mcmod = operation.mcMod,
                    mod = operation.mod,
                    defaultClasses = defaultClasses,
                    onView = onView,
                    openLink = openLink,
                    contentPadding = contentPadding,
                )
            }
            is SearchIdOperation.NotFound -> {
                IconTip(
                    icon = painterResource(R.drawable.ic_box),
                    text = stringResource(R.string.download_assets_id_not_found)
                )
            }
            is SearchIdOperation.Unsupported -> {
                IconTip(
                    icon = painterResource(R.drawable.ic_box),
                    text = stringResource(R.string.download_assets_id_unsupported)
                )
            }
            is SearchIdOperation.Error -> {
                Box(modifier.padding(all = 12.dp)) {
                    ScalingLabel(
                        modifier = Modifier.align(Alignment.Center),
                        text = {
                            AndroidStringText(
                                text = androidText(
                                    R.string.download_assets_failed_to_get_result,
                                    operation.message
                                )
                            )
                        },
                        onClick = onReload
                    )
                }
            }
        }
    }
}

@Composable
private fun IconTip(
    icon: Painter,
    text: String,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            modifier = Modifier.size(68.dp),
            painter = icon,
            contentDescription = null
        )
        Text(text = text)
    }
}

@Composable
private fun ResultLayout(
    mcmod: ModTranslations.McMod?,
    mod: ModTranslations,
    project: PlatformProject,
    defaultClasses: PlatformClasses,
    onView: (Platform, PlatformClasses, id: String, iconUrl: String?) -> Unit,
    openLink: (String) -> Unit,
    contentPadding: PaddingValues = PaddingValues(),
) {
    val context = LocalContext.current

    val platform = remember(project) { project.platform() }
    val title = remember(project) { project.platformTitle() }
    val description = remember(project) { project.platformSummary() ?: "" }
    val iconUrl = remember(project) { project.platformIconUrl() }
    val author = remember(project) { project.platformAuthor() }
    val downloads = remember(project) { project.platformDownloadCount() }
    val follows = remember(project) { project.platformFollows() }
    val modloaders = remember(project) { project.platformModLoaders() }
    val classes = remember(project) { project.platformClasses(defaultClasses) }
    val categories = remember(project, classes) { project.platformCategories(classes) }

    val urls = remember { project.platformUrls(classes) }
    val screenshots = remember { project.platformScreenshots() }

    //项目收藏状态
    val favoriteKeys by AssetFavoriteManager.favoriteKeys.collectAsStateWithLifecycle()
    val projectId = remember(project) { project.platformId() }
    val isFavorite = remember(favoriteKeys, platform, projectId) {
        AssetFavoriteManager.isProjectFavorite(platform, projectId)
    }
    //防止点击闭包捕获陈旧的收藏态
    val currentIsFavorite by rememberUpdatedState(isFavorite)

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = contentPadding
    ) {
        item {
            ResultProjectLayout(
                modifier = Modifier.fillMaxWidth(),
                platform = platform,
                title = mcmod.getMcmodTitle(title, context),
                description = description,
                classes = classes,
                iconUrl = iconUrl,
                author = author,
                downloads = downloads,
                follows = follows,
                modloaders = modloaders,
                categories = categories?.sortedWith { o1, o2 -> o1.index() - o2.index() },
                trailingContent = {
                    FavoriteToggleButton(
                        isFavorite = isFavorite,
                        onToggle = {
                            //项目收藏与版本收藏完全独立，这里只切换项目收藏
                            if (currentIsFavorite) {
                                AssetFavoriteManager.removeProject(platform, projectId)
                            } else {
                                AssetFavoriteManager.addProject(project.toAssetFavorite(classes))
                            }
                        }
                    )
                },
                onClick = {
                    onView(platform, classes, project.platformId(), iconUrl)
                }
            )
        }

        //项目相关链接、截图
        if (!urls.isAllNull()) {
            item {
                val scale = remember { Animatable(initialValue = 0.95f) }
                LaunchedEffect(Unit) {
                    scale.animateTo(targetValue = 1f, animationSpec = getAnimateTween())
                }

                BackgroundCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .graphicsLayer(scaleY = scale.value, scaleX = scale.value),
                    shape = MaterialTheme.shapes.large,
                ) {
                    Column(
                        modifier = Modifier
                            .padding(all = 12.dp)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.download_assets_links),
                            style = MaterialTheme.typography.titleMedium
                        )

                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            ProjectUrlsContent(
                                platform = platform,
                                urls = urls,
                                mcmod = mcmod,
                                mod = mod,
                                openLink = openLink,
                            )
                        }
                    }
                }
            }
        }

        items(screenshots) { screenshot ->
            val scale = remember { Animatable(initialValue = 0.95f) }
            LaunchedEffect(Unit) {
                scale.animateTo(targetValue = 1f, animationSpec = getAnimateTween())
            }

            ScreenshotItemLayout(
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer(scaleY = scale.value, scaleX = scale.value),
                screenshot = screenshot,
                shape = MaterialTheme.shapes.large,
            )
        }
    }
}

@Composable
private fun ContentFilter(
    isVisible: Boolean,
    onSearch: () -> Unit,
    projectId: String,
    onProjectIdChange: (String) -> Unit,
    searchPlatform: Platform,
    onPlatformChange: (Platform) -> Unit,
    modifier: Modifier = Modifier
) {
    val xOffset by swapAnimateDpAsState(
        targetValue = 40.dp,
        swapIn = isVisible,
        isHorizontal = true
    )

    Column(
        modifier = modifier.offset { IntOffset(x = xOffset.roundToPx(), y = 0) },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        OwnOutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = projectId,
            onValueChange = onProjectIdChange,
            singleLine = true,
            label = {
                Text(stringResource(R.string.download_assets_id_holder))
            },
            trailingIcon = {
                IconButton(
                    onClick = onSearch
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_search),
                        contentDescription = stringResource(R.string.generic_search)
                    )
                }
            },
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Search
            ),
            keyboardActions = KeyboardActions(
                onSearch = {
                    onSearch()
                }
            ),
            shape = MaterialTheme.shapes.large
        )

        PlatformListLayout(
            modifier = Modifier.fillMaxWidth(),
            searchPlatform = searchPlatform,
            onPlatformChange = onPlatformChange,
        )
    }
}