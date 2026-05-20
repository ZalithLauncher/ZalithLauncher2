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

package com.movtery.zalithlauncher.viewmodel

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.context.copyLocalFile
import com.movtery.zalithlauncher.coroutine.Task
import com.movtery.zalithlauncher.coroutine.TaskSystem
import com.movtery.zalithlauncher.game.account.Account
import com.movtery.zalithlauncher.game.account.AccountsManager
import com.movtery.zalithlauncher.game.account.addOtherServer
import com.movtery.zalithlauncher.game.account.auth_server.AuthServerHelper
import com.movtery.zalithlauncher.game.account.auth_server.ResponseException
import com.movtery.zalithlauncher.game.account.auth_server.data.AuthServer
import com.movtery.zalithlauncher.game.account.isLocalAccount
import com.movtery.zalithlauncher.game.account.isMicrosoftAccount
import com.movtery.zalithlauncher.game.account.localLogin
import com.movtery.zalithlauncher.game.account.microsoft.MINECRAFT_SERVICES_URL
import com.movtery.zalithlauncher.game.account.microsoft.MinecraftProfileException
import com.movtery.zalithlauncher.game.account.microsoft.NotPurchasedMinecraftException
import com.movtery.zalithlauncher.game.account.microsoft.XboxLoginException
import com.movtery.zalithlauncher.game.account.microsoft.toLocal
import com.movtery.zalithlauncher.game.account.microsoftLogin
import com.movtery.zalithlauncher.game.account.refreshMicrosoft
import com.movtery.zalithlauncher.game.account.wardrobe.EmptyCape
import com.movtery.zalithlauncher.game.account.wardrobe.SkinModelType
import com.movtery.zalithlauncher.game.account.wardrobe.capeLocalRes
import com.movtery.zalithlauncher.game.account.wardrobe.getLocalUUIDWithSkinModel
import com.movtery.zalithlauncher.game.account.wardrobe.isSlimModel
import com.movtery.zalithlauncher.game.account.wardrobe.validateSkinFile
import com.movtery.zalithlauncher.game.account.yggdrasil.PlayerProfile
import com.movtery.zalithlauncher.game.account.yggdrasil.cacheAllCapes
import com.movtery.zalithlauncher.game.account.yggdrasil.changeCape
import com.movtery.zalithlauncher.game.account.yggdrasil.executeWithAuthorization
import com.movtery.zalithlauncher.game.account.yggdrasil.getFile
import com.movtery.zalithlauncher.game.account.yggdrasil.getPlayerProfile
import com.movtery.zalithlauncher.game.account.yggdrasil.uploadSkin
import com.movtery.zalithlauncher.path.PathManager
import com.movtery.zalithlauncher.ui.screens.content.elements.AccountOperation
import com.movtery.zalithlauncher.ui.screens.content.elements.AccountSkinOperation
import com.movtery.zalithlauncher.ui.screens.content.elements.ChangeCape
import com.movtery.zalithlauncher.ui.screens.content.elements.ChangeSkin
import com.movtery.zalithlauncher.ui.screens.content.elements.LocalLoginOperation
import com.movtery.zalithlauncher.ui.screens.content.elements.LoginMenuOperation
import com.movtery.zalithlauncher.ui.screens.content.elements.MicrosoftLoginOperation
import com.movtery.zalithlauncher.ui.screens.content.elements.OtherLoginOperation
import com.movtery.zalithlauncher.ui.screens.content.elements.ServerOperation
import com.movtery.zalithlauncher.utils.logging.Logger.lError
import com.movtery.zalithlauncher.utils.network.safeBodyAsJson
import com.movtery.zalithlauncher.utils.string.getMessageOrToString
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import org.apache.commons.io.FileUtils
import java.io.File
import java.net.ConnectException
import java.net.UnknownHostException
import java.nio.channels.UnresolvedAddressException
import java.util.UUID
import javax.inject.Inject
import io.ktor.client.plugins.ResponseException as KtorResponseException
import kotlinx.coroutines.flow.combine as kotlinxCombine

/**
 * 账号管理界面用户意图 (MVI Intent)
 * 封装了 UI 层发出的所有操作请求
 */
