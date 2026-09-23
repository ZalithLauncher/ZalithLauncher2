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

import com.movtery.zalithlauncher.game.download.engine.BatchDownloader.Companion.PROGRESS_INTERVAL_MS
import com.movtery.zalithlauncher.game.download.engine.BatchDownloader.Companion.SYSTEMIC_FAILURE_LIMIT
import com.movtery.zalithlauncher.utils.logging.Logger
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import java.io.IOException
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference
import kotlin.time.Duration.Companion.milliseconds

/** 整批下载结束后仍有未成功（且未被 [BatchDownloader.onFailureFilter] 接受）的文件 */
class BatchDownloadException(summary: String, cause: Throwable? = null) : IOException(summary, cause)

/**
 * 批量下载编排器：文件级并发由信号量控制，同一时刻至多 [maxConnections] 个文件在传输；
 * 每个文件在单个候选源内的重试与换源由引擎消化，全部候选源耗尽的文件还会参与下一轮整批重试。
 * 系统性故障熔断：整批零成功时，连续 [SYSTEMIC_FAILURE_LIMIT] 个文件永久失败即取消剩余文件并立即失败，
 * 避免在确定性故障（如引擎或源配置错误）上空转数万次重试。
 * 文件维度的统计在 run() 之前登记（调用方可先把本地已复用文件计入），
 * 因此 [run] 对同一实例至多调用一次。
 */
class BatchDownloader(
    private val requests: List<DownloadRequest>,
    private val maxConnections: Int = DEFAULT_MAX_CONNECTIONS,
    private val retryRounds: Int = 1
) {
    val stats = DownloadStats()

    /** 每 [PROGRESS_INTERVAL_MS] 收到一次进度快照；回调运行在调度线程上，只应做轻量转发 */
    var onUpdate: (suspend (BatchProgress) -> Unit)? = null

    var onFileSuccess: (suspend (DownloadRequest) -> Unit)? = null

    /**
     * 文件重试轮次全部结束后仍失败的裁决：返回 true 表示接受现状继续
     * （例如可缺失的附加内容），false 则计入最终失败集合。
     */
    var onFailureFilter: ((DownloadRequest, Throwable) -> Boolean)? = null

    private val files = Semaphore(maxConnections)

    /** 连续永久失败计数，任何文件成功即清零；与零成功条件共同判定系统性故障 */
    private val systemicFailureCount = AtomicInteger(0)

    /** 系统性故障的熔断原因，null 表示未触发 */
    private val systemicAbortCause = AtomicReference<Throwable?>(null)

    /** 当前整批在途文件作业，熔断时逐个直接取消（遍历子任务列表在高速完成-脱离下有漏节点的竞态） */
    private val activeFileJobs = AtomicReference<List<Job>?>(null)

    /** 最近一次 run 结束后的失败清单（目标文件路径 → 异常），供调用方诊断 */
    var lastRunFailures: Map<String, Throwable> = emptyMap()
        private set

    suspend fun run() {
        // 不做任何清零
        // 调用方可能在 run() 之前已把本地复用文件登记进 stats
        requests.forEach { stats.registerFile(it.expectedSize) }
        systemicFailureCount.set(0)
        systemicAbortCause.set(null)

        val failures = ConcurrentHashMap<String, Throwable>()
        coroutineScope {
            val reporter = launch(Dispatchers.Default) {
                while (isActive) {
                    delay(PROGRESS_INTERVAL_MS.milliseconds)
                    onUpdate?.invoke(stats.snapshotProgress())
                }
            }

            try {
                val fileJobs = requests.map { request ->
                    launch(Dispatchers.IO) {
                        //先取得一个文件许可再打开临时文件：
                        //否则全部作业同时持着打开的句柄排队，海量句柄会拖垮存储层
                        files.withPermit {
                            runOne(request, failures)
                        }
                    }
                }
                activeFileJobs.set(fileJobs)
                fileJobs.joinAll()
            } finally {
                activeFileJobs.set(null)
                reporter.cancelAndJoin()
            }
        }

        systemicAbortCause.get()?.let { cause ->
            lastRunFailures = failures.toMap()
            throw BatchDownloadException(
                "Batch aborted after $SYSTEMIC_FAILURE_LIMIT consecutive permanent failures " +
                        "with no successful download; the failure looks systemic rather than per-file",
                cause
            )
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

    private suspend fun runOne(
        request: DownloadRequest,
        failures: MutableMap<String, Throwable>
    ) {
        var lastError: Throwable? = null
        repeat(retryRounds + 1) {
            try {
                FileDownloader(request, stats).download()
                onFileSuccess?.invoke(request)
                stats.markFileFinished()
                systemicFailureCount.set(0)
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
                systemicFailureCount.set(0)
                return
            }
            failures[request.targetFile.absolutePath] = error

            //整批零成功且连续多个文件永久失败：判定为系统性故障，
            //继续磨完剩余文件只会产生海量无效重试，立即取消整批
            if (stats.downloadedFiles == 0) {
                val count = systemicFailureCount.incrementAndGet()
                if (count >= SYSTEMIC_FAILURE_LIMIT && systemicAbortCause.compareAndSet(null, error)) {
                    Logger.warning(TAG, "Aborting batch: $count consecutive permanent failures with zero successful downloads", error)
                    activeFileJobs.get()?.forEach { it.cancel() }
                }
            }
        }
    }

    companion object {
        private const val TAG = "BatchDownloader"
        const val DEFAULT_MAX_CONNECTIONS = 64
        const val PROGRESS_INTERVAL_MS = 100L

        /** 异常详情中最多列出的失败条数 */
        private const val MAX_FAILURE_DETAIL_LINES = 20

        /** 整批零成功时，连续永久失败达到该数量即判定系统性故障并中止整批 */
        const val SYSTEMIC_FAILURE_LIMIT = 5
    }
}
