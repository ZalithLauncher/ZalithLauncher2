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

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.net.URL

/** Range 续传资格的逐字段校验语义 */
class ResumeContextTest {

    private val url = URL("https://example.test/file.bin")

    private fun response(
        code: Int = 200,
        path: String = "/file.bin",
        headers: Map<String, String> = emptyMap()
    ): ResponseInfo = ResponseInfo(
        code = code,
        url = URL("https://example.test$path"),
        headers = headers.mapValues { listOf(it.value) }
    )

    private fun established(
        headers: Map<String, String> = mapOf(
            "ETag" to "\"a\"", "accept-ranges" to "bytes", "Content-Length" to "10"
        )
    ): ResumeContext = ResumeContext.of(response(headers = headers))!!

    @Test
    fun `of requires 200 with ranges identity length and a validator`() {
        assertNull(ResumeContext.of(response(code = 206)))
        assertNull(ResumeContext.of(response(headers = mapOf("ETag" to "\"a\""))))
        assertNull(
            ResumeContext.of(
                response(
                    headers = mapOf(
                        "ETag" to "\"a\"", "accept-ranges" to "bytes",
                        "Content-Length" to "10", "Content-Encoding" to "gzip"
                    )
                )
            )
        )
        assertNull(
            ResumeContext.of(
                response(
                    headers = mapOf("ETag" to "\"a\"", "accept-ranges" to "bytes", "Content-Length" to "abc")
                )
            )
        )
        //弱 ETag 且无 Last-Modified：没有可用验证器
        assertNull(
            ResumeContext.of(
                response(
                    headers = mapOf("ETag" to "W/\"a\"", "accept-ranges" to "bytes", "Content-Length" to "10")
                )
            )
        )
        assertNull(
            ResumeContext.of(
                response(
                    headers = mapOf("accept-ranges" to "bytes", "Content-Length" to "10")
                )
            )
        )
        //强 ETag + Last-Modified + 全部资格条件：建立成功
        val established = ResumeContext.of(
            response(
                headers = mapOf(
                    "ETag" to "\"a\"", "accept-ranges" to "bytes",
                    "Content-Length" to "10", "Last-Modified" to "Tue, 15 Nov 2099 12:00:00 GMT"
                )
            )
        )
        assertTrue(established != null)
    }

    @Test
    fun `weak etag falls back to last-modified validator`() {
        val context = established(
            headers = mapOf(
                "ETag" to "W/\"a\"", "accept-ranges" to "bytes",
                "Content-Length" to "10", "Last-Modified" to "Tue, 15 Nov 2099 12:00:00 GMT"
            )
        )
        assertEquals("Tue, 15 Nov 2099 12:00:00 GMT", context.ifRange())
    }

    @Test
    fun `canResume validates code encoding length etag and content range`() {
        val context = established()
        context.addBytes(4)

        //非 206 拒绝
        assertFalse(
            context.canResume(200, response(headers = partialHeaders(6)))
        )
        //编码非 identity 拒绝
        assertFalse(
            context.canResume(
                206, response(code = 206, headers = partialHeaders(6) + ("Content-Encoding" to "gzip"))
            )
        )
        //总长度对不上拒绝
        assertFalse(
            context.canResume(206, response(code = 206, headers = partialHeaders(100)))
        )
        //ETag 不一致拒绝
        assertFalse(
            context.canResume(
                206, response(code = 206, headers = partialHeaders(6) + ("ETag" to "\"b\""))
            )
        )
        //Content-Range 起点不等于已收字节拒绝
        assertFalse(
            context.canResume(
                206,
                response(
                    code = 206,
                    headers = mapOf(
                        "ETag" to "\"a\"", "Content-Length" to "6",
                        "Content-Range" to "bytes 5-9/10"
                    )
                )
            )
        )
        //Content-Range 结束值与响应长度不符拒绝
        assertFalse(
            context.canResume(
                206,
                response(
                    code = 206,
                    headers = mapOf(
                        "ETag" to "\"a\"", "Content-Length" to "6",
                        "Content-Range" to "bytes 4-7/10"
                    )
                )
            )
        )
        //完全匹配放行
        assertTrue(
            context.canResume(
                206,
                response(
                    code = 206,
                    headers = mapOf(
                        "ETag" to "\"a\"", "Content-Length" to "6",
                        "Content-Range" to "bytes 4-9/10"
                    )
                )
            )
        )
    }

    @Test
    fun `canResume with last-modified validator requires same url`() {
        val lastModified = "Tue, 15 Nov 2099 12:00:00 GMT"
        val context = established(
            headers = mapOf(
                "Last-Modified" to lastModified, "accept-ranges" to "bytes", "Content-Length" to "10"
            )
        )
        context.addBytes(4)

        //同 URL + 同 Last-Modified：放行
        assertTrue(
            context.canResume(
                206,
                response(
                    code = 206,
                    headers = mapOf(
                        "Last-Modified" to lastModified, "Content-Length" to "6",
                        "Content-Range" to "bytes 4-9/10"
                    )
                )
            )
        )
        //URL 不同：拒绝
        assertFalse(
            context.canResume(
                206,
                response(
                    code = 206,
                    path = "/elsewhere.bin",
                    headers = mapOf(
                        "Last-Modified" to lastModified, "Content-Length" to "6",
                        "Content-Range" to "bytes 4-9/10"
                    )
                )
            )
        )
        //Last-Modified 不同：拒绝
        assertFalse(
            context.canResume(
                206,
                response(
                    code = 206,
                    headers = mapOf(
                        "Last-Modified" to "Wed, 16 Nov 2099 12:00:00 GMT", "Content-Length" to "6",
                        "Content-Range" to "bytes 4-9/10"
                    )
                )
            )
        )
    }

    @Test
    fun `hasPartialContent is exclusive of both empty and complete`() {
        val context = established()
        assertFalse(context.hasPartialContent())
        context.addBytes(4)
        assertTrue(context.hasPartialContent())
        context.addBytes(6)
        assertFalse(context.hasPartialContent())
    }

    private fun partialHeaders(remaining: Long): Map<String, String> = mapOf(
        "ETag" to "\"a\"", "Content-Length" to remaining.toString(),
        "Content-Range" to "bytes 4-${4 + remaining - 1}/10"
    )
}
