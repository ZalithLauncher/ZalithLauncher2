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

package com.movtery.zalithlauncher.ui.screens.content.versions.elements

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.TextUnit
import coil3.compose.AsyncImage
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.game.text.MINECRAFT_COLOR_FORMAT
import com.movtery.zalithlauncher.game.text.WHITE
import com.movtery.zalithlauncher.game.version.multiplayer.description.ComponentDescription
import com.movtery.zalithlauncher.ui.androidText
import com.movtery.zalithlauncher.ui.buildAppendedText
import com.movtery.zalithlauncher.ui.components.SimpleAlertDialog
import com.movtery.zalithlauncher.ui.components.SimpleEditDialog
import com.movtery.zalithlauncher.ui.components.SimpleTaskDialog
import com.movtery.zalithlauncher.ui.screens.content.elements.isFilenameInvalid
import com.movtery.zalithlauncher.utils.string.getMessageOrToString
import com.movtery.zalithlauncher.viewmodel.ErrorViewModel
import kotlinx.coroutines.Dispatchers
import org.apache.commons.io.FileUtils
import java.io.File

/** 加载状态 */
sealed interface LoadingState {
    data object None : LoadingState
    /** 正在加载 */
    data object Loading : LoadingState
}

sealed interface DeleteAllOperation {
    data object None : DeleteAllOperation
    /** 警告用户是否真的要批量删除资源 */
    data class Warning(val files: List<File>): DeleteAllOperation
    /** 开始删除所有选中的资源 */
    data class Delete(val files: List<File>): DeleteAllOperation
    /** 已成功删除所有选中的资源 */
    data object Success : DeleteAllOperation
}

@Composable
fun DeleteAllOperation(
    operation: DeleteAllOperation,
    changeOperation: (DeleteAllOperation) -> Unit,
    submitError: (ErrorViewModel.ThrowableMessage) -> Unit,
    onRefresh: () -> Unit
) {
    when (operation) {
        is DeleteAllOperation.None -> {}
        is DeleteAllOperation.Warning -> {
            SimpleAlertDialog(
                title = stringResource(R.string.manage_delete_all),
                text = {
                    Text(text = stringResource(R.string.manage_delete_all_message))
                },
                confirmText = stringResource(R.string.generic_delete),
                onCancel = {
                    changeOperation(DeleteAllOperation.None)
                },
                onConfirm = {
                    changeOperation(DeleteAllOperation.Delete(operation.files))
                }
            )
        }
        is DeleteAllOperation.Delete -> {
            SimpleTaskDialog(
                title = stringResource(R.string.manage_delete_all),
                task = {
                    //开始删除所有文件
                    operation.files.forEach { file ->
                        FileUtils.delete(file)
                    }

                    changeOperation(DeleteAllOperation.Success)
                    onRefresh()
                },
                context = Dispatchers.IO,
                onDismiss = {},
                onError = { th ->
                    submitError(
                        ErrorViewModel.ThrowableMessage(
                            title = androidText(R.string.generic_error),
                            message = buildAppendedText {
                                append(R.string.manage_delete_all_error)
                                append("\r\n")
                                append(th.getMessageOrToString())
                            }
                        )
                    )
                }
            )
        }
        is DeleteAllOperation.Success -> {
            SimpleAlertDialog(
                title = stringResource(R.string.manage_delete_all),
                text = stringResource(R.string.manage_delete_all_success)
            ) {
                changeOperation(DeleteAllOperation.None)
            }
        }
    }
}

/**
 * Minecraft 颜色占位符、样式占位符格式化后的 Text
 * 像 Minecraft 一样，渲染两层文本，底层作为背景层，顶层作为前景层
 * 若输入字符串内不存在 `§`，则使用普通的 Text
 */
@Composable
fun MinecraftColorTextNormal(
    modifier: Modifier = Modifier,
    inputText: String,
    style: TextStyle,
    maxLines: Int = Int.MAX_VALUE,
) {
    if (inputText.contains("§")) {
        MinecraftColorText(
            modifier = modifier,
            inputText = inputText,
            fontSize = style.fontSize,
            maxLines = maxLines,
            style = style,
        )
    } else {
        val density = LocalDensity.current
        val lineHeight: TextUnit = with(density) {
            (style.fontSize.toPx() * 1.1f).toSp()
        }

        Text(
            modifier = modifier,
            text = inputText,
            style = style,
            maxLines = maxLines,
            lineHeight = lineHeight
        )
    }
}

/** 格式化类型：只渲染前景层 */
private const val FORMAT_TYPE_FOREGROUND = 0
/** 格式化类型：只渲染背景层 */
private const val FORMAT_TYPE_BACKGROUND = 1

