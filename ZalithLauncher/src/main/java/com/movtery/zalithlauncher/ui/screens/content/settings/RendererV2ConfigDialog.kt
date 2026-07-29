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

package com.movtery.zalithlauncher.ui.screens.content.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.scrollbar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.game.plugin.renderer_v2.data.EnvSettingUnit
import com.movtery.zalithlauncher.ui.components.DefaultSwitch
import com.movtery.zalithlauncher.ui.components.MarqueeText
import com.movtery.zalithlauncher.ui.components.OwnOutlinedTextField
import com.movtery.zalithlauncher.ui.components.SimpleListItem
import com.movtery.zalithlauncher.ui.components.SingleLineTextCheck
import com.movtery.zalithlauncher.ui.components.TitleAndSummary
import com.movtery.zalithlauncher.ui.components.fadeEdge
import com.movtery.zalithlauncher.ui.screens.content.settings.layouts.CardPosition
import com.movtery.zalithlauncher.ui.screens.content.settings.layouts.rememberSettingsCardShape
import com.movtery.zalithlauncher.ui.theme.cardColor
import com.movtery.zalithlauncher.ui.theme.itemColor
import com.movtery.zalithlauncher.ui.theme.onCardColor
import com.movtery.zalithlauncher.ui.theme.onItemColor
import com.movtery.zalithlauncher.utils.animation.getAnimateTween

/**
 * 新一代渲染器插件的环境变量配置对话框
 * @param units 该渲染器所有可配置的环境变量单元
 */
@Composable
fun RendererV2ConfigDialog(
    units: List<EnvSettingUnit>,
    onDismissRequest: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth(0.6f)
                .fillMaxHeight(),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier
                    .padding(all = 3.dp)
                    .heightIn(max = maxHeight - 6.dp)
                    .wrapContentHeight(),
                shadowElevation = 3.dp,
                color = cardColor(false),
                contentColor = onCardColor(),
                shape = MaterialTheme.shapes.extraLarge
            ) {
                Column(
                    modifier = Modifier.padding(all = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    //标题
                    MarqueeText(
                        text = stringResource(R.string.settings_renderer_config_title),
                        style = MaterialTheme.typography.titleMedium
                    )

                    //配置项列表
                    val scrollState = rememberLazyListState()
                    LazyColumn(
                        modifier = Modifier
                            .fadeEdge(state = scrollState)
                            .weight(1f, fill = false)
                            .fillMaxWidth()
                            .scrollbar(
                                state = scrollState.scrollIndicatorState,
                                orientation = Orientation.Vertical,
                            ),
                        state = scrollState,
                        contentPadding = PaddingValues(vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        itemsIndexed(
                            items = units,
                            key = { _, unit ->
                                when (unit) {
                                    is EnvSettingUnit.Selectable -> unit.rawEnv.key
                                    is EnvSettingUnit.Customizable -> unit.rawEnv.key
                                    is EnvSettingUnit.Toggleable -> unit.rawEnv.key
                                }
                            },
                            contentType = { _, unit -> unit::class }
                        ) { index, unit ->
                            val position = when {
                                units.size == 1 -> CardPosition.Single
                                index == 0 -> CardPosition.Top
                                index == units.lastIndex -> CardPosition.Bottom
                                else -> CardPosition.Middle
                            }
                            when (unit) {
                                is EnvSettingUnit.Selectable -> {
                                    SelectableEnvItem(unit = unit, position = position)
                                }
                                is EnvSettingUnit.Customizable -> {
                                    CustomizableEnvItem(unit = unit, position = position)
                                }
                                is EnvSettingUnit.Toggleable -> {
                                    ToggleableEnvItem(unit = unit, position = position)
                                }
                            }
                        }
                    }

                    //底部按钮
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Button(onClick = onDismissRequest) {
                            MarqueeText(text = stringResource(R.string.generic_close))
                        }
                    }
                }
            }
        }
    }
}

/**
 * Dialog 内使用的配置项基础布局
 */
