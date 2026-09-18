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
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.coroutine.Task
import com.movtery.zalithlauncher.coroutine.TaskSystem
import com.movtery.zalithlauncher.database.AppDatabase
import com.movtery.zalithlauncher.game.account.auth_server.data.AuthServer
import com.movtery.zalithlauncher.game.account.auth_server.data.AuthServerDao
import com.movtery.zalithlauncher.path.PathManager
import com.movtery.zalithlauncher.setting.AllSettings
import com.movtery.zalithlauncher.ui.androidText
import com.movtery.zalithlauncher.utils.isInGreaterChina
import com.movtery.zalithlauncher.utils.logging.Logger
import com.movtery.zalithlauncher.utils.network.isNetworkAvailable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.apache.commons.io.FileUtils
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

private const val TAG = "AccountManager"

object AccountsManager {
    private val scope = CoroutineScope(Dispatchers.IO)

    //账号相关
    private val _accounts = CopyOnWriteArrayList<Account>()
    private val _accountsFlow = MutableStateFlow<List<Account>>(emptyList())
    val accountsFlow = _accountsFlow.asStateFlow()

    private val _currentAccountFlow = MutableStateFlow<Account?>(null)
    val currentAccountFlow = _currentAccountFlow.asStateFlow()

    //认证服务器
    private val _authServers = CopyOnWriteArrayList<AuthServer>()
    private val _authServersFlow = MutableStateFlow<List<AuthServer>>(emptyList())
    val authServersFlow = _authServersFlow.asStateFlow()

    private val _refreshWardrobe = MutableStateFlow(false)
    /** 控制刷新所有账号衣橱 */
    val refreshWardrobe = _refreshWardrobe.asStateFlow()

    private val _isOffline = MutableStateFlow(false)
    val isOffline = _isOffline

    //本次启动器会话内已通过服务端校验的账号
    private val sessionValidatedAccounts: MutableSet<String> = ConcurrentHashMap.newKeySet()

    private lateinit var database: AppDatabase
    private lateinit var accountDao: AccountDao
    private lateinit var authServerDao: AuthServerDao

    /**
     * 初始化整个账号系统
     */
    fun initialize(context: Context) {
        database = AppDatabase.getInstance(context)
        accountDao = database.accountDao()
        authServerDao = database.authServerDao()
    }

    /**
     * 刷新当前已登录的账号，已登录的账号保存在数据库中
     */
    fun reloadAccounts() {
        scope.launch {
            suspendReloadAccounts()
        }
    }

    /**
     * 刷新所有账号的衣橱
     */
    fun refreshWardrobe() {
        _refreshWardrobe.update { !it }
    }

    private suspend fun suspendReloadAccounts() {
        val loadedAccounts = accountDao.getAllAccounts()
        _accounts.clear()
        _accounts.addAll(loadedAccounts)

        _accounts.sortWith(compareBy<Account>(
            { it.accountTypePriority() },
            { it.username },
        ))
        _accountsFlow.value = _accounts.toList()

        if (_accounts.isNotEmpty() && !isAccountExists(AllSettings.currentAccount.getValue())) {
            setCurrentAccountInternal(_accounts[0])
        }

        refreshCurrentAccountState()

        Logger.info(TAG, "Loaded ${_accounts.size} accounts")
    }

    /**
     * 刷新当前已保存的认证服务器，认证服务器保存在数据库中
     */
    fun reloadAuthServers() {
        scope.launch {
            val loadedServers = authServerDao.getAllServers()
            _authServers.clear()
            _authServers.addAll(loadedServers)

            _authServers.sortWith { o1, o2 -> o1.serverName.compareTo(o2.serverName) }
            _authServersFlow.value = _authServers.toList()

            Logger.info(TAG, "Loaded ${_authServers.size} auth servers")
        }
    }

    /**
     * 执行登陆操作
     */
    fun performLogin(
        context: Context,
        account: Account,
        onSuccess: suspend (Account, task: Task) -> Unit = { _, _ -> },
        onFailed: (th: Throwable) -> Unit = {}
    ) {
        val task = performLoginTask(context, account, onSuccess, onFailed)
        task?.let { TaskSystem.submitTask(it) }
    }

    /**
     * 获取登陆操作的任务对象
     */
    fun performLoginTask(
        context: Context,
        account: Account,
        onSuccess: suspend (Account, task: Task) -> Unit = { _, _ -> },
        onFailed: (th: Throwable) -> Unit = {},
        onFinally: () -> Unit = {}
    ): Task? =
        when {
            account.isNoLoginRequired() -> null
            account.isAuthServerAccount() -> {
                otherLogin(context = context, account = account, onSuccess = onSuccess, onFailed = onFailed, onFinally = onFinally)
            }
            account.isMicrosoftAccount() -> {
                microsoftRefresh(account = account, onSuccess = onSuccess, onFailed = onFailed, onFinally = onFinally)
            }
            else -> null
        }

