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

package com.movtery.zalithlauncher.ui.screens.main.control_editor.edit_widget

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.movtery.inputmap.keycodes.ControlEventKeycode
import com.movtery.layer_controller.event.ClickEvent
import com.movtery.layer_controller.observable.ObservableClickEventsProvider
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.ui.components.verticalScrollWithBar
import com.movtery.zalithlauncher.ui.control.event.LAUNCHER_EVENT_SCROLL_DOWN
import com.movtery.zalithlauncher.ui.control.event.LAUNCHER_EVENT_SCROLL_DOWN_SINGLE
import com.movtery.zalithlauncher.ui.control.event.LAUNCHER_EVENT_SCROLL_UP
import com.movtery.zalithlauncher.ui.control.event.LAUNCHER_EVENT_SCROLL_UP_SINGLE
import com.movtery.zalithlauncher.ui.control.event.LAUNCHER_EVENT_SWITCH_IME
import com.movtery.zalithlauncher.ui.control.event.LAUNCHER_EVENT_SWITCH_MENU
import com.movtery.zalithlauncher.ui.screens.main.control_editor.InfoLayoutSwitchItem
import com.movtery.zalithlauncher.ui.screens.main.control_editor.InfoLayoutTextItem

private data class LauncherEventState(
    val switchIme: Boolean = false,
    val switchMenu: Boolean = false,
    val mouseLeft: Boolean = false,
    val mouseMiddle: Boolean = false,
    val mouseRight: Boolean = false,
    val mouseScrollUp: Boolean = false,
    val mouseScrollUpSingle: Boolean = false,
    val mouseScrollDown: Boolean = false,
    val mouseScrollDownSingle: Boolean = false
)

private fun computeLauncherEventState(events: List<ClickEvent>): LauncherEventState {
    var switchIme = false
    var switchMenu = false
    var mouseLeft = false
    var mouseMiddle = false
    var mouseRight = false
    var mouseScrollUp = false
    var mouseScrollUpSingle = false
    var mouseScrollDown = false
    var mouseScrollDownSingle = false
    events.forEach { event ->
        if (event.type == ClickEvent.Type.LauncherEvent) {
            if (!switchIme) switchIme = event.key == LAUNCHER_EVENT_SWITCH_IME
            if (!switchMenu) switchMenu = event.key == LAUNCHER_EVENT_SWITCH_MENU
            if (!mouseLeft) mouseLeft = event.key == ControlEventKeycode.GLFW_MOUSE_BUTTON_LEFT
            if (!mouseMiddle) mouseMiddle = event.key == ControlEventKeycode.GLFW_MOUSE_BUTTON_MIDDLE
            if (!mouseRight) mouseRight = event.key == ControlEventKeycode.GLFW_MOUSE_BUTTON_RIGHT
            if (!mouseScrollUp) mouseScrollUp = event.key == LAUNCHER_EVENT_SCROLL_UP
            if (!mouseScrollUpSingle) mouseScrollUpSingle = event.key == LAUNCHER_EVENT_SCROLL_UP_SINGLE
            if (!mouseScrollDown) mouseScrollDown = event.key == LAUNCHER_EVENT_SCROLL_DOWN
            if (!mouseScrollDownSingle) mouseScrollDownSingle = event.key == LAUNCHER_EVENT_SCROLL_DOWN_SINGLE
        }
    }
    return LauncherEventState(
        switchIme = switchIme,
        switchMenu = switchMenu,
        mouseLeft = mouseLeft,
        mouseMiddle = mouseMiddle,
        mouseRight = mouseRight,
        mouseScrollUp = mouseScrollUp,
        mouseScrollUpSingle = mouseScrollUpSingle,
        mouseScrollDown = mouseScrollDown,
        mouseScrollDownSingle = mouseScrollDownSingle
    )
}

/**
 * 启动器事件编辑
 */
