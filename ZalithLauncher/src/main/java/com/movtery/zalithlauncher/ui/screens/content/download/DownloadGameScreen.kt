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

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.google.gson.JsonSyntaxException
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.game.download.game.GameDownloadInfo
import com.movtery.zalithlauncher.game.download.game.GameInstaller
import com.movtery.zalithlauncher.game.download.game.optifine.CantFetchingOptiFineUrlException
import com.movtery.zalithlauncher.game.download.jvm_server.JvmCrashException
import com.movtery.zalithlauncher.game.version.download.DownloadFailedException
import com.movtery.zalithlauncher.game.version.installed.VersionsManager
import com.movtery.zalithlauncher.notification.NotificationManager
import com.movtery.zalithlauncher.ui.components.MarqueeText
import com.movtery.zalithlauncher.ui.components.NotificationCheck
import com.movtery.zalithlauncher.ui.components.SimpleAlertDialog
import com.movtery.zalithlauncher.ui.components.fadeEdge
import com.movtery.zalithlauncher.ui.screens.NestedNavKey
import com.movtery.zalithlauncher.ui.screens.NormalNavKey
import com.movtery.zalithlauncher.ui.screens.TitledNavKey
import com.movtery.zalithlauncher.ui.screens.content.download.game.DownloadGameWithAddonScreen
import com.movtery.zalithlauncher.ui.screens.content.download.game.SelectGameVersionScreen
import com.movtery.zalithlauncher.ui.screens.content.elements.TitleTaskFlowDialog
import com.movtery.zalithlauncher.ui.screens.navigateTo
import com.movtery.zalithlauncher.ui.screens.onBack
import com.movtery.zalithlauncher.ui.screens.rememberTransitionSpec
import com.movtery.zalithlauncher.utils.logging.Logger
import com.movtery.zalithlauncher.utils.network.isUsingMobileData
import com.movtery.zalithlauncher.viewmodel.EventViewModel
import com.movtery.zalithlauncher.viewmodel.sendKeepScreen
import io.ktor.client.plugins.HttpRequestTimeoutException
import kotlinx.serialization.SerializationException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.nio.channels.UnresolvedAddressException
import java.util.concurrent.TimeoutException

/** 游戏安装状态操作 */
private sealed interface GameInstallOperation {
    data object None : GameInstallOperation
    /** 开始安装 */
    data object Install : GameInstallOperation
    /** 警告通知权限，可以无视，并直接开始安装 */
    data class WarningForNotification(val info: GameDownloadInfo) : GameInstallOperation
    /** 警告正在使用流量 */
    data class WarningForMobileData(val info: GameDownloadInfo) : GameInstallOperation
    /** 游戏安装出现异常 */
    data class Error(val th: Throwable) : GameInstallOperation
    /** 游戏已成功安装 */
    data object Success : GameInstallOperation
}

private class GameDownloadViewModel(): ViewModel() {
    /**
     * 用于刷新游戏下载页面版本名称的检查
     */
    var versionNameErrorCheck by mutableStateOf(false)
    var installOperation by mutableStateOf<GameInstallOperation>(GameInstallOperation.None)

    /**
     * 游戏安装器
     */
    var installer by mutableStateOf<GameInstaller?>(null)

    /**
     * 刷新游戏下载页面内的版本名称检查
     */
    private fun refreshVersionNameCheck() {
        versionNameErrorCheck = !versionNameErrorCheck
    }

    fun install(
        context: Context,
        info: GameDownloadInfo,
        onStart: () -> Unit = {},
        onStop: () -> Unit = {},
    ) {
        installOperation = GameInstallOperation.Install
        installer = GameInstaller(context, info, viewModelScope).also {
            it.installGame(
                onInstalled = { version ->
                    installer = null
                    VersionsManager.refresh("[DownloadGame] GameInstaller.onInstalled", version)
                    installOperation = GameInstallOperation.Success
                    refreshVersionNameCheck()
                    onStop()
                },
                onError = { th ->
                    installer = null
                    installOperation = GameInstallOperation.Error(th)
                    refreshVersionNameCheck()
                    onStop()
                },
                onGameAlreadyInstalled = {
                    //很有可能发生在刚安装完成，再次点击安装按钮时
                    //重置状态，避免无法发起新的安装的问题
                    installOperation = GameInstallOperation.None
                    //保险起见，再次刷新版本名称错误检查
                    refreshVersionNameCheck()
                    onStop()
                }
            )
        }
        onStart()
    }

    fun cancel() {
        installer?.cancelInstall()
        installer = null
        installOperation = GameInstallOperation.None
        refreshVersionNameCheck()
    }

    override fun onCleared() {
        cancel()
    }
}

@Composable
private fun rememberGameDownloadViewModel(
    key: NestedNavKey.DownloadGame
): GameDownloadViewModel {
    return viewModel(
        key = key.toString()
    ) {
        GameDownloadViewModel()
    }
}

