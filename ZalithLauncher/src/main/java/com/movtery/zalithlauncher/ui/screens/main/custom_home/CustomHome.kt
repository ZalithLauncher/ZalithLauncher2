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

package com.movtery.zalithlauncher.ui.screens.main.custom_home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.halilibo.richtext.markdown.node.AstNode
import com.halilibo.richtext.ui.RichTextStyle
import com.movtery.zalithlauncher.ui.components.MarkdownView
import com.movtery.zalithlauncher.ui.components.defaultRichTextStyle
import com.movtery.zalithlauncher.ui.theme.itemColor
import kotlin.random.Random

fun LazyListScope.customHomePage(
    blocks: List<MarkdownBlock>,
    richTextStyle: RichTextStyle,
    onEvent: (MarkdownBlock.Button.Event) -> Unit = {}
) {
    itemsIndexed(
        items = blocks,
        key = { index, it -> "${it.stableKey}_$index" },
        contentType = { _, it -> it::class }
    ) { _, block ->
        BlockItem(
            block = block,
            richTextStyle = richTextStyle,
            onEvent = onEvent
        )
    }
}

@Composable
private fun MarkdownInnerRenderer(
    blocks: List<MarkdownBlock>,
    modifier: Modifier = Modifier,
    richTextStyle: RichTextStyle = defaultRichTextStyle(),
    onEvent: (MarkdownBlock.Button.Event) -> Unit
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        blocks.forEachIndexed { index, block ->
            key("${block.stableKey}_$index") {
                BlockItem(
                    block = block,
                    richTextStyle = richTextStyle,
                    onEvent = onEvent
                )
            }
        }
    }
}

@Composable
private fun BlockItem(
    block: MarkdownBlock,
    modifier: Modifier = Modifier,
    isInsideFlex: Boolean = false,
    richTextStyle: RichTextStyle = defaultRichTextStyle(),
    onEvent: (MarkdownBlock.Button.Event) -> Unit
) {
    val weight = block.weight
    val widthInfo = block.width

    val isContentComponent = block is MarkdownBlock.Button || block is MarkdownBlock.Image

    val finalModifier = if (weight != null && isInsideFlex) {
        modifier
    } else if (widthInfo != null) {
        when (widthInfo) {
            is MarkdownBlock.Width.DP -> Modifier.width(widthInfo.value)
            is MarkdownBlock.Width.Percent -> Modifier.fillMaxWidth(widthInfo.value)
        }
    } else if (!isContentComponent) {
        //布局组件、普通文本在根布局或容器布局中，且未配置 width/weight 时默认填充宽度
        Modifier.fillMaxWidth()
    } else {
        modifier
    }

    when (block) {
        is MarkdownBlock.Normal -> {
            MarkdownView(
                node = block.astNode,
                modifier = finalModifier,
                richTextStyle = richTextStyle
            )
        }
        is MarkdownBlock.Card -> {
            CustomHomeCard(
                modifier = finalModifier,
                title = block.title,
                shape = block.shape?.toShape(),
                contentPadding = block.contentPadding
            ) {
                MarkdownInnerRenderer(
                    blocks = block.content,
                    richTextStyle = defaultRichTextStyle(
                        influencedByBackground = false,
                        codeBackground = itemColor(false)
                    ),
                    onEvent = onEvent
                )
            }
        }
        is MarkdownBlock.Button -> {
            CustomHomeButton(
                modifier = finalModifier,
                text = block.text,
                event = block.event,
                type = block.style,
                shape = block.shape?.toShape(),
                onEvent = onEvent
            )
        }
        is MarkdownBlock.Image -> {
            CustomHomeImage(
                modifier = finalModifier,
                url = block.url,
                shape = block.shape?.toShape()
            )
        }
        is MarkdownBlock.RowBlock -> {
            Row(
                horizontalArrangement = block.horizontal,
                verticalAlignment = block.vertical,
                modifier = finalModifier
            ) {
                block.children.forEach { child ->
                    val childWeight = child.weight
                    val childModifier = if (childWeight != null) {
                        Modifier.weight(childWeight.first, childWeight.second)
                    } else {
                        Modifier
                    }
                    BlockItem(
                        block = child,
                        modifier = childModifier,
                        isInsideFlex = true,
                        richTextStyle = richTextStyle,
                        onEvent = onEvent
                    )
                }
            }
        }
        is MarkdownBlock.ColumnBlock -> {
            Column(
                horizontalAlignment = block.horizontal,
                verticalArrangement = block.vertical,
                modifier = finalModifier
            ) {
                block.children.forEach { child ->
                    BlockItem(
                        block = child,
                        isInsideFlex = true,
                        richTextStyle = richTextStyle,
                        onEvent = onEvent
                    )
                }
            }
        }
    }
}


