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

package com.movtery.zalithlauncher.game.account.microsoft

import com.movtery.zalithlauncher.BuildKeys
import com.movtery.zalithlauncher.game.account.Account
import com.movtery.zalithlauncher.game.account.AccountType
import com.movtery.zalithlauncher.game.account.AccountsManager
import com.movtery.zalithlauncher.game.account.microsoft.MinecraftProfileException.ExceptionStatus.BLOCKED_IP
import com.movtery.zalithlauncher.game.account.microsoft.MinecraftProfileException.ExceptionStatus.FREQUENT
import com.movtery.zalithlauncher.game.account.microsoft.XboxLoginException.ExceptionStatus.BANNED
import com.movtery.zalithlauncher.game.account.microsoft.XboxLoginException.ExceptionStatus.BLOCKED_REGION
import com.movtery.zalithlauncher.game.account.microsoft.XboxLoginException.ExceptionStatus.NOT_ACCEPTED_SERVICE
import com.movtery.zalithlauncher.game.account.microsoft.XboxLoginException.ExceptionStatus.REACHED_PLAYTIME_LIMIT
import com.movtery.zalithlauncher.game.account.microsoft.XboxLoginException.ExceptionStatus.REQUIRES_PROOF_OF_AGE
import com.movtery.zalithlauncher.game.account.microsoft.XboxLoginException.ExceptionStatus.RESTRICTED
import com.movtery.zalithlauncher.game.account.microsoft.XboxLoginException.ExceptionStatus.UNDERAGE
import com.movtery.zalithlauncher.game.account.microsoft.XboxLoginException.ExceptionStatus.UNREGISTERED
import com.movtery.zalithlauncher.game.account.microsoft.models.DeviceCodeResponse
import com.movtery.zalithlauncher.game.account.microsoft.models.MinecraftAuthResponse
import com.movtery.zalithlauncher.game.account.microsoft.models.TokenResponse
import com.movtery.zalithlauncher.game.account.microsoft.models.XBLProperties
import com.movtery.zalithlauncher.game.account.microsoft.models.XBLRequest
import com.movtery.zalithlauncher.game.account.microsoft.models.XSTSAuthResult
import com.movtery.zalithlauncher.game.account.microsoft.models.XSTSProperties
import com.movtery.zalithlauncher.game.account.microsoft.models.XSTSRequest
import com.movtery.zalithlauncher.game.account.wardrobe.SkinModelType
import com.movtery.zalithlauncher.game.account.yggdrasil.findUsing
import com.movtery.zalithlauncher.game.account.yggdrasil.getPlayerProfile
import com.movtery.zalithlauncher.game.account.yggdrasil.getSkinModel
import com.movtery.zalithlauncher.path.GLOBAL_CLIENT
import com.movtery.zalithlauncher.utils.logging.Logger
import com.movtery.zalithlauncher.utils.network.httpPostJson
import com.movtery.zalithlauncher.utils.network.safeBodyAsJson
import com.movtery.zalithlauncher.utils.network.submitForm
import com.movtery.zalithlauncher.utils.string.toUuidStr
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.ResponseException
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.Parameters
import io.ktor.http.contentType
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.coroutines.CoroutineContext
import kotlin.time.Duration.Companion.milliseconds

private const val TAG = "MicrosoftAuth"

private val SCOPES = listOf("XboxLive.signin", "offline_access", "openid", "profile", "email")
private const val TENANT = "/consumers"

/**
 * 轮询令牌时允许的最大连续网络失败次数
 * 网络环境较差时，偶发的失败不应中断登录，但持续失败说明网络不可用，应提前放弃
 */
private const val MAX_CONSECUTIVE_POLL_FAILURES = 5

const val MICROSOFT_AUTH_URL = "https://login.microsoftonline.com"
const val LIVE_AUTH_URL = "https://login.live.com"
const val XBL_AUTH_URL = "https://user.auth.xboxlive.com"
const val XSTS_AUTH_URL = "https://xsts.auth.xboxlive.com"
const val MINECRAFT_SERVICES_URL = "https://api.minecraftservices.com"

/**
 * 从 Microsoft 身份验证终端节点获取设备代码响应
 * 设备代码用于在单独的设备或浏览器上授权用户
 */
suspend fun fetchDeviceCodeResponse(context: CoroutineContext): DeviceCodeResponse = coroutineScope {
    withRetry {
        submitForm(
            url = "$MICROSOFT_AUTH_URL$TENANT/oauth2/v2.0/devicecode",
            parameters = Parameters.build {
                append("client_id", BuildKeys.OAUTH_CLIENT_ID)
                append("scope", SCOPES.joinToString(" "))
            },
            context = context
        )
    }
}

