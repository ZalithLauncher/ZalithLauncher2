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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.movtery.layer_controller.observable.ObservableJoystickData
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.ui.base.BaseScreen
import com.movtery.zalithlauncher.ui.screens.TitledNavKey
import com.movtery.zalithlauncher.ui.screens.main.control_editor.InfoLayoutSliderItem
import com.movtery.zalithlauncher.ui.screens.main.control_editor.InfoLayoutSwitchItem

/**
 * 编辑摇杆设置
 */
@Composable
fun EditJoystickSettings(
    screenKey: TitledNavKey,
    currentKey: TitledNavKey?,
    data: ObservableJoystickData
) {
    BaseScreen(
        screenKey = screenKey,
        currentKey = currentKey
    ) {
        LazyColumn(
            modifier = Modifier
                .padding(start = 4.dp, end = 8.dp)
                .fillMaxSize(),
            contentPadding = PaddingValues(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            //摇杆死区范围
            item {
                InfoLayoutSliderItem(
                    modifier = Modifier.fillMaxWidth(),
                    title = stringResource(R.string.game_styles_joystick_deadzone),
                    value = data.deadZoneRatio * 100f,
                    onValueChange = { data.deadZoneRatio = it / 100f },
                    valueRange = 0f..100f,
                    decimalFormat = "#0",
                    suffix = "%",
                    fineTuningStep = 1.0f
                )
            }

            //摇杆前进锁判定范围
            item {
                InfoLayoutSliderItem(
                    modifier = Modifier.fillMaxWidth(),
                    title = stringResource(R.string.game_styles_joystick_lock_threshold),
                    value = data.lockThreshold * 100f,
                    onValueChange = { data.lockThreshold = it / 100f },
                    valueRange = 0f..100f,
                    decimalFormat = "#0",
                    suffix = "%",
                    fineTuningStep = 1.0f
                )
            }

            //前进锁定
            item {
                InfoLayoutSwitchItem(
                    modifier = Modifier.fillMaxWidth(),
                    title = stringResource(R.string.game_styles_joystick_can_lock),
                    value = data.canLock,
                    onValueChange = { data.canLock = it }
                )
            }

            //锁定时强制疾跑
            item {
                InfoLayoutSwitchItem(
                    modifier = Modifier.fillMaxWidth(),
                    title = stringResource(R.string.game_styles_joystick_lock_sprint),
                    value = data.lockSpring,
                    onValueChange = { data.lockSpring = it },
                    enabled = data.canLock
                )
            }
        }
    }
}
