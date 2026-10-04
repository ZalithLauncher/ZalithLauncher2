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

package com.movtery.zalithlauncher.ui.screens.game.multiplayer

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.nonInteractiveScrollbar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.terracotta.TerracottaState
import com.movtery.zalithlauncher.terracotta.profile.TerracottaProfile
import com.movtery.zalithlauncher.ui.AndroidStringText
import com.movtery.zalithlauncher.ui.components.BackgroundCard
import com.movtery.zalithlauncher.ui.components.MarqueeText
import com.movtery.zalithlauncher.ui.components.rememberDialogMaxHeight
import com.movtery.zalithlauncher.ui.components.verticalScrollWithBar
import com.movtery.zalithlauncher.ui.theme.cardColor
import com.movtery.zalithlauncher.ui.theme.itemColor
import com.movtery.zalithlauncher.ui.theme.onCardColor
import com.movtery.zalithlauncher.ui.theme.onItemColor

sealed interface TerracottaLogOperation {
    /** 正常情况下，不展示日志内容，显示对话框 UI */
    data object None : TerracottaLogOperation
    /** 正在收集日志 */
    data object CollectingLog : TerracottaLogOperation
    /** 切换到展示日志的模式 */
    data class EnableLog(val logString: String) : TerracottaLogOperation
}

