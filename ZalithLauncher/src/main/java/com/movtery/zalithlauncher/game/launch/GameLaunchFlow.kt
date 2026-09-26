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
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.coroutine.Task
import com.movtery.zalithlauncher.coroutine.TaskFlowExecutor
import com.movtery.zalithlauncher.coroutine.TitledTask
import com.movtery.zalithlauncher.coroutine.addTask
import com.movtery.zalithlauncher.coroutine.buildPhase
import com.movtery.zalithlauncher.game.account.Account
import com.movtery.zalithlauncher.game.account.AccountsManager
import com.movtery.zalithlauncher.game.account.auth_server.AuthServerHelper
import com.movtery.zalithlauncher.game.account.isLocalAccount
import com.movtery.zalithlauncher.game.account.isMicrosoftAccount
import com.movtery.zalithlauncher.game.account.isReloginRequired
import com.movtery.zalithlauncher.game.account.microsoft.validateAccessToken
import com.movtery.zalithlauncher.game.account.refreshMicrosoft
import com.movtery.zalithlauncher.game.download.game.GameLibDownloader
import com.movtery.zalithlauncher.game.support.lwjgl3ify.Lwjgl3ifyPatcher
import com.movtery.zalithlauncher.game.version.download.BaseMinecraftDownloader
import com.movtery.zalithlauncher.game.version.download.DownloadMode
import com.movtery.zalithlauncher.game.version.download.MinecraftDownloader
import com.movtery.zalithlauncher.game.version.installed.Version
import com.movtery.zalithlauncher.game.version.installed.VersionFolders
import com.movtery.zalithlauncher.game.version.installed.VersionInfoParser
import com.movtery.zalithlauncher.game.version.mod.AllModReader
import com.movtery.zalithlauncher.game.version.mod.isEnabled
import com.movtery.zalithlauncher.game.versioninfo.models.GameManifest
import com.movtery.zalithlauncher.ui.activities.runGame
import com.movtery.zalithlauncher.ui.androidText
import com.movtery.zalithlauncher.utils.GSON
import com.movtery.zalithlauncher.utils.network.isNetworkAvailable
import com.movtery.zalithlauncher.viewmodel.ErrorViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.StateFlow

private const val TAG = "GameLaunchFlow"

/** 账号凭据已被服务端拒绝 */
private class LaunchReloginRequired(
    val account: Account
) : RuntimeException()

/** 账号校验或刷新失败时抛出 */
private class LaunchCheckFailed(
    val account: Account,
    cause: Throwable
) : RuntimeException(cause)


/**
 * 游戏启动器
 */
class GameLaunchFlow(scope: CoroutineScope) {
    private val taskExecutor = TaskFlowExecutor(scope)
    val tasksFlow: StateFlow<List<TitledTask>> = taskExecutor.tasksFlow

    /**
     * 启动游戏
     * @param version 指定版本
     * @param skipAccountRefresh 跳过启动前的账号校验，直接使用现有凭据
     */
    fun launch(
        context: Context,
        version: Version,
        skipAccountRefresh: Boolean = false,
        exitActivity: () -> Unit,
        submitError: (ErrorViewModel.ThrowableMessage) -> Unit,
        onReloginRequired: (Account) -> Unit = {},
        onRefreshFailed: (Account, Throwable) -> Unit = { _, _ -> },
        isRunning: () -> Unit = {},
        onComplete: () -> Unit,
    ) {
        if (taskExecutor.isRunning()) {
            //正在启动中，阻止这次启动请求
            isRunning()
            return
        }

        val account = AccountsManager.currentAccountFlow.value ?: return

        taskExecutor.executePhasesAsync(
            onStart = {
                taskExecutor.addPhases(
                    listOf(
                        buildLaunchPhases(
                            context = context,
                            version = version,
                            account = account,
                            skipAccountRefresh = skipAccountRefresh,
                            exitActivity = exitActivity,
                            submitError = submitError
                        )
                    )
                )
            },
            onComplete = onComplete,
            onError = { th ->
                when (th) {
                    is LaunchReloginRequired -> onReloginRequired(th.account)
                    is LaunchCheckFailed -> onRefreshFailed(th.account, th.cause ?: th)
                    else -> {}
                }
            },
        )
    }

    /**
     * 取消当前的启动流程
     */
    fun cancel() {
        taskExecutor.cancel()
    }

