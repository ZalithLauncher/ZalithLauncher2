package com.movtery.zalithlauncher.ui.screens.main

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.movtery.zalithlauncher.coroutine.TaskSystem
import com.movtery.zalithlauncher.game.version.installed.Version
import com.movtery.zalithlauncher.game.version.installed.VersionsManager
import com.movtery.zalithlauncher.path.PathManager
import com.movtery.zalithlauncher.ui.guide.sendStartGuideOnce
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
import com.movtery.zalithlauncher.ui.screens.content.SettingsScreen
import com.movtery.zalithlauncher.ui.screens.content.VersionExportScreen
import com.movtery.zalithlauncher.ui.screens.content.VersionSettingsScreen
import com.movtery.zalithlauncher.ui.screens.content.VersionsManageScreen
import com.movtery.zalithlauncher.ui.screens.content.WebViewScreen
import com.movtery.zalithlauncher.ui.screens.content.assetinfo.AssetInfoScreen
import com.movtery.zalithlauncher.ui.screens.content.home.MiraiEditorPage
import com.movtery.zalithlauncher.ui.screens.content.home.MiraiInstanceDetail
import com.movtery.zalithlauncher.ui.screens.content.home.MiraiLibraryPage
import com.movtery.zalithlauncher.ui.screens.content.home.MiraiLogPage
import com.movtery.zalithlauncher.ui.screens.content.home.MiraiLoginPage
import com.movtery.zalithlauncher.ui.screens.content.home.MiraiPackPage
import com.movtery.zalithlauncher.ui.screens.content.home.MiraiPlayPage
import com.movtery.zalithlauncher.ui.screens.content.home.MiraiSettingsPage
import com.movtery.zalithlauncher.ui.screens.content.home.MiraiTutorialPage
import com.movtery.zalithlauncher.ui.screens.navigateTo
import com.movtery.zalithlauncher.ui.screens.onBack
import com.movtery.zalithlauncher.ui.screens.rememberTransitionSpec
import com.movtery.zalithlauncher.viewmodel.ErrorViewModel
import com.movtery.zalithlauncher.viewmodel.EventViewModel
import com.movtery.zalithlauncher.viewmodel.ModpackImportViewModel
import com.movtery.zalithlauncher.viewmodel.ScreenBackStackViewModel
import com.movtery.zalithlauncher.viewmodel.sendKeepScreen

