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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.ui.theme.cardColor
import com.movtery.zalithlauncher.ui.theme.onCardColor

sealed interface LogShareMenuOperation {
    data object None : LogShareMenuOperation
    /** 打开日志操作菜单 */
    data object ShowMenu : LogShareMenuOperation
}

@Composable
fun LogShareMenu(
    operation: LogShareMenuOperation,
    onChange: (LogShareMenuOperation) -> Unit,
    onView: () -> Unit,
    onShare: () -> Unit,
    canUpload: Boolean,
    onUpload: () -> Unit
) {
    when (operation) {
        is LogShareMenuOperation.None -> {}
        is LogShareMenuOperation.ShowMenu -> {
            Dialog(
                onDismissRequest = {
                    onChange(LogShareMenuOperation.None)
                }
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(all = 6.dp),
                    color = cardColor(false),
                    contentColor = onCardColor(),
                    shape = MaterialTheme.shapes.extraLarge,
                    shadowElevation = 6.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(all = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        //查看日志
                        Button(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = {
                                onView()
                                onChange(LogShareMenuOperation.None)
                            }
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_assignment_filled),
                                contentDescription = null
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(text = stringResource(R.string.generic_view))
                        }
                        //分享日志
                        Button(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = {
                                onShare()
                                onChange(LogShareMenuOperation.None)
                            }
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_share_filled),
                                contentDescription = null
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(text = stringResource(R.string.crash_share_logs))
                        }
                        //分享链接
                        Button(
                            modifier = Modifier.fillMaxWidth(),
                            enabled = canUpload,
                            onClick = {
                                onUpload()
                                onChange(LogShareMenuOperation.None)
                            }
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_link),
                                contentDescription = null
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(text = stringResource(R.string.crash_link_share_button))
                        }
                        //关闭
                        Button(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = {
                                onChange(LogShareMenuOperation.None)
                            }
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_close),
                                contentDescription = null
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(text = stringResource(R.string.generic_close))
                        }
                    }
                }
            }
        }
    }
}
