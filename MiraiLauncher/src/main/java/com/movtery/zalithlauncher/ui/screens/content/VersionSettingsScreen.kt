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

import android.content.Context
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.JsonSyntaxException
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.game.download.assets.platform.PlatformClasses
import com.movtery.zalithlauncher.game.download.game.GameDownloadInfo
import com.movtery.zalithlauncher.game.download.game.GameInstaller
import com.movtery.zalithlauncher.game.download.game.optifine.CantFetchingOptiFineUrlException
import com.movtery.zalithlauncher.game.download.jvm_server.JvmCrashException
import com.movtery.zalithlauncher.game.download.jvm_server.isProcessStartRefused
import com.movtery.zalithlauncher.game.optimization.JvmGcAutoTunerDialog
import com.movtery.zalithlauncher.game.optimization.MobileFpsBoosterDialog
import com.movtery.zalithlauncher.game.optimization.ModDependencyResolverDialog
import com.movtery.zalithlauncher.game.version.download.DownloadFailedException
import com.movtery.zalithlauncher.game.version.installed.Version
import com.movtery.zalithlauncher.game.version.installed.VersionsManager
import com.movtery.zalithlauncher.notification.NotificationManager
import com.movtery.zalithlauncher.ui.androidText
import com.movtery.zalithlauncher.ui.base.BaseScreen
import com.movtery.zalithlauncher.ui.components.MarqueeText
import com.movtery.zalithlauncher.ui.components.NotificationCheck
import com.movtery.zalithlauncher.ui.components.fadeEdge
import com.movtery.zalithlauncher.ui.components.verticalScrollWithBar
import com.movtery.zalithlauncher.ui.screens.NestedNavKey
import com.movtery.zalithlauncher.ui.screens.NormalNavKey
import com.movtery.zalithlauncher.ui.screens.TitledNavKey
import com.movtery.zalithlauncher.ui.screens.content.elements.TitleTaskFlowDialog
import com.movtery.zalithlauncher.ui.screens.content.elements.VersionIconImage
import com.movtery.zalithlauncher.ui.screens.content.home.ModrinthMetaPill
import com.movtery.zalithlauncher.ui.screens.content.home.resolveRendererShortLabel
import com.movtery.zalithlauncher.ui.screens.content.versions.AddonDiffs
import com.movtery.zalithlauncher.ui.screens.content.versions.ModsManagerScreen
import com.movtery.zalithlauncher.ui.screens.content.versions.ResourcePackManageScreen
import com.movtery.zalithlauncher.ui.screens.content.versions.SavesManagerScreen
import com.movtery.zalithlauncher.ui.screens.content.versions.ScreenshotsManagerScreen
import com.movtery.zalithlauncher.ui.screens.content.versions.ServerListScreen
import com.movtery.zalithlauncher.ui.screens.content.versions.ShadersManagerScreen
import com.movtery.zalithlauncher.ui.screens.content.versions.UpdateLoaderScreen
import com.movtery.zalithlauncher.ui.screens.content.versions.VersionConfigScreen
import com.movtery.zalithlauncher.ui.screens.content.versions.VersionOverViewScreen
import com.movtery.zalithlauncher.ui.screens.navigateOnce
import com.movtery.zalithlauncher.ui.screens.onBack
import com.movtery.zalithlauncher.ui.screens.rememberTransitionSpec
import com.movtery.zalithlauncher.ui.theme.MiraiThemeManager
import com.movtery.zalithlauncher.ui.theme.showThemed
import com.movtery.zalithlauncher.utils.animation.swapAnimateDpAsState
import com.movtery.zalithlauncher.utils.logging.Logger
import com.movtery.zalithlauncher.viewmodel.ErrorViewModel
import com.movtery.zalithlauncher.viewmodel.EventViewModel
import com.movtery.zalithlauncher.viewmodel.ScreenBackStackViewModel
import com.movtery.zalithlauncher.viewmodel.sendToast
import io.ktor.client.plugins.HttpRequestTimeoutException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.SerializationException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.nio.channels.UnresolvedAddressException
import java.util.concurrent.TimeoutException

