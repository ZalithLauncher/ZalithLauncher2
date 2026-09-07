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

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.game.download.assets.favorites.AssetFavoriteManager
import com.movtery.zalithlauncher.game.download.assets.favorites.toAssetFavorite
import com.movtery.zalithlauncher.game.download.assets.platform.PlatformClasses
import com.movtery.zalithlauncher.game.download.assets.platform.PlatformDependencyType
import com.movtery.zalithlauncher.game.download.assets.platform.PlatformProject
import com.movtery.zalithlauncher.game.download.assets.platform.PlatformVersion
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

/**
 * 操作状态：下载单个资源文件
 */
sealed interface DownloadSingleOperation {
    data object None : DownloadSingleOperation
    /** 警告用户正在使用移动网络 */
    data class WarningForMobileData(
        val classes: PlatformClasses,
        val version: PlatformVersion,
        val dependencyProjects: List<Pair<PlatformVersion.PlatformDependency, PlatformProject>>
    ) : DownloadSingleOperation
    /** 选择版本 */
    data class SelectVersion(
        val classes: PlatformClasses,
        val version: PlatformVersion,
        val dependencyProjects: List<Pair<PlatformVersion.PlatformDependency, PlatformProject>>
    ) : DownloadSingleOperation
    /** 安装 */
    data class Install(
        val classes: PlatformClasses,
        val version: PlatformVersion,
        val versions: List<Version>
    ) : DownloadSingleOperation
}

@Composable
fun DownloadSingleOperation(
    operation: DownloadSingleOperation,
    changeOperation: (DownloadSingleOperation) -> Unit,
    doInstall: (PlatformClasses, PlatformVersion, List<Version>) -> Unit,
    onDependencyClicked: (PlatformVersion.PlatformDependency, PlatformClasses) -> Unit = { _, _ -> }
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
                            dependencyProjects = operation.dependencyProjects
                        )
                    )
                }
            )
        }
        is DownloadSingleOperation.SelectVersion -> {
            val dependencyProjects = operation.dependencyProjects
            val classes = operation.classes

            DownloadDialog(
                dependencyProjects = dependencyProjects,
                classes = classes,
                onDismiss = {
                    changeOperation(DownloadSingleOperation.None)
                },
                onInstall = { versions ->
                    changeOperation(DownloadSingleOperation.Install(classes, operation.version, versions))
                },
                onDependencyClicked = { dependency, classes ->
                    changeOperation(DownloadSingleOperation.None)
                    onDependencyClicked(dependency, classes)
                }
            )
        }
        is DownloadSingleOperation.Install -> {
            LaunchedEffect(Unit) {
                doInstall(operation.classes, operation.version, operation.versions)
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
    dependencyProjects: List<Pair<PlatformVersion.PlatformDependency, PlatformProject>>,
    classes: PlatformClasses,
    onDismiss: () -> Unit,
    onInstall: (List<Version>) -> Unit,
    onDependencyClicked: (PlatformVersion.PlatformDependency, PlatformClasses) -> Unit
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
        val dependencies = remember(dependencyProjects) {
            dependencyProjects.filter { it.first.type == PlatformDependencyType.REQUIRED }
        }
        val optionals = remember(dependencyProjects) {
            dependencyProjects.filter { it.first.type == PlatformDependencyType.OPTIONAL }
        }
        val hasDeps = dependencies.isNotEmpty() || optionals.isNotEmpty()

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
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                    state = listState
                                ) {
                                    dependencies.takeIf { it.isNotEmpty() }?.let { dependencies ->
                                        dependencyLayout(
                                            list = dependencies,
                                            titleRes = R.string.download_assets_dependency_projects,
                                            defaultClasses = classes,
                                            onDependencyClicked = onDependencyClicked
                                        )
                                    }
                                    optionals.takeIf { it.isNotEmpty() }?.let { optionals ->
                                        dependencyLayout(
                                            list = optionals,
                                            titleRes = R.string.download_assets_optional_projects,
                                            defaultClasses = classes,
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
                                        onInstall(selectedVersions)
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
    list: List<Pair<PlatformVersion.PlatformDependency, PlatformProject>>,
    titleRes: Int,
    defaultClasses: PlatformClasses,
    onDependencyClicked: (PlatformVersion.PlatformDependency, PlatformClasses) -> Unit
) {
    if (list.isNotEmpty()) {
        item {
            Text(
                text = stringResource(titleRes),
                style = MaterialTheme.typography.labelLarge
            )
        }
        //前置项目列表
        items(list) { (dependency, dependencyProject) ->
            AssetsVersionDependencyItem(
                modifier = Modifier.fillMaxWidth(),
                project = dependencyProject,
                defaultClasses = defaultClasses,
                onClick = {
                    onDependencyClicked(dependency, dependencyProject.platformClasses(defaultClasses))
                }
            )
        }
    }
}

@Composable
private fun AssetsVersionDependencyItem(
    modifier: Modifier = Modifier,
    project: PlatformProject,
    defaultClasses: PlatformClasses,
    onClick: () -> Unit = {},
    shape: Shape = MaterialTheme.shapes.large,
    color: Color = itemColor(false),
    contentColor: Color = onItemColor(),
) {
    //项目基本信息
    val platform = remember { project.platform() }
    val projectId = remember { project.platformId() }
    val classes = remember { project.platformClasses(defaultClasses) }
    val title = remember { project.platformTitle() }
    val summary = remember { project.platformSummary() }
    val iconUrl = remember { project.platformIconUrl() }

    //项目收藏状态
    val favoriteKeys by AssetFavoriteManager.favoriteKeys.collectAsStateWithLifecycle()
    val isFavorite = remember(favoriteKeys, platform, projectId) {
        AssetFavoriteManager.isProjectFavorite(platform, projectId)
    }
    //防止点击闭包捕获陈旧的收藏态
    val currentIsFavorite by rememberUpdatedState(isFavorite)

    Surface(
        modifier = modifier,
        onClick = onClick,
        shape = shape,
        color = color,
        contentColor = contentColor,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AssetsIcon(
                modifier = Modifier
                    .padding(all = 8.dp)
                    .clip(shape = RoundedCornerShape(10.dp)),
                size = 48.dp,
                iconUrl = iconUrl
            )
            Column(
                modifier = Modifier.weight(1f)
            ) {
                ProjectTitleHead(
                    platform = platform,
                    title = title,
                    author = null, //ui太小，展示不下
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
                    }
                )
                summary?.let { summary ->
                    Text(
                        text = summary,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Spacer(modifier = Modifier.width(2.dp))
        }
    }
}
