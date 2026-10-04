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

package com.movtery.zalithlauncher.game.sdl

import android.app.Activity
import android.view.Surface
import android.view.ViewGroup
import androidx.annotation.Keep
import androidx.annotation.MainThread
import com.movtery.zalithlauncher.setting.AllSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.libsdl.app.SDL
import org.libsdl.app.SDLActivity
import org.libsdl.app.SDLSurface
import org.lwjgl.glfw.CallbackBridge
import java.lang.ref.WeakReference

/**
 * Owns the SDL integration state shared by the launcher and game JVM.
 */
@Keep
object SdlBridge {
    private val _enabled = MutableStateFlow(false)
    val enabled = _enabled.asStateFlow()

    private val _composeFocus = MutableStateFlow(0)
    val composeFocus = _composeFocus.asStateFlow()

    private var activityRef: WeakReference<Activity>? = null
    private var layoutRef: WeakReference<ViewGroup>? = null
    private var currentSurface: Surface? = null


    /** 当前注册 Surface 的来源 */
    private var currentSource: Any? = null

    /** 每次注册新 Surface 递增，供生命周期观测 */
    private var surfaceGeneration = 0L
    private var jniReady = false
    private var sdlInitialized = false

    @JvmStatic
    @Synchronized
    fun setupJNI(): Boolean {
        if (jniReady) {
            return true
        }
        SDL.setupJNI()
        jniReady = true
        return true
    }

    @JvmStatic
    @Synchronized
    fun markSdlInitialized(): Boolean {
        if (sdlInitialized) return false
        sdlInitialized = true
        return true
    }

    @JvmStatic
    @Synchronized
    fun clearSdlInitialized() {
        sdlInitialized = false
    }

    @JvmStatic
    @Volatile
    var sdlEnabled: Boolean = false
        set(value) {
            field = value
            _enabled.value = value
        }

    /**
     * SDL 请求唤起输入法时，启动器侧是否响应
     */
    @JvmStatic
    fun getSdlImeAutoShowEnabled(): Boolean = AllSettings.sdlAutoShowIme.state

    @JvmStatic
    @MainThread
    fun prepareSurface(activity: Activity, surface: Surface, layout: ViewGroup?, source: Any? = null) {
        activityRef = WeakReference(activity)
        layoutRef = WeakReference(layout)
        currentSurface = surface
        currentSource = source
        surfaceGeneration++

        if (SDLActivity.getSDLSurface() == null) {
            SDL.initialize()
            SDL.setContext(activity)
            SDLActivity.externalInitialize(SDLSurface(activity), layout, surface)
        } else {
            SDLSurface.setNativeSurface(surface)
        }
    }

    @JvmStatic
    @MainThread
    fun registerSurface(activity: Activity, surface: Surface, layout: ViewGroup?) {
        activityRef = WeakReference(activity)
        layoutRef = WeakReference(layout)
        currentSurface = surface
    }

    @JvmStatic
    fun requestComposeFocus() {
        _composeFocus.update { it + 1 }
    }

    @JvmStatic
    @MainThread
    fun beginSurfaceDestroy(source: Any?, surface: Surface?): Boolean {
        return source != null && currentSource === source && surface != null && currentSurface === surface
    }

    @JvmStatic
    @MainThread
    fun unregisterSurface(surface: Surface?) {
        if (surface != null && currentSurface === surface) {
            currentSurface = null
            currentSource = null
        }
    }

    @JvmStatic
    @MainThread
    @Synchronized
    fun reset() {
        currentSurface = null
        currentSource = null
        surfaceGeneration = 0L
        activityRef = null
        layoutRef = null
        jniReady = false
        sdlInitialized = false
        sdlEnabled = false
        CallbackBridge.clearSdlBridgeState()
        SDLSurface.clearNativeSurface()
        SDL.initialize()
    }

    @JvmStatic
    external fun initializeControllerSubsystems()

    /**
     * 游戏是否运行在 SDL 渲染路径（SDL 窗口已创建，MC 26.3+）
     * 仅手柄子系统使用 SDL 时（如 MC 26.2 挂 Controlify）返回 false，
     * 此时游戏输入仍走 GLFW 桥，启动器不应把键盘切换委托给 SDL 输入通道
     */
    @JvmStatic
    external fun isSdlRenderActive(): Boolean

    /**
     * 激活/关闭 native 侧 SDL 文本输入通道
     * 游戏侧通道被模组（自绘输入界面）关闭时，启动器显式唤起输入法需代为激活，
     * 否则输入法提交的文本会在 native 层被丢弃
     */
    @JvmStatic
    external fun setNativeTextInputActive(active: Boolean): Boolean
}