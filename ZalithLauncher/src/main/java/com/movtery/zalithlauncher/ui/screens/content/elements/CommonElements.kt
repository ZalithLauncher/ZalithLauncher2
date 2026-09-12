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

package com.movtery.zalithlauncher.ui.screens.content.elements

import android.net.Uri
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.context.copyLocalFile
import com.movtery.zalithlauncher.context.getFileName
import com.movtery.zalithlauncher.contract.extensionToMimeType
import com.movtery.zalithlauncher.coroutine.Task
import com.movtery.zalithlauncher.coroutine.TaskStage
import com.movtery.zalithlauncher.coroutine.TitledTask
import com.movtery.zalithlauncher.ui.AndroidStringText
import com.movtery.zalithlauncher.ui.androidText
import com.movtery.zalithlauncher.ui.components.IconTextButton
import com.movtery.zalithlauncher.ui.components.MarqueeText
import com.movtery.zalithlauncher.ui.components.fadeEdge
import com.movtery.zalithlauncher.ui.components.rememberDialogMaxHeight
import com.movtery.zalithlauncher.ui.screens.TitledNavKey
import com.movtery.zalithlauncher.ui.theme.cardColor
import com.movtery.zalithlauncher.ui.theme.onCardColor
import com.movtery.zalithlauncher.utils.file.checkExtensionOrThrow
import com.movtery.zalithlauncher.utils.file.formatFileSize
import com.movtery.zalithlauncher.utils.platform.bytesToMB
import com.movtery.zalithlauncher.utils.platform.getTotalMemory
import com.movtery.zalithlauncher.utils.platform.getUsedMemory
import com.movtery.zalithlauncher.utils.string.getMessageOrToString
import com.movtery.zalithlauncher.viewmodel.ErrorViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import kotlin.time.Duration.Companion.milliseconds

/**
 * [androidx.compose.material3.DisabledAlpha]
 */
const val DisabledAlpha = 0.38f

@Composable
fun CategoryIcon(
    @DrawableRes
    icon: Int,
    @StringRes
    textRes: Int
) {
    Icon(
        painter = painterResource(icon),
        contentDescription = stringResource(textRes),
        modifier = Modifier.size(24.dp)
    )
}

data class CategoryItem(
    val key: TitledNavKey,
    val icon: @Composable () -> Unit,
    val textRes: Int,
    val division: Boolean = false
)

/**
 * 排序方式枚举
 */
enum class SortByEnum(val textRes: Int) {
    /** 按照名称排序 */
    Name(R.string.sort_by_name),
    /** 按照文件名称排序 */
    FileName(R.string.sort_by_file_name),
    /** 按照文件上次修改时间排序 */
    FileModifiedTime(R.string.sort_by_last_modified),
    /** 按照上次游玩时间排序 */
    LastPlayed(R.string.sort_by_last_played)
}

/**
 * 通用的排序方式下来菜单
 * @param enums 当前菜单支持的排序方式
 * @param currentEnum 当前的排序方式
 * @param onEnumChanged 变更当前的排序方式
 * @param isAscending 当前是否为升序
 * @param onToggleSortOrder 切换当前的排序顺序
 */
@Composable
fun SortByDropdownMenu(
    expanded: Boolean,
    onClose: () -> Unit,
    enums: List<SortByEnum>,
    currentEnum: SortByEnum,
    onEnumChanged: (SortByEnum) -> Unit,
    isAscending: Boolean,
    onToggleSortOrder: () -> Unit
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onClose,
        shape = MaterialTheme.shapes.large
    ) {
        enums.forEach { item ->
            DropdownMenuItem(
                text = { Text(stringResource(item.textRes)) },
                onClick = {
                    onEnumChanged(item)
                },
                trailingIcon = if (item == currentEnum) {
                    {
                        IconButton(
                            onClick = onToggleSortOrder
                        ) {
                            val rotation by animateFloatAsState(
                                if (isAscending) 0f else 180f
                            )
                            Icon(
                                modifier = Modifier.rotate(rotation),
                                painter = painterResource(R.drawable.ic_keyboard_double_arrow_up),
                                contentDescription = null
                            )
                        }
                    }
                } else null
            )
        }
    }
}

