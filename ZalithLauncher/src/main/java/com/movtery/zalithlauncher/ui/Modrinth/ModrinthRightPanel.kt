// File: com/movtery/zalithlauncher/ui/modrinth/ModrinthRightPanel.kt
package com.movtery.zalithlauncher.ui.modrinth

import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

// Temporary data classes so the code compiles. We will replace these later.
data class UserProfile(val name: String, val accountType: String)
data class Friend(val name: String, val status: String)

@Composable
fun ModrinthRightPanel(
    currentUser: UserProfile,
    onlineFriends: List<Friend>,
    offlineFriends: List<Friend>
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // "Playing as" Section
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp).fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(MaterialTheme.colorScheme.primary, shape = MaterialTheme.shapes.circle)
                    )
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(currentUser.name, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                        Text(currentUser.accountType, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        // Friends List Section
        item {
            Text(
                text = "Friends",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        // Online Friends
        item {
            Text(
                text = "Online - ${onlineFriends.size}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        items(onlineFriends) { friend -> FriendRow(friend) }

        // Offline Friends
        item {
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Offline - ${offlineFriends.size}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        items(offlineFriends) { friend -> FriendRow(friend) }
    }
}

@Composable
fun FriendRow(friend: Friend) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(MaterialTheme.colorScheme.secondaryContainer, shape = MaterialTheme.shapes.circle)
        )
        Spacer(Modifier.width(12.dp))
        Column {
            Text(friend.name, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
            Text(friend.status, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
