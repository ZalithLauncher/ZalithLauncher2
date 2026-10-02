package com.movtery.zalithlauncher.ui.screens.main

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.movtery.zalithlauncher.R
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
    onPlay: () -> Unit,
    modifier: Modifier = Modifier
) {
    val account by AccountsManager.currentAccountFlow.collectAsStateWithLifecycle()

    Surface(
        modifier = modifier.widthRail().fillMaxHeight(),
        color = Color(0x66101412),
        contentColor = Color.White
    ) {
        Column(
            modifier = Modifier.fillMaxHeight().padding(vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_mirai_mark),
                contentDescription = stringResource(R.string.launcher_brand_name),
                tint = Color.Unspecified,
                modifier = Modifier.size(28.dp)
            )
            Spacer(Modifier.size(12.dp))
            RailIcon(R.drawable.ic_home_filled, "Play", selectedSection == LauncherSection.HOME) { onNavigate(LauncherSection.HOME) }
            RailIcon(R.drawable.ic_search, "Discover", selectedSection == LauncherSection.DISCOVER) { onNavigate(LauncherSection.DISCOVER) }
            RailIcon(R.drawable.ic_checkroom, "Skins", selectedSection == LauncherSection.SKINS) { onNavigate(LauncherSection.SKINS) }
            RailIcon(R.drawable.ic_group_filled, "Servers", selectedSection == LauncherSection.MULTIPLAYER) { onNavigate(LauncherSection.MULTIPLAYER) }
            Spacer(Modifier.weight(1f))
            AccountIcon(account != null, account?.username ?: "Account", onAccountClick) { if (account != null) PlayerFace(account = account, avatarSize = 28.dp) }
            RailIcon(R.drawable.ic_play_arrow_filled, "Play", false, emphasized = true, onClick = onPlay)
            RailIcon(R.drawable.ic_settings_filled, "Settings", selectedSection == LauncherSection.SETTINGS) { onNavigate(LauncherSection.SETTINGS) }
            RailIcon(R.drawable.ic_add, "Create instance", false, emphasized = true, onClick = onCreateInstance)
        }
    }
}

private fun Modifier.widthRail() = this.then(Modifier.size(width = 72.dp, height = 0.dp)).let { Modifier }

@Composable
private fun RailIcon(
    icon: Int,
    label: String,
    selected: Boolean,
    emphasized: Boolean = false,
    onClick: () -> Unit
) {
    val background = when {
        selected -> Color(0xCC1BD96A)
        emphasized -> Color(0x331BD96A)
        else -> Color(0x22FFFFFF)
    }
    Box(
        modifier = Modifier
            .padding(vertical = 4.dp)
            .size(48.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(background)
            .clickable(role = Role.Button, onClick = onClick)
            .semantics { contentDescription = label },
        contentAlignment = Alignment.Center
    ) {
        Icon(painterResource(icon), contentDescription = null, tint = if (selected) Color(0xFF06210F) else Color.White, modifier = Modifier.size(22.dp))
    }
}

@Composable
private fun AccountIcon(hasAccount: Boolean, label: String, onClick: () -> Unit, face: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .padding(vertical = 4.dp)
            .size(48.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0x33FFFFFF))
            .clickable(role = Role.Button, onClick = onClick)
            .semantics { contentDescription = label },
        contentAlignment = Alignment.Center
    ) {
        if (hasAccount) face() else Icon(painterResource(R.drawable.ic_person_outlined), contentDescription = null, tint = Color.White)
    }
}
