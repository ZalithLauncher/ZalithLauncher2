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

package com.movtery.zalithlauncher.ui.screens.main.control_editor

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SingleChoiceSegmentedButtonRowScope
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.movtery.colorpicker.components.TransparentChecker
import com.movtery.colorpicker.rememberColorPickerController
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.ui.components.ColorPickerDialog
import com.movtery.zalithlauncher.ui.components.DefaultSwitch
import com.movtery.zalithlauncher.ui.components.LittleTextLabel
import com.movtery.zalithlauncher.ui.components.MarqueeText
import com.movtery.zalithlauncher.ui.components.SimpleTextSlider
import com.movtery.zalithlauncher.ui.components.SliderValueEditDialog
import com.movtery.zalithlauncher.ui.screens.content.elements.DisabledAlpha
import com.movtery.zalithlauncher.ui.theme.itemColor
import com.movtery.zalithlauncher.ui.theme.onItemColor
import com.movtery.zalithlauncher.utils.animation.getAnimateTween


@Composable
fun InfoLayoutSliderItem(
    modifier: Modifier = Modifier,
    title: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    onValueChangeFinished: (() -> Unit)? = null,
    decimalFormat: String = "#0.00",
    suffix: String? = null,
    fineTuningControl: Boolean = true,
    fineTuningStep: Float = 0.5f,
    enabled: Boolean = true,
    color: Color = itemColor(false),
    contentColor: Color = onItemColor(),
) {
    var showValueEditDialog by remember { mutableStateOf(false) }

    InfoLayoutItem(
        modifier = modifier,
        onClick = {},
        enabled = enabled,
        color = color,
        contentColor = contentColor
    ) {
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium
            )
            SimpleTextSlider(
                modifier = Modifier.fillMaxWidth(),
                shorter = true,
                value = value,
                decimalFormat = decimalFormat,
                enabled = enabled,
                onValueChange = onValueChange,
                valueRange = valueRange,
                onValueChangeFinished = onValueChangeFinished,
                onTextClick = { showValueEditDialog = true },
                suffix = suffix,
                fineTuningControl = fineTuningControl,
                fineTuningStep = fineTuningStep
            )
        }
    }

    if (showValueEditDialog) {
        SliderValueEditDialog(
            onDismissRequest = { showValueEditDialog = false },
            title = title,
            valueRange = valueRange,
            value = value,
            onValueChange = onValueChange,
            onValueChangeFinished = {
                onValueChangeFinished?.invoke()
            }
        )
    }
}

/**
 * 列表信息设置项
 */
