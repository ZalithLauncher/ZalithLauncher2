package com.movtery.zalithlauncher.ui.screens.content.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

private val PageBg = Color(0xFF0E0E10)
private val CardBg = Color(0xFF161618)
private val Muted = Color(0xFF9A9AA3)
private val Green = Color(0xFF1BD96A)

data class SectionAction(val title: String, val subtitle: String, val onClick: () -> Unit)

@Composable
fun MiraiDiscoverPage(
    onMods: () -> Unit,
    onModpacks: () -> Unit,
    onResourcePacks: () -> Unit,
    onShaders: () -> Unit,
    onWorlds: () -> Unit,
    onVersions: () -> Unit,
    modifier: Modifier = Modifier,
) {
    SectionPage(
        title = "Discover",
        subtitle = "Mods, modpacks, resource packs, shaders, worlds, and game versions.",
        actions = listOf(
            SectionAction("Mods", "Search and install mods", onMods),
            SectionAction("Modpacks", "Install a full pack", onModpacks),
            SectionAction("Resource packs", "Textures and sounds", onResourcePacks),
            SectionAction("Shaders", "Shader packs", onShaders),
            SectionAction("Worlds", "Save downloads", onWorlds),
            SectionAction("Game versions", "Create an instance", onVersions),
        ),
        modifier = modifier,
    )
}

@Composable
fun MiraiLibraryPage(
    onInstances: () -> Unit,
    onExport: () -> Unit,
    onFiles: () -> Unit,
    modifier: Modifier = Modifier,
) {
    SectionPage(
        title = "Library",
        subtitle = "Installed instances, exports, and files.",
        actions = listOf(
            SectionAction("Instances", "Manage installed versions", onInstances),
            SectionAction("Export", "Open an instance, then export", onExport),
            SectionAction("Files", "Open the launcher files", onFiles),
        ),
        modifier = modifier,
    )
}

@Composable
fun MiraiServersPage(
    onMultiplayer: () -> Unit,
    modifier: Modifier = Modifier,
) {
    SectionPage(
        title = "Servers",
        subtitle = "Join and host multiplayer from the launcher.",
        actions = listOf(SectionAction("Multiplayer", "Terracotta and server list", onMultiplayer)),
        modifier = modifier,
    )
}

@Composable
fun MiraiSettingsPage(
    onAllSettings: () -> Unit,
    onAccounts: () -> Unit,
    onRenderer: () -> Unit,
    modifier: Modifier = Modifier,
) {
    SectionPage(
        title = "Settings",
        subtitle = "Launcher, accounts, renderer, and game options.",
        actions = listOf(
            SectionAction("All settings", "Every launcher and game option", onAllSettings),
            SectionAction("Accounts", "Offline and Microsoft accounts", onAccounts),
            SectionAction("Renderer", "Resolution, renderer, and RAM", onRenderer),
        ),
        modifier = modifier,
    )
}

@Composable
private fun SectionPage(title: String, subtitle: String, actions: List<SectionAction>, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxSize().background(PageBg).padding(20.dp)) {
        Text(title, color = Color.White, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(6.dp))
        Text(subtitle, color = Muted, style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(18.dp))
        LazyVerticalGrid(columns = GridCells.Adaptive(220.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(bottom = 24.dp)) {
            items(actions) { action ->
                Column(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(CardBg).clickable(onClick = action.onClick).padding(16.dp)) {
                    Text(action.title, color = Color.White, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(4.dp))
                    Text(action.subtitle, color = Muted, style = MaterialTheme.typography.bodySmall)
                    Spacer(Modifier.height(10.dp))
                    Text("Open", color = Green, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
