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

package com.movtery.zalithlauncher.ui.screens.content.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.bridge.CursorShape
import com.movtery.zalithlauncher.context.copyLocalFile
import com.movtery.zalithlauncher.contract.MediaPickerContract
import com.movtery.zalithlauncher.coroutine.Task
import com.movtery.zalithlauncher.coroutine.TaskSystem
import com.movtery.zalithlauncher.setting.AllSettings
import com.movtery.zalithlauncher.setting.enums.GestureActionType
import com.movtery.zalithlauncher.setting.enums.MouseControlMode
import com.movtery.zalithlauncher.setting.unit.ParcelableSettingUnit
import com.movtery.zalithlauncher.setting.unit.floatRange
import com.movtery.zalithlauncher.ui.androidText
import com.movtery.zalithlauncher.ui.base.BaseScreen
import com.movtery.zalithlauncher.ui.components.AnimatedColumn
import com.movtery.zalithlauncher.ui.components.IconTextButton
import com.movtery.zalithlauncher.ui.components.LittleTextLabel
import com.movtery.zalithlauncher.ui.components.MarqueeText
import com.movtery.zalithlauncher.ui.components.SimpleAlertDialog
import com.movtery.zalithlauncher.ui.components.TitleAndSummary
import com.movtery.zalithlauncher.ui.components.TooltipIconButton
import com.movtery.zalithlauncher.ui.components.infiniteShimmer
import com.movtery.zalithlauncher.ui.components.verticalScrollWithBar
import com.movtery.zalithlauncher.ui.control.gyroscope.isGyroscopeAvailable
import com.movtery.zalithlauncher.ui.control.mouse.CursorHotspot
import com.movtery.zalithlauncher.ui.control.mouse.MouseHotspotEditorDialog
import com.movtery.zalithlauncher.ui.control.mouse.MousePointer
import com.movtery.zalithlauncher.ui.control.mouse.arrowPointerFile
import com.movtery.zalithlauncher.ui.control.mouse.crossHairPointerFile
import com.movtery.zalithlauncher.ui.control.mouse.iBeamPointerFile
import com.movtery.zalithlauncher.ui.control.mouse.linkPointerFile
import com.movtery.zalithlauncher.ui.control.mouse.notAllowedPointerFile
import com.movtery.zalithlauncher.ui.control.mouse.resizeAllPointerFile
import com.movtery.zalithlauncher.ui.control.mouse.resizeEWPointerFile
import com.movtery.zalithlauncher.ui.control.mouse.resizeNSPointerFile
import com.movtery.zalithlauncher.ui.screens.NestedNavKey
import com.movtery.zalithlauncher.ui.screens.NormalNavKey
import com.movtery.zalithlauncher.ui.screens.TitledNavKey
import com.movtery.zalithlauncher.ui.screens.content.settings.layouts.CardPosition
import com.movtery.zalithlauncher.ui.screens.content.settings.layouts.IntSliderSettingsCard
import com.movtery.zalithlauncher.ui.screens.content.settings.layouts.ListSettingsCard
import com.movtery.zalithlauncher.ui.screens.content.settings.layouts.SettingsCard
import com.movtery.zalithlauncher.ui.screens.content.settings.layouts.SettingsCardColumn
import com.movtery.zalithlauncher.ui.screens.content.settings.layouts.SwitchSettingsCard
import com.movtery.zalithlauncher.utils.formatKeyCode
import com.movtery.zalithlauncher.utils.image.isImageFile
import com.movtery.zalithlauncher.utils.string.getMessageOrToString
import com.movtery.zalithlauncher.viewmodel.ErrorViewModel
import com.movtery.zalithlauncher.viewmodel.EventViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.withContext
import org.apache.commons.io.FileUtils
import java.io.File

