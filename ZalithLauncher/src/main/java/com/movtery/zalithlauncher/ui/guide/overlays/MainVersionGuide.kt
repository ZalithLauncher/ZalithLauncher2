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

package com.movtery.zalithlauncher.ui.guide.overlays

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.movtery.guide.GuideCallout
import com.movtery.guide.GuideCalloutOverlay
import com.movtery.guide.GuideScope
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.ui.components.BackgroundCard
import com.movtery.zalithlauncher.ui.components.CutoutBox
import com.movtery.zalithlauncher.ui.components.MarqueeText
import com.movtery.zalithlauncher.ui.components.ScalingIconButton
import com.movtery.zalithlauncher.ui.screens.content.elements.VersionIconImage
import com.movtery.zalithlauncher.ui.screens.content.settings.layouts.CardPosition
import com.movtery.zalithlauncher.ui.screens.content.settings.layouts.rememberSettingsCardShape
import kotlin.math.roundToInt

@Composable
fun MainVersionGuideOverlay(scope: GuideScope) {
    val anchor = scope.anchors.singleOrNull() ?: return
    val density = LocalDensity.current
    val iconWidthPx = with(density) { 58.dp.toPx() }
    val cardSpacingPx = with(density) { 2.dp.toPx() }
    val iconRight = anchor.left + iconWidthPx.coerceAtMost(anchor.width)
    val secondLeft = (iconRight + cardSpacingPx).coerceAtMost(anchor.right)

    GuideCalloutOverlay(
        animations = scope.animations,
        callouts = listOf(
            GuideCallout(
                target = Rect(anchor.left, anchor.top, iconRight, anchor.bottom),
            ) {
                CalloutLabel {
                    Text(stringResource(R.string.guide_main_version_icon_title))
                    Text(stringResource(R.string.guide_main_version_icon_text))
                }
            },
            GuideCallout(
                target = Rect(secondLeft, anchor.top, anchor.right, anchor.bottom),
            ) {
                CalloutLabel {
                    Text(stringResource(R.string.guide_main_version_list))
                    Text(stringResource(R.string.guide_main_version_launch))
                }
            },
        ),
    ) {
        VersionManagerDemo(
            modifier = Modifier
                .offset { IntOffset(anchor.left.roundToInt(), anchor.top.roundToInt()) }
                .size(
                    width = with(density) { anchor.width.toDp() },
                    height = with(density) { anchor.height.toDp() },
                ),
        )
    }
}

@Composable
private fun VersionManagerDemo(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        BackgroundCard(
            modifier = Modifier.fillMaxHeight(),
            shape = rememberSettingsCardShape(CardPosition.BottomStart),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(start = 12.dp, end = 8.dp),
                contentAlignment = Alignment.Center,
            ) {
                CutoutBox(
                    modifier = Modifier.size(38.dp),
                    cutoutRadius = 20.dp,
                    overlayAlignment = Alignment.BottomEnd,
                    overlay = {
                        Icon(
                            modifier = Modifier.size(16.dp),
                            painter = painterResource(R.drawable.ic_settings_filled),
                            contentDescription = stringResource(R.string.versions_manage_settings),
                        )
                    },
                ) {
                    VersionIconImage(
                        modifier = Modifier.fillMaxSize(),
                        version = null,
                    )
                }
            }
        }

        BackgroundCard(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            shape = rememberSettingsCardShape(CardPosition.BottomEnd),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Max),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(horizontal = 8.dp, vertical = 12.dp),
                ) {
                    MarqueeText(
                        text = "1.21.1",
                        style = MaterialTheme.typography.labelMedium,
                    )
                    MarqueeText(
                        text = "1.21.1",
                        style = MaterialTheme.typography.labelSmall,
                    )
                }
                ScalingIconButton(
                    modifier = Modifier.padding(end = 8.dp),
                    onClick = {},
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_play_arrow_filled),
                        contentDescription = stringResource(R.string.main_launch_game),
                    )
                }
            }
        }
    }
}

@Composable
private fun CalloutLabel(
    content: @Composable ColumnScope.() -> Unit,
) {
    Box(
        modifier = Modifier
            .widthIn(max = 320.dp)
            .clip(MaterialTheme.shapes.large)
            .background(Color.Black.copy(alpha = 0.65f)),
    ) {
        CompositionLocalProvider(
            LocalTextStyle provides MaterialTheme.typography.bodyMedium
        ) {
            Column(
                modifier = Modifier.padding(all = 12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                content = content,
            )
        }
    }
}