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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.movtery.zalithlauncher.game.addons.modloader.ModLoader
import com.movtery.zalithlauncher.game.version.installed.Version
import com.movtery.zalithlauncher.ui.theme.MiraiThemeManager
import com.movtery.zalithlauncher.utils.string.compareVersion
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayInputStream
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.zip.ZipFile
import java.util.zip.ZipInputStream

enum class ModIssueSeverity(val badgeText: String, val badgeColor: Color) {
    CRITICAL("Missing / Conflict", Color(0xFFEF4444)),
    WARNING("Recommended Fix", Color(0xFFF59E0B))
}

enum class ModFixActionType {
    DOWNLOAD_MODRINTH_DEPENDENCY,
    DISABLE_FILE
}

data class ModResolutionIssue(
    val id: String,
    val title: String,
    val description: String,
    val actionLabel: String,
    val severity: ModIssueSeverity,
    val actionType: ModFixActionType,
    val targetModIdOrSlug: String? = null,
    val targetFileToDisable: File? = null
)

data class ModScanReport(
    val scannedJarCount: Int,
    val issues: List<ModResolutionIssue>
)

private data class ParsedModJar(
    val file: File,
    val primaryId: String,
    val displayName: String,
    val version: String,
    val loaderKind: String, // "fabric", "quilt", "forge", "neoforge", "hybrid"
    val providedIds: Set<String>,
    val requiredDeps: Set<String>,
    val breaksIds: Set<String>
)

object ModDependencyResolver {

    private val builtInSatisfiedIds = setOf(
        "minecraft",
        "java",
        "fabricloader",
        "fabric-loader",
        "quilt_loader",
        "forge",
        "neoforge",
        "fml",
        "mixinextras"
    )

    /**
     * Maps common required dependency mod IDs to their exact Modrinth project slugs.
     */
    private val knownModIdToModrinthSlug = mapOf(
        "fabric" to "fabric-api",
        "fabric-api" to "fabric-api",
        "fabric-api-base" to "fabric-api",
        "fabric-rendering-api-v1" to "fabric-api",
        "fabric-lifecycle-events-v1" to "fabric-api",
        "fabric-resource-loader-v0" to "fabric-api",
        "fabric-networking-api-v1" to "fabric-api",
        "fabric-command-api-v1" to "fabric-api",
        "fabric-command-api-v2" to "fabric-api",
        "fabric-key-binding-api-v1" to "fabric-api",
        "fabric-screen-api-v1" to "fabric-api",
        "cloth-config" to "cloth-config",
        "cloth-config2" to "cloth-config",
        "cloth_config" to "cloth-config",
        "indium" to "indium",
        "architectury" to "architectury-api",
        "modmenu" to "modmenu",
        "collective" to "collective",
        "geckolib" to "geckolib",
        "geckolib3" to "geckolib",
        "fabric-language-kotlin" to "fabric-language-kotlin",
        "kotlinforforge" to "kotlin-for-forge",
        "yet_another_config_lib_v3" to "yacl",
        "yacl" to "yacl",
        "owo" to "owo-lib",
        "trinkets" to "trinkets",
        "curios" to "curios",
        "bookshelf" to "bookshelf",
        "balm" to "balm",
        "balm-fabric" to "balm",
        "puzzleslib" to "puzzles-lib",
        "resourcefullib" to "resourceful-lib",
        "cristellib" to "cristel-lib",
        "moonlight" to "moonlight",
        "placebo" to "placebo",
        "citadel" to "citadel",
        "ferritecore" to "ferrite-core",
        "entityculling" to "entityculling",
        "immediatelyfast" to "immediatelyfast",
        "modernfix" to "modernfix",
        "reeses-sodium-options" to "reeses-sodium-options",
        "sodium-extra" to "sodium-extra",
        "iris" to "iris",
        "sodium" to "sodium",
        "lithium" to "lithium",
        "cardinal-components-base" to "cardinal-components-api",
        "cardinal-components" to "cardinal-components-api",
        "forgeconfigapiport" to "forge-config-api-port",
        "supermartijn642corelib" to "supermartijn642s-core-lib",
        "supermartijn642configlib" to "supermartijn642s-config-lib",
        "patchouli" to "patchouli",
        "jei" to "jei",
        "rei" to "rei",
        "roughlyenoughitems" to "rei",
        "emi" to "emi",
        "jade" to "jade",
        "athena" to "athena",
        "konkrete" to "konkrete",
        "melody" to "melody",
        "fancymenu" to "fancymenu",
        "amendments" to "amendments",
        "supplementaries" to "supplementaries",
        "flywheel" to "flywheel"
    )

