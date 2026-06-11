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

package com.movtery.zalithlauncher.viewmodel

import android.content.Context
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.google.gson.JsonSyntaxException
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.game.download.jvm_server.JvmCrashException
import com.movtery.zalithlauncher.game.download.modpack.install.ModpackImporter
import com.movtery.zalithlauncher.game.download.modpack.install.PackNotSupportedException
import com.movtery.zalithlauncher.game.download.modpack.install.UnsupportedPackReason
import com.movtery.zalithlauncher.game.download.modpack.platform.PackPlatform
import com.movtery.zalithlauncher.game.version.download.DownloadFailedException
import com.movtery.zalithlauncher.game.version.installed.VersionsManager
import com.movtery.zalithlauncher.ui.components.MarqueeText
import com.movtery.zalithlauncher.ui.components.SimpleAlertDialog
import com.movtery.zalithlauncher.ui.components.fadeEdge
import com.movtery.zalithlauncher.ui.screens.content.download.ModpackVersionNameDialog
import com.movtery.zalithlauncher.ui.screens.content.download.assets.elements.PackIdentifier
import com.movtery.zalithlauncher.ui.screens.content.elements.TitleTaskFlowDialog
import com.movtery.zalithlauncher.utils.logging.Logger
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

private const val TAG = "ModpackImportVM"

/** 导入整合包相关操作 */
sealed interface ModpackImportOperation {
    data object None : ModpackImportOperation
    /** 开始导入整合包 */
    data object Import : ModpackImportOperation
    /** 不支持的整合包或格式无效 */
    data class NotSupport(val reason: UnsupportedPackReason) : ModpackImportOperation
    /** 整合包导入完成 */
    data object Finished : ModpackImportOperation
    /** 导入整合包时出现异常 */
    data class Error(val th: Throwable) : ModpackImportOperation
}

/** 整合包版本名称自定义状态操作 */
sealed interface VersionNameOperation {
    data object None : VersionNameOperation
    /** 等待用户输入版本名称 */
    data class Waiting(val name: String) : VersionNameOperation
}

/** 整合包安装确认使用移动网络状态操作 */
sealed interface ConfirmMobileDataOperation {
    data object None : ConfirmMobileDataOperation
    /** 等待用户确认使用移动网络 */
    data object Waiting : ConfirmMobileDataOperation
}

/**
 * 导入整合包ViewModel
 */
class ModpackImportViewModel : ViewModel() {
    var importOperation by mutableStateOf<ModpackImportOperation>(ModpackImportOperation.None)
    var versionNameOperation by mutableStateOf<VersionNameOperation>(VersionNameOperation.None)
    var confirmMobileDataOperation by mutableStateOf<ConfirmMobileDataOperation>(ConfirmMobileDataOperation.None)

