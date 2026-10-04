/*
 * Zalith Launcher 2
 * Copyright (C) 2025 MovTery <movtery228@qq.com> and contributors
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

package com.movtery.zalithlauncher.game.download.assets

import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.coroutine.Task
import com.movtery.zalithlauncher.coroutine.TaskSystem
import com.movtery.zalithlauncher.game.addons.modloader.ModLoader
import com.movtery.zalithlauncher.game.download.assets.platform.Platform
import com.movtery.zalithlauncher.game.download.assets.platform.PlatformClasses
import com.movtery.zalithlauncher.game.download.assets.platform.PlatformDependencyType
import com.movtery.zalithlauncher.game.download.assets.platform.PlatformDisplayLabel
import com.movtery.zalithlauncher.game.download.assets.platform.PlatformProject
import com.movtery.zalithlauncher.game.download.assets.platform.PlatformVersion
import com.movtery.zalithlauncher.game.download.assets.platform.curseforge.models.CurseForgeModLoader
import com.movtery.zalithlauncher.game.download.assets.platform.getProjectByVersion
import com.movtery.zalithlauncher.game.download.assets.platform.getVersionById
import com.movtery.zalithlauncher.game.download.assets.platform.getVersions
import com.movtery.zalithlauncher.game.download.assets.platform.modrinth.models.ModrinthModLoaderCategory
import com.movtery.zalithlauncher.game.version.installed.Version
import com.movtery.zalithlauncher.game.version.installed.VersionFolders
import com.movtery.zalithlauncher.game.version.mod.matchInstalledMods
import com.movtery.zalithlauncher.game.version.mod.scanModFingerprints
import com.movtery.zalithlauncher.ui.androidText
import com.movtery.zalithlauncher.ui.screens.content.download.assets.elements.initAll
import com.movtery.zalithlauncher.utils.logging.Logger
import com.movtery.zalithlauncher.viewmodel.ErrorViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.sync.withPermit
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

private const val TAG = "DownloadDependency"

/** 同一次依赖安装中允许处理的依赖项目数量上限，防止异常元数据导致依赖爆炸 */
private const val MAX_DEPENDENCY_PROJECTS = 64

/** 同时解析的依赖项目数量上限 */
private const val DEPENDENCY_PARALLELISM = 4

/**
 * 需要一并安装的依赖项
 * @param projectId 依赖项目在平台上的Id
 * @param versionId 依赖的精确版本Id，为null则代表只指定了依赖项目
 * @param classes 依赖资源的类别，决定其安装目录
 * @param projectTitle 依赖项目标题，用于提示信息
 */
class DependencyRequest(
    val platform: Platform,
    val projectId: String,
    val versionId: String?,
    val classes: PlatformClasses,
    val projectTitle: String
)

/**
 * 为给定的游戏版本安装选中的依赖项，并递归展开它们标注的必装依赖
 * @param requests 需要安装的依赖项
 * @param versions 依赖项的目标游戏版本
 */
fun downloadDependenciesForVersions(
    requests: List<DependencyRequest>,
    versions: List<Version>,
    submitError: (ErrorViewModel.ThrowableMessage) -> Unit
) {
    if (requests.isEmpty() || versions.isEmpty()) return

    // 整理唯一任务id
    val distinctRequests = requests.distinctBy { "${it.platform.name}/${it.projectId}/${it.versionId.orEmpty()}" }
    val taskId = distinctRequests
        .map { "${it.projectId}@${it.versionId.orEmpty()}" }
        .sorted()
        .joinToString(",")
        .hashCode()

    TaskSystem.submitTask(
        Task.runTask(
            id = "dependency/${distinctRequests.first().platform.name}/$taskId",
            task = { task ->
                task.updateMessage(
                    androidText(R.string.download_assets_loading_dep_project, distinctRequests.first().projectId)
                )

                val context = DependencyContext(
                    task = task,
                    semaphore = Semaphore(DEPENDENCY_PARALLELISM),
                    processed = ConcurrentHashMap<String, MutableSet<String>>(),
                    projectCache = ConcurrentHashMap<String, PlatformProject>(),
                    budget = AtomicInteger(MAX_DEPENDENCY_PROJECTS),
                    downloadGroups = ConcurrentHashMap<String, DownloadGroup>(),
                    failures = DependencyFailures()
                )

                coroutineScope {
                    distinctRequests.forEach { request ->
                        async { expand(request, versions, context) }
                    }
                }

                context.publishDownloads(submitError)
                context.failures.submit(submitError)
            }
        )
    )
}

