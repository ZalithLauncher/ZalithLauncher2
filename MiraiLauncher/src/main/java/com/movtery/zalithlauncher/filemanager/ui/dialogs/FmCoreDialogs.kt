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

package com.movtery.zalithlauncher.filemanager.ui.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.filemanager.logic.entry.FmEntry
import com.movtery.zalithlauncher.filemanager.ui.components.ExposedDropdown
import com.movtery.zalithlauncher.filemanager.ui.components.FmAlertDialog
import com.movtery.zalithlauncher.filemanager.ui.components.FmDialogSurface
import com.movtery.zalithlauncher.filemanager.ui.components.FmEditDialog
import com.movtery.zalithlauncher.filemanager.ui.components.PropertyRow
import com.movtery.zalithlauncher.filemanager.ui.components.fmFilenameInvalid
import com.movtery.zalithlauncher.filemanager.ui.theme.fmErrorColor
import com.movtery.zalithlauncher.filemanager.viewmodel.DirScanUiState
import com.movtery.zalithlauncher.ui.components.OwnOutlinedTextField
import com.movtery.zalithlauncher.utils.file.formatFileSize

private enum class CreateType {
    File,
    Folder
}

@Composable
private fun CreateType.stringResource(): String {
    return when (this) {
        CreateType.File -> stringResource(R.string.fm_new_file)
        CreateType.Folder -> stringResource(R.string.resource_pack_manage_type_folder)
    }
}

/**
 * 新建文件或文件夹对话框。
 * @param onConfirmFile 以输入的名称创建文件
 * @param onConfirmFolder 以输入的名称创建文件夹
 */
