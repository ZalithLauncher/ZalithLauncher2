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

package com.movtery.zalithlauncher.ui.screens.content.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.setting.AllSettings
import com.movtery.zalithlauncher.setting.enums.DarkMode
import com.movtery.zalithlauncher.ui.base.BaseScreen
import com.movtery.zalithlauncher.ui.components.AnimatedColumn
import com.movtery.zalithlauncher.ui.components.TitleAndSummary
import com.movtery.zalithlauncher.ui.components.animated
import com.movtery.zalithlauncher.ui.components.verticalScrollWithBar
import com.movtery.zalithlauncher.ui.screens.NestedNavKey
import com.movtery.zalithlauncher.ui.screens.NormalNavKey
import com.movtery.zalithlauncher.ui.screens.TitledNavKey
import com.movtery.zalithlauncher.ui.screens.content.settings.layouts.CardPosition
import com.movtery.zalithlauncher.ui.screens.content.settings.layouts.EnumSettingsCard
import com.movtery.zalithlauncher.ui.screens.content.settings.layouts.ListSettingsCard
import com.movtery.zalithlauncher.ui.screens.content.settings.layouts.SettingsCard
import com.movtery.zalithlauncher.ui.screens.content.settings.layouts.SettingsCardColumn
import com.movtery.zalithlauncher.ui.theme.ColorThemeType
import com.movtery.zalithlauncher.ui.theme.DesignStyleType
import com.movtery.zalithlauncher.ui.theme.NavRailSide

/**
 * Tema desain yang bisa dipilih di tab Tampilan.
 * Memilih tema berarti mengatur [AllSettings.launcherColorTheme].
 */
private class DesignTheme(
    val type: ColorThemeType,
    val nameRes: Int,
    val descRes: Int,
    val swatch: List<Color>
)

private val designThemes = listOf(
    DesignTheme(
        type = ColorThemeType.NEV_LAVA,
        nameRes = R.string.theme_color_nev_lava,
        descRes = R.string.settings_display_theme_nev_lava_desc,
        swatch = listOf(Color(0xFF2A0C0A), Color(0xFFB3201A), Color(0xFFFF5A3D))
    ),
    DesignTheme(
        type = ColorThemeType.CRYOGENIC,
        nameRes = R.string.theme_color_cryogenic,
        descRes = R.string.settings_display_theme_cryogenic_desc,
        swatch = listOf(Color(0xFF08131E), Color(0xFF4DD8F0), Color(0xFF9CCBFF))
    )
)

@Composable
fun DisplaySettingsScreen(
    key: NestedNavKey.Settings,
    settingsScreenKey: TitledNavKey?,
    mainScreenKey: TitledNavKey?
) {
    BaseScreen(
        Triple(key, mainScreenKey, false),
        Triple(NormalNavKey.Settings.Display, settingsScreenKey, false)
    ) { isVisible ->
        AnimatedColumn(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScrollWithBar(state = rememberScrollState())
                .padding(all = 12.dp),
            isVisible = isVisible
        ) {
            AnimatedItem { ctx ->
                SettingsCardColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .animated(ctx)
                ) {
                    SettingsCard(
                        modifier = Modifier.fillMaxWidth(),
                        position = CardPosition.Top
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(all = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            TitleAndSummary(
                                title = stringResource(R.string.settings_display_design_theme_title),
                                summary = stringResource(R.string.settings_display_design_theme_summary)
                            )

                            val selectedType = AllSettings.launcherColorTheme.state
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                designThemes.forEach { theme ->
                                    DesignThemeCard(
                                        modifier = Modifier.weight(1f),
                                        theme = theme,
                                        selected = selectedType == theme.type,
                                        onClick = {
                                            AllSettings.launcherColorTheme.save(theme.type)
                                        }
                                    )
                                }
                            }
                        }
                    }

                    EnumSettingsCard(
                        modifier = Modifier.fillMaxWidth(),
                        position = CardPosition.Middle,
                        unit = AllSettings.launcherDesignStyle,
                        title = stringResource(R.string.settings_display_style_title),
                        summary = stringResource(R.string.settings_display_style_summary),
                        entries = DesignStyleType.entries,
                        getRadioEnable = { true },
                        getRadioText = { type ->
                            when (type) {
                                DesignStyleType.NEV -> stringResource(R.string.settings_display_style_nev)
                                DesignStyleType.CRYOGENIC -> stringResource(R.string.settings_display_style_cryogenic)
                            }
                        }
                    )

                    EnumSettingsCard(
                        modifier = Modifier.fillMaxWidth(),
                        position = CardPosition.Middle,
                        unit = AllSettings.launcherNavRailSide,
                        title = stringResource(R.string.settings_display_nav_side_title),
                        summary = stringResource(R.string.settings_display_nav_side_summary),
                        entries = NavRailSide.entries,
                        getRadioEnable = { true },
                        getRadioText = { side ->
                            when (side) {
                                NavRailSide.START -> stringResource(R.string.settings_display_nav_side_start)
                                NavRailSide.END -> stringResource(R.string.settings_display_nav_side_end)
                            }
                        }
                    )

                    ListSettingsCard(
                        modifier = Modifier.fillMaxWidth(),
                        position = CardPosition.Bottom,
                        unit = AllSettings.launcherDarkMode,
                        items = DarkMode.entries,
                        title = stringResource(R.string.settings_launcher_dark_mode_title),
                        getItemText = { stringResource(it.textRes) }
                    )
                }
            }
        }
    }
}

@Composable
private fun DesignThemeCard(
    theme: DesignTheme,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(20.dp)
    val borderStroke = if (selected) {
        //garis tepi bercahaya dengan gradasi dari warna tema
        BorderStroke(2.dp, Brush.linearGradient(theme.swatch.drop(1)))
    } else {
        BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    }

    Column(
        modifier = modifier
            .clip(shape)
            .border(borderStroke, shape)
            .clickable(onClick = onClick)
            .padding(all = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Brush.linearGradient(theme.swatch))
        )
        Text(
            text = stringResource(theme.nameRes),
            style = MaterialTheme.typography.titleSmall,
            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = stringResource(theme.descRes),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
