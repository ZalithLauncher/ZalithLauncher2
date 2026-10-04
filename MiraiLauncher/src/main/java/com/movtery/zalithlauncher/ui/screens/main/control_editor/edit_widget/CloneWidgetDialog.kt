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

package com.movtery.zalithlauncher.ui.screens.main.control_editor.edit_widget

import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.nonInteractiveScrollbar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.movtery.layer_controller.observable.ObservableControlLayer
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.ui.components.MarqueeText
import com.movtery.zalithlauncher.ui.components.rememberDialogMaxHeight
import com.movtery.zalithlauncher.ui.theme.cardColor
import com.movtery.zalithlauncher.ui.theme.itemColor
import com.movtery.zalithlauncher.ui.theme.onCardColor
import com.movtery.zalithlauncher.ui.theme.onItemColor

@Composable
fun SelectLayers(
    layers: List<ObservableControlLayer>,
    initLayer: ObservableControlLayer,
    onDismissRequest: () -> Unit,
    title: String,
    onConfirm: (selected: List<ObservableControlLayer>) -> Unit,
    confirmText: String = stringResource(R.string.generic_confirm)
) {
    //当前选择的控制层
    val selectedLayers = remember { mutableStateListOf(initLayer) }

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
                    .padding(all = 6.dp)
                    .heightIn(max = (maxHeight - 12.dp).coerceAtMost(rememberDialogMaxHeight()))
                    .wrapContentHeight(),
                shape = MaterialTheme.shapes.extraLarge,
                color = cardColor(false),
                contentColor = onCardColor(),
                shadowElevation = 6.dp
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium
                    )

                    ChoseLayersLayout(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = false),
                        layers = layers,
                        selectedLayers = selectedLayers,
                        onLayerSelected = { selectedLayers.add(it) },
                        onLayerUnSelected = { selectedLayers.remove(it) }
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        FilledTonalButton(
                            modifier = Modifier.weight(0.5f),
                            onClick = onDismissRequest
                        ) {
                            MarqueeText(text = stringResource(R.string.generic_cancel))
                        }
                        Button(
                            modifier = Modifier.weight(0.5f),
                            onClick = {
                                if (selectedLayers.isNotEmpty()) {
                                    onConfirm(selectedLayers)
                                }
                            }
                        ) {
                            MarqueeText(text = confirmText)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChoseLayersLayout(
    modifier: Modifier = Modifier,
    layers: List<ObservableControlLayer>,
    selectedLayers: List<ObservableControlLayer>,
    onLayerSelected: (ObservableControlLayer) -> Unit,
    onLayerUnSelected: (ObservableControlLayer) -> Unit,
    shape: Shape = MaterialTheme.shapes.large,
    color: Color = itemColor(false),
    contentColor: Color = onItemColor(),
) {
    Surface(
        modifier = modifier,
        shape = shape,
        color = color,
        contentColor = contentColor,
    ) {
        if (layers.isNotEmpty()) {
            val listState = rememberLazyListState()

            LaunchedEffect(Unit) {
                val target = selectedLayers.firstOrNull() ?: return@LaunchedEffect
                runCatching {
                    val index = layers.indexOf(target)
                    if (index >= 0) {
                        listState.scrollToItem(index)
                    }
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .nonInteractiveScrollbar(
                        state = listState.scrollIndicatorState!!,
                        orientation = Orientation.Vertical,
                    )
                    .padding(vertical = 4.dp),
                contentPadding = PaddingValues(horizontal = 4.dp),
                state = listState
            ) {
                items(layers) { layer ->
                    SelectLayerListItem(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(all = 4.dp),
                        layer = layer,
                        checked = selectedLayers.contains(layer),
                        onChose = {
                            onLayerSelected(layer)
                        },
                        onCancel = {
                            onLayerUnSelected(layer)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun SelectLayerListItem(
    modifier: Modifier = Modifier,
    layer: ObservableControlLayer,
    checked: Boolean,
    onChose: () -> Unit,
    onCancel: () -> Unit
) {
    Row(
        modifier = modifier
            .clip(shape = MaterialTheme.shapes.large)
            .clickable(
                onClick = {
                    if (checked) {
                        onCancel()
                    } else {
                        onChose()
                    }
                }
            )
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = {
                if (it) {
                    onChose()
                } else {
                    onCancel()
                }
            }
        )
        Text(
            modifier = Modifier.basicMarquee(iterations = Int.MAX_VALUE),
            maxLines = 1,
            text = layer.name,
            style = MaterialTheme.typography.labelLarge
        )
    }
}