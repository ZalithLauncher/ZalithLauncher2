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

package com.movtery.zalithlauncher.game.download.jvm_server

import com.movtery.zalithlauncher.coroutine.TaskLogOutput
import com.movtery.zalithlauncher.utils.logging.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File
import java.io.RandomAccessFile
import java.nio.charset.StandardCharsets
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

private const val TAG = "ProcessLogTailer"

/**
 * 增量轮询读取安装 JVM 进程的运行日志文件，将新增的完整日志行推送到 [TaskLogOutput]
 */
class ProcessLogTailer(
    private val logFile: File,
    private val output: TaskLogOutput,
    private val pollInterval: Duration = DEFAULT_POLL_INTERVAL
) {
    /**
     * 在给定作用域内启动轮询，返回的 Job 由调用方负责取消
     */
    fun launch(scope: CoroutineScope): Job = scope.launch(Dispatchers.IO) {
        //从文件当前末尾开始增量读取，避免把上一次 JVM 运行残留的旧日志读入本次会话
        var offset = logFile.takeIf { it.exists() }?.length() ?: 0L
        var remainder = ByteArray(0)

        while (isActive) {
            delay(pollInterval)
            try {
                if (!logFile.exists()) continue
                val length = logFile.length()
                //文件被截断（日志文件在 JRE 重试时会被重新创建覆盖），从头重读
                if (length < offset) {
                    offset = 0
                    remainder = ByteArray(0)
                }
                if (length <= offset) continue

                val bytes = RandomAccessFile(logFile, "r").use { raf ->
                    raf.seek(offset)
                    val buffer = ByteArray((length - offset).toInt())
                    raf.readFully(buffer)
                    buffer
                }
                offset = length

                val (completeLines, newRemainder) = splitCompleteLines(remainder + bytes)
                remainder = newRemainder
                if (remainder.size > MAX_PENDING_BYTES) remainder = ByteArray(0)
                if (completeLines.isNotEmpty()) output.appendLines(completeLines)
            } catch (e: Exception) {
                Logger.warning(TAG, "Failed to tail the installer process log", e)
            }
        }
    }

    /**
     * 按字节层面最后一个换行符切分出完整日志行，不足一行的尾部字节留待下一轮拼接，
     * 避免多字节 UTF-8 字符被截断产生乱码
     */
    private fun splitCompleteLines(bytes: ByteArray): Pair<List<String>, ByteArray> {
        val lastNewline = bytes.lastIndexOf('\n'.code.toByte())
        if (lastNewline < 0) return emptyList<String>() to bytes

        val completeText = String(bytes, 0, lastNewline + 1, StandardCharsets.UTF_8)
        val remainderBytes = bytes.copyOfRange(lastNewline + 1, bytes.size)

        val lines = completeText.split('\n')
            .map { it.removeSuffix("\r") }
            .filter { it.isNotEmpty() }
        return lines to remainderBytes
    }

    companion object {
        /** 默认轮询间隔 */
        val DEFAULT_POLL_INTERVAL = 250.milliseconds
        /** 未遇到换行符时允许缓存的字节数上限，超过后丢弃，防止无限增长 */
        private const val MAX_PENDING_BYTES = 1024 * 1024
    }
}
