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
import kotlin.math.PI

private const val TAG = "PetLib"

/** 连续锁画布失败达到该次数后，判定表面已失效并停止帧驱动 */
private const val SURFACE_FAILURE_LIMIT = 10

/**
 * 帧间隔下限
 */
private const val MIN_FRAME_INTERVAL_NANOS = 1_000_000_000L / 60L * 9L / 10L

/** 归一化到 (-π, π]，供最短路径回正 */
private fun wrapPi(angle: Float): Float {
    val twoPi = (2 * PI).toFloat()
    val a = angle % twoPi
    return when {
        a > PI.toFloat() -> a - twoPi
        a < -PI.toFloat() -> a + twoPi
        else -> a
    }
}

/**
 * PetLib 控制器
 *
 * 内部独占一个渲染线程；除 release 外所有方法都可从任意线程调用，
 * 实际的原生调用全部排队到该线程执行。释放后调用任何方法均为无操作。
 *
 * 若原生库未打包（构建时跳过了 Rust 编译），控制器退化为无操作，不会崩溃。
 *
 * @param skin 初始皮肤数据
 * @param arm 手臂模型，null 自动检测
 * @param defaultYaw 默认偏航角（弧度），回正回到该角度
 * @throws IllegalArgumentException 皮肤非法或解析失败
 */
