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

package com.movtery.zalithlauncher.ui.screens.content.download.assets.elements

import androidx.annotation.StringRes
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.nonInteractiveScrollbar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.game.download.assets.DependencyRequest
import com.movtery.zalithlauncher.game.download.assets.platform.Platform
import com.movtery.zalithlauncher.game.download.assets.platform.PlatformClasses
import com.movtery.zalithlauncher.game.download.assets.platform.PlatformDependencyType
import com.movtery.zalithlauncher.game.download.assets.platform.PlatformDisplayLabel
import com.movtery.zalithlauncher.game.download.assets.platform.PlatformProject
import com.movtery.zalithlauncher.game.download.assets.platform.PlatformVersion
import com.movtery.zalithlauncher.game.download.assets.platform.cacheKey
import com.movtery.zalithlauncher.game.version.installed.Version
import com.movtery.zalithlauncher.game.version.installed.VersionsManager
import com.movtery.zalithlauncher.ui.components.MarqueeText
import com.movtery.zalithlauncher.ui.components.SimpleAlertDialog
import com.movtery.zalithlauncher.ui.components.fadeEdge
import com.movtery.zalithlauncher.ui.components.rememberDialogMaxHeight
import com.movtery.zalithlauncher.ui.screens.content.elements.CommonVersionInfoLayout
import com.movtery.zalithlauncher.ui.theme.cardColor
import com.movtery.zalithlauncher.ui.theme.itemColor
import com.movtery.zalithlauncher.ui.theme.onCardColor
import com.movtery.zalithlauncher.ui.theme.onItemColor
import com.movtery.zalithlauncher.utils.formatNumberByLocale

/**
 * 下载对话框中的依赖项
 * @param dependency 依赖信息
 * @param project 依赖项目信息，为null代表信息获取失败
 * @param notFound 依赖项目在平台上不存在
 */
class DependencyEntry(
    val dependency: PlatformVersion.PlatformDependency,
    val project: PlatformProject?,
    val notFound: Boolean = false
)

/**
 * 操作状态：下载单个资源文件
 */
sealed interface DownloadSingleOperation {
    data object None : DownloadSingleOperation
    /** 警告用户正在使用移动网络 */
    data class WarningForMobileData(
        val classes: PlatformClasses,
        val version: PlatformVersion,
        val dependencyEntries: List<DependencyEntry>
    ) : DownloadSingleOperation
    /** 选择版本 */
    data class SelectVersion(
        val classes: PlatformClasses,
        val version: PlatformVersion,
        val dependencyEntries: List<DependencyEntry>
    ) : DownloadSingleOperation
    /** 安装 */
    data class Install(
        val classes: PlatformClasses,
        val version: PlatformVersion,
        val versions: List<Version>,
        val dependencies: List<DependencyRequest>
    ) : DownloadSingleOperation
}

@Composable
fun DownloadSingleOperation(
    operation: DownloadSingleOperation,
    changeOperation: (DownloadSingleOperation) -> Unit,
    doInstall: (PlatformClasses, PlatformVersion, List<Version>, List<DependencyRequest>) -> Unit,
    installedProjects: Map<Platform, Set<String>> = emptyMap(),
    onDependencyClicked: (Platform, String, PlatformClasses) -> Unit = { _, _, _ -> }
) {
    when (operation) {
        DownloadSingleOperation.None -> {}
        is DownloadSingleOperation.WarningForMobileData -> {
            SimpleAlertDialog(
                title = stringResource(R.string.generic_warning),
                text = stringResource(R.string.download_install_warning_mobile_data),
                confirmText = stringResource(R.string.generic_anyway),
                onDismiss = {
                    changeOperation(DownloadSingleOperation.None)
                },
                onConfirm = {
                    //用户坚持使用移动网络
                    changeOperation(
                        DownloadSingleOperation.SelectVersion(
                            classes = operation.classes,
                            version = operation.version,
                            dependencyEntries = operation.dependencyEntries
                        )
                    )
                }
            )
        }
        is DownloadSingleOperation.SelectVersion -> {
            val dependencyEntries = operation.dependencyEntries
            val classes = operation.classes

            DownloadDialog(
                dependencyEntries = dependencyEntries,
                classes = classes,
                installedProjects = installedProjects,
                onDismiss = {
                    changeOperation(DownloadSingleOperation.None)
                },
                onInstall = { versions, dependencies ->
                    changeOperation(
                        DownloadSingleOperation.Install(classes, operation.version, versions, dependencies)
                    )
                },
                onDependencyClicked = { platform, projectId, dependencyClasses ->
                    changeOperation(DownloadSingleOperation.None)
                    onDependencyClicked(platform, projectId, dependencyClasses)
                }
            )
        }
        is DownloadSingleOperation.Install -> {
            LaunchedEffect(Unit) {
                doInstall(operation.classes, operation.version, operation.versions, operation.dependencies)
                changeOperation(DownloadSingleOperation.None)
            }
        }
    }
}