private const val TAG = "VersionSettings"

private sealed interface UpdateLoaderOperation {
    data object None: UpdateLoaderOperation
    data class Tip(val diffs: AddonDiffs, val info: GameDownloadInfo): UpdateLoaderOperation
    data class WarningForNotification(val diffs: AddonDiffs, val info: GameDownloadInfo): UpdateLoaderOperation
    data object Install: UpdateLoaderOperation
    data class Error(val th: Throwable): UpdateLoaderOperation
}

private class UpdateLoaderViewModel: ViewModel() {
    var installOperation by mutableStateOf<UpdateLoaderOperation>(UpdateLoaderOperation.None)
    var installer by mutableStateOf<GameInstaller?>(null)

    fun install(
        context: Context,
        info: GameDownloadInfo
    ) {
        installOperation = UpdateLoaderOperation.Install
        installer = GameInstaller(context, info, viewModelScope).also {
            it.updateLoader(
                onInstalled = {
                    installer = null
                    installOperation = UpdateLoaderOperation.None

                    viewModelScope.launch(Dispatchers.Main) {
                        VersionsManager.refresh("[UpdateLoader] GameInstaller.onInstalled")

                        MaterialAlertDialogBuilder(context)
                            .setTitle(R.string.download_install_success_title)
                            .setMessage(R.string.versions_update_loader_success_message)
                            .setPositiveButton(R.string.generic_confirm) { dialog, _ ->
                                dialog.dismiss()
                            }
                            .showThemed()
                    }
                },
                onError = { th ->
                    installer = null
                    installOperation = UpdateLoaderOperation.Error(th)
                }
            )
        }
    }

    fun cancel() {
        installer?.cancelInstall(
            clearTarget = false
        )
        installer = null
        installOperation = UpdateLoaderOperation.None
    }

    override fun onCleared() {
        cancel()
    }
}

@Composable
private fun rememberUpdateLoaderViewModel(
    key: NestedNavKey.VersionSettings
): UpdateLoaderViewModel {
    return viewModel(
        key = key.toString() + "_UpdateLoader"
    ) {
        UpdateLoaderViewModel()
    }
}

private data class ModrinthSubTabItem(
    val key: TitledNavKey,
    val label: String,
    val iconRes: Int
)

