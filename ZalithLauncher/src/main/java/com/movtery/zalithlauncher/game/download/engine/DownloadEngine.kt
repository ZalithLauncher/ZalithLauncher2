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

import com.movtery.zalithlauncher.path.DOWNLOAD_OKHTTP_CLIENT
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import okhttp3.OkHttpClient
import java.util.concurrent.atomic.AtomicLong
import kotlin.time.Duration.Companion.milliseconds

/**
 * 引擎的单一文件便捷入口：完整获得分块、换源、降级重试能力，
 * 适合独立的小批量场景；大规模批量请使用 [BatchDownloader]。
 */
object DownloadEngine {
    const val DEFAULT_SINGLE_CONNECTIONS = 4

    /**
     * @param sizeCallback 落盘字节的增量回调；引擎内部的断点重试已被消化，调用方只会收到正值增量
     */
    suspend fun download(
        request: DownloadRequest,
        maxConnections: Int = DEFAULT_SINGLE_CONNECTIONS,
        stats: DownloadStats = DownloadStats(),
        sizeCallback: (Long) -> Unit = {},
        /** 显式注入用于测试；生产环境按请求大小自动选择传输客户端 */
        client: OkHttpClient? = null
    ) {
        if (request.expectedSize > 0) stats.registerFile(request.expectedSize)

        //与批量侧一致
        // 只有全局速度不足时才允许追加连接拆段
        val speedGate = {
            stats.refreshSpeed() < DownloadStats.LOW_SPEED_THRESHOLD_BPS
        }

        if (sizeCallback === defaultCallback) {
            FileDownloader(request, Semaphore(maxConnections), stats, allowExtraConnection = speedGate, client = client).download()
            stats.markFileFinished()
            return
        }

        val reported = AtomicLong(0L)
        coroutineScope {
            val reporter = launch(Dispatchers.Default) {
                while (isActive) {
                    delay(PROGRESS_SAMPLE_MS.milliseconds)
                    drain(reported, stats, sizeCallback)
                }
            }
            try {
                FileDownloader(request, Semaphore(maxConnections), stats, allowExtraConnection = speedGate, client = client).download()
                stats.markFileFinished()
                drain(reported, stats, sizeCallback)
            } finally {
                reporter.cancelAndJoin()
            }
        }
    }

    private fun drain(reported: AtomicLong, stats: DownloadStats, callback: (Long) -> Unit) {
        val total = stats.downloadedBytes
        val delta = total - reported.get()
        if (delta > 0) {
            reported.addAndGet(delta)
            callback(delta)
        }
    }

    private val defaultCallback: (Long) -> Unit = {}

    private const val PROGRESS_SAMPLE_MS = 100L
}