/**
 * 统一展开依赖图时的共享上下文
 */
private class DependencyContext(
    val task: Task,
    val semaphore: Semaphore,
    /** 依赖项目已处理过的目标游戏版本，键为项目键 */
    val processed: ConcurrentHashMap<String, MutableSet<String>>,
    /** 依赖项目信息缓存，避免重复查询 */
    val projectCache: ConcurrentHashMap<String, PlatformProject>,
    /** 剩余可处理的依赖项目数量 */
    val budget: AtomicInteger,
    /** 已解析出的下载分组，键为平台版本键 */
    val downloadGroups: ConcurrentHashMap<String, DownloadGroup>,
    val failures: DependencyFailures
) {
    /** 目标游戏版本对应的本地已安装模组项目集合 */
    val installedMemo = ConcurrentHashMap<String, Set<String>>()
    val installedMutex = Mutex()

    /**
     * 认领依赖项目尚未处理过的目标游戏版本
     * @return 尚未处理过的目标游戏版本，为空则表示该项目无需继续处理
     */
    fun claimTargets(key: String, targets: List<Version>): List<Version> {
        val claimed = processed[key] ?: run {
            if (budget.getAndDecrement() <= 0) {
                Logger.warning(TAG, "The dependency limit of $MAX_DEPENDENCY_PROJECTS has been reached, stopping the expansion.")
                return emptyList()
            }
            processed.computeIfAbsent(key) { ConcurrentHashMap.newKeySet() }
        }
        return targets.filter { claimed.add(it.getVersionName()) }
    }
}

/**
 * 一并下载的依赖版本及其目标游戏版本
 * @param folder 安装到游戏目录下的相对路径
 */
private class DownloadGroup(
    val version: PlatformVersion,
    val folder: String
) {
    private val targets = mutableListOf<Version>()

    fun addTargets(versions: List<Version>) = synchronized(targets) {
        versions.forEach { if (it !in targets) targets.add(it) }
    }

    fun targetList(): List<Version> = synchronized(targets) { targets.toList() }
}

/**
 * 依赖失败信息，键为项目标题，值为未找到适配版本的目标游戏版本名
 */
private class DependencyFailures {
    private val byProject = ConcurrentHashMap<String, MutableList<String>>()

    fun record(title: String, versionName: String) {
        val names = byProject.computeIfAbsent(title) { mutableListOf() }
        synchronized(names) {
            if (!names.contains(versionName)) names.add(versionName)
        }
    }

    fun submit(submitError: (ErrorViewModel.ThrowableMessage) -> Unit) {
        val snapshot = byProject.mapValues { it.value.toList() }.toSortedMap()
        if (snapshot.isEmpty()) return

        snapshot.forEach { (title, versionNames) ->
            Logger.warning(TAG, "No compatible version found for the dependency: $title, ${versionNames.joinToString()}")
        }

        val texts = snapshot.keys.flatMapIndexed { index, title ->
            buildList {
                if (index > 0) add(androidText("\n"))
                add(androidText(R.string.download_assets_dependency_no_compatible_version, title))
            }
        }
        submitError(
            ErrorViewModel.ThrowableMessage(
                title = androidText(R.string.download_assets_install_failed),
                message = androidText(*texts.toTypedArray())
            )
        )
    }
}

private fun projectKey(
    platform: Platform,
    projectId: String
): String = "${platform.name}/$projectId"

/**
 * 执行依赖解析操作，失败时写日志并返回null
 */
private suspend fun <T> resolveOrNull(
    description: String,
    block: suspend () -> T
): T? {
    return try {
        block()
    } catch (e: CancellationException) {
        throw e
    } catch (e: Throwable) {
        Logger.warning(TAG, description, e)
        null
    }
}

/**
 * 展开一个依赖项
 * 解析出具体版本、登记下载任务，并递归展开该版本的必装依赖
 */
