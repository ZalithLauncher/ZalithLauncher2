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

package com.movtery.zalithlauncher.ui.screens.main.control_editor.edit_layer

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.nonInteractiveScrollbar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.movtery.layer_controller.event.ClickEvent
import com.movtery.layer_controller.observable.ObservableClickEventsProvider
import com.movtery.layer_controller.observable.ObservableControlLayer
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.ui.components.MarqueeText
import com.movtery.zalithlauncher.ui.components.fadeEdge
import com.movtery.zalithlauncher.ui.components.rememberDialogMaxHeight
import com.movtery.zalithlauncher.ui.screens.main.control_editor.InfoLayoutItem
import com.movtery.zalithlauncher.ui.theme.cardColor
import com.movtery.zalithlauncher.ui.theme.itemColor
import com.movtery.zalithlauncher.ui.theme.onCardColor
import com.movtery.zalithlauncher.ui.theme.onItemColor

/**
 * 编辑点击事件：切换控件层可见性
 * @param type 控制控件层的类型
 */
@Composable
fun EditSwitchLayersVisibilityDialog(
    data: ObservableClickEventsProvider,
    layers: List<ObservableControlLayer>,
    type: ClickEvent.Type,
    onDismissRequest: () -> Unit
) {
    LaunchedEffect(type) {
        if (!type.isAboutLayers()) error("This type {$type} is unrelated to the control layer.")
    }

    /**
     * 缓存哪些控件层被选中
     */
    val layerSelected = remember { mutableStateListOf<ObservableControlLayer>() }

    LaunchedEffect(data.clickEvents) {
        val layerUuids = layers.map { it.uuid }.toSet()
        val unsafeEvents = data.clickEvents.filter { event ->
            event.isAboutLayers() && event.key !in layerUuids //控件层已不存在
        }

        if (unsafeEvents.isNotEmpty()) {
            data.onRemoveAllEvents(unsafeEvents)
            return@LaunchedEffect
        }

        val validLayerEvents = data.clickEvents.filter {
            it.type == type
        }

        val eventLayerMap = validLayerEvents.associateBy { it.key }
        val selectedLayers = layers.filter { layer ->
            layer.uuid in eventLayerMap
        }

        layerSelected.clear()
        layerSelected.addAll(selectedLayers)
    }

    Dialog(
        onDismissRequest = onDismissRequest
    ) {
        BoxWithConstraints(
            modifier = Modifier
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
                    val title = remember(type) {
                        when (type) {
                            ClickEvent.Type.ShowLayer -> R.string.control_editor_edit_show_layers
                            ClickEvent.Type.HideLayer -> R.string.control_editor_edit_hide_layers
                            else -> R.string.control_editor_edit_switch_layers
                        }
                    }
                    MarqueeText(
                        text = stringResource(title),
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
                        contentPadding = PaddingValues(horizontal = 2.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(layers) { layer ->
                            LayerVisibilityItem(
                                modifier = Modifier.fillMaxWidth(),
                                layer = layer,
                                selected = layerSelected.contains(layer),
                                onSelectedChange = { selected ->
                                    val event = ClickEvent(type, layer.uuid)
                                    if (selected) {
                                        data.onAddEvent(event)
                                    } else {
                                        data.onRemoveEvent(event)
                                    }
                                }
                            )
                        }
                    }
                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = onDismissRequest
                    ) {
                        MarqueeText(
                            text = stringResource(R.string.generic_close)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LayerVisibilityItem(
    modifier: Modifier = Modifier,
    layer: ObservableControlLayer,
    selected: Boolean,
    onSelectedChange: (Boolean) -> Unit,
    color: Color = itemColor(false),
    contentColor: Color = onItemColor()
) {
    InfoLayoutItem(
        modifier = modifier,
        onClick = {
            onSelectedChange(!selected)
        },
        color = color,
        contentColor = contentColor
    ) {
        MarqueeText(
            modifier = Modifier.weight(1f),
            text = layer.name,
            style = MaterialTheme.typography.bodyMedium
        )
        Checkbox(
            checked = selected,
            onCheckedChange = onSelectedChange
        )
    }
}
