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

package com.movtery.zalithlauncher.ui.screens.game.elements

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.nonInteractiveScrollbar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.context.copyLocalFile
import com.movtery.zalithlauncher.context.getFileName
import com.movtery.zalithlauncher.ui.components.BackgroundCard
import com.movtery.zalithlauncher.ui.components.CardTitleLayout
import com.movtery.zalithlauncher.ui.components.MarqueeText
import com.movtery.zalithlauncher.ui.components.ProgressDialog
import com.movtery.zalithlauncher.ui.components.SimpleAlertDialog
import com.movtery.zalithlauncher.ui.screens.content.elements.BaseFileItem
import com.movtery.zalithlauncher.ui.theme.ZalithLauncherTheme
import com.movtery.zalithlauncher.ui.theme.itemColor
import com.movtery.zalithlauncher.ui.theme.onItemColor
import com.movtery.zalithlauncher.ui.theme.showThemed
import com.movtery.zalithlauncher.utils.animation.getAnimateTween
import com.movtery.zalithlauncher.utils.animation.getAnimateTweenJellyBounce
import com.movtery.zalithlauncher.utils.string.getMessageOrToString
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.apache.commons.io.FileUtils
import java.io.File
import java.io.IOException

sealed interface OpenFolderOperation {
    data object None : OpenFolderOperation
    /** 开始浏览目录 */
    data class OpenFolder(val initialPath: File) : OpenFolderOperation
}

/**
 * 游戏内打开的浏览目录菜单，可在这个菜单内导入文件、删除文件等操作
 * 这是一个较为简单的临时页面，所有数据均不长期保存
 * @param requestClose 发起关闭请求
 * @param lifecycleScope 可用的生命周期协程作用域，用于执行删除、导入任务
 */
@Composable
fun OpenFolderLayer(
    operation: OpenFolderOperation,
    requestClose: () -> Unit,
    lifecycleScope: CoroutineScope,
    modifier: Modifier = Modifier
) {
    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl

    var internalPath by remember { mutableStateOf<File?>(null) }
    var refreshFiles by remember { mutableStateOf(false) }
    val files = remember { mutableStateListOf<File>() }

    LaunchedEffect(operation) {
        internalPath = when (operation) {
            is OpenFolderOperation.None -> null
            is OpenFolderOperation.OpenFolder -> operation.initialPath
        }
    }

    LaunchedEffect(internalPath, refreshFiles) {
        withContext(Dispatchers.IO) {
            files.clear()
            val temp = buildList {
                internalPath?.listFiles()?.forEach { file ->
                    add(file)
                }
                sortWith { o1, o2 ->
                    val thisIsFile = o1.isFile
                    val otherIsFile = o2.isFile
                    when {
                        thisIsFile != otherIsFile -> {
                            if (!thisIsFile) -1 else 1
                        }
                        else -> {
                            val nameCompare = o1.name.compareTo(o2.name)
                            if (nameCompare != 0) {
                                nameCompare
                            } else {
                                //如果文件名相同，用绝对路径作为最终依据
                                o1.absolutePath.compareTo(o2.absolutePath)
                            }
                        }
                    }
                }
            }
            files.addAll(temp)
        }
    }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.CenterEnd
    ) {
        if (operation is OpenFolderOperation.OpenFolder) {
            //这里不给动画，尽快恢复触控
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(
                        indication = null, //禁用水波纹点击效果
                        interactionSource = remember { MutableInteractionSource() },
                        onClick = requestClose
                    )
            )
        }

        Box(
            modifier = Modifier.fillMaxWidth(0.4f)
        ) {
            AnimatedVisibility(
                visible = operation is OpenFolderOperation.OpenFolder,
                enter = fadeIn() + slideInHorizontally(
                    animationSpec = getAnimateTweenJellyBounce()
                ) {
                    if (isRtl) -40 else 40
                },
                exit = fadeOut() + slideOutHorizontally {
                    if (isRtl) -40 else 40
                }
            ) {
                BackgroundCard(
                    modifier = Modifier.padding(all = 12.dp),
                    influencedByBackground = false,
                    shape = MaterialTheme.shapes.extraLarge
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        CardTitleLayout(
                            modifier = Modifier.fillMaxWidth(),
                            blur = 0
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(all = 12.dp)
                            ) {
                                Text(
                                    modifier = Modifier.fillMaxWidth(),
                                    text = stringResource(R.string.files_browse_folder),
                                    style = MaterialTheme.typography.titleMedium
                                )
                                internalPath?.let { file ->
                                    MarqueeText(
                                        modifier = Modifier.fillMaxWidth(),
                                        text = file.absolutePath,
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            }
                        }

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            var deleteFile by remember { mutableStateOf<File?>(null) }
                            var deleteJob by remember { mutableStateOf<Job?>(null) }

                            //文件浏览区域
                            val scrollState = rememberLazyListState()
                            LazyColumn(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth()
                                    .nonInteractiveScrollbar(
                                        state = scrollState.scrollIndicatorState!!,
                                        orientation = Orientation.Vertical,
                                    ),
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                contentPadding = PaddingValues(all = 12.dp),
                                state = scrollState,
                            ) {
                                items(files) { file ->
                                    FileItem(
                                        modifier = Modifier.fillMaxWidth(),
                                        file = file,
                                        onDelete = {
                                            deleteFile = file
                                        }
                                    )
                                }
                            }

                            //删除文件对话框
                            deleteFile?.let { file0 ->
                                SimpleAlertDialog(
                                    title = stringResource(R.string.generic_delete),
                                    text = stringResource(R.string.files_delete_file, file0.name),
                                    onConfirm = {
                                        deleteJob?.cancel()
                                        deleteJob = lifecycleScope.launch(Dispatchers.IO) {
                                            FileUtils.deleteQuietly(file0)
                                            refreshFiles = !refreshFiles
                                            deleteJob = null
                                        }
                                        deleteFile = null
                                    },
                                    onDismiss = {
                                        deleteFile = null
                                    }
                                )
                            }

                            //开始执行删除任务
                            if (deleteJob != null) {
                                ProgressDialog()
                            }

                            internalPath?.let { currentPath ->
                                var importOperation by remember {
                                    mutableStateOf<ImportFileOperation>(ImportFileOperation.None)
                                }
                                val context = LocalContext.current

                                //导入文件到当前目录
                                val launcher = rememberLauncherForActivityResult(
                                    contract = ActivityResultContracts.GetMultipleContents()
                                ) { uris ->
                                    uris.takeIf { it.isNotEmpty() }?.let { uris0 ->
                                        importOperation = ImportFileOperation.Import(uris0, currentPath)
                                    }
                                }

                                ImportFileOperation(
                                    context = context,
                                    operation = importOperation,
                                    onImported = { refreshFiles = !refreshFiles },
                                    onFinished = { importOperation = ImportFileOperation.None }
                                )

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp)
                                        .padding(top = 12.dp, bottom = 10.dp),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End)
                                ) {
                                    //关闭按钮
                                    FilledTonalButton(
                                        onClick = requestClose
                                    ) {
                                        Text(text = stringResource(R.string.generic_close))
                                    }
                                    //导入按钮
                                    Button(
                                        onClick = {
                                            launcher.launch("*/*")
                                        }
                                    ) {
                                        Text(text = stringResource(R.string.generic_import))
                                    }
                                }
                            }
                        }
                    }
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
    onDelete: () -> Unit = {},
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
            modifier = Modifier.padding(all = 12.dp),
            suffix = {
                IconButton(
                    onClick = onDelete
                ) {
                    Icon(
                        modifier = Modifier.size(24.dp),
                        painter = painterResource(R.drawable.ic_delete_outlined),
                        contentDescription = stringResource(R.string.generic_delete)
                    )
                }
            }
        )
    }
}

