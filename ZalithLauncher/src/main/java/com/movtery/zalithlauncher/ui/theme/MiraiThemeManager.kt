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

package com.movtery.zalithlauncher.ui.theme

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.movtery.zalithlauncher.setting.AllSettings
import com.movtery.zalithlauncher.viewmodel.BackgroundViewModel
import com.movtery.zalithlauncher.viewmodel.LocalBackgroundViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.graphics.Color as AndroidColor

data class MiraiWallpaperPreset(
    val id: String,
    val title: String,
    val biome: String,
    val assetPath: String,
    val themeName: String,
    val accentColor: Color
)

object MiraiThemeManager {
    private const val PREFS_NAME = "mirai_wallpaper_theme_prefs"
    private const val KEY_SELECTED_WALLPAPER = "selected_wallpaper_id"

    val wallpapers: List<MiraiWallpaperPreset> = listOf(
        MiraiWallpaperPreset(
            id = "wp_01_lush_caves",
            title = "Lush Glowberry Cave",
            biome = "Lush Caves • Complementary Shaders",
            assetPath = "wallpapers/wp_01_lush_caves.jpg",
            themeName = "Lush Emerald",
            accentColor = Color(0xFF1BD96A)
        ),
        MiraiWallpaperPreset(
            id = "wp_02_cherry_blossom",
            title = "Cherry Blossom Grove",
            biome = "Cherry Grove • BSL Shaders",
            assetPath = "wallpapers/wp_02_cherry_blossom.jpg",
            themeName = "Sakura Pink",
            accentColor = Color(0xFFF472B6)
        ),
        MiraiWallpaperPreset(
            id = "wp_03_sunset_river",
            title = "Golden Sunset River",
            biome = "River Valley • SEUS Renewed",
            assetPath = "wallpapers/wp_03_sunset_river.jpg",
            themeName = "Sunset Amber",
            accentColor = Color(0xFFF59E0B)
        ),
        MiraiWallpaperPreset(
            id = "wp_04_crystal_river",
            title = "Crystal River Forest",
            biome = "Birch & Oak River • Complementary",
            assetPath = "wallpapers/wp_04_crystal_river.jpg",
            themeName = "River Cyan",
            accentColor = Color(0xFF06B6D4)
        ),
        MiraiWallpaperPreset(
            id = "wp_05_nether_fortress",
            title = "Nether Lava Cavern",
            biome = "Nether Wastes • RTX Shaders",
            assetPath = "wallpapers/wp_05_nether_fortress.jpg",
            themeName = "Magma Orange",
            accentColor = Color(0xFFF97316)
        ),
        MiraiWallpaperPreset(
            id = "wp_06_the_end",
            title = "The End Obsidian Pillars",
            biome = "The End • Sildur's Vibrant",
            assetPath = "wallpapers/wp_06_the_end.jpg",
            themeName = "Ender Amethyst",
            accentColor = Color(0xFFA855F7)
        ),
        MiraiWallpaperPreset(
            id = "wp_07_ocean_arch",
            title = "Sunlight Ocean Arch",
            biome = "Warm Ocean • Complementary",
            assetPath = "wallpapers/wp_07_ocean_arch.jpg",
            themeName = "Ocean Azure",
            accentColor = Color(0xFF38BDF8)
        ),
        MiraiWallpaperPreset(
            id = "wp_08_moonlit_lake",
            title = "Moonlit Birch Lake",
            biome = "Night Forest • Nostalgia Shaders",
            assetPath = "wallpapers/wp_08_moonlit_lake.jpg",
            themeName = "Moonlight Indigo",
            accentColor = Color(0xFF818CF8)
        ),
        MiraiWallpaperPreset(
            id = "wp_09_flower_meadow",
            title = "Sunrise Flower Meadow",
            biome = "Flower Forest • BSL Shaders",
            assetPath = "wallpapers/wp_09_flower_meadow.jpg",
            themeName = "Rose Meadow",
            accentColor = Color(0xFFEC4899)
        ),
        MiraiWallpaperPreset(
            id = "wp_10_spruce_mist",
            title = "Spruce Lake Morning Mist",
            biome = "Old Growth Taiga • Bliss Shaders",
            assetPath = "wallpapers/wp_10_spruce_mist.jpg",
            themeName = "Taiga Lime",
            accentColor = Color(0xFF84CC16)
        ),
        MiraiWallpaperPreset(
            id = "wp_11_cherry_bee",
            title = "Honeybee Cherry Canopy",
            biome = "Cherry Canopy • Complementary",
            assetPath = "wallpapers/wp_11_cherry_bee.jpg",
            themeName = "Honey Gold",
            accentColor = Color(0xFFFBBF24)
        ),
        MiraiWallpaperPreset(
            id = "wp_12_snowy_valley",
            title = "Snowy Peaks & Cherry Valley",
            biome = "Jagged Peaks • Distant Horizons",
            assetPath = "wallpapers/wp_12_snowy_valley.jpg",
            themeName = "Alpine Sky",
            accentColor = Color(0xFF60A5FA)
        ),
        MiraiWallpaperPreset(
            id = "wp_13_frozen_glacier",
            title = "Frozen Glacier Ridge",
            biome = "Frozen Peaks • Complementary",
            assetPath = "wallpapers/wp_13_frozen_glacier.jpg",
            themeName = "Glacier Frost",
            accentColor = Color(0xFF22D3EE)
        ),
        MiraiWallpaperPreset(
            id = "wp_14_cozy_village",
            title = "Cozy Plains Village",
            biome = "Plains Village • SEUS PTGI",
            assetPath = "wallpapers/wp_14_cozy_village.jpg",
            themeName = "Harvest Emerald",
            accentColor = Color(0xFF10B981)
        ),
        MiraiWallpaperPreset(
            id = "wp_15_end_portal",
            title = "End Portal Starfield",
            biome = "Stronghold Portal • Void Shaders",
            assetPath = "wallpapers/wp_15_end_portal.jpg",
            themeName = "Portal Teal",
            accentColor = Color(0xFF14B8A6)
        ),
        MiraiWallpaperPreset(
            id = "wp_16_ocean_monument",
            title = "Deep Ocean Monument",
            biome = "Deep Ocean • Prismarine Shaders",
            assetPath = "wallpapers/wp_16_ocean_monument.jpg",
            themeName = "Prismarine Blue",
            accentColor = Color(0xFF3B82F6)
        ),
        MiraiWallpaperPreset(
            id = "wp_17_night_clouds",
            title = "Starry Night River",
            biome = "Plains Night • Solas Shaders",
            assetPath = "wallpapers/wp_17_night_clouds.jpg",
            themeName = "Starlight Violet",
            accentColor = Color(0xFF8B5CF6)
        ),
        MiraiWallpaperPreset(
            id = "wp_18_crater_harbor",
            title = "Cherry Crater Village",
            biome = "Cherry Basin • Vanilla+ Shaders",
            assetPath = "wallpapers/wp_18_crater_harbor.jpg",
            themeName = "Coral Blossom",
            accentColor = Color(0xFFFB7185)
        ),
        MiraiWallpaperPreset(
            id = "wp_19_sinkhole_falls",
            title = "Lush Sinkhole Waterfall",
            biome = "Dripstone & Lush • Iris Shaders",
            assetPath = "wallpapers/wp_19_sinkhole_falls.jpg",
            themeName = "Waterfall Mint",
            accentColor = Color(0xFF2DD4BF)
        ),
        MiraiWallpaperPreset(
            id = "wp_20_sakura_sunbeams",
            title = "Sakura Sunbeams",
            biome = "Cherry Glade • Complementary",
            assetPath = "wallpapers/wp_20_sakura_sunbeams.jpg",
            themeName = "Orchid Sunbeam",
            accentColor = Color(0xFFE879F9)
        )
    )