/**
 * 多 Uri 导入文件任务构建器
 * @param checkExtension 检查被选中的文件后缀是否符合要求
 */
@Composable
fun rememberMultipleUriImportTaskBuilder(
    id: String,
    targetDir: File,
    errorTitle: String = stringResource(R.string.generic_error),
    errorMessage: String? = stringResource(R.string.error_import_file),
    checkExtension: List<String>? = null,
    submitError: (ErrorViewModel.ThrowableMessage) -> Unit = {},
    onFileCopied: suspend (Task, File) -> Unit = { _, _ -> },
    onImported: () -> Unit = {}
): (List<Uri>) -> Task {
    val context = LocalContext.current
    val cErrorTitle by rememberUpdatedState(errorTitle)
    val cErrorMessage by rememberUpdatedState(errorMessage)
    val cSubmitError by rememberUpdatedState(submitError)
    val cOnFileCopied by rememberUpdatedState(onFileCopied)
    val cOnImported by rememberUpdatedState(onImported)

    return remember(id) {
        object : (List<Uri>) -> Task {
            override fun invoke(uris: List<Uri>): Task {
                return Task.runTask(
                    id = id,
                    dispatcher = Dispatchers.IO,
                    task = { task ->
                        task.updateProgress(-1f)
                        task.updateMessage(null)
                        uris.forEach { uri ->
                            try {
                                val fileName = context.getFileName(uri) ?: throw IOException("Failed to get file name")
                                task.updateProgress(-1f)
                                task.updateMessage(androidText(fileName))
                                val outputFile = File(targetDir, fileName)
                                if (checkExtension != null) {
                                    outputFile.checkExtensionOrThrow(checkExtension)
                                }
                                context.copyLocalFile(uri, outputFile)
                                //成功复制，如调用者有额外操作，可使用回调运行
                                cOnFileCopied(task, outputFile)
                            } catch (e: Exception) {
                                val eString = e.getMessageOrToString()
                                val messageString = if (cErrorMessage != null) {
                                    cErrorMessage + "\n" + eString
                                } else {
                                    eString
                                }

                                cSubmitError(
                                    ErrorViewModel.ThrowableMessage(
                                        title = androidText(cErrorTitle),
                                        message = androidText(messageString)
                                    )
                                )
                            }
                        }
                        cOnImported()
                    }
                )
            }
        }
    }
}

@Composable
fun ImportMultipleFileButton(
    extension: String,
    progressUris: (uris: List<Uri>) -> Unit,
    modifier: Modifier = Modifier,
    painter: Painter = painterResource(R.drawable.ic_add),
    text: String = stringResource(R.string.generic_import)
) {
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        uris.takeIf { it.isNotEmpty() }?.let { uris1 ->
            progressUris(uris1)
        }
    }

    IconTextButton(
        modifier = modifier,
        onClick = {
            launcher.launch(extension.extensionToMimeType())
        },
        painter = painter,
        text = text
    )
}

@Composable
fun ImportSingleFileButton(
    extension: String,
    progressUris: (uris: List<Uri>) -> Unit,
    modifier: Modifier = Modifier,
    painter: Painter = painterResource(R.drawable.ic_add),
    text: String = stringResource(R.string.generic_import),
    onClick: () -> Unit = {},
    onLongClick: ((launch: () -> Unit) -> Unit)? = null
) {
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { uri1 ->
            progressUris(listOf(uri1))
        }
    }

    IconTextButton(
        modifier = modifier,
        onClick = {
            onClick()
            launcher.launch(extension.extensionToMimeType())
        },
        painter = painter,
        text = text,
        onLongClick = {
            if (onLongClick != null) {
                onLongClick { launcher.launch(extension.extensionToMimeType()) }
            } else {
                launcher.launch(extension.extensionToMimeType())
            }
        }
    )
}