    private val knownMutuallyExclusivePairs = listOf(
        Triple("optifabric", "sodium", "OptiFabric conflicts with Sodium (use Sodium + Iris instead)"),
        Triple("sodium", "rubidium", "Sodium and Rubidium are duplicate rendering engines"),
        Triple("sodium", "embeddium", "Sodium and Embeddium cannot be loaded together"),
        Triple("rubidium", "embeddium", "Rubidium and Embeddium are duplicate rendering engines"),
        Triple("iris", "oculus", "Iris and Oculus are duplicate shader loaders"),
        Triple("lithium", "canary", "Lithium and Canary are duplicate physics optimizers"),
        Triple("lithium", "radium", "Lithium and Radium are duplicate physics optimizers")
    )

    /**
     * Scans all enabled `.jar` files in the instance's `mods/` directory and identifies
     * missing dependencies, duplicate mods, loader mismatches, and mod conflicts.
     */
    suspend fun scanInstanceMods(
        version: Version,
        modsDirOverride: File? = null
    ): ModScanReport = withContext(Dispatchers.IO) {
        val modsDir = modsDirOverride ?: File(version.getGameDir(), "mods")
        if (!modsDir.exists() || !modsDir.isDirectory) {
            return@withContext ModScanReport(scannedJarCount = 0, issues = emptyList())
        }

        val jarFiles = modsDir.listFiles { f ->
            f.isFile && f.name.endsWith(".jar", ignoreCase = true)
        }?.sortedBy { it.name.lowercase() } ?: emptyList()

        if (jarFiles.isEmpty()) {
            return@withContext ModScanReport(scannedJarCount = 0, issues = emptyList())
        }

        val parsedMods = jarFiles.mapNotNull { file ->
            runCatching { parseModJar(file) }.getOrNull()
        }

        val issues = mutableListOf<ModResolutionIssue>()
        val instanceLoader = version.getVersionInfo()?.loaderInfo?.loader
        val instanceMcVer = version.getVersionInfo()?.minecraftVersion ?: "1.16.5"

        // 1. Collect all provided mod IDs across all installed jars (including nested META-INF/jars)
        val allProvidedIds = mutableSetOf<String>()
        allProvidedIds.addAll(builtInSatisfiedIds)
        parsedMods.forEach { mod ->
            allProvidedIds.add(mod.primaryId.lowercase())
            mod.providedIds.forEach { allProvidedIds.add(it.lowercase()) }
        }
        // If fabric-api is installed, satisfy all fabric-* sub-APIs
        val hasFabricApi = allProvidedIds.contains("fabric-api") || allProvidedIds.contains("fabric")

        // 2. Check Duplicate Primary Mod IDs
        val groupedById = parsedMods
            .filter { it.primaryId.isNotBlank() && it.primaryId != "unknown" }
            .groupBy { it.primaryId.lowercase() }

        groupedById.forEach { (modId, modsWithSameId) ->
            if (modsWithSameId.size > 1) {
                // Sort by lastModified descending so we keep the newest and disable older duplicates
                val sorted = modsWithSameId.sortedByDescending { it.file.lastModified() }
                val keepMod = sorted.first()
                sorted.drop(1).forEach { duplicateMod ->
                    issues.add(
                        ModResolutionIssue(
                            id = "dup_${modId}_${duplicateMod.file.name}",
                            title = "Duplicate Mod: ${duplicateMod.displayName}",
                            description = "Both '${keepMod.file.name}' and '${duplicateMod.file.name}' provide mod ID '$modId'. Duplicate mods cause startup crashes.",
                            actionLabel = "Disable Duplicate",
                            severity = ModIssueSeverity.CRITICAL,
                            actionType = ModFixActionType.DISABLE_FILE,
                            targetFileToDisable = duplicateMod.file
                        )
                    )
                }
            }
        }

        // 3. Check Wrong Mod Loader (e.g., Forge-only jar in Fabric instance or Fabric-only jar in Forge instance)
        if (instanceLoader != null) {
            parsedMods.forEach { mod ->
                val isLoaderMismatch = when (instanceLoader) {
                    ModLoader.FABRIC, ModLoader.BABRIC -> mod.loaderKind == "forge" || mod.loaderKind == "neoforge"
                    ModLoader.QUILT -> mod.loaderKind == "forge" || mod.loaderKind == "neoforge"
                    ModLoader.FORGE -> mod.loaderKind == "fabric" || mod.loaderKind == "quilt" || mod.loaderKind == "neoforge"
                    ModLoader.NEOFORGE -> mod.loaderKind == "fabric" || mod.loaderKind == "quilt"
                    else -> false
                }
                if (isLoaderMismatch) {
                    issues.add(
                        ModResolutionIssue(
                            id = "loader_${mod.file.name}",
                            title = "Wrong Loader: ${mod.displayName} (${mod.loaderKind.uppercase()})",
                            description = "'${mod.file.name}' is built for ${mod.loaderKind.uppercase()}, but this instance uses ${instanceLoader.displayName}.",
                            actionLabel = "Disable Incompatible Mod",
                            severity = ModIssueSeverity.CRITICAL,
                            actionType = ModFixActionType.DISABLE_FILE,
                            targetFileToDisable = mod.file
                        )
                    )
                }
            }
        }

        // 4. Check Missing Required Dependencies
        val missingDepToRequiringMods = linkedMapOf<String, MutableList<String>>()
        parsedMods.forEach { mod ->
            mod.requiredDeps.forEach { rawDep ->
                val depId = rawDep.lowercase().trim()
                val isSatisfied = allProvidedIds.contains(depId) ||
                    (hasFabricApi && depId.startsWith("fabric-"))
                if (!isSatisfied && depId.isNotBlank()) {
                    val canonicalKey = if (depId == "fabric" || depId.startsWith("fabric-api") ||
                        (depId.startsWith("fabric-") && depId != "fabric-language-kotlin")
                    ) {
                        "fabric-api"
                    } else if (depId == "cloth-config2" || depId == "cloth_config") {
                        "cloth-config"
                    } else {
                        depId
                    }
                    missingDepToRequiringMods
                        .getOrPut(canonicalKey) { mutableListOf() }
                        .add(mod.displayName)
                }
            }
        }

        missingDepToRequiringMods.forEach { (missingId, requiringMods) ->
            val slug = knownModIdToModrinthSlug[missingId] ?: missingId
            val displayDep = formatModIdDisplayName(missingId)
            val neededBy = requiringMods.distinct().take(3).joinToString(", ")
            issues.add(
                ModResolutionIssue(
                    id = "missing_$missingId",
                    title = "Missing Required Mod: $displayDep",
                    description = "Required by: $neededBy. Without '$displayDep', Minecraft will fail to launch.",
                    actionLabel = "Install $displayDep",
                    severity = ModIssueSeverity.CRITICAL,
                    actionType = ModFixActionType.DOWNLOAD_MODRINTH_DEPENDENCY,
                    targetModIdOrSlug = slug
                )
            )
        }

        // 5. Check Known Mutually Exclusive Conflicts & `breaks` declarations
        val modById = parsedMods.associateBy { it.primaryId.lowercase() }
        knownMutuallyExclusivePairs.forEach { (idA, idB, reason) ->
            val modA = modById[idA]
            val modB = modById[idB]
            if (modA != null && modB != null) {
                val toDisable = if (idA == "optifabric") modA else modB
                issues.add(
                    ModResolutionIssue(
                        id = "conflict_${idA}_$idB",
                        title = "Mod Conflict: ${modA.displayName} + ${modB.displayName}",
                        description = reason,
                        actionLabel = "Disable ${toDisable.displayName}",
                        severity = ModIssueSeverity.CRITICAL,
                        actionType = ModFixActionType.DISABLE_FILE,
                        targetFileToDisable = toDisable.file
                    )
                )
            }
        }

        parsedMods.forEach { mod ->
            mod.breaksIds.forEach { brokenId ->
                val conflicting = modById[brokenId.lowercase()]
                if (conflicting != null && conflicting.file != mod.file) {
                    val issueId = "breaks_${mod.primaryId}_${conflicting.primaryId}"
                    if (issues.none { it.id == issueId }) {
                        issues.add(
                            ModResolutionIssue(
                                id = issueId,
                                title = "Incompatible: ${mod.displayName} ↔ ${conflicting.displayName}",
                                description = "'${mod.displayName}' declares that it breaks '${conflicting.displayName}'.",
                                actionLabel = "Disable ${conflicting.displayName}",
                                severity = ModIssueSeverity.WARNING,
                                actionType = ModFixActionType.DISABLE_FILE,
                                targetFileToDisable = conflicting.file
                            )
                        )
                    }
                }
            }
        }

        // 6. Smart Sodium + Fabric Rendering API (Indium) Check for <= 1.20.4
        val hasSodium = allProvidedIds.contains("sodium")
        val hasIndium = allProvidedIds.contains("indium")
        val needsIndium = hasSodium &&
            !hasIndium &&
            (instanceLoader == ModLoader.FABRIC || instanceLoader == ModLoader.QUILT) &&
            runCatching { instanceMcVer.compareVersion("1.20.4") <= 0 && instanceMcVer.compareVersion("1.16.5") >= 0 }.getOrDefault(true) &&
            parsedMods.any {
                it.primaryId.lowercase() != "sodium" &&
                    it.primaryId.lowercase() != "lithium" &&
                    it.primaryId.lowercase() != "ferritecore"
            }

        if (needsIndium && issues.none { it.targetModIdOrSlug == "indium" }) {
            issues.add(
                ModResolutionIssue(
                    id = "recommend_indium",
                    title = "Recommended for Sodium: Indium",
                    description = "Sodium on Minecraft $instanceMcVer requires Indium to support Fabric Rendering API mods without invisible blocks or crashes.",
                    actionLabel = "Install Indium",
                    severity = ModIssueSeverity.WARNING,
                    actionType = ModFixActionType.DOWNLOAD_MODRINTH_DEPENDENCY,
                    targetModIdOrSlug = "indium"
                )
            )
        }

        ModScanReport(
            scannedJarCount = jarFiles.size,
            issues = issues
        )
    }