@Composable
fun LauncherEventsEdit(
    provider: ObservableClickEventsProvider,
    onSendText: () -> Unit,
    modifier: Modifier = Modifier
) {
    var eventData by remember { mutableStateOf(LauncherEventState()) }

    LaunchedEffect(provider.clickEvents) {
        eventData = computeLauncherEventState(provider.clickEvents)
    }

    fun toggleEvent(value: Boolean, event: ClickEvent) {
        if (value) provider.onAddEvent(event) else provider.onRemoveEvent(event)
    }

    Column(
        modifier = Modifier
            .verticalScrollWithBar(rememberScrollState())
            .then(modifier),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 切换输入法
        InfoLayoutSwitchItem(
            modifier = Modifier.fillMaxWidth(),
            title = stringResource(R.string.game_menu_option_input_method),
            value = eventData.switchIme,
            onValueChange = { toggleEvent(it, ClickEvent(ClickEvent.Type.LauncherEvent, LAUNCHER_EVENT_SWITCH_IME)) }
        )

        // 切换菜单
        InfoLayoutSwitchItem(
            modifier = Modifier.fillMaxWidth(),
            title = stringResource(R.string.control_editor_edit_event_launcher_switch_menu),
            value = eventData.switchMenu,
            onValueChange = { toggleEvent(it, ClickEvent(ClickEvent.Type.LauncherEvent, LAUNCHER_EVENT_SWITCH_MENU)) }
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 鼠标左键
        InfoLayoutSwitchItem(
            modifier = Modifier.fillMaxWidth(),
            title = stringResource(R.string.control_editor_edit_event_launcher_mouse_left),
            value = eventData.mouseLeft,
            onValueChange = { toggleEvent(it, ClickEvent(ClickEvent.Type.LauncherEvent, ControlEventKeycode.GLFW_MOUSE_BUTTON_LEFT)) }
        )

        // 鼠标中键
        InfoLayoutSwitchItem(
            modifier = Modifier.fillMaxWidth(),
            title = stringResource(R.string.control_editor_edit_event_launcher_mouse_middle),
            value = eventData.mouseMiddle,
            onValueChange = { toggleEvent(it, ClickEvent(ClickEvent.Type.LauncherEvent, ControlEventKeycode.GLFW_MOUSE_BUTTON_MIDDLE)) }
        )

        // 鼠标右键
        InfoLayoutSwitchItem(
            modifier = Modifier.fillMaxWidth(),
            title = stringResource(R.string.control_editor_edit_event_launcher_mouse_right),
            value = eventData.mouseRight,
            onValueChange = { toggleEvent(it, ClickEvent(ClickEvent.Type.LauncherEvent, ControlEventKeycode.GLFW_MOUSE_BUTTON_RIGHT)) }
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 持续鼠标滚轮上
        InfoLayoutSwitchItem(
            modifier = Modifier.fillMaxWidth(),
            title = stringResource(R.string.control_editor_edit_event_launcher_mouse_scroll_up),
            value = eventData.mouseScrollUp,
            onValueChange = { toggleEvent(it, ClickEvent(ClickEvent.Type.LauncherEvent, LAUNCHER_EVENT_SCROLL_UP)) }
        )

        // 单次鼠标滚轮上
        InfoLayoutSwitchItem(
            modifier = Modifier.fillMaxWidth(),
            title = stringResource(R.string.control_editor_edit_event_launcher_mouse_scroll_up_single),
            value = eventData.mouseScrollUpSingle,
            onValueChange = { toggleEvent(it, ClickEvent(ClickEvent.Type.LauncherEvent, LAUNCHER_EVENT_SCROLL_UP_SINGLE)) }
        )

        // 持续鼠标滚轮下
        InfoLayoutSwitchItem(
            modifier = Modifier.fillMaxWidth(),
            title = stringResource(R.string.control_editor_edit_event_launcher_mouse_scroll_down),
            value = eventData.mouseScrollDown,
            onValueChange = { toggleEvent(it, ClickEvent(ClickEvent.Type.LauncherEvent, LAUNCHER_EVENT_SCROLL_DOWN)) }
        )

        // 单次鼠标滚轮下
        InfoLayoutSwitchItem(
            modifier = Modifier.fillMaxWidth(),
            title = stringResource(R.string.control_editor_edit_event_launcher_mouse_scroll_down_single),
            value = eventData.mouseScrollDownSingle,
            onValueChange = { toggleEvent(it, ClickEvent(ClickEvent.Type.LauncherEvent, LAUNCHER_EVENT_SCROLL_DOWN_SINGLE)) }
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 发送文本
        InfoLayoutTextItem(
            modifier = Modifier.fillMaxWidth(),
            title = stringResource(R.string.control_editor_edit_event_launcher_send_text),
            onClick = onSendText
        )
    }
}
