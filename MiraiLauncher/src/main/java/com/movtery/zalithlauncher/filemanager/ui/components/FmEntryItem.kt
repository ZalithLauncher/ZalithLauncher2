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

package com.movtery.zalithlauncher.filemanager.ui.components

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.filemanager.logic.entry.FmEntry
import com.movtery.zalithlauncher.filemanager.ui.theme.fmCardColor
import com.movtery.zalithlauncher.filemanager.ui.theme.fmOnCardColor
import com.movtery.zalithlauncher.filemanager.ui.theme.fmSecondaryTextColor
import com.movtery.zalithlauncher.filemanager.ui.theme.fmSelectionColor
import com.movtery.zalithlauncher.utils.file.formatFileSize
import com.movtery.zalithlauncher.utils.formatDate

/** 文件管理器条目行。 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FmEntryItem(
    entry: FmEntry,
    multiSelect: Boolean,
    selected: Boolean,
    cutMarked: Boolean,
    highlighted: Boolean = false,
    position: FmCardPosition = FmCardPosition.Single,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    onLongClick: () -> Unit = {},
    onSwipeTrigger: () -> Unit = {},
    onEdit: () -> Unit = {},
    onRename: () -> Unit = {},
    onProperty: () -> Unit = {},
    onExtract: () -> Unit = {},
    onShare: () -> Unit = {},
    onCompress: () -> Unit = {},
    onCopy: () -> Unit = {},
    onCut: () -> Unit = {},
    onDelete: () -> Unit = {}
) {
    var menuExpanded by remember { mutableStateOf(false) }
    val shape = rememberFmCardShape(position)
    val swipeState = rememberFmSwipeTriggerState()
    val selectionColor = fmSelectionColor()
    val unselectionColor = selectionColor.copy(alpha = 0f)
    val bg by animateColorAsState(
        if (selected || highlighted) {
            selectionColor
        } else {
            unselectionColor
        }
    )
    val contentAlpha by animateFloatAsState(
        if (cutMarked) 0.6f else 1f
    )

    Surface(
        modifier = modifier
            .zIndex(if (swipeState.isDragging) 1f else 0f)
            .fillMaxWidth()
            .fmSwipeTrigger(
                state = swipeState,
                // 任意条目均可作为选区边界
                triggerable = true,
                onTriggered = onSwipeTrigger
            ),
        shape = shape,
        color = fmCardColor(),
        shadowElevation = 6.dp * swipeState.dragFraction
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(bg)
                .combinedClickable(
                    onClick = {
                        if (multiSelect) {
                            onClick()
                        } else if (entry.isFile) {
                            // 非多选模式下单击文件弹出条目菜单
                            menuExpanded = true
                        } else {
                            onClick()
                        }
                    },
                    onLongClick = {
                        if (multiSelect) {
                            onLongClick()
                        } else {
                            menuExpanded = true
                        }
                    }
                )
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            //图标
            FmIcons.IconFor(
                modifier = Modifier.alpha(contentAlpha),
                name = entry.name,
                isDirectory = entry.isDirectory,
            )

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 下拉菜单锚点
                if (!multiSelect) {
                    FmEntryMenu(
                        expanded = menuExpanded,
                        onDismiss = { menuExpanded = false },
                        entry = entry,
                        onEdit = onEdit,
                        onRename = onRename,
                        onProperty = onProperty,
                        onExtract = onExtract,
                        onShare = onShare,
                        onCompress = onCompress,
                        onCopy = onCopy,
                        onCut = onCut,
                        onDelete = onDelete
                    )
                }

                Column(
                    modifier = Modifier.alpha(contentAlpha)
                ) {
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = entry.name,
                        style = MaterialTheme.typography.bodyMedium,
                        color = fmOnCardColor(),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    EntrySubtitle(
                        modifier = Modifier.fillMaxWidth(),
                        entry = entry
                    )
                }
            }
        }
    }
}

@Composable
private fun EntrySubtitle(
    entry: FmEntry,
    modifier: Modifier = Modifier
) {
    @Composable
    fun SubText(
        text: String
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = fmSecondaryTextColor(),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }

    Row(
        modifier = modifier.basicMarquee(Int.MAX_VALUE),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        if (!entry.isDirectory) {
            SubText(
                text = formatFileSize(entry.size)
            )
        }
        SubText(
            text = formatDate(entry.modifiedMs)
        )
    }
}

@Composable
private fun FmEntryMenu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    entry: FmEntry,
    onEdit: () -> Unit,
    onRename: () -> Unit,
    onProperty: () -> Unit,
    onExtract: () -> Unit,
    onShare: () -> Unit,
    onCompress: () -> Unit,
    onCopy: () -> Unit,
    onCut: () -> Unit,
    onDelete: () -> Unit
) {
    @Composable
    fun MenuItem(
        @StringRes
        labelRes: Int,
        @DrawableRes
        iconRes: Int,
        action: () -> Unit,
        modifier: Modifier = Modifier,
    ) {
        DropdownMenuItem(
            modifier = modifier,
            text = { Text(stringResource(labelRes)) },
            leadingIcon = {
                Icon(
                    painter = painterResource(iconRes),
                    contentDescription = null
                )
            },
            onClick = {
                onDismiss()
                action()
            }
        )
    }

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss
    ) {
        if (entry.archiveType == null) {
            if (entry.isFile) {
                MenuItem(R.string.generic_edit, R.drawable.ic_edit_outlined, onEdit, Modifier.fillMaxWidth())
            }
            Row(Modifier.fillMaxWidth()) {
                MenuItem(R.string.generic_rename, R.drawable.ic_edit_filled, onRename, Modifier.weight(1f))
                MenuItem(R.string.fm_archive, R.drawable.ic_archive_filled, onCompress, Modifier.weight(1f))
            }
        } else {
            MenuItem(R.string.generic_rename, R.drawable.ic_edit_filled, onRename, Modifier.fillMaxWidth())
            Row(Modifier.fillMaxWidth()) {
                MenuItem(R.string.fm_archive, R.drawable.ic_archive_filled, onCompress, Modifier.weight(1f))
                MenuItem(R.string.fm_extract, R.drawable.ic_unarchive_filled, onExtract, Modifier.weight(1f))
            }
        }
        Row(Modifier.fillMaxWidth()) {
            MenuItem(R.string.generic_copy, R.drawable.ic_content_copy_filled, onCopy, Modifier.weight(1f))
            MenuItem(R.string.fm_cut, R.drawable.ic_drive_file_move_filled, onCut, Modifier.weight(1f))
        }
        if (entry.isFile) {
            MenuItem(R.string.generic_delete, R.drawable.ic_delete_filled, onDelete, Modifier.fillMaxWidth())
            Row(Modifier.fillMaxWidth()) {
                MenuItem(R.string.fm_property, R.drawable.ic_info_filled, onProperty, Modifier.weight(1f))
                MenuItem(R.string.generic_share, R.drawable.ic_share_filled, onShare, Modifier.weight(1f))
            }
        } else {
            Row(Modifier.fillMaxWidth()) {
                MenuItem(R.string.generic_delete, R.drawable.ic_delete_filled, onDelete, Modifier.weight(1f))
                MenuItem(R.string.generic_share, R.drawable.ic_share_filled, onShare, Modifier.weight(1f))
            }
            MenuItem(R.string.fm_property, R.drawable.ic_info_filled, onProperty, Modifier.fillMaxWidth())
        }
    }
}