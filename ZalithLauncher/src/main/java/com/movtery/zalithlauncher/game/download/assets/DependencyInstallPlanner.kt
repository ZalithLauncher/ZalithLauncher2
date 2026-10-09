/*
 * Zalith Launcher 2
 * Copyright (C) 2025 MovTery <movtery228@qq.com> and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.movtery.zalithlauncher.game.download.assets

import com.movtery.zalithlauncher.game.addons.modloader.ModLoader
import com.movtery.zalithlauncher.game.download.assets.platform.Platform
import com.movtery.zalithlauncher.game.download.assets.platform.PlatformProject
import com.movtery.zalithlauncher.game.download.assets.platform.PlatformVersion
import com.movtery.zalithlauncher.game.version.installed.Version
import com.movtery.zalithlauncher.game.version.installed.VersionFolders
import com.movtery.zalithlauncher.game.version.mod.AllModReader
import com.movtery.zalithlauncher.game.version.mod.LocalMod
import com.movtery.zalithlauncher.game.version.mod.ModFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext

/**
 * One dependency and only the selected instances that still need it.
 */
data class DependencyInstallWork(
    val dependency: PlatformVersion.PlatformDependency,
    val project: PlatformProject,
    val targetVersions: List<Version>
)

data class DependencyInstallPlan(
    val work: List<DependencyInstallWork>,
    val skippedDependencyNames: List<String>,
    val planningErrors: List<Pair<String, String>> = emptyList()
)

data class DependencyRequirementPlan(
    val dependencies: List<Pair<PlatformVersion.PlatformDependency, PlatformProject>>,
    val planningErrors: List<Pair<String, String>>
)

private data class InstalledMod(
    val local: LocalMod,
    val remoteFile: ModFile?,
    val projectId: String?,
    val projectPlatform: Platform?
)

fun planDependencyRequirements(
    dependencies: List<Pair<PlatformVersion.PlatformDependency, PlatformProject>>
): DependencyRequirementPlan {
    val dependencyGroups = dependencies
        .filter { !it.first.projectId.isNullOrBlank() }
        .groupBy { (dependency, _) ->
            "${dependency.platform.name}:${dependency.projectId?.lowercase().orEmpty()}"
        }
    val planningErrors = mutableListOf<Pair<String, String>>()
    val uniqueDependencies = dependencyGroups.values.flatMap { entries ->
        val versionIds = entries.mapNotNull { it.first.versionId }.distinct()
        if (versionIds.size > 1) {
            val project = entries.first().second
            planningErrors += project.platformTitle() to
                "Conflicting exact dependency versions were requested: ${versionIds.joinToString()}"
            emptyList()
        } else {
            listOf(entries.firstOrNull { it.first.versionId != null } ?: entries.first())
        }
    }
    return DependencyRequirementPlan(uniqueDependencies, planningErrors)
}

/**
 * Plans dependency downloads against the selected game instances only.
 *
 * Remote metadata is loaded through the existing [RemoteMod] bridge so project
 * IDs, exact file IDs, game versions, and loader metadata are used before a
 * dependency enters the download queue. Local metadata remains available as a
 * fallback identity source, but cannot by itself prove version compatibility.
 */
