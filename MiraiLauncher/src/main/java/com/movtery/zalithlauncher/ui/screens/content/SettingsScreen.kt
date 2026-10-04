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

package com.movtery.zalithlauncher.ui.screens.content

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.ui.base.BaseScreen
import com.movtery.zalithlauncher.ui.components.fadeEdge
import com.movtery.zalithlauncher.ui.screens.NestedNavKey
import com.movtery.zalithlauncher.ui.screens.NormalNavKey
import com.movtery.zalithlauncher.ui.screens.TitledNavKey
import com.movtery.zalithlauncher.ui.screens.content.settings.AboutInfoScreen
import com.movtery.zalithlauncher.ui.screens.content.settings.ControlManageScreen
import com.movtery.zalithlauncher.ui.screens.content.settings.ControlSettingsScreen
import com.movtery.zalithlauncher.ui.screens.content.settings.GameSettingsScreen
import com.movtery.zalithlauncher.ui.screens.content.settings.GamepadSettingsScreen
import com.movtery.zalithlauncher.ui.screens.content.settings.JavaManageScreen
import com.movtery.zalithlauncher.ui.screens.content.settings.LauncherSettingsScreen
import com.movtery.zalithlauncher.ui.screens.content.settings.RendererSettingsScreen
import com.movtery.zalithlauncher.ui.screens.main.WallpaperPage
import com.movtery.zalithlauncher.ui.screens.navigateOnce
import com.movtery.zalithlauncher.ui.screens.onBack
import com.movtery.zalithlauncher.ui.screens.rememberTransitionSpec
import com.movtery.zalithlauncher.ui.theme.MiraiThemeManager
import com.movtery.zalithlauncher.utils.animation.swapAnimateDpAsState
import com.movtery.zalithlauncher.viewmodel.ErrorViewModel
import com.movtery.zalithlauncher.viewmodel.EventViewModel
import com.movtery.zalithlauncher.viewmodel.ScreenBackStackViewModel

private data class ModrinthSettingNavItem(
    val key: TitledNavKey,
    val iconRes: Int,
    val textRes: Int,
    val division: Boolean = false,
    val customLabel: String? = null
)

@Composable
fun SettingsScreen(
    key: NestedNavKey.Settings,
    backStackViewModel: ScreenBackStackViewModel,
    openLicenseScreen: (raw: Int) -> Unit,
    eventViewModel: EventViewModel,
    submitError: (ErrorViewModel.ThrowableMessage) -> Unit
) {
    BaseScreen(
        screenKey = key,
        currentKey = backStackViewModel.mainScreen.currentKey
    ) { isVisible ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TabMenu(
                modifier = Modifier.fillMaxHeight(),
                isVisible = isVisible,
                settingsScreenKey = backStackViewModel.settingsScreen.currentKey,
                navigateTo = { settingKey ->
                    key.backStack.navigateOnce(settingKey)
                }
            )
            NavigationUI(
                key = key,
                mainScreenKey = backStackViewModel.mainScreen.currentKey,
                settingsScreenKey = backStackViewModel.settingsScreen.currentKey,
                onCurrentKeyChange = { newKey ->
                    backStackViewModel.settingsScreen.currentKey = newKey
                },
                openLicenseScreen = openLicenseScreen,
                eventViewModel = eventViewModel,
                submitError = submitError,
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f)
            )
        }
    }
}

private val settingItems = listOf(
    ModrinthSettingNavItem(NormalNavKey.Settings.Renderer, R.drawable.ic_video_settings, R.string.settings_tab_renderer),
    ModrinthSettingNavItem(NormalNavKey.Settings.Game, R.drawable.ic_rocket_launch_filled, R.string.settings_tab_game),
    ModrinthSettingNavItem(NormalNavKey.Settings.JavaManager, R.drawable.ic_java, R.string.settings_tab_java_manage),
    ModrinthSettingNavItem(NormalNavKey.Settings.Wallpapers, R.drawable.ic_format_paint_outlined, R.string.settings_tab_launcher, division = true, customLabel = "Wallpapers"),
    ModrinthSettingNavItem(NormalNavKey.Settings.Launcher, R.drawable.ic_setting_launcher, R.string.settings_tab_launcher),
    ModrinthSettingNavItem(NormalNavKey.Settings.Control, R.drawable.ic_videogame_asset_outlined, R.string.settings_tab_control, division = true),
    ModrinthSettingNavItem(NormalNavKey.Settings.ControlManager, R.drawable.ic_videogame_asset_outlined, R.string.settings_tab_control_manage),
    ModrinthSettingNavItem(NormalNavKey.Settings.Gamepad, R.drawable.ic_sports_esports_outlined, R.string.settings_tab_gamepad),
    ModrinthSettingNavItem(NormalNavKey.Settings.AboutInfo, R.drawable.ic_info_outlined, R.string.settings_tab_info_about, division = true)
)

