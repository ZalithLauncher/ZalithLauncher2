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

package com.movtery.zalithlauncher.ui.components

import android.annotation.SuppressLint
import android.content.Context
import android.util.Base64
import android.util.Log
import android.view.MotionEvent
import android.view.ViewGroup
import android.webkit.ConsoleMessage
import android.webkit.JsResult
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LoadingIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color.Companion.Transparent
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.webkit.WebViewAssetLoader
import com.movtery.zalithlauncher.game.account.wardrobe.EmptyCape
import com.movtery.zalithlauncher.game.account.wardrobe.SkinModelType
import com.movtery.zalithlauncher.game.account.yggdrasil.PlayerProfile
import com.movtery.zalithlauncher.path.PathManager
import java.io.File
import java.io.InputStream

@SuppressLint("SetJavaScriptEnabled")
class PlayerSkin(
    context: Context,
    localSkinsDir: File = PathManager.DIR_ACCOUNT_SKIN,
    localCapeDir: File = PathManager.DIR_ACCOUNT_CAPE,
) {
    private val assetLoader = WebViewAssetLoader.Builder()
        .addPathHandler("/assets/", WebViewAssetLoader.AssetsPathHandler(context))
        .addPathHandler(
            "/skins/",
            WebViewAssetLoader.InternalStoragePathHandler(context, localSkinsDir)
        )
        .addPathHandler(
            "/capes/",
            WebViewAssetLoader.InternalStoragePathHandler(context, localCapeDir)
        )
        .build()

    private var webview: WebView? = null

    private val skinView = AssetsUrlBuilder()
        .append("assets")
        .append("skinview")
        .append("skinview.html")
        .toString()

    private val defaultSkin = AssetsUrlBuilder()
        .append("assets")
        .append("steve.png")
        .toString()

    // Holds the identifier of the currently loaded cape (if any). This helps preserve the cape when the skin is reset.
    private var currentCapeId: String? = null

    // State tracking to prevent redundant JS calls
    private var currentSkinUrl: String? = null
    private var currentSkinModel: String? = null
    private var currentCapeUrl: String? = null

    fun loadWebView(
        context: Context,
        onPageFinished: () -> Unit = {}
    ): WebView {
        val view = WebView(context).apply {
            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                allowFileAccess = true
                allowContentAccess = true
                loadWithOverviewMode = true
                useWideViewPort = true
            }
            setBackgroundColor(Transparent.toArgb())
            overScrollMode = WebView.OVER_SCROLL_NEVER
            isVerticalScrollBarEnabled = false
            isHorizontalScrollBarEnabled = false
            webViewClient = object : WebViewClient() {
                override fun shouldInterceptRequest(
                    view: WebView,
                    request: WebResourceRequest
                ): WebResourceResponse? {
                    return assetLoader.shouldInterceptRequest(request.url)
                }

                override fun onPageFinished(view: WebView, url: String) {
                    super.onPageFinished(view, url)
                    // Reset state tracking when page is reloaded
                    currentSkinUrl = null
                    currentSkinModel = null
                    currentCapeUrl = null
                    onPageFinished()
                }
            }
            webChromeClient = object : WebChromeClient() {
                override fun onConsoleMessage(consoleMessage: ConsoleMessage): Boolean {
                    Log.d(
                        "WebViewConsole", (consoleMessage.message()
                                + " (line " + consoleMessage.lineNumber() + ")")
                    )
                    return true
                }

                override fun onJsAlert(
                    view: WebView?,
                    url: String?,
                    message: String,
                    result: JsResult
                ): Boolean {
                    Log.d("WebViewAlert", message)
                    result.confirm()
                    return true
                }
            }

            loadUrl(skinView)
        }
        this.webview = view
        return view
    }

    fun loadSkin(skinId: String?, model: SkinModelType?) {
        val modelString = model?.takeIf { it != SkinModelType.NONE }?.modelType ?: "auto-detect"
        val jsUrl = skinId?.let { id ->
            AssetsUrlBuilder()
                .append("skins")
                .append("$id.png")
                .toString()
        } ?: defaultSkin

        if (currentSkinUrl == jsUrl && currentSkinModel == modelString) return
        currentSkinUrl = jsUrl
        currentSkinModel = modelString

        webview?.evaluateJavascript("loadSkin('$jsUrl', '$modelString')", null)
    }

    fun loadSkin(inputStream: InputStream?, model: SkinModelType?) {
        val dataUrl = inputStream?.asBase64Image() ?: defaultSkin
        val modelString = model?.takeIf { it != SkinModelType.NONE }?.modelType ?: "auto-detect"

        if (currentSkinUrl == dataUrl && currentSkinModel == modelString) return
        currentSkinUrl = dataUrl
        currentSkinModel = modelString

        webview?.evaluateJavascript("loadSkin('$dataUrl', '$modelString')", null)
    }

    fun loadCape(cape: PlayerProfile.Cape?) {
        // Store the cape id so that a later skin reset can restore it.
        currentCapeId = cape?.takeIf { it != EmptyCape }?.id
        val path = cape?.takeIf { it != EmptyCape }?.id?.let { id ->
            AssetsUrlBuilder()
                .append("capes")
                .append("$id.png")
                .toString()
        }
        val jsUrl = path ?: "null"

        if (currentCapeUrl == jsUrl) return
        currentCapeUrl = jsUrl

        val jsCall = if (path != null) "'$path'" else "null"
        webview?.evaluateJavascript("loadCape($jsCall)", null)
    }

    fun loadCape(inputStream: InputStream?) {
        val dataUrl = inputStream?.asBase64Image() ?: "null"

        if (currentCapeUrl == dataUrl) return
        currentCapeUrl = dataUrl

        // When loading from a raw InputStream we cannot determine an ID, so we clear the stored ID.
        currentCapeId = null
        val jsCall = if (dataUrl != "null") "'$dataUrl'" else "null"
        webview?.evaluateJavascript("loadCape($jsCall)", null)
    }

    /**
     * Load skin and cape simultaneously to ensure stable rendering.
     */
    fun loadSkinAndCape(
        skinInputStream: InputStream?,
        skinModel: SkinModelType?,
        capeInputStream: InputStream?
    ) {
        val skinDataUrl = skinInputStream?.asBase64Image() ?: defaultSkin
        val modelString = skinModel?.takeIf { it != SkinModelType.NONE }?.modelType ?: "auto-detect"
        val capeDataUrl = capeInputStream?.asBase64Image() ?: "null"

        if (currentSkinUrl == skinDataUrl && currentSkinModel == modelString && currentCapeUrl == capeDataUrl) return
        currentSkinUrl = skinDataUrl
        currentSkinModel = modelString
        currentCapeUrl = capeDataUrl
        currentCapeId = null

        val capeJsCall = if (capeDataUrl != "null") "'$capeDataUrl'" else "null"
        webview?.evaluateJavascript(
            "loadSkinAndCape('$skinDataUrl', '$modelString', $capeJsCall)",
            null
        )
    }

    /**
     * Reset the skin while preserving the currently loaded cape (if any).
     */
    fun resetSkin() {
        // Preserve the currently loaded cape (if any) by re‑applying it after resetting the skin.
        val preservedCapeId = currentCapeId
        // Reset skin to default (no skin, model NONE)
        loadSkin(skinId = null, SkinModelType.NONE)
        // Re‑apply the previously loaded cape if we have its ID.
        if (preservedCapeId != null) {
            // Create a lightweight Cape implementation to trigger loading.
            val dummyCape = PlayerProfile.Cape(preservedCapeId, "ACTIVE", "", "")
            loadCape(dummyCape)
        } else {
            // No cape to preserve – clear it.
            loadCape(cape = null)
        }
    }

    fun startAnim(
        animation: ModelAnimation,
        speed: Float? = null
    ) {
        webview?.evaluateJavascript(
            "startAnim('${animation.name}', $speed)",
            null
        )
    }

    fun setAzimuthAndPitch(azimuthDeg: Int, pitchDeg: Int, distance: Int = 60) {
        webview?.evaluateJavascript(
            "setAzimuthAndPitch($azimuthDeg, $pitchDeg, $distance)",
            null
        )
    }

    /**
     * 设置预览交互开关
     * 关闭后 WebView 不再响应触摸（视角旋转等），仅作展示
     */
    fun setInteractionEnabled(enabled: Boolean) {
        webview?.evaluateJavascript("setInteractionEnabled($enabled)", null)
    }

    /**
     * 销毁 WebView 并释放资源
     * 应在包含该组件的 Composable 离开组合树时调用
     */
    fun destroy() {
        webview?.apply {
            stopLoading()
            loadUrl("about:blank")
            clearHistory()
            removeAllViews()
            destroy()
        }
        webview = null
    }

    private fun InputStream.asBase64Image(): String {
        return readBytes().let { bytes ->
            val base64 = Base64.encodeToString(bytes, Base64.NO_WRAP)
            "data:image/png;base64,$base64"
        }
    }
}

