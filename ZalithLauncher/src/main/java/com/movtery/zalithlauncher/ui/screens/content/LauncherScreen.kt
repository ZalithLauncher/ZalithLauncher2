package com.movtery.zalithlauncher.ui.screens.content

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import com.movtery.zalithlauncher.game.version.installed.Version
import com.movtery.zalithlauncher.ui.guide.GuideKeys
import com.movtery.zalithlauncher.ui.screens.NormalNavKey
import com.movtery.zalithlauncher.ui.screens.content.home.MiraiPlayPage
import com.movtery.zalithlauncher.viewmodel.ScreenBackStackViewModel

@Composable
fun LauncherScreen(
    backStackViewModel: ScreenBackStackViewModel,
    navigateToVersions: (Version) -> Unit,
    onLaunchGame: (Version?) -> Unit,
    onOpenLink: (String) -> Unit,
    startGuideOnce: (GuideKeys.Keys) -> Unit,
) {
    LaunchedEffect(Unit) {
        startGuideOnce(GuideKeys.Main)
    }
    MiraiPlayPage(
        modifier = Modifier.fillMaxSize(),
        onLaunch = onLaunchGame,
        onExploreContent = { backStackViewModel.navigateToDownload(backStackViewModel.downloadModScreen) },
        onCreateInstance = { backStackViewModel.navigateToDownload(backStackViewModel.downloadGameScreen) },
        onAddAccount = { backStackViewModel.mainScreen.clearWith(NormalNavKey.AccountManager(FirstLoginMenu.NONE)) },
        onManageVersions = { backStackViewModel.mainScreen.clearWith(NormalNavKey.VersionsManager) },
        onOpenVersionSettings = navigateToVersions
    )
}
