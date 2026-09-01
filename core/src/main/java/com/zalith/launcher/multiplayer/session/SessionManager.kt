package com.zalith.launcher.multiplayer.session

import com.zalith.launcher.multiplayer.accounts.Account
import com.zalith.launcher.multiplayer.accounts.AccountRepository
import com.zalith.launcher.multiplayer.accounts.MicrosoftAuth
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

/**
 * Sessão de jogo: garante token válido antes de conectar
 * e faz o join no servidor quando ele roda em online-mode.
 */
class SessionManager(private val repository: AccountRepository) {

    var active: Account? = null
        private set

    /**
     * Prepara a sessão da conta ativa. Offline já está pronto;
     * Microsoft renova o token se estiver perto de expirar.
     * Bloqueante (rede) — fora da main thread.
     */
    @Synchronized
    fun startSession(): Account {
        val account = repository.active()
            ?: throw IllegalStateException("nenhuma conta cadastrada")
        val ready = if (account is Account.Microsoft && account.isTokenExpired) {
            val ms = MicrosoftAuth.refresh(account.refreshToken)
            val mc = MicrosoftAuth.loginWithXbox(ms.accessToken)
            val renewed = account.copy(
                accessToken = mc.accessToken,
                refreshToken = ms.refreshToken,
                tokenExpiresAt = System.currentTimeMillis() + mc.expiresInSeconds * 1000L,
                skinUrl = mc.skinUrl ?: account.skinUrl
            )
            repository.update(renewed)
            renewed
        } else {
            account
        }
        active = ready
        return ready
    }

    /** Troca de perfil e prepara a sessão na hora. */
    @Synchronized
    fun switchAccount(uuid: String): Account {
        if (!repository.setActive(uuid)) {
            throw IllegalArgumentException("conta não encontrada: " + uuid)
        }
        return startSession()
    }

    /**
     * Join em servidor online-mode: o servidor desafia e o launcher
     * comprova a identidade no sessionserver da Mojang.
     * Conta offline não tem o que comprovar — no-op.
     */
    fun joinServer(account: Account, serverId: String) {
        if (account !is Account.Microsoft) return
        val body = "accessToken=" + url(account.accessToken) +
                "&selectedProfile=" + url(account.uuid.replace("-", "")) +
                "&serverId=" + url(serverId)
        val conn = URL("https://sessionserver.mojang.com/session/minecraft/join")
            .openConnection() as HttpURLConnection
        conn.requestMethod = "POST"
        conn.doOutput = true
        conn.connectTimeout = 15000
        conn.readTimeout = 15000
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
        conn.outputStream.use { it.write(body.toByteArray(Charsets.UTF_8)) }
        val code = conn.responseCode
        conn.disconnect()
        if (code != 204) throw IllegalStateException("join recusado (HTTP " + code + ")")
    }

    private fun url(s: String) = URLEncoder.encode(s, "UTF-8")
}