    var selectedWallpaperId by mutableStateOf("wp_01_lush_caves")
        private set

    fun init(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        selectedWallpaperId = prefs.getString(KEY_SELECTED_WALLPAPER, "wp_01_lush_caves") ?: "wp_01_lush_caves"
    }

    fun currentAccent(): Color {
        val themeType = AllSettings.launcherColorTheme.state
        val customColorInt = AllSettings.launcherCustomColor.state
        return if (themeType == ColorThemeType.CUSTOM) {
            Color(customColorInt)
        } else {
            Color(0xFF1BD96A)
        }
    }

    @Composable
    fun screenBackground(): Color {
        val isBgValid = LocalBackgroundViewModel.current?.isValid == true
        val opacity = AllSettings.launcherBackgroundOpacity.state
        return if (isBgValid && opacity < 100) {
            Color(0xFF121418).copy(alpha = (opacity.coerceIn(30, 92)) / 100f)
        } else {
            Color(0xFF16181C)
        }
    }

    suspend fun applyPreset(
        context: Context,
        preset: MiraiWallpaperPreset,
        backgroundViewModel: BackgroundViewModel?
    ) {
        backgroundViewModel?.importAsset(context, preset.assetPath)
        withContext(Dispatchers.Main) {
            selectedWallpaperId = preset.id
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putString(KEY_SELECTED_WALLPAPER, preset.id)
                .apply()

            AllSettings.launcherCustomColor.save(preset.accentColor.toArgb())
            AllSettings.launcherColorTheme.save(ColorThemeType.CUSTOM)
            if (AllSettings.launcherBackgroundOpacity.state >= 95) {
                AllSettings.launcherBackgroundOpacity.save(72)
            }
        }
    }