@Composable
private fun TabMenu(
    isVisible: Boolean,
    settingsScreenKey: TitledNavKey?,
    navigateTo: (TitledNavKey) -> Unit,
    modifier: Modifier = Modifier
) {
    val activeAccent = MiraiThemeManager.currentAccent()
    val xOffset by swapAnimateDpAsState(
        targetValue = (-40).dp,
        swapIn = isVisible,
        isHorizontal = true
    )

    val scrollState = rememberScrollState()
    Surface(
        modifier = modifier
            .width(156.dp)
            .offset { IntOffset(x = xOffset.roundToPx(), y = 0) },
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFF1A1D24).copy(alpha = 0.92f),
        border = BorderStroke(1.dp, Color(0xFF2B2F3A))
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .fadeEdge(scrollState)
                .verticalScroll(scrollState)
                .padding(horizontal = 8.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            settingItems.forEach { item ->
                if (item.division) {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 3.dp),
                        color = Color(0xFF282C36)
                    )
                }

                val selected = settingsScreenKey === item.key
                val bgColor by animateColorAsState(
                    targetValue = if (selected) activeAccent else Color.Transparent,
                    animationSpec = tween(140),
                    label = "settingsTabBg"
                )
                val fgColor by animateColorAsState(
                    targetValue = if (selected) Color(0xFF06210F) else Color(0xFFD1D5DB),
                    animationSpec = tween(140),
                    label = "settingsTabFg"
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(34.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(bgColor)
                        .clickable { navigateTo(item.key) }
                        .padding(horizontal = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        painter = painterResource(item.iconRes),
                        contentDescription = null,
                        tint = fgColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = item.customLabel ?: stringResource(item.textRes),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.Medium,
                        color = fgColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun NavigationUI(
    key: NestedNavKey.Settings,
    mainScreenKey: TitledNavKey?,
    settingsScreenKey: TitledNavKey?,
    onCurrentKeyChange: (TitledNavKey?) -> Unit,
    openLicenseScreen: (raw: Int) -> Unit,
    eventViewModel: EventViewModel,
    submitError: (ErrorViewModel.ThrowableMessage) -> Unit,
    modifier: Modifier = Modifier
) {
    val backStack: NavBackStack<TitledNavKey> = key.backStack
    val stackTopKey = backStack.lastOrNull()
    LaunchedEffect(stackTopKey) {
        onCurrentKeyChange(stackTopKey)
    }

    if (backStack.isNotEmpty()) {
        NavDisplay(
            backStack = backStack,
            modifier = modifier,
            onBack = {
                onBack(backStack)
            },
            transitionSpec = rememberTransitionSpec(),
            popTransitionSpec = rememberTransitionSpec(),
            entryProvider = entryProvider {
                entry<NormalNavKey.Settings.Renderer> {
                    RendererSettingsScreen(key, settingsScreenKey, mainScreenKey, eventViewModel)
                }
                entry<NormalNavKey.Settings.Game> {
                    GameSettingsScreen(key, settingsScreenKey, mainScreenKey, eventViewModel)
                }
                entry<NormalNavKey.Settings.Control> {
                    ControlSettingsScreen(key, settingsScreenKey, mainScreenKey, eventViewModel, submitError)
                }
                entry<NormalNavKey.Settings.Gamepad> {
                    GamepadSettingsScreen(key, settingsScreenKey, mainScreenKey, eventViewModel)
                }
                entry<NormalNavKey.Settings.Launcher> {
                    LauncherSettingsScreen(
                        key = key,
                        settingsScreenKey = settingsScreenKey,
                        mainScreenKey = mainScreenKey,
                        submitError = submitError,
                    )
                }
                entry<NormalNavKey.Settings.Wallpapers> {
                    WallpaperPage(modifier = Modifier.fillMaxSize())
                }
                entry<NormalNavKey.Settings.JavaManager> {
                    JavaManageScreen(key, settingsScreenKey, mainScreenKey, eventViewModel, submitError)
                }
                entry<NormalNavKey.Settings.ControlManager> {
                    ControlManageScreen(key, settingsScreenKey, mainScreenKey, eventViewModel, submitError)
                }
                entry<NormalNavKey.Settings.AboutInfo> {
                    AboutInfoScreen(
                        key = key,
                        settingsScreenKey = settingsScreenKey,
                        mainScreenKey = mainScreenKey,
                        checkUpdate = {
                            eventViewModel.sendEvent(EventViewModel.Event.CheckUpdate)
                        },
                        openLicense = openLicenseScreen,
                        openLink = { url ->
                            eventViewModel.sendEvent(EventViewModel.Event.OpenLink(url))
                        }
                    )
                }
            }
        )
    } else {
        Box(modifier)
    }
}