/**
 * 使用设备代码流从 Microsoft Azure Active Directory 检索访问令牌和刷新令牌
 * 此函数会定期轮询 Microsoft 令牌端点，直到获取访问令牌或超时
 */
suspend fun getTokenResponse(
    codeResponse: DeviceCodeResponse,
    context: CoroutineContext,
    checkCancelled: suspend (time: Int) -> Boolean
): TokenResponse = coroutineScope {
    var pollingInterval = codeResponse.interval * 1000L
    val expireTime = System.currentTimeMillis() + codeResponse.expiresIn * 1000L

    var cancelled = 0
    suspend fun checkIsReallyCancelled(): Boolean {
        if (checkCancelled(cancelled)) cancelled++
        return cancelled > 1
    }

    //连续的网络层失败次数，避免网络长期不可用时无意义地轮询到设备码过期
    var consecutiveFailures = 0

    Logger.debug(TAG, "Polling for token, interval = ${pollingInterval}ms, expires in ${codeResponse.expiresIn}s")

    while (System.currentTimeMillis() < expireTime) {
        context.ensureActive()

        try {
            val response: JsonObject = submitForm(
                "$MICROSOFT_AUTH_URL$TENANT/oauth2/v2.0/token",
                parameters = Parameters.build {
                    append("grant_type", "urn:ietf:params:oauth:grant-type:device_code")
                    append("device_code", codeResponse.deviceCode)
                    append("client_id", BuildKeys.OAUTH_CLIENT_ID)
                    append("tenant", TENANT)
                },
                context = context
            )
            consecutiveFailures = 0

            if (response["token_type"]?.jsonPrimitive?.content == "Bearer") {
                Logger.debug(TAG, "Access token successfully retrieved")
                return@coroutineScope TokenResponse(
                    accessToken = response["access_token"].text(),
                    refreshToken = response["refresh_token"].text(),
                    expiresIn = response["expires_in"]?.jsonPrimitive?.int ?: 0
                )
            }
            Logger.warning(TAG, "Token endpoint responded without a Bearer token, continuing to poll")
        } catch (e: ClientRequestException) {
            when (val error = e.errorCode()) {
                // 服务器正常响应，说明网络可用
                "authorization_pending" -> consecutiveFailures = 0 // 正常情况，继续轮询
                "slow_down" -> {
                    consecutiveFailures = 0
                    pollingInterval += 1000L
                    Logger.debug(TAG, "Slowing down polling to ${pollingInterval}ms")
                }
                else -> {
                    Logger.error(TAG, "Token endpoint rejected the polling request: error = $error", e)
                    throw e
                }
            }
        } catch (e: CancellationException) {
            Logger.debug(TAG, "Authentication cancelled")
            throw e
        } catch (e: Exception) {
            // 轮询期间的临时性错误（网络波动、DNS解析失败、服务器5xx、请求超时等）不应中断整个登录流程
            // 只要设备码尚未过期，就继续轮询；但网络持续不可用时应提前放弃
            consecutiveFailures++
            if (consecutiveFailures >= MAX_CONSECUTIVE_POLL_FAILURES ||
                System.currentTimeMillis() + pollingInterval >= expireTime
            ) {
                Logger.error(TAG, "Polling failed $consecutiveFailures time(s) in a row, giving up", e)
                throw e
            }
            Logger.warning(TAG, "Polling failed, will retry in ${pollingInterval}ms: ${e::class.simpleName}: ${e.message}")
        }

        if (checkIsReallyCancelled()) {
            Logger.debug(TAG, "The user left the web page, cancelling the authentication")
            throw CancellationException("Authentication cancelled")
        }

        delay(pollingInterval.milliseconds).also {
            context.ensureActive()
        }
    }
    Logger.warning(TAG, "Device code expired before the user completed authorization")
    throw HttpRequestTimeoutException("Authentication timed out!", expireTime)
}

/**
 * 从 OAuth 错误响应中解析 error 字段
 */
private suspend fun ClientRequestException.errorCode(): String? {
    return runCatching {
        response.safeBodyAsJson<JsonObject>()["error"]?.jsonPrimitive?.content
    }.getOrNull()
}

/**
 * 使用不同的身份验证类型异步验证用户，并检索其 Minecraft 帐户信息
 * 函数通过执行一系列步骤来编排身份验证过程，具体取决于提供的 [authType]。
 *
 * 支持刷新现有访问令牌或使用提供的访问令牌。然后，继续使用 Xbox Live （XBL）、Xbox 安全令牌服务 （XSTS） 进行身份验证，最后访问 Minecraft。
 *
 * 支持验证用户是否拥有游戏，然后创建 [Account] 对象。
 *
 * @param statusUpdate 验证执行到哪个步骤，通过这个进行回调更新
 */
