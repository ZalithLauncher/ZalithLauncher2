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
 * along with this program.  If not, see <https://www.gnu.org/licenses/gpl-3.0.txt>.
 */

package com.movtery.zalithlauncher.game.optimization

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.game.version.installed.Version
import com.movtery.zalithlauncher.setting.AllSettings
import com.movtery.zalithlauncher.ui.theme.MiraiThemeManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

enum class FpsBoostPreset(
    val title: String,
    val subtitle: String,
    val badge: String,
    val renderScale: Int,
    val optionsMap: Map<String, String>
) {
    ULTRA_120FPS(
        title = "Ultra FPS (120Hz Competitive)",
        subtitle = "6 chunks • Fast graphics • No shadows/clouds • 75% scale",
        badge = "+220% FPS",
        renderScale = 75,
        optionsMap = mapOf(
            "renderDistance" to "6",
            "simulationDistance" to "5",
            "graphicsMode" to "0",
            "ao" to "false",
            "biomeBlendRadius" to "0",
            "cloudStatus" to "\"off\"",
            "particles" to "2",
            "entityShadows" to "false",
            "maxFps" to "120",
            "enableVsync" to "false",
            "mipmapLevels" to "1",
            "entityDistanceScaling" to "0.75"
        )
    ),
    BALANCED_MOBILE(
        title = "Balanced Mobile (Recommended)",
        subtitle = "8 chunks • Smooth lighting • Low particles • 85% scale",
        badge = "+140% FPS",
        renderScale = 85,
        optionsMap = mapOf(
            "renderDistance" to "8",
            "simulationDistance" to "6",
            "graphicsMode" to "1",
            "ao" to "true",
            "biomeBlendRadius" to "1",
            "cloudStatus" to "\"fast\"",
            "particles" to "1",
            "entityShadows" to "false",
            "maxFps" to "120",
            "enableVsync" to "false",
            "mipmapLevels" to "2",
            "entityDistanceScaling" to "0.85"
        )
    ),
    BATTERY_SAVER(
        title = "Cool & Battery Saver (60 FPS Cap)",
        subtitle = "6 chunks • 60 FPS VSync cap • Prevents thermal throttling",
        badge = "-8°C Temp",
        renderScale = 70,
        optionsMap = mapOf(
            "renderDistance" to "6",
            "simulationDistance" to "5",
            "graphicsMode" to "0",
            "ao" to "false",
            "biomeBlendRadius" to "0",
            "cloudStatus" to "\"off\"",
            "particles" to "2",
            "entityShadows" to "false",
            "maxFps" to "60",
            "enableVsync" to "true",
            "mipmapLevels" to "1",
            "entityDistanceScaling" to "0.7"
        )
    )
}

object MobileFpsBooster {
    private val optimizationModSlugs = listOf(
        "fabric-api" to "Fabric API",
        "sodium" to "Sodium",
        "lithium" to "Lithium",
        "ferrite-core" to "FerriteCore",
        "entityculling" to "EntityCulling",
        "immediatelyfast" to "ImmediatelyFast",
        "modernfix" to "ModernFix"
    )

    suspend fun applyMobileOptionsTxt(
        version: Version,
        preset: FpsBoostPreset
    ) = withContext(Dispatchers.IO) {
        val gameDir = version.getGameDir()
        if (!gameDir.exists()) gameDir.mkdirs()
        val optionsFile = File(gameDir, "options.txt")

        val existingLines = if (optionsFile.exists() && optionsFile.isFile) {
            runCatching { optionsFile.readLines() }.getOrDefault(emptyList())
        } else {
            emptyList()
        }

        val updatedKeys = mutableSetOf<String>()
        val newLines = existingLines.map { line ->
            val colonIndex = line.indexOf(':')
            if (colonIndex > 0) {
                val key = line.substring(0, colonIndex).trim()
                val replacement = preset.optionsMap[key]
                if (replacement != null) {
                    updatedKeys.add(key)
                    "$key:$replacement"
                } else {
                    line
                }
            } else {
                line
            }
        }.toMutableList()

        preset.optionsMap.forEach { (key, value) ->
            if (key !in updatedKeys) {
                newLines.add("$key:$value")
            }
        }

        optionsFile.writeText(newLines.joinToString("\n") + "\n")

        // Ensure Sodium (1.16.5+) options are tuned for mobile VGPU/GLES renderers
        runCatching {
            val configDir = File(gameDir, "config")
            if (!configDir.exists()) configDir.mkdirs()
            val sodiumFile = File(configDir, "sodium-options.json")
            val smoothLighting = if (preset == FpsBoostPreset.BALANCED_MOBILE) "LOW" else "OFF"
            val enableClouds = preset == FpsBoostPreset.BALANCED_MOBILE
            sodiumFile.writeText(
                """
                {
                  "quality": {
                    "cloud_quality": "FAST",
                    "weather_quality": "FAST",
                    "enable_vignette": false,
                    "enable_clouds": $enableClouds,
                    "smooth_lighting": "$smoothLighting"
                  },
                  "advanced": {
                    "use_vertex_array_objects": true,
                    "use_chunk_multidraw": false,
                    "animate_only_visible_textures": true,
                    "use_entity_culling": true,
                    "use_particle_culling": true,
                    "use_fog_occlusion": true,
                    "use_compact_vertex_format": true,
                    "use_block_face_culling": true,
                    "allow_direct_memory_access": true,
                    "ignore_driver_blacklist": false
                  },
                  "notifications": {
                    "hide_donation_button": true
                  }
                }
                """.trimIndent() + "\n"
            )
        }

        withContext(Dispatchers.Main) {
            AllSettings.resolutionRatio.save(preset.renderScale)
        }
    }

