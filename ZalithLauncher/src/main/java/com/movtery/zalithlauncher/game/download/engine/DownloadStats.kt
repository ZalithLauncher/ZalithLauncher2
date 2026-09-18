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

import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong

/** 一份批量下载的只读进度快照，供 UI 层消费 */
data class BatchProgress(
    val downloadedBytes: Long,
    val totalBytes: Long,
    val downloadedFiles: Int,
    val totalFiles: Int,
    val speedBytesPerSec: Long
)

/**
 * 跨线程的字节/文件计数器，内嵌"逐秒分块"测速：
 * 每个报表值就是刚刚完整过去的一秒里真实落盘的字节数，每秒更新一次，
 * 不做任何跨窗口平滑或外推——报出来的数字永远有对应的实际流量。
 * 热路径只有原子加法。
 * 文件/字节维度全部使用原子量，最多与并发连接数同量级的线程同时完成也不会丢计数。
 */
class DownloadStats {
    private val downloaded = AtomicLong(0L)
    private val totalBytesCounter = AtomicLong(-1L)
    private val totalFilesCounter = AtomicInteger(0)

    private val downloadedFilesCounter = AtomicInteger(0)
    val expectedTotalBytes: Long get() = totalBytesCounter.get()

    val totalFiles: Int get() = totalFilesCounter.get()
    val downloadedFiles: Int get() = downloadedFilesCounter.get()

    fun addBytes(count: Long) {
        downloaded.addAndGet(count)
    }

    fun registerFile(expectedSize: Long) {
        totalFilesCounter.incrementAndGet()
        if (expectedSize > 0) {
            totalBytesCounter.accumulateAndGet(expectedSize) { previous, add ->
                if (previous < 0) add else previous + add
            }
        }
    }

    fun markFileFinished() {
        downloadedFilesCounter.incrementAndGet()
    }

    /**
     * 把"本地已复用文件"的字节并入已下载量
     */
    fun resetSpeedBaseline() {
        synchronized(this) {
            blockStartNanos = System.nanoTime()
            blockStartBytes = downloaded.get()
        }
    }

    val downloadedBytes: Long get() = downloaded.get()

    /**
     * 返回上一个完整采样秒的真实平均吞吐
     */
    fun refreshSpeed(): Long {
        val now = System.nanoTime()
        synchronized(this) {
            val elapsed = now - blockStartNanos
            if (elapsed < SAMPLE_INTERVAL_NANOS) return currentSpeed

            val bytes = downloaded.get()
            currentSpeed = ((bytes - blockStartBytes) * NANOS_PER_SEC / elapsed)
                .coerceAtLeast(0L)
            blockStartNanos = now
            blockStartBytes = bytes
            return currentSpeed
        }
    }

    /** 先刷新测速再产出快照，调用方无需单独触发采样 */
    fun snapshotProgress(): BatchProgress = BatchProgress(
        downloadedBytes = downloaded.get(),
        totalBytes = expectedTotalBytes,
        downloadedFiles = downloadedFiles,
        totalFiles = totalFiles,
        speedBytesPerSec = refreshSpeed()
    )

    private var blockStartNanos = System.nanoTime()
    private var blockStartBytes = 0L

    private var currentSpeed: Long = 0L

    companion object {
        /** 引擎判定"速度偏低需要补连接"的水位线 */
        const val LOW_SPEED_THRESHOLD_BPS: Long = 256L * 1024L
        /** 速率采样周期 */
        private const val SAMPLE_INTERVAL_NANOS = 1_000_000_000L
        private const val NANOS_PER_SEC = 1_000_000_000L
    }
}
