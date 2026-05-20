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
import android.os.Parcelable
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import com.jakewharton.processphoenix.ProcessPhoenix
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.context.COPY_LABEL_LINK
import com.movtery.zalithlauncher.path.PathManager
import com.movtery.zalithlauncher.ui.base.BaseAppCompatActivity
import com.movtery.zalithlauncher.ui.screens.main.ErrorScreen
import com.movtery.zalithlauncher.ui.screens.main.crashlogs.ShareLinkOperation
import com.movtery.zalithlauncher.ui.theme.ZalithLauncherTheme
import com.movtery.zalithlauncher.ui.theme.backgroundColor
import com.movtery.zalithlauncher.ui.theme.onBackgroundColor
import com.movtery.zalithlauncher.utils.copyText
import com.movtery.zalithlauncher.utils.file.shareFile
import com.movtery.zalithlauncher.utils.getParcelableSafely
import com.movtery.zalithlauncher.utils.getSerializableSafely
import com.movtery.zalithlauncher.utils.network.openLink
import com.movtery.zalithlauncher.utils.string.throwableToString
import com.movtery.zalithlauncher.viewmodel.LogsUploadViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.parcelize.Parcelize
import java.io.File

private const val BUNDLE_EXIT_TYPE = "BUNDLE_EXIT_TYPE"
private const val BUNDLE_THROWABLE = "BUNDLE_THROWABLE"
private const val BUNDLE_JVM_CRASH = "BUNDLE_JVM_CRASH"
private const val BUNDLE_CAN_RESTART = "BUNDLE_CAN_RESTART"
private const val EXIT_JVM = "EXIT_JVM"
private const val EXIT_LAUNCHER = "EXIT_LAUNCHER"

fun showExitMessage(
    context: Context,
    code: Int,
    isSignal: Boolean,
    logPath: String
) {
    val intent = Intent(context, ErrorActivity::class.java).apply {
        addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        putExtra(BUNDLE_EXIT_TYPE, EXIT_JVM)
        putExtra(BUNDLE_JVM_CRASH, JvmCrash(code, isSignal, logPath))
    }
    context.startActivity(intent)
}

@Parcelize
private data class JvmCrash(
    val code: Int,
    val isSignal: Boolean,
    val logPath: String
): Parcelable

@AndroidEntryPoint
class ErrorActivity : BaseAppCompatActivity(refreshData = false) {

    /**
     * 游戏崩溃日志上传逻辑管理 ViewModel
     */
    private val viewModel: LogsUploadViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val extras = intent.extras ?: return runFinish()

        val exitType = extras.getString(BUNDLE_EXIT_TYPE, EXIT_LAUNCHER)

        val errorMessage = when (exitType) {
            EXIT_JVM -> {
                val jvmCrash = extras.getParcelableSafely(BUNDLE_JVM_CRASH, JvmCrash::class.java) ?: return runFinish()
                val messageResId = if (jvmCrash.isSignal) R.string.crash_singnal_message else R.string.crash_exit_message
                val message = getString(messageResId, jvmCrash.code)
                val messageBody = getString(R.string.crash_exit_note)
                ErrorMessage(
                    message = message,
                    messageBody = messageBody,
                    crashType = CrashType.GAME_CRASH,
                    logFile = File(jvmCrash.logPath).also { file ->
                        //检查日志文件是否适合上传
                        viewModel.check(file)
                    }
                )
            }
            else -> {
                val throwable = extras.getSerializableSafely(BUNDLE_THROWABLE, Throwable::class.java) ?: return runFinish()
                val message = getString(R.string.crash_launcher_message)
                val messageBody = throwableToString(throwable)
                ErrorMessage(
                    message = message,
                    messageBody = messageBody,
                    crashType = CrashType.LAUNCHER_CRASH,
                    logFile = PathManager.FILE_CRASH_REPORT
                )
            }
        }

        val logFile = errorMessage.logFile
        val canRestart: Boolean = extras.getBoolean(BUNDLE_CAN_RESTART, true)

        setContent {
            ZalithLauncherTheme {

                ShareLinkOperation(
                    operation = viewModel.operation,
                    onChange = { viewModel.operation = it },
                    onUploadChancel = { viewModel.cancel() },
                    onUpload = {
                        viewModel.upload(logFile) { link ->
                            openLink(link)
                            copyText(COPY_LABEL_LINK, link, this@ErrorActivity)
                        }
                    }
                )

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = backgroundColor(),
                    contentColor = onBackgroundColor()
                ) {
                    ErrorScreen(
                        crashType = errorMessage.crashType,
                        shareLogs = logFile.exists() && logFile.isFile,
                        canUpload = viewModel.canUpload,
                        canRestart = canRestart,
                        onShareLogsClick = {
                            if (logFile.exists() && logFile.isFile) {
                                shareFile(this@ErrorActivity, logFile)
                            }
                        },
                        onUploadClick = {
                            viewModel.operation = ShareLinkOperation.Tip
                        },
                        onRestartClick = {
                            ProcessPhoenix.triggerRebirth(this@ErrorActivity)
                        },
                        onExitClick = { finish() }
                    ) {
                        Text(
                            text = errorMessage.message,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = errorMessage.messageBody,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }

    private data class ErrorMessage(
        val message: String,
        val messageBody: String,
        val crashType: CrashType,
        val logFile: File
    )
}

/**
 * 崩溃类型
 */
enum class CrashType(val textRes: Int) {
    /**
     * 启动器崩溃
     */
    LAUNCHER_CRASH(R.string.crash_type_launcher),

    /**
     * 游戏运行崩溃
     */
    GAME_CRASH(R.string.crash_type_game)
}

/**
 * 启动软件崩溃信息页面
 */
fun showLauncherCrash(context: Context, throwable: Throwable, canRestart: Boolean = true) {
    val intent = Intent(context, ErrorActivity::class.java).apply {
        addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        putExtra(BUNDLE_EXIT_TYPE, EXIT_LAUNCHER)
        putExtra(BUNDLE_THROWABLE, throwable)
        putExtra(BUNDLE_CAN_RESTART, canRestart)
    }
    context.startActivity(intent)
}