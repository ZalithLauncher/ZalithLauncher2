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

package com.movtery.zalithlauncher.ui.theme

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.graphics.drawable.RippleDrawable
import android.os.Build
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.google.android.material.button.MaterialButton
import com.google.android.material.color.utilities.CorePalette
import com.google.android.material.color.utilities.DynamicScheme
import com.google.android.material.color.utilities.Hct
import com.google.android.material.color.utilities.Variant
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.movtery.zalithlauncher.setting.AllSettings
import com.movtery.zalithlauncher.setting.enums.DarkMode

data class NativeColorScheme(
    val primary: Color,
    val onPrimary: Color,
    val primaryContainer: Color,
    val onPrimaryContainer: Color,
    val secondary: Color,
    val onSecondary: Color,
    val secondaryContainer: Color,
    val onSecondaryContainer: Color,
    val tertiary: Color,
    val onTertiary: Color,
    val tertiaryContainer: Color,
    val onTertiaryContainer: Color,
    val error: Color,
    val onError: Color,
    val errorContainer: Color,
    val onErrorContainer: Color,
    val background: Color,
    val onBackground: Color,
    val surface: Color,
    val onSurface: Color,
    val surfaceVariant: Color,
    val onSurfaceVariant: Color,
    val outline: Color,
    val outlineVariant: Color,
    val inverseSurface: Color,
    val inverseOnSurface: Color,
    val inversePrimary: Color,
    val surfaceDim: Color,
    val surfaceBright: Color,
    val surfaceContainerLowest: Color,
    val surfaceContainerLow: Color,
    val surfaceContainer: Color,
    val surfaceContainerHigh: Color,
    val surfaceContainerHighest: Color,
)

/**
 * 判断当前是否为暗色主题
 */
fun isDarkTheme(context: Context): Boolean {
    return when (AllSettings.launcherDarkMode.state) {
        DarkMode.Enable -> true
        DarkMode.Disable -> false
        DarkMode.FollowSystem -> {
            val nightModeFlags = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
            nightModeFlags == Configuration.UI_MODE_NIGHT_YES
        }
    }
}

/**
 * 从当前主题设置生成原生主题配色调色板
 */
fun getColorScheme(context: Context): NativeColorScheme {
    val darkTheme = isDarkTheme(context)
    val seedColor = getSeedColor(context, darkTheme)
    return buildColorScheme(seedColor, darkTheme)
}

@SuppressLint("RestrictedApi")
private fun buildColorScheme(seedColor: Int, darkTheme: Boolean): NativeColorScheme {
    val palettes = CorePalette.of(seedColor)

    val scheme = DynamicScheme(
        Hct.fromInt(seedColor),
        Variant.TONAL_SPOT,
        darkTheme,
        0.0,
        palettes.a1,
        palettes.a2,
        palettes.a3,
        palettes.n1,
        palettes.n2
    )

    val hctNeutral = Hct.fromInt(scheme.surface)
    val surfaceDim: Int
    val surfaceBright: Int
    val surfaceContainerLowest: Int
    val surfaceContainerLow: Int
    val surfaceContainer: Int
    val surfaceContainerHigh: Int
    val surfaceContainerHighest: Int

    if (darkTheme) {
        surfaceDim = Hct.from(hctNeutral.hue, hctNeutral.chroma, 6.0).toInt()
        surfaceBright = Hct.from(hctNeutral.hue, hctNeutral.chroma, 24.0).toInt()
        surfaceContainerLowest = Hct.from(hctNeutral.hue, hctNeutral.chroma, 4.0).toInt()
        surfaceContainerLow = Hct.from(hctNeutral.hue, hctNeutral.chroma, 10.0).toInt()
        surfaceContainer = Hct.from(hctNeutral.hue, hctNeutral.chroma, 12.0).toInt()
        surfaceContainerHigh = Hct.from(hctNeutral.hue, hctNeutral.chroma, 17.0).toInt()
        surfaceContainerHighest = Hct.from(hctNeutral.hue, hctNeutral.chroma, 22.0).toInt()
    } else {
        surfaceDim = Hct.from(hctNeutral.hue, hctNeutral.chroma, 87.0).toInt()
        surfaceBright = Hct.from(hctNeutral.hue, hctNeutral.chroma, 98.0).toInt()
        surfaceContainerLowest = Hct.from(hctNeutral.hue, hctNeutral.chroma, 100.0).toInt()
        surfaceContainerLow = Hct.from(hctNeutral.hue, hctNeutral.chroma, 96.0).toInt()
        surfaceContainer = Hct.from(hctNeutral.hue, hctNeutral.chroma, 94.0).toInt()
        surfaceContainerHigh = Hct.from(hctNeutral.hue, hctNeutral.chroma, 92.0).toInt()
        surfaceContainerHighest = Hct.from(hctNeutral.hue, hctNeutral.chroma, 90.0).toInt()
    }

    return NativeColorScheme(
        primary = Color(scheme.primary),
        onPrimary = Color(scheme.onPrimary),
        primaryContainer = Color(scheme.primaryContainer),
        onPrimaryContainer = Color(scheme.onPrimaryContainer),
        secondary = Color(scheme.secondary),
        onSecondary = Color(scheme.onSecondary),
        secondaryContainer = Color(scheme.secondaryContainer),
        onSecondaryContainer = Color(scheme.onSecondaryContainer),
        tertiary = Color(scheme.tertiary),
        onTertiary = Color(scheme.onTertiary),
        tertiaryContainer = Color(scheme.tertiaryContainer),
        onTertiaryContainer = Color(scheme.onTertiaryContainer),
        error = Color(scheme.error),
        onError = Color(scheme.onError),
        errorContainer = Color(scheme.errorContainer),
        onErrorContainer = Color(scheme.onErrorContainer),
        background = Color(scheme.background),
        onBackground = Color(scheme.onBackground),
        surface = Color(scheme.surface),
        onSurface = Color(scheme.onSurface),
        surfaceVariant = Color(scheme.surfaceVariant),
        onSurfaceVariant = Color(scheme.onSurfaceVariant),
        outline = Color(scheme.outline),
        outlineVariant = Color(scheme.outlineVariant),
        inverseSurface = Color(scheme.inverseSurface),
        inverseOnSurface = Color(scheme.inverseOnSurface),
        inversePrimary = Color(scheme.inversePrimary),
        surfaceDim = Color(surfaceDim),
        surfaceBright = Color(surfaceBright),
        surfaceContainerLowest = Color(surfaceContainerLowest),
        surfaceContainerLow = Color(surfaceContainerLow),
        surfaceContainer = Color(surfaceContainer),
        surfaceContainerHigh = Color(surfaceContainerHigh),
        surfaceContainerHighest = Color(surfaceContainerHighest),
    )
}

