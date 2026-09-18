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

package com.movtery.zalithlauncher.game.support.touch_controller

import android.content.Context
import android.os.Vibrator
import android.system.Os
import com.movtery.zalithlauncher.BuildKeys
import com.movtery.zalithlauncher.bridge.LoggerBridge
import com.movtery.zalithlauncher.utils.logging.Logger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import top.fifthlight.touchcontroller.proxy.client.LauncherProxyClient
import top.fifthlight.touchcontroller.proxy.client.PlatformCapability
import top.fifthlight.touchcontroller.proxy.client.android.transport.UnixSocketTransport

private const val TAG = "ControllerProxy"

/**
 * 为适配 TouchController 模组
 * [Touch Controller](https://modrinth.com/mod/touchcontroller)
 */
object ControllerProxy {
    private val _proxyClient = MutableStateFlow<LauncherProxyClient?>(null)
    val proxyClient = _proxyClient.asStateFlow()

    /**
     * 启动控制代理客户端，目的是与 TouchController 模组进行通信
     */
    fun startProxy(
        context: Context,
        vibrateDuration: Int?,
        vibrateKind: VibrationHandler.VibrateKind?,
    ) {
        if (proxyClient.value == null) {
            try {
                val transport = UnixSocketTransport(BuildKeys.LAUNCHER_NAME)
                Os.setenv("TOUCH_CONTROLLER_PROXY_SOCKET", BuildKeys.LAUNCHER_NAME, true)
                val client = LauncherProxyClient(
                    transport = transport,
                    capabilities = setOf(PlatformCapability.TEXT_STATUS),
                )
                val vibrator = context.getSystemService(Vibrator::class.java)
                val handler = VibrationHandler(vibrator, vibrateDuration, vibrateKind)
                client.vibrationHandler = handler
                client.run()
                LoggerBridge.append("TouchController: TouchController Proxy Client has been created!")
                _proxyClient.value = client
            } catch (ex: Throwable) {
                Logger.warning(TAG, "TouchController proxy client create failed", ex)
            }
        }
    }
}