package com.movtery.zalithlauncher.ui.screens.content

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.ui.theme.MiraiThemeManager
import com.movtery.zalithlauncher.game.download.assets.favorites.FavoriteProjectsRepository
import com.movtery.zalithlauncher.game.download.assets.platform.Platform
import com.movtery.zalithlauncher.game.download.assets.platform.PlatformClasses
import com.movtery.zalithlauncher.ui.base.BaseScreen
import com.movtery.zalithlauncher.ui.screens.NestedNavKey
import com.movtery.zalithlauncher.ui.screens.NormalNavKey
import com.movtery.zalithlauncher.ui.screens.TitledNavKey
import com.movtery.zalithlauncher.ui.screens.content.download.DownloadFavoritesScreen
import com.movtery.zalithlauncher.ui.screens.content.download.DownloadGameScreen
import com.movtery.zalithlauncher.ui.screens.content.download.DownloadModPackScreen
import com.movtery.zalithlauncher.ui.screens.content.download.DownloadModScreen
import com.movtery.zalithlauncher.ui.screens.content.download.DownloadResourcePackScreen
import com.movtery.zalithlauncher.ui.screens.content.download.DownloadSavesScreen
import com.movtery.zalithlauncher.ui.screens.content.download.DownloadShadersScreen
import com.movtery.zalithlauncher.ui.screens.content.download.assets.search.SearchIdScreen
import com.movtery.zalithlauncher.ui.screens.navigateOnce
import com.movtery.zalithlauncher.ui.screens.onBack
import com.movtery.zalithlauncher.ui.screens.rememberTransitionSpec
import com.movtery.zalithlauncher.viewmodel.ErrorViewModel
import com.movtery.zalithlauncher.viewmodel.EventViewModel
import com.movtery.zalithlauncher.viewmodel.ModpackImportViewModel
import com.movtery.zalithlauncher.viewmodel.ScreenBackStackViewModel

fun ScreenBackStackViewModel.navigateToDownload(targetScreen: TitledNavKey? = null) {
    downloadScreen.clearWith(targetScreen ?: downloadGameScreen)
    mainScreen.removeAndNavigateTo(removes = clearBeforeNavKeys, screenKey = downloadScreen, useClassEquality = true)
}

private fun ScreenBackStackViewModel.swapToCategoryAssets(
    platform: Platform,
    classes: PlatformClasses,
    projectId: String,
    iconUrl: String?
) {
    val targetScreen = when (classes) {
        PlatformClasses.MOD -> downloadModScreen
        PlatformClasses.MOD_PACK -> downloadModPackScreen
        PlatformClasses.RESOURCE_PACK -> downloadResourcePackScreen
        PlatformClasses.SAVES -> downloadSavesScreen
        PlatformClasses.SHADERS -> downloadShadersScreen
    }
    navigateToDownload(targetScreen = targetScreen.apply {
        navigateTo(NormalNavKey.DownloadAssets(platform = platform, projectId = projectId, classes = classes, iconUrl = iconUrl))
    })
}

private data class DiscoverCategoryItem(
    val label: String,
    val iconRes: Int,
    val target: TitledNavKey
)