@Composable
private fun DialogItemLayout(
    position: CardPosition,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = rememberSettingsCardShape(position),
        color = itemColor(false),
        contentColor = onItemColor()
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            content = content
        )
    }
}

/**
 * 选项式环境变量配置项
 */
@Composable
private fun SelectableEnvItem(
    unit: EnvSettingUnit.Selectable,
    position: CardPosition,
    modifier: Modifier = Modifier
) {
    val hasCheckbox = unit.rawEnv.check != null
    var expanded by remember { mutableStateOf(false) }

    DialogItemLayout(
        position = position,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = unit.isEnabled) { expanded = !expanded }
                .then(
                    if (hasCheckbox) {
                        Modifier.padding(vertical = 16.dp)
                            .padding(start = 8.dp, end = 16.dp)
                    } else Modifier.padding(all = 16.dp)
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (hasCheckbox) {
                Checkbox(
                    checked = unit.isEnabled,
                    onCheckedChange = { enabled ->
                        unit.saveCheck(enabled)
                        if (!enabled) expanded = false
                    }
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                TitleAndSummary(
                    modifier = Modifier.fillMaxWidth(),
                    title = stringResource(R.string.settings_renderer_env_title, unit.rawEnv.key),
                    summary = unit.summary
                )
                Text(
                    modifier = Modifier.alpha(if (!hasCheckbox || unit.isEnabled) 0.7f else 0.38f),
                    text = stringResource(R.string.settings_element_selected, unit.state),
                    style = MaterialTheme.typography.labelSmall
                )
            }
            val rotation by animateFloatAsState(
                targetValue = if (expanded) -180f else 0f,
                animationSpec = getAnimateTween()
            )
            IconButton(
                modifier = Modifier.rotate(rotation),
                enabled = unit.isEnabled,
                onClick = {
                    if (!hasCheckbox || unit.isEnabled) expanded = !expanded
                }
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_arrow_drop_down_rounded),
                    contentDescription = stringResource(if (expanded) R.string.generic_expand else R.string.generic_collapse)
                )
            }
        }
        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically(animationSpec = getAnimateTween()),
            exit = shrinkVertically(animationSpec = getAnimateTween()) + fadeOut()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 8.dp, end = 8.dp, bottom = 8.dp)
            ) {
                unit.values.forEach { value ->
                    SimpleListItem(
                        modifier = Modifier.fillMaxWidth(),
                        selected = unit.state == value,
                        itemName = value,
                        onClick = {
                            if (unit.state != value) unit.save(value)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

/**
 * 自由填写式环境变量配置项
 */
@Composable
private fun CustomizableEnvItem(
    unit: EnvSettingUnit.Customizable,
    position: CardPosition,
    modifier: Modifier = Modifier
) {
    DialogItemLayout(
        position = position,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(all = 16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            TitleAndSummary(
                title = stringResource(R.string.settings_renderer_env_title, unit.rawEnv.key),
                summary = unit.summary
            )
            SingleLineTextCheck(
                text = unit.state,
                onSingleLined = { unit.save(it) }
            )
            OwnOutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = unit.state,
                textStyle = MaterialTheme.typography.labelMedium,
                onValueChange = { unit.save(it) },
                singleLine = true,
                shape = MaterialTheme.shapes.large
            )
        }
    }
}

/**
 * 开关式环境变量配置项
 */
@Composable
private fun ToggleableEnvItem(
    unit: EnvSettingUnit.Toggleable,
    position: CardPosition,
    modifier: Modifier = Modifier
) {
    DialogItemLayout(
        position = position,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { unit.save(if (unit.isEnabled) "" else unit.envValue) }
                .padding(all = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TitleAndSummary(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 16.dp),
                title = stringResource(R.string.settings_renderer_env_title, unit.rawEnv.key),
                summary = unit.summary
            )
            DefaultSwitch(
                checked = unit.isEnabled,
                onCheckedChange = { checked ->
                    unit.save(if (checked) unit.envValue else "")
                }
            )
        }
    }
}
