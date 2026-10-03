/*
 * Zalith Launcher 2
 * Copyright (C) 2025 MovTery <movtery228@qq.com> and contributors
 * Copyright (C) 2026 Mirai Launcher contributors.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/gpl-3.0.txt>.
 */

package com.movtery.zalithlauncher.ui.screens.content.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.game.renderer.RendererPicker
import com.movtery.zalithlauncher.game.version.installed.Version
import com.movtery.zalithlauncher.game.version.installed.VersionsManager
import com.movtery.zalithlauncher.ui.screens.content.elements.VersionIconImage
import java.io.File

private enum class HomeInstanceFilter(val label: String) {
    ALL("All"),
    PINNED("Pinned"),
    MODDED("Modded"),
    LTW_MODERN("1.17+ LTW"),
    LTW_LEGACY("1.8–1.16.5 Legacy")
}

private val ModrinthCardColor = Color(0xFF21242B)
private val ModrinthCardBorder = Color(0xFF2E323C)
private val ModrinthElevatedSurface = Color(0xFF1A1D23)
private val ModrinthEmerald = Color(0xFF1BD96A)
private val ModrinthEmeraldDark = Color(0xFF143825)
private val ModrinthOnEmerald = Color(0xFF06210F)

private val DefaultAvailableRenderers = setOf(
    RendererPicker.LTW,
    RendererPicker.LTW_LEGACY,
    RendererPicker.ZINK,
    RendererPicker.VIRGL,
    RendererPicker.GL4ES
)

fun resolveRendererShortLabel(version: Version?): String {
    val mcVer = version?.getVersionInfo()?.minecraftVersion
    val manual = version?.getRenderer()?.takeIf { it.isNotBlank() }
    val choice = RendererPicker.pick(mcVer, manual, DefaultAvailableRenderers)
    return when {
        choice.identifier.contains("Legacy", ignoreCase = true) -> "LTW Legacy"
        choice.identifier.contains("LTW", ignoreCase = true) -> "LTW"
        choice.identifier.contains("Zink", ignoreCase = true) -> "Kopper Zink"
        choice.identifier.contains("VirGL", ignoreCase = true) -> "VirGL"
        choice.identifier.contains("GL4ES", ignoreCase = true) -> "GL4ES"
        else -> "LTW"
    }
}

