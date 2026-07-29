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

package com.movtery.zalithlauncher.game.launch

import android.content.Context
import com.google.gson.JsonObject
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.coroutine.Task
import com.movtery.zalithlauncher.coroutine.TaskSystem
import com.movtery.zalithlauncher.game.account.Account
import com.movtery.zalithlauncher.game.account.AccountsManager
import com.movtery.zalithlauncher.game.account.auth_server.ResponseException
import com.movtery.zalithlauncher.game.account.isLocalAccount
import com.movtery.zalithlauncher.game.account.microsoft.MinecraftProfileException
import com.movtery.zalithlauncher.game.account.microsoft.NotPurchasedMinecraftException
import com.movtery.zalithlauncher.game.account.microsoft.XboxLoginException
import com.movtery.zalithlauncher.game.account.microsoft.toLocal
import com.movtery.zalithlauncher.game.plugin.vpl.PluginNativeLoadGuard
import com.movtery.zalithlauncher.game.plugin.vpl.PluginTrustGate
import com.movtery.zalithlauncher.utils.string.getMessageOrToString
import com.vpl.verifiedpluginload.model.PluginLoadAuthorization
import com.movtery.zalithlauncher.game.version.download.DownloadMode
import com.movtery.zalithlauncher.game.version.download.MinecraftDownloader
import com.movtery.zalithlauncher.game.version.installed.GraphicsApi
import com.movtery.zalithlauncher.game.version.installed.Version
import com.movtery.zalithlauncher.game.version.installed.VersionFolders
import com.movtery.zalithlauncher.game.version.mod.AllModReader
import com.movtery.zalithlauncher.setting.AllSettings
import com.movtery.zalithlauncher.ui.AndroidStringText
import com.movtery.zalithlauncher.ui.activities.runGame
import com.movtery.zalithlauncher.ui.androidText
import com.movtery.zalithlauncher.utils.GSON
import com.movtery.zalithlauncher.utils.file.readText
import com.movtery.zalithlauncher.utils.logging.Logger
import com.movtery.zalithlauncher.utils.network.isNetworkAvailable
import com.movtery.zalithlauncher.utils.network.toLocal
import com.movtery.zalithlauncher.viewmodel.ErrorViewModel
import io.ktor.client.plugins.HttpRequestTimeoutException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.ConnectException
import java.net.UnknownHostException
import java.nio.channels.UnresolvedAddressException
import java.util.zip.ZipFile

private const val TAG = "LaunchGame"

object LaunchGame {
    var isLaunching: Boolean = false
        private set

    fun launchGame(
        context: Context,
        version: Version,
        exitActivity: () -> Unit,
        waitForVulkanChecker: suspend () -> Unit,
        submitError: (ErrorViewModel.ThrowableMessage) -> Unit
    ) {
        if (isLaunching) return
        val account = AccountsManager.currentAccountFlow.value ?: return
        isLaunching = true

        //检查是否联网，根据这个条件决定是否登录账号
        //以及，没有联网时，让微软账号、外置账号作为离线账号登录
        val hasNetwork = isNetworkAvailable(context)

        // Captured by the verification task below and consumed by the native load guard immediately
        // before any plugin directory is used, so the guard re-checks what the user actually approved.
        var authorizations: List<PluginLoadAuthorization> = emptyList()

        val downloadTask = createDownloadTask(
            context = context,
            version = version,
            account = account,
            exitActivity = exitActivity,
            waitForVulkanChecker = waitForVulkanChecker,
            authorizations = { authorizations },
            submitError = submitError
        )
        fun startDownloadTask() {
            TaskSystem.submitTask(downloadTask) { isLaunching = false }
        }

        val loginTask = createLoginTask(
            context = context,
            hasNetwork = hasNetwork,
            account = account,
            submitError = submitError
        ) {
            startDownloadTask()
        }

        fun startLaunchWork() {
            if (loginTask != null) {
                TaskSystem.submitTask(loginTask)
            } else {
                if (!hasNetwork && !account.isLocalAccount()) {
                    //没联网时作为离线账号登录
                    version.offlineAccountLogin = true
                }
                startDownloadTask()
            }
        }

        // Plugin trust is settled before any other launch work. Refusing here costs the user nothing
        // that a download or a login already spent, and the confirmation dialog does not have to
        // compete with download progress for the same task message.
        if (PluginTrustGate.isVerificationRequired()) {
            TaskSystem.submitTask(
                createPluginVerificationTask(
                    context = context,
                    onVerified = {
                        authorizations = it
                        startLaunchWork()
                    },
                    submitError = submitError
                )
            ) { if (authorizations.isEmpty()) isLaunching = false }
        } else {
            startLaunchWork()
        }
    }

    /**
     * Settles plugin trust before the rest of the launch runs.
     *
     * A refusal has to say why: every guard and gate rejection is reported, so a blocked launch does
     * not look like a launch that silently stopped.
     */
    private fun createPluginVerificationTask(
        context: Context,
        onVerified: (List<PluginLoadAuthorization>) -> Unit,
        submitError: (ErrorViewModel.ThrowableMessage) -> Unit
    ): Task = Task.runTask(
        task = { task ->
            task.updateProgress(-1f)
            task.updateMessage(androidText(R.string.game_plugin_verification_title))
            val authorizations = PluginTrustGate.verifyForLaunch(context) { title ->
                task.updateMessage(title)
            }
            onVerified(authorizations)
        },
        onCancel = {
            Logger.info(TAG, "Plugin trust gate cancelled by user")
        },
        onError = { e ->
            if (e is CancellationException) {
                Logger.info(TAG, "Plugin trust gate cancelled by user")
            } else {
                Logger.error(TAG, "Plugin trust gate failed", e)
                submitError(
                    ErrorViewModel.ThrowableMessage(
                        title = androidText(R.string.plugin_trust_level_error),
                        message = androidText(e.getMessageOrToString())
                    )
                )
            }
        }
    )

