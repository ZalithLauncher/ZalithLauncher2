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
import android.os.Bundle
import android.view.WindowManager
import androidx.annotation.CallSuper
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

abstract class FullScreenAppCompatActivity : AbstractAppCompatActivity() {

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

    @CallSuper
    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            applyFullscreen()
        }
    }

    private fun applyFullscreen() {
        if (isInMultiWindowMode) {
            applyDefault()
        } else {
            applyFullImmersive()
        }
    }

    private fun applyFullImmersive() {
        window?.let { win ->
            // 让内容延伸到系统状态栏/导航栏区域
            WindowCompat.setDecorFitsSystemWindows(win, false)

            // 处理刘海屏 (Android 9+)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                win.attributes.layoutInDisplayCutoutMode =
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            }

            // 隐藏状态栏和导航栏，并设置沉浸式滑动显示
            WindowInsetsControllerCompat(win, win.decorView).apply {
                hide(WindowInsetsCompat.Type.systemBars())
                systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        }
    }

    private fun applyDefault() {
        window?.let { win ->
            // 分屏模式下，通常不需要内容延伸到系统栏底下
            WindowCompat.setDecorFitsSystemWindows(win, true)

            // 恢复刘海屏默认行为
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                win.attributes.layoutInDisplayCutoutMode =
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_NEVER
            }

            // 依然隐藏系统栏（对齐你原代码的逻辑）
            WindowInsetsControllerCompat(win, win.decorView).apply {
                hide(WindowInsetsCompat.Type.systemBars())
                systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        }
    }
}

/**
 * 移除了硬编码的 fillMaxSize()，让 Modifier 的职责更加单一。
 * 使用方可以根据需要自行决定是否需要 fillMaxSize()。
 */
@Composable
fun Modifier.applyFullscreen(isImmersive: Boolean): Modifier {
    return this.then(
        if (isImmersive) Modifier
        else Modifier.windowInsetsPadding(WindowInsets.displayCutout)
    )
}