    /**
     * 刷新账号
     */
    fun refreshAccount(
        context: Context,
        account: Account,
        onFailed: (th: Throwable) -> Unit = {},
    ) {
        if (isNetworkAvailable(context)) {
            performLogin(
                context = context,
                account = account,
                onSuccess = { account, task ->
                    task.updateMessage(androidText(R.string.account_logging_in_saving))
                    account.downloadYggdrasil()
                    markSessionValidated(account)
                    suspendSaveAccount(account)
                },
                onFailed = onFailed
            )
        }
    }

    /**
     * 该账号在本次会话中是否已通过服务端校验
     */
    fun isSessionValidated(account: Account): Boolean =
        sessionValidatedAccounts.contains(account.uniqueUUID)

    fun markSessionValidated(account: Account) {
        sessionValidatedAccounts.add(account.uniqueUUID)
    }

    /**
     * 是否需要执行启动前的账号校验
     */
    fun isLaunchCheckNeeded(account: Account): Boolean = when {
        account.isNoLoginRequired() -> false
        account.isMicrosoftAccount() -> !isSessionValidated(account) ||
                System.currentTimeMillis() > account.expiresAt - 5 * 60 * 1000
        else -> !isSessionValidated(account)
    }

    /**
     * 获取当前已登录的账号
     */
    private fun getCurrentAccount(): Account? {
        return _accounts.find {
            it.uniqueUUID == AllSettings.currentAccount.getValue()
        } ?: _accounts.firstOrNull()
    }

    /**
     * 设置并保存当前账号
     */
    fun setCurrentAccount(account: Account) {
        setCurrentAccountInternal(account)
        refreshCurrentAccountState()
    }

    private fun setCurrentAccountInternal(account: Account) {
        AllSettings.currentAccount.save(account.uniqueUUID)
    }

    /**
     * 刷新当前账号，同时刷新非中国大陆地区的正版状态
     */
    private fun refreshCurrentAccountState() {
        val currentAccount = getCurrentAccount()
        val isOffline = checkLimit()
        _currentAccountFlow.update {
            //若处于非正版状态，不允许使用账号
            if (isOffline) null else currentAccount
        }
        _isOffline.update { isOffline }
    }

    private fun checkLimit(): Boolean {
        val circumventLimit = File(PathManager.DIR_FILES_EXTERNAL, "circumventLimit")
        return !circumventLimit.exists() && !isInGreaterChina() && !hasMicrosoftAccount()
    }

    /**
     * 保存账号到数据库
     */
    fun saveAccount(account: Account) {
        scope.launch {
            suspendSaveAccount(account)
        }
    }

    /**
     * 保存账号到数据库
     */
    suspend fun suspendSaveAccount(account: Account) {
        runCatching {
            accountDao.saveAccount(account)
            Logger.info(TAG, "Saved account: ${account.username}")
            //同时设置当前账号
            setCurrentAccountInternal(account)
        }.onFailure { e ->
            Logger.error(TAG, "Failed to save account: ${account.username}", e)
        }
        suspendReloadAccounts()
    }

    /**
     * 从数据库中删除账号，并刷新
     */
    fun deleteAccount(account: Account) {
        scope.launch {
            accountDao.deleteAccount(account)
            val skinFile = account.getSkinFile()
            FileUtils.deleteQuietly(skinFile)
            suspendReloadAccounts()
        }
    }

    /**
     * 保存认证服务器到数据库
     */
    suspend fun saveAuthServer(server: AuthServer) {
        runCatching {
            authServerDao.saveServer(server)
            Logger.info(TAG, "Saved auth server: ${server.serverName} -> ${server.baseUrl}")
        }.onFailure { e ->
            Logger.error(TAG, "Failed to save auth server: ${server.serverName}", e)
        }
        reloadAuthServers()
    }

    /**
     * 从数据库中删除认证服务器，并刷新
     */
    fun deleteAuthServer(server: AuthServer) {
        scope.launch {
            authServerDao.deleteServer(server)
            reloadAuthServers()
        }
    }

    /**
     * 是否已登录过微软账号
     */
    fun hasMicrosoftAccount(): Boolean = _accounts.any { it.isMicrosoftAccount() }

    /**
     * 通过账号的profileId读取账号
     */
    fun loadFromProfileID(
        profileId: String,
        accountType: String? = null
    ): Account? =
        _accounts.find { it.profileId == profileId && it.accountType == accountType }

    /**
     * 账号是否存在
     */
    fun isAccountExists(uniqueUUID: String): Boolean {
        return uniqueUUID.isNotEmpty() && _accounts.any { it.uniqueUUID == uniqueUUID }
    }

    /**
     * 认证服务器是否存在
     */
    fun isAuthServerExists(baseUrl: String): Boolean {
        return baseUrl.isNotEmpty() && _authServers.any { it.baseUrl == baseUrl }
    }
}