fun resolveRendererBadgeDetail(version: Version?): String {
    val mcVer = version?.getVersionInfo()?.minecraftVersion
    val manual = version?.getRenderer()?.takeIf { it.isNotBlank() }
    val choice = RendererPicker.pick(mcVer, manual, DefaultAvailableRenderers)
    return when {
        choice.identifier.contains("Legacy", ignoreCase = true) -> "Renderer: LTW Legacy (1.8–1.16.5)"
        choice.identifier.contains("LTW", ignoreCase = true) -> "Renderer: LTW (1.17+)"
        choice.identifier.contains("Zink", ignoreCase = true) -> "Renderer: Kopper Zink (Vulkan)"
        choice.identifier.contains("VirGL", ignoreCase = true) -> "Renderer: VirGL"
        choice.identifier.contains("GL4ES", ignoreCase = true) -> "Renderer: GL4ES"
        else -> "Renderer: LTW (1.17+)"
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MiraiHomeDashboard(
    onLaunchVersion: (Version) -> Unit,
    onOpenVersionSettings: (Version) -> Unit,
    onExploreContent: () -> Unit,
    onCreateInstance: () -> Unit,
    onManageVersions: () -> Unit,
    modifier: Modifier = Modifier
) {
    val versions by VersionsManager.versions.collectAsStateWithLifecycle()
    val currentVersion by VersionsManager.currentVersion.collectAsStateWithLifecycle()
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var selectedFilter by rememberSaveable { mutableStateOf(HomeInstanceFilter.ALL) }
    var jumpBackInExpanded by rememberSaveable { mutableStateOf(true) }

    val sortedVersions = remember(versions, currentVersion) {
        versions.sortedWith(
            compareByDescending<Version> { it == currentVersion }
                .thenByDescending { it.pinnedState }
                .thenBy { it.getVersionName().lowercase() }
        )
    }

    val jumpBackInVersions = remember(sortedVersions) {
        sortedVersions.take(3)
    }

    val filteredVersions = remember(sortedVersions, searchQuery, selectedFilter) {
        val q = searchQuery.trim().lowercase()
        sortedVersions.filter { version ->
            val info = version.getVersionInfo()
            val loaderName = info?.loaderInfo?.loader?.displayName.orEmpty()
            val mcVer = info?.minecraftVersion.orEmpty()
            val rendererLabel = resolveRendererShortLabel(version)
            val matchesFilter = when (selectedFilter) {
                HomeInstanceFilter.ALL -> true
                HomeInstanceFilter.PINNED -> version.pinnedState
                HomeInstanceFilter.MODDED -> info?.loaderInfo != null
                HomeInstanceFilter.LTW_MODERN -> rendererLabel == "LTW"
                HomeInstanceFilter.LTW_LEGACY -> rendererLabel == "LTW Legacy"
            }
            val matchesQuery = q.isEmpty() ||
                version.getVersionName().lowercase().contains(q) ||
                version.getVersionSummary().lowercase().contains(q) ||
                mcVer.lowercase().contains(q) ||
                loaderName.lowercase().contains(q)
            matchesFilter && matchesQuery
        }
    }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val columns = when {
            maxWidth >= 680.dp -> 3
            maxWidth >= 440.dp -> 2
            else -> 1
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 14.dp, end = 8.dp, top = 8.dp, bottom = 18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Section 1: Jump back in (Mockup #1 top row)
            item(key = "jump_back_in_header") {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { jumpBackInExpanded = !jumpBackInExpanded }
                                .padding(vertical = 2.dp, horizontal = 4.dp)
                        ) {
                            Text(
                                text = "Jump back in",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Icon(
                                painter = painterResource(
                                    if (jumpBackInExpanded) R.drawable.ic_arrow_drop_up_rounded else R.drawable.ic_arrow_drop_down_rounded
                                ),
                                contentDescription = null,
                                tint = Color(0xFF9CA3AF),
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFF21242B),
                                border = BorderStroke(1.dp, ModrinthCardBorder),
                                onClick = onExploreContent
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.ic_search),
                                        contentDescription = null,
                                        tint = ModrinthEmerald,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = "Discover Mods",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = Color(0xFFE5E7EB)
                                    )
                                }
                            }
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = ModrinthEmeraldDark,
                                border = BorderStroke(1.dp, ModrinthEmerald.copy(alpha = 0.45f)),
                                onClick = onCreateInstance
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.ic_add),
                                        contentDescription = null,
                                        tint = ModrinthEmerald,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = "New Instance",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = ModrinthEmerald
                                    )
                                }
                            }
                        }
                    }

                    AnimatedVisibility(
                        visible = jumpBackInExpanded,
                        enter = expandVertically(tween(250)) + fadeIn(tween(220)),
                        exit = shrinkVertically(tween(220)) + fadeOut(tween(180))
                    ) {
                        if (jumpBackInVersions.isNotEmpty()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                jumpBackInVersions.forEach { version ->
                                    ModrinthJumpBackInCard(
                                        version = version,
                                        isCurrent = version == currentVersion,
                                        onSelect = { VersionsManager.saveVersion(version) },
                                        onPlay = {
                                            VersionsManager.saveVersion(version)
                                            onLaunchVersion(version)
                                        },
                                        onSettings = {
                                            VersionsManager.saveVersion(version)
                                            onOpenVersionSettings(version)
                                        },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                repeat((3 - jumpBackInVersions.size).coerceAtLeast(0)) { idx ->
                                    QuickStartJumpCard(
                                        index = idx,
                                        onCreateInstance = onCreateInstance,
                                        onExploreContent = onExploreContent,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        } else {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                repeat(3) { idx ->
                                    QuickStartJumpCard(
                                        index = idx,
                                        onCreateInstance = onCreateInstance,
                                        onExploreContent = onExploreContent,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Section 2: Recent Instances Header + Search + Filters
            item(key = "recent_instances_toolbar") {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Recent Instances",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Surface(
                                shape = RoundedCornerShape(999.dp),
                                color = Color(0xFF232730)
                            ) {
                                Text(
                                    text = filteredVersions.size.toString(),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = ModrinthEmerald,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Compact Search Input + Library Link
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ModrinthCompactSearchField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                placeholder = "Search instances...",
                                modifier = Modifier.width(190.dp)
                            )
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFF21242B),
                                border = BorderStroke(1.dp, ModrinthCardBorder),
                                onClick = onManageVersions
                            ) {
                                Text(
                                    text = "View All",
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color(0xFFD1D5DB)
                                )
                            }
                        }
                    }

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        HomeInstanceFilter.entries.forEach { filter ->
                            val selected = selectedFilter == filter
                            FilterChip(
                                selected = selected,
                                onClick = { selectedFilter = filter },
                                label = {
                                    Text(
                                        text = filter.label,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    containerColor = Color(0xFF1E2128),
                                    labelColor = Color(0xFFD1D5DB),
                                    selectedContainerColor = ModrinthEmeraldDark,
                                    selectedLabelColor = ModrinthEmerald
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = selected,
                                    borderColor = Color(0xFF2D313A),
                                    selectedBorderColor = ModrinthEmerald
                                )
                            )
                        }
                    }
                }
            }

            // Section 3: Recent Instances Grid (3 columns like Mockup #1)
            if (filteredVersions.isEmpty()) {
                item(key = "empty_instances_state") {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = ModrinthCardColor,
                        border = BorderStroke(1.dp, ModrinthCardBorder)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = if (versions.isEmpty()) {
                                    stringResource(R.string.home_empty_library_title)
                                } else {
                                    stringResource(R.string.home_no_matching_instances)
                                },
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = stringResource(R.string.home_empty_library_summary),
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF9CA3AF)
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Button(
                                    onClick = onCreateInstance,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = ModrinthEmerald,
                                        contentColor = ModrinthOnEmerald
                                    )
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.ic_add),
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        text = stringResource(R.string.home_new_instance),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                OutlinedButton(onClick = onExploreContent) {
                                    Text(text = stringResource(R.string.home_explore_content))
                                }
                            }
                        }
                    }
                }
            } else {
                val rows = filteredVersions.chunked(columns)
                items(
                    count = rows.size,
                    key = { rowIndex -> rows[rowIndex].joinToString("_") { it.getVersionName() } }
                ) { rowIndex ->
                    val rowItems = rows[rowIndex]
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        rowItems.forEach { version ->
                            ModrinthRecentInstanceCard(
                                version = version,
                                isSelected = version == currentVersion,
                                onSelect = { VersionsManager.saveVersion(version) },
                                onPlay = {
                                    VersionsManager.saveVersion(version)
                                    onLaunchVersion(version)
                                },
                                onSettings = {
                                    VersionsManager.saveVersion(version)
                                    onOpenVersionSettings(version)
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        repeat((columns - rowItems.size).coerceAtLeast(0)) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ModrinthJumpBackInCard(
    version: Version,
    isCurrent: Boolean,
    onSelect: () -> Unit,
    onPlay: () -> Unit,
    onSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "jumpCardScale"
    )
    val borderColor by animateColorAsState(
        targetValue = if (isCurrent) ModrinthEmerald else ModrinthCardBorder,
        animationSpec = tween(220),
        label = "jumpCardBorder"
    )

    val info = version.getVersionInfo()
    val mcVer = info?.minecraftVersion ?: "Minecraft"
    val loaderName = info?.loaderInfo?.loader?.displayName ?: "Vanilla"
    val rendererName = remember(version) { resolveRendererShortLabel(version) }

    val latestScreenshot = remember(version) {
        val dir = File(version.getGameDir(), "screenshots")
        dir.listFiles { f ->
            f.isFile && (f.name.endsWith(".png", true) || f.name.endsWith(".jpg", true))
        }?.maxByOrNull { it.lastModified() }
    }

    Surface(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            },
        shape = RoundedCornerShape(14.dp),
        color = ModrinthCardColor,
        border = BorderStroke(if (isCurrent) 1.5.dp else 1.dp, borderColor),
        onClick = onSelect
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Top 16:9 Screenshot / Hero Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(82.dp)
            ) {
                if (latestScreenshot != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(context).data(latestScreenshot).build(),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Image(
                        painter = painterResource(R.drawable.mirai_hero_bg),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Black.copy(alpha = 0.15f),
                                    Color(0xFF21242B).copy(alpha = 0.92f)
                                )
                            )
                        )
                )

                // Instance Icon + Renderer Pill on banner
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Surface(
                        shape = RoundedCornerShape(9.dp),
                        color = Color(0xFF15171C).copy(alpha = 0.85f),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f))
                    ) {
                        VersionIconImage(
                            version = version,
                            modifier = Modifier
                                .padding(4.dp)
                                .size(24.dp)
                        )
                    }

                    ModrinthMetaPill(
                        text = rendererName,
                        highlighted = true
                    )
                }
            }

            // Card Body
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 11.dp, vertical = 9.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = version.getVersionName(),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "$loaderName • $mcVer",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF9CA3AF),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onSettings,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_settings_filled),
                                contentDescription = stringResource(R.string.versions_manage_settings),
                                tint = Color(0xFF9CA3AF),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Surface(
                            shape = CircleShape,
                            color = if (isCurrent) ModrinthEmerald else Color(0xFF2D323D),
                            contentColor = if (isCurrent) ModrinthOnEmerald else Color.White,
                            onClick = onPlay,
                            modifier = Modifier.size(30.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_play_arrow_filled),
                                    contentDescription = stringResource(R.string.main_launch_game),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ModrinthMetaPill(text = loaderName)
                    ModrinthMetaPill(text = mcVer)
                    ModrinthMetaPill(text = rendererName, highlighted = isCurrent)
                }
            }
        }
    }
}

