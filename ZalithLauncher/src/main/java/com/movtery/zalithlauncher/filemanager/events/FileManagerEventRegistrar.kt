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

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Handler
import android.os.IBinder
import android.os.Message
import android.os.Messenger
import com.movtery.zalithlauncher.filemanager.os.FmLog
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import java.util.concurrent.CopyOnWriteArraySet

private const val TAG = "FmEventRegistrar"

/**
 * 文件管理器事件注册器，负责绑定事件服务并接收派发的文件变更事件
 */
class FileManagerEventRegistrar(
    private val context: Context,
    private val onEvent: ((FileManagerEvent) -> Unit)? = null
) {
    private val incoming = Handler { msg ->
        when (msg.what) {
            FileManagerEvent.MSG_REGISTER_CLIENT -> {
                FmLog.info(TAG, "registered at remote service.")
                _connected = true
                emitConnected()
            }

            FileManagerEvent.MSG_EVENT -> {
                runCatching {
                    FileManagerEvent.fromMessage(msg)?.let { event ->
                        onEvent?.invoke(event)
                        _events.tryEmit(event)
                    }
                }.onFailure { e ->
                    FmLog.warn(TAG, "Failed to handle incoming file manager event", e)
                }
            }

            else -> Unit
        }
        true
    }

    private val incomingMessenger = Messenger(incoming)

    private var _service: Messenger? = null
    private var _connected = false
    private var _started = false

    private val listeners = CopyOnWriteArraySet<(Boolean) -> Unit>()

    private val _events = MutableSharedFlow<FileManagerEvent>(extraBufferCapacity = 64)
    /**
     * 接收到的文件管理器事件流
     */
    val events: SharedFlow<FileManagerEvent> = _events.asSharedFlow()

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            _service = Messenger(service)
            registerAtRemote()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            _service = null
            _connected = false
            emitConnected()
        }

        override fun onBindingDied(name: ComponentName?) {
            _service = null
            _connected = false
            emitConnected()
        }
    }

    /**
     * 启动监听，绑定事件服务并注册客户端
     */
    fun start() {
        if (_started) return
        val intent = Intent(FileManagerEventService.ACTION_BIND).apply {
            setClassName(context, FileManagerEventService::class.java.name)
        }
        val ok = runCatching {
            context.bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }.getOrDefault(false)

        _started = ok
        FmLog.info(TAG, "start: bindService result=$ok")
    }

    /**
     * 停止监听，注销客户端并解绑事件服务
     */
    fun stop() {
        if (!_started) return
        _started = false
        runCatching { unregisterAtRemote() }
        runCatching { context.unbindService(connection) }
        _service = null
        _connected = false
        emitConnected()
    }

    fun addOnConnectionState(listener: (Boolean) -> Unit) {
        listeners += listener
        listener(_connected)
    }

    fun removeOnConnectionState(listener: (Boolean) -> Unit) {
        listeners -= listener
    }

    private fun emitConnected() {
        listeners.forEach { runCatching { it(_connected) } }
    }

    private fun registerAtRemote() {
        val msg = Message.obtain().apply {
            what = FileManagerEvent.MSG_REGISTER_CLIENT
            replyTo = incomingMessenger
        }
        runCatching { _service?.send(msg) }
    }

    private fun unregisterAtRemote() {
        val msg = Message.obtain().apply {
            what = FileManagerEvent.MSG_UNREGISTER_CLIENT
            replyTo = incomingMessenger
        }
        runCatching { _service?.send(msg) }
    }
}