@Composable
private fun rememberValidVersions(): State<List<Version>> {
    val vers by VersionsManager.versions.collectAsStateWithLifecycle()
    return remember(vers) {
        derivedStateOf {
            vers.filter { it.isValid() }
        }
    }
}

@Composable
private fun DownloadDialog(
    dependencyEntries: List<DependencyEntry>,
    classes: PlatformClasses,
    installedProjects: Map<Platform, Set<String>>,
    onDismiss: () -> Unit,
    onInstall: (List<Version>, List<DependencyRequest>) -> Unit,
    onDependencyClicked: (Platform, String, PlatformClasses) -> Unit
) {
    val versions by rememberValidVersions()
    val version by VersionsManager.currentVersion.collectAsStateWithLifecycle()
    val version0 = version

    if (version0 == null || versions.isEmpty()) {
        SimpleAlertDialog(
            title = stringResource(R.string.generic_warning),
            text = stringResource(R.string.download_assets_no_installed_versions),
            confirmText = stringResource(R.string.generic_got_it),
            onDismiss = onDismiss
        )
    } else {
        //当前选择的版本，将会把资源安装到该版本
        val selectedVersions = remember { mutableStateListOf(version0) }

        //拆分依赖项目、可选项目
        val dependencies = remember(dependencyEntries) {
            dependencyEntries.filter { it.dependency.type == PlatformDependencyType.REQUIRED }
        }
        val optionals = remember(dependencyEntries) {
            dependencyEntries.filter { it.dependency.type == PlatformDependencyType.OPTIONAL }
        }
        val hasDeps = dependencies.isNotEmpty() || optionals.isNotEmpty()

        //用户是否手动修改过依赖的勾选状态
        var userChangedDependencies by remember(dependencyEntries) { mutableStateOf(false) }

        val selectedDependencyKeys = remember(dependencyEntries) { mutableStateListOf<String>() }
        val onDependencySelectedChange: (String, Boolean) -> Unit = { key, checked ->
            userChangedDependencies = true
            if (checked) {
                if (!selectedDependencyKeys.contains(key)) selectedDependencyKeys.add(key)
            } else {
                selectedDependencyKeys.remove(key)
            }
        }

        // 默认勾选必装依赖，本地已安装的模组依赖默认不勾选
        // 已安装信息可能晚于对话框到达，用户未手动修改过时重新套用默认状态
        LaunchedEffect(dependencyEntries, installedProjects, userChangedDependencies) {
            if (userChangedDependencies) return@LaunchedEffect
            selectedDependencyKeys.clear()
            selectedDependencyKeys.addAll(
                dependencies
                    .filter { it.isInstallable() && !it.isModInstalled(installedProjects, classes) }
                    .map { it.dependency.cacheKey() }
            )
        }

        Dialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(
                usePlatformDefaultWidth = false
            )
        ) {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth(
                        fraction = if (hasDeps) 0.8f else 0.5f
                    )
                    .heightIn(max = rememberDialogMaxHeight())
                    .fillMaxHeight(),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    modifier = Modifier
                        .padding(all = 6.dp)
                        .heightIn(max = (maxHeight - 12.dp).coerceAtMost(rememberDialogMaxHeight()))
                        .wrapContentHeight(),
                    shape = MaterialTheme.shapes.extraLarge,
                    color = cardColor(false),
                    contentColor = onCardColor(),
                    shadowElevation = 6.dp
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .weight(1f, fill = false)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (hasDeps) {
                                val listState = rememberLazyListState()

                                LazyColumn(
                                    modifier = Modifier
                                        .fadeEdge(state = listState)
                                        .weight(1f)
                                        .nonInteractiveScrollbar(
                                            state = listState.scrollIndicatorState!!,
                                            orientation = Orientation.Vertical,
                                        ),
                                    contentPadding = PaddingValues(vertical = 8.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp),
                                    state = listState
                                ) {
                                    dependencies.takeIf { it.isNotEmpty() }?.let { dependencies ->
                                        dependencyLayout(
                                            list = dependencies,
                                            titleRes = R.string.download_assets_dependency_projects,
                                            defaultClasses = classes,
                                            selectedKeys = selectedDependencyKeys,
                                            onSelectedChange = onDependencySelectedChange,
                                            installedProjects = installedProjects,
                                            onDependencyClicked = onDependencyClicked
                                        )
                                    }
                                    optionals.takeIf { it.isNotEmpty() }?.let { optionals ->
                                        dependencyLayout(
                                            list = optionals,
                                            titleRes = R.string.download_assets_optional_projects,
                                            defaultClasses = classes,
                                            selectedKeys = selectedDependencyKeys,
                                            onSelectedChange = onDependencySelectedChange,
                                            installedProjects = installedProjects,
                                            onDependencyClicked = onDependencyClicked
                                        )
                                    }
                                }
                            }

                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                MarqueeText(
                                    modifier = if (hasDeps) {
                                        Modifier.padding(top = 8.dp)
                                    } else {
                                        Modifier.align(Alignment.CenterHorizontally)
                                    },
                                    text = stringResource(R.string.download_assets_install_assets_for_versions),
                                    style = MaterialTheme.typography.titleMedium
                                )

                                val listState = rememberLazyListState()

                                LaunchedEffect(Unit) {
                                    val target = selectedVersions.firstOrNull() ?: return@LaunchedEffect
                                    runCatching {
                                        val index = versions.indexOf(target)
                                        if (index >= 0) {
                                            listState.scrollToItem(index)
                                        }
                                    }
                                }

                                //选择游戏版本
                                ChoseGameVersionLayout(
                                    modifier = Modifier.fadeEdge(state = listState),
                                    versions = versions,
                                    selectedVersions = selectedVersions,
                                    onVersionSelected = { selectedVersions.add(it) },
                                    onVersionUnSelected = { selectedVersions.remove(it) },
                                    listState = listState
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            FilledTonalButton(
                                modifier = Modifier.weight(0.5f),
                                onClick = onDismiss
                            ) {
                                MarqueeText(text = stringResource(R.string.generic_cancel))
                            }
                            Button(
                                modifier = Modifier.weight(0.5f),
                                enabled = selectedVersions.isNotEmpty(),
                                onClick = {
                                    if (selectedVersions.isNotEmpty()) {
                                        onInstall(
                                            selectedVersions,
                                            dependencyEntries.mapNotNull { entry ->
                                                val project = entry.project ?: return@mapNotNull null
                                                if (entry.dependency.cacheKey() !in selectedDependencyKeys) {
                                                    return@mapNotNull null
                                                }
                                                DependencyRequest(
                                                    platform = entry.dependency.platform,
                                                    projectId = project.platformId(),
                                                    versionId = entry.dependency.versionId,
                                                    classes = project.platformClasses(classes),
                                                    projectTitle = project.platformTitle()
                                                )
                                            }
                                        )
                                    }
                                }
                            ) {
                                MarqueeText(text = stringResource(R.string.download_install))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChoseGameVersionLayout(
    modifier: Modifier = Modifier,
    versions: List<Version>,
    selectedVersions: List<Version>,
    onVersionSelected: (Version) -> Unit,
    onVersionUnSelected: (Version) -> Unit,
    listState: LazyListState
) {
    if (versions.isNotEmpty()) {
        LazyColumn(
            modifier = modifier.nonInteractiveScrollbar(
                state = listState.scrollIndicatorState!!,
                orientation = Orientation.Vertical,
            ),
            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            state = listState
        ) {
            items(versions) { version ->
                SelectVersionListItem(
                    modifier = Modifier.fillMaxWidth(),
                    version = version,
                    checked = selectedVersions.contains(version),
                    onChose = {
                        onVersionSelected(version)
                    },
                    onCancel = {
                        onVersionUnSelected(version)
                    }
                )
            }
        }
    }
}

@Composable
private fun SelectVersionListItem(
    modifier: Modifier = Modifier,
    version: Version,
    checked: Boolean,
    onChose: () -> Unit,
    onCancel: () -> Unit,
    shape: Shape = MaterialTheme.shapes.large,
    color: Color = itemColor(false),
    contentColor: Color = onItemColor(),
) {
    Surface(
        modifier = modifier,
        shape = shape,
        color = color,
        contentColor = contentColor,
        onClick = {
            if (checked) {
                onCancel()
            } else {
                onChose()
            }
        }
    ) {
        Row(
            modifier = modifier.padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = checked,
                onCheckedChange = {
                    if (it) {
                        onChose()
                    } else {
                        onCancel()
                    }
                }
            )
            CommonVersionInfoLayout(
                modifier = Modifier.weight(1f),
                version = version
            )
        }
    }
}

private fun LazyListScope.dependencyLayout(
    list: List<DependencyEntry>,
    titleRes: Int,
    defaultClasses: PlatformClasses,
    selectedKeys: List<String>,
    onSelectedChange: (String, Boolean) -> Unit,
    installedProjects: Map<Platform, Set<String>>,
    onDependencyClicked: (Platform, String, PlatformClasses) -> Unit
) {
    if (list.isEmpty()) return

    item {
        Text(
            text = stringResource(titleRes),
            style = MaterialTheme.typography.labelLarge
        )
    }
    //前置项目列表
    items(list) { entry ->
        val dependency = entry.dependency
        val project = entry.project
        if (project == null) {
            //依赖项目信息获取失败，展示占位项且不可选
            AssetsUnavailableDependencyItem(
                modifier = Modifier.fillMaxWidth(),
                title = dependency.projectId ?: dependency.versionId.orEmpty(),
                statusRes = if (entry.notFound) {
                    R.string.download_assets_id_not_found
                } else {
                    R.string.download_assets_dependency_project_unavailable
                }
            )
            return@items
        }

        val key = dependency.cacheKey()
        val dependencyClasses = project.platformClasses(defaultClasses)
        AssetsVersionDependencyItem(
            modifier = Modifier.fillMaxWidth(),
            project = project,
            checked = selectedKeys.contains(key),
            onCheckedChange = { onSelectedChange(key, it) },
            installed = dependencyClasses == PlatformClasses.MOD &&
                    installedProjects[dependency.platform]?.contains(project.platformId()) == true,
            onClick = {
                onDependencyClicked(dependency.platform, project.platformId(), dependencyClasses)
            }
        )
    }
}

/**
 * 是否可安装：依赖项目信息获取成功的才能被选中安装
 */
private fun DependencyEntry.isInstallable(): Boolean = project != null

/**
 * 该依赖是否为本地已安装的模组项目
 */
private fun DependencyEntry.isModInstalled(
    installedProjects: Map<Platform, Set<String>>,
    defaultClasses: PlatformClasses
): Boolean {
    val project = project ?: return false
    return project.platformClasses(defaultClasses) == PlatformClasses.MOD &&
            installedProjects[dependency.platform]?.contains(project.platformId()) == true
}

/**
 * 依赖项目信息获取失败时的占位项
 */
@Composable
private fun AssetsUnavailableDependencyItem(
    modifier: Modifier = Modifier,
    title: String,
    @StringRes statusRes: Int,
    shape: Shape = MaterialTheme.shapes.large,
    color: Color = itemColor(false),
    contentColor: Color = onItemColor(),
) {
    Surface(
        modifier = modifier,
        shape = shape,
        color = color,
        contentColor = contentColor,
    ) {
        Column(
            modifier = Modifier.padding(all = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AssetsIcon(
                    modifier = Modifier.clip(shape = RoundedCornerShape(10.dp)),
                    size = 48.dp,
                    iconUrl = null
                )
                Text(
                    modifier = Modifier.weight(1f),
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    modifier = Modifier.height(28.dp),
                    checked = false,
                    onCheckedChange = null
                )
                Text(
                    modifier = Modifier.weight(1f),
                    text = stringResource(statusRes),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun AssetsVersionDependencyItem(
    modifier: Modifier = Modifier,
    project: PlatformProject,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    installed: Boolean,
    onClick: () -> Unit = {},
    shape: Shape = MaterialTheme.shapes.large,
    color: Color = itemColor(false),
    contentColor: Color = onItemColor(),
) {
    //项目基本信息
    val platform = remember { project.platform() }
    val title = remember { project.platformTitle() }
    val summary = remember { project.platformSummary() }
    val iconUrl = remember { project.platformIconUrl() }
    val downloads = remember { project.platformDownloadCount() }
    val follows = remember { project.platformFollows() }
    val modLoaders = remember { project.platformModLoaders() }

    Surface(
        modifier = modifier,
        onClick = onClick,
        shape = shape,
        color = color,
        contentColor = contentColor,
    ) {
        Row(
            modifier = Modifier
                .padding(all = 8.dp)
                .height(IntrinsicSize.Min),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxHeight()
            ) {
                AssetsIcon(
                    modifier = Modifier.clip(shape = RoundedCornerShape(10.dp)),
                    size = 48.dp,
                    iconUrl = iconUrl
                )
                Spacer(modifier = Modifier.weight(1f))
                Checkbox(
                    modifier = Modifier.height(24.dp),
                    checked = checked,
                    onCheckedChange = onCheckedChange
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                ProjectTitleHead(
                    platform = platform,
                    title = title,
                    author = null //ui太小，展示不下
                )
                summary?.let { summary ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        //描述
                        Text(
                            modifier = Modifier.weight(1f),
                            text = summary,
                            style = MaterialTheme.typography.bodySmall,
                            minLines = 2,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        AssetsDependencyCounts(
                            downloads = downloads,
                            follows = follows
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                //加载器标签与已安装标注
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AssetsDependencyTags(
                        modifier = Modifier.weight(1f),
                        modLoaders = modLoaders
                    )
                    if (summary == null) {
                        //没有摘要时，计数移到底部行展示
                        AssetsDependencyCounts(
                            downloads = downloads,
                            follows = follows
                        )
                    }
                    if (installed) {
                        InstalledModBadge()
                    }
                }
            }
        }
    }
}

/**
 * 依赖项目支持的模组加载器标签
 */
@Composable
private fun AssetsDependencyTags(
    modifier: Modifier = Modifier,
    modLoaders: List<PlatformDisplayLabel>?
) {
    Row(
        modifier = modifier
            .alpha(0.7f)
            .basicMarquee(iterations = Int.MAX_VALUE),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        modLoaders?.forEach { modLoader ->
            Text(
                text = modLoader.getDisplayName(),
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}

/**
 * 依赖项目的下载量与收藏量
 */
@Composable
private fun AssetsDependencyCounts(
    modifier: Modifier = Modifier,
    downloads: Long,
    follows: Long?
) {
    val context = LocalContext.current

    Column(
        modifier = modifier.alpha(0.7f),
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                modifier = Modifier.size(16.dp),
                painter = painterResource(R.drawable.ic_download_2_outlined),
                contentDescription = null
            )
            Text(
                text = formatNumberByLocale(context, downloads),
                style = MaterialTheme.typography.labelSmall
            )
        }
        follows?.let {
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    modifier = Modifier.size(14.dp),
                    painter = painterResource(R.drawable.ic_favorite_outlined),
                    contentDescription = null
                )
                Text(
                    text = formatNumberByLocale(context, it),
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}