/**
 * Minecraft 颜色占位符、样式占位符格式化后的 Text
 * 像 Minecraft 一样，渲染两层文本，底层作为背景层，顶层作为前景层
 */
@Composable
fun MinecraftColorText(
    modifier: Modifier = Modifier,
    inputText: String,
    fontSize: TextUnit = TextUnit.Unspecified,
    maxLines: Int = Int.MAX_VALUE,
    style: TextStyle = LocalTextStyle.current,
) {
    val (foreground, background) = remember(inputText) {
        val segments = parseSegments(inputText)
        buildTextWithSegments(segments, FORMAT_TYPE_FOREGROUND) to
        buildTextWithSegments(segments, FORMAT_TYPE_BACKGROUND)
    }

    MinecraftColorText(
        modifier = modifier,
        foreground = foreground,
        background = background,
        fontSize = fontSize,
        maxLines = maxLines,
        style = style,
    )
}

@Composable
private fun MinecraftColorText(
    foreground: AnnotatedString,
    background: AnnotatedString,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = TextUnit.Unspecified,
    maxLines: Int = Int.MAX_VALUE,
    softWrap: Boolean = false,
    style: TextStyle = LocalTextStyle.current.merge(fontSize = fontSize),
) {
    val density = LocalDensity.current

    val lineHeight: TextUnit = with(density) {
        (style.fontSize.toPx() * 1.1f).toSp()
    }

    //计算出合适的偏移量
    val offsetFactor = 1f / 16f
    val offsetDp = with(density) {
        (style.fontSize.toPx() * offsetFactor).toDp()
    }

    Box(
        modifier = modifier
    ) {
        //背景层
        Text(
            text = background,
            fontSize = fontSize,
            maxLines = maxLines,
            modifier = Modifier.offset(x = offsetDp, y = offsetDp),
            lineHeight = lineHeight,
            softWrap = softWrap,
            style = style,
        )
        //前景层
        Text(
            text = foreground,
            fontSize = fontSize,
            maxLines = maxLines,
            lineHeight = lineHeight,
            softWrap = softWrap,
            style = style,
        )
    }
}

private fun buildTextWithSegments(
    segments: List<Pair<String, TextStyleState>>,
    type: Int = FORMAT_TYPE_FOREGROUND
): AnnotatedString {
    return buildAnnotatedString {
        segments.forEach { (text, styleState) ->
            withStyle(styleState.toSpanStyle(type)) {
                append(text)
            }
        }
    }
}

private fun parseSegments(input: String): List<Pair<String, TextStyleState>> {
    val segments = mutableListOf<Pair<String, TextStyleState>>()
    var currentStyle = TextStyleState()
    var index = 0
    var buffer = StringBuilder()

    while (index < input.length) {
        //判断是否是格式代码
        if (input[index] == '§' && index + 1 < input.length) {
            if (buffer.isNotEmpty()) {
                segments.add(buffer.toString() to currentStyle)
                buffer = StringBuilder()
            }

            val code = input[index + 1].lowercaseChar()
            currentStyle = when (code) {
                in MINECRAFT_COLOR_FORMAT -> {
                    val colors = MINECRAFT_COLOR_FORMAT[code]!!
                    currentStyle.copy(
                        color = colors.foreground,
                        background = colors.background
                    )
                }
                'r' -> TextStyleState() //重置样式
                'l' -> currentStyle.copy(bold = true)
                'o' -> currentStyle.copy(italic = true)
                'n' -> currentStyle.copy(underline = true)
                'm' -> currentStyle.copy(strikethrough = true)
                else -> currentStyle //忽略未知或不支持的格式代码（如k）
            }

            index += 2
        } else {
            buffer.append(input[index])
            index++
        }
    }

    if (buffer.isNotEmpty()) {
        segments.add(buffer.toString() to currentStyle)
    }

    return segments
}

private fun parseTextWithStyle(input: ComponentDescription) =
    input.text to TextStyleState(
        color = input.color?.foreground ?: WHITE.foreground,
        background = input.color?.background ?: WHITE.background,
        bold = input.bold ?: false,
        italic = input.italic ?: false,
        underline = input.underlined ?: false,
        strikethrough = input.strikethrough ?: false,
    )

/**
 * 拼接单节点所有文本组件
 * @param description 文本组件节点
 */
private fun flattenComponents(
    description: ComponentDescription,
    out: MutableList<Pair<String, TextStyleState>>
) {
    if (description.text.isNotEmpty()) {
        if (description.text.contains('§')) {
            out.addAll(parseSegments(description.text))
        } else {
            out += parseTextWithStyle(description)
        }
    }

    description.extra.forEach { child ->
        flattenComponents(child, out)
    }
}

