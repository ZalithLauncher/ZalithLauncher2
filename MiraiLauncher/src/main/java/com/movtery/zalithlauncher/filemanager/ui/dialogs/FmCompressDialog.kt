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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.filemanager.logic.compress.CompressFormat
import com.movtery.zalithlauncher.filemanager.logic.compress.CompressMethod
import com.movtery.zalithlauncher.filemanager.logic.compress.CompressOptions
import com.movtery.zalithlauncher.filemanager.ui.components.ExposedDropdown
import com.movtery.zalithlauncher.filemanager.ui.components.FmDialogButtons
import com.movtery.zalithlauncher.filemanager.ui.components.FmDialogSurface
import com.movtery.zalithlauncher.filemanager.ui.theme.fmErrorColor
import com.movtery.zalithlauncher.ui.components.ButtonPosition
import com.movtery.zalithlauncher.ui.components.IndicatorSlider
import com.movtery.zalithlauncher.ui.components.OwnOutlinedTextField
import com.movtery.zalithlauncher.ui.components.PositionButton
import com.movtery.zalithlauncher.ui.components.PositionFilledTonalButton
import com.movtery.zalithlauncher.ui.components.fadeEdge
import com.movtery.zalithlauncher.ui.components.verticalScrollWithBar

/**
 * 压缩设置对话框
 * @param defaultName 输出名称默认值
 * @param onConfirm 确认压缩，回传输出名称与压缩选项
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FmCompressDialog(
    defaultName: String,
    onDismiss: () -> Unit,
    onConfirm: (name: String, options: CompressOptions) -> Unit
) {
    val keyboard = LocalSoftwareKeyboardController.current
    var name by remember { mutableStateOf(defaultName) }
    var format by remember { mutableStateOf(CompressFormat.ZIP) }
    var advanced by remember { mutableStateOf(false) }
    var level by remember { mutableStateOf(5) }
    var method by remember { mutableStateOf(CompressFormat.ZIP.defaultMethod) }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordMismatch by remember { mutableStateOf(false) }

    val methods = when (format) {
        CompressFormat.ZIP -> CompressMethod.zipMethods
        CompressFormat.SEVEN_Z -> CompressMethod.sevenZMethods
        CompressFormat.TAR -> CompressMethod.tarMethods
    }

    fun applyFormat(newFormat: CompressFormat) {
        val baseName = baseNameWithoutSuffix(name)
        format = newFormat
        name = baseName + newFormat.suffix
        method = newFormat.defaultMethod
        if (newFormat == CompressFormat.TAR) {
            password = ""
            confirmPassword = ""
            passwordMismatch = false
        }
    }

    fun confirmEnabled(): Boolean {
        if (name.isBlank()) return false
        if (advanced && format != CompressFormat.TAR && password.isNotEmpty() && password != confirmPassword) return false
        return true
    }

    FmDialogSurface(onDismissRequest = onDismiss) {
        Text(
            text = stringResource(R.string.fm_archive),
            style = MaterialTheme.typography.titleMedium
        )

        val scrollState = rememberScrollState()
        Column(
            modifier = Modifier
                .fadeEdge(state = scrollState)
                .weight(1f, fill = false)
                .verticalScrollWithBar(state = scrollState)
                .fillMaxWidth(),
        ) {
            OwnOutlinedTextField(
                value = name,
                onValueChange = { name = it },
                singleLine = true,
                label = { Text(stringResource(R.string.fm_new_name)) },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large
            )

            Spacer(Modifier.size(8.dp))

            FormatDropdown(
                format = format,
                onFormatChange = { applyFormat(it) }
            )

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(checked = advanced, onCheckedChange = { advanced = it })
                Text(stringResource(R.string.fm_compress_advanced))
            }

            if (advanced && format == CompressFormat.TAR) {
                // TAR仅提供格式选择
                ExposedDropdown(
                    selectedText = method.displayName,
                    label = stringResource(R.string.fm_compress_method),
                    options = methods,
                    optionLabel = { it.displayName },
                    onSelect = { method = it }
                )
            } else if (advanced) {
                AdvancedCompressSection(
                    format = format,
                    level = level,
                    onLevelChange = { level = it },
                    method = method,
                    onMethodChange = { method = it },
                    password = password,
                    confirmPassword = confirmPassword,
                    passwordMismatch = passwordMismatch,
                    onPasswordChange = {
                        password = it
                        passwordMismatch = password != confirmPassword
                    },
                    onConfirmPasswordChange = {
                        confirmPassword = it
                        passwordMismatch = password != confirmPassword
                    }
                )
            }
        }

        FmDialogButtons(
            confirmEnabled = confirmEnabled(),
            onConfirm = {
                keyboard?.hide()
                onConfirm(
                    name,
                    CompressOptions(
                        format = format,
                        method = method,
                        level = if (format == CompressFormat.TAR) null else level,
                        password = if (advanced && format != CompressFormat.TAR && password.isNotEmpty()) password else null
                    )
                )
            },
            onDismiss = onDismiss
        )
    }
}

private fun baseNameWithoutSuffix(name: String): String {
    val lower = name.lowercase()
    for (fmt in CompressFormat.entries) {
        if (lower.endsWith(fmt.suffix)) {
            return name.substring(0, name.length - fmt.suffix.length)
        }
    }
    return name
}

/**
 * 压缩输出位置选择对话框
 * @param onCurrentDir 在当前目录生成压缩包
 * @param onSaf 通过 SAF 选择输出目录
 */
