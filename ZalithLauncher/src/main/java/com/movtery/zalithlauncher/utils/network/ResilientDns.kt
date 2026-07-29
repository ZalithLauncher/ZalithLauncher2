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

package com.movtery.zalithlauncher.utils.network

import com.movtery.zalithlauncher.utils.logging.Logger
import okhttp3.Dns
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.IOException
import java.net.InetAddress
import java.net.UnknownHostException
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

/**
 * 容灾 DNS 解析器
 *
 * 优先使用系统 DNS 解析；当系统 DNS 失败（DNS 污染、运营商 DNS 故障、
 * 私人 DNS 不可达等）时，回退到公共 DNS-over-HTTPS 服务解析，
 * 避免单纯的解析失败导致整个网络请求不可用。
 *
 * 所有 DoH 服务均通过 IP 字面量访问，其 TLS 证书包含对应 IP SAN，因此
 * 回退解析本身不依赖系统 DNS。
 */
object ResilientDns : Dns {
    private const val TAG = "ResilientDns"

    /** DoH 解析结果的缓存时长 */
    private const val CACHE_TTL_MILLIS = 10 * 60 * 1000L

    /** 缓存已成功通过 DoH 解析的结果（系统 DNS 结果由系统自行缓存，不在此处缓存） */
    private val cache = ConcurrentHashMap<String, Pair<List<InetAddress>, Long>>()

    /** 用于 DoH 查询的引导客户端，全部使用 IP 字面量地址，无需预先解析域名 */
    private val dohClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(5, TimeUnit.SECONDS)
            .readTimeout(5, TimeUnit.SECONDS)
            .build()
    }

    /**
     * 公共 DoH 服务的查询地址模板（均为 dns-json 响应格式）
     * 按优先级排序，靠前的优先尝试
     */
    private val dohProviders = listOf(
        "https://1.1.1.1/dns-query?name=%s&type=A", //Cloudflare
        "https://8.8.8.8/resolve?name=%s&type=A", //Google
        "https://223.5.5.5/resolve?name=%s&type=A" //AliDNS
    )

    override fun lookup(hostname: String): List<InetAddress> {
        try {
            return Dns.SYSTEM.lookup(hostname)
        } catch (e: Exception) {
            Logger.warning(TAG, "System DNS resolution failed for $hostname, falling back to DoH: ${e.message}")
        }

        //命中缓存，避免对同一域名频繁发起 DoH 查询
        cache[hostname]?.let { (addresses, timestamp) ->
            if (System.currentTimeMillis() - timestamp < CACHE_TTL_MILLIS) {
                return addresses
            }
            cache.remove(hostname)
        }

        var lastError: Exception? = null
        for (template in dohProviders) {
            try {
                val addresses = dohLookup(template.format(hostname))
                if (addresses.isNotEmpty()) {
                    Logger.info(
                        TAG,
                        "Resolved $hostname via DoH: ${addresses.joinToString { it.hostAddress ?: "?" }}"
                    )
                    cache[hostname] = addresses to System.currentTimeMillis()
                    return addresses
                }
            } catch (e: Exception) {
                lastError = e
                Logger.warning(TAG, "DoH lookup failed for $hostname: ${e.message}")
            }
        }

        throw UnknownHostException("Unable to resolve $hostname: system DNS and all DoH providers failed").apply {
            lastError?.let { initCause(it) }
        }
    }

    /**
     * 通过 dns-json 格式的 DoH 服务解析域名
     * @return 解析出的地址列表，无有效记录时返回空列表
     */
    private fun dohLookup(url: String): List<InetAddress> {
        val request = Request.Builder()
            .url(url)
            .header("accept", "application/dns-json")
            .build()

        dohClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IOException("DoH request failed: HTTP ${response.code}")

            val json = JSONObject(response.body.string())
            val status = json.optInt("Status", -1)
            if (status != 0) throw IOException("DoH response status: $status")

            val answers = json.optJSONArray("Answer") ?: return emptyList()
            return (0 until answers.length())
                .mapNotNull { answers.optJSONObject(it) }
                .filter { it.optInt("type") == 1 || it.optInt("type") == 28 } //A 与 AAAA 记录
                .mapNotNull { record ->
                    record.optString("data").takeIf { it.isNotBlank() }?.let { ip ->
                        //IP 字面量不会触发额外的 DNS 查询
                        runCatching { InetAddress.getByName(ip) }.getOrNull()
                    }
                }
                .distinctBy { it.hostAddress }
        }
    }
}
