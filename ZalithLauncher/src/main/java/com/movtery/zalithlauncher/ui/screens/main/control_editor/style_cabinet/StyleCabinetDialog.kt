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

package com.movtery.zalithlauncher.ui.screens.main.control_editor.style_cabinet

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.movtery.layer_controller.layout.RendererStyleBox
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.game.control.CabinetStyleItem
import com.movtery.zalithlauncher.setting.enums.isLauncherInDarkTheme
import com.movtery.zalithlauncher.ui.components.CheckChip
import com.movtery.zalithlauncher.ui.components.MarqueeText
import com.movtery.zalithlauncher.ui.components.SimpleAlertDialog
import com.movtery.zalithlauncher.ui.components.WarningCard
import com.movtery.zalithlauncher.ui.components.rememberDialogMaxHeight
import com.movtery.zalithlauncher.ui.screens.main.control_editor.InfoLayoutItem
import com.movtery.zalithlauncher.ui.screens.main.control_editor.edit_joystick.JoystickStylePreview
import com.movtery.zalithlauncher.ui.screens.main.control_editor.edit_joystick.resolveThemeConfig
import com.movtery.zalithlauncher.ui.screens.main.control_editor.orUnspecified
import com.movtery.zalithlauncher.ui.screens.rememberSwapTween
import com.movtery.zalithlauncher.ui.theme.cardColor
import com.movtery.zalithlauncher.ui.theme.onCardColor

/** 收藏柜的样式类型过滤 */
private enum class CabinetFilter {
    ALL, CONTROLS, JOYSTICK
}

/**
 * 样式收藏柜对话框
 * **不使用真正的Dialog**
 */
@Composable
fun StyleCabinetDialog(
    visible: Boolean,
    items: List<CabinetStyleItem>,
    onClose: () -> Unit,
    onEditItem: (CabinetStyleItem) -> Unit,
    onAddToLayout: (CabinetStyleItem) -> Unit,
    onDeleteItem: (CabinetStyleItem) -> Unit
) {
    val tween = rememberSwapTween()

    AnimatedVisibility(
        modifier = Modifier.fillMaxSize(),
        visible = visible,
        enter = fadeIn(animationSpec = tween),
        exit = fadeOut(animationSpec = tween)
    ) {
        var filter by remember { mutableStateOf(CabinetFilter.ALL) }
        var pendingDelete by remember { mutableStateOf<CabinetStyleItem?>(null) }

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            //作为背景层，被点击时关闭对话框
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(0f)
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() },
                        onClick = onClose
                    )
            )

            BoxWithConstraints(
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
                        modifier = Modifier
                            .fillMaxWidth()
                            .animateContentSize()
                    ) {
                        Column(
                            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            MarqueeText(
                                text = stringResource(R.string.control_editor_style_cabinet),
                                style = MaterialTheme.typography.titleMedium
                            )

                            //类型过滤
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                CheckChip(
                                    selected = filter == CabinetFilter.ALL,
                                    onClick = { filter = CabinetFilter.ALL },
                                    label = { Text(text = stringResource(R.string.generic_all)) }
                                )
                                CheckChip(
                                    selected = filter == CabinetFilter.CONTROLS,
                                    onClick = { filter = CabinetFilter.CONTROLS },
                                    label = { Text(text = stringResource(R.string.control_editor_edit_category_style)) }
                                )
                                CheckChip(
                                    selected = filter == CabinetFilter.JOYSTICK,
                                    onClick = { filter = CabinetFilter.JOYSTICK },
                                    label = { Text(text = stringResource(R.string.control_editor_edit_category_joystick_style)) }
                                )
                            }
                        }

                        val filtered = remember(items, filter) {
                            when (filter) {
                                CabinetFilter.ALL -> items
                                CabinetFilter.CONTROLS -> items.filterIsInstance<CabinetStyleItem.Button>()
                                CabinetFilter.JOYSTICK -> items.filterIsInstance<CabinetStyleItem.Joystick>()
                            }
                        }

                        if (filtered.isNotEmpty()) {
                            LazyVerticalGrid(
                                modifier = Modifier
                                    .weight(1f, fill = false)
                                    .fillMaxWidth(),
                                columns = GridCells.Adaptive(minSize = 120.dp),
                                contentPadding = PaddingValues(all = 6.dp),
                            ) {
                                items(filtered, key = { it.file.absolutePath }) { item ->
                                    CabinetStyleGridItem(
                                        modifier = Modifier.padding(all = 6.dp),
                                        item = item,
                                        onEdit = { onEditItem(item) },
                                        onAddToLayout = { onAddToLayout(item) },
                                        onDelete = { pendingDelete = item }
                                    )
                                }
                            }
                        } else {
                            WarningCard(
                                modifier = Modifier
                                    .padding(top = 8.dp, bottom = 16.dp)
                                    .padding(horizontal = 16.dp),
                                title = stringResource(R.string.generic_tip),
                                icon = { innerModifier ->
                                    Icon(
                                        modifier = innerModifier,
                                        painter = painterResource(R.drawable.ic_lightbulb),
                                        contentDescription = null
                                    )
                                },
                                text = {
                                    Text(
                                        text = stringResource(R.string.control_editor_style_cabinet_empty),
                                        style = MaterialTheme.typography.bodySmall,
                                    )
                                }
                            )
                        }
                    }
                }
            }

            //删除确认
            pendingDelete?.let { item ->
                SimpleAlertDialog(
                    title = stringResource(R.string.generic_delete),
                    text = stringResource(
                        R.string.control_editor_style_cabinet_delete,
                        item.name.orUnspecified()
                    ),
                    onConfirm = {
                        onDeleteItem(item)
                        pendingDelete = null
                    },
                    onDismiss = {
                        pendingDelete = null
                    }
                )
            }
        }
    }
}

