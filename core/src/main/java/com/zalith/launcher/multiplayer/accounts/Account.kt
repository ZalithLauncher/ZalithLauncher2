package com.zalith.launcher.multiplayer.accounts

import org.json.JSONArray
import org.json.JSONObject

/**
 * Modelo unificado de conta do launcher.
 */
sealed class Account {

    abstract val type: String
    abstract val username: String
    abstract val uuid: String

    /** Conta offline: só precisa de nome. UUID derivado, sem autenticação. */
    data class Offline(
        override val username: String
    ) : Account() {
        override val type = TYPE
        override val uuid: String = OfflineUuid.fromUsername(username).toString()

        companion object { const val TYPE = "offline" }
    }

    /** Conta Microsoft: tokens + perfil do jogador. */
    data class Microsoft(
        override val username: String,
        override val uuid: String,
        val accessToken: String,
        val refreshToken: String,
        val tokenExpiresAt: Long, // epoch millis
        val xuid: String? = null,
        val skinUrl: String? = null
    ) : Account() {
        override val type = TYPE

        val isTokenExpired: Boolean
            get() = System.currentTimeMillis() >= tokenExpiresAt - 60_000L

        companion object { const val TYPE = "microsoft" }
    }

    fun toJson(): JSONObject {
        val json = JSONObject()
        json.put("type", type)
        json.put("username", username)
        json.put("uuid", uuid)
        when (this) {
            is Offline -> Unit
            is Microsoft -> {
                json.put("accessToken", accessToken)
                json.put("refreshToken", refreshToken)
                json.put("tokenExpiresAt", tokenExpiresAt)
                xuid?.let { json.put("xuid", it) }
                skinUrl?.let { json.put("skinUrl", it) }
            }
        }
        return json
    }

    companion object {
        fun fromJson(json: JSONObject): Account {
            return when (json.optString("type")) {
                Offline.TYPE -> Offline(json.optString("username"))
                Microsoft.TYPE -> Microsoft(
                    username = json.optString("username"),
                    uuid = json.optString("uuid"),
                    accessToken = json.optString("accessToken"),
                    refreshToken = json.optString("refreshToken"),
                    tokenExpiresAt = json.optLong("tokenExpiresAt", 0L),
                    xuid = json.optString("xuid").takeIf { it.isNotEmpty() },
                    skinUrl = json.optString("skinUrl").takeIf { it.isNotEmpty() }
                )
                else -> throw IllegalArgumentException("tipo de conta desconhecido: " + json.optString("type"))
            }
        }

        fun listFromJson(array: JSONArray): List<Account> {
            val out = ArrayList<Account>(array.length())
            for (i in 0 until array.length()) out.add(fromJson(array.getJSONObject(i)))
            return out
        }

        fun listToJson(accounts: List<Account>): JSONArray {
            val array = JSONArray()
            for (a in accounts) array.put(a.toJson())
            return array
        }
    }
}
