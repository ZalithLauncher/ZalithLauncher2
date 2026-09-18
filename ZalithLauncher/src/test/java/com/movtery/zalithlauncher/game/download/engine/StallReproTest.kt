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

import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import mockwebserver3.Dispatcher
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import mockwebserver3.RecordedRequest
import okhttp3.OkHttpClient
import okio.Buffer
import org.junit.Test
import java.io.File
import java.nio.file.Files
import java.security.MessageDigest
import java.util.Random
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong

/**
 * 复现"字节冻结 + 0B/s + 文件数上涨"的用户症状：
 * 官方源周期性进入'建连成功但字节停摆'窗口（跨国拥塞常见形态），
 * 镜像源保持快；主机级熔断应当在停摆后把后续文件切换到镜像源。
 * 统计每 100ms 快照并检测特征窗口。
 */

@org.junit.Ignore("manual stall repro")
class StallReproTest {

    private class SlowOfficialSource : Dispatcher() {
        val hits = AtomicLong(0)

        override fun dispatch(request: RecordedRequest): MockResponse {
            val index = request.target.trim('/').toIntOrNull() ?: 0
            val content = payloadFor(index)
            val served = hits.incrementAndGet()

            //停摆编排（读超时 5s，模拟生产端 8s 短超时的角色）：
            //40~49 号 body 停摆 8~10 秒：远超读超时，批量超时使官方主机熔断，后续文件应切到镜像
            //300~349 号 body 停摆 6~8 秒：同样越界超时，用于制造 0B/s 特征窗口
            //其余请求 0.4~0.6 秒：官方源常态高延迟，派发速率受限（贴近真实网络节奏）
            val phase = served % 1000
            val bodyDelayMs: Long = when {
                phase in 40..49 -> 8000L + (index % 5) * 600L
                phase in 300..349 -> 6000L + (index % 7) * 300L
                else -> 400L + (index % 11) * 20L
            }
            return MockResponse.Builder()
                .addHeader("Content-Length", content.size.toString())
                .body(Buffer().write(content))
                .bodyDelay(bodyDelayMs, TimeUnit.MILLISECONDS)
                .build()
        }
    }

    private class FastMirrorSource : Dispatcher() {
        val hits = AtomicLong(0)

        override fun dispatch(request: RecordedRequest): MockResponse {
            hits.incrementAndGet()
            val index = request.target.trim('/').toIntOrNull() ?: 0
            val content = payloadFor(index)
            return MockResponse.Builder()
                .addHeader("Content-Length", content.size.toString())
                .body(Buffer().write(content))
                .bodyDelay(15L, TimeUnit.MILLISECONDS)
                .build()
        }
    }

    private data class Sample(
        val t: Long,
        val files: Int,
        val bytes: Long,
        val speed: Long
    )

    @Test
    fun `reproduce stall signature`() = runBlocking<Unit> {
        val dir = Files.createTempDirectory("stall-repro").toFile()
        val official = MockWebServer().also { it.dispatcher = SlowOfficialSource(); it.start() }
        val mirror = MockWebServer().also { it.dispatcher = FastMirrorSource(); it.start() }

        val fileCount = 2000
        val client = OkHttpClient.Builder()
            .connectionPool(okhttp3.ConnectionPool(64, 5, TimeUnit.MINUTES))
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(5, TimeUnit.SECONDS)
            .build()

        val requests = (0 until fileCount).map { index ->
            DownloadRequest(
                urls = listOf(official.url("/$index").toString(), mirror.url("/$index").toString()),
                targetFile = File(dir, "f-$index.bin"),
                sha1 = sha1Of(payloadFor(index)),
                expectedSize = payloadFor(index).size.toLong()
            )
        }

        val samples = ConcurrentLinkedQueue<Sample>()
        val byteFlow = ConcurrentLinkedQueue<Pair<Long, Long>>() // t -> downloadedBytes
        val finishEvents = ConcurrentLinkedQueue<Pair<Long, Int>>() // t -> downloadedFiles after mark

        val start = System.nanoTime()
        fun now(): Long = (System.nanoTime() - start) / 1_000_000

        val poller = Thread {
            try {
                var lastBytes = -1L
                while (true) {
                    val b = byteCounterRef.get()?.invoke() ?: -1L
                    if (b >= 0 && b != lastBytes) {
                        byteFlow.add(now() to b)
                        lastBytes = b
                    }
                    Thread.sleep(5)
                }
            } catch (_: InterruptedException) {
            }
        }.apply { isDaemon = true }

        var statsRef: DownloadStats? = null
        try {
            withTimeout(300_000) {
                val batch = BatchDownloader(
                    requests = requests,
                    maxConnections = 64,
                    retryRounds = 1,
                    clientOverride = client
                )
                batch.onUpdate = { snapshot ->
                    samples.add(Sample(now(), snapshot.downloadedFiles, snapshot.downloadedBytes, snapshot.speedBytesPerSec))
                }
                batch.onFileSuccess = { finishEvents.add(now() to 0) }
                statsRef = batch.stats
                byteCounterRef.set(batch.stats.downloadedBytesGetter())
                poller.start()
                //Logger 的静态初始化在 JVM 单测中会失败（AllSettings 依赖 Android），
                //完成数守恒告警触发时 batch.run() 会带着 ExceptionInInitializerError 冒出，这里吞掉以便继续分析
                try {
                    batch.run()
                } catch (t: Throwable) {
                    println("[RUN-ERROR] ${t::class.simpleName}: ${t.message}")
                }
                val failedCount = batch.lastRunFailures.size
                println("[OUTCOME] success=${finishEvents.size} failures=$failedCount lost=${fileCount - finishEvents.size - failedCount}")
                batch.lastRunFailures.entries.take(5).forEach { (path, err) ->
                    println("[FAIL] $path -> ${err::class.simpleName}: ${err.message?.lineSequence()?.first()}")
                }
            }
        } finally {
            poller.interrupt()
        }

        analyze(samples.toList(), byteFlow.toList(), finishEvents.toList(), official, mirror, fileCount)

        //熔断有效性：修复前官方源几乎吃下全部请求（2000:20），
        //修复后超过 readTimeout 的停摆应把官方主机熔断，后续文件由镜像承接
        val mirrorHits = (mirror.dispatcher as FastMirrorSource).hits.get()
        println("[BREAKER] mirrorHits=$mirrorHits of $fileCount")
        check(mirrorHits >= fileCount / 10) {
            "主机级熔断未生效：镜像源仅承接 $mirrorHits 次请求"
        }

        official.close(); mirror.close(); dir.deleteRecursively()
    }

