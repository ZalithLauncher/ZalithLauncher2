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

package com.movtery.zalithlauncher.ui.screens.content.download.assets.elements

import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.movtery.zalithlauncher.R

/**
 * 收藏切换按钮（心形 filled/outlined 双态）
 * @param isFavorite 当前是否已收藏：已收藏显示实心图标并按主题色着色，未收藏显示描边图标（沿用内容色，不显式 tint）
 * @param onToggle 点击切换收藏状态的回调
 * @param modifier 修饰符
 * @param enabled 是否可点击
 */
@Composable
fun FavoriteToggleButton(
    isFavorite: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    IconButton(
        modifier = modifier,
        onClick = onToggle,
        enabled = enabled
    ) {
        Icon(
            painter = painterResource(
                if (isFavorite) R.drawable.ic_favorite_filled
                else R.drawable.ic_favorite_outlined
            ),
            contentDescription = stringResource(
                if (isFavorite) R.string.download_favorites_remove
                else R.string.download_favorites_add
            ),
            tint = if (isFavorite) MaterialTheme.colorScheme.primary
            else LocalContentColor.current
        )
    }
}