@Composable
fun VersionSettingsScreen(
    key: NestedNavKey.VersionSettings,
    backScreenViewModel: ScreenBackStackViewModel,
    backToMainScreen: () -> Unit,
    onExportModpack: () -> Unit,
    eventViewModel: EventViewModel,
    submitError: (ErrorViewModel.ThrowableMessage) -> Unit
) {
    val context = LocalContext.current
    val viewModel = rememberUpdateLoaderViewModel(key = key)

    val cBackToMainScreen by rememberUpdatedState(backToMainScreen)
    DisposableEffect(key) {
        val listener = object : suspend () -> Unit {
            override suspend fun invoke() {
                cBackToMainScreen()
            }
        }
        VersionsManager.registerListener(listener)
        onDispose {
            VersionsManager.unregisterListener(listener)
        }
    }

    UpdateLoaderOperation(
        operation = viewModel.installOperation,
        changeOperation = { viewModel.installOperation = it },
        installer = viewModel.installer,
        onInstall = { info ->
            viewModel.install(context, info)
        },
        onCancel = {
            viewModel.cancel()
        }
    )

    BaseScreen(
        screenKey = key,
        currentKey = backScreenViewModel.mainScreen.currentKey
    ) { isVisible ->
        val yOffset by swapAnimateDpAsState(
            targetValue = (-30).dp,
            swapIn = isVisible
        )
        val loaderInfo = remember(key) {
            key.version.getVersionInfo()?.loaderInfo
        }
        val canUpdateLoader = loaderInfo == null || loaderInfo.loader.autoDownloadable
        val isUpdateLoader = loaderInfo != null && loaderInfo.loader.autoDownloadable

        Column(
            modifier = Modifier
                .fillMaxSize()
                .offset { IntOffset(x = 0, y = yOffset.roundToPx()) }
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 1. Compact Mobile Instance Header Row (Mockup #3)
            ModrinthInstanceHeroBanner(
                version = key.version,
                onBack = backToMainScreen,
                onPlay = {
                    VersionsManager.saveVersion(key.version)
                    eventViewModel.sendEvent(EventViewModel.Event.Launch.Game(key.version))
                },
                onOpenFolder = {
                    eventViewModel.sendEvent(
                        EventViewModel.Event.OpenFileManager(
                            rootPath = key.version.getGameDir().absolutePath
                        )
                    )
                }
            )

            // 2. Horizontal Pill Tabs (Mockup #3: Mods, Resource Packs, Shaders, Worlds, Settings)
            ModrinthInstanceSubTabs(
                backStack = key.backStack,
                versionsScreenKey = key.currentKey,
                canUpdateLoader = canUpdateLoader,
                isUpdateLoader = isUpdateLoader
            )

            // 3. Sub-Screen Content Area
            NavigationUI(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                key = key,
                viewModel = viewModel,
                backScreenViewModel = backScreenViewModel,
                versionsScreenKey = key.currentKey,
                onCurrentKeyChange = { newKey ->
                    key.currentKey = newKey
                },
                backToMainScreen = backToMainScreen,
                onExport = onExportModpack,
                version = key.version,
                eventViewModel = eventViewModel,
                submitError = submitError
            )
        }
    }
}

@Composable
private fun ModrinthInstanceHeroBanner(
    version: Version,
    onBack: () -> Unit,
    onPlay: () -> Unit,
    onOpenFolder: () -> Unit
) {
    val context = LocalContext.current
    val activeAccent = MiraiThemeManager.currentAccent()
    val info = version.getVersionInfo()
    val mcVer = info?.minecraftVersion ?: "Unknown"
    val loaderName = info?.loaderInfo?.loader?.displayName ?: "Vanilla"
    val ramMb = remember(version) { version.getRamAllocation(context) }
    var showFpsBooster by remember { mutableStateOf(false) }
    var showJreGcTuner by remember { mutableStateOf(false) }
    var showModResolver by remember { mutableStateOf(false) }

    if (showFpsBooster) {
        MobileFpsBoosterDialog(
            version = version,
            onDismiss = { showFpsBooster = false }
        )
    }

    if (showJreGcTuner) {
        JvmGcAutoTunerDialog(
            version = version,
            onDismiss = { showJreGcTuner = false }
        )
    }

    if (showModResolver) {
        ModDependencyResolverDialog(
            version = version,
            onDismiss = { showModResolver = false }
        )
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Back Arrow Button
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = Color(0xFF21242B),
            border = BorderStroke(1.dp, Color(0xFF2E333E)),
            contentColor = Color.White,
            onClick = onBack
        ) {
            Box(
                modifier = Modifier.size(34.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_arrow_back),
                    contentDescription = stringResource(R.string.generic_back),
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        // Instance Icon
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = Color(0xFF21242B),
            border = BorderStroke(1.dp, Color(0xFF2E333E))
        ) {
            VersionIconImage(
                version = version,
                modifier = Modifier
                    .padding(5.dp)
                    .size(28.dp)
            )
        }

        // Title + Inline Subtitle
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(1.dp)
        ) {
            Text(
                text = version.getVersionName(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "$loaderName $mcVer • $ramMb MB",
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF9CA3AF),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // Smart Mod Dependency & Conflict Resolver Button (Feature #2)
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = Color(0xFF21242B),
            border = BorderStroke(1.dp, Color(0xFF2E333E)),
            onClick = { showModResolver = true }
        ) {
            Row(
                modifier = Modifier
                    .height(34.dp)
                    .padding(horizontal = 9.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🛡️ Mod Check",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFE5E7EB)
                )
            }
        }

        // JRE Auto-Tuner & GC Optimizer Button (Feature #17)
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = Color(0xFF21242B),
            border = BorderStroke(1.dp, Color(0xFF2E333E)),
            onClick = { showJreGcTuner = true }
        ) {
            Row(
                modifier = Modifier
                    .height(34.dp)
                    .padding(horizontal = 9.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "☕ JRE & GC",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFE5E7EB)
                )
            }
        }

        // 1-Tap Mobile FPS Booster Button
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = activeAccent.copy(alpha = 0.16f),
            border = BorderStroke(1.dp, activeAccent.copy(alpha = 0.7f)),
            onClick = { showFpsBooster = true }
        ) {
            Row(
                modifier = Modifier
                    .height(34.dp)
                    .padding(horizontal = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "⚡ Boost FPS",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = activeAccent
                )
            }
        }

        // Open Folder Button
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = Color(0xFF21242B),
            border = BorderStroke(1.dp, Color(0xFF2E333E)),
            contentColor = Color(0xFFE5E7EB),
            onClick = onOpenFolder
        ) {
            Box(
                modifier = Modifier.size(34.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_folder_outlined),
                    contentDescription = "Folder",
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        // Dynamic Accent '▶ Play' Button
        Button(
            onClick = onPlay,
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = activeAccent,
                contentColor = Color(0xFF06210F)
            ),
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
            modifier = Modifier.height(34.dp)
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_play_arrow_filled),
                contentDescription = stringResource(R.string.main_launch_game),
                modifier = Modifier.size(16.dp)
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text = "Play",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}

