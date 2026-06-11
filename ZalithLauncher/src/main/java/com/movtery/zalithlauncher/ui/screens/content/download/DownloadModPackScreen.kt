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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
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
import com.movtery.zalithlauncher.game.download.assets.platform.PlatformClasses
import com.movtery.zalithlauncher.game.download.assets.platform.PlatformVersion
import com.movtery.zalithlauncher.game.download.jvm_server.JvmCrashException
import com.movtery.zalithlauncher.game.download.modpack.install.ModPackInfo
import com.movtery.zalithlauncher.game.download.modpack.install.ModPackInstaller
import com.movtery.zalithlauncher.game.version.download.DownloadFailedException
import com.movtery.zalithlauncher.game.version.installed.VersionsManager
import com.movtery.zalithlauncher.notification.NotificationManager
import com.movtery.zalithlauncher.ui.components.MarqueeText
import com.movtery.zalithlauncher.ui.components.NotificationCheck
import com.movtery.zalithlauncher.ui.components.SimpleAlertDialog
import com.movtery.zalithlauncher.ui.components.SimpleEditDialog
import com.movtery.zalithlauncher.ui.components.fadeEdge
import com.movtery.zalithlauncher.ui.screens.NestedNavKey
import com.movtery.zalithlauncher.ui.screens.NormalNavKey
import com.movtery.zalithlauncher.ui.screens.TitledNavKey
import com.movtery.zalithlauncher.ui.screens.content.download.assets.download.DownloadAssetsScreen
import com.movtery.zalithlauncher.ui.screens.content.download.assets.search.SearchModPackScreen
import com.movtery.zalithlauncher.ui.screens.content.elements.TitleTaskFlowDialog
import com.movtery.zalithlauncher.ui.screens.content.elements.isFilenameInvalid
import com.movtery.zalithlauncher.ui.screens.navigateTo
import com.movtery.zalithlauncher.ui.screens.onBack
import com.movtery.zalithlauncher.ui.screens.rememberTransitionSpec
import com.movtery.zalithlauncher.utils.logging.Logger
import com.movtery.zalithlauncher.viewmodel.ConfirmMobileDataOperation
import com.movtery.zalithlauncher.viewmodel.EventViewModel
import com.movtery.zalithlauncher.viewmodel.ModpackConfirmUseMobileDataOperation
import com.movtery.zalithlauncher.viewmodel.ModpackImportViewModel
import com.movtery.zalithlauncher.viewmodel.sendKeepScreen
import io.ktor.client.plugins.HttpRequestTimeoutException
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.serialization.SerializationException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.nio.channels.UnresolvedAddressException
import java.util.concurrent.TimeoutException
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume

/** 整合包安装状态操作 */
private sealed interface ModPackInstallOperation {
    data object None : ModPackInstallOperation
    /** 警告整合包的兼容性，同意后将进行安装 */
    data class Warning(val version: PlatformVersion, val iconUrl: String?) : ModPackInstallOperation
    /** 开始安装 */
    data object Install : ModPackInstallOperation
    /** 警告通知权限，可以无视，并直接开始安装 */
    data class WarningForNotification(val version: PlatformVersion, val iconUrl: String?) : ModPackInstallOperation
    /** 整合包安装出现异常 */
    data class Error(val th: Throwable) : ModPackInstallOperation
    /** 整合包已成功安装 */
    data object Success : ModPackInstallOperation
}

/** 整合包版本名称自定义状态操作 */
private sealed interface VersionNameOperation {
    data object None : VersionNameOperation
    /** 等待用户输入版本名称 */
    data class Waiting(val info: ModPackInfo) : VersionNameOperation
}

private class ModPackViewModel: ViewModel() {
    var installOperation by mutableStateOf<ModPackInstallOperation>(ModPackInstallOperation.None)
    var versionNameOperation by mutableStateOf<VersionNameOperation>(VersionNameOperation.None)
    var confirmMobileDataOperation by mutableStateOf<ConfirmMobileDataOperation>(ConfirmMobileDataOperation.None)

    //等待用户输入版本名称相关
    private var versionNameContinuation: (Continuation<String>)? = null
    suspend fun waitForVersionName(modPackInfo: ModPackInfo): String {
        return suspendCancellableCoroutine { cont ->
            versionNameContinuation = cont
            versionNameOperation = VersionNameOperation.Waiting(modPackInfo)
        }
    }

    /**
     * 用户确认输入版本名称
     */
    fun confirmVersionName(name: String) {
        //恢复continuation
        versionNameContinuation?.resume(name)
        versionNameContinuation = null
        versionNameOperation = VersionNameOperation.None
    }


    //警告使用移动网络相关
    private var confirmMobileData : (Continuation<Boolean>)? = null
    suspend fun waitForConfirmMobileData(): Boolean {
        return suspendCancellableCoroutine { cont ->
            confirmMobileData = cont
            confirmMobileDataOperation = ConfirmMobileDataOperation.Waiting
        }
    }