    //等待用户输入版本名称相关
    private var versionNameContinuation: (Continuation<String>)? = null
    suspend fun waitForVersionName(name: String): String {
        return suspendCancellableCoroutine { cont ->
            versionNameContinuation = cont
            versionNameOperation = VersionNameOperation.Waiting(name)
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
     * 整合包导入器
     */
    var importer by mutableStateOf<ModpackImporter?>(null)

    /**
     * 开始导入整合包
     */
    fun import(
        context: Context,
        uri: Uri,
        onStart: () -> Unit = {},
        onStop: () -> Unit = {}
    ) {
        if (importOperation != ModpackImportOperation.None) {
            //当前有别的导入任务，拒绝这次导入
            return
        }
        importOperation = ModpackImportOperation.Import
        importer = ModpackImporter(
            context = context,
            uri = uri,
            scope = viewModelScope,
            waitForVersionName = ::waitForVersionName,
            waitForConfirmMobileData = ::waitForConfirmMobileData
        ).also {
            it.startImport(
                onFinished = { version ->
                    importer = null
                    VersionsManager.refresh("[Modpack] ModpackImporter.onFinished", version)
                    importOperation = ModpackImportOperation.Finished
                    onStop()
                },
                onCancelled = {
                    importer = null
                    importOperation = ModpackImportOperation.None
                    onStop()
                },
                onError = { th ->
                    importer = null
                    importOperation = if (th is PackNotSupportedException) {
                        //整合包不受支持，无法导入
                        ModpackImportOperation.NotSupport(th.reason)
                    } else {
                        ModpackImportOperation.Error(th)
                    }
                    onStop()
                }
            )
        }
        onStart()
    }

    fun cancel() {
        importer?.cancel()
        importer = null
        importOperation = ModpackImportOperation.None
        versionNameOperation = VersionNameOperation.None
        confirmMobileDataOperation = ConfirmMobileDataOperation.None
    }

    override fun onCleared() {
        cancel()
    }
}

@Composable
fun ModpackImportOperation(
    operation: ModpackImportOperation,
    changeOperation: (ModpackImportOperation) -> Unit,
    importer: ModpackImporter?,
    onCancel: () -> Unit
) {
    when (operation) {
        is ModpackImportOperation.None -> {}
        is ModpackImportOperation.Import -> {
            if (importer != null) {
                val tasks by importer.taskFlow.collectAsStateWithLifecycle()
                if (tasks.isNotEmpty()) {
                    TitleTaskFlowDialog(
                        title = stringResource(R.string.import_modpack),
                        tasks = tasks,
                        onCancel = {
                            onCancel()
                            changeOperation(ModpackImportOperation.None)
                        }
                    )
                }
            }
        }
        is ModpackImportOperation.NotSupport -> {
            AlertDialog(
                onDismissRequest = {},
                title = {
                    Text(text = stringResource(R.string.import_modpack_not_supported_title))
                },
                text = {
                    val scrollState = rememberScrollState()
                    Column(
                        modifier = Modifier
                            .fadeEdge(state = scrollState)
                            .verticalScroll(state = scrollState),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        when (operation.reason) {
                            UnsupportedPackReason.CorruptedArchive -> {
                                //因文件无法解压导致的无法导入
                                Text(text = stringResource(R.string.import_modpack_not_supported_text1))

                                Text(text = stringResource(R.string.import_modpack_not_supported_text2))
                                Text(text = stringResource(R.string.import_modpack_not_supported_text3))

                                Text(text = stringResource(R.string.import_modpack_not_supported_text4))
                            }
                            UnsupportedPackReason.UnsupportedFormat -> {
                                //启动器确实不支持这个格式
                                Text(text = stringResource(R.string.import_modpack_not_supported_formats))
                                AllSupportPackDisplay(modifier = Modifier.fillMaxWidth())
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            changeOperation(ModpackImportOperation.None)
                        }
                    ) {
                        MarqueeText(text = stringResource(R.string.generic_confirm))
                    }
                }
            )
        }
        is ModpackImportOperation.Finished -> {
            SimpleAlertDialog(
                title = stringResource(R.string.import_modpack_finished_title),
                text = stringResource(R.string.import_modpack_finished_text)
            ) {
                changeOperation(ModpackImportOperation.None)
            }
        }
        is ModpackImportOperation.Error -> {
            val th = operation.th
            Logger.error(TAG, "Failed to download the game!", th)
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
                changeOperation(ModpackImportOperation.None)
            }
            AlertDialog(
                onDismissRequest = dismiss,
                title = {
                    Text(text = stringResource(R.string.import_modpack_failed_title))
                },
                text = {
                    val scrollState = rememberScrollState()
                    Column(
                        modifier = Modifier
                            .fadeEdge(state = scrollState)
                            .verticalScroll(state = scrollState),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(text = stringResource(R.string.import_modpack_failed_text))
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

/**
 * 所有支持的整合包格式展示
 */
@Composable
fun AllSupportPackDisplay(
    modifier: Modifier = Modifier
) {
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        PackPlatform.entries.forEach { platform ->
            PackIdentifier(
                platform = platform
            )
        }
    }
}

@Composable
fun ModpackVersionNameOperation(
    operation: VersionNameOperation,
    onConfirmVersionName: (String) -> Unit,
    onCancel: () -> Unit
) {
    when (operation) {
        is VersionNameOperation.None -> {}
        is VersionNameOperation.Waiting -> {
            ModpackVersionNameDialog(
                name = operation.name,
                onConfirmVersionName = onConfirmVersionName,
                onCancel = onCancel
            )
        }
    }
}

@Composable
fun ModpackConfirmUseMobileDataOperation(
    operation: ConfirmMobileDataOperation,
    onConfirmUse: (Boolean) -> Unit
) {
    when (operation) {
        is ConfirmMobileDataOperation.None -> {}
        is ConfirmMobileDataOperation.Waiting -> {
            SimpleAlertDialog(
                title = stringResource(R.string.generic_warning),
                text = stringResource(R.string.download_install_warning_mobile_data),
                confirmText = stringResource(R.string.generic_anyway),
                onDismiss = {
                    onConfirmUse(false)
                },
                onConfirm = {
                    //用户坚持使用移动网络
                    onConfirmUse(true)
                }
            )
        }
    }
}