@Composable
private fun ModrinthInstanceSubTabs(
    backStack: NavBackStack<TitledNavKey>,
    versionsScreenKey: TitledNavKey?,
    canUpdateLoader: Boolean,
    isUpdateLoader: Boolean
) {
    val activeAccent = MiraiThemeManager.currentAccent()
    val tabs = remember(canUpdateLoader, isUpdateLoader) {
        buildList {
            add(ModrinthSubTabItem(NormalNavKey.Versions.ModsManager, "Mods", R.drawable.ic_extension_outlined))
            add(ModrinthSubTabItem(NormalNavKey.Versions.ResourcePackManager, "Resource Packs", R.drawable.ic_format_paint_outlined))
            add(ModrinthSubTabItem(NormalNavKey.Versions.ShadersManager, "Shaders", R.drawable.ic_lightbulb))
            add(ModrinthSubTabItem(NormalNavKey.Versions.SavesManager, "Worlds", R.drawable.ic_public))
            add(ModrinthSubTabItem(NormalNavKey.Versions.ScreenshotsManager, "Screenshots", R.drawable.ic_image_outlined))
            add(ModrinthSubTabItem(NormalNavKey.Versions.Config, "Settings", R.drawable.ic_build_outlined))
            add(ModrinthSubTabItem(NormalNavKey.Versions.OverView, "Overview", R.drawable.ic_dashboard_outlined))
            if (canUpdateLoader) {
                add(ModrinthSubTabItem(NormalNavKey.Versions.UpdateLoader, if (isUpdateLoader) "Update Loader" else "Install Loader", R.drawable.ic_update))
            }
        }
    }

    val scrollState = rememberScrollState()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        tabs.forEach { tab ->
            val selected = versionsScreenKey === tab.key
            val bgColor by animateColorAsState(
                targetValue = if (selected) activeAccent else Color(0xFF21242B),
                animationSpec = tween(140),
                label = "subTabBg"
            )
            val textColor by animateColorAsState(
                targetValue = if (selected) Color(0xFF06210F) else Color(0xFFE5E7EB),
                animationSpec = tween(140),
                label = "subTabText"
            )

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = bgColor,
                border = BorderStroke(
                    1.dp,
                    if (selected) activeAccent else Color(0xFF2E333E)
                ),
                onClick = {
                    if (tab.key == NormalNavKey.Versions.UpdateLoader) {
                        if (isUpdateLoader) {
                            NormalNavKey.Versions.UpdateLoader.title = androidText(R.string.versions_update_loader)
                        } else {
                            NormalNavKey.Versions.UpdateLoader.title = androidText(R.string.versions_install_loader)
                        }
                    }
                    backStack.navigateOnce(tab.key)
                }
            ) {
                Text(
                    text = tab.label,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.Medium,
                    color = textColor
                )
            }
        }
    }
}