suspend fun microsoftAuthAsync(
    authType: AuthType,
    refreshToken: String,
    accessToken: String = "NULL",
    context: CoroutineContext,
    statusUpdate: (AsyncStatus) -> Unit,
): Account = coroutineScope {
    val (finalAccessToken, newRefreshToken) = when (authType) {
        AuthType.Refresh -> refreshAccessToken(refreshToken, statusUpdate, context)
        else -> Pair(accessToken, refreshToken)
    }

    Logger.debug(TAG, "Authenticating with Xbox Live (XBL)")
    val xblToken = authenticateXBL(finalAccessToken, statusUpdate)
    Logger.debug(TAG, "Authenticating with Xbox Secure Token Service (XSTS)")
    val xstsToken = authenticateXSTS(xblToken.first, xblToken.second, statusUpdate, context)
    Logger.debug(TAG, "Authenticating with Minecraft services")
    val authResponse = authenticateMinecraft(xstsToken, statusUpdate, context)
    Logger.debug(TAG, "Verifying Minecraft ownership")
    verifyGameOwnership(authResponse.accessToken, statusUpdate)

    return@coroutineScope createAccount(authResponse, newRefreshToken, xblToken.second, statusUpdate)
}

private suspend fun refreshAccessToken(
    refreshToken: String,
    update: (AsyncStatus) -> Unit,
    context: CoroutineContext
): Pair<String, String> {
    update(AsyncStatus.GETTING_ACCESS_TOKEN)

    return withRetry {
        val response = submitForm<JsonObject>(
            url = "$LIVE_AUTH_URL/oauth20_token.srf",
            parameters = Parameters.build {
                append("client_id", BuildKeys.OAUTH_CLIENT_ID)
                append("refresh_token", refreshToken)
                append("grant_type", "refresh_token")
            },
            context = context
        )
        Pair(
            response["access_token"].text(),
            response["refresh_token"]?.jsonPrimitive?.content ?: refreshToken
        )
    }
}

private suspend fun authenticateXBL(accessToken: String, update: (AsyncStatus) -> Unit): Pair<String, String> {
    update(AsyncStatus.GETTING_XBL_TOKEN)

    suspend fun requestXblToken(rpsTicket: String): Pair<String, String> {
        val requestBody = XBLRequest(
            properties = XBLProperties(
                authMethod = "RPS",
                siteName = "user.auth.xboxlive.com",
                rpsTicket = rpsTicket
            ),
            relyingParty = "http://auth.xboxlive.com",
            tokenType = "JWT"
        )

        val response = GLOBAL_CLIENT.post("$XBL_AUTH_URL/user/authenticate") {
            contentType(ContentType.Application.Json)
            setBody(requestBody)
        }.safeBodyAsJson<JsonObject>()

        //提取uhs
        val uhs = response["DisplayClaims"]?.jsonObject
            ?.get("xui")?.jsonArray
            ?.firstOrNull()?.jsonObject
            ?.get("uhs")?.jsonPrimitive
            ?.content ?: throw Exception("Missing uhs in XBL response")

        return Pair(response["Token"].text(), uhs)
    }

    return withRetry {
        try {
            requestXblToken("d=$accessToken")
        } catch (e: ClientRequestException) {
            // 参考 Wiki：RpsTicket 如遇 400 Bad Request，可尝试去掉 "d=" 前缀重新请求
            // https://zh.minecraft.wiki/w/Tutorial:%E7%BC%96%E5%86%99%E5%90%AF%E5%8A%A8%E5%99%A8#Xbox_Live%E8%BA%AB%E4%BB%BD%E9%AA%8C%E8%AF%81
            if (e.response.status.value == 400) {
                Logger.warning(TAG, "XBL authentication rejected the d= prefixed RpsTicket, retrying without the prefix")
                requestXblToken(accessToken)
            } else throw e
        }
    }
}

