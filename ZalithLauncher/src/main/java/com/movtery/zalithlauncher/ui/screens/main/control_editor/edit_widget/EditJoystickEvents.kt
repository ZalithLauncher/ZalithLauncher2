package com.movtery.zalithlauncher.ui.screens.main.control_editor.edit_widget

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.movtery.layer_controller.data.JoystickDirection
import com.movtery.layer_controller.event.ClickEvent
import com.movtery.layer_controller.observable.ObservableClickEventsProvider
import com.movtery.layer_controller.observable.ObservableJoystickData
import com.movtery.layer_controller.observable.joystickDirectionEventsProvider
import com.movtery.layer_controller.observable.joystickLockEventsProvider
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.ui.components.MarqueeText
import com.movtery.zalithlauncher.ui.components.rememberDialogMaxHeight
import com.movtery.zalithlauncher.ui.components.verticalScrollWithBar
import com.movtery.zalithlauncher.ui.screens.content.settings.layouts.CardPosition
import com.movtery.zalithlauncher.ui.screens.content.settings.layouts.rememberSettingsCardShape
import com.movtery.zalithlauncher.ui.screens.main.control_editor.InfoLayoutTextItem
import com.movtery.zalithlauncher.ui.theme.cardColor
import com.movtery.zalithlauncher.ui.theme.onCardColor

private enum class JoystickArea {
    North, NorthEast,
    East,
    SouthEast, South, SouthWest,
    West,
    NorthWest,
    Lock
}

private fun JoystickArea.toDirection(): JoystickDirection? = when (this) {
    JoystickArea.North -> JoystickDirection.North
    JoystickArea.NorthEast -> JoystickDirection.NorthEast
    JoystickArea.East -> JoystickDirection.East
    JoystickArea.SouthEast -> JoystickDirection.SouthEast
    JoystickArea.South -> JoystickDirection.South
    JoystickArea.SouthWest -> JoystickDirection.SouthWest
    JoystickArea.West -> JoystickDirection.West
    JoystickArea.NorthWest -> JoystickDirection.NorthWest
    JoystickArea.Lock -> null
}

private sealed interface JoystickEventDialogState {
    data object None : JoystickEventDialogState
    data class LauncherEvent(val area: JoystickArea) : JoystickEventDialogState
    data class KeyEvent(val area: JoystickArea) : JoystickEventDialogState
}

@Composable
fun EditJoystickEvents(
    data: ObservableJoystickData,
    switchControlLayers: (ObservableClickEventsProvider, ClickEvent.Type) -> Unit,
    sendText: (ObservableClickEventsProvider) -> Unit,
) {
    var selectedArea by remember { mutableStateOf<JoystickArea?>(null) }
    var dialogState by remember { mutableStateOf<JoystickEventDialogState>(JoystickEventDialogState.None) }

    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 4.dp)
    ) {
        // 摇杆方向选择
        Box(
            modifier = Modifier
                .weight(2f)
                .fillMaxHeight()
                .padding(end = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                AreaButton(
                    text = stringResource(R.string.control_editor_edit_joystick_lock_events),
                    position = CardPosition.Single,
                    isSelected = selectedArea == JoystickArea.Lock,
                    onClick = {
                        selectedArea = if (selectedArea == JoystickArea.Lock) null else JoystickArea.Lock
                    }
                )

                FakeJoystick(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    selectedArea = selectedArea,
                    onAreaSelected = { area ->
                        selectedArea = if (selectedArea == area) null else area
                    }
                )
            }
        }

        // 事件编辑入口
        Box(
            modifier = Modifier
                .weight(3f)
                .fillMaxHeight()
                .padding(start = 8.dp, end = 4.dp, top = 12.dp)
        ) {
            if (selectedArea == null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.control_editor_edit_joystick_select_area),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier.padding(start = 6.dp, end = 12.dp)
                    )
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    InfoLayoutTextItem(
                        modifier = Modifier.fillMaxWidth(),
                        title = stringResource(R.string.control_editor_edit_event_launcher),
                        onClick = { dialogState = JoystickEventDialogState.LauncherEvent(selectedArea!!) }
                    )

                    InfoLayoutTextItem(
                        modifier = Modifier.fillMaxWidth(),
                        title = stringResource(R.string.control_editor_edit_event_key),
                        onClick = { dialogState = JoystickEventDialogState.KeyEvent(selectedArea!!) }
                    )
                }
            }
        }
    }

    when (val state = dialogState) {
        is JoystickEventDialogState.LauncherEvent -> {
            val direction = state.area.toDirection()
            val isLock = state.area == JoystickArea.Lock
            JoystickLauncherEventDialog(
                data = data,
                direction = direction,
                isLock = isLock,
                switchControlLayers = switchControlLayers,
                sendText = sendText,
                onDismiss = { dialogState = JoystickEventDialogState.None }
            )
        }
        is JoystickEventDialogState.KeyEvent -> {
            val direction = state.area.toDirection()
            val isLock = state.area == JoystickArea.Lock
            JoystickKeyEventDialog(
                data = data,
                direction = direction,
                isLock = isLock,
                onDismiss = { dialogState = JoystickEventDialogState.None }
            )
        }
        is JoystickEventDialogState.None -> {}
    }
}

