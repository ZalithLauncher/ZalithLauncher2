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
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.context.COPY_LABEL_LINK
import com.movtery.zalithlauncher.coroutine.Task
import com.movtery.zalithlauncher.coroutine.TaskSystem
import com.movtery.zalithlauncher.game.control.ControlManager
import com.movtery.zalithlauncher.game.plugin.driver.DriverPluginManager
import com.movtery.zalithlauncher.game.version.installed.VersionsManager
import com.movtery.zalithlauncher.notification.NotificationManager
import com.movtery.zalithlauncher.path.PathManager
import com.movtery.zalithlauncher.path.URL_SUPPORT
import com.movtery.zalithlauncher.setting.AllSettings
import com.movtery.zalithlauncher.ui.base.BaseAppCompatActivity
import com.movtery.zalithlauncher.ui.components.SimpleAlertDialog
import com.movtery.zalithlauncher.ui.screens.NestedNavKey
import com.movtery.zalithlauncher.ui.screens.NormalNavKey
import com.movtery.zalithlauncher.ui.screens.content.elements.Background
import com.movtery.zalithlauncher.ui.screens.content.elements.LaunchGameOperation
import com.movtery.zalithlauncher.ui.screens.content.navigateToLogView
import com.movtery.zalithlauncher.ui.screens.main.MainScreen
import com.movtery.zalithlauncher.ui.screens.main.crashlogs.LogShareMenu
import com.movtery.zalithlauncher.ui.screens.main.crashlogs.LogShareMenuOperation
import com.movtery.zalithlauncher.ui.screens.main.crashlogs.ShareLinkOperation
import com.movtery.zalithlauncher.ui.theme.ZalithLauncherTheme
import com.movtery.zalithlauncher.ui.theme.feativals.FestivalEffects
import com.movtery.zalithlauncher.ui.vulkan_checker.VCOperation
import com.movtery.zalithlauncher.ui.vulkan_checker.VulkanChecker
import com.movtery.zalithlauncher.upgrade.TooFrequentOperationException
import com.movtery.zalithlauncher.utils.compareLangTag
import com.movtery.zalithlauncher.utils.copyText
import com.movtery.zalithlauncher.utils.device.VulkanChecker
import com.movtery.zalithlauncher.utils.festival.getTodayFestivals
import com.movtery.zalithlauncher.utils.file.shareFile
import com.movtery.zalithlauncher.utils.isChinese
import com.movtery.zalithlauncher.utils.logging.Logger.lInfo
import com.movtery.zalithlauncher.utils.logging.Logger.lWarning
import com.movtery.zalithlauncher.utils.network.openLink
import com.movtery.zalithlauncher.utils.network.openLinkInternal
import com.movtery.zalithlauncher.utils.string.getMessageOrToString
import com.movtery.zalithlauncher.viewmodel.BackgroundViewModel
import com.movtery.zalithlauncher.viewmodel.ErrorViewModel
import com.movtery.zalithlauncher.viewmodel.EventViewModel
import com.movtery.zalithlauncher.viewmodel.HomePageOperation
import com.movtery.zalithlauncher.viewmodel.HomePageViewModel
import com.movtery.zalithlauncher.viewmodel.LaunchGameViewModel
import com.movtery.zalithlauncher.viewmodel.LauncherUpgradeOperation
import com.movtery.zalithlauncher.viewmodel.LauncherUpgradeViewModel
import com.movtery.zalithlauncher.viewmodel.LocalHomePageViewModel
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

@AndroidEntryPoint
class MainActivity : BaseAppCompatActivity() {
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
     * 启动器自定义主页 ViewModel
     */
    val homePageViewModel: HomePageViewModel by viewModels()

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //初始化通知管理（创建渠道）
        NotificationManager.initManager(this)