@Composable
fun FmCompressOutputChoiceDialog(
    onDismiss: () -> Unit,
    onCurrentDir: () -> Unit,
    onSaf: () -> Unit
) {
    FmDialogSurface(onDismissRequest = onDismiss) {
        Text(
            text = stringResource(R.string.fm_compress_output_choice_title),
            style = MaterialTheme.typography.titleMedium
        )

        Text(
            modifier = Modifier.fillMaxWidth(),
            text = stringResource(R.string.fm_compress_output_choice_text),
            style = MaterialTheme.typography.bodyMedium
        )

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            PositionButton(
                onClick = {
                    onDismiss()
                    onCurrentDir()
                },
                position = ButtonPosition.Top,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.fm_compress_output_current_dir))
            }

            PositionButton(
                onClick = {
                    onDismiss()
                    onSaf()
                },
                position = ButtonPosition.Middle,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.fm_compress_output_saf))
            }

            PositionFilledTonalButton(
                onClick = onDismiss,
                position = ButtonPosition.Bottom,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.generic_cancel))
            }
        }
    }
}

@Composable
private fun FormatDropdown(
    format: CompressFormat,
    onFormatChange: (CompressFormat) -> Unit
) {
    ExposedDropdown(
        selectedText = format.extension,
        label = stringResource(R.string.fm_compress_format),
        options = CompressFormat.entries,
        optionLabel = { it.extension },
        onSelect = onFormatChange
    )
}

@Composable
private fun PasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String
) {
    OwnOutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        label = { Text(label) },
        visualTransformation = PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large
    )
}

@Composable
private fun AdvancedCompressSection(
    format: CompressFormat,
    level: Int,
    onLevelChange: (Int) -> Unit,
    method: CompressMethod,
    onMethodChange: (CompressMethod) -> Unit,
    password: String,
    confirmPassword: String,
    passwordMismatch: Boolean,
    onPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit
) {
    // 压缩等级（1..9）
    Column(Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.fm_compress_level, level),
            style = MaterialTheme.typography.bodyMedium
        )
        IndicatorSlider(
            value = level.toFloat(),
            onValueChange = { onLevelChange(it.toInt()) },
            valueRange = 1f..9f,
            steps = 7
        )
    }

    // 压缩方法
    ExposedDropdown(
        selectedText = method.displayName,
        label = stringResource(R.string.fm_compress_method),
        options = if (format == CompressFormat.ZIP) CompressMethod.zipMethods else CompressMethod.sevenZMethods,
        optionLabel = { it.displayName },
        onSelect = onMethodChange
    )

    // 密码
    PasswordField(
        value = password,
        onValueChange = onPasswordChange,
        label = stringResource(R.string.fm_compress_password)
    )
    PasswordField(
        value = confirmPassword,
        onValueChange = onConfirmPasswordChange,
        label = stringResource(R.string.fm_compress_confirm_password)
    )
    if (passwordMismatch) {
        Text(
            text = stringResource(R.string.fm_compress_password_mismatch),
            color = fmErrorColor(),
            style = MaterialTheme.typography.bodySmall
        )
    }
}
