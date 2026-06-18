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

package com.movtery.zalithlauncher.ui.screens.content.elements

import android.app.Activity
import android.net.Uri
import android.os.Parcelable
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import coil3.ImageLoader
import coil3.compose.AsyncImage
import coil3.gif.GifDecoder
import coil3.request.ImageRequest
import coil3.request.allowHardware
import coil3.request.crossfade
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.game.account.AccountsManager
import com.movtery.zalithlauncher.game.launch.LaunchGame
import com.movtery.zalithlauncher.game.plugin.ApkPlugin
import com.movtery.zalithlauncher.game.plugin.natives.NativePluginManager
import com.movtery.zalithlauncher.game.plugin.renderer.RendererPluginManager
import com.movtery.zalithlauncher.game.renderer.RendererInterface
import com.movtery.zalithlauncher.game.renderer.Renderers
import com.movtery.zalithlauncher.game.version.installed.Version
import com.movtery.zalithlauncher.setting.AllSettings
import com.movtery.zalithlauncher.setting.enums.BackgroundBlur
import com.movtery.zalithlauncher.ui.components.SimpleAlertDialog
import com.movtery.zalithlauncher.ui.components.VideoPlayer
import com.movtery.zalithlauncher.ui.screens.content.FirstLoginMenu
import com.movtery.zalithlauncher.utils.canHandlePermission
import com.movtery.zalithlauncher.utils.checkStoragePermissions
import com.movtery.zalithlauncher.utils.file.InvalidFilenameException
import com.movtery.zalithlauncher.utils.file.checkFilenameValidity
import com.movtery.zalithlauncher.utils.hasStoragePermission
import com.movtery.zalithlauncher.utils.string.isBiggerTo
import com.movtery.zalithlauncher.utils.string.isLowerTo
import com.movtery.zalithlauncher.viewmodel.BackgroundViewModel
import com.movtery.zalithlauncher.viewmodel.ErrorViewModel
import com.movtery.zalithlauncher.viewmodel.LocalBackgroundViewModel
import dev.chrisbanes.haze.HazeInputScale
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.blur.HazeColorEffect
import dev.chrisbanes.haze.blur.blurEffect
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.parcelize.Parcelize
import java.io.File
import kotlin.math.sqrt

@Parcelize
sealed interface QuickPlay : Parcelable {
    /** 快速启动游玩存档  仅支持 1.20+ 23w14a+ */
    @Parcelize
    data class Save(val saveName: String): QuickPlay

    /** 快速启动游玩服务器 */
    @Parcelize
    data class Server(val serverAddress: String): QuickPlay
}

sealed interface LaunchGameOperation {
    data object None : LaunchGameOperation
    /** 没有安装版本/没有选中有效版本 */
    data object NoVersion : LaunchGameOperation
    /** 版本名称非法时 */
    data class InvalidVersionName(val th: InvalidFilenameException) : LaunchGameOperation
    /** 没有可用账号 */
    data object NoAccount : LaunchGameOperation

    /** 渲染器可配置，但需要用到文件管理权限 */
    data class RendererNoStoragePermission(
        val renderer: RendererInterface,
        val version: Version,
        val quickPlay: QuickPlay?
    ) : LaunchGameOperation

    /** 当前渲染器不支持选中版本 */
    data class UnsupportedRenderer(
        val renderer: RendererInterface,
        val version: Version,
        val quickPlay: QuickPlay?
    ): LaunchGameOperation

    /** 当前已加载的插件不支持选中的版本 */
    data class UnsupportedPlugins(
        val plugins: List<ApkPlugin>,
        val version: Version,
        val quickPlay: QuickPlay?
    ): LaunchGameOperation

    /** 尝试启动：启动前检查一些东西 */
    data class TryLaunch(
        val version: Version?,
        val quickPlay: QuickPlay? = null
    ) : LaunchGameOperation

    /** 正式启动 */
    data class RealLaunch(
        val version: Version,
        val quickPlay: QuickPlay?
    ) : LaunchGameOperation
}