    private fun createDownloadTask(
        context: Context,
        version: Version,
        account: Account,
        exitActivity: () -> Unit,
        waitForVulkanChecker: suspend () -> Unit,
        authorizations: () -> List<PluginLoadAuthorization>,
        submitError: (ErrorViewModel.ThrowableMessage) -> Unit
    ): Task {
        return MinecraftDownloader(
            context = context,
            version = version.getVersionInfo()?.minecraftVersion ?: version.getVersionName(),
            customName = version.getVersionName(),
            verifyIntegrity = !version.skipGameIntegrityCheck(),
            mode = DownloadMode.VERIFY_AND_REPAIR,
            onCompletion = { task ->
                task.updateProgress(-1f)
                task.updateMessage(null)
                checkEnableTouchProxy(version)
                task.updateMessage(androidText(R.string.game_vulkan_check_title))
                checkVulkanCapabilities(version, waitForVulkanChecker)

                runCatching {
                    // TOCTOU re-check: the guard validates that the APK, native directory, and
                    // signatures still match what verifyForLaunch authorized at launch start.
                    PluginNativeLoadGuard.verify(context, authorizations())
                }.onSuccess {
                    runGame(context, version, account)
                    exitActivity()
                }.onFailure { e ->
                    if (e is CancellationException) {
                        Logger.info(TAG, "Plugin load guard cancelled")
                    } else {
                        Logger.error(TAG, "Plugin load guard failed", e)
                        submitError(
                            ErrorViewModel.ThrowableMessage(
                                title = androidText(R.string.plugin_trust_level_error),
                                message = androidText(e.getMessageOrToString())
                            )
                        )
                    }
                }
            },
            onError = { message ->
                submitError(
                    ErrorViewModel.ThrowableMessage(
                        title = androidText(R.string.minecraft_download_failed),
                        message = androidText(message)
                    )
                )
            }
        ).getDownloadTask()
    }

    /**
     * 检查是否安装了 TouchController，安装后开启控制代理
     */
    private suspend fun checkEnableTouchProxy(version: Version) {
        val modsDir = VersionFolders.MOD.getDir(version.getGameDir())
        val reader = AllModReader(modsDir)
        for (mod in reader.readAllLocals()) {
            if (mod.id == "touchcontroller") {
                version.enableTouchProxy = true
                break
            }
        }
    }

    private suspend fun checkVulkanCapabilities(
        version: Version,
        waitForVulkanChecker: suspend () -> Unit
    ) {
        if (!AllSettings.autoVulkanChecker.getValue()) return

        val api = version.getGraphicsApi()
        if (api == GraphicsApi.OPENGL) return

        //游戏可能使用Vulkan，检查版本是否为 26.2+
        val clientJar = version.getClientJar()
        if (clientJar.exists()) {
            val hasVulkan = runCatching {
                withContext(Dispatchers.IO) {
                    //在客户端中读取数据版本
                    ZipFile(clientJar).use { zip ->
                        zip.getEntry("version.json")
                            ?.readText(zip)
                            ?.let { GSON.fromJson(it, JsonObject::class.java) }
                            ?.let { json ->
                                //https://zh.minecraft.wiki/w/%E7%89%88%E6%9C%AC%E4%BF%A1%E6%81%AF%E6%96%87%E4%BB%B6%E6%A0%BC%E5%BC%8F
                                json.get("world_version")?.asInt
                            }
                    }?.let { worldVersion ->
                        //26.2-snapshot-1
                        worldVersion >= 4883
                    }
                } ?: false
            }.onFailure { e ->
                Logger.warning(TAG, "Unable to determine the data version of this client Jar, possibly due to an outdated version.", e)
            }.getOrDefault(false)

            if (hasVulkan) {
                //等待Vulkan检查完成
                waitForVulkanChecker()
            }
        }
    }

    private fun createLoginTask(
        context: Context,
        hasNetwork: Boolean,
        account: Account,
        submitError: (ErrorViewModel.ThrowableMessage) -> Unit,
        onFinally: () -> Unit
    ): Task? {
        val needsRefresh = hasNetwork && System.currentTimeMillis() > account.expiresAt - 5 * 60 * 1000

        return if (needsRefresh) {
            AccountsManager.performLoginTask(
                context = context,
                account = account,
                onSuccess = { acc, _ ->
                    AccountsManager.suspendSaveAccount(acc)
                },
                onFailed = { error ->
                    val message: AndroidStringText = when (error) {
                        is NotPurchasedMinecraftException -> toLocal()
                        is MinecraftProfileException -> error.toLocal()
                        is XboxLoginException -> error.toLocal()
                        is ResponseException -> androidText(error.responseMessage)
                        is HttpRequestTimeoutException -> androidText(R.string.error_timeout)
                        is UnknownHostException, is UnresolvedAddressException -> androidText(R.string.error_network_unreachable)
                        is ConnectException -> androidText(R.string.error_connection_failed)
                        is io.ktor.client.plugins.ResponseException -> error.toLocal()
                        else -> {
                            Logger.error(TAG, "An unknown exception was caught!", error)
                            androidText(
                                error.localizedMessage ?: error.message ?: error::class.qualifiedName ?: "Unknown error"
                            )
                        }
                    }

                    submitError(
                        ErrorViewModel.ThrowableMessage(
                            title = androidText(R.string.account_logging_in_failed),
                            message = message
                        )
                    )
                },
                onFinally = onFinally
            )
        } else {
            null
        }
    }
}
