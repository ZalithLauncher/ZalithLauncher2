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

package com.movtery.zalithlauncher.utils.logging

import android.content.Context
import android.util.Log
import com.movtery.zalithlauncher.BuildKeys
import com.movtery.zalithlauncher.path.PathManager
import com.movtery.zalithlauncher.setting.AllSettings
import com.movtery.zalithlauncher.utils.file.zipDirectory
import com.movtery.zalithlauncher.utils.printLauncherInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.apache.commons.io.FileUtils
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.io.PrintWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.CoroutineContext

/**
 * [Modified from HMCL](https://github.com/HMCL-dev/HMCL/blob/57018bef47417108b75e2298ab61f89a7586b1b9/HMCLCore/src/main/java/org/jackhuang/hmcl/util/logging/Logger.java)
 */
object Logger : CoroutineScope {
    override val coroutineContext: CoroutineContext = Dispatchers.Default + Job()

    private lateinit var PACKAGE_PREFIX: String

    /**
     * 当前进程的标识
     */
    private lateinit var PROCESS_TAG: String
    private val isInitialized = AtomicBoolean(false)
    private val channel = Channel<LogMessage>(Channel.UNLIMITED)

    private val logRetentionDays: Int
        get() = AllSettings.launcherLogRetentionDays.getValue()

    private var currentLogFile: File? = null
    private var logWriter: PrintWriter? = null
    private var inMemoryLogs: ByteArrayOutputStream? = null

    /**
     * 初始化日志
     */
    fun initialize(context: Context) {
        PACKAGE_PREFIX = "${context.packageName}."
        PROCESS_TAG = getProcessTag(context)

        if (!isInitialized.compareAndSet(false, true)) return

        launch(Dispatchers.IO) {
            setupLogWriter()

            //由于安卓不存在“退出”这种设置
            //所以清理旧的日志的工作需要放到初始化阶段
            deleteOldLogs()

            printLauncherInfo()

            processEvents()
        }
    }

    /**
     * 获取当前进程标识
     */
    private fun getProcessTag(context: Context): String {
        val processName = context.applicationInfo.processName
        val separatorIndex = processName.lastIndexOf(':')
        return if (separatorIndex < 0) "main" else processName.substring(separatorIndex + 1)
    }

    private suspend fun printLauncherInfo() {
        logWriter?.apply {
            withContext(Dispatchers.IO) {
                println("================ ${BuildKeys.LAUNCHER_IDENTIFIER} Log ================")
                printLauncherInfo { println(it) }
                println("====================================================")
                flush()
            }
        }
    }

    private suspend fun setupLogWriter() = withContext(Dispatchers.IO) {
        try {
            currentLogFile = createLogFile()
            logWriter = PrintWriter(currentLogFile!!.writer())
        } catch (e: IOException) {
            val logMessage = LogMessage(
                System.currentTimeMillis(),
                "Logger.setupLogWriter",
                Level.WARNING,
                "Failed to create log file", e
            )
            channel.send(logMessage)
            inMemoryLogs = ByteArrayOutputStream(1024 * 1024) // 1MB buffer
            logWriter = PrintWriter(inMemoryLogs!!)
        }
    }

    private fun createLogFile(): File {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH-mm-ss", Locale.US)
        var file: File
        var counter = 0

        do {
            val suffix = if (counter == 0) "" else ".$counter"
            file = File(PathManager.DIR_LAUNCHER_LOGS, "log_${dateFormat.format(Date())}_$PROCESS_TAG$suffix.log")
            counter++
        } while (!file.createNewFile())

        return file
    }

    private suspend fun processEvents() = withContext(Dispatchers.IO) {
        for (message in channel) {
            handleLogMessage(message)
        }
    }

    private fun handleLogMessage(message: LogMessage) {
        val formatted = formatMessage(message)

        //输出到 Logcat
        printToLogcat(message.level, formatted)

        logWriter?.apply {
            println(formatted)
            message.throwable?.also { th ->
                th.printStackTrace(this@apply)
                printToLogcat(message.level, th)
            }
            flush()
        }
    }

    private fun printToLogcat(level: Level, message: String) {
        when (level) {
            Level.ERROR -> Log.e("AppLog", message)
            Level.WARNING -> Log.w("AppLog", message)
            Level.INFO -> Log.i("AppLog", message)
            Level.DEBUG -> Log.d("AppLog", message)
            Level.TRACE -> Log.v("AppLog", message)
        }
    }

    private fun printToLogcat(level: Level, throwable: Throwable) {
        val thMessage = Log.getStackTraceString(throwable)

        when (level) {
            Level.ERROR -> Log.e("AppLog", thMessage)
            Level.WARNING -> Log.w("AppLog", thMessage)
            Level.INFO -> Log.i("AppLog", thMessage)
            Level.DEBUG -> Log.d("AppLog", thMessage)
            Level.TRACE -> Log.v("AppLog", thMessage)
        }
    }

    private fun formatMessage(message: LogMessage): String {
        val time = SimpleDateFormat("HH:mm:ss.SSS", Locale.US).format(message.time)
        return buildString {
            append("[$time] [")
            append(message.tag)
            append("/")
            append(message.level.name)
            append("] ")
            append(message.message)
        }
    }

    private suspend fun deleteOldLogs() = withContext(Dispatchers.IO) {
        PathManager.DIR_LAUNCHER_LOGS.listFiles()?.let { files ->
            val cutoff = System.currentTimeMillis() - logRetentionDays * 86400000L
            //清理遗留的 0kb 空日志
            val emptyLogCutoff = System.currentTimeMillis() - 60 * 1000L
            files.filter {
                it.lastModified() < cutoff ||
                    (it != currentLogFile && it.length() == 0L && it.lastModified() < emptyLogCutoff)
            }.forEach {
                FileUtils.deleteQuietly(it)
            }
        }
    }

    fun error(tag: String, msg: String, t: Throwable? = null) =
        log(Level.ERROR, tag, msg, t)

    fun warning(tag: String, msg: String, t: Throwable? = null) =
        log(Level.WARNING, tag, msg, t)

    fun info(tag: String, msg: String, t: Throwable? = null) =
        log(Level.INFO, tag, msg, t)

    fun debug(tag: String, msg: String, t: Throwable? = null) =
        log(Level.DEBUG, tag, msg, t)

    /**
     * 输出日志
     */
    fun log(level: Level, tag: String, message: String, throwable: Throwable? = null) {
        if (!isInitialized.get()) return

        val logMessage = LogMessage(
            time = System.currentTimeMillis(),
            tag = tag,
            level = level,
            message = message,
            throwable = throwable
        )

        launch {
            channel.send(logMessage)
        }
    }

    /**
     * 打包所有日志文件
     */
    suspend fun pack(target: File) {
        withContext(Dispatchers.IO) {
            zipDirectory(
                sourceDir = PathManager.DIR_LAUNCHER_LOGS,
                outputZipFile = target
            )
        }
    }
}