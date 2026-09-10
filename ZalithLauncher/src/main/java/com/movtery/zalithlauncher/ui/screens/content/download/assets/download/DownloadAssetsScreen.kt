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

package com.movtery.zalithlauncher.ui.screens.content.download.assets.download

import androidx.compose.animation.animateContentSize
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.game.download.assets.platform.Platform
import com.movtery.zalithlauncher.game.download.assets.platform.PlatformClasses
import com.movtery.zalithlauncher.game.download.assets.platform.PlatformProject
import com.movtery.zalithlauncher.game.download.assets.platform.PlatformVersion
import com.movtery.zalithlauncher.game.download.assets.platform.cacheKey
import com.movtery.zalithlauncher.game.download.assets.platform.getProject
import com.movtery.zalithlauncher.game.download.assets.platform.getVersionById
import com.movtery.zalithlauncher.game.download.assets.platform.getVersions
import com.movtery.zalithlauncher.game.download.assets.platform.isAllNull
import com.movtery.zalithlauncher.game.download.assets.utils.ModTranslations
import com.movtery.zalithlauncher.game.download.assets.utils.getMcmodTitle
import com.movtery.zalithlauncher.game.download.assets.utils.getTranslations
import com.movtery.zalithlauncher.game.version.mod.InstalledMod
import com.movtery.zalithlauncher.game.versioninfo.filterRelease
import com.movtery.zalithlauncher.ui.AndroidStringText
import com.movtery.zalithlauncher.ui.androidText
import com.movtery.zalithlauncher.ui.base.BaseScreen
import com.movtery.zalithlauncher.ui.components.BackgroundCard
import com.movtery.zalithlauncher.ui.components.CheckChip
import com.movtery.zalithlauncher.ui.components.ScalingLabel
import com.movtery.zalithlauncher.ui.components.ShimmerBox
import com.movtery.zalithlauncher.ui.components.SimpleTextInputField
import com.movtery.zalithlauncher.ui.screens.NestedNavKey
import com.movtery.zalithlauncher.ui.screens.NormalNavKey
import com.movtery.zalithlauncher.ui.screens.TitledNavKey
import com.movtery.zalithlauncher.ui.screens.content.download.assets.elements.AssetsIcon
import com.movtery.zalithlauncher.ui.screens.content.download.assets.elements.AssetsVersionItemLayout
import com.movtery.zalithlauncher.ui.screens.content.download.assets.elements.DependencyEntry
import com.movtery.zalithlauncher.ui.screens.content.download.assets.elements.DownloadAssetsState
import com.movtery.zalithlauncher.ui.screens.content.download.assets.elements.DownloadAssetsVersionLoading
import com.movtery.zalithlauncher.ui.screens.content.download.assets.elements.ProjectUrlsContent
import com.movtery.zalithlauncher.ui.screens.content.download.assets.elements.ScreenshotItemLayout
import com.movtery.zalithlauncher.ui.screens.content.download.assets.elements.VersionInfoMap
import com.movtery.zalithlauncher.ui.screens.content.download.assets.elements.initAll
import com.movtery.zalithlauncher.ui.screens.content.download.assets.elements.mapWithVersions
import com.movtery.zalithlauncher.ui.theme.onCardColor
import com.movtery.zalithlauncher.utils.animation.swapAnimateDpAsState
import com.movtery.zalithlauncher.viewmodel.EventViewModel
import io.ktor.client.plugins.ClientRequestException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlin.time.Duration.Companion.milliseconds