@Composable
private fun CabinetStyleGridItem(
    item: CabinetStyleItem,
    modifier: Modifier = Modifier,
    onEdit: () -> Unit,
    onAddToLayout: () -> Unit,
    onDelete: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }
    val isDark = isLauncherInDarkTheme()

    InfoLayoutItem(
        modifier = modifier,
        onClick = { menuExpanded = true }
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (item) {
                is CabinetStyleItem.Button -> {
                    RendererStyleBox(
                        modifier = Modifier.size(50.dp),
                        style = item.style,
                        text = "abc",
                        isPressed = false,
                        isDark = isDark
                    )
                }
                is CabinetStyleItem.Joystick -> {
                    JoystickStylePreview(
                        modifier = Modifier.size(50.dp),
                        config = resolveThemeConfig(item.style, isDark)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            MarqueeText(
                modifier = Modifier.fillMaxWidth(),
                text = item.name.orUnspecified(),
                textAlign = TextAlign.Center
            )
            //类型小标签
            Text(
                text = stringResource(
                    when (item) {
                        is CabinetStyleItem.Button -> R.string.control_editor_edit_category_style
                        is CabinetStyleItem.Joystick -> R.string.control_editor_edit_category_joystick_style
                    }
                ),
                style = MaterialTheme.typography.labelSmall
            )
        }

        DropdownMenu(
            expanded = menuExpanded,
            shape = MaterialTheme.shapes.large,
            shadowElevation = 3.dp,
            onDismissRequest = { menuExpanded = false }
        ) {
            DropdownMenuItem(
                text = {
                    Text(text = stringResource(R.string.generic_edit))
                },
                leadingIcon = {
                    Icon(
                        modifier = Modifier.size(20.dp),
                        painter = painterResource(R.drawable.ic_edit_filled),
                        contentDescription = null
                    )
                },
                onClick = {
                    onEdit()
                    menuExpanded = false
                }
            )
            DropdownMenuItem(
                text = {
                    Text(text = stringResource(R.string.generic_delete))
                },
                leadingIcon = {
                    Icon(
                        modifier = Modifier.size(20.dp),
                        painter = painterResource(R.drawable.ic_delete_filled),
                        contentDescription = null
                    )
                },
                onClick = {
                    onDelete()
                    menuExpanded = false
                }
            )
            DropdownMenuItem(
                text = {
                    Text(text = stringResource(R.string.control_editor_style_cabinet_add_to_layout))
                },
                leadingIcon = {
                    Icon(
                        modifier = Modifier.size(20.dp),
                        painter = painterResource(R.drawable.ic_add_box_filled),
                        contentDescription = null
                    )
                },
                onClick = {
                    onAddToLayout()
                    menuExpanded = false
                }
            )
        }
    }
}
