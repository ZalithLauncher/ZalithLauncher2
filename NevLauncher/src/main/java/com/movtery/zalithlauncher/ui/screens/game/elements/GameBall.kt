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

package com.movtery.zalithlauncher.ui.screens.game.elements

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandIn
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.setting.enums.FpsDisplayMode
import com.movtery.zalithlauncher.ui.components.FloatingBall
import com.movtery.zalithlauncher.ui.screens.content.elements.MemoryPreview

internal val BallBackground = Color(0xCC140808)
internal val BallShape = RoundedCornerShape(16.dp)
private val FpsGood = Color(0xFF4CAF50)
private val FpsOk = Color(0xFFFFB300)
private val FpsBad = Color(0xFFFF4D4F)

/** 根据帧率给出状态颜色：流畅为绿色，一般为黄色，卡顿为红色 */
private fun fpsStatusColor(fps: Int): Color = when {
    fps >= 50 -> FpsGood
    fps >= 30 -> FpsOk
    else -> FpsBad
}

@Composable
fun DraggableGameBall(
    position: Offset,
    onPositionChanged: (Offset) -> Unit,
    onSavePos: () -> Unit,
    gameFps: () -> Int?,
    fpsDisplayMode: FpsDisplayMode,
    fpsHistory: () -> List<Int>,
    fpsMax: () -> Int,
    fpsMin: () -> Int,
    showMemory: Boolean,
    opened: Boolean,
    alpha: Float = 1f,
    onClick: () -> Unit = {}
) {
    FloatingBall(
        modifier = Modifier.focusProperties {
            canFocus = false
        },
        position = position,
        onPositionChanged = onPositionChanged,
        onSavePos = onSavePos,
        onClick = onClick,
        alpha = alpha,
        color = BallBackground,
        shape = BallShape
    ) {
        GameBallContent(
            gameFps = gameFps,
            fpsDisplayMode = fpsDisplayMode,
            fpsHistory = fpsHistory,
            fpsMax = fpsMax,
            fpsMin = fpsMin,
            showMemory = showMemory,
            opened = opened,
        )
    }
}

@Composable
private fun GameBallContent(
    gameFps: () -> Int?,
    fpsDisplayMode: FpsDisplayMode,
    fpsHistory: () -> List<Int>,
    fpsMax: () -> Int,
    fpsMin: () -> Int,
    showMemory: Boolean,
    opened: Boolean,
) {
    val fps = gameFps()
    val history = fpsHistory()
    val fpsMaxValue = fpsMax()
    val fpsMinValue = fpsMin()

    val showFps = remember(fps) {
        fps != null
    }

    val accent = MaterialTheme.colorScheme.primary
    val borderColor by animateColorAsState(
        targetValue = if (opened) accent else accent.copy(alpha = 0.45f),
        label = "gameBallBorder"
    )

    Row(
        modifier = Modifier
            .border(width = 1.dp, color = borderColor, shape = BallShape)
            .padding(horizontal = 8.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        //只有在没有任何信息可显示时，才显示菜单图标，保证悬浮球依旧可以点开菜单
        val showIcon = !showFps && !showMemory

        AnimatedVisibility(
            visible = showIcon
        ) {
            Box(
                modifier = Modifier.size(28.dp),
                contentAlignment = Alignment.Center
            ) {
                Crossfade(opened) { state ->
                    Icon(
                        modifier = Modifier.size(24.dp),
                        painter = painterResource(
                            if (state) {
                                R.drawable.ic_menu_open
                            } else {
                                R.drawable.ic_menu
                            }
                        ),
                        contentDescription = null
                    )
                }
            }
        }

        //实际内容
        Column(
            modifier = Modifier
                .wrapContentSize()
                .animateContentSize()
                .width(IntrinsicSize.Max)
        ) {
            CustomAnimatedVisibility(
                visible = showFps || showMemory
            ) {
                Spacer(Modifier.height(4.dp))
            }
            //帧率显示
            CustomAnimatedVisibility(
                visible = showFps
            ) {
                if (fpsDisplayMode == FpsDisplayMode.CHART) {
                    //帧率图表
                    FpsChart(
                        modifier = Modifier.padding(end = 4.dp),
                        history = history,
                        fpsMax = fpsMaxValue,
                        fpsMin = fpsMinValue
                    )
                } else {
                    //帧率数字：状态圆点 + 标签 + 粗体数字
                    val fpsValue = fps ?: 0
                    Row(
                        modifier = Modifier.padding(end = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(color = fpsStatusColor(fpsValue), shape = CircleShape)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "FPS",
                            style = MaterialTheme.typography.labelSmall,
                            color = LocalContentColor.current.copy(alpha = 0.65f)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            modifier = Modifier.widthIn(min = 22.dp),
                            text = fpsValue.toString(),
                            style = MaterialTheme.typography.labelLarge.copy(fontFeatureSettings = "tnum"),
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.End
                        )
                    }
                }
            }
            CustomAnimatedVisibility(
                visible = showFps && showMemory
            ) {
                Spacer(Modifier.height(4.dp))
            }
            //内存显示
            CustomAnimatedVisibility(
                visible = showMemory
            ) {
                MemoryPreview(
                    modifier = Modifier
                        .defaultMinSize(minWidth = 168.dp)
                        .fillMaxWidth()
                        .padding(end = 4.dp),
                    mainColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                    backgroundColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    textStyle = MaterialTheme.typography.labelSmall,
                    usedText = { usedMemory, totalMemory ->
                        "${usedMemory.toInt()}MB/${totalMemory.toInt()}MB"
                    }
                )
            }
            CustomAnimatedVisibility(
                visible = showFps || showMemory
            ) {
                Spacer(Modifier.height(4.dp))
            }
        }
    }
}

@Composable
private fun ColumnScope.CustomAnimatedVisibility(
    visible: Boolean,
    content: @Composable (AnimatedVisibilityScope.() -> Unit)
) {
    AnimatedVisibility(
        visible = visible,
        enter = expandIn(expandFrom = Alignment.CenterStart) + fadeIn(),
        exit = shrinkOut(shrinkTowards = Alignment.CenterStart) + fadeOut(),
        content = content
    )
}