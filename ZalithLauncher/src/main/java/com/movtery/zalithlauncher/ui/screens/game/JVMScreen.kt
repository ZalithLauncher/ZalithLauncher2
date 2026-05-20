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

package com.movtery.zalithlauncher.ui.screens.game

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.bridge.ZLBridge
import com.movtery.zalithlauncher.bridge.ZLNativeInvoker
import com.movtery.zalithlauncher.game.input.AWTCharSender
import com.movtery.zalithlauncher.game.input.AWTInputEvent
import com.movtery.zalithlauncher.ui.components.TouchableButton
import com.movtery.zalithlauncher.ui.control.mouse.VirtualPointerLayout
import com.movtery.zalithlauncher.ui.screens.game.elements.ForceCloseOperation
import com.movtery.zalithlauncher.ui.screens.game.elements.LogBox
import com.movtery.zalithlauncher.ui.screens.game.elements.LogState
import com.movtery.zalithlauncher.viewmodel.EventViewModel
import kotlinx.coroutines.flow.filterIsInstance

@Composable
fun JVMScreen(
    logState: LogState,
    onLogStateChange: (LogState) -> Unit,
    eventViewModel: EventViewModel
) {
    var forceCloseState by remember { mutableStateOf<ForceCloseOperation>(ForceCloseOperation.None) }

    ForceCloseOperation(
        operation = forceCloseState,
        onChange = { forceCloseState = it },
        onForceClose = {
            ZLNativeInvoker.jvmExit(0, false)
        },
        text = stringResource(R.string.game_dialog_force_close_message)
    )

    LaunchedEffect(Unit) {
        eventViewModel.events
            .filterIsInstance<EventViewModel.Event.Game.OnBack>()
            .collect {
                forceCloseState = ForceCloseOperation.Show
            }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        SimpleMouseControlLayout(
            modifier = Modifier.fillMaxSize(),
            sendMousePress = { ZLBridge.sendMousePress(AWTInputEvent.BUTTON1_DOWN_MASK) },
            sendMouseCodePress = { code, pressed ->
                ZLBridge.sendMousePress(code, pressed)
            },
            sendMouseLongPress = { isPressed ->
                ZLBridge.sendMousePress(AWTInputEvent.BUTTON1_DOWN_MASK, isPressed)
            },
            placeMouse = { mouseX, mouseY ->
                ZLBridge.sendMousePos((mouseX * 0.8).toInt(), (mouseY * 0.8).toInt())
            }
        )

        LogBox(
            enableLog = logState.value,
            onClose = {
                onLogStateChange(LogState.CLOSE)
            },
            modifier = Modifier.fillMaxSize()
        )

        ButtonsLayout(
            modifier = Modifier
                .alpha(alpha = if (logState.value) 0.5f else 1f)
                .fillMaxSize()
                .padding(8.dp),
            changeKeyboard = {
                eventViewModel.sendEvent(EventViewModel.Event.Game.SwitchIme(null))
            },
            forceCloseClick = {
                forceCloseState = ForceCloseOperation.Show
            },
            changeLogOutput = {
                onLogStateChange(logState.next())
            }
        )
    }
}

@Composable
private fun SimpleMouseControlLayout(
    modifier: Modifier = Modifier,
    sendMousePress: () -> Unit,
    sendMouseCodePress: (Int, Boolean) -> Unit,
    sendMouseLongPress: (Boolean) -> Unit,
    placeMouse: (mouseX: Float, mouseY: Float) -> Unit
) {
    VirtualPointerLayout(
        modifier = modifier,
        onTap = { sendMousePress() },
        onPointerMove = { placeMouse(it.x, it.y) },
        onLongPress = { sendMouseLongPress(true) },
        onLongPressEnd = { sendMouseLongPress(false) },
        onMouseButton = { button, pressed ->
            val code = AWTCharSender.getMouseButton(button) ?: return@VirtualPointerLayout
            sendMouseCodePress(code, pressed)
        }
    )
}

