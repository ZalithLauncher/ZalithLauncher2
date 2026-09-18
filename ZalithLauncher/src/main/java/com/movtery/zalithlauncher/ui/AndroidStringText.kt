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

package com.movtery.zalithlauncher.ui

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit

/**
 * Android 可展示字符串文本代理接口，提供不同的文本展现方式
 */
sealed interface AndroidStringText {
    /**
     * 直接展示普通字符串
     *
     * @property value 字符串内容
     */
    data class Text(val value: String) : AndroidStringText

    /**
     * 展示带有样式的富文本
     *
     * @property value [AnnotatedString] 内容
     */
    data class Annotated(val value: AnnotatedString) : AndroidStringText

    /**
     * 通过 Android 资源 ID 加载字符串，支持格式化参数
     *
     * @property key 字符串资源 ID
     * @property args 格式化参数，支持 [AndroidStringText]
     */
    data class StringRes(
        @field:androidx.annotation.StringRes
        val key: Int,
        val args: Array<out Any>?
    ) : AndroidStringText {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as StringRes

            if (key != other.key) return false
            if (!args.contentEquals(other.args)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = key
            result = 31 * result + (args?.contentHashCode() ?: 0)
            return result
        }
    }

    /**
     * 拼接多个 [AndroidStringText] 实例
     *
     * @property texts 要拼接的字符串列表
     */
    data class Appended(
        val texts: List<AndroidStringText>
    ) : AndroidStringText
}

/**
 * 创建 [AndroidStringText.Text] 实例
 *
 * @param string 字符串内容
 */
fun androidText(string: String) = AndroidStringText.Text(string)

/**
 * 创建 [AndroidStringText.Annotated] 实例
 *
 * @param annotated [AnnotatedString] 内容
 */
fun androidText(annotated: AnnotatedString) = AndroidStringText.Annotated(annotated)

/**
 * 创建 [AndroidStringText.StringRes] 实例
 *
 * @param key 字符串资源 ID
 */
fun androidText(@StringRes key: Int) = AndroidStringText.StringRes(key, null)

/**
 * 创建带有格式化参数的 [AndroidStringText.StringRes] 实例
 *
 * @param key 字符串资源 ID
 * @param args 格式化参数
 */
fun androidText(
    @StringRes
    key: Int,
    vararg args: Any
) = AndroidStringText.StringRes(key, args)

/**
 * 创建 [AndroidStringText.Appended] 实例
 */
fun androidText(vararg texts: AndroidStringText) = AndroidStringText.Appended(texts.toList())


/**
 * 使用 DSL 构建 [AndroidStringText.Appended] 实例
 */
inline fun buildAppendedText(
    block: AndroidStringTextBuilder.() -> Unit
): AndroidStringText = AndroidStringTextBuilder().apply(block).build()

@DslMarker
private annotation class AndroidStringTextDsl
@AndroidStringTextDsl
class AndroidStringTextBuilder {
    private val texts = mutableListOf<AndroidStringText>()
    /**
     * 追加普通字符串
     */
    fun append(text: String) {
        texts.add(AndroidStringText.Text(text))
    }
    /**
     * 追加带有样式的富文本
     */
    fun append(text: AnnotatedString) {
        texts.add(AndroidStringText.Annotated(text))
    }
    /**
     * 通过 Android 资源 ID 加载字符串
     */
    fun append(@StringRes resId: Int) {
        texts.add(AndroidStringText.StringRes(resId, null))
    }
    /**
     * 通过 Android 资源 ID 加载字符串，支持格式化参数
     */
    fun append(@StringRes resId: Int, vararg args: Any) {
        texts.add(AndroidStringText.StringRes(resId, args))
    }
    /**
     * 追加另一个字符串代理对象
     */
    fun append(other: AndroidStringText) {
        texts.add(other)
    }
    /**
     * 构建 [AndroidStringText.Appended] 实例
     */
    fun build(): AndroidStringText = AndroidStringText.Appended(texts.toList())
}



/**
 * 用于展示 [AndroidStringText] 的 Composable 组件
 *
 * 该组件是对 [Text] 的封装，能够根据 [AndroidStringText] 的具体类型
 * 自动选择合适的方式进行渲染
 */
@Composable
fun AndroidStringText(
    text: AndroidStringText,
    modifier: Modifier = Modifier,
    autoSize: TextAutoSize? = null,
    fontSize: TextUnit = TextUnit.Unspecified,
    fontStyle: FontStyle? = null,
    fontWeight: FontWeight? = null,
    fontFamily: FontFamily? = null,
    letterSpacing: TextUnit = TextUnit.Unspecified,
    textDecoration: TextDecoration? = null,
    textAlign: TextAlign? = null,
    lineHeight: TextUnit = TextUnit.Unspecified,
    overflow: TextOverflow = TextOverflow.Clip,
    softWrap: Boolean = true,
    maxLines: Int = Int.MAX_VALUE,
    minLines: Int = 1,
    style: TextStyle = LocalTextStyle.current,
) {
    Text(
        text = resolveAndroidString(text),
        modifier = modifier,
        autoSize = autoSize,
        fontSize = fontSize,
        fontStyle = fontStyle,
        fontWeight = fontWeight,
        fontFamily = fontFamily,
        letterSpacing = letterSpacing,
        textDecoration = textDecoration,
        textAlign = textAlign,
        lineHeight = lineHeight,
        overflow = overflow,
        softWrap = softWrap,
        maxLines = maxLines,
        minLines = minLines,
        style = style,
    )
}

/**
 * 将 [AndroidStringText] 解析为 [AnnotatedString]
 */
@Composable
fun resolveAndroidString(text: AndroidStringText): AnnotatedString {
    return when (text) {
        is AndroidStringText.Text -> AnnotatedString(text.value)
        is AndroidStringText.Annotated -> text.value
        is AndroidStringText.StringRes -> {
            val args = text.args
            AnnotatedString(
                if (args == null) {
                    stringResource(text.key)
                } else {
                    val resolvedArgs = args.map { arg ->
                        if (arg is AndroidStringText) {
                            resolveAndroidString(arg).text
                        } else {
                            arg
                        }
                    }.toTypedArray()
                    stringResource(text.key, *resolvedArgs)
                }
            )
        }
        is AndroidStringText.Appended -> {
            buildAnnotatedString {
                text.texts.forEach {
                    append(resolveAndroidString(it))
                }
            }
        }
    }
}

/**
 * 在非 Composable 环境中将 [AndroidStringText] 解析为 [String]
 *
 * @param context Android [Context]，用于加载 [AndroidStringText.StringRes] 类型的字符串资源
 */
fun AndroidStringText.toAndroidString(context: Context): String = when (this) {
    is AndroidStringText.Text -> value
    is AndroidStringText.Annotated -> value.toString()
    is AndroidStringText.StringRes -> if (args == null) {
        context.getString(key)
    } else {
        val resolvedArgs = args.map { arg ->
            if (arg is AndroidStringText) {
                arg.toAndroidString(context)
            } else {
                arg
            }
        }.toTypedArray()
        context.getString(key, *resolvedArgs)
    }
    is AndroidStringText.Appended -> texts.joinToString("") { it.toAndroidString(context) }
}
