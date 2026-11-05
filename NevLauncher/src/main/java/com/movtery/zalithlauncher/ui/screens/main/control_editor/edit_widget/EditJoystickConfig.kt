package com.movtery.zalithlauncher.ui.screens.main.control_editor.edit_widget

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.movtery.layer_controller.data.JOYSTICK_DEAD_ZONE_RANGE
import com.movtery.layer_controller.data.JOYSTICK_LOCK_THRESHOLD_RANGE
import com.movtery.layer_controller.data.JoystickTriggerMode
import com.movtery.layer_controller.observable.ObservableJoystickData
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.ui.base.BaseScreen
import com.movtery.zalithlauncher.ui.components.verticalScrollWithBar
import com.movtery.zalithlauncher.ui.screens.TitledNavKey
import com.movtery.zalithlauncher.ui.screens.main.control_editor.InfoLayoutListItem
import com.movtery.zalithlauncher.ui.screens.main.control_editor.InfoLayoutSliderItem
import com.movtery.zalithlauncher.ui.screens.main.control_editor.InfoLayoutSwitchItem
import com.movtery.zalithlauncher.ui.screens.main.control_editor.getTriggerModeText

@Composable
fun EditJoystickConfig(
    screenKey: TitledNavKey,
    currentKey: TitledNavKey?,
    data: ObservableJoystickData
) {
    BaseScreen(
        screenKey = screenKey,
        currentKey = currentKey
    ) {
        Column(
            modifier = Modifier
                .padding(start = 4.dp, end = 8.dp)
                .fillMaxSize()
                .verticalScrollWithBar(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Spacer(Modifier)

            // 死区比例
            InfoLayoutSliderItem(
                modifier = Modifier.fillMaxWidth(),
                title = stringResource(R.string.control_editor_edit_joystick_dead_zone),
                value = data.deadZoneRatio,
                onValueChange = { data.deadZoneRatio = it },
                valueRange = JOYSTICK_DEAD_ZONE_RANGE,
                decimalFormat = "#0.00",
                fineTuningStep = 0.1f,
            )

            // 操控方式
            InfoLayoutListItem(
                modifier = Modifier.fillMaxWidth(),
                title = stringResource(R.string.control_editor_edit_joystick_trigger_mode),
                items = JoystickTriggerMode.entries,
                selectedItem = data.triggerMode,
                onItemSelected = { data.triggerMode = it },
                getItemText = { it.getTriggerModeText() }
            )

            Spacer(modifier = Modifier.height(4.dp))

            // 前进锁
            InfoLayoutSwitchItem(
                modifier = Modifier.fillMaxWidth(),
                title = stringResource(R.string.control_editor_edit_joystick_can_lock),
                value = data.canLock,
                onValueChange = { data.canLock = it }
            )

            // 锁定阈值
            if (data.canLock) {
                InfoLayoutSliderItem(
                    modifier = Modifier.fillMaxWidth(),
                    title = stringResource(R.string.control_editor_edit_joystick_lock_threshold),
                    value = data.lockThreshold,
                    onValueChange = { data.lockThreshold = it },
                    valueRange = JOYSTICK_LOCK_THRESHOLD_RANGE,
                    decimalFormat = "#0.00",
                    fineTuningStep = 0.1f
                )
            }

            Spacer(Modifier)
        }
    }
}
