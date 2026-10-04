/*
 * Zalith Launcher 2
 * Copyright (C) 2025 MovTery <movtery228@qq.com> and contributors
 * Copyright (C) 2026 Mirai Launcher contributors.
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

import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.setting.AllSettings
import com.movtery.zalithlauncher.ui.components.FloatingBall
import com.movtery.zalithlauncher.ui.screens.content.elements.MemoryPreview
import com.movtery.zalithlauncher.ui.theme.MiraiThemeManager
import kotlinx.coroutines.delay

@Composable
fun DraggableGameBall(
    position: Offset,
    onPositionChanged: (Offset) -> Unit,
    onSavePos: () -> Unit,
    gameFps: Int?,
    showMemory: Boolean,
    opened: Boolean,
    alpha: Float = 1f,
    onClick: () -> Unit = {},
    onTakeScreenshot: () -> Unit = {},
    onToggleDebugF3: () -> Unit = {},
    onToggleLogOverlay: () -> Unit = {},
    onForceClose: () -> Unit = {},
    onRefreshResolution: () -> Unit = {}
) {
    FloatingBall(
        modifier = Modifier.focusProperties {
            canFocus = false
        },
        position = position,
        onPositionChanged = onPositionChanged,
        onSavePos = onSavePos,
        onClick = onClick,
        alpha = alpha
    ) {
        MiraiQuickOverlayPillContent(
            gameFps = gameFps,
            showMemory = showMemory,
            opened = opened,
            onOpenFullMenu = onClick,
            onTakeScreenshot = onTakeScreenshot,
            onToggleDebugF3 = onToggleDebugF3,
            onToggleLogOverlay = onToggleLogOverlay,
            onForceClose = onForceClose,
            onRefreshResolution = onRefreshResolution
        )
    }
}

@Composable
private fun MiraiQuickOverlayPillContent(
    gameFps: Int?,
    showMemory: Boolean,
    opened: Boolean,
    onOpenFullMenu: () -> Unit,
    onTakeScreenshot: () -> Unit,
    onToggleDebugF3: () -> Unit,
    onToggleLogOverlay: () -> Unit,
    onForceClose: () -> Unit,
    onRefreshResolution: () -> Unit
) {
    val context = LocalContext.current
    val accentColor = MiraiThemeManager.currentAccent()
    var hudExpanded by remember { mutableStateOf(false) }

    var batteryTempTenths by remember { mutableIntStateOf(350) }
    var batteryPct by remember { mutableIntStateOf(100) }

    LaunchedEffect(Unit) {
        while (true) {
            runCatching {
                val intent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
                if (intent != null) {
                    val temp = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 350)
                    val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 100)
                    val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 100).coerceAtLeast(1)
                    batteryTempTenths = temp
                    batteryPct = ((level * 100f) / scale).toInt().coerceIn(0, 100)
                }
            }
            delay(5000L)
        }
    }

    val tempCelsius = batteryTempTenths / 10
    val fpsVal = gameFps ?: 0
    val fpsColor = when {
        gameFps == null -> accentColor
        fpsVal >= 55 -> Color(0xFF1BD96A)
        fpsVal >= 30 -> Color(0xFFFBBF24)
        else -> Color(0xFFEF4444)
    }

    Column(
        modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Collapsed Mirai Pill Header Row
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Full Menu Icon
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.2f))
                    .clickable(onClick = onOpenFullMenu),
                contentAlignment = Alignment.Center
            ) {
                Crossfade(opened, label = "menuBallIcon") { state ->
                    Icon(
                        modifier = Modifier.size(16.dp),
                        painter = painterResource(
                            if (state) R.drawable.ic_menu_open else R.drawable.ic_menu
                        ),
                        tint = accentColor,
                        contentDescription = null
                    )
                }
            }

            // Live FPS Pill
            if (gameFps != null) {
                Text(
                    text = "⚡ $fpsVal FPS",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = fpsColor
                )
            } else {
                Text(
                    text = "⚡ Mirai",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = accentColor
                )
            }

            // Battery Temp Badge
            Text(
                text = "${tempCelsius}°C",
                style = MaterialTheme.typography.labelSmall,
                color = if (tempCelsius >= 43) Color(0xFFFBBF24) else Color(0xFFD1D5DB)
            )

            // Expand / Collapse Quick HUD Toggle
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF282C36))
                    .clickable { hudExpanded = !hudExpanded },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(
                        if (hudExpanded) R.drawable.ic_keyboard_arrow_up else R.drawable.ic_keyboard_arrow_down
                    ),
                    contentDescription = "Quick HUD",
                    tint = Color.White,
                    modifier = Modifier.size(15.dp)
                )
            }
        }

        // Compact Memory Bar if enabled in settings
        if (showMemory) {
            MemoryPreview(
                modifier = Modifier
                    .width(164.dp)
                    .padding(horizontal = 2.dp),
                mainColor = accentColor.copy(alpha = 0.7f),
                backgroundColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                textStyle = MaterialTheme.typography.labelSmall,
                usedText = { usedMemory, totalMemory ->
                    "${usedMemory.toInt()} / ${totalMemory.toInt()} MB"
                }
            )
        }

        // Expanded Mirai Quick-Overlay Drawer
        AnimatedVisibility(
            visible = hudExpanded,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            val scaleRatio = AllSettings.resolutionRatio.state
            val cursorSpeed = AllSettings.mouseCaptureSensitivity.state

            Surface(
                modifier = Modifier.width(232.dp),
                shape = RoundedCornerShape(12.dp),
                color = Color(0xE614161A),
                border = BorderStroke(1.dp, accentColor.copy(alpha = 0.55f))
            ) {
                Column(
                    modifier = Modifier.padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Telemetry Summary Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "MIRAI QUICK HUD",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = accentColor
                        )
                        Text(
                            text = "🔋 $batteryPct% • ${tempCelsius}°C",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF9CA3AF)
                        )
                    }

                    // Render Scale Slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Scale: ${scaleRatio}%",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            modifier = Modifier.width(68.dp)
                        )
                        Slider(
                            value = scaleRatio.toFloat().coerceIn(50f, 100f),
                            onValueChange = { AllSettings.resolutionRatio.updateState(it.toInt()) },
                            onValueChangeFinished = {
                                AllSettings.resolutionRatio.save()
                                onRefreshResolution()
                            },
                            valueRange = 50f..100f,
                            modifier = Modifier
                                .weight(1f)
                                .height(22.dp),
                            colors = SliderDefaults.colors(
                                thumbColor = accentColor,
                                activeTrackColor = accentColor
                            )
                        )
                    }

                    // Camera Sensitivity Slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Sens: ${cursorSpeed}%",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            modifier = Modifier.width(68.dp)
                        )
                        Slider(
                            value = cursorSpeed.toFloat().coerceIn(25f, 300f),
                            onValueChange = { AllSettings.mouseCaptureSensitivity.updateState(it.toInt()) },
                            onValueChangeFinished = { AllSettings.mouseCaptureSensitivity.save() },
                            valueRange = 25f..300f,
                            modifier = Modifier
                                .weight(1f)
                                .height(22.dp),
                            colors = SliderDefaults.colors(
                                thumbColor = accentColor,
                                activeTrackColor = accentColor
                            )
                        )
                    }

                    // Quick Action Buttons Row (F2 Shot, F3 Debug, Logs, Kill)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        QuickHudPillButton(
                            text = "📸 Shot",
                            accent = accentColor,
                            modifier = Modifier.weight(1f),
                            onClick = onTakeScreenshot
                        )
                        QuickHudPillButton(
                            text = "F3",
                            accent = accentColor,
                            modifier = Modifier.weight(0.75f),
                            onClick = onToggleDebugF3
                        )
                        QuickHudPillButton(
                            text = "Logs",
                            accent = accentColor,
                            modifier = Modifier.weight(0.85f),
                            onClick = onToggleLogOverlay
                        )
                        QuickHudPillButton(
                            text = "Kill",
                            accent = Color(0xFFEF4444),
                            isDanger = true,
                            modifier = Modifier.weight(0.8f),
                            onClick = onForceClose
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickHudPillButton(
    text: String,
    accent: Color,
    isDanger: Boolean = false,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        color = if (isDanger) Color(0xFF3A181C) else Color(0xFF21242B),
        border = BorderStroke(
            1.dp,
            if (isDanger) Color(0xFFEF4444).copy(alpha = 0.6f) else accent.copy(alpha = 0.4f)
        )
    ) {
        Box(
            modifier = Modifier.padding(vertical = 5.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = if (isDanger) Color(0xFFFCA5A5) else Color.White
            )
        }
    }
}