private fun getSeedColor(context: Context, darkTheme: Boolean): Int {
    return when (AllSettings.launcherColorTheme.state) {
        ColorThemeType.DYNAMIC -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    if (darkTheme) {
                        context.resources.getColor(android.R.color.system_primary_dark, context.theme)
                    } else {
                        context.resources.getColor(android.R.color.system_primary_light, context.theme)
                    }
                } else {
                    context.resources.getColor(android.R.color.system_accent1_200, context.theme)
                }
            } else {
                getPredefinedSeedColor(ColorThemeType.EMBERMIRE, darkTheme)
            }
        }
        ColorThemeType.CUSTOM -> AllSettings.launcherCustomColor.state
        else -> getPredefinedSeedColor(AllSettings.launcherColorTheme.state, darkTheme)
    }
}

private fun getPredefinedSeedColor(theme: ColorThemeType, darkTheme: Boolean): Int {
    return when (theme) {
        ColorThemeType.EMBERMIRE -> if (darkTheme) 0xFFFFB598.toInt() else 0xFFA63A17.toInt()
        ColorThemeType.VELVET_ROSE -> if (darkTheme) 0xFFF9B2D2.toInt() else 0xFF723D57.toInt()
        ColorThemeType.MISTWAVE -> if (darkTheme) 0xFFCEF3F9.toInt() else 0xFF426469.toInt()
        ColorThemeType.GLACIER -> if (darkTheme) 0xFF73D2FB.toInt() else 0xFF006684.toInt()
        ColorThemeType.VERDANTFIELD -> if (darkTheme) 0xFFD2C972.toInt() else 0xFF676014.toInt()
        ColorThemeType.URBAN_ASH -> if (darkTheme) 0xFFC7C6C6.toInt() else 0xFF5E5E5F.toInt()
        ColorThemeType.VERDANT_DAWN -> if (darkTheme) 0xFF8ED88E.toInt() else 0xFF004814.toInt()
        else -> 0xFFA63A17.toInt()
    }
}

fun MaterialAlertDialogBuilder.showThemed(): AlertDialog {
    val colors = getColorScheme(context)
    background?.setTint(colors.surfaceContainerHigh.toArgb())

    fun Button.applyThemeColors(
        colors: NativeColorScheme,
    ) {
        val contentColor = colors.primary.toArgb()
        val rippleColor = colors.secondaryContainer.copy(alpha = 0.5f).toArgb()

        this@applyThemeColors.setTextColor(contentColor)
        if (this is MaterialButton) {
            this@applyThemeColors.rippleColor = ColorStateList.valueOf(rippleColor)
        } else {
            (this@applyThemeColors.background as? RippleDrawable)?.setColor(ColorStateList.valueOf(rippleColor))
        }
    }

    return create().apply {
        setOnShowListener {
            findViewById<TextView>(androidx.appcompat.R.id.alertTitle)?.setTextColor(colors.onSurface.toArgb())
            findViewById<TextView>(android.R.id.message)?.setTextColor(colors.onSurfaceVariant.toArgb())
            getButton(AlertDialog.BUTTON_POSITIVE)?.applyThemeColors(colors)
            getButton(AlertDialog.BUTTON_NEGATIVE)?.applyThemeColors(colors)
            getButton(AlertDialog.BUTTON_NEUTRAL)?.applyThemeColors(colors)
        }
    }.also { it.show() }
}