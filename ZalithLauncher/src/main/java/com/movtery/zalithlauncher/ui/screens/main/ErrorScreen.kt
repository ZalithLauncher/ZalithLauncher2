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

package com.movtery.zalithlauncher.ui.screens.main

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContent
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.info.InfoDistributor
import com.movtery.zalithlauncher.setting.AllSettings
import com.movtery.zalithlauncher.ui.activities.CrashType
import com.movtery.zalithlauncher.ui.components.BackgroundCard
import com.movtery.zalithlauncher.ui.components.MarqueeText
import com.movtery.zalithlauncher.ui.components.ScalingActionButton
import com.movtery.zalithlauncher.ui.theme.backgroundColor
import com.movtery.zalithlauncher.ui.theme.onBackgroundColor

@Composable
fun ErrorScreen(
    crashType: CrashType,
    shareLogs: Boolean = true,
    canUpload: Boolean = false,
    canRestart: Boolean = true,
    onShareLogsClick: () -> Unit = {},
    onUploadClick: () -> Unit = {},
    onRestartClick: () -> Unit = {},
    onExitClick: () -> Unit = {},
    body: @Composable ColumnScope.() -> Unit
) {
    //获取方向信息，展示两套不同的UI
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    if (isLandscape) {
        ErrorScreenLandscape(
            crashType = crashType,
            shareLogs = shareLogs,
            canUpload = canUpload,
            canRestart = canRestart,
            onShareLogsClick = onShareLogsClick,
            onUploadClick = onUploadClick,
            onRestartClick = onRestartClick,
            onExitClick = onExitClick,
            body = body
        )
    } else {
        ErrorScreenPortrait(
            crashType = crashType,
            shareLogs = shareLogs,
            canUpload = canUpload,
            canRestart = canRestart,
            onShareLogsClick = onShareLogsClick,
            onUploadClick = onUploadClick,
            onRestartClick = onRestartClick,
            onExitClick = onExitClick,
            body = body
        )
    }
}

/**
 * 崩溃页面（横屏页面）
 */
@Composable
private fun ErrorScreenLandscape(
    crashType: CrashType,
    shareLogs: Boolean,
    canUpload: Boolean,
    canRestart: Boolean,
    onShareLogsClick: () -> Unit,
    onUploadClick: () -> Unit,
    onRestartClick: () -> Unit,
    onExitClick: () -> Unit,
    body: @Composable ColumnScope.() -> Unit
) {
    Scaffold(
        topBar = {
            TopBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp),
                crashType = crashType,
            )
        },
        modifier = Modifier.fillMaxSize(),
        containerColor = backgroundColor(),
        contentColor = onBackgroundColor(),
        contentWindowInsets = if (AllSettings.launcherFullScreen.state) {
            WindowInsets()
        } else {
            WindowInsets.safeContent.only(WindowInsetsSides.Horizontal)
        }
    ) { innerPadding ->
        Row(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ErrorContent(
                modifier = Modifier.weight(7f),
                body = body
            )

            ActionLayout(
                modifier = Modifier.weight(3f),
                crashType = crashType,
                shareLogs = shareLogs,
                canUpload = canUpload,
                canRestart = canRestart,
                onShareLogsClick = onShareLogsClick,
                onUploadClick = onUploadClick,
                onRestartClick = onRestartClick,
                onExitClick = onExitClick
            )
        }
    }
}

