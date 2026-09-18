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

package com.movtery.zalithlauncher.ui.screens.content.elements

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.ui.AndroidStringText
import com.movtery.zalithlauncher.ui.androidText
import com.movtery.zalithlauncher.ui.components.MarqueeText
import com.movtery.zalithlauncher.ui.components.rememberDialogMaxHeight
import com.movtery.zalithlauncher.ui.theme.cardColor
import com.movtery.zalithlauncher.ui.theme.itemColor
import com.movtery.zalithlauncher.ui.theme.onCardColor
import com.movtery.zalithlauncher.utils.microphone.MicMeter

sealed interface MicrophoneCheckState {
    data object None : MicrophoneCheckState
    data object Start : MicrophoneCheckState
}

@Composable
fun MicrophoneCheckOperation(
    state: MicrophoneCheckState,
    changeState: (MicrophoneCheckState) -> Unit,
    onShowToast: (AndroidStringText) -> Unit,
) {
    when (state) {
        is MicrophoneCheckState.None -> {}
        is MicrophoneCheckState.Start -> {
            MicrophoneCheckDialog(
                title = stringResource(R.string.microphone_check_title),
                noPermissions = {
                    onShowToast(androidText(R.string.microphone_check_no_permissions))
                },
                onDismissRequest = {
                    changeState(MicrophoneCheckState.None)
                }
            )
        }
    }
}

@Composable
fun MicrophoneCheckDialog(
    title: String = stringResource(R.string.microphone_check_title),
    noPermissions: () -> Unit,
    onDismissRequest: () -> Unit
) {
    val context = LocalContext.current
    /** 测试获得的相对音量值 */
    var level by remember { mutableDoubleStateOf(0.0) }

    val micMeter = remember { MicMeter() }

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                micMeter.start(
                    context,
                    onLevelUpdate = { level = it },
                    onPermissionRequest = {
                        //本次已授权，无需处理权限申请，忽略
                    }
                )
            } else {
                //用户拒绝授权，停止麦克风测试
                micMeter.stop()
                noPermissions()
                onDismissRequest()
            }
        }
    )

    Dialog(
        onDismissRequest = {
            micMeter.stop()
        }
    ) {
        LaunchedEffect(Unit) {
            micMeter.start(
                context,
                onLevelUpdate = { level = it },
                onPermissionRequest = {
                    //申请麦克风权限
                    requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                }
            )
        }

        DisposableEffect(Unit) {
            onDispose { micMeter.stop() }
        }

        BoxWithConstraints(
            modifier = Modifier
                .heightIn(max = rememberDialogMaxHeight())
                .fillMaxHeight(),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier
                    .padding(all = 6.dp)
                    .heightIn(max = (maxHeight - 12.dp).coerceAtMost(rememberDialogMaxHeight()))
                    .wrapContentHeight(),
                shape = MaterialTheme.shapes.extraLarge,
                color = cardColor(false),
                contentColor = onCardColor(),
                shadowElevation = 6.dp
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.size(16.dp))

                    VoiceDbShower(
                        modifier = Modifier.fillMaxWidth(),
                        level = level,
                        height = 45.dp
                    )

                    Spacer(modifier = Modifier.size(16.dp))

                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            micMeter.stop()
                            onDismissRequest()
                        }
                    ) {
                        MarqueeText(text = stringResource(R.string.generic_close))
                    }
                }
            }
        }
    }
}

@Composable
fun VoiceDbShower(
    level: Double,
    modifier: Modifier = Modifier,
    height: Dp = 24.dp,
    shape: Shape = RoundedCornerShape(50.dp),
    backgroundColor: Color = itemColor()
) {
    val normalizedDb = level.coerceIn(0.0, 100.0) //0~100db
    val progress by animateFloatAsState((normalizedDb / 100.0f).toFloat())

    val color = if (level <= 50) {
        //0~50：绿色到黄色的渐变
        lerp(Color(0xFF4CAF50), Color(0xFFFFEB3B), (level / 50).toFloat())
    } else {
        //50~100+：黄色到红色的渐变
        lerp(Color(0xFFFFEB3B), Color(0xFFF44336), ((level - 50) / 50).toFloat().coerceIn(0f, 1f))
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(shape)
            .background(backgroundColor)
    ) {
        //背景
        Box(
            modifier = Modifier
                .fillMaxWidth(progress)
                .fillMaxHeight()
                .clip(shape)
                .background(color)
        )

        Text(
            text = stringResource(R.string.microphone_check_volume, level.toInt()),
            color = Color.White,
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 8.dp),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

private fun lerp(start: Color, end: Color, fraction: Float): Color {
    val r = (start.red + fraction * (end.red - start.red)).coerceIn(0f, 1f)
    val g = (start.green + fraction * (end.green - start.green)).coerceIn(0f, 1f)
    val b = (start.blue + fraction * (end.blue - start.blue)).coerceIn(0f, 1f)
    val a = (start.alpha + fraction * (end.alpha - start.alpha)).coerceIn(0f, 1f)
    return Color(r, g, b, a)
}