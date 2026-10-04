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

import com.movtery.zalithlauncher.path.URL_USER_AGENT
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
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
import java.io.ByteArrayOutputStream
import java.io.File
import java.nio.file.Files
import java.security.MessageDigest
import java.util.Random
import java.util.concurrent.TimeUnit
import java.util.zip.GZIPOutputStream

/** 按请求序号回放的假源，并记录全部请求供断言使用 */
private class ScriptedSource(
    private val handler: (index: Int, request: RecordedRequest) -> MockResponse
) : Dispatcher() {
    val requests = java.util.Collections.synchronizedList(mutableListOf<RecordedRequest>())

    override fun dispatch(request: RecordedRequest): MockResponse {
        val index = synchronized(requests) {
            requests.add(request)
            requests.size - 1
        }
        return handler(index, request)
    }
}

private fun scriptedResponse(
    code: Int,
    body: ByteArray,
    vararg headers: Pair<String, String>
): MockResponse {
    val builder = MockResponse.Builder().code(code)
    headers.forEach { (name, value) -> builder.addHeader(name, value) }
    builder.body(Buffer().write(body))
    return builder.build()
}

class FetcherHttpTest {

    private val servers = mutableListOf<MockWebServer>()
    private var workDir: File? = null

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

    private fun newWorkDir(): File = Files.createTempDirectory("fetcher-test").toFile().also { workDir = it }

    private fun sha1(bytes: ByteArray): String =
        MessageDigest.getInstance("SHA-1").digest(bytes).joinToString(separator = "") { "%02x".format(it) }

    private val payload = ByteArray(1024).also { Random(7).nextBytes(it) }

    @Test
    fun `downloads a file and verifies sha1`() = runBlocking<Unit> {
        val source = ScriptedSource { _, _ -> scriptedResponse(200, payload) }
        val server = startServer(source)
        val target = File(newWorkDir(), "out.bin")

        Fetcher.downloadFile(listOf(server.url("/file").toString()), target, sha1(payload))

        assertArrayEquals(payload, target.readBytes())
        assertEquals(1, source.requests.size)
    }

    @Test
    fun `retries on sha1 mismatch and cleans temp files`() = runBlocking<Unit> {
        val source = ScriptedSource { _, _ -> scriptedResponse(200, payload) }
        val server = startServer(source)
        val dir = newWorkDir()
        val target = File(dir, "out.bin")

        val failure = runCatching {
            Fetcher.downloadFile(listOf(server.url("/file").toString()), target, "deadbeef", retry = 2)
        }.exceptionOrNull()

        assertTrue(failure is AllSourcesFailedException)
        val candidate = failure!!.cause
        assertTrue(candidate is DownloadException)
        assertTrue(candidate!!.cause is ChecksumMismatchException)
        //重试了 retry 次，临时文件全部清理
        assertEquals(2, source.requests.size)
        assertTrue(dir.listFiles().isEmpty())
        assertFalse(target.exists())
    }

    @Test
    fun `rotates to next candidate immediately on 404`() = runBlocking<Unit> {
        val missing = ScriptedSource { _, _ -> scriptedResponse(404, ByteArray(0)) }
        val healthy = ScriptedSource { _, _ -> scriptedResponse(200, payload) }
        val server1 = startServer(missing)
        val server2 = startServer(healthy)
        val target = File(newWorkDir(), "out.bin")

        Fetcher.downloadFile(listOf(server1.url("/f").toString(), server2.url("/f").toString()), target)

        //404 不消耗重试次数，该源仅请求一次
        assertEquals(1, missing.requests.size)
        assertArrayEquals(payload, target.readBytes())
    }

    @Test
    fun `retries on 5xx within the same source before rotating`() = runBlocking<Unit> {
        val broken = ScriptedSource { _, _ -> scriptedResponse(500, ByteArray(0)) }
        val healthy = ScriptedSource { _, _ -> scriptedResponse(200, payload) }
        val server1 = startServer(broken)
        val server2 = startServer(healthy)
        val target = File(newWorkDir(), "out.bin")

        Fetcher.downloadFile(listOf(server1.url("/f").toString(), server2.url("/f").toString()), target, retry = 2)

        assertEquals(2, broken.requests.size)
        assertArrayEquals(payload, target.readBytes())
    }

