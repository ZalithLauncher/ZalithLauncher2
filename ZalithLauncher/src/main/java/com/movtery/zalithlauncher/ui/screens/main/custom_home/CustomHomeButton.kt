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

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

enum class HomeButtonType {
    Filled, Outlined, FilledTonal, Text
}

/**
 * 自定义主页内的按钮组件
 */
@Composable
fun CustomHomeButton(
    text: String,
    event: MarkdownBlock.Button.Event?,
    type: HomeButtonType,
    onEvent: (MarkdownBlock.Button.Event) -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape? = null,
) {
    val onClick: () -> Unit = {
        event?.let { e ->
            onEvent(e)
        }
    }

    val content: @Composable (RowScope.() -> Unit) = @Composable {
        Text(text = text)
    }

    val buttonModifier = modifier.padding(vertical = 4.dp)
    val buttonShape = shape ?: ButtonDefaults.shape

    when (type) {
        HomeButtonType.Filled -> {
            Button(
                modifier = buttonModifier,
                onClick = onClick,
                shape = buttonShape,
                content = content
            )
        }
        HomeButtonType.Outlined -> {
            OutlinedButton(
                modifier = buttonModifier,
                onClick = onClick,
                shape = buttonShape,
                content = content
            )
        }
        HomeButtonType.FilledTonal -> {
            FilledTonalButton(
                modifier = buttonModifier,
                onClick = onClick,
                shape = buttonShape,
                content = content
            )
        }
        HomeButtonType.Text -> {
            TextButton(
                modifier = buttonModifier,
                onClick = onClick,
                shape = buttonShape,
                content = content
            )
        }
    }
}