@Composable
fun DownloadScreen(
    key: NestedNavKey.Download,
    backScreenViewModel: ScreenBackStackViewModel,
    modpackImportViewModel: ModpackImportViewModel,
    eventViewModel: EventViewModel,
    submitError: (ErrorViewModel.ThrowableMessage) -> Unit
) {
    LaunchedEffect(Unit) { FavoriteProjectsRepository.ensureLoaded() }
    BaseScreen(screenKey = key, currentKey = backScreenViewModel.mainScreen.currentKey, useClassEquality = true) {
        NavigationUI(
            key = key,
            backScreenViewModel = backScreenViewModel,
            eventViewModel = eventViewModel,
            modpackImportViewModel = modpackImportViewModel,
            submitError = submitError,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
private fun NavigationUI(
    key: NestedNavKey.Download,
    backScreenViewModel: ScreenBackStackViewModel,
    eventViewModel: EventViewModel,
    modpackImportViewModel: ModpackImportViewModel,
    submitError: (ErrorViewModel.ThrowableMessage) -> Unit,
    modifier: Modifier = Modifier
) {
    val backStack = key.backStack
    val stackTopKey = backStack.lastOrNull()
    LaunchedEffect(stackTopKey) { backScreenViewModel.downloadScreen.currentKey = stackTopKey }

    val categories = listOf(
        DiscoverCategoryItem("Modpacks", R.drawable.ic_package_2_outlined, backScreenViewModel.downloadModPackScreen),
        DiscoverCategoryItem("Mods", R.drawable.ic_extension_outlined, backScreenViewModel.downloadModScreen),
        DiscoverCategoryItem("Resource Packs", R.drawable.ic_format_paint_outlined, backScreenViewModel.downloadResourcePackScreen),
        DiscoverCategoryItem("Shaders", R.drawable.ic_lightbulb, backScreenViewModel.downloadShadersScreen),
        DiscoverCategoryItem("Worlds", R.drawable.ic_public, backScreenViewModel.downloadSavesScreen),
        DiscoverCategoryItem("Install Vanilla", R.drawable.ic_videogame_asset_outlined, backScreenViewModel.downloadGameScreen),
        DiscoverCategoryItem("Favorites", R.drawable.ic_favorite_filled, backScreenViewModel.downloadFavoritesScreen)
    )

    val isCreateInstanceScreen = stackTopKey is NestedNavKey.DownloadGame

    Column(
        modifier = modifier.padding(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (!isCreateInstanceScreen) {
            // Modrinth Discover Top Category Pill Bar (Mockup #4)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    categories.forEach { cat ->
                        val selected = stackTopKey?.javaClass == cat.target.javaClass
                        val bgColor by animateColorAsState(
                            targetValue = if (selected) MiraiThemeManager.currentAccent() else Color(0xFF161920),
                            animationSpec = tween(160),
                            label = "discoverCatBg"
                        )
                        val fgColor by animateColorAsState(
                            targetValue = if (selected) Color(0xFF06210F) else Color(0xFFE5E7EB),
                            animationSpec = tween(160),
                            label = "discoverCatFg"
                        )

                        Surface(
                            shape = RoundedCornerShape(18.dp),
                            color = bgColor,
                            border = BorderStroke(
                                1.dp,
                                if (selected) MiraiThemeManager.currentAccent() else Color(0xFF262B37)
                            ),
                            onClick = {
                                backScreenViewModel.navigateToDownload(cat.target)
                            }
                        ) {
                            Text(
                                text = cat.label,
                                modifier = Modifier.padding(horizontal = 13.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.Medium,
                                color = fgColor
                            )
                        }
                    }
                }
            }
        }

        if (backStack.isNotEmpty()) {
            NavDisplay(
                backStack = backStack,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                onBack = { onBack(backStack) },
                transitionSpec = rememberTransitionSpec(),
                popTransitionSpec = rememberTransitionSpec(),
                entryProvider = entryProvider {
                    entry<NestedNavKey.DownloadGame> { child ->
                        DownloadGameScreen(
                            key = child,
                            mainScreenKey = backScreenViewModel.mainScreen.currentKey,
                            downloadScreenKey = backScreenViewModel.downloadScreen.currentKey,
                            downloadGameScreenKey = backScreenViewModel.downloadGameScreen.currentKey,
                            onCurrentKeyChange = { backScreenViewModel.downloadGameScreen.currentKey = it },
                            eventViewModel = eventViewModel
                        )
                    }
                    entry<NestedNavKey.DownloadModPack> { child ->
                        DownloadModPackScreen(
                            key = child,
                            mainScreenKey = backScreenViewModel.mainScreen.currentKey,
                            downloadScreenKey = backScreenViewModel.downloadScreen.currentKey,
                            downloadModPackScreenKey = backScreenViewModel.downloadModPackScreen.currentKey,
                            onCurrentKeyChange = { backScreenViewModel.downloadModPackScreen.currentKey = it },
                            eventViewModel = eventViewModel,
                            importerViewModel = modpackImportViewModel
                        )
                    }
                    entry<NestedNavKey.DownloadMod> { child ->
                        DownloadModScreen(
                            key = child,
                            mainScreenKey = backScreenViewModel.mainScreen.currentKey,
                            downloadScreenKey = backScreenViewModel.downloadScreen.currentKey,
                            downloadModScreenKey = backScreenViewModel.downloadModScreen.currentKey,
                            onCurrentKeyChange = { backScreenViewModel.downloadModScreen.currentKey = it },
                            submitError = submitError,
                            eventViewModel = eventViewModel
                        )
                    }
                    entry<NestedNavKey.DownloadResourcePack> { child ->
                        DownloadResourcePackScreen(
                            key = child,
                            mainScreenKey = backScreenViewModel.mainScreen.currentKey,
                            downloadScreenKey = backScreenViewModel.downloadScreen.currentKey,
                            downloadResourcePackScreenKey = backScreenViewModel.downloadResourcePackScreen.currentKey,
                            onCurrentKeyChange = { backScreenViewModel.downloadResourcePackScreen.currentKey = it },
                            submitError = submitError,
                            eventViewModel = eventViewModel
                        )
                    }
                    entry<NestedNavKey.DownloadSaves> { child ->
                        DownloadSavesScreen(
                            key = child,
                            mainScreenKey = backScreenViewModel.mainScreen.currentKey,
                            downloadScreenKey = backScreenViewModel.downloadScreen.currentKey,
                            downloadSavesScreenKey = backScreenViewModel.downloadSavesScreen.currentKey,
                            onCurrentKeyChange = { backScreenViewModel.downloadSavesScreen.currentKey = it },
                            submitError = submitError,
                            eventViewModel = eventViewModel
                        )
                    }
                    entry<NestedNavKey.DownloadShaders> { child ->
                        DownloadShadersScreen(
                            key = child,
                            mainScreenKey = backScreenViewModel.mainScreen.currentKey,
                            downloadScreenKey = backScreenViewModel.downloadScreen.currentKey,
                            downloadShadersScreenKey = backScreenViewModel.downloadShadersScreen.currentKey,
                            onCurrentKeyChange = { backScreenViewModel.downloadShadersScreen.currentKey = it },
                            submitError = submitError,
                            eventViewModel = eventViewModel
                        )
                    }
                    entry<NormalNavKey.SearchId> {
                        SearchIdScreen(
                            mainScreenKey = backScreenViewModel.mainScreen.currentKey,
                            downloadScreenKey = backScreenViewModel.downloadScreen.currentKey,
                            swapToDownload = { platform, classes, projectId, iconUrl ->
                                backScreenViewModel.swapToCategoryAssets(platform, classes, projectId, iconUrl)
                            },
                            openLink = { eventViewModel.sendEvent(EventViewModel.Event.OpenLink(it)) }
                        )
                    }
                    entry<NestedNavKey.DownloadFavorites> { child ->
                        DownloadFavoritesScreen(
                            key = child,
                            mainScreenKey = backScreenViewModel.mainScreen.currentKey,
                            downloadScreenKey = backScreenViewModel.downloadScreen.currentKey,
                            downloadFavoritesScreenKey = backScreenViewModel.downloadFavoritesScreen.currentKey,
                            onCurrentKeyChange = { backScreenViewModel.downloadFavoritesScreen.currentKey = it },
                            swapToDownload = { platform, classes, projectId, iconUrl ->
                                backScreenViewModel.swapToCategoryAssets(platform, classes, projectId, iconUrl)
                            }
                        )
                    }
                }
            )
        } else {
            Spacer(
                Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )
        }
    }
}
