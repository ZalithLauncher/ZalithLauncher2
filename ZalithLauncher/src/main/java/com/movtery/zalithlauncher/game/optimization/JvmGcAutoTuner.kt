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

import android.content.Context
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
import androidx.compose.material3.Icon
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.game.addons.modloader.ModLoader
import com.movtery.zalithlauncher.game.multirt.RuntimesManager
import com.movtery.zalithlauncher.game.version.installed.Version
import com.movtery.zalithlauncher.game.versioninfo.models.GameManifest
import com.movtery.zalithlauncher.setting.AllSettings
import com.movtery.zalithlauncher.ui.theme.MiraiThemeManager
import com.movtery.zalithlauncher.utils.platform.bytesToMB
import com.movtery.zalithlauncher.utils.platform.getMaxMemoryForSettings
import com.movtery.zalithlauncher.utils.platform.getTotalMemory
import com.movtery.zalithlauncher.utils.string.compareVersion
import com.movtery.zalithlauncher.utils.string.isBiggerTo

enum class GcTuningPreset(
    val title: String,
    val subtitle: String,
    val badge: String
) {
    MOBILE_LOW_PAUSE_G1GC(
        title = "Low-Pause G1GC (Recommended Mobile)",
        subtitle = "Eliminates 1s chunk-border lag spikes • 50ms pause target • JRE 8/17/21",
        badge = "Zero Stutter"
    ),
    GENERATIONAL_ZGC_TURBO(
        title = "Generational ZGC (8GB+ RAM & Java 21)",
        subtitle = "Sub-1ms concurrent GC pauses on Java 21 • Safe G1GC fallback on Java 8/17",
        badge = "<1ms Pause"
    ),
    COMPACT_LOW_RAM_G1GC(
        title = "Compact Memory Saver (3–4 GB Phones)",
        subtitle = "String deduplication + tight 8MB regions to prevent Android LMK kills",
        badge = "-25% RAM"
    )
}

data class JreGcRecommendation(
    val targetJavaMajor: Int,
    val matchedRuntimeName: String?,
    val recommendedRamMb: Int,
    val totalDeviceRamMb: Int,
    val recommendedPreset: GcTuningPreset,
    val reasonSummary: String
)

object JvmGcAutoTuner {

    /**
     * Determines the ideal Java major version (8, 17, 21, or 25) for a given Minecraft version & loader.
     */
    fun recommendJavaMajorVersion(
        minecraftVersion: String,
        loader: ModLoader?,
        loaderVersion: String? = null,
        manifestJavaMajor: Int? = null
    ): Int {
        if (loader == ModLoader.BABRIC) return 17
        if (loader == ModLoader.CLEANROOM) {
            return if (loaderVersion != null && loaderVersion.isBiggerTo("0.4.4-alpha")) 25 else 21
        }
        if (manifestJavaMajor != null && manifestJavaMajor > 0) {
            return manifestJavaMajor
        }
        val cleanMc = minecraftVersion.trim()
        if (cleanMc.isEmpty()) return 8

        return when {
            compareMcVersionSafe(cleanMc, "1.20.5") >= 0 -> 21
            compareMcVersionSafe(cleanMc, "1.17") >= 0 -> 17
            else -> 8
        }
    }

    /**
     * Selects the best installed runtime name for launching [version], preferring an exact major
     * version match (e.g. JRE 8 for 1.16.5/1.12.2, JRE 17 for 1.17-1.20.4, JRE 21 for 1.20.5+)
     * before falling back to nearest compatible runtime.
     */
    fun resolveOptimalRuntimeForLaunch(
        version: Version,
        gameManifest: GameManifest?
    ): String? {
        val info = version.getVersionInfo()
        val mcVer = info?.minecraftVersion ?: version.getVersionName()
        val loaderInfo = info?.loaderInfo
        val targetMajor = recommendJavaMajorVersion(
            minecraftVersion = mcVer,
            loader = loaderInfo?.loader,
            loaderVersion = loaderInfo?.version,
            manifestJavaMajor = gameManifest?.javaVersion?.majorVersion
        )

        // First try an exact major version match (critical for <= 1.16.5 where Java 8 avoids
        // old Forge/mod reflection crashes and uses less native heap than Java 17/21).
        RuntimesManager.getExactJreName(targetMajor)?.let { return it }

        // Special case: Fabric 1.16.5 can also run cleanly on JRE 17 if JRE 8 is missing.
        return RuntimesManager.getNearestJreName(targetMajor)
    }

