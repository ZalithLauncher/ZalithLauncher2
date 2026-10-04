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

package com.movtery.zalithlauncher.game.download.jvm_server

import com.movtery.zalithlauncher.utils.logging.Logger
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import java.io.IOException
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.SocketException
import java.net.UnknownHostException

private const val TAG = "JVMSocketServer"

/**
 * [Reference FCL](https://github.com/FCL-Team/FoldCraftLauncher/blob/main/FCLCore/src/main/java/com/tungsten/fclcore/util/SocketServer.java)
 */
object JVMSocketServer {
    private lateinit var ip: String
    private var port: Int = PROCESS_SERVICE_PORT

    private var scope: CoroutineScope? = null
    private var packet: DatagramPacket? = null
    private var socket: DatagramSocket? = null

    /**
     * 上一次接收的消息
     */
    var receiveMsg: String? = null
        private set

    fun start(
        ip: String = "127.0.0.1",
        port: Int = PROCESS_SERVICE_PORT,
        onReceive: suspend (receiveMsg: String) -> Unit
    ) {
        this.ip = ip
        this.port = port
        //清空上一轮的接收结果，防止陈旧退出码被当作本轮结果消费
        receiveMsg = null

        scope?.let {
            it.cancel()
            scope = null
        }
        scope = CoroutineScope(Dispatchers.Default)

        scope?.launch(Dispatchers.IO) {
            val bytes = ByteArray(1024)
            packet = DatagramPacket(bytes, bytes.size)
            try {
                socket = DatagramSocket(port, InetAddress.getByName(ip))
                Logger.info(TAG, "Socket server init!")
            } catch (e: SocketException) {
                Logger.error(TAG, "Failed to init socket server", e)
            } catch (e: UnknownHostException) {
                Logger.error(TAG, "Failed to init socket server", e)
            }

            startServer(onReceive)
        }
    }

    private fun startServer(
        onReceive: suspend (receiveMsg: String) -> Unit
    ) {
        scope?.launch(Dispatchers.IO) {
            if (packet == null || socket == null) {
                return@launch
            }
            Logger.info(TAG, "Socket server $ip:$port start!")

            while (true) {
                try {
                    ensureActive()
                    socket!!.receive(packet)
                    val receiveMsg = String(packet!!.data, packet!!.offset, packet!!.length)
                    Logger.info(TAG, "receive msg: $receiveMsg")
                    this@JVMSocketServer.receiveMsg = receiveMsg
                    onReceive(receiveMsg)
                } catch (e: Exception) {
                    if (e is CancellationException) return@launch
                    else {
                        Logger.warning(TAG, "Socket server $ip:$port crashed!", e)
                    }
                }
            }
        }
    }

    @Throws(IOException::class)
    fun send(msg: String) {
        socket!!.connect(InetSocketAddress(ip, port))
        val data = msg.toByteArray()
        val packet = DatagramPacket(data, data.size)
        socket!!.send(packet)
    }

    fun stop() {
        socket?.let {
            it.close()
            Logger.info(TAG, "Socket server $ip:$port stopped!")
        }
        scope?.cancel()
        scope = null
        socket = null
        packet = null
    }
}