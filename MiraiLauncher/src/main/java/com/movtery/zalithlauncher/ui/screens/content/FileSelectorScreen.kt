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

package com.movtery.zalithlauncher.ui.screens.content

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.nonInteractiveScrollbar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavBackStack
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.ui.base.BaseScreen
import com.movtery.zalithlauncher.ui.components.BackgroundCard
import com.movtery.zalithlauncher.ui.components.CardTitleLayout
import com.movtery.zalithlauncher.ui.components.MarqueeText
import com.movtery.zalithlauncher.ui.components.ScalingActionButton
import com.movtery.zalithlauncher.ui.components.ScalingLabel
import com.movtery.zalithlauncher.ui.screens.NormalNavKey
import com.movtery.zalithlauncher.ui.screens.TitledNavKey
import com.movtery.zalithlauncher.ui.screens.content.elements.BaseFileItem
import com.movtery.zalithlauncher.ui.screens.content.elements.CreateNewDirDialog
import com.movtery.zalithlauncher.ui.screens.navigateTo
import com.movtery.zalithlauncher.ui.theme.itemColor
import com.movtery.zalithlauncher.ui.theme.onItemColor
import com.movtery.zalithlauncher.utils.animation.getAnimateTween
import com.movtery.zalithlauncher.utils.animation.swapAnimateDpAsState
import com.movtery.zalithlauncher.utils.file.sortWithFileName
import com.movtery.zalithlauncher.viewmodel.ScreenBackStackViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

/**
 * 导航至FileSelectorScreen
 */
fun NavBackStack<TitledNavKey>.navigateToFileSelector(
    startPath: String,
    selectFile: Boolean,
    saveKey: TitledNavKey,
    onSelected: (path: String) -> Unit
) = this.navigateTo(
    screenKey = NormalNavKey.FileSelector(
        startPath = startPath,
        selectFile = selectFile,
        saveKey = saveKey,
        onSelected = onSelected
    ),
    useClassEquality = true
)

private sealed interface SelectorOperation {
    data object None : SelectorOperation
    /** 创建文件夹时 */
    data object CreateDir : SelectorOperation
}

@Composable
private fun SelectorOperation(
    operation: SelectorOperation,
    onChange: (SelectorOperation) -> Unit,
    currentPath: String,
    onCreatePath: (File) -> Unit,
) {
    when (operation) {
        SelectorOperation.None -> {}
        SelectorOperation.CreateDir -> {
            CreateNewDirDialog(
                onDismissRequest = { onChange(SelectorOperation.None) },
                createDir = {
                    onCreatePath(File(currentPath, it))
                }
            )
        }
    }
}

@Composable
fun FileSelectorScreen(
    key: NormalNavKey.FileSelector,
    backScreenViewModel: ScreenBackStackViewModel,
    back: () -> Unit
) {
    //特殊情况：文件选择器仅作为临时使用的页面
    //不需要长期存储数据，所以，此处不应该使用 ViewModel

    var currentPath by remember(key.startPath) {
        mutableStateOf(key.startPath)
    }
    var files by remember { mutableStateOf<List<File>>(emptyList()) }
    var operation by remember { mutableStateOf<SelectorOperation>(SelectorOperation.None) }

    LaunchedEffect(currentPath, key.selectFile) {
        val loadedFiles = withContext(Dispatchers.IO) {
            val path = File(currentPath)
            path.listFiles()?.toList()?.filter {
                if (!key.selectFile) it.isDirectory else true
            }?.sortedWith { o1, o2 ->
                sortWithFileName(o1, o2)
            } ?: emptyList()
        }
        files = loadedFiles
    }

    val scope = rememberCoroutineScope()
    val createDir = { dirName: String ->
        scope.launch(Dispatchers.IO) {
            val newDir = File(currentPath, dirName)
            if (newDir.mkdirs()) {
                withContext(Dispatchers.Main) {
                    currentPath = newDir.absolutePath
                }
            }
        }
    }

    SelectorOperation(
        operation = operation,
        onChange = { operation = it },
        currentPath = currentPath,
        onCreatePath = { newDir ->
            createDir(newDir.name)
        }
    )

    BaseScreen(
        screenKey = key,
        currentKey = backScreenViewModel.mainScreen.currentKey,
        useClassEquality = true
    ) { isVisible ->
        Row(
            modifier = Modifier
                .padding(all = 12.dp)
                .fillMaxSize()
        ) {
            LeftActionMenu(
                isVisible = isVisible,
                backEnabled = currentPath != key.startPath,
                backToParent = {
                    File(currentPath).parentFile?.let {
                        currentPath = it.absolutePath
                    }
                },
                createDir = { operation = SelectorOperation.CreateDir },
                selectDir = {
                    val path = currentPath
                    key.onSelected(path)
                    back()
                },
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(2.5f)
            )

            FilesLayout(
                isVisible = isVisible,
                currentPath = currentPath,
                updatePath = { path ->
                    currentPath = path
                },
                files = files,
                selectFile = key.selectFile,
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(7.5f)
                    .padding(start = 12.dp)
            )
        }
    }
}

