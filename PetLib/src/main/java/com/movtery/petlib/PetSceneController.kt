/*
 * Zalith Launcher 2
 * Copyright (C) 2025-2026 MovTery <movtery228@qq.com> and contributors
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

package com.movtery.petlib

import android.graphics.Bitmap
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.util.Log
import android.view.Choreographer
import android.view.Surface
import androidx.annotation.FloatRange
import androidx.core.graphics.createBitmap
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference

private const val TAG = "PetLib"

/** 连续锁画布失败达到该次数后，判定表面已失效并停止帧驱动 */
private const val SURFACE_FAILURE_LIMIT = 10

/**
 * 帧间隔下限
 */
private const val MIN_FRAME_INTERVAL_NANOS = 1_000_000_000L / 60L * 9L / 10L

/** 场景相机投影模式 */
sealed interface PetCameraMode {
    /**
     * 正交模式
     * @param zoom 屏宽覆盖的世界单位数（1 单位 = 1 方块）
     */
    data class Ortho(val zoom: Float) : PetCameraMode

    /**
     * 透视模式
     * @param fov 垂直视场角（度）
     */
    data class Perspective(val fov: Float) : PetCameraMode
}

/**
 * 场景相机：绕注视目标的轨道参数。
 */
data class PetCamera(
    val target: Triple<Float, Float, Float> = Triple(0f, 15f / 16f, 0f),
    val azimuth: Float = 23.19859f,
    val elevation: Float = 16.83836f,
    val distance: Float = 5f,
    val mode: PetCameraMode = PetCameraMode.Ortho(44f / 16f),
)

/**
 * 场景控制器，多模型实例的容器，内部独占一个渲染线程。
 */
class PetSceneController : AutoCloseable {
    private val thread = HandlerThread("PetLibSceneRender").apply { start() }
    private val handler = Handler(thread.looper)
    private val mainHandler = Handler(Looper.getMainLooper())

    @Volatile
    private var ptr: Long = PetLibNative.nativeSceneCreate()

    @Volatile
    private var released = false

    // 相机镜像：供轨道拖拽在调用侧直接累加方位角
    @Volatile
    private var cameraMirror = PetCamera()