    /**
     * Resolves a single [ModResolutionIssue] by downloading the missing dependency from Modrinth
     * or disabling the conflicting/duplicate `.jar`.
     */
    suspend fun resolveIssue(
        version: Version,
        issue: ModResolutionIssue,
        modsDirOverride: File? = null
    ): Boolean = withContext(Dispatchers.IO) {
        val modsDir = (modsDirOverride ?: File(version.getGameDir(), "mods")).apply { mkdirs() }
        when (issue.actionType) {
            ModFixActionType.DISABLE_FILE -> {
                val target = issue.targetFileToDisable ?: return@withContext false
                if (!target.exists()) return@withContext true
                val disabledFile = File(target.parentFile, "${target.name}.disabled")
                target.renameTo(disabledFile)
            }
            ModFixActionType.DOWNLOAD_MODRINTH_DEPENDENCY -> {
                val slugOrId = issue.targetModIdOrSlug ?: return@withContext false
                val mcVersion = version.getVersionInfo()?.minecraftVersion ?: "1.16.5"
                val loaderName = when (version.getVersionInfo()?.loaderInfo?.loader) {
                    ModLoader.FORGE -> "forge"
                    ModLoader.NEOFORGE -> "neoforge"
                    ModLoader.QUILT -> "quilt"
                    else -> "fabric"
                }
                downloadDependencyFromModrinth(
                    slugOrModId = slugOrId,
                    mcVersion = mcVersion,
                    loader = loaderName,
                    modsDir = modsDir
                )
            }
        }
    }