@Composable
private fun LeftActionMenu(
    isVisible: Boolean,
    backEnabled: Boolean,
    backToParent: () -> Unit,
    selectDir: () -> Unit,
    createDir: () -> Unit,
    modifier: Modifier = Modifier
) {
    val surfaceXOffset by swapAnimateDpAsState(
        targetValue = (-40).dp,
        swapIn = isVisible,
        isHorizontal = true
    )

    Column(
        modifier = modifier
            .offset { IntOffset(x = surfaceXOffset.roundToPx(), y = 0) },
        verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.Bottom),
    ) {
        ScalingActionButton(
            enabled = backEnabled,
            modifier = Modifier.fillMaxWidth(),
            onClick = backToParent
        ) {
            MarqueeText(text = stringResource(R.string.files_back_to_parent))
        }
        ScalingActionButton(
            modifier = Modifier.fillMaxWidth(),
            onClick = createDir
        ) {
            MarqueeText(text = stringResource(R.string.files_create_dir))
        }
        ScalingActionButton(
            modifier = Modifier.fillMaxWidth(),
            onClick = selectDir
        ) {
            MarqueeText(text = stringResource(R.string.files_select_dir))
        }
    }
}

@Composable
private fun TopPathHeader(
    path: String,
    modifier: Modifier = Modifier,
) {
    CardTitleLayout(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 12.dp, end = 12.dp, top = 16.dp, bottom = 12.dp)
        ) {
            Text(
                text = stringResource(R.string.files_current_path, path),
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}

@Composable
private fun FilesLayout(
    isVisible: Boolean,
    currentPath: String,
    files: List<File>,
    updatePath: (String) -> Unit,
    selectFile: Boolean,
    modifier: Modifier = Modifier
) {
    val surfaceXOffset by swapAnimateDpAsState(
        targetValue = 40.dp,
        swapIn = isVisible,
        isHorizontal = true
    )

    BackgroundCard(
        modifier = modifier.offset { IntOffset(x = surfaceXOffset.roundToPx(), y = 0) },
        shape = MaterialTheme.shapes.extraLarge
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopPathHeader(
                modifier = Modifier.fillMaxWidth(),
                path = currentPath
            )

            if (files.isNotEmpty()) {
                val scrollState = rememberLazyListState()
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .nonInteractiveScrollbar(
                            state = scrollState.scrollIndicatorState!!,
                            orientation = Orientation.Vertical,
                        ),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    state = scrollState,
                ) {
                    items(files, key = { it.absolutePath }) { file ->
                        FileItem(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            file = file,
                            onClick = {
                                if (!selectFile && file.isDirectory) {
                                    updatePath(file.absolutePath)
                                }
                            }
                        )
                    }
                }
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    ScalingLabel(
                        text = stringResource(R.string.files_no_selectable_content)
                    )
                }
            }
        }
    }
}

@Composable
private fun FileItem(
    modifier: Modifier = Modifier,
    file: File,
    onClick: () -> Unit = {},
    color: Color = itemColor(),
    contentColor: Color = onItemColor(),
) {
    val scale = remember { Animatable(initialValue = 0.95f) }
    LaunchedEffect(Unit) {
        scale.animateTo(targetValue = 1f, animationSpec = getAnimateTween())
    }
    Surface(
        modifier = modifier.graphicsLayer(scaleY = scale.value, scaleX = scale.value),
        color = color,
        contentColor = contentColor,
        shape = MaterialTheme.shapes.large,
        onClick = onClick
    ) {
        BaseFileItem(
            file = file,
            modifier = Modifier.padding(all = 12.dp)
        )
    }
}