private suspend fun authenticateXSTS(
    xblToken: String,
    uhs: String,
    update: (AsyncStatus) -> Unit,
    context: CoroutineContext
): XSTSAuthResult {
    update(AsyncStatus.GETTING_XSTS_TOKEN)

    return withRetry {
        try {
            val response = httpPostJson<JsonObject>(
                url = "$XSTS_AUTH_URL/xsts/authorize",
                body = XSTSRequest(
                    properties = XSTSProperties(
                        sandboxId = "RETAIL",
                        userTokens = listOf(xblToken)
                    ),
                    relyingParty = "rp://api.minecraftservices.com/",
                    tokenType = "JWT"
                ),
                context = context
            )

            XSTSAuthResult(token = response["Token"].text(), uhs = uhs)
        } catch (e: ClientRequestException) {
            // XSTS 对账号类问题统一返回 4xx 及 XErr 错误码，expectSuccess 会提前抛出异常
            // 因此必须从异常响应体中解析 XErr，才能向用户展示真实的失败原因
            val errorBody = runCatching { e.response.safeBodyAsJson<JsonObject>() }.getOrNull()
            when (val xErr = errorBody?.get("XErr").text()) {
                //Reference : https://github.com/PrismarineJS/prismarine-auth/blob/1aef6e1/src/common/Constants.js#L50-L59
                "2148916227" -> throw XboxLoginException(BANNED)
                "2148916229" -> throw XboxLoginException(RESTRICTED)
                "2148916233" -> throw XboxLoginException(UNREGISTERED)
                "2148916234" -> throw XboxLoginException(NOT_ACCEPTED_SERVICE)
                "2148916235" -> throw XboxLoginException(BLOCKED_REGION)
                "2148916236" -> throw XboxLoginException(REQUIRES_PROOF_OF_AGE)
                "2148916237" -> throw XboxLoginException(REACHED_PLAYTIME_LIMIT)
                "2148916238" -> throw XboxLoginException(UNDERAGE)
                else -> {
                    Logger.error(
                        TAG,
                        "XSTS authentication failed: status = ${e.response.status}, XErr = $xErr, message = ${errorBody?.get("Message").text()}"
                    )
                    throw e
                }
            }
        }
    }
}

private suspend fun authenticateMinecraft(
    xstsResult: XSTSAuthResult,
    update: (AsyncStatus) -> Unit,
    context: CoroutineContext
): MinecraftAuthResponse {
    update(AsyncStatus.AUTHENTICATE_MINECRAFT)

    return withRetry {
        runCatching {
            httpPostJson<MinecraftAuthResponse>(
                url = "$MINECRAFT_SERVICES_URL/authentication/login_with_xbox",
                body = mapOf("identityToken" to "XBL3.0 x=${xstsResult.uhs};${xstsResult.token}"),
                context = context
            )
        }.onFailure { e ->
            if (e is ResponseException) {
                when (e.response.status.value) {
                    429 -> throw MinecraftProfileException(FREQUENT)
                    403 -> throw MinecraftProfileException(BLOCKED_IP)
                }
            }
        }.getOrThrow()
    }
}

private suspend fun verifyGameOwnership(accessToken: String, update: (AsyncStatus) -> Unit) {
    update(AsyncStatus.VERIFY_GAME_OWNERSHIP)
    withRetry {
        val response = GLOBAL_CLIENT.get("$MINECRAFT_SERVICES_URL/entitlements/mcstore") {
            header(HttpHeaders.Authorization, "Bearer $accessToken")
        }
        if (response.safeBodyAsJson<JsonObject>()["items"]?.jsonArray?.isEmpty() != false) {
            throw NotPurchasedMinecraftException()
        }
    }
}

private suspend fun createAccount(
    authResponse: MinecraftAuthResponse,
    refreshToken: String,
    uhs: String,
    statusUpdate: (AsyncStatus) -> Unit
): Account {
    statusUpdate(AsyncStatus.GETTING_PLAYER_PROFILE)

    val profile = getPlayerProfile(
        apiUrl = MINECRAFT_SERVICES_URL,
        accessToken = authResponse.accessToken
    )

    val profileId = profile.id
    //避免同一个账号反复添加
    val account = AccountsManager.loadFromProfileID(profileId, AccountType.MICROSOFT.tag) ?: Account()

    return account.apply {
        this.username = profile.name
        this.accessToken = authResponse.accessToken
        this.expiresAt = System.currentTimeMillis() + authResponse.expiresIn * 1000
        this.accountType = AccountType.MICROSOFT.tag
        this.clientToken = BuildKeys.LAUNCHER_NAME.toUuidStr().replace("-", "")
        this.profileId = profileId
        this.refreshToken = refreshToken.ifEmpty { "None" }
        this.xUid = uhs
        this.skinModelType = profile.skins.findUsing()?.getSkinModel() ?: SkinModelType.NONE
    }
}

private fun JsonElement?.text() = this?.jsonPrimitive?.content.orEmpty()

private suspend fun <T> withRetry(
    maxRetries: Int = 3,
    initialDelay: Long = 1000,
    maxDelay: Long = 10_000,
    block: suspend () -> T
): T = com.movtery.zalithlauncher.utils.network.withRetry(
    "MicrosoftAuthenticator", maxRetries, initialDelay, maxDelay, block
)