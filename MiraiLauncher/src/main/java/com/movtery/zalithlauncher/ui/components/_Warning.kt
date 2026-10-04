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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.setting.AllSettings
import com.movtery.zalithlauncher.ui.screens.content.settings.layouts.CardPosition
import com.movtery.zalithlauncher.ui.screens.content.settings.layouts.rememberSettingsCardShape

/**
 * 一个简单的警告/提示卡片，用于将信息以醒目的方式展示给用户看
 * @param title 这个卡片的标题
 * @param text 这个卡片的实际内容
 * @param position 卡片在UI组中的位置，用于控制卡片的四个角的圆角度
 * @param influencedByBackground 背景颜色是否受到启动器自定义背景影响
 */
@Composable
fun WarningCard(
    title: String,
    text: @Composable ColumnScope.() -> Unit,
    modifier: Modifier = Modifier,
    icon: (@Composable (innerModifier: Modifier) -> Unit) = @Composable { innerModifier ->
        Icon(
            modifier = innerModifier,
            painter = painterResource(R.drawable.ic_warning_filled),
            contentDescription = title
        )
    },
    position: CardPosition = CardPosition.Single,
    outerShapeSize: Dp = 12.dp,
    innerShapeSize: Dp = 4.dp,
    influencedByBackground: Boolean = true,
    blur: Int = AllSettings.backgroundBlur.state,
    containerColor: Color = influencedByBackgroundColor(
        color = MaterialTheme.colorScheme.secondaryContainer,
        enabled = influencedByBackground
    ),
    contentColor: Color = MaterialTheme.colorScheme.onSecondaryContainer
) {
    val cardShape = rememberSettingsCardShape(
        position = position,
        outerShape = outerShapeSize,
        innerShape = innerShapeSize
    )

    BackgroundCard(
        modifier = modifier,
        shape = cardShape,
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        blur = blur,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(all = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            //标题部分
            Row(
                modifier = Modifier
                    .height(IntrinsicSize.Min)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                icon(
                    Modifier
                        .fillMaxHeight()
                        .aspectRatio(1f)
                        .padding(vertical = 2.dp)
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium
                )
            }

            //实际自定义的警告内容部分
            Column(
                modifier = Modifier.fillMaxWidth(),
                content = text
            )
        }
    }
}