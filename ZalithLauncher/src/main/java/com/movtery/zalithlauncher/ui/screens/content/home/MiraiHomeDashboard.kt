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

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.stickyHeader
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.coroutine.TaskSystem
import com.movtery.zalithlauncher.game.optimization.JvmGcAutoTunerDialog
import com.movtery.zalithlauncher.game.optimization.MobileFpsBoosterDialog
import com.movtery.zalithlauncher.game.optimization.SmartCrashDoctorDialog
import com.movtery.zalithlauncher.game.renderer.RendererPicker
import com.movtery.zalithlauncher.game.version.installed.Version
import com.movtery.zalithlauncher.game.version.installed.VersionsManager
import com.movtery.zalithlauncher.setting.AllSettings
import com.movtery.zalithlauncher.ui.screens.content.elements.VersionIconImage
import com.movtery.zalithlauncher.ui.theme.MiraiThemeManager

private val ModrinthCardColor = Color(0xFF21242B)
private val ModrinthCardBorder = Color(0xFF2E333E)
private val ModrinthEmerald = Color(0xFF1BD96A)
private val ModrinthOnEmerald = Color(0xFF06210F)
private val ModrinthAmberBg = Color(0xFF9A6712)
private val ModrinthAmberFg = Color(0xFFFEF3C7)

private val DefaultAvailableRenderers = setOf(
    RendererPicker.LTW,
    RendererPicker.LTW_LEGACY,
    RendererPicker.VGPU,
    RendererPicker.VGPU_1368,
    RendererPicker.ZINK,
    RendererPicker.VIRGL,
    RendererPicker.GL4ES
)

fun resolveRendererShortLabel(version: Version?): String {
    val mcVer = version?.getVersionInfo()?.minecraftVersion.orEmpty()
    val manual = version?.getRenderer().orEmpty()
    val choice = RendererPicker.pick(mcVer, manual, DefaultAvailableRenderers)
    return when {
        choice.identifier == RendererPicker.VGPU -> "vgpu"
        choice.identifier == RendererPicker.VGPU_1368 -> "VGPU 1.3.6β"
        choice.identifier == RendererPicker.LTW_LEGACY || choice.identifier.contains("Legacy", ignoreCase = true) -> "LTW Legacy"
        choice.identifier == RendererPicker.LTW || choice.identifier.contains("LTW", ignoreCase = true) -> "LTW"
        choice.identifier == RendererPicker.ZINK || choice.identifier.contains("Zink", ignoreCase = true) -> "Kopper Zink"
        choice.identifier == RendererPicker.VIRGL || choice.identifier.contains("VirGL", ignoreCase = true) -> "VirGL"
        choice.identifier == RendererPicker.GL4ES || choice.identifier.contains("GL4ES", ignoreCase = true) -> "GL4ES"
        else -> "LTW"
    }
}

fun resolveRendererBadgeDetail(version: Version?): String {
    val mcVer = version?.getVersionInfo()?.minecraftVersion.orEmpty()
    val manual = version?.getRenderer().orEmpty()
    val choice = RendererPicker.pick(mcVer, manual, DefaultAvailableRenderers)
    return when {
        choice.identifier == RendererPicker.VGPU -> "vgpu - (up to 1.16.5, fast)"
        choice.identifier == RendererPicker.VGPU_1368 -> "VGPU 1.3.6β"
        choice.identifier == RendererPicker.LTW_LEGACY || choice.identifier.contains("Legacy", ignoreCase = true) -> "LTW Legacy 1.8–1.16.5"
        choice.identifier == RendererPicker.LTW || choice.identifier.contains("LTW", ignoreCase = true) -> "LTW 1.17+"
        choice.identifier == RendererPicker.ZINK || choice.identifier.contains("Zink", ignoreCase = true) -> "Kopper Zink"
        choice.identifier == RendererPicker.VIRGL || choice.identifier.contains("VirGL", ignoreCase = true) -> "VirGL"
        choice.identifier == RendererPicker.GL4ES || choice.identifier.contains("GL4ES", ignoreCase = true) -> "GL4ES"
        else -> "LTW 1.17+"
    }
}