enum class ModelAnimation {
    DefaultIdle,
    NewIdle,
    Walking,
    Running,
    Flying,
    Wave,
    Crouch,
    Hit
}

/**
 * 3D 玩家皮肤预览：基于 skinview3d 的 WebView 渲染正面立绘与待机动画
 *
 * @param skinFile 皮肤文件，null 时展示默认皮肤
 * @param capeFile 披风文件，null 时移除披风
 * @param modelType 皮肤模型类型，null 时自动检测
 * @param animation 预览动画
 * @param azimuth 水平视角（度）
 * @param pitch 俯仰视角（度）
 * @param interactionEnabled 是否允许触摸交互（拖拽旋转视角）；
 * 关闭时触摸不进入 WebView，交还给上层手势处理
 * @param refreshKey 变化时重新加载皮肤与披风
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SkinPreview3D(
    skinFile: File?,
    capeFile: File?,
    modelType: SkinModelType?,
    modifier: Modifier = Modifier,
    animation: ModelAnimation = ModelAnimation.NewIdle,
    azimuth: Int = -35,
    pitch: Int = 10,
    interactionEnabled: Boolean = true,
    refreshKey: Any? = null,
) {
    val context = LocalContext.current
    val playerSkin = remember { PlayerSkin(context) }
    var pageFinished by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        onDispose {
            playerSkin.destroy()
        }
    }

    Box(modifier = modifier) {
        AndroidView(
            modifier = Modifier.matchParentSize(),
            factory = { context ->
                TouchGateLayout(context).apply {
                    addView(
                        playerSkin.loadWebView(context) { pageFinished = true },
                        ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                    )
                }
            },
            update = { container ->
                container.gateOpen = interactionEnabled
            }
        )

        LaunchedEffect(pageFinished, animation, azimuth, pitch) {
            if (!pageFinished) return@LaunchedEffect
            playerSkin.startAnim(animation)
            playerSkin.setAzimuthAndPitch(azimuth, pitch)
        }
        LaunchedEffect(pageFinished, interactionEnabled) {
            if (!pageFinished) return@LaunchedEffect
            playerSkin.setInteractionEnabled(interactionEnabled)
        }
        LaunchedEffect(pageFinished, skinFile, capeFile, modelType, refreshKey) {
            if (!pageFinished) return@LaunchedEffect
            runCatching {
                skinFile?.inputStream().use { playerSkin.loadSkin(it, modelType) }
                capeFile?.inputStream().use { playerSkin.loadCape(it) }
            }
        }

        if (!pageFinished) {
            LoadingIndicator(modifier = Modifier.align(Alignment.Center))
        }
    }
}

/**
 * 触摸门控容器：gateOpen 为 false 时拦截全部发往子 View 的触摸，
 * 且自身不消费，手势继续交还上层处理
 */
private class TouchGateLayout(context: Context) : FrameLayout(context) {
    var gateOpen: Boolean = true

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean = !gateOpen

    override fun onTouchEvent(ev: MotionEvent): Boolean = false
}

private class AssetsUrlBuilder {
    private val builder = StringBuilder()

    init {
        builder.append("https://appassets.androidplatform.net")
    }

    fun append(path: String): AssetsUrlBuilder {
        builder.append("/")
        builder.append(path)
        return this
    }

    override fun toString(): String {
        return builder.toString()
    }
}
