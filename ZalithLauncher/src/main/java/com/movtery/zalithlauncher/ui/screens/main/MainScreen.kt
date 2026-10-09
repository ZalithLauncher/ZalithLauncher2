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

import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.movtery.zalithlauncher.BuildKeys
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.coroutine.Task
import com.movtery.zalithlauncher.coroutine.TaskStage
import com.movtery.zalithlauncher.coroutine.TaskSystem
import com.movtery.zalithlauncher.coroutine.InstallerRestoreRegistry
import com.movtery.zalithlauncher.coroutine.TitledTask
import androidx.compose.ui.text.style.TextOverflow
import com.movtery.zalithlauncher.ui.screens.content.elements.TitleTaskFlowDialog
import com.movtery.zalithlauncher.game.version.installed.Version
import com.movtery.zalithlauncher.path.URL_ORIGINAL_PROJECT
import com.movtery.zalithlauncher.game.version.installed.VersionsManager
import com.movtery.zalithlauncher.path.PathManager
import com.movtery.zalithlauncher.setting.AllSettings
import com.movtery.zalithlauncher.setting.enums.MainScreenMode
import com.movtery.zalithlauncher.ui.AndroidStringText
import com.movtery.zalithlauncher.ui.androidText
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import com.movtery.zalithlauncher.ui.components.BackgroundCard
import com.movtery.zalithlauncher.ui.components.CardTitleLayout
import com.movtery.zalithlauncher.ui.components.RadioCard
import com.movtery.zalithlauncher.ui.components.SimpleAlertDialog
import com.movtery.zalithlauncher.ui.components.TextRailItem
import com.movtery.zalithlauncher.ui.guide.sendStartGuideOnce
import com.movtery.zalithlauncher.ui.screens.BackStackNavKey
import com.movtery.zalithlauncher.ui.screens.NestedNavKey
import com.movtery.zalithlauncher.ui.screens.NormalNavKey
import com.movtery.zalithlauncher.ui.screens.TitledNavKey
import com.movtery.zalithlauncher.ui.screens.content.AccountManageScreen
import com.movtery.zalithlauncher.ui.screens.content.BuiltInFileManagerScreen
import com.movtery.zalithlauncher.ui.screens.content.DownloadScreen
import com.movtery.zalithlauncher.ui.screens.content.FileEditorScreen
import com.movtery.zalithlauncher.ui.screens.content.FileSelectorScreen
import com.movtery.zalithlauncher.ui.screens.content.LauncherScreen
import com.movtery.zalithlauncher.ui.screens.content.elements.AboutDialog
import com.movtery.zalithlauncher.ui.screens.content.LicenseScreen
import com.movtery.zalithlauncher.ui.screens.content.GameStatsScreen
import com.movtery.zalithlauncher.ui.screens.content.CapeGalleryScreen
import com.movtery.zalithlauncher.ui.screens.content.PlayTimeStatsScreen
import com.movtery.zalithlauncher.ui.screens.content.RecordingsScreen
import com.movtery.zalithlauncher.ui.screens.content.LogViewScreen
import com.movtery.zalithlauncher.ui.screens.content.GameLogScreen
import com.movtery.zalithlauncher.ui.screens.content.MultiplayerScreen
import com.movtery.zalithlauncher.ui.screens.content.StatsScreen
import com.movtery.zalithlauncher.ui.screens.content.SettingsScreen
import com.movtery.zalithlauncher.ui.screens.content.VersionExportScreen
import com.movtery.zalithlauncher.ui.screens.content.VersionSettingsScreen
import com.movtery.zalithlauncher.ui.screens.content.VersionsManageScreen
import com.movtery.zalithlauncher.ui.screens.content.WebViewScreen
import com.movtery.zalithlauncher.ui.screens.content.assetinfo.AssetInfoScreen
import com.movtery.zalithlauncher.ui.screens.content.navigateToDownload
import com.movtery.zalithlauncher.ui.screens.navigateTo
import com.movtery.zalithlauncher.ui.screens.onBack
import com.movtery.zalithlauncher.ui.screens.rememberTransitionSpec
import com.movtery.zalithlauncher.ui.theme.backgroundColor
import com.movtery.zalithlauncher.ui.theme.cardColor
import com.movtery.zalithlauncher.ui.theme.festivals.FestivalTitleText
import com.movtery.zalithlauncher.ui.theme.onBackgroundColor
import com.movtery.zalithlauncher.ui.theme.onCardColor
import com.movtery.zalithlauncher.utils.animation.getAnimateTween
import com.movtery.zalithlauncher.utils.festival.LocalFestivals
import com.movtery.zalithlauncher.utils.file.formatFileSize
import com.movtery.zalithlauncher.viewmodel.ErrorViewModel
import com.movtery.zalithlauncher.viewmodel.EventViewModel
import com.movtery.zalithlauncher.viewmodel.LocalBackgroundViewModel
import com.movtery.zalithlauncher.viewmodel.ModifyVersionViewModel
import com.movtery.zalithlauncher.viewmodel.ModpackImportViewModel
import com.movtery.zalithlauncher.viewmodel.ScreenBackStackViewModel
import com.movtery.zalithlauncher.viewmodel.sendKeepScreen
import com.movtery.zalithlauncher.viewmodel.sendToast

