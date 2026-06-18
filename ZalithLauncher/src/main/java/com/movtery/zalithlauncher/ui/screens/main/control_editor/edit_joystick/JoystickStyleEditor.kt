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

package com.movtery.zalithlauncher.ui.screens.main.control_editor.edit_joystick

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.movtery.layer_controller.data.BORDER_RADIO_RANGE
import com.movtery.layer_controller.data.SHAPE_PERCENT_RANGE
import com.movtery.layer_controller.data.SIZE_PERCENT_RANGE
import com.movtery.layer_controller.observable.ObservableJoystickStyle
import com.movtery.layer_controller.observable.ObservableJoystickStyleConfig
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.setting.unit.toFloatRange
import com.movtery.zalithlauncher.ui.components.MarqueeText
import com.movtery.zalithlauncher.ui.screens.main.control_editor.InfoLayoutColorItem
import com.movtery.zalithlauncher.ui.screens.main.control_editor.InfoLayoutSliderItem
import com.movtery.zalithlauncher.ui.theme.cardColor

internal data class JoystickTabItem(val titleRes: Int)

@Composable
fun JoystickStyleEditor(
    modifier: Modifier = Modifier,
    style: ObservableJoystickStyle,
    onTabChanged: (Int) -> Unit = {}
) {
    val tabs = remember {
        listOf(
            JoystickTabItem(R.string.control_editor_edit_style_config_light),
            JoystickTabItem(R.string.control_editor_edit_style_config_dark)
        )
    }

    val pagerState = rememberPagerState(pageCount = { tabs.size })
    var selectedTabIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(selectedTabIndex) {
        pagerState.animateScrollToPage(selectedTabIndex)
        onTabChanged(selectedTabIndex)
    }

    Column(
        modifier = modifier
    ) {
        //顶贴标签栏
        SecondaryTabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = cardColor(false)
        ) {
            tabs.forEachIndexed { index, item ->
                Tab(
                    selected = index == selectedTabIndex,
                    onClick = {
                        selectedTabIndex = index
                    },
                    text = {
                        MarqueeText(text = stringResource(item.titleRes))
                    }
                )
            }
        }

        HorizontalPager(
            state = pagerState,
            userScrollEnabled = false,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) { page ->
            when (page) {
                0 -> {
                    StyleConfigEditor(
                        modifier = Modifier.fillMaxSize(),
                        config = style.lightStyle,
                    )
                }

                1 -> {
                    StyleConfigEditor(
                        modifier = Modifier.fillMaxSize(),
                        config = style.darkStyle,
                    )
                }
            }
        }
    }
}

@Composable
internal fun StyleConfigEditor(
    modifier: Modifier = Modifier,
    config: ObservableJoystickStyleConfig
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        val itemModifier = Modifier
            .fillMaxWidth()
            .padding(end = 12.dp)

        //整体不透明度
        item(key = "opacity") {
            InfoLayoutSliderItem(
                modifier = itemModifier.animateItem(),
                title = stringResource(R.string.control_editor_edit_style_config_alpha),
                value = config.alpha * 100f,
                onValueChange = {
                    config.alpha = it / 100f
                },
                valueRange = 0f..100f,
                decimalFormat = "#0",
                suffix = "%",
                fineTuningStep = 0.1f
            )
        }

        item {
            Spacer(Modifier)
        }

        //背景颜色
        item(key = "background_color") {
            InfoLayoutColorItem(
                modifier = itemModifier.animateItem(),
                title = stringResource(R.string.control_editor_edit_style_config_background_color),
                color = config.backgroundColor,
                onColorChanged = {
                    config.backgroundColor = it
                }
            )
        }

        //摇杆颜色
        item(key = "joystick_color") {
            InfoLayoutColorItem(
                modifier = itemModifier.animateItem(),
                title = stringResource(R.string.control_editor_special_joystick_style_joystick_color),
                color = config.joystickColor,
                onColorChanged = {
                    config.joystickColor = it
                }
            )
        }

        //摇杆颜色（可锁定时）
        item(key = "joystick_color_can_lock") {
            InfoLayoutColorItem(
                modifier = itemModifier.animateItem(),
                title = stringResource(R.string.control_editor_special_joystick_style_joystick_can_lock_color),
                color = config.joystickCanLockColor,
                onColorChanged = {
                    config.joystickCanLockColor = it
                }
            )
        }

        //摇杆颜色（锁定时）
        item(key = "joystick_color_locked") {
            InfoLayoutColorItem(
                modifier = itemModifier.animateItem(),
                title = stringResource(R.string.control_editor_special_joystick_style_joystick_locked_color),
                color = config.joystickLockedColor,
                onColorChanged = {
                    config.joystickLockedColor = it
                }
            )
        }

        //前进锁定标记颜色
        item(key = "lock_mark_color") {
            InfoLayoutColorItem(
                modifier = itemModifier.animateItem(),
                title = stringResource(R.string.control_editor_special_joystick_style_lock_mark_color),
                color = config.lockMarkColor,
                onColorChanged = {
                    config.lockMarkColor = it
                }
            )
        }

        //边框颜色
        item(key = "border_color") {
            InfoLayoutColorItem(
                modifier = itemModifier.animateItem(),
                title = stringResource(R.string.control_editor_edit_style_config_border_color),
                color = config.borderColor,
                onColorChanged = {
                    config.borderColor = it
                }
            )
        }

        item {
            Spacer(Modifier)
        }

        //背景层圆角
        item(key = "background_shape") {
            InfoLayoutSliderItem(
                modifier = itemModifier,
                title = stringResource(R.string.control_editor_special_joystick_style_background_rounded_corner),
                value = config.backgroundShape.toFloat(),
                onValueChange = {
                    config.backgroundShape = it.toInt()
                },
                valueRange = SHAPE_PERCENT_RANGE.toFloatRange(),
                decimalFormat = "#0",
                suffix = "%",
                fineTuningStep = 1f,
            )
        }

        //边框粗细
        item(key = "border_width") {
            InfoLayoutSliderItem(
                modifier = itemModifier.animateItem(),
                title = stringResource(R.string.control_editor_edit_style_config_border_width),
                value = config.borderWidthRatio.toFloat(),
                onValueChange = {
                    config.borderWidthRatio = it.toInt()
                },
                valueRange = BORDER_RADIO_RANGE.toFloatRange(),
                decimalFormat = "#0",
                suffix = "%",
                fineTuningStep = 1f,
            )
        }

        //摇杆圆角
        item(key = "joystick_shape") {
            InfoLayoutSliderItem(
                modifier = itemModifier,
                title = stringResource(R.string.control_editor_special_joystick_style_joystick_rounded_corner),
                value = config.joystickShape.toFloat(),
                onValueChange = {
                    config.joystickShape = it.toInt()
                },
                valueRange = SHAPE_PERCENT_RANGE.toFloatRange(),
                decimalFormat = "#0",
                suffix = "%",
                fineTuningStep = 1f,
            )
        }

        //摇杆大小
        item(key = "joystick_size") {
            InfoLayoutSliderItem(
                modifier = itemModifier,
                title = stringResource(R.string.control_editor_special_joystick_style_joystick_size),
                value = config.joystickSize,
                onValueChange = {
                    config.joystickSize = it
                },
                valueRange = SIZE_PERCENT_RANGE,
                suffix = "%",
            )
        }
    }
}
