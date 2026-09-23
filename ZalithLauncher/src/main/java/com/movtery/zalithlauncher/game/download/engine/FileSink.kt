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

package com.movtery.zalithlauncher.game.download.engine

import java.io.Closeable
import java.io.File
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.channels.FileChannel
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.nio.file.StandardOpenOption
import java.security.MessageDigest

/**
 * [Modified from HMCL FileDownloadTask](https://github.com/HMCL-dev/HMCL/blob/59bcc7fe/HMCLCore/src/main/java/org/jackhuang/hmcl/task/FileDownloadTask.java)
 *
 * 下载落盘上下文：先写入目标同目录下的临时文件，成功收尾时校验 SHA-1
 * 并原子替换目标文件；失败丢弃时删除临时文件。
 * 写盘失败由 [broken] 标记，调用方据此在重试时放弃续传、从头开始。
 */
class FileSink(
    targetFile: File,
    private val expectedChecksum: String?
) : Closeable {
    private val target: Path = targetFile.toPath()
    private val temp: Path
    private val channel: FileChannel
    private val digest: MessageDigest? = expectedChecksum?.let { MessageDigest.getInstance("SHA-1") }

    /** 最近一次写盘是否失败；为真时既有内容不可信，重试不得续传 */
    var broken = false
        private set

    init {
        val parent = target.toAbsolutePath().parent
        Files.createDirectories(parent)
        temp = Files.createTempFile(parent, null, null)
        channel = FileChannel.open(
            temp,
            StandardOpenOption.WRITE,
            StandardOpenOption.TRUNCATE_EXISTING,
            StandardOpenOption.CREATE
        )
    }

    fun write(buffer: ByteArray, offset: Int, length: Int) {
        try {
            digest?.update(buffer, offset, length)
            val wrapped = ByteBuffer.wrap(buffer, offset, length)
            while (wrapped.hasRemaining()) {
                channel.write(wrapped)
            }
        } catch (e: IOException) {
            broken = true
            throw e
        }
    }

    /** 下载成功后的收尾：校验 SHA-1 并把临时文件原子替换为目标文件；抛出视为可重试失败 */
    fun finish() {
        var moved = false
        try {
            channel.close()

            if (expectedChecksum != null) {
                val actual = digest!!.digest().toHexString()
                if (!expectedChecksum.equals(actual, ignoreCase = true)) {
                    throw ChecksumMismatchException("SHA-1", expectedChecksum, actual)
                }
            }

            Files.move(temp, target, StandardCopyOption.REPLACE_EXISTING)
            moved = true
        } finally {
            if (!moved) deleteTemp()
        }
    }

    override fun close() {
        runCatching { channel.close() }
        deleteTemp()
    }

    private fun deleteTemp() {
        runCatching { Files.deleteIfExists(temp) }
    }

    private fun ByteArray.toHexString(): String = joinToString(separator = "") { "%02x".format(it) }
}
