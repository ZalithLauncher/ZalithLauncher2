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
import androidx.compose.foundation.layout.captionBar
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.union
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
        applyFullImmersive()
    }

    private fun applyFullImmersive() {
        // 让应用内容延伸到系统栏（状态栏、导航栏）下方
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // 使用 WindowInsetsControllerCompat 控制系统栏的显示与隐藏
        val insetsController = WindowCompat.getInsetsController(window, window.decorView)
        insetsController.apply {
            // 隐藏状态栏和导航栏
            hide(WindowInsetsCompat.Type.systemBars())
            // 对应原有的 SYSTEM_UI_FLAG_IMMERSIVE_STICKY，滑动边缘可短暂显示系统栏
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }

        // 处理刘海屏/挖孔屏区域的延伸 (API 28+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val attributes = window.attributes
            attributes.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            window.attributes = attributes
        }
    }
}

@Composable
fun Modifier.applyFullscreen(value: Boolean): Modifier = this
    .fillMaxSize()
    .windowInsetsPadding(
        if (value) {
            WindowInsets.captionBar
        } else {
            WindowInsets.captionBar.union(WindowInsets.displayCutout)
        }
    )