/**
 * 多人联机菜单Dialog
 * @param logOperation 陶瓦联机核心日志展示状态
 * @param onShowLog 联机菜单请求切换到日志展示状态
 * @param onHideLog 联机菜单请求退出日志展示状态
 * @param isWaitingInteractive 在等待页面是否可以进行交互
 * @param terracottaVer 陶瓦联机核心版本号
 * @param easyTierVer EasyTier版本号
 * @param profiles 陶瓦联机当前房间所有玩家配置
 * @param onHostRoleClick 用户选择成为房主
 * @param onHostCopyCode 房主复制房间邀请码
 * @param onGuestPositive 房客正确输入邀请码
 * @param onGuestCopyUrl 房客复制备用链接
 * @param onBack 退出当前步骤
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MultiplayerDialog(
    onClose: () -> Unit,
    dialogState: TerracottaState.Ready?,
    logOperation: TerracottaLogOperation,
    onShowLog: () -> Unit,
    onHideLog: () -> Unit,
    isWaitingInteractive: Boolean,
    terracottaVer: String?,
    easyTierVer: String?,
    profiles: List<TerracottaProfile>,
    onHostRoleClick: () -> Unit,
    onHostCopyCode: (TerracottaState.HostOK) -> Unit,
    onGuestPositive: (roomCode: String) -> Unit,
    onGuestCopyUrl: (TerracottaState.GuestOK) -> Unit,
    onBack: () -> Unit,
    onShowToast: (AndroidStringText) -> Unit = {}
) {
    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = false,
            dismissOnClickOutside = false
        )
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth(0.7f)
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
                shadowElevation = 6.dp
            ) {
                Column(
                    modifier = Modifier.padding(all = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = stringResource(R.string.terracotta_menu),
                        style = MaterialTheme.typography.titleLarge
                    )

                    val commonModifier = Modifier
                        .weight(1f, fill = false)
                        .fillMaxWidth()

                    when (logOperation) {
                        is TerracottaLogOperation.None, TerracottaLogOperation.CollectingLog -> {
                            when (dialogState) {
                                null -> {
                                    Box(
                                        modifier = commonModifier,
                                        contentAlignment = Alignment.Center
                                    ) {
                                        LoadingIndicator()
                                    }
                                }
                                is TerracottaState.Waiting -> {
                                    WaitingUI(
                                        modifier = commonModifier,
                                        onHostClick = onHostRoleClick,
                                        onGuestPositive = onGuestPositive,
                                        isInteractive = isWaitingInteractive,
                                        onShowToast = onShowToast
                                    )
                                }
                                is TerracottaState.HostScanning -> {
                                    CommonProgressLayout(
                                        modifier = commonModifier,
                                        progress = stringResource(R.string.terracotta_status_host_scanning),
                                        text = {
                                            Text(
                                                text = stringResource(R.string.terracotta_status_host_scanning_desc),
                                                style = MaterialTheme.typography.labelMedium
                                            )
                                        },
                                        backDescription = stringResource(R.string.terracotta_status_host_scanning_back),
                                        onBack = onBack
                                    )
                                }
                                is TerracottaState.HostStarting -> {
                                    CommonProgressLayout(
                                        modifier = commonModifier,
                                        progress = stringResource(R.string.terracotta_status_host_starting),
                                        backDescription = stringResource(R.string.terracotta_status_host_starting_back),
                                        onBack = onBack
                                    )
                                }
                                is TerracottaState.HostOK -> {
                                    OkRoomUI(
                                        modifier = commonModifier,
                                        code = dialogState.code ?: "",//不会为null
                                        profiles = profiles,
                                        onCopy = {
                                            onHostCopyCode(dialogState)
                                        },
                                        onExit = onBack,
                                        okText = stringResource(R.string.terracotta_status_host_ok),
                                        codeLabel = stringResource(R.string.terracotta_status_host_ok_code),
                                        copyTitle = stringResource(R.string.terracotta_status_host_ok_code_copy),
                                        copyDesc = stringResource(R.string.terracotta_status_host_ok_code_desc),
                                        backDesc = stringResource(R.string.terracotta_status_host_ok_back)
                                    )
                                }
                                is TerracottaState.GuestConnecting -> {
                                    CommonProgressLayout(
                                        modifier = commonModifier,
                                        progress = stringResource(R.string.terracotta_status_guest_starting),
                                        backDescription = stringResource(R.string.terracotta_status_guest_starting_back),
                                        onBack = onBack
                                    )
                                }
                                is TerracottaState.GuestStarting -> {
                                    GuestStartingUI(
                                        modifier = commonModifier,
                                        difficulty = dialogState.difficulty,
                                        onBack = onBack
                                    )
                                }
                                is TerracottaState.GuestOK -> {
                                    OkRoomUI(
                                        modifier = commonModifier,
                                        code = dialogState.url ?: "",
                                        profiles = profiles,
                                        onCopy = {
                                            onGuestCopyUrl(dialogState)
                                        },
                                        onExit = onBack,
                                        okText = stringResource(R.string.terracotta_status_guest_ok),
                                        codeLabel = stringResource(R.string.terracotta_status_guest_ok_address),
                                        copyTitle = stringResource(R.string.terracotta_status_guest_ok_address_copy),
                                        copyDesc = stringResource(R.string.terracotta_status_guest_ok_address_desc),
                                        backDesc = stringResource(R.string.terracotta_status_guest_ok_back)
                                    )
                                }
                                is TerracottaState.Exception -> {
                                    ExceptionUI(
                                        modifier = commonModifier,
                                        title = stringResource(dialogState.getEnumType().textRes),
                                        onExit = onBack
                                    )
                                }
                            }
                        }
                        is TerracottaLogOperation.EnableLog -> {
                            LogUI(
                                modifier = commonModifier,
                                logString = logOperation.logString,
                                onExit = onHideLog
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        //版本号
                        Column(modifier = Modifier.weight(1f)) {
                            val terracottaVer0 = terracottaVer ?: stringResource(R.string.generic_loading)
                            val easyTierVer0 = easyTierVer ?: stringResource(R.string.generic_loading)
                            Text(
                                text = stringResource(R.string.terracotta_metadata_ver, terracottaVer0),
                                style = MaterialTheme.typography.labelMedium
                            )
                            Text(
                                text = stringResource(R.string.terracotta_metadata_easytier_ver, easyTierVer0),
                                style = MaterialTheme.typography.labelMedium
                            )
                        }

                        //查看日志
                        TextButton(
                            onClick = onShowLog,
                            enabled = logOperation !is TerracottaLogOperation.CollectingLog
                        ) {
                            if (logOperation is TerracottaLogOperation.EnableLog) {
                                //切换文字到 -> 刷新
                                Text(text = stringResource(R.string.generic_refresh))
                            } else {
                                Text(text = stringResource(R.string.terracotta_log))
                            }
                        }

                        //关闭
                        TextButton(
                            onClick = onClose
                        ) {
                            Text(text = stringResource(R.string.generic_close))
                        }
                    }
                }
            }
        }
    }
}

/**
 * 等待选择角色
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun WaitingUI(
    isInteractive: Boolean,
    onHostClick: () -> Unit,
    onGuestPositive: (roomCode: String) -> Unit,
    modifier: Modifier = Modifier,
    onShowToast: (AndroidStringText) -> Unit = {},
    scrollState: ScrollState = rememberScrollState()
) {
    var guestOperation by remember { mutableStateOf<GuestWaitingOperation>(GuestWaitingOperation.None) }

    Box(
        modifier = modifier.verticalScroll(scrollState),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            //房主
            SimpleCardButton(
                modifier = Modifier.fillMaxWidth(),
                icon = painterResource(R.drawable.ic_home_filled),
                title = stringResource(R.string.terracotta_status_waiting_host_title),
                description = stringResource(R.string.terracotta_status_waiting_host_desc),
                onClick = onHostClick,
                enabled = isInteractive
            )

            //房客
            SimpleCardButton(
                modifier = Modifier.fillMaxWidth(),
                icon = painterResource(R.drawable.ic_group_filled),
                title = stringResource(R.string.terracotta_status_waiting_guest_title),
                description = stringResource(R.string.terracotta_status_waiting_guest_desc),
                onClick = {
                    guestOperation = GuestWaitingOperation.OnClick
                },
                enabled = isInteractive
            )
        }

        //禁止交互时，提示用户正在加载中
        if (!isInteractive) {
            LoadingIndicator()
        }
    }

    GuestWaitingOperation(
        operation = guestOperation,
        onChange = { guestOperation = it },
        onPositive = onGuestPositive,
        onShowToast = onShowToast
    )
}

@Preview(showBackground = true)
@Composable
private fun WaitingUIPreview() {
    WaitingUI(
        isInteractive = true,
        onHostClick = {},
        onGuestPositive = {}
    )
}

/**
 * 房客开始中
 */