@Composable
fun LaunchGameOperation(
    activity: Activity,
    launchGameOperation: LaunchGameOperation,
    updateOperation: (LaunchGameOperation) -> Unit,
    exitActivity: () -> Unit,
    waitForVulkanChecker: suspend () -> Unit,
    submitError: (ErrorViewModel.ThrowableMessage) -> Unit,
    toAccountManageScreen: (FirstLoginMenu) -> Unit = {},
    toVersionManageScreen: () -> Unit = {}
) {
    when (launchGameOperation) {
        is LaunchGameOperation.None -> {}
        is LaunchGameOperation.NoVersion -> {
            LaunchedEffect(Unit) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(activity, R.string.game_launch_no_version, Toast.LENGTH_SHORT).show()
                }
                toVersionManageScreen()
                updateOperation(LaunchGameOperation.None)
            }
        }
        is LaunchGameOperation.InvalidVersionName -> {
            val th = launchGameOperation.th
            SimpleAlertDialog(
                title = stringResource(R.string.versions_manage_invalid),
                text = th.getInvalidSummary(),
                confirmText = stringResource(R.string.generic_cancel),
                onDismiss = {
                    updateOperation(LaunchGameOperation.None)
                }
            )
        }
        is LaunchGameOperation.NoAccount -> {
            LaunchedEffect(Unit) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(activity, R.string.game_launch_no_account, Toast.LENGTH_SHORT).show()
                }
                val isOffline = AccountsManager.isOffline.value
                toAccountManageScreen(
                    if (isOffline) FirstLoginMenu.MICROSOFT
                    else FirstLoginMenu.NORMAL
                )
                updateOperation(LaunchGameOperation.None)
            }
        }
        is LaunchGameOperation.RendererNoStoragePermission -> {
            LaunchedEffect(Unit) {
                val renderer = launchGameOperation.renderer
                val version = launchGameOperation.version
                val quickPlay = launchGameOperation.quickPlay
                withContext(Dispatchers.Main) {
                    checkStoragePermissions(
                        activity = activity,
                        message = activity.getString(R.string.renderer_version_storage_permissions, renderer.getRendererName()),
                        messageSdk30 = activity.getString(R.string.renderer_version_storage_permissions_sdk30, renderer.getRendererName()),
                        onDialogCancel = {
                            //用户拒绝授权，但仍然允许启动（不过这会导致配置无法读取）
                            updateOperation(LaunchGameOperation.RealLaunch(version, quickPlay))
                        }
                    )
                }
                updateOperation(LaunchGameOperation.None)
            }
        }
        is LaunchGameOperation.UnsupportedRenderer -> {
            val renderer = launchGameOperation.renderer
            val version = launchGameOperation.version
            val quickPlay = launchGameOperation.quickPlay
            SimpleAlertDialog(
                title = stringResource(R.string.generic_warning),
                text = stringResource(R.string.renderer_version_unsupported_warning, renderer.getRendererName()),
                confirmText = stringResource(R.string.generic_anyway),
                onConfirm = {
                    updateOperation(LaunchGameOperation.RealLaunch(version, quickPlay))
                },
                onDismiss = {
                    updateOperation(LaunchGameOperation.None)
                }
            )
        }
        is LaunchGameOperation.UnsupportedPlugins -> {
            val plugins = launchGameOperation.plugins
            val version = launchGameOperation.version
            val quickPlay = launchGameOperation.quickPlay
            SimpleAlertDialog(
                title = stringResource(R.string.generic_warning),
                text = stringResource(R.string.plugin_unsupported_warning, plugins.joinToString(", ") { it.appName }),
                confirmText = stringResource(R.string.generic_anyway),
                onConfirm = {
                    updateOperation(LaunchGameOperation.RealLaunch(version, quickPlay))
                },
                onDismiss = {
                    updateOperation(LaunchGameOperation.None)
                }
            )
        }
        is LaunchGameOperation.TryLaunch -> {
            LaunchedEffect(Unit) {
                val version = launchGameOperation.version ?: run {
                    updateOperation(LaunchGameOperation.NoVersion)
                    return@LaunchedEffect
                }

                try {
                    checkFilenameValidity(version.getVersionName())
                } catch (th: InvalidFilenameException) {
                    updateOperation(LaunchGameOperation.InvalidVersionName(th))
                    return@LaunchedEffect
                }

                val quickPlay = launchGameOperation.quickPlay

                AccountsManager.currentAccountFlow.value ?: run {
                    updateOperation(LaunchGameOperation.NoAccount)
                    return@LaunchedEffect
                }

                //开始检查渲染器的版本支持情况
                Renderers.setCurrentRenderer(activity, version.getRenderer())
                val currentRenderer = Renderers.getCurrentRenderer()
                val rendererMinVer = currentRenderer.getMinMCVersion()
                val rendererMaxVer = currentRenderer.getMaxMCVersion()

                val mcVer = version.getVersionInfo()!!.minecraftVersion

                val isRendererUnsupported =
                    (rendererMinVer?.let { mcVer.isLowerTo(it) } ?: false) ||
                            (rendererMaxVer?.let { mcVer.isBiggerTo(it) } ?: false)

                if (isRendererUnsupported) {
                    updateOperation(LaunchGameOperation.UnsupportedRenderer(currentRenderer, version, quickPlay))
                    return@LaunchedEffect
                }

                val unsupportedPlugins = NativePluginManager.getCheckedPlugins().filter { plugin ->
                    (plugin.minMCVer?.let { mcVer.isLowerTo(it) } ?: false) ||
                            (plugin.maxMCVer?.let { mcVer.isBiggerTo(it) } ?: false)
                }
                if (unsupportedPlugins.isNotEmpty()) {
                    updateOperation(LaunchGameOperation.UnsupportedPlugins(unsupportedPlugins, version, quickPlay))
                    return@LaunchedEffect
                }

                //为可配置的渲染器检查文件管理权限
                //前提：系统支持这个设置
                if (
                    canHandlePermission &&  !hasStoragePermission &&
                    RendererPluginManager.isConfigurablePlugin(version.getRenderer())
                ) {
                    updateOperation(LaunchGameOperation.RendererNoStoragePermission(currentRenderer, version, quickPlay))
                    return@LaunchedEffect
                }

                //正式启动游戏
                updateOperation(LaunchGameOperation.RealLaunch(version, quickPlay))
            }
        }
        is LaunchGameOperation.RealLaunch -> {
            LaunchedEffect(Unit) {
                val version = launchGameOperation.version
                val quickPlay = launchGameOperation.quickPlay
                version.apply {
                    offlineAccountLogin = false
                    quickPlaySingle = quickPlay
                }
                LaunchGame.launchGame(
                    context = activity,
                    version = version,
                    exitActivity = exitActivity,
                    waitForVulkanChecker = waitForVulkanChecker,
                    submitError = submitError
                )
                updateOperation(LaunchGameOperation.None)
            }
        }
    }
}

