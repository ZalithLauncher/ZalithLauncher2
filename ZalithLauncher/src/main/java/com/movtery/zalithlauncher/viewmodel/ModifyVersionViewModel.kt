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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.google.gson.JsonSyntaxException
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.game.addons.modloader.ModLoader
import com.movtery.zalithlauncher.game.download.game.GameDownloadInfo
import com.movtery.zalithlauncher.game.download.game.GameInstaller
import com.movtery.zalithlauncher.game.download.game.optifine.CantFetchingOptiFineUrlException
import com.movtery.zalithlauncher.game.download.game.optifine.OptiFineForge17IncompatibleException
import com.movtery.zalithlauncher.game.download.jvm_server.JvmCrashException
import com.movtery.zalithlauncher.game.download.jvm_server.isProcessStartRefused
import com.movtery.zalithlauncher.game.version.download.DownloadFailedException
import com.movtery.zalithlauncher.game.version.installed.Version
import com.movtery.zalithlauncher.game.version.installed.VersionsManager
import com.movtery.zalithlauncher.game.version.installed.VersionsManager.isVersionExists
import com.movtery.zalithlauncher.ui.components.MarqueeText
import com.movtery.zalithlauncher.ui.components.NotificationCheck
import com.movtery.zalithlauncher.ui.components.OwnOutlinedTextField
import com.movtery.zalithlauncher.ui.components.SimpleAlertDialog
import com.movtery.zalithlauncher.ui.components.TextTransition
import com.movtery.zalithlauncher.ui.components.fadeEdge
import com.movtery.zalithlauncher.ui.components.rememberDialogMaxHeight
import com.movtery.zalithlauncher.ui.components.verticalScrollWithBar
import com.movtery.zalithlauncher.ui.screens.content.elements.TitleTaskFlowDialog
import com.movtery.zalithlauncher.ui.screens.content.elements.isFilenameInvalid
import com.movtery.zalithlauncher.ui.screens.content.home.version.VersionCardManager
import com.movtery.zalithlauncher.ui.theme.cardColor
import com.movtery.zalithlauncher.ui.theme.onCardColor
import com.movtery.zalithlauncher.utils.logging.Logger
import io.ktor.client.plugins.HttpRequestTimeoutException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.SerializationException
import java.io.File
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.nio.channels.UnresolvedAddressException
import java.util.concurrent.TimeoutException

private const val TAG = "ModifyVersionViewModel"

/** 版本修改状态操作 */
sealed interface ModifyOperation {
    data object None : ModifyOperation
    /** 警告通知权限，可以无视，并进入确认对话框 */
    data class WarningForNotification(val payload: ModifyPayload) : ModifyOperation
    /** 确认对话框：确认版本名称与变更内容 */
    data class Confirm(val payload: ModifyPayload) : ModifyOperation
    /** 开始修改 */
    data object Install : ModifyOperation
    /** 修改过程中出现异常 */
    data class Error(val th: Throwable) : ModifyOperation
    /** 修改成功 */
    data object Success : ModifyOperation
}

/**
 * 版本修改载荷
 * @param info 修改后的游戏安装信息
 * @param currentVersion 版本对应的 [Version] 对象
 * @param newVersionName 修改完成后的新版本名称，与当前名称一致时不重命名
 * @param diffs 变更内容
 */
data class ModifyPayload(
    val info: GameDownloadInfo,
    val currentVersion: Version,
    val newVersionName: String = currentVersion.getVersionName(),
    val diffs: ModifyDiffs
) {
    val currentVersionName: String
        get() = currentVersion.getVersionName()
}

/**
 * 版本的变更内容
 */
data class ModifyDiffs(
    val list: List<Diff>
) {
    /** Minecraft 版本 */
    data class McChange(
        val original: String,
        val updateTo: String
    ) : Diff

    /** 模组加载器版本 */
    data class LoaderChange(
        val modloader: ModLoader,
        val original: String,
        val updateTo: String
    ) : Diff

    /** 移除模组加载器 */
    data class LoaderRemove(
        val modloader: ModLoader
    ) : Diff

    /** 安装模组加载器 */
    data class LoaderInstall(
        val modloader: ModLoader,
        val version: String
    ) : Diff

    sealed interface Diff
}

/**
 * 版本修改的安装操作状态机
 */
class ModifyVersionViewModel : ViewModel() {
    var installOperation by mutableStateOf<ModifyOperation>(ModifyOperation.None)

    /**
     * 游戏安装器
     */
    var installer by mutableStateOf<GameInstaller?>(null)

