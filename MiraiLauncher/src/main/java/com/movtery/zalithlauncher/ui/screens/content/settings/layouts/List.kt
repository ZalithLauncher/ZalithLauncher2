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

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.setting.unit.EnumSettingUnit
import com.movtery.zalithlauncher.setting.unit.StringListSettingUnit
import com.movtery.zalithlauncher.setting.unit.StringSettingUnit
import com.movtery.zalithlauncher.ui.components.IDItem
import com.movtery.zalithlauncher.ui.components.SimpleListItem
import com.movtery.zalithlauncher.ui.components.TitleAndSummary
import com.movtery.zalithlauncher.ui.screens.content.elements.DisabledAlpha
import com.movtery.zalithlauncher.utils.animation.getAnimateTween

@Composable
private fun <E> BaseListSettingsCard(
    items: List<E>,
    expanded: Boolean,
    onExpandChange: (Boolean) -> Unit,
    title: String,
    position: CardPosition,
    itemLayout: @Composable (E) -> Unit,
    modifier: Modifier = Modifier,
    outerShape: Dp = 28.dp,
    innerShape: Dp = 4.dp,
    summary: String? = null,
    middleLayout: (@Composable ColumnScope.() -> Unit)? = null,
    trailingIcon: (@Composable RowScope.() -> Unit)? = null,
    enabled: Boolean = true,
    itemListPadding: PaddingValues = PaddingValues(start = 8.dp, end = 8.dp, bottom = 8.dp),
    titleStyle: TextStyle = MaterialTheme.typography.titleSmall,
    summaryStyle: TextStyle = MaterialTheme.typography.labelSmall
) {
    require(items.isNotEmpty()) { "Items list cannot be empty" }

    LaunchedEffect(enabled) {
        if (!enabled) onExpandChange(false)
    }

    SettingsCard(
        modifier = modifier,
        position = position,
        outerShape = outerShape,
        innerShape = innerShape
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .alpha(alpha = if (enabled) 1f else DisabledAlpha)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = enabled) { onExpandChange(!expanded) }
                        .padding(all = 16.dp)
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) innerColumn@ {
                        TitleAndSummary(
                            title = title,
                            summary = summary,
                            titleStyle = titleStyle,
                            summaryStyle = summaryStyle
                        )
                        middleLayout?.invoke(this@innerColumn)
                    }
                    trailingIcon?.let { trailing ->
                        Row(
                            modifier = Modifier.align(Alignment.CenterVertically),
                            content = trailing
                        )
                    }
                    val rotation by animateFloatAsState(
                        targetValue = if (expanded) -180f else 0f,
                        animationSpec = getAnimateTween()
                    )
                    IconButton(
                        modifier = Modifier
                            .align(Alignment.CenterVertically)
                            .size(34.dp)
                            .rotate(rotation),
                        enabled = enabled,
                        onClick = { onExpandChange(!expanded) }
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_arrow_drop_down_rounded),
                            contentDescription = stringResource(if (expanded) R.string.generic_expand else R.string.generic_collapse)
                        )
                    }
                }

                Column(modifier = Modifier.fillMaxWidth()) {
                    AnimatedVisibility(
                        modifier = Modifier.fillMaxWidth(),
                        visible = expanded,
                        enter = expandVertically(animationSpec = getAnimateTween()),
                        exit = shrinkVertically(animationSpec = getAnimateTween()) + fadeOut(),
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(itemListPadding)
                        ) {
                            items.forEach { item ->
                                itemLayout(item)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun <E> ListSettingsCard(
    items: List<E>,
    currentId: String,
    defaultId: String,
    title: String,
    position: CardPosition,
    modifier: Modifier = Modifier,
    outerShape: Dp = 28.dp,
    innerShape: Dp = 4.dp,
    summary: String? = null,
    getItemText: @Composable (E) -> String,
    getItemId: (E) -> String,
    getItemSummary: (@Composable (E) -> Unit)? = null,
    trailingIcon: (@Composable RowScope.() -> Unit)? = null,
    enabled: Boolean = true,
    autoCollapse: Boolean = true,
    itemListPadding: PaddingValues = PaddingValues(start = 8.dp, end = 8.dp, bottom = 8.dp),
    onValueChange: (E) -> Unit = {},
    titleStyle: TextStyle = MaterialTheme.typography.titleSmall,
    summaryStyle: TextStyle = MaterialTheme.typography.labelSmall
) {
    require(items.isNotEmpty()) { "Items list cannot be empty" }

    var selectedItem by remember(currentId) {
        mutableStateOf(
            items.firstOrNull { getItemId(it) == currentId }
                ?: items.firstOrNull { getItemId(it) == defaultId }
                ?: items.first()
        )
    }
    var expanded by remember { mutableStateOf(false) }

    BaseListSettingsCard(
        items = items,
        expanded = expanded,
        onExpandChange = { expanded = it },
        title = title,
        position = position,
        itemLayout = { item ->
            SimpleListItem(
                modifier = Modifier.fillMaxWidth(),
                selected = getItemId(selectedItem) == getItemId(item),
                itemName = getItemText(item),
                summary = getItemSummary?.let {
                    { it.invoke(item) }
                },
                onClick = {
                    if (expanded && getItemId(selectedItem) != getItemId(item)) {
                        selectedItem = item
                        onValueChange(item)
                        if (autoCollapse) expanded = false
                    }
                }
            )
        },
        modifier = modifier,
        outerShape = outerShape,
        innerShape = innerShape,
        summary = summary,
        middleLayout = {
            Text(
                modifier = Modifier.alpha(0.7f),
                text = stringResource(R.string.settings_element_selected, getItemText(selectedItem)),
                style = MaterialTheme.typography.labelSmall
            )
        },
        trailingIcon = trailingIcon,
        enabled = enabled,
        itemListPadding = itemListPadding,
        titleStyle = titleStyle,
        summaryStyle = summaryStyle
    )
}

@Composable
fun <E> ListSettingsCard(
    unit: StringSettingUnit,
    items: List<E>,
    title: String,
    position: CardPosition,
    modifier: Modifier = Modifier,
    outerShape: Dp = 28.dp,
    innerShape: Dp = 4.dp,
    summary: String? = null,
    getItemText: @Composable (E) -> String,
    getItemId: (E) -> String,
    getItemSummary: (@Composable (E) -> Unit)? = null,
    trailingIcon: (@Composable RowScope.() -> Unit)? = null,
    enabled: Boolean = true,
    autoCollapse: Boolean = true,
    itemListPadding: PaddingValues = PaddingValues(start = 8.dp, end = 8.dp, bottom = 8.dp),
    onValueChange: (E) -> Unit = {},
    titleStyle: TextStyle = MaterialTheme.typography.titleSmall,
    summaryStyle: TextStyle = MaterialTheme.typography.labelSmall
) {
    ListSettingsCard(
        modifier = modifier,
        items = items,
        currentId = unit.state,
        defaultId = unit.defaultValue,
        position = position,
        outerShape = outerShape,
        innerShape = innerShape,
        title = title,
        summary = summary,
        getItemText = getItemText,
        getItemId = getItemId,
        getItemSummary = getItemSummary,
        trailingIcon = trailingIcon,
        enabled = enabled,
        autoCollapse = autoCollapse,
        itemListPadding = itemListPadding,
        onValueChange = { item ->
            unit.save(getItemId(item))
            onValueChange(item)
        },
        titleStyle = titleStyle,
        summaryStyle = summaryStyle
    )
}

@Composable
fun <E: Enum<E>> ListSettingsCard(
    unit: EnumSettingUnit<E>,
    items: List<E>,
    title: String,
    position: CardPosition,
    modifier: Modifier = Modifier,
    outerShape: Dp = 28.dp,
    innerShape: Dp = 4.dp,
    summary: String? = null,
    getItemText: @Composable (E) -> String,
    getItemSummary: (@Composable (E) -> Unit)? = null,
    enabled: Boolean = true,
    autoCollapse: Boolean = true,
    itemListPadding: PaddingValues = PaddingValues(start = 8.dp, end = 8.dp, bottom = 8.dp),
    onValueChange: (E) -> Unit = {},
    titleStyle: TextStyle = MaterialTheme.typography.titleSmall,
    summaryStyle: TextStyle = MaterialTheme.typography.labelSmall
) {
    ListSettingsCard(
        modifier = modifier,
        items = items,
        currentId = unit.state.name,
        defaultId = unit.defaultValue.name,
        position = position,
        outerShape = outerShape,
        innerShape = innerShape,
        title = title,
        summary = summary,
        getItemText = getItemText,
        getItemId = { it.name },
        getItemSummary = getItemSummary,
        enabled = enabled,
        autoCollapse = autoCollapse,
        itemListPadding = itemListPadding,
        onValueChange = { item ->
            unit.save(item)
            onValueChange(item)
        },
        titleStyle = titleStyle,
        summaryStyle = summaryStyle
    )
}

@Composable
fun <E> StringListSettingsCard(
    unit: StringListSettingUnit,
    items: List<E>,
    onItemsChange: List<String>.(check: Boolean, item: E) -> List<String>,
    title: String,
    position: CardPosition,
    modifier: Modifier = Modifier,
    outerShape: Dp = 28.dp,
    innerShape: Dp = 4.dp,
    summary: String? = null,
    getItemID: (E) -> String,
    getItemText: @Composable (E) -> String,
    getItemSummary: (@Composable (E) -> Unit)? = null,
    getItemCheck: (contains: Boolean) -> Boolean = { it },
    trailingIcon: (@Composable RowScope.() -> Unit)? = null,
    enabled: Boolean = true,
    itemListPadding: PaddingValues = PaddingValues(start = 8.dp, end = 8.dp, bottom = 8.dp),
    titleStyle: TextStyle = MaterialTheme.typography.titleSmall,
    summaryStyle: TextStyle = MaterialTheme.typography.labelSmall
) {
    require(items.isNotEmpty()) { "Items list cannot be empty" }

    var expanded by remember { mutableStateOf(false) }

    BaseListSettingsCard(
        items = items,
        expanded = expanded,
        onExpandChange = { expanded = it },
        title = title,
        position = position,
        itemLayout = { item ->
            val itemID = getItemID(item)
            val listState = unit.state
            val contains = itemID in listState

            SimpleListItem(
                modifier = Modifier.fillMaxWidth(),
                checked = getItemCheck(contains),
                onCheckedChange = { value ->
                    unit.save(
                        listState.onItemsChange(value, item)
                    )
                },
                itemName = getItemText(item),
                summary = getItemSummary?.let {
                    { it.invoke(item) }
                }
            )
        },
        trailingIcon = trailingIcon,
        modifier = modifier,
        outerShape = outerShape,
        innerShape = innerShape,
        summary = summary,
        enabled = enabled,
        itemListPadding = itemListPadding,
        titleStyle = titleStyle,
        summaryStyle = summaryStyle
    )
}

@Composable
fun SimpleIDListCard(
    items: List<IDItem>,
    currentId: String,
    defaultId: String,
    title: String,
    position: CardPosition,
    modifier: Modifier = Modifier,
    summary: String? = null,
    enabled: Boolean = true,
    itemListPadding: PaddingValues = PaddingValues(bottom = 4.dp),
    onValueChange: (IDItem) -> Unit = {}
) {
    ListSettingsCard(
        modifier = modifier,
        position = position,
        items = items,
        currentId = currentId,
        defaultId = defaultId,
        title = title,
        summary = summary,
        getItemText = { it.title },
        getItemId = { it.id },
        enabled = enabled,
        itemListPadding = itemListPadding,
        onValueChange = onValueChange
    )
}