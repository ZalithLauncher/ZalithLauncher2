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

import android.net.Uri
import android.view.KeyEvent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.movtery.zalithlauncher.game.version.installed.Version
import com.movtery.zalithlauncher.ui.control.input.TextInputMode
import com.movtery.zalithlauncher.ui.screens.main.custom_home.MarkdownBlock
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import java.io.File

class EventViewModel : ViewModel() {
    private val _events = MutableSharedFlow<Event>(extraBufferCapacity = 1)
    val events = _events.asSharedFlow()

    /**
     * 发送一个事件
     */
    fun sendEvent(event: Event) {
        viewModelScope.launch {
            _events.emit(event)
        }
    }

    sealed interface Event {
        sealed interface Key : Event {
            /** 让MainActivity开始按键捕获 */
            data object StartKeyCapture : Key
            /** 让MainActivity停止按键捕获 */
            data object StopKeyCapture : Key
            /** 由MainActivity发送的按键捕获结果 */
            data class OnKeyDown(val key: KeyEvent) : Key
        }
        sealed interface Game : Event {
            /** 禁用/启用VMActivity按键处理 */
            data class KeyHandle(val handle: Boolean): Game
            /** 呼出IME */
            data class SwitchIme(val mode: TextInputMode?) : Game
            /** 刷新游戏画面分辨率 */
            data object RefreshSize : Game
            /** 用户按下系统返回键 */
            data object OnBack : Game
            /** [com.movtery.zalithlauncher.game.launch.handler.AbstractHandler.onResume] */
            data object OnResume: Game
        }
        sealed interface Terracotta : Event {
            /** 申请 VPN 权限 */
            data object RequestVPN : Terracotta
            /** 更新 VPN 状态文本 */
            data class VPNUpdateState(val stringRes: Int): Terracotta
            /** 关停 VPN */
            data object StopVPN : Terracotta
        }
        /** 启动游戏相关的事件 */
        sealed interface Launch : Event {
            /** 主菜单的启动游戏 */
            data class Game(val version: Version?) : Launch
            /** 快速启动游戏并进入服务器 */
            data class PlayServer(val version: Version, val address: String): Launch
            /** 快速启动游戏并进入存档 */
            data class PlaySave(val version: Version, val saveName: String): Launch
        }
        /** 检查更新 */
        data object CheckUpdate : Event
        /** 在浏览器访问链接 */
        data class OpenLink(val url: String) : Event
        /** 让 MainActivity 防止熄屏 */
        data class KeepScreen(val on: Boolean) : Event
        /** 导入控制布局 */
        data class ImportControls(val uris: List<Uri>) : Event
        /** 打开下载插件的链接窗口 */
        data class DownloadPlugins(val link: Links): Event {
            data class Links(
                val github: String,
                val cloudDrives: List<CloudDrive> = emptyList()
            )
            /**
             * 网盘链接，按语言区分
             * @param language 语言标识
             * @param link 网盘链接
             */
            data class CloudDrive(
                val language: String,
                val link: String
            )
        }
        /** 分享游戏日志 */
        sealed interface LogShare : Event {
            data class ShareGameLog(val logFile: File) : LogShare
        }
        /** 启动器主页相关 */
        sealed interface HomePage: Event {
            /** 重载启动器主页 */
            data object Reload: HomePage
            /** 生成文档主页 */
            data object GenDocPage: HomePage
            /** 主页触发的事件 */
            data class Event(val event: MarkdownBlock.Button.Event): HomePage
        }
        /** 设备 Vulkan 检查 */
        data object VulkanCheck: Event
    }
}

fun EventViewModel.sendKeepScreen(
    on: Boolean
) {
    sendEvent(EventViewModel.Event.KeepScreen(on))
}

fun EventViewModel.sendDLPlugin(
    githubLink: String,
    cloudDrives: List<EventViewModel.Event.DownloadPlugins.CloudDrive> = emptyList()
) {
    sendEvent(
        EventViewModel.Event.DownloadPlugins(
            link = EventViewModel.Event.DownloadPlugins.Links(
                github = githubLink,
                cloudDrives = cloudDrives
            )
        )
    )
}