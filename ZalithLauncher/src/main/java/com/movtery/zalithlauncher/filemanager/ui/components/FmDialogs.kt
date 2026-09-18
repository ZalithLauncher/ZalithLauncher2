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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenu
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.filemanager.ui.theme.fmCardColor
import com.movtery.zalithlauncher.filemanager.ui.theme.fmOnCardColor
import com.movtery.zalithlauncher.ui.components.ImePanContainer
import com.movtery.zalithlauncher.ui.components.MarqueeText
import com.movtery.zalithlauncher.ui.components.OwnOutlinedTextField
import com.movtery.zalithlauncher.ui.components.fadeEdge
import com.movtery.zalithlauncher.ui.components.imePanAnchor
import com.movtery.zalithlauncher.ui.components.rememberDialogMaxHeight
import com.movtery.zalithlauncher.ui.components.verticalScrollWithBar

/** 文件管理器通用对话框容器 */
@Composable
internal fun FmDialogSurface(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    horizontalAlignment: Alignment.Horizontal = Alignment.CenterHorizontally,
    verticalArrangement: Arrangement.Vertical = Arrangement.spacedBy(16.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(decorFitsSystemWindows = false)
    ) {
        ImePanContainer(
            modifier = Modifier
                .heightIn(max = rememberDialogMaxHeight())
                .fillMaxHeight(),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = modifier
                    .padding(all = 6.dp)
                    .heightIn(max = (maxHeight - 12.dp).coerceAtMost(rememberDialogMaxHeight()))
                    .wrapContentHeight(),
                shape = MaterialTheme.shapes.extraLarge,
                color = fmCardColor(),
                contentColor = fmOnCardColor(),
                shadowElevation = 6.dp
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = horizontalAlignment,
                    verticalArrangement = verticalArrangement,
                    content = content,
                )
            }
        }
    }
}

@Composable
internal fun FmDialogButtons(
    confirmText: String = stringResource(R.string.generic_confirm),
    dismissText: String = stringResource(R.string.generic_cancel),
    confirmEnabled: Boolean = true,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        FilledTonalButton(
            modifier = Modifier.weight(1f),
            onClick = onDismiss
        ) {
            MarqueeText(text = dismissText)
        }

        Button(
            modifier = Modifier.weight(1f),
            enabled = confirmEnabled,
            onClick = onConfirm
        ) {
            MarqueeText(text = confirmText)
        }
    }
}


/** 显示带确认与取消按钮的提示对话框 */
@Composable
fun FmAlertDialog(
    title: String,
    text: String,
    confirmText: String = stringResource(R.string.generic_confirm),
    dismissText: String = stringResource(R.string.generic_cancel),
    dismissByDialog: Boolean = true,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    FmAlertDialog(
        title = title,
        text = {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
            )
        },
        confirmText = confirmText,
        dismissText = dismissText,
        dismissByDialog = dismissByDialog,
        onConfirm = onConfirm,
        onDismiss = onDismiss
    )
}

/** 显示仅带确认按钮的提示对话框 */
@Composable
fun FmAlertDialog(
    title: String,
    text: String,
    confirmText: String = stringResource(R.string.generic_confirm),
    dismissByDialog: Boolean = true,
    onDismiss: () -> Unit
) {
    FmAlertDialog(
        title = title,
        text = {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
            )
        },
        confirmText = confirmText,
        dismissByDialog = dismissByDialog,
        onDismiss = onDismiss
    )
}

/** 显示带确认与取消按钮的提示对话框，内容为自定义组件 */
@Composable
fun FmAlertDialog(
    title: String,
    text: @Composable ColumnScope.() -> Unit = {},
    confirmText: String = stringResource(R.string.generic_confirm),
    dismissText: String = stringResource(R.string.generic_cancel),
    dismissByDialog: Boolean = true,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    FmDialogSurface(
        onDismissRequest = { if (dismissByDialog) onDismiss() }
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium
        )

        val scrollState = rememberScrollState()
        Column(
            modifier = Modifier
                .fadeEdge(state = scrollState)
                .weight(1f, fill = false)
                .verticalScrollWithBar(state = scrollState)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            text()
        }

        FmDialogButtons(
            confirmText = confirmText,
            dismissText = dismissText,
            onConfirm = onConfirm,
            onDismiss = onDismiss
        )
    }
}

/** 显示仅带确认按钮的提示对话框，内容为自定义组件 */
@Composable
fun FmAlertDialog(
    title: String,
    text: @Composable ColumnScope.() -> Unit = {},
    confirmText: String = stringResource(R.string.generic_confirm),
    dismissByDialog: Boolean = true,
    onDismiss: () -> Unit
) {
    FmDialogSurface(
        onDismissRequest = { if (dismissByDialog) onDismiss() }
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium
        )

        val scrollState = rememberScrollState()
        Column(
            modifier = Modifier
                .fadeEdge(state = scrollState)
                .weight(1f, fill = false)
                .verticalScrollWithBar(state = scrollState)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            content = text,
        )

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = onDismiss
        ) {
            MarqueeText(text = confirmText)
        }
    }
}

