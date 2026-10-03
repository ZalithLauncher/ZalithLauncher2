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
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.game.account.Account
import com.movtery.zalithlauncher.game.account.AccountsManager
import com.movtery.zalithlauncher.setting.AllSettings
import com.movtery.zalithlauncher.ui.screens.content.elements.PlayerFace

/** Primary destinations kept visible while nested launcher screens are open. */
enum class LauncherSection {
    HOME,
    DISCOVER,
    LIBRARY,
    MULTIPLAYER,
    SETTINGS,
    ACCOUNTS
}

private val ModrinthRailBg = Color(0xFF121418)
private val ModrinthGreen = Color(0xFF1BD96A)
private val ModrinthGreenSurface = Color(0xFF163826)
private val ModrinthOnGreen = Color(0xFF06210F)

@Composable
fun MiraiNavigationRail(
    selectedSection: LauncherSection?,
    onNavigate: (LauncherSection) -> Unit,
    onCreateInstance: () -> Unit,
    onAccountClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val account by AccountsManager.currentAccountFlow.collectAsStateWithLifecycle()
    val quiet = AllSettings.miraiQuietMode.state
    val scrollState = rememberScrollState()

    Surface(
        modifier = modifier
            .width(142.dp)
            .fillMaxHeight(),
        color = ModrinthRailBg,
        contentColor = Color.White,
        border = BorderStroke(1.dp, Color(0xFF232730))
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .verticalScroll(scrollState)
                .padding(horizontal = 10.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.Start
        ) {
            // Mirai Launcher Brand Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .combinedClickable(
                        onClick = { onNavigate(LauncherSection.HOME) },
                        onLongClick = { AllSettings.miraiQuietMode.save(!quiet) }
                    )
                    .padding(horizontal = 6.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_mirai_mark),
                    contentDescription = stringResource(R.string.launcher_brand_name),
                    tint = Color.Unspecified,
                    modifier = Modifier.size(30.dp)
                )
                Column(verticalArrangement = Arrangement.spacedBy((-2).dp)) {
                    Text(
                        text = "Mirai",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1
                    )
                    Text(
                        text = "Launcher",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF9CA3AF),
                        maxLines = 1
                    )
                }
            }

            Spacer(Modifier.height(8.dp))
            HorizontalDivider(color = Color(0xFF232730))
            Spacer(Modifier.height(8.dp))

            LauncherSectionItem(
                icon = R.drawable.ic_home_filled,
                label = "Home",
                selected = selectedSection == LauncherSection.HOME,
                onClick = { onNavigate(LauncherSection.HOME) }
            )
            if (!quiet) LauncherSectionItem(
                icon = R.drawable.ic_search,
                label = "Discover",
                selected = selectedSection == LauncherSection.DISCOVER,
                onClick = { onNavigate(LauncherSection.DISCOVER) }
            )
            if (!quiet) LauncherSectionItem(
                icon = R.drawable.ic_dashboard_filled,
                label = "Library",
                selected = selectedSection == LauncherSection.LIBRARY,
                onClick = { onNavigate(LauncherSection.LIBRARY) }
            )
            if (!quiet) LauncherSectionItem(
                icon = R.drawable.ic_group_filled,
                label = "Multiplayer",
                selected = selectedSection == LauncherSection.MULTIPLAYER,
                onClick = { onNavigate(LauncherSection.MULTIPLAYER) }
            )

            Spacer(Modifier.weight(1f))
            Spacer(Modifier.height(8.dp))

            if (!quiet) {
                CreateInstanceRailButton(onClick = onCreateInstance)
                Spacer(Modifier.height(6.dp))
            }

            if (!quiet) LauncherSectionItem(
                icon = R.drawable.ic_settings_filled,
                label = "Settings",
                selected = selectedSection == LauncherSection.SETTINGS,
                onClick = { onNavigate(LauncherSection.SETTINGS) }
            )

            Spacer(Modifier.height(6.dp))
            HorizontalDivider(color = Color(0xFF232730))
            Spacer(Modifier.height(6.dp))
            if (!quiet) {
                AccountShortcut(
                    account = account,
                    selected = selectedSection == LauncherSection.ACCOUNTS,
                    onClick = onAccountClick
                )
            }
        }
    }
}

@Composable
private fun CreateInstanceRailButton(onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "createInstanceScale"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(RoundedCornerShape(11.dp))
            .background(ModrinthGreen)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                role = Role.Button,
                onClick = onClick
            )
            .padding(horizontal = 10.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_add),
            contentDescription = null,
            tint = ModrinthOnGreen,
            modifier = Modifier.size(16.dp)
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = "CREATE\nINSTANCE",
            color = ModrinthOnGreen,
            fontSize = 10.sp,
            lineHeight = 11.sp,
            fontWeight = FontWeight.ExtraBold,
            maxLines = 2
        )
    }
}

@Composable
private fun LauncherSectionItem(
    icon: Int,
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "railItemScale"
    )
    val bgColor by animateColorAsState(
        targetValue = if (selected) ModrinthGreenSurface else Color.Transparent,
        animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing),
        label = "railItemBg"
    )
    val contentColor by animateColorAsState(
        targetValue = if (selected) ModrinthGreen else Color(0xFFD1D5DB),
        animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing),
        label = "railItemFg"
    )
    val indicatorHeight by animateDpAsState(
        targetValue = if (selected) 20.dp else 0.dp,
        animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing),
        label = "railIndicator"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
            .height(40.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(RoundedCornerShape(11.dp))
            .background(bgColor)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                role = Role.Button,
                onClick = onClick
            )
            .semantics { contentDescription = label }
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(9.dp)
    ) {
        if (indicatorHeight > 0.dp) {
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(indicatorHeight)
                    .clip(RoundedCornerShape(2.dp))
                    .background(ModrinthGreen)
            )
        }
        Icon(
            painter = painterResource(icon),
            contentDescription = null,
            tint = contentColor,
            modifier = Modifier.size(19.dp)
        )
        Text(
            text = label,
            color = if (selected) ModrinthGreen else Color(0xFFE5E7EB),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun AccountShortcut(
    account: Account?,
    selected: Boolean,
    onClick: () -> Unit
) {
    val description = account?.username ?: stringResource(R.string.account_add_new_account)
    val borderColor by animateColorAsState(
        targetValue = if (selected) ModrinthGreen else Color(0xFF282C35),
        animationSpec = tween(220),
        label = "accountBorder"
    )
    val containerColor by animateColorAsState(
        targetValue = if (selected) ModrinthGreenSurface else Color(0xFF191C22),
        animationSpec = tween(220),
        label = "accountBg"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(containerColor)
            .border(1.5.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable(role = Role.Button, onClick = onClick)
            .semantics { contentDescription = description }
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (account != null) {
            PlayerFace(account = account, avatarSize = 28.dp)
        } else {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF262A33)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_person_outlined),
                    contentDescription = null,
                    tint = Color(0xFF9CA3AF),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = account?.username ?: "Add Account",
                color = Color.White,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = if (account != null) "Online / Ready" else "Tap to sign in",
                color = if (account != null) ModrinthGreen else Color(0xFF9CA3AF),
                fontSize = 9.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
