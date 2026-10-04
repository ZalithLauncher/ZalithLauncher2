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

package com.movtery.zalithlauncher.filemanager.ui

import android.app.ActivityManager
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.DEFAULT_ARGS_KEY
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.MutableCreationExtras
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.filemanager.FileManagerLauncher
import com.movtery.zalithlauncher.filemanager.os.FmLog
import com.movtery.zalithlauncher.filemanager.viewmodel.FileManagerViewModel
import com.movtery.zalithlauncher.setting.loadAllSettings
import com.movtery.zalithlauncher.ui.theme.ZalithLauncherTheme
import dagger.hilt.android.AndroidEntryPoint
import java.nio.file.Paths

/**
 * 文件管理器初始化结果
 */
sealed interface FileManagerInitResult {
    data object Pending : FileManagerInitResult
    /** Intent 参数解析成功 */
    data object Ok : FileManagerInitResult
    data class Failed(val message: String) : FileManagerInitResult
}

/**
 * 文件管理器 Activity
 */
@AndroidEntryPoint
class FileManagerActivity : ComponentActivity() {
    private var initResult by mutableStateOf<FileManagerInitResult>(FileManagerInitResult.Pending)

    /** Intent 中的可访问范围目录（仅字符串，作用域校验交给 ViewModel 初始化） */
    private var rootPathStr: String? = null
    /** Intent 中的可选初始当前目录（仅字符串） */
    private var currentPathStr: String? = null

    // Compose 可观察，重唤起重建 VM 后自动重组
    private var _vm by mutableStateOf<FileManagerViewModel?>(null)

    override val defaultViewModelCreationExtras: CreationExtras
        get() = MutableCreationExtras(super.defaultViewModelCreationExtras).apply {
            if (initResult !is FileManagerInitResult.Ok) return@apply
            val bundle = Bundle().apply {
                putString(FileManagerViewModel.KEY_ROOT_PATH, rootPathStr)
                currentPathStr?.let { putString(FileManagerViewModel.KEY_CURRENT_PATH, it) }
            }
            set(DEFAULT_ARGS_KEY, bundle)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        loadAllSettings(this, true)
        initializeFromIntent()

        setContent {
            ZalithLauncherTheme {
                FileManagerRootScreen(
                    initResult = initResult,
                    vm = _vm,
                    onExit = { finish() },
                    onToggleOrientation = { toggleOrientation() }
                )
            }
        }
    }

    /**
     * 重唤起（任务已存在，经 SINGLE_TOP 送达）：重新解析可访问范围与当前路径，
     * 重建 ViewModel 并刷新。文件管理器未 finish 时，此方法替代 onCreate 生效。
     */
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        loadAllSettings(this, true)
        initializeFromIntent()
    }

    /** 从当前 Intent 解析参数并重建 ViewModel */
    private fun initializeFromIntent() {
        // 日志初始化
        val logsDir = intent.getStringExtra(FileManagerLauncher.EXTRA_LOGS_DIR)?.let { Paths.get(it) }
        FmLog.init(lifecycleScope, logsDir)

        initResult = parseIntent()

        // 重建 ViewModel
        viewModelStore.clear()
        _vm = if (initResult is FileManagerInitResult.Ok) {
            ViewModelProvider(this)[FileManagerViewModel::class.java]
        } else {
            null
        }
    }

    /** 解析 Intent 参数 */
    private fun parseIntent(): FileManagerInitResult {
        val root = intent.getStringExtra(FileManagerLauncher.EXTRA_ROOT_PATH)
        if (root.isNullOrBlank()) {
            return FileManagerInitResult.Failed("Missing required extra: ROOT_PATH")
        }
        rootPathStr = root
        currentPathStr = intent.getStringExtra(FileManagerLauncher.EXTRA_CURRENT_PATH)
        return FileManagerInitResult.Ok
    }

    override fun onResume() {
        super.onResume()
        loadAllSettings(this, true)
        applySystemUiByOrientation()
        @Suppress("DEPRECATION")
        setTaskDescription(
            ActivityManager.TaskDescription(getString(R.string.fm_activity_name))
        )
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        applySystemUiByOrientation()
    }

    override fun onDestroy() {
        FmLog.close()
        super.onDestroy()
    }

    private fun applySystemUiByOrientation() {
        val isLandscape = resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        if (isLandscape) {
            window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            controller.hide(WindowInsetsCompat.Type.systemBars())
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
            controller.show(WindowInsetsCompat.Type.systemBars())
        }
    }

    private fun toggleOrientation() {
        val current = resources.configuration.orientation
        requestedOrientation = if (current == Configuration.ORIENTATION_LANDSCAPE) {
            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        } else {
            ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        }
    }

    companion object {
        private const val TAG = "FileManagerActivity"
    }
}