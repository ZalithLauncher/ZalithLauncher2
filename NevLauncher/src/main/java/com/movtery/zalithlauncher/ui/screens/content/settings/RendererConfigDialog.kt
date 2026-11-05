/*
 * Zalith Launcher 2
 * Copyright (C) 2025-2026 MovTery <movtery228@qq.com> and contributors
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.nonInteractiveScrollbar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.game.renderer.ConfigurableRenderer
import com.movtery.zalithlauncher.game.renderer.Renderers
import com.movtery.zalithlauncher.game.renderer.config.ConfigEntryState
import com.movtery.zalithlauncher.game.renderer.config.ConfigScalar
import com.movtery.zalithlauncher.game.renderer.config.ConfigText
import com.movtery.zalithlauncher.game.renderer.config.GlobalRendererConfigStore
import com.movtery.zalithlauncher.game.renderer.config.NumberEntry
import com.movtery.zalithlauncher.game.renderer.config.OrderedChoiceEntry
import com.movtery.zalithlauncher.game.renderer.config.RendererConfigSession
import com.movtery.zalithlauncher.game.renderer.config.SingleChoiceEntry
import com.movtery.zalithlauncher.game.renderer.config.SwitchEntry
import com.movtery.zalithlauncher.game.renderer.config.TextEntry
import com.movtery.zalithlauncher.game.renderer.config.VersionRendererConfigStore
import com.movtery.zalithlauncher.ui.components.DefaultSwitch
import com.movtery.zalithlauncher.ui.components.ImePanContainer
import com.movtery.zalithlauncher.ui.components.MarqueeText
import com.movtery.zalithlauncher.ui.components.OwnOutlinedTextField
import com.movtery.zalithlauncher.ui.components.SimpleAlertDialog
import com.movtery.zalithlauncher.ui.components.SimpleListItem
import com.movtery.zalithlauncher.ui.components.SingleLineTextCheck
import com.movtery.zalithlauncher.ui.components.TitleAndSummary
import com.movtery.zalithlauncher.ui.components.fadeEdge
import com.movtery.zalithlauncher.ui.components.rememberDialogMaxHeight
import com.movtery.zalithlauncher.ui.screens.content.settings.layouts.CardPosition
import com.movtery.zalithlauncher.ui.screens.content.settings.layouts.rememberSettingsCardShape
import com.movtery.zalithlauncher.ui.theme.cardColor
import com.movtery.zalithlauncher.ui.theme.itemColor
import com.movtery.zalithlauncher.ui.theme.onCardColor
import com.movtery.zalithlauncher.ui.theme.onItemColor
import com.movtery.zalithlauncher.utils.animation.getAnimateTween
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * 统一的渲染器配置对话框
 */
@Composable
fun RendererConfigDialog(
    renderer: ConfigurableRenderer,
    versionFolder: File? = null,
    onDismissRequest: () -> Unit
) {
    val rendererId = renderer.getUniqueIdentifier()
    val sessionState = produceState<RendererConfigSession?>(null, renderer, versionFolder) {
        value = withContext(Dispatchers.IO) {
            GlobalRendererConfigStore.migrateLegacy(rendererId, renderer.legacyConfigKeys)
            val versionStore = versionFolder?.let {
                VersionRendererConfigStore(it, Renderers.getLegacyRendererIds())
            }
            RendererConfigSession(
                rendererId = rendererId,
                spec = renderer.configSpec,
                localStore = versionStore ?: GlobalRendererConfigStore
            )
        }
    }
    val session = sessionState.value ?: return
    var showResetDialog by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        ImePanContainer(
            modifier = Modifier
                .fillMaxWidth(0.6f)
                .heightIn(max = rememberDialogMaxHeight())
                .fillMaxHeight(),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier
                    .padding(all = 3.dp)
                    .heightIn(max = (maxHeight - 6.dp).coerceAtMost(rememberDialogMaxHeight()))
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
                    MarqueeText(
                        text = stringResource(R.string.settings_renderer_config_title),
                        style = MaterialTheme.typography.titleMedium
                    )

                    val scrollState = rememberLazyListState()
                    LazyColumn(
                        modifier = Modifier
                            .fadeEdge(state = scrollState)
                            .weight(1f, fill = false)
                            .fillMaxWidth()
                            .nonInteractiveScrollbar(
                                state = scrollState.scrollIndicatorState!!,
                                orientation = Orientation.Vertical,
                            ),
                        state = scrollState,
                        contentPadding = PaddingValues(vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        itemsIndexed(
                            items = session.states,
                            key = { _, state -> state.entry.id },
                            contentType = { _, state -> state.entry::class }
                        ) { index, state ->
                            val position = when {
                                session.states.size == 1 -> CardPosition.Single
                                index == 0 -> CardPosition.Top
                                index == session.states.lastIndex -> CardPosition.Bottom
                                else -> CardPosition.Middle
                            }
                            when (state.entry) {
                                is SwitchEntry -> SwitchConfigItem(state, position)
                                is SingleChoiceEntry -> ChoiceConfigItem(state, position)
                                is OrderedChoiceEntry -> OrderedChoiceConfigItem(state, position)
                                is NumberEntry -> NumberConfigItem(state, position)
                                is TextEntry -> TextConfigItem(state, position)
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        FilledTonalButton(
                            onClick = { showResetDialog = true },
                            enabled = session.hasOverrides
                        ) {
                            MarqueeText(
                                text = stringResource(
                                    if (versionFolder != null) R.string.versions_config_renderer_reset_global
                                    else R.string.generic_reset
                                )
                            )
                        }
                        Button(onClick = onDismissRequest) {
                            MarqueeText(text = stringResource(R.string.generic_close))
                        }
                    }
                }
            }
        }
    }

    if (showResetDialog) {
        SimpleAlertDialog(
            title = stringResource(R.string.generic_reset),
            text = stringResource(
                if (versionFolder != null) R.string.versions_config_renderer_reset_global_message
                else R.string.settings_renderer_config_reset_message,
                renderer.getRendererName()
            ),
            onConfirm = {
                session.resetAll()
                showResetDialog = false
            },
            onDismiss = {
                showResetDialog = false
            }
        )
    }
}

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

