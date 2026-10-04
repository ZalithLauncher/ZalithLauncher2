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

package com.movtery.zalithlauncher.ui.theme.festivals

import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.SurfaceTexture
import android.os.Handler
import android.os.HandlerThread
import android.os.Process
import android.view.Choreographer
import android.view.Surface
import android.view.TextureView
import com.movtery.zalithlauncher.ui.theme.festivals.simulations.BatSwarmSimulator
import com.movtery.zalithlauncher.ui.theme.festivals.simulations.FireworksSimulator
import com.movtery.zalithlauncher.ui.theme.festivals.simulations.HeartsSimulator
import com.movtery.zalithlauncher.ui.theme.festivals.simulations.ParticleSimulator

/**
 * 彩蛋效果引擎，在独立渲染线程上驱动粒子模拟与绘制。
 *
 * Surface 生命周期完全在渲染线程上串行处理：销毁回调把 SurfaceTexture
 * 的所有权移交渲染线程，在当前帧绘制完成后才释放，避免框架在主线程销毁
 * Surface 与渲染线程 lock/unlock 并发引发的原生崩溃。
 */
class FestivalEffectsEngine : TextureView.SurfaceTextureListener {
    private data class Config(
        val types: List<FestivalEffectType>,
        val isDark: Boolean
    )

    private var textureView: TextureView? = null
    private var density = 1f

    private var handlerThread: HandlerThread? = null
    private var handler: Handler? = null
    private var choreographer: Choreographer? = null
    private var frameCallback: Choreographer.FrameCallback? = null

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val framePacer = FramePacer()
    private var effects: List<Pair<ParticleSimulator, EffectDrawer>> = emptyList()

    @Volatile
    private var pendingConfig: Config = Config(emptyList(), isDark = false)

    private var configuredTypes: List<FestivalEffectType> = emptyList()
    private var isDark = false

    // 以下状态仅在渲染线程读写
    private var surfaceTexture: SurfaceTexture? = null
    private var renderSurface: Surface? = null
    private var surfaceWidth = 0f
    private var surfaceHeight = 0f
    private var surfaceReady = false
    private var active = false
    private var running = false

    /** 绑定承载效果的 TextureView */
    fun attach(view: TextureView) {
        textureView = view
        density = view.resources.displayMetrics.density
        view.surfaceTextureListener = this

        val thread = HandlerThread(THREAD_NAME, Process.THREAD_PRIORITY_DEFAULT).also {
            it.start()
        }
        handlerThread = thread
        handler = Handler(thread.looper)
        if (view.isAvailable) {
            view.surfaceTexture?.let { surface ->
                onSurfaceTextureAvailable(surface, view.width, view.height)
            }
        }
    }

    /** 更新效果种类与主题，主题切换仅重着色 */
    fun applyConfig(types: List<FestivalEffectType>, isDark: Boolean) {
        pendingConfig = Config(types, isDark)
        handler?.post {
            applyPendingConfig()
            if (active && surfaceReady) {
                startLoop()
            } else {
                stopLoop()
            }
        }
    }

    /** 设置效果是否处于可见运行状态 */
    fun setActive(active: Boolean) {
        handler?.post {
            this.active = active
            if (active && surfaceReady) {
                startLoop()
            } else {
                stopLoop()
            }
        }
    }

    fun burstAt(x: Float, y: Float) {
        handler?.post {
            if (!active || !surfaceReady) return@post
            effects.forEach { (simulator, _) ->
                when (simulator) {
                    is FireworksSimulator -> simulator.burstAt(x, y)
                    is BatSwarmSimulator -> simulator.startleAt(x, y)
                    is HeartsSimulator -> simulator.burstHearts(x, y)
                }
            }
        }
    }

    fun release() {
        val thread = handlerThread ?: return
        handlerThread = null
        // 清理消息先入队，quitSafely 会先执行已到期的消息再退出
        handler?.post {
            stopLoop()
            effects = emptyList()
            releaseSurface()
        }
        handler = null
        thread.quitSafely()
        choreographer = null
        frameCallback = null
        textureView?.surfaceTextureListener = null
        textureView = null
    }

    override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
        handler?.post {
            // 同一纹理的销毁清理若已排队，会先于本消息执行，此处直接接管新表面
            surfaceTexture = surface
            renderSurface = Surface(surface)
            surfaceWidth = width.toFloat()
            surfaceHeight = height.toFloat()
            surfaceReady = true
            applyPendingConfig()
            if (active) startLoop()
        }
    }

    override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {
        handler?.post {
            surfaceWidth = width.toFloat()
            surfaceHeight = height.toFloat()
            effects.forEach { it.first.resize(width.toFloat(), height.toFloat(), density) }
        }
    }

    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
        // 返回 false 保留所有权：由渲染线程在当前帧绘制完成后释放，
        // 消除主线程销毁与渲染线程 lock/unlock 的并发窗口
        handler?.post {
            stopLoop()
            surfaceReady = false
            if (surfaceTexture === surface) {
                releaseSurface()
            }
        }
        return false
    }

    override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {
    }

    private fun releaseSurface() {
        surfaceTexture = null
        renderSurface?.release()
        renderSurface = null
    }

    private fun applyPendingConfig() {
        if (surfaceWidth <= 0f || surfaceHeight <= 0f) return
        val config = pendingConfig

        val needsRebuild = effects.isEmpty() || config.types != configuredTypes

        if (needsRebuild) {
            configuredTypes = config.types
            isDark = config.isDark
            effects = config.types.map { type ->
                createEffect(
                    type = type,
                    isDark = config.isDark,
                    width = surfaceWidth,
                    height = surfaceHeight,
                    density = density
                )
            }
        } else if (config.isDark != isDark) {
            isDark = config.isDark
            effects.forEach { it.second.setTheme(config.isDark) }
        }
    }

    private fun startLoop() {
        if (running) return
        running = true
        framePacer.reset()
        val choreographer = Choreographer.getInstance()
        this.choreographer = choreographer

        val callback = Choreographer.FrameCallback {
            onFrame(it)
        }
        frameCallback = callback
        choreographer.postFrameCallback(callback)
    }

    private fun stopLoop() {
        running = false
        frameCallback?.let { choreographer?.removeFrameCallback(it) }
        frameCallback = null
    }

    private fun onFrame(frameNanos: Long) {
        if (!running) return

        // 60fps
        val deltaNanos = framePacer.onVsync(frameNanos)
        if (deltaNanos != null && active && surfaceReady && effects.isNotEmpty()) {
            val deltaSeconds = (deltaNanos / 1_000_000_000f).coerceIn(0f, MAX_DELTA)
            if (deltaSeconds > 0f) {
                effects.forEach { it.first.step(deltaSeconds) }
            }
            renderFrame()
        }

        if (running) {
            choreographer?.postFrameCallback(frameCallback)
        }
    }

    private fun renderFrame() {
        val surface = renderSurface ?: return
        // 每帧全量重绘，无需脏区
        val canvas = surface.lockCanvas(null) ?: return
        try {
            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
            effects.forEach { it.second.draw(canvas, paint) }
        } finally {
            surface.unlockCanvasAndPost(canvas)
        }
    }

    companion object {
        private const val THREAD_NAME = "festival-effects"
        private const val MAX_DELTA = 0.05f
    }
}