    fun modify(context: Context, payload: ModifyPayload) {
        installOperation = ModifyOperation.Install
        installer = GameInstaller(
            context = context,
            info = payload.info,
            scope = viewModelScope,
            targetGameFolder = File(payload.currentVersion.getGameHome())
        ).also {
            it.modifyVersion(
                onModified = {
                    installer = null
                    viewModelScope.launch(Dispatchers.Main) {
                        val version = payload.currentVersion
                        val gameHome = version.getGameHome()
                        val isNameChanged = payload.newVersionName != payload.currentVersionName
                        if (isNameChanged) {
                            VersionsManager.renameVersion(version, payload.newVersionName, false)
                        }

                        if (isNameChanged) {
                            lateinit var refreshListener: suspend () -> Unit
                            refreshListener = {
                                // 同步主界面版本卡片的记录，避免卡片因改名失效
                                VersionCardManager.onVersionRenamed(
                                    gameHome = gameHome,
                                    oldName = payload.currentVersionName,
                                    newName = payload.newVersionName
                                )
                                VersionsManager.unregisterListener(refreshListener)
                            }
                            VersionsManager.registerListener(refreshListener)
                        }

                        VersionsManager.refresh("[ModifyVersion] GameInstaller.onModified")

                        installOperation = ModifyOperation.Success
                    }
                },
                onError = { th ->
                    installer = null
                    installOperation = ModifyOperation.Error(th)
                }
            )
        }
    }

    fun cancel() {
        //目标是已存在的版本，取消时不能清除版本目录
        installer?.cancelInstall(clearTarget = false)
        installer = null
        installOperation = ModifyOperation.None
    }

    override fun onCleared() {
        cancel()
    }
}


/**
 * 修订差异条目
 */
