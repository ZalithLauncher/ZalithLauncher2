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

package com.movtery.zalithlauncher.ui.control

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.nonInteractiveScrollbar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.movtery.inputmap.keycodes.ControlEventKeycode
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.ui.components.AutoSizeText
import com.movtery.zalithlauncher.ui.components.MarqueeText
import com.movtery.zalithlauncher.ui.control.gamepad.SPECIAL_KEY_MOUSE_SCROLL_DOWN
import com.movtery.zalithlauncher.ui.control.gamepad.SPECIAL_KEY_MOUSE_SCROLL_UP
import com.movtery.zalithlauncher.ui.screens.main.control_editor.InfoLayoutTextItem
import com.movtery.zalithlauncher.ui.theme.cardColor
import com.movtery.zalithlauncher.ui.theme.cardTitleColor
import com.movtery.zalithlauncher.ui.theme.itemColor
import com.movtery.zalithlauncher.ui.theme.onCardColor
import com.movtery.zalithlauncher.ui.theme.onItemColor

private data class TabItem(val title: String)

private data class KeySpec(
    val label: String,
    val code: String = "",
    val weight: Float = 1f,
    val aspect: Float = 1f,
    val isSpacer: Boolean = false
)

private val MAIN_LAYOUT: List<List<KeySpec>> = listOf(
    //1
    listOf(
        KeySpec(label = "Esc", code = ControlEventKeycode.GLFW_KEY_ESCAPE),
        KeySpec(label = "", code = "", weight = 0.6f, isSpacer = true),
        KeySpec(label = "F1", code = ControlEventKeycode.GLFW_KEY_F1),
        KeySpec(label = "F2", code = ControlEventKeycode.GLFW_KEY_F2),
        KeySpec(label = "F3", code = ControlEventKeycode.GLFW_KEY_F3),
        KeySpec(label = "F4", code = ControlEventKeycode.GLFW_KEY_F4),
        KeySpec(label = "", code = "", weight = 0.1f, isSpacer = true),
        KeySpec(label = "F5", code = ControlEventKeycode.GLFW_KEY_F5),
        KeySpec(label = "F6", code = ControlEventKeycode.GLFW_KEY_F6),
        KeySpec(label = "F7", code = ControlEventKeycode.GLFW_KEY_F7),
        KeySpec(label = "F8", code = ControlEventKeycode.GLFW_KEY_F8),
        KeySpec(label = "", code = "", weight = 0.1f, isSpacer = true),
        KeySpec(label = "F9", code = ControlEventKeycode.GLFW_KEY_F9),
        KeySpec(label = "F10", code = ControlEventKeycode.GLFW_KEY_F10),
        KeySpec(label = "F11", code = ControlEventKeycode.GLFW_KEY_F11),
        KeySpec(label = "F12", code = ControlEventKeycode.GLFW_KEY_F12)
    ),
    //2
    listOf(
        KeySpec(label = "`", code = ControlEventKeycode.GLFW_KEY_GRAVE_ACCENT),
        KeySpec(label = "1", code = ControlEventKeycode.GLFW_KEY_1),
        KeySpec(label = "2", code = ControlEventKeycode.GLFW_KEY_2),
        KeySpec(label = "3", code = ControlEventKeycode.GLFW_KEY_3),
        KeySpec(label = "4", code = ControlEventKeycode.GLFW_KEY_4),
        KeySpec(label = "5", code = ControlEventKeycode.GLFW_KEY_5),
        KeySpec(label = "6", code = ControlEventKeycode.GLFW_KEY_6),
        KeySpec(label = "7", code = ControlEventKeycode.GLFW_KEY_7),
        KeySpec(label = "8", code = ControlEventKeycode.GLFW_KEY_8),
        KeySpec(label = "9", code = ControlEventKeycode.GLFW_KEY_9),
        KeySpec(label = "0", code = ControlEventKeycode.GLFW_KEY_0),
        KeySpec(label = "-", code = ControlEventKeycode.GLFW_KEY_MINUS),
        KeySpec(label = "+", code = ControlEventKeycode.GLFW_KEY_EQUAL),
        KeySpec(label = "Backspace", code = ControlEventKeycode.GLFW_KEY_BACKSPACE, weight = 1.5f, aspect = 1.5f)
    ),
    //3
    listOf(
        KeySpec(label = "Tab", code = ControlEventKeycode.GLFW_KEY_TAB, weight = 1.3f, aspect = 1.3f),
        KeySpec(label = "Q", code = ControlEventKeycode.GLFW_KEY_Q),
        KeySpec(label = "W", code = ControlEventKeycode.GLFW_KEY_W),
        KeySpec(label = "E", code = ControlEventKeycode.GLFW_KEY_E),
        KeySpec(label = "R", code = ControlEventKeycode.GLFW_KEY_R),
        KeySpec(label = "T", code = ControlEventKeycode.GLFW_KEY_T),
        KeySpec(label = "Y", code = ControlEventKeycode.GLFW_KEY_Y),
        KeySpec(label = "U", code = ControlEventKeycode.GLFW_KEY_U),
        KeySpec(label = "I", code = ControlEventKeycode.GLFW_KEY_I),
        KeySpec(label = "O", code = ControlEventKeycode.GLFW_KEY_O),
        KeySpec(label = "P", code = ControlEventKeycode.GLFW_KEY_P),
        KeySpec(label = "[", code = ControlEventKeycode.GLFW_KEY_LEFT_BRACKET),
        KeySpec(label = "]", code = ControlEventKeycode.GLFW_KEY_RIGHT_BRACKET),
        KeySpec(label = "\\", code = ControlEventKeycode.GLFW_KEY_BACKSLASH, weight = 1.2f, aspect = 1.2f)
    ),
    //4
    listOf(
        KeySpec(label = "Capslock", code = ControlEventKeycode.GLFW_KEY_CAPS_LOCK, weight = 1.4f, aspect = 1.4f),
        KeySpec(label = "A", code = ControlEventKeycode.GLFW_KEY_A),
        KeySpec(label = "S", code = ControlEventKeycode.GLFW_KEY_S),
        KeySpec(label = "D", code = ControlEventKeycode.GLFW_KEY_D),
        KeySpec(label = "F", code = ControlEventKeycode.GLFW_KEY_F),
        KeySpec(label = "G", code = ControlEventKeycode.GLFW_KEY_G),
        KeySpec(label = "H", code = ControlEventKeycode.GLFW_KEY_H),
        KeySpec(label = "J", code = ControlEventKeycode.GLFW_KEY_J),
        KeySpec(label = "K", code = ControlEventKeycode.GLFW_KEY_K),
        KeySpec(label = "L", code = ControlEventKeycode.GLFW_KEY_L),
        KeySpec(label = ";", code = ControlEventKeycode.GLFW_KEY_SEMICOLON),
        KeySpec(label = "'", code = ControlEventKeycode.GLFW_KEY_APOSTROPHE),
        KeySpec(label = "Enter", code = ControlEventKeycode.GLFW_KEY_ENTER, weight = 2.1f, aspect = 2.1f)
    ),
    //5
    listOf(
        KeySpec(label = "Shift", code = ControlEventKeycode.GLFW_KEY_LEFT_SHIFT, weight = 2f, aspect = 2f),
        KeySpec(label = "Z", code = ControlEventKeycode.GLFW_KEY_Z),
        KeySpec(label = "X", code = ControlEventKeycode.GLFW_KEY_X),
        KeySpec(label = "C", code = ControlEventKeycode.GLFW_KEY_C),
        KeySpec(label = "V", code = ControlEventKeycode.GLFW_KEY_V),
        KeySpec(label = "B", code = ControlEventKeycode.GLFW_KEY_B),
        KeySpec(label = "N", code = ControlEventKeycode.GLFW_KEY_N),
        KeySpec(label = "M", code = ControlEventKeycode.GLFW_KEY_M),
        KeySpec(label = ",", code = ControlEventKeycode.GLFW_KEY_COMMA),
        KeySpec(label = ".", code = ControlEventKeycode.GLFW_KEY_PERIOD),
        KeySpec(label = "/", code = ControlEventKeycode.GLFW_KEY_SLASH),
        KeySpec(label = "Shift", code = ControlEventKeycode.GLFW_KEY_RIGHT_SHIFT, weight = 2.2f, aspect = 2.2f)
    ),
    //6
    listOf(
        KeySpec(label = "Ctrl", code = ControlEventKeycode.GLFW_KEY_LEFT_CONTROL),
        KeySpec(label = "", code = "", isSpacer = true),
        KeySpec(label = "Alt", code = ControlEventKeycode.GLFW_KEY_LEFT_ALT),
        KeySpec(label = "Space", code = ControlEventKeycode.GLFW_KEY_SPACE, weight = 7f, aspect = 7f),
        KeySpec(label = "Alt", code = ControlEventKeycode.GLFW_KEY_RIGHT_ALT),
        KeySpec(label = "", code = "", weight = 2f, aspect = 2f, isSpacer = true),
        KeySpec(label = "Ctrl", code = ControlEventKeycode.GLFW_KEY_RIGHT_CONTROL)
    )
)

