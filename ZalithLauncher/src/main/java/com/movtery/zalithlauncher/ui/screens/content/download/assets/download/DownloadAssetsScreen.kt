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
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import com.movtery.zalithlauncher.ui.theme.MiraiThemeManager
import com.movtery.zalithlauncher.utils.formatNumberByLocale
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
import com.movtery.zalithlauncher.game.download.assets.favorites.FavoriteProjectsRepository
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
import com.movtery.zalithlauncher.ui.screens.content.download.assets.elements.ClassesIdentifier
import com.movtery.zalithlauncher.ui.screens.content.download.assets.elements.DependencyEntry
import com.movtery.zalithlauncher.ui.screens.content.download.assets.elements.DownloadAssetsState
import com.movtery.zalithlauncher.ui.screens.content.download.assets.elements.DownloadAssetsVersionLoading
import com.movtery.zalithlauncher.ui.screens.content.download.assets.elements.FavoriteIdentifier
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
    initialClasses: PlatformClasses
): ViewModel() {
    /**
     * 资源类型，优先使用项目详情返回的准确类型
     */
    var classes by mutableStateOf(initialClasses)
        private set

    //版本
    private var _versionsList by mutableStateOf<List<VersionInfoMap>>(emptyList())
    //未经映射的原始版本数据，类型修正后用于重新映射
    private var rawVersions: List<PlatformVersion> = emptyList()
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

    /**
     * 类型修正后，使用原始版本数据重新映射版本列表
     */
    private fun remapVersions() {
        if (rawVersions.isEmpty()) return
        _versionsList = rawVersions.mapWithVersions(classes)
        versionsResult = DownloadAssetsState.Success(_versionsList.filterInfos())
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
                    rawVersions = versions

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
                    //以项目详情返回的类型为准
                    val accurateClasses = result.platformClasses(classes)
                    if (accurateClasses != classes) {
                        classes = accurateClasses
                        remapVersions()
                    }
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
            initialClasses = key.classes
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
    installedChecker: ((PlatformVersion) -> InstalledMod?)? = null,
) {
    val viewModel: DownloadScreenViewModel = rememberDownloadAssetsViewModel(key)

    //Shared dependency resolution for both version-row taps and the hero "Download Latest" button.
    fun resolveDeps(version: PlatformVersion): List<DependencyEntry> {
        return version.platformDependencies().mapNotNull { dep ->
            val depKey = dep.cacheKey()
            val project = viewModel.cachedDependencyProject[depKey]
            when {
                project != null -> DependencyEntry(dep, project)
                viewModel.notFoundDependencyProjects.contains(depKey) ->
                    DependencyEntry(dep, null, notFound = true)
                viewModel.failedDependencyProjects.contains(depKey) ->
                    DependencyEntry(dep, null)
                //依赖项目信息仍在获取中
                else -> null
            }
        }
    }

    fun launchDownload(version: PlatformVersion) {
        onItemClicked(viewModel.classes, version, key.iconUrl, resolveDeps(version))
    }

    BaseScreen(
        levels1 = listOf(
            Pair(nestedNavKeyClass ?: NestedNavKey.Download::class.java, mainScreenKey)
        ),
        Triple(parentScreenKey, parentCurrentKey, false),
        Triple(key, currentKey, false),
    ) { isVisible ->
        val yOffset by swapAnimateDpAsState(targetValue = (-40).dp, swapIn = isVisible)
        MiraiDownloadColumn(
            modifier = Modifier
                .fillMaxSize()
                .offset { IntOffset(x = 0, y = yOffset.roundToPx()) },
            viewModel = viewModel,
            platform = key.platform,
            projectId = key.projectId,
            installedChecker = installedChecker,
            onReloadVersions = { viewModel.getVersions() },
            onReloadProject = { viewModel.getProject() },
            onVersionClicked = { launchDownload(it) },
            openLink = { url ->
                eventViewModel.sendEvent(EventViewModel.Event.OpenLink(url))
            }
        )
    }
}

/**
 * Number of LazyColumn header items above the version rows (hero card + filter row).
 * Used as the scroll offset when auto-scrolling to the adapted version.
 */
private const val DOWNLOAD_HEADER_COUNT = 2

/**
 * Single-column "Mobile Hero" layout: hero card, filters, version rows,
 * then related links and screenshots at the bottom.
 */
@Composable
private fun MiraiDownloadColumn(
    viewModel: DownloadScreenViewModel,
    platform: Platform,
    projectId: String,
    installedChecker: ((PlatformVersion) -> InstalledMod?)?,
    onReloadVersions: () -> Unit,
    onReloadProject: () -> Unit,
    onVersionClicked: (PlatformVersion) -> Unit,
    openLink: (url: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val versionsResult = viewModel.versionsResult
    val scrollState = rememberLazyListState()

    val projectData = (viewModel.projectResult as? DownloadAssetsState.Success)?.result
    val project = projectData?.first
    val projectUrls = remember(project, viewModel.classes) {
        project?.platformUrls(viewModel.classes)
    }
    val projectScreenshots = remember(project) {
        project?.platformScreenshots().orEmpty()
    }

    //Auto-scroll to the version adapted to the current instance, once per fresh load.
    var versionsLoaded by remember { mutableStateOf(false) }
    LaunchedEffect(versionsResult) {
        val result = (versionsResult as? DownloadAssetsState.Success)?.result
        val justLoaded = result != null && !versionsLoaded
        versionsLoaded = result != null
        if (!justLoaded || result == null) return@LaunchedEffect
        delay(100L.milliseconds)
        runCatching {
            val index = result.indexOfFirst { it.isAdapt }
            if (index >= 0) {
                //自动滚动到适配的资源版本
                scrollState.animateScrollToItem(DOWNLOAD_HEADER_COUNT + index)
            }
        }
    }

    //"Download Latest" target: the newest file of the adapted group, otherwise the newest group.
    val latestVersion = (versionsResult as? DownloadAssetsState.Success)
        ?.result?.let { list ->
            (list.firstOrNull { it.isAdapt } ?: list.firstOrNull())?.versions?.firstOrNull()
        }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        state = scrollState,
        contentPadding = PaddingValues(bottom = 12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        item(key = "mirai_hero") {
            DownloadHeroCard(
                projectResult = viewModel.projectResult,
                classes = viewModel.classes,
                platform = platform,
                projectId = projectId,
                latestVersion = latestVersion,
                onDownloadLatest = onVersionClicked,
                onReload = onReloadProject
            )
        }

        when (versionsResult) {
            is DownloadAssetsState.Getting -> {
                item(key = "mirai_versions_loading") {
                    VersionsLoadingItem(loading = viewModel.versionsLoading)
                }
            }
            is DownloadAssetsState.Success -> {
                item(key = "mirai_filters") {
                    FilterRow(
                        showOnlyMCRelease = viewModel.showOnlyMCRelease,
                        onToggleReleases = {
                            viewModel.filterWith(showOnlyMCRelease = viewModel.showOnlyMCRelease.not())
                        },
                        searchMCVersion = viewModel.searchMCVersion,
                        onSearchChange = { viewModel.filterWith(searchMCVersion = it) }
                    )
                }
                items(
                    items = versionsResult.result,
                    key = { "${it.gameVersion}_${it.loader?.getDisplayName()}" }
                ) { info ->
                    AssetsVersionItemLayout(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        infoMap = info,
                        installedChecker = installedChecker,
                        onItemClicked = onVersionClicked
                    )
                }
            }
            is DownloadAssetsState.Error -> {
                item(key = "mirai_versions_error") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(all = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        ScalingLabel(
                            text = {
                                AndroidStringText(
                                    text = androidText(
                                        R.string.download_assets_failed_to_get_versions,
                                        versionsResult.message
                                    )
                                )
                            },
                            onClick = onReloadVersions
                        )
                    }
                }
            }
        }

        //Related links rail (project data only, independent of the versions state).
        if (projectData != null && projectUrls != null && !projectUrls.isAllNull()) {
            val (_, mod, mcmod) = projectData
            item(key = "mirai_links") {
                LinksRail(
                    platform = platform,
                    urls = projectUrls,
                    mcmod = mcmod,
                    mod = mod,
                    openLink = openLink
                )
            }
        }

        //Screenshots carousel.
        if (projectScreenshots.isNotEmpty()) {
            item(key = "mirai_screenshots") {
                ScreenshotsRail(screenshots = projectScreenshots)
            }
        }
    }
}

/**
 * Hero header: icon, title, author, summary, stats, favorite toggle and Download Latest.
 */
@Composable
private fun DownloadHeroCard(
    projectResult: DownloadAssetsState<Triple<PlatformProject, ModTranslations, ModTranslations.McMod?>>,
    classes: PlatformClasses,
    platform: Platform,
    projectId: String,
    latestVersion: PlatformVersion?,
    onDownloadLatest: (PlatformVersion) -> Unit,
    onReload: () -> Unit,
    modifier: Modifier = Modifier,
) {
    BackgroundCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        shape = MaterialTheme.shapes.extraLarge
    ) {
        when (projectResult) {
            is DownloadAssetsState.Getting -> {
                HeroShimmer()
            }
            is DownloadAssetsState.Success -> {
                val (project, _, mcmod) = projectResult.result
                val context = LocalContext.current
                val iconUrl = remember(project) { project.platformIconUrl() }
                val title = remember(project) { project.platformTitle() }
                val summary = remember(project) { project.platformSummary() }
                val author = remember(project) { project.platformAuthor() }
                val downloads = remember(project) { project.platformDownloadCount() }
                val follows = remember(project) { project.platformFollows() }
                val accent = MiraiThemeManager.currentAccent()

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(all = 14.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AssetsIcon(
                        modifier = Modifier.clip(shape = RoundedCornerShape(16.dp)),
                        size = 84.dp,
                        iconUrl = iconUrl
                    )

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        ClassesIdentifier(
                            classes = classes,
                            iconSize = 14.dp,
                            textStyle = MaterialTheme.typography.labelSmall
                        )
                        Text(
                            text = mcmod.getMcmodTitle(title, context),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        author?.takeIf { it.isNotBlank() }?.let {
                            Text(
                                text = "by $it",
                                style = MaterialTheme.typography.labelMedium,
                                color = accent,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        summary?.takeIf { it.isNotBlank() }?.let {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Text(
                            text = buildString {
                                append(formatNumberByLocale(context, downloads))
                                append(" downloads")
                                if (follows != null) {
                                    append("  •  ")
                                    append(formatNumberByLocale(context, follows))
                                    append(" followers")
                                }
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val isFavorite = FavoriteProjectsRepository.isFavorite(platform, projectId)
                        FavoriteIdentifier(
                            isFavorite = isFavorite,
                            iconSize = 16.dp,
                            textStyle = MaterialTheme.typography.labelMedium,
                            onClick = {
                                if (isFavorite) {
                                    FavoriteProjectsRepository.unfavorite(platform, projectId)
                                } else {
                                    FavoriteProjectsRepository.favorite(project, classes)
                                }
                            }
                        )
                        Button(
                            onClick = { latestVersion?.let(onDownloadLatest) },
                            enabled = latestVersion != null,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = accent,
                                contentColor = Color(0xFF06210F)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            Icon(
                                modifier = Modifier.size(16.dp),
                                painter = painterResource(R.drawable.ic_download_2_outlined),
                                contentDescription = null
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Download Latest",
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }
            }
            is DownloadAssetsState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(all = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    ScalingLabel(
                        text = {
                            AndroidStringText(
                                text = androidText(
                                    R.string.download_assets_failed_to_get_project,
                                    projectResult.message
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
private fun HeroShimmer() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(all = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ShimmerBox(
            modifier = Modifier
                .clip(shape = RoundedCornerShape(16.dp))
                .size(84.dp)
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ShimmerBox(
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(22.dp)
                    .clip(RoundedCornerShape(4.dp))
            )
            ShimmerBox(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .height(16.dp)
                    .clip(RoundedCornerShape(4.dp))
            )
            ShimmerBox(
                modifier = Modifier
                    .fillMaxWidth(0.5f)
                    .height(14.dp)
                    .clip(RoundedCornerShape(4.dp))
            )
        }
    }
}

/**
 * Release filter chip + MC version search.
 */
@Composable
private fun FilterRow(
    showOnlyMCRelease: Boolean,
    onToggleReleases: () -> Unit,
    searchMCVersion: String,
    onSearchChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CheckChip(
                selected = showOnlyMCRelease,
                onClick = onToggleReleases,
                label = {
                    Text(text = stringResource(R.string.download_assets_show_only_mc_release))
                },
            )

            SimpleTextInputField(
                modifier = Modifier.weight(1f),
                value = searchMCVersion,
                onValueChange = onSearchChange,
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
    }
}

@Composable
private fun VersionsLoadingItem(
    loading: DownloadAssetsVersionLoading,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(all = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(
            modifier = Modifier.animateContentSize()
        ) {
            when (loading) {
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
                        text = stringResource(R.string.download_assets_loaded_chunk_page, loading.chunk, loading.page),
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

/**
 * Related project links as a horizontal rail.
 */
@Composable
private fun LinksRail(
    platform: Platform,
    urls: PlatformProject.Urls,
    mcmod: ModTranslations.McMod?,
    mod: ModTranslations,
    openLink: (url: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = stringResource(R.string.download_assets_links),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
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

/**
 * Screenshots carousel.
 */
@Composable
private fun ScreenshotsRail(
    screenshots: List<PlatformProject.Screenshot>,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(screenshots) { screenshot ->
            ScreenshotItemLayout(
                modifier = Modifier.width(260.dp),
                screenshot = screenshot,
                shape = RoundedCornerShape(12.dp)
            )
        }
    }
}