@Composable
fun MainScreen(
    screenBackStackModel: ScreenBackStackViewModel,
    eventViewModel: EventViewModel,
    modpackImportViewModel: ModpackImportViewModel,
    modifyVersionViewModel: ModifyVersionViewModel,
    submitError: (ErrorViewModel.ThrowableMessage) -> Unit
) {
    val tasks by TaskSystem.tasksFlow.collectAsStateWithLifecycle()

    //çæ§å½åæ¯å¦æä»»å¡æ­£å¨è¿è¡
    LaunchedEffect(tasks) {
        if (tasks.isEmpty()) {
            eventViewModel.sendKeepScreen(false)
        } else {
            //æä»»å¡æ­£å¨è¿è¡ï¼é¿åçå±
            eventViewModel.sendKeepScreen(true)
        }
    }

    val isTaskMenuExpanded = AllSettings.launcherTaskMenuExpanded.state
    val showDisclaimer = AllSettings.disclaimerAccepted.state
    val mainScreenModeSelected = AllSettings.mainScreenModeSelected.state
    val context = androidx.compose.ui.platform.LocalContext.current

    if (!showDisclaimer) {
        SimpleAlertDialog(
            title = stringResource(R.string.disclaimer_title),
            text = stringResource(R.string.disclaimer_content),
            confirmText = stringResource(R.string.generic_got_it),
            dismissText = stringResource(R.string.disclaimer_original_repo),
            onConfirm = {
                AllSettings.disclaimerAccepted.save(true)
            },
            onDismiss = {
                val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(URL_ORIGINAL_PROJECT))
                context.startActivity(intent)
            }
        )
    } else if (!mainScreenModeSelected) {
        // First-launch only: ask the user which main screen layout they prefer.
        // Non-dismissable — the user must tap a card and confirm.
        var selectedMode by remember { mutableStateOf(MainScreenMode.Default) }

        AlertDialog(
            onDismissRequest = { /* non-dismissable — user must make a choice */ },
            icon = {
                Icon(
                    painter = painterResource(R.drawable.ic_setting_launcher),
                    contentDescription = null,
                    modifier = Modifier.size(28.dp)
                )
            },
            title = {
                Text(text = stringResource(R.string.onboarding_main_screen_mode_title))
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {

                    // Default option card
                    RadioCard(
                        selected = selectedMode == MainScreenMode.Default,
                        onClick = { selectedMode = MainScreenMode.Default },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_home_filled),
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = stringResource(R.string.settings_launcher_main_screen_mode_default),
                                    style = MaterialTheme.typography.titleSmall
                                )
                            }
                            Text(
                                text = stringResource(R.string.onboarding_main_screen_mode_default_desc),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Advanced option card
                    RadioCard(
                        selected = selectedMode == MainScreenMode.Advanced,
                        onClick = { selectedMode = MainScreenMode.Advanced },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_dashboard_filled),
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = stringResource(R.string.settings_launcher_main_screen_mode_advanced),
                                    style = MaterialTheme.typography.titleSmall
                                )
                            }
                            Text(
                                text = stringResource(R.string.onboarding_main_screen_mode_advanced_desc),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Hint: can be changed later in Settings
                    Text(
                        text = stringResource(R.string.onboarding_main_screen_mode_hint),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        AllSettings.mainScreenMode.save(selectedMode)
                        AllSettings.mainScreenModeSelected.save(true)
                    }
                ) {
                    Text(stringResource(R.string.generic_confirm))
                }
            }
        )
    }

    fun changeTasksExpandedState() {
        AllSettings.launcherTaskMenuExpanded.save(!isTaskMenuExpanded)
    }

    /** åå°ä¸»é¡µé¢éç¨å½æ° */
    val toMainScreen: () -> Unit = {
        screenBackStackModel.mainScreen.clearWith(NormalNavKey.LauncherMain)
    }

    val mainScreenKey = screenBackStackModel.mainScreen.currentKey
    val inLauncherScreen = mainScreenKey == null || mainScreenKey is NormalNavKey.LauncherMain
    val launcherRightPanelCollapsed by screenBackStackModel.launcherRightPanelCollapsed.collectAsStateWithLifecycle()
    var showAboutDialog by remember { mutableStateOf(false) }

    val isBackgroundValid = LocalBackgroundViewModel.current?.isValid == true
    val launcherBackgroundOpacity = AllSettings.launcherBackgroundOpacity.state.toFloat() / 100f

    val backgroundColor = if (isBackgroundValid) {
        backgroundColor().copy(alpha = launcherBackgroundOpacity)
    } else backgroundColor()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = backgroundColor,
        contentColor = onBackgroundColor()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            TopBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp),
                mainScreenKey = mainScreenKey,
                inLauncherScreen = inLauncherScreen,
                tasksCount = tasks.size,
                isTasksExpanded = isTaskMenuExpanded,
                contentColor = onBackgroundColor(),
                onScreenBack = {
                    screenBackStackModel.mainScreen.backStack.removeFirstOrNull()
                },
                toMainScreen = toMainScreen,
                toSettingsScreen = {
                    screenBackStackModel.mainScreen.removeAndNavigateTo(
                        removes = screenBackStackModel.clearBeforeNavKeys,
                        screenKey = screenBackStackModel.settingsScreen
                    )
                },
                toDownloadScreen = {
                    screenBackStackModel.navigateToDownload()
                },
                toMultiplayerScreen = {
                    screenBackStackModel.mainScreen.removeAndNavigateTo(
                        removes = screenBackStackModel.clearBeforeNavKeys,
                        screenKey = NormalNavKey.Multiplayer
                    )
                },
                toRecordingsScreen = {
                    screenBackStackModel.mainScreen.removeAndNavigateTo(
                        removes = screenBackStackModel.clearBeforeNavKeys,
                        screenKey = NormalNavKey.Recordings
                    )
                },
                toStatsScreen = {
                    screenBackStackModel.mainScreen.removeAndNavigateTo(
                        removes = screenBackStackModel.clearBeforeNavKeys,
                        screenKey = NormalNavKey.Stats
                    )
                },
                toGameLogScreen = {
                    screenBackStackModel.mainScreen.removeAndNavigateTo(
                        removes = screenBackStackModel.clearBeforeNavKeys,
                        screenKey = NormalNavKey.GameLog
                    )
                },
                openFileManager = {
                    eventViewModel.sendEvent(
                        EventViewModel.Event.OpenFileManager(
                            rootPath = PathManager.DIR_FILES_EXTERNAL.absolutePath
                        )
                    )
                },
                changeExpandedState = {
                    changeTasksExpandedState()
                },
                onTitleClick = { showAboutDialog = true },
                launcherRightPanelCollapsed = launcherRightPanelCollapsed,
            )

            if (showAboutDialog) {
                AboutDialog(onDismissRequest = { showAboutDialog = false })
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                NavigationUI(
                    modifier = Modifier.fillMaxSize(),
                    screenBackStackModel = screenBackStackModel,
                    toMainScreen = toMainScreen,
                    eventViewModel = eventViewModel,
                    modpackImportViewModel = modpackImportViewModel,
                    modifyVersionViewModel = modifyVersionViewModel,
                    submitError = submitError
                )

                TaskMenu(
                    tasks = tasks,
                    isExpanded = isTaskMenuExpanded,
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(0.3f)
                        .align(Alignment.CenterStart)
                        .padding(all = 6.dp)
                ) {
                    changeTasksExpandedState()
                }
            }
        }
    }
}

