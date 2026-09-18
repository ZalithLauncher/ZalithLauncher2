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

import android.app.Activity
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.terracotta.Terracotta
import com.movtery.zalithlauncher.terracotta.TerracottaVPNService
import com.movtery.zalithlauncher.terracotta.fetchNodes
import com.movtery.zalithlauncher.ui.AndroidStringText
import com.movtery.zalithlauncher.ui.androidText
import com.movtery.zalithlauncher.utils.logging.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.burningtnt.terracotta.TerracottaAndroidAPI

private const val TAG = "TerracottaOperation"

/**
 * 陶瓦联机状态操作
 */
sealed interface TerracottaOperation {
    data object None: TerracottaOperation
    /** 打开陶瓦联机菜单 */
    data object ShowMenu: TerracottaOperation
}

/**
 * 陶瓦联机状态操作
 */
@Composable
fun TerracottaOperation(
    viewModel: TerracottaViewModel,
    onShowToast: (AndroidStringText, Int) -> Unit = { _, _ -> }
) {
    val scope = rememberCoroutineScope()

    val vpnLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val context = viewModel.gameHandler.activity
        if (result.resultCode == Activity.RESULT_OK) {
            val vpnIntent = Intent(context, TerracottaVPNService::class.java).apply {
                action = TerracottaVPNService.ACTION_START
            }
            ContextCompat.startForegroundService(context, vpnIntent)
        } else {
            TerracottaAndroidAPI.getPendingVpnServiceRequest().reject()
            Terracotta.setWaiting(true)
            onShowToast(androidText(R.string.terracotta_permission_vpn), Toast.LENGTH_SHORT)
        }
    }

    DisposableEffect(Unit) {
        viewModel.vpnLauncher = vpnLauncher
        onDispose {
            viewModel.vpnLauncher = null
        }
    }

    when (viewModel.operation) {
        is TerracottaOperation.None -> {}
        is TerracottaOperation.ShowMenu -> {
            val anonymousString = stringResource(R.string.terracotta_player_anonymous)

            val userName: String = remember(viewModel) {
                viewModel.getUserName()
            } ?: anonymousString //未设置，使用“匿名玩家”

            //支持任何房间，实时展示所有玩家配置
            val profiles by viewModel.profiles.collectAsStateWithLifecycle()

            MultiplayerDialog(
                onClose = { viewModel.operation = TerracottaOperation.None },
                dialogState = viewModel.dialogState,
                logOperation = viewModel.dialogLogOperation,
                onShowLog = { viewModel.showLog() },
                onHideLog = { viewModel.hideLog() },
                isWaitingInteractive = viewModel.isWaitingInteractive,
                terracottaVer = viewModel.terracottaVer,
                easyTierVer = viewModel.easyTierVer,
                profiles = profiles,
                onHostRoleClick = {
                    scope.launch(Dispatchers.Main) {
                        viewModel.isWaitingInteractive = false

                        val nodes = fetchNodes()
                        val nodeList = withContext(Dispatchers.Default) {
                            nodes.map { node ->
                                node.toString()
                            }
                        }

                        runCatching {
                            Terracotta.setScanning(null, userName, nodeList)
                            viewModel.isWaitingInteractive = false
                        }.onFailure { e ->
                            Logger.warning(TAG, "Error occurred at \"Terracotta.setScanning(null, userName)\", message = ${e.message}")
                            viewModel.isWaitingInteractive = true
                        }
                    }
                },
                onHostCopyCode = { state ->
                    viewModel.copyInviteCode(state)
                },
                onGuestPositive = { roomCode ->
                    scope.launch(Dispatchers.Main) {
                        viewModel.isWaitingInteractive = false

                        val nodes = fetchNodes()
                        val nodeList = withContext(Dispatchers.Default) {
                            nodes.map { node ->
                                node.toString()
                            }
                        }

                        runCatching {
                            val success = Terracotta.setGuesting(roomCode, userName, nodeList)
                            viewModel.isWaitingInteractive = false
                            if (!success) {
                                onShowToast(androidText(R.string.terracotta_status_waiting_guest_prompt_invalid), Toast.LENGTH_SHORT)
                            }
                        }.onFailure { e ->
                            Logger.warning(TAG, "Error occurred at \"Terracotta.setGuesting(roomCode, userName)\", message = ${e.message}")
                            viewModel.isWaitingInteractive = true
                        }
                    }
                },
                onGuestCopyUrl = { state ->
                    viewModel.copyServerAddress(state)
                },
                onBack = {
                    Terracotta.setWaiting(true)
                },
                onShowToast = { text -> onShowToast(androidText(text), Toast.LENGTH_SHORT) }
            )
        }
    }
}