@Composable
fun <E> InfoLayoutListItem(
    modifier: Modifier = Modifier,
    title: String,
    items: List<E>,
    selectedItem: E,
    onItemSelected: (E) -> Unit,
    getItemText: @Composable (E) -> String,
    color: Color = itemColor(false),
    contentColor: Color = onItemColor(),
    maxListHeight: Dp = 200.dp
) {
    var expanded by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        color = color,
        contentColor = contentColor,
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            InfoListLayoutHeader(
                modifier = Modifier,
                items = items,
                title = title,
                selectedItemLayout = {
                    LittleTextLabel(text = getItemText(selectedItem))
                },
                expanded = expanded,
                onClick = { expanded = !expanded }
            )

            if (items.isNotEmpty()) {
                fun onClick(item: E) {
                    if (expanded && selectedItem != item) {
                        onItemSelected(item)
                        expanded = false
                    }
                }

                Column(modifier = Modifier.fillMaxWidth()) {
                    AnimatedVisibility(
                        visible = expanded,
                        enter = expandVertically(animationSpec = getAnimateTween()),
                        exit = shrinkVertically(animationSpec = getAnimateTween()) + fadeOut(),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = maxListHeight)
                                .padding(vertical = 4.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp)
                        ) {
                            items(items) { item ->
                                Row(
                                    modifier = modifier
                                        .clip(shape = MaterialTheme.shapes.medium)
                                        .clickable {
                                            onClick(item)
                                        },
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = selectedItem == item,
                                        onClick = {
                                            onClick(item)
                                        }
                                    )
                                    Column(
                                        verticalArrangement = Arrangement.spacedBy(4.dp),
                                        content = {
                                            MarqueeText(
                                                text = getItemText(item),
                                                style = MaterialTheme.typography.labelMedium
                                            )
                                        }
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

@Composable
private fun <E> InfoListLayoutHeader(
    modifier: Modifier = Modifier,
    items: List<E>,
    title: String,
    selectedItemLayout: @Composable RowScope.() -> Unit,
    expanded: Boolean,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        MarqueeText(
            text = title,
            style = MaterialTheme.typography.bodyMedium
        )
        Row(
            Modifier
                .weight(1f)
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.End
        ) {
            selectedItemLayout()
        }

        if (!items.isEmpty()) {
            Row(
                modifier = Modifier.padding(end = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                val rotation by animateFloatAsState(
                    targetValue = if (expanded) -180f else 0f,
                    animationSpec = getAnimateTween()
                )
                Icon(
                    modifier = Modifier
                        .size(28.dp)
                        .rotate(rotation),
                    painter = painterResource(R.drawable.ic_arrow_drop_down_rounded),
                    contentDescription = stringResource(if (expanded) R.string.generic_expand else R.string.generic_collapse)
                )
            }
        }
    }
}

@Composable
fun InfoLayoutSwitchItem(
    modifier: Modifier = Modifier,
    title: String,
    value: Boolean,
    onValueChange: (Boolean) -> Unit,
    enabled: Boolean = true,
    color: Color = itemColor(false),
    contentColor: Color = onItemColor()
) {
    InfoLayoutItem(
        modifier = modifier,
        onClick = {
            onValueChange(!value)
        },
        enabled = enabled,
        color = color,
        contentColor = contentColor
    ) {
        MarqueeText(
            modifier = Modifier
                .alpha(if (enabled) 1f else DisabledAlpha)
                .weight(1f),
            text = title,
            style = MaterialTheme.typography.bodyMedium
        )
        DefaultSwitch(
            checked = value,
            onCheckedChange = onValueChange,
            enabled = enabled
        )
    }
}

@Composable
fun <E> InfoLayoutSelectItem(
    modifier: Modifier = Modifier,
    title: String,
    options: List<E>,
    current: E,
    onClick: (E) -> Unit,
    label: @Composable SingleChoiceSegmentedButtonRowScope.(E) -> Unit,
    color: Color = itemColor(false),
    contentColor: Color = onItemColor()
) {
    InfoLayoutItem(
        modifier = modifier,
        onClick = {},
        color = color,
        contentColor = contentColor
    ) {
        MarqueeText(
            modifier = Modifier.weight(1f),
            text = title,
            style = MaterialTheme.typography.bodyMedium
        )
        SingleChoiceSegmentedButtonRow {
            options.forEachIndexed { index, option ->
                SegmentedButton(
                    selected = current == option,
                    shape = SegmentedButtonDefaults.itemShape(
                        index = index,
                        count = options.size
                    ),
                    onClick = {
                        onClick(option)
                    },
                    label = {
                        label(option)
                    }
                )
            }
        }
    }
}

@Composable
fun InfoLayoutTextItem(
    modifier: Modifier = Modifier,
    title: String,
    onClick: () -> Unit,
    showArrow: Boolean = true,
    selected: Boolean = false,
    color: Color = itemColor(false),
    contentColor: Color = onItemColor(),
    enabled: Boolean = true,
) {
    InfoLayoutTextItem(
        modifier = modifier,
        title = title,
        icon = {
            if (showArrow) {
                Icon(
                    modifier = Modifier
                        .size(28.dp),
                    painter = painterResource(R.drawable.ic_arrow_right_rounded),
                    contentDescription = null
                )
            }
        },
        onClick = onClick,
        selected = selected,
        color = color,
        contentColor = contentColor,
        enabled = enabled,
    )
}

@Composable
fun InfoLayoutTextItem(
    modifier: Modifier = Modifier,
    title: String,
    icon: @Composable () -> Unit,
    onClick: () -> Unit,
    selected: Boolean = false,
    color: Color = itemColor(false),
    contentColor: Color = onItemColor(),
    enabled: Boolean = true,
) {
    InfoLayoutItem(
        modifier = modifier,
        onClick = onClick,
        selected = selected,
        color = color,
        contentColor = contentColor,
        enabled = enabled,
    ) {
        MarqueeText(
            modifier = Modifier
                .padding(vertical = 8.dp)
                .weight(1f),
            text = title,
            style = MaterialTheme.typography.bodyMedium
        )
        icon()
    }
}

@Composable
fun InfoLayoutColorItem(
    modifier: Modifier = Modifier,
    title: String,
    color: Color,
    onColorChanged: (Color) -> Unit
) {
    var showColorDialog by remember { mutableStateOf(false) }

    InfoLayoutTextItem(
        modifier = modifier,
        title = title,
        icon = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(modifier = Modifier.size(28.dp)) {
                    TransparentChecker(
                        modifier = Modifier.fillMaxSize(),
                        gridSize = 18f
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(color = color)
                    )
                }

                Icon(
                    modifier = Modifier
                        .size(28.dp),
                    painter = painterResource(R.drawable.ic_arrow_right_rounded),
                    contentDescription = null
                )
            }
        },
        onClick = {
            showColorDialog = true
        }
    )

    if (showColorDialog) {
        var tempColor by remember { mutableStateOf(color) }
        val colorController = rememberColorPickerController(initialColor = tempColor)

        val currentColor by remember(colorController) { colorController.color }

        LaunchedEffect(currentColor) {
            onColorChanged(currentColor)
        }

        ColorPickerDialog(
            colorController = colorController,
            onCancel = {
                onColorChanged(colorController.getOriginalColor())
                showColorDialog = false
            },
            onConfirm = { color ->
                showColorDialog = false
                onColorChanged(color)
            }
        )
    }
}

@Composable
fun InfoLayoutItem(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    selected: Boolean = false,
    enabled: Boolean = true,
    shape: Shape = MaterialTheme.shapes.large,
    borderColor: Color = MaterialTheme.colorScheme.primary,
    color: Color = itemColor(false),
    contentColor: Color = onItemColor(),
    content: @Composable RowScope.() -> Unit
) {
    val borderWidth by animateDpAsState(
        if (selected) 2.dp else (-1).dp
    )

    Surface(
        modifier = modifier
            .border(
                width = borderWidth,
                color = borderColor,
                shape = shape
            ),
        color = color,
        contentColor = contentColor,
        shape = shape,
        onClick = onClick,
        enabled = enabled
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape = MaterialTheme.shapes.large)
                .padding(all = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            content = content
        )
    }
}