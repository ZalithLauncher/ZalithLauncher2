package com.movtery.zalithlauncher.ui.screens.content.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.movtery.zalithlauncher.setting.AllSettings

private val PageBg = Color(0xFF0E0E10)
private val CardBg = Color(0xFF161618)
private val Muted = Color(0xFF9A9AA3)
private val Green = Color(0xFF1BD96A)

@Composable
fun MiraiSearchInstallPage(title: String, onInstall: () -> Unit, onBack: () -> Unit, modifier: Modifier = Modifier) {
    var query by remember { mutableStateOf("") }
    Page(title, onBack, modifier) {
        Field(query, { query = it }, "Search $title")
        Text(if (query.isBlank()) "Search, then install into the selected instance." else "Install results for $query", color = Muted)
        GreenButton("Install", onInstall)
    }
}

@Composable
fun MiraiEditorPage(title: String, onEdit: () -> Unit, onBack: () -> Unit, modifier: Modifier = Modifier) {
    var javaRuntime by remember { mutableStateOf(AllSettings.javaRuntime.getValue()) }
    var controls by remember { mutableStateOf(AllSettings.controlLayout.getValue()) }
    Page(title, onBack, modifier) {
        Read("Renderer", AllSettings.renderer.getValue())
        Field(javaRuntime, { javaRuntime = it }, "Java runtime")
        Field(controls, { controls = it }, "Control layout")
        GreenButton("Save") {
            AllSettings.javaRuntime.save(javaRuntime)
            AllSettings.controlLayout.save(controls)
        }
        GreenButton("More $title options", onEdit)
    }
}

@Composable
fun MiraiHostPage(onHost: () -> Unit, onBack: () -> Unit, modifier: Modifier = Modifier) {
    val enabled = AllSettings.enableTerracotta.state
    var invite by remember { mutableStateOf("") }
    Page("Servers", onBack, modifier) {
        Row(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(CardBg).padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) { Text("Multiplayer", color = Color.White, fontWeight = FontWeight.SemiBold); Text(if (enabled) "On" else "Off", color = Muted) }
            Switch(checked = enabled, onCheckedChange = { AllSettings.enableTerracotta.save(it) })
        }
        Field(invite, { invite = it }, "Invite or host code")
        Text(if (invite.isBlank()) "Turn multiplayer on, then join or host." else "Ready to use $invite", color = Muted)
        GreenButton("Open servers", onHost)
    }
}

@Composable
fun MiraiLoginPage(onOffline: () -> Unit, onMicrosoft: () -> Unit, onBack: () -> Unit, modifier: Modifier = Modifier) {
    var name by remember { mutableStateOf("") }
    Page("Accounts", onBack, modifier) {
        Field(name, { name = it }, "Account name")
        Text(if (name.isBlank()) "Enter a name for offline, or continue with Microsoft." else "Offline name: $name", color = Muted)
        GreenButton("Offline account", onOffline)
        GreenButton("Microsoft login", onMicrosoft)
    }
}

@Composable
fun MiraiPackPage(instanceName: String?, onPack: () -> Unit, onBack: () -> Unit, modifier: Modifier = Modifier) {
    Page("Export", onBack, modifier) {
        Text(instanceName ?: "Select an instance on Play first.", color = Muted)
        if (instanceName != null) GreenButton("Pack instance", onPack)
    }
}

@Composable
private fun Page(title: String, onBack: () -> Unit, modifier: Modifier, content: @Composable () -> Unit) {
    Column(modifier = modifier.fillMaxSize().background(PageBg).padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Back", color = Green, fontWeight = FontWeight.SemiBold, modifier = Modifier.clickable(onClick = onBack))
        Text(title, color = Color.White, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
        content()
    }
}

@Composable
private fun Field(value: String, onChange: (String) -> Unit, hint: String) {
    BasicTextField(value = value, onValueChange = onChange, singleLine = true, textStyle = MaterialTheme.typography.bodyLarge.copy(color = Color.White), cursorBrush = SolidColor(Green), modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(CardBg).padding(14.dp), decorationBox = { inner -> if (value.isEmpty()) Text(hint, color = Muted); inner() })
}

@Composable
private fun Read(label: String, value: String) {
    Column(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(CardBg).padding(14.dp)) {
        Text(label, color = Muted, style = MaterialTheme.typography.bodySmall)
        Text(value, color = Color.White, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun GreenButton(label: String, onClick: () -> Unit) {
    Text(label, color = Color(0xFF0E0E10), fontWeight = FontWeight.SemiBold, modifier = Modifier.clip(RoundedCornerShape(14.dp)).background(Green).clickable(onClick = onClick).padding(14.dp))
}
