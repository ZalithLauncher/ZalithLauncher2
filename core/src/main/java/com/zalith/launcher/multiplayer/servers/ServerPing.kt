package com.zalith.launcher.multiplayer.servers

import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.EOFException
import java.io.IOException
import java.net.InetSocketAddress
import java.net.Socket
import java.nio.charset.StandardCharsets
import java.util.concurrent.Executors

/**
 * Server List Ping — o protocolo binário de status do Minecraft Java.
 *
 * Conecta, manda o handshake (versão -1, próximo estado = status),
 * pede o status e lê o JSON: motd, jogadores, versão.
 * Implementação própria, sem bibliotecas externas.
 */
object ServerPing {

    private val executor = Executors.newCachedThreadPool { runnable ->
        Thread(runnable, "zl3-server-ping").apply { isDaemon = true }
    }

    /**
     * Ping sem travar a thread chamadora. O callback roda numa thread
     * de pool — a UI precisa voltar pra main thread antes de desenhar.
     */
    fun pingAsync(host: String, port: Int, timeoutMs: Int = 5000, callback: (ServerList.Status) -> Unit) {
        executor.execute {
            val status = try {
                ping(host, port, timeoutMs)
            } catch (e: Exception) {
                ServerList.Status(online = false)
            }
            callback(status)
        }
    }

    /**
     * Ping bloqueante. Nunca lança exceção: servidor fora do ar
     * devolve Status(online = false).
     */
    fun ping(host: String, port: Int, timeoutMs: Int = 5000): ServerList.Status {
        val started = System.currentTimeMillis()
        return try {
            val json = exchange(host, port, timeoutMs)
            val version = json.optJSONObject("version")
            val players = json.optJSONObject("players")
            ServerList.Status(
                online = true,
                motd = motd(json.opt("description")),
                playersOnline = players?.optInt("online", -1) ?: -1,
                playersMax = players?.optInt("max", -1) ?: -1,
                version = version?.optString("name"),
                protocol = version?.optInt("protocol", -1) ?: -1,
                latencyMs = System.currentTimeMillis() - started
            )
        } catch (e: Exception) {
            ServerList.Status(online = false)
        }
    }

    /** Abre o socket, manda handshake + pedido de status e devolve o JSON. */
    private fun exchange(host: String, port: Int, timeoutMs: Int): JSONObject {
        Socket().use { socket ->
            socket.tcpNoDelay = true
            socket.connect(InetSocketAddress(host, port), timeoutMs)
            socket.soTimeout = timeoutMs
            val out = DataOutputStream(socket.getOutputStream())
            val input = DataInputStream(socket.getInputStream())

            // handshake: id 0, protocolo -1 (desconhecido), host, porta, estado 1 (status)
            out.write(frame(packet {
                varInt(0x00)
                varInt(-1)
                string(host)
                uShort(port)
                varInt(1)
            }))
            // pedido de status: só o id 0
            out.write(frame(packet { varInt(0x00) }))
            out.flush()

            // resposta: tamanho total, id do pacote, tamanho do JSON, bytes do JSON
            readVarInt(input) // tamanho total (ignorado: lemos por partes)
            val packetId = readVarInt(input)
            if (packetId != 0x00) throw IOException("pacote inesperado: " + packetId)
            val jsonLen = readVarInt(input)
            if (jsonLen < 0 || jsonLen > 8 * 1024 * 1024) throw IOException("resposta inválida/gigante")
            val jsonBytes = ByteArray(jsonLen)
            input.readFully(jsonBytes)
            return JSONObject(String(jsonBytes, StandardCharsets.UTF_8))
        }
    }

    /** Monta pacotes do protocolo: VarInt, String, unsigned short. */
    private class Packet {
        private val buf = ByteArrayOutputStream()

        fun varInt(value: Int): Packet {
            var v = value
            while (true) {
                if ((v and 0xFFFFFF80.inv()) == 0) {
                    buf.write(v)
                    return this
                }
                buf.write((v and 0x7F) or 0x80)
                v = v ushr 7
            }
        }

        fun string(s: String): Packet {
            val bytes = s.toByteArray(StandardCharsets.UTF_8)
            varInt(bytes.size)
            buf.write(bytes)
            return this
        }

        fun uShort(value: Int): Packet {
            buf.write((value ushr 8) and 0xFF)
            buf.write(value and 0xFF)
            return this
        }

        fun bytes(): ByteArray = buf.toByteArray()
    }

    private fun packet(block: Packet.() -> Unit): ByteArray =
        Packet().apply(block).bytes()

    /** Prefixa o payload com seu tamanho (VarInt), como o protocolo exige. */
    private fun frame(payload: ByteArray): ByteArray {
        val header = Packet().varInt(payload.size).bytes()
        return header + payload
    }

    private fun readVarInt(input: DataInputStream): Int {
        var result = 0
        var count = 0
        while (true) {
            val b = input.read()
            if (b < 0) throw EOFException("conexão fechou no meio do pacote")
            result = result or ((b and 0x7F) shl (7 * count))
            if ((b and 0x80) == 0) return result
            count++
            if (count > 5) throw IOException("VarInt maior que 5 bytes")
        }
    }

    /** A motd pode ser texto puro ou componente ("text" + "extra"). */
    private fun motd(description: Any?): String = when (description) {
        is String -> description
        is JSONObject -> {
            val sb = StringBuilder(description.optString("text", ""))
            val extra = description.optJSONArray("extra")
            if (extra != null) {
                for (i in 0 until extra.length()) sb.append(motd(extra.opt(i)))
            }
            sb.toString()
        }
        else -> ""
    }
}