private val EDIT_LAYOUT: List<List<KeySpec>> = listOf(
    listOf(
        KeySpec(label = "Printf", code = ControlEventKeycode.GLFW_KEY_PRINT_SCREEN),
        KeySpec(label = "Scroll", code = ControlEventKeycode.GLFW_KEY_SCROLL_LOCK),
        KeySpec(label = "Pause", code = ControlEventKeycode.GLFW_KEY_PAUSE),
        KeySpec(label = "", code = "", weight = 12.2f, isSpacer = true)
    ),
    listOf(
        KeySpec(label = "Insert", code = ControlEventKeycode.GLFW_KEY_INSERT),
        KeySpec(label = "Home", code = ControlEventKeycode.GLFW_KEY_HOME),
        KeySpec(label = "PgUp", code = ControlEventKeycode.GLFW_KEY_PAGE_UP),
        KeySpec(label = "", code = "", weight = 7.8f, isSpacer = true),
        KeySpec(label = "Num lk", code = ControlEventKeycode.GLFW_KEY_NUM_LOCK),
        KeySpec(label = "/", code = ControlEventKeycode.GLFW_KEY_KP_DIVIDE),
        KeySpec(label = "*", code = ControlEventKeycode.GLFW_KEY_KP_MULTIPLY),
        KeySpec(label = "-", code = ControlEventKeycode.GLFW_KEY_KP_SUBTRACT)
    ),
    listOf(
        KeySpec(label = "Delete", code = ControlEventKeycode.GLFW_KEY_DELETE),
        KeySpec(label = "End", code = ControlEventKeycode.GLFW_KEY_END),
        KeySpec(label = "PgDn", code = ControlEventKeycode.GLFW_KEY_PAGE_DOWN),
        KeySpec(label = "", code = "", weight = 7.8f, isSpacer = true),
        KeySpec(label = "7", code = ControlEventKeycode.GLFW_KEY_KP_7),
        KeySpec(label = "8", code = ControlEventKeycode.GLFW_KEY_KP_8),
        KeySpec(label = "9", code = ControlEventKeycode.GLFW_KEY_KP_9),
        KeySpec(label = "+", code = ControlEventKeycode.GLFW_KEY_KP_ADD)
    ),
    listOf(
        KeySpec(label = "", code = "", weight = 11f, isSpacer = true),
        KeySpec(label = "4", code = ControlEventKeycode.GLFW_KEY_KP_4),
        KeySpec(label = "5", code = ControlEventKeycode.GLFW_KEY_KP_5),
        KeySpec(label = "6", code = ControlEventKeycode.GLFW_KEY_KP_6),
        KeySpec(label = "", code = "", isSpacer = true)
    ),
    listOf(
        KeySpec(label = "", code = "", isSpacer = true),
        KeySpec(label = "↑", code = ControlEventKeycode.GLFW_KEY_UP),
        KeySpec(label = "", code = "", 8.8f, isSpacer = true),
        KeySpec(label = "1", code = ControlEventKeycode.GLFW_KEY_KP_1),
        KeySpec(label = "2", code = ControlEventKeycode.GLFW_KEY_KP_2),
        KeySpec(label = "3", code = ControlEventKeycode.GLFW_KEY_KP_3),
        KeySpec(label = "Enter", code = ControlEventKeycode.GLFW_KEY_KP_ENTER)
    ),
    listOf(
        KeySpec(label = "←", code = ControlEventKeycode.GLFW_KEY_LEFT),
        KeySpec(label = "↓", code = ControlEventKeycode.GLFW_KEY_DOWN),
        KeySpec(label = "→", code = ControlEventKeycode.GLFW_KEY_RIGHT),
        KeySpec(label = "", code = "", weight = 8.8f, isSpacer = true),
        KeySpec(label = "0", code = ControlEventKeycode.GLFW_KEY_KP_0),
        KeySpec(label = ".", code = ControlEventKeycode.GLFW_KEY_KP_DECIMAL),
        KeySpec(label = "", code = "", isSpacer = true)
    )
)