@Composable
private fun NavigationUI(
    modifier: Modifier = Modifier,
    key: NestedNavKey.VersionSettings,
    viewModel: UpdateLoaderViewModel,
    backScreenViewModel: ScreenBackStackViewModel,
    versionsScreenKey: TitledNavKey?,
    onCurrentKeyChange: (TitledNavKey?) -> Unit,
    backToMainScreen: () -> Unit,
    onExport: () -> Unit,
    version: Version,
    eventViewModel: EventViewModel,
    submitError: (ErrorViewModel.ThrowableMessage) -> Unit
) {
    val context = LocalContext.current
    val mainScreenKey = backScreenViewModel.mainScreen.currentKey

    val backStack = key.backStack
    val stackTopKey = backStack.lastOrNull()
    LaunchedEffect(stackTopKey) {
        onCurrentKeyChange(stackTopKey)
    }

    if (backStack.isNotEmpty()) {
        NavDisplay(
            backStack = backStack,
            modifier = modifier,
            onBack = {
                onBack(backStack)
            },
            transitionSpec = rememberTransitionSpec(),
            popTransitionSpec = rememberTransitionSpec(),
            entryProvider = entryProvider {
                entry<NormalNavKey.Versions.OverView> {
                    VersionOverViewScreen(
                        mainScreenKey = mainScreenKey,
                        versionsScreenKey = versionsScreenKey,
                        backToMainScreen = backToMainScreen,
                        onExport = onExport,
                        version = version,
                        eventViewModel = eventViewModel,
                        submitError = submitError
                    )
                }
                entry<NormalNavKey.Versions.Config> {
                    VersionConfigScreen(
                        mainScreenKey = mainScreenKey,
                        versionsScreenKey = versionsScreenKey,
                        version = version,
                        backToMainScreen = backToMainScreen,
                        onCheckVulkan = { version ->
                            eventViewModel.sendEvent(
                                EventViewModel.Event.VulkanCheck(version)
                            )
                        },
                        showToast = { text ->
                            eventViewModel.sendToast(text)
                        },
                        submitError = submitError
                    )
                }
                entry<NormalNavKey.Versions.UpdateLoader> {
                    UpdateLoaderScreen(
                        mainScreenKey = mainScreenKey,
                        versionsScreenKey = versionsScreenKey,
                        backToMainScreen = backToMainScreen,
                        version = version
                    ) { diffs, info ->
                        if (viewModel.installOperation !is UpdateLoaderOperation.None) {
                            return@UpdateLoaderScreen
                        }
                        if (!NotificationManager.checkNotificationEnabled(context)) {
                            viewModel.installOperation = UpdateLoaderOperation.WarningForNotification(diffs, info)
                        } else {
                            viewModel.installOperation = UpdateLoaderOperation.Tip(diffs, info)
                        }
                    }
                }
                entry(NormalNavKey.Versions.ModsManager) {
                    ModsManagerScreen(
                        mainScreenKey = mainScreenKey,
                        versionsScreenKey = versionsScreenKey,
                        version = version,
                        backToMainScreen = backToMainScreen,
                        swapToDownload = {
                            backScreenViewModel.navigateToDownload(
                                targetScreen = backScreenViewModel.downloadModScreen
                            )
                        },
                        onSwapMoreInfo = { projectId, platform ->
                            backScreenViewModel.mainScreen.removeAndNavigateTo(
                                NestedNavKey.AssetInfo::class,
                                NestedNavKey.AssetInfo(platform, projectId, PlatformClasses.MOD)
                            )
                        },
                        eventViewModel = eventViewModel,
                        submitError = submitError
                    )
                }
                entry<NormalNavKey.Versions.SavesManager> {
                    SavesManagerScreen(
                        mainScreenKey = mainScreenKey,
                        versionsScreenKey = versionsScreenKey,
                        version = version,
                        backToMainScreen = backToMainScreen,
                        swapToDownload = {
                            backScreenViewModel.navigateToDownload(
                                targetScreen = backScreenViewModel.downloadSavesScreen
                            )
                        },
                        onQuickPlay = { version, saveName ->
                            eventViewModel.sendEvent(
                                EventViewModel.Event.Launch.PlaySave(
                                    version = version,
                                    saveName = saveName
                                )
                            )
                        },
                        submitError = submitError
                    )
                }
                entry<NormalNavKey.Versions.ResourcePackManager> {
                    ResourcePackManageScreen(
                        mainScreenKey = mainScreenKey,
                        versionsScreenKey = versionsScreenKey,
                        version = version,
                        backToMainScreen = backToMainScreen,
                        swapToDownload = {
                            backScreenViewModel.navigateToDownload(
                                targetScreen = backScreenViewModel.downloadResourcePackScreen
                            )
                        },
                        submitError = submitError
                    )
                }
                entry<NormalNavKey.Versions.ShadersManager> {
                    ShadersManagerScreen(
                        mainScreenKey = mainScreenKey,
                        versionsScreenKey = versionsScreenKey,
                        version = version,
                        backToMainScreen = backToMainScreen,
                        swapToDownload = {
                            backScreenViewModel.navigateToDownload(
                                targetScreen = backScreenViewModel.downloadShadersScreen
                            )
                        },
                        submitError = submitError
                    )
                }
                entry<NormalNavKey.Versions.ScreenshotsManager> {
                    ScreenshotsManagerScreen(
                        mainScreenKey = mainScreenKey,
                        versionsScreenKey = versionsScreenKey,
                        version = version,
                        backToMainScreen = backToMainScreen,
                        eventViewModel = eventViewModel,
                        submitError = submitError
                    )
                }
                entry<NormalNavKey.Versions.ServerList> {
                    ServerListScreen(
                        mainScreenKey = mainScreenKey,
                        versionsScreenKey = versionsScreenKey,
                        version = version,
                        onQuickPlay = { version, address ->
                            eventViewModel.sendEvent(
                                EventViewModel.Event.Launch.PlayServer(
                                    version = version,
                                    address = address
                                )
                            )
                        },
                        backToMainScreen = backToMainScreen,
                    )
                }
            }
        )
    } else {
        Box(modifier)
    }
}