private fun parseComponentSegments(
    descriptions: List<ComponentDescription>
): List<Pair<String, TextStyleState>> {
    val result = mutableListOf<Pair<String, TextStyleState>>()
    descriptions.forEach {
        flattenComponents(it, result)
    }
    return result
}

/**
 * 模仿 Minecraft 原版对于文本组件的渲染
 */
@Composable
fun ComponentText(
    descriptions: List<ComponentDescription>,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = TextUnit.Unspecified,
    maxLines: Int = Int.MAX_VALUE,
    softWrap: Boolean = false,
) {
    val density = LocalDensity.current

    val lineHeight: TextUnit = with(density) {
        (fontSize.toPx() * 1.1f).toSp()
    }

    val offsetFactor = 1f / 16f
    val offsetDp = with(density) { (fontSize.toPx() * offsetFactor).toDp() }

    val (foreground, background) = remember(descriptions) {
        val segments = parseComponentSegments(descriptions)
        buildTextWithSegments(segments, FORMAT_TYPE_FOREGROUND) to
        buildTextWithSegments(segments, FORMAT_TYPE_BACKGROUND)
    }

    Box(modifier = modifier) {
        Text(
            modifier = Modifier.offset(offsetDp, offsetDp),
            text = background,
            fontSize = fontSize,
            maxLines = maxLines,
            lineHeight = lineHeight,
            softWrap = softWrap,
        )
        Text(
            text = foreground,
            fontSize = fontSize,
            maxLines = maxLines,
            lineHeight = lineHeight,
            softWrap = softWrap,
        )
    }
}

/**
 * @param color 前景颜色
 * @param background 背景颜色，默认为深灰色
 * @param bold 加粗
 * @param italic 斜体
 * @param underline 下划线
 * @param strikethrough 删除线
 */
private data class TextStyleState(
    val color: Color? = WHITE.foreground,
    val background: Color? = WHITE.background,
    val bold: Boolean = false,
    val italic: Boolean = false,
    val underline: Boolean = false,
    val strikethrough: Boolean = false
)

private fun TextStyleState.toSpanStyle(
    type: Int
): SpanStyle {
    val isForeground = type == FORMAT_TYPE_FOREGROUND

    return SpanStyle(
        color = if (isForeground) {
            color ?: Color.Unspecified
        } else {
            background ?: Color.Transparent
        },
        fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal,
        fontStyle = if (italic) FontStyle.Italic else FontStyle.Normal,
        textDecoration = when {
            underline && strikethrough ->
                TextDecoration.combine(
                    listOf(TextDecoration.Underline, TextDecoration.LineThrough)
                )
            underline -> TextDecoration.Underline
            strikethrough -> TextDecoration.LineThrough
            else -> TextDecoration.None
        }
    )
}

@Composable
fun FileNameInputDialog(
    initValue: String,
    existsCheck: @Composable (String) -> String?,
    title: String,
    label: String,
    onDismissRequest: () -> Unit = {},
    onConfirm: (vale: String) -> Unit = {}
) {
    var value by remember { mutableStateOf(initValue) }

    val filenameInvalidMessage = key(value) {
        isFilenameInvalid(value)
    }
    val existsText = key(value) { existsCheck(value) }
    val isError = value.isEmpty() || filenameInvalidMessage != null || existsText != null

    SimpleEditDialog(
        title = title,
        value = value,
        onValueChange = { value = it },
        isError = isError,
        label = {
            Text(text = label)
        },
        supportingText = {
            when {
                value.isEmpty() -> Text(text = stringResource(R.string.generic_cannot_empty))
                filenameInvalidMessage != null -> Text(text = filenameInvalidMessage)
                existsText != null -> Text(text = existsText)
            }
        },
        singleLine = true,
        onDismissRequest = onDismissRequest,
        onConfirm = {
            if (!isError) {
                onConfirm(value)
            }
        }
    )
}

@Composable
fun ByteArrayIcon(
    modifier: Modifier = Modifier,
    triggerRefresh: Any? = null,
    defaultIcon: Int = R.drawable.ic_unknown_pack,
    icon: ByteArray?,
    colorFilter: ColorFilter? = null
) {
    val context = LocalContext.current

    val model = remember(triggerRefresh, context) {
        icon ?: defaultIcon
    }

    AsyncImage(
        modifier = modifier,
        model = model,
        contentDescription = null,
        alignment = Alignment.Center,
        contentScale = ContentScale.Fit,
        colorFilter = colorFilter
    )
}