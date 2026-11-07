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

package com.movtery.zalithlauncher.ui.nusa

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Efek shimmer untuk keadaan memuat: pita terang menyapu dari kiri ke kanan.
 * Jika animasi sistem dimatikan, yang tampil hanya blok abu statis.
 */
fun Modifier.nusaShimmer(): Modifier = composed {
    val reduced = rememberReducedMotion()
    if (reduced) {
        this.background(NusaColors.ShimmerBase)
    } else {
        val transition = rememberInfiniteTransition(label = "nusaShimmer")
        val progress by transition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(NusaMotion.SHIMMER_MS, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "nusaShimmerProgress"
        )
        this.drawWithCache {
            onDrawBehind {
                val sweep = size.width * 0.6f
                val x = -sweep + (size.width + sweep) * progress
                drawRect(NusaColors.ShimmerBase)
                drawRect(
                    brush = Brush.linearGradient(
                        colors = listOf(Color.Transparent, NusaColors.ShimmerHighlight, Color.Transparent),
                        start = Offset(x, 0f),
                        end = Offset(x + sweep, 0f)
                    )
                )
            }
        }
    }
}

/** Blok kerangka untuk menggantikan konten yang sedang dimuat. */
@Composable
fun NusaSkeleton(
    modifier: Modifier = Modifier,
    shape: Shape = NusaShapes.Small
) {
    Box(modifier = modifier.clip(shape).nusaShimmer())
}

/** Satu baris teks palsu. */
@Composable
fun NusaSkeletonLine(
    modifier: Modifier = Modifier,
    height: Dp = 14.dp
) {
    NusaSkeleton(modifier = modifier.fillMaxWidth().height(height))
}
