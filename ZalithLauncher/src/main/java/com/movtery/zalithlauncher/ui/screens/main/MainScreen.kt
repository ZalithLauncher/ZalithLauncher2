package com.movtery.zalithlauncher.ui.screens.main

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.coroutine.Task
import com.movtery.zalithlauncher.coroutine.TaskSystem
import com.movtery.zalithlauncher.game.version.installed.Version
import com.movtery.zalithlauncher.path.PathManager
import com.movtery.zalithlauncher.setting.AllSettings
import com.movtery.zalithlauncher.ui.AndroidStringText
import com.movtery.zalithlauncher.ui.components.BackgroundCard
import com.movtery.zalithlauncher.ui.components.CardTitleLayout
import com.movtery.zalithlauncher.ui.screens.NestedNavKey
import com.movtery.zalithlauncher.ui.screens.NormalNavKey
import com.movtery.zalithlauncher.ui.screens.TitledNavKey
import com.movtery.zalithlauncher.ui.screens.content.AccountManageScreen
import com.movtery.zalithlauncher.ui.screens.content.FirstLoginMenu
import com.movtery.zalithlauncher.ui.screens.content.DownloadScreen
import com.movtery.zalithlauncher.ui.screens.content.FileSelectorScreen
import com.movtery.zalithlauncher.ui.screens.content.LauncherScreen
import com.movtery.zalithlauncher.ui.screens.content.LicenseScreen
import com.movtery.zalithlauncher.ui.screens.content.LogViewScreen
import com.movtery.zalithlauncher.ui.screens.content.MultiplayerScreen
import com.movtery.zalithlauncher.ui.screens.content.SettingsScreen
import com.movtery.zalithlauncher.ui.screens.content.VersionExportScreen
import com.movtery.zalithlauncher.ui.screens.content.VersionSettingsScreen
import com.movtery.zalithlauncher.ui.screens.content.VersionsManageScreen
import com.movtery.zalithlauncher.ui.screens.content.WebViewScreen
import com.movtery.zalithlauncher.ui.screens.content.assetinfo.AssetInfoScreen
import com.movtery.zalithlauncher.ui.screens.content.home.MiraiDiscoverPage
import com.movtery.zalithlauncher.ui.screens.content.home.MiraiLibraryPage
import com.movtery.zalithlauncher.ui.screens.content.home.MiraiServersPage
import com.movtery.zalithlauncher.ui.screens.content.home.MiraiSettingsPage
import com.movtery.zalithlauncher.ui.screens.navigateTo
import com.movtery.zalithlauncher.ui.screens.onBack
import com.movtery.zalithlauncher.ui.screens.rememberTransitionSpec
import com.movtery.zalithlauncher.ui.theme.backgroundColor
import com.movtery.zalithlauncher.ui.theme.cardColor
import com.movtery.zalithlauncher.ui.theme.onBackgroundColor
import com.movtery.zalithlauncher.ui.theme.onCardColor
import com.movtery.zalithlauncher.utils.animation.getAnimateTween
import com.movtery.zalithlauncher.utils.file.formatFileSize
import com.movtery.zalithlauncher.viewmodel.ErrorViewModel
import com.movtery.zalithlauncher.viewmodel.EventViewModel
import com.movtery.zalithlauncher.viewmodel.ModpackImportViewModel
import com.movtery.zalithlauncher.viewmodel.ScreenBackStackViewModel
import com.movtery.zalithlauncher.viewmodel.sendKeepScreen

