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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
    val glass = ButtonDefaults.buttonColors(containerColor = Color(0x991BD96A), contentColor = Color(0xFF06210F))

    Row(modifier = modifier.fillMaxSize().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        Column(modifier = Modifier.fillMaxHeight().weight(1f)) {
            Text("Instances", color = Color.White, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(12.dp))
            if (versions.isEmpty()) {
                Text("No instances yet.", color = Color(0xFFD7CFC8))
                Spacer(Modifier.weight(1f))
            } else {
                LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(bottom = 12.dp)) {
                    items(versions, key = { it.getVersionName() }) { version ->
                        InstanceRow(version, version.getVersionName() == selected?.getVersionName()) { VersionsManager.saveVersion(version) }
                    }
                }
            }
            Button(onClick = onCreateInstance, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp), colors = glass) { Text("Create instance", fontWeight = FontWeight.SemiBold) }
        }
        Column(modifier = Modifier.weight(1f).fillMaxHeight().clip(RoundedCornerShape(22.dp)).background(Color(0x66161618)).padding(24.dp)) {
            Text(selected?.getVersionName() ?: "Pick or create an instance", color = Color.White, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(8.dp))
            Text("Install a version, then play it from here. Skins, servers, and settings stay in the sidebar.", color = Color(0xFFD7CFC8))
            Spacer(Modifier.height(22.dp))
            Button(onClick = { onLaunch(selected) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp), colors = glass) { Text("Play", fontWeight = FontWeight.SemiBold) }
            if (selected != null) {
                Spacer(Modifier.height(12.dp))
                Text("Add content", color = Color(0xFF1BD96A), fontWeight = FontWeight.SemiBold, modifier = Modifier.clickable(onClick = onExploreContent))
                Text("Instance settings", color = Color.White, modifier = Modifier.padding(top = 8.dp).clickable { onOpenVersionSettings(selected) })
            }
            Spacer(Modifier.weight(1f))
        }
    }
}

@Composable
private fun InstanceRow(version: Version, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(if (selected) Color(0xCC1BD96A) else Color(0x331C1C1F)).clickable(onClick = onClick).padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(modifier = Modifier.size(28.dp).clip(RoundedCornerShape(8.dp)).background(Color(0x33000000)), contentAlignment = Alignment.Center) {
            VersionIconImage(version = version, modifier = Modifier.size(22.dp))
        }
        Text(version.getVersionName(), color = if (selected) Color(0xFF06210F) else Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}
