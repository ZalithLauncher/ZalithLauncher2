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

package com.movtery.zalithlauncher.game.account

import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.context.COPY_LABEL_DEVICE_CODE
import com.movtery.zalithlauncher.coroutine.Task
import com.movtery.zalithlauncher.coroutine.TaskSystem
import com.movtery.zalithlauncher.game.account.auth_server.AuthServerHelper
import com.movtery.zalithlauncher.game.account.auth_server.data.AuthServer
import com.movtery.zalithlauncher.game.account.auth_server.getAuthServeInfo
import com.movtery.zalithlauncher.game.account.microsoft.AsyncStatus
import com.movtery.zalithlauncher.game.account.microsoft.AuthType
import com.movtery.zalithlauncher.game.account.microsoft.MinecraftProfileException
import com.movtery.zalithlauncher.game.account.microsoft.NotPurchasedMinecraftException
import com.movtery.zalithlauncher.game.account.microsoft.XboxLoginException
import com.movtery.zalithlauncher.game.account.microsoft.fetchDeviceCodeResponse
import com.movtery.zalithlauncher.game.account.microsoft.getTokenResponse
import com.movtery.zalithlauncher.game.account.microsoft.microsoftAuthAsync
import com.movtery.zalithlauncher.game.account.microsoft.toLocal
import com.movtery.zalithlauncher.path.URL_USER_AGENT
import com.movtery.zalithlauncher.ui.AndroidStringText
import com.movtery.zalithlauncher.ui.androidText
import com.movtery.zalithlauncher.ui.screens.content.elements.MicrosoftLoginOperation
import com.movtery.zalithlauncher.utils.copyText
import com.movtery.zalithlauncher.utils.logging.Logger
import com.movtery.zalithlauncher.utils.network.toLocal
import com.movtery.zalithlauncher.viewmodel.ErrorViewModel
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.ResponseException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import org.json.JSONObject
import java.net.ConnectException
import java.net.HttpURLConnection
import java.net.URL
import java.net.UnknownHostException
import java.nio.channels.UnresolvedAddressException
import java.util.Locale
import java.util.Objects
import java.util.UUID
import kotlin.coroutines.CoroutineContext

private const val TAG = "AccountUtils"

fun Account.isAuthServerAccount(): Boolean {
    return !isLocalAccount() && !Objects.isNull(otherBaseUrl) && otherBaseUrl != "0"
}

fun Account.isMicrosoftAccount(): Boolean {
    return accountType == AccountType.MICROSOFT.tag
}

fun Account.isLocalAccount(): Boolean {
    return accountType == AccountType.LOCAL.tag
}

fun Account?.isNoLoginRequired(): Boolean {
    return this == null || isLocalAccount()
}

fun Account.isSkinChangeAllowed(): Boolean {
    return isMicrosoftAccount() || isLocalAccount()
}

fun Account.accountTypePriority(): Int {
    return when (this.accountType) {
        AccountType.MICROSOFT.tag -> 0 //微软账号优先
        null -> Int.MAX_VALUE
        else -> 1
    }
}

private const val MICROSOFT_LOGGING_TASK = "microsoft_logging_task"

/**
 * 检查当前微软账号登陆是否正在进行中
 */
fun isMicrosoftLogging() = TaskSystem.containsTask(MICROSOFT_LOGGING_TASK)

