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
import org.junit.After
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.nio.file.Files
import java.util.Random
import java.util.concurrent.atomic.AtomicInteger

/** 每个文件一条独立路径的简单静态源 */
private class StaticSource : Dispatcher() {
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

    companion object {
        fun payloadFor(index: Int): ByteArray {
            val size: Int = 64_000 + (index * 13_997)
            val array = ByteArray(size)
            Random(index.toLong()).nextBytes(array)
            return array
        }
    }
}

class BatchDownloaderE2ETest {

    private lateinit var workDir: File
    private val servers = mutableListOf<MockWebServer>()

    @After
    fun tearDown() {
        servers.forEach { runCatching { it.close() } }
        workDir?.takeIf { it.exists() }?.deleteRecursively()
    }

    private fun startServer(): MockWebServer =
        MockWebServer().also { server ->
            server.dispatcher = StaticSource()
            server.start()
            servers += server
        }

    @Test
    fun `mixed small files all complete with accurate counters`() = runBlocking {
        workDir = Files.createTempDirectory("batch-e2e").toFile()
        val primary = startServer()
        val mirror = startServer()

        val requests = (0 until 24).map { index ->
            DownloadRequest(
                urls = listOf(primary.url("/$index").toString(), mirror.url("/$index").toString()),
                targetFile = File(workDir, "file-$index.bin"),
                sha1 = null,
                expectedSize = StaticSource.payloadFor(index).size.toLong(),
                tag = index
            )
        }

        val completedTags = mutableListOf<Any>()
        val observedFailures = mutableListOf<Pair<String, String?>>()

        var batchRef: BatchDownloader? = null
        withTimeout(60_000) {
            val batch = BatchDownloader(
                requests = requests,
                maxConnections = 12,
                retryRounds = 1,
                clientOverride = OkHttpClient()
            )
            batch.onUpdate = { }
            batch.onFileSuccess = { request ->
                synchronized(completedTags) { completedTags.add(request.tag!!) }
            }
            batch.onFailureFilter = { request, error ->
                synchronized(observedFailures) {
                    observedFailures.add(request.targetFile.name to error.message)
                }
                false
            }

            try {
                batch.run()
            } finally {
                batchRef = batch
            }
        }

        val finalProgress = batchRef!!.stats.snapshotProgress()
        assertEquals(observedFailures.toString(), requests.size, completedTags.size)
        assertEquals(finalProgress.downloadedFiles, requests.size)
        assertTrue(observedFailures.isEmpty())

        //落盘内容与源一致，且请求总量覆盖了全部文件
        assertArrayEquals(
            StaticSource.payloadFor(0),
            File(workDir, "file-0.bin").readBytes()
        )
        val totalHits = servers.map { server ->
            (server.dispatcher as StaticSource).hits.get()
        }.sum()
        assert(totalHits >= requests.size)
    }
}