@Composable
private fun DiffItem(diff: ModifyDiffs.Diff) {
    when (diff) {
        is ModifyDiffs.McChange -> {
            DiffChangeItem(label = "Minecraft", from = diff.original, to = diff.updateTo)
        }
        is ModifyDiffs.LoaderChange -> DiffChangeItem(
            label = diff.modloader.displayName,
            from = diff.original,
            to = diff.updateTo
        )
        is ModifyDiffs.LoaderRemove -> Text(
            text = stringResource(R.string.versions_modify_diff_remove, diff.modloader.displayName),
            style = MaterialTheme.typography.bodyMedium
        )
        is ModifyDiffs.LoaderInstall -> Text(
            text = stringResource(R.string.versions_modify_diff_install, diff.modloader.displayName, diff.version),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun DiffChangeItem(label: String, from: String, to: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium
        )
        TextTransition(
            from = from,
            to = to,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
fun ModifyVersionOperation(
    operation: ModifyOperation,
    changeOperation: (ModifyOperation) -> Unit,
    installer: GameInstaller?,
    onModify: (ModifyPayload) -> Unit,
    onCancel: () -> Unit
) {
    when (operation) {
        is ModifyOperation.None -> {}
        is ModifyOperation.WarningForNotification -> {
            NotificationCheck(
                text = stringResource(R.string.notification_data_jvm_service_message),
                onGranted = {
                    changeOperation(ModifyOperation.Confirm(operation.payload))
                },
                onIgnore = {
                    changeOperation(ModifyOperation.Confirm(operation.payload))
                },
                onDismiss = {
                    changeOperation(ModifyOperation.None)
                }
            )
        }
        is ModifyOperation.Confirm -> {
            val payload = operation.payload

            var nameValue by remember(payload) { mutableStateOf(payload.currentVersionName) }

            val emptyError = stringResource(R.string.generic_cannot_empty)
            val existsError = stringResource(R.string.versions_manage_install_exists)

            val filenameInvalidMessage = key(nameValue) {
                isFilenameInvalid(nameValue)
            }
            val nameConflict = remember(nameValue, payload) {
                nameValue != payload.currentVersionName && isVersionExists(nameValue, true)
            }
            val nameValid = nameValue.isNotEmpty() && filenameInvalidMessage == null && !nameConflict

            Dialog(
                onDismissRequest = { changeOperation(ModifyOperation.None) },
                properties = DialogProperties(
                    usePlatformDefaultWidth = false
                )
            ) {
                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxWidth(0.45f)
                        .heightIn(max = rememberDialogMaxHeight())
                        .fillMaxHeight(),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(all = 6.dp)
                            .heightIn(max = (maxHeight - 12.dp).coerceAtMost(rememberDialogMaxHeight()))
                            .wrapContentHeight(),
                        shape = MaterialTheme.shapes.extraLarge,
                        color = cardColor(false),
                        contentColor = onCardColor(),
                        shadowElevation = 3.dp
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            //标题
                            Text(
                                modifier = Modifier
                                    .padding(horizontal = 16.dp)
                                    .padding(top = 16.dp),
                                text = stringResource(R.string.versions_modify_version),
                                style = MaterialTheme.typography.titleLarge
                            )

                            val scrollState = rememberScrollState()
                            Column(
                                modifier = Modifier
                                    .weight(1f, fill = false)
                                    .padding(horizontal = 16.dp)
                                    .fadeEdge(state = scrollState)
                                    .verticalScrollWithBar(state = scrollState),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OwnOutlinedTextField(
                                    modifier = Modifier.fillMaxWidth(),
                                    value = nameValue,
                                    onValueChange = { nameValue = it },
                                    singleLine = true,
                                    shape = MaterialTheme.shapes.large,
                                    label = {
                                        Text(text = stringResource(R.string.download_game_version_name))
                                    },
                                    supportingText = {
                                        Text(
                                            text = if (!nameValid) {
                                                filenameInvalidMessage
                                                    ?: (if (nameConflict) existsError else emptyError)
                                            } else {
                                                stringResource(R.string.versions_modify_name_tip)
                                            }
                                        )
                                    },
                                    isError = !nameValid
                                )

                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = MaterialTheme.shapes.large,
                                    color = MaterialTheme.colorScheme.secondaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                                ) {
                                    Column(
                                        modifier = Modifier.padding(all = 8.dp),
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(
                                            text = stringResource(R.string.versions_modify_confirm_message),
                                            style = MaterialTheme.typography.titleMedium
                                        )

                                        payload.diffs.list.forEach { diff ->
                                            DiffItem(diff)
                                        }
                                    }
                                }
                            }

                            //按钮
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp)
                                    .padding(bottom = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                FilledTonalButton(
                                    modifier = Modifier.weight(0.5f),
                                    onClick = { changeOperation(ModifyOperation.None) }
                                ) {
                                    MarqueeText(text = stringResource(R.string.generic_cancel))
                                }
                                Button(
                                    modifier = Modifier.weight(0.5f),
                                    enabled = nameValid,
                                    onClick = { onModify(payload.copy(newVersionName = nameValue)) }
                                ) {
                                    MarqueeText(text = stringResource(R.string.generic_confirm))
                                }
                            }
                        }
                    }
                }
            }
        }
        is ModifyOperation.Install -> {
            if (installer != null) {
                val tasks by installer.tasksFlow.collectAsStateWithLifecycle()
                val installLog = installer.logOutput.collectAsStateWithLifecycle()
                if (tasks.isNotEmpty()) {
                    //修改版本流程对话框
                    TitleTaskFlowDialog(
                        title = stringResource(R.string.versions_modify_version),
                        tasks = tasks,
                        onCancel = {
                            onCancel()
                            changeOperation(ModifyOperation.None)
                        },
                        logOutput = installLog.value
                    )
                }
            }
        }
        is ModifyOperation.Error -> {
            val th = operation.th
            Logger.error(TAG, "Failed to modify the version!", th)
            val message = when (th) {
                is HttpRequestTimeoutException, is SocketTimeoutException, is TimeoutException -> stringResource(R.string.error_timeout)
                is UnknownHostException, is UnresolvedAddressException -> stringResource(R.string.error_network_unreachable)
                is ConnectException -> stringResource(R.string.error_connection_failed)
                is SerializationException, is JsonSyntaxException -> stringResource(R.string.error_parse_failed)
                is CantFetchingOptiFineUrlException -> stringResource(R.string.download_install_error_cant_fetch_optifine_download_url)
                is OptiFineForge17IncompatibleException -> stringResource(R.string.download_install_error_optifine_forge17_incompatible, th.buildof)
                is JvmCrashException -> stringResource(R.string.download_install_error_jvm_crash, th.code)
                is DownloadFailedException -> stringResource(R.string.download_install_error_download_failed)
                else -> when {
                    th.isProcessStartRefused() -> stringResource(R.string.download_install_error_process_start)
                    else -> th.localizedMessage ?: th.message ?: th::class.qualifiedName ?: "Unknown error"
                }
            }
            val dismiss = {
                changeOperation(ModifyOperation.None)
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
                            .verticalScrollWithBar(state = scrollState),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(text = stringResource(R.string.versions_modify_error_message))
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
        is ModifyOperation.Success -> {
            SimpleAlertDialog(
                title = stringResource(R.string.download_install_success_title),
                text = stringResource(R.string.versions_modify_success)
            ) {
                changeOperation(ModifyOperation.None)
            }
        }
    }
}