fun microsoftLogin(
    context: Context,
    toWeb: (url: String) -> Unit,
    backToMain: () -> Unit,
    checkIfInWebScreen: () -> Boolean,
    updateOperation: (MicrosoftLoginOperation) -> Unit,
    showToast: (AndroidStringText, duration: Int) -> Unit,
    submitError: (ErrorViewModel.ThrowableMessage) -> Unit
) {
    val task = Task.runTask(
        id = MICROSOFT_LOGGING_TASK,
        dispatcher = Dispatchers.IO,
        task = { task ->
            Logger.info(TAG, "Starting Microsoft account login")
            task.updateProgress(-1f)
            task.updateMessage(androidText(R.string.account_microsoft_fetch_device_code))
            val deviceCode = fetchDeviceCodeResponse(coroutineContext)
            Logger.debug(TAG, "Device code received, verification url: ${deviceCode.verificationUrl}, expires in ${deviceCode.expiresIn}s")
            copyText(COPY_LABEL_DEVICE_CODE, deviceCode.userCode, context, false)
            showToast(
                androidText(R.string.account_microsoft_coped_device_code, deviceCode.userCode),
                Toast.LENGTH_SHORT
            )
            toWeb(deviceCode.verificationUrl)
            task.updateProgress(-1f)
            task.updateMessage(androidText(R.string.account_microsoft_get_token, deviceCode.userCode))
            val tokenResponse = getTokenResponse(deviceCode, coroutineContext) { time ->
                (!checkIfInWebScreen()).also { exit ->
                    if (exit && time > 0) {
                        //如果已退出网页，则视为用户想要退出登录
                        //弹出提示
                        Logger.debug(TAG, "User left the web page during device code polling")
                        showToast(
                            androidText(R.string.account_microsoft_exit_by_user),
                            Toast.LENGTH_LONG
                        )
                    }
                }
            }
            backToMain()
            val account = microsoftAuth(
                AuthType.Access,
                tokenResponse.refreshToken,
                tokenResponse.accessToken,
                coroutineContext = coroutineContext,
                updateProgress = task::updateProgress,
                updateMessage = task::updateMessage,
            )
            task.updateMessage(androidText(R.string.account_logging_in_saving))
            account.downloadYggdrasil()
            AccountsManager.saveAccount(account)
            Logger.info(TAG, "Microsoft account login successful: ${account.username}")
        },
        onError = { th ->
            if (th !is CancellationException) {
                Logger.error(TAG, "Microsoft account login failed", th)
            }
            when (th) {
                is HttpRequestTimeoutException -> androidText(R.string.account_logging_time_out)
                is NotPurchasedMinecraftException -> toLocal()
                is MinecraftProfileException -> th.toLocal()
                is XboxLoginException -> th.toLocal()
                is UnknownHostException, is UnresolvedAddressException -> androidText(R.string.error_network_unreachable)
                is ConnectException -> androidText(R.string.error_connection_failed)
                is ResponseException -> th.toLocal()
                is CancellationException -> { null }
                else -> {
                    androidText(
                        th.localizedMessage ?: th.message ?: th::class.qualifiedName ?: "Unknown error"
                    )
                }
            }?.let { message ->
                submitError(
                    ErrorViewModel.ThrowableMessage(
                        title = androidText(R.string.account_logging_in_failed),
                        message = message
                    )
                )
            }
        },
        onFinally = {
            updateOperation(MicrosoftLoginOperation.None)
        }
    )

    TaskSystem.submitTask(task)
}

private suspend fun microsoftAuth(
    authType: AuthType,
    refreshToken: String,
    accessToken: String = "NULL",
    coroutineContext: CoroutineContext,
    updateProgress: (Float) -> Unit,
    updateMessage: (AndroidStringText?) -> Unit,
): Account {
    return microsoftAuthAsync(authType, refreshToken, accessToken, coroutineContext) { asyncStatus ->
        when (asyncStatus) {
            AsyncStatus.GETTING_ACCESS_TOKEN -> {
                updateProgress(0.25f)
                updateMessage(androidText(R.string.account_microsoft_getting_access_token))
            }
            AsyncStatus.GETTING_XBL_TOKEN -> {
                updateProgress(0.4f)
                updateMessage(androidText(R.string.account_microsoft_getting_xbl_token))
            }
            AsyncStatus.GETTING_XSTS_TOKEN -> {
                updateProgress(0.55f)
                updateMessage(androidText(R.string.account_microsoft_getting_xsts_token))
            }
            AsyncStatus.AUTHENTICATE_MINECRAFT -> {
                updateProgress(0.7f)
                updateMessage(androidText(R.string.account_microsoft_authenticate_minecraft))
            }
            AsyncStatus.VERIFY_GAME_OWNERSHIP -> {
                updateProgress(0.85f)
                updateMessage(androidText(R.string.account_microsoft_verify_game_ownership))
            }
            AsyncStatus.GETTING_PLAYER_PROFILE -> {
                updateProgress(1f)
                updateMessage(androidText(R.string.account_microsoft_getting_player_profile))
            }
        }
    }
}

