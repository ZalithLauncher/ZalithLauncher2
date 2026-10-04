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
import mockwebserver3.SocketEffect
import okio.Buffer
import org.junit.After
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.nio.file.Files
import java.util.Random
import java.util.concurrent.TimeUnit

/**
 * Range 断点续传语义：
 * 首个响应在传输中途断连（节流 + 响应体起始即断开），触发引擎重试；
 * 是否已收到部分字节取决于客户端缓冲，因此断言对"携带 Range 续传"与
 * "从头重下"两条路径都保持成立，续传资格的精确校验由 ResumeContextTest 覆盖。
 */
class FetcherResumeTest {

    private val servers = mutableListOf<MockWebServer>()
    private var workDir: File? = null

    private val content = ByteArray(512 * 1024).also { Random(42).nextBytes(it) }
    private val etag = "\"etag-42\""

    @After
    fun tearDown() {
        servers.forEach { runCatching { it.close() } }
        workDir?.takeIf { it.exists() }?.deleteRecursively()
    }

    private fun startServer(dispatcher: Dispatcher): MockWebServer =
        MockWebServer().also { server ->
            server.dispatcher = dispatcher
            server.start()
            servers.add(server)
        }

    private fun newWorkDir(): File = Files.createTempDirectory("resume-test").toFile().also { workDir = it }

    private fun completeResponse(validatorHeaders: List<Pair<String, String>>, code: Int = 200): MockResponse {
        val builder = MockResponse.Builder()
            .code(code)
            .addHeader("accept-ranges", "bytes")
        if (code == 206) {
            //完整内容的 206 响应：起点 0、终点为末尾
            builder.addHeader("Content-Range", "bytes 0-${content.size - 1}/${content.size}")
        }
        validatorHeaders.forEach { (name, value) -> builder.addHeader(name, value) }
        builder.body(Buffer().write(content))
        return builder.build()
    }

    /** 首个响应：响应体一开始就断连，制造确定性的传输失败 */
    private fun interruptedResponse(validatorHeaders: List<Pair<String, String>>): MockResponse {
        val builder = MockResponse.Builder()
            .code(200)
            .addHeader("accept-ranges", "bytes")
            .onResponseBody(SocketEffect.CloseSocket())
            .throttleBody(8192, 1, TimeUnit.SECONDS)
        validatorHeaders.forEach { (name, value) -> builder.addHeader(name, value) }
        builder.body(Buffer().write(content))
        return builder.build()
    }

    private fun resumedSlice(request: RecordedRequest): MockResponse {
        val start = RANGE_PATTERN.find(request.headers["Range"]!!)!!.groupValues[1].toLong()
        return MockResponse.Builder()
            .code(206)
            .addHeader("accept-ranges", "bytes")
            .addHeader("Content-Range", "bytes $start-${content.size - 1}/${content.size}")
            .body(Buffer().write(content, start.toInt(), content.size - start.toInt()))
            .build()
    }

    private fun handler(
        validatorHeaders: List<Pair<String, String>>,
        onRangeRequest: (request: RecordedRequest) -> MockResponse
    ): Pair<Dispatcher, MutableList<RecordedRequest>> {
        val requests = mutableListOf<RecordedRequest>()
        val dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse {
                requests.add(request)
                val range = request.headers["Range"]
                return when {
                    range != null -> onRangeRequest(request)
                    requests.size == 1 -> interruptedResponse(validatorHeaders)
                    else -> completeResponse(validatorHeaders)
                }
            }
        }
        return dispatcher to requests
    }

    private val RecordedRequest.rangeStart: Long?
        get() = headers["Range"]?.let { RANGE_PATTERN.find(it)?.groupValues?.get(1)?.toLong() }

    @Test
    fun `retries after mid-body disconnect and completes the file`() = runBlocking {
        withTimeout(60_000) {
            val (dispatcher, requests) = handler(listOf("ETag" to etag)) { resumedSlice(it) }
            val server = startServer(dispatcher)
            val target = File(newWorkDir(), "out.bin")

            Fetcher.downloadFile(listOf(server.url("/f").toString()), target)

            assertTrue(requests.size >= 2)
            if (requests[1].headers["Range"] != null) {
                //走了续传路径：If-Range 必须携带强 ETag
                assertEquals(etag, requests[1].headers["If-Range"])
            }
            assertArrayEquals(content, target.readBytes())
        }
    }

    @Test
    fun `416 discards resume context and retries without range`() = runBlocking {
        withTimeout(60_000) {
            val (dispatcher, requests) = handler(listOf("ETag" to etag)) { _ ->
                MockResponse.Builder().code(416).build()
            }
            val server = startServer(dispatcher)
            val target = File(newWorkDir(), "out.bin")

            Fetcher.downloadFile(listOf(server.url("/f").toString()), target)

            assertArrayEquals(content, target.readBytes())
            assertRangeEmittedAtMostOnce(requests)
        }
    }

    @Test
    fun `full 200 answer to a range request restarts from scratch`() = runBlocking {
        withTimeout(60_000) {
            val (dispatcher, requests) = handler(listOf("ETag" to etag)) { _ ->
                completeResponse(listOf("ETag" to etag))
            }
            val server = startServer(dispatcher)
            val target = File(newWorkDir(), "out.bin")

            Fetcher.downloadFile(listOf(server.url("/f").toString()), target)

            assertArrayEquals(content, target.readBytes())
            assertRangeEmittedAtMostOnce(requests)
        }
    }

    @Test
    fun `weak etag never emits range request`() = runBlocking {
        withTimeout(60_000) {
            val (dispatcher, requests) = handler(listOf("ETag" to "W/\"weak-42\"")) { resumedSlice(it) }
            val server = startServer(dispatcher)
            val target = File(newWorkDir(), "out.bin")

            Fetcher.downloadFile(listOf(server.url("/f").toString()), target)

            //弱 ETag 不构成续传资格，任何请求都不应携带 Range
            requests.forEach { assertNull(it.headers["Range"]) }
            assertArrayEquals(content, target.readBytes())
        }
    }

    @Test
    fun `mismatched content range answer restarts from scratch`() = runBlocking {
        withTimeout(60_000) {
            val (dispatcher, requests) = handler(listOf("ETag" to etag)) { request ->
                //Content-Range 起点与续传进度不符：续传必须被拒绝
                val claimedStart = request.rangeStart!! + 1024
                MockResponse.Builder()
                    .code(206)
                    .addHeader("accept-ranges", "bytes")
                    .addHeader("ETag", etag)
                    .addHeader("Content-Range", "bytes $claimedStart-${content.size}/${content.size}")
                    .body(Buffer().write(content))
                    .build()
            }
            val server = startServer(dispatcher)
            val target = File(newWorkDir(), "out.bin")

            Fetcher.downloadFile(listOf(server.url("/f").toString()), target)

            assertArrayEquals(content, target.readBytes())
            assertRangeEmittedAtMostOnce(requests)
        }
    }

    /** 续传请求至多出现一次；被拒绝后不得再次携带 Range */
    private fun assertRangeEmittedAtMostOnce(requests: List<RecordedRequest>) {
        val rangeIndexes = requests.indices.filter { requests[it].headers["Range"] != null }
        assertTrue(rangeIndexes.size <= 1)
        rangeIndexes.firstOrNull()?.let { index ->
            if (index + 1 < requests.size) {
                assertNull(requests[index + 1].headers["Range"])
            }
        }
    }

    companion object {
        private val RANGE_PATTERN = Regex("""bytes=(\d+)-""")
    }
}