suspend fun planDependencyDownloads(
    dependencies: List<Pair<PlatformVersion.PlatformDependency, PlatformProject>>,
    gameVersions: List<Version>
): DependencyInstallPlan = withContext(Dispatchers.IO) {
    val selectedVersions = gameVersions.distinctBy { it.getVersionName() }
    val installedByVersion = coroutineScope {
        selectedVersions.map { version ->
            async {
                val modsDir = VersionFolders.MOD.getDir(version.getGameDir())
                val remoteMods = AllModReader(modsDir).readAllForRemote()
                val semaphore = Semaphore(5)
                val installed = remoteMods.map { remote ->
                    async {
                        semaphore.withPermit {
                            runCatching { remote.load(loadFromCache = true) }
                            InstalledMod(
                                local = remote.localMod,
                                remoteFile = remote.remoteFile,
                                projectId = remote.projectInfo?.id,
                                projectPlatform = remote.projectInfo?.platform
                            )
                        }
                    }
                }.awaitAll()
                version to installed
            }
        }.awaitAll().toMap()
    }

    val requirements = planDependencyRequirements(dependencies)
    val uniqueDependencies = requirements.dependencies

    val skipped = mutableListOf<String>()
    val work = uniqueDependencies.mapNotNull { (dependency, project) ->
        val missingVersions = selectedVersions.filter { version ->
            installedByVersion[version].orEmpty().none { installed ->
                installed.isCompatibleDependency(dependency, project, version)
            }
        }

        if (missingVersions.isEmpty()) {
            skipped += project.platformTitle()
            null
        } else {
            DependencyInstallWork(dependency, project, missingVersions)
        }
    }

    DependencyInstallPlan(
        work = work,
        skippedDependencyNames = skipped,
        planningErrors = requirements.planningErrors
    )
}

private fun InstalledMod.isCompatibleDependency(
    dependency: PlatformVersion.PlatformDependency,
    project: PlatformProject,
    targetVersion: Version
): Boolean {
    if (local.notMod || remoteFile == null) return false

    val remote = remoteFile
    val projectMatches = remote.platform == dependency.platform &&
        remote.projectId.equals(dependency.projectId, ignoreCase = true)
    val exactVersionMatches = dependency.versionId != null &&
        remote.platform == dependency.platform &&
        remote.id.equals(dependency.versionId, ignoreCase = true)
    val cachedProjectMatches = projectPlatform == dependency.platform &&
        projectId.equals(dependency.projectId, ignoreCase = true)

    val metadataMatches = projectMatches || exactVersionMatches ||
        cachedProjectMatches ||
        project.idMatches(remote)

    if (!metadataMatches) return false
    if (dependency.versionId != null && !exactVersionMatches) return false

    val targetInfo = targetVersion.getVersionInfo() ?: return false
    val targetMinecraft = targetInfo.minecraftVersion
    if (remote.gameVersions.isNotEmpty() && targetMinecraft !in remote.gameVersions) {
        return false
    }

    val targetLoader = targetInfo.primaryLoader?.loader
    if (targetLoader != null && !localLoaderMatches(local.loader, targetLoader)) {
        return false
    }

    if (remote.loaders.isNotEmpty() && targetLoader != null) {
        val targetName = normalize(remoteLoaderName(targetLoader))
        val remoteLoaderMatches = remote.loaders.any { label ->
            val name = normalize(label.getDisplayName())
            name == targetName || name.contains(targetName) || targetName.contains(name)
        }
        if (!remoteLoaderMatches) return false
    }

    return true
}

private fun PlatformProject.idMatches(remote: ModFile): Boolean {
    return platform() == remote.platform &&
        platformId().equals(remote.projectId, ignoreCase = true)
}

private fun localLoaderMatches(local: ModLoader, target: ModLoader): Boolean {
    if (local == ModLoader.UNKNOWN) return true
    return when (target) {
        ModLoader.FABRIC -> local == ModLoader.FABRIC || local == ModLoader.FABRIC_API
        ModLoader.LEGACY_FABRIC -> local == ModLoader.LEGACY_FABRIC || local == ModLoader.LEGACY_FABRIC_API
        ModLoader.BABRIC -> local == ModLoader.BABRIC || local == ModLoader.BABRIC_API
        ModLoader.QUILT -> local == ModLoader.QUILT || local == ModLoader.QUILT_API
        else -> local == target
    }
}

private fun remoteLoaderName(loader: ModLoader): String {
    return when (loader) {
        ModLoader.FABRIC_API -> ModLoader.FABRIC.displayName
        ModLoader.LEGACY_FABRIC_API -> ModLoader.LEGACY_FABRIC.displayName
        ModLoader.BABRIC_API -> ModLoader.BABRIC.displayName
        ModLoader.QUILT_API -> ModLoader.QUILT.displayName
        else -> loader.displayName
    }
}

private fun normalize(value: String): String {
    return value.filter(Char::isLetterOrDigit).lowercase()
}