    /**
     * Builds a complete device + instance recommendation for the UI dialog.
     */
    fun analyzeDeviceAndInstance(
        context: Context,
        version: Version?
    ): JreGcRecommendation {
        val totalDeviceRamMb = runCatching {
            getTotalMemory(context).bytesToMB(decimals = 0, roundDown = true).toInt()
        }.getOrDefault(4096).coerceAtLeast(2048)

        val maxAllocMb = runCatching {
            getMaxMemoryForSettings(context)
        }.getOrDefault(2048).coerceAtLeast(1024)

        val info = version?.getVersionInfo()
        val mcVer = info?.minecraftVersion ?: "1.16.5"
        val loader = info?.loaderInfo?.loader
        val loaderVer = info?.loaderInfo?.version

        val targetJavaMajor = recommendJavaMajorVersion(
            minecraftVersion = mcVer,
            loader = loader,
            loaderVersion = loaderVer
        )

        val matchedRuntime = runCatching {
            RuntimesManager.getExactJreName(targetJavaMajor)
                ?: RuntimesManager.getNearestJreName(targetJavaMajor)
        }.getOrNull()

        val modsCount = runCatching {
            version?.getGameDir()?.resolve("mods")
                ?.listFiles { f -> f.isFile && (f.name.endsWith(".jar") || f.name.endsWith(".jar.disabled")) }
                ?.size ?: 0
        }.getOrDefault(0)

        val idealRamMb = when {
            totalDeviceRamMb <= 4200 -> 1536
            compareMcVersionSafe(mcVer, "1.16.5") <= 0 && modsCount < 40 -> 2048
            compareMcVersionSafe(mcVer, "1.16.5") <= 0 -> 2560
            modsCount >= 80 -> 3584
            totalDeviceRamMb >= 7500 -> 3072
            else -> 2304
        }.coerceIn(1024, maxAllocMb)

        val recommendedPreset = when {
            totalDeviceRamMb <= 4200 -> GcTuningPreset.COMPACT_LOW_RAM_G1GC
            targetJavaMajor >= 21 && totalDeviceRamMb >= 7500 -> GcTuningPreset.GENERATIONAL_ZGC_TURBO
            else -> GcTuningPreset.MOBILE_LOW_PAUSE_G1GC
        }

        val loaderLabel = loader?.displayName ?: "Vanilla"
        val reasonSummary = "Detected ${totalDeviceRamMb}MB device RAM • $loaderLabel $mcVer ($modsCount mods) → Best with JRE $targetJavaMajor & ${idealRamMb}MB heap"

        return JreGcRecommendation(
            targetJavaMajor = targetJavaMajor,
            matchedRuntimeName = matchedRuntime,
            recommendedRamMb = idealRamMb,
            totalDeviceRamMb = totalDeviceRamMb,
            recommendedPreset = recommendedPreset,
            reasonSummary = reasonSummary
        )
    }

    /**
     * Generates JVM GC flags for a given [preset], [javaMajor], and [ramAllocationMb].
     */
    fun buildJvmFlags(
        preset: GcTuningPreset,
        javaMajor: Int,
        ramAllocationMb: Int
    ): List<String> {
        val effectivePreset = if (preset == GcTuningPreset.GENERATIONAL_ZGC_TURBO && javaMajor < 21) {
            GcTuningPreset.MOBILE_LOW_PAUSE_G1GC
        } else {
            preset
        }

        return when (effectivePreset) {
            GcTuningPreset.GENERATIONAL_ZGC_TURBO -> listOf(
                "-XX:+UnlockExperimentalVMOptions",
                "-XX:+UseZGC",
                "-XX:+ZGenerational",
                "-XX:+DisableExplicitGC",
                "-XX:+AlwaysActAsServerClassMachine"
            )
            GcTuningPreset.COMPACT_LOW_RAM_G1GC -> listOf(
                "-XX:+UnlockExperimentalVMOptions",
                "-XX:+UseG1GC",
                "-XX:MaxGCPauseMillis=35",
                "-XX:G1NewSizePercent=15",
                "-XX:G1ReservePercent=15",
                "-XX:G1HeapRegionSize=8M",
                "-XX:InitiatingHeapOccupancyPercent=20",
                "-XX:+UseStringDeduplication",
                "-XX:+DisableExplicitGC",
                "-XX:+ParallelRefProcEnabled"
            )
            GcTuningPreset.MOBILE_LOW_PAUSE_G1GC -> {
                val regionSize = if (ramAllocationMb >= 4096) "32M" else "16M"
                listOf(
                    "-XX:+UnlockExperimentalVMOptions",
                    "-XX:+UseG1GC",
                    "-XX:G1NewSizePercent=20",
                    "-XX:G1MaxNewSizePercent=40",
                    "-XX:G1ReservePercent=20",
                    "-XX:MaxGCPauseMillis=50",
                    "-XX:G1HeapRegionSize=$regionSize",
                    "-XX:+DisableExplicitGC",
                    "-XX:+ParallelRefProcEnabled"
                )
            }
        }
    }

