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

package com.movtery.zalithlauncher.ui.screens.content.versions.export

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TriStateCheckbox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
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
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.game.version.export.data.FileSelectionData
import com.movtery.zalithlauncher.game.version.export.data.Selected
import com.movtery.zalithlauncher.game.version.installed.Version
import com.movtery.zalithlauncher.ui.base.BaseScreen
import com.movtery.zalithlauncher.ui.screens.NestedNavKey
import com.movtery.zalithlauncher.ui.screens.NormalNavKey
import com.movtery.zalithlauncher.ui.screens.TitledNavKey
import com.movtery.zalithlauncher.utils.animation.swapAnimateDpAsState

/**
 * 选择要导出的文件
 * @param onFinish 完成文件选择
 */
@Composable
fun ExportSelectFilesScreen(
    allFiles: List<FileSelectionData>,
    selectedFiles: Boolean,
    onRefreshRootSelect: () -> Unit,
    isSelectingFolder: Boolean,
    isRefreshingFiles: Boolean,
    mainScreenKey: TitledNavKey?,
    exportScreenKey: TitledNavKey?,
    version: Version,
    backToMainScreen: () -> Unit,
    onFinish: () -> Unit
) {
    if (!version.isValid()) {
        backToMainScreen()
        return
    }

    BaseScreen(
        levels1 = listOf(
            Pair(NestedNavKey.VersionExport::class.java, mainScreenKey)
        ),
        Triple(NormalNavKey.VersionExports.SelectFiles, exportScreenKey, false)
    ) { isVisible ->
        val yOffset by swapAnimateDpAsState(
            targetValue = (-40).dp,
            swapIn = isVisible
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset { IntOffset(x = 0, y = yOffset.roundToPx()) },
            contentAlignment = Alignment.BottomEnd
        ) {
            //文件选择区域
            FileSelectorList(
                modifier = Modifier.fillMaxSize(),
                list = allFiles,
                isRefreshingFiles = isRefreshingFiles,
                onUnselectedAll = { data ->
                    data.updateSelectState(Selected.Unselected)
                    onRefreshRootSelect()
                },
                onSelectedAll = { data ->
                    data.updateSelectState(Selected.Selected)
                    onRefreshRootSelect()
                },
            )

            Button(
                modifier = Modifier.padding(all = 12.dp),
                onClick = onFinish,
                enabled = !isSelectingFolder && selectedFiles
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val text = stringResource(R.string.versions_export_pack_select_output)
                    Icon(
                        painter = painterResource(R.drawable.ic_file_export_outlined),
                        contentDescription = text
                    )

                    Text(text = text)
                }
            }
        }
    }
}

@Composable
private fun FileSelectorList(
    list: List<FileSelectionData>,
    isRefreshingFiles: Boolean,
    onUnselectedAll: (FileSelectionData) -> Unit,
    onSelectedAll: (FileSelectionData) -> Unit,
    modifier: Modifier = Modifier
) {
    var refreshExpand by remember { mutableStateOf(false) }
    //实际文件选择区域
    val visibleNodes = rememberVisibleNodes(list, refreshExpand)

    CompositionLocalProvider(
        LocalContentColor provides MaterialTheme.colorScheme.onSurface
    ) {
        LazyColumn(
            modifier = modifier
                .horizontalScroll(rememberScrollState()),
            contentPadding = PaddingValues(all = 12.dp),
        ) {
            item {
                Row(
                    modifier = Modifier.padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.versions_export_pack_files),
                        style = MaterialTheme.typography.titleMedium
                    )

                    if (isRefreshingFiles) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                    }
                }
            }

            items(
                items = visibleNodes,
                key = { it.key }
            ) { node ->
                FileNodeItem(
                    modifier = Modifier.animateItem(),
                    node = node,
                    onUnselectedAll = onUnselectedAll,
                    onSelectedAll = onSelectedAll,
                    onRefreshExpand = {
                        refreshExpand = !refreshExpand
                    },
                )
            }
        }
    }
}

@Composable
private fun FileNodeItem(
    node: VisibleNode,
    onUnselectedAll: (FileSelectionData) -> Unit,
    onSelectedAll: (FileSelectionData) -> Unit,
    onRefreshExpand: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        val indentation = node.indentation

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(Modifier.width((indentation * 16).dp))

            when (node) {
                is VisibleNode.EmptyNode -> {
                    Text(
                        modifier = Modifier
                            .padding(vertical = 8.dp)
                            .padding(start = 46.dp)
                            .alpha(0.7f),
                        text = stringResource(R.string.versions_export_pack_dir_empty),
                        style = MaterialTheme.typography.labelMedium
                    )
                }
                is VisibleNode.FileNode -> {
                    val data = node.data
                    val child = remember(data) { data.child }

                    if (child != null) {
                        val expand by data.expand.collectAsStateWithLifecycle()
                        IconButton(
                            modifier = Modifier.size(48.dp),
                            onClick = {
                                data.expandDirs(!data.expand.value)
                                onRefreshExpand()
                            }
                        ) {
                            val rotation by animateFloatAsState(
                                if (expand) 90f else 0f
                            )

                            Icon(
                                modifier = Modifier.rotate(rotation),
                                painter = painterResource(R.drawable.ic_arrow_right_rounded),
                                contentDescription = null
                            )
                        }
                    } else {
                        //仅用于视觉上的对齐
                        Spacer(Modifier.size(48.dp))
                    }

                    val selected by data.selected.collectAsStateWithLifecycle()
                    TriStateCheckbox(
                        state = when (selected) {
                            Selected.Selected -> ToggleableState.On
                            Selected.Indeterminate -> ToggleableState.Indeterminate
                            Selected.Unselected -> ToggleableState.Off
                        },
                        onClick = {
                            when (selected) {
                                Selected.Selected -> onUnselectedAll(data)
                                else -> onSelectedAll(data)
                            }
                        }
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        //文件别名
                        data.alias?.let { alias ->
                            Text(
                                modifier = Modifier.alpha(0.7f),
                                text = stringResource(alias),
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                        //文件名
                        Text(
                            text = data.file.name,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            }
        }
    }
}