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
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.withTimeout
import mockwebserver3.Dispatcher
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import mockwebserver3.RecordedRequest
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okio.Buffer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.util.Random
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

/**
 * 停摆场景回归：源"建连成功但正文长时间无字节"是生产中最常见的拥塞形态，
 * 引擎必须能识别超时、熔断换源，并在全部源不可用时快速失败而不是无限重试
 * （修复前 HTTP/1.1 的 okio 读超时会被误判为取消，停摆的源被无限重试，整批长时间 0B/s）。
 */

/** 正文统一延迟 [stallMs] 毫秒的假源；startHealthyAfterMs >= 0 时，超过该时间后转为正常服务 */
private class StallingSource(
    private val stallMs: Long,
    private val startHealthyAfterMs: Long = -1L
) : Dispatcher() {
    private val started = System.currentTimeMillis()

    override fun dispatch(request: RecordedRequest): MockResponse {
        val index = request.target.trim('/').toIntOrNull() ?: 0
        val content = payloadFor(index)
        val builder = MockResponse.Builder()
            .addHeader("Content-Length", content.size.toString())
            .body(Buffer().write(content))
        if (startHealthyAfterMs < 0 || System.currentTimeMillis() - started < startHealthyAfterMs) {
            builder.bodyDelay(stallMs, TimeUnit.MILLISECONDS)
        }
        return builder.build()
    }
}

/** 快速正常服务的小文件假源 */
private class FastSource : Dispatcher() {
    val hits = AtomicInteger(0)

    override fun dispatch(request: RecordedRequest): MockResponse {
        hits.incrementAndGet()
        val index = request.target.trim('/').toIntOrNull() ?: 0
        val content = payloadFor(index)
        return MockResponse.Builder()
            .addHeader("Content-Length", content.size.toString())
            .body(Buffer().write(content))
            .build()
    }
}

private fun payloadFor(index: Int): ByteArray {
    val size = 20_000 + (index % 31) * 907
    val array = ByteArray(size)
    Random(index.toLong()).nextBytes(array)
    return array
}

class StallRecoveryTest {

    private lateinit var workDir: File
    private val servers = mutableListOf<MockWebServer>()

    @After
    fun tearDown() {
        servers.forEach { runCatching { it.close() } }
        workDir?.takeIf { it.exists() }?.deleteRecursively()
    }

    private fun newWorkDir(): File = Files.createTempDirectory("stall-recovery").toFile().also { workDir = it }

    private fun startServer(dispatcher: Dispatcher): MockWebServer =
        MockWebServer().also { server ->
            server.dispatcher = dispatcher
            server.start()
            servers += server
        }

    /** 读超时 1s、正文停摆 1.5s 的固定台架：一定在正文阶段触发读超时 */
    private fun stallingClient(protocols: List<Protocol>): OkHttpClient =
        OkHttpClient.Builder()
            .protocols(protocols)
            .readTimeout(1000, TimeUnit.MILLISECONDS)
            .build()

    /**
     * HTTP/1.1 单源永久停摆：超时记到源上触发熔断与软禁用，
     * 单文件应在数秒内有界失败，而不是无限重试停在 0B/s。
     */
    @Test
    fun `http11 stall fails cleanly instead of retrying forever`() = runBlocking {
        val server = startServer(StallingSource(stallMs = 1500))
        val target = File(newWorkDir(), "one.bin")
        val request = DownloadRequest(
            urls = listOf(server.url("/one").toString()),
            targetFile = target,
            expectedSize = payloadFor(1).size.toLong()
        )
        val health = HostHealth(tripThreshold = 1, baseCooldownNanos = 400_000_000L)

        try {
            withTimeout(30_000) {
                FileDownloader(
                    request = request,
                    connections = Semaphore(4),
                    stats = DownloadStats(),
                    allowExtraConnection = { false },
                    maxAllSourcesWaits = 1,
                    client = stallingClient(listOf(Protocol.HTTP_1_1)),
                    sourceHealth = health
                ).download()
            }
            fail("expected the download to fail with bounded attempts")
        } catch (_: IOException) {
            //预期：全部源不可用时快速失败
        }
        assertTrue(!target.exists())
    }

    /**
     * h2 全部源熔断后的冷却等待回归：acquire 不再派发注定空转的工人，
     * 引擎原地等待冷却到期后真实尝试，并在源持续停摆时有界失败。
     */
    @Test
    fun `h2 all sources tripped fails cleanly instead of spinning`() = runBlocking {
        val server = startServer(StallingSource(stallMs = 1500))
        val target = File(newWorkDir(), "two.bin")
        val request = DownloadRequest(
            urls = listOf(server.url("/two").toString()),
            targetFile = target,
            expectedSize = payloadFor(2).size.toLong()
        )
        val health = HostHealth(tripThreshold = 1, baseCooldownNanos = 400_000_000L)

        try {
            withTimeout(30_000) {
                FileDownloader(
                    request = request,
                    connections = Semaphore(4),
                    stats = DownloadStats(),
                    allowExtraConnection = { false },
                    maxAllSourcesWaits = 1,
                    client = stallingClient(listOf(Protocol.HTTP_2, Protocol.HTTP_1_1)),
                    sourceHealth = health
                ).download()
            }
            fail("expected the download to fail with bounded attempts")
        } catch (_: IOException) {
        }
        assertTrue(!target.exists())
    }

    /**
     * 整批集成：两个源同时停摆触发主机级熔断，引擎原地等待，
     * 源恢复后批次应自动继续并全部完成——"卡 0B/s，过一会自动恢复"的正确形态。
     */
    @Test
    fun `batch completes after both hosts recover from outage`() = runBlocking {
        val fileCount = 12
        val official = startServer(StallingSource(stallMs = 1500, startHealthyAfterMs = 1500))
        val mirror = startServer(StallingSource(stallMs = 1500, startHealthyAfterMs = 1500))
        newWorkDir()

        val requests = (0 until fileCount).map { index ->
            DownloadRequest(
                urls = listOf(official.url("/$index").toString(), mirror.url("/$index").toString()),
                targetFile = File(workDir, "f-$index.bin"),
                expectedSize = payloadFor(index).size.toLong()
            )
        }

        val successPaths = ConcurrentHashMap.newKeySet<String>()
        withTimeout(45_000) {
            val batch = BatchDownloader(
                requests = requests,
                maxConnections = 12,
                retryRounds = 1,
                clientOverride = stallingClient(listOf(Protocol.HTTP_2, Protocol.HTTP_1_1))
            )
            batch.onFileSuccess = { request -> successPaths.add(request.targetFile.name) }
            batch.run()
        }

        assertEquals(fileCount, successPaths.size)
        requests.forEachIndexed { index, request ->
            assertTrue("${request.targetFile.name} should exist", request.targetFile.exists())
            assertEquals(payloadFor(index).size.toLong(), request.targetFile.length())
        }
    }
}
