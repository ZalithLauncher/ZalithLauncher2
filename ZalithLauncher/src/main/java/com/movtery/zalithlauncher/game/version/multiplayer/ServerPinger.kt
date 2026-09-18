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

package com.movtery.zalithlauncher.game.version.multiplayer

import io.ktor.network.selector.SelectorManager
import io.ktor.network.sockets.aSocket
import io.ktor.network.sockets.openReadChannel
import io.ktor.network.sockets.openWriteChannel
import io.ktor.utils.io.readLong
import io.ktor.utils.io.writeFully
import io.ktor.utils.io.writeLong
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout

data class ServerPingResult(
    val rawJson: String,
    val pingMs: Long,
    val status: StatusResponse
)

private const val HANDSHAKE_PACKET_ID = 0x00
private const val STATUS_REQUEST_PACKET_ID = 0x00
private const val STATUS_RESPONSE_PACKET_ID = 0x00
private const val PING_REQUEST_PACKET_ID = 0x01
private const val PONG_RESPONSE_PACKET_ID = 0x01
private const val STATUS_INTENT = 1

suspend fun pingServer(
    resolved: ResolvedServerAddress,
    protocolVersion: Int = 760,
    timeoutMillis: Long = 15000
): ServerPingResult = withContext(Dispatchers.IO) {
    withTimeout(timeoutMillis) {
        SelectorManager(Dispatchers.IO).use { selector ->
            aSocket(selector)
                .tcp()
                .connect(resolved.hostName, resolved.port)
                .use { socket ->
                    val input = socket.openReadChannel()
                    val output = socket.openWriteChannel(autoFlush = true)

                    // ---------- Handshake ----------
                    val handshakeBytes = buildPacket {
                        writeVarInt(HANDSHAKE_PACKET_ID)
                        writeVarInt(protocolVersion)
                        writeMCString(resolved.hostName)
                        writeShort(resolved.port.toShort())
                        writeVarInt(STATUS_INTENT)
                    }
                    output.writeVarInt(handshakeBytes.size)
                    output.writeFully(handshakeBytes)

                    // ---------- Status Request ----------
                    output.writeVarInt(1) // Packet length
                    output.writeVarInt(STATUS_REQUEST_PACKET_ID)

                    // ---------- Status Response ----------
                    input.readVarInt()          // packet length
                    val responsePacketId = input.readVarInt()
                    require(responsePacketId == STATUS_RESPONSE_PACKET_ID) { "Invalid status packet id: $responsePacketId" }

                    val json = input.readMCString()

                    // ---------- Ping / Pong ----------
                    val pingTime = System.currentTimeMillis()
                    output.writeVarInt(9) // Packet Length (1 byte for ID + 8 bytes for long)
                    output.writeVarInt(PING_REQUEST_PACKET_ID)
                    output.writeLong(pingTime)

                    // ---------- Pong Response ----------
                    input.readVarInt() // packet length
                    val pongPacketId = input.readVarInt()
                    require(pongPacketId == PONG_RESPONSE_PACKET_ID) { "Invalid pong packet id: $pongPacketId" }
                    val pongTime = input.readLong()
                    require(pongTime == pingTime) { "Invalid pong payload" }

                    val status = StatusResponse.parse(json)
                    ServerPingResult(json, System.currentTimeMillis() - pingTime, status)
                }
        }
    }
}