/**
 * 虚拟键盘对话框，展示一个包含主要按键的键盘
 * @param onSwitch [isTapMode] 为 `false` 时，触摸按键的回调函数
 * @param onTap [isTapMode] 为 `true` 时，点击按键的回调函数
 */
@Composable
fun Keyboard(
    onDismissRequest: () -> Unit,
    isTapMode: Boolean = false,
    onSwitch: (key: String, pressed: Boolean) -> Unit = { _, _ -> },
    onTap: (key: String) -> Unit = {}
) {
    val tabs = remember {
        listOf(
            TabItem("①"),
            TabItem("②")
        )
    }

    KeyboardNavDialog(
        tabs = tabs,
        onDismissRequest = onDismissRequest
    ) { page ->
        when (page) {
            0 -> {
                MainKeyboardArea(
                    modifier = Modifier.padding(all = 12.dp),
                    isTapMode = isTapMode,
                    onTap = onTap,
                    onSwitch = onSwitch
                )
            }
            1 -> {
                EditingKeyboardArea(
                    modifier = Modifier.padding(all = 12.dp),
                    isTapMode = isTapMode,
                    onTap = onTap,
                    onSwitch = onSwitch
                )
            }
        }
    }
}

/**
 * 虚拟键盘对话框，展示一个包含主要按键的键盘，主要用于手柄键值绑定
 * @param selectedKeys 当前键绑定的所有键值
 * @param onKeyAdd 绑定新的键值
 * @param onKeyRemove 解绑键值
 */