fun microsoftRefresh(
    account: Account,
    onSuccess: suspend (Account, Task) -> Unit,
    onFailed: (th: Throwable) -> Unit = {},
    onFinally: () -> Unit = {}
): Task? {
    if (TaskSystem.containsTask(account.profileId)) return null

    return Task.runTask(
        id = account.profileId,
        dispatcher = Dispatchers.IO,
        task = { task ->
            account.refreshMicrosoft(task, coroutineContext)
            onSuccess(account, task)
        },
        onError = { e ->
            if (e is CancellationException) return@runTask
            onFailed(e)
        },
        onFinally = onFinally
    )
}

suspend fun Account.refreshMicrosoft(
    task: Task,
    coroutineContext: CoroutineContext = Dispatchers.IO
) {
    val newAcc = microsoftAuth(
        AuthType.Refresh,
        refreshToken,
        accessToken,
        coroutineContext = coroutineContext,
        updateProgress = task::updateProgress,
        updateMessage = task::updateMessage,
    )
    apply {
        this.accessToken = newAcc.accessToken
        this.clientToken = newAcc.clientToken
        this.profileId = newAcc.profileId
        this.username = newAcc.username
        this.refreshToken = newAcc.refreshToken
        this.xUid = newAcc.xUid
    }
}

fun otherLogin(
    context: Context,
    account: Account,
    onSuccess: suspend (Account, task: Task) -> Unit = { _, _ -> },
    onFailed: (th: Throwable) -> Unit = {},
    onFinally: () -> Unit = {}
): Task? {
    if (TaskSystem.containsTask(account.uniqueUUID)) return null

    return AuthServerHelper(
        baseUrl = account.otherBaseUrl!!,
        serverName = account.accountType!!,
        email = account.otherAccount!!,
        password = account.otherPassword!!,
        onSuccess = onSuccess,
        onFailed = onFailed,
        onFinally = onFinally
    ).justLogin(context, account)
}

/**
 * 离线账号登陆
 */
fun localLogin(userName: String, userUUID: String?) {
    val account = if (userUUID != null) {
        Account(
            username = userName,
            profileId = userUUID,
            accountType = AccountType.LOCAL.tag
        )
    } else {
        //如果不填，则使用默认生成的 uuid
        Account(
            username = userName,
            accountType = AccountType.LOCAL.tag
        )
    }
    AccountsManager.saveAccount(account)
}

/**
 * [From HMCL](https://github.com/HMCL-dev/HMCL/blob/5c2bb1cc251901dd471a8aa8048d90c22bb56916/HMCLCore/src/main/java/org/jackhuang/hmcl/util/gson/UUIDTypeAdapter.java#L55)
 */
private val regex = Regex("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})")

/**
 * [From HMCL](https://github.com/HMCL-dev/HMCL/blob/5c2bb1cc251901dd471a8aa8048d90c22bb56916/HMCLCore/src/main/java/org/jackhuang/hmcl/util/gson/UUIDTypeAdapter.java#L57-L59)
 */
fun accountUUID(input: String): UUID {
    val formatted = regex.replace(input, "$1-$2-$3-$4-$5")
    return UUID.fromString(formatted)
}

fun accountUUID(input: UUID): String {
    return input.toString().replace("-", "")
}

/**
 * [From HMCL](https://github.com/HMCL-dev/HMCL/blob/5c2bb1cc251901dd471a8aa8048d90c22bb56916/HMCLCore/src/main/java/org/jackhuang/hmcl/auth/offline/OfflineAccountFactory.java#L79-L81)
 */
fun getUUIDFromUserName(username: String): UUID {
    return UUID.nameUUIDFromBytes(("OfflinePlayer:$username").toByteArray(Charsets.UTF_8))
}

