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

import android.app.Service
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Message
import android.os.Messenger
import com.movtery.zalithlauncher.filemanager.os.FmLog

private const val TAG = "FmEventService"

/**
 * 文件管理器事件服务，作为跨进程客户端与 [FileManagerEventBus] 之间的中转
 */
class FileManagerEventService : Service() {
    private val handler = Handler { msg ->
        when (msg.what) {
            FileManagerEvent.MSG_REGISTER_CLIENT -> msg.replyTo?.let {
                FileManagerEventBus.register(it)
                it.send(Message.obtain().apply { what = FileManagerEvent.MSG_REGISTER_CLIENT })
            }

            FileManagerEvent.MSG_UNREGISTER_CLIENT -> msg.replyTo?.let(FileManagerEventBus::unregister)
            else -> FmLog.warn(TAG, "Unknown message what=${msg.what}")
        }
        true
    }

    private val messenger = Messenger(handler)

    override fun onBind(intent: Intent?): IBinder? = messenger.binder

    companion object {
        const val ACTION_BIND = "com.movtery.zalithlauncher.filemanager.EVENT_SERVICE"
    }
}