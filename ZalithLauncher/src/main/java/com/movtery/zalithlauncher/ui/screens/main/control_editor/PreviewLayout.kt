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

package com.movtery.zalithlauncher.ui.screens.main.control_editor

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateSetOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerId
import androidx.compose.ui.unit.IntSize
import com.movtery.layer_controller.ControlBoxLayout
import com.movtery.layer_controller.data.HideLayerWhen
import com.movtery.layer_controller.observable.ObservableControlLayout
import com.movtery.zalithlauncher.setting.enums.isLauncherInDarkTheme
import com.movtery.zalithlauncher.ui.components.rememberBoxSize
import com.movtery.zalithlauncher.ui.control.mouse.SwitchableMouseLayout

/**
 * 预览控制布局层
 * @param observableLayout 被预览的控制布局
 * @param previewScenario 控制布局预览的场景
 * @param previewHideLayerWhen  控制布局预览时，模拟当前使用的设备
 *                              控件层会根据该值决定是否隐藏
 */
@Composable
fun BoxWithConstraintsScope.PreviewControlBox(
    observableLayout: ObservableControlLayout,
    previewScenario: PreviewScenario,
    previewHideLayerWhen: HideLayerWhen,
    modifier: Modifier = Modifier,
) {
    val occupiedPointers = remember(observableLayout) { mutableStateSetOf<PointerId>() }
    val moveOnlyPointers = remember(observableLayout) { mutableStateSetOf<PointerId>() }

    val screenSize = rememberBoxSize()

    ControlBoxLayout(
        modifier = modifier.fillMaxSize(),
        observedLayout = observableLayout,
        checkOccupiedPointers = { occupiedPointers.contains(it) },
        markPointerAsMoveOnly = { moveOnlyPointers.add(it) },
        isCursorGrabbing = previewScenario.isCursorGrabbing,
        enableJoystick = true,
        hideLayerWhen = previewHideLayerWhen,
        isDark = isLauncherInDarkTheme()
    ) {
        PreviewMouseLayout(
            modifier = Modifier.fillMaxSize(),
            screenSize = screenSize,
            isMoveOnlyPointer = { moveOnlyPointers.contains(it) },
            onOccupiedPointer = { occupiedPointers.add(it) },
            onReleasePointer = {
                occupiedPointers.remove(it)
                moveOnlyPointers.remove(it)
            },
            previewScenario = previewScenario
        )
    }
}

/**
 * 预览鼠标控制层
 * @param isMoveOnlyPointer 检查指针是否被标记为仅处理滑动事件
 * @param onOccupiedPointer 标记指针已被占用
 * @param onReleasePointer 标记指针已被释放
 * @param previewScenario 控制布局预览的场景
 */
@Composable
private fun PreviewMouseLayout(
    modifier: Modifier = Modifier,
    screenSize: IntSize,
    isMoveOnlyPointer: (PointerId) -> Boolean,
    onOccupiedPointer: (PointerId) -> Unit,
    onReleasePointer: (PointerId) -> Unit,
    previewScenario: PreviewScenario
) {
    Box(
        modifier = modifier
    ) {
        SwitchableMouseLayout(
            modifier = Modifier.fillMaxSize(),
            screenSize = screenSize,
            cursorMode = previewScenario.cursorMode,
            isMoveOnlyPointer = isMoveOnlyPointer,
            onOccupiedPointer = onOccupiedPointer,
            onReleasePointer = onReleasePointer
        )
    }
}