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
 * along with this program. If not, see <https://www.gnu.org/licenses/gpl-3.0.txt>.
 */

package com.movtery.zalithlauncher.ui.screens.main

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.game.account.Account
import com.movtery.zalithlauncher.game.account.AccountsManager
import com.movtery.zalithlauncher.ui.screens.content.elements.PlayerFace
import com.movtery.zalithlauncher.ui.theme.MiraiThemeManager

/** Primary destinations kept visible while nested launcher screens are open. */
enum class LauncherSection {
    HOME,
    LIBRARY,
    DISCOVER,
    WALLPAPERS,
    MULTIPLAYER,
    SETTINGS,
    ACCOUNTS
}

private val ModrinthRailBg = Color(0xFF121418)
private val ModrinthRailDivider = Color(0xFF222630)

@Composable
fun MiraiNavigationRail(
    selectedSection: LauncherSection?,
    onNavigate: (LauncherSection) -> Unit,
    onCreateInstance: () -> Unit,
    onAccountClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val account by AccountsManager.currentAccountFlow.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()
    val activeAccent = MiraiThemeManager.currentAccent()

    Row(modifier = modifier.fillMaxHeight()) {
        Surface(
            modifier = Modifier
                .width(56.dp)
                .fillMaxHeight(),
            color = ModrinthRailBg.copy(alpha = 0.94f),
            contentColor = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .verticalScroll(scrollState)
                    .padding(vertical = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top: M Logo + Primary Nav Icons (Home, Library, Discover, Wallpapers)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Mirai 'M' Logo Button
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(11.dp))
                            .clickable(role = Role.Button) { onNavigate(LauncherSection.HOME) }
                            .semantics { contentDescription = "Mirai Home" },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_mirai_mark),
                            contentDescription = "Mirai",
                            tint = activeAccent,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    Spacer(Modifier.height(2.dp))

                    RailIconItem(
                        iconRes = R.drawable.ic_home_filled,
                        label = "Home",
                        selected = selectedSection == LauncherSection.HOME,
                        accentColor = activeAccent,
                        onClick = { onNavigate(LauncherSection.HOME) }
                    )

                    RailIconItem(
                        iconRes = R.drawable.ic_dashboard_filled,
                        label = "Library",
                        selected = selectedSection == LauncherSection.LIBRARY,
                        accentColor = activeAccent,
                        onClick = { onNavigate(LauncherSection.LIBRARY) }
                    )

                    RailIconItem(
                        iconRes = R.drawable.ic_public,
                        label = "Discover",
                        selected = selectedSection == LauncherSection.DISCOVER,
                        accentColor = activeAccent,
                        onClick = { onNavigate(LauncherSection.DISCOVER) }
                    )

                    RailIconItem(
                        iconRes = R.drawable.ic_format_paint_outlined,
                        label = "Wallpapers",
                        selected = selectedSection == LauncherSection.WALLPAPERS,
                        accentColor = activeAccent,
                        onClick = { onNavigate(LauncherSection.WALLPAPERS) }
                    )
                }

                // Bottom: Settings Gear
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    HorizontalDivider(
                        modifier = Modifier.width(28.dp),
                        color = ModrinthRailDivider
                    )

                    RailIconItem(
                        iconRes = R.drawable.ic_settings_filled,
                        label = "Settings",
                        selected = selectedSection == LauncherSection.SETTINGS,
                        accentColor = activeAccent,
                        onClick = { onNavigate(LauncherSection.SETTINGS) }
                    )
                }
            }
        }

        // Subtle 1dp vertical border separating rail from screen content
        Box(
            modifier = Modifier
                .width(1.dp)
                .fillMaxHeight()
                .background(ModrinthRailDivider)
        )
    }
}

@Composable
private fun RailIconItem(
    iconRes: Int,
    label: String,
    selected: Boolean,
    accentColor: Color,
    onClick: () -> Unit
) {
    val containerColor by animateColorAsState(
        targetValue = if (selected) accentColor.copy(alpha = 0.18f) else Color.Transparent,
        animationSpec = tween(140),
        label = "railContainerColor"
    )
    val iconTint by animateColorAsState(
        targetValue = if (selected) accentColor else Color(0xFF9CA3AF),
        animationSpec = tween(140),
        label = "railIconTint"
    )

    Box(
        modifier = Modifier
            .size(width = 42.dp, height = 38.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(containerColor)
            .then(
                if (selected) {
                    Modifier.border(
                        BorderStroke(1.5.dp, accentColor.copy(alpha = 0.75f)),
                        RoundedCornerShape(12.dp)
                    )
                } else Modifier
            )
            .clickable(role = Role.Tab, onClick = onClick)
            .semantics { contentDescription = label },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = label,
            tint = iconTint,
            modifier = Modifier.size(21.dp)
        )
    }
}

@Composable
private fun AccountAvatarRailButton(
    account: Account?,
    selected: Boolean,
    onClick: () -> Unit
) {
    val ringColor by animateColorAsState(
        targetValue = if (selected) ModrinthEmerald else Color(0xFF2D323E),
        animationSpec = tween(150),
        label = "accountAvatarRing"
    )

    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(Color(0xFF1E222B))
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = ringColor,
                shape = CircleShape
            )
            .clickable(role = Role.Button, onClick = onClick)
            .semantics { contentDescription = account?.username ?: "Accounts" },
        contentAlignment = Alignment.Center
    ) {
        PlayerFace(
            modifier = Modifier
                .size(30.dp)
                .clip(CircleShape),
            account = account
        )
    }
}
