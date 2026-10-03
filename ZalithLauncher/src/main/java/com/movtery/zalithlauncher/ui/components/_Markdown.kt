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
import com.halilibo.richtext.ui.CodeBlockStyle
import com.halilibo.richtext.ui.ListStyle
import com.halilibo.richtext.ui.RichTextStyle
import com.halilibo.richtext.ui.TableStyle
import com.halilibo.richtext.ui.material3.RichText
import com.halilibo.richtext.ui.string.RichTextStringStyle
import com.movtery.zalithlauncher.ui.theme.cardColor

/**
 * Renders a Markdown [content] string using the halilibo richtext library.
 */
@Composable
fun MarkdownView(
    content: String,
    modifier: Modifier = Modifier,
    richTextStyle: RichTextStyle = defaultRichTextStyle(),
) {
    RichText(
        modifier = modifier,
        style = richTextStyle,
    ) {
        Markdown(content)
    }
}

/**
 * Returns the default [RichTextStyle] used by [MarkdownView].
 */
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
        listStyle = ListStyle(),
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
