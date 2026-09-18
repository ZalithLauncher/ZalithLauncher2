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

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.movtery.zalithlauncher.setting.AllSettings
import com.movtery.zalithlauncher.setting.enums.isLauncherInDarkTheme
import com.movtery.zalithlauncher.utils.festival.Festival
import kotlin.random.Random

@Composable
fun FestivalEffects(
    festivals: List<Festival>,
    modifier: Modifier = Modifier,
) {
    val isDark = isLauncherInDarkTheme()
    val enableEffects = AllSettings.launcherFestivalEffects.state

    if (enableEffects && Festival.QING_MING in festivals) {
        val color = if (isDark) Color.White else Color(0xFF707070)
        RainEffect(
            modifier = modifier,
            count = 40,
            strokeWidth = 1.dp,
            getX = { Random.nextFloat() * it * 1.5f - it * 0.25f },
            isOutOfScreen = { dropLength, dropX, dropY, width, height ->
                dropX + dropLength < -width * 0.3f || dropX > width * 1.3f || dropY > height
            },
            getAngle = { Random.nextFloat() * (-8f) - 5f },
            getColor = { color },
        )
    }

    if (enableEffects && Festival.CHRISTMAS in festivals) {
        val color = if (isDark) Color.White else Color(0xFFDFF6FF)
        RainEffect(
            modifier = modifier,
            count = 150,
            getX = { Random.nextFloat() * it * 1.5f - it * 0.25f },
            isOutOfScreen = { dropLength, dropX, dropY, width, height ->
                dropX + dropLength < -width * 0.3f || dropX > width * 1.3f || dropY > height
            },
            getLength = { Random.nextFloat() * 12f + 8f },
            getSpeed = { Random.nextFloat() * 8f + 6f },
            getAngle = { Random.nextFloat() * 15f + 10f },
            getAlpha = { Random.nextFloat() * 0.4f + 0.4f },
            getColor = { color },
        )
    }
}