    private fun downloadDependencyFromModrinth(
        slugOrModId: String,
        mcVersion: String,
        loader: String,
        modsDir: File
    ): Boolean {
        // First try direct project slug, then fall back to Modrinth search if 404
        if (tryDownloadProjectVersion(slugOrModId, mcVersion, loader, modsDir)) {
            return true
        }
        val searchedSlug = searchModrinthProjectSlug(slugOrModId, mcVersion, loader) ?: return false
        return tryDownloadProjectVersion(searchedSlug, mcVersion, loader, modsDir)
    }

    private fun tryDownloadProjectVersion(
        projectSlug: String,
        mcVersion: String,
        loader: String,
        modsDir: File
    ): Boolean {
        return runCatching {
            val loadersParam = URLEncoder.encode("[\"$loader\"]", "UTF-8")
            val gameVersionsParam = URLEncoder.encode("[\"$mcVersion\"]", "UTF-8")
            val apiUrl = "https://api.modrinth.com/v2/project/$projectSlug/version?loaders=$loadersParam&game_versions=$gameVersionsParam"

            val conn = (URL(apiUrl).openConnection() as HttpURLConnection).apply {
                connectTimeout = 8000
                readTimeout = 8000
                setRequestProperty("User-Agent", "ZalithLauncher/2.6.1 (Android)")
            }
            if (conn.responseCode != 200) return false

            val body = conn.inputStream.bufferedReader().use { it.readText() }
            val versionsArray = JSONArray(body)
            if (versionsArray.length() == 0) return false

            val firstVersion = versionsArray.getJSONObject(0)
            val filesArray = firstVersion.getJSONArray("files")
            if (filesArray.length() == 0) return false

            val fileObj = filesArray.getJSONObject(0)
            val downloadUrl = fileObj.getString("url")
            val fileName = fileObj.getString("filename")
            val targetFile = File(modsDir, fileName)
            if (targetFile.exists() && targetFile.length() > 1024) return true

            val dlConn = (URL(downloadUrl).openConnection() as HttpURLConnection).apply {
                connectTimeout = 10000
                readTimeout = 15000
                setRequestProperty("User-Agent", "ZalithLauncher/2.6.1 (Android)")
            }
            if (dlConn.responseCode == 200) {
                val tempFile = File(modsDir, "$fileName.tmp")
                dlConn.inputStream.use { input ->
                    tempFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                tempFile.renameTo(targetFile)
                true
            } else {
                false
            }
        }.getOrDefault(false)
    }

    private fun searchModrinthProjectSlug(
        query: String,
        mcVersion: String,
        loader: String
    ): String? {
        return runCatching {
            val q = URLEncoder.encode(query, "UTF-8")
            val facets = URLEncoder.encode(
                "[[\"categories:$loader\"],[\"versions:$mcVersion\"],[\"project_type:mod\"]]",
                "UTF-8"
            )
            val url = "https://api.modrinth.com/v2/search?query=$q&facets=$facets&limit=3"
            val conn = (URL(url).openConnection() as HttpURLConnection).apply {
                connectTimeout = 8000
                readTimeout = 8000
                setRequestProperty("User-Agent", "ZalithLauncher/2.6.1 (Android)")
            }
            if (conn.responseCode != 200) return null
            val json = JSONObject(conn.inputStream.bufferedReader().use { it.readText() })
            val hits = json.optJSONArray("hits") ?: return null
            if (hits.length() == 0) return null
            hits.getJSONObject(0).optString("slug").takeIf { it.isNotBlank() }
        }.getOrNull()
    }

    private fun parseModJar(file: File): ParsedModJar {
        ZipFile(file).use { zip ->
            val fabricEntry = zip.getEntry("fabric.mod.json")
            val quiltEntry = zip.getEntry("quilt.mod.json")
            val neoforgeEntry = zip.getEntry("META-INF/neoforge.mods.toml")
            val forgeTomlEntry = zip.getEntry("META-INF/mods.toml")
            val mcmodEntry = zip.getEntry("mcmod.info")

            val hasFabric = fabricEntry != null
            val hasQuilt = quiltEntry != null
            val hasForge = forgeTomlEntry != null || mcmodEntry != null
            val hasNeoForge = neoforgeEntry != null

            val loaderKind = when {
                (hasFabric || hasQuilt) && (hasForge || hasNeoForge) -> "hybrid"
                hasQuilt -> "quilt"
                hasFabric -> "fabric"
                hasNeoForge -> "neoforge"
                hasForge -> "forge"
                else -> "unknown"
            }

            if (fabricEntry != null) {
                val text = zip.getInputStream(fabricEntry).bufferedReader().use { it.readText() }
                val json = JSONObject(text)
                val primaryId = json.optString("id", file.nameWithoutExtension)
                val displayName = json.optString("name", primaryId).ifBlank { primaryId }
                val version = json.optString("version", "")

                val providedIds = mutableSetOf<String>()
                providedIds.add(primaryId.lowercase())
                json.optJSONArray("provides")?.let { arr ->
                    for (i in 0 until arr.length()) {
                        val item = arr.opt(i)
                        when (item) {
                            is String -> providedIds.add(item.lowercase())
                            is JSONObject -> item.optString("id").takeIf { it.isNotBlank() }?.let {
                                providedIds.add(it.lowercase())
                            }
                        }
                    }
                }

                // Scan nested Jar-in-Jar entries (META-INF/jars/*.jar) so bundled libraries are counted as provided
                val entries = zip.entries()
                while (entries.hasMoreElements()) {
                    val entry = entries.nextElement()
                    if (!entry.isDirectory && entry.name.startsWith("META-INF/jars/") && entry.name.endsWith(".jar")) {
                        runCatching {
                            val nestedBytes = zip.getInputStream(entry).readBytes()
                            ZipInputStream(ByteArrayInputStream(nestedBytes)).use { nestedZip ->
                                var subEntry = nestedZip.nextEntry
                                while (subEntry != null) {
                                    if (subEntry.name == "fabric.mod.json") {
                                        val subText = nestedZip.bufferedReader().readText()
                                        val subJson = JSONObject(subText)
                                        val subId = subJson.optString("id")
                                        if (subId.isNotBlank()) providedIds.add(subId.lowercase())
                                        subJson.optJSONArray("provides")?.let { pArr ->
                                            for (k in 0 until pArr.length()) {
                                                val p = pArr.optString(k)
                                                if (p.isNotBlank()) providedIds.add(p.lowercase())
                                            }
                                        }
                                        break
                                    }
                                    subEntry = nestedZip.nextEntry
                                }
                            }
                        }
                    }
                }

                val requiredDeps = mutableSetOf<String>()
                json.optJSONObject("depends")?.keys()?.forEach { key ->
                    val lower = key.lowercase()
                    if (lower !in builtInSatisfiedIds) {
                        requiredDeps.add(lower)
                    }
                }

                val breaksIds = mutableSetOf<String>()
                json.optJSONObject("breaks")?.keys()?.forEach { key ->
                    breaksIds.add(key.lowercase())
                }
                json.optJSONObject("conflicts")?.keys()?.forEach { key ->
                    breaksIds.add(key.lowercase())
                }

                return ParsedModJar(
                    file = file,
                    primaryId = primaryId,
                    displayName = displayName,
                    version = version,
                    loaderKind = loaderKind,
                    providedIds = providedIds,
                    requiredDeps = requiredDeps,
                    breaksIds = breaksIds
                )
            }

            val tomlEntry = neoforgeEntry ?: forgeTomlEntry
            if (tomlEntry != null) {
                val text = zip.getInputStream(tomlEntry).bufferedReader().use { it.readText() }
                val modIdMatch = Regex("""modId\s*=\s*"([^"]+)"""").find(text)
                val displayNameMatch = Regex("""displayName\s*=\s*"([^"]+)"""").find(text)
                val primaryId = modIdMatch?.groupValues?.getOrNull(1) ?: file.nameWithoutExtension
                val displayName = displayNameMatch?.groupValues?.getOrNull(1) ?: primaryId

                val requiredDeps = mutableSetOf<String>()
                val depBlocks = text.split(Regex("""\[\[dependencies\.[^\]]+\]\]""")).drop(1)
                depBlocks.forEach { block ->
                    val depId = Regex("""modId\s*=\s*"([^"]+)"""").find(block)?.groupValues?.getOrNull(1)?.lowercase()
                    val mandatory = !block.contains(Regex("""mandatory\s*=\s*false""")) &&
                        !block.contains(Regex("""type\s*=\s*"optional"""")) &&
                        !block.contains(Regex("""type\s*=\s*"incompatible""""))
                    if (depId != null && mandatory && depId !in builtInSatisfiedIds) {
                        requiredDeps.add(depId)
                    }
                }

                return ParsedModJar(
                    file = file,
                    primaryId = primaryId,
                    displayName = displayName,
                    version = "",
                    loaderKind = loaderKind,
                    providedIds = setOf(primaryId.lowercase()),
                    requiredDeps = requiredDeps,
                    breaksIds = emptySet()
                )
            }

            return ParsedModJar(
                file = file,
                primaryId = file.nameWithoutExtension,
                displayName = file.nameWithoutExtension,
                version = "",
                loaderKind = loaderKind,
                providedIds = setOf(file.nameWithoutExtension.lowercase()),
                requiredDeps = emptySet(),
                breaksIds = emptySet()
            )
        }
    }

    private fun formatModIdDisplayName(modId: String): String {
        return when (modId.lowercase()) {
            "fabric-api", "fabric" -> "Fabric API"
            "cloth-config", "cloth-config2" -> "Cloth Config API"
            "indium" -> "Indium"
            "architectury" -> "Architectury API"
            "modmenu" -> "Mod Menu"
            "fabric-language-kotlin" -> "Fabric Language Kotlin"
            "kotlinforforge" -> "Kotlin for Forge"
            "yet_another_config_lib_v3", "yacl" -> "YetAnotherConfigLib (YACL)"
            "owo" -> "oωo (owo-lib)"
            "geckolib", "geckolib3" -> "GeckoLib"
            else -> modId.replace('-', ' ').replace('_', ' ').replaceFirstChar { it.uppercase() }
        }
    }
}

@Composable
fun ModDependencyResolverDialog(
    version: Version,
    modsDir: File? = null,
    onModsChanged: () -> Unit = {},
    onDismiss: () -> Unit
) {
    val activeAccent = MiraiThemeManager.currentAccent()
    val scope = rememberCoroutineScope()

    var isScanning by remember { mutableStateOf(true) }
    var isFixingAll by remember { mutableStateOf(false) }
    var fixingIssueId by remember { mutableStateOf<String?>(null) }
    var report by remember { mutableStateOf<ModScanReport?>(null) }
    var statusText by remember { mutableStateOf<String?>(null) }
    var scanTrigger by remember { mutableIntStateOf(0) }

    LaunchedEffect(version, scanTrigger) {
        isScanning = true
        report = ModDependencyResolver.scanInstanceMods(version, modsDir)
        isScanning = false
    }

    Dialog(onDismissRequest = { if (!isFixingAll) onDismiss() }) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = Color(0xFF16181D),
            border = BorderStroke(1.dp, activeAccent.copy(alpha = 0.45f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
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
                                painter = painterResource(R.drawable.ic_check),
                                contentDescription = null,
                                tint = activeAccent,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Smart Mod Dependency & Conflict Resolver",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                            val scannedCount = report?.scannedJarCount ?: 0
                            Text(
                                text = "${version.getVersionName()} • Scanned $scannedCount active mod jars",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF9CA3AF)
                            )
                        }
                    }
                }

                when {
                    isScanning -> {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                strokeWidth = 2.5.dp,
                                color = activeAccent
                            )
                            Spacer(Modifier.width(12.dp))
                            Text(
                                text = "Scanning fabric.mod.json, mods.toml & Jar-in-Jar dependencies...",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFFD1D5DB)
                            )
                        }
                    }

                    report != null && report!!.issues.isEmpty() -> {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            color = activeAccent.copy(alpha = 0.12f),
                            border = BorderStroke(1.dp, activeAccent.copy(alpha = 0.45f))
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "✅ All ${report!!.scannedJarCount} Mods Verified Clean!",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = activeAccent
                                )
                                Text(
                                    text = "No missing dependencies, duplicate mod IDs, loader mismatches, or known conflicts were found in this instance.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFFE5E7EB)
                                )
                            }
                        }
                    }

                    report != null -> {
                        val currentIssues = report!!.issues
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 220.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(currentIssues, key = { it.id }) { issue ->
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color(0xFF20232A),
                                    border = BorderStroke(1.dp, issue.severity.badgeColor.copy(alpha = 0.45f))
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 12.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(
                                            modifier = Modifier.weight(1f),
                                            verticalArrangement = Arrangement.spacedBy(3.dp)
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Surface(
                                                    shape = RoundedCornerShape(6.dp),
                                                    color = issue.severity.badgeColor.copy(alpha = 0.2f)
                                                ) {
                                                    Text(
                                                        text = issue.severity.badgeText,
                                                        style = MaterialTheme.typography.labelSmall,
                                                        fontWeight = FontWeight.Bold,
                                                        color = issue.severity.badgeColor,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }
                                                Text(
                                                    text = issue.title,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                            Text(
                                                text = issue.description,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Color(0xFF9CA3AF),
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }

                                        Spacer(Modifier.width(8.dp))

                                        Button(
                                            onClick = {
                                                if (fixingIssueId == null && !isFixingAll) {
                                                    fixingIssueId = issue.id
                                                    scope.launch {
                                                        val ok = ModDependencyResolver.resolveIssue(version, issue, modsDir)
                                                        statusText = if (ok) "Fixed: ${issue.title}" else "Could not auto-fetch ${issue.title}"
                                                        fixingIssueId = null
                                                        onModsChanged()
                                                        scanTrigger++
                                                    }
                                                }
                                            },
                                            enabled = fixingIssueId == null && !isFixingAll,
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = activeAccent,
                                                contentColor = Color(0xFF06210F)
                                            ),
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                            modifier = Modifier.height(32.dp)
                                        ) {
                                            if (fixingIssueId == issue.id) {
                                                CircularProgressIndicator(
                                                    modifier = Modifier.size(14.dp),
                                                    strokeWidth = 2.dp,
                                                    color = Color(0xFF06210F)
                                                )
                                            } else {
                                                Text(
                                                    text = issue.actionLabel,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.ExtraBold
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                statusText?.let { msg ->
                    Text(
                        text = msg,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = activeAccent
                    )
                }

                // Bottom Action Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = onDismiss,
                        enabled = !isFixingAll,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF262932),
                            contentColor = Color(0xFFE5E7EB)
                        ),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text("Close", style = MaterialTheme.typography.labelMedium)
                    }

                    val issuesList = report?.issues ?: emptyList()
                    if (issuesList.isNotEmpty()) {
                        Spacer(Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (!isFixingAll) {
                                    isFixingAll = true
                                    scope.launch {
                                        var fixedCount = 0
                                        issuesList.forEach { issue ->
                                            statusText = "Resolving: ${issue.title}..."
                                            if (ModDependencyResolver.resolveIssue(version, issue, modsDir)) {
                                                fixedCount++
                                            }
                                        }
                                        statusText = "Resolved $fixedCount/${issuesList.size} mod issues!"
                                        isFixingAll = false
                                        onModsChanged()
                                        scanTrigger++
                                    }
                                }
                            },
                            enabled = !isFixingAll && fixingIssueId == null,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = activeAccent,
                                contentColor = Color(0xFF06210F)
                            ),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            if (isFixingAll) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(15.dp),
                                    strokeWidth = 2.dp,
                                    color = Color(0xFF06210F)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = "Fixing All...",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            } else {
                                Text(
                                    text = "1-Tap Fix All (${issuesList.size})",
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
}