@Composable
private fun ConfigText.resolve(): String = when (this) {
    is ConfigText.Resource -> stringResource(resId)
    is ConfigText.Dynamic -> value
}

@Composable
private fun EntryHeader(state: ConfigEntryState, modifier: Modifier = Modifier) {
    TitleAndSummary(
        modifier = modifier,
        title = state.entry.title?.resolve() ?: state.entry.id,
        summary = state.entry.description?.resolve()
    )
}

@Composable
private fun ResetEntryButton(state: ConfigEntryState) {
    if (state.isOverridden) {
        IconButton(onClick = state::reset) {
            Icon(
                painter = painterResource(R.drawable.ic_refresh),
                contentDescription = stringResource(R.string.generic_reset)
            )
        }
    }
}

@Composable
private fun SwitchConfigItem(state: ConfigEntryState, position: CardPosition) {
    val checked = (state.value as? ConfigScalar.BooleanValue)?.value == true
    DialogItemLayout(position) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { state.saveValue(ConfigScalar.BooleanValue(!checked)) }
                .padding(all = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            EntryHeader(state, Modifier.weight(1f))

            ResetEntryButton(state)

            DefaultSwitch(
                checked = checked,
                onCheckedChange = { state.saveValue(ConfigScalar.BooleanValue(it)) }
            )
        }
    }
}

