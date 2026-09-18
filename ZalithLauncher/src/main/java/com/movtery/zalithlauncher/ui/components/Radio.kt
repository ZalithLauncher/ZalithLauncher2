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

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonColors
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.movtery.zalithlauncher.ui.theme.itemColor
import com.movtery.zalithlauncher.ui.theme.onItemColor

@Composable
fun RadioCard(
    selected: Boolean,
    text: String,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    color: Color = itemColor(false),
    contentColor: Color = onItemColor(),
    radioColors: RadioButtonColors = RadioButtonDefaults.colors(),
    interactionSource: MutableInteractionSource? = null,
    textStyle: TextStyle = MaterialTheme.typography.bodyMedium,
) {
    RadioCard(
        selected = selected,
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        color = color,
        contentColor = contentColor,
        radioColors = radioColors,
        interactionSource = interactionSource
    ) {
        Text(
            text = text,
            style = textStyle
        )
    }
}

@Composable
fun RadioCard(
    selected: Boolean,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    color: Color = itemColor(false),
    contentColor: Color = onItemColor(),
    shape: Shape = MaterialTheme.shapes.large,
    radioColors: RadioButtonColors = RadioButtonDefaults.colors(),
    interactionSource: MutableInteractionSource? = null,
    content: @Composable RowScope.() -> Unit,
) {
    InternalSurface(
        modifier = modifier,
        color = color,
        contentColor = contentColor,
        shape = shape,
        onClick = onClick,
        interactionSource = interactionSource,
        enabled = enabled
    ) {
        Row(
            modifier = Modifier.padding(end = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = selected,
                onClick = onClick,
                colors = radioColors,
                interactionSource = interactionSource
            )

            content()
        }
    }
}

@Composable
private fun InternalSurface(
    color: Color,
    contentColor: Color,
    shape: Shape,
    onClick: (() -> Unit)?,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    interactionSource: MutableInteractionSource? = null,
    content: @Composable () -> Unit
) {
    if (onClick != null) {
        Surface(
            modifier = modifier,
            color = color,
            contentColor = contentColor,
            shape = shape,
            onClick = onClick,
            enabled = enabled,
            interactionSource = interactionSource,
            content = content
        )
    } else {
        Surface(
            modifier = modifier,
            color = color,
            contentColor = contentColor,
            shape = shape,
            content = content
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewRadioCard() {
    MaterialExpressiveTheme {
        Column(
            modifier = Modifier.padding(all = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            RadioCard(
                selected = true,
                text = "Selected",
                onClick = {}
            )
            RadioCard(
                selected = false,
                text = "Unselected",
                onClick = {}
            )
        }
    }
}