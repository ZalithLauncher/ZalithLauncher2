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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.movtery.layer_controller.data.ButtonPosition
import com.movtery.layer_controller.data.ButtonSize
import com.movtery.layer_controller.data.JOYSTICK_MIN_SIZE_DP
import com.movtery.layer_controller.data.JOYSTICK_MIN_SIZE_PERCENTAGE
import com.movtery.layer_controller.data.MIN_SIZE_DP
import com.movtery.layer_controller.data.SIZE_PERCENTAGE_EDITOR
import com.movtery.layer_controller.data.VisibilityType
import com.movtery.layer_controller.observable.ObservableJoystickData
import com.movtery.layer_controller.observable.ObservableNormalData
import com.movtery.layer_controller.observable.ObservableTextData
import com.movtery.layer_controller.observable.ObservableWidget
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.ui.base.BaseScreen
import com.movtery.zalithlauncher.ui.screens.TitledNavKey
import com.movtery.zalithlauncher.ui.screens.main.control_editor.InfoLayoutListItem
import com.movtery.zalithlauncher.ui.screens.main.control_editor.InfoLayoutSliderItem
import com.movtery.zalithlauncher.ui.screens.main.control_editor.getVisibilityText

/**
 * 编辑控件基本信息
 */
@Composable
fun EditWidgetInfo(
    screenKey: TitledNavKey,
    currentKey: TitledNavKey?,
    data: ObservableWidget,
    onPreviewRequested: () -> Unit,
    onDismissRequested: () -> Unit
) {
    BaseScreen(
        screenKey = screenKey,
        currentKey = currentKey
    ) {
        val screenSize = LocalWindowInfo.current.containerSize
        val density = LocalDensity.current
        val screenWidth = remember(screenSize, density) {
            with(density) { screenSize.width.toDp() }.value
        }
        val screenHeight = remember(screenSize, density) {
            with(density) { screenSize.height.toDp() }.value
        }

        LazyColumn(
            modifier = Modifier
                .padding(start = 4.dp, end = 8.dp)
                .fillMaxSize(),
            contentPadding = PaddingValues(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            when (data) {
                is ObservableTextData -> {
                    commonInfos(
                        onPreviewRequested = onPreviewRequested,
                        onDismissRequested = onDismissRequested,
                        screenWidth = screenWidth,
                        screenHeight = screenHeight,
                        visibilityType = data.visibilityType,
                        onVisibilityTypeChanged = { data.visibilityType = it },
                        position = data.position,
                        onPositionChanged = { data.position = it },
                        buttonSize = data.buttonSize,
                        onButtonSizeChanged = { data.buttonSize = it }
                    )
                }
                is ObservableNormalData -> {
                    commonInfos(
                        onPreviewRequested = onPreviewRequested,
                        onDismissRequested = onDismissRequested,
                        screenWidth = screenWidth,
                        screenHeight = screenHeight,
                        visibilityType = data.visibilityType,
                        onVisibilityTypeChanged = { data.visibilityType = it },
                        position = data.position,
                        onPositionChanged = { data.position = it },
                        buttonSize = data.buttonSize,
                        onButtonSizeChanged = { data.buttonSize = it }
                    )
                }
                is ObservableJoystickData -> {
                    joystickInfos(
                        onPreviewRequested = onPreviewRequested,
                        onDismissRequested = onDismissRequested,
                        screenWidth = screenWidth,
                        screenHeight = screenHeight,
                        data = data
                    )
                }
            }
        }
    }
}

private fun LazyListScope.commonInfos(
    onPreviewRequested: () -> Unit,
    onDismissRequested: () -> Unit,
    screenWidth: Float,
    screenHeight: Float,
    visibilityType: VisibilityType,
    onVisibilityTypeChanged: (VisibilityType) -> Unit,
    position: ButtonPosition,
    onPositionChanged: (ButtonPosition) -> Unit,
    buttonSize: ButtonSize,
    onButtonSizeChanged: (ButtonSize) -> Unit,
) {
    //可见场景
    item {
        InfoLayoutListItem(
            modifier = Modifier.fillMaxWidth(),
            title = stringResource(R.string.control_editor_edit_visibility),
            items = VisibilityType.entries,
            selectedItem = visibilityType,
            onItemSelected = { onVisibilityTypeChanged(it) },
            getItemText = { it.getVisibilityText() }
        )
    }

    item {
        Spacer(modifier = Modifier.height(4.dp))
    }

    //x
    item {
        InfoLayoutSliderItem(
            modifier = Modifier.fillMaxWidth(),
            title = stringResource(R.string.control_editor_edit_position_x),
            value = position.x / 100f,
            onValueChange = {
                onPositionChanged(position.copy(x = (it * 100).toInt()))
                onPreviewRequested()
            },
            valueRange = 0f..100f,
            onValueChangeFinished = onDismissRequested,
            decimalFormat = "#0.00",
            suffix = "%"
        )
    }

    //y
    item {
        InfoLayoutSliderItem(
            modifier = Modifier.fillMaxWidth(),
            title = stringResource(R.string.control_editor_edit_position_y),
            value = position.y / 100f,
            onValueChange = {
                onPositionChanged(position.copy(y = (it * 100).toInt()))
                onPreviewRequested()
            },
            valueRange = 0f..100f,
            onValueChangeFinished = onDismissRequested,
            decimalFormat = "#0.00",
            suffix = "%"
        )
    }

    item {
        Spacer(modifier = Modifier.height(4.dp))
    }

    //尺寸类型
    item {
        InfoLayoutListItem(
            modifier = Modifier.fillMaxWidth(),
            title = stringResource(R.string.control_editor_edit_size_type),
            items = ButtonSize.Type.entries,
            selectedItem = buttonSize.type,
            onItemSelected = { onButtonSizeChanged(buttonSize.copy(type = it)) },
            getItemText = { type ->
                val textRes = when (type) {
                    ButtonSize.Type.Dp -> R.string.control_editor_edit_size_type_dp
                    ButtonSize.Type.Percentage -> R.string.control_editor_edit_size_type_percentage
                    ButtonSize.Type.WrapContent -> R.string.control_editor_edit_size_type_wrap_content
                }
                stringResource(textRes)
            }
        )
    }

    when (buttonSize.type) {
        ButtonSize.Type.Dp -> {
            //绝对宽度
            item {
                InfoLayoutSliderItem(
                    modifier = Modifier.fillMaxWidth(),
                    title = stringResource(R.string.control_editor_edit_size_width),
                    value = buttonSize.widthDp,
                    onValueChange = {
                        onButtonSizeChanged(buttonSize.copy(widthDp = it))
                        onPreviewRequested()
                    },
                    valueRange = MIN_SIZE_DP..screenWidth,
                    onValueChangeFinished = onDismissRequested,
                    suffix = "Dp"
                )
            }

            //绝对高度
            item {
                InfoLayoutSliderItem(
                    modifier = Modifier.fillMaxWidth(),
                    title = stringResource(R.string.control_editor_edit_size_height),
                    value = buttonSize.heightDp,
                    onValueChange = {
                        onButtonSizeChanged(buttonSize.copy(heightDp = it))
                        onPreviewRequested()
                    },
                    valueRange = MIN_SIZE_DP..screenHeight,
                    onValueChangeFinished = onDismissRequested,
                    suffix = "Dp"
                )
            }
        }
        ButtonSize.Type.Percentage -> {
            @Composable fun ButtonSize.Reference.getReferenceText(): String {
                val textRes = when (this) {
                    ButtonSize.Reference.ScreenWidth -> R.string.control_editor_edit_size_reference_screen_width
                    ButtonSize.Reference.ScreenHeight -> R.string.control_editor_edit_size_reference_screen_height
                }
                return stringResource(textRes)
            }

            //百分比宽度
            item {
                InfoLayoutSliderItem(
                    modifier = Modifier.fillMaxWidth(),
                    title = stringResource(R.string.control_editor_edit_size_width),
                    value = buttonSize.widthPercentage / 100f,
                    onValueChange = {
                        onButtonSizeChanged(buttonSize.copy(widthPercentage = (it * 100).toInt()))
                        onPreviewRequested()
                    },
                    valueRange = SIZE_PERCENTAGE_EDITOR,
                    onValueChangeFinished = onDismissRequested,
                    decimalFormat = "#0.00",
                    suffix = "%"
                )
            }

            //控件宽度参考对象
            item {
                InfoLayoutListItem(
                    modifier = Modifier.fillMaxWidth(),
                    title = stringResource(R.string.control_editor_edit_size_width_reference),
                    items = ButtonSize.Reference.entries,
                    selectedItem = buttonSize.widthReference,
                    onItemSelected = {
                        onButtonSizeChanged(buttonSize.copy(widthReference = it))
                    },
                    getItemText = { it.getReferenceText() }
                )
            }

            //百分比高度
            item {
                InfoLayoutSliderItem(
                    modifier = Modifier.fillMaxWidth(),
                    title = stringResource(R.string.control_editor_edit_size_height),
                    value = buttonSize.heightPercentage / 100f,
                    onValueChange = {
                        onButtonSizeChanged(buttonSize.copy(heightPercentage = (it * 100).toInt()))
                        onPreviewRequested()
                    },
                    valueRange = SIZE_PERCENTAGE_EDITOR,
                    onValueChangeFinished = onDismissRequested,
                    decimalFormat = "#0.00",
                    suffix = "%"
                )
            }

            //控件高度参考对象
            item {
                InfoLayoutListItem(
                    modifier = Modifier.fillMaxWidth(),
                    title = stringResource(R.string.control_editor_edit_size_height_reference),
                    items = ButtonSize.Reference.entries,
                    selectedItem = buttonSize.heightReference,
                    onItemSelected = {
                        onButtonSizeChanged(buttonSize.copy(heightReference = it))
                    },
                    getItemText = { it.getReferenceText() }
                )
            }
        }
        ButtonSize.Type.WrapContent -> {}
    }
}

private fun LazyListScope.joystickInfos(
    onPreviewRequested: () -> Unit,
    onDismissRequested: () -> Unit,
    screenWidth: Float,
    screenHeight: Float,
    data: ObservableJoystickData
) {
    // 可见场景
    item {
        InfoLayoutListItem(
            modifier = Modifier.fillMaxWidth(),
            title = stringResource(R.string.control_editor_edit_visibility),
            items = VisibilityType.entries,
            selectedItem = data.visibilityType,
            onItemSelected = { data.visibilityType = it },
            getItemText = { it.getVisibilityText() }
        )
    }

    item {
        Spacer(modifier = Modifier.height(4.dp))
    }

    // 位置 X
    item {
        InfoLayoutSliderItem(
            modifier = Modifier.fillMaxWidth(),
            title = stringResource(R.string.control_editor_edit_position_x),
            value = data.position.x / 100f,
            onValueChange = {
                data.position = data.position.copy(x = (it * 100).toInt())
                onPreviewRequested()
            },
            valueRange = 0f..100f,
            onValueChangeFinished = onDismissRequested,
            decimalFormat = "#0.00",
            suffix = "%"
        )
    }

    // 位置 Y
    item {
        InfoLayoutSliderItem(
            modifier = Modifier.fillMaxWidth(),
            title = stringResource(R.string.control_editor_edit_position_y),
            value = data.position.y / 100f,
            onValueChange = {
                data.position = data.position.copy(y = (it * 100).toInt())
                onPreviewRequested()
            },
            valueRange = 0f..100f,
            onValueChangeFinished = onDismissRequested,
            decimalFormat = "#0.00",
            suffix = "%"
        )
    }

    item {
        Spacer(modifier = Modifier.height(4.dp))
    }

    // 尺寸类型
    val sizeTypes = listOf(ButtonSize.Type.Dp, ButtonSize.Type.Percentage)
    item {
        InfoLayoutListItem(
            modifier = Modifier.fillMaxWidth(),
            title = stringResource(R.string.control_editor_edit_size_type),
            items = sizeTypes,
            selectedItem = data.sizeType,
            onItemSelected = { data.sizeType = it },
            getItemText = { type ->
                val textRes = when (type) {
                    ButtonSize.Type.Dp -> R.string.control_editor_edit_size_type_dp
                    ButtonSize.Type.Percentage -> R.string.control_editor_edit_size_type_percentage
                    ButtonSize.Type.WrapContent -> R.string.control_editor_edit_size_type_wrap_content
                }
                stringResource(textRes)
            }
        )
    }

    when (data.sizeType) {
        ButtonSize.Type.Dp -> {
            item {
                InfoLayoutSliderItem(
                    modifier = Modifier.fillMaxWidth(),
                    title = stringResource(R.string.control_editor_edit_size_joystick),
                    value = data.sizeDp,
                    onValueChange = {
                        data.sizeDp = it
                        onPreviewRequested()
                    },
                    valueRange = JOYSTICK_MIN_SIZE_DP..screenHeight,
                    onValueChangeFinished = onDismissRequested,
                    suffix = "Dp"
                )
            }
        }
        ButtonSize.Type.Percentage -> {
            item {
                InfoLayoutSliderItem(
                    modifier = Modifier.fillMaxWidth(),
                    title = stringResource(R.string.control_editor_edit_size_joystick),
                    value = data.sizePercentage / 100f,
                    onValueChange = {
                        data.sizePercentage = (it * 100).toInt()
                        onPreviewRequested()
                    },
                    valueRange = (JOYSTICK_MIN_SIZE_PERCENTAGE / 100f)..100f,
                    onValueChangeFinished = onDismissRequested,
                    decimalFormat = "#0.00",
                    suffix = "%"
                )
            }
        }
        ButtonSize.Type.WrapContent -> {}
    }
}