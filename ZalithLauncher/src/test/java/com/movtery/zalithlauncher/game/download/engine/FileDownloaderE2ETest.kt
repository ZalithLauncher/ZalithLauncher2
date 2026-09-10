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
import okio.Buffer
import org.junit.After
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import okhttp3.OkHttpClient
import java.io.File
import java.nio.file.Files
import java.security.MessageDigest
import java.util.Random
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

/** 支持 Range 的可编程假源：可强制忽略 Range、制造失败、限速 */
private class FakeSource(
    private val content: ByteArray,
    private val ignoreRange: Boolean = false,
    private val failFirstRequests: Int = 0,
    private val throttleBytesPerSecond: Long = 0L,
    val rangedRequests: AtomicInteger = AtomicInteger(0)
) : Dispatcher() {

    private val served = AtomicInteger(0)

    override fun dispatch(request: RecordedRequest): MockResponse {
        if (served.incrementAndGet() <= failFirstRequests) {
            return MockResponse.Builder().code(500).build()
        }

        val rangeHeader = request.headers["Range"]
        if (ignoreRange || rangeHeader == null) {
            return baseBuilder(content.size.toLong())
                .body(Buffer().write(content))
                .build()
        }

        rangedRequests.incrementAndGet()
        val start = RANGE_PATTERN.find(rangeHeader)!!.groupValues[1].toLong().toInt()
        val buffer = Buffer().write(content, start, content.size - start)
        return baseBuilder((content.size - start).toLong())
            .code(206)
            .addHeader("Content-Range", "bytes $start-${content.size - 1}/${content.size}")
            .body(buffer)
            .build()
    }

    private fun baseBuilder(length: Long): MockResponse.Builder {
        val builder = MockResponse.Builder()
            .addHeader("Content-Length", length.toString())
        if (throttleBytesPerSecond > 0) {
            builder.throttleBody(throttleBytesPerSecond, 1, TimeUnit.SECONDS)
        }
        return builder
    }

    companion object {
        private val RANGE_PATTERN = Regex("""bytes=(\d+)-""")
    }
}

/**
 * 分区守卫源：prefixOnly=true 时仅服务起点位于文件前半段的请求，
 * 起点越过中位的一律 500；prefixOnly=false 时拒绝起点为 0 的请求，
 * 其余区间正常服务。两者配合可强制一次下载必然发生跨源断点拼接。
 * 未携带 Range 的请求按起点 0 处理。
 */
private class GuardedHalfSource(
    private val content: ByteArray,
    private val prefixOnly: Boolean
) : Dispatcher() {
    val hits = AtomicInteger(0)
    val servedRanges = AtomicInteger(0)

    override fun dispatch(request: RecordedRequest): MockResponse {
        hits.incrementAndGet()
        val range = request.headers["Range"]
        val start = range?.let { RANGE.find(it)!!.groupValues[1].toLong().toInt() } ?: 0
        val servesPrefixHalf = start < content.size / 2

        if (prefixOnly != servesPrefixHalf) {
            return MockResponse.Builder().code(500).build()
        }

        //前半段源只出借到中位为止，迫使尾部区间必须由另一源承接
        val end = if (prefixOnly) content.size / 2 else content.size
        servedRanges.incrementAndGet()
        return MockResponse.Builder()
            .addHeader("Content-Length", (end - start).toString())
            .code(206)
            .addHeader("Content-Range", "bytes $start-${end - 1}/${content.size}")
            .body(Buffer().write(content, start, end - start))
            .build()
    }

    companion object {
        private val RANGE = Regex("""bytes=(\d+)-""")
    }
}

/** 模拟拒绝任何 Range 请求的 CDN 鉴权层：携带 Range 一律 404，普通 GET 正常服务 */
private class RangeRejectingSource(private val content: ByteArray) : Dispatcher() {
    override fun dispatch(request: RecordedRequest): MockResponse {
        if (request.headers["Range"] != null) {
            return MockResponse.Builder().code(404).build()
        }
        return MockResponse.Builder()
            .addHeader("Content-Length", content.size.toString())
            .body(Buffer().write(content))
            .build()
    }
}

class FileDownloaderE2ETest {

    private lateinit var workDir: File
    private val servers = mutableListOf<MockWebServer>()

    private val payload = ByteArray(2_600_000).also { Random(42).nextBytes(it) }

    @After
    fun tearDown() {
        servers.forEach { runCatching { it.close() } }
        workDir?.takeIf { it.exists() }?.deleteRecursively()
    }

    private fun newWorkDir(): File = Files.createTempDirectory("engine-e2e").toFile().also { workDir = it }

    private fun startServer(dispatcher: Dispatcher): MockWebServer =
        MockWebServer().also { server ->
            server.dispatcher = dispatcher
            server.start()
            servers += server
        }

    private fun engineRequest(targetName: String, vararg urls: String, sha1: String? = null): Pair<DownloadRequest, File> {
        val target = File(newWorkDir(), targetName)
        return DownloadRequest(urls.toList(), target, sha1, payload.size.toLong()) to target
    }

    private fun sha1HexOf(bytes: ByteArray): String =
        MessageDigest.getInstance("SHA-1").digest(bytes).joinToString("") { "%02x".format(it) }