private suspend fun expand(
    request: DependencyRequest,
    targets: List<Version>,
    context: DependencyContext
) {
    // 只处理该项目尚未处理过的目标游戏版本，既避免重复处理，也能阻断依赖成环
    val pendingTargets = context.claimTargets(projectKey(request.platform, request.projectId), targets)
    if (pendingTargets.isEmpty()) return

    try {
        val folder = request.classes.versionFolder
        if (folder == VersionFolders.NONE) {
            // 无法确定依赖的安装目录
            pendingTargets.forEach {
                context.failures.record(request.projectTitle, it.getVersionName())
            }
            return
        }

        // 仅模组类型依赖会跳过本地已安装的目标游戏版本，已安装的依赖不再展开其依赖
        val installTargets = if (request.classes == PlatformClasses.MOD) {
            pendingTargets.filterNot { context.isInstalled(request, it) }
        } else {
            pendingTargets
        }
        if (installTargets.isEmpty()) return

        val resolved = context.semaphore.withPermit {
            context.resolve(request, installTargets)
        }
        resolved.forEach { (version, group) ->
            context.registerDownload(request, version, group)
        }

        // 递归展开已解析版本的必装依赖
        // 信号量只在解析期间持有，进入子树前已释放，避免递归等待许可造成死锁
        resolved.forEach { (version, group) ->
            val dependencies = resolveOrNull("Failed to read the dependencies of: ${request.projectTitle}") {
                version.platformDependencies()
            } ?: return@forEach
            val required = dependencies.filter { it.type == PlatformDependencyType.REQUIRED }
            if (required.isEmpty()) return@forEach

            coroutineScope {
                required.forEach { dependency ->
                    val child = context.expandChild(dependency, request) ?: return@forEach
                    async { expand(child, group, context) }
                }
            }
        }
    } catch (e: CancellationException) {
        throw e
    } catch (e: Throwable) {
        Logger.warning(TAG, "An error occurred while installing the dependency: ${request.projectTitle}", e)
        pendingTargets.forEach {
            context.failures.record(request.projectTitle, it.getVersionName())
        }
    }
}

/**
 * 解析依赖项在目标游戏版本上的具体版本
 * @return 各具体版本与其对应的目标游戏版本
 */
private suspend fun DependencyContext.resolve(
    request: DependencyRequest,
    targets: List<Version>
): List<Pair<PlatformVersion, List<Version>>> {
    task.updateMessage(
        androidText(R.string.download_assets_loading_dep_project, request.projectId)
    )

    if (request.versionId != null) {
        // 主资源指定了依赖的精确版本，直接下载该版本
        val version = resolveOrNull("Failed to retrieve the dependency version: ${request.versionId}") {
            getVersionById(
                versionId = request.versionId,
                platform = request.platform,
                printLog = false
            )
        }
        if (version == null || !version.initFile(request.projectId)) {
            targets.forEach {
                failures.record(request.projectTitle, it.getVersionName())
            }
            return emptyList()
        }
        return listOf(version to targets)
    }

    // 拉取依赖项目的全部版本，再按各个目标游戏版本本地挑选
    // initAll 已按发布时间倒序排列，取第一个适配的即为最新的版本
    val allVersions = resolveOrNull("Failed to retrieve the dependency versions: ${request.projectId}") {
        getVersions(
            projectID = request.projectId,
            platform = request.platform
        ).initAll(request.projectId)
    }
    if (allVersions == null) {
        targets.forEach {
            failures.record(request.projectTitle, it.getVersionName())
        }
        return emptyList()
    }

    return targets
        .groupBy { target -> allVersions.firstOrNull { it.isCompatibleWith(target, request.classes) } }
        .mapNotNull { (version, group) ->
            if (version == null) {
                group.forEach {
                    failures.record(request.projectTitle, it.getVersionName())
                }
                null
            } else {
                version to group
            }
        }
}

/**
 * 将发现的传递依赖转换为可安装的依赖项
 * 同一个项目在整张依赖图中只会被处理一次
 */