@Composable
fun GamepadBindingKeyboard(
    selectedKeys: List<String>,
    onKeyAdd: (String) -> Unit,
    onKeyRemove: (String) -> Unit,
    onDismissRequest: () -> Unit
) {
    val tabs = remember {
        listOf(
            TabItem("①"),
            TabItem("②"),
            TabItem("③")
        )
    }

    val currentSelectedKeys by rememberUpdatedState(selectedKeys)
    val currentOnKeyAdd by rememberUpdatedState(onKeyAdd)
    val currentOnKeyRemove by rememberUpdatedState(onKeyRemove)

    var refreshed by remember { mutableStateOf(false) }

    fun onKeyTap(key: String) {
        if (currentSelectedKeys.contains(key)) currentOnKeyRemove(key)
        else currentOnKeyAdd(key)
        refreshed = refreshed.not()
    }

    KeyboardNavDialog(
        tabs = tabs,
        onDismissRequest = onDismissRequest
    ) { page ->
        when (page) {
            0 -> {
                MainKeyboardArea(
                    modifier = Modifier.padding(all = 12.dp),
                    isTapMode = true,
                    onTap = { key ->
                        onKeyTap(key)
                    },
                    onSwitch = { _, _ -> },
                    refreshed = refreshed,
                    isSelected = { key ->
                        currentSelectedKeys.contains(key)
                    }
                )
            }
            1 -> {
                EditingKeyboardArea(
                    modifier = Modifier.padding(all = 12.dp),
                    isTapMode = true,
                    onTap = { key ->
                        onKeyTap(key)
                    },
                    onSwitch = { _, _ -> },
                    refreshed = refreshed,
                    isSelected = { key ->
                        currentSelectedKeys.contains(key)
                    }
                )
            }
            2 -> {
                GamepadSpecialArea(
                    onTap = { key ->
                        onKeyTap(key)
                    },
                    refreshed = refreshed,
                    isSelected = { key ->
                        currentSelectedKeys.contains(key)
                    }
                )
            }
        }
    }
}