@Composable
fun <I, O> ImportFileButton(
    contract: ActivityResultContract<I, O>,
    onLaunch: (launcher: ManagedActivityResultLauncher<I, O>) -> Unit,
    progressOutput: (output: O) -> Unit,
    modifier: Modifier = Modifier,
    painter: Painter = painterResource(R.drawable.ic_add),
    text: String = stringResource(R.string.generic_import)
) {
    val launcher = rememberLauncherForActivityResult(
        contract = contract,
        onResult = progressOutput
    )

    IconTextButton(
        modifier = modifier,
        onClick = {
            onLaunch(launcher)
        },
        painter = painter,
        text = text
    )
}

@Composable
fun TitleTaskFlowDialog(
    title: String,
    tasks: List<TitledTask>,
    onCancel: () -> Unit = {}
) {
    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(
            dismissOnClickOutside = false
        )
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

                    val scrollState = rememberLazyListState()
                    LazyColumn(
                        modifier = Modifier
                            .fadeEdge(state = scrollState)
                            .weight(1f, fill = false),
                        state = scrollState
                    ) {
                        items(tasks) { task ->
                            InstallingTaskItem(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                title = task.title,
                                runningIcon = task.runningIcon,
                                task = task.task
                            )
                        }
                    }

                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = onCancel
                    ) {
                        MarqueeText(text = stringResource(R.string.generic_cancel))
                    }
                }
            }
        }
    }
}