    private var frameCallback: Choreographer.FrameCallback? = null
    // 双缓冲：光栅写后台缓冲，提交展示前台缓冲，锁画布的等待期不再把
    // 光栅时间串联进提交关键路径
    private var backBuffer: FrameBuffer? = null
    private var frontBuffer: FrameBuffer? = null
    // 位图以 SRC 模式整幅覆写画布，透明区域随位图零像素一并替换，
    // 免去提交前对旧内容做全画布清屏的额外一遍操作
    private val overwritePaint = Paint().apply {
        xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC)
    }
    private var surfaceFailures = 0
    private var lastRenderNanos = 0L
    private var lastSlowLogNanos = 0L
    private var attachedSurface: Surface? = null
    private val surfaceRef = AtomicReference<Surface?>(null)
    private val frameInFlight = AtomicBoolean(false)
    private val widthHint = AtomicInteger(0)
    private val heightHint = AtomicInteger(0)

    /**
     * 添加内置 classic/slim 玩家模型实例。
     * 模型在调用线程同步创建（皮肤非法或解析失败时同步抛 [IllegalArgumentException]），
     * @param skin PNG 皮肤数据
     * @param arm 手臂模型类别，null 自动检测
     * @param onAdded 加入成功后回调实例句柄（渲染线程）
     * @param onError 加入失败时回调（渲染线程）
     */
    fun addPlayer(
        skin: ByteArray,
        arm: ArmModel?,
        onAdded: ((Int) -> Unit)? = null,
        onError: ((String) -> Unit)? = null,
    ) {
        val model = PetLibNative.nativeModelCreatePlayer(skin, ArmModel.codeOf(arm))
        if (model == 0L) throw IllegalArgumentException("Failed to create player model.")
        addModel(model, onAdded, onError)
    }

    /**
     * 添加自定义 GLB 道具模型实例（材质须携带内嵌纹理）。
     * 创建失败同步抛 [IllegalArgumentException]，句柄经 [onAdded] 回调。
     * @param glb GLB 字节数据
     * @param onAdded 加入成功后回调实例句柄（渲染线程）
     * @param onError 加入失败时回调（渲染线程）
     */
    fun addProp(
        glb: ByteArray,
        onAdded: ((Int) -> Unit)? = null,
        onError: ((String) -> Unit)? = null,
    ) {
        val model = PetLibNative.nativeModelCreateProp(glb)
        if (model == 0L) throw IllegalArgumentException("Failed to create prop model.")
        addModel(model, onAdded, onError)
    }

    /**
     * 添加自定义 GLB 模型实例并绑定玩家皮肤（材质名映射皮肤/披风）。
     * 创建失败同步抛 [IllegalArgumentException]，句柄经 [onAdded] 回调。
     * @param glb GLB 字节数据
     * @param skin PNG 皮肤数据
     * @param arm 手臂模型类别，null 自动检测
     * @param onAdded 加入成功后回调实例句柄（渲染线程）
     * @param onError 加入失败时回调（渲染线程）
     */
    fun addPropWithSkin(
        glb: ByteArray,
        skin: ByteArray,
        arm: ArmModel?,
        onAdded: ((Int) -> Unit)? = null,
        onError: ((String) -> Unit)? = null,
    ) {
        val model = PetLibNative.nativeModelCreatePropWithSkin(glb, skin, ArmModel.codeOf(arm))
        if (model == 0L) throw IllegalArgumentException("Failed to create prop model.")
        addModel(model, onAdded, onError)
    }

    /**
     * 移除实例
     */
    fun remove(instance: Int) {
        post { PetLibNative.nativeSceneRemoveModel(ptr, instance) }
    }

    /**
     * 设置场景相机
     */
    fun setCamera(camera: PetCamera) {
        cameraMirror = camera
        post {
            val (mode, zoomOrFov) = when (val m = camera.mode) {
                is PetCameraMode.Ortho -> 0 to m.zoom
                is PetCameraMode.Perspective -> 1 to m.fov
            }
            PetLibNative.nativeSceneSetCamera(
                ptr,
                camera.target.first, camera.target.second, camera.target.third,
                camera.azimuth, camera.elevation, camera.distance,
                mode, zoomOrFov,
            )
        }
    }

    /** 相机方位角增量叠加（度） */
    fun rotateCameraAzimuth(deltaDegrees: Float) {
        val current = cameraMirror
        setCamera(current.copy(azimuth = current.azimuth + deltaDegrees))
    }

    /**
     * 场景级方向光照参数
     * @param azimuth 光源方位角（度）
     * @param elevation 光源仰角（度）
     * @param brightness 亮度百分比，100 为原始色
     * @param softness 柔和度百分比，越高明暗对比越平缓
     */
    fun setLighting(
        azimuth: Float,
        elevation: Float,
        brightness: Float,
        softness: Float,
    ) {
        post { PetLibNative.nativeSceneSetLighting(ptr, azimuth, elevation, brightness, softness) }
    }

    /** 场景级超采样系数 */
    fun setSuperSample(@FloatRange(0.5, 2.0) value: Float) {
        post { PetLibNative.nativeSceneSetSuperSample(ptr, value) }
    }

    /** 替换实例皮肤（玩家语义），失败经 [onError] 回调 */
    fun setSkin(
        instance: Int,
        skin: ByteArray,
        arm: ArmModel?,
        onError: ((String) -> Unit)? = null,
    ) {
        post {
            PetLibNative.nativeSceneSetSkin(ptr, instance, skin, ArmModel.codeOf(arm))?.let { error ->
                Log.w(TAG, "Skin rejected: $error")
                onError?.invoke(error)
            }
        }
    }

    /** 替换实例披风（玩家语义），null 移除，失败经 [onError] 回调 */
    fun setCape(
        instance: Int,
        cape: ByteArray?,
        onError: ((String) -> Unit)? = null,
    ) {
        post {
            PetLibNative.nativeSceneSetCape(ptr, instance, cape)?.let { error ->
                Log.w(TAG, "Cape rejected: $error")
                onError?.invoke(error)
            }
        }
    }

    /** 按材质名覆盖实例纹理（PNG），失败经 [onError] 回调 */
    fun setTexture(
        instance: Int,
        material: String,
        texture: ByteArray,
        onError: ((String) -> Unit)? = null,
    ) {
        post {
            PetLibNative.nativeSceneSetTexture(ptr, instance, material, texture)?.let { error ->
                Log.w(TAG, "Texture rejected: $error")
                onError?.invoke(error)
            }
        }
    }

    /** 实例位置（场景单位，1 单位 = 1 方块） */
    fun setPosition(instance: Int, x: Float, y: Float, z: Float) {
        post { PetLibNative.nativeSceneSetPosition(ptr, instance, x, y, z) }
    }

    /** 实例欧拉旋转（度，pitch/yaw/roll） */
    fun setRotation(instance: Int, pitch: Float, yaw: Float, roll: Float) {
        post { PetLibNative.nativeSceneSetRotation(ptr, instance, pitch, yaw, roll) }
    }

    /** 实例均匀缩放 */
    fun setScale(instance: Int, scale: Float) {
        post { PetLibNative.nativeSceneSetScale(ptr, instance, scale) }
    }

    /** 实例外层（半透明帽层）显隐 */
    fun setOuterLayer(instance: Int, enabled: Boolean) {
        post { PetLibNative.nativeSceneSetOuterLayer(ptr, instance, enabled) }
    }

    /** 实例注视目标（视图像素坐标），null 回正 */
    fun setGazeScreen(instance: Int, target: Pair<Float, Float>?) {
        post {
            if (target == null) {
                PetLibNative.nativeSceneSetGazeScreen(ptr, instance, 0f, 0f, false)
            } else {
                PetLibNative.nativeSceneSetGazeScreen(ptr, instance, target.first, target.second, true)
            }
        }
    }

    /** 实例注视目标（场景空间固定点，模型互看），null 回正 */
    fun setGazeScene(instance: Int, target: Triple<Float, Float, Float>?) {
        post {
            if (target == null) {
                PetLibNative.nativeSceneSetGazeScene(ptr, instance, 0f, 0f, 0f, false)
            } else {
                PetLibNative.nativeSceneSetGazeScene(
                    ptr, instance,
                    target.first, target.second, target.third, true,
                )
            }
        }
    }

    /**
     * 播放实例的动画剪辑（GLB 内嵌剪辑，按名引用）。
     */
    fun playClip(
        instance: Int,
        clip: String,
        speed: Float = 1f,
        onError: ((String) -> Unit)? = null,
    ) {
        post {
            PetLibNative.nativeSceneClipPlay(ptr, instance, clip, speed)?.let { error ->
                Log.w(TAG, "Clip rejected: $error")
                onError?.invoke(error)
            }
        }
    }

    /** 暂停剪辑 */
    fun pauseClip(instance: Int, clip: String, onError: ((String) -> Unit)? = null) {
        post {
            PetLibNative.nativeSceneClipPause(ptr, instance, clip)?.let { error ->
                Log.w(TAG, "Clip rejected: $error")
                onError?.invoke(error)
            }
        }
    }

    /** 停止剪辑 */
    fun stopClip(instance: Int, clip: String, onError: ((String) -> Unit)? = null) {
        post {
            PetLibNative.nativeSceneClipStop(ptr, instance, clip)?.let { error ->
                Log.w(TAG, "Clip rejected: $error")
                onError?.invoke(error)
            }
        }
    }

    fun setClipSpeed(instance: Int, clip: String, speed: Float, onError: ((String) -> Unit)? = null) {
        post {
            PetLibNative.nativeSceneClipSetSpeed(ptr, instance, clip, speed)?.let { error ->
                Log.w(TAG, "Clip rejected: $error")
                onError?.invoke(error)
            }
        }
    }

    fun setClipLoop(instance: Int, clip: String, loop: Boolean, onError: ((String) -> Unit)? = null) {
        post {
            PetLibNative.nativeSceneClipSetLoop(ptr, instance, clip, loop)?.let { error ->
                Log.w(TAG, "Clip rejected: $error")
                onError?.invoke(error)
            }
        }
    }

    /** 混合权重，用于多剪辑并行混合 */
    fun setClipWeight(
        instance: Int,
        clip: String,
        @FloatRange(0.0, 1.0) weight: Float,
        onError: ((String) -> Unit)? = null,
    ) {
        post {
            PetLibNative.nativeSceneClipSetWeight(ptr, instance, clip, weight)?.let { error ->
                Log.w(TAG, "Clip rejected: $error")
                onError?.invoke(error)
            }
        }
    }

    /** 查询剪辑是否播放中 */
    fun isClipPlaying(instance: Int, clip: String, onResult: (Boolean) -> Unit) {
        post { onResult(PetLibNative.nativeSceneClipIsPlaying(ptr, instance, clip)) }
    }

    /** 播放实例的点按压扁动画；[now] 为兼容保留参数，内部按帧间隔计时 */
    fun squashTap(instance: Int, now: Float) {
        post { PetLibNative.nativeSceneSquashTap(ptr, instance, now) }
    }

    /** 播放实例的点击反应动画，播放中重复调用时无效果 */
    fun playInteract(instance: Int) {
        post { PetLibNative.nativeScenePlayInteract(ptr, instance) }
    }

    /**
     * 归一化坐标 (0..1) 命中测试。
     * 经 [onResult] 异步返回命中的实例句柄（null 为未命中）。
     */
    fun hitTest(x: Float, y: Float, onResult: (Int?) -> Unit) {
        post {
            val id = PetLibNative.nativeSceneHitTest(ptr, x, y)
            onResult(id.takeIf { it >= 0 })
        }
    }

    /** 渲染循环控制 */
    internal fun attachSurface(surface: Surface, width: Int, height: Int) {
        // surface 生命周期全部排队到渲染线程执行，与逐帧回调串行，
        // 避免一帧正持有旧surface 时它被另一线程释放。
        post {
            widthHint.set(width)
            heightHint.set(height)
            // 视图附着、surface 创建与尺寸变化回调会接连触发，
            // 同一 surface 的重复附着不再重复登记
            if (attachedSurface === surface && surfaceRef.get() != null) {
                startFrameLoop()
                return@post
            }
            // surface 由宿主视图持有，这里只借用，不负责释放
            surfaceRef.set(surface)
            attachedSurface = surface
            surfaceFailures = 0
            lastRenderNanos = 0L
            startFrameLoop()
        }
    }

    internal fun updateSurfaceSize(width: Int, height: Int) {
        widthHint.set(width)
        heightHint.set(height)
    }

    internal fun detachSurface() {
        post {
            if (surfaceRef.getAndSet(null) != null) {
                attachedSurface = null
            }
            stopFrameLoop()
        }
    }

    override fun close() {
        if (released) return
        released = true

        mainHandler.post {
            frameCallback?.let { Choreographer.getInstance().removeFrameCallback(it) }
            frameCallback = null
        }
        handler.post {
            surfaceRef.set(null)
            attachedSurface = null
            PetLibNative.nativeSceneDestroy(ptr)
            ptr = 0
            backBuffer = null
            frontBuffer = null
        }
        handler.looper.quitSafely()
    }

    /**
     * 消费模型句柄并排队加入场景。
     */
    private fun addModel(model: Long, onAdded: ((Int) -> Unit)?, onError: ((String) -> Unit)?) {
        if (released || ptr == 0L) {
            PetLibNative.nativeModelDestroy(model)
            throw IllegalStateException("Controller is released.")
        }
        handler.post {
            if (released || ptr == 0L) {
                // 创建后、入队前控制器被释放：句柄由本块回收
                PetLibNative.nativeModelDestroy(model)
                return@post
            }
            val id = PetLibNative.nativeSceneAddModel(ptr, model)
            if (id < 0) {
                PetLibNative.nativeModelDestroy(model)
                Log.w(TAG, "Scene rejected model: handle invalid")
                onError?.invoke("Scene handle is invalid.")
            } else {
                onAdded?.invoke(id)
            }
        }
    }

    private fun post(block: () -> Unit) {
        if (released) return

        handler.post {
            if (!released && ptr != 0L) block()
        }
    }

    private fun startFrameLoop() {
        mainHandler.post {
            if (frameCallback != null || released) return@post
            val choreographer = Choreographer.getInstance()
            val callback = object : Choreographer.FrameCallback {
                override fun doFrame(nanos: Long) {
                    if (frameCallback !== this || released) return
                    if (frameInFlight.compareAndSet(false, true)) {
                        handler.post {
                            try {
                                renderOneFrame()
                            } finally {
                                frameInFlight.set(false)
                            }
                        }
                    }
                    choreographer.postFrameCallback(this)
                }
            }
            frameCallback = callback
            choreographer.postFrameCallback(callback)
        }
    }

    private fun stopFrameLoop() {
        mainHandler.post {
            frameCallback?.let { Choreographer.getInstance().removeFrameCallback(it) }
            frameCallback = null
        }
    }

    private fun onSurfaceFailure(surface: Surface, error: Exception? = null) {
        surfaceFailures++
        if (error != null) {
            Log.w(TAG, "Frame failed: $error")
        }
        if (surfaceFailures >= SURFACE_FAILURE_LIMIT && surfaceRef.compareAndSet(surface, null)) {
            // 连续失败判定废弃并停帧
            attachedSurface = null
            stopFrameLoop()
            Log.w(TAG, "Surface unusable after $surfaceFailures consecutive failures; frame loop stopped")
        }
    }

    private fun renderOneFrame() {
        val surface = surfaceRef.get() ?: return
        val width = widthHint.get()
        val height = heightHint.get()
        if (width <= 0 || height <= 0) return

        // 帧率上限：跳过的帧不推进计时基准，帧间隔随之累积，动画按真实时间推进不受采样率影响
        val startedNanos = System.nanoTime()
        if (lastRenderNanos != 0L && startedNanos - lastRenderNanos < MIN_FRAME_INTERVAL_NANOS) {
            return
        }

        // 帧计时取自渲染线程的真实出帧间隔，帧率下降时动画仍按真实时间推进
        val dt = if (lastRenderNanos == 0L) {
            1f / 60f
        } else {
            ((startedNanos - lastRenderNanos) / 1_000_000_000f).coerceIn(0.001f, 0.25f)
        }
        lastRenderNanos = startedNanos

        // surface 被系统侧失效时，lockCanvas 可能抛异常，也可能只返回 null 并由
        // native 层打印错误。先锁画布再渲染，失败帧不产生渲染开销。
        val canvas = try {
            surface.lockCanvas(null)
        } catch (e: IllegalStateException) {
            onSurfaceFailure(surface, e)
            return
        } catch (e: IllegalArgumentException) {
            onSurfaceFailure(surface, e)
            return
        } ?: run {
            onSurfaceFailure(surface)
            return
        }
        try {
            var back = backBuffer
            if (back == null || back.bitmap.width != width || back.bitmap.height != height) {
                back = FrameBuffer(createBitmap(width, height))
                backBuffer = back
            }
            // 渲染直写位图像素，位图为预乘 RGBA，随 SRC 模式整幅覆写画布
            PetLibNative.nativeSceneRender(ptr, width, height, dt, back.bitmap)
            // 交换：刚完成的帧转为待提交，旧的前台缓冲留给下一轮光栅
            val previousFront = frontBuffer
            frontBuffer = back
            backBuffer = previousFront
            canvas.drawBitmap(back.bitmap, 0f, 0f, overwritePaint)
            surface.unlockCanvasAndPost(canvas)
        } catch (e: IllegalArgumentException) {
            // 原生渲染失败或 surface 在锁画布与提交之间被废弃。先释放画布锁，
            // 单帧失败不得级联堵死后续帧，再按 surface 故障计数处理。
            try {
                surface.unlockCanvasAndPost(canvas)
            } catch (suppressed: Exception) {
                e.addSuppressed(suppressed)
            }
            onSurfaceFailure(surface, e)
            return
        } catch (e: IllegalStateException) {
            try {
                surface.unlockCanvasAndPost(canvas)
            } catch (suppressed: Exception) {
                e.addSuppressed(suppressed)
            }
            onSurfaceFailure(surface, e)
            return
        }
        surfaceFailures = 0

        val costMs = (System.nanoTime() - startedNanos) / 1_000_000L
        if (costMs > 100 && startedNanos - lastSlowLogNanos > 3_000_000_000L) {
            lastSlowLogNanos = startedNanos
            Log.i(TAG, "Slow frame: ${costMs}ms (surface ${width}x${height})")
        }
    }

    /** 帧缓冲：原生侧渲染结果直接落入位图像素 */
    private class FrameBuffer(val bitmap: Bitmap)
}