@Composable
private fun <E: TitledNavKey> TopBar(
    mainScreenKey: E?,
    inLauncherScreen: Boolean,
    launcherRightPanelCollapsed: Boolean = false,
    tasksCount: Int,
    isTasksExpanded: Boolean,
    modifier: Modifier = Modifier,
    contentColor: Color,
    onScreenBack: () -> Unit,
    toMainScreen: () -> Unit,
    toSettingsScreen: () -> Unit,
    toDownloadScreen: () -> Unit,
    toMultiplayerScreen: () -> Unit,
    toRecordingsScreen: () -> Unit,
    toStatsScreen: () -> Unit,
    toGameLogScreen: () -> Unit,
    openFileManager: () -> Unit,
    changeExpandedState: () -> Unit,
    onTitleClick: () -> Unit = {},
) {
    val festivals = LocalFestivals.current

    val inMultiplayerScreen = mainScreenKey is NormalNavKey.Multiplayer
    val inRecordingsScreen = mainScreenKey is NormalNavKey.Recordings
    val inStatsScreen = mainScreenKey is NormalNavKey.Stats || mainScreenKey is NormalNavKey.GameStats || mainScreenKey is NormalNavKey.PlayTimeStats
    val inGameLogScreen = mainScreenKey is NormalNavKey.GameLog || mainScreenKey is NormalNavKey.LogView
    val inDownloadScreen = mainScreenKey is NestedNavKey.Download
    val inSettingsScreen = mainScreenKey is NestedNavKey.Settings

    CompositionLocalProvider(
        LocalContentColor provides contentColor
    ) {
        ConstraintLayout(modifier = modifier) {
            val (backCenter, title, endButtons) = createRefs()
            val rightPanelGuidelineOffset = if (inLauncherScreen && !launcherRightPanelCollapsed) 290.dp else 0.dp
            val contentEnd = createGuidelineFromEnd(rightPanelGuidelineOffset)

            val backDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher

            Row(
                modifier = Modifier
                    .constrainAs(backCenter) {
                        start.linkTo(parent.start)
                        top.linkTo(parent.top)
                        bottom.linkTo(parent.bottom)
                    }
                    .fillMaxHeight()
            ) {
                AnimatedVisibility(
                    visible = !inLauncherScreen
                ) {
                    Row(modifier = Modifier.fillMaxHeight()) {
                        Spacer(Modifier.width(12.dp))

                        IconButton(
                            modifier = Modifier.fillMaxHeight(),
                            onClick = {
                                if (!inLauncherScreen) {
                                    //ä¸å¨ä¸»å±å¹æ¶æåè®¸è¿å
                                    backDispatcher?.onBackPressed() ?: run {
                                        onScreenBack()
                                    }
                                }
                            }
                        ) {
                            Icon(
                                modifier = Modifier.size(24.dp),
                                painter = painterResource(R.drawable.ic_arrow_back),
                                contentDescription = stringResource(R.string.generic_back)
                            )
                        }

                        IconButton(
                            modifier = Modifier.fillMaxHeight(),
                            onClick = {
                                if (!inLauncherScreen) {
                                    //ä¸å¨ä¸»å±å¹æ¶æåè®¸åå°ä¸»é¡µé¢
                                    toMainScreen()
                                }
                            }
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_home_filled),
                                contentDescription = stringResource(R.string.generic_main_menu)
                            )
                        }
                    }
                }
            }
            val parentRes = mainScreenKey?.title
            val childRes = (mainScreenKey as? BackStackNavKey<*>)?.currentKey?.title

            Crossfade(
                modifier = Modifier.constrainAs(title) {
                    centerVerticallyTo(parent)
                    if (inLauncherScreen) {
                        start.linkTo(parent.start)
                        end.linkTo(contentEnd)
                    } else {
                        start.linkTo(backCenter.end, margin = 16.dp)
                    }
                },
                targetState = parentRes to childRes
            ) { (parent, child) ->
                val style = MaterialTheme.typography.titleMedium
                val softWarp = false
                val maxLines = 1

                if (parent == null) {
                    Column(
                        modifier = Modifier.clickable { onTitleClick() },
                        verticalArrangement = Arrangement.Center
                    ) {
                        if (festivals.isEmpty()) {
                            Text(
                                text = BuildKeys.LAUNCHER_IDENTIFIER,
                                style = style,
                                softWrap = softWarp,
                                maxLines = maxLines
                            )
                        } else {
                            FestivalTitleText(
                                festivals = festivals,
                                style = style,
                                maxLines = maxLines
                            )
                        }
                    }
                } else {
                    val titleText = if (child != null) {
                        androidText(parent, androidText(" - "), child)
                    } else {
                        parent
                    }

                    AndroidStringText(
                        text = titleText,
                        style = style,
                        softWrap = softWarp,
                        maxLines = maxLines
                    )
                }
            }

            Row(
                modifier = Modifier
                    .constrainAs(endButtons) {
                        top.linkTo(parent.top)
                        bottom.linkTo(parent.bottom)
                        end.linkTo(parent.end, margin = 12.dp)
                    },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AnimatedVisibility(
                    visible = !(isTasksExpanded || tasksCount == 0),
                    enter = slideInVertically(
                        initialOffsetY = { -50 }
                    ) + fadeIn(),
                    exit = slideOutVertically(
                        targetOffsetY = { -50 }
                    ) + fadeOut()
                ) {
                    Row(
                        modifier = Modifier
                            .clip(shape = MaterialTheme.shapes.large)
                            .clickable { changeExpandedState() }
                            .padding(all = 8.dp)
                            .width(120.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        LinearProgressIndicator(modifier = Modifier.weight(1f))
                        if (tasksCount > 1) {
                            Text(
                                text = "$tasksCount",
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                        Icon(
                            modifier = Modifier.size(22.dp),
                            painter = painterResource(R.drawable.ic_assignment_filled),
                            contentDescription = stringResource(R.string.main_task_menu)
                        )
                    }
                }

                IconButton(
                    onClick = openFileManager
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_folder_filled),
                        contentDescription = null
                    )
                }

                TopBarRailItem(
                    selected = inRecordingsScreen,
                    painter = painterResource(R.drawable.ic_videocam_filled),
                    text = stringResource(R.string.page_title_recordings),
                    onClick = {
                        if (!inRecordingsScreen) toRecordingsScreen()
                    },
                )

                TopBarRailItem(
                    selected = inMultiplayerScreen,
                    painter = painterResource(R.drawable.ic_group_filled),
                    text = stringResource(R.string.terracotta),
                    onClick = {
                        if (!inMultiplayerScreen) toMultiplayerScreen()
                    },
                )

                TopBarRailItem(
                    selected = inStatsScreen,
                    painter = painterResource(R.drawable.ic_dashboard_filled),
                    text = stringResource(R.string.page_title_stats),
                    onClick = {
                        if (!inStatsScreen) toStatsScreen()
                    },
                )

                TopBarRailItem(
                    selected = inGameLogScreen,
                    painter = painterResource(R.drawable.ic_article_outlined),
                    text = stringResource(R.string.page_title_game_log),
                    onClick = {
                        if (!inGameLogScreen) toGameLogScreen()
                    },
                )

                TopBarRailItem(
                    selected = inDownloadScreen,
                    painter = painterResource(R.drawable.ic_download_2_filled),
                    text = stringResource(R.string.generic_download),
                    onClick = {
                        if (!inDownloadScreen) toDownloadScreen()
                    },
                )

                TopBarRailItem(
                    selected = inSettingsScreen,
                    painter = painterResource(R.drawable.ic_settings_filled),
                    text = stringResource(R.string.generic_setting),
                    onClick = {
                        if (!inSettingsScreen) toSettingsScreen()
                    },
                )
            }
        }
    }
}