@Composable
private fun InstallingTaskItem(
    modifier: Modifier = Modifier,
    title: AndroidStringText,
    @DrawableRes
    runningIcon: Int? = null,
    task: Task
) {
    val taskStage by task.stage.collectAsStateWithLifecycle()
    val taskProgress by task.progress.collectAsStateWithLifecycle()
    val taskMessage by task.message.collectAsStateWithLifecycle()
    val rateBytesPerSec by task.rateBytesPerSec.collectAsStateWithLifecycle()

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val icon = when (taskStage) {
            TaskStage.PREPARING -> R.drawable.ic_schedule_outlined
            TaskStage.RUNNING -> runningIcon ?: R.drawable.ic_download
            TaskStage.COMPLETED -> R.drawable.ic_check
        }
        Icon(
            modifier = Modifier.size(24.dp),
            painter = painterResource(icon),
            contentDescription = null
        )

        Column(modifier = modifier.weight(1f)) {
            AndroidStringText(
                text = title,
                style = MaterialTheme.typography.labelLarge
            )
            if (taskStage == TaskStage.RUNNING) {
                taskMessage?.let { message ->
                    AndroidStringText(
                        modifier = Modifier.padding(top = 4.dp),
                        text = message,
                        style = MaterialTheme.typography.labelMedium,
                    )
                }
                @Composable
                fun RateBytesPerSecText() {
                    rateBytesPerSec?.let { bytes ->
                        val text = remember(bytes) { "${formatFileSize(bytes)}/s" }
                        Text(
                            text = text,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
                if (taskProgress < 0) { //负数则代表不确定
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        LinearProgressIndicator(
                            modifier = Modifier.weight(1f)
                        )
                        RateBytesPerSecText()
                    }
                } else {
                    val progressText = "${(taskProgress * 100).toInt()}%"

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        LinearProgressIndicator(
                            progress = { taskProgress },
                            modifier = Modifier
                                .weight(1f)
                                .align(Alignment.CenterVertically)
                        )
                        RateBytesPerSecText()
                        Text(
                            text = progressText,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            }
        }
    }
}

/**
 * 内存显示（已使用、内存预览、总内存）
 * 可以直观的展示当前设备的运行内存可用情况
 * @param delay 计算内存信息频率间隔时间
 * @param preview 需要预览的内存，将展示在所有可用内存中的占用情况（单位:MB）
 */
@Composable
fun MemoryPreview(
    modifier: Modifier = Modifier,
    delay: Long = 1000,
    preview: Double? = null,
    mainColor: Color = MaterialTheme.colorScheme.primary,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    textStyle: TextStyle = MaterialTheme.typography.labelMedium,
    textColorOnMemory: Color = MaterialTheme.colorScheme.onPrimary,
    textColorOnBackground: Color = MaterialTheme.colorScheme.onSurface,
    usedText: @Composable (usedMemory: Double, totalMemory: Double) -> String,
    previewText: (@Composable (preview: Double) -> String)? = null
) {
    val context = LocalContext.current

    //总内存、已使用内存（单位：MB）
    var totalMemory by remember { mutableDoubleStateOf(0.0) }
    var usedMemory by remember { mutableDoubleStateOf(0.0) }

    LaunchedEffect(Unit) {
        infinityCancellableBlock(delay = delay) {
            //总内存
            totalMemory = getTotalMemory(context).bytesToMB()
            //已使用内存
            usedMemory = getUsedMemory(context).bytesToMB()
        }
    }

    //计算已使用内存比例（基于总内存计算）
    val usedRatio by animateFloatAsState(
        targetValue = if (totalMemory > 0) usedMemory.toFloat() / totalMemory.toFloat() else 0f
    )
    //预览内存比例（基于可用内存计算）
    val previewRatio = remember(preview, totalMemory, usedMemory) {
        if (preview != null && totalMemory > 0) {
            //可用内存，这里不使用getFreeMemory函数
            val availableMemory = totalMemory.toFloat() - usedMemory.toFloat()
            if (availableMemory > 0) preview.toFloat() / availableMemory else 0f
        } else 0f
    }

    //内存进度条直观展示
    Box(
        modifier = modifier
            .height(IntrinsicSize.Min)
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
    ) {
        val usedText = usedText(usedMemory, totalMemory)

        @Composable
        fun UsedMemoryText(
            modifier: Modifier = Modifier,
            textColor: Color = textColorOnMemory,
            marquee: Boolean = true
        ) {
            if (marquee) {
                MarqueeText(
                    modifier = modifier,
                    text = usedText,
                    style = textStyle,
                    color = textColor
                )
            } else {
                Text(
                    modifier = modifier,
                    text = usedText,
                    style = textStyle,
                    color = textColor,
                    softWrap = false,
                    maxLines = 1
                )
            }
        }

        if (preview == null) {
            UsedMemoryText(
                modifier = Modifier.padding(horizontal = 8.dp),
                textColor = textColorOnBackground,
                marquee = false,
            )
        }

        Row(modifier = Modifier.fillMaxWidth()) {
            //已使用内存部分
            if (usedRatio > 0) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(usedRatio)
                        .clip(
                            RoundedCornerShape(
                                topStart = 12.dp,
                                bottomStart = 12.dp,
                                topEnd = if (usedRatio == 1f) 12.dp else 0.dp,
                                bottomEnd = if (usedRatio == 1f) 12.dp else 0.dp
                            )
                        )
                        .background(mainColor),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (preview != null) {
                        UsedMemoryText(
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                    } else {
                        UsedMemoryText(
                            modifier = Modifier
                                .width(IntrinsicSize.Max)
                                .padding(start = 8.dp),
                            textColor = textColorOnMemory,
                            marquee = false,
                        )
                    }
                }
            }

            Row(modifier = Modifier.weight(1f)) {
                //预览内存部分
                if (preview != null && previewRatio > 0f) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(previewRatio)
                            .clip(
                                RoundedCornerShape(
                                    topStart = if (usedRatio == 0f) 12.dp else 0.dp,
                                    bottomStart = if (usedRatio == 0f) 12.dp else 0.dp,
                                    topEnd = if (previewRatio == 1f) 12.dp else 0.dp,
                                    bottomEnd = if (previewRatio == 1f) 12.dp else 0.dp
                                )
                            )
                            .background(mainColor.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        previewText?.invoke(preview)?.let { text ->
                            MarqueeText(
                                modifier = Modifier.padding(horizontal = 8.dp),
                                text = text,
                                style = textStyle,
                                color = textColorOnMemory
                            )
                        }
                    }
                }
            }
        }
    }
}

private suspend fun infinityCancellableBlock(
    delay: Long = 1000,
    block: suspend () -> Unit
) = withContext(Dispatchers.Default) {
    while (true) {
        try {
            block()
            ensureActive()
            delay(delay.milliseconds)
        } catch (_: CancellationException) {
            break
        }
    }
}