    suspend fun installModrinthOptimizationMods(
        version: Version,
        onProgress: (String) -> Unit
    ): Int = withContext(Dispatchers.IO) {
        val info = version.getVersionInfo() ?: return@withContext 0
        val mcVer = info.minecraftVersion
        val rawLoader = info.loaderInfo?.loader?.name?.lowercase() ?: ""
        val loader = when {
            rawLoader.contains("fabric") -> "fabric"
            rawLoader.contains("quilt") -> "quilt"
            rawLoader.contains("neoforge") -> "neoforge"
            rawLoader.contains("forge") -> "forge"
            else -> return@withContext 0
        }

        val modsDir = File(version.getGameDir(), "mods")
        if (!modsDir.exists()) modsDir.mkdirs()

        var installedCount = 0
        val existingLower = modsDir.listFiles()?.map { it.name.lowercase() } ?: emptyList()

        for ((slug, displayName) in optimizationModSlugs) {
            if (slug == "fabric-api" && loader != "fabric") continue
            if (existingLower.any { it.contains(slug.replace("-", "")) || it.contains(slug) }) {
                onProgress("$displayName already installed ✓")
                installedCount++
                continue
            }

            onProgress("Fetching $displayName for $loader $mcVer...")
            val downloaded = runCatching {
                downloadLatestModrinthJar(
                    slug = slug,
                    mcVersion = mcVer,
                    loader = loader,
                    modsDir = modsDir
                )
            }.getOrDefault(false)

            if (downloaded) {
                installedCount++
                onProgress("Installed $displayName ✓")
            }
        }
        installedCount
    }

