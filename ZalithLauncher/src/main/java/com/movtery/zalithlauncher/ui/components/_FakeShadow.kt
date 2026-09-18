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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun FakeShadowUp(
    modifier: Modifier = Modifier,
    height: Dp = 4.dp
) {
    Box(
        modifier = modifier
            .height(height)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color(0x3A000000),
                    )
                )
            )
    )
}

@Composable
fun FakeShadowDown(
    modifier: Modifier = Modifier,
    height: Dp = 4.dp
) {
    Box(
        modifier = modifier
            .height(height)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0x3A000000),
                        Color.Transparent,
                    )
                )
            )
    )
}

@Composable
fun FakeShadowLeft(
    modifier: Modifier = Modifier,
    width: Dp = 4.dp
) {
    Box(
        modifier = modifier
            .width(width)
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color(0x1A000000),
                        Color.Transparent,
                    )
                )
            )
    )
}

@Composable
fun FakeShadowRight(
    modifier: Modifier = Modifier,
    width: Dp = 4.dp
) {
    Box(
        modifier = modifier
            .width(width)
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color(0x1A000000),
                    )
                )
            )
    )
}

@Composable
@Preview(showBackground = true)
private fun PreviewFakeShadows() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        FakeShadowUp(
            modifier = Modifier.fillMaxWidth()
        )
        FakeShadowDown(
            modifier = Modifier.fillMaxWidth()
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            FakeShadowLeft(
                modifier = Modifier.fillMaxHeight()
            )
            FakeShadowRight(
                modifier = Modifier.fillMaxHeight()
            )
        }
    }
}