@Composable
fun ControlSettingsScreen(
    key: NestedNavKey.Settings,
    settingsScreenKey: TitledNavKey?,
    mainScreenKey: TitledNavKey?,
    eventViewModel: EventViewModel,
    submitError: (ErrorViewModel.ThrowableMessage) -> Unit
) {
    BaseScreen(
        Triple(key, mainScreenKey, false),
        Triple(NormalNavKey.Settings.Control, settingsScreenKey, false)
    ) { isVisible ->
        AnimatedColumn(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScrollWithBar(state = rememberScrollState())
                .padding(all = 12.dp),
            isVisible = isVisible
        ) { scope ->
            AnimatedItem(scope) { yOffset ->
                SettingsCardColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset { IntOffset(x = 0, y = yOffset.roundToPx()) }
                ) {
                    SwitchSettingsCard(
                        modifier = Modifier.fillMaxWidth(),
                        position = CardPosition.Top,
                        unit = AllSettings.physicalMouseMode,
                        title = stringResource(R.string.settings_control_mouse_physical_mouse_mode_title),
                        summary = stringResource(R.string.settings_control_mouse_physical_mouse_mode_summary),
                        trailingIcon = {
                            TooltipIconButton(
                                modifier = Modifier
                                    .align(Alignment.CenterVertically)
                                    .padding(horizontal = 8.dp),
                                tooltipTitle = stringResource(R.string.generic_warning),
                                tooltipMessage = stringResource(R.string.settings_control_mouse_physical_mouse_warning)
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_warning_filled),
                                    contentDescription = stringResource(R.string.generic_warning),
                                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                                )
                            }
                        }
                    )

                    var operation by remember { mutableStateOf<PhysicalKeyOperation>(PhysicalKeyOperation.None) }
                    SettingsCard(
                        modifier = Modifier.fillMaxWidth(),
                        position = CardPosition.Bottom,
                        onClick = { operation = PhysicalKeyOperation.Bind }
                    ) {
                        PhysicalKeyImeTrigger(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(all = 16.dp),
                            operation = operation,
                            changeOperation = { operation = it },
                            eventViewModel = eventViewModel
                        )
                    }
                }
            }

            AnimatedItem(scope) { yOffset ->
                SettingsCardColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset { IntOffset(x = 0, y = yOffset.roundToPx()) }
                ) {
                    val mouseSize = AllSettings.mouseSize.state

                    var arrowMouseOperation by remember { mutableStateOf<MousePointerOperation>(MousePointerOperation.None) }
                    MousePointerCard(
                        modifier = Modifier.fillMaxWidth(),
                        position = CardPosition.Top,
                        title = stringResource(R.string.settings_control_mouse_pointer_arrow_title),
                        summary = stringResource(R.string.settings_control_mouse_pointer_arrow_summary),
                        mouseSize = mouseSize,
                        mousePointerFile = arrowPointerFile,
                        cursorShape = CursorShape.Arrow,
                        hotspot = AllSettings.arrowMouseHotspot,
                        mouseOperation = arrowMouseOperation,
                        changeOperation = { arrowMouseOperation = it },
                        submitError = submitError
                    )

                    var linkMouseOperation by remember { mutableStateOf<MousePointerOperation>(MousePointerOperation.None) }
                    MousePointerCard(
                        modifier = Modifier.fillMaxWidth(),
                        position = CardPosition.Middle,
                        title = stringResource(R.string.settings_control_mouse_pointer_link_title),
                        summary = stringResource(R.string.settings_control_mouse_pointer_link_summary),
                        mouseSize = mouseSize,
                        mousePointerFile = linkPointerFile,
                        cursorShape = CursorShape.Hand,
                        hotspot = AllSettings.linkMouseHotspot,
                        mouseOperation = linkMouseOperation,
                        changeOperation = { linkMouseOperation = it },
                        submitError = submitError
                    )

                    var ibeamMouseOperation by remember { mutableStateOf<MousePointerOperation>(MousePointerOperation.None) }
                    MousePointerCard(
                        modifier = Modifier.fillMaxWidth(),
                        position = CardPosition.Middle,
                        title = stringResource(R.string.settings_control_mouse_pointer_ibeam_title),
                        summary = stringResource(R.string.settings_control_mouse_pointer_ibeam_summary),
                        mouseSize = mouseSize,
                        mousePointerFile = iBeamPointerFile,
                        cursorShape = CursorShape.IBeam,
                        hotspot = AllSettings.iBeamMouseHotspot,
                        mouseOperation = ibeamMouseOperation,
                        changeOperation = { ibeamMouseOperation = it },
                        submitError = submitError
                    )

                    var crosshairMouseOperation by remember { mutableStateOf<MousePointerOperation>(MousePointerOperation.None) }
                    MousePointerCard(
                        modifier = Modifier.fillMaxWidth(),
                        position = CardPosition.Middle,
                        title = stringResource(R.string.settings_control_mouse_pointer_crosshair_title),
                        summary = stringResource(R.string.settings_control_mouse_pointer_common_summary),
                        mouseSize = mouseSize,
                        mousePointerFile = crossHairPointerFile,
                        cursorShape = CursorShape.CrossHair,
                        hotspot = AllSettings.crossHairMouseHotspot,
                        mouseOperation = crosshairMouseOperation,
                        changeOperation = { crosshairMouseOperation = it },
                        submitError = submitError
                    )

                    var resizeNSMouseOperation by remember { mutableStateOf<MousePointerOperation>(MousePointerOperation.None) }
                    MousePointerCard(
                        modifier = Modifier.fillMaxWidth(),
                        position = CardPosition.Middle,
                        title = stringResource(R.string.settings_control_mouse_pointer_resize_ns_title),
                        summary = stringResource(R.string.settings_control_mouse_pointer_resize_ns_summary),
                        mouseSize = mouseSize,
                        mousePointerFile = resizeNSPointerFile,
                        cursorShape = CursorShape.ResizeNS,
                        hotspot = AllSettings.resizeNSMouseHotspot,
                        mouseOperation = resizeNSMouseOperation,
                        changeOperation = { resizeNSMouseOperation = it },
                        submitError = submitError
                    )

                    var resizeEWMouseOperation by remember { mutableStateOf<MousePointerOperation>(MousePointerOperation.None) }
                    MousePointerCard(
                        modifier = Modifier.fillMaxWidth(),
                        position = CardPosition.Middle,
                        title = stringResource(R.string.settings_control_mouse_pointer_resize_ew_title),
                        summary = stringResource(R.string.settings_control_mouse_pointer_resize_ew_summary),
                        mouseSize = mouseSize,
                        mousePointerFile = resizeEWPointerFile,
                        cursorShape = CursorShape.ResizeEW,
                        hotspot = AllSettings.resizeEWMouseHotspot,
                        mouseOperation = resizeEWMouseOperation,
                        changeOperation = { resizeEWMouseOperation = it },
                        submitError = submitError
                    )

                    var resizeAllMouseOperation by remember { mutableStateOf<MousePointerOperation>(MousePointerOperation.None) }
                    MousePointerCard(
                        modifier = Modifier.fillMaxWidth(),
                        position = CardPosition.Middle,
                        title = stringResource(R.string.settings_control_mouse_pointer_resize_all_title),
                        summary = stringResource(R.string.settings_control_mouse_pointer_common_summary),
                        mouseSize = mouseSize,
                        mousePointerFile = resizeAllPointerFile,
                        cursorShape = CursorShape.ResizeAll,
                        hotspot = AllSettings.resizeAllMouseHotspot,
                        mouseOperation = resizeAllMouseOperation,
                        changeOperation = { resizeAllMouseOperation = it },
                        submitError = submitError
                    )

                    var notAllowedMouseOperation by remember { mutableStateOf<MousePointerOperation>(MousePointerOperation.None) }
                    MousePointerCard(
                        modifier = Modifier.fillMaxWidth(),
                        position = CardPosition.Middle,
                        title = stringResource(R.string.settings_control_mouse_pointer_not_allowed_title),
                        summary = stringResource(R.string.settings_control_mouse_pointer_not_allowed_summary),
                        mouseSize = mouseSize,
                        mousePointerFile = notAllowedPointerFile,
                        cursorShape = CursorShape.NotAllowed,
                        hotspot = AllSettings.notAllowedMouseHotspot,
                        mouseOperation = notAllowedMouseOperation,
                        changeOperation = { notAllowedMouseOperation = it },
                        submitError = submitError
                    )

                    IntSliderSettingsCard(
                        modifier = Modifier.fillMaxWidth(),
                        position = CardPosition.Bottom,
                        unit = AllSettings.mouseSize,
                        title = stringResource(R.string.settings_control_mouse_size_title),
                        valueRange = AllSettings.mouseSize.floatRange,
                        suffix = "Dp",
                        fineTuningControl = true
                    )
                }
            }

            AnimatedItem(scope) { yOffset ->
                SettingsCardColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset { IntOffset(x = 0, y = yOffset.roundToPx()) }
                ) {
                    SwitchSettingsCard(
                        modifier = Modifier.fillMaxWidth(),
                        position = CardPosition.Top,
                        unit = AllSettings.hideMouse,
                        title = stringResource(R.string.settings_control_mouse_hide_title),
                        summary = stringResource(R.string.settings_control_mouse_hide_summary),
                        enabled = AllSettings.mouseControlMode.state == MouseControlMode.CLICK //仅点击模式下可更改设置
                    )

                    SwitchSettingsCard(
                        unit = AllSettings.enableMouseClick,
                        position = CardPosition.Middle,
                        title = stringResource(R.string.settings_control_mouse_enable_click_title),
                        summary = stringResource(R.string.settings_control_mouse_enable_click_summary),
                        enabled = AllSettings.mouseControlMode.state == MouseControlMode.SLIDE //仅滑动模式下可更改设置
                    )

                    ListSettingsCard(
                        modifier = Modifier.fillMaxWidth(),
                        position = CardPosition.Middle,
                        unit = AllSettings.mouseControlMode,
                        items = MouseControlMode.entries,
                        title = stringResource(R.string.settings_control_mouse_control_mode_title),
                        summary = stringResource(R.string.settings_control_mouse_control_mode_summary),
                        getItemText = { stringResource(it.nameRes) }
                    )

                    IntSliderSettingsCard(
                        modifier = Modifier.fillMaxWidth(),
                        position = CardPosition.Middle,
                        unit = AllSettings.cursorSensitivity,
                        title = stringResource(R.string.settings_control_mouse_sensitivity_title),
                        summary = stringResource(R.string.settings_control_mouse_sensitivity_summary),
                        valueRange = AllSettings.cursorSensitivity.floatRange,
                        suffix = "%",
                        fineTuningControl = true
                    )

                    IntSliderSettingsCard(
                        modifier = Modifier.fillMaxWidth(),
                        position = CardPosition.Middle,
                        unit = AllSettings.mouseCaptureSensitivity,
                        title = stringResource(R.string.settings_control_mouse_capture_sensitivity_title),
                        summary = stringResource(R.string.settings_control_mouse_capture_sensitivity_summary),
                        valueRange = AllSettings.mouseCaptureSensitivity.floatRange,
                        suffix = "%",
                        fineTuningControl = true
                    )

                    IntSliderSettingsCard(
                        modifier = Modifier.fillMaxWidth(),
                        position = CardPosition.Bottom,
                        unit = AllSettings.mouseLongPressDelay,
                        title = stringResource(R.string.settings_control_mouse_long_press_delay_title),
                        summary = stringResource(R.string.settings_control_mouse_long_press_delay_summary),
                        valueRange = AllSettings.mouseLongPressDelay.floatRange,
                        suffix = "ms",
                        fineTuningControl = true
                    )
                }
            }

            AnimatedItem(scope) { yOffset ->
                SettingsCardColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset { IntOffset(x = 0, y = yOffset.roundToPx()) }
                ) {
                    SwitchSettingsCard(
                        modifier = Modifier.fillMaxWidth(),
                        position = CardPosition.Top,
                        unit = AllSettings.gestureControl,
                        title = stringResource(R.string.settings_control_gesture_control_title),
                        summary = stringResource(R.string.settings_control_gesture_control_summary)
                    )

                    ListSettingsCard(
                        modifier = Modifier.fillMaxWidth(),
                        position = CardPosition.Middle,
                        unit = AllSettings.gestureTapMouseAction,
                        items = GestureActionType.entries,
                        title = stringResource(R.string.settings_control_gesture_tap_action_title),
                        summary = stringResource(R.string.settings_control_gesture_tap_action_summary),
                        getItemText = { stringResource(it.nameRes) },
                        enabled = AllSettings.gestureControl.state
                    )

                    ListSettingsCard(
                        modifier = Modifier.fillMaxWidth(),
                        position = CardPosition.Middle,
                        unit = AllSettings.gestureLongPressMouseAction,
                        items = GestureActionType.entries,
                        title = stringResource(R.string.settings_control_gesture_long_press_action_title),
                        summary = stringResource(R.string.settings_control_gesture_long_press_action_summary),
                        getItemText = { stringResource(it.nameRes) },
                        enabled = AllSettings.gestureControl.state
                    )

                    IntSliderSettingsCard(
                        modifier = Modifier.fillMaxWidth(),
                        position = CardPosition.Bottom,
                        unit = AllSettings.gestureLongPressDelay,
                        title = stringResource(R.string.settings_control_gesture_long_press_delay_title),
                        summary = stringResource(R.string.settings_control_mouse_long_press_delay_summary),
                        valueRange = AllSettings.gestureLongPressDelay.floatRange,
                        suffix = "ms",
                        enabled = AllSettings.gestureControl.state,
                        fineTuningControl = true
                    )
                }
            }

            AnimatedItem(scope) { yOffset ->
                SettingsCardColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset { IntOffset(x = 0, y = yOffset.roundToPx()) }
                ) {
                    //检查陀螺仪是否可用
                    val context = LocalContext.current
                    val isGyroscopeAvailable = remember(context) {
                        isGyroscopeAvailable(context = context)
                    }

                    SwitchSettingsCard(
                        modifier = Modifier.fillMaxWidth(),
                        position = CardPosition.Top,
                        unit = AllSettings.gyroscopeControl,
                        title = stringResource(R.string.settings_control_gyroscope_title),
                        summary = stringResource(R.string.settings_control_gyroscope_summary),
                        enabled = isGyroscopeAvailable,
                        trailingIcon = if (!isGyroscopeAvailable) {
                            @Composable {
                                TooltipIconButton(
                                    modifier = Modifier
                                        .align(Alignment.CenterVertically)
                                        .padding(horizontal = 8.dp),
                                    tooltipTitle = stringResource(R.string.generic_warning),
                                    tooltipMessage = stringResource(R.string.settings_control_gyroscope_unsupported)
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.ic_warning_filled),
                                        contentDescription = stringResource(R.string.generic_warning),
                                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                                    )
                                }
                            }
                        } else null
                    )

                    IntSliderSettingsCard(
                        modifier = Modifier.fillMaxWidth(),
                        position = CardPosition.Middle,
                        unit = AllSettings.gyroscopeSensitivity,
                        title = stringResource(R.string.settings_control_gyroscope_sensitivity_title),
                        valueRange = AllSettings.gyroscopeSensitivity.floatRange,
                        suffix = "%",
                        enabled = isGyroscopeAvailable && AllSettings.gyroscopeControl.state,
                        fineTuningControl = true
                    )

                    IntSliderSettingsCard(
                        modifier = Modifier.fillMaxWidth(),
                        position = CardPosition.Middle,
                        unit = AllSettings.gyroscopeSampleRate,
                        title = stringResource(R.string.settings_control_gyroscope_sample_rate_title),
                        summary = stringResource(R.string.settings_control_gyroscope_sample_rate_summary),
                        valueRange = AllSettings.gyroscopeSampleRate.floatRange,
                        suffix = "ms",
                        enabled = isGyroscopeAvailable && AllSettings.gyroscopeControl.state,
                        fineTuningControl = true
                    )

                    SwitchSettingsCard(
                        modifier = Modifier.fillMaxWidth(),
                        position = CardPosition.Middle,
                        unit = AllSettings.gyroscopeSmoothing,
                        title = stringResource(R.string.settings_control_gyroscope_smoothing_title),
                        summary = stringResource(R.string.settings_control_gyroscope_smoothing_summary),
                        enabled = isGyroscopeAvailable && AllSettings.gyroscopeControl.state
                    )

                    IntSliderSettingsCard(
                        modifier = Modifier.fillMaxWidth(),
                        position = CardPosition.Middle,
                        unit = AllSettings.gyroscopeSmoothingWindow,
                        title = stringResource(R.string.settings_control_gyroscope_smoothing_window_title),
                        summary = stringResource(R.string.settings_control_gyroscope_smoothing_window_summary),
                        valueRange = AllSettings.gyroscopeSmoothingWindow.floatRange,
                        enabled = isGyroscopeAvailable && AllSettings.gyroscopeControl.state && AllSettings.gyroscopeSmoothing.state
                    )

                    SwitchSettingsCard(
                        modifier = Modifier.fillMaxWidth(),
                        position = CardPosition.Middle,
                        unit = AllSettings.gyroscopeInvertX,
                        title = stringResource(R.string.settings_control_gyroscope_invert_x_title),
                        summary = stringResource(R.string.settings_control_gyroscope_invert_x_summary),
                        enabled = isGyroscopeAvailable && AllSettings.gyroscopeControl.state
                    )

                    SwitchSettingsCard(
                        modifier = Modifier.fillMaxWidth(),
                        position = CardPosition.Bottom,
                        unit = AllSettings.gyroscopeInvertY,
                        title = stringResource(R.string.settings_control_gyroscope_invert_y_title),
                        summary = stringResource(R.string.settings_control_gyroscope_invert_y_summary),
                        enabled = isGyroscopeAvailable && AllSettings.gyroscopeControl.state
                    )
                }
            }
        }
    }
}