@Composable
private fun QuickStartJumpCard(
    index: Int,
    onCreateInstance: () -> Unit,
    onExploreContent: () -> Unit,
    modifier: Modifier = Modifier
) {
    val title = when (index) {
        0 -> "Create 1.21+ Instance"
        1 -> "Explore Modrinth Packs"
        else -> "Classic 1.8.9–1.16.5"
    }
    val subtitle = when (index) {
        0 -> "Fabric / NeoForge • LTW"
        1 -> "Mods, Shaders & Worlds"
        else -> "Hypixel / Forge • LTW Legacy"
    }
    val chip1 = when (index) {
        0 -> "1.17+ LTW"
        1 -> "Modrinth"
        else -> "LTW Legacy"
    }
    val chip2 = when (index) {
        0 -> "GLES 3.2"
        1 -> "CurseForge"
        else -> "1.8–1.16.5"
    }
    val action = if (index == 1) onExploreContent else onCreateInstance

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = ModrinthCardColor,
        border = BorderStroke(1.dp, ModrinthCardBorder),
        onClick = action
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(82.dp)
            ) {
                Image(
                    painter = painterResource(R.drawable.mirai_hero_bg),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Black.copy(alpha = 0.25f),
                                    Color(0xFF21242B).copy(alpha = 0.94f)
                                )
                            )
                        )
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    ModrinthMetaPill(text = chip1, highlighted = true)
                    ModrinthMetaPill(text = chip2)
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 11.dp, vertical = 9.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF9CA3AF),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Surface(
                        shape = CircleShape,
                        color = ModrinthEmeraldDark,
                        contentColor = ModrinthEmerald,
                        onClick = action,
                        modifier = Modifier.size(30.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                painter = painterResource(if (index == 1) R.drawable.ic_search else R.drawable.ic_add),
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    ModrinthMetaPill(text = chip1, highlighted = true)
                    ModrinthMetaPill(text = chip2)
                }
            }
        }
    }
}