@Composable
private fun GuestStartingUI(
    modifier: Modifier = Modifier,
    difficulty: TerracottaState.GuestStarting.Difficulty,
    onBack: () -> Unit
) {
    CommonProgressLayout(
        modifier = modifier,
        progress = stringResource(R.string.terracotta_status_guest_starting),
        text = if (difficulty != TerracottaState.GuestStarting.Difficulty.UNKNOWN) (@Composable {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    painter = when (difficulty) {
                        TerracottaState.GuestStarting.Difficulty.EASIEST,
                        TerracottaState.GuestStarting.Difficulty.SIMPLE ->
                            painterResource(R.drawable.ic_info_filled)
                        else ->
                            painterResource(R.drawable.ic_warning_filled)
                    },
                    contentDescription = null
                )
                if (difficulty != TerracottaState.GuestStarting.Difficulty.UNKNOWN) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            modifier = Modifier.fillMaxWidth(),
                            text = stringResource(difficulty.textRes)
                        )
                        Text(
                            modifier = Modifier
                                .fillMaxWidth()
                                .alpha(0.7f),
                            text = stringResource(R.string.terracotta_difficulty_estimate_only),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }) else null,
        backDescription = stringResource(R.string.terracotta_status_guest_starting_back),
        onBack = onBack
    )
}

@Preview(showBackground = true)
@Composable
private fun GuestStartingUIPreview() {
    GuestStartingUI(
        difficulty = TerracottaState.GuestStarting.Difficulty.UNKNOWN,
        onBack = {}
    )
}

/**
 * 已进入房间
 */
@Composable
private fun OkRoomUI(
    modifier: Modifier = Modifier,
    code: String,
    profiles: List<TerracottaProfile>,
    onCopy: () -> Unit,
    onExit: () -> Unit,
    okText: String,
    codeLabel: String,
    copyTitle: String,
    copyDesc: String,
    backTitle: String = stringResource(R.string.terracotta_back),
    backDesc: String,
    profilesLabel: String = stringResource(R.string.terracotta_player_list)
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Column(
            modifier = Modifier.weight(1f),
        ) {
            //文字部分
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScrollWithBar(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(text = okText)
                HorizontalDivider(modifier = Modifier.fillMaxWidth())
                Text(
                    text = codeLabel,
                    style = MaterialTheme.typography.labelMedium
                )
                Text(
                    text = code,
                    style = MaterialTheme.typography.labelMedium
                )
            }
            //按钮部分
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                //复制按钮
                SimpleRowButton(
                    modifier = Modifier.fillMaxWidth(),
                    icon = painterResource(R.drawable.ic_copy_all_filled),
                    title = copyTitle,
                    description = copyDesc,
                    onClick = onCopy
                )
                //退出按钮
                SimpleRowButton(
                    modifier = Modifier.fillMaxWidth(),
                    icon = painterResource(R.drawable.ic_arrow_back),
                    title = backTitle,
                    description = backDesc,
                    onClick = onExit
                )
            }
        }

        //玩家列表
        ProfileListPanel(
            modifier = Modifier.weight(1f),
            title = profilesLabel,
            profiles = profiles
        )
    }
}

/**
 * 通用房间玩家列表
 */
