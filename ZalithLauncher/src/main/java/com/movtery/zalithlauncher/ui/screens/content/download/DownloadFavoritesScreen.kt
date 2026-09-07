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

package com.movtery.zalithlauncher.ui.screens.content.download

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.game.download.assets.downloadSingleForVersions
import com.movtery.zalithlauncher.game.download.assets.favorites.AssetFavorite
import com.movtery.zalithlauncher.game.download.assets.favorites.hasFixedVersion
import com.movtery.zalithlauncher.game.download.assets.favorites.toPlatformVersion
import com.movtery.zalithlauncher.game.download.assets.platform.getProjectByVersion
import com.movtery.zalithlauncher.game.download.assets.platform.getVersions
import com.movtery.zalithlauncher.ui.androidText
import com.movtery.zalithlauncher.ui.screens.NestedNavKey
import com.movtery.zalithlauncher.ui.screens.NormalNavKey
import com.movtery.zalithlauncher.ui.screens.TitledNavKey
import com.movtery.zalithlauncher.ui.screens.content.download.assets.download.DownloadAssetsScreen
import com.movtery.zalithlauncher.ui.screens.content.download.assets.elements.DownloadSingleOperation
import com.movtery.zalithlauncher.ui.screens.content.download.assets.favorites.FavoritesListScreen
import com.movtery.zalithlauncher.ui.screens.navigateTo
import com.movtery.zalithlauncher.ui.screens.onBack
import com.movtery.zalithlauncher.ui.screens.rememberTransitionSpec
import com.movtery.zalithlauncher.utils.copyText
import com.movtery.zalithlauncher.utils.logging.Logger
import com.movtery.zalithlauncher.utils.network.isUsingMobileData
import com.movtery.zalithlauncher.viewmodel.ErrorViewModel
import com.movtery.zalithlauncher.viewmodel.EventViewModel
import com.movtery.zalithlauncher.viewmodel.sendToast
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import java.io.IOException

private const val TAG = "DownloadFavoritesScreen"

@Composable
fun DownloadFavoritesScreen(
    key: NestedNavKey.DownloadFavorites,
    mainScreenKey: TitledNavKey?,
    downloadScreenKey: TitledNavKey?,
    downloadFavoritesScreenKey: TitledNavKey?,
    onCurrentKeyChange: (TitledNavKey?) -> Unit,
    submitError: (ErrorViewModel.ThrowableMessage) -> Unit,
    eventViewModel: EventViewModel
) {
    val backStack = key.backStack
    val stackTopKey = backStack.lastOrNull()
    LaunchedEffect(stackTopKey) {
        onCurrentKeyChange(stackTopKey)
    }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var operation by remember { mutableStateOf<DownloadSingleOperation>(DownloadSingleOperation.None) }
    DownloadSingleOperation(
        operation = operation,
        changeOperation = { operation = it },
        doInstall = { classes, version, gameVersions ->
            downloadSingleForVersions(
                version = version,
                versions = gameVersions,
                folder = classes.versionFolder.folderName,
                submitError = submitError
            )
        },
        onDependencyClicked = { dep, classes ->
            backStack.navigateTo(
                NormalNavKey.DownloadAssets(dep.platform, dep.projectId, classes)
            )
        }
    )

    /**
     * 下载版本收藏：先在线重取该版本的最新信息（依赖列表与最新直链），
     * 在线获取失败时回退收藏快照并提示
     */
    fun startVersionDownload(favorite: AssetFavorite) {
        //无固定版本/直链的快照没有可下载内容（版本行按钮已用同一条件禁用，这里兜底）
        if (!favorite.hasFixedVersion) return
        scope.launch {
            val version = runCatching {
                val fresh = getVersions(favorite.projectId, favorite.platform)
                    .firstOrNull { it.platformId() == favorite.versionId }
                    ?: throw IOException("Version ${favorite.versionId} no longer exists online")
                if (!fresh.initFile(favorite.projectId)) {
                    throw IOException("Failed to init file for version ${favorite.versionId}")
                }
                fresh
            }.getOrElse { e ->
                if (e is CancellationException) throw e
                Logger.warning(TAG, "Failed to refetch version online, fallback to snapshot", e)
                eventViewModel.sendToast(androidText(R.string.download_favorites_download_fallback))
                favorite.toPlatformVersion()
            }

            //在线重取的版本可解析依赖项目；快照不缓存依赖，恒为空列表
            val dependencyProjects = version.platformDependencies().mapNotNull { dep ->
                runCatching { getProjectByVersion(dep.projectId, dep.platform, printLog = false) }
                    .getOrNull()
                    ?.let { dep to it }
            }

            operation = if (isUsingMobileData(context)) {
                DownloadSingleOperation.WarningForMobileData(
                    classes = favorite.classes,
                    version = version,
                    dependencyProjects = dependencyProjects
                )
            } else {
                DownloadSingleOperation.SelectVersion(
                    classes = favorite.classes,
                    version = version,
                    dependencyProjects = dependencyProjects
                )
            }
        }
    }

    if (backStack.isNotEmpty()) {
        NavDisplay(
            backStack = backStack,
            modifier = Modifier.fillMaxSize(),
            onBack = {
                onBack(backStack)
            },
            entryDecorators = listOf(
                rememberSaveableStateHolderNavEntryDecorator(),
                rememberViewModelStoreNavEntryDecorator()
            ),
            transitionSpec = rememberTransitionSpec(),
            popTransitionSpec = rememberTransitionSpec(),
            entryProvider = entryProvider {
                entry<NormalNavKey.FavoritesList> {
                    FavoritesListScreen(
                        mainScreenKey = mainScreenKey,
                        downloadScreenKey = downloadScreenKey,
                        downloadFavoritesScreenKey = key,
                        downloadFavoritesScreenCurrentKey = downloadFavoritesScreenKey,
                        swapToDownload = { platform, projectId, classes, iconUrl ->
                            backStack.navigateTo(
                                NormalNavKey.DownloadAssets(
                                    platform = platform,
                                    projectId = projectId,
                                    classes = classes,
                                    iconUrl = iconUrl
                                )
                            )
                        },
                        onDownloadVersion = { favorite ->
                            startVersionDownload(favorite)
                        },
                        onCopyLink = { favorite ->
                            copyText(favorite.versionName, favorite.downloadUrl, context, showToast = false)
                            eventViewModel.sendToast(androidText(R.string.download_favorites_link_copied))
                        }
                    )
                }
                entry<NormalNavKey.DownloadAssets> { assetsKey ->
                    DownloadAssetsScreen(
                        mainScreenKey = mainScreenKey,
                        parentScreenKey = key,
                        parentCurrentKey = downloadScreenKey,
                        currentKey = downloadFavoritesScreenKey,
                        key = assetsKey,
                        eventViewModel = eventViewModel,
                        onItemClicked = { classes, version, _, deps ->
                            operation = if (isUsingMobileData(context)) {
                                DownloadSingleOperation.WarningForMobileData(classes, version, deps)
                            } else {
                                DownloadSingleOperation.SelectVersion(classes, version, deps)
                            }
                        }
                    )
                }
            }
        )
    } else {
        Box(Modifier.fillMaxSize())
    }
}
