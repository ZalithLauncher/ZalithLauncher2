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

import com.movtery.zalithlauncher.path.createRequestBuilder
import com.movtery.zalithlauncher.utils.file.calculateFileSha1
import com.movtery.zalithlauncher.utils.file.ensureParentDirectory
import com.movtery.zalithlauncher.utils.logging.Logger
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runInterruptible
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.ResponseBody
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.nio.channels.ClosedByInterruptException
import java.nio.channels.FileChannel
import kotlin.time.Duration.Companion.milliseconds

/**
 * 单文件下载作业：
 * 先以单连接确定大小与 Range 能力，全局速度不足时对大文件的剩余区间动态二分、多连接并发拉取，
 * 候选源失败自动轮转补位；全部源失效后降级为逐源单流整文件重试。
 * 全程写入 `<目标名>.part`，校验通过后改名为目标文件。
 */
class FileDownloader(
    private val request: DownloadRequest,
    private val connections: Semaphore,
    private val stats: DownloadStats,
    private val allowExtraConnection: () -> Boolean = { true },
    private val maxWorkersPerFile: Int = MAX_WORKERS_PER_FILE,
    private val maxAllSourcesWaits: Int = MAX_ALL_SOURCES_WAITS,
    private val client: OkHttpClient? = null,
    /** 整批共享的主机级健康状态，缺省时本文件独立计数 */
    private val sourceHealth: HostHealth = HostHealth(),
    private val slowWindowNanos: Long = SLOW_WINDOW_NANOS,
    private val slowFloorBytesPerSec: Long = SLOW_FLOOR_BYTES_PER_SEC
) {
    private val sources = SourceSet(request.urls, sourceHealth)
    private val transferClient: OkHttpClient = client ?: BatchDownloader.resolveTransferClient(request)

    /** 本轮尝试中实际向文件写入了数据的候选源，用于校验失败时的诊断 */
    private val contributedSources = LinkedHashSet<String>()

    suspend fun download() {
        val target = request.targetFile
        target.ensureParentDirectory()
        val partFile = File(target.parentFile, "${target.name}.part")

        val hashRetries = if (request.sha1 != null) 2 else 1
        repeat(hashRetries) { attempt ->
            try {
                runSingleAttempt(partFile)
                verifyAndCommit(partFile, target)
                return
            } catch (e: CancellationException) {
                partFile.delete()
                throw e
            } catch (e: Exception) {
                partFile.delete()
                if (e is ShaMismatchException) {
                    //校验失败前实际写过的源可能正在返回坏数据，计入主机健康让整批避开它
                    synchronized(contributedSources) {
                        contributedSources.toList()
                    }.forEach {
                        sourceHealth.recordCorruption(it)
                    }
                }
                if (attempt == hashRetries - 1) {
                    throw IOException("Failed to download ${target.name}\n${sources.describe()}", e)
                }
            }
        }
    }

    private suspend fun runSingleAttempt(partFile: File) {
        synchronized(contributedSources) { contributedSources.clear() }
        val chain = SegmentChain(request.expectedSize.takeIf { it > 0 } ?: -1L)
        withContext(Dispatchers.IO) {
            RandomAccessFile(partFile, "rw").use { randomAccess ->
                val channel = randomAccess.channel
                if (chain.totalSize >= 0) randomAccess.setLength(chain.totalSize)

                phaseNormal(chain, channel)
                if (!chain.isComplete()) {
                    //降级：复活非致命源，摊平进度并禁用分块逐源完整重下
                    chain.resetForSingleStream()
                    sources.degrade()
                    phaseSingleStream(chain, channel)
                }
                if (!chain.isComplete()) {
                    throw IOException("Incomplete download of ${request.targetFile.name}\n${sources.describe()}")
                }
            }
        }
    }

    /**
     * 主阶段：为无主且未完成的分段启动工作线程，
     * 并在全局速度不足时对剩余量最大的分段做二分追加分块。
     */
    private suspend fun phaseNormal(chain: SegmentChain, channel: FileChannel): Unit = coroutineScope {
        val splittable = chain.totalSize >= MIN_FILE_SIZE_FOR_SPLIT
        val workers = HashMap<SegmentChain.Segment, Job>()

        fun startWorker(
            source: SourceSet.Source,
            segment: SegmentChain.Segment
        ) {
            workers[segment] = launch(Dispatchers.IO) {
                connections.withPermit {
                    runInterruptible {
                        //源在派发时刻选好，但真正建连是拿到许可之后的事
                        //海量文件会在批次早期抢占式选源排队，停摆触发的熔断必须作用到建连时刻
                        if (!sourceHealth.isViable(source.url)) return@runInterruptible
                        guardFailure(source) {
                            executeSegment(source, segment, chain, channel)
                        }
                    }
                }
            }
        }

        var stalledTicks = 0
        var cooldownWaits = 0

        while (true) {
            workers.entries.removeAll { (_, job) -> job.isCompleted }

            val pending = chain.pendingSegments()
            if (pending.isEmpty()) break

            for (segment in pending) {
                if (workers.size >= maxWorkersPerFile) break
                if (segment in workers) continue
                //中段或已有进度的段必须依赖 Range 续传；从零开始的头段可退化为整流应答
                val needRange = segment.start > 0 || segment.position() > 0
                sources.acquire(needRange)?.let { source ->
                    startWorker(source, segment)
                }
            }

            if (splittable && allowExtraConnection() && workers.size < maxWorkersPerFile) {
                val largest = pending.maxByOrNull { it.remaining(chain.totalSize) }
                largest?.takeIf { it in workers }?.let { host ->
                    SegmentChain.splitOffset(host.position(), host.endOr(chain.totalSize), MIN_SPLIT_TAIL)?.let { cut ->
                        val tail = chain.split(host, cut)
                        sources.acquire(requireRange = true)?.let { source ->
                            startWorker(source, tail)
                        }
                    }
                }
            }

            if (workers.isNotEmpty()) {
                stalledTicks = 0
                delay(SPLIT_TICK_MS.milliseconds)
                continue
            }

            val waitingMs = sources.cooldownRemainingMillis()
            if (waitingMs > 0 && cooldownWaits < maxAllSourcesWaits) {
                cooldownWaits++
                stalledTicks = 0
                delay(waitingMs.milliseconds)
                continue
            }

            stalledTicks++
            if (stalledTicks > STALLED_TICK_LIMIT) return@coroutineScope
            delay(SPLIT_TICK_MS.milliseconds)
        }
    }

    /** 降级阶段：按源顺序各做一次无 Range 的完整单流下载 */
    private suspend fun phaseSingleStream(chain: SegmentChain, channel: FileChannel) {
        while (!chain.isComplete()) {
            val source = sources.acquireDegraded(requireRange = false) ?: break
            chain.resetForSingleStream()
            connections.withPermit {
                runInterruptible {
                    guardFailure(source) {
                        executeWhole(source, chain, channel)
                    }
                }
            }
        }
    }

    // ---------------- 阻塞世界（内无线程挂起点） ----------------

    /** 统一的失败归类：换源由调度循环在下一拍自动发生 */
    private fun guardFailure(source: SourceSet.Source, block: () -> Unit) {
        Logger.info(TAG, "Downloading: ${source.url}")
        try {
            block()
            synchronized(contributedSources) {
                contributedSources.add(source.url)
            }
            source.recordSuccess()
        } catch (_: AbandonedSegmentException) {
            //断点越界说明另一条连接已覆盖该区间，视作无事发生
            source.recordSuccess()
        } catch (e: ClosedByInterruptException) {
            throw cancellationOf(e)
        } catch (e: Exception) {
            if (e.isInterruptedByCancellation()) throw cancellationOf(e)
            Logger.warning(TAG, "Failed: ${source.url}")
            source.recordFailure(e)
        }
    }

    private fun cancellationOf(cause: Throwable): CancellationException =
        CancellationException("download interrupted").initCauseIfNeeded(cause)

    private fun CancellationException.initCauseIfNeeded(cause: Throwable): CancellationException =
        apply { initCause(cause) }

    private fun executeSegment(source: SourceSet.Source, segment: SegmentChain.Segment, chain: SegmentChain, channel: FileChannel) {
        //部分 CDN 的鉴权层会拒绝任何携带 Range 的请求，从零开始的段必须以普通 GET 起步
        val rangeFrom = segment.position().takeIf { it > 0L } ?: -1L
        executeCall(source.url, rangeFrom).use { response ->
            val body = checkStatus(response, rangeFrom, rangedRequest = rangeFrom >= 0, source = source, chain = chain)
            streamInto(body.byteStream(), segment, chain, channel)
        }
    }

    private fun executeWhole(source: SourceSet.Source, chain: SegmentChain, channel: FileChannel) {
        executeCall(source.url, -1L).use { response ->
            val body = checkStatus(response, -1L, rangedRequest = false, source = source, chain = chain)
            streamInto(body.byteStream(), chain.first, chain, channel)
        }
    }

    /**
     * 校验状态码与 Range 契约：
     * 416 视作越界完成；被忽略的 Range 应答仅在“可当作完整文件消费”时放行。
     */
    private fun checkStatus(response: Response, rangeFrom: Long, rangedRequest: Boolean, source: SourceSet.Source, chain: SegmentChain): ResponseBody {
        val code = response.code
        val body = response.body ?: throw HttpResultException(code, "Empty body")
        when (code) {
            416 -> throw AbandonedSegmentException()

            206 -> parseContentRangeTotal(response.header(CONTENT_RANGE))?.let { chain.adoptTotal(it) }

            200 -> {
                if (rangedRequest) {
                    source.markNoRangeSupport()
                    if (rangeFrom > 0) {
                        throw HttpResultException(code, "Server ignored Range header")
                    }
                }
                //尽力从声明长度升级为有界模式：否则无界流的提前断流会被误判为完整下载
                val declared = body.contentLength()
                if (declared > 0) chain.adoptTotal(declared)
            }

            else -> throw HttpResultException(code, "HTTP $code ${response.message}")
        }
        return body
    }

    private fun executeCall(url: String, rangeFrom: Long): Response {
        val builder = createRequestBuilder(url)
        if (rangeFrom >= 0) builder.header(RANGE_HEADER, "bytes=$rangeFrom-")
        return transferClient.newCall(builder.build()).execute()
    }

    /**
     * 把响应流写入所属区间：每次读取前重新计算右边界（分块分裂会把边界向内收缩），
     * 多线程经 positioned write 落盘到同一文件的不同偏移，互不重叠。
     */
    private fun streamInto(input: InputStream, segment: SegmentChain.Segment, chain: SegmentChain, channel: FileChannel) {
        val unknownSize = chain.totalSize < 0
        //每个并发读流各持一份缓冲与同底数组的写视图，整个流生命周期零增量分配
        val buffer = ByteArray(BUFFER_SIZE)
        val bufferView = ByteBuffer.wrap(buffer)
        var position = segment.position()
        var windowStartNanos = System.nanoTime()
        var windowBytes = 0L

        while (true) {
            val endNow = segment.endOr(chain.tailEnd())
            val remaining = endNow - position
            if (remaining <= 0L) break

            val wanted = minOf(buffer.size.toLong(), remaining).toInt()
            val read = input.read(buffer, 0, wanted)
            if (read < 0) {
                if (unknownSize) {
                    chain.eofReached()
                    break
                }
                throw IOException("Unexpected EOF at $position")
            }

            writeAt(channel, bufferView, read, position)
            position += read
            segment.done.addAndGet(read.toLong())
            stats.addBytes(read.toLong())
            windowBytes += read

            // 慢速看门狗
            val elapsedNanos = System.nanoTime() - windowStartNanos
            if (elapsedNanos >= slowWindowNanos) {
                if (windowBytes * NANOS_PER_SEC / elapsedNanos < slowFloorBytesPerSec) {
                    throw SlowTransferException(windowBytes, elapsedNanos)
                }
                windowStartNanos = System.nanoTime()
                windowBytes = 0L
            }
        }
    }

    private fun writeAt(channel: FileChannel, view: ByteBuffer, count: Int, fromOffset: Long) {
        view.clear()
        view.limit(count)

        var position = fromOffset
        while (view.hasRemaining()) {
            val written = channel.write(view, position)
            if (written <= 0) throw IOException("FileChannel refused write ($written)")
            position += written
        }
    }

    private suspend fun verifyAndCommit(partFile: File, target: File) {
        request.sha1?.let { expected ->
            val actual = calculateFileSha1(partFile)
            if (!actual.equals(expected, ignoreCase = true)) {
                throw ShaMismatchException(
                    targetName = target.name,
                    expected = expected.lowercase(),
                    actual = actual,
                    length = partFile.length(),
                    sources = synchronized(contributedSources) { contributedSources.toList() }
                )
            }
        }
        target.delete()
        if (!partFile.renameTo(target)) {
            partFile.copyTo(target, overwrite = true)
            partFile.delete()
        }
    }

    private fun parseContentRangeTotal(headerValue: String?): Long? {
        val totalPart = headerValue?.substringAfter('/', "")?.trim() ?: return null
        if (totalPart == "*") return null
        return totalPart.toLongOrNull()
    }

    /** 标记区间已被其他连接完成时的静默退出信号 */
    private class AbandonedSegmentException : IOException("Range not satisfiable")

    private class SlowTransferException(
        bytes: Long,
        elapsedNanos: Long
    ) : IOException("Transfer rate ${bytes * NANOS_PER_SEC / elapsedNanos} B/s is below the floor")

    private class ShaMismatchException(
        targetName: String,
        expected: String,
        actual: String,
        length: Long,
        sources: List<String>
    ) : IOException(buildString {
        append("Content integrity mismatch for ").append(targetName)
        appendLine().append("expected sha1: ").append(expected)
        append("actual   sha1: ").append(actual)
        if (length > 0) {
            appendLine().append("actual   size: ").append(length)
        }
        if (!sources.isEmpty()) {
            appendLine().append("sources used:")
            sources.forEach { appendLine().append("  ").append(it) }
        }
    })

    companion object {
        private const val TAG = "FileDownloader"

        const val BUFFER_SIZE = 64 * 1024
        const val MIN_SPLIT_TAIL = 256L * 1024L
        const val MIN_FILE_SIZE_FOR_SPLIT = 1024L * 1024L
        const val MAX_WORKERS_PER_FILE = 6
        const val SPLIT_TICK_MS = 200L

        private const val STALLED_TICK_LIMIT = 5
        /** 全部源熔断冷却时最多原地等待的轮数，之后进入降级通道真实尝试 */
        const val MAX_ALL_SOURCES_WAITS = 2
        private const val RANGE_HEADER = "Range"
        private const val CONTENT_RANGE = "Content-Range"

        // 慢速看门狗的观测窗口与速率地板
        const val SLOW_WINDOW_NANOS = 10_000_000_000L
        const val SLOW_FLOOR_BYTES_PER_SEC = 8L * 1024L
        private const val NANOS_PER_SEC = 1_000_000_000L
    }
}
