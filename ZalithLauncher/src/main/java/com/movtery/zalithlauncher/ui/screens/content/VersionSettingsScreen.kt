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

package com.movtery.zalithlauncher.ui.screens.content

import android.content.Context
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.JsonSyntaxException
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.game.download.assets.platform.PlatformClasses
import com.movtery.zalithlauncher.game.download.game.GameDownloadInfo
import com.movtery.zalithlauncher.game.download.game.GameInstaller
import com.movtery.zalithlauncher.game.download.game.optifine.CantFetchingOptiFineUrlException
import com.movtery.zalithlauncher.game.download.jvm_server.JvmCrashException
import com.movtery.zalithlauncher.game.version.download.DownloadFailedException
import com.movtery.zalithlauncher.game.version.installed.Version
import com.movtery.zalithlauncher.game.version.installed.VersionsManager
import com.movtery.zalithlauncher.notification.NotificationManager
import com.movtery.zalithlauncher.ui.base.BaseScreen
import com.movtery.zalithlauncher.ui.components.MarqueeText
import com.movtery.zalithlauncher.ui.components.NotificationCheck
import com.movtery.zalithlauncher.ui.components.fadeEdge
import com.movtery.zalithlauncher.ui.screens.NestedNavKey
import com.movtery.zalithlauncher.ui.screens.NormalNavKey
import com.movtery.zalithlauncher.ui.screens.TitledNavKey
import com.movtery.zalithlauncher.ui.screens.content.elements.CategoryIcon
import com.movtery.zalithlauncher.ui.screens.content.elements.CategoryItem
import com.movtery.zalithlauncher.ui.screens.content.elements.TitleTaskFlowDialog
import com.movtery.zalithlauncher.ui.screens.content.versions.AddonDiffs
import com.movtery.zalithlauncher.ui.screens.content.versions.ModsManagerScreen
import com.movtery.zalithlauncher.ui.screens.content.versions.ResourcePackManageScreen
import com.movtery.zalithlauncher.ui.screens.content.versions.SavesManagerScreen
import com.movtery.zalithlauncher.ui.screens.content.versions.ScreenshotsManagerScreen
import com.movtery.zalithlauncher.ui.screens.content.versions.ServerListScreen
import com.movtery.zalithlauncher.ui.screens.content.versions.ShadersManagerScreen
import com.movtery.zalithlauncher.ui.screens.content.versions.UpdateLoaderScreen
import com.movtery.zalithlauncher.ui.screens.content.versions.VersionConfigScreen
import com.movtery.zalithlauncher.ui.screens.content.versions.VersionOverViewScreen
import com.movtery.zalithlauncher.ui.screens.navigateOnce
import com.movtery.zalithlauncher.ui.screens.onBack
import com.movtery.zalithlauncher.ui.screens.rememberTransitionSpec
import com.movtery.zalithlauncher.ui.theme.showThemed
import com.movtery.zalithlauncher.utils.animation.swapAnimateDpAsState
import com.movtery.zalithlauncher.utils.logging.Logger
import com.movtery.zalithlauncher.viewmodel.ErrorViewModel
import com.movtery.zalithlauncher.viewmodel.EventViewModel
import com.movtery.zalithlauncher.viewmodel.ScreenBackStackViewModel
import io.ktor.client.plugins.HttpRequestTimeoutException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.SerializationException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.nio.channels.UnresolvedAddressException
import java.util.concurrent.TimeoutException

private const val TAG = "VersionSettings"

/** 更新加载器状态操作 */
private sealed interface UpdateLoaderOperation {
    data object None: UpdateLoaderOperation
    /** 提醒加载器的变更情况 */
    data class Tip(val diffs: AddonDiffs, val info: GameDownloadInfo): UpdateLoaderOperation
    /** 警告通知权限，可以无视，并直接开始安装 */
    data class WarningForNotification(val diffs: AddonDiffs, val info: GameDownloadInfo): UpdateLoaderOperation
    /** 开始安装 */
    data object Install: UpdateLoaderOperation
    /** 安装过程中出现异常 */
    data class Error(val th: Throwable): UpdateLoaderOperation
}

private class UpdateLoaderViewModel: ViewModel() {
    var installOperation by mutableStateOf<UpdateLoaderOperation>(UpdateLoaderOperation.None)

    /**
     * 游戏安装器
     */
    var installer by mutableStateOf<GameInstaller?>(null)

