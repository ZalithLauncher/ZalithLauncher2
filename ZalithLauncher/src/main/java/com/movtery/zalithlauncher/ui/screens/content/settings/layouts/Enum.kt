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

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.NonRestartableComposable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.movtery.zalithlauncher.setting.unit.EnumSettingUnit
import com.movtery.zalithlauncher.ui.components.TitleAndSummary
import com.movtery.zalithlauncher.ui.screens.content.elements.DisabledAlpha
import com.movtery.zalithlauncher.utils.animation.getAnimateTween
import kotlin.enums.EnumEntries

@Composable
fun <E: Enum<E>> EnumSettingsCard(
    value: E,
    entries: EnumEntries<E>,
    title: String,
    position: CardPosition,
    modifier: Modifier = Modifier,
    outerShape: Dp = 28.dp,
    innerShape: Dp = 4.dp,
    summary: String? = null,
    getRadioText: @Composable (E) -> String,
    getRadioEnable: (E) -> Boolean,
    maxItemsInEachRow: Int = Int.MAX_VALUE,
    titleStyle: TextStyle = MaterialTheme.typography.titleSmall,
    summaryStyle: TextStyle = MaterialTheme.typography.labelSmall,
    onRadioClick: (E) -> Unit = {},
) {
    SettingsCard(
        modifier = modifier,
        position = position,
        outerShape = outerShape,
        innerShape = innerShape
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(all = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TitleAndSummary(
                title = title,
                summary = summary,
                titleStyle = titleStyle,
                summaryStyle = summaryStyle
            )
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize(animationSpec = getAnimateTween()),
                horizontalArrangement = Arrangement.SpaceEvenly,
                maxItemsInEachRow = maxItemsInEachRow
            ) {
                entries.forEach { enum ->
                    Row {
                        val radioText = getRadioText(enum)
                        RadioButton(
                            enabled = getRadioEnable(enum),
                            selected = value == enum,
                            onClick = {
                                onRadioClick(enum)
                            }
                        )
                        Text(
                            text = radioText,
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier
                                .align(Alignment.CenterVertically)
                                .alpha(alpha = if (getRadioEnable(enum)) 1f else DisabledAlpha)
                        )
                    }
                }
            }
        }
    }
}

@NonRestartableComposable
@Composable
fun <E: Enum<E>> EnumSettingsCard(
    unit: EnumSettingUnit<E>,
    entries: EnumEntries<E>,
    title: String,
    position: CardPosition,
    modifier: Modifier = Modifier,
    outerShape: Dp = 28.dp,
    innerShape: Dp = 4.dp,
    summary: String? = null,
    getRadioText: @Composable (E) -> String,
    getRadioEnable: (E) -> Boolean,
    maxItemsInEachRow: Int = Int.MAX_VALUE,
    titleStyle: TextStyle = MaterialTheme.typography.titleSmall,
    summaryStyle: TextStyle = MaterialTheme.typography.labelSmall,
    onRadioClick: (E) -> Unit = {},
    onValueChange: (E) -> Unit = {},
) = EnumSettingsCard(
    value = unit.state,
    entries = entries,
    title = title,
    position = position,
    modifier = modifier,
    outerShape = outerShape,
    innerShape = innerShape,
    summary = summary,
    getRadioText = getRadioText,
    getRadioEnable = getRadioEnable,
    maxItemsInEachRow = maxItemsInEachRow,
    titleStyle = titleStyle,
    summaryStyle = summaryStyle,
    onRadioClick = { enum ->
        onRadioClick(enum)
        if (unit.state == enum) return@EnumSettingsCard
        unit.save(enum)
        onValueChange(enum)
    },
)