@Composable
fun MainScreen(
    screenBackStackModel: ScreenBackStackViewModel,
    eventViewModel: EventViewModel,
    modpackImportViewModel: ModpackImportViewModel,
    submitError: (ErrorViewModel.ThrowableMessage) -> Unit
) {
    val tasks by TaskSystem.tasksFlow.collectAsStateWithLifecycle()
    LaunchedEffect(tasks) { eventViewModel.sendKeepScreen(tasks.isNotEmpty()) }
    val isTaskMenuExpanded = AllSettings.launcherTaskMenuExpanded.state
    fun changeTasksExpandedState() { AllSettings.launcherTaskMenuExpanded.save(!isTaskMenuExpanded) }
    val toMainScreen: () -> Unit = { screenBackStackModel.mainScreen.clearWith(NormalNavKey.LauncherMain) }
    var section by remember { mutableStateOf(LauncherSection.HOME) }
    var showTool by remember { mutableStateOf(false) }
    fun openDownload(target: TitledNavKey) {
        screenBackStackModel.downloadScreen.backStack.clearWith(target)
        screenBackStackModel.mainScreen.clearWith(screenBackStackModel.downloadScreen)
        showTool = true
    }
    Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFF0E0E10), contentColor = Color.White) {
        Row(modifier = Modifier.fillMaxSize()) {
            MiraiNavigationRail(
                modifier = Modifier.fillMaxHeight(),
                selectedSection = section,
                onNavigate = {
                    section = it
                    showTool = false
                    if (it == LauncherSection.HOME) toMainScreen()
                    if (it == LauncherSection.SKINS) screenBackStackModel.mainScreen.clearWith(NormalNavKey.McSkinLibrary)
                },
                onCreateInstance = { openDownload(screenBackStackModel.downloadGameScreen) },
                onAccountClick = {
                    screenBackStackModel.mainScreen.clearWith(NormalNavKey.AccountManager(FirstLoginMenu.NONE))
                    showTool = true
                }
            )
            Box(modifier = Modifier.fillMaxHeight().weight(1f)) {
                Crossfade(targetState = section to showTool, animationSpec = tween(durationMillis = 180), label = "mirai-page") { (page, tool) ->
                    when {
                        page == LauncherSection.DISCOVER && !tool -> MiraiDiscoverPage(
                            onMods = { openDownload(screenBackStackModel.downloadModScreen) },
                            onModpacks = { openDownload(screenBackStackModel.downloadModPackScreen) },
                            onResourcePacks = { openDownload(screenBackStackModel.downloadResourcePackScreen) },
                            onShaders = { openDownload(screenBackStackModel.downloadShadersScreen) },
                            onWorlds = { openDownload(screenBackStackModel.downloadSavesScreen) },
                            onVersions = { openDownload(screenBackStackModel.downloadGameScreen) },
                            modifier = Modifier.fillMaxSize()
                        )
                        page == LauncherSection.LIBRARY && !tool -> MiraiLibraryPage(
                            onInstances = { screenBackStackModel.mainScreen.clearWith(NormalNavKey.VersionsManager); showTool = true },
                            onExport = { screenBackStackModel.mainScreen.clearWith(NormalNavKey.VersionsManager); showTool = true },
                            onFiles = { eventViewModel.sendEvent(EventViewModel.Event.OpenFileManager(rootPath = PathManager.DIR_FILES_EXTERNAL.absolutePath)) },
                            modifier = Modifier.fillMaxSize()
                        )
                        page == LauncherSection.MULTIPLAYER && !tool -> MiraiServersPage(
                            onMultiplayer = { screenBackStackModel.mainScreen.clearWith(NormalNavKey.Multiplayer); showTool = true },
                            modifier = Modifier.fillMaxSize()
                        )
                        page == LauncherSection.SETTINGS && !tool -> MiraiSettingsPage(
                            onAllSettings = { screenBackStackModel.mainScreen.clearWith(screenBackStackModel.settingsScreen); showTool = true },
                            onAccounts = { screenBackStackModel.mainScreen.clearWith(NormalNavKey.AccountManager(FirstLoginMenu.NONE)); showTool = true },
                            onRenderer = { screenBackStackModel.mainScreen.clearWith(screenBackStackModel.settingsScreen); showTool = true },
                            modifier = Modifier.fillMaxSize()
                        )
                        else -> NavigationUI(modifier = Modifier.fillMaxSize(), screenBackStackModel = screenBackStackModel, toMainScreen = toMainScreen, eventViewModel = eventViewModel, modpackImportViewModel = modpackImportViewModel, submitError = submitError)
                    }
                }
            }
        }
    }
}

private fun TitledNavKey?.toLauncherSection(): LauncherSection? = when (this) {
    null, NormalNavKey.LauncherMain -> LauncherSection.HOME
    is NestedNavKey.Download, is NestedNavKey.DownloadGame, is NestedNavKey.DownloadModPack, is NestedNavKey.DownloadMod, is NestedNavKey.DownloadResourcePack, is NestedNavKey.DownloadSaves, is NestedNavKey.DownloadShaders, is NestedNavKey.DownloadFavorites, is NestedNavKey.AssetInfo -> LauncherSection.DISCOVER
    NormalNavKey.VersionsManager, is NestedNavKey.VersionSettings, is NestedNavKey.VersionExport -> LauncherSection.LIBRARY
    NormalNavKey.Multiplayer -> LauncherSection.MULTIPLAYER
    is NestedNavKey.Settings -> LauncherSection.SETTINGS
    NormalNavKey.McSkinLibrary -> LauncherSection.SKINS
    else -> null
}

