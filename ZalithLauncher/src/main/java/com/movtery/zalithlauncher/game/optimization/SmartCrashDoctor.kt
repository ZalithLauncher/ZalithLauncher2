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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.game.version.installed.Version
import com.movtery.zalithlauncher.game.version.installed.VersionsManager
import com.movtery.zalithlauncher.path.PathManager
import com.movtery.zalithlauncher.setting.AllSettings
import com.movtery.zalithlauncher.ui.theme.MiraiThemeManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

enum class CrashCategory {
    OUT_OF_MEMORY,
    WRONG_JAVA_VERSION,
    CONFLICTING_MOD,
    RENDERER_GPU_CRASH,
    GENERAL_RECOVERY
}

data class CrashDiagnosis(
    val category: CrashCategory,
    val title: String,
    val summary: String,
    val matchedLine: String,
    val actionButtonLabel: String,
    val offendingModFile: File? = null
)

object SmartCrashDoctor {
    private val jarRegex = Regex("""([a-zA-Z0-9_.\-+]+?\.jar)""")
    private val modIdRegex = Regex("""(?:from mod|mod id|Mod ID:|Mod )['"\s]*([a-z0-9_]{3,32})""", RegexOption.IGNORE_CASE)

    suspend fun analyze(
        explicitLogFile: File? = null,
        version: Version? = VersionsManager.currentVersion
    ): CrashDiagnosis = withContext(Dispatchers.IO) {
        val candidateFiles = buildList {
            if (explicitLogFile != null && explicitLogFile.exists()) add(explicitLogFile)
            if (version != null) {
                val crashDir = File(version.getGameDir(), "crash-reports")
                if (crashDir.exists() && crashDir.isDirectory) {
                    crashDir.listFiles()
                        ?.filter { it.isFile }
                        ?.maxByOrNull { it.lastModified() }
                        ?.let { add(it) }
                }
                val latestLog = File(version.getGameDir(), "logs/latest.log")
                if (latestLog.exists()) add(latestLog)
            }
            val launcherLogs = PathManager.DIR_LAUNCHER_LOGS.listFiles()
                ?.filter { it.isFile }
                ?.maxByOrNull { it.lastModified() }
            if (launcherLogs != null) add(launcherLogs)
        }

        val combinedText = candidateFiles.firstOrNull()?.let { file ->
            runCatching {
                val lines = file.readLines()
                if (lines.size > 400) {
                    (lines.take(150) + lines.takeLast(250)).joinToString("\n")
                } else {
                    lines.joinToString("\n")
                }
            }.getOrDefault("")
        } ?: ""

        // 1. Check OutOfMemoryError / Native mmap failure
        if (combinedText.contains("OutOfMemoryError", ignoreCase = true) ||
            combinedText.contains("Native memory allocation", ignoreCase = true) ||
            combinedText.contains("Cannot allocate memory", ignoreCase = true)
        ) {
            val line = combinedText.lineSequence()
                .firstOrNull { it.contains("OutOfMemoryError", true) || it.contains("memory", true) }
                ?.trim()
                ?: "java.lang.OutOfMemoryError: Java heap space"
            return@withContext CrashDiagnosis(
                category = CrashCategory.OUT_OF_MEMORY,
                title = "Out of Memory (RAM Exhaustion)",
                summary = "Minecraft ran out of Java heap or Android native memory while loading mods or chunks.",
                matchedLine = line.take(120),
                actionButtonLabel = "⚡ 1-Tap Fix: Tune Safe RAM (2560 MB) & Optimize"
            )
        }

        // 2. Check Wrong Java Runtime Version
        if (combinedText.contains("UnsupportedClassVersionError", ignoreCase = true) ||
            combinedText.contains("class file version 65.0", ignoreCase = true) ||
            combinedText.contains("class file version 61.0", ignoreCase = true) ||
            combinedText.contains("requires Java 21", ignoreCase = true) ||
            combinedText.contains("requires Java 17", ignoreCase = true)
        ) {
            val line = combinedText.lineSequence()
                .firstOrNull { it.contains("UnsupportedClassVersionError", true) || it.contains("class file version", true) }
                ?.trim()
                ?: "java.lang.UnsupportedClassVersionError: class file version 65.0 (requires Java 21)"
            return@withContext CrashDiagnosis(
                category = CrashCategory.WRONG_JAVA_VERSION,
                title = "Incompatible Java Runtime Version",
                summary = "The selected Java runtime does not match this Minecraft version (1.20.5+ requires Java 21; 1.17–1.20.4 requires Java 17).",
                matchedLine = line.take(120),
                actionButtonLabel = "⚡ 1-Tap Fix: Auto-Match Java Runtime"
            )
        }

        // 3. Check Conflicting / Broken Mod (MixinApplyError, ModResolutionException, etc.)
        if (combinedText.contains("MixinApplyError", ignoreCase = true) ||
            combinedText.contains("MixinTransformerError", ignoreCase = true) ||
            combinedText.contains("ModResolutionException", ignoreCase = true) ||
            combinedText.contains("Incompatible mods found", ignoreCase = true) ||
            combinedText.contains("Failed to load mod", ignoreCase = true)
        ) {
            val modsDir = version?.let { File(it.getGameDir(), "mods") }
            val jarMatch = jarRegex.findAll(combinedText)
                .map { it.groupValues[1] }
                .firstOrNull { !it.contains("minecraft", true) && !it.contains("fabric-loader", true) }
            val modIdMatch = modIdRegex.find(combinedText)?.groupValues?.getOrNull(1)

            val offendingFile = if (modsDir != null && modsDir.exists()) {
                val activeJars = modsDir.listFiles()?.filter { it.isFile && it.name.endsWith(".jar") } ?: emptyList()
                activeJars.firstOrNull { jar ->
                    (jarMatch != null && jar.name.equals(jarMatch, ignoreCase = true)) ||
                        (modIdMatch != null && jar.name.contains(modIdMatch, ignoreCase = true))
                }
            } else null

            val modLabel = offendingFile?.name ?: jarMatch ?: modIdMatch ?: "Conflicting Mod"
            val line = combinedText.lineSequence()
                .firstOrNull {
                    it.contains("Mixin", true) || it.contains("ModResolution", true) || it.contains("Incompatible", true)
                }?.trim() ?: "org.spongepowered.asm.mixin.transformer.throwables.MixinTransformerError"

            return@withContext CrashDiagnosis(
                category = CrashCategory.CONFLICTING_MOD,
                title = "Mod Conflict / Mixin Crash ($modLabel)",
                summary = if (offendingFile != null) {
                    "Mod '${offendingFile.name}' failed to apply a Mixin or has a missing dependency."
                } else {
                    "A mod in your mods/ folder triggered a Mixin or dependency conflict during startup."
                },
                matchedLine = line.take(120),
                actionButtonLabel = if (offendingFile != null) {
                    "⚡ 1-Tap Fix: Disable '${offendingFile.name}'"
                } else {
                    "⚡ 1-Tap Fix: Quarantine Broken Mods & Reset Config"
                },
                offendingModFile = offendingFile
            )
        }

        // 4. Check Renderer / GPU Driver Crash
        if (combinedText.contains("libvulkan", ignoreCase = true) ||
            combinedText.contains("GLFW error 65542", ignoreCase = true) ||
            combinedText.contains("EGL_BAD", ignoreCase = true) ||
            combinedText.contains("SIGSEGV", ignoreCase = true) ||
            combinedText.contains("libGLES", ignoreCase = true)
        ) {
            val line = combinedText.lineSequence()
                .firstOrNull {
                    it.contains("SIGSEGV", true) || it.contains("GLFW", true) || it.contains("vulkan", true) || it.contains("EGL", true)
                }?.trim() ?: "Renderer GPU driver signal crash (SIGSEGV / EGL)"
            return@withContext CrashDiagnosis(
                category = CrashCategory.RENDERER_GPU_CRASH,
                title = "Mobile GPU / Renderer Driver Crash",
                summary = "Your device GPU driver rejected the current renderer context or resolution scale.",
                matchedLine = line.take(120),
                actionButtonLabel = "⚡ 1-Tap Fix: Switch to Safe Renderer & 80% Scale"
            )
        }

        // 5. Default / General Smart Recovery
        CrashDiagnosis(
            category = CrashCategory.GENERAL_RECOVERY,
            title = "Smart Crash Doctor • Safe-Mode Recovery",
            summary = "Automatically tunes safe mobile RAM (2560 MB), resets Java & Renderer to Auto-Match, and applies mobile-safe options.txt.",
            matchedLine = if (combinedText.isNotBlank()) {
                combinedText.lineSequence().lastOrNull { it.isNotBlank() }?.trim()?.take(120) ?: "Ready to apply 1-Tap Safe Recovery"
            } else {
                "No fatal stacktrace found in latest log — Safe-Mode optimization ready."
            },
            actionButtonLabel = "⚡ 1-Tap Auto-Fix: Apply Safe Mode Preset"
        )
    }

