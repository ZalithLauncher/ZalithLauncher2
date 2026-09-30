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
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.SplashException
import com.movtery.zalithlauncher.components.Components
import com.movtery.zalithlauncher.components.InstallableItem
import com.movtery.zalithlauncher.components.UnpackComponentsTask
import com.movtery.zalithlauncher.components.UnpackMinecraftTask
import com.movtery.zalithlauncher.components.jre.Jre
import com.movtery.zalithlauncher.components.jre.UnpackJnaTask
import com.movtery.zalithlauncher.components.jre.UnpackJreTask
import com.movtery.zalithlauncher.setting.AllSettings
import com.umeng.message.PushAgent
import com.umeng.union.api.UMAdConfig
import com.umeng.union.api.UMUnionApi
import com.umeng.union.UMUnionSdk
import com.umeng.union.UMSplashAD
import com.movtery.zalithlauncher.ui.base.BaseAppCompatActivity
import com.movtery.zalithlauncher.ui.screens.splash.SplashScreen
import com.movtery.zalithlauncher.ui.theme.ZalithLauncherTheme
import com.movtery.zalithlauncher.ui.theme.backgroundColor
import com.movtery.zalithlauncher.ui.theme.onBackgroundColor
import com.movtery.zalithlauncher.utils.logging.Logger
import com.movtery.zalithlauncher.viewmodel.SplashBackStackViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicInteger

private const val TAG = "SplashActivity"

const val EXTRA_IMPORT_ACTION = "EXTRA_IMPORT_ACTION"
const val EXTRA_IMPORT_URI    = "EXTRA_IMPORT_URI"
const val EXTRA_IMPORT_TYPE   = "EXTRA_IMPORT_TYPE"

const val IMPORT_TYPE_MODPACK = "modpack"
const val IMPORT_TYPE_CONTROLS = "controls"
const val IMPORT_TYPE_UNKNOWN = "unknown"

/** 友盟开屏广告位 ID */
private const val SPLASH_AD_SLOT_ID = "100012689"

/** 开屏广告请求超时（毫秒） */
private const val AD_REQUEST_TIMEOUT = 5_000L

@SuppressLint("CustomSplashScreen")
@AndroidEntryPoint
class SplashActivity : BaseAppCompatActivity() {
    private val unpackItems: MutableList<InstallableItem> = ArrayList()
    private val finishedTaskCount = AtomicInteger(0)

    private val backStackViewModel: SplashBackStackViewModel by viewModels()

    /** 是否已经跳转到主界面 */
    private var hasNavigatedToMain = false

    /** canJump: 防止后台切回时误跳转。onResume 时设为 true，onPause 时设为 false。 */
    private var canJump = false

    /** 广告请求超时任务 */
    private var mReqTimeout: Runnable? = null

    /** 广告展示容器（我们将使用一个全屏父容器 + 居中子容器来承载竖屏广告） */
    private var adContainer: FrameLayout? = null

    /** 原来请求的方向，展示广告时临时切换为竖屏，展示结束后恢复 */
    private var originalRequestedOrientation: Int? = null

    private val mHandler = Handler(Looper.getMainLooper())

    // ======================== 友盟开屏广告加载回调 ========================

