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

package com.movtery.zalithlauncher.utils.file

import java.io.File
import java.nio.file.Files

object MurmurHash2Incremental {
    private const val M32 = 0x5bd1e995
    private const val R32 = 24

    fun computeHash(file: File, byteToSkip: List<Int> = emptyList(), seed: Int = 1): Long {
        val totalLength = getFilteredLength(file, byteToSkip)
        return computeHashInternal(file, byteToSkip, totalLength, seed)
    }

    /**
     * 已知过滤后长度时的哈希计算，避免为统计长度而重复扫描文件
     */
    fun computeHash(
        file: File,
        byteToSkip: List<Int>,
        filteredLength: Int,
        seed: Int = 1
    ): Long = computeHashInternal(file, byteToSkip, filteredLength, seed)

    private fun getFilteredLength(
        file: File,
        byteToSkip: List<Int>
    ): Int {
        var length = 0
        Files.newInputStream(file.toPath()).use { stream ->
            val buf = ByteArray(8192)
            var bytesRead: Int
            while (stream.read(buf).also { bytesRead = it } != -1) {
                for (i in 0 until bytesRead) {
                    val value = buf[i].toInt() and 0xFF
                    if (value !in byteToSkip) length++
                }
            }
        }
        return length
    }

    private fun computeHashInternal(
        file: File,
        byteToSkip: List<Int>,
        totalLength: Int,
        seed: Int
    ): Long {
        var h = seed.toLong() and 0xFFFFFFFFL xor totalLength.toLong()
        val buffer = ByteArray(4)
        var bufferIndex = 0

        Files.newInputStream(file.toPath()).use { stream ->
            val buf = ByteArray(8192)
            var bytesRead: Int
            while (stream.read(buf).also { bytesRead = it } != -1) {
                for (i in 0 until bytesRead) {
                    val b = buf[i]
                    val value = b.toInt() and 0xFF

                    //跳过指定字节
                    if (value in byteToSkip) continue

                    buffer[bufferIndex++] = b

                    if (bufferIndex == 4) {
                        val k = (buffer[0].toInt() and 0xFF) or
                                ((buffer[1].toInt() and 0xFF) shl 8) or
                                ((buffer[2].toInt() and 0xFF) shl 16) or
                                ((buffer[3].toInt() and 0xFF) shl 24)

                        var kk = k
                        kk *= M32
                        kk = kk xor (kk ushr R32)
                        kk *= M32

                        h = (h * M32) and 0xFFFFFFFFL
                        h = h xor (kk.toLong() and 0xFFFFFFFFL)
                        bufferIndex = 0
                    }
                }
            }
        }

        when (bufferIndex) {
            3 -> {
                h = h xor ((buffer[2].toInt() and 0xFF) shl 16).toLong()
                h = h xor ((buffer[1].toInt() and 0xFF) shl 8).toLong()
                h = h xor (buffer[0].toInt() and 0xFF).toLong()
                h = (h * M32) and 0xFFFFFFFFL
            }
            2 -> {
                h = h xor ((buffer[1].toInt() and 0xFF) shl 8).toLong()
                h = h xor (buffer[0].toInt() and 0xFF).toLong()
                h = (h * M32) and 0xFFFFFFFFL
            }
            1 -> {
                h = h xor (buffer[0].toInt() and 0xFF).toLong()
                h = (h * M32) and 0xFFFFFFFFL
            }
        }

        h = h xor (h ushr 13)
        h = (h * M32) and 0xFFFFFFFFL
        h = h xor (h ushr 15)

        return h and 0xFFFFFFFFL
    }
}