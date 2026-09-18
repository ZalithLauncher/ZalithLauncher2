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

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.withTimeout
import mockwebserver3.Dispatcher
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import mockwebserver3.RecordedRequest
import okhttp3.OkHttpClient
import okio.Buffer
import org.junit.Test
import java.io.File
import java.lang.management.ManagementFactory
import java.nio.file.Files
import java.util.Random
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong

/**
 * 手动基准台架（默认跳过）：移除类上的 @Ignore 运行采集吞吐、测速保真与故障开销数据。
 *
 * 口径说明：MockWebServer 跑在本机回环上，所有吞吐数字都是"引擎处理上限"，
 * 真实网络场景由带宽与 RTT 决定，远低于该值属正常；绝对值请勿直接外推到真机。
 * 参考基线：48MiB 分块 ≈29.8MiB/s（16 连接）；2000 小文件批 ≈567-719 files/s；
 * 20% 注入 500 的重试开销约降低一成吞吐。
 */
@org.junit.Ignore("manual benchmark")
class DownloadEngineBench {

    private fun mebibytes(bytes: Long): String = "%.2f MiB".format(bytes / 1048576.0)

    private fun secondsOf(nanos: Long): String = "%.3fs".format(nanos / 1e9)

    private class RangedSource(private val content: ByteArray) : Dispatcher() {
        override fun dispatch(request: RecordedRequest): MockResponse {
            val range = request.headers["Range"]
                ?: return MockResponse.Builder()
                    .addHeader("Content-Length", content.size.toString())
                    .body(Buffer().write(content))
                    .build()

            val start = RANGE.find(range)!!.groupValues[1].toLong().toInt()
            return MockResponse.Builder()
                .addHeader("Content-Length", (content.size - start).toString())
                .code(206)
                .addHeader("Content-Range", "bytes $start-${content.size - 1}/${content.size}")
                .body(Buffer().write(content, start, content.size - start))
                .build()
        }

        companion object {
            private val RANGE = Regex("bytes=(\\d+)-")
        }
    }

    private class MultiFileSource(
        private val failRatePercent: Int,
        seed: Long
    ) : Dispatcher() {
        private val rng = Random(seed)
        val hits = AtomicLong(0)
        val injected500 = AtomicInteger(0)

        override fun dispatch(request: RecordedRequest): MockResponse {
            hits.incrementAndGet()
            if (failRatePercent > 0 && rng.nextInt(100) < failRatePercent &&
                injected500.incrementAndGet() % 2 == 1
            ) {
                return MockResponse.Builder().code(500).build()
            }
            val index = request.target.trim('/').toIntOrNull() ?: 0
            val content = payloadFor(index)
            return MockResponse.Builder()
                .addHeader("Content-Length", content.size.toString())
                .body(Buffer().write(content))
                .build()
        }

        companion object {
            fun payloadFor(index: Int): ByteArray {
                val size: Int = 6000 + (index % 37) * 7349
                val array = ByteArray(size)
                Random(index.toLong()).nextBytes(array)
                return array
            }
        }
    }

    private fun processAllocatedBytes(): Long {
        val bean = ManagementFactory.getThreadMXBean() as? com.sun.management.ThreadMXBean ?: return -1L
        if (!bean.isThreadAllocatedMemorySupported) return -1L
        if (!bean.isThreadAllocatedMemoryEnabled) bean.setThreadAllocatedMemoryEnabled(true)
        return bean.allThreadIds.sumOf { id ->
            runCatching { bean.getThreadAllocatedBytes(id) }.getOrDefault(0L)
        }.coerceAtLeast(0L)
    }

    private inline fun <T> withAllocTracking(block: () -> T): Pair<T, Long> {
        val before = processAllocatedBytes()
        val result = block()
        return result to (processAllocatedBytes() - before)
    }

    private fun allocText(delta: Long): String =
        if (delta <= 0) "N/A" else "%.1f MiB".format(delta / 1048576.0)

