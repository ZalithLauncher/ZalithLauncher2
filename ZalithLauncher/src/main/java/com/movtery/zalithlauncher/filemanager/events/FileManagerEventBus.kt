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

package com.movtery.zalithlauncher.filemanager.events

import android.os.Messenger
import com.movtery.zalithlauncher.filemanager.os.FmLog
import java.util.concurrent.CopyOnWriteArrayList

/**
 * 文件管理器事件总线，维护已注册客户端并向其派发文件变更事件
 */
object FileManagerEventBus {
    private const val TAG = "FmEventBus"

    private val clients = CopyOnWriteArrayList<Messenger>()

    internal fun register(client: Messenger) {
        clients.addIfAbsent(client)
        FmLog.debug(TAG, "client registered: total=${clients.size}")
    }

    internal fun unregister(client: Messenger) {
        clients.remove(client)
        FmLog.debug(TAG, "client unregistered: total=${clients.size}")
    }

    /**
     * 向所有已注册的客户端派发一个文件变更事件
     * @param event 待派发的事件
     */
    fun dispatch(event: FileManagerEvent) {
        if (clients.isEmpty()) {
            FmLog.debug(TAG, "dispatch skipped (no listeners): type=${event.type}, dirs=${event.changedDirs}")
            return
        }
        FmLog.info(TAG, "dispatch: type=${event.type}, dirs=${event.changedDirs}")
        val dead = mutableListOf<Messenger>()
        clients.forEach { client ->
            val ok = runCatching { client.send(FileManagerEvent.toMessage(event)) }.isSuccess
            if (!ok) dead += client
        }
        if (dead.isNotEmpty()) {
            clients.removeAll(dead.toSet())
        }
    }
}