@Composable
fun MainScreen(screenBackStackModel: ScreenBackStackViewModel, eventViewModel: EventViewModel, modpackImportViewModel: ModpackImportViewModel, submitError: (ErrorViewModel.ThrowableMessage) -> Unit) {
    val tasks by TaskSystem.tasksFlow.collectAsStateWithLifecycle()
    LaunchedEffect(tasks) { eventViewModel.sendKeepScreen(tasks.isNotEmpty()) }
    val toMainScreen: () -> Unit = { screenBackStackModel.mainScreen.clearWith(NormalNavKey.LauncherMain) }
    var section by remember { mutableStateOf(LauncherSection.HOME) }
    var showTool by remember { mutableStateOf(false) }
    var tutorialOpen by remember { mutableStateOf(false) }
    var accountsOpen by remember { mutableStateOf(false) }
    var exportVersion by remember { mutableStateOf<Version?>(null) }
    var logPath by remember { mutableStateOf<String?>(null) }
    var openedVersion by remember { mutableStateOf<Version?>(null) }
    var instanceEditor by remember { mutableStateOf(false) }
    var serversOpen by remember { mutableStateOf(false) }
    var settingsTarget by remember { mutableStateOf<NormalNavKey.Settings?>(null) }
    var runAction by remember { mutableStateOf(false) }
    fun openDownload(target: TitledNavKey) { screenBackStackModel.downloadScreen.clearWith(target); screenBackStackModel.mainScreen.currentKey = screenBackStackModel.downloadScreen; runAction = false }
    fun openSetting(target: NormalNavKey.Settings) { screenBackStackModel.settingsScreen.clearWith(target); screenBackStackModel.mainScreen.currentKey = screenBackStackModel.settingsScreen; settingsTarget = target; runAction = false }
    fun openAccounts() { section = LauncherSection.SETTINGS; accountsOpen = true; runAction = true }
    fun playSelected() { section = LauncherSection.HOME; VersionsManager.currentVersion.value?.let { eventViewModel.sendEvent(EventViewModel.Event.Launch.Game(it)) } }
    fun goDashboard() {
        section = LauncherSection.HOME
        showTool = false
        tutorialOpen = false
        accountsOpen = false
        exportVersion = null
        logPath = null
        openedVersion = null
        instanceEditor = false
        serversOpen = false
        settingsTarget = null
        runAction = false
        toMainScreen()
    }
    LauncherBack(onDashboard = { goDashboard() })
    CaveBackground {
        if (section == LauncherSection.DISCOVER && !showTool) {
            DownloadScreen(key = screenBackStackModel.downloadScreen, backScreenViewModel = screenBackStackModel, eventViewModel = eventViewModel, modpackImportViewModel = modpackImportViewModel, submitError = submitError)
            LauncherBack(onDashboard = { goDashboard() })
        } else Row(modifier = Modifier.fillMaxSize()) {
            MiraiNavigationRail(modifier = Modifier.fillMaxHeight(), selectedSection = section, onNavigate = { next -> section = next; showTool = false; tutorialOpen = false; accountsOpen = false; exportVersion = null; logPath = null; openedVersion = null; instanceEditor = false; serversOpen = false; settingsTarget = null; runAction = false; if (next == LauncherSection.HOME) toMainScreen(); if (next == LauncherSection.DISCOVER) openDownload(screenBackStackModel.downloadModScreen) }, onCreateInstance = { section = LauncherSection.DISCOVER; openDownload(screenBackStackModel.downloadGameScreen) }, onAccountClick = { openAccounts() }, onPlay = { playSelected() })
            Box(modifier = Modifier.fillMaxHeight().weight(1f)) {
                Crossfade(targetState = section to showTool, animationSpec = tween(120), label = "mirai-page") { (page, tool) ->
                    when {
                        page == LauncherSection.HOME && !tool -> MiraiPlayPage(onLaunch = { version -> version?.let { eventViewModel.sendEvent(EventViewModel.Event.Launch.Game(it)) } }, onExploreContent = { section = LauncherSection.DISCOVER; openDownload(screenBackStackModel.downloadModScreen) }, onCreateInstance = { section = LauncherSection.DISCOVER; openDownload(screenBackStackModel.downloadGameScreen) }, onManageVersions = { }, onOpenVersionSettings = { version -> openedVersion = version; instanceEditor = true }, modifier = Modifier.fillMaxSize())
                        page == LauncherSection.SKINS && !tool -> Column(modifier = Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) { Text("Skins", color = Color.White, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold); Text("Browse a skin, download it, and equip it.", color = Color(0xFFD7CFC8)); Text("Open skins", color = Color(0xFF1BD96A), fontWeight = FontWeight.SemiBold, modifier = Modifier.clickable { screenBackStackModel.mainScreen.clearWith(NormalNavKey.McSkinLibrary); showTool = true }) }
                        page == LauncherSection.HOME && instanceEditor && openedVersion != null -> VersionSettingsScreen(key = NestedNavKey.VersionSettings(openedVersion!!), backScreenViewModel = screenBackStackModel, backToMainScreen = { instanceEditor = false }, onExportModpack = { exportVersion = openedVersion; section = LauncherSection.SETTINGS; instanceEditor = false }, eventViewModel = eventViewModel, submitError = submitError)
                        page == LauncherSection.MULTIPLAYER && !tool -> WallpaperPage(Modifier.fillMaxSize())
                        page == LauncherSection.SETTINGS && !tool && accountsOpen && runAction -> AccountManageScreen(key = NormalNavKey.AccountManager(FirstLoginMenu.NONE), backStackViewModel = screenBackStackModel, backToMainScreen = { accountsOpen = false; runAction = false }, openLink = { url -> eventViewModel.sendEvent(EventViewModel.Event.OpenLink(url)) }, eventViewModel = eventViewModel, submitError = submitError)
                        page == LauncherSection.SETTINGS && !tool && accountsOpen -> MiraiLoginPage(onOffline = { runAction = true }, onMicrosoft = { runAction = true }, onBack = { accountsOpen = false }, modifier = Modifier.fillMaxSize())
                        page == LauncherSection.SETTINGS && !tool && exportVersion != null && runAction -> VersionExportScreen(key = NestedNavKey.VersionExport(exportVersion!!), backScreenViewModel = screenBackStackModel, eventViewModel = eventViewModel, backToMainScreen = { exportVersion = null; runAction = false })
                        page == LauncherSection.SETTINGS && !tool && exportVersion != null -> MiraiPackPage(exportVersion?.getVersionName(), onPack = { runAction = true }, onBack = { exportVersion = null }, modifier = Modifier.fillMaxSize())
                        page == LauncherSection.SETTINGS && !tool && logPath != null -> MiraiLogPage(logPath, onBack = { logPath = null }, modifier = Modifier.fillMaxSize())
                        page == LauncherSection.SETTINGS && !tool && tutorialOpen -> MiraiTutorialPage(onBack = { tutorialOpen = false }, modifier = Modifier.fillMaxSize())
                        page == LauncherSection.SETTINGS && !tool && settingsTarget != null && runAction -> SettingsScreen(key = screenBackStackModel.settingsScreen, backStackViewModel = screenBackStackModel, openLicenseScreen = { raw -> screenBackStackModel.mainScreen.navigateTo(NormalNavKey.License(raw)); showTool = true }, eventViewModel = eventViewModel, submitError = submitError)
                        page == LauncherSection.SETTINGS && !tool && settingsTarget != null -> MiraiEditorPage("Settings", onEdit = { runAction = true }, onBack = { settingsTarget = null }, modifier = Modifier.fillMaxSize())
                        page == LauncherSection.SETTINGS && !tool -> MiraiSettingsPage(onRenderer = { openSetting(NormalNavKey.Settings.Renderer) }, onGame = { openSetting(NormalNavKey.Settings.Game) }, onControls = { openSetting(NormalNavKey.Settings.Control) }, onGamepad = { openSetting(NormalNavKey.Settings.Gamepad) }, onLauncher = { openSetting(NormalNavKey.Settings.Launcher) }, onJava = { openSetting(NormalNavKey.Settings.JavaManager) }, onControlLayouts = { openSetting(NormalNavKey.Settings.ControlManager) }, onAccounts = { openAccounts() }, onAbout = { openSetting(NormalNavKey.Settings.AboutInfo) }, onExport = { exportVersion = VersionsManager.currentVersion.value; runAction = false }, onSkins = { screenBackStackModel.mainScreen.clearWith(NormalNavKey.McSkinLibrary); showTool = true }, onFiles = { eventViewModel.sendEvent(EventViewModel.Event.OpenFileManager(rootPath = PathManager.DIR_FILES_EXTERNAL.absolutePath)) }, onLogs = { logPath = VersionsManager.currentVersion.value?.getLatestLog()?.absolutePath ?: "" }, onWeb = { eventViewModel.sendEvent(EventViewModel.Event.OpenLink("https://modrinth.com")) }, onTutorial = { tutorialOpen = true }, modifier = Modifier.fillMaxSize())
                        else -> NavigationUI(Modifier.fillMaxSize(), screenBackStackModel, toMainScreen, eventViewModel, modpackImportViewModel, submitError)
                    }
                }
            }
            LauncherBack(onDashboard = { goDashboard() })
        }
    }
}

@Composable
private fun NavigationUI(modifier: Modifier = Modifier, screenBackStackModel: ScreenBackStackViewModel, toMainScreen: () -> Unit, eventViewModel: EventViewModel, modpackImportViewModel: ModpackImportViewModel, submitError: (ErrorViewModel.ThrowableMessage) -> Unit) {
    val backStack = screenBackStackModel.mainScreen.backStack
    val currentKey = backStack.lastOrNull()
    LaunchedEffect(currentKey) { screenBackStackModel.mainScreen.currentKey = currentKey }
    if (backStack.isEmpty()) { Box(modifier); return }
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
        entry<NormalNavKey.LogView> { key -> LogViewScreen(key = key, backStackViewModel = screenBackStackModel) }
    })
}
