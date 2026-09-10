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

package com.movtery.zalithlauncher.game.version.download

import com.movtery.zalithlauncher.coroutine.Task
import com.movtery.zalithlauncher.game.download.engine.BatchDownloader
import com.movtery.zalithlauncher.game.download.engine.BatchProgress
import com.movtery.zalithlauncher.utils.logging.Logger
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext

private const val TAG = "TaskBatch"

/** 本地已存在文件校验的并发度 */
private const val LOCAL_VERIFY_PARALLELISM = 4

/**
 * 把一批 [DownloadTask] 交给下载引擎执行的统一入口：
 * 先挑出本地已可复用的文件（直接计入完成并执行挂后任务），
 * 剩余的交给引擎分块并发下载；仍有失败时抛出 [DownloadFailedException]，
 * message 内含失败文件路径。
 *
 * @param onSnapshot 每 100ms 收到一次引擎统计快照
 */
suspend fun Task.runBatchDownloads(
    tasks: List<DownloadTask>,
    maxConnections: Int,
    retryRounds: Int = 1,
    onSnapshot: suspend (BatchProgress) -> Unit = {},
    acceptFailure: ((task: DownloadTask, error: Throwable) -> Boolean)? = null
) {
    val verifyStarted = System.currentTimeMillis()
    val (reusable, pending) = verifyExistingFilesConcurrently(tasks)
    Logger.info(TAG, "Local file check done: reusable=${reusable.size} pending=${pending.size}, took=${System.currentTimeMillis() - verifyStarted}ms")

    //清单里只要有未声明大小的文件，字节数就不能构成可靠的进度分母
    val sizesFullyKnown = tasks.all { it.size > 0 }

    val batch = BatchDownloader(
        requests = pending.map { it.toRequest() },
        maxConnections = maxConnections,
        retryRounds = retryRounds
    )

    reusable.forEach {
        batch.stats.registerFile(it.size)
        batch.stats.markFileFinished()
        it.runFileDownloadedTask()
    }

    //已复用文件的字节并入"已下载"口径，进度条才能从已完成部分起步
    if (reusable.isNotEmpty()) {
        batch.stats.addBytes(reusable.sumOf { maxOf(it.size, 0L) })
        //随后重置测速基线，避免一次性并入的字节被当作瞬时速率报出
        batch.stats.resetSpeedBaseline()
    }

    batch.onUpdate = { snapshot ->
        updateProgress(progressFor(snapshot, sizesFullyKnown, hasFileCount = snapshot.totalFiles > 0))
        onSnapshot(snapshot)
    }
    batch.onFileSuccess = { request ->
        (request.tag as? DownloadTask)?.runFileDownloadedTask()
    }
    acceptFailure?.let { judge ->
        batch.onFailureFilter = { request, error ->
            (request.tag as? DownloadTask)?.let { judge(it, error) } ?: false
        }
    }

    try {
        batch.run()
        updateProgress(1f)
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        val failedDetail = batch.lastRunFailures.keys.joinToString("\n") { path -> path }
        throw DownloadFailedException("Failed downloads:\n$failedDetail", e)
    } finally {
        clearSpeed()
    }
}

/**
 * 并行校验本地已存在的文件是否可复用
 */
private suspend fun verifyExistingFilesConcurrently(
    tasks: List<DownloadTask>
): Pair<List<DownloadTask>, List<DownloadTask>> =
    withContext(Dispatchers.IO.limitedParallelism(LOCAL_VERIFY_PARALLELISM)) {
        coroutineScope {
            tasks.map { task ->
                async { task to task.existingFileValid() }
            }.awaitAll()
        }.partition { it.second }
            .let { (reusable, pending) -> reusable.map { it.first } to pending.map { it.first } }
    }

/**
 * 进度条的显示策略：
 * - 清单内所有文件都声明了大小时，按字节数计算最平滑的进度；
 * - 存在未知大小但文件总数确定时，退化为按完成文件数计算；
 * - 连文件总数都无法确定时，显示为不确定进度。
 */
private fun progressFor(snapshot: BatchProgress, sizesFullyKnown: Boolean, hasFileCount: Boolean): Float = when {
    sizesFullyKnown && snapshot.totalBytes > 0 ->
        (snapshot.downloadedBytes.toFloat() / snapshot.totalBytes).coerceIn(0f, 1f)

    hasFileCount && snapshot.totalFiles > 0 ->
        snapshot.downloadedFiles.toFloat() / snapshot.totalFiles

    else -> -1f
}