    /**
     * 用户是否确认使用移动网络
     */
    fun confirmUseMobileData(use: Boolean) {
        //恢复continuation
        confirmMobileData?.resume(use)
        confirmMobileData = null
        confirmMobileDataOperation = ConfirmMobileDataOperation.None
    }

    /**
     * 整合包安装器
     */
    var installer by mutableStateOf<ModPackInstaller?>(null)

    fun install(
        context: Context,
        version: PlatformVersion,
        iconUrl: String?,
        onStart: () -> Unit = {},
        onStop: () -> Unit = {},
    ) {
        installOperation = ModPackInstallOperation.Install
        installer = ModPackInstaller(
            context = context,
            version = version,
            iconUrl = iconUrl,
            scope = viewModelScope,
            waitForVersionName = ::waitForVersionName,
            waitForConfirmMobileData = ::waitForConfirmMobileData
        ).also {
            it.installModPack(
                onInstalled = { version ->
                    installer = null
                    VersionsManager.refresh("[Modpack] ModPackInstaller.onInstalled", version)
                    installOperation = ModPackInstallOperation.Success
                    onStop()
                },
                onCancelled = {
                    installer = null
                    installOperation = ModPackInstallOperation.None
                    onStop()
                },
                onError = { th ->
                    installer = null
                    installOperation = ModPackInstallOperation.Error(th)
                    onStop()
                }
            )
        }
        onStart()
    }

    fun cancel() {
        installer?.cancelInstall()
        installer = null
        installOperation = ModPackInstallOperation.None
        versionNameOperation = VersionNameOperation.None
        confirmMobileDataOperation = ConfirmMobileDataOperation.None
    }

    override fun onCleared() {
        cancel()
    }
}

@Composable
private fun rememberModPackViewModel(
    key: NestedNavKey.DownloadModPack
): ModPackViewModel {
    return viewModel(
        key = key.toString()
    ) {
        ModPackViewModel()
    }
}

