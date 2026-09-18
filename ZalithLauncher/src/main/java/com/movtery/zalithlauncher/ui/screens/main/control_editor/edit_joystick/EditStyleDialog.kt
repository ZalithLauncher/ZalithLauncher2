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

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.movtery.layer_controller.data.BORDER_RADIO_RANGE
import com.movtery.layer_controller.data.SHAPE_PERCENT_RANGE
import com.movtery.layer_controller.data.SIZE_PERCENT_RANGE
import com.movtery.layer_controller.layout.JoystickStyleWidget
import com.movtery.layer_controller.observable.ObservableJoystickStyle
import com.movtery.layer_controller.observable.ObservableJoystickStyleConfig
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.setting.unit.toFloatRange
import com.movtery.zalithlauncher.ui.components.MarqueeText
import com.movtery.zalithlauncher.ui.components.SingleLineTextCheck
import com.movtery.zalithlauncher.ui.screens.main.control_editor.InfoLayoutColorItem
import com.movtery.zalithlauncher.ui.screens.main.control_editor.InfoLayoutSliderItem
import com.movtery.zalithlauncher.ui.screens.main.control_editor.InfoLayoutSwitchItem
import com.movtery.zalithlauncher.ui.screens.rememberSwapTween
import com.movtery.zalithlauncher.ui.theme.cardColor
import com.movtery.zalithlauncher.ui.theme.itemColor
import com.movtery.zalithlauncher.ui.theme.onCardColor
import com.movtery.zalithlauncher.ui.theme.onItemColor

private data class TabItem(val titleRes: Int)

/**
 * 摇杆样式编辑对话框
 * **不再真正使用Dialog，真的会有性能问题！**
 */
@Composable
fun EditJoystickStyleDialog(
    visible: Boolean,
    style: ObservableJoystickStyle?,
    onClose: () -> Unit,
) {
    val tween = rememberSwapTween()

    AnimatedVisibility(
        modifier = Modifier.fillMaxSize(),
        visible = visible,
        enter = fadeIn(animationSpec = tween),
        exit = fadeOut(animationSpec = tween)
    ) {
        val tabs = remember {
            listOf(
                TabItem(R.string.control_editor_edit_style_config_light),
                TabItem(R.string.control_editor_edit_style_config_dark)
            )
        }

        val pagerState = rememberPagerState(pageCount = { tabs.size })
        var selectedTabIndex by remember { mutableIntStateOf(0) }

        LaunchedEffect(selectedTabIndex) {
            pagerState.animateScrollToPage(selectedTabIndex)
        }

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            //作为背景层，被点击时关闭Dialog
            if (visible) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .alpha(0f)
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() },
                            onClick = onClose
                        )
                )
            }

            if (style != null) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .fillMaxHeight()
                        .padding(all = 16.dp),
                    shadowElevation = 3.dp,
                    color = cardColor(false),
                    contentColor = onCardColor(),
                    shape = MaterialTheme.shapes.extraLarge
                ) {
                    Row(
                        modifier = Modifier.fillMaxHeight()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxHeight()
                                .padding(all = 12.dp)
                                .weight(0.4f),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            RenderBox(
                                modifier = Modifier.weight(1f),
                                style = style,
                                isDarkMode = !style.commonStyle && selectedTabIndex == 1
                            )
                            // 不区分暗色主题
                            InfoLayoutSwitchItem(
                                modifier = Modifier.fillMaxWidth(),
                                title = stringResource(R.string.control_editor_edit_style_config_common_style),
                                value = style.commonStyle,
                                onValueChange = { style.commonStyle = it }
                            )
                        }

                        Column(
                            modifier = Modifier
                                .weight(0.6f)
                                .fillMaxHeight()
                        ) {
                            SingleLineTextCheck(
                                text = style.name,
                                onSingleLined = { style.name = it }
                            )

                            if (style.commonStyle) {
                                StyleConfigEditor(
                                    modifier = Modifier.fillMaxSize(),
                                    config = style.lightStyle
                                )
                            } else {
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
                    }
                }
            }
        }
    }
}

@Composable
private fun StyleConfigEditor(
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

/**
 * 渲染摇杆样式预览
 */
@Composable
private fun RenderBox(
    style: ObservableJoystickStyle,
    isDarkMode: Boolean,
    modifier: Modifier = Modifier,
    color: Color = itemColor(false),
    contentColor: Color = onItemColor(),
    shape: Shape = MaterialTheme.shapes.large
) {
    Surface(
        modifier = modifier,
        color = color,
        contentColor = contentColor,
        shape = shape
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(all = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            JoystickStyleWidget(
                modifier = Modifier.size(120.dp),
                style = style,
                isDarkTheme = isDarkMode
            )
        }
    }
}