sealed interface MarkdownBlock {
    val stableKey: Any
    val params: String
    val width: Width?
    val weight: Pair<Float, Boolean>?  //Modifier.weight(value, fill)

    sealed interface Width {
        data class Percent(val value: Float) : Width
        data class DP(val value: Dp) : Width
    }

    /**
     * 普通的Markdown内容
     */
    data class Normal(val astNode: AstNode) : MarkdownBlock {
        override val stableKey: Any get() = astNode.hashCode()
        override val params: String get() = ""
        override val width: Width? = null
        override val weight: Pair<Float, Boolean>? = null
    }

    /**
     * 一个预设好的Card组件
     * @param title 卡片的标题，必须存在，如果字符串内容不为空，则正常显示标题，如果为空，则不显示标题
     * @param params 卡片的参数字符串
     * @param content 卡片内部的组件
     * @param shape 预解析的形状规格
     * @param contentPadding 预解析的内边距
     */
    data class Card(
        val title: String,
        override val params: String,
        val content: List<MarkdownBlock>,
        val shape: ShapeSpec?,
        val contentPadding: PaddingValues?
    ) : MarkdownBlock {
        override val stableKey: Any get() = "card_${title}_${params.hashCode()}_${content.hashCode()}_${shape}_${contentPadding}"
        override val width: Width? = null
        override val weight: Pair<Float, Boolean>? = null
    }

    /**
     * 一个Button组件
     * @param text 必须携带的文本内容组件，按钮的文本内容
     * @param event 可选的按钮执行事件组件
     * @param style 该按钮的样式
     * @param shape 预解析的形状规格
     */
    data class Button(
        val text: String,
        val event: Event?,
        val style: HomeButtonType,
        val shape: ShapeSpec?,
        override val width: Width?,
        override val params: String,
        override val weight: Pair<Float, Boolean>?
    ) : MarkdownBlock {
        override val stableKey: Any get() = "btn_${text}_${event}_${style}_${width}_${params.hashCode()}_$weight"

        /**
         * 按钮事件
         */
        data class Event(
            val key: String,
            val data: String? = null
        )
    }

    /**
     * 图片组件
     * @param url 必须携带的图片链接
     * @param width 可选的宽度属性
     * @param shape 预解析的形状规格
     */
    data class Image(
        val url: String,
        override val width: Width?,
        override val params: String,
        val shape: ShapeSpec?,
        override val weight: Pair<Float, Boolean>?
    ) : MarkdownBlock {
        override val stableKey: Any get() = "img_${url}_width=${width}_${params.hashCode()}_${shape}_$weight"
    }

    /**
     * Row组件，和Compose原生的Row一致
     */
    data class RowBlock(
        val horizontal: Arrangement.Horizontal,
        val vertical: Alignment.Vertical,
        val children: List<MarkdownBlock>,
        override val width: Width?,
        override val params: String,
        override val weight: Pair<Float, Boolean>?
    ) : MarkdownBlock {
        override val stableKey: Any get() = "row_${width}_${params.hashCode()}_${children.hashCode()}_$weight"
    }

    /**
     * Column组件，和Compose原生的Column一致
     */
    data class ColumnBlock(
        val horizontal: Alignment.Horizontal,
        val vertical: Arrangement.Vertical,
        val children: List<MarkdownBlock>,
        override val width: Width?,
        override val params: String,
        override val weight: Pair<Float, Boolean>?
    ) : MarkdownBlock {
        override val stableKey: Any get() = "col_${width}_${params.hashCode()}_${children.hashCode()}_$weight"
    }
}

