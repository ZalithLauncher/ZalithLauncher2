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

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp

enum class NusaButtonStyle { Primary, Secondary, Ghost }

/**
 * Tombol Nusa. Micro-interaction saat ditekan: mengecil ke 96% dan menggelap 14%,
 * lalu kembali dengan pantulan kecil. Target sentuh minimal 48 dp.
 */
@Composable
fun NusaButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: NusaButtonStyle = NusaButtonStyle.Primary,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() }
) {
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by rememberPressScale(pressed, NusaMotion.BUTTON_PRESSED_SCALE)
    val overlay by animateFloatAsState(
        targetValue = if (pressed) 0.14f else 0f,
        animationSpec = tween(NusaMotion.COLOR_MS),
        label = "nusaButtonOverlay"
    )

    val shape = NusaShapes.Medium
    val background = when (style) {
        NusaButtonStyle.Primary -> Modifier.background(NusaColors.AccentBrush)
        NusaButtonStyle.Secondary -> Modifier.background(NusaColors.SurfaceRaised)
        NusaButtonStyle.Ghost -> Modifier
    }
    val textColor = when (style) {
        NusaButtonStyle.Primary -> NusaColors.OnAccent
        NusaButtonStyle.Secondary -> NusaColors.TextPrimary
        NusaButtonStyle.Ghost -> NusaColors.AccentText
    }

    Box(
        modifier = modifier
            .heightIn(min = 48.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                alpha = if (enabled) 1f else 0.4f
            }
            .clip(shape)
            .then(background)
            .drawWithContent {
                drawContent()
                if (overlay > 0f) drawRect(Color.Black.copy(alpha = overlay))
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                role = Role.Button,
                onClick = onClick
            )
            .padding(horizontal = 18.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, style = NusaType.Button, color = textColor)
    }
}