/** 显示带文本输入框的编辑对话框 */
@Composable
fun FmEditDialog(
    title: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    supportingText: @Composable (() -> Unit)? = null,
    singleLine: Boolean = false,
    maxLines: Int = 3,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions? = null,
    extraBody: @Composable (() -> Unit)? = null,
    extraContent: @Composable (() -> Unit)? = null,
    confirmText: String = stringResource(R.string.generic_confirm),
    confirmEnabled: Boolean = true,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    onDismissRequest: () -> Unit = {},
    onCancel: () -> Unit = onDismissRequest,
    onConfirm: () -> Unit = {}
) {
    FmDialogSurface(
        onDismissRequest = onDismissRequest,
        modifier = modifier
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium
        )

        val scrollState = rememberScrollState()
        Column(
            modifier = Modifier
                .fadeEdge(state = scrollState)
                .weight(1f, fill = false)
                .verticalScrollWithBar(state = scrollState)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            extraBody?.let {
                it()
                Spacer(modifier = Modifier.size(8.dp))
            }

            val focusManager = LocalFocusManager.current
            OwnOutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = value,
                onValueChange = onValueChange,
                label = label,
                isError = isError,
                supportingText = supportingText,
                singleLine = singleLine,
                maxLines = maxLines,
                keyboardOptions = if (singleLine) {
                    keyboardOptions.copy(imeAction = ImeAction.Done)
                } else {
                    keyboardOptions
                },
                keyboardActions = keyboardActions ?: KeyboardActions(
                    onDone = {
                        focusManager.clearFocus(true)
                        onConfirm()
                    }
                ),
                visualTransformation = visualTransformation,
                shape = MaterialTheme.shapes.large
            )
            extraContent?.invoke()
        }

        FmDialogButtons(
            confirmText = confirmText,
            confirmEnabled = confirmEnabled,
            onConfirm = onConfirm,
            onDismiss = onCancel
        )
    }
}

/** 显示带文本输入框的编辑对话框 */
@Composable
fun FmEditDialog(
    title: String,
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    modifier: Modifier = Modifier,
    label: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    supportingText: @Composable (() -> Unit)? = null,
    singleLine: Boolean = false,
    maxLines: Int = 3,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    extraBody: @Composable (() -> Unit)? = null,
    extraContent: @Composable (() -> Unit)? = null,
    confirmText: String = stringResource(R.string.generic_confirm),
    confirmEnabled: Boolean = true,
    focusRequester: FocusRequester? = null,
    onDismissRequest: () -> Unit = {},
    onCancel: () -> Unit = onDismissRequest,
    onConfirm: () -> Unit = {}
) {
    focusRequester?.let {
        LaunchedEffect(Unit) {
            it.requestFocus()
        }
    }

    FmDialogSurface(
        onDismissRequest = onDismissRequest,
        modifier = modifier
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium
        )

        val scrollState = rememberScrollState()
        Column(
            modifier = Modifier
                .fadeEdge(state = scrollState)
                .weight(1f, fill = false)
                .verticalScrollWithBar(state = scrollState)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            extraBody?.let {
                it()
                Spacer(modifier = Modifier.size(8.dp))
            }

            val focusManager = LocalFocusManager.current
            OutlinedTextField(
                modifier = Modifier
                    .imePanAnchor()
                    .fillMaxWidth()
                    .let { if (focusRequester != null) it.focusRequester(focusRequester) else it },
                value = value,
                onValueChange = onValueChange,
                label = label,
                isError = isError,
                supportingText = supportingText,
                singleLine = singleLine,
                maxLines = maxLines,
                keyboardOptions = if (singleLine) {
                    keyboardOptions.copy(imeAction = ImeAction.Done)
                } else {
                    keyboardOptions
                },
                keyboardActions = KeyboardActions(
                    onDone = {
                        focusManager.clearFocus(true)
                        onConfirm()
                    }
                ),
                shape = MaterialTheme.shapes.large
            )
            extraContent?.invoke()
        }

        FmDialogButtons(
            confirmText = confirmText,
            confirmEnabled = confirmEnabled,
            onConfirm = onConfirm,
            onDismiss = onCancel
        )
    }
}

/** 显示带文本输入框与复选框的编辑对话框 */
@Composable
fun FmCheckEditDialog(
    title: String,
    text: String? = null,
    value: String,
    checked: Boolean,
    checkBoxText: String? = null,
    onValueChange: (String) -> Unit,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    label: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    supportingText: @Composable (() -> Unit)? = null,
    singleLine: Boolean = false,
    maxLines: Int = 3,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions? = null,
    confirmText: String = stringResource(R.string.generic_confirm),
    confirmEnabled: Boolean = true,
    onDismissRequest: () -> Unit = {},
    onCancel: () -> Unit = onDismissRequest,
    onConfirm: () -> Unit = {}
) {
    FmEditDialog(
        title = title,
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        label = label,
        isError = isError,
        supportingText = supportingText,
        singleLine = singleLine,
        maxLines = maxLines,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        extraBody = text?.let {
            {
                Text(
                    text = it,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        },
        extraContent = {
            Column(
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = checked,
                        onCheckedChange = onCheckedChange
                    )
                    checkBoxText?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        },
        confirmText = confirmText,
        confirmEnabled = confirmEnabled,
        onDismissRequest = onDismissRequest,
        onCancel = onCancel,
        onConfirm = onConfirm
    )
}


/** 通用下拉选择框 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> ExposedDropdown(
    selectedText: String,
    label: String,
    options: List<T>,
    optionLabel: @Composable (T) -> String,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        OwnOutlinedTextField(
            value = selectedText,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            modifier = modifier
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth(),
            shape = MaterialTheme.shapes.large
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(optionLabel(option)) },
                    onClick = {
                        expanded = false
                        onSelect(option)
                    }
                )
            }
        }
    }
}