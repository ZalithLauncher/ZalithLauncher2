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

package com.movtery.zalithlauncher.ui.screens.game.multiplayer

import android.content.Context
import android.content.Intent
import android.net.VpnService
import android.widget.Toast
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.result.ActivityResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.context.COPY_LABEL_TERRACOTTA_INVITE_CODE
import com.movtery.zalithlauncher.context.COPY_LABEL_TERRACOTTA_SERVER_ADDRESS
import com.movtery.zalithlauncher.game.launch.handler.GameHandler
import com.movtery.zalithlauncher.terracotta.Terracotta
import com.movtery.zalithlauncher.terracotta.TerracottaState
import com.movtery.zalithlauncher.terracotta.TerracottaVPNService
import com.movtery.zalithlauncher.terracotta.profile.TerracottaProfile
import com.movtery.zalithlauncher.utils.copyText
import com.movtery.zalithlauncher.viewmodel.EventViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

class TerracottaViewModel(
    val gameHandler: GameHandler,
    val eventViewModel: EventViewModel,
    val getUserName: () -> String?
): ViewModel() {
    var operation by mutableStateOf<TerracottaOperation>(TerracottaOperation.None)

    /**
     * 联机菜单状态
     */
    var dialogState by mutableStateOf<TerracottaState.Ready?>(null)

    /**
     * 联机菜单日志展示状态
     */
    var dialogLogOperation by mutableStateOf<TerracottaLogOperation>(TerracottaLogOperation.None)
        private set

    /**
     * 陶瓦联机核心版本号，在初始化完成后非null
     */
    var terracottaVer by mutableStateOf<String?>(null)

    /**
     * EasyTier版本号，在初始化完成后非null
     */
    var easyTierVer by mutableStateOf<String?>(null)

    /**
     * VPN权限申请，由TerracottaOperation设置
     */
    var vpnLauncher: ManagedActivityResultLauncher<Intent, ActivityResult>? = null

    /**
     * 在等待状态页面中是否允许进行交互
     */
    var isWaitingInteractive by mutableStateOf(false)

    private val _profiles = MutableStateFlow<List<TerracottaProfile>>(emptyList())
    /**
     * 陶瓦联机当前房间的玩家列表
     */
    val profiles = _profiles.asStateFlow()

    private val allJobs: MutableList<Job> = mutableListOf()

    /**
     * 打开陶瓦联机菜单
     */
    fun openMenu() {
        if (operation !is TerracottaOperation.None) return

        if (!Terracotta.initialized) initialize()
        if (allJobs.isEmpty()) initJobs()

        operation = TerracottaOperation.ShowMenu
    }

    private val logMutex = Mutex()
    /**
     * 在联机菜单中显示当前核心的日志
     */
    fun showLog() {
        viewModelScope.launch(Dispatchers.IO) {
            val result = logMutex.withLock {
                if (dialogLogOperation is TerracottaLogOperation.CollectingLog) return@withLock null
                TerracottaLogOperation.CollectingLog
            }

            result ?: return@launch
            dialogLogOperation = result

            val finalState = Terracotta.collectLogs()?.let { logString ->
                TerracottaLogOperation.EnableLog(logString)
            } ?: TerracottaLogOperation.None //收集失败，回退到正常状态

            dialogLogOperation = finalState
        }
    }

    /**
     * 让联机菜单退出日志状态
     */
    fun hideLog() {
        dialogLogOperation = TerracottaLogOperation.None
    }

    /**
     * 复制房间邀请码到系统剪贴板
     */
    fun copyInviteCode(
        state: TerracottaState.HostOK
    ) {
        val code = state.code ?: return //理论上不会是null
        copyText(label = COPY_LABEL_TERRACOTTA_INVITE_CODE, text = code) {
            it.getString(R.string.terracotta_status_host_ok_code_copy_toast)
        }
    }

    /**
     * 复制房间备用链接到系统剪贴板
     */
    fun copyServerAddress(
        state: TerracottaState.GuestOK
    ) {
        val address = state.url ?: return
        copyText(label = COPY_LABEL_TERRACOTTA_SERVER_ADDRESS, text = address) {
            it.getString(R.string.terracotta_status_guest_ok_address_copy_toast)
        }
    }

    private fun copyText(
        label: String,
        text: String,
        toast: (Context) -> String
    ) {
        val context = gameHandler.activity
        copyText(
            label = label,
            text = text,
            context = context,
            showToast = false
        )
        Toast.makeText(context, toast(context), Toast.LENGTH_SHORT).show()
    }

    /**
     * 更新当前陶瓦联机的玩家列表
     */
    private fun updateProfiles(profiles: List<TerracottaProfile>?) {
        //在这里仅更新玩家列表，为避免频繁更新dialogState造成大面积重组
        _profiles.update {
            profiles ?: emptyList()
        }
    }

    /**
     * 初始化陶瓦联机
     */
    private fun initialize() {
        Terracotta.initialize(viewModelScope, eventViewModel)
        Terracotta.setWaiting(true)

        val metadata = Terracotta.getMetadata()
        terracottaVer = metadata.terracottaVersion
        easyTierVer = metadata.easyTierVersion
    }

    private fun initJobs() {
        val activity = gameHandler.activity

        val stateChangeJob = viewModelScope.launch {
            Terracotta.stateChanges.collect { (old, new) ->
                when (new) {
                    is TerracottaState.Waiting -> {
                        if (old !is TerracottaState.Waiting) {
                            //首次或再次进入等待大厅
                            isWaitingInteractive = true
                        }
                    }
                    is TerracottaState.HostOK -> {
                        if (old !is TerracottaState.HostOK) {
                            //刚切换到这个状态，默认复制一次邀请码
                            copyInviteCode(new)
                            //然后首次更新玩家列表状态
                            updateProfiles(new.profiles)
                        }
                        if (new.isForkOf(old)) {
                            updateProfiles(new.profiles)
                            return@collect
                        }
                    }
                    is TerracottaState.GuestOK -> {
                        if (old !is TerracottaState.GuestOK) {
                            updateProfiles(new.profiles)
                        }
                        if (new.isForkOf(old)) {
                            updateProfiles(new.profiles)
                            return@collect
                        }
                    }
                    else -> {
                        if (_profiles.value.isNotEmpty()) {
                            //当前已经不在房间内，所以需要清空所有玩家配置
                            updateProfiles(emptyList())
                        }
                    }
                }
                dialogState = new
            }
        }

        val eventJob = viewModelScope.launch {
            eventViewModel.events
                .filterIsInstance<EventViewModel.Event.Terracotta>()
                .collect { event ->
                    when (event) {
                        is EventViewModel.Event.Terracotta.RequestVPN -> {
                            withContext(Dispatchers.Main) {
                                val intent = VpnService.prepare(activity)
                                if (intent != null) {
                                    vpnLauncher?.launch(intent)
                                } else {
                                    val vpnIntent = Intent(activity, TerracottaVPNService::class.java)
                                        .setAction(TerracottaVPNService.ACTION_START)
                                    activity.startForegroundService(vpnIntent)
                                }
                            }
                        }
                        is EventViewModel.Event.Terracotta.VPNUpdateState -> {
                            withContext(Dispatchers.Main) {
                                val vpnIntent = Intent(activity, TerracottaVPNService::class.java)
                                    .setAction(TerracottaVPNService.ACTION_UPDATE_STATE)
                                    .putExtra(TerracottaVPNService.EXTRA_STATE_TEXT, event.stringRes)
                                activity.startForegroundService(vpnIntent)
                            }
                        }
                        is EventViewModel.Event.Terracotta.StopVPN -> {
                            withContext(Dispatchers.Main) {
                                val activity = gameHandler.activity
                                if (TerracottaVPNService.isRunning()) {
                                    //必须用 stopService 下发停止指令，避免 FGS 启动时限崩溃
                                    activity.stopService(
                                        Intent(activity, TerracottaVPNService::class.java)
                                    )
                                }
                            }
                        }
                    }
                }
        }

        allJobs.add(stateChangeJob)
        allJobs.add(eventJob)
    }

    override fun onCleared() {
        allJobs.forEach { it.cancel() }
        allJobs.clear()
    }
}

@Composable
fun rememberTerracottaViewModel(
    keyTag: String,
    gameHandler: GameHandler,
    eventViewModel: EventViewModel,
    getUserName: () -> String?
): TerracottaViewModel {
    return viewModel(
        key = keyTag
    ) {
        TerracottaViewModel(
            gameHandler = gameHandler,
            eventViewModel = eventViewModel,
            getUserName = getUserName
        )
    }
}