private sealed interface PhysicalKeyOperation {
    data object None: PhysicalKeyOperation
    data object Bind: PhysicalKeyOperation
}

@Composable
private fun PhysicalKeyImeTrigger(
    modifier: Modifier = Modifier,
    operation: PhysicalKeyOperation,
    changeOperation: (PhysicalKeyOperation) -> Unit,
    eventViewModel: EventViewModel
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .animateContentSize()
        ) {
            TitleAndSummary(
                title = stringResource(R.string.settings_control_physical_key_bind_ime_title),
                summary = stringResource(R.string.settings_control_physical_key_bind_ime_summary)
            )
            when (operation) {
                PhysicalKeyOperation.None -> {}
                PhysicalKeyOperation.Bind -> {
                    LaunchedEffect(Unit) {
                        eventViewModel.sendEvent(EventViewModel.Event.Key.StartKeyCapture)
                        //接收Activity发送的按键事件
                        eventViewModel.events
                            .filterIsInstance<EventViewModel.Event.Key.OnKeyDown>()
                            .collect { event ->
                                changeOperation(PhysicalKeyOperation.None)
                                AllSettings.physicalKeyImeCode.save(event.key.keyCode)
                            }
                    }

                    DisposableEffect(Unit) {
                        onDispose {
                            eventViewModel.sendEvent(EventViewModel.Event.Key.StopKeyCapture)
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        LittleTextLabel(text = stringResource(R.string.control_keyboard_bind_title))
                        MarqueeText(
                            modifier = Modifier
                                .weight(1f)
                                .infiniteShimmer(
                                    initialValue = 0.5f,
                                    targetValue = 1f
                                ),
                            text = stringResource(R.string.control_keyboard_bind_summary),
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }
        }

        Row(
            modifier = Modifier.padding(start = 8.dp)
        ) {
            val code = AllSettings.physicalKeyImeCode.state
            when {
                code == null -> {
                    Text(
                        modifier = Modifier.padding(end = 12.dp),
                        text = stringResource(R.string.settings_control_physical_key_bind_ime_un_bind),
                        style = MaterialTheme.typography.labelMedium
                    )
                }
                else -> {
                    IconTextButton(
                        onClick = { AllSettings.physicalKeyImeCode.save(null) },
                        painter = painterResource(R.drawable.ic_restart_alt),
                        contentDescription = stringResource(R.string.generic_reset),
                        text = stringResource(
                            R.string.settings_control_physical_key_bind_ime_bound,
                            formatKeyCode(code)
                        )
                    )
                }
            }
        }
    }
}

private sealed interface MousePointerOperation {
    data object None: MousePointerOperation
    /** 重置鼠标指针前的提醒 */
    data object PreReset: MousePointerOperation
    /** 重置鼠标指针 */
    data object Reset: MousePointerOperation
    /** 变更鼠标热点 */
    data object Hotspot: MousePointerOperation
}

@Composable
private fun MousePointerCard(
    modifier: Modifier = Modifier,
    position: CardPosition,
    title: String,
    summary: String,
    mouseSize: Int,
    mousePointerFile: File,
    cursorShape: CursorShape,
    hotspot: ParcelableSettingUnit<CursorHotspot>,
    mouseOperation: MousePointerOperation,
    changeOperation: (MousePointerOperation) -> Unit,
    submitError: (ErrorViewModel.ThrowableMessage) -> Unit
) {
    val context = LocalContext.current
    var triggerState by remember { mutableIntStateOf(0) }
    var fileExists by remember { mutableStateOf(false) }

    LaunchedEffect(triggerState) {
        fileExists = withContext(Dispatchers.IO) { mousePointerFile.exists() }
    }

    MousePointerOperation(
        operation = mouseOperation,
        changeOperation = changeOperation,
        mousePointerFile = mousePointerFile,
        hotspot = hotspot,
        cursorShape = cursorShape,
        onRefresh = {
            triggerState++
        }
    )

    val filePicker = rememberLauncherForActivityResult(
        contract = MediaPickerContract(
            allowImages = true,
            allowVideos = false,
            allowMultiple = false
        )
    ) { result ->
        if (result != null) {
            TaskSystem.submitTask(
                Task.runTask(
                    dispatcher = Dispatchers.IO,
                    task = {
                        context.copyLocalFile(result[0], mousePointerFile)
                        if (!mousePointerFile.isImageFile()) error("The selected file is not an image!")
                        triggerState++
                        changeOperation(MousePointerOperation.None)
                    },
                    onError = { th ->
                        FileUtils.deleteQuietly(mousePointerFile)
                        submitError(
                            ErrorViewModel.ThrowableMessage(
                                title = androidText(R.string.error_import_image),
                                message = androidText(th.getMessageOrToString())
                            )
                        )
                    }
                )
            )
        }
    }

    SettingsCard(
        modifier = modifier,
        position = position
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable { filePicker.launch(Unit) }
                    .padding(all = 16.dp)
            ) {
                TitleAndSummary(
                    title = title,
                    summary = summary
                )
            }

            Row(
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                MousePointer(
                    modifier = Modifier.padding(all = 8.dp),
                    mouseSize = mouseSize.dp,
                    cursorShape = cursorShape,
                    mouseFile = mousePointerFile,
                    centerIcon = true,
                    triggerRefresh = triggerState,
                    crossfade = true
                )

                IconTextButton(
                    onClick = {
                        if (mouseOperation == MousePointerOperation.None) {
                            changeOperation(MousePointerOperation.Hotspot)
                        }
                    },
                    painter = painterResource(R.drawable.ic_highlight_mouse_cursor),
                    contentDescription = stringResource(R.string.settings_control_mouse_pointer_hotspot),
                    text = stringResource(R.string.settings_control_mouse_pointer_hotspot)
                )

                AnimatedVisibility(
                    visible = fileExists
                ) {
                    IconTextButton(
                        onClick = {
                            if (mouseOperation == MousePointerOperation.None) {
                                changeOperation(MousePointerOperation.PreReset)
                            }
                        },
                        painter = painterResource(R.drawable.ic_restart_alt),
                        contentDescription = stringResource(R.string.generic_reset),
                        text = stringResource(R.string.generic_reset)
                    )
                }
            }
        }
    }
}

