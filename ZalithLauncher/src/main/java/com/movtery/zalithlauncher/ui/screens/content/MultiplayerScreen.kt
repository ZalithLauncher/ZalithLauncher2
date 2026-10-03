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

        Column(
            modifier = Modifier
                .fillMaxSize()
                .offset { IntOffset(x = 0, y = yOffset.roundToPx()) }
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Top Row: Bento Split (Terracotta P2P on Left, Touch Controls & Gamepad Editor on Right - Mockup #7)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Card 1: Terracotta P2P Multiplayer (Left)
                MainMenu(
                    modifier = Modifier.weight(1f),
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

                // Card 2: Touch Controls & Gamepad Editor (Right)
                TouchControlsAndGamepadBentoCard(
                    modifier = Modifier.weight(1f),
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

            // Card 3: Live Game Log & Diagnostics Console (Bottom Full-Width - Mockup #7)
            LiveDiagnosticsConsoleCard(
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

            // Card 4: Terracotta P2P Host & Guest Interactive Guide
            TutorialMenu(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
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
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF1D2027),
        border = BorderStroke(1.dp, Color(0xFF2D313C))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Touch Controls & Gamepad Editor",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                    Text(
                        text = "Customize on-screen HUD buttons, opacity, gyro & controller bindings",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF9CA3AF)
                    )
                }
                ModrinthMetaPill(text = "DualSense / Xbox Ready", highlighted = true)
            }

            // Visual On-Screen Touch HUD Mini-Stage (Mockup #7)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(145.dp),
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF13161B),
                border = BorderStroke(1.dp, Color(0xFF282C36))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp)
                ) {
                    // Left D-Pad Cluster (W A S D)
                    Column(
                        modifier = Modifier.align(Alignment.CenterStart),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        HudKeyBox("W")
                        Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                            HudKeyBox("A")
                            HudKeyBox("S")
                            HudKeyBox("D")
                        }
                    }

                    // Center Crosshair + Hotbar Preview
                    Text(
                        text = "+",
                        color = Color(0xFF1BD96A),
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        modifier = Modifier.align(Alignment.Center)
                    )

                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF1E222B))
                            .border(1.dp, Color(0xFF323744), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        repeat(6) { idx ->
                            Box(
                                modifier = Modifier
                                    .size(14.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(if (idx == 0) Color(0xFF143825) else Color(0xFF14161C))
                                    .border(
                                        1.dp,
                                        if (idx == 0) Color(0xFF1BD96A) else Color(0xFF2D313B),
                                        RoundedCornerShape(3.dp)
                                    )
                            )
                        }
                    }

                    // Top Function Keys (F3, Chat,Debug)
                    Row(
                        modifier = Modifier.align(Alignment.TopCenter),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        HudKeyBox("ESC", small = true)
                        HudKeyBox("F3", small = true)
                        HudKeyBox("F5", small = true)
                        HudKeyBox("CHAT", small = true)
                    }

                    // Right Action Cluster (ATK, USE, JMP, SNK)
                    Column(
                        modifier = Modifier.align(Alignment.CenterEnd),
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                            HudActionCircle("ATK", highlighted = true)
                            HudActionCircle("USE", highlighted = false)
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                            HudActionCircle("SNK", highlighted = false)
                            HudActionCircle("JMP", highlighted = true)
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    modifier = Modifier.weight(1f),
                    onClick = onOpenControlManager,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1BD96A),
                        contentColor = Color(0xFF06210F)
                    )
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_videogame_asset_outlined),
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "Touch Layout Editor",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                OutlinedButton(
                    modifier = Modifier.weight(1f),
                    onClick = onOpenGamepadSettings,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_sports_esports_outlined),
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "Gamepad Settings",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )
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
    currentVersion: com.movtery.zalithlauncher.game.version.installed.Version?,
    onShareLog: () -> Unit,
    onOpenFileManager: () -> Unit
) {
    var levelFilter by rememberSaveable { mutableStateOf(LogLevelFilter.ALL) }
    var refreshTick by remember { mutableIntStateOf(0) }

    val logLines = remember(currentVersion, refreshTick) {
        val gameLogFile = currentVersion?.getLatestLog()?.takeIf { it.exists() }
        val fallbackLogFile = PathManager.FILE_TERRACOTTA_LOG.takeIf { it.exists() }
        val fileToRead = gameLogFile ?: fallbackLogFile
        val rawLines = runCatching {
            fileToRead?.readLines()?.takeLast(60)
        }.getOrNull()

        if (!rawLines.isNullOrEmpty()) {
            rawLines
        } else {
            val verName = currentVersion?.getVersionName() ?: "Mirai Default Profile"
            val mcVer = currentVersion?.getVersionInfo()?.minecraftVersion ?: "1.21.1"
            val rendererLabel = resolveRendererShortLabel(currentVersion)
            listOf(
                "[INFO] [MiraiLauncher/Core]: Mirai Launcher v${BuildConfig.VERSION_NAME} initialized (Modrinth Dark Theme)",
                "[INFO] [RendererPicker]: Selected instance '$verName' (MC $mcVer) -> Renderer: $rendererLabel",
                "[INFO] [JVM/Memory]: Global heap allocation set to ${AllSettings.ramAllocation.state} MB",
                "[INFO] [Terracotta/P2P]: EasyTier P2P Multiplayer subsystem standby (${if (AllSettings.enableTerracotta.state) "ENABLED" else "READY"})",
                "[WARN] [Diagnostics]: No active crash detected; live game stdout/stderr will stream here during launch."
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
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF1D2027),
        border = BorderStroke(1.dp, Color(0xFF2D313C))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Live Game Log & Diagnostics Console",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                    Text(
                        text = currentVersion?.let { "Instance log: ${it.getVersionName()}/logs/latest.log" }
                            ?: "Real-time JVM, renderer & P2P diagnostic output",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF9CA3AF)
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LogLevelFilter.entries.forEach { filter ->
                        val selected = levelFilter == filter
                        FilterChip(
                            selected = selected,
                            onClick = { levelFilter = filter },
                            label = {
                                Text(
                                    text = filter.label,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = Color(0xFF232730),
                                labelColor = Color(0xFFD1D5DB),
                                selectedContainerColor = Color(0xFF143825),
                                selectedLabelColor = Color(0xFF1BD96A)
                            )
                        )
                    }

                    IconButton(
                        onClick = { refreshTick++ },
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_refresh),
                            contentDescription = stringResource(R.string.generic_refresh),
                            tint = Color(0xFFD1D5DB),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // Terminal Output Box
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp),
                shape = RoundedCornerShape(12.dp),
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
                            line.contains("RendererPicker", true) || line.contains("LTW", true) -> Color(0xFF1BD96A)
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

            // Bottom Actions Row (Mockup #7: Share Crash Log + Open Built-in File Manager)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onShareLog,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_share_filled),
                        contentDescription = null,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "Share Crash Log",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(Modifier.width(8.dp))

                Button(
                    onClick = onOpenFileManager,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1BD96A),
                        contentColor = Color(0xFF06210F)
                    )
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_folder_filled),
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "Open Built-in File Manager",
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

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF1D2027),
        border = BorderStroke(1.dp, Color(0xFF2D313C))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Terracotta P2P Multiplayer",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                    Text(
                        text = "Host or join LAN worlds over EasyTier P2P VPN",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF9CA3AF)
                    )
                }
                ModrinthMetaPill(
                    text = if (AllSettings.enableTerracotta.state) "P2P Active" else "P2P Standby",
                    highlighted = AllSettings.enableTerracotta.state
                )
            }

            SettingsCardColumn(
                modifier = Modifier.fillMaxWidth()
            ) {
                SwitchSettingsCard(
                    modifier = Modifier.fillMaxWidth(),
                    position = CardPosition.Top,
                    unit = AllSettings.enableTerracotta,
                    title = stringResource(R.string.terracotta_enable),
                    verticalAlignment = Alignment.CenterVertically,
                    onCheckedChange = { value ->
                        if (value) {
                            when {
                                AllSettings.terracottaNoticeVer.getValue() < Terracotta.TERRACOTTA_USER_NOTICE_VERSION -> {
                                    operation = MultiplayerOperation.Notice
                                }
                                !NotificationManager.checkNotificationEnabled(context) -> {
                                    operation = MultiplayerOperation.WarningNotification
                                }
                            }
                        }
                    }
                )

                SwitchSettingsCard(
                    modifier = Modifier.fillMaxWidth(),
                    position = CardPosition.Middle,
                    unit = AllSettings.enableTerracottaNodes,
                    title = stringResource(R.string.terracotta_custom_note_list),
                    verticalAlignment = Alignment.CenterVertically,
                    enabled = AllSettings.enableTerracotta.state,
                    columnLayout = {
                        AnimatedVisibility(
                            visible = AllSettings.enableTerracotta.state && AllSettings.enableTerracottaNodes.state,
                        ) {
                            OwnOutlinedTextField(
                                modifier = Modifier.fillMaxWidth(),
                                value = AllSettings.terracottaNodes.state,
                                onValueChange = { value ->
                                    AllSettings.terracottaNodes.save(value)
                                },
                                label = {
                                    Text(text = stringResource(R.string.terracotta_custom_note_list_hint))
                                },
                                textStyle = MaterialTheme.typography.labelMedium,
                                singleLine = true,
                                shape = MaterialTheme.shapes.large,
                            )
                        }
                    }
                )

                val terracottaEnabled = AllSettings.enableTerracotta.state

                SettingsCard(
                    modifier = Modifier.fillMaxWidth(),
                    position = CardPosition.Middle,
                    title = stringResource(R.string.terracotta_export_log_share),
                    innerPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
                    onClick = onShareLogs,
                    enabled = terracottaEnabled
                )

                SettingsCard(
                    modifier = Modifier.fillMaxWidth(),
                    position = CardPosition.Bottom,
                    title = stringResource(R.string.terracotta_easytier),
                    innerPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
                    onClick = {
                        eventViewModel.sendEvent(EventViewModel.Event.OpenLink(URL_EASYTIER))
                    }
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
