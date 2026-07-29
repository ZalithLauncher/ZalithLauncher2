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

package com.movtery.zalithlauncher.utils.network

import com.movtery.zalithlauncher.path.GLOBAL_CLIENT
import com.movtery.zalithlauncher.path.GLOBAL_JSON
import com.movtery.zalithlauncher.utils.logging.Logger
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.request.forms.submitForm
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.ContentType
import io.ktor.http.Parameters
import io.ktor.http.contentType
import io.ktor.utils.io.jvm.javaio.toInputStream
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.decodeFromStream
import java.io.IOException
import java.nio.channels.UnresolvedAddressException
import kotlin.coroutines.CoroutineContext
import kotlin.time.Duration.Companion.milliseconds

private const val TAG = "NetWorks"

@OptIn(ExperimentalSerializationApi::class)
suspend inline fun <reified T> HttpResponse.safeBodyAsJson(): T {
    return GLOBAL_JSON.decodeFromStream(bodyAsChannel().toInputStream())
}

suspend fun HttpResponse.safeBodyAsText(): String {
    val channel = bodyAsChannel()
    return channel.toInputStream().bufferedReader().use { reader ->
        val buffer = CharArray(8 * 1024)
        val sb = StringBuilder()
        var read: Int
        while (reader.read(buffer).also { read = it } != -1) {
            sb.appendRange(buffer, 0, read)
        }
        sb.toString()
    }
}

suspend inline fun <reified T> submitForm(
    url: String,
    parameters: Parameters,
    context: CoroutineContext = Dispatchers.IO
): T = withContext(context) {
    GLOBAL_CLIENT.submitForm(
        url = url,
        formParameters = parameters
    ) {
        contentType(ContentType.Application.FormUrlEncoded)
    }.body()
}

suspend inline fun <reified T> httpPostJson(
    url: String,
    headers: List<Pair<String, Any?>>? = null,
    body: Any,
    context: CoroutineContext = Dispatchers.IO
): T = withContext(context) {
    GLOBAL_CLIENT.post(url) {
        contentType(ContentType.Application.Json)
        headers?.takeIf { it.isNotEmpty() }?.forEach { (k, v) -> header(k, v) }
        setBody(body)
    }.safeBodyAsJson()
}

suspend inline fun <reified T> httpGetJson(
    url: String,
    headers: List<Pair<String, Any?>>? = null,
    parameters: Parameters? = null,
    context: CoroutineContext = Dispatchers.IO
): T = withContext(context) {
    GLOBAL_CLIENT.get(url) {
        headers?.takeIf { it.isNotEmpty() }?.forEach { (k, v) -> header(k, v) }
        parameters?.let { value ->
            url {
                this.parameters.appendAll(value)
            }
        }
    }.safeBodyAsJson()
}

suspend fun <T> withRetry(
    logTag: String,
    maxRetries: Int = 3,
    initialDelay: Long = 1000,
    maxDelay: Long = 10_000,
    block: suspend () -> T
): T {
    var currentDelay = initialDelay
    var retryCount = 0
    var lastError: Throwable? = null

    while (retryCount < maxRetries) {
        try {
            return block()
        } catch (e: CancellationException) {
            //协程被取消时不重试，直接抛出
            Logger.debug(TAG, "$logTag: Cancelled: ${e.message}")
            throw e
        } catch (e: Exception) {
            // 部分异常（如 UnresolvedAddressException）message 为 null，需要输出异常类型以便诊断
            Logger.debug(TAG, "$logTag: Attempt ${retryCount + 1} failed: ${e::class.simpleName}: ${e.message}")
            lastError = e
            if (canRetry(e)) {
                delay(currentDelay.milliseconds)
                currentDelay = (currentDelay * 2).coerceAtMost(maxDelay)
                retryCount++
            } else {
                throw e //不可重试
            }
        }
    }
    throw lastError ?: Exception("Failed after $maxRetries retries")
}

private fun canRetry(e: Exception): Boolean {
    return when (e) {
        is ClientRequestException -> e.response.status.value in 500..599 //5xx错误可重试
        is UnresolvedAddressException -> true // DNS解析失败，可能是临时性故障
        is IOException -> true //网络错误
        else -> false
    }
}