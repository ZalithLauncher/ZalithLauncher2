package com.movtery.zalithlauncher.ui.screens.content.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.io.File

private val PageBg = Color(0xFF0E0E10)
private val CardBg = Color(0xFF161618)
private val Muted = Color(0xFF9A9AA3)
private val Green = Color(0xFF1BD96A)

@Composable
fun MiraiAccountsPage(onOffline: () -> Unit, onMicrosoft: () -> Unit, onBack: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxSize().background(PageBg).padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Back", color = Green, fontWeight = FontWeight.SemiBold, modifier = Modifier.clickable(onClick = onBack))
        Text("Accounts", color = Color.White, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
        Text("Add an offline account or sign in with Microsoft. The login step uses the existing account flow.", color = Muted)
        ActionCard("Offline", "Create or select an offline account", onOffline)
        ActionCard("Microsoft", "Sign in and use that account", onMicrosoft)
    }
}

@Composable
fun MiraiExportPage(instanceName: String?, onExport: () -> Unit, onBack: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxSize().background(PageBg).padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Back", color = Green, fontWeight = FontWeight.SemiBold, modifier = Modifier.clickable(onClick = onBack))
        Text("Export", color = Color.White, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
        Text(instanceName ?: "Select an instance on Play first.", color = Muted)
        if (instanceName != null) ActionCard("Export instance", "Pack this instance with the existing export tool", onExport)
    }
}

@Composable
fun MiraiLogPage(path: String?, onBack: () -> Unit, modifier: Modifier = Modifier) {
    val text = remember(path) {
        if (path.isNullOrBlank()) "Select an instance on Play first."
        else runCatching { File(path).takeIf { it.exists() }?.readText() }.getOrNull()?.takeIf { it.isNotBlank() } ?: "No log yet."
    }
    Column(modifier = modifier.fillMaxSize().background(PageBg).padding(20.dp)) {
        Text("Back", color = Green, fontWeight = FontWeight.SemiBold, modifier = Modifier.clickable(onClick = onBack))
        Text("Latest log", color = Color.White, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(vertical = 12.dp))
        Text(text, color = Color.White, style = MaterialTheme.typography.bodySmall, modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()))
    }
}

@Composable
private fun ActionCard(title: String, body: String, onClick: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(CardBg).clickable(onClick = onClick).padding(16.dp)) {
        Text(title, color = Color.White, fontWeight = FontWeight.SemiBold)
        Text(body, color = Muted, style = MaterialTheme.typography.bodySmall)
    }
}
