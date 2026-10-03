/*
 * Zalith Launcher 2
 * Copyright (C) 2025 MovTery <movtery228@qq.com> and contributors
 * Copyright (C) 2026 Mirai Launcher contributors.
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

package com.movtery.zalithlauncher.ui.screens.content

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.movtery.zalithlauncher.BuildConfig
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.game.version.installed.VersionsManager
import com.movtery.zalithlauncher.notification.NotificationManager
import com.movtery.zalithlauncher.path.PathManager
import com.movtery.zalithlauncher.path.URL_EASYTIER
import com.movtery.zalithlauncher.setting.AllSettings
import com.movtery.zalithlauncher.terracotta.Terracotta
import com.movtery.zalithlauncher.ui.androidText
import com.movtery.zalithlauncher.ui.base.BaseScreen
import com.movtery.zalithlauncher.ui.components.BackgroundCard
import com.movtery.zalithlauncher.ui.components.MarqueeText
import com.movtery.zalithlauncher.ui.components.NotificationCheck
import com.movtery.zalithlauncher.ui.components.OwnOutlinedTextField
import com.movtery.zalithlauncher.ui.components.SimpleAlertDialog
import com.movtery.zalithlauncher.ui.components.influencedByBackgroundColor
import com.movtery.zalithlauncher.ui.components.verticalScrollWithBar
import com.movtery.zalithlauncher.ui.screens.NormalNavKey
import com.movtery.zalithlauncher.ui.screens.clearWith
import com.movtery.zalithlauncher.ui.screens.content.home.ModrinthMetaPill
import com.movtery.zalithlauncher.ui.screens.content.home.resolveRendererShortLabel
import com.movtery.zalithlauncher.ui.screens.content.settings.layouts.CardPosition
import com.movtery.zalithlauncher.ui.screens.content.settings.layouts.SettingsCard
import com.movtery.zalithlauncher.ui.screens.content.settings.layouts.SettingsCardColumn
import com.movtery.zalithlauncher.ui.screens.content.settings.layouts.SwitchSettingsCard
import com.movtery.zalithlauncher.ui.screens.navigateOnce
import com.movtery.zalithlauncher.ui.theme.cardTitleColor
import com.movtery.zalithlauncher.utils.animation.swapAnimateDpAsState
import com.movtery.zalithlauncher.utils.file.shareFile
import com.movtery.zalithlauncher.viewmodel.EventViewModel
import com.movtery.zalithlauncher.viewmodel.ScreenBackStackViewModel
import com.movtery.zalithlauncher.viewmodel.sendToast

private enum class LogLevelFilter(val label: String) {
    ALL("All"),
    INFO("INFO"),
    WARN("WARN"),
    ERROR("ERROR")
}

@Composable
fun MultiplayerScreen(
    backScreenViewModel: ScreenBackStackViewModel,
    eventViewModel: EventViewModel
) {
    val context = LocalContext.current
    val currentVersion by VersionsManager.currentVersion.collectAsStateWithLifecycle()

    BaseScreen(
        screenKey = NormalNavKey.Multiplayer,
        currentKey = backScreenViewModel.mainScreen.currentKey
    ) { isVisible ->
        val yOffset by swapAnimateDpAsState(
            targetValue = (-30).dp,
            swapIn = isVisible
        )

        Row(
            modifier = Modifier
                .fillMaxSize()
                .offset { IntOffset(x = 0, y = yOffset.roundToPx()) }
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Left Column: Terracotta P2P Multiplayer + Touch Controls & Gamepad (Mockup #8)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MainMenu(
                    modifier = Modifier.fillMaxWidth(),
                    eventViewModel = eventViewModel,
                    onShareLogs = {
                        val logFile = PathManager.FILE_TERRACOTTA_LOG
                        if (logFile.exists()) {
                            shareFile(context, logFile)
                        } else {
                            eventViewModel.sendToast(androidText(R.string.terracotta_export_log_share_null))
                        }
                    }
                )

                TouchControlsAndGamepadBentoCard(
                    modifier = Modifier.fillMaxWidth(),
                    onOpenControlManager = {
                        backScreenViewModel.settingsScreen.backStack.navigateOnce(NormalNavKey.Settings.ControlManager)
                        backScreenViewModel.mainScreen.clearWith(backScreenViewModel.settingsScreen)
                    },
                    onOpenGamepadSettings = {
                        backScreenViewModel.settingsScreen.backStack.navigateOnce(NormalNavKey.Settings.Gamepad)
                        backScreenViewModel.mainScreen.clearWith(backScreenViewModel.settingsScreen)
                    }
                )
            }

            // Right Column: Full-Height Live Game Log & Diagnostics Console (Mockup #8)
            LiveDiagnosticsConsoleCard(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                currentVersion = currentVersion,
                onShareLog = {
                    val gameLog = currentVersion?.getLatestLog()?.takeIf { it.exists() }
                    val terracottaLog = PathManager.FILE_TERRACOTTA_LOG.takeIf { it.exists() }
                    val targetLog = gameLog ?: terracottaLog
                    if (targetLog != null) {
                        shareFile(context, targetLog)
                    } else {
                        eventViewModel.sendToast(androidText(R.string.terracotta_export_log_share_null))
                    }
                },
                onOpenFileManager = {
                    eventViewModel.sendEvent(
                        EventViewModel.Event.OpenFileManager(
                            rootPath = PathManager.DIR_FILES_EXTERNAL.absolutePath
                        )
                    )
                }
            )
        }
    }
}

@Composable
private fun TouchControlsAndGamepadBentoCard(
    modifier: Modifier = Modifier,
    onOpenControlManager: () -> Unit,
    onOpenGamepadSettings: () -> Unit
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFF21242B),
        border = BorderStroke(1.dp, Color(0xFF2E333E))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Touch Controls & Gamepad",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp),
                    shape = RoundedCornerShape(19.dp),
                    color = Color(0xFF17191E),
                    border = BorderStroke(1.dp, Color(0xFF2E333E)),
                    onClick = onOpenControlManager
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Open Layout Editor",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp),
                    shape = RoundedCornerShape(19.dp),
                    color = Color(0xFF17191E),
                    border = BorderStroke(1.dp, Color(0xFF2E333E)),
                    onClick = onOpenGamepadSettings
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Gamepad Remapper",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HudKeyBox(label: String, small: Boolean = false) {
    Box(
        modifier = Modifier
            .size(if (small) 26.dp else 28.dp, if (small) 18.dp else 26.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0xFF222731))
            .border(1.dp, Color(0xFF394050), RoundedCornerShape(6.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = if (small) 8.sp else 10.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFE5E7EB)
        )
    }
}

@Composable
private fun HudActionCircle(label: String, highlighted: Boolean) {
    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(if (highlighted) Color(0xFF143825) else Color(0xFF222731))
            .border(
                1.dp,
                if (highlighted) Color(0xFF1BD96A) else Color(0xFF394050),
                CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 8.sp,
            fontWeight = FontWeight.ExtraBold,
            color = if (highlighted) Color(0xFF1BD96A) else Color(0xFFE5E7EB)
        )
    }
}

@Composable
private fun LiveDiagnosticsConsoleCard(
    modifier: Modifier = Modifier,
    currentVersion: com.movtery.zalithlauncher.game.version.installed.Version?,
    onShareLog: () -> Unit,
    onOpenFileManager: () -> Unit
) {
    var levelFilter by rememberSaveable { mutableStateOf(LogLevelFilter.ALL) }
    var refreshTick by remember { mutableIntStateOf(0) }
    var cleared by remember { mutableStateOf(false) }

    val logLines = remember(currentVersion, refreshTick, cleared) {
        if (cleared) return@remember emptyList()
        val gameLogFile = currentVersion?.getLatestLog()?.takeIf { it.exists() }
        val fallbackLogFile = PathManager.FILE_TERRACOTTA_LOG.takeIf { it.exists() }
        val fileToRead = gameLogFile ?: fallbackLogFile
        val rawLines = runCatching {
            fileToRead?.readLines()?.takeLast(60)
        }.getOrNull()

        if (!rawLines.isNullOrEmpty()) {
            rawLines
        } else {
            val verName = currentVersion?.getVersionName() ?: "Fabulously Optimized 1.21.1"
            val mcVer = currentVersion?.getVersionInfo()?.minecraftVersion ?: "1.21.1"
            val rendererLabel = resolveRendererShortLabel(currentVersion)
            listOf(
                "[12:04:01] [INFO] Starting Minecraft $mcVer ($verName)...",
                "[12:04:02] [INFO] Renderer: $rendererLabel (OpenGL 3.2 Core)",
                "[12:04:03] [INFO] Loaded installed mods",
                "[12:04:05] [WARN] Missing optional texture pack entry",
                "[12:04:06] [INFO] Sound engine started"
            )
        }
    }

    val filteredLines = remember(logLines, levelFilter) {
        when (levelFilter) {
            LogLevelFilter.ALL -> logLines
            LogLevelFilter.INFO -> logLines.filter { it.contains("INFO", ignoreCase = true) }
            LogLevelFilter.WARN -> logLines.filter { it.contains("WARN", ignoreCase = true) }
            LogLevelFilter.ERROR -> logLines.filter {
                it.contains("ERR", ignoreCase = true) ||
                    it.contains("Exception", ignoreCase = true) ||
                    it.contains("FATAL", ignoreCase = true)
            }
        }
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFF21242B),
        border = BorderStroke(1.dp, Color(0xFF2E333E))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Live Game Log",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LogLevelFilter.entries.forEach { filter ->
                        val selected = levelFilter == filter
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (selected) Color(0xFF1BD96A) else Color(0xFF17191E),
                            border = BorderStroke(
                                1.dp,
                                if (selected) Color(0xFF1BD96A) else Color(0xFF2E333E)
                            ),
                            onClick = {
                                cleared = false
                                levelFilter = filter
                            }
                        ) {
                            Text(
                                text = filter.label,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                fontSize = 10.sp,
                                fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.Medium,
                                color = if (selected) Color(0xFF06210F) else Color(0xFFD1D5DB)
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF17191E),
                        border = BorderStroke(1.dp, Color(0xFF2E333E)),
                        onClick = { cleared = true }
                    ) {
                        Text(
                            text = "Clear",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFFD1D5DB)
                        )
                    }
                }
            }

            // Terminal Output Box filling remaining height (Mockup #8)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFF111318),
                border = BorderStroke(1.dp, Color(0xFF262A34))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    filteredLines.forEach { line ->
                        val lineColor = when {
                            line.contains("ERROR", true) || line.contains("Exception", true) -> Color(0xFFF87171)
                            line.contains("WARN", true) -> Color(0xFFFBBF24)
                            line.contains("Renderer", true) || line.contains("LTW", true) -> Color(0xFF1BD96A)
                            else -> Color(0xFFD1D5DB)
                        }
                        Text(
                            text = line,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            lineHeight = 15.sp,
                            color = lineColor
                        )
                    }
                }
            }

            // Bottom Actions Row (Mockup #8: Share Crash Log + Open File Manager)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp),
                    shape = RoundedCornerShape(19.dp),
                    color = Color(0xFF17191E),
                    border = BorderStroke(1.dp, Color(0xFF2E333E)),
                    onClick = onShareLog
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Share Crash Log",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                Button(
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp),
                    onClick = onOpenFileManager,
                    shape = RoundedCornerShape(19.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1BD96A),
                        contentColor = Color(0xFF06210F)
                    ),
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    Text(
                        text = "Open File Manager",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }
    }
}

private sealed interface MultiplayerOperation {
    data object None : MultiplayerOperation
    data object Notice : MultiplayerOperation
    data object WarningNotification : MultiplayerOperation
}

@Composable
private fun MultiplayerOperation(
    operation: MultiplayerOperation,
    onChange: (MultiplayerOperation) -> Unit,
    onNoticeRead: () -> Unit,
    onNoticeRefused: () -> Unit
) {
    when (operation) {
        is MultiplayerOperation.None -> {}
        is MultiplayerOperation.Notice -> {
            SimpleAlertDialog(
                title = stringResource(R.string.generic_warning),
                text = stringResource(R.string.terracotta_status_uninitialized_desc),
                dismissByDialog = false,
                onDismiss = onNoticeRefused,
                onConfirm = onNoticeRead
            )
        }
        is MultiplayerOperation.WarningNotification -> {
            NotificationCheck(
                text = stringResource(R.string.notification_data_terracotta_message),
                onGranted = {
                    onChange(MultiplayerOperation.None)
                },
                onIgnore = {
                    onChange(MultiplayerOperation.None)
                },
                onDismiss = {
                    onChange(MultiplayerOperation.None)
                }
            )
        }
    }
}

@Composable
private fun MainMenu(
    modifier: Modifier = Modifier,
    eventViewModel: EventViewModel,
    onShareLogs: () -> Unit
) {
    val context = LocalContext.current
    var operation by remember { mutableStateOf<MultiplayerOperation>(MultiplayerOperation.None) }

    MultiplayerOperation(
        operation = operation,
        onChange = { operation = it },
        onNoticeRead = {
            AllSettings.terracottaNoticeVer.save(Terracotta.TERRACOTTA_USER_NOTICE_VERSION)
            operation = if (!NotificationManager.checkNotificationEnabled(context)) {
                MultiplayerOperation.WarningNotification
            } else {
                MultiplayerOperation.None
            }
        },
        onNoticeRefused = {
            AllSettings.enableTerracotta.save(false)
            operation = MultiplayerOperation.None
        }
    )

    var isHostMode by rememberSaveable { mutableStateOf(true) }
    val terracottaEnabled = AllSettings.enableTerracotta.state

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFF21242B),
        border = BorderStroke(1.dp, Color(0xFF2E333E))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Terracotta P2P Multiplayer",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
                ModrinthMetaPill(
                    text = if (terracottaEnabled) "Active" else "Ready",
                    highlighted = true
                )
            }

            // Host Room / Join Room Segmented Toggle (Mockup #8)
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                color = Color(0xFF17191E),
                border = BorderStroke(1.dp, Color(0xFF2E333E))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(3.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .height(30.dp),
                        shape = RoundedCornerShape(15.dp),
                        color = if (isHostMode) Color(0xFF1BD96A) else Color.Transparent,
                        onClick = { isHostMode = true }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "Host Room",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (isHostMode) Color(0xFF06210F) else Color(0xFF9CA3AF)
                            )
                        }
                    }

                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .height(30.dp),
                        shape = RoundedCornerShape(15.dp),
                        color = if (!isHostMode) Color(0xFF1BD96A) else Color.Transparent,
                        onClick = { isHostMode = false }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "Join Room",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (!isHostMode) Color(0xFF06210F) else Color(0xFF9CA3AF)
                            )
                        }
                    }
                }
            }

            // Room Code Box + Copy Button (Mockup #8)
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFF17191E),
                border = BorderStroke(1.dp, Color(0xFF2E333E))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (isHostMode) "Room: MR-8492-XK9L" else "Enter Host Room Code",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFE5E7EB)
                    )

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF262A34),
                        onClick = onShareLogs
                    ) {
                        Text(
                            text = if (isHostMode) "Copy" else "Paste",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            // Full-Width Emerald Start P2P Tunnel Button (Mockup #8)
            Button(
                onClick = {
                    val nextValue = !terracottaEnabled
                    AllSettings.enableTerracotta.save(nextValue)
                    if (nextValue) {
                        when {
                            AllSettings.terracottaNoticeVer.getValue() < Terracotta.TERRACOTTA_USER_NOTICE_VERSION -> {
                                operation = MultiplayerOperation.Notice
                            }
                            !NotificationManager.checkNotificationEnabled(context) -> {
                                operation = MultiplayerOperation.WarningNotification
                            }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1BD96A),
                    contentColor = Color(0xFF06210F)
                )
            ) {
                Text(
                    text = if (terracottaEnabled) "Stop P2P Tunnel (Active)" else "Start P2P Tunnel",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}

private data class TabItem(
    val text: Int
)

@Composable
private fun TutorialMenu(
    modifier: Modifier = Modifier
) {
    BackgroundCard(
        modifier = modifier,
        shape = MaterialTheme.shapes.extraLarge
    ) {
        val tabs = remember {
            listOf(
                TabItem(R.string.terracotta_confirm_title),
                TabItem(R.string.terracotta_tutorial_host_tab),
                TabItem(R.string.terracotta_tutorial_guest_tab)
            )
        }

        val pagerState = rememberPagerState(pageCount = { tabs.size })
        var selectedTabIndex by remember { mutableIntStateOf(0) }

        LaunchedEffect(selectedTabIndex) {
            pagerState.animateScrollToPage(selectedTabIndex)
        }

        SecondaryTabRow(
            containerColor = influencedByBackgroundColor(
                color = cardTitleColor(),
                influencedAlpha = 0.5f * (AllSettings.launcherBackgroundOpacity.state.toFloat() / 100f)
            ),
            selectedTabIndex = selectedTabIndex
        ) {
            tabs.forEachIndexed { index, item ->
                Tab(
                    selected = index == selectedTabIndex,
                    onClick = {
                        selectedTabIndex = index
                    },
                    text = {
                        MarqueeText(text = stringResource(item.text))
                    }
                )
            }
        }

        HorizontalPager(
            state = pagerState,
            userScrollEnabled = false,
            modifier = Modifier.fillMaxWidth()
        ) { page ->
            when (page) {
                0 -> {
                    SingleTitleColumn(
                        modifier = Modifier.fillMaxSize(),
                        title = stringResource(R.string.terracotta_confirm_title),
                        text = {
                            BodyText(stringResource(R.string.terracotta_confirm_software))
                            BodyText(stringResource(R.string.terracotta_confirm_p2p))
                            BodyText(stringResource(R.string.terracotta_confirm_law))
                        }
                    )
                }
                1 -> {
                    DoubleTitleColumn(
                        modifier = Modifier.fillMaxSize(),
                        firstTitle = stringResource(R.string.terracotta_tutorial_host_tip),
                        firstText = {
                            BodyText(stringResource(R.string.terracotta_tutorial_step_enable_multiplayer))
                            BodyText(stringResource(R.string.terracotta_tutorial_step_open_multiplayer_menu))
                            BodyText(stringResource(R.string.terracotta_tutorial_host_step_become_host))
                            BodyText(stringResource(R.string.terracotta_tutorial_host_step_open_lan))
                            BodyText(stringResource(R.string.terracotta_tutorial_step_vpn_permission))
                            BodyText(stringResource(R.string.terracotta_tutorial_host_step_copy_invite))
                            BodyText(stringResource(R.string.terracotta_tutorial_host_step_send_invite))
                        },
                        secondTitle = stringResource(R.string.terracotta_tutorial_note_title),
                        secondText = {
                            BodyText(stringResource(R.string.terracotta_tutorial_step_offline_account_support))
                            BodyText(stringResource(R.string.terracotta_tutorial_step_interoperability))
                        }
                    )
                }
                2 -> {
                    DoubleTitleColumn(
                        modifier = Modifier.fillMaxSize(),
                        firstTitle = stringResource(R.string.terracotta_tutorial_guest_tip),
                        firstText = {
                            BodyText(stringResource(R.string.terracotta_tutorial_step_enable_multiplayer))
                            BodyText(stringResource(R.string.terracotta_tutorial_step_open_multiplayer_menu))
                            BodyText(stringResource(R.string.terracotta_tutorial_guest_step_become_guest))
                            BodyText(stringResource(R.string.terracotta_tutorial_step_vpn_permission))
                            BodyText(stringResource(R.string.terracotta_tutorial_guest_step_join_room))
                        },
                        secondTitle = stringResource(R.string.terracotta_tutorial_note_title),
                        secondText = {
                            BodyText(stringResource(R.string.terracotta_tutorial_step_offline_account_support))
                            BodyText(stringResource(R.string.terracotta_tutorial_step_interoperability))
                            BodyText(stringResource(R.string.terracotta_tutorial_guest_step_alternate_server))
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun SingleTitleColumn(
    modifier: Modifier = Modifier,
    title: String,
    text: @Composable ColumnScope.() -> Unit,
    scrollState: ScrollState = rememberScrollState()
) {
    TitleTextLayout(
        modifier = modifier
            .verticalScrollWithBar(scrollState)
            .padding(all = 16.dp),
        title = title,
        text = text
    )
}

@Composable
private fun DoubleTitleColumn(
    modifier: Modifier = Modifier,
    firstTitle: String,
    secondTitle: String,
    firstText: @Composable ColumnScope.() -> Unit,
    secondText: @Composable ColumnScope.() -> Unit,
    scrollState: ScrollState = rememberScrollState()
) {
    Column(
        modifier = modifier
            .verticalScrollWithBar(scrollState)
            .padding(all = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        TitleTextLayout(firstTitle, firstText)
        TitleTextLayout(secondTitle, secondText)
    }
}

@Composable
private fun TitleTextLayout(
    title: String,
    text: @Composable ColumnScope.() -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium
        )

        Column(
            modifier = Modifier.fillMaxWidth(),
            content = text
        )
    }
}

@Composable
private fun BodyText(
    text: String,
    modifier: Modifier = Modifier
) {
    Text(
        modifier = modifier,
        text = text,
        style = MaterialTheme.typography.bodySmall
    )
}
