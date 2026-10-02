package com.movtery.zalithlauncher.ui.screens.main

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.game.account.Account
import com.movtery.zalithlauncher.game.account.AccountsManager
import com.movtery.zalithlauncher.ui.screens.content.elements.PlayerFace

enum class LauncherSection {
    HOME,
    DISCOVER,
    LIBRARY,
    MULTIPLAYER,
    SETTINGS,
    SKINS
}

@Composable
fun MiraiNavigationRail(
    selectedSection: LauncherSection?,
    onNavigate: (LauncherSection) -> Unit,
    onCreateInstance: () -> Unit,
    onAccountClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val account by AccountsManager.currentAccountFlow.collectAsStateWithLifecycle()

    Surface(
        modifier = modifier
            .width(68.dp)
            .fillMaxHeight(),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(horizontal = 8.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_mirai_mark),
                    contentDescription = stringResource(R.string.launcher_brand_name),
                    tint = androidx.compose.ui.graphics.Color.Unspecified,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(Modifier.height(10.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))
            Spacer(Modifier.height(8.dp))

            LauncherSectionItem(
                icon = R.drawable.ic_home_filled,
                label = stringResource(R.string.generic_main_menu),
                selected = selectedSection == LauncherSection.HOME,
                onClick = { onNavigate(LauncherSection.HOME) }
            )
            LauncherSectionItem(
                icon = R.drawable.ic_search,
                label = stringResource(R.string.generic_download),
                selected = selectedSection == LauncherSection.DISCOVER,
                onClick = { onNavigate(LauncherSection.DISCOVER) }
            )
            LauncherSectionItem(
                icon = R.drawable.ic_checkroom,
                label = "MCSkin",
                selected = selectedSection == LauncherSection.SKINS,
                onClick = { onNavigate(LauncherSection.SKINS) }
            )
            LauncherSectionItem(
                icon = R.drawable.ic_dashboard_filled,
                label = stringResource(R.string.page_title_version_list),
                selected = selectedSection == LauncherSection.LIBRARY,
                onClick = { onNavigate(LauncherSection.LIBRARY) }
            )
            LauncherSectionItem(
                icon = R.drawable.ic_group_filled,
                label = stringResource(R.string.terracotta),
                selected = selectedSection == LauncherSection.MULTIPLAYER,
                onClick = { onNavigate(LauncherSection.MULTIPLAYER) }
            )

            Spacer(Modifier.weight(1f))

            LauncherSectionItem(
                icon = R.drawable.ic_add,
                label = stringResource(R.string.home_new_instance),
                selected = false,
                highlighted = true,
                onClick = onCreateInstance
            )
            LauncherSectionItem(
                icon = R.drawable.ic_settings_filled,
                label = stringResource(R.string.generic_setting),
                selected = selectedSection == LauncherSection.SETTINGS,
                onClick = { onNavigate(LauncherSection.SETTINGS) }
            )

            Spacer(Modifier.height(6.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))
            Spacer(Modifier.height(6.dp))
            AccountShortcut(account = account, onClick = onAccountClick)
        }
    }
}

@Composable
private fun LauncherSectionItem(
    icon: Int,
    label: String,
    selected: Boolean,
    highlighted: Boolean = false,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(13.dp)
    val backgroundColor = when {
        selected -> MaterialTheme.colorScheme.primary
        highlighted -> MaterialTheme.colorScheme.primaryContainer
        else -> MaterialTheme.colorScheme.surfaceContainerLow
    }
    val iconColor = when {
        selected -> MaterialTheme.colorScheme.onPrimary
        highlighted -> MaterialTheme.colorScheme.onPrimaryContainer
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp)
            .height(42.dp)
            .clip(shape)
            .background(backgroundColor)
            .clickable(role = Role.Button, onClick = onClick)
            .semantics { contentDescription = label },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(21.dp)
        )
    }
}

@Composable
private fun AccountShortcut(
    account: Account?,
    onClick: () -> Unit
) {
    val description = account?.username ?: stringResource(R.string.account_add_new_account)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .clip(RoundedCornerShape(13.dp))
            .clickable(role = Role.Button, onClick = onClick)
            .semantics { contentDescription = description },
        contentAlignment = Alignment.Center
    ) {
        if (account != null) {
            PlayerFace(account = account, avatarSize = 34.dp)
        } else {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(50))
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_person_outlined),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(19.dp)
                )
            }
        }
    }
}
