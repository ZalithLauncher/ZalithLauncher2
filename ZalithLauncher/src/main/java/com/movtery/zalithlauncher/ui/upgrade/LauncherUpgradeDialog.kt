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

package com.movtery.zalithlauncher.ui.upgrade

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.ui.components.MarkdownView
import com.movtery.zalithlauncher.ui.components.defaultRichTextStyle
import com.movtery.zalithlauncher.ui.components.verticalScrollWithBar
import com.movtery.zalithlauncher.ui.theme.cardColor
import com.movtery.zalithlauncher.ui.theme.onCardColor
import com.movtery.zalithlauncher.upgrade.RemoteData
import com.movtery.zalithlauncher.upgrade.findCurrentBody
import com.movtery.zalithlauncher.upgrade.getCurrentCouldDrive
import com.movtery.zalithlauncher.utils.formatDate
import java.util.Locale

@Composable
fun UpgradeDialog(
    data: RemoteData,
    onDismissRequest: () -> Unit,
    onFilesClick: () -> Unit,
    onIgnored: () -> Unit,
    onLinkClick: (String) -> Unit,
    onCloudDriveClick: (RemoteData.CloudDrive) -> Unit
) {
    val body = remember(data) {
        data.findCurrentBody(Locale.getDefault()) ?: data.defaultBody
    }
    val cloudDrive = remember(data) {
        data.getCurrentCouldDrive(Locale.getDefault())
    }

    Dialog(
        onDismissRequest = onDismissRequest
    ) {
        Surface(
            modifier = Modifier.padding(all = 3.dp),
            shape = MaterialTheme.shapes.extraLarge,
            color = cardColor(false),
            contentColor = onCardColor(),
            shadowElevation = 3.dp
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .padding(top = 16.dp),
                    text = stringResource(R.string.upgrade_new)
                )

                //版本号
                val versionStr = stringResource(R.string.upgrade_version_change, data.version)
                //更新时间
                val dateStr = stringResource(
                    R.string.upgrade_version_create_at,
                    formatDate(
                        input = data.createdAt,
                        pattern = stringResource(R.string.date_format)
                    )
                )
                val markdownBody = "$versionStr  \n$dateStr  \n\n${body.markdown}"

                CompositionLocalProvider(
                    LocalUriHandler provides object : UriHandler {
                        override fun openUri(uri: String) {
                            onLinkClick(uri)
                        }
                    }
                ) {
                    MarkdownView(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = false)
                            .padding(horizontal = 20.dp)
                            .verticalScrollWithBar(rememberScrollState()),
                        content = markdownBody,
                        richTextStyle = defaultRichTextStyle(),
                    )
                }

                //按钮
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (cloudDrive == null) {
                        Spacer(Modifier.weight(1f))
                    } else {
                        FilledTonalButton(
                            onClick = {
                                if (cloudDrive.links.isEmpty()) {
                                    //未配置多网盘链接，使用默认链接（旧版兼容，必定会有）
                                    onLinkClick(cloudDrive.link)
                                } else if (cloudDrive.links.size == 1) {
                                    //只有一个网盘链接，则直接访问链接
                                    onLinkClick(cloudDrive.links[0].link)
                                } else {
                                    onCloudDriveClick(cloudDrive)
                                }
                            }
                        ) {
                            Text(text = stringResource(R.string.upgrade_cloud_drive))
                        }
                        Spacer(Modifier.weight(1f))
                    }

                    FilledTonalButton(
                        onClick = {
                            onIgnored()
                            onDismissRequest()
                        }
                    ) {
                        Text(text = stringResource(R.string.generic_ignore))
                    }

                    Button(
                        onClick = onFilesClick
                    ) {
                        Text(text = stringResource(R.string.upgrade_more))
                    }
                }
            }
        }
    }
}