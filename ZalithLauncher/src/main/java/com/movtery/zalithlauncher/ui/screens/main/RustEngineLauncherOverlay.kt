package com.movtery.zalithlauncher.ui.screens.main

import android.app.Activity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.horizon.android.HorizonEngine

/** Temporary Rust-first launcher control, overlaid without replacing Java's screens. */
@Composable
fun RustEngineLauncherOverlay(activity: Activity) {
    var status by remember { mutableStateOf("Starling native runtime") }
    Box(Modifier.fillMaxSize()) {
        Card(
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text("Rust Edition", style = MaterialTheme.typography.titleMedium)
                Text(status, style = MaterialTheme.typography.bodySmall)
                Button(onClick = { HorizonEngine.launch(activity) { status = it } }) {
                    Text("Launch Rust")
                }
            }
        }
    }
}