private sealed interface ImportFileOperation {
    data object None : ImportFileOperation
    /** 正式开始导入文件 */
    data class Import(val uris: List<Uri>, val targetDir: File) : ImportFileOperation
}

/**
 * 简单的导入文件任务
 */
@Composable
private fun ImportFileOperation(
    context: Context,
    operation: ImportFileOperation,
    onImported: () -> Unit = {},
    onFinished: () -> Unit = {}
) {
    when (operation) {
        is ImportFileOperation.Import -> {
            val errorTitle = stringResource(R.string.generic_error)
            val errorMessage = stringResource(R.string.error_import_file)

            val uris = operation.uris
            val targetDir = operation.targetDir

            LaunchedEffect(Unit) {
                launch(Dispatchers.IO) {
                    uris.forEach { uri ->
                        try {
                            val fileName = context.getFileName(uri) ?: throw IOException("Failed to get file name")
                            val outputFile = File(targetDir, fileName)
                            context.copyLocalFile(uri, outputFile)
                            onImported()
                        } catch (e: Exception) {
                            val eString = e.getMessageOrToString()
                            val messageString = errorMessage + "\n" + eString

                            withContext(Dispatchers.Main) {
                                MaterialAlertDialogBuilder(context)
                                    .setTitle(errorTitle)
                                    .setMessage(messageString)
                                    .setPositiveButton(R.string.generic_confirm) { dialog, _ ->
                                        dialog.dismiss()
                                    }.showThemed()
                            }
                        }
                    }
                    onFinished()
                }
            }

            ProgressDialog(
                title = stringResource(R.string.files_importing)
            )
        }
        is ImportFileOperation.None -> {}
    }
}

@Preview(showBackground = true)
@Composable
private fun OpenFolderLayerPreview() {
    ZalithLauncherTheme {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            OpenFolderLayer(
                modifier = Modifier.fillMaxSize(),
                operation = OpenFolderOperation.OpenFolder(File("")),
                requestClose = {},
                lifecycleScope = rememberCoroutineScope()
            )
        }
    }
}
