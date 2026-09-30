// File: com/movtery/zalithlauncher/ui/modrinth/ModrinthSidebar.kt
package com.movtery.zalithlauncher.ui.modrinth

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

// Define your navigation items
sealed class SidebarDestination(val route: String, val icon: ImageVector, val label: String) {
    object Home : SidebarDestination("home", Icons.Default.Home, "Home")
    object Discover : SidebarDestination("discover", Icons.Default.Search, "Discover")
    object Library : SidebarDestination("library", Icons.Default.LibraryBooks, "Library")
    object Settings : SidebarDestination("settings", Icons.Default.Settings, "Settings")
}

@Composable
fun ModrinthSidebar(
    destinations: List<SidebarDestination>,
    currentRoute: String,
    onNavigate: (SidebarDestination) -> Unit
) {
    NavigationRail(
        containerColor = MaterialTheme.colorScheme.surface,
        header = {
            // Your app logo can go here
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = "App Logo",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(vertical = 16.dp)
            )
        }
    ) {
        Spacer(Modifier.height(16.dp))
        destinations.forEach { destination ->
            NavigationRailItem(
                icon = { Icon(destination.icon, contentDescription = destination.label) },
                label = { Text(destination.label) },
                selected = currentRoute == destination.route,
                onClick = { onNavigate(destination) },
                colors = NavigationRailItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    selectedTextColor = MaterialTheme.colorScheme.onSurface,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
            Spacer(Modifier.height(8.dp))
        }
    }
}
