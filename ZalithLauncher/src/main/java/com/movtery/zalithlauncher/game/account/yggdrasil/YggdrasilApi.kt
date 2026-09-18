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

package com.movtery.zalithlauncher.game.account.yggdrasil

import com.movtery.zalithlauncher.game.account.microsoft.MinecraftProfileException
import com.movtery.zalithlauncher.game.account.microsoft.MinecraftProfileException.ExceptionStatus.FREQUENT
import com.movtery.zalithlauncher.game.account.microsoft.MinecraftProfileException.ExceptionStatus.PROFILE_NOT_EXISTS
import com.movtery.zalithlauncher.game.account.wardrobe.SkinModelType
import com.movtery.zalithlauncher.game.download.engine.BatchDownloader
import com.movtery.zalithlauncher.game.version.download.DownloadTask
import com.movtery.zalithlauncher.path.GLOBAL_CLIENT
import com.movtery.zalithlauncher.path.PathManager
import com.movtery.zalithlauncher.utils.logging.Logger
import com.movtery.zalithlauncher.utils.network.safeBodyAsJson
import com.movtery.zalithlauncher.utils.network.withRetry
import io.ktor.client.plugins.ResponseException
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.headers
import io.ktor.client.request.request
import io.ktor.client.request.setBody
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import org.apache.commons.io.FileUtils
import java.io.File
import java.util.concurrent.TimeUnit

private const val TAG = "YggdrasilApi"

/**
 * 使用 Yggdrasil 上传皮肤
 */
suspend fun uploadSkin(
    apiUrl: String,
    accessToken: String,
    file: File,
    modelType: SkinModelType,
    maxRetries: Int = 1
) {
    val skinData = file.readBytes()
    val logTag = "YggdrasilApi.uploadSkin"

    Logger.info(TAG, "$logTag: uploading skin -> ${file.name}")
    withRetry(logTag = logTag, maxRetries = maxRetries) {
        GLOBAL_CLIENT.submitFormWithBinaryData(
            url = "$apiUrl/minecraft/profile/skins",
            formData = formData {
                append("variant", modelType.modelType)
                append("file", skinData, Headers.build {
                    append(HttpHeaders.ContentDisposition, "filename=\"${file.name}\"")
                })
            }
        ) {
            method = HttpMethod.Post
            headers {
                append(HttpHeaders.Authorization, "Bearer $accessToken")
            }
        }
    }
}

/**
 * 使用 Yggdrasil 更改玩家披风
 * @param capeId 披风的uuid，为空字符串时则表示重置披风
 */
suspend fun changeCape(
    apiUrl: String,
    accessToken: String,
    capeId: String = "",
    maxRetries: Int = 1
) {
    val url = "$apiUrl/minecraft/profile/capes/active"
    val logTag = "YggdrasilApi.changeCape"

    if (capeId.isBlank()) {
        //重置玩家选择的披风
        Logger.info(TAG, "$logTag: reset cape")
        withRetry(logTag = logTag, maxRetries = maxRetries) {
            GLOBAL_CLIENT.request(url) {
                method = HttpMethod.Delete
                headers {
                    append("Authorization", "Bearer $accessToken")
                    append("Content-Type", "application/json")
                }
            }
        }
    } else {
        Logger.info(TAG, "$logTag: capeId -> $capeId")
        withRetry(logTag = logTag, maxRetries = maxRetries) {
            GLOBAL_CLIENT.request(url) {
                method = HttpMethod.Put
                headers {
                    append("Authorization", "Bearer $accessToken")
                    append("Content-Type", "application/json")
                }
                setBody(JsonObject(mapOf("capeId" to JsonPrimitive(capeId))))
            }
        }
    }
}

/**
 * 使用 Yggdrasil 获取玩家配置信息
 */
suspend fun getPlayerProfile(
    apiUrl: String,
    accessToken: String
) = runCatching {
    GLOBAL_CLIENT.get("$apiUrl/minecraft/profile") {
        header(HttpHeaders.Authorization, "Bearer $accessToken")
    }.safeBodyAsJson<PlayerProfile>()
}.onFailure { e ->
    if (e is ResponseException) {
        when (e.response.status.value) {
            429 -> throw MinecraftProfileException(FREQUENT)
            404 -> throw MinecraftProfileException(PROFILE_NOT_EXISTS)
        }
    }
}.getOrThrow()

/**
 * 缓存玩家的所有披风图片文件
 */
suspend fun cacheAllCapes(
    profile: PlayerProfile,
    maxThreads: Int = 6
) = runCatching {
    withContext(Dispatchers.IO) {
        val tasks = profile.capes.mapNotNull { cape ->
            val file = cape.getFile(PathManager.DIR_ACCOUNT_CAPE)
            if (file.exists()) {
                if (file.lastModified() + TimeUnit.DAYS.toMillis(7) < System.currentTimeMillis()) {
                    //超过一周，更新一次缓存
                    FileUtils.deleteQuietly(file)
                } else {
                    return@mapNotNull null
                }
            }
            DownloadTask(
                urls = listOf(cape.url),
                verifyIntegrity = false,
                targetFile = file
            )
        }

        if (tasks.isNotEmpty()) {
            BatchDownloader(
                requests = tasks.map { it.toRequest() },
                maxConnections = maxThreads,
                retryRounds = 0
            ).run()
        }
    }
}.onFailure { e ->
    if (e is ResponseException) {
        when (e.response.status.value) {
            429 -> throw MinecraftProfileException(FREQUENT)
            404 -> throw MinecraftProfileException(PROFILE_NOT_EXISTS)
        }
    }
}.getOrThrow()

/**
 * 执行需要授权的操作，如果遇到未授权（HTTP 401），会调用刷新授权的回调
 */
suspend fun executeWithAuthorization(
    block: suspend () -> Unit,
    onRefreshRequest: suspend () -> Unit
) {
    var refreshed = false
    while (true) {
        try {
            block()
            break
        } catch (e: ResponseException) {
            if (e.response.status == HttpStatusCode.Unauthorized) {
                if (refreshed) throw e //已经刷新过，还是遇到这个问题就抛出异常
                onRefreshRequest()
                refreshed = true
                continue
            } else throw e
        }
    }
}