@Composable
private fun NavigationUI(modifier: Modifier = Modifier, screenBackStackModel: ScreenBackStackViewModel, toMainScreen: () -> Unit, eventViewModel: EventViewModel, modpackImportViewModel: ModpackImportViewModel, submitError: (ErrorViewModel.ThrowableMessage) -> Unit) {
    val backStack = screenBackStackModel.mainScreen.backStack
    val currentKey = backStack.lastOrNull()
    LaunchedEffect(currentKey) { screenBackStackModel.mainScreen.currentKey = currentKey }
    if (backStack.isNotEmpty()) {
        val navigateToVersions: (Version) -> Unit = { version -> screenBackStackModel.mainScreen.navigateTo(screenKey = NestedNavKey.VersionSettings(version), useClassEquality = true) }
        val navigateToExport: (Version) -> Unit = { version -> screenBackStackModel.mainScreen.removeAndNavigateTo(remove = NestedNavKey.VersionSettings::class, screenKey = NestedNavKey.VersionExport(version), useClassEquality = true) }
        NavDisplay(backStack = backStack, modifier = modifier, onBack = { onBack(backStack) }, transitionSpec = rememberTransitionSpec(), popTransitionSpec = rememberTransitionSpec(), entryProvider = entryProvider {
            entry<NormalNavKey.LauncherMain> { LauncherScreen(backStackViewModel = screenBackStackModel, navigateToVersions = navigateToVersions, onLaunchGame = { version -> eventViewModel.sendEvent(EventViewModel.Event.Launch.Game(version)) }, onOpenLink = { eventViewModel.sendEvent(EventViewModel.Event.OpenLink(it)) }, startGuideOnce = { keys -> eventViewModel.sendStartGuideOnce(keys) }) }
            entry<NestedNavKey.Settings> { key -> SettingsScreen(key = key, backStackViewModel = screenBackStackModel, openLicenseScreen = { raw -> backStack.navigateTo(NormalNavKey.License(raw)) }, eventViewModel = eventViewModel, submitError = submitError) }
            entry<NormalNavKey.License> { key -> LicenseScreen(key = key, backStackViewModel = screenBackStackModel) }
            entry<NormalNavKey.McSkinLibrary> { com.movtery.zalithlauncher.ui.screens.content.McSkinScreen() }
            entry<NormalNavKey.AccountManager> { key -> AccountManageScreen(key = key, backStackViewModel = screenBackStackModel, backToMainScreen = toMainScreen, openLink = { url -> eventViewModel.sendEvent(EventViewModel.Event.OpenLink(url)) }, eventViewModel = eventViewModel, submitError = submitError) }
            entry<NormalNavKey.WebScreen> { key -> WebViewScreen(key = key, backStackViewModel = screenBackStackModel, eventViewModel = eventViewModel) }
            entry<NormalNavKey.VersionsManager> { VersionsManageScreen(backScreenViewModel = screenBackStackModel, navigateToVersions = navigateToVersions, navigateToExport = navigateToExport, eventViewModel = eventViewModel, submitError = submitError) }
            entry<NormalNavKey.FileSelector> { key -> FileSelectorScreen(key = key, backScreenViewModel = screenBackStackModel) { backStack.removeLastOrNull() } }
            entry<NestedNavKey.VersionSettings> { key -> VersionSettingsScreen(key = key, backScreenViewModel = screenBackStackModel, backToMainScreen = toMainScreen, onExportModpack = { navigateToExport(key.version) }, eventViewModel = eventViewModel, submitError = submitError) }
            entry<NestedNavKey.VersionExport> { key -> VersionExportScreen(key = key, backScreenViewModel = screenBackStackModel, eventViewModel = eventViewModel, backToMainScreen = toMainScreen) }
            entry<NestedNavKey.Download> { key -> DownloadScreen(key = key, backScreenViewModel = screenBackStackModel, eventViewModel = eventViewModel, modpackImportViewModel = modpackImportViewModel, submitError = submitError) }
            entry<NestedNavKey.AssetInfo> { key -> AssetInfoScreen(key = key, mainScreenKey = screenBackStackModel.mainScreen.currentKey, assetInfoScreenKey = key.currentKey, eventViewModel = eventViewModel, submitError = submitError) }
            entry<NormalNavKey.Multiplayer> { MultiplayerScreen(backScreenViewModel = screenBackStackModel, eventViewModel = eventViewModel) }
            entry<NormalNavKey.LogView> { key -> LogViewScreen(key = key, backStackViewModel = screenBackStackModel) }
        })
    } else Box(modifier)
}

