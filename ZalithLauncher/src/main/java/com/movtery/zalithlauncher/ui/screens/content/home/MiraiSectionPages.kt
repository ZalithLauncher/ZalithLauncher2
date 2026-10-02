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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.movtery.zalithlauncher.game.version.installed.Version
import com.movtery.zalithlauncher.game.version.installed.VersionsManager
import com.movtery.zalithlauncher.ui.screens.content.elements.VersionIconImage

private val PageBg = Color(0xFF0E0E10)
private val CardBg = Color(0xFF161618)
private val Muted = Color(0xFF9A9AA3)
private val Green = Color(0xFF1BD96A)

data class SectionAction(val title: String, val subtitle: String, val onClick: () -> Unit)

@Composable
fun MiraiDiscoverPage(onMods: () -> Unit, onModpacks: () -> Unit, onResourcePacks: () -> Unit, onShaders: () -> Unit, onWorlds: () -> Unit, onVersions: () -> Unit, modifier: Modifier = Modifier) {
    SectionPage("Discover", "Mods, modpacks, resource packs, shaders, worlds, and game versions.", listOf(SectionAction("Mods", "Search and install mods", onMods), SectionAction("Modpacks", "Install a full pack", onModpacks), SectionAction("Resource packs", "Textures and sounds", onResourcePacks), SectionAction("Shaders", "Shader packs", onShaders), SectionAction("Worlds", "Save downloads", onWorlds), SectionAction("Game versions", "Create an instance", onVersions)), modifier)
}

@Composable
fun MiraiLibraryPage(onOpenInstance: (Version) -> Unit, onFiles: () -> Unit, modifier: Modifier = Modifier) {
    val versions by VersionsManager.versions.collectAsStateWithLifecycle()
    Column(modifier = modifier.fillMaxSize().background(PageBg).padding(20.dp)) {
        Text("Library", color = Color.White, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(6.dp))
        Text("Installed instances. Open one for its content and settings.", color = Muted)
        Spacer(Modifier.height(16.dp))
        if (versions.isEmpty()) Text("No instances yet. Create one from Play.", color = Muted) else LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(bottom = 24.dp)) {
            items(versions, key = { it.getVersionName() }) { version ->
                Row(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(CardBg).clickable { onOpenInstance(version) }.padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    VersionIconImage(version = version, modifier = Modifier.size(36.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(version.getVersionName(), color = Color.White, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text("Instance", color = Muted, style = MaterialTheme.typography.bodySmall)
                    }
                    Text("Open", color = Green, fontWeight = FontWeight.SemiBold)
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        Text("Files", color = Green, modifier = Modifier.clickable(onClick = onFiles), fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun MiraiServersPage(onMultiplayer: () -> Unit, modifier: Modifier = Modifier) {
    SectionPage("Servers", "Join and host multiplayer from the launcher.", listOf(SectionAction("Multiplayer", "Terracotta and server list", onMultiplayer)), modifier)
}

@Composable
fun MiraiSettingsPage(onRenderer: () -> Unit, onGame: () -> Unit, onControls: () -> Unit, onGamepad: () -> Unit, onLauncher: () -> Unit, onJava: () -> Unit, onAccounts: () -> Unit, onAbout: () -> Unit, modifier: Modifier = Modifier) {
    SectionPage("Settings", "Launcher, game, controls, and accounts.", listOf(SectionAction("Renderer", "Resolution, renderer, and RAM", onRenderer), SectionAction("Game", "Game options", onGame), SectionAction("Controls", "Touch controls", onControls), SectionAction("Gamepad", "Controller options", onGamepad), SectionAction("Launcher", "Launcher behavior", onLauncher), SectionAction("Java", "Java runtime", onJava), SectionAction("Accounts", "Offline and Microsoft accounts", onAccounts), SectionAction("About", "Version and licenses", onAbout)), modifier)
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
