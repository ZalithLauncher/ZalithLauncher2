/*
 * Zalith Launcher 2
 * Copyright (C) 2025-2026 MovTery <movtery228@qq.com> and contributors
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

package com.movtery.zalithlauncher.ui.screens.content.download.addon.elements

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.nonInteractiveScrollbar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.game.download.addon.platform.PlatformClasses
import com.movtery.zalithlauncher.game.download.addon.quick.QuickCompatibility
import com.movtery.zalithlauncher.game.download.addon.quick.QuickDownloadRequest
import com.movtery.zalithlauncher.game.download.addon.quick.downloadQuickForVersions
import com.movtery.zalithlauncher.game.version.installed.Version
import com.movtery.zalithlauncher.game.version.installed.VersionsManager
import com.movtery.zalithlauncher.ui.components.AppDialog
import com.movtery.zalithlauncher.ui.components.MarqueeText
import com.movtery.zalithlauncher.ui.components.SimpleAlertDialog
import com.movtery.zalithlauncher.ui.components.fadeEdge
import com.movtery.zalithlauncher.ui.components.rememberDialogMaxHeight
import com.movtery.zalithlauncher.ui.resolveAndroidString
import com.movtery.zalithlauncher.ui.theme.cardColor
import com.movtery.zalithlauncher.ui.theme.onCardColor
import com.movtery.zalithlauncher.utils.network.isUsingMobileData
import com.movtery.zalithlauncher.viewmodel.ErrorViewModel

/** 页面内部承接的快捷下载状态机 */
sealed interface QuickDownloadOperation {
    data object None : QuickDownloadOperation
    /** 选择本地已安装的游戏版本对话框 */
    data class SelectVersion(val request: QuickDownloadRequest) : QuickDownloadOperation
    /** 警告用户正在使用移动网络 */
    data class WarningForMobileData(
        val request: QuickDownloadRequest,
        val versions: List<Version>
    ) : QuickDownloadOperation
    /** 安装任务，一次性副作用提交任务 */
    data class Install(
        val request: QuickDownloadRequest,
        val versions: List<Version>
    ) : QuickDownloadOperation
    /** 安装过程中遇到错误 */
    data class Error(val message: ErrorViewModel.ThrowableMessage) : QuickDownloadOperation
}

/**
 * 渲染当前状态
 *
 * [QuickDownloadOperation.Install] 只通过一次性副作用提交任务
 */
@Composable
fun QuickDownloadOperation(
    operation: QuickDownloadOperation,
    changeOperation: (QuickDownloadOperation) -> Unit,
    submitError: ((ErrorViewModel.ThrowableMessage) -> Unit)? = null
) {
    when (operation) {
        is QuickDownloadOperation.None -> {}
        is QuickDownloadOperation.SelectVersion -> {
            val context = LocalContext.current
            QuickDownloadDialog(
                request = operation.request,
                onDismiss = { changeOperation(QuickDownloadOperation.None) },
                onInstall = { versions ->
                    changeOperation(
                        if (isUsingMobileData(context)) {
                            QuickDownloadOperation.WarningForMobileData(operation.request, versions)
                        } else {
                            QuickDownloadOperation.Install(operation.request, versions)
                        }
                    )
                }
            )
        }
        is QuickDownloadOperation.WarningForMobileData -> {
            SimpleAlertDialog(
                title = stringResource(R.string.generic_warning),
                text = stringResource(R.string.download_install_warning_mobile_data),
                confirmText = stringResource(R.string.generic_anyway),
                onDismiss = { changeOperation(QuickDownloadOperation.None) },
                onConfirm = {
                    changeOperation(QuickDownloadOperation.Install(operation.request, operation.versions))
                }
            )
        }
        is QuickDownloadOperation.Install -> {
            LaunchedEffect(operation) {
                changeOperation(QuickDownloadOperation.None)
                downloadQuickForVersions(
                    request = operation.request,
                    versions = operation.versions,
                    submitError = { message ->
                        if (submitError != null) submitError(message)
                        else changeOperation(QuickDownloadOperation.Error(message))
                    }
                )
            }
        }
        is QuickDownloadOperation.Error -> {
            SimpleAlertDialog(
                title = resolveAndroidString(operation.message.title).text,
                text = resolveAndroidString(operation.message.message).text,
                confirmText = stringResource(R.string.generic_got_it),
                onDismiss = { changeOperation(QuickDownloadOperation.None) }
            )
        }
    }
}

/** 仅使用已安装版本快照和请求摘要 */
@Composable
private fun QuickDownloadDialog(
    request: QuickDownloadRequest,
    onDismiss: () -> Unit,
    onInstall: (List<Version>) -> Unit
) {
    val versions by rememberValidVersions()
    val currentVersion by VersionsManager.currentVersion.collectAsStateWithLifecycle()
    val current = currentVersion

    if (current == null || versions.isEmpty()) {
        // 没有安装游戏版本
        SimpleAlertDialog(
            title = stringResource(R.string.generic_warning),
            text = stringResource(R.string.download_addon_no_installed_versions),
            confirmText = stringResource(R.string.generic_got_it),
            onDismiss = onDismiss
        )
        return
    }

    val selectedVersions = remember(request, current, versions) {
        mutableStateListOf<Version>().apply {
            if (current in versions) add(current)
        }
    }

    val listState = rememberLazyListState()
    // 定位到当前选中的游戏版本
    LaunchedEffect(versions, current) {
        val index = versions.indexOf(current)
        if (index >= 0) listState.scrollToItem(index)
    }

    AppDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth(0.5f)
                .heightIn(max = rememberDialogMaxHeight()),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier
                    .padding(6.dp)
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
                    MarqueeText(
                        text = stringResource(R.string.download_addon_install_addons_for_versions),
                        style = MaterialTheme.typography.titleMedium
                    )

                    LazyColumn(
                        modifier = Modifier
                            .weight(1f, fill = false)
                            .fillMaxWidth()
                            .fadeEdge(listState)
                            .nonInteractiveScrollbar(
                                state = listState.scrollIndicatorState!!,
                                orientation = Orientation.Vertical
                            ),
                        state = listState,
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(versions) { version ->
                            val compatibility = request.compatibilityWith(version)
                            SelectVersionListItem(
                                modifier = Modifier.fillMaxWidth(),
                                version = version,
                                checked = version in selectedVersions,
                                supportingText = when (compatibility) {
                                    QuickCompatibility.COMPATIBLE -> null
                                    QuickCompatibility.INCOMPATIBLE -> stringResource(R.string.download_addon_quick_incompatible)
                                    QuickCompatibility.UNKNOWN -> stringResource(R.string.download_addon_quick_unknown)
                                },
                                supportingTextColor = when (compatibility) {
                                    QuickCompatibility.INCOMPATIBLE -> MaterialTheme.colorScheme.error
                                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                                },
                                onChose = {
                                    if (version !in selectedVersions) selectedVersions.add(version)
                                },
                                onCancel = {
                                    selectedVersions.remove(version)
                                }
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
                            enabled = selectedVersions.isNotEmpty() && request.classes != PlatformClasses.MOD_PACK,
                            onClick = {
                                onInstall(selectedVersions.toList())
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
