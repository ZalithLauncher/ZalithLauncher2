package com.movtery.zalithlauncher.ui.screens.content.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import com.movtery.zalithlauncher.game.version.installed.Version

private val PageBg = Color(0xFF0E0E10)
private val CardBg = Color(0xFF161618)
private val Muted = Color(0xFF9A9AA3)
private val Green = Color(0xFF1BD96A)

@Composable
fun MiraiInstanceDetail(version: Version, onOpenContent: () -> Unit, onBack: () -> Unit, modifier: Modifier = Modifier) {
    DetailPage(
        title = version.getVersionName(),
        subtitle = "Instance content",
        onBack = onBack,
        cards = listOf(
            "Mods" to "Installed mods for this instance",
            "Saves" to "Worlds in this instance",
            "Resource packs" to "Packs installed here",
            "Shaders" to "Shader packs installed here",
            "Settings" to "Instance options and overview"
        ),
        onOpen = onOpenContent,
        modifier = modifier
    )
}

@Composable
fun MiraiSettingsDetail(title: String, onOpenEditor: () -> Unit, onBack: () -> Unit, modifier: Modifier = Modifier) {
    DetailPage(
        title = title,
        subtitle = "Launcher settings",
        onBack = onBack,
        cards = listOf(
            title to "Options for this group",
            "Apply" to "Changes save in the editor",
            "Back" to "Return to the settings list"
        ),
        onOpen = onOpenEditor,
        modifier = modifier
    )
}

@Composable
fun MiraiDiscoverResults(title: String, onOpenSearch: () -> Unit, onBack: () -> Unit, modifier: Modifier = Modifier) {
    DetailPage(
        title = title,
        subtitle = "Search and install",
        onBack = onBack,
        cards = listOf(
            title to "Browse this category",
            "Project" to "Open a project and install it",
            "Install" to "Install into an instance"
        ),
        onOpen = onOpenSearch,
        modifier = modifier
    )
}

@Composable
fun MiraiServerDetail(onOpen: () -> Unit, onBack: () -> Unit, modifier: Modifier = Modifier) {
    DetailPage(
        title = "Servers",
        subtitle = "Join or host",
        onBack = onBack,
        cards = listOf(
            "Join" to "Enter an invite and join a world",
            "Host" to "Open a world to LAN and share an invite",
            "Nodes" to "Custom nodes and logs"
        ),
        onOpen = onOpen,
        modifier = modifier
    )
}

@Composable
private fun DetailPage(title: String, subtitle: String, onBack: () -> Unit, cards: List<Pair<String, String>>, onOpen: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxSize().background(PageBg).padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Back", color = Green, fontWeight = FontWeight.SemiBold, modifier = Modifier.clickable(onClick = onBack))
        Text(title, color = Color.White, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
        Text(subtitle, color = Muted)
        LazyVerticalGrid(columns = GridCells.Adaptive(220.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.weight(1f, fill = false)) {
            items(cards) { (name, body) ->
                Column(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(CardBg).clickable(onClick = onOpen).padding(16.dp)) {
                    Text(name, color = Color.White, fontWeight = FontWeight.SemiBold)
                    Text(body, color = Muted, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
        Row(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(Green).clickable(onClick = onOpen).padding(14.dp)) {
            Text("Open", color = Color(0xFF0E0E10), fontWeight = FontWeight.SemiBold)
        }
    }
}
