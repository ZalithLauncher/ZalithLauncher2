// File: com/movtery/zalithlauncher/ui/modrinth/ModrinthLayout.kt
package com.movtery.zalithlauncher.ui.modrinth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ModrinthAppLayout(
    sidebarContent: @Composable () -> Unit,
    mainContent: @Composable () -> Unit,
    rightPanelContent: @Composable () -> Unit
) {
    Row(modifier = Modifier.fillMaxSize()) {
        // 1. Left Sidebar (Navigation Rail)
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(72.dp)
                .background(MaterialTheme.colorScheme.surface)
        ) {
            sidebarContent()
        }

        // 2. Main Content Area
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .background(MaterialTheme.colorScheme.background)
        ) {
            mainContent()
        }

        // 3. Right Panel (Details/Social)
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(300.dp)
                .background(MaterialTheme.colorScheme.surface)
        ) {
            rightPanelContent()
        }
    }
}