/**
 * 崩溃页面（竖屏版本）
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ErrorScreenPortrait(
    crashType: CrashType,
    shareLogs: Boolean,
    canUpload: Boolean,
    canRestart: Boolean,
    onShareLogsClick: () -> Unit,
    onUploadClick: () -> Unit,
    onRestartClick: () -> Unit,
    onExitClick: () -> Unit,
    body: @Composable ColumnScope.() -> Unit
) {
    //控制下拉菜单的显示状态
    var showMenu by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(
                            R.string.crash_type,
                            stringResource(crashType.textRes)
                        )
                    )
                },
                actions = {
                    IconButton(
                        onClick = { showMenu = true }
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_more_vert),
                            contentDescription = stringResource(R.string.generic_more)
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        if (canUpload) {
                            DropdownMenuItem(
                                text = {
                                    MarqueeText(text = stringResource(R.string.crash_link_share_button))
                                },
                                onClick = {
                                    showMenu = false
                                    onUploadClick()
                                }
                            )
                        }
                        DropdownMenuItem(
                            text = {
                                MarqueeText(text = stringResource(R.string.crash_share_logs))
                            },
                            onClick = {
                                showMenu = false
                                onShareLogsClick()
                            },
                            enabled = shareLogs
                        )
                        if (canRestart) {
                            DropdownMenuItem(
                                text = {
                                    MarqueeText(text = stringResource(R.string.crash_restart))
                                },
                                onClick = {
                                    showMenu = false
                                    onRestartClick()
                                }
                            )
                        }
                        DropdownMenuItem(
                            text = {
                                MarqueeText(text = stringResource(R.string.crash_exit))
                            },
                            onClick = {
                                showMenu = false
                                onExitClick()
                            }
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(all = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (crashType == CrashType.LAUNCHER_CRASH) {
                //仅在启动器崩溃时，才显示这行略显严重的文本
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(R.string.crash_launcher_title, InfoDistributor.LAUNCHER_NAME)
                    )
                }

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    content = body
                )
            } else {
                body()
            }
        }
    }
}

@Composable
private fun TopBar(
    modifier: Modifier = Modifier,
    crashType: CrashType,
) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center
    ) {
        val text = when (crashType) {
            //在启动器崩溃的时候，显示一个较为严重的标题
            CrashType.LAUNCHER_CRASH -> stringResource(R.string.crash_launcher_title, InfoDistributor.LAUNCHER_NAME)
            //游戏运行崩溃了，大概和启动器关系不大，仅展示应用标题
            CrashType.GAME_CRASH -> InfoDistributor.LAUNCHER_NAME
        }
        Text(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            text = text
        )
    }
}

@Composable
private fun ErrorContent(
    modifier: Modifier = Modifier,
    body: @Composable ColumnScope.() -> Unit
) {
    BackgroundCard(
        modifier = modifier,
        influencedByBackground = false,
        shape = MaterialTheme.shapes.extraLarge
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(state = rememberScrollState())
                .padding(all = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            content = body
        )
    }
}

@Composable
private fun ActionLayout(
    modifier: Modifier = Modifier,
    crashType: CrashType,
    shareLogs: Boolean,
    canUpload: Boolean,
    canRestart: Boolean,
    onShareLogsClick: () -> Unit = {},
    onUploadClick: () -> Unit = {},
    onRestartClick: () -> Unit = {},
    onExitClick: () -> Unit = {}
) {
    Column(modifier = modifier) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Top
        ) {
            Text(
                text = stringResource(
                    R.string.crash_type,
                    stringResource(crashType.textRes)
                ),
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.Bottom)
        ) {
            if (canUpload) {
                ScalingActionButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onUploadClick
                ) {
                    MarqueeText(text = stringResource(R.string.crash_link_share_button))
                }
            }
            if (shareLogs) {
                ScalingActionButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onShareLogsClick
                ) {
                    MarqueeText(text = stringResource(R.string.crash_share_logs))
                }
            }
            if (canRestart) {
                ScalingActionButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onRestartClick
                ) {
                    MarqueeText(text = stringResource(R.string.crash_restart))
                }
            }
            ScalingActionButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = onExitClick
            ) {
                MarqueeText(text = stringResource(R.string.crash_exit))
            }
        }
    }
}

@Preview
@Composable
private fun PreviewErrorScreen() {
    MaterialExpressiveTheme {
        ErrorScreen(
            crashType = CrashType.LAUNCHER_CRASH,
            shareLogs = true,
            canUpload = true,
            canRestart = true,
            onShareLogsClick = {},
            onUploadClick = {},
            onRestartClick = {},
            onExitClick = {},
            body = {}
        )
    }
}