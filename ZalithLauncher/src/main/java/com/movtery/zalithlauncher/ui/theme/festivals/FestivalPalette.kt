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

package com.movtery.zalithlauncher.ui.theme.festivals

/**
 * 各效果在深浅主题下的绘制颜色
 */
object FestivalPalette {
    data class Rain(val streak: Int, val splash: Int)
    data class Snow(val flake: Int)
    data class Firework(
        /** 爆炸光点色彩，槽位数与 [com.movtery.zalithlauncher.ui.theme.festivals.simulations.FireworksSimulator.BURST_COLOR_COUNT] 对应 */
        val bursts: List<Int>,
        /** 爆炸瞬间闪光 */
        val flash: Int,
        /** 火箭尾迹 */
        val rocket: Int,
        val additive: Boolean
    )
    data class Fireflies(
        val core: Int,
        val halo: Int,
        /** 月盘：水印化低透明度，不遮挡下层内容 */
        val moon: Int,
        /** 月晕：比月盘更淡 */
        val moonHalo: Int
    )
    data class Halloween(
        val bat: Int,
        val wispHalo: Int,
        val wispCore: Int
    )
    data class Valentines(val heart: Int)

    fun rain(isDark: Boolean): Rain = if (isDark) {
        Rain(streak = 0x66FFFFFF.toInt(), splash = 0x99FFFFFF.toInt())
    } else {
        Rain(streak = 0x4D5F7186.toInt(), splash = 0x8C5F7186.toInt())
    }

    fun snow(isDark: Boolean): Snow = if (isDark) {
        Snow(flake = 0x99FFFFFF.toInt())
    } else {
        Snow(flake = 0xCCA9B4BD.toInt())
    }

    fun firework(isDark: Boolean): Firework = if (isDark) {
        Firework(
            bursts = listOf(
                0xB3FFD166.toInt(),
                0xB3FF5E7E.toInt(),
                0xB34DE3FF.toInt(),
                0xB3C08BFF.toInt(),
                0xB37BFFC4.toInt()
            ),
            flash = 0xB3FFF8D9.toInt(),
            rocket = 0xFFF7F3E3.toInt(),
            additive = true
        )
    } else {
        Firework(
            bursts = listOf(
                0x99E09F1F.toInt(),
                0x99D6295E.toInt(),
                0x991F8FC9.toInt(),
                0x998C5BE0.toInt(),
                0x991F9E72.toInt()
            ),
            flash = 0x803D4A5C.toInt(),
            rocket = 0xF2E6A623.toInt(),
            additive = false
        )
    }

    /** 国庆限定：红金配色，呼应国旗与庆典 */
    fun fireworkNational(isDark: Boolean): Firework = if (isDark) {
        Firework(
            bursts = listOf(
                0xB3FF5252.toInt(),
                0xB3FFC94D.toInt(),
                0xB3FF8A3D.toInt(),
                0xB3FFE8B0.toInt(),
                0xB3FFB84D.toInt()
            ),
            flash = 0xB3FFE9C4.toInt(),
            rocket = 0xFFF2DCB0.toInt(),
            additive = true
        )
    } else {
        Firework(
            bursts = listOf(
                0x99D22B2B.toInt(),
                0x99C98F1F.toInt(),
                0x99D96A1F.toInt(),
                0x99A82A2A.toInt(),
                0x99B8861F.toInt()
            ),
            flash = 0x808A3A3A.toInt(),
            rocket = 0xF2D9A023.toInt(),
            additive = false
        )
    }

    fun fireflies(isDark: Boolean): Fireflies = if (isDark) {
        Fireflies(
            core = 0xFFFFF3B0.toInt(),
            halo = 0xCCFFD54F.toInt(),
            moon = 0x73F7F2DC.toInt(),
            moonHalo = 0x33F2E8BC.toInt()
        )
    } else {
        Fireflies(
            core = 0xFFC08008.toInt(),
            halo = 0x80B87400.toInt(),
            moon = 0x73E8D29A.toInt(),
            moonHalo = 0x33E0C98C.toInt()
        )
    }

    /** 万圣节 */
    fun halloween(isDark: Boolean): Halloween = if (isDark) {
        Halloween(
            bat = 0x8C5A5184.toInt(),
            wispHalo = 0x8C4DE88F.toInt(),
            wispCore = 0xFF8DFFAB.toInt()
        )
    } else {
        Halloween(
            bat = 0x9940384E.toInt(),
            wispHalo = 0x9935A35C.toInt(),
            wispCore = 0xCC2E9E58.toInt()
        )
    }

    /** 情人节/七夕 */
    fun valentines(isDark: Boolean): Valentines = if (isDark) {
        Valentines(heart = 0xB3FF6E8E.toInt())
    } else {
        Valentines(heart = 0x8CD63366.toInt())
    }
}