@Composable
fun FmCreateDialog(
    onDismiss: () -> Unit,
    onConfirmFile: (String) -> Unit,
    onConfirmFolder: (String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var type by remember { mutableStateOf(CreateType.File) }

    // 实时校验文件名合法性，错误就地标注在输入框下方
    val filenameError = key(name) { fmFilenameInvalid(name) }
    val isError = name.isEmpty() || filenameError != null

    FmDialogSurface(onDismissRequest = onDismiss) {
        Text(
            text = stringResource(R.string.control_editor_layers_create),
            style = MaterialTheme.typography.titleMedium
        )

        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            OwnOutlinedTextField(
                value = name,
                onValueChange = { name = it },
                singleLine = true,
                label = { Text(stringResource(R.string.fm_new_name)) },
                isError = isError,
                supportingText = {
                    when {
                        name.isEmpty() -> Text(stringResource(R.string.generic_cannot_empty))
                        filenameError != null -> Text(filenameError)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large
            )

            ExposedDropdown(
                modifier = Modifier.fillMaxWidth(),
                selectedText = type.stringResource(),
                label = stringResource(R.string.fm_property_type),
                options = CreateType.entries,
                optionLabel = { type ->
                    type.stringResource()
                },
                onSelect = { type0 ->
                    type = type0
                },
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
        ) {
            FilledTonalButton(
                onClick = onDismiss
            ) {
                Text(stringResource(R.string.generic_cancel))
            }

            Button(
                enabled = !isError,
                onClick = {
                    when (type) {
                        CreateType.File -> onConfirmFile(name)
                        CreateType.Folder -> onConfirmFolder(name)
                    }
                }
            ) {
                Text(stringResource(R.string.generic_confirm))
            }
        }
    }
}

/**
 * 跳转目录对话框。
 * @param currentPath 输入框默认填充的当前路径
 * @param onConfirm 以校验通过的目标路径跳转
 */
@Composable
fun FmJumpDialog(
    currentPath: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    // 初始化时默认将光标定位到路径末尾，便于直接追加修改
    var input by remember {
        mutableStateOf(TextFieldValue(currentPath, TextRange(currentPath.length)))
    }
    var error by remember { mutableStateOf<String?>(null) }
    val keyboard = LocalSoftwareKeyboardController.current
    val emptyError = stringResource(R.string.fm_jump_invalid)

    FmEditDialog(
        title = stringResource(R.string.fm_jump_to),
        value = input,
        onValueChange = {
            input = it
            error = null
        },
        label = {
            Text(stringResource(R.string.fm_jump_hint))
        },
        isError = error != null,
        supportingText = error?.let {
            { Text(it) }
        },
        singleLine = true,
        onDismissRequest = onDismiss,
        onCancel = onDismiss,
        onConfirm = {
            if (input.text.isBlank()) {
                error = emptyError
            } else {
                keyboard?.hide()
                onConfirm(input.text.trim())
            }
        }
    )
}

/**
 * 删除确认对话框，可选择是否放入回收站
 * @param count 待删除的条目数量
 * @param onConfirm 确认删除，参数表示是否放入回收站
 */
@Composable
fun FmDeleteConfirmDialog(
    count: Int,
    onDismiss: () -> Unit,
    onConfirm: (toTrash: Boolean) -> Unit
) {
    var toTrash by remember { mutableStateOf(true) }

    FmAlertDialog(
        title = stringResource(R.string.generic_delete),
        text = {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(R.string.fm_delete, count),
                    style = MaterialTheme.typography.bodyMedium
                )

                if (!toTrash) {
                    Text(
                        text = stringResource(R.string.fm_permanent_delete),
                        style = MaterialTheme.typography.bodyMedium,
                        color = fmErrorColor(),
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = toTrash,
                        onCheckedChange = { toTrash = it }
                    )
                    Text(
                        text = stringResource(R.string.fm_move_to_trash),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        },
        confirmText = stringResource(R.string.generic_delete),
        onConfirm = { onConfirm(toTrash) },
        onDismiss = onDismiss
    )
}

/**
 * 重命名对话框，实时校验并就地提示错误。
 * @param entry 待重命名的条目
 * @param initialName 输入框的初始名称
 * @param isFile 是否为文件，决定默认选中范围
 * @param onConfirm 以校验通过的新名称确认重命名
 * @param validate 名称校验函数，返回 null 表示通过，否则返回错误提示文本
 */
@Composable
fun FmRenameDialog(
    entry: FmEntry,
    initialName: String,
    isFile: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
    validate: (FmEntry, String) -> String?
) {
    val initialSelection: TextRange = if (isFile) {
        val dot = initialName.lastIndexOf('.')
        if (dot > 0) {
            TextRange(0, dot)
        } else {
            TextRange(0, initialName.length)
        }
    } else {
        TextRange(0, initialName.length)
    }
    var tfv by remember { mutableStateOf(TextFieldValue(initialName, initialSelection)) }
    val keyboard = LocalSoftwareKeyboardController.current
    val focus = remember { FocusRequester() }

    // 输入变化即校验，有错误时禁用确认
    val err = key(tfv.text) { validate(entry, tfv.text) }

    FmEditDialog(
        title = stringResource(R.string.generic_rename),
        value = tfv,
        onValueChange = { tfv = it },
        isError = err != null,
        supportingText = err?.let { { Text(it) } },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii),
        focusRequester = focus,
        confirmEnabled = err == null,
        onDismissRequest = onDismiss,
        onCancel = onDismiss,
        onConfirm = {
            // 有错误时确认已禁用，此处直接提交
            keyboard?.hide()
            onConfirm(tfv.text)
        }
    )
}

/**
 * 文件属性对话框。
 * @param name 条目名称
 * @param path 条目完整路径
 * @param isDirectory 是否为目录
 * @param sizeText 大小文本
 * @param modifiedText 修改时间文本
 * @param dirScan 目录扫描状态，用于展示目录的异步统计结果
 * @param onDismiss 关闭对话框
 */
@Composable
fun FmPropertyDialog(
    name: String,
    path: String,
    isDirectory: Boolean,
    sizeText: String,
    modifiedText: String,
    dirScan: DirScanUiState? = null,
    onDismiss: () -> Unit
) {
    val typeText = if (isDirectory) {
        stringResource(R.string.resource_pack_manage_type_folder)
    } else {
        stringResource(R.string.fm_new_file)
    }
    val dirStats = dirScan?.stats

    FmAlertDialog(
        title = stringResource(R.string.fm_property),
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                PropertyRow(R.string.fm_new_name, name)
                PropertyRow(R.string.fm_property_path, path)
                PropertyRow(R.string.fm_property_type, typeText)
                PropertyRow(R.string.fm_property_modified, modifiedText)

                if (isDirectory) {
                    val scanning = dirScan?.running == true
                    PropertyRow(
                        R.string.fm_property_size,
                        if (dirStats != null) {
                            formatFileSize(dirStats.totalSize)
                        } else if (scanning) {
                            stringResource(R.string.fm_calculating)
                        } else "-"
                    )
                    PropertyRow(
                        R.string.fm_property_files,
                        dirStats?.fileCount?.toString() ?: if (scanning) stringResource(R.string.fm_calculating) else "-"
                    )
                    PropertyRow(
                        R.string.fm_property_folders,
                        dirStats?.dirCount?.toString() ?: if (scanning) stringResource(R.string.fm_calculating) else "-"
                    )
                    if (scanning) {
                        LinearProgressIndicator(modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp))
                    }
                } else {
                    PropertyRow(R.string.fm_property_size, sizeText)
                }
            }
        },
        confirmText = stringResource(R.string.generic_close),
        onDismiss = onDismiss
    )
}
