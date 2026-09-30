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

package com.movtery.zalithlauncher.ui.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.movtery.guide.GuideHost
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.context.COPY_LABEL_LINK
import com.movtery.zalithlauncher.coroutine.Task
import com.movtery.zalithlauncher.coroutine.TaskSystem
import com.movtery.zalithlauncher.filemanager.FileManagerLauncher
import com.movtery.zalithlauncher.filemanager.events.FileManagerEvent
import com.movtery.zalithlauncher.filemanager.events.FileManagerEventRegistrar
import com.movtery.zalithlauncher.game.control.ControlManager
import com.movtery.zalithlauncher.game.path.getVersionsHome
import com.movtery.zalithlauncher.game.plugin.PluginLoader
import com.movtery.zalithlauncher.game.renderer.Renderers
import com.movtery.zalithlauncher.game.version.installed.Version
import com.movtery.zalithlauncher.game.version.installed.VersionsManager
import com.movtery.zalithlauncher.notification.NotificationManager
import com.movtery.zalithlauncher.path.PathManager
import com.movtery.zalithlauncher.path.URL_SUPPORT
import com.movtery.zalithlauncher.setting.AllSettings
import com.movtery.zalithlauncher.ui.AndroidStringText
import com.movtery.zalithlauncher.ui.androidText
import com.movtery.zalithlauncher.ui.base.BaseAppCompatActivity
import com.movtery.zalithlauncher.ui.base.ObserveFullScreenSetting
import com.movtery.zalithlauncher.ui.buildAppendedText
import com.movtery.zalithlauncher.ui.components.SimpleAlertDialog
import com.movtery.zalithlauncher.ui.guide.NextTipLabel
import com.movtery.zalithlauncher.ui.guide.rememberAppGuides
import com.movtery.zalithlauncher.ui.modrinth.*
import com.movtery.zalithlauncher.ui.screens.NestedNavKey
import com.movtery.zalithlauncher.ui.screens.NormalNavKey
import com.movtery.zalithlauncher.ui.screens.content.elements.Background
import com.movtery.zalithlauncher.ui.screens.content.elements.LaunchGameOperation
import com.movtery.zalithlauncher.ui.screens.content.elements.TitleTaskFlowDialog
import com.movtery.zalithlauncher.ui.screens.content.navigateToLogView
import com.movtery.zalithlauncher.ui.screens.content.navigateToWeb
import com.movtery.zalithlauncher.ui.screens.main.crashlogs.LogShareMenu
import com.movtery.zalithlauncher.ui.screens.main.crashlogs.LogShareMenuOperation
import com.movtery.zalithlauncher.ui.screens.main.crashlogs.ShareLinkOperation
import com.movtery.zalithlauncher.ui.theme.ZalithLauncherTheme
import com.movtery.zalithlauncher.ui.theme.festivals.FestivalEffects
import com.movtery.zalithlauncher.ui.theme.festivals.FestivalTapObserver
import com.movtery.zalithlauncher.ui.theme.showThemed
import com.movtery.zalithlauncher.ui.toAndroidString
import com.movtery.zalithlauncher.ui.vulkan_checker.VCOperation
import com.movtery.zalithlauncher.ui.vulkan_checker.VulkanChecker
import com.movtery.zalithlauncher.upgrade.TooFrequentOperationException
import com.movtery.zalithlauncher.utils.compareLangTag
import com.movtery.zalithlauncher.utils.copyText
import com.movtery.zalithlauncher.utils.festival.getTodayFestivals
import com.movtery.zalithlauncher.utils.file.shareFile
import com.movtery.zalithlauncher.utils.isChinese
import com.movtery.zalithlauncher.utils.logging.Logger
import com.movtery.zalithlauncher.utils.network.openLink
import com.movtery.zalithlauncher.utils.network.openLinkInternal
import com.movtery.zalithlauncher.utils.string.getMessageOrToString
import com.movtery.zalithlauncher.viewmodel.BackgroundViewModel
import com.movtery.zalithlauncher.viewmodel.ErrorViewModel
import com.movtery.zalithlauncher.viewmodel.EventViewModel
import com.movtery.zalithlauncher.viewmodel.LaunchGameViewModel
import com.movtery.zalithlauncher.viewmodel.LauncherUpgradeOperation
import com.movtery.zalithlauncher.viewmodel.LauncherUpgradeViewModel
import com.movtery.zalithlauncher.viewmodel.LogShareViewModel
import com.movtery.zalithlauncher.viewmodel.LogsUploadViewModel
import com.movtery.zalithlauncher.viewmodel.ModpackConfirmUseMobileDataOperation
import com.movtery.zalithlauncher.viewmodel.ModpackImportOperation
import com.movtery.zalithlauncher.viewmodel.ModpackImportViewModel
import com.movtery.zalithlauncher.viewmodel.ModpackVersionNameOperation
import com.movtery.zalithlauncher.viewmodel.ScreenBackStackViewModel
import com.movtery.zalithlauncher.viewmodel.VulkanCheckerViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Locale