    private val byteCounterRef = java.util.concurrent.atomic.AtomicReference<(() -> Long)?>(null)

    private fun DownloadStats.downloadedBytesGetter(): () -> Long = { this.downloadedBytes }

    private fun analyze(
        samples: List<Sample>,
        byteFlow: List<Pair<Long, Long>>,
        finishEvents: List<Pair<Long, Int>>,
        official: MockWebServer,
        mirror: MockWebServer,
        fileCount: Int
    ) {
        println("[SUMMARY] files=$fileCount officialHits=${(official.dispatcher as SlowOfficialSource).hits.get()} "
                + "mirrorHits=${(mirror.dispatcher as FastMirrorSource).hits.get()} samples=${samples.size}")
        if (samples.isEmpty()) return

        //特征窗口：speed == 0 持续 >= 1.5s，且窗口内 downloadedFiles 至少 +3
        val windows = mutableListOf<Triple<Sample, Sample, Int>>()
        var i = 0
        while (i < samples.size) {
            if (samples[i].speed != 0L) { i++; continue }
            var j = i
            while (j < samples.size && samples[j].speed == 0L) j++
            val from = samples[i]; val to = samples[j - 1]
            val duration = to.t - from.t
            val filesGain = finishEvents.count { (t, _) -> t in from.t..to.t }
            //文件增益改用快照差
            val filesGainSnapshot = run {
                val after = samples.drop(j).firstOrNull() ?: to
                after.files - from.files
            }
            if (duration >= 1500 && filesGainSnapshot >= 3) {
                windows.add(Triple(from, to, filesGainSnapshot))
            }
            i = j
        }

        println("[SIGNATURE] zeroSpeedWindows>=1.5s=${windows.size}")
        windows.sortedByDescending { it.second.t - it.first.t }.take(6).forEach { (from, to, gain) ->
            println("  window ${from.t}ms..${to.t}ms dur=${to.t - from.t}ms files=${from.files}->${to.files} (+$gain) bytes=${from.bytes}->${to.bytes}")
            //窗口附近的字节流与完成事件时间线
            byteFlow.filter { it.first in (from.t - 3000)..(to.t + 3000) }
                .takeIf { it.isNotEmpty() }?.let { flow ->
                    println("    bytes-flow around: first=${flow.first()} last=${flow.last()} events=${flow.size}")
                }
        }

        //总体统计：0速样本占比、最大字节间隙
        val zeroSpeedSamples = samples.count { it.speed == 0L && it.t > 3000 }
        println("[ZERO-SPEED] ${zeroSpeedSamples}/${samples.size} samples show 0 B/s (after 3s warmup)")
        val gaps = byteFlow.zipWithNext().map { (a, b) -> b.first - a.first to b.second - a.second }
        val bigGaps = gaps.filter { it.first >= 1500 }
        println("[BYTE-GAPS] gaps>=1.5s: ${bigGaps.size}, maxGap=${gaps.maxOfOrNull { it.first }}ms")
        bigGaps.take(8).forEach { println("    gap ${it.first}ms (+${it.second}B)") }

        val final = samples.last()
        println("[FINAL] files=${final.files} bytes=${final.bytes} speed=${final.speed}")
    }

    companion object {
        fun payloadFor(index: Int): ByteArray {
            val size = 800 + (index % 211) * 97
            val array = ByteArray(size)
            Random(index.toLong()).nextBytes(array)
            return array
        }

        fun sha1Of(bytes: ByteArray): String =
            MessageDigest.getInstance("SHA-1").digest(bytes).joinToString("") { "%02x".format(it) }
    }
}
