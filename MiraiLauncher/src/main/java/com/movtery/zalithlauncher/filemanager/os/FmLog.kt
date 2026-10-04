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

package com.movtery.zalithlauncher.filemanager.os

import android.util.Log
import com.movtery.zalithlauncher.filemanager.os.FmLog.init
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import java.io.File
import java.io.PrintWriter
import java.nio.file.Files
import java.nio.file.Path
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.atomic.AtomicBoolean

/**
 * 文件管理器日志
 */
object FmLog {
    private const val SUB_DIR = "file_manager"

    /** 默认保留的日志文件数量 */
    const val DEFAULT_MAX_FILES = 5

    private val isInitialized = AtomicBoolean(false)
    private val channel = Channel<FmLogMessage>(Channel.UNLIMITED)

    private var consumerJob: Job? = null

    @Volatile
    private var logWriter: PrintWriter? = null

    /**
     * 初始化日志
     * @param scope 日志消费协程的作用域
     * @param logsDir 日志目录，写入 `<logsDir>/file_manager/`；为 null 时仅输出 logcat
     * @param maxFiles 保留的日志文件数量，超出删除最旧
     */
    fun init(
        scope: CoroutineScope,
        logsDir: Path?,
        maxFiles: Int = DEFAULT_MAX_FILES
    ) {
        val keepFiles = maxFiles.coerceAtLeast(1)
        synchronized(this) {
            // 重新初始化
            consumerJob?.cancel()
            consumerJob = null
            logWriter?.close()
            logWriter = null

            consumerJob = scope.launch(Dispatchers.IO) {
                // 文件创建等磁盘操作放在消费协程内，避免阻塞调用线程
                if (logsDir != null) {
                    try {
                        val dir = logsDir.resolve(SUB_DIR).toFile()
                        Files.createDirectories(dir.toPath())
                        val writer = PrintWriter(createLogFile(dir).writer())
                        // 先建新文件再清理，保证清理时总数准确
                        cleanupOldLogs(dir, keepFiles)
                        writer.println("================ File Manager Log ================")
                        writer.flush()
                        logWriter = writer
                    } catch (e: Exception) {
                        Log.w("FmLog", "Failed to log the line.", e)
                        // 文件日志失败不影响 logcat 输出
                        logWriter = null
                    }
                }
                for (message in channel) {
                    handleLogMessage(message)
                }
            }
            isInitialized.set(true)
        }
    }

    /** 停止消费并关闭日志文件，可再次 [init] */
    fun close() {
        synchronized(this) {
            consumerJob?.cancel()
            consumerJob = null
            logWriter?.close()
            logWriter = null
            isInitialized.set(false)
        }
    }

    fun info(tag: String, msg: String, t: Throwable? = null) = log(FmLogLevel.INFO, tag, msg, t)
    fun warn(tag: String, msg: String, t: Throwable? = null) = log(FmLogLevel.WARNING, tag, msg, t)
    fun error(tag: String, msg: String, t: Throwable? = null) = log(FmLogLevel.ERROR, tag, msg, t)
    fun debug(tag: String, msg: String, t: Throwable? = null) = log(FmLogLevel.DEBUG, tag, msg, t)

    private fun log(level: FmLogLevel, tag: String, msg: String, t: Throwable?) {
        if (!isInitialized.get()) return
        channel.trySend(FmLogMessage(System.currentTimeMillis(), tag, level, msg, t))
    }

    private fun handleLogMessage(message: FmLogMessage) {
        val formatted = formatMessage(message)
        printToLogcat(message)

        runCatching {
            logWriter?.apply {
                println(formatted)
                message.throwable?.also { th -> th.printStackTrace(this) }
                flush()
            }
        }
    }

    private fun formatMessage(message: FmLogMessage): String {
        val time = SimpleDateFormat("HH:mm:ss.SSS", Locale.US).format(Date(message.time))
        return buildString {
            append("[$time] [")
            append(message.tag)
            append("/")
            append(message.level.name)
            append("] ")
            append(message.message)
        }
    }

    private fun printToLogcat(message: FmLogMessage) {
        runCatching {
            val throwable = message.throwable
            when (message.level) {
                FmLogLevel.ERROR -> {
                    if (throwable != null) {
                        Log.e(message.tag, message.message, throwable)
                    } else {
                        Log.e(message.tag, message.message)
                    }
                }
                FmLogLevel.WARNING -> {
                    if (throwable != null) {
                        Log.w(message.tag, message.message, throwable)
                    } else {
                        Log.w(message.tag, message.message)
                    }
                }
                FmLogLevel.INFO -> {
                    if (throwable != null) {
                        Log.i(message.tag, message.message, throwable)
                    } else {
                        Log.i(message.tag, message.message)
                    }
                }
                FmLogLevel.DEBUG -> {
                    if (throwable != null) {
                        Log.d(message.tag, message.message, throwable)
                    } else {
                        Log.d(message.tag, message.message)
                    }
                }
            }
        }
    }

    // 日志文件名：fm_<yyyy-MM-dd'T'HH-mm-ss>[.<counter>].log
    private fun createLogFile(dir: File): File {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH-mm-ss", Locale.US)
        var counter = 0
        var file: File
        do {
            val suffix = if (counter == 0) "" else ".$counter"
            file = File(dir, "fm_${dateFormat.format(Date())}$suffix.log")
            counter++
        } while (file.exists())
        file.createNewFile()
        return file
    }

    private fun cleanupOldLogs(dir: File, maxFiles: Int) {
        val files = dir.listFiles { f ->
            f.isFile && f.name.startsWith("fm_") && f.name.endsWith(".log")
        }?.sortedByDescending {
            it.lastModified()
        } ?: return

        if (files.size <= maxFiles) return
        files.drop(maxFiles).forEach { file ->
            runCatching {
                Files.deleteIfExists(file.toPath())
            }
        }
    }
}

/** 文件管理器日志级别 */
enum class FmLogLevel {
    ERROR,
    WARNING,
    INFO,
    DEBUG
}

/** 文件管理器日志消息 */
data class FmLogMessage(
    val time: Long,
    val tag: String,
    val level: FmLogLevel,
    val message: String,
    val throwable: Throwable?
)