        //处理外部导入
        val isImporting = handleImportIfNeeded(intent)

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
                        lInfo("Start key capture!")
                        isCaptureKey = true
                    }
                    is EventViewModel.Event.Key.StopKeyCapture -> {
                        lInfo("Stop key capture!")
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
                    is EventViewModel.Event.HomePage.Reload -> {
                        homePageViewModel.reloadPage(true)
                    }
                    is EventViewModel.Event.HomePage.GenDocPage -> {
                        if (homePageViewModel.isLocalExists()) {
                            //如果本地主页文件已存在，则警告用户是否进行覆盖
                            homePageViewModel.updateOperation(
                                HomePageOperation.WarningOverwrite
                            )
                        } else {
                            homePageViewModel.genDocPage(this@MainActivity)
                        }
                    }
                    is EventViewModel.Event.HomePage.Event -> {
                        val event0 = event.event
                        handleHomePageEvent(event0.key, event0.data)
                    }
                    is EventViewModel.Event.VulkanCheck -> {
                        checkVulkan()
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
                Box {
                    Background(
                        modifier = Modifier.fillMaxSize(),
                        viewModel = backgroundViewModel
                    )

                    CompositionLocalProvider(
                        LocalHomePageViewModel provides homePageViewModel
                    ) {
                        MainScreen(
                            screenBackStackModel = screenBackStackModel,
                            eventViewModel = eventViewModel,
                            modpackImportViewModel = modpackImportViewModel,
                            submitError = {
                                errorViewModel.showError(it)
                            }
                        )
                    }

                    //节日彩蛋效果层
                    FestivalEffects(
                        modifier = Modifier.fillMaxSize(),
                        festivals = festivals
                    )

                    //启动游戏操作流程
                    LaunchGameOperation(
                        activity = this@MainActivity,
                        launchGameOperation = launchGameViewModel.launchGameOperation,
                        updateOperation = { launchGameViewModel.updateOperation(it) },
                        exitActivity = {
                            this@MainActivity.finish()
                        },
                        waitForVulkanChecker = vulkanCheckerViewModel::waitForVulkanChecker,
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
                        }
                    )
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

                //启动器主页操作流程
                val homePageOp by homePageViewModel.pageOp.collectAsStateWithLifecycle()
                HomePageOperation(
                    operation = homePageOp,
                    onChange = {
                        homePageViewModel.updateOperation(it)
                    },
                    onGenDocPage = {
                        homePageViewModel.genDocPage(this@MainActivity)
                    }
                )

                //游戏日志分享菜单
                val logFile = logShareViewModel.currentLogFile
                if (logShareViewModel.showMenu && logFile != null) {
                    LogShareMenu(
                        operation = LogShareMenuOperation.ShowMenu,
                        onChange = { operation ->
                            if (operation == LogShareMenuOperation.None) {
                                logShareViewModel.closeMenu()
                            }
                        },
                        onView = {
                            screenBackStackModel.mainScreen.backStack.navigateToLogView(
                                logPath = logFile.absolutePath
                            )
                            logShareViewModel.closeMenu()
                        },
                        onShare = {
                            shareFile(this@MainActivity, logFile)
                            logShareViewModel.closeMenu()
                        },
                        canUpload = logsUploadViewModel.canUpload,
                        onUpload = {
                            logsUploadViewModel.operation = ShareLinkOperation.Tip
                            logShareViewModel.closeMenu()
                        }
                    )
                }

                ShareLinkOperation(
                    operation = logsUploadViewModel.operation,
                    onChange = { logsUploadViewModel.operation = it },
                    onUploadChancel = { logsUploadViewModel.cancel() },
                    onUpload = {
                        logFile?.let { file ->
                            logsUploadViewModel.upload(file) { link ->
                                openLink(link)
                                copyText(COPY_LABEL_LINK, link, this@MainActivity)
                            }
                        }
                    }
                )

                //检查更新操作流程
                LauncherUpgradeOperation(
                    operation = launcherUpgradeViewModel.operation,
                    onChanged = { launcherUpgradeViewModel.operation = it },
                    onIgnoredClick = { ver ->
                        AllSettings.lastIgnoredVersion.save(ver)
                    },
                    onLinkClick = { eventViewModel.sendEvent(EventViewModel.Event.OpenLink(it)) }
                )

                val vcOperation by vulkanCheckerViewModel.vcOperation.collectAsStateWithLifecycle()
                VulkanChecker(
                    operation = vcOperation,
                    onChange = {
                        vulkanCheckerViewModel.changeOperation(it)
                    },
                    startCheck = {
                        eventViewModel.sendEvent(EventViewModel.Event.VulkanCheck)
                    },
                    confirmResult = {
                        vulkanCheckerViewModel.resumeCont()
                        AllSettings.autoVulkanChecker.save(false)
                    }
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleImportIfNeeded(intent)
    }

    /**
     * 检查设备 Vulkan 支持情况
     */
    private suspend fun checkVulkan() {
        val driver = DriverPluginManager.getDriver()
        val useTurnip = !(AllSettings.zinkPreferSystemDriver.getValue() || driver.isLauncher)

        withContext(Dispatchers.Main) {
            val result = if (useTurnip) {
                val tempDir = File(PathManager.DIR_CACHE, "vulkan_temp")
                VulkanChecker.checkCapabilities(null, driver.path, tempDir.absolutePath)
            } else {
                VulkanChecker.checkCapabilities(null, null, null)
            }
            vulkanCheckerViewModel.changeOperation(VCOperation.Result(result, useTurnip))
        }
    }

    /**
     * 检查启动器更新
     */
    private fun checkUpdate() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val success = launcherUpgradeViewModel.checkManually(
                    onInProgress = {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@MainActivity, getString(R.string.generic_in_progress), Toast.LENGTH_SHORT).show()
                        }
                    },
                    onIsLatest = {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@MainActivity, getString(R.string.upgrade_is_latest), Toast.LENGTH_SHORT).show()
                        }
                    }
                )
                if (!success) throw RuntimeException()
            } catch (_: TooFrequentOperationException) {
                //太频繁了
                return@launch
            } catch (_: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, getString(R.string.upgrade_get_remote_failed), Toast.LENGTH_SHORT).show()
                }
                return@launch
            }
        }
    }

    /**
     * 处理自定义主页的事件
     */
    private suspend fun handleHomePageEvent(
        key: String,
        data: String?
    ) {
        runCatching {
            when (key) {
                //浏览器内打开指定链接
                "url" -> {
                    data?.let { url ->
                        val trimmed = url.trim()
                        //防止 file://、intent:// 等危险 scheme
                        if (trimmed.startsWith("http://", ignoreCase = true) ||
                            trimmed.startsWith("https://", ignoreCase = true)
                        ) {
                            withContext(Dispatchers.Main) {
                                this@MainActivity.openLink(trimmed)
                            }
                        } else {
                            lWarning("Blocked unsafe URL from homepage event: $trimmed")
                        }
                    }
                }
                //检查启动器更新
                "check_update" -> checkUpdate()
                //启动当前选中的游戏版本
                "launch_game" -> {
                    val serverIp = data?.let { raw ->
                        runCatching {
                            val parms = raw.split("=", limit = 2)
                            if (parms.size == 2 && parms[0] == "server") {
                                parms[1].trim()
                            } else null
                        }.onFailure { e ->
                            lWarning("Failed to parse quick join server parameters: $raw", e)
                        }.getOrNull()
                    }
                    if (!serverIp.isNullOrEmpty()) {
                        //禁止控制字符与换行，防止注入命令行参数或配置文件
                        if (serverIp.none { it.code < 32 }) {
                            launchGameViewModel.tryPlayServer(serverIp)
                        } else {
                            lWarning("Invalid server address from homepage event: $serverIp")
                        }
                    } else {
                        launchGameViewModel.tryLaunch()
                    }
                }
                //复制指定文本
                "copy" -> {
                    data?.let { text ->
                        val trimmed = text.trim()
                        withContext(Dispatchers.Main) {
                            copyText(
                                null,
                                trimmed.take(10_000), //限制复制内容长度
                                this@MainActivity,
                                showToast = true
                            )
                        }
                    }
                }
                //刷新主页
                "refresh_page" -> homePageViewModel.reloadPage(true)
                //分享游戏日志
                "share_game_log" -> {
                    VersionsManager.currentVersion.value?.let { version ->
                        VersionsManager.getLatestLog(version).takeIf { it.exists() }
                    }?.let { logFile ->
                        withContext(Dispatchers.Main) {
                            logsUploadViewModel.check(logFile)
                            logShareViewModel.openMenu(logFile)
                        }
                    }
                }
                else -> {
                    lWarning("Unknown homepage event: key=$key, data=$data")
                }
            }
        }.onFailure { e ->
            lWarning("Failed to handle homepage event: key=$key, data=$data", e)
        }
    }

    /**
     * 是否保持屏幕不熄屏
     */
    private suspend fun keepScreen(on: Boolean) {
        withContext(Dispatchers.Main) {
            window?.apply {
                if (on) {
                    addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                } else {
                    clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                }
            }
        }
    }

    /**
     * 弹出下载插件的链接提示对话框
     */
    private suspend fun showDownloadPlugins(link: EventViewModel.Event.DownloadPlugins.Links) {
        //匹配当前系统语言可见的网盘链接
        val locale = Locale.getDefault()
        val cloudDrive = link.cloudDrives.sortedByDescending {
            it.language.contains("_")
        }.find { drive ->
            locale.compareLangTag(drive.language)
        }

        withContext(Dispatchers.Main) {
            val builder = MaterialAlertDialogBuilder(this@MainActivity)
                .setTitle(R.string.plugin_download_title)
                .setMessage(R.string.plugin_download_summary)
                .setPositiveButton("Github") { dialog, _ ->
                    openLinkInternal(link.github)
                    dialog.dismiss()
                }

            cloudDrive?.link?.let { link ->
                builder.setNegativeButton(R.string.upgrade_cloud_drive) { dialog, _ ->
                    openLinkInternal(link)
                    dialog.dismiss()
                }
            }

            builder.create().show()
        }
    }

    /**
     * 导入控制布局
     */
    private fun importControlFiles(uris: List<Uri>) {
        fun showError(
            title: String = getString(R.string.control_manage_import_failed),
            message: String
        ) {
            errorViewModel.showError(
                ErrorViewModel.ThrowableMessage(
                    title = title,
                    message = message
                )
            )
        }
        TaskSystem.submitTask(
            Task.runTask(
                dispatcher = Dispatchers.IO,
                task = {
                    var done = false
                    uris.forEach { uri ->
                        val inputStream = contentResolver.openInputStream(uri) ?: run {
                            showError(message = getString(R.string.multirt_runtime_import_failed_input_stream))
                            return@forEach
                        }
                        ControlManager.importControl(
                            inputStream = inputStream,
                            onSerializationError = {
                                showError(
                                    message = getString(R.string.control_manage_import_failed_to_parse) + "\n" +
                                            it.getMessageOrToString()
                                )
                            },
                            catchedError =  {
                                showError(message = it.getMessageOrToString())
                            },
                            onFinished = {
                                done = true
                            }
                        )
                    }
                    ControlManager.refresh()
                    if (done) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                this@MainActivity,
                                getString(R.string.generic_done),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            )
        )
    }

    /**
     * 处理外部导入
     * @return 是否有导入任务正在进行中
     */
    private fun handleImportIfNeeded(intent: Intent?): Boolean {
        if (intent == null) return false

        val type = intent.getStringExtra(EXTRA_IMPORT_TYPE) ?: return false

        val importing = when (type) {
            IMPORT_TYPE_MODPACK -> handleModpackImport(intent)
            IMPORT_TYPE_CONTROLS -> handleControlsImport(intent)
            else -> false
        }

        intent.removeExtra(EXTRA_IMPORT_TYPE)
        return importing
    }

    /**
     * @return 是否已经触发了整合包导入程序
     */
    private fun handleModpackImport(intent: Intent): Boolean {
        val uri: Uri? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(EXTRA_IMPORT_URI, Uri::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra(EXTRA_IMPORT_URI)
        }
        if (uri != null) {
            modpackImportViewModel.import(
                context = this@MainActivity,
                uri = uri,
                onStart = {
                    lifecycleScope.launch {
                        keepScreen(true)
                    }
                },
                onStop = {
                    lifecycleScope.launch {
                        keepScreen(false)
                    }
                }
            )
        }
        return uri != null
    }

    /**
     * @return 是否已经触发了控制布局导入程序
     */
    private fun handleControlsImport(intent: Intent): Boolean {
        val uri: Uri? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(EXTRA_IMPORT_URI, Uri::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra(EXTRA_IMPORT_URI)
        }
        if (uri != null) {
            importControlFiles(listOf(uri))
        }
        return uri != null
    }

    override fun onResume() {
        super.onResume()
        ControlManager.checkDefaultAndRefresh(this@MainActivity)
    }

    @SuppressLint("RestrictedApi")
    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (isCaptureKey) {
            lInfo("Capture key event: $event")
            eventViewModel.sendEvent(EventViewModel.Event.Key.OnKeyDown(event))
            return true
        }
        return super.dispatchKeyEvent(event)
    }
}