@Composable
private fun FakeJoystick(
    selectedArea: JoystickArea?,
    onAreaSelected: (JoystickArea) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.aspectRatio(1f),
        contentAlignment = Alignment.Center
    ) {
        val gridSpacing = 2.dp

        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(gridSpacing),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // NW, N, NE
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(gridSpacing)
            ) {
                AreaButton(
                    modifier = Modifier.fillMaxHeight().weight(1f),
                    text = "↖",
                    isSelected = selectedArea == JoystickArea.NorthWest,
                    position = CardPosition.TopStart,
                    onClick = { onAreaSelected(JoystickArea.NorthWest) }
                )
                AreaButton(
                    modifier = Modifier.fillMaxHeight().weight(1f),
                    text = "↑",
                    isSelected = selectedArea == JoystickArea.North,
                    position = CardPosition.Middle,
                    onClick = { onAreaSelected(JoystickArea.North) }
                )
                AreaButton(
                    modifier = Modifier.fillMaxHeight().weight(1f),
                    text = "↗",
                    isSelected = selectedArea == JoystickArea.NorthEast,
                    position = CardPosition.TopEnd,
                    onClick = { onAreaSelected(JoystickArea.NorthEast) }
                )
            }

            // W E
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(gridSpacing)
            ) {
                AreaButton(
                    modifier = Modifier.fillMaxHeight().weight(1f),
                    text = "←",
                    isSelected = selectedArea == JoystickArea.West,
                    position = CardPosition.Middle,
                    onClick = { onAreaSelected(JoystickArea.West) }
                )

                Spacer(modifier = Modifier.weight(1f))

                AreaButton(
                    modifier = Modifier.fillMaxHeight().weight(1f),
                    text = "→",
                    isSelected = selectedArea == JoystickArea.East,
                    position = CardPosition.Middle,
                    onClick = { onAreaSelected(JoystickArea.East) }
                )
            }

            // SW, S, SE
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(gridSpacing)
            ) {
                AreaButton(
                    modifier = Modifier.fillMaxHeight().weight(1f),
                    text = "↙",
                    isSelected = selectedArea == JoystickArea.SouthWest,
                    position = CardPosition.BottomStart,
                    onClick = { onAreaSelected(JoystickArea.SouthWest) }
                )
                AreaButton(
                    modifier = Modifier.fillMaxHeight().weight(1f),
                    text = "↓",
                    isSelected = selectedArea == JoystickArea.South,
                    position = CardPosition.Middle,
                    onClick = { onAreaSelected(JoystickArea.South) }
                )
                AreaButton(
                    modifier = Modifier.fillMaxHeight().weight(1f),
                    text = "↘",
                    isSelected = selectedArea == JoystickArea.SouthEast,
                    position = CardPosition.BottomEnd,
                    onClick = { onAreaSelected(JoystickArea.SouthEast) }
                )
            }
        }
    }
}

@Composable
private fun AreaButton(
    modifier: Modifier = Modifier,
    text: String,
    isSelected: Boolean,
    position: CardPosition,
    onClick: () -> Unit
) {
    val containerColor by animateColorAsState(
        if (isSelected) {
            MaterialTheme.colorScheme.secondary
        } else {
            MaterialTheme.colorScheme.secondaryContainer
        }
    )
    val contentColor by animateColorAsState(
        if (isSelected) {
            MaterialTheme.colorScheme.onSecondary
        } else {
            MaterialTheme.colorScheme.onSecondaryContainer
        }
    )

    Surface(
        modifier = modifier,
        color = containerColor,
        contentColor = contentColor,
        onClick = onClick,
        shape = rememberSettingsCardShape(position)
    ) {
        Box(
            modifier = if (position == CardPosition.Single) {
                Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
            } else {
                Modifier.padding(all = 8.dp)
            },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                textAlign = TextAlign.Center
            )
        }
    }
}