@Composable
private fun ButtonsLayout(
    modifier: Modifier = Modifier,
    changeKeyboard: () -> Unit = {},
    forceCloseClick: () -> Unit = {},
    changeLogOutput: () -> Unit = {}
) {

    ConstraintLayout(modifier = modifier) {
        val (
            input, copy, paste,
            forceClose, logOutput,
            leftClick, rightClick,
            up, right, down, left
        ) = createRefs()

        TextButton(
            modifier = Modifier.constrainAs(input) {
                top.linkTo(parent.top)
                start.linkTo(parent.start)
            },
            onClick = changeKeyboard,
            text = stringResource(R.string.game_button_input)
        )
        TextButton(
            modifier = Modifier.constrainAs(copy) {
                top.linkTo(anchor = input.bottom, margin = 8.dp)
                start.linkTo(parent.start)
            },
            onClick = {
                ZLBridge.sendKey(' ', AWTInputEvent.VK_CONTROL, 1)
                ZLBridge.sendKey(' ', AWTInputEvent.VK_C)
                ZLBridge.sendKey(' ', AWTInputEvent.VK_CONTROL, 0)
            },
            text = stringResource(R.string.generic_copy)
        )
        TextButton(
            modifier = Modifier.constrainAs(paste) {
                top.linkTo(anchor = copy.bottom, margin = 8.dp)
                start.linkTo(parent.start)
            },
            onClick = {
                ZLBridge.sendKey(' ', AWTInputEvent.VK_CONTROL, 1)
                ZLBridge.sendKey(' ', AWTInputEvent.VK_V)
                ZLBridge.sendKey(' ', AWTInputEvent.VK_CONTROL, 0)
            },
            text = stringResource(R.string.generic_paste)
        )

        TextButton(
            modifier = Modifier.constrainAs(forceClose) {
                top.linkTo(parent.top)
                end.linkTo(parent.end)
            },
            onClick = forceCloseClick,
            text = stringResource(R.string.game_button_force_close)
        )
        TextButton(
            modifier = Modifier.constrainAs(logOutput) {
                top.linkTo(anchor = forceClose.bottom, margin = 8.dp)
                end.linkTo(parent.end)
            },
            onClick = changeLogOutput,
            text = stringResource(R.string.game_button_log_output)
        )

        TouchableButton(
            modifier = Modifier.constrainAs(leftClick) {
                bottom.linkTo(anchor = rightClick.top, margin = 16.dp)
                start.linkTo(parent.start)
            },
            onTouch = { isPressed ->
                ZLBridge.sendMousePress(AWTInputEvent.BUTTON1_DOWN_MASK, isPressed)
            },
            text = stringResource(R.string.game_button_mouse_left)
        )
        TouchableButton(
            modifier = Modifier.constrainAs(rightClick) {
                bottom.linkTo(anchor = parent.bottom, margin = 8.dp)
                start.linkTo(parent.start)
            },
            onTouch = { isPressed ->
                ZLBridge.sendMousePress(AWTInputEvent.BUTTON3_DOWN_MASK, isPressed)
            },
            text = stringResource(R.string.game_button_mouse_right)
        )

        TextButton(
            modifier = Modifier.constrainAs(up) {
                bottom.linkTo(anchor = down.top, margin = 8.dp)
                end.linkTo(anchor = right.start, margin = 8.dp)
            },
            onClick = {
                ZLBridge.moveWindow(0, -10)
            },
            text = "▲"
        )
        TextButton(
            modifier = Modifier.constrainAs(right) {
                bottom.linkTo(anchor = left.top, margin = 8.dp)
                end.linkTo(parent.end)
            },
            onClick = {
                ZLBridge.moveWindow(10, 0)
            },
            text = "▶"
        )
        TextButton(
            modifier = Modifier.constrainAs(down) {
                bottom.linkTo(parent.bottom)
                end.linkTo(anchor = right.start, margin = 8.dp)
            },
            onClick = {
                ZLBridge.moveWindow(0, 10)
            },
            text = "▼"
        )
        TextButton(
            modifier = Modifier.constrainAs(left) {
                bottom.linkTo(parent.bottom)
                end.linkTo(parent.end)
            },
            onClick = {
                ZLBridge.moveWindow(-10, 0)
            },
            text = "◀"
        )
    }
}

@Composable
private fun TextButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    text: String
) {
    Button(
        modifier = modifier,
        onClick = onClick
    ) {
        Text(text = text)
    }
}