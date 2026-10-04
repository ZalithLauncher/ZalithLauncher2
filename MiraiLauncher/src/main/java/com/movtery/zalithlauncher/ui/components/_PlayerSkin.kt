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
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay

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
                override fun shouldInterceptRequest(view: WebView, request: WebResourceRequest): WebResourceResponse? {
                    return assetLoader.shouldInterceptRequest(request.url)
                }

                override fun onPageFinished(view: WebView, url: String) {
                    super.onPageFinished(view, url)
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

                override fun onJsAlert(view: WebView?, url: String?, message: String, result: JsResult): Boolean {
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
        webview?.evaluateJavascript("loadSkin('$jsUrl', '$modelString')", null)
    }

    fun loadSkin(inputStream: InputStream?, model: SkinModelType?) {
        inputStream?.asBase64Image()?.let { dataUrl ->
            val modelString = model.takeIf { it != SkinModelType.NONE }?.modelType ?: "auto-detect"
            webview?.evaluateJavascript("loadSkin('$dataUrl', '$modelString')", null)
        } ?: run {
            loadSkin(skinId = null, model)
        }
    }

    fun loadCape(cape: PlayerProfile.Cape?) {
        val path = cape?.takeIf { it != EmptyCape }?.id?.let { id ->
            AssetsUrlBuilder()
                .append("capes")
                .append("$id.png")
                .toString()
        }
        val jsUrl = path?.let { "'$it'" } ?: "null"
        webview?.evaluateJavascript("loadCape($jsUrl)", null)
    }

    fun loadCape(inputStream: InputStream?) {
        inputStream?.asBase64Image()?.let { dataUrl ->
            webview?.evaluateJavascript("loadCape('$dataUrl')", null)
        } ?: run {
            loadCape(cape = null)
        }
    }

    fun resetSkin() {
        loadSkin(skinId = null, SkinModelType.NONE)
        loadCape(cape = null)
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

    fun setInteractionEnabled(enabled: Boolean) {
        webview?.evaluateJavascript("setInteractionEnabled($enabled)", null)
    }

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
 * 3D skin preview. Idle animation is off by default so the WebView does not
 * repaint every frame while the rest of the launcher is open.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SkinPreview3D(
    skinFile: File?,
    capeFile: File?,
    modelType: SkinModelType?,
    modifier: Modifier = Modifier,
    animation: ModelAnimation? = null,
    azimuth: Int = -35,
    pitch: Int = 10,
    interactionEnabled: Boolean = false,
    refreshKey: Any? = null,
) {
    val context = LocalContext.current
    val playerSkin = remember { PlayerSkin(context) }
    var pageFinished by remember { mutableStateOf(false) }
    //Coalesces rapid camera updates: only the newest angle survives until the paced reader takes it.
    val cameraAngles = remember { Channel<Pair<Int, Int>>(Channel.CONFLATED) }

    DisposableEffect(Unit) {
        onDispose {
            playerSkin.destroy()
        }
    }

    Box(modifier = modifier) {
        AndroidView(
            modifier = Modifier.matchParentSize(),
            factory = { viewContext ->
                TouchGateLayout(viewContext).apply {
                    addView(
                        playerSkin.loadWebView(viewContext) { pageFinished = true },
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

        LaunchedEffect(pageFinished, animation) {
            if (!pageFinished) return@LaunchedEffect
            if (animation != null) playerSkin.startAnim(animation)
        }
        //Cheap observer: every drag tick only offers the newest angle to the channel.
        LaunchedEffect(pageFinished, azimuth, pitch) {
            if (!pageFinished) return@LaunchedEffect
            cameraAngles.trySend(azimuth to pitch)
        }
        //Paced reader: camera JS calls are capped at ~60fps and always carry the
        //latest angle, so fast drags and flings stay smooth instead of queueing up.
        LaunchedEffect(pageFinished) {
            if (!pageFinished) return@LaunchedEffect
            for ((azimuthDeg, pitchDeg) in cameraAngles) {
                playerSkin.setAzimuthAndPitch(azimuthDeg, pitchDeg)
                delay(16)
            }
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
