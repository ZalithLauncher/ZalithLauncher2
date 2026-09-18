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

package com.movtery.zalithlauncher.game.support.touch_controller

import android.annotation.SuppressLint
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import com.google.gson.annotations.SerializedName
import com.movtery.zalithlauncher.utils.logging.Logger
import top.fifthlight.touchcontroller.proxy.client.LauncherProxyClient
import top.fifthlight.touchcontroller.proxy.message.VibrateMessage

private const val TAG = "VibrationHandler"

class VibrationHandler(
    private val vibrator: Vibrator,
    private val vibrateDuration: Int?,
    private val vibrateKind: VibrateKind?,
) : LauncherProxyClient.VibrationHandler {
    enum class VibrateKind {
        @SerializedName("ONE_SHOT")
        ONE_SHOT,
        @SerializedName("CLICK")
        CLICK,
        @SerializedName("DOUBLE_CLICK")
        DOUBLE_CLICK,
        @SerializedName("HEAVY_CLICK")
        HEAVY_CLICK,
        @SerializedName("TICK")
        TICK;

        companion object {
            val default = ONE_SHOT
        }
    }

    companion object {
        @SuppressLint("NewApi")
        fun vibrate(
            vibrator: Vibrator,
            vibrateDuration: Int?,
            vibrateKind: VibrateKind?,
        ) {
            runCatching {
                val effectKind = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                    VibrateKind.ONE_SHOT
                } else {
                    vibrateKind
                }
                val effect = when (effectKind) {
                    VibrateKind.ONE_SHOT, null -> {
                        val duration = vibrateDuration?.coerceAtMost(500)?.coerceAtLeast(80)?.toLong()
                        VibrationEffect.createOneShot(
                            duration ?: 100L,
                            VibrationEffect.DEFAULT_AMPLITUDE
                        )
                    }

                    VibrateKind.CLICK -> VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK)
                    VibrateKind.DOUBLE_CLICK -> VibrationEffect.createPredefined(VibrationEffect.EFFECT_DOUBLE_CLICK)
                    VibrateKind.HEAVY_CLICK -> VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK)
                    VibrateKind.TICK -> VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK)
                }
                vibrator.vibrate(effect)
            }.onFailure {
                Logger.error(TAG, "Failed to attempt vibrating the device!", it)
            }
        }
    }

    override fun vibrate(kind: VibrateMessage.Kind) = vibrate(vibrator, vibrateDuration, vibrateKind)
}