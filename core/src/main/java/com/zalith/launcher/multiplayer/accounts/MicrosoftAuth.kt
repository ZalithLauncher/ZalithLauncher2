package com.zalith.launcher.multiplayer.accounts

import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

/**
 * Autenticação Microsoft completa para Minecraft Java.
 *
 * Fluxo (device code — funciona sem navegador embutido):
 *  1. requestDeviceCode()  -> código que o usuário digita em microsoft.com/link
 *  2. awaitApproval()      -> poll até aprovar (ou refresh() com token salvo)
 *  3. loginWithXbox()      -> XBL -> XSTS -> login com Xbox -> perfil
 *
 * Zero dependências: HttpURLConnection + org.json (nativo do Android).
 * Todas as chamadas são bloqueantes — rodar fora da main thread.
 */
object MicrosoftAuth {

    /**
     * Registre um "public client" em https://portal.azure.com
     * (App registrations, habilite "Allow public client flows")
     * e preencha o ID (GUID) aqui na inicialização do app.
     */
    var clientId: String = "PREENCHER-CLIENT-ID-AQUI"

    private const val SCOPE = "XboxLive.signin offline_access"
    private const val DEVICE_ENDPOINT = "https://login.microsoftonline.com/consumers/oauth2/v2.0/devicecode"
    private const val TOKEN_ENDPOINT = "https://login.microsoftonline.com/consumers/oauth2/v2.0/token"
    private const val XBL_ENDPOINT = "https://user.auth.xboxlive.com/user/authenticate"
    private const val XSTS_ENDPOINT = "https://xsts.auth.xboxlive.com/xsts/authorize"
    private const val MC_LOGIN_ENDPOINT = "https://api.minecraftservices.com/authentication/login_with_xbox"
    private const val MC_PROFILE_ENDPOINT = "https://api.minecraftservices.com/minecraft/profile"

    class AuthException(message: String) : Exception(message)

    class DeviceCode(
        val userCode: String,
        val verificationUri: String,
        val deviceCode: String,
        val expiresInSeconds: Long,
        val pollIntervalSeconds: Long
    )

    class MsToken(
        val accessToken: String,
        val refreshToken: String,
        val expiresInSeconds: Long
    )

    class McAuth(
        val accessToken: String,
        val expiresInSeconds: Long,
        val username: String,
        val uuid: String,
        val xuid: String?,
        val skinUrl: String?
    )

    /** Etapa 1: pede o código que o usuário digita no navegador. */
    fun requestDeviceCode(): DeviceCode {
        val json = httpForm(DEVICE_ENDPOINT, form(
            "client_id" to clientId,
            "scope" to SCOPE
        ), tolerateError = true)
        if (json.has("error")) {
            throw AuthException("device code falhou: " + json.optString("error_description", json.optString("error")))
        }
        return DeviceCode(
            userCode = json.getString("user_code"),
            verificationUri = json.getString("verification_uri"),
            deviceCode = json.getString("device_code"),
            expiresInSeconds = json.optLong("expires_in", 900L),
            pollIntervalSeconds = json.optLong("interval", 5L)
        )
    }

    /** Etapa 2: bloqueante. onStillPending recebe (userCode, segundos restantes). */
    fun awaitApproval(device: DeviceCode, onStillPending: (String, Long) -> Unit = { _, _ -> }): MsToken {
        val deadline = System.currentTimeMillis() + device.expiresInSeconds * 1000L
        val intervalMs = device.pollIntervalSeconds * 1000L
        while (System.currentTimeMillis() < deadline) {
            Thread.sleep(intervalMs)
            val json = httpForm(TOKEN_ENDPOINT, form(
                "grant_type" to "urn:ietf:params:oauth:grant-type:device_code",
                "client_id" to clientId,
                "device_code" to device.deviceCode
            ), tolerateError = true)
            if (json.has("access_token")) {
                return MsToken(
                    json.getString("access_token"),
                    json.optString("refresh_token", ""),
                    json.optLong("expires_in", 0L)
                )
            }
            when (json.optString("error")) {
                "authorization_pending" -> onStillPending(device.userCode, (deadline - System.currentTimeMillis()) / 1000L)
                "slow_down" -> Thread.sleep(intervalMs)
                "authorization_declined" -> throw AuthException("login recusado no navegador")
                "expired_token" -> throw AuthException("código expirou — peça outro")
                "" -> Unit
                else -> throw AuthException("oauth: " + json.optString("error"))
            }
        }
        throw AuthException("tempo esgotado aguardando a aprovação")
    }

    /** Renova o token de uma sessão salva. Bloqueante. */
    fun refresh(refreshToken: String): MsToken {
        val json = httpForm(TOKEN_ENDPOINT, form(
            "grant_type" to "refresh_token",
            "client_id" to clientId,
            "refresh_token" to refreshToken,
            "scope" to SCOPE
        ))
        if (!json.has("access_token")) throw AuthException("refresh token inválido — faça login de novo")
        return MsToken(
            json.getString("access_token"),
            json.optString("refresh_token", refreshToken),
            json.optLong("expires_in", 0L)
        )
    }