/**
 * 形状规格，在解析阶段预定义形状
 */
sealed interface ShapeSpec {
    /**
     * [androidx.compose.material3.ShapeDefaults.ExtraSmall]
     */
    object ExtraSmall : ShapeSpec
    /**
     * [androidx.compose.material3.ShapeDefaults.Small]
     */
    object Small : ShapeSpec
    /**
     * [androidx.compose.material3.ShapeDefaults.Medium]
     */
    object Medium : ShapeSpec
    /**
     * [androidx.compose.material3.ShapeDefaults.Large]
     */
    object Large : ShapeSpec
    /**
     * [androidx.compose.material3.ShapeDefaults.ExtraLarge]
     */
    object ExtraLarge : ShapeSpec
    data class RoundedCornerDP(val size: Dp) : ShapeSpec
    data class RoundedCornerPercent(val percent: Int) : ShapeSpec
}

/**
 * 将形状规格转换为实际的 Compose Shape
 */
@Composable
fun ShapeSpec.toShape(): Shape = when (this) {
    ShapeSpec.ExtraSmall -> MaterialTheme.shapes.extraSmall
    ShapeSpec.Small -> MaterialTheme.shapes.small
    ShapeSpec.Medium -> MaterialTheme.shapes.medium
    ShapeSpec.Large -> MaterialTheme.shapes.large
    ShapeSpec.ExtraLarge -> MaterialTheme.shapes.extraLarge
    is ShapeSpec.RoundedCornerDP -> RoundedCornerShape(size)
    is ShapeSpec.RoundedCornerPercent -> RoundedCornerShape(percent)
}



private val blockPattern = Regex(
    """^[ \t]*(?:(\.\.\.card-start([^\n]*))|(\.\.\.button(-outlined|-filled-tonal|-text)?\s+([^\n]*))|(\.\.\.row-start([^\n]*))|(\.\.\.column-start([^\n]*))|(\.\.\.image\s+([^\n]*)))""",
    RegexOption.MULTILINE
)

/**
 * 由于扩展组件相对独立，需要将其拆分出来，作为单独的部分进行渲染，
 * 这里会把内容进行拆分成多个块
 */
fun parseMarkdownBlocks(
    content: String,
    parseMarkdown: (String) -> AstNode,
): List<MarkdownBlock> {
    var inCodeBlock = false
    val cleaned = content.lineSequence()
        .filter { line ->
            val trimmed = line.trimStart()
            if (trimmed.startsWith("```")) {
                inCodeBlock = !inCodeBlock
                return@filter true
            }
            if (inCodeBlock) return@filter true
            //清洗掉在代码框之外的注释行
            !trimmed.startsWith("//")
        }
        .joinToString("\n")
        .replace(Regex("<br\\s*/?>", RegexOption.IGNORE_CASE), "\u2028")

    return parseMarkdownBlocksInternal(
        cleared = cleaned,
        parseMarkdown = parseMarkdown
    )
}