    private fun buildLaunchPhases(
        context: Context,
        version: Version,
        account: Account,
        skipAccountRefresh: Boolean,
        exitActivity: () -> Unit,
        submitError: (ErrorViewModel.ThrowableMessage) -> Unit
    ): TaskFlowExecutor.TaskPhase {
        //检查是否联网，根据这个条件决定是否校验账号
        //以及，没有联网时，让微软账号、外置账号作为离线账号登录
        val hasNetwork = isNetworkAvailable(context)
        if (!hasNetwork && !account.isLocalAccount()) {
            version.offlineAccountLogin = true
        }

        return buildPhase {
            if (hasNetwork && !skipAccountRefresh && AccountsManager.isLaunchCheckNeeded(account)) {
                //账号管理页正在刷新该账号时，直接使用现有凭据启动
                addTask(
                    icon = R.drawable.ic_login,
                    title = androidText(R.string.account_logging_in, account.username),
                    dispatcher = Dispatchers.IO
                ) { task ->
                    try {
                        if (account.isMicrosoftAccount()) {
                            checkMicrosoftAccount(task, account)
                        } else {
                            checkOtherAccount(task, context, account)
                        }
                        AccountsManager.markSessionValidated(account)
                        AccountsManager.suspendSaveAccount(account)
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: Throwable) {
                        if (e.isReloginRequired()) throw LaunchReloginRequired(account)
                        throw LaunchCheckFailed(account, e)
                    }
                }
            }

            //扫描模组列表，开启对应功能
            addTask(
                icon = R.drawable.ic_extension_outlined,
                title = androidText(R.string.launch_check_mods),
                dispatcher = Dispatchers.IO
            ) { task ->
                val patchedManifest = checkMods(version)
                val manifest = patchedManifest ?: VersionInfoParser(version).setInheriting().build()
                val manifestString = GSON.toJson(manifest)

                version.launchManifest = manifestString

                // 如果打了补丁，此处需要重新检索一下依赖库并下载
                patchedManifest?.let {
                    val libDownloader = GameLibDownloader(
                        downloader = BaseMinecraftDownloader(version.getGameHome()),
                        gameJson = manifestString
                    )
                    libDownloader.schedule(task)
                    libDownloader.download(task)
                }
            }

            if (!version.skipGameIntegrityCheck()) {
                //校验并修复游戏文件
                addTask(
                    icon = R.drawable.ic_assignment_filled,
                    title = androidText(R.string.minecraft_download_stat_verify_task),
                    task = createGameDownloadTask(
                        context = context,
                        version = version,
                        submitError = submitError
                    )
                )
            }

            //启动游戏
            addTask(
                icon = R.drawable.ic_rocket_launch_filled,
                title = androidText(R.string.main_launch_game)
            ) { _ ->
                runGame(context, version, account)
                exitActivity()
            }
        }
    }

    /**
     * 微软账号：向服务端校验缓存的凭据，被拒绝或已临近过期时静默刷新
     */
    private suspend fun checkMicrosoftAccount(task: Task, account: Account) {
        val expired = System.currentTimeMillis() > account.expiresAt - 5 * 60 * 1000
        if (expired || !validateAccessToken(account)) {
            account.refreshMicrosoft(task, currentCoroutineContext())
        }
    }

    /**
     * 外置账号：依次尝试 validate 与 refresh，均被服务端拒绝时再用账号密码重新登录
     */
    private suspend fun checkOtherAccount(task: Task, context: Context, account: Account) {
        task.updateMessage(androidText(R.string.account_logging_in, account.username))
        val helper = AuthServerHelper(
            baseUrl = account.otherBaseUrl!!,
            serverName = account.accountType!!,
            email = account.otherAccount!!,
            password = account.otherPassword!!
        )
        if (!helper.validateOrRefresh(context, account)) {
            helper.passwordLogin(context, account)
        }
    }

    private fun createGameDownloadTask(
        context: Context,
        version: Version,
        submitError: (ErrorViewModel.ThrowableMessage) -> Unit
    ): Task {
        return MinecraftDownloader(
            context = context,
            version = version.getVersionInfo()?.minecraftVersion ?: version.getVersionName(),
            customName = version.getVersionName(),
            gameHome = version.getGameHome(),
            mode = DownloadMode.VERIFY_AND_REPAIR,
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
     * 扫描模组列表并开启对应功能
     * @return 经过补丁的游戏清单，为 null 则表示没什么补丁
     */
    private suspend fun checkMods(version: Version): GameManifest? {
        val modsDir = VersionFolders.MOD.getDir(version.getGameDir())
        val mods = AllModReader(modsDir).readAllLocals()

        if (mods.any { it.id == "touchcontroller" && it.file.isEnabled() }) {
            version.enableTouchProxy = true
        }

        val patched = Lwjgl3ifyPatcher.patchIfNeeded(version, mods)
        return patched
    }
}
