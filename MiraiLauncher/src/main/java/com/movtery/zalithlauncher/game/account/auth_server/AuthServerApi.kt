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

package com.movtery.zalithlauncher.game.account.auth_server

import android.content.Context
import com.google.gson.Gson
import com.movtery.zalithlauncher.BuildKeys
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.game.account.Account
import com.movtery.zalithlauncher.game.account.auth_server.models.AuthRequest
import com.movtery.zalithlauncher.game.account.auth_server.models.AuthResult
import com.movtery.zalithlauncher.game.account.auth_server.models.Refresh
import com.movtery.zalithlauncher.path.GLOBAL_CLIENT
import com.movtery.zalithlauncher.utils.logging.Logger
import com.movtery.zalithlauncher.utils.network.safeBodyAsJson
import com.movtery.zalithlauncher.utils.network.safeBodyAsText
import com.movtery.zalithlauncher.utils.string.decodeUnicode
import com.movtery.zalithlauncher.utils.string.toUuidStr
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.io.IOException
import java.util.Objects

private const val TAG = "AuthServerApi"

class AuthServerApi(private var baseUrl: String) {
    fun formatUrl(baseUrl: String): String {
        var url = baseUrl
        if (baseUrl.endsWith("/")) {
            url = baseUrl.dropLast(1)
        }
        return url
    }

    init {
        baseUrl = formatUrl(baseUrl)
    }

    @Throws(IOException::class)
    suspend fun authenticate(context: Context, userName: String, password: String): AuthResult {
        requireBaseUrl(context)

        val authRequest = AuthRequest(
            username = userName,
            password = password,
            agent = AuthRequest.Agent(
                name = "Minecraft",
                version = 1
            ),
            requestUser = true,
            clientToken = BuildKeys.LAUNCHER_NAME.toUuidStr().replace("-", "")
        )

        return requestAuth(Gson().toJson(authRequest), "/authserver/authenticate")
    }

    @Throws(IOException::class)
    suspend fun login(
        context: Context,
        userName: String,
        password: String,
        onSuccess: suspend (AuthResult) -> Unit = {},
        onFailed: suspend (th: Throwable) -> Unit = {}
    ) {
        try {
            onSuccess(authenticate(context, userName, password))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            onFailed(e)
        }
    }

    @Throws(IOException::class)
    suspend fun refreshToken(context: Context, account: Account, select: Boolean): AuthResult {
        requireBaseUrl(context)

        val refresh = Refresh(
            clientToken = account.clientToken,
            accessToken = account.accessToken
        )

        if (select) {
            refresh.selectedProfile = Refresh.SelectedProfile(
                name = account.username,
                id = account.profileId
            )
        }

        return requestAuth(Gson().toJson(refresh), "/authserver/refresh")
    }

    @Throws(IOException::class)
    suspend fun refresh(
        context: Context,
        account: Account,
        select: Boolean,
        onSuccess: suspend (AuthResult) -> Unit = {},
        onFailed: suspend (th: Throwable) -> Unit = {}
    ) {
        try {
            onSuccess(refreshToken(context, account, select))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            onFailed(e)
        }
    }

    /**
     * 校验缓存的 accessToken 是否仍被服务端接受
     * @returm false 表示服务端已拒绝该凭据
     */
    @Throws(IOException::class)
    suspend fun validate(context: Context, account: Account): Boolean {
        requireBaseUrl(context)

        return withContext(Dispatchers.IO) {
            try {
                val response = GLOBAL_CLIENT.post("$baseUrl/authserver/validate") {
                    contentType(ContentType.Application.Json)
                    setBody(
                        Gson().toJson(
                            Refresh(
                                clientToken = account.clientToken,
                                accessToken = account.accessToken
                            )
                        )
                    )
                }
                response.status.isSuccess()
            } catch (e: ClientRequestException) {
                if (e.response.status.value == 403) false
                else throw ResponseException(e.response.getErrorMessage(), e.response.status.value)
            }
        }
    }

    private fun requireBaseUrl(context: Context) {
        if (Objects.isNull(baseUrl)) {
            throw ResponseException(context.getString(R.string.account_other_login_baseurl_not_set))
        }
    }

    private suspend fun requestAuth(data: String, url: String): AuthResult = withContext(Dispatchers.IO) {
        try {
            val response = GLOBAL_CLIENT.post(baseUrl + url) {
                contentType(ContentType.Application.Json)
                setBody(data)
            }
            if (response.status.isSuccess()) response.safeBodyAsJson()
            else throw ResponseException(response.getErrorMessage(), response.status.value)
        } catch (e: ClientRequestException) {
            throw ResponseException(e.response.getErrorMessage(), e.response.status.value)
        }
    }

    private suspend fun HttpResponse.getErrorMessage(): String {
        return "(${status.value}) ${parseError(this)}"
    }

    private suspend fun parseError(response: HttpResponse): String {
        return try {
            val json = response.safeBodyAsJson<JsonObject>()
            var message = when {
                "errorMessage" in json -> json["errorMessage"]?.jsonPrimitive?.content ?: "Unknown error"
                "message" in json -> json["message"]?.jsonPrimitive?.content ?: "Unknown error"
                else -> "Unknown error"
            }
            if (message.contains("\\u")) {
                message = decodeUnicode(message.replace("\\\\u", "\\u"))
            }
            message
        } catch (e: Exception) {
            Logger.error(TAG, "Failed to parse error", e)
            "Unknown error"
        }
    }
}

suspend fun getAuthServeInfo(url: String): String? = withContext(Dispatchers.IO) {
    val response = GLOBAL_CLIENT.get(url)
    if (response.status == HttpStatusCode.OK) {
        response.safeBodyAsText()
    } else {
        null
    }
}