sealed interface AccountManageIntent {
    /** 呼出账号登录菜单 */
    data class UpdateLoginMenuOp(val operation: LoginMenuOperation) : AccountManageIntent

    data class UpdateMicrosoftLoginOp(val operation: MicrosoftLoginOperation) :
        AccountManageIntent

    data class UpdateLocalLoginOp(val operation: LocalLoginOperation) : AccountManageIntent
    data class UpdateOtherLoginOp(val operation: OtherLoginOperation) : AccountManageIntent
    data class UpdateServerOp(val operation: ServerOperation) : AccountManageIntent
    data class UpdateAccountOp(val operation: AccountOperation) : AccountManageIntent
    data class UpdateAccountSkinOp(val operation: AccountSkinOperation) :
        AccountManageIntent
    data class UpdatePendingSkinData(val skinState: ChangeSkin) :
        AccountManageIntent
    data class UpdatePendingCapeData(val capeState: ChangeCape) :
        AccountManageIntent
    data class OnSkinPicked(val uri: Uri) : AccountManageIntent
    data object ResetAccountSkinDialogState : AccountManageIntent


    /** 执行微软登录流程 */
    data class PerformMicrosoftLogin(
        val toWeb: (url: String) -> Unit,
        val backToMain: () -> Unit,
        val checkIfInWebScreen: () -> Boolean
    ) : AccountManageIntent

    /** 应用选中的皮肤 */
    data class ApplySkin(val account: Account, val file: File, val model: SkinModelType) : AccountManageIntent

    /** 内部使用的 Intent，用于在文件导入后上传皮肤 */
    data class UploadMicrosoftSkin(
        val account: Account,
        val skinFile: File,
        val skinModel: SkinModelType
    ) : AccountManageIntent

    /** 抓取该账号可用的微软披风列表 */
    data class FetchMicrosoftCapes(
        val account: Account,
    ) : AccountManageIntent

    /** 应用选中的微软披风 */
    data class ApplyMicrosoftCape(
        val account: Account,
        val cape: PlayerProfile.Cape
    ) : AccountManageIntent

    /** 创建新的离线账号 */
    data class CreateLocalAccount(val userName: String, val userUUID: String?) :
        AccountManageIntent

    /** 使用第三方验证服务器进行登录 */
    data class LoginWithOtherServer(
        val server: AuthServer,
        val email: String,
        val pass: String
    ) : AccountManageIntent

    /** 添加新的 Yggdrasil 验证服务器 */
    data class AddServer(val url: String) : AccountManageIntent

    /** 删除指定的验证服务器 */
    data class DeleteServer(val server: AuthServer) : AccountManageIntent

    /** 删除账号及其相关数据 */
    data class DeleteAccount(val account: Account) : AccountManageIntent

    /** 刷新账号的登录凭据（Token） */
    data class RefreshAccount(val account: Account) : AccountManageIntent

    /** 将账号皮肤重置为默认状态 */
    data class ResetSkin(val account: Account) : AccountManageIntent
}

/**
 * 账号管理界面单次副作用 (MVI Effect)
 * 用于处理 Toast、错误弹窗或 UI 通知等瞬时事件
 */
sealed class AccountManageEffect {
    /** 在 UI 层显示错误信息对话框 */
    data class ShowError(val title: String, val message: String) : AccountManageEffect()

    /** 在 UI 层显示 Toast 提示 */
    data class ShowToast(
        val messageRes: Int,
        val formatArgs: List<Any> = emptyList(),
        val duration: Int = Toast.LENGTH_SHORT
    ) : AccountManageEffect()
}

/**
 * 账号管理界面 ViewModel
 * 
 * 核心逻辑处理器，负责将 Intent 转化为状态更新或副作用。
 * 通过 ApplicationContext 避免了 Activity 生命周期导致的内存泄漏。
 * 
 * @property context 全局应用上下文
 */
