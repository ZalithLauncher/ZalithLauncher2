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

package com.movtery.zalithlauncher.ui.screens.main.crashlogs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.crashlogs.LinkNotFoundException
import com.movtery.zalithlauncher.ui.components.SimpleAlertDialog
import com.movtery.zalithlauncher.utils.string.getMessageOrToString

/**
 * 上传游戏崩溃日志操作流程
 */
sealed interface ShareLinkOperation {
    data object None : ShareLinkOperation
    /** 提示对话框 */
    data object Tip : ShareLinkOperation
    /**
     * 上传日志中
     * @param apiRoot API 站点链接，仅作透明化展示
     */
    data class Uploading(val apiRoot: String) : ShareLinkOperation
    /** 发生错误，展示对话框 */
    data class Error(val error: Throwable) : ShareLinkOperation
}

@Composable
fun ShareLinkOperation(
    operation: ShareLinkOperation,
    onChange: (ShareLinkOperation) -> Unit,
    onUpload: () -> Unit,
    onUploadChancel: () -> Unit
) {
    when (operation) {
        is ShareLinkOperation.None -> {}
        is ShareLinkOperation.Tip -> {
            SimpleAlertDialog(
                title = stringResource(R.string.crash_link_share_button),
                text = stringResource(R.string.crash_link_share_tip),
                dismissByDialog = false,
                onConfirm = onUpload,
                onDismiss = {
                    onChange(ShareLinkOperation.None)
                }
            )
        }
        is ShareLinkOperation.Uploading -> {
            AlertDialog(
                onDismissRequest = {},
                title = {
                    Text(
                        text = stringResource(R.string.crash_link_share_button),
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                text = {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = stringResource(R.string.crash_link_share_uploading, operation.apiRoot)
                        )
                        LinearWavyProgressIndicator(
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    FilledTonalButton(
                        onClick = onUploadChancel
                    ) {
                        Text(text = stringResource(R.string.generic_cancel))
                    }
                }
            )
        }
        is ShareLinkOperation.Error -> {
            SimpleAlertDialog(
                title = stringResource(R.string.crash_link_share_failed),
                text = when (val error = operation.error) {
                    is LinkNotFoundException -> {
                        stringResource(R.string.crash_link_share_failed_link_not_found)
                    }
                    else -> {
                        error.getMessageOrToString()
                    }
                },
                dismissByDialog = false,
                onDismiss = {
                    onChange(ShareLinkOperation.None)
                }
            )
        }
    }
}