    private val mLoadListener = object : UMUnionApi.AdLoadListener<UMSplashAD> {
        override fun onSuccess(type: UMUnionApi.AdType, display: UMSplashAD) {
            Logger.info(TAG, "Splash ad loaded successfully")
            // 移除超时
            mReqTimeout?.let { mHandler.removeCallbacks(it) }
            mReqTimeout = null

            if (isFinishing) return

            display.setAdEventListener(object : UMUnionApi.SplashAdListener {
                override fun onDismissed() {
                    Logger.info(TAG, "Splash ad dismissed")
                    // 移除广告容器
                    (adContainer?.parent as? ViewGroup)?.removeView(adContainer)
                    adContainer = null
                    // 恢复方向
                    originalRequestedOrientation?.let {
                        requestedOrientation = it
                        originalRequestedOrientation = null
                    }
                    goToContentOrHome()
                }

                override fun onExposed() {
                    Logger.info(TAG, "Splash ad exposed")
                }

                override fun onClicked(view: android.view.View?) {
                    Logger.info(TAG, "Splash ad clicked")
                }

                override fun onError(code: Int, message: String) {
                    Logger.warning(TAG, "Splash ad display error: code=$code, msg=$message")
                    // 移除广告容器（如果已添加）
                    (adContainer?.parent as? ViewGroup)?.removeView(adContainer)
                    adContainer = null
                    // 恢复方向
                    originalRequestedOrientation?.let {
                        requestedOrientation = it
                        originalRequestedOrientation = null
                    }
                    goToContentOrHome()
                }
            })

            // 在展示广告前临时切换为竖屏，以便竖屏广告能以原始比例展示
            try {
                originalRequestedOrientation = requestedOrientation
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
            } catch (e: Exception) {
                Logger.warning(TAG, "Failed to change orientation for splash ad: ${e.message}")
            }

            // 创建全屏父容器 + 居中子容器，竖屏广告在横屏下保持原始比例不拉伸
            val full = FrameLayout(this@SplashActivity).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            }
            val inner = FrameLayout(this@SplashActivity).apply {
                layoutParams = FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                ).apply { gravity = Gravity.CENTER }
            }
            full.addView(inner)
            adContainer = full
            // 将容器添加到根视图
            (window.decorView as ViewGroup).addView(full)
            // 展示广告到居中子容器中
            display.show(inner)
        }

        override fun onFailure(type: UMUnionApi.AdType, message: String) {
            Logger.warning(TAG, "Splash ad load failed: $message")
            mReqTimeout?.let { mHandler.removeCallbacks(it) }
            mReqTimeout = null
            if (isFinishing) return
            // 若提前切换了方向（理论上不会在加载失败前切换），也尝试恢复
            originalRequestedOrientation?.let {
                requestedOrientation = it
                originalRequestedOrientation = null
            }
            goToContentOrHome()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        // 友盟推送：活跃统计（必须在同意隐私政策后调用）
        PushAgent.getInstance(this).onAppStart()

        initUnpackItems()
        checkAllTask()

        if (checkTasksToMain()) {
            return
        }

        // 根据设置开关加载友盟开屏广告
        loadSplashAd()

        setContent {
            ZalithLauncherTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = backgroundColor(),
                    contentColor = onBackgroundColor()
                ) {
                    SplashScreen(
                        startAllTask = { startAllTask() },
                        unpackItems = unpackItems,
                        screenViewModel = backStackViewModel
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        canJump = true
        // 从后台切回时，如果任务已完成且广告展示流程已完成，跳转主页
        if (areAllTasksFinished() && !hasNavigatedToMain && mReqTimeout == null) {
            goToContentOrHome()
        }
    }

    override fun onPause() {
        super.onPause()
        canJump = false
    }

    override fun onDestroy() {
        super.onDestroy()
        canJump = false
        mReqTimeout?.let { mHandler.removeCallbacks(it) }
        mReqTimeout = null
        // 恢复方向（防止意外）
        originalRequestedOrientation?.let {
            try {
                requestedOrientation = it
            } catch (_: Exception) {}
            originalRequestedOrientation = null
        }
    }

    /** 禁用返回键：开屏期间不允许退出 */
    override fun onBackPressed() {
        // 空实现，阻止返回键退出
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)

        //若依赖未完成，忽略所有外部导入
        if (!areAllTasksFinished()) {
            Logger.info(TAG, "Import intent received but dependencies are not ready, ignoring.")
            return
        }

        if (isImportIntent(intent) && !isLauncherIntent(intent)) {
            handleImportIntent(intent)
            finish()
        }
    }

    // ======================== 友盟开屏广告 ========================

    /** 根据设置开关加载友盟开屏广告 */
    private fun loadSplashAd() {
        if (!AllSettings.showSplashAd.getValue()) {
            return
        }

        try {
            val config = UMAdConfig.Builder()
                .setSlotId(SPLASH_AD_SLOT_ID)
                .build()
            UMUnionSdk.loadSplashAd(config, mLoadListener, AD_REQUEST_TIMEOUT.toInt())
            // 设置超时保护
            val timeoutRunnable = Runnable {
                mReqTimeout = null
                // 若我们临时切换了方向，尝试恢复
                originalRequestedOrientation?.let {
                    try {
                        requestedOrientation = it
                    } catch (_: Exception) {}
                    originalRequestedOrientation = null
                }
                if (!hasNavigatedToMain) {
                    goToContentOrHome()
                }
            }
            mReqTimeout = timeoutRunnable
            mHandler.postDelayed(timeoutRunnable, AD_REQUEST_TIMEOUT)
        } catch (e: Exception) {
            Logger.warning(TAG, "Failed to load splash ad: ${e.message}")
            // 加载失败，直接走正常流程
            goToContentOrHome()
        }
    }

    /** 统一的跳转入口：canJump 防误跳 + 任务完成检查 */
    private fun goToContentOrHome() {
        if (!canJump || hasNavigatedToMain) return
        if (!areAllTasksFinished()) return

        navigateToMain()
    }

    // ======================== 原有逻辑 ========================

    private fun initUnpackItems() {
        Components.entries.forEach { component ->
            val task = UnpackComponentsTask(this@SplashActivity, component)
            if (!task.isCheckFailed()) {
                unpackItems.add(
                    InstallableItem(
                        component.displayName,
                        getString(component.summary),
                        task
                    )
                )
            }
        }
        Jre.entries.forEach { jre ->
            val task = UnpackJreTask(this@SplashActivity, jre)
            if (!task.isCheckFailed()) {
                unpackItems.add(
                    InstallableItem(
                        jre.jreName,
                        getString(jre.summary),
                        task
                    )
                )
            }
        }
        val jnaTask = UnpackJnaTask(this@SplashActivity)
        if (!jnaTask.isCheckFailed()) {
            unpackItems.add(
                InstallableItem(
                    "JNA",
                    getString(R.string.unpack_screen_jna),
                    jnaTask
                )
            )
        }
        val minecraftTask = UnpackMinecraftTask(this@SplashActivity)
        if (!minecraftTask.isCheckFailed()) {
            unpackItems.add(
                InstallableItem(
                    ".minecraft",
                    getString(R.string.unpack_screen_minecraft),
                    minecraftTask
                )
            )
        }
        unpackItems.sort()
    }

    private fun checkAllTask() {
        //检查应用 assets 目录
        listAssetsPath("runtimes").forEach { filePath ->
            Logger.info(TAG, "The launcher contains the runtime environment: $filePath")
        }

        unpackItems.forEach { item ->
            val state = item.task.checkState()
            item.updateState(state)
            if (state == InstallableItem.State.FINISHED) {
                finishedTaskCount.incrementAndGet()
            }
        }
    }

    private fun listAssetsPath(root: String): List<String> {
        return buildList {
            val rootFiles = runCatching {
                assets.list(root)?.takeIf { it.isNotEmpty() }
            }.getOrNull()
            if (rootFiles != null) {
                rootFiles.forEach { child ->
                    val childPath = "$root/$child"
                    val childFiles = runCatching {
                        assets.list(childPath)?.takeIf { it.isNotEmpty() }
                    }.getOrNull()

                    if (childFiles != null) {
                        addAll(listAssetsPath(childPath))
                    } else {
                        add(childPath)
                    }
                }
            } else {
                add(root)
            }
        }
    }

    private fun startAllTask() {
        lifecycleScope.launch {
            val jobs = unpackItems
                .filter {
                    val state = it.state.value
                    state == InstallableItem.State.NOT_STARTED ||
                    state == InstallableItem.State.PENDING
                }
                .map { item ->
                    launch(Dispatchers.IO) {
                        item.updateState(InstallableItem.State.RUNNING)
                        runCatching {
                            item.task.run()
                        }.onFailure {
                            throw SplashException(it)
                        }
                        finishedTaskCount.incrementAndGet()
                        item.updateState(InstallableItem.State.FINISHED)
                    }
                }
            jobs.joinAll()
        }.invokeOnCompletion {
            AllSettings.javaRuntime.apply {
                //检查并设置默认的Java环境
                if (getValue().isEmpty()) save(Jre.JRE_8.jreName)
            }
            // 解压任务全部完成后加载开屏广告，再跳转主页
            if (!hasNavigatedToMain && areAllTasksFinished()) {
                showSplashAdIfAvailable()
            }
        }
    }

    /** 解压任务完成后加载并展示开屏广告 */
    private fun showSplashAdIfAvailable() {
        if (!canJump || hasNavigatedToMain) return
        if (!AllSettings.showSplashAd.getValue()) {
            goToContentOrHome()
            return
        }
        // 广告已在 onCreate 中发起加载，如果尚未完成会通过超时或回调触发跳转
        // 但如果广告加载已经完成且展示完毕（mReqTimeout == null），直接跳转
        if (mReqTimeout == null) {
            goToContentOrHome()
        }
        // 否则等待广告回调或超时处理
    }

    private fun checkTasksToMain(): Boolean {
        if (!areAllTasksFinished()) return false

        Logger.info(TAG, "All content that needs to be extracted is already the latest version!")

        if (isImportIntent(intent) && !isLauncherIntent(intent)) {
            val success = handleImportIntent(intent)
            if (success) {
                finish()
                return true
            }
        }

        // 所有任务已完成 → 加载开屏广告后再跳转
        canJump = true
        loadSplashAd()
        return true
    }

    private fun swapToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        hasNavigatedToMain = true
        finish()
    }

    /** 统一的主界面跳转入口 */
    private fun navigateToMain() {
        if (hasNavigatedToMain) return
        swapToMain()
    }

    private fun handleImportIntent(source: Intent): Boolean {
        if (!isImportIntent(source)) return false

        val uri: Uri? = when (source.action) {
            Intent.ACTION_SEND -> {
                source.clipData?.getItemAt(0)?.uri
                    ?: source.getParcelableExtra(Intent.EXTRA_STREAM)
            }
            Intent.ACTION_VIEW -> source.data
            else -> null
        }

        if (uri == null) {
            Logger.warning(TAG, "No valid import Uri found")
            return false
        } else {
            try {
                //可持久化访问授权
                contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            } catch (_: Exception) {
                Logger.warning(TAG, "No persistable permission granted for $uri")
            }
        }

        val forward = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP

            putExtra(EXTRA_IMPORT_ACTION, source.action)
            putExtra(EXTRA_IMPORT_URI, uri)
            putExtra(EXTRA_IMPORT_TYPE, resolveImportType(source))
        }

        startActivity(forward)
        return true
    }

    /**
     * 根据 AndroidManifest 内为 activity-alias 配置的 meta-data 来判断导入类型
     */
    private fun resolveImportType(intent: Intent): String {
        val comp = intent.component ?: return IMPORT_TYPE_UNKNOWN
        val info = packageManager.getActivityInfo(comp, PackageManager.GET_META_DATA)
        return info.metaData?.getString("import_type") ?: IMPORT_TYPE_UNKNOWN
    }

    private fun isLauncherIntent(intent: Intent?): Boolean {
        if (intent == null) return false
        return intent.action == Intent.ACTION_MAIN &&
                intent.categories?.contains(Intent.CATEGORY_LAUNCHER) == true
    }

    private fun isImportIntent(intent: Intent?): Boolean {
        val comp = intent?.component ?: return false
        val info = packageManager.getActivityInfo(comp, PackageManager.GET_META_DATA)
        return info.metaData?.getString("import_type") != null
    }

    private fun areAllTasksFinished(): Boolean {
        return finishedTaskCount.get() >= unpackItems.size
    }
}