@Composable
fun DownloadModPackScreen(
    key: NestedNavKey.DownloadModPack,
    mainScreenKey: TitledNavKey?,
    downloadScreenKey: TitledNavKey?,
    downloadModPackScreenKey: TitledNavKey?,
    onCurrentKeyChange: (TitledNavKey?) -> Unit,
    importerViewModel: ModpackImportViewModel,
    eventViewModel: EventViewModel
) {
    val viewModel: ModPackViewModel = rememberModPackViewModel(key)

    val context = LocalContext.current
    val backStack = key.backStack
    val stackTopKey = backStack.lastOrNull()
    LaunchedEffect(stackTopKey) {
        onCurrentKeyChange(stackTopKey)
    }

    ModPackInstallOperation(
        operation = viewModel.installOperation,
        updateOperation = { viewModel.installOperation = it },
        installer = viewModel.installer,
        onInstall = { version, iconUrl ->
            viewModel.install(
                context = context,
                version = version,
                iconUrl = iconUrl,
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

    //用户确认版本名称 操作流程
    VersionNameOperation(
        operation = viewModel.versionNameOperation,
        onConfirmVersionName = { name ->
            viewModel.confirmVersionName(name)
        },
        onCancel = {
            viewModel.cancel()
        }
    )

    //用户确认使用移动网络 操作流程
    ModpackConfirmUseMobileDataOperation(
        operation = viewModel.confirmMobileDataOperation,
        onConfirmUse = { use ->
            viewModel.confirmUseMobileData(use)
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
                entry<NormalNavKey.SearchModPack> {
                    SearchModPackScreen(
                        mainScreenKey = mainScreenKey,
                        downloadScreenKey = downloadScreenKey,
                        downloadModPackScreenKey = key,
                        downloadModPackScreenCurrentKey = downloadModPackScreenKey,
                        viewModel = importerViewModel,
                        eventViewModel = eventViewModel
                    ) { platform, projectId, iconUrl ->
                        backStack.navigateTo(
                            NormalNavKey.DownloadAssets(platform, projectId, PlatformClasses.MOD_PACK, iconUrl)
                        )
                    }
                }
                entry<NormalNavKey.DownloadAssets> { assetsKey ->
                    DownloadAssetsScreen(
                        mainScreenKey = mainScreenKey,
                        parentScreenKey = key,
                        parentCurrentKey = downloadScreenKey,
                        currentKey = downloadModPackScreenKey,
                        key = assetsKey,
                        eventViewModel = eventViewModel,
                        onItemClicked = { _, version, iconUrl, _ ->
                            if (viewModel.installOperation !is ModPackInstallOperation.None) {
                                //不是待安装状态，拒绝此次安装
                                return@DownloadAssetsScreen
                            }
                            viewModel.installOperation = if (!NotificationManager.checkNotificationEnabled(context)) {
                                //警告通知权限
                                ModPackInstallOperation.WarningForNotification(version, iconUrl)
                            } else {
                                ModPackInstallOperation.Warning(version, iconUrl)
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

@Composable
private fun ModPackInstallOperation(
    operation: ModPackInstallOperation,
    updateOperation: (ModPackInstallOperation) -> Unit,
    installer: ModPackInstaller?,
    onInstall: (PlatformVersion, iconUrl: String?) -> Unit,
    onCancel: () -> Unit
) {
    when (operation) {
        is ModPackInstallOperation.None -> {}
        is ModPackInstallOperation.WarningForNotification -> {
            NotificationCheck(
                text = stringResource(R.string.notification_data_jvm_service_message),
                onGranted = {
                    //权限被授予，开始安装
                    updateOperation(ModPackInstallOperation.Warning(operation.version, operation.iconUrl))
                },
                onIgnore = {
                    //用户不想授权，但是支持继续进行安装
                    updateOperation(ModPackInstallOperation.Warning(operation.version, operation.iconUrl))
                },
                onDismiss = {
                    updateOperation(ModPackInstallOperation.None)
                }
            )
        }
        is ModPackInstallOperation.Warning -> {
            //警告整合包的兼容性（免责声明）
            SimpleAlertDialog(
                title = stringResource(R.string.generic_warning),
                text = {
                    Text(text = stringResource(R.string.download_modpack_warning1))
                    Text(text = stringResource(R.string.download_modpack_warning2))

                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.download_modpack_warning3),
                        fontWeight = FontWeight.Bold
                    )
                },
                confirmText = stringResource(R.string.download_install),
                onCancel = {
                    updateOperation(ModPackInstallOperation.None)
                },
                onConfirm = {
                    onInstall(operation.version, operation.iconUrl)
                }
            )
        }
        is ModPackInstallOperation.Install -> {
            if (installer != null) {
                val tasks = installer.tasksFlow.collectAsStateWithLifecycle()
                if (tasks.value.isNotEmpty()) {
                    //安装整合包流程对话框
                    TitleTaskFlowDialog(
                        title = stringResource(R.string.download_modpack_install_title),
                        tasks = tasks.value,
                        onCancel = {
                            onCancel()
                            updateOperation(ModPackInstallOperation.None)
                        }
                    )
                }
            }
        }
        is ModPackInstallOperation.Error -> {
            val th = operation.th
            Logger.error("InstallModpack", "Failed to download the game!", th)
            val message = when (th) {
                is HttpRequestTimeoutException, is SocketTimeoutException, is TimeoutException -> stringResource(R.string.error_timeout)
                is UnknownHostException, is UnresolvedAddressException -> stringResource(R.string.error_network_unreachable)
                is ConnectException -> stringResource(R.string.error_connection_failed)
                is SerializationException, is JsonSyntaxException -> stringResource(R.string.error_parse_failed)
                is JvmCrashException -> stringResource(R.string.download_install_error_jvm_crash, th.code)
                is DownloadFailedException -> stringResource(R.string.download_install_error_download_failed)
                else -> {
                    val errorMessage = th.localizedMessage ?: th.message ?: th::class.qualifiedName ?: "Unknown error"
                    stringResource(R.string.empty_holder, errorMessage)
                }
            }
            val dismiss = {
                updateOperation(ModPackInstallOperation.None)
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
        is ModPackInstallOperation.Success -> {
            SimpleAlertDialog(
                title = stringResource(R.string.download_install_success_title),
                text = stringResource(R.string.download_install_success_message)
            ) {
                updateOperation(ModPackInstallOperation.None)
            }
        }
    }
}

@Composable
private fun VersionNameOperation(
    operation: VersionNameOperation,
    onConfirmVersionName: (String) -> Unit,
    onCancel: () -> Unit
) {
    when (operation) {
        is VersionNameOperation.None -> {}
        is VersionNameOperation.Waiting -> {
            val modpackInfo = operation.info
            ModpackVersionNameDialog(
                name = modpackInfo.name,
                onConfirmVersionName = onConfirmVersionName,
                onCancel = onCancel
            )
        }
    }
}

/**
 * 将要安装的整合包版本名称
 * @param name 预填写的整合包版本名称
 * @param onConfirmVersionName 用户输入并确认了版本名称
 * @param onCancel 用户取消了导入
 */
@Composable
fun ModpackVersionNameDialog(
    name: String,
    onConfirmVersionName: (String) -> Unit,
    onCancel: () -> Unit,
) {
    var name by remember { mutableStateOf(name) }

    val filenameInvalidMessage = key(name) {
        isFilenameInvalid(name)
    }

    val isVersionExists = remember(name) {
        VersionsManager.isVersionExists(name, true)
    }
    val isError = name.isEmpty() || filenameInvalidMessage != null || isVersionExists

    SimpleEditDialog(
        title = stringResource(R.string.download_install_input_version_name_title),
        value = name,
        onValueChange = { name = it },
        isError = isError,
        supportingText = {
            when {
                name.isEmpty() -> Text(stringResource(R.string.generic_cannot_empty))
                filenameInvalidMessage != null -> Text(filenameInvalidMessage)
                isVersionExists -> Text(stringResource(R.string.versions_manage_install_exists))
            }
        },
        singleLine = true,
        onDismissRequest = {},
        onCancel = onCancel,
        onConfirm = {
            if (!isError) {
                onConfirmVersionName(name)
            }
        }
    )
}