private const val TAG = "MainActivity"

@AndroidEntryPoint
class MainActivity : BaseAppCompatActivity() {
    override fun isIgnoreNotch(): Boolean = AllSettings.launcherFullScreen.getValue()

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        FestivalTapObserver.observe(event)
        return super.dispatchTouchEvent(event)
    }

    /**
     * 屏幕堆栈管理ViewModel
     */
    private val screenBackStackModel: ScreenBackStackViewModel by viewModels()

    /**
     * 启动游戏ViewModel
     */
    private val launchGameViewModel: LaunchGameViewModel by viewModels()

    /**
     * 错误信息ViewModel
     */
    private val errorViewModel: ErrorViewModel by viewModels()

    /**
     * 与Compose交互的事件ViewModel
     */
    val eventViewModel: EventViewModel by viewModels()

    /**
     * 启动器背景内容管理 ViewModel
     */
    val backgroundViewModel: BackgroundViewModel by viewModels()

    /**
     * 整合包导入 ViewModel
     */
    val modpackImportViewModel: ModpackImportViewModel by viewModels()

    /**
     * 启动器更新状态 ViewModel
     */
    val launcherUpgradeViewModel: LauncherUpgradeViewModel by viewModels()

    /**
     * 游戏日志分享菜单 ViewModel
     */
    private val logShareViewModel: LogShareViewModel by viewModels()

    /**
     * 游戏日志上传 ViewModel
     */
    private val logsUploadViewModel: LogsUploadViewModel by viewModels()

    /**
     * Vulkan检测状态 ViewModel
     */
    private val vulkanCheckerViewModel: VulkanCheckerViewModel by viewModels()

    /**
     * 是否开启捕获按键模式
     */
    private var isCaptureKey = false

    /**
     * 文件管理器事件监听
     */
    private var fmEventRegistrar: FileManagerEventRegistrar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //处理外部导入
        val isImporting = handleImportIfNeeded(intent)

        //加载渲染器
        Renderers.init()
        //加载插件
        PluginLoader.loadAllPlugins(this, false)
        refreshData()

        //注册文件管理器事件监听
        fmEventRegistrar = FileManagerEventRegistrar(this, ::onFileManagerEvent).also { it.start() }

        //初始化通知管理（创建渠道）
        NotificationManager.initManager(this)

        //检查更新
        if (!isImporting && launcherUpgradeViewModel.operation == LauncherUpgradeOperation.None) {
            lifecycleScope.launch {
                launcherUpgradeViewModel.checkOnAppStart()
            }
        }

        //错误信息展示
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                errorViewModel.errorEvents.collect { tm ->
                    errorViewModel.showErrorDialog(
                        context = this@MainActivity,
                        tm = tm
                    )
                }
            }
        }

        //事件处理
        lifecycleScope.launch {
            eventViewModel.events.collect { event ->
                when (event) {
                    is EventViewModel.Event.Key.StartKeyCapture -> {
                        Logger.info("CollectEvent", "Start key capture!")
                        isCaptureKey = true
                    }
                    is EventViewModel.Event.Key.StopKeyCapture -> {
                        Logger.info("CollectEvent", "Stop key capture!")
                        isCaptureKey = false
                    }
                    is EventViewModel.Event.OpenLink -> {
                        val url = event.url
                        withContext(Dispatchers.Main) {
                            this@MainActivity.openLink(url)
                        }
                    }
                    is EventViewModel.Event.CheckUpdate -> {
                        checkUpdate()
                    }
                    is EventViewModel.Event.KeepScreen -> {
                        keepScreen(event.on)
                    }
                    is EventViewModel.Event.ImportControls -> {
                        importControlFiles(event.uris)
                    }
                    is EventViewModel.Event.DownloadPlugins -> {
                        showDownloadPlugins(event.link)
                    }
                    is EventViewModel.Event.Launch.Game -> {
                        launchGameViewModel.tryLaunch(event.version)
                    }
                    is EventViewModel.Event.Launch.PlayServer -> {
                        launchGameViewModel.quickPlayServer(event.version, event.address)
                    }
                    is EventViewModel.Event.Launch.PlaySave -> {
                        launchGameViewModel.quickPlaySave(event.version, event.saveName)
                    }
                    is EventViewModel.Event.LogShare.ShareGameLog -> {
                        val file = event.logFile
                        if (file.exists()) {
                            logsUploadViewModel.check(file)
                            logShareViewModel.openMenu(file)
                        }
                    }
                    is EventViewModel.Event.VulkanCheck -> {
                        checkVulkan(event.version)
                    }
                    is EventViewModel.Event.ShowToast -> {
                        Toast.makeText(
                            this@MainActivity,
                            event.text.toAndroidString(this@MainActivity),
                            event.duration
                        ).show()
                    }
                    is EventViewModel.Event.OpenFileManager -> {
                        FileManagerLauncher.launch(
                            context = this@MainActivity,
                            rootPath = event.rootPath,
                            currentPath = event.currentPath,
                            logsDir = PathManager.DIR_LAUNCHER_LOGS.absolutePath
                        )
                    }
                    else -> {
                        //忽略
                    }
                }
            }
        }

        val finishedGame = AllSettings.finishedGame
        val showSponsorship = AllSettings.showSponsorship

        val festivals = getTodayFestivals(
            containsChinese = isChinese(this@MainActivity)
        )

        setContent {
            ZalithLauncherTheme(
                backgroundViewModel = backgroundViewModel,
                festivals = festivals
            ) {
                ObserveFullScreenSetting(AllSettings.launcherFullScreen.state)

                val guides = rememberAppGuides(eventViewModel)
                GuideHost(
                    guides.mainScreen,
                    nextTip = { NextTipLabel(it) }
                ) {
                    Background(
                        modifier = Modifier.fillMaxSize(),
                        viewModel = backgroundViewModel
                    )

                    val dummyJumpIn = listOf(
                        GameInstance("Redstone Playground", "Minecraft 26.2"),
                        GameInstance("Wynncraft", "Wynncraft v2.2.3"),
                        GameInstance("Cobblemon", "Fabric 1.21.1")
                    )
                    val dummyLibrary = listOf(
                        GameInstance("Cobblemon", "Fabric 1.21.1"),
                        GameInstance("Modrinth SMP", "Neoforge 26.1.2"),
                        GameInstance("QoL Mods", "Fabric 26.2"),
                        GameInstance("Redstone Play...", "Vanilla 26.2"),
                        GameInstance("Wynncraft", "Vanilla 1.21.11")
                    )
                    val dummyOnlineFriends = listOf(
                        Friend("Stantios", "Playing Minecraft"),
                        Friend("coolbot100s", "Online")
                    )
                    val dummyOfflineFriends = List(14) { Friend("Friend $it", "Offline") }

                    ModrinthAppLayout(
                        sidebarContent = {
                            ModrinthSidebar(
                                destinations = listOf(
                                    SidebarDestination.Home,
                                    SidebarDestination.Discover,
                                    SidebarDestination.Library,
                                    SidebarDestination.Settings
                                ),
                                currentRoute = "home",
                                onNavigate = { /* Navigation logic to be added later */ }
                            )
                        },
                        mainContent = {
                            ModrinthMainContent(
                                jumpInItems = dummyJumpIn,
                                libraryItems = dummyLibrary,
                                onPlayClick = { /* Game launch logic to be added later */ }
                            )
                        },
                        rightPanelContent = {
                            ModrinthRightPanel(
                                currentUser = UserProfile("ProspectorDev", "Minecraft account"),
                                onlineFriends = dummyOnlineFriends,
                                offlineFriends = dummyOfflineFriends
                            )
                        }
                    )

                    //节日彩蛋效果层
                    FestivalEffects(
                        modifier = Modifier.fillMaxSize(),
                        festivals = festivals
                    )

                    //启动游戏操作流程
                    LaunchGameOperation(
                        activity = this@MainActivity,
                        eventViewModel = eventViewModel,
                        launchGameViewModel = launchGameViewModel,
                        exitActivity = {
                            this@MainActivity.finish()
                        },
                        ensureVulkanSupported = vulkanCheckerViewModel::ensureSupported,
                        submitError = {
                            errorViewModel.showError(it)
                        },
                        toAccountManageScreen = { menu ->
                            screenBackStackModel.mainScreen.navigateTo(
                                screenKey = NormalNavKey.AccountManager(menu)
                            )
                        },
                        toVersionManageScreen = {
                            screenBackStackModel.mainScreen.removeAndNavigateTo(
                                remove = NestedNavKey.VersionSettings::class,
                                screenKey = NormalNavKey.VersionsManager
                            )
                        },
                        navigateToWeb = { url ->
                            screenBackStackModel.mainScreen.backStack.navigateToWeb(url)
                        },
                        backToMain = {
                            screenBackStackModel.mainScreen.clearWith(NormalNavKey.LauncherMain)
                        },
                        checkIfInWebScreen = {
                            screenBackStackModel.mainScreen.currentKey is NormalNavKey.WebScreen
                        }
                    )

                    //启动游戏流程展示
                    val launchFlow by launchGameViewModel.launchFlow.collectAsStateWithLifecycle()
                    val flow = launchFlow
                    if (flow != null) {
                        val launchTasks by flow.tasksFlow.collectAsStateWithLifecycle()
                        TitleTaskFlowDialog(
                            title = stringResource(R.string.main_launch_game),
                            tasks = launchTasks,
                            onCancel = {
                                launchGameViewModel.cancel()
                            }
                        )
                    }
                }

                //显示赞助支持的小弹窗
                if (!isImporting && finishedGame.state >= 100 && showSponsorship.state) {
                    SimpleAlertDialog(
                        title = stringResource(R.string.about_sponsor),
                        text = stringResource(R.string.game_saponsorship_finished_game, finishedGame.state),
                        dismissText = stringResource(R.string.generic_close),
                        onDismiss = {
                            showSponsorship.save(false)
                        },
                        onConfirm = {
                            showSponsorship.save(false)
                            eventViewModel.sendEvent(
                                EventViewModel.Event.OpenLink(URL_SUPPORT)
                            )
                        }
                    )
                }

                ModpackImportOperation(
                    operation = modpackImportViewModel.importOperation,
                    changeOperation = { modpackImportViewModel.importOperation = it },
                    importer = modpackImportViewModel.importer,
                    onCancel = {
                        modpackImportViewModel.cancel()
                        lifecycleScope.launch {
                            keepScreen(false)
                        }
                    }
                )

                //用户确认版本名称 操作流程
                ModpackVersionNameOperation(
                    operation = modpackImportViewModel.versionNameOperation,
                    onConfirmVersionName = { name ->
                        modpackImportViewModel.confirmVersionName(name)
                    },
                    onCancel = {
                        modpackImportViewModel.cancel()
                    }
                )

                //用户确认使用移动网络 操作流程
                ModpackConfirmUseMobileDataOperation(
                    operation = modpackImportViewModel.confirmMobileDataOperation,
                    onConfirmUse = { use ->
                        modpackImportViewModel.confirmUseMobileData(use)
                    }
                )

                //游戏日志分享菜单
                val lo
