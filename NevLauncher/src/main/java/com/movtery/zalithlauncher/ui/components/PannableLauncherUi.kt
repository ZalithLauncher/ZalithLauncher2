/*
 * Nev Launcher
 * Based on Zalith Launcher 2, Copyright (C) 2025-2026 MovTery <movtery228@qq.com> and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/gpl-3.0.txt>.
 */

package com.movtery.zalithlauncher.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.movtery.zalithlauncher.setting.AllSettings
import kotlin.math.roundToInt

/** Batas geser: maksimal separuh lebar/tinggi jendela, supaya UI tidak hilang sepenuhnya. */
private const val MAX_PAN_FRACTION = 0.5f

private fun Offset.clampTo(size: IntSize): Offset {
    if (size.width <= 0 || size.height <= 0) return this
    val maxX = size.width * MAX_PAN_FRACTION
    val maxY = size.height * MAX_PAN_FRACTION
    return Offset(x.coerceIn(-maxX, maxX), y.coerceIn(-maxY, maxY))
}

/**
 * Membuat seluruh tampilan launcher bisa digeser di ukuran launcher berapa pun
 * (skala 60%–150%). Cara menggeser:
 * - seret dengan DUA jari di mana saja (satu jari tetap bekerja normal: klik, scroll, dll.)
 * - atau seret gagang kecil di bagian bawah layar dengan satu jari
 * - ketuk dua kali gagang tersebut untuk mengembalikan posisi
 *
 * Posisi disimpan di [AllSettings.launcherUiPanX]/[AllSettings.launcherUiPanY].
 * Fitur bisa dimatikan lewat [AllSettings.launcherUiPanEnabled].
 */
@Composable
fun PannableLauncherUi(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val enabled = AllSettings.launcherUiPanEnabled.state
    val savedX = AllSettings.launcherUiPanX.state
    val savedY = AllSettings.launcherUiPanY.state

    var containerSize by remember { mutableStateOf(IntSize.Zero) }
    var offset by remember { mutableStateOf(Offset(savedX.toFloat(), savedY.toFloat())) }

    //posisi di pengaturan berubah (mis. tombol reset): ikuti
    LaunchedEffect(savedX, savedY) {
        offset = Offset(savedX.toFloat(), savedY.toFloat())
    }

    fun savePosition() {
        AllSettings.launcherUiPanX.save(offset.x.roundToInt())
        AllSettings.launcherUiPanY.save(offset.y.roundToInt())
    }

    fun resetPosition() {
        offset = Offset.Zero
        savePosition()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .onSizeChanged { containerSize = it }
            .pointerInput(enabled) {
                if (!enabled) return@pointerInput
                awaitPointerEventScope {
                    var panning = false
                    while (true) {
                        //pass Initial: lihat event sebelum anak-anaknya, tapi hanya
                        //diambil alih saat ada dua jari atau lebih
                        val event = awaitPointerEvent(PointerEventPass.Initial)
                        val pressedCount = event.changes.count { it.pressed }
                        if (pressedCount >= 2) {
                            panning = true
                            val pan = event.calculatePan()
                            if (pan != Offset.Zero) {
                                offset = (offset + pan).clampTo(containerSize)
                            }
                            event.changes.forEach { if (it.positionChanged()) it.consume() }
                        } else if (panning && pressedCount == 0) {
                            panning = false
                            savePosition()
                        }
                    }
                }
            }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    val applied = if (enabled) offset.clampTo(containerSize) else Offset.Zero
                    translationX = applied.x
                    translationY = applied.y
                },
            content = content
        )

        if (enabled) {
            //gagang geser: tetap di posisi layar, tidak ikut bergeser
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 3.dp)
                    .width(56.dp)
                    .height(14.dp)
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragEnd = { savePosition() },
                            onDragCancel = { savePosition() }
                        ) { change, dragAmount ->
                            change.consume()
                            offset = (offset + dragAmount).clampTo(containerSize)
                        }
                    }
                    .pointerInput(Unit) {
                        detectTapGestures(onDoubleTap = { resetPosition() })
                    },
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .width(36.dp)
                        .height(5.dp)
                        .clip(RoundedCornerShape(50))
                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f))
                )
            }
        }
    }
}