/**
 * 启动器背景图片/视频层
 */
@Composable
fun Background(
    viewModel: BackgroundViewModel,
    modifier: Modifier = Modifier,
    allowVideo: Boolean = true
) {
    Box(
        modifier = modifier.backgroundBlur(
            blur = AllSettings.backgroundBlur.state,
            hazeState = viewModel.hazeState,
        )
    ) {
        if (viewModel.isValid) {
            when {
                viewModel.isVideo && allowVideo -> {
                    VideoPlayer(
                        videoUri = Uri.fromFile(viewModel.backgroundFile),
                        modifier = Modifier.fillMaxSize(),
                        refreshTrigger = viewModel.refreshTrigger,
                        volume = AllSettings.videoBackgroundVolume.state / 100f
                    )
                }
                viewModel.isImage -> {
                    BackgroundImage(
                        modifier = Modifier.fillMaxSize(),
                        imageFile = viewModel.backgroundFile,
                        refreshTrigger = viewModel.refreshTrigger
                    )
                }
            }
        }
    }
}

@Composable
private fun Modifier.backgroundBlur(
    blur: Int,
    hazeState: HazeState,
): Modifier {
    return when (AllSettings.backgroundBlurType.state) {
        BackgroundBlur.Background -> this.glass(blur, null, null)
        BackgroundBlur.Foreground -> this.hazeSource(hazeState)
    }
}

/**
 * 背景模糊效果
 * @param enabled 是否应用模糊效果
 */
@Composable
fun Modifier.backgroundGlass(
    blur: Int,
    color: Color,
    enabled: Boolean = true,
): Modifier {
    if (AllSettings.backgroundBlurType.state == BackgroundBlur.Background) return this
    if (!enabled) return this
    val state = LocalBackgroundViewModel.current?.hazeState ?: return this
    return this.glass(blur, color, state)
}

/**
 * 背景模糊效果
 */
@Composable
private fun Modifier.glass(
    blur: Int,
    color: Color?,
    hazeState: HazeState?,
): Modifier {
    if (blur <= 0 || AllSettings.launcherBackgroundOpacity.state >= 100) return this

    val t = remember(blur) {
        (blur / 80f).coerceIn(0f, 1f)
    }

    val inputScale = remember(t) {
        val scale = lerp(
            start = 0.66f,
            stop = 0.8f,
            fraction = sqrt(t)
        )
        HazeInputScale.Fixed(scale)
    }
    val noiseFactor = remember(t) {
        lerp(
            start = 0.3f,
            stop = 0.25f,
            fraction = sqrt(t)
        )
    }
    val colorEffects = remember(t, color) {
        val whiteAlpha = lerp(
            start = 0f,
            stop = 0.25f,
            fraction = sqrt(t)
        )
        buildList {
            if (color != null) {
                add(HazeColorEffect.tint(color, BlendMode.SrcOver))
            }
            add(HazeColorEffect.tint(Color.White.copy(alpha = whiteAlpha), BlendMode.Softlight))
        }
    }

    return this
        .hazeEffect(hazeState) {
            this.inputScale = inputScale
            blurEffect {
                this.blurEnabled = true
                this.blurRadius = blur.dp
                this.noiseFactor = noiseFactor
                this.colorEffects = colorEffects
            }
        }
}

@Composable
private fun BackgroundImage(
    refreshTrigger: Any,
    imageFile: File,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val imageLoader = remember(refreshTrigger) {
        ImageLoader.Builder(context)
            .components { add(GifDecoder.Factory()) }
            .build()
    }
    val request = remember(refreshTrigger) {
        ImageRequest.Builder(context)
            .data(imageFile)
            .allowHardware(false)
            .crossfade(false)
            .build()
    }

    AsyncImage(
        modifier = modifier,
        model = request,
        imageLoader = imageLoader,
        contentDescription = null,
        contentScale = ContentScale.Crop
    )
}