    suspend fun clearWallpaper(
        context: Context,
        backgroundViewModel: BackgroundViewModel?
    ) {
        backgroundViewModel?.delete()
        withContext(Dispatchers.Main) {
            selectedWallpaperId = "none"
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putString(KEY_SELECTED_WALLPAPER, "none")
                .apply()
            AllSettings.launcherCustomColor.save(Color(0xFF1BD96A).toArgb())
            AllSettings.launcherColorTheme.save(ColorThemeType.MIRAI)
        }
    }

    suspend fun applyCustomUri(
        context: Context,
        uri: Uri,
        backgroundViewModel: BackgroundViewModel?
    ) {
        backgroundViewModel?.import(context, uri)
        val extractedAccent = withContext(Dispatchers.IO) {
            runCatching {
                val options = BitmapFactory.Options().apply { inSampleSize = 8 }
                val bmp = context.contentResolver.openInputStream(uri)?.use {
                    BitmapFactory.decodeStream(it, null, options)
                }
                if (bmp != null) {
                    val c = extractVibrantAccent(bmp)
                    bmp.recycle()
                    c
                } else {
                    Color(0xFF1BD96A)
                }
            }.getOrDefault(Color(0xFF1BD96A))
        }
        withContext(Dispatchers.Main) {
            selectedWallpaperId = "custom"
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putString(KEY_SELECTED_WALLPAPER, "custom")
                .apply()
            AllSettings.launcherCustomColor.save(extractedAccent.toArgb())
            AllSettings.launcherColorTheme.save(ColorThemeType.CUSTOM)
            if (AllSettings.launcherBackgroundOpacity.state >= 95) {
                AllSettings.launcherBackgroundOpacity.save(72)
            }
        }
    }

    private fun extractVibrantAccent(bitmap: Bitmap): Color {
        val width = bitmap.width
        val height = bitmap.height
        if (width <= 0 || height <= 0) return Color(0xFF1BD96A)

        val buckets = FloatArray(12)
        val hueSums = FloatArray(12)
        val satSums = FloatArray(12)
        val hsv = FloatArray(3)

        val stepX = (width / 24).coerceAtLeast(1)
        val stepY = (height / 24).coerceAtLeast(1)

        var y = 0
        while (y < height) {
            var x = 0
            while (x < width) {
                val pixel = bitmap.getPixel(x, y)
                AndroidColor.colorToHSV(pixel, hsv)
                val h = hsv[0]
                val s = hsv[1]
                val v = hsv[2]
                if (s >= 0.25f && v >= 0.20f) {
                    val bucket = ((h / 30f).toInt()).coerceIn(0, 11)
                    val weight = s * s * v
                    buckets[bucket] += weight
                    hueSums[bucket] += h * weight
                    satSums[bucket] += s * weight
                }
                x += stepX
            }
            y += stepY
        }

        var bestBucket = -1
        var bestScore = 0f
        for (i in 0 until 12) {
            if (buckets[i] > bestScore) {
                bestScore = buckets[i]
                bestBucket = i
            }
        }

        if (bestBucket == -1 || bestScore <= 0.01f) {
            return Color(0xFF1BD96A)
        }

        val avgHue = (hueSums[bestBucket] / bestScore).coerceIn(0f, 360f)
        val avgSat = (satSums[bestBucket] / bestScore).coerceIn(0.65f, 0.90f)
        val outArgb = AndroidColor.HSVToColor(floatArrayOf(avgHue, avgSat, 0.92f))
        return Color(outArgb)
    }
}
