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

package com.movtery.zalithlauncher.ui.screens.content.versions.elements

import android.content.Context
import androidx.compose.foundation.basicMarquee
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.gson.JsonSyntaxException
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.game.download.assets.platform.PlatformVersion
import com.movtery.zalithlauncher.game.download.assets.utils.getMcmodTitle
import com.movtery.zalithlauncher.game.download.jvm_server.JvmCrashException
import com.movtery.zalithlauncher.game.version.download.DownloadFailedException
import com.movtery.zalithlauncher.game.version.mod.LocalMod
import com.movtery.zalithlauncher.game.version.mod.RemoteMod
import com.movtery.zalithlauncher.game.version.mod.isEnabled
import com.movtery.zalithlauncher.game.version.mod.update.ModData
import com.movtery.zalithlauncher.game.version.mod.update.ModUpdater
import com.movtery.zalithlauncher.ui.components.MarqueeText
import com.movtery.zalithlauncher.ui.components.ProgressDialog
import com.movtery.zalithlauncher.ui.components.SimpleAlertDialog
import com.movtery.zalithlauncher.ui.components.fadeEdge
import com.movtery.zalithlauncher.ui.screens.content.download.assets.elements.AssetsIcon
import com.movtery.zalithlauncher.ui.screens.content.elements.TitleTaskFlowDialog
import com.movtery.zalithlauncher.ui.screens.content.versions.elements.ModStateFilter.All
import com.movtery.zalithlauncher.ui.screens.content.versions.elements.ModStateFilter.Disabled
import com.movtery.zalithlauncher.ui.screens.content.versions.elements.ModStateFilter.Enabled
import com.movtery.zalithlauncher.ui.theme.cardColor
import com.movtery.zalithlauncher.ui.theme.itemColor
import com.movtery.zalithlauncher.ui.theme.onCardColor
import com.movtery.zalithlauncher.ui.theme.onItemColor
import com.movtery.zalithlauncher.utils.logging.Logger
import io.ktor.client.plugins.HttpRequestTimeoutException
import kotlinx.serialization.SerializationException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.nio.channels.UnresolvedAddressException
import java.util.concurrent.TimeoutException

sealed interface ModsOperation {
    data object None : ModsOperation
    /** 执行任务中 */
    data object Progress : ModsOperation
    /** 删除模组对话框 */
    data class Delete(val mod: LocalMod) : ModsOperation
}

sealed interface ModsUpdateOperation {
    data object None : ModsUpdateOperation
    /** 警告用户更新模组的注意事项 */
    data class Warning(val mods: List<RemoteMod>) : ModsUpdateOperation
    /** 开始更新模组 */
    data object Update : ModsUpdateOperation
    /** 更新模组时出现异常 */
    data class Error(val th: Throwable) : ModsUpdateOperation
    /** 更新模组成功 */
    data object Success : ModsUpdateOperation
}

sealed interface ModsConfirmOperation {
    data object None : ModsConfirmOperation
    /** 等待用户确认模组更新的信息 */
    data class WaitingConfirm(val map: Map<ModData, PlatformVersion>) : ModsConfirmOperation
}

@Composable
fun ModsOperation(
    modsOperation: ModsOperation,
    updateOperation: (ModsOperation) -> Unit,
    onDelete: (LocalMod) -> Unit
) {
    when (modsOperation) {
        is ModsOperation.None -> {}
        is ModsOperation.Progress -> {
            ProgressDialog()
        }
        is ModsOperation.Delete -> {
            val mod = modsOperation.mod
            SimpleAlertDialog(
                title = stringResource(R.string.generic_warning),
                text = stringResource(R.string.mods_manage_delete_warning, mod.name),
                onDismiss = {
                    updateOperation(ModsOperation.None)
                },
                onConfirm = {
                    onDelete(mod)
                    updateOperation(ModsOperation.None)
                }
            )
        }
    }
}