@Composable
fun GamepadSpecialArea(
    modifier: Modifier = Modifier,
    onTap: (String) -> Unit,
    refreshed: Any? = null,
    isSelected: (String) -> Boolean
) {
    val scrollState = rememberLazyListState()
    LazyColumn(
        modifier = modifier
            .nonInteractiveScrollbar(
                state = scrollState.scrollIndicatorState!!,
                orientation = Orientation.Vertical,
            )
            .padding(horizontal = 2.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(all = 12.dp),
        state = scrollState
    ) {
        //鼠标左键
        item {
            val selected = remember(refreshed) { isSelected(ControlEventKeycode.GLFW_MOUSE_BUTTON_LEFT) }
            InfoLayoutTextItem(
                modifier = Modifier.fillMaxWidth(),
                title = stringResource(R.string.control_editor_edit_event_launcher_mouse_left),
                onClick = {
                    onTap(ControlEventKeycode.GLFW_MOUSE_BUTTON_LEFT)
                },
                showArrow = false,
                selected = selected
            )
        }
        //鼠标中键
        item {
            val selected = remember(refreshed) { isSelected(ControlEventKeycode.GLFW_MOUSE_BUTTON_MIDDLE) }
            InfoLayoutTextItem(
                modifier = Modifier.fillMaxWidth(),
                title = stringResource(R.string.control_editor_edit_event_launcher_mouse_middle),
                onClick = {
                    onTap(ControlEventKeycode.GLFW_MOUSE_BUTTON_MIDDLE)
                },
                showArrow = false,
                selected = selected
            )
        }
        //鼠标右键
        item {
            val selected = remember(refreshed) { isSelected(ControlEventKeycode.GLFW_MOUSE_BUTTON_RIGHT) }
            InfoLayoutTextItem(
                modifier = Modifier.fillMaxWidth(),
                title = stringResource(R.string.control_editor_edit_event_launcher_mouse_right),
                onClick = {
                    onTap(ControlEventKeycode.GLFW_MOUSE_BUTTON_RIGHT)
                },
                showArrow = false,
                selected = selected
            )
        }
        //单次鼠标滚轮上
        item {
            val selected = remember(refreshed) { isSelected(SPECIAL_KEY_MOUSE_SCROLL_UP) }
            InfoLayoutTextItem(
                modifier = Modifier.fillMaxWidth(),
                title = stringResource(R.string.control_editor_edit_event_launcher_mouse_scroll_up_single),
                onClick = {
                    onTap(SPECIAL_KEY_MOUSE_SCROLL_UP)
                },
                showArrow = false,
                selected = selected
            )
        }
        //单次鼠标滚轮下
        item {
            val selected = remember(refreshed) { isSelected(SPECIAL_KEY_MOUSE_SCROLL_DOWN) }
            InfoLayoutTextItem(
                modifier = Modifier.fillMaxWidth(),
                title = stringResource(R.string.control_editor_edit_event_launcher_mouse_scroll_down_single),
                onClick = {
                    onTap(SPECIAL_KEY_MOUSE_SCROLL_DOWN)
                },
                showArrow = false,
                selected = selected
            )
        }
    }
}

@Composable
private fun KeyboardNavDialog(
    tabs: List<TabItem>,
    onDismissRequest: () -> Unit,
    pageContent: @Composable (Int) -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { tabs.size })
    var selectedTabIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(selectedTabIndex) {
        pagerState.animateScrollToPage(selectedTabIndex)
    }
    LaunchedEffect(pagerState.currentPage, pagerState.isScrollInProgress) {
        if (!pagerState.isScrollInProgress) {
            selectedTabIndex = pagerState.currentPage
        }
    }

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(0.75f),
            shadowElevation = 3.dp,
            shape = MaterialTheme.shapes.extraLarge,
            color = cardColor(false),
            contentColor = onCardColor(),
        ) {
            Column {
                //顶贴标签栏
                SecondaryTabRow(
                    selectedTabIndex = selectedTabIndex,
                    containerColor = cardTitleColor()
                ) {
                    tabs.forEachIndexed { index, item ->
                        Tab(
                            selected = index == selectedTabIndex,
                            onClick = {
                                selectedTabIndex = index
                            },
                            text = {
                                MarqueeText(text = item.title)
                            }
                        )
                    }
                }

                HorizontalPager(
                    state = pagerState,
//                    userScrollEnabled = false,
                    modifier = Modifier.fillMaxWidth()
                ) { page ->
                    pageContent(page)
                }
            }
        }
    }
}