@Composable
private fun ModrinthRecentInstanceCard(
    version: Version,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onPlay: () -> Unit,
    onSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) ModrinthEmerald else ModrinthCardBorder,
        animationSpec = tween(200),
        label = "recentCardBorder"
    )
    val info = version.getVersionInfo()
    val mcVer = info?.minecraftVersion ?: "Unknown"
    val loader = info?.loaderInfo?.loader?.displayName ?: "Vanilla"
    val rendererName = remember(version) { resolveRendererShortLabel(version) }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = ModrinthCardColor,
        border = BorderStroke(if (isSelected) 1.5.dp else 1.dp, borderColor),
        onClick = onSelect
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFF17191E),
                    border = BorderStroke(1.dp, Color(0xFF2D313B))
                ) {
                    VersionIconImage(
                        version = version,
                        modifier = Modifier
                            .padding(6.dp)
                            .size(34.dp)
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onSettings,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_settings_filled),
                            contentDescription = stringResource(R.string.versions_manage_settings),
                            tint = Color(0xFF9CA3AF),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) ModrinthEmerald else Color(0xFF2B303C),
                        contentColor = if (isSelected) ModrinthOnEmerald else Color.White,
                        onClick = onPlay
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_play_arrow_filled),
                                contentDescription = stringResource(R.string.main_launch_game),
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "Play",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = version.getVersionName(),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Minecraft $mcVer ($loader)",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF9CA3AF),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ModrinthMetaPill(text = loader)
                ModrinthMetaPill(text = mcVer)
                ModrinthMetaPill(text = rendererName, highlighted = true)
            }
        }
    }
}

