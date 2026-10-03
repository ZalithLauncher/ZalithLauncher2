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

package com.movtery.zalithlauncher.ui.screens.game.elements

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandIn
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.Crossfade
import androidx.compose.animation.shrinkOut
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
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.game.recorder.RecordingState
import com.movtery.zalithlauncher.setting.enums.MemoryDisplayMode
import com.movtery.zalithlauncher.setting.enums.FpsDisplayMode
import com.movtery.zalithlauncher.ui.components.FloatingBall
import com.movtery.zalithlauncher.ui.screens.content.elements.MemoryPreview

@Composable
fun DraggableGameBall(
    position: Offset,
    onPositionChanged: (Offset) -> Unit,
    onSavePos: () -> Unit,
    gameFps: Int?,
    fpsDisplayMode: FpsDisplayMode,
    fpsHistory: List<Int>,
    fpsMax: Int,
    fpsMin: Int,
    showMemory: Boolean,
    memoryDisplayMode: MemoryDisplayMode = MemoryDisplayMode.System,
    opened: Boolean,
    alpha: Float = 1f,
    onClick: () -> Unit = {},
    recordingState: RecordingState = RecordingState.IDLE,
    elapsedMs: Long = 0L,
    micEnabled: Boolean = false,
    onPauseRecording: () -> Unit = {},
    onResumeRecording: () -> Unit = {},
    onStopRecording: () -> Unit = {},
    onToggleMic: () -> Unit = {},
    onTakeScreenshot: () -> Unit = {}
) {
    val isRecordingActive = recordingState == RecordingState.RECORDING ||
            recordingState == RecordingState.PAUSED

    FloatingBall(
        modifier = Modifier.focusProperties {
            canFocus = false
        },
        position = position,
        onPositionChanged = onPositionChanged,
        onSavePos = onSavePos,
        // Ball click always opens the Game Menu — fully accessible even while recording.
        // Recording controls expand to the right of the ball instead of replacing it.
        onClick = onClick,
        alpha = alpha
    ) {
        GameBallContent(
            gameFps = gameFps,
            fpsDisplayMode = fpsDisplayMode,
            fpsHistory = fpsHistory,
            fpsMax = fpsMax,
            fpsMin = fpsMin,
            showMemory = showMemory,
            memoryDisplayMode = memoryDisplayMode,
            opened = opened,
            isRecordingActive = isRecordingActive,
            isPaused = recordingState == RecordingState.PAUSED,
            elapsedMs = elapsedMs,
            micEnabled = micEnabled,
            onPauseRecording = onPauseRecording,
            onResumeRecording = onResumeRecording,
            onStopRecording = onStopRecording,
            onToggleMic = onToggleMic,
            onTakeScreenshot = onTakeScreenshot,
        )
    }
}

/**
 * Compact recording-control strip that expands to the **right** of the floating ball while a
 * recording is active — matching the same horizontal-expansion pattern used by the FPS display
 * and Memory display.
 *
 * Controls are rendered inside Compose and therefore live in the overlay layer on top of the
 * game's SurfaceView.  Because [com.movtery.zalithlauncher.game.recorder.GameRecorder] captures
 * frames via PixelCopy / getBitmap directly from the SurfaceView buffer, this strip is naturally
 * absent from recorded video.
 *
 * The floating ball itself remains clickable and continues to open the built-in Game Menu as
 * normal, so the user can access all in-game launcher functions without stopping the recording.
 */
