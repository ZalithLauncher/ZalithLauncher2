package com.zalith.launcher.game

import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

/**
 * HTTP mínimo, sem dependências. Bloqueante — fora da main thread.
 */
object Http {

    const val USER_AGENT = "ZalithLauncher3/0.1 (Android; zero-dep)"

    fun getString(url: String, timeoutMs: Int = 15000): String {
        val conn = URL(url).openConnection() as HttpURLConnection
        try {
            conn.requestMethod = "GET"
            conn.connectTimeout = timeoutMs
            conn.readTimeout = timeoutMs
            conn.setRequestProperty("User-Agent", USER_AGENT)
            conn.setRequestProperty("Accept", "application/json")
            val code = conn.responseCode
            val stream = if (code in 200..299) conn.inputStream else conn.errorStream
            val text = stream?.bufferedReader()?.use { it.readText() } ?: ""
            if (code !in 200..299) throw IOException("HTTP " + code + " em " + url)
            return text
        } finally {
            conn.disconnect()
        }
    }

    fun getJson(url: String, timeoutMs: Int = 15000): JSONObject =
        JSONObject(getString(url, timeoutMs))
}