@Composable
fun MiraiHomeDashboard(
    onLaunchVersion: (Version) -> Unit,
    onOpenVersionSettings: (Version) -> Unit,
    onExploreContent: () -> Unit,
    onCreateInstance: () -> Unit,
    onManageVersions: () -> Unit,
    onOpenFileManager: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val versions by VersionsManager.versions.collectAsStateWithLifecycle()
    val currentVersion by VersionsManager.currentVersion.collectAsStateWithLifecycle()
    val tasks by TaskSystem.tasksFlow.collectAsStateWithLifecycle()
    val activeAccent = MiraiThemeManager.currentAccent()

    var showFpsBooster by remember { mutableStateOf(false) }
    var showJreGcTuner by remember { mutableStateOf(false) }
    var showCrashDoctor by remember { mutableStateOf(false) }

    if (showFpsBooster) {
        MobileFpsBoosterDialog(
            version = currentVersion ?: versions.firstOrNull(),
            onDismiss = { showFpsBooster = false }
        )
    }

    if (showJreGcTuner) {
        JvmGcAutoTunerDialog(
            version = currentVersion ?: versions.firstOrNull(),
            onDismiss = { showJreGcTuner = false }
        )
    }

    if (showCrashDoctor) {
        SmartCrashDoctorDialog(
            version = currentVersion ?: versions.firstOrNull(),
            onDismiss = { showCrashDoctor = false }
        )
    }

    val jumpBackInVersions = remember(versions, currentVersion) {
        buildList {
            currentVersion?.let { add(it) }
            versions.filter { it != currentVersion }.take(3).forEach { add(it) }
        }.take(2)
    }

    val recentListVersions = remember(versions, jumpBackInVersions) {
        val rest = versions.filter { it !in jumpBackInVersions }
        rest.ifEmpty { versions }.take(8)
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(start = 14.dp, end = 6.dp, top = 10.dp),
        contentPadding = PaddingValues(bottom = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 1. Header pinned at the very top: quick-action pills toolbar first (scrollable
        // so narrow screens never clip a pill), dashboard title directly below it.
        stickyHeader {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFF14161B).copy(alpha = 0.94f)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                    // 1-Tap Mobile FPS Booster Pill (Always visible on dashboard)
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(18.dp))
                            .clickable { showFpsBooster = true },
                        shape = RoundedCornerShape(18.dp),
                        color = activeAccent.copy(alpha = 0.18f),
                        border = BorderStroke(1.dp, activeAccent.copy(alpha = 0.75f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_rocket_launch_filled),
                                contentDescription = "Boost FPS",
                                tint = activeAccent,
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = "Boost FPS",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = activeAccent
                            )
                        }
                    }

                    // JRE & GC Auto-Tuner Pill (Feature #17)
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(18.dp))
                            .clickable { showJreGcTuner = true },
                        shape = RoundedCornerShape(18.dp),
                        color = ModrinthCardColor,
                        border = BorderStroke(1.dp, ModrinthCardBorder)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "☕ JRE & GC",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFFE5E7EB)
                            )
                        }
                    }

                    // Smart Crash Doctor Pill
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(18.dp))
                            .clickable { showCrashDoctor = true },
                        shape = RoundedCornerShape(18.dp),
                        color = ModrinthCardColor,
                        border = BorderStroke(1.dp, ModrinthCardBorder)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "🩺 Crash Doctor",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFFE5E7EB)
                            )
                        }
                    }

                    // Files pill button
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(18.dp))
                            .clickable(onClick = onOpenFileManager),
                        shape = RoundedCornerShape(18.dp),
                        color = ModrinthCardColor,
                        border = BorderStroke(1.dp, ModrinthCardBorder)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_folder_outlined),
                                contentDescription = "Files",
                                tint = Color(0xFFD1D5DB),
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "Files",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFFE5E7EB)
                            )
                        }
                    }

                    // Tasks pill button
                    val hasActiveTasks = tasks.isNotEmpty()
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(18.dp))
                            .clickable {
                                AllSettings.launcherTaskMenuExpanded.save(
                                    !AllSettings.launcherTaskMenuExpanded.state
                                )
                            },
                        shape = RoundedCornerShape(18.dp),
                        color = if (hasActiveTasks) activeAccent.copy(alpha = 0.18f) else ModrinthCardColor,
                        border = BorderStroke(
                            1.dp,
                            if (hasActiveTasks) activeAccent else ModrinthCardBorder
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            if (hasActiveTasks) {
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .clip(CircleShape)
                                        .background(activeAccent)
                                )
                            }
                            Text(
                                text = "${tasks.size} Tasks",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (hasActiveTasks) activeAccent else Color(0xFFE5E7EB)
                            )
                        }
                    }
                    }

                    Text(
                        text = "Mirai Launcher",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                }
            }
        }

        if (versions.isEmpty()) {
            // Empty state when no instances are installed yet
            item {
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
                            text = stringResource(R.string.versions_manage_no_versions),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Button(
                                onClick = onCreateInstance,
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = activeAccent,
                                    contentColor = ModrinthOnEmerald
                                )
                            ) {
                                Text(
                                    text = "+ New Instance",
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                            Button(
                                onClick = onExploreContent,
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF2B2F3A),
                                    contentColor = Color.White
                                )
                            ) {
                                Text(
                                    text = "Browse Modpacks",
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }
        } else {
            // 2. 'Jump Back In' — 2 Wide Cards Side-by-Side (Mockup #1)
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Jump Back In",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        jumpBackInVersions.forEach { version ->
                            JumpBackInMobileCard(
                                version = version,
                                isSelected = version == currentVersion,
                                onSelect = { VersionsManager.saveVersion(version) },
                                onPlay = {
                                    VersionsManager.saveVersion(version)
                                    onLaunchVersion(version)
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        if (jumpBackInVersions.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }

            // 3. 'Recent Instances' — Clean Horizontal Rows (Mockup #1)
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recent Instances",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "View All",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = activeAccent,
                        modifier = Modifier.clickable(onClick = onManageVersions)
                    )
                }
            }

            items(
                items = recentListVersions,
                key = { it.getVersionName() }
            ) { version ->
                RecentInstanceMobileRow(
                    version = version,
                    isSelected = version == currentVersion,
                    onSelect = { VersionsManager.saveVersion(version) },
                    onSettings = {
                        VersionsManager.saveVersion(version)
                        onOpenVersionSettings(version)
                    }
                )
            }
        }
    }
}