/**
 * 启动器事件编辑
 */
@Composable
private fun JoystickLauncherEventDialog(
    data: ObservableJoystickData,
    direction: JoystickDirection?,
    isLock: Boolean,
    switchControlLayers: (ObservableClickEventsProvider, ClickEvent.Type) -> Unit,
    sendText: (ObservableClickEventsProvider) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.extraLarge,
            color = cardColor(false),
            contentColor = onCardColor(),
            tonalElevation = 3.dp
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                val tabs = remember {
                    listOf(
                        R.string.control_editor_edit_event_basic,
                        R.string.control_editor_edit_event_launcher
                    )
                }

                val pagerState = rememberPagerState(pageCount = { tabs.size })
                var selectedTabIndex by remember { mutableIntStateOf(0) }

                LaunchedEffect(selectedTabIndex) {
                    pagerState.animateScrollToPage(selectedTabIndex)
                }

                SecondaryTabRow(
                    selectedTabIndex = selectedTabIndex,
                    containerColor = cardColor(false)
                ) {
                    tabs.forEachIndexed { index, titleRes ->
                        Tab(
                            selected = index == selectedTabIndex,
                            onClick = { selectedTabIndex = index },
                            text = {
                                MarqueeText(text = stringResource(titleRes))
                            }
                        )
                    }
                }

                HorizontalPager(
                    state = pagerState,
                    userScrollEnabled = false,
                    modifier = Modifier.fillMaxWidth()
                ) { page ->
                    when (page) {
                        0 -> JoystickBasicEventPage(
                            switchControlLayers = { type ->
                                switchControlLayers(
                                    if (isLock) {
                                        joystickLockEventsProvider(data)
                                    } else {
                                        joystickDirectionEventsProvider(data, direction)
                                    }, type
                                )
                            }
                        )
                        1 -> {
                            val provider = if (isLock) {
                                joystickLockEventsProvider(data)
                            } else {
                                joystickDirectionEventsProvider(data, direction)
                            }
                            LauncherEventsEdit(
                                provider = provider,
                                onSendText = {
                                    sendText(provider)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(all = 12.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun JoystickBasicEventPage(
    switchControlLayers: (ClickEvent.Type) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScrollWithBar(rememberScrollState())
            .padding(all = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 切换控件层可见性
        InfoLayoutTextItem(
            modifier = Modifier.fillMaxWidth(),
            title = stringResource(R.string.control_editor_edit_switch_layers),
            onClick = { switchControlLayers(ClickEvent.Type.SwitchLayer) }
        )

        // 强制显示控件层
        InfoLayoutTextItem(
            modifier = Modifier.fillMaxWidth(),
            title = stringResource(R.string.control_editor_edit_show_layers),
            onClick = { switchControlLayers(ClickEvent.Type.ShowLayer) }
        )

        // 强制隐藏控件层
        InfoLayoutTextItem(
            modifier = Modifier.fillMaxWidth(),
            title = stringResource(R.string.control_editor_edit_hide_layers),
            onClick = { switchControlLayers(ClickEvent.Type.HideLayer) }
        )
    }
}



/**
 * 按键事件编辑
 */
@Composable
private fun JoystickKeyEventDialog(
    data: ObservableJoystickData,
    direction: JoystickDirection?,
    isLock: Boolean,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        BoxWithConstraints(
            modifier = Modifier
                .heightIn(max = rememberDialogMaxHeight())
                .fillMaxHeight(),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier
                    .padding(all = 3.dp)
                    .heightIn(max = (maxHeight - 6.dp).coerceAtMost(rememberDialogMaxHeight()))
                    .wrapContentHeight(),
                shape = MaterialTheme.shapes.extraLarge,
                color = cardColor(false),
                contentColor = onCardColor(),
                shadowElevation = 3.dp
            ) {
                Column(
                    modifier = Modifier.wrapContentHeight(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .padding(top = 16.dp),
                        text = stringResource(R.string.control_editor_edit_event_key),
                        style = MaterialTheme.typography.titleMedium,
                    )

                    KeyEventEdit(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = false)
                            .padding(horizontal = 12.dp),
                        provider = if (isLock) {
                            joystickLockEventsProvider(data)
                        } else {
                            joystickDirectionEventsProvider(data, direction)
                        },
                    )
                }
            }
        }
    }
}