private class DownloadScreenViewModel(
    private val platform: Platform,
    private val projectId: String,
    private val classes: PlatformClasses
): ViewModel() {
    //版本
    private var _versionsList by mutableStateOf<List<VersionInfoMap>>(emptyList())
    var versionsResult by mutableStateOf<DownloadAssetsState<List<VersionInfoMap>>>(DownloadAssetsState.Getting())
    var versionsLoading by mutableStateOf<DownloadAssetsVersionLoading>(DownloadAssetsVersionLoading.None)
        private set

    var showOnlyMCRelease by mutableStateOf(true)
    var searchMCVersion by mutableStateOf("")

    fun filterWith(
        showOnlyMCRelease: Boolean = this.showOnlyMCRelease,
        searchMCVersion: String = this.searchMCVersion
    ) {
        this.showOnlyMCRelease = showOnlyMCRelease
        this.searchMCVersion = searchMCVersion
        viewModelScope.launch {
            versionsLoading = DownloadAssetsVersionLoading.None
            val infos = _versionsList.filterInfos()
            versionsResult = DownloadAssetsState.Success(infos)
        }
    }

    private fun List<VersionInfoMap>.filterInfos(): List<VersionInfoMap> {
        return filter { info ->
            (!showOnlyMCRelease || filterRelease(info.gameVersion)) &&
                    (searchMCVersion.isEmpty() || info.gameVersion.contains(searchMCVersion, true))
        }
    }

    fun getVersions() {
        viewModelScope.launch {
            versionsResult = DownloadAssetsState.Getting()
            //重新加载时重试此前获取失败的依赖项目
            failedDependencyProjects.clear()
            if (platform == Platform.CURSEFORGE) {
                versionsLoading = DownloadAssetsVersionLoading.StartLoadPage
            }

            getVersions(
                projectID = projectId,
                platform = platform,
                pageCallback = { chunk, page ->
                    versionsLoading = DownloadAssetsVersionLoading.LoadingPage(chunk, page)
                },
                onSuccess = { result ->
                    val versions: List<PlatformVersion> = result.initAll(projectId)

                    //版本列表先行展示，依赖项目信息改为后台缓存，不再阻塞列表加载
                    _versionsList = versions.mapWithVersions(classes)
                    versionsResult = DownloadAssetsState.Success(_versionsList.filterInfos())
                    versionsLoading = DownloadAssetsVersionLoading.None

                    if (classes == PlatformClasses.MOD_PACK) return@getVersions
                    val dependencies = versions
                        .flatMap { it.platformDependencies() }
                        .distinctBy { it.cacheKey() }
                    if (dependencies.isEmpty()) return@getVersions

                    viewModelScope.launch {
                        val semaphore = Semaphore(8)
                        dependencies.map { dependency ->
                            async {
                                semaphore.withPermit {
                                    cacheDependencyProject(
                                        platform = dependency.platform,
                                        dependency = dependency
                                    )
                                }
                            }
                        }.awaitAll()
                    }
                },
                onError = {
                    versionsResult = it
                    versionsLoading = DownloadAssetsVersionLoading.None
                }
            )
        }
    }

    //项目信息
    var projectResult by mutableStateOf<DownloadAssetsState<Triple<PlatformProject, ModTranslations, ModTranslations.McMod?>>>(DownloadAssetsState.Getting())

    fun getProject() {
        viewModelScope.launch {
            projectResult = DownloadAssetsState.Getting()
            getProject(
                projectID = projectId,
                platform = platform,
                onSuccess = { result ->
                    val mod = classes.getTranslations()
                    val mcmod = mod.getModBySlugId(result.platformSlug())
                    projectResult = DownloadAssetsState.Success(Triple(result, mod, mcmod))
                },
                onError = { state, _ ->
                    projectResult = state
                }
            )
        }
    }

    //缓存依赖项目
    val cachedDependencyProject = mutableStateMapOf<String, PlatformProject>()
    //该依赖项目未找到，但是多个版本同时依赖这个不存在的项目
    //就会进行很多次无效的访问，非常耗时
    //需要记录不存在的依赖项目的id，避免下次继续获取
    val notFoundDependencyProjects = mutableStateListOf<String>()
    //依赖项目信息获取失败，在对话框里展示占位项
    val failedDependencyProjects = mutableStateListOf<String>()

    /**
     * 缓存依赖项目
     */
    private suspend fun cacheDependencyProject(
        platform: Platform,
        dependency: PlatformVersion.PlatformDependency
    ) {
        val key = dependency.cacheKey()
        if (notFoundDependencyProjects.contains(key) || cachedDependencyProject.containsKey(key)) return

        try {
            val projectId = dependency.projectId ?: run {
                //依赖只标注了精确版本，先通过版本反查其所属项目
                getVersionById(
                    versionId = dependency.versionId
                        ?: error("The dependency does not provide a project id or a version id."),
                    platform = platform,
                    printLog = false
                ).platformProjectId()
            }
            getProject<PlatformProject>(
                projectID = projectId,
                platform = platform,
                onSuccess = { result ->
                    cachedDependencyProject[key] = result
                },
                onError = { _, e ->
                    if (e.isNotFound()) {
                        notFoundDependencyProjects.add(key)
                    } else {
                        if (!failedDependencyProjects.contains(key)) failedDependencyProjects.add(key)
                    }
                }
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Throwable) {
            if (e.isNotFound()) {
                notFoundDependencyProjects.add(key)
            } else {
                if (!failedDependencyProjects.contains(key)) failedDependencyProjects.add(key)
            }
        }
    }

    init {
        //初始化后，获取项目、版本信息
        getVersions()
        getProject()
    }

    override fun onCleared() {
        viewModelScope.cancel()
    }
}

/**
 * 平台的接口是否返回了未找到
 */
private fun Throwable.isNotFound(): Boolean =
    this is ClientRequestException && response.status.value == 404

@Composable
private fun rememberDownloadAssetsViewModel(
    key: NormalNavKey.DownloadAssets
): DownloadScreenViewModel {
    return viewModel(
        key = key.toString()
    ) {
        DownloadScreenViewModel(
            platform = key.platform,
            projectId = key.projectId,
            classes = key.classes
        )
    }
}

/**
 * @param parentScreenKey 父屏幕Key
 * @param parentCurrentKey 父屏幕当前Key
 * @param currentKey 当前的Key
 * @param installedChecker 查询版本本地是否已安装，null 则不进行已安装标注
 */
@Composable
fun DownloadAssetsScreen(
    mainScreenKey: TitledNavKey?,
    parentScreenKey: TitledNavKey,
    parentCurrentKey: TitledNavKey?,
    currentKey: TitledNavKey?,
    key: NormalNavKey.DownloadAssets,
    eventViewModel: EventViewModel,
    onItemClicked: (PlatformClasses, PlatformVersion, iconUrl: String?, deps: List<DependencyEntry>) -> Unit,
    nestedNavKeyClass: Class<out TitledNavKey>? = null,
    versionsUIWeight: Float = 6.5f,
    projectUIWeight: Float = 3.5f,
    installedChecker: ((PlatformVersion) -> InstalledMod?)? = null,
) {
    val viewModel: DownloadScreenViewModel = rememberDownloadAssetsViewModel(key)

    BaseScreen(
        levels1 = listOf(
            Pair(nestedNavKeyClass ?: NestedNavKey.Download::class.java, mainScreenKey)
        ),
        Triple(parentScreenKey, parentCurrentKey, false),
        Triple(key, currentKey, false),
    ) { isVisible ->
        Row(
            modifier = Modifier.fillMaxSize()
        ) {
            val yOffset by swapAnimateDpAsState(targetValue = (-40).dp, swapIn = isVisible)
            Versions(
                modifier = Modifier
                    .weight(versionsUIWeight)
                    .fillMaxHeight()
                    .offset { IntOffset(x = 0, y = yOffset.roundToPx()) },
                viewModel = viewModel,
                installedChecker = installedChecker,
                onReload = { viewModel.getVersions() },
                onItemClicked = { version ->
                    val deps = version.platformDependencies().mapNotNull { dep ->
                        val key = dep.cacheKey()
                        val project = viewModel.cachedDependencyProject[key]
                        when {
                            project != null -> DependencyEntry(dep, project)
                            viewModel.notFoundDependencyProjects.contains(key) ->
                                DependencyEntry(dep, null, notFound = true)
                            viewModel.failedDependencyProjects.contains(key) ->
                                DependencyEntry(dep, null)
                            //依赖项目信息仍在获取中
                            else -> null
                        }
                    }
                    onItemClicked(key.classes, version, key.iconUrl, deps)
                },
            )

            val xOffset by swapAnimateDpAsState(
                targetValue = 40.dp,
                swapIn = isVisible,
                isHorizontal = true
            )
            ProjectInfo(
                modifier = Modifier
                    .weight(projectUIWeight)
                    .fillMaxHeight()
                    .padding(vertical = 12.dp)
                    .padding(end = 12.dp)
                    .offset { IntOffset(x = xOffset.roundToPx(), y = 0) },
                projectResult = viewModel.projectResult,
                defaultClasses = key.classes,
                onReload = { viewModel.getProject() },
                openLink = { url ->
                    eventViewModel.sendEvent(EventViewModel.Event.OpenLink(url))
                }
            )
        }
    }
}

/**
 * 所有版本列表
 */
@Composable
private fun Versions(
    modifier: Modifier = Modifier,
    viewModel: DownloadScreenViewModel,
    installedChecker: ((PlatformVersion) -> InstalledMod?)? = null,
    onReload: () -> Unit = {},
    onItemClicked: (PlatformVersion) -> Unit = {}
) {
    when (val versions = viewModel.versionsResult) {
        is DownloadAssetsState.Getting -> {
            Box(
                modifier.padding(all = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column(
                        modifier = Modifier.animateContentSize()
                    ) {
                        when (val state = viewModel.versionsLoading) {
                            is DownloadAssetsVersionLoading.None -> {}
                            is DownloadAssetsVersionLoading.StartLoadPage -> {
                                Text(
                                    text = stringResource(R.string.download_assets_loading_page_data),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    textAlign = TextAlign.Center
                                )
                            }
                            is DownloadAssetsVersionLoading.LoadingPage -> {
                                Text(
                                    text = stringResource(R.string.download_assets_loaded_chunk_page, state.chunk, state.page),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                    LinearWavyProgressIndicator(
                        modifier = Modifier.width(168.dp),
                        wavelength = 32.dp
                    )
                }
            }
        }
        is DownloadAssetsState.Success -> {
            Column(modifier = modifier) {
                //简单过滤条件
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CheckChip(
                        selected = viewModel.showOnlyMCRelease,
                        onClick = {
                            viewModel.filterWith(showOnlyMCRelease = viewModel.showOnlyMCRelease.not())
                        },
                        label = {
                            Text(text = stringResource(R.string.download_assets_show_only_mc_release))
                        },
                    )

                    SimpleTextInputField(
                        modifier = Modifier.weight(1f),
                        value = viewModel.searchMCVersion,
                        onValueChange = { viewModel.filterWith(searchMCVersion = it) },
                        singleLine = true,
                        textStyle = TextStyle(color = onCardColor()).copy(fontSize = 12.sp),
                        hint = {
                            Text(
                                text = stringResource(R.string.download_assets_search_mc_versions),
                                style = TextStyle(color = onCardColor()).copy(fontSize = 12.sp)
                            )
                        }
                    )
                }

                HorizontalDivider(
                    modifier = Modifier
                        .padding(horizontal = 12.dp)
                        .fillMaxWidth(),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                )

                val scrollState = rememberLazyListState()

                LaunchedEffect(Unit) {
                    delay(100L.milliseconds)
                    runCatching {
                        val result = versions.result
                        val index = versions.result.indexOfFirst { it.isAdapt }
                        if (index >= 0 && index < result.size) {
                            //自动滚动到适配的资源版本
                            scrollState.animateScrollToItem(index)
                        }
                    }
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    state = scrollState
                ) {
                    items(versions.result) { info ->
                        AssetsVersionItemLayout(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            infoMap = info,
                            installedChecker = installedChecker,
                            onItemClicked = onItemClicked
                        )
                    }
                }
            }
        }
        is DownloadAssetsState.Error -> {
            Box(modifier.padding(all = 12.dp)) {
                ScalingLabel(
                    modifier = Modifier.align(Alignment.Center),
                    text = {
                        AndroidStringText(
                            text = androidText(
                                R.string.download_assets_failed_to_get_versions,
                                versions.message
                            )
                        )
                    },
                    onClick = onReload
                )
            }
        }
    }
}

/**
 * 项目信息板块
 */
@Composable
private fun ProjectInfo(
    modifier: Modifier = Modifier,
    projectResult: DownloadAssetsState<Triple<PlatformProject, ModTranslations, ModTranslations.McMod?>>,
    defaultClasses: PlatformClasses,
    onReload: () -> Unit = {},
    openLink: (url: String) -> Unit = {}
) {
    val context = LocalContext.current
    BackgroundCard(
        modifier = modifier,
        shape = MaterialTheme.shapes.extraLarge
    ) {
        when (val result = projectResult) {
            is DownloadAssetsState.Getting -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(all = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    //图标、标题、简介的骨架
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            ShimmerBox(
                                modifier = Modifier
                                    .clip(shape = RoundedCornerShape(10.dp))
                                    .size(72.dp)
                            )
                            Column(
                                modifier = Modifier.padding(top = 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                //标题
                                ShimmerBox(
                                    modifier = Modifier
                                        .fillMaxWidth(0.6f)
                                        .height(20.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                )
                                //简介
                                ShimmerBox(
                                    modifier = Modifier
                                        .fillMaxWidth(0.9f)
                                        .height(16.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                )
                            }
                        }
                    }
                }
            }
            is DownloadAssetsState.Success -> {
                val (project, mod, mcmod) = result.result
                //项目基本信息
                val platform = remember { project.platform() }
                val iconUrl = remember { project.platformIconUrl() }
                val title = remember { project.platformTitle() }
                val summary = remember { project.platformSummary() }
                val urls = remember { project.platformUrls(defaultClasses) }
                val screenshots = remember { project.platformScreenshots() }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(all = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    //图标、标题、简介
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            AssetsIcon(
                                modifier = Modifier.clip(shape = RoundedCornerShape(10.dp)),
                                size = 72.dp,
                                iconUrl = iconUrl
                            )
                            //标题、简介
                            Column(
                                modifier = Modifier.padding(top = 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = mcmod.getMcmodTitle(title, context),
                                    style = MaterialTheme.typography.titleMedium,
                                    textAlign = TextAlign.Center
                                )
                                summary?.let { summary ->
                                    Text(
                                        text = summary,
                                        style = MaterialTheme.typography.bodyMedium,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }

                    //相关链接
                    if (!urls.isAllNull()) {
                        item {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = stringResource(R.string.download_assets_links),
                                    style = MaterialTheme.typography.titleMedium
                                )

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

                    //屏幕截图
                    items(screenshots) { screenshot ->
                        ScreenshotItemLayout(
                            modifier = Modifier.fillMaxWidth(),
                            screenshot = screenshot
                        )
                    }
                }
            }
            is DownloadAssetsState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(all = 12.dp)
                ) {
                    ScalingLabel(
                        modifier = Modifier.align(Alignment.Center),
                        text = {
                            AndroidStringText(
                                text = androidText(
                                    R.string.download_assets_failed_to_get_project,
                                    result.message
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