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

package com.movtery.zalithlauncher.ui.components

import androidx.annotation.IntRange
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderColors
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.ui.screens.content.elements.DisabledAlpha
import com.movtery.zalithlauncher.utils.math.addBigDecimal
import com.movtery.zalithlauncher.utils.math.subtractBigDecimal
import java.text.DecimalFormat

/**
 * 简单的文本滑动条，支持实时显示当前滑动条的数值，支持显示自定义数值的单位
 * @param shorter 是否使用更短的指示器的滑动条
 */
@Composable
fun SimpleTextSlider(
    modifier: Modifier = Modifier,
    shorter: Boolean = false,
    value: Float,
    decimalFormat: String = "#0.00",
    enabled: Boolean = true,
    onValueChange: (Float) -> Unit,
    toInt: Boolean = false,
    suffix: String? = null,
    steps: Int = 0,
    onValueChangeFinished: (() -> Unit)? = null,
    onTextClick: (() -> Unit)? = null,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    fineTuningControl: Boolean = false,
    fineTuningStep: Float = 0.5f,
    appendContent: @Composable () -> Unit = {}
) {
    val formatter = DecimalFormat(decimalFormat)
    fun getTextString(value: Float) = "${if (toInt) {
        value.toInt()
    } else {
        formatter.format(value)
    }}"

    fun changeValue(newValue: Float, finished: Boolean) {
        onValueChange(newValue)
        if (finished) onValueChangeFinished?.invoke()
    }

    LaunchedEffect(Unit) {
        //检查值是否被刻意的修改为超出范围
        if (value !in valueRange) {
            val newValue = value.coerceIn(valueRange)
            //调回范围内
            changeValue(newValue, true)
        }
    }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (shorter) {
            IndicatorSlider(
                value = value,
                enabled = enabled,
                onValueChange = { changeValue(it, false) },
                onValueChangeFinished = onValueChangeFinished,
                valueRange = valueRange,
                steps = steps,
                modifier = Modifier.weight(1f)
            )
        } else {
            Slider(
                value = value,
                enabled = enabled,
                onValueChange = { changeValue(it, false) },
                onValueChangeFinished = onValueChangeFinished,
                valueRange = valueRange,
                steps = steps,
                modifier = Modifier.weight(1f)
            )
        }
        Surface(
            modifier = Modifier
                .alpha(alpha = if (enabled) 1f else DisabledAlpha)
                .padding(start = 12.dp)
                .align(Alignment.CenterVertically),
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ) {
            Row(
                modifier = Modifier.padding(PaddingValues(horizontal = 8.dp, vertical = 4.dp)),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier
                        .then(
                            if (onTextClick != null) {
                                Modifier.clickable(enabled = enabled, onClick = onTextClick)
                            } else Modifier
                        )
                ) {
                    Text(
                        text = getTextString(value),
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                    suffix?.let { text ->
                        Text(text = text)
                    }
                }
                if (fineTuningControl) {
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        enabled = enabled,
                        modifier = Modifier.size(26.dp),
                        onClick = {
                            val newValue = value.subtractBigDecimal(fineTuningStep)
                            if (newValue <= valueRange.start) {
                                changeValue(valueRange.start, true)
                            } else {
                                changeValue(newValue, true)
                            }
                        }
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_arrow_left_rounded),
                            contentDescription = null
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        enabled = enabled,
                        modifier = Modifier.size(26.dp),
                        onClick = {
                            val newValue = value.addBigDecimal(fineTuningStep)
                            if (newValue >= valueRange.endInclusive) {
                                changeValue(valueRange.endInclusive, true)
                            } else {
                                changeValue(newValue, true)
                            }
                        }
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_arrow_right_rounded),
                            contentDescription = null
                        )
                    }
                }
            }
        }
        appendContent()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IndicatorSlider(
    modifier: Modifier = Modifier,
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    enabled: Boolean = true,
    onValueChangeFinished: (() -> Unit)? = null,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    @IntRange(from = 0) steps: Int = 0,
    colors: SliderColors = SliderDefaults.colors()
) {
    /** Slider顶部需要裁切的像素 */
    val sliderTopCut = with(LocalDensity.current) { 8.dp.toPx().toInt() }
    /** Slider底部需要裁切的像素 */
    val sliderBottomCut = with(LocalDensity.current) { 6.dp.toPx().toInt() }
    Layout(
        modifier = modifier,
        content = {
            Slider(
                value = value,
                onValueChange = onValueChange,
                valueRange = valueRange,
                enabled = enabled,
                onValueChangeFinished = onValueChangeFinished,
                interactionSource = interactionSource,
                steps = steps,
                colors = colors,
                thumb = {
                    SliderDefaults.Thumb(
                        interactionSource = interactionSource,
                        colors = colors,
                        enabled = enabled,
                        thumbSize = DpSize(4.0.dp, 18.5.dp)
                    )
                }
            )
        }
    ) { measurables, constraints ->
        val placeable = measurables.first().measure(constraints)
        val newHeight = (placeable.height - sliderTopCut - sliderBottomCut).coerceAtLeast(0)
        layout(placeable.width, newHeight) {
            placeable.place(0, -sliderTopCut)
        }
    }
}