@Composable
private fun TopBarRailItem(
    selected: Boolean,
    painter: Painter,
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    textStyle: TextStyle = MaterialTheme.typography.labelMedium
) {
    TextRailItem(
        modifier = modifier,
        onClick = onClick,
        text = {
            AnimatedVisibility(visible = selected) {
                Row {
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = text,
                        style = textStyle
                    )
                }
            }
        },
        icon = {
            Icon(
                painter = painter,
                contentDescription = text
            )
        },
        selected = selected,
        selectedPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
        unSelectedPadding = PaddingValues(all = 8.dp),
    )
}

@Composable
private fun NavigationUI(
    modifier: Modifier = Modifier,
    screenBackStackModel: ScreenBackStackViewModel,
    toMainScreen: () -> Unit,
    eventViewModel: EventViewModel,
    modpackImportViewModel: ModpackImportViewModel,
    modifyVersionViewModel: ModifyVersionViewModel,
    submitError: (ErrorViewModel.ThrowableMessage) -> Unit
) {
    val backStack = screenBackStackModel.mainScreen.backStack
    val currentKey = backStack.lastOrNull()

    LaunchedEffect(currentKey) {
        screenBackStackModel.mainScreen.currentKey = currentKey
    }

    if (backStack.isNotEmpty()) {
        /** å¯¼èªè³çæ¬è¯¦ç»ä¿¡æ¯å±å¹ */
        val navigateToVersions: (Version) -> Unit = remember(screenBackStackModel) {
            { version ->
                screenBackStackModel.mainScreen.navigateTo(
                    screenKey = NestedNavKey.VersionSettings(version),
                    useClassEquality = true
                )
            }
        }
        /** å¯¼èªè³æ´ååå¯¼åºå±å¹ */
        val navigateToExport: (Version) -> Unit = remember(screenBackStackModel) {
            { version ->
                screenBackStackModel.mainScreen.removeAndNavigateTo(
                    remove = NestedNavKey.VersionSettings::class,
                    screenKey = NestedNavKey.VersionExport(version),
                    useClassEquality = true
                )
            }
        }

        val provider = remember(
            screenBackStackModel,
            toMainScreen,
            eventViewModel,
            modpackImportViewModel,
            submitError,
            navigateToVersions,
            navigateToExport
        ) {
            entryProvider {
                entry<NormalNavKey.LauncherMain> {
                    LauncherScreen(
                        backStackViewModel = screenBackStackModel,
                        navigateToVersions = navigateToVersions,
                        onLaunchGame = { version ->
                            eventViewModel.sendEvent(
                                EventViewModel.Event.Launch.Game(version)
                            )
                        },
                        onQuickPlayServer = { version, address ->
                            val target = (version ?: VersionsManager.currentVersion.value)?.takeIf { it.isValid() }
                            if (target != null) {
                                eventViewModel.sendEvent(
                                    EventViewModel.Event.Launch.PlayServer(target, address)
                                )
                            } else {
                                eventViewModel.sendToast(androidText(R.string.game_launch_no_version))
                                screenBackStackModel.mainScreen.navigateTo(NormalNavKey.VersionsManager)
                            }
                        },
                        onOpenLink = {
                            eventViewModel.sendEvent(EventViewModel.Event.OpenLink(it))
                        },
                        startGuideOnce = { keys ->
                            eventViewModel.sendStartGuideOnce(keys)
                        }
                    )
                }
                entry<NestedNavKey.Settings> { key ->
                    SettingsScreen(
                        key = key,
                        backStackViewModel = screenBackStackModel,
                        openLicenseScreen = { raw ->
                            backStack.navigateTo(NormalNavKey.License(raw))
                        },
                        eventViewModel = eventViewModel,
                        submitError = submitError
                    )
                }
                entry<NormalNavKey.License> { key ->
                    LicenseScreen(
                        key = key,
                        backStackViewModel = screenBackStackModel
                    )
                }
                entry<NormalNavKey.AccountManager> { key ->
                    AccountManageScreen(
                        key = key,
                        backStackViewModel = screenBackStackModel,
                        backToMainScreen = toMainScreen,
                        openLink = { url ->
                            eventViewModel.sendEvent(EventViewModel.Event.OpenLink(url))
                        },
                        eventViewModel = eventViewModel,
                        submitError = submitError
                    )
                }
                entry<NormalNavKey.WebScreen> { key ->
                    WebViewScreen(
                        key = key,
                        backStackViewModel = screenBackStackModel,
                        eventViewModel = eventViewModel
                    )
                }
                entry<NormalNavKey.VersionsManager> {
                    VersionsManageScreen(
                        backScreenViewModel = screenBackStackModel,
                        navigateToVersions = navigateToVersions,
                        navigateToExport = navigateToExport,
                        eventViewModel = eventViewModel,
                        submitError = submitError,
                        onLaunchGame = { version ->
                            eventViewModel.sendEvent(
                                EventViewModel.Event.Launch.Game(version)
                            )
                        }
                    )
                }
                entry<NormalNavKey.FileSelector> { key ->
                    FileSelectorScreen(
                        key = key,
                        backScreenViewModel = screenBackStackModel
                    ) {
                        backStack.removeLastOrNull()
                    }
                }
                entry<NestedNavKey.VersionSettings> { key ->
                    VersionSettingsScreen(
                        key = key,
                        modifyViewModel = modifyVersionViewModel,
                        backScreenViewModel = screenBackStackModel,
                        backToMainScreen = toMainScreen,
                        onExportModpack = {
                            navigateToExport(key.version)
                        },
                        eventViewModel = eventViewModel,
                        submitError = submitError
                    )
                }
                entry<NestedNavKey.VersionExport> { key ->
                    VersionExportScreen(
                        key = key,
                        backScreenViewModel = screenBackStackModel,
                        eventViewModel = eventViewModel,
                        backToMainScreen = toMainScreen
                    )
                }
                entry<NestedNavKey.Download> { key ->
                    DownloadScreen(
                        key = key,
                        backScreenViewModel = screenBackStackModel,
                        eventViewModel = eventViewModel,
                        modpackImportViewModel = modpackImportViewModel,
                        submitError = submitError
                    )
                }
                entry<NestedNavKey.AssetInfo> { key ->
                    AssetInfoScreen(
                        key = key,
                        mainScreenKey = screenBackStackModel.mainScreen.currentKey,
                        assetInfoScreenKey = key.currentKey,
                        eventViewModel = eventViewModel,
                        submitError = submitError,
                    )
                }
                entry<NormalNavKey.Multiplayer> {
                    MultiplayerScreen(
                        backScreenViewModel = screenBackStackModel,
                        eventViewModel = eventViewModel
                    )
                }
                entry<NormalNavKey.BuiltInFileManager> { key ->
                    BuiltInFileManagerScreen(
                        key = key,
                        backStackViewModel = screenBackStackModel,
                        submitError = submitError,
                        navigateToEditor = { path ->
                            backStack.navigateTo(NormalNavKey.FileEditor(filePath = path))
                        }
                    )
                }
                entry<NormalNavKey.FileEditor> { key ->
                    FileEditorScreen(
                        key = key,
                        backStackViewModel = screenBackStackModel,
                        submitError = submitError
                    )
                }
                entry<NormalNavKey.LogView> { key ->
                    LogViewScreen(
                        key = key,
                        backStackViewModel = screenBackStackModel,
                    )
                }
                entry<NormalNavKey.GameLog> {
                    GameLogScreen(
                        backStackViewModel = screenBackStackModel,
                    )
                }
                entry<NormalNavKey.Stats> {
                    StatsScreen(
                        backStackViewModel = screenBackStackModel,
                    )
                }
                entry<NormalNavKey.GameStats> {
                    GameStatsScreen(
                        backStackViewModel = screenBackStackModel,
                    )
                }
                entry<NormalNavKey.PlayTimeStats> {
                    PlayTimeStatsScreen(
                        backStackViewModel = screenBackStackModel,
                    )
                }
                entry<NormalNavKey.CapeGallery> { key ->
                    CapeGalleryScreen(
                        key = key,
                        backStackViewModel = screenBackStackModel,
                    )
                }
                entry<NormalNavKey.Recordings> {
                    RecordingsScreen(
                        backStackViewModel = screenBackStackModel,
                    )
                }

            }
        }

        NavDisplay(
            backStack = backStack,
            modifier = modifier,
            onBack = {
                onBack(backStack)
            },
            transitionSpec = rememberTransitionSpec(),
            popTransitionSpec = rememberTransitionSpec(),
            entryProvider = provider
        )
    } else {
        Box(modifier)
    }
}

