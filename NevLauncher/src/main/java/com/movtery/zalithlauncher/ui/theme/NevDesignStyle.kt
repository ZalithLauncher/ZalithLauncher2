/*
 * Nev Launcher
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

package com.movtery.zalithlauncher.ui.theme

import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Gaya desain launcher. Terpisah dari tema warna: yang berbeda di sini adalah
 * bentuk sudut, tebal garis tepi, cahaya, dan transparansi panel.
 */
enum class DesignStyleType {
    /** Sudut bulat, panel solid (tampilan asli Nev Launcher). */
    NEV,

    /** Sudut terpotong, panel kaca transparan, cahaya tepi lebih kuat. */
    CRYOGENIC
}

/** Sisi layar tempat menu navigasi utama diletakkan. */
enum class NavRailSide {
    START,
    END
}

/** Token gaya yang dibaca komponen bersama lewat [LocalNevStyle]. */
@Immutable
data class NevStyle(
    /** Pengali radius sudut luar kartu pengaturan. */
    val cardOuterScale: Float,
    /** Pengali radius sudut dalam kartu pengaturan (antar kartu dalam satu grup). */
    val cardInnerScale: Float,
    val cardBorderWidth: Dp,
    val glowRadiusScale: Float,
    val glowIntensityScale: Float,
    /** Pengali opasitas latar kartu (1 = solid). */
    val cardAlpha: Float,
    /** true: tab, tombol, dan panel memakai sudut terpotong, bukan bulat. */
    val cutCorners: Boolean,
    /** true: menu navigasi utama berupa kapsul kaca melayang di tengah, bukan kolom penuh. */
    val floatingRail: Boolean
)

val NevClassicStyle = NevStyle(
    cardOuterScale = 1f,
    cardInnerScale = 1f,
    cardBorderWidth = 1.5.dp,
    glowRadiusScale = 1f,
    glowIntensityScale = 1f,
    cardAlpha = 1f,
    cutCorners = false,
    floatingRail = false
)

val NevCryogenicStyle = NevStyle(
    cardOuterScale = 0.55f,
    cardInnerScale = 0.3f,
    cardBorderWidth = 1.dp,
    glowRadiusScale = 1.4f,
    glowIntensityScale = 1.2f,
    cardAlpha = 0.8f,
    cutCorners = true,
    floatingRail = true
)

fun nevStyleFor(type: DesignStyleType): NevStyle = when (type) {
    DesignStyleType.NEV -> NevClassicStyle
    DesignStyleType.CRYOGENIC -> NevCryogenicStyle
}

val LocalNevStyle = compositionLocalOf { NevClassicStyle }

/** Bentuk "pil": kapsul penuh pada gaya klasik, potongan diagonal pada gaya Cryogenic. */
@Composable
fun nevPillShape(): Shape =
    if (LocalNevStyle.current.cutCorners) {
        CutCornerShape(topStart = 16.dp, topEnd = 0.dp, bottomEnd = 16.dp, bottomStart = 0.dp)
    } else {
        RoundedCornerShape(50)
    }

/** Bentuk panel/tombol/tab dengan radius dasar [radius], menyesuaikan gaya aktif. */
@Composable
fun nevShape(radius: Dp): Shape =
    if (LocalNevStyle.current.cutCorners) {
        val cut = radius * 0.6f
        CutCornerShape(topStart = cut, topEnd = 0.dp, bottomEnd = cut, bottomStart = 0.dp)
    } else {
        RoundedCornerShape(radius)
    }