@Composable
private fun TaskMenu(tasks: List<Task>, isExpanded: Boolean, modifier: Modifier = Modifier, changeExpandedState: () -> Unit = {}) {
    val show = isExpanded && tasks.isNotEmpty()
    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    AnimatedVisibility(modifier = modifier, enter = fadeIn(tween(180)) + slideInHorizontally(initialOffsetX = { if (isRtl) it / 8 else -it / 8 }), exit = fadeOut(tween(180)) + slideOutHorizontally(targetOffsetX = { if (isRtl) it / 8 else -it / 8 }), visible = show) {
        BackgroundCard(modifier = Modifier.fillMaxSize().padding(all = 6.dp), influencedByBackground = false, shape = MaterialTheme.shapes.extraLarge, colors = CardDefaults.cardColors(containerColor = backgroundColor(), contentColor = onBackgroundColor()), elevation = CardDefaults.elevatedCardElevation(defaultElevation = 6.dp)) {
            Column {
                CardTitleLayout(blur = 0) {
                    Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp).padding(top = 8.dp, bottom = 4.dp)) {
                        IconButton(modifier = Modifier.size(28.dp).align(Alignment.CenterStart), onClick = changeExpandedState) { Icon(modifier = Modifier.size(28.dp), painter = painterResource(R.drawable.ic_arrow_left_rounded), contentDescription = stringResource(R.string.generic_collapse)) }
                        Text(modifier = Modifier.align(Alignment.Center), text = stringResource(R.string.main_task_menu))
                    }
                }
                LazyColumn(modifier = Modifier.fillMaxHeight().weight(1f), contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)) {
                    items(tasks) { task ->
                        val taskProgress by task.progress.collectAsStateWithLifecycle()
                        val taskMessage by task.message.collectAsStateWithLifecycle()
                        val rateBytesPerSec by task.rateBytesPerSec.collectAsStateWithLifecycle()
                        TaskItem(taskProgress, taskMessage, rateBytesPerSec, Modifier.fillMaxWidth().padding(vertical = 6.dp)) { TaskSystem.cancelTask(task.id) }
                    }
                }
            }
        }
    }
}

@Composable
private fun TaskItem(taskProgress: Float, taskMessage: AndroidStringText?, rateBytesPerSec: Long?, modifier: Modifier = Modifier, shape: Shape = MaterialTheme.shapes.large, color: Color = cardColor(false), contentColor: Color = onCardColor(), onCancelClick: () -> Unit = {}) {
    Surface(modifier = modifier, shape = shape, color = color, contentColor = contentColor) {
        Row(modifier = Modifier.padding(all = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            IconButton(modifier = Modifier.size(24.dp).align(Alignment.CenterVertically), onClick = onCancelClick) { Icon(modifier = Modifier.size(20.dp), painter = painterResource(R.drawable.ic_close), contentDescription = stringResource(R.string.generic_cancel)) }
            Column(modifier = Modifier.weight(1f).align(Alignment.CenterVertically)) {
                taskMessage?.let { AndroidStringText(text = it, style = MaterialTheme.typography.labelMedium) }
                if (taskProgress < 0) LinearProgressIndicator(modifier = Modifier.fillMaxWidth()) else LinearProgressIndicator(progress = { taskProgress }, modifier = Modifier.fillMaxWidth())
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (taskProgress >= 0f) Text("${(taskProgress * 100).toInt()}%", style = MaterialTheme.typography.labelMedium)
                    rateBytesPerSec?.let { bytes -> Text(remember(bytes) { "${formatFileSize(bytes)}/s" }, style = MaterialTheme.typography.labelMedium) }
                }
            }
        }
    }
}
