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

package com.movtery.zalithlauncher.ui.screens.content.settings.layouts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.movtery.zalithlauncher.setting.unit.IntSettingUnit
import com.movtery.zalithlauncher.setting.unit.NullableIntSettingUnit
import com.movtery.zalithlauncher.setting.unit.min
import com.movtery.zalithlauncher.ui.components.SimpleTextSlider
import com.movtery.zalithlauncher.ui.components.SliderValueEditDialog
import com.movtery.zalithlauncher.ui.components.TitleAndSummary
import com.movtery.zalithlauncher.ui.screens.content.elements.DisabledAlpha

@Composable
fun IntSliderSettingsCard(
    value: Int,
    title: String,
    position: CardPosition,
    modifier: Modifier = Modifier,
    outerShape: Dp = 28.dp,
    innerShape: Dp = 4.dp,
    summary: String? = null,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int = 0,
    suffix: String? = null,
    onValueChange: (Int) -> Unit = {},
    onValueChangeFinished: () -> Unit = {},
    enabled: Boolean = true,
    shorter: Boolean = true,
    fineTuningControl: Boolean = false,
    titleStyle: TextStyle = MaterialTheme.typography.titleSmall,
    summaryStyle: TextStyle = MaterialTheme.typography.labelSmall,
    appendContent: @Composable () -> Unit = {},
    previewContent: @Composable ColumnScope.() -> Unit = {}
) {
    SettingsCard(
        modifier = modifier,
        position = position,
        outerShape = outerShape,
        innerShape = innerShape
    ) {
        var showValueEditDialog by remember { mutableStateOf(false) }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(all = 16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Column(
                modifier = Modifier.alpha(alpha = if (enabled) 1f else DisabledAlpha)
            ) {
                TitleAndSummary(
                    title = title,
                    summary = summary,
                    titleStyle = titleStyle,
                    summaryStyle = summaryStyle
                )
            }
            SimpleTextSlider(
                modifier = Modifier.fillMaxWidth(),
                value = value.toFloat(),
                shorter = shorter,
                enabled = enabled,
                onValueChange = { onValueChange(it.toInt()) },
                onValueChangeFinished = { onValueChangeFinished() },
                onTextClick = { showValueEditDialog = true },
                toInt = true,
                valueRange = valueRange,
                steps = steps,
                suffix = suffix,
                fineTuningControl = fineTuningControl,
                fineTuningStep = 1f,
                appendContent = appendContent
            )
            previewContent()
        }

        if (showValueEditDialog) {
            SliderValueEditDialog(
                onDismissRequest = { showValueEditDialog = false },
                title = title,
                valueRange = valueRange,
                value = value.toFloat(),
                onValueChange = { onValueChange(it.toInt()) },
                onValueChangeFinished = onValueChangeFinished,
                intCheck = true
            )
        }
    }
}

@Composable
fun IntSliderSettingsCard(
    unit: IntSettingUnit,
    onValueChange: (Int) -> Unit = {},
    title: String,
    position: CardPosition,
    modifier: Modifier = Modifier,
    outerShape: Dp = 28.dp,
    innerShape: Dp = 4.dp,
    summary: String? = null,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int = 0,
    suffix: String? = null,
    enabled: Boolean = true,
    shorter: Boolean = true,
    fineTuningControl: Boolean = false,
    titleStyle: TextStyle = MaterialTheme.typography.titleSmall,
    summaryStyle: TextStyle = MaterialTheme.typography.labelSmall,
    appendContent: @Composable () -> Unit = {},
    previewContent: @Composable ColumnScope.() -> Unit = {}
) {
    IntSliderSettingsCard(
        value = unit.state,
        title = title,
        position = position,
        modifier = modifier,
        outerShape = outerShape,
        innerShape = innerShape,
        summary = summary,
        valueRange = valueRange,
        steps = steps,
        suffix = suffix,
        onValueChange = {
            unit.updateState(it)
            onValueChange(it)
        },
        onValueChangeFinished = { unit.save(unit.state) },
        enabled = enabled,
        shorter = shorter,
        fineTuningControl = fineTuningControl,
        titleStyle = titleStyle,
        summaryStyle = summaryStyle,
        appendContent = appendContent,
        previewContent = previewContent
    )
}

@Composable
fun IntSliderSettingsCard(
    unit: NullableIntSettingUnit,
    onValueChange: (Int) -> Unit = {},
    title: String,
    position: CardPosition,
    modifier: Modifier = Modifier,
    outerShape: Dp = 28.dp,
    innerShape: Dp = 4.dp,
    summary: String? = null,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int = 0,
    suffix: String? = null,
    enabled: Boolean = true,
    shorter: Boolean = true,
    fineTuningControl: Boolean = false,
    titleStyle: TextStyle = MaterialTheme.typography.titleSmall,
    summaryStyle: TextStyle = MaterialTheme.typography.labelSmall,
    appendContent: @Composable () -> Unit = {},
    previewContent: @Composable ColumnScope.() -> Unit = {}
) {
    IntSliderSettingsCard(
        value = unit.state ?: unit.min,
        title = title,
        position = position,
        modifier = modifier,
        outerShape = outerShape,
        innerShape = innerShape,
        summary = summary,
        valueRange = valueRange,
        steps = steps,
        suffix = suffix,
        onValueChange = {
            unit.updateState(it)
            onValueChange(it)
        },
        onValueChangeFinished = { unit.save(unit.state) },
        enabled = enabled,
        shorter = shorter,
        fineTuningControl = fineTuningControl,
        titleStyle = titleStyle,
        summaryStyle = summaryStyle,
        appendContent = appendContent,
        previewContent = previewContent
    )
}