@Composable
private fun RecordingControlContent(
    isPaused: Boolean,
    elapsedMs: Long,
    micEnabled: Boolean,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onStop: () -> Unit,
    onToggleMic: () -> Unit,
    onTakeScreenshot: () -> Unit = {}
) {
    Row(
        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Recording indicator dot — red when active, dimmed when paused
        Icon(
            painter = painterResource(R.drawable.ic_fiber_manual_record),
            contentDescription = null,
            modifier = Modifier.size(12.dp),
            tint = if (isPaused) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                   else Color.Red
        )
        Spacer(Modifier.width(3.dp))
        // Live elapsed timer (MM:SS or HH:MM:SS)
        Text(
            text = elapsedMs.formatElapsedTime(),
            style = MaterialTheme.typography.labelSmall,
        )
        // Microphone toggle — lit when mic capture is active
        IconButton(
            onClick = onToggleMic,
            modifier = Modifier.size(28.dp)
        ) {
            Icon(
                painter = painterResource(
                    if (micEnabled) R.drawable.ic_mic
                    else R.drawable.ic_mic_off
                ),
                contentDescription = stringResource(
                    if (micEnabled) R.string.recorder_mic_on else R.string.recorder_mic_off
                ),
                modifier = Modifier.size(18.dp),
                tint = if (micEnabled) MaterialTheme.colorScheme.primary
                       else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        }
        // Screenshot — placed immediately beside the Microphone toggle for quick access.
        // Injects F2 through the existing native input bridge so Minecraft captures the
        // screenshot itself (identical to pressing F2 on a physical keyboard).
        IconButton(
            onClick = onTakeScreenshot,
            modifier = Modifier.size(28.dp)
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_screenshot),
                contentDescription = stringResource(R.string.recorder_screenshot),
                modifier = Modifier.size(18.dp)
            )
        }
        // Pause / Resume toggle
        IconButton(
            onClick = if (isPaused) onResume else onPause,
            modifier = Modifier.size(28.dp)
        ) {
            Icon(
                painter = painterResource(
                    if (isPaused) R.drawable.ic_play_arrow_filled
                    else R.drawable.ic_pause_filled
                ),
                contentDescription = stringResource(
                    if (isPaused) R.string.recorder_resume else R.string.recorder_pause
                ),
                modifier = Modifier.size(18.dp)
            )
        }
        // Stop & Save
        IconButton(
            onClick = onStop,
            modifier = Modifier.size(28.dp)
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_stop_filled),
                contentDescription = stringResource(R.string.recorder_stop_and_save),
                modifier = Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.error
            )
        }
    }
}

/** Formats elapsed milliseconds as MM:SS, or HH:MM:SS for recordings longer than an hour. */
private fun Long.formatElapsedTime(): String {
    val totalSeconds = this / 1000L
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return if (hours > 0) {
        "%02d:%02d:%02d".format(hours, minutes, seconds)
    } else {
        "%02d:%02d".format(minutes, seconds)
    }
}

@Composable
private fun GameBallContent(
    gameFps: Int?,
    fpsDisplayMode: FpsDisplayMode,
    fpsHistory: List<Int>,
    fpsMax: Int,
    fpsMin: Int,
    showMemory: Boolean,
    memoryDisplayMode: MemoryDisplayMode = MemoryDisplayMode.System,
    opened: Boolean,
    isRecordingActive: Boolean = false,
    isPaused: Boolean = false,
    elapsedMs: Long = 0L,
    micEnabled: Boolean = false,
    onPauseRecording: () -> Unit = {},
    onResumeRecording: () -> Unit = {},
    onStopRecording: () -> Unit = {},
    onToggleMic: () -> Unit = {},
    onTakeScreenshot: () -> Unit = {},
) {
    val showFps = remember(gameFps) {
        gameFps != null
    }

    Row(
        modifier = Modifier.padding(all = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val showIcon = !(showMemory || (showFps && fpsDisplayMode == FpsDisplayMode.CHART))

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

        AnimatedVisibility(
            visible = showFps || showMemory
        ) {
            Spacer(Modifier.width(4.dp))
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
                        history = fpsHistory,
                        fpsMax = fpsMax,
                        fpsMin = fpsMin
                    )
                } else {
                    Text(
                        modifier = Modifier.padding(end = 4.dp),
                        text = "FPS: ${gameFps ?: 0}",
                        style = MaterialTheme.typography.labelMedium
                    )
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
                    isAllocatedMode = memoryDisplayMode == MemoryDisplayMode.Allocated,
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

        // Recording controls expand to the right of the ball while recording is active.
        // This mirrors how FPS and Memory displays expand horizontally from the ball.
        // The ball's click target (Game Menu) remains fully functional during recording.
        AnimatedVisibility(
            visible = isRecordingActive,
            enter = expandIn(expandFrom = Alignment.CenterStart) + fadeIn(),
            exit = shrinkOut(shrinkTowards = Alignment.CenterStart) + fadeOut(),
        ) {
            RecordingControlContent(
                isPaused = isPaused,
                elapsedMs = elapsedMs,
                micEnabled = micEnabled,
                onPause = onPauseRecording,
                onResume = onResumeRecording,
                onStop = onStopRecording,
                onToggleMic = onToggleMic,
                onTakeScreenshot = onTakeScreenshot,
            )
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
