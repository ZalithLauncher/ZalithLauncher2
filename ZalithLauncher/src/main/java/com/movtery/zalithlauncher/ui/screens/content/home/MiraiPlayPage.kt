package com.movtery.zalithlauncher.ui.screens.content.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.game.version.installed.Version
import com.movtery.zalithlauncher.game.version.installed.VersionsManager
import com.movtery.zalithlauncher.ui.screens.content.elements.VersionIconImage

@Composable
fun MiraiPlayPage(
    onLaunch: (Version?) -> Unit,
    onExploreContent: () -> Unit,
    onCreateInstance: () -> Unit,
    onManageVersions: () -> Unit,
    onOpenVersionSettings: (Version) -> Unit,
    modifier: Modifier = Modifier,
) {
    val versions by VersionsManager.versions.collectAsStateWithLifecycle()
    val current by VersionsManager.currentVersion.collectAsStateWithLifecycle()
    val selected = current ?: versions.firstOrNull()

    Row(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0E0E10))
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column(modifier = Modifier.width(280.dp).fillMaxHeight()) {
            Text(text = "Instances", color = Color.White, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(12.dp))
            if (versions.isEmpty()) {
                Text(text = "No instances yet.", color = Color(0xFF9A9AA3), style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.weight(1f))
            } else {
                LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(bottom = 12.dp)) {
                    items(versions, key = { it.getVersionName() }) { version ->
                        InstanceRow(version = version, selected = version.getVersionName() == selected?.getVersionName(), onClick = { VersionsManager.saveVersion(version) })
                    }
                }
            }
            Button(onClick = onCreateInstance, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1BD96A), contentColor = Color(0xFF06210F))) {
                Text("Create instance", fontWeight = FontWeight.SemiBold)
            }
        }
        Column(modifier = Modifier.weight(1f).fillMaxHeight().clip(RoundedCornerShape(18.dp)).background(Color(0xFF161618)).padding(24.dp)) {
            if (selected == null) {
                Text("Pick or create an instance", color = Color.White, style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.height(8.dp))
                Text(text = "Install a version, then play it from here. Library, skins, servers, and settings stay in the sidebar.", color = Color(0xFF9A9AA3))
            } else {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    VersionIconImage(version = selected, modifier = Modifier.size(56.dp))
                    Column {
                        Text(text = selected.getVersionName(), color = Color.White, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text("Ready to play", color = Color(0xFF1BD96A), style = MaterialTheme.typography.labelLarge)
                    }
                }
                Spacer(Modifier.height(22.dp))
                Button(onClick = { onLaunch(selected) }, shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1BD96A), contentColor = Color(0xFF06210F))) {
                    Icon(painterResource(R.drawable.ic_play_arrow_filled), contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Play", fontWeight = FontWeight.SemiBold)
                }
                Spacer(Modifier.height(18.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = { onOpenVersionSettings(selected) }) { Text("Instance settings") }
                    TextButton(onClick = onExploreContent) { Text("Add content") }
                    TextButton(onClick = onManageVersions) { Text("Manage") }
                }
            }
            Spacer(Modifier.weight(1f))
        }
    }
}

@Composable
private fun InstanceRow(version: Version, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(if (selected) Color(0xFF1BD96A) else Color(0xFF1C1C1F)).clickable(onClick = onClick).padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(modifier = Modifier.size(28.dp).clip(RoundedCornerShape(8.dp)).background(if (selected) Color(0xFF06210F) else Color(0xFF2A2A2E)), contentAlignment = Alignment.Center) {
            VersionIconImage(version = version, modifier = Modifier.size(22.dp))
        }
        Text(text = version.getVersionName(), color = if (selected) Color(0xFF06210F) else Color.White, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}