    @Test
    fun bench_single_large_file_segmented() {
        val payload = ByteArray((48L * 1024L * 1024L).toInt()).also { Random(7).nextBytes(it) }
        val dir = Files.createTempDirectory("bench-big").toFile()
        val server = MockWebServer().also { it.dispatcher = RangedSource(payload); it.start() }

        val target = File(dir, "big.bin")
        val request = DownloadRequest(listOf(server.url("/f").toString()), target, null, payload.size.toLong())

        runBlocking {
            withTimeout(120000) {
                val outcome = withAllocTracking {
                    val started = System.nanoTime()
                    FileDownloader(
                        request = request,
                        connections = Semaphore(16),
                        stats = DownloadStats(),
                        allowExtraConnection = { true },
                        maxWorkersPerFile = 16,
                        client = OkHttpClient()
                    ).download()
                    System.nanoTime() - started
                }
                println("[BIG] size=${mebibytes(payload.size.toLong())} wall=${secondsOf(outcome.first)} "
                        + "throughput=%.1f MiB/s alloc=%s".format(payload.size / 1048576.0 / (outcome.first / 1e9), allocText(outcome.second)))
            }
        }
        server.close(); dir.deleteRecursively()
    }

    @Test
    fun bench_batch_tail_small_files_and_speed_meter_fidelity() {
        val fileCount = 2000
        val dir = Files.createTempDirectory("bench-tail").toFile()
        val source = MultiFileSource(failRatePercent = 0, seed = 1)
        val server = MockWebServer().also { it.dispatcher = source; it.start() }

        val requests = (0 until fileCount).map { index ->
            DownloadRequest(
                urls = listOf(server.url("/$index").toString()),
                targetFile = File(dir, "f-$index.bin"),
                sha1 = null,
                expectedSize = MultiFileSource.payloadFor(index).size.toLong()
            )
        }
        val totalBytes = requests.sumOf { it.expectedSize }

        val speedSamples = ConcurrentLinkedQueue<Pair<Long, Long>>()
        val peakThreads = AtomicInteger(0)

        runBlocking {
            withTimeout(180000) {
                val outcome: Pair<Pair<Long, Int>, Long> = withAllocTracking {
                    val sampler = Thread {
                        while (!Thread.currentThread().isInterrupted) {
                            peakThreads.accumulateAndGet(Thread.activeCount()) { a, b -> maxOf(a, b) }
                            Thread.sleep(150)
                        }
                    }.apply { isDaemon = true }

                    val started = System.nanoTime()
                    val batch = BatchDownloader(
                        requests = requests,
                        maxConnections = 64,
                        retryRounds = 0,
                        clientOverride = OkHttpClient()
                    )
                    val successPaths = ConcurrentHashMap.newKeySet<String>()
                    batch.onFileSuccess = { r -> successPaths.add(r.targetFile.name); Unit }
                    batch.onUpdate = { snapshot -> speedSamples.add(System.nanoTime() to snapshot.speedBytesPerSec) }
                    sampler.start()
                    try {
                        batch.run()
                    } finally {
                        sampler.interrupt()
                        sampler.join(1000)
                    }
                    val missing = requests.mapNotNull { req ->
                        req.targetFile.name.takeIf { it !in successPaths }
                    }
                    println("[AUDIT] requested=${requests.size} onSuccess=${successPaths.size} "
                            + "missing=${missing.size} missingNames=${missing.take(10)}")
                    missing.forEach { name ->
                        val f = File(dir, name)
                        println("[AUDIT-MISS] $name exists=${f.exists()} len=${if (f.exists()) f.length() else -1} "
                                + "partExists=${File(dir, "$name.part").exists()}")
                    }
                    (System.nanoTime() - started) to successPaths.size
                }
                val wallNanosPair = outcome.first
                val wall = wallNanosPair.first
                val filesDone = wallNanosPair.second

                val values = speedSamples.map { it.second }.filter { it > 0 }
                val distinct = values.distinct()
                val reportedAvg = values.average()
                val trueAvg = totalBytes / (wall / 1e9)

                println("[TAIL] files=$fileCount done=$filesDone wall=${secondsOf(wall)} "
                        + "%.0f files/s bytes=%s".format(fileCount / (wall / 1e9), mebibytes(totalBytes)))
                println("[METER] updates=${speedSamples.size} nonzeroSamples=${values.size} "
                        + "distinct=${distinct.size} reportedAvg=%.0f trueAvg=%.0f dev=%.1f%%".format(reportedAvg, trueAvg, if (trueAvg > 0) (reportedAvg - trueAvg) / trueAvg * 100 else 0.0))
                println("[METER-VALUES] $distinct")
                println("[THREADS] peakActive=$peakThreads")
                println("[ALLOC] ${allocText(outcome.second)} for $fileCount tiny files")
            }
        }
        server.close(); dir.deleteRecursively()
    }

