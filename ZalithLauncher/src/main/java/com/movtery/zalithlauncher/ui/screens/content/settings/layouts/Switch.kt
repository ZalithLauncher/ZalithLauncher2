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

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.movtery.zalithlauncher.setting.unit.BooleanSettingUnit
import com.movtery.zalithlauncher.ui.components.DefaultSwitch
import com.movtery.zalithlauncher.ui.components.TitleAndSummary

@Composable
fun SwitchSettingsCard(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    title: String,
    position: CardPosition,
    modifier: Modifier = Modifier,
    outerShape: Dp = 28.dp,
    innerShape: Dp = 4.dp,
    summary: String? = null,
    enabled: Boolean = true,
    verticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
    titleStyle: TextStyle = MaterialTheme.typography.titleSmall,
    summaryStyle: TextStyle = MaterialTheme.typography.labelSmall,
    trailingIcon: @Composable (RowScope.() -> Unit)? = null,
    columnLayout: @Composable (ColumnScope.() -> Unit)? = null,
) {
    SettingsCard(
        modifier = modifier,
        position = position,
        outerShape = outerShape,
        innerShape = innerShape,
        onClick = { onCheckedChange(!checked) },
        enabled = enabled
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(all = 16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = verticalAlignment
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 16.dp)
                ) {
                    TitleAndSummary(
                        title = title,
                        summary = summary,
                        titleStyle = titleStyle,
                        summaryStyle = summaryStyle
                    )
                }

                Row(
                    verticalAlignment = verticalAlignment
                ) {
                    trailingIcon?.invoke(this)
                }

                DefaultSwitch(
                    checked = checked,
                    enabled = enabled,
                    onCheckedChange = { value -> onCheckedChange(value) }
                )
            }

            columnLayout?.invoke(this@Column)
        }
    }
}

@Composable
fun SwitchSettingsCard(
    unit: BooleanSettingUnit,
    modifier: Modifier = Modifier,
    onCheckedChange: (Boolean) -> Unit = {},
    title: String,
    position: CardPosition,
    outerShape: Dp = 28.dp,
    innerShape: Dp = 4.dp,
    summary: String? = null,
    enabled: Boolean = true,
    verticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
    titleStyle: TextStyle = MaterialTheme.typography.titleSmall,
    summaryStyle: TextStyle = MaterialTheme.typography.labelSmall,
    trailingIcon: @Composable (RowScope.() -> Unit)? = null,
    columnLayout: @Composable (ColumnScope.() -> Unit)? = null,
) {
    SwitchSettingsCard(
        checked = unit.state,
        onCheckedChange = { value ->
            unit.save(value)
            onCheckedChange(value)
        },
        title = title,
        position = position,
        modifier = modifier,
        outerShape = outerShape,
        innerShape = innerShape,
        summary = summary,
        enabled = enabled,
        verticalAlignment = verticalAlignment,
        titleStyle = titleStyle,
        summaryStyle = summaryStyle,
        trailingIcon = trailingIcon,
        columnLayout = columnLayout,
    )
}