    /**
     * Sanitizes incompatible GC flags before JVM launch and injects mobile-tuned G1GC flags if
     * no GC algorithm is present. Guarantees that switching between Java 8, 17, and 21 never fails
     * with `Unrecognized VM option`.
     */
    fun sanitizeAndInjectGcArgs(
        args: MutableList<String>,
        javaMajor: Int,
        ramAllocationMb: Int
    ) {
        val effectiveJava = if (javaMajor > 0) javaMajor else 8

        // 1. Strip Java 8 CMS flags if running on Java 14+ (CMS was removed in JDK 14)
        if (effectiveJava >= 14) {
            args.removeIf { arg ->
                arg.startsWith("-XX:+UseConcMarkSweepGC") ||
                    arg.startsWith("-XX:+CMSIncrementalMode") ||
                    arg.startsWith("-XX:+UseParNewGC") ||
                    arg.startsWith("-XX:CMSInitiatingOccupancyFraction") ||
                    arg.startsWith("-XX:+UseCMSInitiatingOccupancyOnly")
            }
        }

        // 2. Strip Java 21 Generational ZGC flags if running on Java 8 or Java 17
        if (effectiveJava < 21) {
            val hadZgc = args.any { it == "-XX:+UseZGC" || it == "-XX:+ZGenerational" }
            if (hadZgc) {
                args.removeIf { arg ->
                    arg == "-XX:+UseZGC" ||
                        arg == "-XX:+ZGenerational" ||
                        arg == "-XX:-ZGenerational"
                }
            }
        }

        // 3. If no GC algorithm flag is present, inject tuned Low-Pause G1GC flags automatically
        val hasGcSelector = args.any { arg ->
            arg == "-XX:+UseG1GC" ||
                arg == "-XX:+UseZGC" ||
                arg == "-XX:+UseShenandoahGC" ||
                arg == "-XX:+UseParallelGC" ||
                arg == "-XX:+UseSerialGC" ||
                arg == "-XX:+UseConcMarkSweepGC"
        }

        if (!hasGcSelector) {
            val defaultFlags = buildJvmFlags(
                preset = GcTuningPreset.MOBILE_LOW_PAUSE_G1GC,
                javaMajor = effectiveJava,
                ramAllocationMb = ramAllocationMb
            )
            defaultFlags.forEach { flag ->
                val prefix = flag.substringBefore('=')
                if (args.none { it.startsWith(prefix) }) {
                    args.add(flag)
                }
            }
        }
    }

    /**
     * Applies the chosen JRE + GC tuning preset + RAM allocation to [version] (and global settings).
     */
    fun applyTuningToInstance(
        context: Context,
        version: Version?,
        preset: GcTuningPreset,
        autoMatchJre: Boolean,
        applyOptimalRam: Boolean
    ): String {
        val recommendation = analyzeDeviceAndInstance(context, version)
        val flagsString = buildJvmFlags(
            preset = preset,
            javaMajor = recommendation.targetJavaMajor,
            ramAllocationMb = recommendation.recommendedRamMb
        ).joinToString(" ")

        AllSettings.autoPickJavaRuntime.save(true)

        if (version != null) {
            val config = version.getVersionConfig()
            // Preserve non-GC custom args if user had any custom -D properties
            val existingNonGc = config.jvmArgs
                .split(Regex("\\s+"))
                .filter { token ->
                    token.isNotBlank() &&
                        !token.startsWith("-XX:") &&
                        !token.startsWith("-Xms") &&
                        !token.startsWith("-Xmx")
                }
            config.jvmArgs = (existingNonGc + flagsString).joinToString(" ").trim()

            if (autoMatchJre && recommendation.matchedRuntimeName != null) {
                config.javaRuntime = recommendation.matchedRuntimeName
            }
            if (applyOptimalRam) {
                config.ramAllocation = recommendation.recommendedRamMb
            }
            config.save()
        } else {
            AllSettings.jvmArgs.save(flagsString)
            if (applyOptimalRam) {
                AllSettings.ramAllocation.save(recommendation.recommendedRamMb)
            }
        }

        val jreLabel = recommendation.matchedRuntimeName ?: "Auto (JRE ${recommendation.targetJavaMajor})"
        return "Applied ${preset.title} • Runtime: $jreLabel • Heap: ${recommendation.recommendedRamMb} MB"
    }

    private fun compareMcVersionSafe(v1: String, v2: String): Int {
        return runCatching { v1.compareVersion(v2) }.getOrDefault(0)
    }
}