@Composable
fun ModrinthMetaPill(
    text: String,
    highlighted: Boolean = false,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(6.dp),
        color = if (highlighted) ModrinthEmeraldDark else Color(0xFF2B2F3A),
        border = if (highlighted) BorderStroke(1.dp, ModrinthEmerald.copy(alpha = 0.5f)) else null
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            fontSize = 10.sp,
            fontWeight = if (highlighted) FontWeight.Bold else FontWeight.Medium,
            color = if (highlighted) ModrinthEmerald else Color(0xFFD1D5DB),
            maxLines = 1
        )
    }
}

@Composable
fun ModrinthCompactSearchField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.height(34.dp),
        shape = RoundedCornerShape(10.dp),
        color = ModrinthElevatedSurface,
        border = BorderStroke(1.dp, ModrinthCardBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_search),
                contentDescription = null,
                tint = Color(0xFF9CA3AF),
                modifier = Modifier.size(15.dp)
            )
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.CenterStart
            ) {
                if (value.isEmpty()) {
                    Text(
                        text = placeholder,
                        style = MaterialTheme.typography.labelMedium,
                        color = Color(0xFF6B7280),
                        maxLines = 1
                    )
                }
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    singleLine = true,
                    textStyle = MaterialTheme.typography.labelMedium.copy(color = Color.White),
                    cursorBrush = SolidColor(ModrinthEmerald),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            if (value.isNotEmpty()) {
                Icon(
                    painter = painterResource(R.drawable.ic_close),
                    contentDescription = stringResource(R.string.generic_clear),
                    tint = Color(0xFF9CA3AF),
                    modifier = Modifier
                        .size(15.dp)
                        .clickable { onValueChange("") }
                )
            }
        }
    }
}
