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

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * 根据屏幕宽与高计算的屏幕类型
 */
enum class ScreenType {
    /** 手表 */
    Watch,
    /** 平板 */
    Tablet,
    /** 手机 */
    Phone
}

/**
 * [BoxWithConstraints] 包装函数，便于自动判断当前设备的屏幕类型
 * @param content 包含屏幕类型枚举
 */
@Composable
fun ScreenTypeLayout(
    modifier: Modifier = Modifier,
    contentAlignment: Alignment = Alignment.TopStart,
    propagateMinConstraints: Boolean = false,
    content: @Composable BoxWithConstraintsScope.(ScreenType) -> Unit
) {
    BoxWithConstraints(
        modifier = modifier,
        contentAlignment = contentAlignment,
        propagateMinConstraints = propagateMinConstraints
    ) {
        val screenType = when {
            maxWidth < 800.dp && maxHeight < 500.dp &&
                    maxWidth / maxHeight in 0.75f..1.6f -> ScreenType.Watch
            maxWidth >= 800.dp && maxHeight >= 500.dp -> ScreenType.Tablet
            else -> ScreenType.Phone
        }
        content(screenType)
    }
}