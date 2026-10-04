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
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.sp
import com.halilibo.richtext.commonmark.Markdown
import com.halilibo.richtext.markdown.AstBlockNodeComposer
import com.halilibo.richtext.markdown.BasicMarkdown
import com.halilibo.richtext.markdown.node.AstNode
import com.halilibo.richtext.ui.CodeBlockStyle
import com.halilibo.richtext.ui.ListStyle
import com.halilibo.richtext.ui.RichTextStyle
import com.halilibo.richtext.ui.TableStyle
import com.halilibo.richtext.ui.material3.RichText
import com.halilibo.richtext.ui.string.RichTextStringStyle
import com.movtery.zalithlauncher.ui.theme.cardColor

@Composable
fun MarkdownView(
    content: String,
    modifier: Modifier = Modifier,
    richTextStyle: RichTextStyle = defaultRichTextStyle(),
) {
    RichText(
        modifier = modifier,
        style = richTextStyle
    ) {
        Markdown(content = content)
    }
}

@Composable
fun MarkdownView(
    node: AstNode,
    modifier: Modifier = Modifier,
    astBlockNodeComposer: AstBlockNodeComposer? = null,
    richTextStyle: RichTextStyle = defaultRichTextStyle(),
) {
    RichText(
        modifier = modifier,
        style = richTextStyle
    ) {
        BasicMarkdown(
            astNode = node,
            astBlockNodeComposer = astBlockNodeComposer
        )
    }
}

@Composable
fun defaultRichTextStyle(
    influencedByBackground: Boolean = true,
    codeBackground: Color = cardColor(influencedByBackground),
    headingColor: Color = MaterialTheme.colorScheme.primary,
    tableColor: Color = MaterialTheme.colorScheme.outlineVariant,
    linkColor: Color = MaterialTheme.colorScheme.secondary,
    linkBackgroundColor: Color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f),
): RichTextStyle {
    return RichTextStyle(
        headingStyle = { level, textStyle ->
            when (level) {
                0 -> TextStyle(
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Bold,
                    color = headingColor,
                )
                1 -> TextStyle(
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = headingColor,
                )
                2 -> TextStyle(
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = headingColor,
                )
                3 -> TextStyle(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = headingColor,
                )
                4 -> TextStyle(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = headingColor,
                )
                5 -> TextStyle(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = headingColor,
                )
                else -> textStyle
            }
        },
        listStyle = ListStyle(

        ),
        codeBlockStyle = CodeBlockStyle(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = codeBackground,
                    shape = MaterialTheme.shapes.small
                ).horizontalScroll(
                    state = rememberScrollState()
                ),
            textStyle = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
            padding = 8.sp
        ),
        tableStyle = TableStyle(
            borderColor = tableColor,
        ),
        stringStyle = RichTextStringStyle(
            linkStyle = TextLinkStyles(
                style = SpanStyle(
                    color = linkColor,
                    textDecoration = TextDecoration.Underline,
                    fontWeight = FontWeight.Bold
                ),
                pressedStyle = SpanStyle(
                    color = linkColor,
                    background = linkBackgroundColor,
                    textDecoration = TextDecoration.Underline,
                    fontWeight = FontWeight.Bold
                )
            ),
            codeStyle = SpanStyle(
                fontFamily = FontFamily.Monospace,
                background = codeBackground
            )
        )
    )
}
