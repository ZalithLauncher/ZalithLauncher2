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

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.BoxWithConstraints

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.movtery.layer_controller.layout.ControlLayout
import com.movtery.layer_controller.layout.loadLayoutFromFile
import com.movtery.zalithlauncher.setting.AllSettings
import com.movtery.zalithlauncher.ui.base.BaseAppCompatActivity
import com.movtery.zalithlauncher.ui.screens.content.elements.Background
import com.movtery.zalithlauncher.ui.screens.main.control_editor.ControlEditor
import com.movtery.zalithlauncher.ui.theme.ZalithLauncherTheme
import com.movtery.zalithlauncher.ui.theme.backgroundColor
import com.movtery.zalithlauncher.ui.theme.onBackgroundColor
import com.movtery.zalithlauncher.viewmodel.BackgroundViewModel
import com.movtery.zalithlauncher.viewmodel.EditorViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.io.File

private const val BUNDLE_CONTROL = "BUNDLE_CONTROL"

@AndroidEntryPoint
class ControlEditorActivity : BaseAppCompatActivity() {
    override fun isIgnoreNotch(): Boolean = AllSettings.gameFullScreen.getValue()

    /** 编辑器 */
    private val editorViewModel: EditorViewModel by viewModels()

    /**
     * 启动器背景内容管理 ViewModel
     */
    private val backgroundViewModel: BackgroundViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        /** 控制布局绝对路径 */
        val controlPath: String = intent.extras?.getString(BUNDLE_CONTROL) ?: return runFinish()
        /** 控制布局文件 */
        val controlFile: File = File(controlPath).takeIf { it.isFile && it.exists() } ?: return runFinish()
        /** 控制布局 */
        val layout: ControlLayout = runCatching {
            loadLayoutFromFile(controlFile)
        }.getOrNull() ?: return runFinish()

        //初始化控制布局
        editorViewModel.initLayout(layout)

        //绑定返回键按下事件，防止直接退出导致控制布局丢失所有变更
        //提醒用户保存并退出
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                editorViewModel.onBackPressed(context = this@ControlEditorActivity) {
                    this@ControlEditorActivity.finish()
                }
            }
        })

        setContent {
            ZalithLauncherTheme(
                backgroundViewModel = backgroundViewModel
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = backgroundColor(),
                    contentColor = onBackgroundColor()
                ) {
                    BoxWithConstraints(
                        modifier = Modifier
                            .fillMaxSize()
                    ) {
                        Background(
                            modifier = Modifier.fillMaxSize(),
                            viewModel = backgroundViewModel,
                            allowVideo = false
                        )

                        ControlEditor(
                            viewModel = editorViewModel,
                            targetFile = controlFile,
                            exit = {
                                //已保存控制布局后进行的退出
                                finish()
                            },
                            menuExit = {
                                //菜单要求的直接退出，使用对话框让用户确认
                                editorViewModel.showExitEditorDialog(
                                    context = this@ControlEditorActivity,
                                    onExit = {
                                        this@ControlEditorActivity.finish()
                                    }
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}

/**
 * 开启控制布局编辑器
 */
fun startEditorActivity(context: Context, file: File) {
    val intent = Intent(context, ControlEditorActivity::class.java).apply {
        putExtra(BUNDLE_CONTROL, file.absolutePath)
    }
    context.startActivity(intent)
}