@Composable
private fun ProfileListPanel(
    title: String,
    profiles: List<TerracottaProfile>,
    modifier: Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(text = title)
        HorizontalDivider()

        val scrollState = rememberLazyListState()
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .nonInteractiveScrollbar(
                    state = scrollState.scrollIndicatorState!!,
                    orientation = Orientation.Vertical,
                ),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            state = scrollState,
        ) {
            items(items = profiles, key = { it.toString() }) { profile ->
                TerracottaProfileLayout(
                    modifier = Modifier.fillMaxWidth(),
                    profile = profile
                )
            }
        }
    }
}

@Composable
private fun TerracottaProfileLayout(
    profile: TerracottaProfile,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            maxLines = 2
        ) {
            //玩家名字
            MarqueeText(text = profile.name ?: stringResource(R.string.terracotta_player_anonymous))
            //身份/类别
            Text(text = stringResource(profile.type.textRes))
        }
        MarqueeText(
            modifier = Modifier.alpha(0.7f),
            text = profile.vendor,
            style = MaterialTheme.typography.labelSmall
        )
    }
}

/**
 * 出现错误
 */
@Composable
private fun ExceptionUI(
    title: String,
    onExit: () -> Unit,
    modifier: Modifier = Modifier,
    scrollState: ScrollState = rememberScrollState()
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        //文字部分
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScrollWithBar(scrollState),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(text = title)
            HorizontalDivider(modifier = Modifier.fillMaxWidth())
            Text(
                text = stringResource(R.string.terracotta_export_log),
                style = MaterialTheme.typography.labelMedium
            )
        }
        //退出按钮
        SimpleRowButton(
            modifier = Modifier.fillMaxWidth(),
            icon = painterResource(R.drawable.ic_arrow_back),
            title = stringResource(R.string.terracotta_back),
            description = stringResource(R.string.terracotta_status_exception_back),
            onClick = onExit
        )
    }
}

/**
 * 展示日志
 */
@Composable
private fun LogUI(
    logString: String,
    onExit: () -> Unit,
    modifier: Modifier = Modifier,
    scrollState: ScrollState = rememberScrollState()
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScrollWithBar(scrollState),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(text = logString)
        }
        //退出按钮
        SimpleRowButton(
            modifier = Modifier.fillMaxWidth(),
            icon = painterResource(R.drawable.ic_arrow_back),
            title = stringResource(R.string.terracotta_back),
            description = stringResource(R.string.terracotta_log_exit),
            onClick = onExit
        )
    }
}

@Composable
private fun CommonProgressLayout(
    modifier: Modifier = Modifier,
    progress: String,
    backTitle: String = stringResource(R.string.terracotta_back),
    backDescription: String,
    onBack: () -> Unit,
    text: (@Composable ColumnScope.() -> Unit)? = null,
    scrollState: ScrollState = rememberScrollState(),
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        //文字部分
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScrollWithBar(scrollState),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) c1@{
            Text(text = progress)
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            text?.invoke(this@c1)
        }
        //退出按钮
        SimpleCardButton(
            modifier = Modifier.fillMaxWidth(),
            icon = painterResource(R.drawable.ic_arrow_left_rounded),
            title = backTitle,
            description = backDescription,
            onClick = onBack
        )
    }
}

/**
 * 用Card实现的可点击按钮
 */
@Composable
private fun SimpleCardButton(
    modifier: Modifier = Modifier,
    icon: Painter,
    title: String,
    description: String,
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    BackgroundCard(
        modifier = modifier,
        influencedByBackground = false,
        onClick = onClick,
        enabled = enabled,
        colors = CardDefaults.cardColors(
            containerColor = itemColor(false),
            contentColor = onItemColor(),
            disabledContainerColor = itemColor(false)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                painter = icon,
                contentDescription = title
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                //标题
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = title,
                    style = MaterialTheme.typography.titleMedium
                )
                //描述
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .alpha(0.7f),
                    text = description,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

/**
 * 紧凑型可点击按钮，这个按钮的[description]描述被锁定为单行显示
 */
@Composable
private fun SimpleRowButton(
    modifier: Modifier = Modifier,
    icon: Painter,
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Row(
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(all = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            modifier = Modifier.size(18.dp),
            painter = icon,
            contentDescription = title
        )

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            //标题
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = title,
                style = MaterialTheme.typography.titleSmall
            )
            //描述
            MarqueeText(
                modifier = Modifier
                    .fillMaxWidth()
                    .alpha(0.7f),
                text = description,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}