@HiltViewModel
class AccountManageViewModel @Inject constructor(
    @param:ApplicationContext private val context: Context
) : ViewModel() {
    private val _loginMenuOp = MutableStateFlow<LoginMenuOperation>(LoginMenuOperation.None)

    private val _microsoftLoginOp =
        MutableStateFlow<MicrosoftLoginOperation>(MicrosoftLoginOperation.None)
    private val _localLoginOp = MutableStateFlow<LocalLoginOperation>(LocalLoginOperation.None)
    private val _otherLoginOp = MutableStateFlow<OtherLoginOperation>(OtherLoginOperation.None)
    private val _serverOp = MutableStateFlow<ServerOperation>(ServerOperation.None)
    private val _accountOp = MutableStateFlow<AccountOperation>(AccountOperation.None)
    private val _accountSkinOp = MutableStateFlow<AccountSkinOperation>(AccountSkinOperation.None)
    private val _accountSkinDialogState = MutableStateFlow(AccountSkinDialogState())
    private val _accountCapeOpMap = MutableStateFlow<Map<String, List<PlayerProfile.Cape>>>(emptyMap())

    private val _effect = Channel<AccountManageEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    /**
     * 登录相关操作状态流统一管理
     */
    val loginUiState: StateFlow<LoginUiState> = kotlinxCombine(
        _loginMenuOp,
        _microsoftLoginOp,
        _localLoginOp,
        _otherLoginOp
    ) { loginMenuOp, microsoftLoginOp, localLoginOp, otherLoginOp ->
        LoginUiState(
            menuOp = loginMenuOp,
            microsoftOp = microsoftLoginOp,
            localOp = localLoginOp,
            otherOp = otherLoginOp
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = LoginUiState()
    )

    data class LoginUiState(
        val menuOp: LoginMenuOperation = LoginMenuOperation.None,
        val microsoftOp: MicrosoftLoginOperation = MicrosoftLoginOperation.None,
        val localOp: LocalLoginOperation = LocalLoginOperation.None,
        val otherOp: OtherLoginOperation = OtherLoginOperation.None
    )

    /**
     * 账号数据状态流统一管理
     */
    val profileUiState: StateFlow<ProfileUiState> = kotlinxCombine(
        AccountsManager.accountsFlow,
        AccountsManager.currentAccountFlow,
        AccountsManager.authServersFlow,
        _accountCapeOpMap,
        AccountsManager.isOffline
    ) { accounts, currentAccount, authServers, accountCapeOpMap, isOffline ->
        ProfileUiState(
            accounts = accounts,
            currentAccount = currentAccount,
            authServers = authServers,
            accountCapeOpMap = accountCapeOpMap,
            isOffline = isOffline
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ProfileUiState()
    )

    data class ProfileUiState(
        val accounts: List<Account> = emptyList(),
        val currentAccount: Account? = null,
        val authServers: List<AuthServer> = emptyList(),
        val accountCapeOpMap: Map<String, List<PlayerProfile.Cape>> = emptyMap(),
        val isOffline: Boolean = false
    )

    /**
     * 更改账号皮肤状态流
     * @param pendingSkinData 将要更改的皮肤
     * @param pendingCapeData 将要更改的披风
     * @param importingSkin 是否正在导入皮肤文件，不交给onIntent处理
     */
    data class AccountSkinDialogState(
        val pendingSkinData: ChangeSkin = ChangeSkin.None,
        val pendingCapeData: ChangeCape = ChangeCape.None,
        val importingSkin: Boolean = false
    )

    /**
     * 数据相关操作状态流统一管理
     */
    val operationUiState: StateFlow<OperationUiState> = kotlinxCombine(
        _serverOp,
        _accountOp,
        _accountSkinOp,
        _accountSkinDialogState
    ) { serverOp, accountOp, accountSkinOp, accountSkinDialogState ->
        OperationUiState(serverOp, accountOp, accountSkinOp, accountSkinDialogState)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = OperationUiState()
    )

    data class OperationUiState(
        val serverOp: ServerOperation = ServerOperation.None,
        val accountOp: AccountOperation = AccountOperation.None,
        val accountSkinOp: AccountSkinOperation = AccountSkinOperation.None,
        val accountSkinDialogState: AccountSkinDialogState = AccountSkinDialogState()
    )

    /**
     * 处理来自 UI 层的所有 Intent
     */
    fun onIntent(intent: AccountManageIntent) {
        when (intent) {
            is AccountManageIntent.UpdateLoginMenuOp ->
                _loginMenuOp.value = intent.operation

            is AccountManageIntent.UpdateMicrosoftLoginOp ->
                _microsoftLoginOp.value = intent.operation

            is AccountManageIntent.UpdateLocalLoginOp -> _localLoginOp.value = intent.operation
            is AccountManageIntent.UpdateOtherLoginOp -> _otherLoginOp.value = intent.operation
            is AccountManageIntent.UpdateServerOp -> _serverOp.value = intent.operation
            is AccountManageIntent.UpdateAccountOp -> _accountOp.value = intent.operation
            is AccountManageIntent.UpdateAccountSkinOp -> {
                _accountSkinOp.value = intent.operation
            }

            is AccountManageIntent.UpdatePendingSkinData -> {
                _accountSkinDialogState.update {
                    it.copy(
                        pendingSkinData = intent.skinState
                    )
                }
            }

            is AccountManageIntent.UpdatePendingCapeData -> {
                _accountSkinDialogState.update {
                    it.copy(
                        pendingCapeData = intent.capeState
                    )
                }
            }

            is AccountManageIntent.OnSkinPicked -> onSkinPicked(intent)
            is AccountManageIntent.ResetAccountSkinDialogState -> {
                _accountSkinDialogState.update { AccountSkinDialogState() }
            }

            is AccountManageIntent.PerformMicrosoftLogin -> performMicrosoftLogin(intent)
            is AccountManageIntent.ApplySkin ->
                applySkin(intent.account, intent.file, intent.model)

            is AccountManageIntent.UploadMicrosoftSkin -> uploadMicrosoftSkin(intent)
            is AccountManageIntent.FetchMicrosoftCapes -> fetchMicrosoftCapes(intent.account)
            is AccountManageIntent.ApplyMicrosoftCape -> applyMicrosoftCape(intent)
            is AccountManageIntent.CreateLocalAccount -> createLocalAccount(
                intent.userName,
                intent.userUUID
            )

            is AccountManageIntent.LoginWithOtherServer -> loginWithOtherServer(intent)
            is AccountManageIntent.AddServer -> addServer(intent.url)
            is AccountManageIntent.DeleteServer -> deleteServer(intent.server)
            is AccountManageIntent.DeleteAccount -> deleteAccount(intent.account)
            is AccountManageIntent.RefreshAccount -> refreshAccount(intent.account)
            is AccountManageIntent.ResetSkin -> resetSkin(intent.account)
        }
    }

    /**
     * 选中皮肤后，先在 VM 层做文件合法性校验，再推进后续 Dialog 流程状态
     */
    private fun onSkinPicked(intent: AccountManageIntent.OnSkinPicked) {
        viewModelScope.launch(Dispatchers.IO) {
            _accountSkinDialogState.update {
                it.copy(importingSkin = true)
            }

            val cacheFile = File(
                PathManager.DIR_IMAGE_CACHE,
                "skin_pick_${UUID.randomUUID()}"
            )

            runCatching {
                context.copyLocalFile(intent.uri, cacheFile)
                validateSkinFile(cacheFile)
            }.onSuccess { isValid ->
                if (!isValid) {
                    emitError(
                        context.getString(R.string.generic_warning),
                        context.getString(R.string.account_change_skin_invalid)
                    )
                    return@onSuccess
                }
                val recommendedModel = if (cacheFile.isSlimModel()) {
                    SkinModelType.ALEX
                } else {
                    SkinModelType.STEVE
                }

                _accountSkinDialogState.update {
                    it.copy(
                        pendingSkinData = ChangeSkin.ChangeSkinData(
                            cacheFile = cacheFile,
                            skinModel = recommendedModel
                        )
                    )
                }
            }.onFailure { th ->
                emitError(
                    context.getString(R.string.generic_error),
                    context.getString(R.string.account_change_skin_failed_to_import) + "\r\n" + th.getMessageOrToString()
                )
            }

            _accountSkinDialogState.update {
                it.copy(importingSkin = false)
            }
        }
    }

    /** 内部方法：发送错误通知 */
    private fun emitError(title: String, message: String) {
        viewModelScope.launch(Dispatchers.Main) {
            _effect.send(AccountManageEffect.ShowError(title, message))
        }
    }

    /** 内部方法：发送 Toast 消息 */
    private fun emitToast(
        messageRes: Int,
        vararg args: Any,
        duration: Int = Toast.LENGTH_SHORT
    ) {
        viewModelScope.launch(Dispatchers.Main) {
            _effect.send(AccountManageEffect.ShowToast(messageRes, args.toList(), duration))
        }
    }

    /** 执行微软登录流程 */
    private fun performMicrosoftLogin(intent: AccountManageIntent.PerformMicrosoftLogin) {
        microsoftLogin(
            context,
            intent.toWeb,
            intent.backToMain,
            intent.checkIfInWebScreen,
            { onIntent(AccountManageIntent.UpdateMicrosoftLoginOp(it)) },
            { emitError(it.title, it.message) }
        )
        onIntent(AccountManageIntent.UpdateMicrosoftLoginOp(MicrosoftLoginOperation.None))
    }

    /** 应用选中的皮肤 */
    private fun applySkin(account: Account, file: File, model: SkinModelType) {
        when {
            account.isLocalAccount() -> saveLocalSkin(account, file, model)
            account.isMicrosoftAccount() -> importSkinFile(account, file, model)
        }
    }

    /** 处理皮肤文件导入 */
    private fun importSkinFile(account: Account, file: File, model: SkinModelType) {
        TaskSystem.submitTask(
            Task.runTask(
                id = account.uniqueUUID,
                dispatcher = Dispatchers.IO,
                task = {
                    if (validateSkinFile(file)) {
                        onIntent(
                            AccountManageIntent.UploadMicrosoftSkin(
                                account = account,
                                skinFile = file,
                                skinModel = model
                            )
                        )
                    } else {
                        FileUtils.deleteQuietly(file)
                        emitError(
                            context.getString(R.string.generic_warning),
                            context.getString(R.string.account_change_skin_invalid)
                        )
                    }
                },
                onError = { th ->
                    FileUtils.deleteQuietly(file)
                    emitError(
                        context.getString(R.string.generic_error),
                        context.getString(R.string.account_change_skin_failed_to_import) + "\r\n" + th.getMessageOrToString()
                    )
                }
            )
        )
    }

    /** 上传微软皮肤 */
    private fun uploadMicrosoftSkin(intent: AccountManageIntent.UploadMicrosoftSkin) {
        val account = intent.account
        val skinFile = intent.skinFile
        val skinModel = intent.skinModel

        TaskSystem.submitTask(
            Task.runTask(
                dispatcher = Dispatchers.IO,
                task = { task ->
                    executeWithAuthorization(block = {
                        task.updateProgress(-1f, R.string.account_change_skin_uploading)
                        uploadSkin(MINECRAFT_SERVICES_URL, account.accessToken, skinFile, skinModel)
                    }, onRefreshRequest = {
                        account.refreshMicrosoft(task = task, coroutineContext = coroutineContext)
                        AccountsManager.suspendSaveAccount(account)
                    })

                    task.updateMessage(R.string.account_change_skin_update_local)
                    runCatching { account.downloadYggdrasil() }.onFailure { th ->
                        emitError(
                            context.getString(R.string.account_logging_in_failed),
                            formatAccountError(th)
                        )
                    }

                    emitToast(
                        R.string.account_change_skin_update_toast,
                        duration = Toast.LENGTH_LONG
                    )
                },
                onError = { th ->
                    val (title, msg) = if (th is KtorResponseException) {
                        val body = th.response.safeBodyAsJson<JsonObject>()
                        context.getString(
                            R.string.account_change_skin_failed_to_upload,
                            th.response.status.value
                        ) to (body["errorMessage"]?.jsonPrimitive?.contentOrNull
                            ?: th.getMessageOrToString())
                    } else {
                        context.getString(R.string.generic_error) to formatAccountError(th)
                    }
                    emitError(title, msg)
                }
            )
        )
    }

    /** 获取微软披风列表 */
    private fun fetchMicrosoftCapes(account: Account) {
        TaskSystem.submitTask(
            Task.runTask(
                id = account.uniqueUUID,
                dispatcher = Dispatchers.IO,
                task = { task ->
                    executeWithAuthorization(block = {
                        task.updateProgress(-1f, R.string.account_change_cape_fetch_all)
                        val profile = getPlayerProfile(MINECRAFT_SERVICES_URL, account.accessToken)
                        task.updateProgress(-1f, R.string.account_change_cape_cache_all)
                        cacheAllCapes(profile)
                        //同时更新本地的皮肤/披风
                        account.downloadYggdrasil()
                        _accountCapeOpMap.update { it + (account.uniqueUUID to profile.capes) }
                    }, onRefreshRequest = {
                        account.refreshMicrosoft(task = task, coroutineContext = coroutineContext)
                        AccountsManager.suspendSaveAccount(account)
                    })
                },
                onError = { th ->
                    emitError(
                        context.getString(R.string.generic_error),
                        context.getString(R.string.account_change_cape_fetch_all_failed) + "\r\n" + th.getMessageOrToString()
                    )
                }
            )
        )
    }

    /** 更改微软账号披风 */
    private fun applyMicrosoftCape(intent: AccountManageIntent.ApplyMicrosoftCape) {
        val account = intent.account
        val cape = intent.cape
        val capeId = cape.id
        val isReset = cape == EmptyCape

        TaskSystem.submitTask(
            Task.runTask(
                id = account.uniqueUUID + "_cape",
                dispatcher = Dispatchers.IO,
                task = { task ->
                    executeWithAuthorization(block = {
                        task.updateMessage(R.string.account_change_cape_apply)
                        changeCape(
                            MINECRAFT_SERVICES_URL,
                            account.accessToken,
                            capeId
                        )
                    }, onRefreshRequest = {
                        account.refreshMicrosoft(task = task, coroutineContext = coroutineContext)
                        AccountsManager.suspendSaveAccount(account)
                    })

                    val capeFile = cape.getFile(PathManager.DIR_ACCOUNT_CAPE)
                    val targetCape = account.getCapeFile()
                    FileUtils.deleteQuietly(targetCape)
                    if (!isReset && capeFile.exists()) {
                        runCatching {
                            capeFile.copyTo(targetCape)
                        }
                    }

                    AccountsManager.refreshWardrobe()

                    _accountCapeOpMap.update { capesMap ->
                        if (!capesMap.containsKey(account.uniqueUUID)) return@update capesMap
                        buildMap {
                            capesMap.forEach { (accountId, capes) ->
                                if (accountId == account.uniqueUUID) {
                                    val newList = capes.map { cape ->
                                        when {
                                            cape.id == capeId -> cape.copy(state = "ACTIVE")
                                            cape.state == "ACTIVE" -> cape.copy(state = "INACTIVE")
                                            else -> cape
                                        }
                                    }
                                    put(accountId, newList)
                                } else put(accountId, capes)
                            }
                        }
                    }

                    if (isReset) emitToast(R.string.account_change_cape_apply_reset)
                    else emitToast(
                        R.string.account_change_cape_apply_success,
                        cape.capeLocalRes()?.let {
                            context.getString(it)
                        } ?: cape.alias
                    )
                },
                onError = { th ->
                    val (title, msg) = if (th is KtorResponseException) {
                        val body = th.response.safeBodyAsJson<JsonObject>()
                        context.getString(
                            R.string.account_change_cape_apply_failed,
                            th.response.status.value
                        ) to (body["errorMessage"]?.jsonPrimitive?.contentOrNull
                            ?: th.getMessageOrToString())
                    } else {
                        context.getString(R.string.generic_error) to formatAccountError(th)
                    }
                    emitError(title, msg)
                }
            )
        )
    }

    /** 创建离线账号 */
    private fun createLocalAccount(userName: String, userUUID: String?) {
        localLogin(userName, userUUID)
        onIntent(AccountManageIntent.UpdateLocalLoginOp(LocalLoginOperation.None))
    }

    /** 第三方 Yggdrasil 服务器登录 */
    private fun loginWithOtherServer(intent: AccountManageIntent.LoginWithOtherServer) {
        AuthServerHelper(intent.server, intent.email, intent.pass, onSuccess = { account, task ->
            task.updateMessage(R.string.account_logging_in_saving)
            account.downloadYggdrasil()
            AccountsManager.suspendSaveAccount(account)
        }, onFailed = {
            onIntent(AccountManageIntent.UpdateOtherLoginOp(OtherLoginOperation.OnFailed(it)))
        }).createNewAccount(context) { profiles, select ->
            onIntent(
                AccountManageIntent.UpdateOtherLoginOp(
                    OtherLoginOperation.SelectRole(profiles, select)
                )
            )
        }
    }

    /** 添加自定义验证服务器 */
    private fun addServer(url: String) {
        addOtherServer(url) {
            onIntent(AccountManageIntent.UpdateServerOp(ServerOperation.OnThrowable(it)))
        }
        onIntent(AccountManageIntent.UpdateServerOp(ServerOperation.None))
    }

    private fun deleteServer(server: AuthServer) {
        AccountsManager.deleteAuthServer(server)
        onIntent(AccountManageIntent.UpdateServerOp(ServerOperation.None))
    }

    private fun deleteAccount(account: Account) {
        AccountsManager.deleteAccount(account)
        onIntent(AccountManageIntent.UpdateAccountOp(AccountOperation.None))
    }

    /** 强制刷新账号凭据 */
    private fun refreshAccount(account: Account) {
        AccountsManager.refreshAccount(context, account) {
            onIntent(AccountManageIntent.UpdateAccountOp(AccountOperation.OnFailed(it)))
        }
    }

    /** 保存离线账号皮肤到本地存储 */
    private fun saveLocalSkin(account: Account, file: File, model: SkinModelType) {
        val skinFile = account.getSkinFile()

        TaskSystem.submitTask(Task.runTask(dispatcher = Dispatchers.IO, task = {
            if (validateSkinFile(file)) {
                account.skinModelType = model
                account.profileId = getLocalUUIDWithSkinModel(account.username, model)
                file.copyTo(skinFile, true)
                FileUtils.deleteQuietly(file)
                AccountsManager.suspendSaveAccount(account)
                AccountsManager.refreshWardrobe()
                onIntent(
                    AccountManageIntent.UpdateAccountSkinOp(
                        AccountSkinOperation.None
                    )
                )
            } else {
                emitError(
                    context.getString(R.string.generic_warning),
                    context.getString(R.string.account_change_skin_invalid)
                )
                onIntent(
                    AccountManageIntent.UpdateAccountSkinOp(
                        AccountSkinOperation.None
                    )
                )
            }
        }, onError = { th ->
            FileUtils.deleteQuietly(file)
            emitError(context.getString(R.string.error_import_image), th.getMessageOrToString())
            AccountsManager.refreshWardrobe()
            onIntent(
                AccountManageIntent.UpdateAccountSkinOp(
                    AccountSkinOperation.None
                )
            )
        }))
    }

    /** 重置皮肤数据 */
    private fun resetSkin(account: Account) {
        TaskSystem.submitTask(Task.runTask(dispatcher = Dispatchers.IO, task = {
            account.apply {
                FileUtils.deleteQuietly(getSkinFile())
                skinModelType = SkinModelType.NONE
                profileId = getLocalUUIDWithSkinModel(username, skinModelType)
                AccountsManager.suspendSaveAccount(this)
                AccountsManager.refreshWardrobe()
            }
        }))
        onIntent(
            AccountManageIntent.UpdateAccountSkinOp(
                AccountSkinOperation.None
            )
        )
    }

    /**
     * 将多种异常类型统一转化为用户可读的本地化字符串。
     *
     * @param th 捕获的异常
     * @return 格式化后的错误提示
     */
    fun formatAccountError(th: Throwable): String = when (th) {
        is NotPurchasedMinecraftException -> toLocal(context)
        is MinecraftProfileException -> th.toLocal(context)
        is XboxLoginException -> th.toLocal(context)
        is HttpRequestTimeoutException -> context.getString(R.string.error_timeout)
        is UnknownHostException, is UnresolvedAddressException -> context.getString(R.string.error_network_unreachable)
        is ConnectException -> context.getString(R.string.error_connection_failed)
        is KtorResponseException -> {
            val res = when (th.response.status) {
                HttpStatusCode.Unauthorized -> R.string.error_unauthorized
                HttpStatusCode.NotFound -> R.string.error_notfound
                else -> R.string.error_client_error
            }
            context.getString(res, th.response.status.value)
        }

        is ResponseException -> th.responseMessage
        else -> {
            lError("An unknown exception was caught!", th)
            val errorMessage =
                th.localizedMessage ?: th.message ?: th::class.qualifiedName ?: "Unknown error"
            context.getString(R.string.error_unknown, errorMessage)
        }
    }
}
