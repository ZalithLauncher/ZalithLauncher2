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
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.coroutine.Task
import com.movtery.zalithlauncher.coroutine.TaskSystem
import com.movtery.zalithlauncher.game.account.Account
import com.movtery.zalithlauncher.game.account.AccountsManager
import com.movtery.zalithlauncher.game.account.auth_server.data.AuthServer
import com.movtery.zalithlauncher.game.account.auth_server.models.AuthResult
import com.movtery.zalithlauncher.ui.androidText
import com.movtery.zalithlauncher.utils.logging.Logger
import kotlinx.coroutines.Dispatchers
import java.util.Objects

private const val TAG = "AuthServerHelper"

/**
 * 帮助登录外置账号（创建新的外置账号、仅登录当前外置账号）
 */
class AuthServerHelper(
    private val baseUrl: String,
    private val serverName: String,
    private val email: String,
    private val password: String,
    private val onSuccess: suspend (Account, Task) -> Unit = { _, _ -> },
    private val onFailed: (th: Throwable) -> Unit = {},
    private val onFinally: () -> Unit = {}
) {
    constructor(
        server: AuthServer,
        email: String,
        password: String,
        onSuccess: suspend (Account, Task) -> Unit = { _, _ -> },
        onFailed: (th: Throwable) -> Unit = {},
        onFinally: () -> Unit = {}
    ): this(server.baseUrl, server.serverName, email, password, onSuccess, onFailed, onFinally)

    private val apiServer = AuthServerApi(baseUrl)

    private fun login(
        context: Context,
        taskId: String? = null,
        loggingString: String = serverName,
        onlyOneRole: suspend (AuthResult, Task) -> Unit = { _, _ -> },
        hasMultipleRoles: suspend (AuthResult, Task) -> Unit = { _, _ -> }
    ) : Task {
        return Task.runTask(
            id = taskId,
            dispatcher = Dispatchers.IO,
            task = { task ->
                apiServer.login(
                    context, email, password,
                    onSuccess = { authResult ->
                        if (!Objects.isNull(authResult.selectedProfile)) {
                            onlyOneRole(authResult, task)
                        } else if (!Objects.isNull(authResult.availableProfiles)) {
                            hasMultipleRoles(authResult, task)
                        }
                    },
                    onFailed = onFailed
                )
            },
            onError = { e ->
                Logger.error(TAG, "An exception was encountered while performing the login task.", e)
                onFailed(e)
            },
            onFinally = onFinally
        ).apply {
            updateMessage(
                androidText(R.string.account_logging_in, loggingString)
            )
        }
    }

    private fun updateAccountInfo(
        account: Account,
        authResult: AuthResult,
        userName: String,
        profileId: String
    ) {
        account.apply {
            this.accessToken = authResult.accessToken
            this.clientToken = authResult.clientToken
            this.otherBaseUrl = baseUrl
            this.otherAccount = email
            this.otherPassword = password
            this.accountType = serverName
            this.username = userName
            this.profileId = profileId
        }
    }

    /**
     * 启动前自检
     * @return 两者都被服务端拒绝时返回 false
     */
    suspend fun validateOrRefresh(context: Context, account: Account): Boolean {
        if (apiServer.validate(context, account)) return true

        return try {
            val authResult = apiServer.refreshToken(context, account, select = true)
            account.accessToken = authResult.accessToken
            true
        } catch (e: ResponseException) {
            if (e.statusCode == 403) false else throw e
        }
    }

    /**
     * 使用账号密码重新登录并匹配当前角色，仅在 validate 与 refresh 均被拒绝时调用
     */
    suspend fun passwordLogin(context: Context, account: Account) {
        val authResult = apiServer.authenticate(context, account.otherAccount!!, password)
        val selected = authResult.selectedProfile
        val matchedName: String
        val matchedId: String
        if (selected != null && selected.id == account.profileId) {
            matchedId = selected.id
            matchedName = selected.name
        } else {
            val profile = authResult.availableProfiles?.find { it.id == account.profileId }
                ?: throw ResponseException(context.getString(R.string.account_other_login_role_not_found))
            matchedId = profile.id
            matchedName = profile.name
        }
        updateAccountInfo(account, authResult, matchedName, matchedId)
    }

    /**
     * 从账号管理中查找同一个配置Id的账号（当前账号类型）
     */
    private fun loadFromProfileID(profileId: String): Account {
        return AccountsManager.loadFromProfileID(profileId, serverName) ?: Account()
    }

    /**
     * 通过账号密码，登录一个新的账号
     * @param selectRole 当账号拥有多个角色时，需要选择角色
     */
    fun createNewAccount(
        context: Context,
        selectRole: (List<AuthResult.AvailableProfiles>, (AuthResult.AvailableProfiles) -> Unit) -> Unit
    ) {
        val task = login(
            context,
            onlyOneRole = { authResult, task ->
                val profileId = authResult.selectedProfile!!.id
                val account: Account = loadFromProfileID(profileId)
                updateAccountInfo(account, authResult, authResult.selectedProfile!!.name, profileId)
                onSuccess(account, task)
            },
            hasMultipleRoles = { authResult, _ ->
                selectRole(authResult.availableProfiles!!) { selectedProfile ->
                    val profileId = selectedProfile.id
                    val account: Account = loadFromProfileID(profileId)
                    updateAccountInfo(account, authResult, selectedProfile.name, profileId)
                    refresh(context, account)
                }
            }
        )
        TaskSystem.submitTask(task)
    }

    /**
     * 仅仅只是登录外置账号（使用账号密码登录）
     * JUST DO IT!!!
     * @return 登陆的任务对象
     */
    fun justLogin(context: Context, account: Account): Task {
        fun roleNotFound() { //未找到匹配的ID
            onFailed(ResponseException(context.getString(R.string.account_other_login_role_not_found)))
        }

        return login(
            context,
            onlyOneRole = { authResult, task ->
                if (authResult.selectedProfile!!.id != account.profileId) {
                    roleNotFound()
                    return@login
                }
                updateAccountInfo(account, authResult, authResult.selectedProfile!!.name, authResult.selectedProfile!!.id)
                onSuccess(account, task)
            },
            hasMultipleRoles = { authResult, task ->
                authResult.availableProfiles!!.forEach { profile ->
                    if (profile.id == account.profileId) {
                        //匹配当前账号的ID时，那么这个角色就是这个账号
                        updateAccountInfo(account, authResult, profile.name, profile.id)
                        onSuccess(account, task)
                        return@login
                    }
                }
                roleNotFound()
            },
            taskId = account.uniqueUUID,
            loggingString = account.username
        )
    }

    private fun refresh(context: Context, account: Account) {
        val task = Task.runTask(
            task = { task ->
                apiServer.refresh(context, account, true,
                    onSuccess = { authResult ->
                        account.accessToken = authResult.accessToken
                        onSuccess(account, task)
                    },
                    onFailed = onFailed
                )
            },
            onError = { e ->
                Logger.error(TAG, "An exception was encountered while performing the refresh task.", e)
                onFailed(e)
            }
        ).apply {
            updateMessage(
                androidText(R.string.account_other_login_select_role_logging, account.username)
            )
        }

        TaskSystem.submitTask(task)
    }
}