    private fun downloadLatestModrinthJar(
        slug: String,
        mcVersion: String,
        loader: String,
        modsDir: File
    ): Boolean {
        val loadersParam = URLEncoder.encode("[\"$loader\"]", "UTF-8")
        val gameVersionsParam = URLEncoder.encode("[\"$mcVersion\"]", "UTF-8")
        val apiUrl = "https://api.modrinth.com/v2/project/$slug/version?loaders=$loadersParam&game_versions=$gameVersionsParam"

        val conn = (URL(apiUrl).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 8000
            readTimeout = 8000
            setRequestProperty("User-Agent", "MiraiLauncher/2.6.1 (Android)")
        }

        if (conn.responseCode != 200) return false
        val body = conn.inputStream.bufferedReader().use { it.readText() }
        val versionsArray = JSONArray(body)
        if (versionsArray.length() == 0) return false

        val firstVersion = versionsArray.getJSONObject(0)
        val filesArray = firstVersion.optJSONArray("files") ?: return false
        if (filesArray.length() == 0) return false

        var chosenFileObj = filesArray.getJSONObject(0)
        for (i in 0 until filesArray.length()) {
            val f = filesArray.getJSONObject(i)
            if (f.optBoolean("primary", false)) {
                chosenFileObj = f
                break
            }
        }

        val downloadUrl = chosenFileObj.optString("url", "")
        val fileName = chosenFileObj.optString("filename", "$slug-$mcVersion.jar")
        if (downloadUrl.isBlank()) return false

        val targetFile = File(modsDir, fileName)
        val tempFile = File(modsDir, "$fileName.tmp")

        val dlConn = (URL(downloadUrl).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 10000
            readTimeout = 15000
            setRequestProperty("User-Agent", "MiraiLauncher/2.6.1 (Android)")
        }
        if (dlConn.responseCode != 200) return false

        dlConn.inputStream.use { input ->
            tempFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        if (tempFile.length() > 1024) {
            tempFile.renameTo(targetFile)
            return true
        } else {
            tempFile.delete()
            return false
        }
    }
}

@Composable
fun MobileFpsBoosterDialog(
    version: Version?,
    onDismiss: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val accentColor = MiraiThemeManager.currentAccent()
    val info = remember(version) { version?.getVersionInfo() }
    val loaderName = info?.loaderInfo?.loader?.displayName
    val hasModLoader = version != null && loaderName != null && !loaderName.equals("Vanilla", ignoreCase = true)

    // Restore the last selection so reopening the dialog never resets the UI.
    var selectedPreset by remember {
        mutableStateOf(
            FpsBoostPreset.entries.firstOrNull { it.name == AllSettings.fpsBoostPreset.state }
                ?: FpsBoostPreset.BALANCED_MOBILE
        )
    }
    var tuneOptionsTxt by remember { mutableStateOf(AllSettings.fpsBoostTuneOptions.state) }
    var installFpsMods by remember {
        mutableStateOf(if (hasModLoader) AllSettings.fpsBoostInstallMods.state else false)
    }
    var isRunning by remember { mutableStateOf(false) }
    var statusText by remember { mutableStateOf<String?>(null) }
    var isDone by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = { if (!isRunning) onDismiss() }) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 560.dp),
            shape = RoundedCornerShape(18.dp),
            color = Color(0xFF1A1D24),
            border = BorderStroke(1.dp, accentColor.copy(alpha = 0.5f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(accentColor.copy(alpha = 0.18f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_rocket_launch_filled),
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "1-Tap Mobile FPS Booster",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                        Text(
                            text = if (version != null) {
                                "Instance: ${version.getVersionName()} (${loaderName ?: "Vanilla"} ${info?.minecraftVersion ?: ""})"
                            } else {
                                "Global Mobile FPS Preset (Applies to launcher & instances)"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF9CA3AF),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Preset Selector Cards
                FpsBoostPreset.entries.forEach { preset ->
                    val isSelected = selectedPreset == preset
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable(enabled = !isRunning) {
                                selectedPreset = preset
                                AllSettings.fpsBoostPreset.save(preset.name)
                            },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) accentColor.copy(alpha = 0.14f) else Color(0xFF21242B),
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) accentColor else Color(0xFF2E333E)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = preset.title,
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) accentColor else Color.White
                                )
                                Text(
                                    text = preset.subtitle,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFF9CA3AF)
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = accentColor.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = preset.badge,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = accentColor,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }
                }

                // Checkboxes
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = !isRunning) {
                            tuneOptionsTxt = !tuneOptionsTxt
                            AllSettings.fpsBoostTuneOptions.save(tuneOptionsTxt)
                        }
                        .padding(vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = tuneOptionsTxt,
                        onCheckedChange = {
                            if (!isRunning) {
                                tuneOptionsTxt = it
                                AllSettings.fpsBoostTuneOptions.save(it)
                            }
                        },
                        colors = CheckboxDefaults.colors(checkedColor = accentColor)
                    )
                    Text(
                        text = "Optimize options.txt & Mobile Render Scale (${selectedPreset.renderScale}%)",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color(0xFFE5E7EB)
                    )
                }

                if (hasModLoader) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = !isRunning) {
                                installFpsMods = !installFpsMods
                                AllSettings.fpsBoostInstallMods.save(installFpsMods)
                            }
                            .padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = installFpsMods,
                            onCheckedChange = {
                                if (!isRunning) {
                                    installFpsMods = it
                                    AllSettings.fpsBoostInstallMods.save(it)
                                }
                            },
                            colors = CheckboxDefaults.colors(checkedColor = accentColor)
                        )
                        Text(
                            text = "Auto-Install Modrinth FPS Mods (Sodium, Lithium, FerriteCore, EntityCulling, ModernFix)",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color(0xFFE5E7EB)
                        )
                    }
                }

                // Status / Progress Banner
                statusText?.let { msg ->
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFF14161A),
                        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.4f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (isRunning) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = accentColor
                                )
                            } else {
                                Icon(
                                    painter = painterResource(R.drawable.ic_check),
                                    contentDescription = null,
                                    tint = accentColor,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Text(
                                text = msg,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = accentColor
                            )
                        }
                    }
                }

                // Bottom Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFF21242B),
                        border = BorderStroke(1.dp, Color(0xFF2E333E)),
                        onClick = { if (!isRunning) onDismiss() }
                    ) {
                        Text(
                            text = if (isDone) "Done" else "Cancel",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFE5E7EB),
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                        )
                    }

                    if (!isDone) {
                        Spacer(Modifier.width(8.dp))
                        Button(
                            enabled = !isRunning && (tuneOptionsTxt || installFpsMods),
                            onClick = {
                                isRunning = true
                                scope.launch {
                                    if (tuneOptionsTxt) {
                                        statusText = "Applying mobile FPS preset..."
                                        if (version != null) {
                                            MobileFpsBooster.applyMobileOptionsTxt(version, selectedPreset)
                                        } else {
                                            AllSettings.resolutionRatio.save(selectedPreset.renderScale)
                                        }
                                    }
                                    var modsInstalled = 0
                                    if (installFpsMods && hasModLoader && version != null) {
                                        modsInstalled = MobileFpsBooster.installModrinthOptimizationMods(version) { step ->
                                            statusText = step
                                        }
                                    }
                                    isRunning = false
                                    isDone = true
                                    statusText = buildString {
                                        append("Boost Complete! ")
                                        if (tuneOptionsTxt) append("Mobile render scale set to ${selectedPreset.renderScale}%. ")
                                        if (installFpsMods && hasModLoader) append("$modsInstalled FPS mods ready in mods/.")
                                    }
                                }
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = accentColor,
                                contentColor = Color(0xFF06210F)
                            ),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = "⚡ Apply Boost Now",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }
            }
        }
    }
}
