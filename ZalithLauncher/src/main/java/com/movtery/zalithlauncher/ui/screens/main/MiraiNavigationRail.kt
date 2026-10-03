package com.movtery.zalithlauncher.ui.screens.main

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.game.version.installed.VersionsManager

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
    val versions by VersionsManager.versions.collectAsStateWithLifecycle()
    val currentVersion by VersionsManager.currentVersion.collectAsStateWithLifecycle()
    val selected = currentVersion ?: versions.firstOrNull()
    Surface(modifier = modifier.width(72.dp).fillMaxHeight(), color = Color(0x66101412), contentColor = Color.White) {
        Column(modifier = Modifier.fillMaxHeight().padding(vertical = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Column(
                modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(painterResource(R.drawable.ic_mirai_mark), contentDescription = stringResource(R.string.launcher_brand_name), modifier = Modifier.size(28.dp))
                RailIcon(R.drawable.ic_home_filled, "Home", selectedSection == LauncherSection.HOME) { onNavigate(LauncherSection.HOME) }
                RailIcon(R.drawable.ic_search, "Discover", selectedSection == LauncherSection.DISCOVER) { onNavigate(LauncherSection.DISCOVER) }
                RailIcon(R.drawable.ic_checkroom, "Skins", selectedSection == LauncherSection.SKINS) { onNavigate(LauncherSection.SKINS) }
                RailIcon(R.drawable.ic_wallpaper, "Wallpaper", selectedSection == LauncherSection.MULTIPLAYER) { onNavigate(LauncherSection.MULTIPLAYER) }
                RailIcon(R.drawable.ic_settings_filled, "Settings", selectedSection == LauncherSection.SETTINGS) { onNavigate(LauncherSection.SETTINGS) }
            }
            InstanceMark(selected?.getVersionName() ?: "Choose instance") {
                if (versions.isEmpty()) onCreateInstance()
                else {
                    val index = versions.indexOfFirst { it.getVersionName() == selected?.getVersionName() }
                    VersionsManager.saveVersion(versions[(index + 1).mod(versions.size)])
                }
            }
            RailIcon(R.drawable.ic_play_arrow_filled, "Play", false, emphasized = true, onClick = onPlay)
        }
    }
}

@Composable
private fun RailIcon(icon: Int, label: String, selected: Boolean, emphasized: Boolean = false, onClick: () -> Unit) {
    val background = when {
        selected -> Color(0xCC1BD96A)
        emphasized -> Color(0x331BD96A)
        else -> Color(0x22FFFFFF)
    }
    Box(
        modifier = Modifier.padding(vertical = 4.dp).size(48.dp).clip(RoundedCornerShape(16.dp)).background(background).clickable(role = Role.Button, onClick = onClick).semantics { contentDescription = label },
        contentAlignment = Alignment.Center
    ) {
        Image(painterResource(icon), contentDescription = null, colorFilter = ColorFilter.tint(if (selected) Color(0xFF06210F) else Color.White), modifier = Modifier.size(26.dp))
    }
}

@Composable
private fun InstanceMark(name: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier.padding(vertical = 4.dp).size(48.dp).clip(RoundedCornerShape(16.dp)).background(Color(0x331BD96A)).clickable(role = Role.Button, onClick = onClick).semantics { contentDescription = "Choose instance" },
        contentAlignment = Alignment.Center
    ) {
        Text(name.take(2), color = Color.White, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Clip)
    }
}
