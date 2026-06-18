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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.movtery.layer_controller.data.VisibilityType
import com.movtery.layer_controller.observable.ObservableControlLayer
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.ui.components.MarqueeText
import com.movtery.zalithlauncher.ui.components.OwnOutlinedTextField
import com.movtery.zalithlauncher.ui.components.SingleLineTextCheck
import com.movtery.zalithlauncher.ui.components.fadeEdge
import com.movtery.zalithlauncher.ui.components.verticalScrollWithBar
import com.movtery.zalithlauncher.ui.screens.main.control_editor.InfoLayoutListItem
import com.movtery.zalithlauncher.ui.screens.main.control_editor.InfoLayoutSwitchItem
import com.movtery.zalithlauncher.ui.screens.main.control_editor.InfoLayoutTextItem
import com.movtery.zalithlauncher.ui.screens.main.control_editor.getVisibilityText
import com.movtery.zalithlauncher.ui.theme.cardColor
import com.movtery.zalithlauncher.ui.theme.onCardColor

@Composable
fun EditControlLayerDialog(
    layer: ObservableControlLayer,
    onDismissRequest: () -> Unit,
    onDelete: () -> Unit,
    onMergeDownward: () -> Unit,
    onCopy: () -> Unit,
    onHideChange: (Boolean) -> Unit,
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(
            dismissOnClickOutside = false,
        )
    ) {
        BoxWithConstraints(
            modifier = Modifier.fillMaxHeight(),
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
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    MarqueeText(
                        text = stringResource(R.string.control_editor_layers_attribute),
                        style = MaterialTheme.typography.titleMedium
                    )

                    val scrollState = rememberScrollState()

                    var scrollToTop by remember { mutableStateOf(true) }
                    LaunchedEffect(scrollToTop) {
                        if (scrollToTop) {
                            runCatching {
                                scrollState.scrollTo(0)
                                scrollToTop = false
                            }
                        }
                    }

                    Column(
                        modifier = Modifier
                            .fadeEdge(state = scrollState)
                            .weight(1f, fill = false)
                            .fillMaxWidth()
                            .verticalScrollWithBar(state = scrollState),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        SingleLineTextCheck(
                            text = layer.name,
                            onSingleLined = { layer.name = it }
                        )

                        //控件层名称
                        OwnOutlinedTextField(
                            modifier = Modifier.fillMaxWidth(),
                            value = layer.name,
                            onValueChange = {
                                layer.name = it
                            },
                            label = {
                                Text(stringResource(R.string.control_editor_layers_attribute_name))
                            },
                            singleLine = true,
                            shape = MaterialTheme.shapes.large
                        )

                        //可见场景
                        InfoLayoutListItem(
                            modifier = Modifier.fillMaxWidth(),
                            title = stringResource(R.string.control_editor_edit_visibility),
                            items = VisibilityType.entries,
                            selectedItem = layer.visibilityType,
                            onItemSelected = { layer.visibilityType = it },
                            getItemText = { it.getVisibilityText() }
                        )

                        //默认隐藏控件层
                        InfoLayoutSwitchItem(
                            modifier = Modifier.fillMaxWidth(),
                            title = stringResource(R.string.control_editor_layers_attribute_hide),
                            value = layer.editorHide,
                            onValueChange = {
                                layer.editorHide = it
                                onHideChange(it)
                            }
                        )

                        //在实体鼠标操作时隐藏
                        InfoLayoutSwitchItem(
                            modifier = Modifier.fillMaxWidth(),
                            title = stringResource(R.string.control_editor_layers_attribute_hide_when_mouse),
                            value = layer.hideWhenMouse,
                            onValueChange = { layer.hideWhenMouse = it }
                        )

                        //在手柄操作时隐藏
                        InfoLayoutSwitchItem(
                            modifier = Modifier.fillMaxWidth(),
                            title = stringResource(R.string.control_editor_layers_attribute_hide_when_gamepad),
                            value = layer.hideWhenGamepad,
                            onValueChange = { layer.hideWhenGamepad = it }
                        )

                        //合并控件至下层
                        InfoLayoutTextItem(
                            modifier = Modifier.fillMaxWidth(),
                            title = stringResource(R.string.control_editor_layers_merge_downward),
                            icon = {
                                Icon(
                                    modifier = Modifier
                                        .rotate(180f)
                                        .size(20.dp),
                                    painter = painterResource(R.drawable.ic_merge),
                                    contentDescription = null
                                )
                            },
                            onClick = {
                                onDismissRequest()
                                onMergeDownward()
                            }
                        )

                        //复制
                        InfoLayoutTextItem(
                            modifier = Modifier.fillMaxWidth(),
                            title = stringResource(R.string.generic_copy),
                            icon = {
                                Icon(
                                    modifier = Modifier.size(20.dp),
                                    painter = painterResource(R.drawable.ic_copy_all_outlined),
                                    contentDescription = null
                                )
                            },
                            onClick = {
                                onCopy()
                                scrollToTop = true
                            }
                        )

                        Spacer(Modifier)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        FilledTonalButton(
                            modifier = Modifier.weight(1f, fill = false),
                            onClick = onDelete
                        ) {
                            MarqueeText(
                                text = stringResource(R.string.generic_delete)
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Button(
                            modifier = Modifier.weight(1f, fill = false),
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
}