    @Test
    fun `follows relative and absolute redirects`() = runBlocking<Unit> {
        val payloadHere = payload
        val server2 = startServer(ScriptedSource { _, request ->
            if (request.target == "/c") scriptedResponse(200, payloadHere) else scriptedResponse(404, ByteArray(0))
        })
        val server1 = startServer(ScriptedSource { _, request ->
            when (request.target) {
                "/a" -> scriptedResponse(302, ByteArray(0), "Location" to "/b")
                "/b" -> scriptedResponse(302, ByteArray(0), "Location" to server2.url("/c").toString())
                else -> scriptedResponse(404, ByteArray(0))
            }
        })
        val target = File(newWorkDir(), "out.bin")

        Fetcher.downloadFile(listOf(server1.url("/a").toString()), target)

        assertArrayEquals(payload, target.readBytes())
    }

    @Test
    fun `fails after too many redirects`() = runBlocking<Unit> {
        val source = ScriptedSource { index, _ ->
            scriptedResponse(302, ByteArray(0), "Location" to "/r${index + 1}")
        }
        val server = startServer(source)
        val target = File(newWorkDir(), "out.bin")

        val failure = runCatching {
            Fetcher.downloadFile(listOf(server.url("/r0").toString()), target, retry = 1)
        }.exceptionOrNull()

        assertTrue(failure is AllSourcesFailedException)
        assertTrue(source.requests.size >= 21)
        assertFalse(target.exists())
    }

    @Test
    fun `decodes gzip response body`() = runBlocking<Unit> {
        val gzipped = ByteArrayOutputStream().also { output ->
            java.util.zip.GZIPOutputStream(output).use { it.write(payload) }
        }.toByteArray()
        val source = ScriptedSource { _, _ ->
            scriptedResponse(200, gzipped, "Content-Encoding" to "gzip")
        }
        val server = startServer(source)
        val target = File(newWorkDir(), "out.bin")

        Fetcher.downloadFile(listOf(server.url("/file").toString()), target)

        assertArrayEquals(payload, target.readBytes())
    }

    @Test
    fun `sends user agent and accept encoding headers`() = runBlocking<Unit> {
        var recorded: RecordedRequest? = null
        val source = ScriptedSource { _, request ->
            recorded = request
            scriptedResponse(200, payload)
        }
        val server = startServer(source)
        val target = File(newWorkDir(), "out.bin")

        Fetcher.downloadFile(listOf(server.url("/file").toString()), target)

        assertEquals(URL_USER_AGENT, recorded!!.headers["User-Agent"])
        assertEquals("gzip", recorded!!.headers["accept-encoding"])
    }

    @Test
    fun `aggregates per-source failures when all candidates fail`() = runBlocking<Unit> {
        val source1 = ScriptedSource { _, _ -> scriptedResponse(404, ByteArray(0)) }
        val source2 = ScriptedSource { _, _ -> scriptedResponse(503, ByteArray(0)) }
        val server1 = startServer(source1)
        val server2 = startServer(source2)
        val target = File(newWorkDir(), "out.bin")
        val url1 = server1.url("/f").toString()
        val url2 = server2.url("/f").toString()

        val failure = runCatching {
            Fetcher.downloadFile(listOf(url1, url2), target, retry = 1)
        }.exceptionOrNull()

        assertTrue(failure is AllSourcesFailedException)
        assertTrue(failure!!.message!!.contains(url1))
        assertTrue(failure.message!!.contains(url2))
        //因果链上是最后一个源的失败；404 源的失败记录在 suppressed 里
        assertEquals(503, failure.findHttpCode())
        val suppressed = failure.cause!!.suppressed.single()
        assertEquals(404, (suppressed.cause as HttpResultException).code)
    }

    @Test
    fun `cancellation cleans temp files`() = runBlocking<Unit> {
        val source = ScriptedSource { _, _ ->
            MockResponse.Builder()
                .code(200)
                .body(Buffer().write(ByteArray(512 * 1024)))
                .throttleBody(1024, 1, TimeUnit.SECONDS)
                .build()
        }
        val server = startServer(source)
        val dir = newWorkDir()
        val target = File(dir, "out.bin")

        val job = launch(Dispatchers.IO) {
            Fetcher.downloadFile(listOf(server.url("/file").toString()), target)
        }
        //等临时文件开始增长再取消
        val startedAt = System.currentTimeMillis()
        while ((dir.listFiles().orEmpty()).none { it.length() > 0 }) {
            if (System.currentTimeMillis() - startedAt > 15_000) break
            delay(50)
        }
        job.cancelAndJoin()

        assertTrue(dir.listFiles().isEmpty())
        assertFalse(target.exists())
    }
}
