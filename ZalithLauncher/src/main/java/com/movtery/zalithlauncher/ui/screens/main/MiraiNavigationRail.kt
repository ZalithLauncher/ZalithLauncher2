package com.movtery.zalithlauncher.ui.screens.main

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.movtery.zalithlauncher.R

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
    Surface(modifier = modifier.width(72.dp).fillMaxHeight(), color = Color(0xCC101412), contentColor = Color.White) {
        Column(modifier = Modifier.fillMaxHeight().padding(vertical = 12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Image(painterResource(R.drawable.ic_mirai_mark), contentDescription = stringResource(R.string.launcher_brand_name), modifier = Modifier.size(28.dp))
            RailIcon(R.drawable.ic_home_filled, "Home", selectedSection == LauncherSection.HOME) { onNavigate(LauncherSection.HOME) }
            RailIcon(R.drawable.ic_search, "Discover", selectedSection == LauncherSection.DISCOVER) { onNavigate(LauncherSection.DISCOVER) }
            RailIcon(R.drawable.ic_checkroom, "Library", selectedSection == LauncherSection.LIBRARY) { onNavigate(LauncherSection.LIBRARY) }
            RailIcon(R.drawable.ic_person_outlined, "Account", selectedSection == null) { onAccountClick() }
            Spacer(Modifier.weight(1f))
            RailIcon(R.drawable.ic_add, "New instance", false, emphasized = true, onClick = onCreateInstance)
        }
    }
}

@Composable
private fun RailIcon(icon: Int, label: String, selected: Boolean, emphasized: Boolean = false, onClick: () -> Unit) {
    val background = when {
        selected -> Color(0xCC1BD96A)
        emphasized -> Color(0xCC1BD96A)
        else -> Color.Transparent
    }
    Box(
        modifier = Modifier.padding(vertical = 6.dp).size(48.dp).clip(RoundedCornerShape(16.dp)).background(background).clickable(role = Role.Button, onClick = onClick).semantics { contentDescription = label },
        contentAlignment = Alignment.Center
    ) {
        Image(painterResource(icon), contentDescription = null, colorFilter = ColorFilter.tint(if (selected || emphasized) Color(0xFF06210F) else Color.White), modifier = Modifier.size(24.dp))
    }
}
