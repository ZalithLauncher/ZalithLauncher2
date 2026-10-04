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

package com.movtery.zalithlauncher.ui.screens.content.versions.layouts

import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.game.version.installed.SettingState
import com.movtery.zalithlauncher.game.version.installed.Version
import com.movtery.zalithlauncher.ui.components.TitleAndSummary
import com.movtery.zalithlauncher.ui.screens.content.elements.VersionIconImage
import com.movtery.zalithlauncher.ui.screens.content.settings.layouts.CardPosition
import com.movtery.zalithlauncher.ui.screens.content.settings.layouts.IntSliderSettingsCard
import com.movtery.zalithlauncher.ui.screens.content.settings.layouts.SettingsCard
import kotlin.math.min

@Composable
fun StatefulDropdownMenuFollowGlobal(
    title: String,
    position: CardPosition,
    modifier: Modifier = Modifier,
    currentValue: SettingState,
    onValueChange: (SettingState) -> Unit,
    enabled: Boolean = true,
    iconSize: Dp = 20.dp,
    summary: String? = null
) {
    var value by remember { mutableStateOf(currentValue) }

    var expanded by remember { mutableStateOf(false) }

    SettingsCard(
        modifier = modifier,
        position = position,
        onClick = {
            expanded = !expanded
        },
        enabled = enabled
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(all = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TitleAndSummary(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 16.dp),
                title = title,
                summary = summary
            )

            Text(
                modifier = Modifier.align(Alignment.CenterVertically),
                text = stringResource(value.textRes),
                style = MaterialTheme.typography.labelMedium
            )

            Row(
                modifier = Modifier.align(Alignment.CenterVertically),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    modifier = Modifier.size(34.dp),
                    enabled = enabled,
                    onClick = { expanded = !expanded }
                ) {
                    Icon(
                        modifier = Modifier.size(iconSize),
                        painter = painterResource(R.drawable.ic_settings_filled),
                        contentDescription = stringResource(R.string.generic_setting)
                    )
                }

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    shape = MaterialTheme.shapes.large,
                    shadowElevation = 3.dp,
                ) {
                    val allEntries = SettingState.entries
                    repeat(allEntries.size) { index ->
                        val state = allEntries[index]
                        DropdownMenuItem(
                            text = {
                                Text(text = stringResource(state.textRes))
                            },
                            enabled = enabled,
                            onClick = {
                                value = state
                                onValueChange(value)
                                expanded = false
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ToggleableIntSliderSettingsCard(
    currentValue: Int,
    valueRange: ClosedFloatingPointRange<Float>,
    defaultValue: Int,
    position: CardPosition,
    modifier: Modifier = Modifier,
    title: String,
    summary: String? = null,
    suffix: String? = null,
    enabled: Boolean = true,
    onValueChange: (Int) -> Unit = {},
    onValueChangeFinished: () -> Unit = {},
    previewContent: @Composable (ColumnScope.() -> Unit) = {}
) {
    var checked by remember { mutableStateOf(currentValue >= valueRange.start) }
    var value by remember {
        val v1 = currentValue.takeIf { it >= valueRange.start } ?: defaultValue
        mutableIntStateOf(min(v1, valueRange.endInclusive.toInt()))
    }

    if (!enabled) checked = false

    IntSliderSettingsCard(
        modifier = modifier,
        position = position,
        value = value,
        title = title,
        summary = summary,
        valueRange = valueRange,
        onValueChange = {
            value = it
            onValueChange(value)
        },
        onValueChangeFinished = onValueChangeFinished,
        suffix = suffix,
        enabled = checked,
        fineTuningControl = true,
        appendContent = {
            Checkbox(
                modifier = Modifier.padding(start = 12.dp),
                checked = checked,
                enabled = enabled,
                onCheckedChange = {
                    checked = it
                    value = defaultValue
                    onValueChange(if (checked) value else -1)
                    onValueChangeFinished()
                }
            )
        },
        previewContent = previewContent
    )
}

@Composable
fun VersionOverviewItem(
    modifier: Modifier = Modifier,
    version: Version,
    versionName: String = version.getVersionName(),
    versionSummary: String,
    refreshKey: Any? = null
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        VersionIconImage(
            version = version,
            modifier = Modifier.size(34.dp),
            refreshKey = refreshKey
        )
        Column(
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                modifier = Modifier.basicMarquee(iterations = Int.MAX_VALUE),
                maxLines = 1,
                text = versionName,
                style = MaterialTheme.typography.labelLarge
            )
            Text(
                modifier = Modifier
                    .alpha(0.7f)
                    .basicMarquee(iterations = Int.MAX_VALUE),
                maxLines = 1,
                text = versionSummary,
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}