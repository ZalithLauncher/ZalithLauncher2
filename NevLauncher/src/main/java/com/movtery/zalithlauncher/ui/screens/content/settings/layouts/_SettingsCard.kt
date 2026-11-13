/*
 * Zalith Launcher 2
 * Copyright (C) 2025-2026 MovTery <movtery228@qq.com> and contributors
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

package com.movtery.zalithlauncher.ui.screens.content.settings.layouts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.movtery.zalithlauncher.ui.components.BackgroundCard
import com.movtery.zalithlauncher.ui.components.TitleAndSummary
import com.movtery.zalithlauncher.ui.theme.LocalNevStyle

/**
 * 根据卡片在UI组中的位置，选择不同的圆角形状
 */
enum class CardPosition {
    /**
     * 位于 UI 组的顶部
     * ``` txt
     *   _______
     *  +       +
     * |         |
     * |         |
     * |         |
     *  ---------
     * ```
     */
    Top,

    /**
     * 位于 UI 组的顶部左侧
     * ``` txt
     *   ________
     *  +        |
     * |         |
     * |         |
     * |         |
     *  ---------
     * ```
     */
    TopStart,

    /**
     * 位于 UI 组的顶部右侧
     * ``` txt
     *  ________
     * |        +
     * |         |
     * |         |
     * |         |
     *  ---------
     * ```
     */
    TopEnd,

    /**
     * 位于 UI 组的中部
     * ``` txt
     *  _________
     * |         |
     * |         |
     * |         |
     * |         |
     *  ---------
     * ```
     */
    Middle,

    /**
     * 位于 UI 组的底部
     * ``` txt
     *  _________
     * |         |
     * |         |
     * |         |
     *  +       +
     *   -------
     * ```
     */
    Bottom,

    /**
     * 位于 UI 组的底部左侧
     * ``` txt
     *  _________
     * |         |
     * |         |
     * |         |
     *  +        |
     *   --------
     * ```
     */
    BottomStart,

    /**
     * 位于 UI 组的底部右侧
     * ``` txt
     *  _________
     * |         |
     * |         |
     * |         |
     * |        +
     *  --------
     * ```
     */
    BottomEnd,

    /**
     * 单个 UI 组件
     * ``` txt
     *   _______
     *  +       +
     * |         |
     * |         |
     *  +       +
     *   -------
     * ```
     */
    Single
}

/**
 * 根据 UI 组件在组中的位置决定的形状
 */
@Composable
fun rememberSettingsCardShape(
    position: CardPosition,
    outerShape: Dp = 22.dp,
    innerShape: Dp = 18.dp
): Shape {
    val style = LocalNevStyle.current
    val outer = outerShape * style.cardOuterScale
    val inner = innerShape * style.cardInnerScale
    return remember(position, outer, inner) {
        when (position) {
            CardPosition.Top -> RoundedCornerShape(
                topStart = outer,
                topEnd = outer,
                bottomStart = inner,
                bottomEnd = inner
            )
            CardPosition.TopStart -> RoundedCornerShape(
                topStart = outer,
                topEnd = inner,
                bottomStart = inner,
                bottomEnd = inner
            )
            CardPosition.TopEnd -> RoundedCornerShape(
                topStart = inner,
                topEnd = outer,
                bottomStart = inner,
                bottomEnd = inner
            )
            CardPosition.Middle -> RoundedCornerShape(inner)
            CardPosition.Bottom -> RoundedCornerShape(
                topStart = inner,
                topEnd = inner,
                bottomStart = outer,
                bottomEnd = outer
            )
            CardPosition.BottomStart -> RoundedCornerShape(
                topStart = inner,
                topEnd = inner,
                bottomStart = outer,
                bottomEnd = inner
            )
            CardPosition.BottomEnd -> RoundedCornerShape(
                topStart = inner,
                topEnd = inner,
                bottomStart = inner,
                bottomEnd = outer
            )
            CardPosition.Single -> RoundedCornerShape(outer)
        }
    }
}

@Composable
fun SettingsCard(
    modifier: Modifier = Modifier,
    position: CardPosition,
    outerShape: Dp = 22.dp,
    innerShape: Dp = 18.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = rememberSettingsCardShape(position, outerShape, innerShape)

    BackgroundCard(
        modifier = modifier,
        shape = shape,
        content = content
    )
}

@Composable
fun SettingsCard(
    modifier: Modifier = Modifier,
    position: CardPosition,
    outerShape: Dp = 22.dp,
    innerShape: Dp = 18.dp,
    onClick: () -> Unit,
    enabled: Boolean = true,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = rememberSettingsCardShape(position, outerShape, innerShape)

    BackgroundCard(
        modifier = modifier,
        shape = shape,
        onClick = onClick,
        enabled = enabled,
        content = content
    )
}

@Composable
fun SettingsCard(
    position: CardPosition,
    title: String,
    modifier: Modifier = Modifier,
    summary: String? = null,
    titleStyle: TextStyle = MaterialTheme.typography.titleSmall,
    summaryStyle: TextStyle = MaterialTheme.typography.labelSmall,
    outerShape: Dp = 22.dp,
    innerShape: Dp = 18.dp,
    innerPadding: PaddingValues = PaddingValues(all = 16.dp),
    onClick: () -> Unit,
    trailingIcon: (@Composable RowScope.() -> Unit)? = null,
    enabled: Boolean = true
) {
    val shape = rememberSettingsCardShape(position, outerShape, innerShape)

    BackgroundCard(
        modifier = modifier,
        shape = shape,
        onClick = onClick,
        enabled = enabled
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(innerPadding),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TitleAndSummary(
                modifier = Modifier.weight(1f),
                title = title,
                summary = summary,
                titleStyle = titleStyle,
                summaryStyle = summaryStyle
            )
            trailingIcon?.let { trailing ->
                Row(
                    modifier = Modifier.align(Alignment.CenterVertically),
                    content = trailing
                )
            }
        }
    }
}

@Composable
fun SettingsCardColumn(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(10.dp),
        content = content
    )
}