@Composable
fun JvmGcAutoTunerDialog(
    version: Version?,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val activeAccent = MiraiThemeManager.currentAccent()
    val recommendation = remember(version) {
        JvmGcAutoTuner.analyzeDeviceAndInstance(context, version)
    }

    // Restore the last selection so reopening the dialog never resets the UI.
    var selectedPreset by remember {
        mutableStateOf(
            GcTuningPreset.entries.firstOrNull { it.name == AllSettings.jvmGcPreset.state }
                ?: recommendation.recommendedPreset
        )
    }
    var autoMatchJre by remember { mutableStateOf(AllSettings.jvmGcAutoMatchJre.state) }
    var applyOptimalRam by remember { mutableStateOf(AllSettings.jvmGcApplyOptimalRam.state) }
    var statusMessage by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 560.dp),
            shape = RoundedCornerShape(20.dp),
            color = Color(0xFF16181D),
            border = BorderStroke(1.dp, activeAccent.copy(alpha = 0.45f))
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
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(activeAccent.copy(alpha = 0.18f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_settings_filled),
                                contentDescription = null,
                                tint = activeAccent,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "JRE Auto-Tuner & GC Optimizer",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                            Text(
                                text = version?.let { "Instance: ${it.getVersionName()}" } ?: "Global Runtime & GC Settings",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF9CA3AF)
                            )
                        }
                    }
                }

                // Hardware & JRE Detection Banner
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF1E242B),
                    border = BorderStroke(1.dp, activeAccent.copy(alpha = 0.35f))
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Text(
                            text = "☕ Recommended: JRE ${recommendation.targetJavaMajor} (${recommendation.matchedRuntimeName ?: "Auto-Pick"})",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = activeAccent
                        )
                        Text(
                            text = recommendation.reasonSummary,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFFD1D5DB)
                        )
                    }
                }

                // GC Preset Cards
                GcTuningPreset.entries.forEach { preset ->
                    val isSelected = selectedPreset == preset
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                selectedPreset = preset
                                AllSettings.jvmGcPreset.save(preset.name)
                            },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) activeAccent.copy(alpha = 0.14f) else Color(0xFF20232A),
                        border = BorderStroke(
                            width = if (isSelected) 1.5.dp else 1.dp,
                            color = if (isSelected) activeAccent else Color(0xFF2D313B)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Text(
                                    text = preset.title,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) activeAccent else Color.White,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = preset.subtitle,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFF9CA3AF),
                                    maxLines = 3,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Spacer(Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) activeAccent else Color(0xFF2D313B)
                            ) {
                                Text(
                                    text = preset.badge,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (isSelected) Color(0xFF06210F) else Color(0xFFE5E7EB),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }

                // Checkboxes for Auto-Match JRE & Optimal RAM
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .clickable {
                                autoMatchJre = !autoMatchJre
                                AllSettings.jvmGcAutoMatchJre.save(autoMatchJre)
                            },
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFF20232A),
                        border = BorderStroke(1.dp, Color(0xFF2D313B))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = autoMatchJre,
                                onCheckedChange = {
                                    autoMatchJre = it
                                    AllSettings.jvmGcAutoMatchJre.save(it)
                                },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = activeAccent,
                                    checkmarkColor = Color(0xFF06210F)
                                )
                            )
                            Text(
                                text = "Pin JRE ${recommendation.targetJavaMajor}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White,
                                maxLines = 1
                            )
                        }
                    }

                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .clickable {
                                applyOptimalRam = !applyOptimalRam
                                AllSettings.jvmGcApplyOptimalRam.save(applyOptimalRam)
                            },
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFF20232A),
                        border = BorderStroke(1.dp, Color(0xFF2D313B))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = applyOptimalRam,
                                onCheckedChange = {
                                    applyOptimalRam = it
                                    AllSettings.jvmGcApplyOptimalRam.save(it)
                                },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = activeAccent,
                                    checkmarkColor = Color(0xFF06210F)
                                )
                            )
                            Text(
                                text = "Set ${recommendation.recommendedRamMb}MB RAM",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White,
                                maxLines = 1
                            )
                        }
                    }
                }

                statusMessage?.let { msg ->
                    Text(
                        text = msg,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = activeAccent
                    )
                }

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF262932),
                            contentColor = Color(0xFFE5E7EB)
                        ),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text("Close", style = MaterialTheme.typography.labelMedium)
                    }

                    Spacer(Modifier.width(8.dp))

                    Button(
                        onClick = {
                            statusMessage = JvmGcAutoTuner.applyTuningToInstance(
                                context = context,
                                version = version,
                                preset = selectedPreset,
                                autoMatchJre = autoMatchJre,
                                applyOptimalRam = applyOptimalRam
                            )
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = activeAccent,
                            contentColor = Color(0xFF06210F)
                        ),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "Apply JRE & GC Tuning",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
        }
    }
}