@Composable
fun DownloadGameScreen(
    key: NestedNavKey.DownloadGame,
    mainScreenKey: TitledNavKey?,
    downloadScreenKey: TitledNavKey?,
    downloadGameScreenKey: TitledNavKey?,
    onCurrentKeyChange: (TitledNavKey?) -> Unit,
    eventViewModel: EventViewModel
) {
    val viewModel: GameDownloadViewModel = rememberGameDownloadViewModel(key)

    val context = LocalContext.current
    val backStack = key.backStack
    val stackTopKey = backStack.lastOrNull()
    LaunchedEffect(stackTopKey) {
        onCurrentKeyChange(stackTopKey)
    }

    GameInstallOperation(
        gameInstallOperation = viewModel.installOperation,
        updateOperation = { viewModel.installOperation = it },
        installer = viewModel.installer,
        onInstall = { info ->
            viewModel.install(
                context = context,
                info = info,
                onStart = {
                    eventViewModel.sendKeepScreen(true)
                },
                onStop = {
                    eventViewModel.sendKeepScreen(false)
                }
            )
        },
        onCancel = {
            viewModel.cancel()
            eventViewModel.sendKeepScreen(false)
        }
    )

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
                entry<NormalNavKey.DownloadGame.SelectGameVersion> {
                    SelectGameVersionScreen(
                        mainScreenKey = mainScreenKey,
                        downloadScreenKey = downloadScreenKey,
                        downloadGameScreenKey = downloadGameScreenKey,
                        eventViewModel = eventViewModel,
                    ) { versionString ->
                        backStack.navigateTo(
                            NormalNavKey.DownloadGame.Addons(versionString)
                        )
                    }
                }
                entry<NormalNavKey.DownloadGame.Addons> { key ->
                    val context = LocalContext.current
                    DownloadGameWithAddonScreen(
                        mainScreenKey = mainScreenKey,
                        downloadScreenKey = downloadScreenKey,
                        downloadGameScreenKey = downloadGameScreenKey,
                        key = key,
                        refreshErrorCheck = viewModel.versionNameErrorCheck
                    ) { info ->
                        if (viewModel.installOperation !is GameInstallOperation.None) {
                            //不是待安装状态，拒绝此次安装
                            return@DownloadGameWithAddonScreen
                        }
                        if (!NotificationManager.checkNotificationEnabled(context)) {
                            //警告通知权限
                            viewModel.installOperation = GameInstallOperation.WarningForNotification(info)
                        } else {
                            if (isUsingMobileData(context)) {
                                //警告正在使用流量
                                viewModel.installOperation = GameInstallOperation.WarningForMobileData(info)
                            } else {
                                viewModel.install(
                                    context = context,
                                    info = info,
                                    onStart = {
                                        eventViewModel.sendKeepScreen(true)
                                    },
                                    onStop = {
                                        eventViewModel.sendKeepScreen(false)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        )
    } else {
        Box(Modifier.fillMaxSize())
    }
}

@Composable
private fun GameInstallOperation(
    gameInstallOperation: GameInstallOperation,
    updateOperation: (GameInstallOperation) -> Unit = {},
    installer: GameInstaller?,
    onInstall: (GameDownloadInfo) -> Unit,
    onCancel: () -> Unit
) {
    when (gameInstallOperation) {
        is GameInstallOperation.None -> {}
        is GameInstallOperation.WarningForNotification -> {
            NotificationCheck(
                text = stringResource(R.string.notification_data_jvm_service_message),
                onGranted = {
                    //权限被授予，开始安装
                    onInstall(gameInstallOperation.info)
                },
                onIgnore = {
                    //用户不想授权，但是支持继续进行安装
                    onInstall(gameInstallOperation.info)
                },
                onDismiss = {
                    updateOperation(GameInstallOperation.None)
                }
            )
        }
        is GameInstallOperation.WarningForMobileData -> {
            SimpleAlertDialog(
                title = stringResource(R.string.generic_warning),
                text = stringResource(R.string.download_install_warning_mobile_data),
                confirmText = stringResource(R.string.generic_anyway),
                onDismiss = {
                    updateOperation(GameInstallOperation.None)
                },
                onConfirm = {
                    //用户坚持使用移动网络
                    onInstall(gameInstallOperation.info)
                }
            )
        }
        is GameInstallOperation.Install -> {
            if (installer != null) {
                val installGame = installer.tasksFlow.collectAsStateWithLifecycle()
                if (installGame.value.isNotEmpty()) {
                    //安装游戏流程对话框
                    TitleTaskFlowDialog(
                        title = stringResource(R.string.download_game_install_title),
                        tasks = installGame.value,
                        onCancel = {
                            onCancel()
                            updateOperation(GameInstallOperation.None)
                        }
                    )
                }
            }
        }
        is GameInstallOperation.Error -> {
            val th = gameInstallOperation.th
            Logger.error("InstallGame", "Failed to download the game!", th)
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
                updateOperation(GameInstallOperation.None)
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
                        Text(text = stringResource(R.string.download_install_error_message))
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
        is GameInstallOperation.Success -> {
            SimpleAlertDialog(
                title = stringResource(R.string.download_install_success_title),
                text = stringResource(R.string.download_install_success_message)
            ) {
                updateOperation(GameInstallOperation.None)
            }
        }
    }
}