    /** Etapa 3: XBL -> XSTS -> login do Minecraft -> perfil. Bloqueante. */
    fun loginWithXbox(msAccessToken: String): McAuth {
        val xblJson = httpJson(XBL_ENDPOINT, JSONObject()
            .put("Properties", JSONObject()
                .put("AuthMethod", "RPS")
                .put("SiteName", "user.auth.xboxlive.com")
                .put("RpsTicket", "d=" + msAccessToken))
            .put("RelyingParty", "http://auth.xboxlive.com")
            .put("TokenType", "JWT"))
        val xblToken = xblJson.getString("Token")
        val uhs = xblJson.getJSONObject("DisplayClaims")
            .getJSONArray("xui").getJSONObject(0).getString("uhs")

        val xstsJson = httpJson(XSTS_ENDPOINT, JSONObject()
            .put("Properties", JSONObject()
                .put("SandboxId", "RETAIL")
                .put("UserTokens", JSONArray().put(xblToken)))
            .put("RelyingParty", "rp://api.minecraftservices.com/")
            .put("TokenType", "JWT"), tolerateError = true)
        if (xstsJson.has("XErr")) {
            throw AuthException(xstsMessage(xstsJson.getLong("XErr")))
        }
        val xstsToken = xstsJson.getString("Token")
        val xuid = xstsJson.optJSONObject("DisplayClaims")
            ?.optJSONArray("xui")?.optJSONObject(0)?.optString("xid")?.takeIf { it.isNotEmpty() }

        val mcJson = httpJson(MC_LOGIN_ENDPOINT, JSONObject()
            .put("identityToken", "XBL3.0 x=" + uhs + ";" + xstsToken), tolerateError = true)
        if (!mcJson.has("access_token")) {
            throw AuthException("esta conta Microsoft não tem Minecraft Java")
        }

        val profile = httpGetJson(MC_PROFILE_ENDPOINT, "Bearer " + mcJson.getString("access_token"), tolerateError = true)
        if (!profile.has("id")) {
            throw AuthException("perfil do Minecraft indisponível — tenta de novo")
        }
        return McAuth(
            accessToken = mcJson.getString("access_token"),
            expiresInSeconds = mcJson.optLong("expires_in", 0L),
            username = profile.getString("name"),
            uuid = dashed(profile.getString("id")),
            xuid = xuid,
            skinUrl = activeSkinUrl(profile)
        )
    }

    private fun xstsMessage(xerr: Long): String = when (xerr) {
        2148916233L -> "a conta Microsoft não tem perfil Xbox"
        2148916235L -> "região/conta não permitida no Xbox"
        2148916236L, 2148916237L -> "conta infantil: precisa de um adulto na família"
        2148916238L -> "conta infantil: verificação pendente"
        else -> "XSTS recusou o login (XErr " + xerr + ")"
    }

    private fun activeSkinUrl(profile: JSONObject): String? {
        val skins = profile.optJSONArray("skins") ?: return null
        for (i in 0 until skins.length()) {
            val skin = skins.optJSONObject(i) ?: continue
            if (skin.optString("state") == "ACTIVE") {
                return skin.optString("url").takeIf { it.isNotEmpty() }
            }
        }
        return null
    }

    private fun dashed(raw: String): String {
        val id = raw.replace("-", "")
        if (id.length != 32) return raw
        return id.substring(0, 8) + "-" + id.substring(8, 12) + "-" +
                id.substring(12, 16) + "-" + id.substring(16, 20) + "-" + id.substring(20, 32)
    }

    private fun form(vararg pairs: Pair<String, String>): String =
        pairs.joinToString("&") { it.first + "=" + url(it.second) }

    private fun url(s: String) = URLEncoder.encode(s, "UTF-8")

    private fun httpForm(endpoint: String, formBody: String, tolerateError: Boolean = false): JSONObject =
        post(endpoint, "application/x-www-form-urlencoded", formBody.toByteArray(Charsets.UTF_8), tolerateError)

    private fun httpJson(endpoint: String, payload: JSONObject, tolerateError: Boolean = false): JSONObject =
        post(endpoint, "application/json", payload.toString().toByteArray(Charsets.UTF_8), tolerateError)

    private fun httpGetJson(endpoint: String, authorization: String, tolerateError: Boolean = false): JSONObject {
        val conn = URL(endpoint).openConnection() as HttpURLConnection
        conn.requestMethod = "GET"
        conn.connectTimeout = 15000
        conn.readTimeout = 15000
        conn.setRequestProperty("Accept", "application/json")
        conn.setRequestProperty("Authorization", authorization)
        return readJson(conn, tolerateError)
    }

    private fun post(endpoint: String, contentType: String, body: ByteArray, tolerateError: Boolean): JSONObject {
        val conn = URL(endpoint).openConnection() as HttpURLConnection
        conn.requestMethod = "POST"
        conn.doOutput = true
        conn.connectTimeout = 15000
        conn.readTimeout = 15000
        conn.setRequestProperty("Content-Type", contentType)
        conn.setRequestProperty("Accept", "application/json")
        conn.outputStream.use { it.write(body) }
        return readJson(conn, tolerateError)
    }

    private fun readJson(conn: HttpURLConnection, tolerateError: Boolean): JSONObject {
        val code = conn.responseCode
        val stream = if (code in 200..299) conn.inputStream else conn.errorStream
        val text = try {
            stream?.bufferedReader()?.use { it.readText() } ?: ""
        } finally {
            conn.disconnect()
        }
        if (text.isBlank()) {
            if (tolerateError || code in 200..299) return JSONObject()
            throw AuthException("HTTP " + code + " sem corpo")
        }
        return try {
            JSONObject(text)
        } catch (e: Exception) {
            if (tolerateError) JSONObject() else throw AuthException("resposta não-JSON (HTTP " + code + ")")
        }
    }
}
