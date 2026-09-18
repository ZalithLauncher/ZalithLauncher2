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

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.movtery.zalithlauncher.game.launch.GameLaunchFlow
import com.movtery.zalithlauncher.game.version.installed.Version
import com.movtery.zalithlauncher.game.version.installed.VersionsManager
import com.movtery.zalithlauncher.ui.screens.content.elements.LaunchGameOperation
import com.movtery.zalithlauncher.ui.screens.content.elements.QuickPlay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class LaunchGameViewModel : ViewModel() {
    private val _launchFlow = MutableStateFlow<GameLaunchFlow?>(null)
    /**
     * 游戏启动流程
     */
    val launchFlow = _launchFlow.asStateFlow()

    private val _launchGameOperation = MutableStateFlow<LaunchGameOperation>(LaunchGameOperation.None)
    /**
     * 启动游戏操作状态
     */
    val launchGameOperation = _launchGameOperation.asStateFlow()

    fun start(
        activity: Activity,
        version: Version,
        exitActivity: () -> Unit,
        submitError: (ErrorViewModel.ThrowableMessage) -> Unit,
        quickPlay: QuickPlay?,
        skipAccountRefresh: Boolean
    ) {
        _launchFlow.update {
            GameLaunchFlow(viewModelScope).also {
                it.launch(
                    context = activity,
                    version = version,
                    exitActivity = exitActivity,
                    submitError = submitError,
                    onReloginRequired = { account ->
                        activity.runOnUiThread {
                            updateOperation(
                                LaunchGameOperation.AccountRelogin(account, version, quickPlay)
                            )
                        }
                        cancel()
                    },
                    onRefreshFailed = { account, th ->
                        activity.runOnUiThread {
                            updateOperation(
                                LaunchGameOperation.AccountRefreshFailed(account, th, version, quickPlay)
                            )
                        }
                        cancel()
                    },
                    onComplete = {
                        _launchFlow.update { null }
                    },
                    skipAccountRefresh = skipAccountRefresh
                )
            }
        }
    }

    fun cancel() {
        _launchFlow.value?.cancel()
        _launchFlow.update { null }
    }

    /**
     * 尝试启动游戏
     */
    fun tryLaunch(
        version: Version? = null
    ) {
        if (launchGameOperation.value == LaunchGameOperation.None && _launchFlow.value == null) {
            updateOperation(
                LaunchGameOperation.TryLaunch(
                    version ?: VersionsManager.currentVersion.value
                )
            )
        }
    }

    /**
     * 快速启动（通过存档管理快速游玩存档）
     * @param saveName 存档文件名称
     */
    fun quickPlaySave(
        version: Version,
        saveName: String
    ) {
        if (launchGameOperation.value == LaunchGameOperation.None && _launchFlow.value == null) {
            updateOperation(
                LaunchGameOperation.TryLaunch(
                    version = version,
                    quickPlay = QuickPlay.Save(saveName),
                )
            )
        }
    }

    /**
     * 尝试启动游戏快速游玩服务器
     * @param address 服务器地址
     */
    fun tryPlayServer(address: String) {
        val version = VersionsManager.currentVersion.value ?: return
        quickPlayServer(version, address)
    }

    /**
     * 通过服务器列表快速游玩服务器
     * @param address 服务器地址
     */
    fun quickPlayServer(
        version: Version,
        address: String
    ) {
        if (launchGameOperation.value == LaunchGameOperation.None && _launchFlow.value == null) {
            updateOperation(
                LaunchGameOperation.TryLaunch(
                    version = version,
                    quickPlay = QuickPlay.Server(address),
                )
            )
        }
    }

    fun updateOperation(operation: LaunchGameOperation) {
        this._launchGameOperation.update { operation }
    }
}