@Composable
fun ModsUpdateOperation(
    operation: ModsUpdateOperation,
    changeOperation: (ModsUpdateOperation) -> Unit,
    modsUpdater: ModUpdater?,
    onUpdate: (List<RemoteMod>) -> Unit,
    onCancel: () -> Unit
) {
    when (operation) {
        is ModsUpdateOperation.None -> {}
        is ModsUpdateOperation.Warning -> {
            //警告更新模组可能带来问题
            SimpleAlertDialog(
                title = stringResource(R.string.generic_warning),
                text = {
                    Text(text = stringResource(R.string.mods_update_warning_1))
                    Text(text = stringResource(R.string.mods_update_warning_2))
                    Text(text = stringResource(R.string.mods_update_warning_3))
                },
                confirmText = stringResource(R.string.mods_update),
                onCancel = {
                    changeOperation(ModsUpdateOperation.None)
                },
                onConfirm = {
                    onUpdate(operation.mods)
                }
            )
        }
        is ModsUpdateOperation.Update -> {
            if (modsUpdater != null) {
                val tasks = modsUpdater.tasksFlow.collectAsStateWithLifecycle()
                if (tasks.value.isNotEmpty()) {
                    //更新模组流程对话框
                    TitleTaskFlowDialog(
                        title = stringResource(R.string.mods_update),
                        tasks = tasks.value,
                        onCancel = {
                            onCancel()
                            changeOperation(ModsUpdateOperation.None)
                        }
                    )
                }
            }
        }
        is ModsUpdateOperation.Error -> {
            val th = operation.th
            Logger.error("UpdateMods", "Failed to update the mods", th)
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
                changeOperation(ModsUpdateOperation.None)
            }
            AlertDialog(
                onDismissRequest = dismiss,
                title = {
                    Text(text = stringResource(R.string.mods_update_failed))
                },
                text = {
                    val scrollState = rememberScrollState()
                    Column(
                        modifier = Modifier
                            .fadeEdge(state = scrollState)
                            .verticalScroll(state = scrollState),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(text = stringResource(R.string.mods_update_failed_text))
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
        is ModsUpdateOperation.Success -> {
            SimpleAlertDialog(
                title = stringResource(R.string.mods_update_success),
                text = stringResource(R.string.mods_update_success_text)
            ) {
                changeOperation(ModsUpdateOperation.None)
            }
        }
    }
}

@Composable
fun ModsConfirmOperation(
    operation: ModsConfirmOperation,
    onCancel: () -> Unit,
    onConfirm: () -> Unit
) {
    when (operation) {
        is ModsConfirmOperation.None -> {}
        is ModsConfirmOperation.WaitingConfirm -> {
            ModsUpdateListDialog(
                data = operation.map.toList(),
                onCancel = {
                    onCancel()
                },
                onConfirm = {
                    onConfirm()
                }
            )
        }
    }
}

/**
 * 模组更新：展示需要更新的模组的详细信息
 */
@Composable
private fun ModsUpdateListDialog(
    data: List<Pair<ModData, PlatformVersion>>,
    onCancel: () -> Unit,
    onConfirm: () -> Unit
) {
    Dialog(
        onDismissRequest = {}
    ) {
        BoxWithConstraints(
            modifier = Modifier.fillMaxHeight(),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier
                    .padding(all = 6.dp)
                    .heightIn(max = maxHeight - 12.dp)
                    .wrapContentHeight(),
                shape = MaterialTheme.shapes.extraLarge,
                color = cardColor(false),
                contentColor = onCardColor(),
                shadowElevation = 6.dp
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .wrapContentHeight(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(R.string.mods_update_task_show),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.size(16.dp))

                    val scrollState = rememberLazyListState()
                    LazyColumn(
                        modifier = Modifier
                            .fadeEdge(state = scrollState)
                            .weight(1f, fill = false),
                        state = scrollState,
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(vertical = 12.dp)
                    ) {
                        items(data) { entry ->
                            ModsUpdateEntryItem(
                                modifier = Modifier.fillMaxWidth(),
                                entry = entry
                            )
                        }
                    }

                    Spacer(modifier = Modifier.size(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        FilledTonalButton(
                            modifier = Modifier.weight(0.5f),
                            onClick = onCancel
                        ) {
                            MarqueeText(text = stringResource(R.string.generic_cancel))
                        }
                        Button(
                            modifier = Modifier.weight(0.5f),
                            onClick = onConfirm
                        ) {
                            MarqueeText(text = stringResource(R.string.mods_update))
                        }
                    }
                }
            }
        }
    }
}

/**
 * 模组更新：单个模组更新详情展示
 * 展示旧版本与新版本对比
 */
@Composable
private fun ModsUpdateEntryItem(
    entry: Pair<ModData, PlatformVersion>,
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.large,
    color: Color = itemColor(false),
    contentColor: Color = onItemColor(),
) {
    val context = LocalContext.current

    val data = entry.first
    val newVersion = entry.second

    Surface(
        modifier = modifier,
        shape = shape,
        color = color,
        contentColor = contentColor,
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AssetsIcon(
                modifier = Modifier.clip(shape = RoundedCornerShape(10.dp)),
                size = 34.dp,
                iconUrl = data.project.iconUrl
            )

            Column(
                modifier = Modifier.weight(1f)
            ) {
                val title = data.project.title
                val displayTitle = data.mcMod?.getMcmodTitle(title, context) ?: title
                Text(
                    modifier = Modifier.basicMarquee(iterations = Int.MAX_VALUE),
                    text = displayTitle,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1
                )

                //新旧版本对比
                Row(
                    modifier = Modifier
                        .height(IntrinsicSize.Min)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    //旧版本
                    MarqueeText(
                        modifier = Modifier.weight(1f),
                        text = data.currentVersion ?: "???", //未知
                        style = MaterialTheme.typography.labelSmall.copy(
                            textDecoration = TextDecoration.LineThrough
                        )
                    )
                    VerticalDivider(
                        modifier = Modifier
                            .fillMaxHeight()
                            .padding(vertical = 2.dp),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                    )
                    //新版本
                    MarqueeText(
                        modifier = Modifier.weight(1f),
                        text = newVersion.platformVersion(),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
    }
}

enum class ModStateFilter(val textRes: Int) {
    All(R.string.generic_all),
    Enabled(R.string.generic_enabled),
    Disabled(R.string.generic_disabled)
}

/**
 * 根据名称，筛选模组
 */
fun List<RemoteMod>.filterMods(
    nameFilter: String,
    stateFilter: ModStateFilter = All,
    context: Context? = null
) = this.filter { mod ->
    val matchesName = nameFilter.isEmpty() || (
            mod.localMod.file.name.contains(nameFilter, true) ||
            mod.localMod.name.contains(nameFilter, true) ||
            mod.projectInfo?.title?.contains(nameFilter, true) == true ||
            mod.mcMod?.getMcmodTitle(mod.localMod.name, context)?.contains(nameFilter, true) == true
    )

    val matchesState = when (stateFilter) {
        All -> true
        Enabled -> mod.localMod.file.isEnabled()
        Disabled -> !mod.localMod.file.isEnabled()
    }

    matchesName && matchesState
}