private suspend fun DependencyContext.expandChild(
    dependency: PlatformVersion.PlatformDependency,
    parent: DependencyRequest
): DependencyRequest? = semaphore.withPermit {
    val projectId = dependency.projectId ?: run {
        val versionId = dependency.versionId ?: return@withPermit null
        resolveOrNull("Failed to resolve the project of the dependency version: $versionId") {
            getVersionById(versionId, dependency.platform, printLog = false).platformProjectId()
        } ?: return@withPermit null
    }

    val (classes, title) = resolveProject(dependency.platform, projectId, parent.classes)
    if (classes.versionFolder == VersionFolders.NONE) return@withPermit null

    DependencyRequest(
        platform = dependency.platform,
        projectId = projectId,
        versionId = dependency.versionId,
        classes = classes,
        projectTitle = title
    )
}

/**
 * 获取依赖项目的类别与标题
 * 查询失败时回退到父依赖的类别，类别只影响安装目录，不应中断整条依赖链
 */
private suspend fun DependencyContext.resolveProject(
    platform: Platform,
    projectId: String,
    fallbackClasses: PlatformClasses
): Pair<PlatformClasses, String> {
    projectCache[projectId]?.let { project ->
        return project.platformClasses(fallbackClasses) to project.platformTitle()
    }

    val project = resolveOrNull("Failed to retrieve the dependency project information: $projectId") {
        getProjectByVersion(projectId, platform, printLog = false)
    } ?: return fallbackClasses to projectId

    projectCache[projectId] = project
    return project.platformClasses(fallbackClasses) to project.platformTitle()
}

private fun DependencyContext.registerDownload(
    request: DependencyRequest,
    version: PlatformVersion,
    targets: List<Version>
) {
    val key = "${version.platform().name}/${version.platformId()}"
    downloadGroups.computeIfAbsent(key) {
        DownloadGroup(version, request.classes.versionFolder.folderName)
    }.addTargets(targets)
}

/**
 * 依赖图展开完成后，统一发布下载任务
 */
private fun DependencyContext.publishDownloads(
    submitError: (ErrorViewModel.ThrowableMessage) -> Unit
) {
    downloadGroups.toSortedMap().values.forEach { group ->
        val targets = group.targetList()
        if (targets.isEmpty()) return@forEach
        downloadSingleForVersions(
            version = group.version,
            versions = targets,
            folder = group.folder,
            submitError = submitError
        )
    }
}

/**
 * 目标游戏版本的模组目录中是否已安装该依赖项目
 * 检查失败时按未安装处理，避免漏装
 */
private suspend fun DependencyContext.isInstalled(
    request: DependencyRequest,
    target: Version
): Boolean {
    val key = "${request.platform.name}/${target.getVersionName()}"
    installedMemo[key]?.let { return request.projectId in it }

    val projects = installedMutex.withLock {
        installedMemo[key] ?: resolveOrNull("Failed to check whether the dependency is installed locally: ${request.projectTitle}") {
            val modsDir = VersionFolders.MOD.getDir(target.getGameDir())
            matchInstalledMods(
                fingerprints = scanModFingerprints(modsDir),
                platform = request.platform
            ).byProject.keys.toSet()
        }?.also { installedMemo[key] = it }
    }
    return projects?.contains(request.projectId) == true
}

/**
 * 该依赖版本是否适配目标游戏版本
 */
private fun PlatformVersion.isCompatibleWith(
    target: Version,
    classes: PlatformClasses
): Boolean {
    val info = target.getVersionInfo() ?: return true
    if (info.minecraftVersion !in platformGameVersion()) return false

    // 仅模组依赖需要校验模组加载器
    if (classes != PlatformClasses.MOD) return true
    val targetLoader = info.loaderInfo?.loader?.toPlatformLoader(platform()) ?: return true
    val versionLoaders = platformLoaders()
    return versionLoaders.isEmpty() || targetLoader in versionLoaders
}

/**
 * 将本地模组加载器转换为平台上的加载器标识
 */
private fun ModLoader.toPlatformLoader(platform: Platform): PlatformDisplayLabel? {
    if (displayName.isEmpty()) return null
    return when (platform) {
        Platform.MODRINTH -> ModrinthModLoaderCategory.entries.find {
            it.getDisplayName().equals(displayName, true)
        }
        Platform.CURSEFORGE -> CurseForgeModLoader.entries.find {
            it.getDisplayName().equals(displayName, true)
        }
    }
}