    @Test
    fun `large file downloads through multiple ranged connections`() = runBlocking {
        val source = FakeSource(payload, throttleBytesPerSecond = 320_000L)
        val server = startServer(source)
        val (request, target) = engineRequest("chunked.bin", server.url("/file.bin").toString(), sha1 = sha1HexOf(payload))

        withTimeout(TEST_TIMEOUT_MS) {
            FileDownloader(
                request = request,
                connections = Semaphore(16),
                stats = DownloadStats(),
                allowExtraConnection = { true },
                maxWorkersPerFile = 6,
                client = OkHttpClient()
            ).download()
        }

        assertArrayEquals(payload, target.readBytes())
        assertTrue("expected multiple ranged requests", source.rangedRequests.get() > 0)
        assertFalse(File(target.parentFile, "${target.name}.part").exists())
    }

    @Test
    fun `falls back to whole-file streaming when server ignores Range`() = runBlocking {
        val source = FakeSource(payload, ignoreRange = true)
        val server = startServer(source)
        val (request, target) = engineRequest("norange.bin", server.url("/file.bin").toString())

        withTimeout(TEST_TIMEOUT_MS) {
            FileDownloader(
                request = request,
                connections = Semaphore(8),
                stats = DownloadStats(),
                allowExtraConnection = { false },
                client = OkHttpClient()
            ).download()
        }

        assertArrayEquals(payload, target.readBytes())
        assertEquals(0, source.rangedRequests.get())
    }

    @Test
    fun `switches to fallback source when primary keeps failing`() = runBlocking {
        val broken = FakeSource(payload, failFirstRequests = Int.MAX_VALUE)
        val healthy = FakeSource(payload)
        val brokenServer = startServer(broken)
        val healthyServer = startServer(healthy)
        val (request, target) = engineRequest(
            "failover.bin",
            brokenServer.url("/file.bin").toString(),
            healthyServer.url("/file.bin").toString()
        )

        withTimeout(TEST_TIMEOUT_MS) {
            FileDownloader(
                request = request,
                connections = Semaphore(8),
                stats = DownloadStats(),
                allowExtraConnection = { true },
                client = OkHttpClient()
            ).download()
        }

        assertArrayEquals(payload, target.readBytes())
    }

    /** 起点为零的段不得携带 Range：部分 CDN 会直接拒绝任何带 Range 的请求 */
    @Test
    fun `fresh download must not carry a range header`() = runBlocking {
        val source = RangeRejectingSource(payload)
        val server = startServer(source)
        val (request, target) = engineRequest("no-range.bin", server.url("/file.bin").toString(), sha1 = sha1HexOf(payload))

        withTimeout(TEST_TIMEOUT_MS) {
            FileDownloader(
                request = request,
                connections = Semaphore(8),
                stats = DownloadStats(),
                allowExtraConnection = { false },
                client = OkHttpClient()
            ).download()
        }

        assertArrayEquals(payload, target.readBytes())
    }

    @Test
    fun `unknown size streams till eof`() = runBlocking {
        val server = startServer(FakeSource(payload))
        val (_, target) = engineRequest("unknown.bin", server.url("/file.bin").toString())
        val unknownSizeRequest = DownloadRequest(
            urls = listOf(server.url("/file.bin").toString()),
            targetFile = target
        )

        withTimeout(TEST_TIMEOUT_MS) {
            DownloadEngine.download(request = unknownSizeRequest, client = OkHttpClient())
        }

        assertArrayEquals(payload, target.readBytes())
    }

    /** 特调替换库后清单可能残留旧版 size：引擎应以远端声明为准下载完整文件 */
    @Test
    fun `stale smaller declared size heals from range probe`() = runBlocking {
        val server = startServer(FakeSource(payload))
        val (_, target) = engineRequest("stale-size.bin", server.url("/file.bin").toString(), sha1 = sha1HexOf(payload))
        val staleRequest = DownloadRequest(
            urls = listOf(server.url("/file.bin").toString()),
            targetFile = target,
            sha1 = sha1HexOf(payload),
            expectedSize = (payload.size - 10_080L)
        )

        withTimeout(TEST_TIMEOUT_MS) {
            FileDownloader(
                request = staleRequest,
                connections = Semaphore(4),
                stats = DownloadStats(),
                allowExtraConnection = { false },
                client = OkHttpClient()
            ).download()
        }

        assertArrayEquals(payload, target.readBytes())
    }

    /** 分区双源：头部段来自 A、断点后半承接自 B，拼接结果必须完整一致 */
    @Test
    fun `assembles spliced content across guarded half sources`() = runBlocking {
        val guardA = GuardedHalfSource(payload, prefixOnly = true)
        val sourceB = GuardedHalfSource(payload, prefixOnly = false)
        val serverA = startServer(guardA)
        val serverB = startServer(sourceB)

        val target = File(newWorkDir(), "spliced.bin")
        val request = DownloadRequest(
            urls = listOf(serverA.url("/f").toString(), serverB.url("/f").toString()),
            targetFile = target,
            sha1 = sha1HexOf(payload),
            expectedSize = payload.size.toLong()
        )

        withTimeout(TEST_TIMEOUT_MS) {
            FileDownloader(
                request = request,
                connections = Semaphore(4),
                stats = DownloadStats(),
                allowExtraConnection = { false },
                client = OkHttpClient()
            ).download()
        }

        assertArrayEquals(payload, target.readBytes())
        assertTrue("server A should have served the prefix", guardA.servedRanges.get() > 0)
        assertTrue("server B should have served beyond midpoint", sourceB.servedRanges.get() > 0)
    }

    companion object {
        private const val TEST_TIMEOUT_MS = 60_000L
    }
}
