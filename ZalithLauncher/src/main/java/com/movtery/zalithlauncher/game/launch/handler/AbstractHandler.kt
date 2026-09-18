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

package com.movtery.zalithlauncher.game.launch.handler

import android.view.KeyEvent
import android.view.Surface
import androidx.annotation.CallSuper
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import com.movtery.zalithlauncher.game.launch.Launcher
import com.movtery.zalithlauncher.ui.control.input.TextInputMode
import com.movtery.zalithlauncher.viewmodel.ErrorViewModel
import com.movtery.zalithlauncher.viewmodel.EventViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

abstract class AbstractHandler(
    val type: HandlerType,
    protected val errorViewModel: ErrorViewModel,
    protected val eventViewModel: EventViewModel,
    val launcher: Launcher,
    val onExit: (code: Int) -> Unit
) {
    var mIsSurfaceDestroyed: Boolean = false
    open val inputArea: StateFlow<IntRect?> = MutableStateFlow(null)

    @CallSuper
    open suspend fun execute(
        surface: Surface,
        screenSize: IntSize,
        scope: CoroutineScope
    ) {
        scope.launch(Dispatchers.Default) {
            val code = launcher.launch(screenSize)
            onExit(code)
        }
    }

    abstract fun onPause()
    abstract fun onResume()
    abstract fun onDestroy()
    abstract fun onGraphicOutput()
    abstract fun shouldIgnoreKeyEvent(event: KeyEvent): Boolean
    abstract fun sendMouseRight(isPressed: Boolean)

    @Composable
    abstract fun ComposableLayout(
        textInputMode: TextInputMode
    )
}