    fun install(
        context: Context,
        info: GameDownloadInfo
    ) {
        installOperation = UpdateLoaderOperation.Install
        installer = GameInstaller(context, info, viewModelScope).also {
            it.updateLoader(
                onInstalled = {
                    installer = null
                    installOperation = UpdateLoaderOperation.None

                    viewModelScope.launch(Dispatchers.Main) {
                        VersionsManager.refresh("[UpdateLoader] GameInstaller.onInstalled")

                        MaterialAlertDialogBuilder(context)
                            .setTitle(R.string.download_install_success_title)
                            .setMessage(R.string.versions_update_loader_success_message)
                            .setPositiveButton(R.string.generic_confirm) { dialog, _ ->
                                dialog.dismiss()
                            }
                            .showThemed()
                    }
                },
                onError = { th ->
                    installer = null
                    installOperation = UpdateLoaderOperation.Error(th)
                }
            )
        }
    }

    fun cancel() {
        installer?.cancelInstall(
            clearTarget = false
        )
        installer = null
        installOperation = UpdateLoaderOperation.None
    }

    override fun onCleared() {
        cancel()
    }
}

@Composable
private fun rememberUpdateLoaderViewModel(
    key: NestedNavKey.VersionSettings
): UpdateLoaderViewModel {
    return viewModel(
        key = key.toString() + "_UpdateLoader"
    ) {
        UpdateLoaderViewModel()
    }
}

@Composable
fun VersionSettingsScreen(
    key: NestedNavKey.VersionSettings,
    backScreenViewModel: ScreenBackStackViewModel,
    backToMainScreen: () -> Unit,
    onExportModpack: () -> Unit,
    eventViewModel: EventViewModel,
    submitError: (ErrorViewModel.ThrowableMessage) -> Unit
) {
    val context = LocalContext.current
    val viewModel = rememberUpdateLoaderViewModel(key = key)

    val cBackToMainScreen by rememberUpdatedState(backToMainScreen)
    DisposableEffect(key) {
        val listener = object : suspend (List<Version>) -> Unit {
            override suspend fun invoke(versions: List<Version>) {
                cBackToMainScreen()
            }
        }
        VersionsManager.registerListener(listener)
        onDispose {
            VersionsManager.unregisterListener(listener)
        }
    }

    UpdateLoaderOperation(
        operation = viewModel.installOperation,
        changeOperation = { viewModel.installOperation = it },
        installer = viewModel.installer,
        onInstall = { info ->
            viewModel.install(context, info)
        },
        onCancel = {
            viewModel.cancel()
        }
    )

    BaseScreen(
        screenKey = key,
        currentKey = backScreenViewModel.mainScreen.currentKey
    ) { isVisible ->
        Row(modifier = Modifier.fillMaxSize()) {
            val loaderInfo = remember(key) {
                key.version.getVersionInfo()?.loaderInfo
            }

            TabMenu(
                isVisible = isVisible,
                backStack = key.backStack,
                versionsScreenKey = key.currentKey,
                canUpdateLoader = loaderInfo == null || loaderInfo.loader.autoDownloadable,
                isUpdateLoader = loaderInfo != null && loaderInfo.loader.autoDownloadable,
                modifier = Modifier.fillMaxHeight()
            )

            NavigationUI(
                modifier = Modifier.fillMaxHeight(),
                key = key,
                viewModel = viewModel,
                backScreenViewModel = backScreenViewModel,
                versionsScreenKey = key.currentKey,
                onCurrentKeyChange = { newKey ->
                    key.currentKey = newKey
                },
                backToMainScreen = backToMainScreen,
                onExport = onExportModpack,
                version = key.version,
                eventViewModel = eventViewModel,
                submitError = submitError
            )
        }
    }
}

private val settingItems = listOf(
    CategoryItem(NormalNavKey.Versions.OverView, { CategoryIcon(R.drawable.ic_dashboard_outlined, R.string.versions_settings_overview) }, R.string.versions_settings_overview),
    CategoryItem(NormalNavKey.Versions.Config, { CategoryIcon(R.drawable.ic_build_outlined, R.string.versions_settings_config) }, R.string.versions_settings_config),
    CategoryItem(NormalNavKey.Versions.UpdateLoader, { CategoryIcon(R.drawable.ic_update, R.string.versions_update_loader) }, R.string.versions_update_loader),
    CategoryItem(NormalNavKey.Versions.ModsManager, { CategoryIcon(R.drawable.ic_extension_outlined, R.string.mods_manage) }, R.string.mods_manage, division = true),
    CategoryItem(NormalNavKey.Versions.SavesManager, { CategoryIcon(R.drawable.ic_public, R.string.saves_manage) }, R.string.saves_manage),
    CategoryItem(NormalNavKey.Versions.ResourcePackManager, { CategoryIcon(R.drawable.ic_format_paint_outlined, R.string.resource_pack_manage) }, R.string.resource_pack_manage),
    CategoryItem(NormalNavKey.Versions.ShadersManager, { CategoryIcon(R.drawable.ic_lightbulb, R.string.shader_pack_manage) }, R.string.shader_pack_manage),
    CategoryItem(NormalNavKey.Versions.ScreenshotsManager, { CategoryIcon(R.drawable.ic_photo_library_outlined, R.string.screenshots_manage) }, R.string.screenshots_manage),
    CategoryItem(NormalNavKey.Versions.ServerList, { CategoryIcon(R.drawable.ic_dns_outlined, R.string.servers_list) }, R.string.servers_list, division = true),
)

