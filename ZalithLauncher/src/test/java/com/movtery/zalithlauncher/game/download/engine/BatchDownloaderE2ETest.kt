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
import okio.Buffer
import org.junit.After
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.nio.file.Files
import java.util.Collections
import java.util.Random
import java.util.concurrent.atomic.AtomicInteger

class BatchDownloaderE2ETest {

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

    private fun newWorkDir(): File = Files.createTempDirectory("batch-test").toFile().also { workDir = it }

    private fun sha1(bytes: ByteArray): String =
        java.security.MessageDigest.getInstance("SHA-1")
            .digest(bytes).joinToString(separator = "") { "%02x".format(it) }

    private fun payload(seed: Int): ByteArray = ByteArray(2048).also { Random(seed.toLong()).nextBytes(it) }

    @Test
    fun `runs a batch with bounded file concurrency`() = runBlocking {
        withTimeout(60_000) {
            val contents = mapOf(
                "/a" to payload(1),
                "/b" to payload(2),
                "/c" to payload(3),
                "/d" to payload(4)
            )
            val inFlight = AtomicInteger(0)
            val maxInFlight = AtomicInteger(0)
            val server = startServer(object : Dispatcher() {
                override fun dispatch(request: RecordedRequest): MockResponse {
                    val current = inFlight.incrementAndGet()
                    maxInFlight.updateAndGet { existing -> maxOf(existing, current) }
                    Thread.sleep(150)
                    inFlight.decrementAndGet()
                    return MockResponse.Builder()
                        .body(Buffer().write(contents.getValue(request.target)))
                        .build()
                }
            })
            val dir = newWorkDir()
            val requests = contents.keys.map { path ->
                val file = File(dir, path.trim('/'))
                DownloadRequest(listOf(server.url(path).toString()), file, sha1(contents.getValue(path)))
            }

            val snapshots = Collections.synchronizedList(mutableListOf<BatchProgress>())
            val batch = BatchDownloader(requests, maxConnections = 2, retryRounds = 0)
            batch.onUpdate = { snapshots.add(it) }
            batch.run()

            assertEquals(4, batch.stats.totalFiles)
            assertEquals(4, batch.stats.downloadedFiles)
            assertTrue(snapshots.isNotEmpty())
            assertTrue(maxInFlight.get() in 1..2)
            contents.forEach { (path, bytes) ->
                assertArrayEquals(bytes, File(dir, path.trim('/')).readBytes())
            }
        }
    }

    @Test
    fun `aggregates failures after retry rounds and keeps successful files`() = runBlocking {
        withTimeout(60_000) {
            val good = payload(5)
            val requestsSeen = AtomicInteger(0)
            val server = startServer(object : Dispatcher() {
                override fun dispatch(request: RecordedRequest): MockResponse =
                    if (request.target == "/missing") {
                        requestsSeen.incrementAndGet()
                        MockResponse.Builder().code(404).build()
                    } else {
                        MockResponse.Builder().body(Buffer().write(good)).build()
                    }
            })
            val dir = newWorkDir()
            val missingFile = File(dir, "missing.mod")
            val goodFile = File(dir, "good.mod")
            val batch = BatchDownloader(
                requests = listOf(
                    DownloadRequest(listOf(server.url("/missing").toString()), missingFile),
                    DownloadRequest(listOf(server.url("/good").toString()), goodFile)
                ),
                maxConnections = 2,
                retryRounds = 1
            )

            val failure = runCatching { batch.run() }.exceptionOrNull()

            assertTrue(failure is BatchDownloadException)
            //两轮整批重试，404 每轮只请求一次
            assertEquals(2, requestsSeen.get())
            assertEquals(setOf(missingFile.absolutePath), batch.lastRunFailures.keys)
            assertEquals(404, batch.lastRunFailures.values.first().findHttpCode())
            assertArrayEquals(good, goodFile.readBytes())
            assertFalse(missingFile.exists())
            assertEquals(1, batch.stats.downloadedFiles)
        }
    }

    @Test
    fun `aborts batch early on consecutive systemic failures`() = runBlocking {
        withTimeout(60_000) {
            val requestsSeen = AtomicInteger(0)
            val server = startServer(object : Dispatcher() {
                override fun dispatch(request: RecordedRequest): MockResponse {
                    requestsSeen.incrementAndGet()
                    return MockResponse.Builder().code(404).build()
                }
            })
            val dir = newWorkDir()
            val requests = (0 until 10).map { i ->
                DownloadRequest(listOf(server.url("/f$i").toString()), File(dir, "f$i.bin"))
            }
            val batch = BatchDownloader(requests, maxConnections = 2, retryRounds = 0)

            val failure = runCatching { batch.run() }.exceptionOrNull()

            //10 个文件里只有少数被真正尝试，剩余的随熔断取消，不再空转
            assertTrue(failure is BatchDownloadException)
            assertTrue(failure!!.message!!.contains("aborted"))
            assertTrue(requestsSeen.get() < 10)
            assertTrue(batch.lastRunFailures.isNotEmpty())
            assertTrue(dir.listFiles().isEmpty())
        }
    }

    @Test
    fun `successes keep the systemic breaker from tripping`() = runBlocking {
        withTimeout(60_000) {
            val good = payload(7)
            val server = startServer(object : Dispatcher() {
                override fun dispatch(request: RecordedRequest): MockResponse =
                    if (request.target.startsWith("/missing")) {
                        MockResponse.Builder().code(404).build()
                    } else {
                        MockResponse.Builder().body(Buffer().write(good)).build()
                    }
            })
            val dir = newWorkDir()
            val requests = (0 until 2).map { i ->
                DownloadRequest(listOf(server.url("/missing$i").toString()), File(dir, "missing$i.bin"))
            } + (0 until 6).map { i ->
                DownloadRequest(listOf(server.url("/good$i").toString()), File(dir, "good$i.bin"))
            }
            val batch = BatchDownloader(requests, maxConnections = 4, retryRounds = 0)

            val failure = runCatching { batch.run() }.exceptionOrNull()

            //有文件持续成功：不触发熔断，走常规失败聚合
            assertTrue(failure is BatchDownloadException)
            assertFalse(failure!!.message!!.contains("aborted"))
            assertEquals(6, batch.stats.downloadedFiles)
            repeat(6) { i -> assertArrayEquals(good, File(dir, "good$i.bin").readBytes()) }
        }
    }

    @Test
    fun `onFailureFilter accepts a lost file and lets the batch succeed`() = runBlocking {
        withTimeout(60_000) {
            val good = payload(6)
            val server = startServer(object : Dispatcher() {
                override fun dispatch(request: RecordedRequest): MockResponse =
                    if (request.target == "/missing") {
                        MockResponse.Builder().code(404).build()
                    } else {
                        MockResponse.Builder().body(Buffer().write(good)).build()
                    }
            })
            val dir = newWorkDir()
            val batch = BatchDownloader(
                requests = listOf(
                    DownloadRequest(listOf(server.url("/missing").toString()), File(dir, "missing.mod")),
                    DownloadRequest(listOf(server.url("/good").toString()), File(dir, "good.mod"))
                ),
                maxConnections = 2,
                retryRounds = 0
            )
            batch.onFailureFilter = { _, _ -> true }
            batch.onFileSuccess = { request ->
                assertNotNull(request.targetFile)
            }

            batch.run()

            assertEquals(2, batch.stats.downloadedFiles)
            assertTrue(batch.lastRunFailures.isEmpty())
        }
    }
}
