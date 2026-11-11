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

package com.movtery.zalithlauncher.ui.components

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchColors
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.ui.theme.NevRed

@Composable
fun DefaultSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: SwitchColors = SwitchDefaults.colors(
        checkedTrackColor = NevRed.GradientEnd,
        checkedThumbColor = NevRed.OnGradient,
        checkedBorderColor = NevRed.OutlineBright,
        checkedIconColor = NevRed.GradientEnd,
        uncheckedTrackColor = NevRed.SurfaceHighest,
        uncheckedBorderColor = NevRed.Outline,
        uncheckedThumbColor = NevRed.Muted
    ),
    interactionSource: MutableInteractionSource? = null
) {
    val shape = RoundedCornerShape(50)
    val knobOffset by animateDpAsState(
        targetValue = if (checked) 26.dp else 0.dp,
        animationSpec = tween(durationMillis = 180),
        label = "nevSwitchKnob"
    )
    Box(
        modifier = modifier
            .alpha(if (enabled) 1f else 0.5f)
            .size(width = 64.dp, height = 38.dp)
            .clip(shape)
            .background(
                if (checked) NevRed.Gradient else NevRed.solid(NevRed.SurfaceHighest),
                shape
            )
            .border(1.dp, if (checked) NevRed.OutlineBright else NevRed.Outline, shape)
            .toggleable(
                value = checked,
                enabled = enabled,
                role = Role.Switch,
                interactionSource = interactionSource,
                indication = null,
                onValueChange = onCheckedChange
            )
            .padding(4.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        //Kenop gelap; saat aktif ada titik putih di tengah
        Box(
            modifier = Modifier
                .offset(x = knobOffset)
                .size(30.dp)
                .clip(CircleShape)
                .background(if (checked) NevRed.PanelDeep else NevRed.Muted),
            contentAlignment = Alignment.Center
        ) {
            if (checked) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                )
            }
        }
    }
}

/**
 * Represents the color used for the switch's track, depending on [enabled] and [checked].
 *
 * @param enabled whether the [Switch] is enabled or not
 * @param checked whether the [Switch] is checked or not
 */
@Stable
private fun SwitchColors.trackColor(enabled: Boolean, checked: Boolean): Color =
    if (enabled) {
        if (checked) checkedTrackColor else uncheckedTrackColor
    } else {
        if (checked) disabledCheckedTrackColor else disabledUncheckedTrackColor
    }