@Composable
private fun TabMenu(
    isVisible: Boolean,
    backStack: NavBackStack<TitledNavKey>,
    versionsScreenKey: TitledNavKey?,
    canUpdateLoader: Boolean,
    isUpdateLoader: Boolean,
    modifier: Modifier = Modifier
) {
    val xOffset by swapAnimateDpAsState(
        targetValue = (-40).dp,
        swapIn = isVisible,
        isHorizontal = true
    )

    val scrollState = rememberScrollState()
    Column(
        modifier = modifier
            .fadeEdge(scrollState)
            .width(IntrinsicSize.Min)
            .padding(start = 8.dp)
            .offset { IntOffset(x = xOffset.roundToPx(), y = 0) }
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(12.dp))
        settingItems.forEach { item ->
            if (item.key == NormalNavKey.Versions.UpdateLoader && !canUpdateLoader) {
                //不支持自动更新安装，不放置“更新加载器/安装加载器”入口
                return@forEach
            }

            if (item.division) {
                HorizontalDivider(
                    modifier = Modifier
                        .padding(vertical = 12.dp)
                        .fillMaxWidth(0.4f)
                        .alpha(0.4f),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            NavigationRailItem(
                selected = versionsScreenKey === item.key,
                onClick = {
                    if (item.key == NormalNavKey.Versions.UpdateLoader) {
                        if (isUpdateLoader) {
                            NormalNavKey.Versions.UpdateLoader.title = R.string.versions_update_loader
                        } else {
                            NormalNavKey.Versions.UpdateLoader.title = R.string.versions_install_loader
                        }
                    }
                    backStack.navigateOnce(item.key)
                },
                icon = {
                    item.icon()
                },
                label = {
                    val text = if (item.key == NormalNavKey.Versions.UpdateLoader) {
                        if (isUpdateLoader) {
                            NormalNavKey.Versions.UpdateLoader.title = R.string.versions_update_loader
                            stringResource(item.textRes)
                        } else {
                            NormalNavKey.Versions.UpdateLoader.title = R.string.versions_install_loader
                            stringResource(R.string.versions_install_loader)
                        }
                    } else {
                        stringResource(item.textRes)
                    }
                    Text(
                        modifier = Modifier.basicMarquee(iterations = Int.MAX_VALUE),
                        text = text,
                        maxLines = 1,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            )

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun NavigationUI(
    modifier: Modifier = Modifier,
    key: NestedNavKey.VersionSettings,
    viewModel: UpdateLoaderViewModel,
    backScreenViewModel: ScreenBackStackViewModel,
    versionsScreenKey: TitledNavKey?,
    onCurrentKeyChange: (TitledNavKey?) -> Unit,
    backToMainScreen: () -> Unit,
    onExport: () -> Unit,
    version: Version,
    eventViewModel: EventViewModel,
    submitError: (ErrorViewModel.ThrowableMessage) -> Unit
) {
    val context = LocalContext.current
    val mainScreenKey = backScreenViewModel.mainScreen.currentKey

    val backStack = key.backStack
    val stackTopKey = backStack.lastOrNull()
    LaunchedEffect(stackTopKey) {
        onCurrentKeyChange(stackTopKey)
    }

    if (backStack.isNotEmpty()) {
        NavDisplay(
            backStack = backStack,
            modifier = modifier,
            onBack = {
                onBack(backStack)
            },
            transitionSpec = rememberTransitionSpec(),
            popTransitionSpec = rememberTransitionSpec(),
            entryProvider = entryProvider {
                entry<NormalNavKey.Versions.OverView> {
                    VersionOverViewScreen(
                        mainScreenKey = mainScreenKey,
                        versionsScreenKey = versionsScreenKey,
                        backToMainScreen = backToMainScreen,
                        onExport = onExport,
                        version = version,
                        eventViewModel = eventViewModel,
                        submitError = submitError
                    )
                }
                entry<NormalNavKey.Versions.Config> {
                    VersionConfigScreen(
                        mainScreenKey = mainScreenKey,
                        versionsScreenKey = versionsScreenKey,
                        version = version,
                        backToMainScreen = backToMainScreen,
                        onCheckVulkan = {
                            eventViewModel.sendEvent(
                                EventViewModel.Event.VulkanCheck
                            )
                        },
                        submitError = submitError
                    )
                }
                entry<NormalNavKey.Versions.UpdateLoader> {
                    UpdateLoaderScreen(
                        mainScreenKey = mainScreenKey,
                        versionsScreenKey = versionsScreenKey,
                        backToMainScreen = backToMainScreen,
                        version = version
                    ) { diffs, info ->
                        if (viewModel.installOperation !is UpdateLoaderOperation.None) {
                            //不是待安装状态，拒绝此次安装
                            return@UpdateLoaderScreen
                        }
                        if (!NotificationManager.checkNotificationEnabled(context)) {
                            //警告通知权限
                            viewModel.installOperation = UpdateLoaderOperation.WarningForNotification(diffs, info)
                        } else {
                            viewModel.installOperation = UpdateLoaderOperation.Tip(diffs, info)
                        }
                    }
                }
                entry(NormalNavKey.Versions.ModsManager) {
                    ModsManagerScreen(
                        mainScreenKey = mainScreenKey,
                        versionsScreenKey = versionsScreenKey,
                        version = version,
                        backToMainScreen = backToMainScreen,
                        swapToDownload = {
                            backScreenViewModel.navigateToDownload(
                                targetScreen = backScreenViewModel.downloadModScreen
                            )
                        },
                        onSwapMoreInfo = { projectId, platform ->
                            backScreenViewModel.navigateToDownload(
                                targetScreen = backScreenViewModel.downloadModScreen.apply {
                                    navigateTo(
                                        NormalNavKey.DownloadAssets(platform, projectId, PlatformClasses.MOD)
                                    )
                                }
                            )
                        },
                        eventViewModel = eventViewModel,
                        submitError = submitError
                    )
                }
                entry<NormalNavKey.Versions.SavesManager> {
                    SavesManagerScreen(
                        mainScreenKey = mainScreenKey,
                        versionsScreenKey = versionsScreenKey,
                        version = version,
                        backToMainScreen = backToMainScreen,
                        swapToDownload = {
                            backScreenViewModel.navigateToDownload(
                                targetScreen = backScreenViewModel.downloadSavesScreen
                            )
                        },
                        onQuickPlay = { version, saveName ->
                            eventViewModel.sendEvent(
                                EventViewModel.Event.Launch.PlaySave(
                                    version = version,
                                    saveName = saveName
                                )
                            )
                        },
                        submitError = submitError
                    )
                }
                entry<NormalNavKey.Versions.ResourcePackManager> {
                    ResourcePackManageScreen(
                        mainScreenKey = mainScreenKey,
                        versionsScreenKey = versionsScreenKey,
                        version = version,
                        backToMainScreen = backToMainScreen,
                        swapToDownload =  {
                            backScreenViewModel.navigateToDownload(
                                targetScreen = backScreenViewModel.downloadResourcePackScreen
                            )
                        },
                        submitError = submitError
                    )
                }
                entry<NormalNavKey.Versions.ShadersManager> {
                    ShadersManagerScreen(
                        mainScreenKey = mainScreenKey,
                        versionsScreenKey = versionsScreenKey,
                        version = version,
                        backToMainScreen = backToMainScreen,
                        swapToDownload = {
                            backScreenViewModel.navigateToDownload(
                                targetScreen = backScreenViewModel.downloadShadersScreen
                            )
                        },
                        submitError = submitError
                    )
                }
                entry<NormalNavKey.Versions.ScreenshotsManager> {
                    ScreenshotsManagerScreen(
                        mainScreenKey = mainScreenKey,
                        versionsScreenKey = versionsScreenKey,
                        version = version,
                        backToMainScreen = backToMainScreen,
                        submitError = submitError
                    )
                }
                entry<NormalNavKey.Versions.ServerList> {
                    ServerListScreen(
                        mainScreenKey = mainScreenKey,
                        versionsScreenKey = versionsScreenKey,
                        version = version,
                        onQuickPlay = { version, address ->
                            eventViewModel.sendEvent(
                                EventViewModel.Event.Launch.PlayServer(
                                    version = version,
                                    address = address
                                )
                            )
                        },
                        backToMainScreen = backToMainScreen,
                    )
                }
            }
        )
    } else {
        Box(modifier)
    }
}

@Composable
private fun UpdateLoaderOperation(
    operation: UpdateLoaderOperation,
    changeOperation: (UpdateLoaderOperation) -> Unit,
    installer: GameInstaller?,
    onInstall: (GameDownloadInfo) -> Unit,
    onCancel: () -> Unit
) {
    when (operation) {
        is UpdateLoaderOperation.None -> {}
        is UpdateLoaderOperation.WarningForNotification -> {
            NotificationCheck(
                text = stringResource(R.string.notification_data_jvm_service_message),
                onGranted = {
                    changeOperation(UpdateLoaderOperation.Tip(operation.diffs, operation.info))
                },
                onIgnore = {
                    changeOperation(UpdateLoaderOperation.Tip(operation.diffs, operation.info))
                },
                onDismiss = {
                    changeOperation(UpdateLoaderOperation.None)
                }
            )
        }
        is UpdateLoaderOperation.Tip -> {
            val dismiss = {
                changeOperation(UpdateLoaderOperation.None)
            }
            AlertDialog(
                onDismissRequest = dismiss,
                title = {
                    Text(text = stringResource(R.string.generic_tip))
                },
                text = {
                    val scrollState = rememberScrollState()
                    Column(
                        modifier = Modifier
                            .fadeEdge(state = scrollState)
                            .verticalScroll(state = scrollState),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(text = stringResource(R.string.versions_update_loader_diff_message))

                        //格式化差异文本
                        operation.diffs.list.forEach { diff ->
                            val modloader = diff.getLoader().displayName
                            val string = when (diff) {
                                is AddonDiffs.VersionChangeDiff -> {
                                    stringResource(R.string.versions_update_loader_diff_change, modloader, diff.original, diff.updateTo)
                                }
                                is AddonDiffs.RemoveDiff -> {
                                    stringResource(R.string.versions_update_loader_diff_remove, modloader)
                                }
                                is AddonDiffs.NewLoadDiff -> {
                                    stringResource(R.string.versions_update_loader_diff_load, modloader, diff.version)
                                }
                            }
                            Text(text = string)
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { onInstall(operation.info) }
                    ) {
                        MarqueeText(text = stringResource(R.string.generic_confirm))
                    }
                },
                dismissButton = {
                    Button(onClick = dismiss) {
                        MarqueeText(text = stringResource(R.string.generic_cancel))
                    }
                }
            )
        }
        is UpdateLoaderOperation.Install -> {
            if (installer != null) {
                val updateLoader by installer.tasksFlow.collectAsStateWithLifecycle()
                if (updateLoader.isNotEmpty()) {
                    //安装/变更加载器流程对话框
                    TitleTaskFlowDialog(
                        title = stringResource(R.string.versions_update_loader),
                        tasks = updateLoader,
                        onCancel = {
                            onCancel()
                            changeOperation(UpdateLoaderOperation.None)
                        }
                    )
                }
            }
        }
        is UpdateLoaderOperation.Error -> {
            val th = operation.th
            Logger.error(TAG, "Failed to download the game!", th)
            val message = when (th) {
                is HttpRequestTimeoutException, is SocketTimeoutException, is TimeoutException -> stringResource(R.string.error_timeout)
                is UnknownHostException, is UnresolvedAddressException -> stringResource(R.string.error_network_unreachable)
                is ConnectException -> stringResource(R.string.error_connection_failed)
                is SerializationException, is JsonSyntaxException -> stringResource(R.string.error_parse_failed)
                is CantFetchingOptiFineUrlException -> stringResource(R.string.download_install_error_cant_fetch_optifine_download_url)
                is JvmCrashException -> stringResource(R.string.download_install_error_jvm_crash, th.code)
                is DownloadFailedException -> stringResource(R.string.download_install_error_download_failed)
                else -> {
                    val errorMessage = th.localizedMessage ?: th.message ?: th::class.qualifiedName ?: "Unknown error"
                    stringResource(R.string.empty_holder, errorMessage)
                }
            }
            val dismiss = {
                changeOperation(UpdateLoaderOperation.None)
            }
            AlertDialog(
                onDismissRequest = dismiss,
                title = {
                    Text(text = stringResource(R.string.download_install_error_title))
                },
                text = {
                    val scrollState = rememberScrollState()
                    Column(
                        modifier = Modifier
                            .fadeEdge(state = scrollState)
                            .verticalScroll(state = scrollState),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(text = stringResource(R.string.versions_update_loader_error_message))
                        Text(text = message)
                    }
                },
                confirmButton = {
                    Button(onClick = dismiss) {
                        MarqueeText(text = stringResource(R.string.generic_confirm))
                    }
                }
            )
        }
    }
}