@Composable
private fun MainKeyboardArea(
    modifier: Modifier = Modifier,
    isTapMode: Boolean,
    onTap: (String) -> Unit,
    onSwitch: (String, Boolean) -> Unit,
    refreshed: Any? = null,
    isSelected: (String) -> Boolean = { false }
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        MAIN_LAYOUT.forEach { row ->
            KeyboardRow(
                row = row,
                isTapMode = isTapMode,
                onTap = onTap,
                onSwitch = onSwitch,
                refreshed = refreshed,
                isSelected = isSelected
            )
        }
    }
}

@Composable
private fun EditingKeyboardArea(
    modifier: Modifier = Modifier,
    isTapMode: Boolean,
    onTap: (String) -> Unit,
    onSwitch: (String, Boolean) -> Unit,
    refreshed: Any? = null,
    isSelected: (String) -> Boolean = { false }
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        EDIT_LAYOUT.forEach { row ->
            KeyboardRow(
                row = row,
                isTapMode = isTapMode,
                onTap = onTap,
                onSwitch = onSwitch,
                refreshed = refreshed,
                isSelected = isSelected
            )
        }
    }
}

@Composable
private fun KeyboardRow(
    row: List<KeySpec>,
    isTapMode: Boolean,
    onTap: (String) -> Unit,
    onSwitch: (String, Boolean) -> Unit,
    refreshed: Any? = null,
    isSelected: (String) -> Boolean
) {
    Row(
        modifier = Modifier.height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        row.forEach { key ->
            if (key.isSpacer || key.label.isEmpty()) {
                Spacer(
                    modifier = Modifier.weight(key.weight)
                )
            } else {
                KeyButton(
                    modifier = Modifier.weight(key.weight),
                    name = key.label,
                    identifier = key.code,
                    isTapMode = isTapMode,
                    onTap = onTap,
                    onSwitch = onSwitch,
                    refreshed = refreshed,
                    isSelected = isSelected,
                    aspectRatio = key.aspect
                )
            }
        }
    }
}

@Composable
private fun KeyButton(
    modifier: Modifier = Modifier,
    name: String,
    identifier: String,
    isTapMode: Boolean,
    onTap: (identifier: String) -> Unit,
    onSwitch: (identifier: String, pressed: Boolean) -> Unit,
    refreshed: Any? = null,
    isSelected: (String) -> Boolean,
    color: Color = itemColor(false),
    contentColor: Color = onItemColor(),
    shape: Shape = MaterialTheme.shapes.medium,
    borderColor: Color = MaterialTheme.colorScheme.primary,
    style: TextStyle = MaterialTheme.typography.labelSmall,
    maxFontSize: TextUnit = style.fontSize,
    aspectRatio: Float = 1f
) {
    /**
     * 当前按钮是否为按下的状态
     */
    val isSelected = remember(refreshed) { isSelected(identifier) }
    var pressed by remember { mutableStateOf(false) }
    val currentOnTap by rememberUpdatedState(onTap)
    val currentOnSwitch by rememberUpdatedState(onSwitch)

    val borderWidth by animateDpAsState(
        if (pressed || isSelected) 2.dp
        else (-1).dp
    )

    Surface(
        modifier = modifier
            .aspectRatio(aspectRatio)
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = {
                        if (isTapMode) {
                            currentOnTap(identifier)
                        } else {
                            pressed = !pressed
                            currentOnSwitch(identifier, pressed)
                        }
                    }
                )
            }
            .border(
                width = borderWidth,
                color = borderColor,
                shape = shape
            ),
        color = color,
        contentColor = contentColor,
        shape = shape
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            AutoSizeText(
                modifier = Modifier.basicMarquee(Int.MAX_VALUE),
                text = name,
                style = style,
                maxLines = 1,
                autoSize = TextAutoSize.StepBased(maxFontSize = maxFontSize)
            )
        }
    }
}