@Composable
private fun TaskMenu(
    tasks: List<Task>,
    isExpanded: Boolean,
    modifier: Modifier = Modifier,
    changeExpandedState: () -> Unit = {}
) {
    var restoredEntry by remember { mutableStateOf<InstallerRestoreRegistry.RestorableInstaller?>(null) }

    // Restore dialog: shown when user taps a minimized installer task
    restoredEntry?.let { entry ->
        val restoredTasks = entry.tasksFlow.collectAsStateWithLifecycle()
        if (restoredTasks.value.isNotEmpty()) {
            TitleTaskFlowDialog(
                title = entry.title,
                tasks = restoredTasks.value,
                onCancel = {
                    entry.onCancel()
                    restoredEntry = null
                },
                onMinimize = {
                    // Re-minimize: just dismiss the restored overlay
                    restoredEntry = null
                }
            )
        } else {
            // Tasks finished while dialog was open — dismiss cleanly
            restoredEntry = null
        }
    }
    val show = isExpanded && tasks.isNotEmpty()

    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    AnimatedVisibility(
        modifier = modifier,
        enter = slideInHorizontally(
            initialOffsetX = { if (isRtl) it else -it },
            animationSpec = getAnimateTween()
        ) + fadeIn(),
        exit = slideOutHorizontally(
            targetOffsetX = { if (isRtl) it else -it },
            animationSpec = getAnimateTween()
        ) + fadeOut(),
        visible = show
    ) {
        BackgroundCard(
            modifier = Modifier
                .fillMaxSize()
                .padding(all = 6.dp),
            influencedByBackground = false,
            shape = MaterialTheme.shapes.extraLarge,
            colors = CardDefaults.cardColors(
                containerColor = backgroundColor(),
                contentColor = onBackgroundColor()
            ),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 6.dp)
        ) {
            Column {
                CardTitleLayout(blur = 0) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp)
                            .padding(top = 8.dp, bottom = 4.dp)
                    ) {
                        IconButton(
                            modifier = Modifier
                                .size(28.dp)
                                .align(Alignment.CenterStart),
                            onClick = changeExpandedState
                        ) {
                            Icon(
                                modifier = Modifier.size(28.dp),
                                painter = painterResource(R.drawable.ic_arrow_left_rounded),
                                contentDescription = stringResource(R.string.generic_collapse)
                            )
                        }

                        Text(
                            modifier = Modifier.align(Alignment.Center),
                            text = stringResource(R.string.main_task_menu)
                        )
                    }
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(1f),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    items(
                        items = tasks,
                        key = { it.id },
                        contentType = { "task" }
                    ) { task ->
                        val canRestore = InstallerRestoreRegistry.hasEntry(task.id)
                        TaskItem(
                            task = task,
                            onTaskClick = if (canRestore) {
                                { restoredEntry = InstallerRestoreRegistry.getEntry(task.id) }
                            } else null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                        ) {
                            TaskSystem.cancelTask(task.id)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TaskItem(
    task: Task,
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.large,
    color: Color = cardColor(false),
    contentColor: Color = onCardColor(),
    onTaskClick: (() -> Unit)? = null,
    onCancelClick: () -> Unit = {}
) {
    val taskStage by task.stage.collectAsStateWithLifecycle()
    val taskMessage by task.message.collectAsStateWithLifecycle()
    val taskProgress by task.progress.collectAsStateWithLifecycle()
    val rateBytesPerSec by task.rateBytesPerSec.collectAsStateWithLifecycle()

    val stateIcon = when (taskStage) {
        TaskStage.PREPARING -> R.drawable.ic_schedule_outlined
        TaskStage.RUNNING   -> task.runningIcon ?: R.drawable.ic_download
        TaskStage.COMPLETED -> R.drawable.ic_check
    }

    Surface(
        modifier = modifier,
        shape = shape,
        color = color,
        contentColor = contentColor,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Task-state icon
            Icon(
                modifier = Modifier
                    .padding(top = 2.dp)
                    .size(18.dp),
                painter = painterResource(stateIcon),
                contentDescription = null
            )

            // Main content
            Column(modifier = Modifier.weight(1f)) {

                // Title row with action buttons
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    task.title?.let { title ->
                        Text(
                            modifier = Modifier.weight(1f),
                            text = title,
                            style = MaterialTheme.typography.labelLarge,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    } ?: Spacer(modifier = Modifier.weight(1f))

                    if (onTaskClick != null) {
                        IconButton(
                            modifier = Modifier.size(22.dp),
                            onClick = onTaskClick
                        ) {
                            Icon(
                                modifier = Modifier.size(18.dp),
                                painter = painterResource(R.drawable.ic_arrow_drop_up_rounded),
                                contentDescription = stringResource(R.string.generic_expand)
                            )
                        }
                    }

                    IconButton(
                        modifier = Modifier.size(22.dp),
                        onClick = onCancelClick
                    ) {
                        Icon(
                            modifier = Modifier.size(18.dp),
                            painter = painterResource(R.drawable.ic_close),
                            contentDescription = stringResource(R.string.generic_cancel)
                        )
                    }
                }

                // Status message
                taskMessage?.let { message ->
                    AndroidStringText(
                        modifier = Modifier
                            .padding(top = 2.dp)
                            .alpha(0.75f),
                        text = message,
                        style = MaterialTheme.typography.labelSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Progress bar
                if (taskProgress < 0) {
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp)
                    )
                } else {
                    LinearProgressIndicator(
                        progress = { taskProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp)
                    )
                }

                // Percentage + download speed
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    taskProgress.takeIf { it >= 0f }?.let {
                        Text(
                            text = "${(it * 100).toInt()}%",
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                    rateBytesPerSec?.let { bytes ->
                        val text = remember(bytes) { "${formatFileSize(bytes)}/s" }
                        Text(
                            text = text,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }
        }
    }
}
