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
    private var correctScaledMultiTouch = false

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
            // 部分设备的窗口坐标经过缩放，而 raw 坐标仍是物理屏幕坐标。
            correctScaledMultiTouch = isScaledWindowCoordinateSpace()
        } else if (!correctScaledMultiTouch &&
            action == MotionEvent.ACTION_POINTER_DOWN && hasRawToLocalScaleMismatch(event)) {
            // 窗口尺寸尚未稳定时，在第二指到达后再次判断。
            correctScaledMultiTouch = true
        }

        val handled = if (correctScaledMultiTouch) {
            val corrected = event.copyWithLocalCoordinatesAndOriginalOffset()
            try {
                super.dispatchTouchEvent(corrected)
            } finally {
                corrected.recycle()
            }
        } else {
            super.dispatchTouchEvent(event)
        }

        if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
            correctScaledMultiTouch = false
        }
        return handled
    }

    private fun hasRawToLocalScaleMismatch(event: MotionEvent): Boolean {
        if (Build.VERSION.SDK_INT < VERSION_CODES.Q || event.pointerCount < 2) return false

        val localDeltaX = event.getX(1) - event.getX(0)
        val localDeltaY = event.getY(1) - event.getY(0)
        val rawDeltaX = event.getRawX(1) - event.getRawX(0)
        val rawDeltaY = event.getRawY(1) - event.getRawY(0)
        val mismatch = abs(rawDeltaX - localDeltaX) > 1f || abs(rawDeltaY - localDeltaY) > 1f
        return mismatch
    }

    /** 检测窗口局部坐标系与物理屏幕坐标系之间是否存在缩放差异 */
    private fun isScaledWindowCoordinateSpace(): Boolean {
        if (Build.VERSION.SDK_INT < VERSION_CODES.R) return false
        val display = display ?: return false
        val physicalWidth = display.mode.physicalWidth
        val physicalHeight = display.mode.physicalHeight
        val decor = window.decorView
        if (physicalWidth <= 0 || physicalHeight <= 0 || decor.width <= 0 || decor.height <= 0) {
            return false
        }
        val directWidthScale = decor.width.toFloat() / physicalWidth
        val directHeightScale = decor.height.toFloat() / physicalHeight
        val swappedWidthScale = decor.width.toFloat() / physicalHeight
        val swappedHeightScale = decor.height.toFloat() / physicalWidth
        val directError = abs(directWidthScale - 1f) + abs(directHeightScale - 1f)
        val swappedError = abs(swappedWidthScale - 1f) + abs(swappedHeightScale - 1f)
        val widthScale: Float
        val heightScale: Float
        if (directError <= swappedError) {
            widthScale = directWidthScale
            heightScale = directHeightScale
        } else {
            widthScale = swappedWidthScale
            heightScale = swappedHeightScale
        }
        return abs(widthScale - 1f) > 0.01f || abs(heightScale - 1f) > 0.01f
    }

    private fun MotionEvent.copyWithLocalCoordinatesAndOriginalOffset(): MotionEvent {
        val properties = Array(pointerCount) { MotionEvent.PointerProperties() }
        val coordinates = Array(pointerCount) { MotionEvent.PointerCoords() }
        for (index in 0 until pointerCount) {
            getPointerProperties(index, properties[index])
            getPointerCoords(index, coordinates[index])
        }
        val rootLocation = IntArray(2)
        window.decorView.getLocationOnScreen(rootLocation)
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
        ).also {
            // 让 raw 坐标与窗口局部坐标使用同一缩放比例。
            it.offsetLocation(rootLocation[0].toFloat(), rootLocation[1].toFloat())
        }
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