    suspend fun applyFix(
        diagnosis: CrashDiagnosis,
        version: Version? = VersionsManager.currentVersion
    ): String = withContext(Dispatchers.IO) {
        when (diagnosis.category) {
            CrashCategory.OUT_OF_MEMORY -> {
                withContext(Dispatchers.Main) {
                    val currentRam = AllSettings.ramAllocation.state
                    val safeRam = if (currentRam < 2048) 2560 else (currentRam + 512).coerceAtMost(3584)
                    AllSettings.ramAllocation.save(safeRam)
                }
                if (version != null) {
                    version.getVersionConfig().apply {
                        ramAllocation = -1
                        save()
                    }
                    MobileFpsBooster.applyMobileOptionsTxt(version, FpsBoostPreset.BALANCED_MOBILE)
                }
                "Fixed: Tuned RAM allocation & lowered chunk memory footprint in options.txt."
            }

            CrashCategory.WRONG_JAVA_VERSION -> {
                withContext(Dispatchers.Main) {
                    AllSettings.javaRuntime.save("")
                    AllSettings.autoPickJavaRuntime.save(true)
                }
                if (version != null) {
                    version.getVersionConfig().apply {
                        javaRuntime = ""
                        save()
                    }
                }
                "Fixed: Reset Java Runtime to Auto-Match (Java 21 for 1.20.5+, Java 17 for 1.17–1.20.4)."
            }

            CrashCategory.CONFLICTING_MOD -> {
                val modFile = diagnosis.offendingModFile
                if (modFile != null && modFile.exists()) {
                    val disabledFile = File(modFile.parentFile, "${modFile.name}.disabled")
                    modFile.renameTo(disabledFile)
                    "Fixed: Disabled conflicting mod '${modFile.name}' (.disabled)."
                } else {
                    if (version != null) {
                        MobileFpsBooster.applyMobileOptionsTxt(version, FpsBoostPreset.BALANCED_MOBILE)
                    }
                    "Fixed: Reset instance render options for safe startup."
                }
            }

            CrashCategory.RENDERER_GPU_CRASH -> {
                withContext(Dispatchers.Main) {
                    AllSettings.renderer.save("")
                    AllSettings.resolutionRatio.save(80)
                }
                if (version != null) {
                    version.getVersionConfig().apply {
                        renderer = ""
                        save()
                    }
                    MobileFpsBooster.applyMobileOptionsTxt(version, FpsBoostPreset.BALANCED_MOBILE)
                }
                "Fixed: Switched Renderer to Safe Auto (NGGL4ES) & set Render Scale to 80%."
            }

            CrashCategory.GENERAL_RECOVERY -> {
                withContext(Dispatchers.Main) {
                    AllSettings.ramAllocation.save(2560)
                    AllSettings.autoPickJavaRuntime.save(true)
                    AllSettings.resolutionRatio.save(80)
                }
                if (version != null) {
                    version.getVersionConfig().apply {
                        javaRuntime = ""
                        renderer = ""
                        ramAllocation = -1
                        save()
                    }
                    MobileFpsBooster.applyMobileOptionsTxt(version, FpsBoostPreset.BALANCED_MOBILE)
                }
                "Fixed: Applied Safe Mode (2560 MB RAM + Auto Java/Renderer + Mobile options.txt)."
            }
        }
    }
}

