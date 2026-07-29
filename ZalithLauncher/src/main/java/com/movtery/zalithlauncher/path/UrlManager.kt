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

package com.movtery.zalithlauncher.path

import com.movtery.zalithlauncher.BuildConfig
import com.movtery.zalithlauncher.BuildKeys
import com.movtery.zalithlauncher.utils.network.ResilientDns
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.HttpRequestPipeline
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import java.util.concurrent.TimeUnit

val URL_USER_AGENT: String = "${BuildKeys.LAUNCHER_SHORT_NAME}/Android_${BuildConfig.VERSION_NAME}"
val TIME_OUT = TimeUnit.SECONDS.toMillis(30L)

const val HOST_CURSEFORGE_API = "api.curseforge.com"
const val HOST_CURSEFORGE_EDGE = "edge.forgecdn.net"

const val URL_MCMOD: String = "https://www.mcmod.cn/"
const val URL_MINECRAFT_VERSION_REPOS: String = "https://piston-meta.mojang.com/mc/game/version_manifest_v2.json"
const val URL_MINECRAFT_ASSETS_INDEX: String = "https://launchermeta.mojang.com/v1/packages"
const val URL_MINECRAFT_PURCHASE = "https://www.xbox.com/games/store/minecraft-java-bedrock-edition-for-pc/9nxp44l49shj"
const val URL_PROJECT: String = "https://github.com/ZalithLauncher/ZalithLauncher2"
const val URL_PROJECT_INFO: String = "https://api.github.com/repos/ZalithLauncher/Zalith-Info/contents/v2"
const val URL_COMMUNITY: String = "https://github.com/ZalithLauncher/ZalithLauncher2/graphs/contributors"
const val URL_WEBLATE: String = "https://hosted.weblate.org/projects/zalithlauncher2"
const val URL_SUPPORT: String = "https://ifdian.net/a/MovTery"
const val URL_EASYTIER: String = "https://easytier.cn/"

const val URL_GITHUB_RENDERER_PLUGINS = "https://github.com/ShirosakiMio/FCLRendererPlugin/releases/tag/Renderer"
const val URL_GITHUB_DRIVER_PLUGINS = "https://github.com/FCL-Team/FCLDriverPlugin/releases/tag/Turnip"
const val URL_GITHUB_NATIVE_LIB_PLUGINS = "https://github.com/ZalithLauncher/NativeLibPlugin/releases"

const val URL_CLOUD_RENDERER_PLUGINS = "https://www.123865.com/s/YLIUVv-hae0v"
const val URL_CLOUD_DRIVE_DRIVER_PLUGINS = "https://www.123865.com/s/YLIUVv-3ae0v"
const val URL_CLOUD_NATIVE_LIB_PLUGINS = "https://www.123865.com/s/YLIUVv-Hae0v"

/**
 * An [Interceptor] for CurseForge API requests.
 *
 * It automatically injects the `x-api-key` header when the request host matches
 * [HOST_CURSEFORGE_API] or [HOST_CURSEFORGE_EDGE], provided the API key is not blank.
 */
private val CURSEFORGE_INTERCEPTOR = Interceptor { chain ->
    val request = chain.request()
    val host = request.url.host
    if (host == HOST_CURSEFORGE_API || host == HOST_CURSEFORGE_EDGE) {
        val apiKey = BuildKeys.CURSEFORGE_API
        if (apiKey.isNotBlank()) {
            val newRequest = request.newBuilder()
                .header("x-api-key", apiKey)
                .build()
            return@Interceptor chain.proceed(newRequest)
        }
    }
    chain.proceed(request)
}

/**
 * An [Interceptor] that ensures the [URL_USER_AGENT] header is present on every request.
 *
 * If a request already carries a User-Agent header (set by [createRequestBuilder] or
 * similar), this interceptor is a no-op — avoiding duplicate headers.
 */
private val USER_AGENT_INTERCEPTOR = Interceptor { chain ->
    val request = chain.request()
    if (request.header("User-Agent") != null) {
        chain.proceed(request)
    } else {
        val newRequest = request.newBuilder()
            .header("User-Agent", URL_USER_AGENT)
            .build()
        chain.proceed(newRequest)
    }
}

val GLOBAL_JSON = Json {
    ignoreUnknownKeys = true
    explicitNulls = true
    coerceInputValues = true
}

val GLOBAL_CLIENT = HttpClient(OkHttp) {
    install(HttpTimeout) {
        requestTimeoutMillis = TIME_OUT
    }
    install(ContentNegotiation) {
        json(GLOBAL_JSON)
    }
    expectSuccess = true

    defaultRequest {
        header(HttpHeaders.UserAgent, URL_USER_AGENT)
    }
    engine {
        // 使用内置的 OkHttp 客户端：遵循系统代理设置，并具备容灾 DNS 解析能力
        preconfigured = createOkHttpClientBuilder().build()
    }
}.apply {
    requestPipeline.intercept(HttpRequestPipeline.State) {
        // 检查 host 是否为 CurseForge
        // 自动添加 CurseForge 的 api 密钥
        val host = context.url.host
        if (host == HOST_CURSEFORGE_API || host == HOST_CURSEFORGE_EDGE) {
            val apiKey = BuildKeys.CURSEFORGE_API
            if (apiKey.isNotBlank()) {
                context.header("x-api-key", apiKey)
            }
        }
    }
}

fun createRequestBuilder(url: String): Request.Builder {
    return createRequestBuilder(url, null)
}

fun createRequestBuilder(url: String, body: RequestBody?): Request.Builder {
    val request = Request.Builder().url(url).header("User-Agent", URL_USER_AGENT)
    body?.let{ request.post(it) }
    return request
}

fun createOkHttpClient(): OkHttpClient = createOkHttpClientBuilder().build()

/**
 * 创建一个OkHttpClient，可自定义一些内容
 */
fun createOkHttpClientBuilder(action: (OkHttpClient.Builder) -> Unit = { }): OkHttpClient.Builder {
    return OkHttpClient.Builder()
        .dns(ResilientDns) //系统 DNS 解析失败时，自动回退到 DoH 解析
        .callTimeout(TIME_OUT, TimeUnit.MILLISECONDS)
        .addInterceptor(CURSEFORGE_INTERCEPTOR)
        .addInterceptor(USER_AGENT_INTERCEPTOR)
        .apply(action)
}

/**
 * 创建用于文件下载的 OkHttpClient。
 * 与普通 API 调用不同，文件下载需要更长的超时配置，且不设 callTimeout
 * （因为文件大小差异很大，不能用一个固定值限制整体下载时间）。
 *
 * 使用 OkHttp 替代 HttpURLConnection 的主要原因是：
 * OkHttp 使用自实现的 AsyncTimeout 机制，比依赖操作系统 socket 超时的
 * HttpURLConnection 在 Android 上更加可靠，能有效避免"卡 0b/s"问题。
 */
val DOWNLOAD_OKHTTP_CLIENT: OkHttpClient by lazy {
    OkHttpClient.Builder()
        .dns(ResilientDns) //系统 DNS 解析失败时，自动回退到 DoH 解析
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .addInterceptor(CURSEFORGE_INTERCEPTOR)
        .addInterceptor(USER_AGENT_INTERCEPTOR)
        .build()
        // 注意：不设置 callTimeout，因为文件大小差异极大
        // 协程层的 withTimeout 提供整体兜底保护
}