private val titleRegex = Regex("""title\s*=\s*"([^"]*)"""")
private fun parseMarkdownBlocksInternal(
    cleared: String,
    parseMarkdown: (String) -> AstNode,
    allowCard: Boolean = true,
): List<MarkdownBlock> {
    val blocks = mutableListOf<MarkdownBlock>()

    var lastIndex = 0
    while (lastIndex < cleared.length) {
        val match = blockPattern.find(cleared, lastIndex)
        if (match == null) {
            //没有更多匹配，添加剩余内容
            val remaining = cleared.substring(lastIndex).trim('\n')
            val processedRemaining = processRandomBlocksInText(remaining)
            if (processedRemaining.isNotEmpty()) {
                blocks.add(MarkdownBlock.Normal(astNode = parseMarkdown(processedRemaining)))
            }
            break
        }

        //处理匹配项之前的普通文本
        if (match.range.first > lastIndex) {
            val text = cleared.substring(lastIndex, match.range.first).trim('\n')
            val processedText = processRandomBlocksInText(text)
            if (processedText.isNotEmpty()) {
                blocks.add(MarkdownBlock.Normal(astNode = parseMarkdown(processedText)))
            }
        }

        val isCardStart = match.groupValues[1].isNotEmpty()
        val isButton = match.groupValues[3].isNotEmpty()
        val isRowStart = match.groupValues[6].isNotEmpty()
        val isColumnStart = match.groupValues[8].isNotEmpty()
        val isImage = match.groupValues[10].isNotEmpty()

        when {
            isCardStart && allowCard -> {
                val params = match.groupValues[2]
                val title = titleRegex.find(params)?.groupValues?.get(1) ?: ""
                //寻找对应的卡片闭合标签
                val closingRange = findNestedClosingTag(
                    content = cleared,
                    startIndex = match.range.last + 1, //这里是为了防止嵌套行为，动态添加闭合范围
                    openTagPattern = """\.\.\.card-start""",
                    closeTag = "...card-end"
                )

                if (closingRange != null) {
                    val innerContent = cleared.substring(match.range.last + 1, closingRange.first).trim('\n')
                    blocks.add(
                        MarkdownBlock.Card(
                            title = title,
                            params = params,
                            content = parseMarkdownBlocksInternal(
                                cleared = innerContent,
                                parseMarkdown = parseMarkdown,
                                allowCard = false, //不允许内部嵌套卡片组件
                            ),
                            shape = parseShape(params),
                            contentPadding = parseCardPadding(params)
                        )
                    )
                    lastIndex = closingRange.last + 1
                } else {
                    //如果没找到匹配的结束标记，则将此开始标记视为Markdown
                    blocks.add(
                        MarkdownBlock.Normal(
                            astNode = parseMarkdown(match.value)
                        )
                    )
                    lastIndex = match.range.last + 1
                }
            }

            isRowStart -> {
                val params = match.groupValues[7]
                // 寻找对应的Row闭合标签，支持嵌套
                val closingRange = findNestedClosingTag(
                    content = cleared,
                    startIndex = match.range.last + 1,
                    openTagPattern = """\.\.\.row-start""",
                    closeTag = "...row-end"
                )
                if (closingRange != null) {
                    val innerContent = cleared.substring(
                        startIndex = match.range.last + 1,
                        endIndex = closingRange.first
                    ).trim('\n')

                    val children = parseMarkdownBlocksInternal(
                        cleared = innerContent,
                        parseMarkdown = parseMarkdown,
                        allowCard = false, //布局组件内不允许放卡片
                    )

                    blocks.add(
                        MarkdownBlock.RowBlock(
                            horizontal = parseHorizontalArrangement(params),
                            vertical = parseVerticalAlignment(params),
                            children = children,
                            width = parseWidth(params),
                            params = params,
                            weight = parseWeight(params)
                        )
                    )
                    lastIndex = closingRange.last + 1
                } else {
                    blocks.add(
                        MarkdownBlock.Normal(
                            astNode = parseMarkdown(match.value)
                        )
                    )
                    lastIndex = match.range.last + 1
                }
            }

            isColumnStart -> {
                val params = match.groupValues[9]
                //寻找对应的Column闭合标签，支持嵌套
                val closingRange = findNestedClosingTag(
                    content = cleared,
                    startIndex = match.range.last + 1,
                    openTagPattern = """\.\.\.column-start""",
                    closeTag = "...column-end"
                )
                if (closingRange != null) {
                    val innerContent = cleared.substring(match.range.last + 1, closingRange.first).trim('\n')
                    val children = parseMarkdownBlocksInternal(
                        cleared = innerContent,
                        parseMarkdown = parseMarkdown,
                        allowCard = false, //布局组件内不允许放卡片
                    )

                    blocks.add(
                        MarkdownBlock.ColumnBlock(
                            horizontal = parseHorizontalAlignment(params),
                            vertical = parseVerticalArrangement(params),
                            children = children,
                            width = parseWidth(params),
                            params = params,
                            weight = parseWeight(params)
                        )
                    )
                    lastIndex = closingRange.last + 1
                } else {
                    blocks.add(
                        MarkdownBlock.Normal(
                            astNode = parseMarkdown(match.value)
                        )
                    )
                    lastIndex = match.range.last + 1
                }
            }

            isButton -> {
                parseButton(
                    styleSuffix = match.groupValues[4],
                    params = match.groupValues[5]
                )?.let { button ->
                    blocks.add(button)
                }
                lastIndex = match.range.last + 1
            }

            isImage -> {
                parseImage(match.groupValues[11])?.let { image ->
                    blocks.add(image)
                }

                lastIndex = match.range.last + 1
            }

            else -> {
                //如果是标记但当前上下文不允许（比如卡片嵌套），则视为普通Markdown
                blocks.add(
                    MarkdownBlock.Normal(
                        astNode = parseMarkdown(match.value)
                    )
                )
                lastIndex = match.range.last + 1
            }
        }
    }

    return blocks
}

/**
 * 寻找嵌套结构的闭合标记
 * @param content 完整内容
 * @param startIndex 开始寻找的位置
 * @param openTagPattern 开始标记的正则模式
 * @param closeTag 结束标记的字符串
 * @return 结束标记的范围，如果未找到则返回 null
 */
private fun findNestedClosingTag(
    content: String,
    startIndex: Int,
    openTagPattern: String,
    closeTag: String
): IntRange? {
    var depth = 1
    var current = startIndex
    val pattern = Regex("(?m)^[ \t]*(?:($openTagPattern)|(${Regex.escape(closeTag)}))")

    while (current < content.length) {
        val match = pattern.find(content, current) ?: return null
        if (match.groupValues[1].isNotEmpty()) {
            depth++
        } else {
            depth--
        }
        if (depth == 0) return match.range
        current = match.range.last + 1
    }
    return null
}




private val buttonTextRegex = Regex("""text\s*=\s*"([^"]*)"""")
private val buttonEventRegex = Regex("""event\s*=\s*"([^"]*)"""")
private val buttonEventDataRegex = Regex("""^([^\s{]+)(?:\s*\{([\s\S]*)\})?$""")
private fun parseButton(
    styleSuffix: String,
    params: String
): MarkdownBlock.Button? {
    val style = when (styleSuffix) {
        "-outlined" -> HomeButtonType.Outlined
        "-filled-tonal" -> HomeButtonType.FilledTonal
        "-text" -> HomeButtonType.Text
        else -> HomeButtonType.Filled
    }
    val text = buttonTextRegex.find(params)?.groupValues?.get(1) ?: return null
    val eventValue = buttonEventRegex.find(params)?.groupValues?.get(1)
    val event = eventValue?.let { eventValue0 ->
        val value = buttonEventDataRegex.find(eventValue0) ?: return@let null
        val eventKey = value.groupValues.getOrNull(1) ?: return@let null
        val eventData = value.groupValues.getOrNull(2)?.takeIf { it.isNotEmpty() }
        MarkdownBlock.Button.Event(eventKey, eventData)
    }
    return MarkdownBlock.Button(
        text = text,
        event = event,
        style = style,
        shape = parseShape(params),
        width = parseWidth(params),
        params = params,
        weight = parseWeight(params)
    )
}

private val horizontalRegex = Regex("""horizontal\s*=\s*(spacedBy\([^)]*\)|[^\s\t\n]+)""")
private val spacedByRegex = Regex("""spacedBy\(\s*(\d+(?:\.\d+)?)\s*(?:,\s*(\w+))?\s*\)""")

private fun parseHorizontalArrangement(params: String): Arrangement.Horizontal {
    val horizontalValue = horizontalRegex.find(params)?.groupValues?.get(1)?.trim() ?: return Arrangement.Start
    spacedByRegex.find(horizontalValue)?.let { match ->
        val space = match.groupValues[1].toFloatOrNull() ?: 0f
        val alignment = when (match.groupValues[2]) {
            "Start" -> Alignment.Start
            "End" -> Alignment.End
            "Center", "CenterHorizontally" -> Alignment.CenterHorizontally
            else -> null
        }
        return if (alignment != null) {
            Arrangement.spacedBy(space.dp, alignment)
        } else {
            Arrangement.spacedBy(space.dp)
        }
    }
    return when (horizontalValue) {
        "Center", "CenterHorizontally" -> Arrangement.Center
        "End" -> Arrangement.End
        "SpaceEvenly" -> Arrangement.SpaceEvenly
        "SpaceBetween" -> Arrangement.SpaceBetween
        "SpaceAround" -> Arrangement.SpaceAround
        else -> Arrangement.Start
    }
}

private fun parseVerticalArrangement(params: String): Arrangement.Vertical {
    val verticalValue = verticalRegex.find(params)?.groupValues?.get(1)?.trim() ?: return Arrangement.Top
    spacedByRegex.find(verticalValue)?.let { match ->
        val space = match.groupValues[1].toFloatOrNull() ?: 0f
        val alignment = when (match.groupValues[2]) {
            "Top" -> Alignment.Top
            "Bottom" -> Alignment.Bottom
            "Center", "CenterVertically" -> Alignment.CenterVertically
            else -> null
        }
        return if (alignment != null) {
            Arrangement.spacedBy(space.dp, alignment)
        } else {
            Arrangement.spacedBy(space.dp)
        }
    }
    return when (verticalValue) {
        "Center", "CenterVertically" -> Arrangement.Center
        "Bottom" -> Arrangement.Bottom
        "SpaceEvenly" -> Arrangement.SpaceEvenly
        "SpaceBetween" -> Arrangement.SpaceBetween
        "SpaceAround" -> Arrangement.SpaceAround
        else -> Arrangement.Top
    }
}

private val verticalRegex = Regex("""vertical\s*=\s*(spacedBy\([^)]*\)|[^\s\t\n]+)""")
private fun parseVerticalAlignment(params: String): Alignment.Vertical {
    return when (verticalRegex.find(params)?.groupValues?.get(1)) {
        "Center", "CenterVertically" -> Alignment.CenterVertically
        "Bottom" -> Alignment.Bottom
        else -> Alignment.Top
    }
}

private fun parseHorizontalAlignment(params: String): Alignment.Horizontal {
    return when (horizontalRegex.find(params)?.groupValues?.get(1)) {
        "Center", "CenterHorizontally" -> Alignment.CenterHorizontally
        "End" -> Alignment.End
        else -> Alignment.Start
    }
}

private val shapeRegex = Regex("""shape\s*=\s*([^\s\t\n]+)""")
private fun parseShape(params: String): ShapeSpec? {
    val shapeValue = shapeRegex.find(params)?.groupValues?.get(1) ?: return null
    return when (shapeValue) {
        "extraSmall" -> ShapeSpec.ExtraSmall
        "small" -> ShapeSpec.Small
        "medium" -> ShapeSpec.Medium
        "large" -> ShapeSpec.Large
        "extraLarge" -> ShapeSpec.ExtraLarge
        else -> {
            when {
                shapeValue.endsWith("dp") -> {
                    val size = shapeValue.dropLast(2).toFloatOrNull() ?: return null
                    ShapeSpec.RoundedCornerDP(size.dp)
                }
                shapeValue.endsWith("%") -> {
                    val percent = shapeValue.dropLast(1).toIntOrNull() ?: return null
                    ShapeSpec.RoundedCornerPercent(percent)
                }
                else -> null
            }
        }
    }
}

private val paddingRegex = Regex("""contentPadding\s*=\s*\(([\d.\s,]+)\)""")
private fun parseCardPadding(params: String): PaddingValues? {
    val match = paddingRegex.find(params) ?: return null
    val values = match.groupValues[1]
        .split(",")
        .mapNotNull { it.trim().toFloatOrNull() }
    return when (values.size) {
        1 -> PaddingValues(
            all = values[0].dp
        )
        2 -> PaddingValues(
            horizontal = values[0].dp,
            vertical = values[1].dp
        )
        4 -> PaddingValues(
            start = values[0].dp,
            top = values[1].dp,
            end = values[2].dp,
            bottom = values[3].dp
        )
        else -> null
    }
}

private val urlRegex = Regex("""url\s*=\s*"([^"]*)"""")
private val widthRegex = Regex("""width\s*=\s*(\d+%|\d+(?:\.\d+)?dp)""")
private fun parseWidth(params: String): MarkdownBlock.Width? {
    return widthRegex.find(params)?.groupValues?.get(1)?.let { w ->
        when {
            w.endsWith("%") -> {
                val percent = w.dropLast(1).toIntOrNull() ?: 100
                MarkdownBlock.Width.Percent((percent.toFloat() / 100f).coerceIn(0f, 1f))
            }
            w.endsWith("dp") -> {
                val value = w.dropLast(2).toFloatOrNull() ?: 0f
                MarkdownBlock.Width.DP(value.dp)
            }
            else -> null
        }
    }
}

private fun parseImage(params: String): MarkdownBlock.Image? {
    val url = urlRegex.find(params)?.groupValues?.get(1) ?: return null
    return MarkdownBlock.Image(
        url = url,
        width = parseWidth(params),
        params = params,
        shape = parseShape(params),
        weight = parseWeight(params)
    )
}

private val weightRegex = Regex("""weight\s*(?:=\s*)?\(([\d.]+)(?:,\s*(noFill))?\)""")
private fun parseWeight(params: String): Pair<Float, Boolean>? {
    val match = weightRegex.find(params) ?: return null
    val weight = match.groupValues[1].toFloatOrNull() ?: return null
    val fill = match.groupValues[2] != "noFill"
    return Pair(weight, fill)
}

private const val randomStartTag = "...random-start"
private const val randomEndTag = "...random-end"
private val randomOptionRegex = Regex("""^weight\((\d+(?:\.\d+)?)\)\s*:\s*(.*)$""")

private data class RandomOption(
    val text: String,
    val weight: Double
)

/**
 * 处理文本中的随机文本块，自动将其替换为随机选中的文本
 * 语法：
 * ```
 * ...random-start
 * weight(N): 文本内容
 * 默认权重的文本内容
 * ...random-end
 * ```
 * - 以 `weight(N): ` 开头可指定权重，否则权重默认为 1
 * - 随机抽取后的结果中不包含换行符
 */
private fun processRandomBlocksInText(text: String): String {
    if (!text.contains(randomStartTag)) return text

    val lines = text.lines()
    val result = mutableListOf<String>()
    var inRandomBlock = false
    var randomStartLine: String? = null
    val randomLines = mutableListOf<String>()

    for (line in lines) {
        val trimmed = line.trimStart()

        if (inRandomBlock) {
            if (trimmed.startsWith(randomEndTag)) {
                val selected = pickRandomText(randomLines)
                result.add(selected)
                randomLines.clear()
                inRandomBlock = false
                randomStartLine = null
            } else {
                randomLines.add(line)
            }
        } else {
            if (trimmed.startsWith(randomStartTag)) {
                inRandomBlock = true
                randomStartLine = line
            } else {
                result.add(line)
            }
        }
    }

    //如果文本结束但随机块未闭合，则忽略此随机文本块
    if (inRandomBlock) {
        randomStartLine?.let { result.add(it) }
        result.addAll(randomLines)
    }

    return result.joinToString("\n")
}

/**
 * 从随机文本候选项中根据权重抽取一个文本
 */
private fun pickRandomText(lines: List<String>): String {
    val options = lines.mapNotNull { line ->
        val trimmed = line.trim()
        if (trimmed.isEmpty()) return@mapNotNull null

        //读取权重值
        val match = randomOptionRegex.find(trimmed)
        if (match != null) {
            val weight = match.groupValues[1].toDoubleOrNull() ?: 1.0
            val text = match.groupValues[2]
            RandomOption(text, weight)
        } else {
            //未配置权重时，默认为 1
            RandomOption(trimmed, 1.0)
        }
    }

    if (options.isEmpty()) return ""

    val totalWeight = options.sumOf { it.weight }
    var randomValue = Random.nextDouble() * totalWeight

    for (option in options) {
        randomValue -= option.weight
        if (randomValue <= 0) {
            return option.text
        }
    }

    return options.last().text
}