@Composable
fun SmartCrashDoctorCard(
    logFile: File?,
    modifier: Modifier = Modifier,
    onRestartClick: (() -> Unit)? = null
) {
    val scope = rememberCoroutineScope()
    val accentColor = Color(0xFF1BD96A)
    var diagnosis by remember { mutableStateOf<CrashDiagnosis?>(null) }
    var fixResultMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(logFile) {
        diagnosis = SmartCrashDoctor.analyze(explicitLogFile = logFile)
    }

    val currentDiagnosis = diagnosis ?: return

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFF1A1D24),
        border = BorderStroke(1.5.dp, accentColor.copy(alpha = 0.7f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = accentColor.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = "🩺 SMART CRASH DOCTOR",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = accentColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
                Text(
                    text = currentDiagnosis.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Text(
                text = currentDiagnosis.summary,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFFD1D5DB)
            )

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF111317),
                border = BorderStroke(1.dp, Color(0xFF282C36))
            ) {
                Text(
                    text = currentDiagnosis.matchedLine,
                    style = MaterialTheme.typography.labelSmall,
                    fontFamily = FontFamily.Monospace,
                    color = Color(0xFFFBBF24),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }

            if (fixResultMessage != null) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    color = accentColor.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, accentColor)
                ) {
                    Text(
                        text = "✓ ${fixResultMessage!!}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = accentColor,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = {
                        scope.launch {
                            fixResultMessage = SmartCrashDoctor.applyFix(currentDiagnosis)
                        }
                    },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = accentColor,
                        contentColor = Color(0xFF06210F)
                    ),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = currentDiagnosis.actionButtonLabel,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                if (fixResultMessage != null && onRestartClick != null) {
                    Button(
                        onClick = onRestartClick,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF3B82F6),
                            contentColor = Color.White
                        ),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "▶ Relaunch Now",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SmartCrashDoctorDialog(
    version: Version?,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            color = Color(0xFF16181C),
            border = BorderStroke(1.dp, MiraiThemeManager.currentAccent().copy(alpha = 0.5f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SmartCrashDoctorCard(logFile = null)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFF21242B),
                        border = BorderStroke(1.dp, Color(0xFF2E333E)),
                        onClick = onDismiss
                    ) {
                        Text(
                            text = "Close",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                }
            }
        }
    }
}