fun addOtherServer(
    serverUrl: String,
    onThrowable: (Throwable) -> Unit = {}
) {
    val task = Task.runTask(
        task = { task ->
            task.updateProgress(-1f)
            task.updateMessage(androidText(R.string.account_other_login_getting_full_url))
            val isNide8 = isValidPassportId(serverUrl)
            val fullServerUrl = if (isNide8) {
                //可能是一个统一通行证服务器ID
                "https://auth.mc-user.com:233/$serverUrl"
            } else {
                tryGetFullServerUrl(serverUrl)
            }
            ensureActive()
            task.updateProgress(0.5f)
            task.updateMessage(androidText(R.string.account_other_login_getting_server_info))
            runCatching {
                getAuthServeInfo(fullServerUrl)
            }.onFailure { th ->
                Logger.error(TAG, "Failed to get server info", th)
                onThrowable(th)
            }.getOrNull()?.let { data ->
                JSONObject(data).optJSONObject("meta")?.let { meta ->
                    if (AccountsManager.isAuthServerExists(fullServerUrl)) {
                        //确保服务器不重复
                        return@runTask
                    }
                    val server = AuthServer(
                        serverName = meta.optString("serverName"),
                        baseUrl = fullServerUrl,
                        register = if (!isNide8) {
                            meta.optJSONObject("links")?.optString("register") ?: ""
                        } else "https://login.mc-user.com:233/$serverUrl"
                    )
                    task.updateProgress(0.8f)
                    task.updateMessage(androidText(R.string.account_other_login_saving_server))
                    AccountsManager.saveAuthServer(server)
                    task.updateProgress(1f)
                    task.updateMessage(androidText(R.string.generic_done))
                }
            }
        },
        onError = { e ->
            onThrowable(e)
            Logger.error(TAG, "Failed to add auth server", e)
        }
    )

    TaskSystem.submitTask(task)
}

/**
 * 获取账号类型名称
 */
@Composable
fun getAccountTypeName(account: Account): String {
    return if (account.isMicrosoftAccount()) {
        stringResource(R.string.account_type_microsoft)
    } else if (account.isAuthServerAccount()) {
        account.accountType ?: "Unknown"
    } else {
        stringResource(R.string.account_type_local)
    }
}

/**
 * 修改自源代码：[HMCL Core: AuthlibInjectorServer.java](https://github.com/HMCL-dev/HMCL/blob/b38076f/HMCLCore/src/main/java/org/jackhuang/hmcl/auth/authlibinjector/AuthlibInjectorServer.java#L53-L85)
 * <br>原项目版权归原作者所有，遵循GPL v3协议
 */
fun tryGetFullServerUrl(baseUrl: String): String {
    fun String.addSlashIfMissing(): String {
        if (!endsWith("/")) return "$this/"
        return this
    }

    val initialUrl = addHttpsIfMissing(baseUrl)
    return runCatching {
        var finalUrl = initialUrl

        fun open(url: String): HttpURLConnection =
            (URL(url).openConnection() as HttpURLConnection).apply {
                connectTimeout = 5000
                readTimeout = 5000
                setRequestProperty("User-Agent", URL_USER_AGENT)
            }

        var conn: HttpURLConnection? = null
        try {
            conn = open(finalUrl)
            conn.getHeaderField("x-authlib-injector-api-location")?.let { ali ->
                val absoluteAli = URL(conn.url, ali).toString().addSlashIfMissing()
                if (absoluteAli != finalUrl.addSlashIfMissing()) {
                    conn.disconnect()
                    conn = open(absoluteAli)
                    finalUrl = absoluteAli
                }
            }
            finalUrl.addSlashIfMissing()
        } finally {
            conn?.disconnect()
        }
    }.getOrElse { e ->
        Logger.error(TAG, "Failed to get full server url", e)
        initialUrl
    }
}

/**
 * 修改自源代码：[HMCL Core: AuthlibInjectorServer.java](https://github.com/HMCL-dev/HMCL/blob/main/HMCLCore/src/main/java/org/jackhuang/hmcl/auth/authlibinjector/AuthlibInjectorServer.java#L90-#L96)
 * <br>原项目版权归原作者所有，遵循GPL v3协议
 */
private fun addHttpsIfMissing(baseUrl: String): String {
    return if (!baseUrl.startsWith("http://", true) && !baseUrl.startsWith("https://")) {
        "https://$baseUrl".lowercase(Locale.ROOT)
    } else baseUrl.lowercase(Locale.ROOT)
}

/**
 * 检查是否为32位16进制字符串，这可能是一个
 * 统一通行证的服务器ID
 */
private fun isValidPassportId(id: String): Boolean {
    val pattern = Regex("^[0-9a-f]{32}$", RegexOption.IGNORE_CASE)
    return pattern.matches(id)
}