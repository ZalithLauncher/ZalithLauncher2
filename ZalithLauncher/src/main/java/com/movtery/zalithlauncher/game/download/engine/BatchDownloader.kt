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
import com.movtery.zalithlauncher.path.DOWNLOAD_OKHTTP_CLIENT_MULTIPLEX
import com.movtery.zalithlauncher.utils.logging.Logger
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import okhttp3.OkHttpClient
import java.io.IOException
import java.util.concurrent.ConcurrentHashMap
import kotlin.time.Duration.Companion.milliseconds

/** 整批下载结束后仍有未成功（且未被 [BatchDownloader.onFailureFilter] 接受）的文件 */
class BatchDownloadException(summary: String) : IOException(summary)

/**
 * 批量下载编排器：
 * 所有文件共享一个全局连接信号量；大文件的分块决策基于整批聚合速度，
 * 低速时才追加分块连接，避免对下载源的请求风暴。
 * 每个失败文件在所有候选源耗尽后还会参与下一轮整批重试。
 * 文件维度的统计在 run() 之前登记（调用方可先把本地已复用文件计入），
 * 因此 [run] 对同一实例至多调用一次。
 */
class BatchDownloader(
    private val requests: List<DownloadRequest>,
    private val maxConnections: Int = DEFAULT_MAX_CONNECTIONS,
    private val retryRounds: Int = 1,
    /** 显式注入用于测试；生产环境下按文件大小自动选择传输客户端 */
    private val clientOverride: OkHttpClient? = null
) {
    val stats = DownloadStats()

    /** 整批共享的主机级健康登记 */
    private val sourceHealth = HostHealth()

    /** 每 100ms 收到一次进度快照；回调运行在调度线程上，只应做轻量转发 */
    var onUpdate: (suspend (BatchProgress) -> Unit)? = null

    var onFileSuccess: (suspend (DownloadRequest) -> Unit)? = null

    /**
     * 文件重试轮次全部结束后仍失败的裁决：返回 true 表示接受现状继续
     * （例如可缺失的附加内容），false 则计入最终失败集合。
     */
    var onFailureFilter: ((DownloadRequest, Throwable) -> Boolean)? = null

    private val connections = Semaphore(maxConnections)

    /** 最近一次 run 结束后的失败清单（目标文件路径 → 异常），供调用方诊断 */
    var lastRunFailures: Map<String, Throwable> = emptyMap()
        private set

    suspend fun run() {
        // 不做任何清零
        // 调用方可能在 run() 之前已把本地复用文件登记进 stats
        requests.forEach { stats.registerFile(it.expectedSize) }

        val failures = ConcurrentHashMap<String, Throwable>()
        coroutineScope {
            val reporter = launch(Dispatchers.Default) {
                while (isActive) {
                    delay(PROGRESS_INTERVAL_MS.milliseconds)
                    onUpdate?.invoke(stats.snapshotProgress())
                }
            }

            try {
                requests.map { request ->
                    launch(Dispatchers.IO) {
                        //先取得一个连接许可再打开 .part 文件：
                        //否则全部作业同时持着打开的句柄排队，海量句柄会拖垮存储层
                        connections.withPermit { }
                        runOne(request, failures, fileClientFor(request))
                    }
                }.joinAll()
            } finally {
                reporter.cancelAndJoin()
            }
        }

        val finished = stats.downloadedFiles
        if (finished != requests.size) {
            val failedCount = failures.size
            val outcome = if (failedCount == 0) "all" else "$failedCount failed"
            Logger.warning(TAG, "Completed-count mismatch: requests=" + requests.size
                    + " finished=" + finished + " failures=" + outcome)
        }

        if (failures.isNotEmpty()) {
            lastRunFailures = failures.toMap()
            val detail = failures.entries.joinToString(separator = "\n") { (path, error) ->
                "$path: ${error.message ?: error::class.simpleName}"
            }
            //数千文件全挂时详情会淹没日志，仅保留前若干条；完整清单留在 lastRunFailures
            val summaryLines = detail.lines()
            val summary = if (summaryLines.size > MAX_FAILURE_DETAIL_LINES) {
                summaryLines.take(MAX_FAILURE_DETAIL_LINES).joinToString("\n") +
                        "\n... and ${summaryLines.size - MAX_FAILURE_DETAIL_LINES} more lines"
            } else {
                detail
            }
            throw BatchDownloadException(summary)
        }
    }

    /** 小文件走可多路复用的 h2 客户端，大文件保持 HTTP/1.1 的分段并发；测试注入的客户端优先生效 */
    private fun fileClientFor(request: DownloadRequest): OkHttpClient =
        clientOverride ?: if (request.expectedSize in 1 until SMALL_TRANSFER_MAX_BYTES) {
            DOWNLOAD_OKHTTP_CLIENT_MULTIPLEX
        } else {
            DOWNLOAD_OKHTTP_CLIENT
        }

    private suspend fun runOne(
        request: DownloadRequest,
        failures: MutableMap<String, Throwable>,
        transferClient: OkHttpClient
    ) {
        var lastError: Throwable? = null
        repeat(retryRounds + 1) {
            try {
                FileDownloader(
                    request = request,
                    connections = connections,
                    stats = stats,
                    allowExtraConnection = ::speedGate,
                    client = transferClient,
                    sourceHealth = sourceHealth
                ).download()
                onFileSuccess?.invoke(request)
                stats.markFileFinished()
                return
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                lastError = e
            }
        }
        lastError?.let { error ->
            Logger.error(TAG, "Download failed permanently: ${request.targetFile.absolutePath}", error)
            if (onFailureFilter?.invoke(request, error) == true) {
                stats.markFileFinished()
            } else {
                failures[request.targetFile.absolutePath] = error
            }
        }
    }

    /** 分块扩张的闸门：只有整批速度偏低时才允许新开连接拆段 */
    private fun speedGate(): Boolean = stats.refreshSpeed() < DownloadStats.LOW_SPEED_THRESHOLD_BPS

    companion object {
        private const val TAG = "BatchDownloader"
        const val DEFAULT_MAX_CONNECTIONS = 64
        const val PROGRESS_INTERVAL_MS = 100L

        /** 异常详情中最多列出的失败条数 */
        private const val MAX_FAILURE_DETAIL_LINES = 20

        /** 小于该阈值的文件走 h2 多路复用客户端；更大的文件保持 1.1 分段并发 */
        const val SMALL_TRANSFER_MAX_BYTES: Long = 4L * 1024L * 1024L

        fun resolveTransferClient(sample: DownloadRequest?): OkHttpClient =
            sample?.takeIf { it.expectedSize in 1 until SMALL_TRANSFER_MAX_BYTES }
                ?.let { DOWNLOAD_OKHTTP_CLIENT_MULTIPLEX }
                ?: DOWNLOAD_OKHTTP_CLIENT
    }
}