@Composable
private fun MousePointerOperation(
    operation: MousePointerOperation,
    changeOperation: (MousePointerOperation) -> Unit,
    mousePointerFile: File,
    hotspot: ParcelableSettingUnit<CursorHotspot>,
    cursorShape: CursorShape,
    onRefresh: () -> Unit
) {
    when (operation) {
        is MousePointerOperation.None -> {}
        is MousePointerOperation.PreReset -> {
            SimpleAlertDialog(
                title = stringResource(R.string.generic_reset),
                text = stringResource(R.string.settings_control_mouse_pointer_reset_message),
                onConfirm = {
                    //正式开始重置鼠标指针
                    changeOperation(MousePointerOperation.Reset)
                },
                onDismiss = {
                    changeOperation(MousePointerOperation.None)
                }
            )
        }
        is MousePointerOperation.Reset -> {
            LaunchedEffect(Unit) {
                FileUtils.deleteQuietly(mousePointerFile)
                onRefresh()
                changeOperation(MousePointerOperation.None)
            }
        }
        is MousePointerOperation.Hotspot -> {
            MouseHotspotEditorDialog(
                hotspot = hotspot,
                cursorShape = cursorShape,
                onClose = {
                    changeOperation(MousePointerOperation.None)
                }
            )
        }
    }
}