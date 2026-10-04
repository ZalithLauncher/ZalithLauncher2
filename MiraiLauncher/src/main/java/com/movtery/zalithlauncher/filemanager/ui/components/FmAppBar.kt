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

import android.content.res.Configuration
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.filemanager.config.FmConfig
import com.movtery.zalithlauncher.filemanager.ui.theme.fmSecondaryTextColor
import com.movtery.zalithlauncher.filemanager.ui.theme.fmTopbarColors
import com.movtery.zalithlauncher.filemanager.viewmodel.SortConfig

/** 文件管理器顶部应用栏 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FmAppBar(
    currentPath: String,
    folderCount: Int,
    fileCount: Int,
    selectedCount: Int,
    multiSelect: Boolean,
    clipboardPresent: Boolean,
    showHidden: Boolean,
    onJump: () -> Unit,
    onPaste: () -> Unit,
    onRefresh: () -> Unit,
    onImportFile: () -> Unit,
    onImportDir: () -> Unit,
    onSearch: () -> Unit,
    searchEnabled: Boolean = true,
    onSortChange: (SortConfig) -> Unit,
    sortConfig: SortConfig,
    onToggleHidden: () -> Unit,
    onOpenTrash: () -> Unit,
    onExit: () -> Unit,
    onToggleOrientation: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }
    var sortExpanded by remember { mutableStateOf(false) }

    TopAppBar(
        colors = fmTopbarColors(),
        title = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onJump() }
            ) {
                Text(
                    text = currentPath,
                    maxLines = 1,
                    overflow = TextOverflow.StartEllipsis,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                val countsTexts = listOfNotNull(
                    stringResource(R.string.fm_count_folders, folderCount),
                    stringResource(R.string.fm_count_files, fileCount),
                    if (multiSelect && selectedCount > 0) {
                        stringResource(R.string.fm_count_selected, selectedCount)
                    } else null
                )
                AppBarSubTexts(
                    texts = countsTexts
                )
            }
        },
        actions = {
            if (clipboardPresent) {
                IconButton(onClick = onPaste) {
                    Icon(
                        painter = painterResource(R.drawable.ic_assignment_filled),
                        contentDescription = stringResource(R.string.generic_paste)
                    )
                }
            }
            val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
            IconButton(onClick = onToggleOrientation) {
                if (isLandscape) {
                    Icon(
                        painter = painterResource(R.drawable.ic_fullscreen_exit),
                        contentDescription = null
                    )
                } else {
                    Icon(
                        painter = painterResource(R.drawable.ic_mobile_rotate_filled),
                        contentDescription = null
                    )
                }
            }
            IconButton(onClick = { menuExpanded = true }) {
                Icon(
                    painter = painterResource(R.drawable.ic_more_vert),
                    contentDescription = stringResource(R.string.generic_more)
                )
            }

            // 下拉菜单
            DropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = {
                    menuExpanded = false
                    sortExpanded = false
                }
            ) {
                DropdownMenuItem(
                    text = {
                        Text(stringResource(R.string.generic_refresh))
                    },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(R.drawable.ic_refresh),
                            contentDescription = null
                        )
                    },
                    onClick = {
                        menuExpanded = false
                        onRefresh()
                    }
                )

                DropdownMenuItem(
                    text = {
                        Text(stringResource(R.string.fm_import_file))
                    },
                    enabled = !multiSelect,
                    leadingIcon = {
                        Icon(
                            painter = painterResource(R.drawable.ic_text_snippet_filled),
                            contentDescription = null
                        )
                    },
                    onClick = {
                        menuExpanded = false
                        onImportFile()
                    }
                )

                DropdownMenuItem(
                    text = {
                        Text(stringResource(R.string.fm_import_dir))
                    },
                    enabled = !multiSelect,
                    leadingIcon = {
                        Icon(
                            painter = painterResource(R.drawable.ic_create_new_folder_filled),
                            contentDescription = null
                        )
                    },
                    onClick = {
                        menuExpanded = false
                        onImportDir()
                    }
                )

                DropdownMenuItem(
                    text = {
                        Text(stringResource(R.string.generic_search))
                    },
                    enabled = searchEnabled,
                    leadingIcon = {
                        Icon(
                            painter = painterResource(R.drawable.ic_search),
                            contentDescription = null
                        )
                    },
                    onClick = {
                        menuExpanded = false
                        onSearch()
                    }
                )

                SortSubmenu(
                    sortConfig = sortConfig,
                    expanded = sortExpanded,
                    onExpandedChange = { sortExpanded = it },
                    onSortChange = onSortChange
                )

                DropdownMenuItem(
                    text = {
                        Text(stringResource(R.string.fm_show_hidden))
                    },
                    trailingIcon = {
                        Row {
                            Spacer(Modifier.width(12.dp))
                            Checkbox(
                                checked = showHidden,
                                onCheckedChange = { onToggleHidden() }
                            )
                        }
                    },
                    onClick = onToggleHidden
                )

                DropdownMenuItem(
                    text = {
                        Text(stringResource(R.string.fm_trash))
                    },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(R.drawable.ic_delete_filled),
                            contentDescription = null
                        )
                    },
                    onClick = {
                        menuExpanded = false
                        onOpenTrash()
                    }
                )

                DropdownMenuItem(
                    text = {
                        Text(stringResource(R.string.fm_exit))
                    },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(R.drawable.ic_exit_to_app),
                            contentDescription = null
                        )
                    },
                    onClick = {
                        menuExpanded = false
                        onExit()
                    }
                )
            }
        }
    )
}

@Composable
fun AppBarSubTexts(
    texts: List<String>,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.basicMarquee(Int.MAX_VALUE),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        texts.forEach { text ->
            Text(
                text = text,
                style = MaterialTheme.typography.bodySmall,
                color = fmSecondaryTextColor(),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}


@Composable
private fun SortSubmenu(
    sortConfig: SortConfig,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onSortChange: (SortConfig) -> Unit
) {
    DropdownMenuItem(
        text = {
            Text(stringResource(R.string.sort_by))
        },
        trailingIcon = {
            val rotation by animateFloatAsState(
                if (expanded) 180f else 0f
            )
            Icon(
                modifier = Modifier.rotate(rotation),
                painter = painterResource(R.drawable.ic_arrow_drop_down_rounded),
                contentDescription = null
            )
        },
        onClick = { onExpandedChange(true) }
    )

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = { onExpandedChange(false) }
    ) {
        SortMenuItem(
            labelRes = R.string.fm_sort_name,
            candidate = sortConfig.copy(field = FmConfig.SortField.NAME),
            current = sortConfig,
            onSortChange = onSortChange
        )
        SortMenuItem(
            labelRes = R.string.fm_sort_size,
            candidate = sortConfig.copy(field = FmConfig.SortField.SIZE),
            current = sortConfig,
            onSortChange = onSortChange
        )
        SortMenuItem(
            labelRes = R.string.fm_sort_modified,
            candidate = sortConfig.copy(field = FmConfig.SortField.MODIFIED),
            current = sortConfig,
            onSortChange = onSortChange
        )
        DropdownMenuItem(
            text = {
                Text(stringResource(R.string.fm_sort_ascending))
            },
            trailingIcon = {
                Row {
                    Spacer(Modifier.width(12.dp))
                    Checkbox(
                        checked = sortConfig.ascending,
                        onCheckedChange = { onSortChange(sortConfig.copy(ascending = it)) }
                    )
                }
            },
            onClick = { onSortChange(sortConfig.copy(ascending = !sortConfig.ascending)) }
        )
        DropdownMenuItem(
            text = {
                Text(stringResource(R.string.fm_sort_folder_first))
            },
            trailingIcon = {
                Row {
                    Spacer(Modifier.width(12.dp))
                    Checkbox(
                        checked = sortConfig.folderFirst,
                        onCheckedChange = { onSortChange(sortConfig.copy(folderFirst = it)) }
                    )
                }
            },
            onClick = { onSortChange(sortConfig.copy(folderFirst = !sortConfig.folderFirst)) }
        )
    }
}

@Composable
private fun SortMenuItem(
    labelRes: Int,
    candidate: SortConfig,
    current: SortConfig,
    onSortChange: (SortConfig) -> Unit
) {
    DropdownMenuItem(
        text = {
            Text(stringResource(labelRes))
        },
        trailingIcon = {
            if (candidate.field == current.field) {
                Icon(
                    painter = painterResource(R.drawable.ic_check),
                    contentDescription = null
                )
            }
        },
        onClick = {
            onSortChange(candidate)
        }
    )
}