@Composable
private fun JumpBackInMobileCard(
    version: Version,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onPlay: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activeAccent = MiraiThemeManager.currentAccent()
    val info = version.getVersionInfo()
    val mcVer = info?.minecraftVersion ?: "Unknown"
    val loaderName = info?.loaderInfo?.loader?.displayName ?: "Vanilla"

    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onSelect),
        shape = RoundedCornerShape(14.dp),
        color = ModrinthCardColor.copy(alpha = 0.92f),
        border = BorderStroke(
            width = if (isSelected) 1.5.dp else 1.dp,
            color = if (isSelected) activeAccent.copy(alpha = 0.7f) else ModrinthCardBorder
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF2B2F3A))
                    .padding(4.dp),
                contentAlignment = Alignment.Center
            ) {
                VersionIconImage(
                    version = version,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = version.getVersionName(),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = "$loaderName $mcVer",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF9CA3AF),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Circular Dynamic Accent Play Button
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(activeAccent)
                    .clickable(onClick = onPlay),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_play_arrow_filled),
                    contentDescription = stringResource(R.string.main_launch_game),
                    tint = ModrinthOnEmerald,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun RecentInstanceMobileRow(
    version: Version,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onSettings: () -> Unit
) {
    val activeAccent = MiraiThemeManager.currentAccent()
    val info = version.getVersionInfo()
    val mcVer = info?.minecraftVersion ?: "Unknown"
    val loaderName = info?.loaderInfo?.loader?.displayName ?: "Vanilla"

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onSelect),
        shape = RoundedCornerShape(12.dp),
        color = ModrinthCardColor.copy(alpha = 0.92f),
        border = BorderStroke(
            width = 1.dp,
            color = if (isSelected) activeAccent.copy(alpha = 0.55f) else ModrinthCardBorder
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            VersionIconImage(
                version = version,
                modifier = Modifier
                    .size(22.dp)
                    .clip(RoundedCornerShape(6.dp))
            )

            Text(
                text = "${version.getVersionName()} • $loaderName $mcVer",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFFE5E7EB),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )

            IconButton(
                onClick = onSettings,
                modifier = Modifier.size(26.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_settings_filled),
                    contentDescription = "Instance Settings",
                    tint = Color(0xFF9CA3AF),
                    modifier = Modifier.size(15.dp)
                )
            }
        }
    }
}

@Composable
fun ModrinthMetaPill(
    text: String,
    highlighted: Boolean = false,
    backgroundColor: Color = if (highlighted) Color(0xFF1A3A2A) else Color(0xFF282C36),
    textColor: Color = if (highlighted) ModrinthEmerald else Color(0xFFD1D5DB),
    borderColor: Color? = if (highlighted) ModrinthEmerald.copy(alpha = 0.45f) else null
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .then(
                if (borderColor != null) {
                    Modifier.border(1.dp, borderColor, RoundedCornerShape(8.dp))
                } else Modifier
            )
            .padding(horizontal = 7.dp, vertical = 2.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = textColor,
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
    Row(
        modifier = modifier
            .height(36.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(Color(0xFF21242B))
            .border(1.dp, Color(0xFF2E333E), RoundedCornerShape(18.dp))
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_search),
            contentDescription = null,
            tint = Color(0xFF9CA3AF),
            modifier = Modifier.size(16.dp)
        )
        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.CenterStart
        ) {
            if (value.isEmpty()) {
                Text(
                    text = placeholder,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF8A909E),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                textStyle = TextStyle(
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                ),
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
