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

import android.graphics.SurfaceTexture
import android.view.Surface
import android.view.TextureView
import android.view.View
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlin.math.PI

/** 点按模型命中时的反馈形式 */
enum class PetTapReaction {
    /** 无反馈 */
    None,
    /** 压扁脉冲 */
    Squash,
    /** 点击反应动画 */
    Interact
}

/**
 * 玩家模型预览组件
 *
 * @param tapReaction 点按模型命中时的反馈形式
 * @param dragYaw 是否响应拖拽旋转偏航
 */
@Composable
fun PetPreview(
    controller: PetController,
    modifier: Modifier = Modifier,
    tapReaction: PetTapReaction = PetTapReaction.Squash,
    dragYaw: Boolean = true,
) {
    val gestureModifier = Modifier.let {
        if (tapReaction != PetTapReaction.None) it.then(Modifier.pointerInput(controller, tapReaction) {
            detectTapGestures(
                onPress = { offset ->
                    if (size.width > 0 && size.height > 0) {
                        controller.hitTest(
                            offset.x / size.width,
                            offset.y / size.height
                        ) { hit ->
                            if (hit) {
                                when (tapReaction) {
                                    PetTapReaction.Squash -> controller.squashTap(System.nanoTime() / 1_000_000_000f)
                                    PetTapReaction.Interact -> controller.playInteract()
                                    PetTapReaction.None -> Unit
                                }
                            }
                        }
                    }
                }
            )
        }) else it
    }.let {
        if (dragYaw) it.then(Modifier.pointerInput(controller) {
            // 整宽水平拖拽对应一整圈偏航
            detectDragGestures { change, dragAmount ->
                change.consume()
                if (size.width > 0) {
                    controller.rotateYaw(dragAmount.x / size.width * 2f * PI.toFloat())
                }
            }
        }) else it
    }

    var textureView by remember { mutableStateOf<TextureView?>(null) }
    AndroidView(
        modifier = modifier.then(gestureModifier),
        factory = { context ->
            TextureView(context).apply {
                isOpaque = false
                textureView = this
            }
        }
    )

    DisposableEffect(controller, textureView) {
        val view = textureView
        if (view == null) {
            onDispose { }
        } else {
            val listener = TextureLifecycle(controller)
            view.surfaceTextureListener = listener
            // 视图附着即渲染、分离即停止：窗口分离时宿主会废弃底层缓冲队列，
            // 该信号不依赖导航实现与生命周期分发，是停帧的最后防线。
            val attachStateListener = object : View.OnAttachStateChangeListener {
                override fun onViewAttachedToWindow(v: View) {
                    val st = (v as? TextureView)?.surfaceTexture ?: return
                    listener.ensureAttached(st, v.width, v.height)
                }

                override fun onViewDetachedFromWindow(v: View) {
                    controller.detachSurface()
                }
            }
            view.addOnAttachStateChangeListener(attachStateListener)
            // 视图复用时 SurfaceTexture 可能已就绪，直接补挂。
            view.surfaceTexture?.let { st ->
                listener.ensureAttached(st, view.width, view.height)
            }
            onDispose {
                view.removeOnAttachStateChangeListener(attachStateListener)
                view.surfaceTextureListener = null
                controller.detachSurface()
            }
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, controller, textureView) {
        val observer = LifecycleEventObserver { _, event ->
            val view = textureView ?: return@LifecycleEventObserver
            when (event) {
                Lifecycle.Event.ON_PAUSE -> {
                    controller.detachSurface()
                }
                Lifecycle.Event.ON_RESUME -> {
                    val st = view.surfaceTexture ?: return@LifecycleEventObserver
                    view.surfaceTextureListener?.let { listener ->
                        (listener as? TextureLifecycle)?.ensureAttached(
                            st, view.width, view.height
                        )
                    }
                }
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
}

private class TextureLifecycle(private val controller: PetController) :
    TextureView.SurfaceTextureListener {
    private var surface: Surface? = null

    fun ensureAttached(st: SurfaceTexture, width: Int, height: Int) {
        val existing = surface
        if (existing != null) {
            controller.attachSurface(existing, width, height)
        } else {
            Surface(st).let { wrapped ->
                surface = wrapped
                controller.attachSurface(wrapped, width, height)
            }
        }
    }

    override fun onSurfaceTextureAvailable(st: SurfaceTexture, width: Int, height: Int) {
        ensureAttached(st, width, height)
    }

    override fun onSurfaceTextureSizeChanged(st: SurfaceTexture, width: Int, height: Int) {
        controller.updateSurfaceSize(width, height)
    }

    override fun onSurfaceTextureDestroyed(st: SurfaceTexture): Boolean {
        controller.detachSurface()
        surface?.release()
        surface = null
        return true
    }

    override fun onSurfaceTextureUpdated(st: SurfaceTexture) = Unit
}