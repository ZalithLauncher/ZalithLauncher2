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

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.database.ContentObserver
import android.hardware.display.DisplayManager
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.Display
import android.view.TextureView
import android.view.Window
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.movtery.zalithlauncher.setting.AllSettings
import com.movtery.zalithlauncher.setting.enums.isLauncherInDarkTheme
import com.movtery.zalithlauncher.utils.festival.Festival

@Composable
fun FestivalEffects(
    festivals: List<Festival>,
    modifier: Modifier = Modifier,
) {
    val enableEffects = AllSettings.launcherFestivalEffects.state
    val effectTypes = remember(festivals) { FestivalEffectType.of(festivals) }
    if (!enableEffects || effectTypes.isEmpty()) return

    val isDark = isLauncherInDarkTheme()
    if (rememberAnimationsRemoved()) return

    val inForeground = rememberInForeground()

    val engine = remember { FestivalEffectsEngine() }

    DisposableEffect(engine) {
        onDispose { engine.release() }
    }

    val tapsEnabled = effectTypes.any(FestivalEffectType::supportsTapBurst)
    DisposableEffect(tapsEnabled) {
        val unsubscribe = if (tapsEnabled) {
            FestivalTapObserver.subscribe { x, y ->
                engine.burstAt(x, y)
            }
        } else {
            null
        }
        onDispose {
            unsubscribe?.invoke()
        }
    }

    AndroidView(
        modifier = modifier,
        factory = { context ->
            TextureView(context).apply {
                isOpaque = false
                engine.attach(this)
            }
        }
    )

    LaunchedEffect(effectTypes, isDark) {
        engine.applyConfig(effectTypes, isDark)
    }

    LaunchedEffect(inForeground) {
        engine.setActive(inForeground)
    }

    // 效果运行期间保持当前刷新率
    val context = LocalContext.current
    DisposableEffect(inForeground) {
        if (inForeground) {
            val display = context.getSystemService(DisplayManager::class.java)
                ?.getDisplay(Display.DEFAULT_DISPLAY)

            val rate = display?.mode?.refreshRate ?: 0f
            val window = context.findActivityWindow()
            if (window != null && rate > 0f) {
                window.attributes = window.attributes.apply {
                    preferredRefreshRate = rate
                }
            }
        }
        onDispose {
            val window = context.findActivityWindow()
            if (window != null && window.attributes.preferredRefreshRate != 0f) {
                window.attributes = window.attributes.apply {
                    preferredRefreshRate = 0f
                }
            }
        }
    }
}

/**
 * @return 系统是否关闭了全部动画
 */
@Composable
private fun rememberAnimationsRemoved(): Boolean {
    val context = LocalContext.current
    return produceState(readAnimationsRemoved(context), context) {
        val observer = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean) {
                value = readAnimationsRemoved(context)
            }
        }
        context.contentResolver.registerContentObserver(
            Settings.Global.getUriFor(Settings.Global.ANIMATOR_DURATION_SCALE),
            false,
            observer
        )
        awaitDispose {
            context.contentResolver.unregisterContentObserver(observer)
        }
    }.value
}

private fun readAnimationsRemoved(context: Context): Boolean {
    return Settings.Global.getFloat(
        context.contentResolver,
        Settings.Global.ANIMATOR_DURATION_SCALE,
        1f
    ) == 0f
}

/**
 * @return 启动器是否在前台
 */
@Composable
private fun rememberInForeground(): Boolean {
    val lifecycleOwner = LocalLifecycleOwner.current
    return produceState(initialValue = true, lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> value = true
                Lifecycle.Event.ON_STOP -> value = false
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        awaitDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }.value
}

private tailrec fun Context.findActivityWindow(): Window? = when (this) {
    is Activity -> window
    is ContextWrapper -> baseContext.findActivityWindow()
    else -> null
}