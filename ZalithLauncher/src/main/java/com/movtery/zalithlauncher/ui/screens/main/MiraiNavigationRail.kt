package com.movtery.zalithlauncher.ui.screens.main

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
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
            .width(208.dp)
            .fillMaxHeight(),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(horizontal = 12.dp, vertical = 14.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_mirai_mark),
                    contentDescription = null,
                    tint = Color.Unspecified,
                    modifier = Modifier.size(28.dp)
                )
                Text(
                    text = stringResource(R.string.launcher_brand_name),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(Modifier.height(16.dp))

            RailItem(
                icon = R.drawable.ic_home_filled,
                label = "Play",
                selected = selectedSection == LauncherSection.HOME,
                onClick = { onNavigate(LauncherSection.HOME) }
            )
            RailItem(
                icon = R.drawable.ic_search,
                label = "Discover",
                selected = selectedSection == LauncherSection.DISCOVER,
                onClick = { onNavigate(LauncherSection.DISCOVER) }
            )
            RailItem(
                icon = R.drawable.ic_dashboard_filled,
                label = "Library",
                selected = selectedSection == LauncherSection.LIBRARY,
                onClick = { onNavigate(LauncherSection.LIBRARY) }
            )
            RailItem(
                icon = R.drawable.ic_checkroom,
                label = "Skins",
                selected = selectedSection == LauncherSection.SKINS,
                onClick = { onNavigate(LauncherSection.SKINS) }
            )
            RailItem(
                icon = R.drawable.ic_group_filled,
                label = "Servers",
                selected = selectedSection == LauncherSection.MULTIPLAYER,
                onClick = { onNavigate(LauncherSection.MULTIPLAYER) }
            )

            Spacer(Modifier.weight(1f))

            RailItem(
                icon = R.drawable.ic_add,
                label = "Create instance",
                selected = false,
                emphasized = true,
                onClick = onCreateInstance
            )
            Spacer(Modifier.height(6.dp))
            RailItem(
                icon = R.drawable.ic_settings_filled,
                label = "Settings",
                selected = selectedSection == LauncherSection.SETTINGS,
                onClick = { onNavigate(LauncherSection.SETTINGS) }
            )

            Spacer(Modifier.height(10.dp))
            AccountShortcut(account = account, onClick = onAccountClick)
        }
    }
}

@Composable
private fun RailItem(
    icon: Int,
    label: String,
    selected: Boolean,
    emphasized: Boolean = false,
    onClick: () -> Unit
) {
    val background = when {
        selected -> MaterialTheme.colorScheme.primary
        emphasized -> MaterialTheme.colorScheme.primaryContainer
        else -> Color.Transparent
    }
    val content = when {
        selected -> MaterialTheme.colorScheme.onPrimary
        emphasized -> MaterialTheme.colorScheme.onPrimaryContainer
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
            .height(42.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(background)
            .clickable(role = Role.Button, onClick = onClick)
            .semantics { contentDescription = label }
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = null,
            tint = content,
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = label,
            color = content,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = if (selected || emphasized) FontWeight.SemiBold else FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun AccountShortcut(
    account: Account?,
    onClick: () -> Unit
) {
    val name = account?.username ?: stringResource(R.string.account_add_new_account)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .clickable(role = Role.Button, onClick = onClick)
            .semantics { contentDescription = name }
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (account != null) {
            PlayerFace(account = account, avatarSize = 32.dp)
        } else {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(50))
                    .background(MaterialTheme.colorScheme.surfaceContainerHighest),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_person_outlined),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = name,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = if (account == null) "Sign in" else "Account",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
    }
}