class PetController(
    skin: ByteArray,
    arm: ArmModel?,
    private val defaultYaw: Float = 0f
) : AutoCloseable {
    private val thread = HandlerThread("PetLibRender").apply { start() }
    private val handler = Handler(thread.looper)
    private val mainHandler = Handler(Looper.getMainLooper())

    // ptr == 0 表示原生库不可用或已释放，post() 会据此丢弃所有原生调用
    @Volatile
    private var ptr: Long = if (PetLibNative.AVAILABLE) {
        PetLibNative.nativeCreate(skin, ArmModel.codeOf(arm))
    } else {
        0L
    }

    @Volatile
    private var released = false

    // 偏航目标角的调用侧镜像，供增量旋转直接累加
    @Volatile
    private var yawTarget = 0f

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

    init {
        setModelYaw(defaultYaw)
    }

    /**
     * 替换皮肤
     * @param skin PNG 皮肤数据
     * @param arm 手臂模型类别，为 null 则自动检测类型
     * @param onError 失败时在渲染线程的回调
     */
    fun setSkin(
        skin: ByteArray,
        arm: ArmModel?,
        onError: ((String) -> Unit)? = null
    ) {
        post {
            PetLibNative.nativeSetSkin(ptr, skin, ArmModel.codeOf(arm))?.let { error ->
                Log.w(TAG, "Skin rejected: $error")
                onError?.invoke(error)
            }
        }
    }

    /**
     * 替换披风
     * @param cape 披风，为 null 则移除
     * @param onError 失败时在渲染线程的回调
     */
    fun setCape(
        cape: ByteArray?,
        onError: ((String) -> Unit)? = null
    ) {
        post {
            PetLibNative.nativeSetCape(ptr, cape)?.let { error ->
                Log.w(TAG, "Cape rejected: $error")
                onError?.invoke(error)
            }
        }
    }

    /**
     * 方向光照参数
     * @param azimuth 光源方位角（度）
     * @param elevation 光源仰角（度）
     * @param brightness 亮度百分比，100 为原始色
     * @param softness 柔和度百分比，越高明暗对比越平缓
     */
    fun setLighting(
        azimuth: Float,
        elevation: Float,
        brightness: Float,
        softness: Float
    ) {
        post { PetLibNative.nativeSetLighting(ptr, azimuth, elevation, brightness, softness) }
    }

    /**
     * 超采样系数
     * @param value 系数
     */
    fun setSuperSample(@FloatRange(0.5, 2.0) value: Float) {
        post { PetLibNative.nativeSetSuperSample(ptr, value) }
    }

    /**
     * 是否开启双层皮肤
     */
    fun setOuterLayer(enabled: Boolean) {
        post { PetLibNative.nativeSetOuterLayer(ptr, enabled) }
    }

    /**
     * 姿态平滑时间常数（秒），帧率无关的指数平滑
     * @param gaze 注视追随
     * @param yaw 整体偏航
     * @param anim 动画切换的交叉过渡
     * @param recenter 头部回正
     */
    fun setTau(
        gaze: Float,
        yaw: Float,
        anim: Float,
        recenter: Float
    ) {
        post { PetLibNative.nativeSetTau(ptr, gaze, yaw, anim, recenter) }
    }

    /**
     * 注视夹角限制，超出的注视增量被截断
     * @param x 水平夹角上限（度）
     * @param y 垂直夹角上限（度）
     */
    fun setHeadRange(
        x: Float,
        y: Float
    ) {
        post { PetLibNative.nativeSetHeadRange(ptr, x, y) }
    }

    /** 设置压扁动画深度百分比 */
    fun setSquashDepth(@FloatRange(.0, 30.0) depth: Float) {
        post { PetLibNative.nativeSetSquashDepth(ptr, depth) }
    }

    /**
     * 注视目标（视图像素坐标），null 表示回正。
     */
    fun setGaze(target: Pair<Float, Float>?) {
        post {
            if (target == null) {
                PetLibNative.nativeSetGaze(ptr, 0f, 0f, false)
            } else {
                PetLibNative.nativeSetGaze(ptr, target.first, target.second, true)
            }
        }
    }

    /** 整体偏航目标角（弧度） */
    fun setModelYaw(yaw: Float) {
        yawTarget = yaw
        post { PetLibNative.nativeSetYaw(ptr, yaw) }
    }

    /**
     * 在当前偏航目标角上叠加增量（弧度），供拖拽旋转
     * @param delta 偏航增量（弧度）
     */
    fun rotateYaw(delta: Float) {
        val next = yawTarget + delta
        yawTarget = next
        post { PetLibNative.nativeSetYaw(ptr, next) }
    }

    /** 回正：从当前姿态以最短路径转回默认偏航角 */
    fun recenterYaw() {
        post {
            val current = PetLibNative.nativeGetYaw(ptr)
            val next = current + wrapPi(defaultYaw - current)
            yawTarget = next
            PetLibNative.nativeSetYaw(ptr, next)
        }
    }

    /** 查询当前偏航角，结果异步回到调用方的回调 */
    fun getModelYaw(onResult: (Float) -> Unit) {
        post { onResult(PetLibNative.nativeGetYaw(ptr)) }
    }

    /**
     * 播放循环动画
     * @param animation 动画预设
     * @param speed 播放速度倍率
     * @param headFollow 播放期间头部是否追随注视输入
     */
    fun playAnimation(animation: PetAnimation, speed: Float = 1f, headFollow: Boolean = true) {
        post { PetLibNative.nativePlayAnimation(ptr, animation.id, speed, headFollow) }
    }

    /** 停止动画 */
    fun stopAnimation() {
        post { PetLibNative.nativeStopAnimation(ptr) }
    }

    /**
     * 播放点按压扁动画
     * @param now 兼容保留；内部按帧间隔计时，不读取该值
     */
    fun squashTap(now: Float) {
        post { PetLibNative.nativeSquashTap(ptr, now) }
    }

    /**
     * 播放攻击摆臂动画
     * @param now 兼容保留；内部按帧间隔计时，不读取该值
     */
    fun playSwing(now: Float) {
        post { PetLibNative.nativePlaySwing(ptr, now) }
    }

    /**
     * 播放点击反应动画，播放中重复调用时无效果
     */
    fun playInteract() {
        post { PetLibNative.nativePlayInteract(ptr) }
    }

    /**
     * 归一化坐标命中测试，结果异步回到调用方的回调
     * @param x 横坐标（0..1）
     * @param y 纵坐标（0..1）
     * @param onResult 命中结果回调，在渲染线程上执行
     */
    fun hitTest(x: Float, y: Float, onResult: (Boolean) -> Unit) {
        post { onResult(PetLibNative.nativeHitTest(ptr, x, y)) }
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
            if (ptr != 0L) PetLibNative.nativeDestroy(ptr)
            ptr = 0
            backBuffer = null
            frontBuffer = null
        }
        handler.looper.quitSafely()
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
        // 原生库不可用或已释放：不渲染
        if (ptr == 0L) return
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
            PetLibNative.nativeRender(ptr, width, height, startedNanos / 1_000_000_000f, dt, back.bitmap)
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
