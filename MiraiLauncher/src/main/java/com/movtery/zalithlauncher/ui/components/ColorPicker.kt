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

package com.movtery.zalithlauncher.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.movtery.colorpicker.ColorPickerController
import com.movtery.colorpicker.components.AlphaBarPicker
import com.movtery.colorpicker.components.ColorSquarePicker
import com.movtery.colorpicker.components.HueBarPicker
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.ui.theme.cardColor
import com.movtery.zalithlauncher.ui.theme.onCardColor
import com.movtery.zalithlauncher.utils.logging.Logger

private const val TAG = "ColorPicker"

/**
 * 一个简易的颜色选择器
 * @param onChangeFinished 颜色完成变更
 * @param showAlpha 是否使用透明度调节器
 * @param showHue 是否使用色相调节器
 */
@Composable
fun ColorPickerDialog(
    colorController: ColorPickerController,
    onChangeFinished: () -> Unit = {},
    onCancel: () -> Unit,
    onConfirm: (Color) -> Unit,
    showAlpha: Boolean = true,
    showHue: Boolean = true
) {
    val selectedColor by colorController.color
    val selectedHex = remember(selectedColor) {
        selectedColor.toHex()
    }

    /**
     * 是否开启编辑Hex对话框
     */
    var editHex by remember {
        mutableStateOf(false)
    }

    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth(0.55f)
                .heightIn(max = rememberDialogMaxHeight())
                .fillMaxHeight(),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier
                    .padding(all = 16.dp)
                    .heightIn(max = (maxHeight - 32.dp).coerceAtMost(rememberDialogMaxHeight()))
                    .wrapContentHeight(),
                shadowElevation = 3.dp,
                color = cardColor(false),
                contentColor = onCardColor(),
                shape = MaterialTheme.shapes.extraLarge
            ) {
                Column(
                    modifier = Modifier.padding(all = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = stringResource(R.string.theme_color_picker_title),
                        style = MaterialTheme.typography.titleMedium
                    )

                    Column(
                        modifier = Modifier
                            .weight(1f, fill = false)
                            .fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .wrapContentHeight()
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            ColorSquarePicker(
                                modifier = Modifier
                                    .aspectRatio(1f)
                                    .weight(1f),
                                controller = colorController,
                                onChangeFinished = onChangeFinished
                            )

                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .verticalScrollWithBar(rememberScrollState()),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                if (showAlpha || showHue) {
                                    Column(
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        if (showAlpha) {
                                            AlphaBarPicker(
                                                modifier = Modifier
                                                    .height(30.dp)
                                                    .fillMaxWidth(),
                                                controller = colorController,
                                                onChangeFinished = onChangeFinished
                                            )
                                        }

                                        if (showHue) {
                                            HueBarPicker(
                                                modifier = Modifier
                                                    .height(30.dp)
                                                    .fillMaxWidth(),
                                                controller = colorController,
                                                onChangeFinished = onChangeFinished
                                            )
                                        }
                                    }
                                }

                                //颜色预览
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    val originalColor = remember {
                                        colorController.getOriginalColor()
                                    }

                                    //初始颜色
                                    Text(
                                        text = originalColor.toHex(),
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(30.dp)
                                            .background(color = originalColor)
                                    )
                                }

                                Column(modifier = Modifier.fillMaxWidth()) {
                                    //当前颜色
                                    Text(
                                        text = selectedHex,
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(30.dp)
                                                .background(color = selectedColor)
                                        )
                                        //手动编辑Hex
                                        IconButton(
                                            modifier = Modifier.size(36.dp),
                                            onClick = { editHex = true }
                                        ) {
                                            Icon(
                                                painter = painterResource(R.drawable.ic_edit_outlined),
                                                contentDescription = stringResource(R.string.theme_color_picker_edit_hex)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        FilledTonalButton(
                            modifier = Modifier.weight(1f),
                            onClick = {
                                onChangeFinished()
                                onCancel()
                            }
                        ) {
                            MarqueeText(text = stringResource(R.string.generic_cancel))
                        }
                        Button(
                            modifier = Modifier.weight(1f),
                            onClick = {
                                onConfirm(selectedColor)
                            }
                        ) {
                            MarqueeText(text = stringResource(R.string.generic_confirm))
                        }
                    }
                }
            }
        }
    }

    if (editHex) {
        var value by remember {
            mutableStateOf(selectedHex)
        }
        val newColor = remember(value) {
            //尝试转换为颜色对象
            value.toColorOrNull()
        }

        SimpleEditDialog(
            title = stringResource(R.string.theme_color_picker_edit_hex),
            value = value,
            onValueChange = { new ->
                value = new
            },
            isError = newColor == null,
            supportingText = {
                if (newColor == null) {
                    Text(text = stringResource(R.string.theme_color_picker_edit_hex_invalid))
                }
            },
            onDismissRequest = { editHex = false },
            onConfirm = {
                if (newColor != null) {
                    colorController.setColor(
                        if (showAlpha) newColor else newColor.copy(alpha = 1f)
                    )
                    editHex = false
                }
            }
        )
    }
}

/**
 * 将颜色转换为Hex字符串
 */
fun Color.toHex(): String {
    val alpha = (alpha * 255).toInt().toString(16).padStart(2, '0')
    val red = (red * 255).toInt().toString(16).padStart(2, '0')
    val green = (green * 255).toInt().toString(16).padStart(2, '0')
    val blue = (blue * 255).toInt().toString(16).padStart(2, '0')
    return "$alpha$red$green$blue".uppercase()
}

/**
 * 将Hex字符串转换为颜色对象
 * 若无法转换，则返回null
 *
 * 支持格式 AARRGGBB 或 RRGGBB
 */
fun String.toColorOrNull(): Color? {
    val hex = this.removePrefix("#")
    return try {
        val value = hex.toLong(16)
        when (hex.length) {
            6 -> {
                val r = ((value shr 16) and 0xFF) / 255f
                val g = ((value shr 8) and 0xFF) / 255f
                val b = (value and 0xFF) / 255f
                Color(r, g, b, 1f)
            }
            8 -> {
                val a = ((value shr 24) and 0xFF) / 255f
                val r = ((value shr 16) and 0xFF) / 255f
                val g = ((value shr 8) and 0xFF) / 255f
                val b = (value and 0xFF) / 255f
                Color(r, g, b, a)
            }
            else -> null
        }
    } catch (_: Exception) {
        Logger.debug(TAG, "Failed to convert hex to color, input: $this")
        null
    }
}