@Composable
private fun ChoiceConfigItem(state: ConfigEntryState, position: CardPosition) {
    val entry = state.entry as SingleChoiceEntry
    val hasCheckbox = entry.defaultEnabled != null
    val enabled = !hasCheckbox || state.enabled == true
    var expanded by remember { mutableStateOf(false) }
    val selected = entry.options.find { it.value.asString() == state.value.asString() }

    DialogItemLayout(position) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = enabled) { expanded = !expanded }
                .padding(all = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (hasCheckbox) {
                Checkbox(
                    checked = enabled,
                    onCheckedChange = {
                        state.saveEnabled(it)
                        if (!it) expanded = false
                    }
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                EntryHeader(state)
                Text(
                    modifier = Modifier.alpha(if (enabled) 0.7f else 0.38f),
                    text = stringResource(
                        R.string.settings_element_selected,
                        selected?.label?.resolve() ?: state.value.asString()
                    ),
                    style = MaterialTheme.typography.labelSmall
                )
            }

            ResetEntryButton(state)
            val rotation by animateFloatAsState(
                targetValue = if (expanded) -180f else 0f,
                animationSpec = getAnimateTween()
            )

            IconButton(
                modifier = Modifier.rotate(rotation),
                enabled = enabled,
                onClick = { expanded = !expanded }
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_arrow_drop_down_rounded),
                    contentDescription = stringResource(
                        if (expanded) R.string.generic_collapse else R.string.generic_expand
                    )
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
                entry.options.forEach { option ->
                    SimpleListItem(
                        modifier = Modifier.fillMaxWidth(),
                        selected = option.value.asString() == state.value.asString(),
                        itemName = option.label?.resolve() ?: option.value.asString(),
                        onClick = {
                            state.saveValue(option.value)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun OrderedChoiceConfigItem(state: ConfigEntryState, position: CardPosition) {
    val entry = state.entry as OrderedChoiceEntry
    val hasCheckbox = entry.defaultEnabled != null
    val enabled = !hasCheckbox || state.enabled == true
    var expanded by remember { mutableStateOf(false) }
    val options = entry.options.associateBy { it.value.asString() }
    val orderedOptions = state.value.asString().split(',').mapNotNull(options::get)

    DialogItemLayout(position) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = enabled) { expanded = !expanded }
                .padding(all = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (hasCheckbox) {
                Checkbox(
                    checked = enabled,
                    onCheckedChange = {
                        state.saveEnabled(it)
                        if (!it) expanded = false
                    }
                )
            }
            EntryHeader(state, Modifier.weight(1f))
            ResetEntryButton(state)
            val rotation by animateFloatAsState(
                targetValue = if (expanded) -180f else 0f,
                animationSpec = getAnimateTween()
            )
            IconButton(
                modifier = Modifier.rotate(rotation),
                enabled = enabled,
                onClick = { expanded = !expanded }
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_arrow_drop_down_rounded),
                    contentDescription = stringResource(
                        if (expanded) R.string.generic_collapse else R.string.generic_expand
                    )
                )
            }
        }

        AnimatedVisibility(
            visible = expanded && enabled,
            enter = expandVertically(animationSpec = getAnimateTween()),
            exit = shrinkVertically(animationSpec = getAnimateTween()) + fadeOut()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 8.dp, end = 8.dp, bottom = 8.dp)
            ) {
                orderedOptions.forEachIndexed { index, option ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            modifier = Modifier.padding(horizontal = 8.dp),
                            text = "${index + 1}.",
                            style = MaterialTheme.typography.labelMedium
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = option.label?.resolve() ?: option.value.asString(),
                                style = MaterialTheme.typography.bodyMedium
                            )
                            option.description?.let {
                                Text(
                                    text = it.resolve(),
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.alpha(0.7f)
                                )
                            }
                        }
                        IconButton(
                            enabled = index > 0,
                            onClick = { moveOrderedChoice(state, orderedOptions, index, -1) }
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_arrow_upward),
                                contentDescription = stringResource(R.string.generic_move_up)
                            )
                        }
                        IconButton(
                            enabled = index < orderedOptions.lastIndex,
                            onClick = { moveOrderedChoice(state, orderedOptions, index, 1) }
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_arrow_downward),
                                contentDescription = stringResource(R.string.generic_move_down)
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun moveOrderedChoice(
    state: ConfigEntryState,
    options: List<com.movtery.zalithlauncher.game.renderer.config.ChoiceOption>,
    index: Int,
    offset: Int
) {
    val targetIndex = index + offset
    if (targetIndex !in options.indices) return
    val keys = options.map { it.value.asString() }.toMutableList()
    val key = keys.removeAt(index)
    keys.add(targetIndex, key)
    state.saveValue(ConfigScalar.StringValue(keys.joinToString(",")))
}

@Composable
private fun TextConfigItem(state: ConfigEntryState, position: CardPosition) {
    DialogItemLayout(position) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(all = 16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                EntryHeader(state, Modifier.weight(1f))
                ResetEntryButton(state)
            }

            val text = state.value.asString()
            SingleLineTextCheck(
                text = text,
                onSingleLined = { state.saveValue(ConfigScalar.StringValue(it)) }
            )

            OwnOutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = text,
                textStyle = MaterialTheme.typography.labelMedium,
                onValueChange = { state.saveValue(ConfigScalar.StringValue(it)) },
                singleLine = true,
                shape = MaterialTheme.shapes.large
            )
        }
    }
}

@Composable
private fun NumberConfigItem(state: ConfigEntryState, position: CardPosition) {
    val entry = state.entry as NumberEntry
    var text by remember(state.value, state.isOverridden) { mutableStateOf(state.value.asString()) }
    val parsed = entry.parse(text)

    DialogItemLayout(position) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(all = 16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                EntryHeader(state, Modifier.weight(1f))
                ResetEntryButton(state)
            }

            OwnOutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = text,
                textStyle = MaterialTheme.typography.labelMedium,
                onValueChange = {
                    text = it
                    entry.parse(it)?.let(state::saveValue)
                },
                singleLine = true,
                isError = parsed == null,
                keyboardOptions = KeyboardOptions(
                    keyboardType = if (entry.integerOnly) KeyboardType.Number else KeyboardType.Decimal,
                    imeAction = ImeAction.Done
                ),
                shape = MaterialTheme.shapes.large
            )
        }
    }
}