@Composable
private fun UpdateLoaderOperation(
    operation: UpdateLoaderOperation,
    changeOperation: (UpdateLoaderOperation) -> Unit,
    installer: GameInstaller?,
    onInstall: (GameDownloadInfo) -> Unit,
    onCancel: () -> Unit
) {
    when (operation) {
        is UpdateLoaderOperation.None -> {}
        is UpdateLoaderOperation.WarningForNotification -> {
            NotificationCheck(
                text = stringResource(R.string.notification_data_jvm_service_message),
                onGranted = {
                    changeOperation(UpdateLoaderOperation.Tip(operation.diffs, operation.info))
                },
                onIgnore = {
                    changeOperation(UpdateLoaderOperation.Tip(operation.diffs, operation.info))
                },
                onDismiss = {
                    changeOperation(UpdateLoaderOperation.None)
                }
            )
        }
        is UpdateLoaderOperation.Tip -> {
            val dismiss = {
                changeOperation(UpdateLoaderOperation.None)
            }
            AlertDialog(
                onDismissRequest = dismiss,
                title = {
                    Text(text = stringResource(R.string.generic_tip))
                },
                text = {
                    val scrollState = rememberScrollState()
                    Column(
                        modifier = Modifier
                            .fadeEdge(state = scrollState)
                            .verticalScrollWithBar(state = scrollState),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(text = stringResource(R.string.versions_update_loader_diff_message))

                        operation.diffs.list.forEach { diff ->
                            val modloader = diff.getLoader().displayName
                            val string = when (diff) {
                                is AddonDiffs.VersionChangeDiff -> {
                                    stringResource(R.string.versions_update_loader_diff_change, modloader, diff.original, diff.updateTo)
                                }
                                is AddonDiffs.RemoveDiff -> {
                                    stringResource(R.string.versions_update_loader_diff_remove, modloader)
                                }
                                is AddonDiffs.NewLoadDiff -> {
                                    stringResource(R.string.versions_update_loader_diff_load, modloader, diff.version)
                                }
                            }
                            Text(text = string)
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { onInstall(operation.info) }
                    ) {
                        MarqueeText(text = stringResource(R.string.generic_confirm))
                    }
                },
                dismissButton = {
                    Button(onClick = dismiss) {
                        MarqueeText(text = stringResource(R.string.generic_cancel))
                    }
                }
            )
        }
        is UpdateLoaderOperation.Install -> {
            if (installer != null) {
                val updateLoader by installer.tasksFlow.collectAsStateWithLifecycle()
                val installLog = installer.logOutput.collectAsStateWithLifecycle()
                if (updateLoader.isNotEmpty()) {
                    TitleTaskFlowDialog(
                        title = stringResource(R.string.versions_update_loader),
                        tasks = updateLoader,
                        onCancel = {
                            onCancel()
                            changeOperation(UpdateLoaderOperation.None)
                        },
                        logOutput = installLog.value
                    )
                }
            }
        }
        is UpdateLoaderOperation.Error -> {
            val th = operation.th
            Logger.error(TAG, "Failed to download the game!", th)
            val message = when (th) {
                is HttpRequestTimeoutException, is SocketTimeoutException, is TimeoutException -> stringResource(R.string.error_timeout)
                is UnknownHostException, is UnresolvedAddressException -> stringResource(R.string.error_network_unreachable)
                is ConnectException -> stringResource(R.string.error_connection_failed)
                is SerializationException, is JsonSyntaxException -> stringResource(R.string.error_parse_failed)
                is CantFetchingOptiFineUrlException -> stringResource(R.string.download_install_error_cant_fetch_optifine_download_url)
                is JvmCrashException -> stringResource(R.string.download_install_error_jvm_crash, th.code)
                is DownloadFailedException -> stringResource(R.string.download_install_error_download_failed)
                else -> when {
                    th.isProcessStartRefused() -> stringResource(R.string.download_install_error_process_start)
                    else -> th.localizedMessage ?: th.message ?: th::class.qualifiedName ?: "Unknown error"
                }
            }
            val dismiss = {
                changeOperation(UpdateLoaderOperation.None)
            }
            AlertDialog(
                onDismissRequest = dismiss,
                title = {
                    Text(text = stringResource(R.string.download_install_error_title))
                },
                text = {
                    val scrollState = rememberScrollState()
                    Column(
                        modifier = Modifier
                            .fadeEdge(state = scrollState)
                            .verticalScrollWithBar(state = scrollState),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(text = stringResource(R.string.versions_update_loader_error_message))
                        Text(text = message)
                    }
                },
                confirmButton = {
                    Button(onClick = dismiss) {
                        MarqueeText(text = stringResource(R.string.generic_confirm))
                    }
                }
            )
        }
    }
}
