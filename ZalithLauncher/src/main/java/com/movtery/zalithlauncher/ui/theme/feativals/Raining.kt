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

package com.movtery.zalithlauncher.ui.theme.feativals

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.isActive
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

private class RainDrop(
    var x: Float,
    var y: Float,
    var dx: Float,
    var dy: Float,
    var length: Float,
    var alpha: Float
)

@Composable
fun RainEffect(
    modifier: Modifier = Modifier,
    count: Int = 80,
    strokeWidth: Dp = 2.dp,
    getX: (width: Float) -> Float = { Random.nextFloat() * it },
    getY: (height: Float) -> Float = { Random.nextFloat() * -it * 0.5f },
    isOutOfScreen: (dropLength: Float, dropX: Float, dropY: Float, width: Float, height: Float) -> Boolean = {
        _, dropX, dropY, width, height ->
        dropX !in 0f..width || dropY > height
    },
    getLength: () -> Float = { Random.nextFloat() * 25f + 50f },
    getSpeed: () -> Float = { Random.nextFloat() * 12f + 50f },
    getAngle: () -> Float = { 0f },
    getAlpha: () -> Float = { Random.nextFloat() * 0.2f + 0.1f },
    getColor: () -> Color = { Color.White },
) {
    var width by remember { mutableFloatStateOf(0f) }
    var height by remember { mutableFloatStateOf(0f) }

    val rainDrops = remember { mutableListOf<RainDrop>() }

    LaunchedEffect(width, height) {
        if (width == 0f || height == 0f) return@LaunchedEffect

        rainDrops.clear()

        repeat(count) {
            val angle = getAngle()
            val speed = getSpeed()
            val rad = Math.toRadians(angle.toDouble())

            val dx = (-speed * sin(rad)).toFloat()
            val dy = (speed * cos(rad)).toFloat()

            rainDrops.add(
                RainDrop(
                    x = getX(width),
                    y = getY(height),
                    dx = dx,
                    dy = dy,
                    length = getLength(),
                    alpha = getAlpha()
                )
            )
        }
    }

    var tick by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) {
        while (isActive) {
            withFrameNanos {
                tick++
            }
        }
    }

    Canvas(
        modifier = modifier.fillMaxSize()
    ) {
        width = size.width
        height = size.height

        //读取 tick 以建立重绘依赖
        tick

        rainDrops.forEach { drop ->
            drop.x += drop.dx
            drop.y += drop.dy

            if (
                isOutOfScreen(drop.length, drop.x, drop.y, width, height)
            ) {
                drop.x = getX(width)
                drop.y = getY(height)

                val angle = getAngle()
                val speed = getSpeed()
                val rad = Math.toRadians(angle.toDouble())

                drop.dx = (-speed * sin(rad)).toFloat()
                drop.dy = (speed * cos(rad)).toFloat()

                drop.length = getLength()
                drop.alpha = getAlpha()
            }

            val endX = drop.x - drop.dx * (drop.length / sqrt(drop.dx * drop.dx + drop.dy * drop.dy))
            val endY = drop.y - drop.dy * (drop.length / sqrt(drop.dx * drop.dx + drop.dy * drop.dy))

            drawLine(
                color = getColor().copy(alpha = drop.alpha),
                start = Offset(drop.x, drop.y),
                end = Offset(endX, endY),
                strokeWidth = strokeWidth.toPx(),
                cap = StrokeCap.Round
            )
        }
    }
}