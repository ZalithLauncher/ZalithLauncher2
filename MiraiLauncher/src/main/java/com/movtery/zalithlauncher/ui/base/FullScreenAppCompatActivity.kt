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

package com.movtery.zalithlauncher.ui.base

import android.os.Build
import android.os.Build.VERSION_CODES
import android.os.Bundle
import android.view.InputDevice
import android.view.MotionEvent
import android.view.View
import android.view.View.OnSystemUiVisibilityChangeListener
import android.view.WindowManager
import androidx.activity.compose.LocalActivity
import androidx.annotation.CallSuper
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import kotlin.math.abs

abstract class FullScreenAppCompatActivity : AbstractAppCompatActivity() {
    /** 当前手势的 raw 坐标与局部坐标是否分属不同坐标系，需要重建事件 */
    private var correctTouchCoordinates = false

    /**
     * @return 决定是否忽略前置摄像头区域
     */
    protected open fun isIgnoreNotch(): Boolean = true

    @CallSuper
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        applyFullscreen()
    }

    @CallSuper
    override fun onPostResume() {
        super.onPostResume()
        applyFullscreen()
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        if (!event.isFromSource(InputDevice.SOURCE_TOUCHSCREEN)) {
            return super.dispatchTouchEvent(event)
        }

        val action = event.actionMasked
        if (action == MotionEvent.ACTION_DOWN) {
            correctTouchCoordinates = hasCoordinateSpaceMismatch(event)
        } else if (!correctTouchCoordinates &&
            action == MotionEvent.ACTION_POINTER_DOWN && hasCoordinateSpaceMismatch(event)) {
            // 窗口位置可能在首指按下后才稳定，此时再次判断。
            correctTouchCoordinates = true
        }

        val handled = if (correctTouchCoordinates) {
            val corrected = event.copyWithConsistentCoordinates()
            try {
                super.dispatchTouchEvent(corrected)
            } finally {
                corrected.recycle()
            }
        } else {
            super.dispatchTouchEvent(event)
        }

        if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
            correctTouchCoordinates = false
        }
        return handled
    }

    /**
     * 检测事件中各指针的 raw 坐标是否偏离“窗口局部坐标 + 窗口在屏幕上的起点”。
     * 正常情况下两者相等；厂商缩放窗口却未同步 raw 坐标时会不相等。
     */
    private fun hasCoordinateSpaceMismatch(event: MotionEvent): Boolean {
        val origin = IntArray(2)
        window.decorView.getLocationOnScreen(origin)
        if (abs(event.rawX - (event.x + origin[0])) > 1f) return true
        if (abs(event.rawY - (event.y + origin[1])) > 1f) return true
        if (Build.VERSION.SDK_INT >= VERSION_CODES.Q) {
            // getRawX(index)/getRawY(index) 需要 Q 以上，首指已用无参版本检查过
            for (index in 1 until event.pointerCount) {
                if (abs(event.getRawX(index) - (event.getX(index) + origin[0])) > 1f) return true
                if (abs(event.getRawY(index) - (event.getY(index) + origin[1])) > 1f) return true
            }
        }
        return false
    }

    /**
     * 以“窗口局部坐标 + 窗口在屏幕上的起点”重建触摸事件，统一 raw 坐标与局部坐标的坐标系。
     */
    private fun MotionEvent.copyWithConsistentCoordinates(): MotionEvent {
        val origin = IntArray(2)
        window.decorView.getLocationOnScreen(origin)
        val properties = Array(pointerCount) { MotionEvent.PointerProperties() }
        val coordinates = Array(pointerCount) { MotionEvent.PointerCoords() }
        for (index in 0 until pointerCount) {
            getPointerProperties(index, properties[index])
            getPointerCoords(index, coordinates[index])
            // obtain 创建的事件 raw 坐标恒等于局部坐标；
            // offsetLocation 只平移局部坐标、不影响 raw 坐标，因此必须预先移位
            coordinates[index].x += origin[0]
            coordinates[index].y += origin[1]
        }
        return MotionEvent.obtain(
            downTime,
            eventTime,
            action,
            pointerCount,
            properties,
            coordinates,
            metaState,
            buttonState,
            xPrecision,
            yPrecision,
            deviceId,
            edgeFlags,
            source,
            flags,
        )
    }

    /**
     * 全屏/忽略前置摄像头区域的代码实现参考了 [Amethyst-Android](https://github.com/AngelAuraMC/Amethyst-Android/blob/9c83fc6/app_pojavlauncher/src/main/java/net/kdt/pojavlaunch/BaseActivity.java)
     *
     * 注：targetSdk 需要设置为 34，从 35 开始，Activity 会被强行加入 enableEdgeToEdge，该实现就会彻底失效
     */
    private fun applyFullscreen() {
        val decorView = window.decorView
        val visibilityChangeListener = OnSystemUiVisibilityChangeListener { visibility: Int ->
            if (!isInMultiWindowMode) {
                if ((visibility and View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                    decorView.systemUiVisibility = (
                            View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                    or View.SYSTEM_UI_FLAG_FULLSCREEN
                                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                    or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            )
                }
            } else {
                decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
            }
        }
        decorView.setOnSystemUiVisibilityChangeListener(visibilityChangeListener)
        visibilityChangeListener.onSystemUiVisibilityChange(decorView.systemUiVisibility) //call it once since the UI state may not change after the call, so the activity wont become fullscreen

        refreshIgnoreNotch()
    }

    fun refreshIgnoreNotch() {
        if (Build.VERSION.SDK_INT >= VERSION_CODES.P) {
            val mode = if (isIgnoreNotch()) {
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            } else {
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_NEVER
            }

            val params = window.attributes

            if (params.layoutInDisplayCutoutMode != mode) {
                params.layoutInDisplayCutoutMode = mode

                window.clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN)
                window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN)

                window.attributes = params
            }
        }
    }
}

/**
 * 实时监听全屏设置变化并更新刘海屏模式
 */
@Composable
fun ObserveFullScreenSetting(fullScreen: Boolean) {
    val activity = LocalActivity.current as? FullScreenAppCompatActivity ?: return
    LaunchedEffect(fullScreen) {
        activity.refreshIgnoreNotch()
    }
}
