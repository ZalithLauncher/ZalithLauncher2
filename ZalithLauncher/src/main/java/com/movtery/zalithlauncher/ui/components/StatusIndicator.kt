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
 * along with this program. If not, see <https://www.gnu.org/licenses/gpl-3.0.txt>.
 */

package com.movtery.zalithlauncher.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Minimalist status indicator with monochrome colors
 */
enum class StatusType {
    SUCCESS, WARNING, ERROR, IDLE, LOADING
}

@Composable
fun StatusIndicator(
    status: StatusType,
    modifier: Modifier = Modifier,
    label: String? = null
) {
    val color = when (status) {
        StatusType.SUCCESS -> MaterialTheme.colorScheme.primary
        StatusType.WARNING -> MaterialTheme.colorScheme.secondary
        StatusType.ERROR -> MaterialTheme.colorScheme.error
        StatusType.IDLE -> MaterialTheme.colorScheme.onSurface
        StatusType.LOADING -> MaterialTheme.colorScheme.outline
    }

    val alpha by animateFloatAsState(
        targetValue = if (status == StatusType.LOADING) 0.6f else 1f
    )

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = alpha))
        )
        if (label != null) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(start = 4.dp)
            )
        }
    }
}

/**
 * Inline status bar for screens
 */
@Composable
fun StatusBar(
    statuses: List<Pair<String, StatusType>>,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        statuses.forEach { (label, status) ->
            StatusIndicator(
                status = status,
                label = label,
                modifier = Modifier.padding(end = 12.dp)
            )
        }
    }
}