    @Test
    fun bench_failure_injection_overhead() {
        val fileCount = 800
        val dir = Files.createTempDirectory("bench-fail").toFile()
        val source = MultiFileSource(failRatePercent = 20, seed = 42)
        val server = MockWebServer().also { it.dispatcher = source; it.start() }

        val requests = (0 until fileCount).map { index ->
            DownloadRequest(
                urls = listOf(server.url("/$index").toString()),
                targetFile = File(dir, "f-$index.bin"),
                sha1 = null,
                expectedSize = MultiFileSource.payloadFor(index).size.toLong()
            )
        }

        runBlocking {
            withTimeout(180000) {
                val outcome = withAllocTracking {
                    val started = System.nanoTime()
                    val batch = BatchDownloader(
                        requests = requests,
                        maxConnections = 64,
                        retryRounds = 1,
                        clientOverride = OkHttpClient()
                    )
                    batch.onFailureFilter = { _, _ -> true }
                    batch.run()
                    System.nanoTime() - started
                }
                println("[FAIL-INJ] files=$fileCount injected500=${source.injected500.get()} "
                        + "serverHits=${source.hits.get()} wall=${secondsOf(outcome.first)} "
                        + "%.0f files/s alloc=%s".format(fileCount / (outcome.first / 1e9), allocText(outcome.second)))
            }
        }
        server.close(); dir.deleteRecursively()
    }

    @Test
    fun bench_speed_meter_cadence_and_truthfulness() {
        runBlocking {
            val stats = DownloadStats()
            val samples = mutableListOf<Pair<Long, Long>>()

            coroutineScope {
                launch(Dispatchers.Default) {
                    val deadline = System.nanoTime() + 5200000000L
                    while (System.nanoTime() < deadline) {
                        stats.addBytes(4L * 1024L * 1024L / 50)
                        delay(20)
                    }
                }

                val deadline = System.nanoTime() + 6500000000L
                while (System.nanoTime() < deadline) {
                    samples.add(System.nanoTime() to stats.refreshSpeed())
                    delay(100)
                }
            }

            val values = samples.map { it.second }
            val nonzeroDistinct = values.filter { it > 0 }.distinct().sorted()
            val changes = samples.zipWithNext().count { it.first.second != it.second.second }
            val steady = values.filter { it > 0 }
            val avg = steady.take(maxOf(steady.size - 8, 1)).average()

            println("[CADENCE] tick=100ms changesOver6.5s=$changes nonzeroSlots=${values.count { it > 0 }}")
            println("[CADENCE-VALUES] $nonzeroDistinct")
            println("[CADENCE-FIDELITY] steadyAvg=%.0f ideal=%.0f dev=%.2f%%".format(avg, 4194304.0, (avg - 4194304.0) / 4194304.0 * 100))
        }
    }
}
