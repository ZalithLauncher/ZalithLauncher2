/*
 * Zalith Launcher 2
 * Copyright (C) 2025-2026 MovTery <movtery228@qq.com> and contributors
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

package com.movtery.zalithlauncher.game.download.addon.quick

import com.movtery.zalithlauncher.game.download.addon.DependencyRequest
import com.movtery.zalithlauncher.game.download.addon.isCompatibleWith
import com.movtery.zalithlauncher.game.download.addon.platform.Platform
import com.movtery.zalithlauncher.game.download.addon.platform.PlatformClasses
import com.movtery.zalithlauncher.game.download.addon.platform.PlatformDependencyType
import com.movtery.zalithlauncher.game.download.addon.platform.PlatformProject
import com.movtery.zalithlauncher.game.download.addon.platform.PlatformVersion
import com.movtery.zalithlauncher.game.download.addon.platform.getProjectByVersion
import com.movtery.zalithlauncher.game.download.addon.platform.getVersionById
import com.movtery.zalithlauncher.game.download.addon.platform.getVersions
import com.movtery.zalithlauncher.game.version.installed.Version
import com.movtery.zalithlauncher.game.version.installed.VersionFolders
import com.movtery.zalithlauncher.game.version.mod.matchInstalledMods
import com.movtery.zalithlauncher.game.version.mod.scanModFingerprints
import com.movtery.zalithlauncher.utils.logging.Logger
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.sync.withPermit
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

private const val TAG = "QuickDownloadPlanner"
private const val MAX_DEPENDENCY_PROJECTS = 64
private const val DEPENDENCY_PARALLELISM = 4

/** 规划阶段的不可恢复错误 */
internal class QuickPlanningException(message: String, cause: Throwable? = null) : Exception(message, cause)

/**
 * 逻辑游戏版本及其兼容性快照
 * @param gameDir 版本隔离解析后的真实游戏目录
 */
internal data class QuickGameTarget(
    val versionName: String,
    val gameDir: File,
    val minecraftVersion: String?,
    val loaders: List<String>?
) {
    val key: String = "${gameDir.path}\u0000$versionName"

    fun installTo(classes: PlatformClasses): QuickInstallTarget = QuickInstallTarget(
        game = this,
        installDir = classes.versionFolder.getDir(gameDir).canonicalFile
    )

    companion object {
        fun from(version: Version): QuickGameTarget {
            val info = version.getVersionInfo()
            return QuickGameTarget(
                versionName = version.getVersionName(),
                gameDir = version.getGameDir().canonicalFile,
                minecraftVersion = info?.minecraftVersion,
                loaders = info?.loaderInfos?.map { it.loader.displayName }
            )
        }
    }
}

/** 解析到实际安装目录的目标 */
internal data class QuickInstallTarget(
    val game: QuickGameTarget,
    val installDir: File
)

/** 已完成严格初始化的远端文件快照 */
internal data class QuickResolvedFile(
    val versionId: String,
    val displayName: String,
    val fileName: String,
    val downloadUrl: String,
    val sha1: String?,
    /** 小于零表示大小未知 */
    val size: Long,
)

/** 下载聚合键 */
internal data class QuickItemKey(
    val platform: Platform,
    val projectId: String,
    val versionId: String,
    val classes: PlatformClasses
)

/** 一份远端文件及其全部安装目标 */
internal data class QuickDownloadItem(
    val key: QuickItemKey,
    val projectTitle: String,
    val file: QuickResolvedFile,
    val targets: List<QuickInstallTarget>
)

/**
 * 统计一个逻辑游戏版本上没有可安装的兼容文件
 * @param platform 来源平台
 * @param projectId 目标项目的 ID
 * @param projectTitle 项目可展示的名称
 * @param versionName 要安装到的游戏版本的名称
 */
internal data class QuickPlanningIssue(
    val platform: Platform,
    val projectId: String,
    val projectTitle: String,
    val versionName: String
)

/** 计算规范化的最终路径 */
internal fun quickDestination(
    key: QuickItemKey,
    file: QuickResolvedFile,
    target: QuickInstallTarget
): File {
    val destinationName = if (key.classes == PlatformClasses.SAVES) {
        // 存档使用去掉扩展名的目录名
        file.fileName.substringBeforeLast('.')
    } else {
        file.fileName
    }
    requireSafeQuickFileName(destinationName)
    return File(target.installDir, destinationName).canonicalFile
}

internal fun QuickDownloadItem.destinationFor(target: QuickInstallTarget): File =
    quickDestination(key, file, target)

/**
 * 解析主项目及全部必要依赖，并按实际安装目录聚合下载项
 * @param onIssue 无兼容文件的目标通过此回调汇报并跳过
 */
internal suspend fun planQuickDownload(
    request: QuickDownloadRequest,
    versions: List<Version>,
    onProgress: (String) -> Unit = {},
    onIssue: (QuickPlanningIssue) -> Unit = {}
): List<QuickDownloadItem> {
    require(request.classes != PlatformClasses.MOD_PACK) { "Mod packs do not support quick download." }
    require(versions.isNotEmpty()) { "No game versions selected." }
    val targets = versions.map(QuickGameTarget::from).distinctBy { it.key }
    return QuickDownloadPlanner().plan(request, targets, onProgress, onIssue)
}

/**
 * 生成下载安装的计划
 */
internal class QuickDownloadPlanner(
    private val loadVersions: suspend (String, Platform) -> List<PlatformVersion> = { id, platform ->
        getVersions(projectID = id, platform = platform)
    },
    private val loadVersion: suspend (String, Platform) -> PlatformVersion = { id, platform ->
        getVersionById(versionId = id, platform = platform, printLog = false)
    },
    private val loadProject: suspend (String, Platform) -> PlatformProject = { id, platform ->
        getProjectByVersion(projectId = id, platform = platform, printLog = false)
    },
    private val installedChecker: (suspend (Platform, String, String?, QuickGameTarget) -> Boolean)? = null
) {
    private val semaphore = Semaphore(DEPENDENCY_PARALLELISM)
    private val installedModIndex = InstalledModIndex()


    suspend fun plan(
        request: QuickDownloadRequest,
        targets: List<QuickGameTarget>,
        onProgress: (String) -> Unit = {},
        onIssue: (QuickPlanningIssue) -> Unit = {}
    ): List<QuickDownloadItem> {
        require(targets.isNotEmpty()) { "No game versions selected." }
        val context = PlanningContext(request, targets, onProgress, onIssue)
        context.plan()
        return context.finish()
    }

    /** 单次规划的可变上下文；缓存、依赖声明和预算不跨任务复用。 */
    private inner class PlanningContext(
        private val root: QuickDownloadRequest,
        private val rootTargets: List<QuickGameTarget>,
        private val onProgress: (String) -> Unit,
        private val onIssue: (QuickPlanningIssue) -> Unit
    ) {
        private val claims = ConcurrentHashMap<String, DependencyClaim>()
        private val budgets = ConcurrentHashMap<String, AtomicInteger>()
        private val groups = ConcurrentHashMap<QuickItemKey, DownloadGroup>()
        private val reportedIssues = ConcurrentHashMap.newKeySet<QuickPlanningIssue>()

        private val versionsCache = ConcurrentHashMap<String, List<PlatformVersion>>()
        private val versionsLocks = ConcurrentHashMap<String, Mutex>()
        private val exactCache = ConcurrentHashMap<String, PlatformVersion>()
        private val exactLocks = ConcurrentHashMap<String, Mutex>()
        private val projectCache = ConcurrentHashMap<String, PlatformProject>()
        private val projectLocks = ConcurrentHashMap<String, Mutex>()

        suspend fun plan() {
            val rootRequest = DependencyRequest(
                platform = root.platform,
                projectId = root.projectId,
                versionId = null,
                classes = root.classes,
                projectTitle = root.title
            )
            val resolved = resolveClaimed(rootRequest, rootTargets, countBudget = false)
            resolved.forEach(::register)
            resolved.forEach { expandDependencies(it) }
        }

        private suspend fun expand(request: DependencyRequest, targets: List<QuickGameTarget>) {
            val installTargets = if (request.classes == PlatformClasses.MOD) {
                targets.filterNot { target -> isInstalled(request, target) }
            } else {
                targets
            }
            if (installTargets.isEmpty()) return

            val resolved = resolveClaimed(request, installTargets, countBudget = true)
            resolved.forEach(::register)
            resolved.forEach { expandDependencies(it) }
        }

        private suspend fun resolveClaimed(
            request: DependencyRequest,
            targets: List<QuickGameTarget>,
            countBudget: Boolean
        ): List<ResolvedVersion> = resolve(request, targets).mapNotNull { resolved ->
            claim(resolved, countBudget)
        }

        /** 仅展开必要依赖 */
        private suspend fun expandDependencies(resolved: ResolvedVersion) {
            val required = try {
                resolved.version.platformDependencies().filter {
                    it.type == PlatformDependencyType.REQUIRED // 仅接受必须安装的依赖
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Throwable) {
                throw QuickPlanningException(
                    "Failed to read required dependencies of ${resolved.request.projectTitle}.", e
                )
            }
            if (required.isEmpty()) return

            coroutineScope {
                required.map { dependency ->
                    async {
                        val child = expandChild(dependency, resolved.request)
                        expand(child, resolved.targets)
                    }
                }.awaitAll()
            }
        }

        private suspend fun expandChild(
            dependency: PlatformVersion.PlatformDependency,
            parent: DependencyRequest
        ): DependencyRequest {
            val projectId = dependency.projectId ?: run {
                val versionId = dependency.versionId
                    ?: throw QuickPlanningException("A required dependency of ${parent.projectTitle} has no project or version id.")

                exactVersion(
                    platform = dependency.platform,
                    versionId = versionId,
                    currentProjectId = null,
                    initialize = false
                ).platformProjectId()
            }
            val project = project(dependency.platform, projectId)
            val classes = project.platformClasses(parent.classes)
            if (classes == PlatformClasses.MOD_PACK || classes.versionFolder == VersionFolders.NONE) {
                throw QuickPlanningException("Dependency ${project.platformTitle()} has no supported install folder.")
            }
            return DependencyRequest(
                platform = dependency.platform,
                projectId = projectId,
                versionId = dependency.versionId,
                classes = classes,
                projectTitle = project.platformTitle().ifEmpty { projectId }
            )
        }

        /**
         * 为同一逻辑目标声明项目版本：重复声明相同版本时去重，不同版本则属于规划冲突。
         * 依赖项目预算只统计显式依赖，不计入用户选择的主项目。
         */
        private fun claim(
            resolved: ResolvedVersion,
            countBudget: Boolean
        ): ResolvedVersion? {
            val request = resolved.request
            val versionId = resolved.version.platformId()
            val projectKey = "${request.platform.name}/${request.projectId}"
            val pendingTargets = resolved.targets.filter { target ->
                val claimKey = "$projectKey|${target.key}"
                val previous = claims.putIfAbsent(claimKey, DependencyClaim(versionId))
                if (previous != null) {
                    if (previous.versionId != versionId) {
                        throw QuickPlanningException(
                            "Conflicting versions required for ${request.projectTitle} on ${target.versionName}."
                        )
                    }
                    return@filter false
                }
                if (countBudget) {
                    val used = budgets.computeIfAbsent(target.key) { AtomicInteger() }.incrementAndGet()
                    if (used > MAX_DEPENDENCY_PROJECTS) {
                        throw QuickPlanningException(
                            "The dependency limit of $MAX_DEPENDENCY_PROJECTS was exceeded for ${target.versionName}."
                        )
                    }
                }
                true
            }
            return resolved.copy(targets = pendingTargets).takeIf { pendingTargets.isNotEmpty() }
        }

        private suspend fun isInstalled(request: DependencyRequest, target: QuickGameTarget): Boolean {
            return try {
                installedChecker?.invoke(request.platform, request.projectId, request.versionId, target)
                    ?: installedModIndex.isInstalled(
                        platform = request.platform,
                        projectId = request.projectId,
                        versionId = request.versionId,
                        target = target
                    )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Throwable) {
                Logger.warning(TAG, "Failed to check installed dependency: ${request.projectTitle}", e)
                false
            }
        }

        private suspend fun resolve(
            request: DependencyRequest,
            targets: List<QuickGameTarget>
        ): List<ResolvedVersion> {
            if (targets.isEmpty()) return emptyList()
            onProgress(request.projectTitle)

            request.versionId?.let { versionId ->
                // 精确依赖始终使用指定版本
                val version = exactVersion(
                    platform = request.platform,
                    versionId = versionId,
                    currentProjectId = request.projectId,
                    initialize = true
                )
                val compatibleTargets = targets.filter { target ->
                    version.isCompatibleWith(
                        gameVersion = target.minecraftVersion,
                        targetLoaders = target.loaders,
                        classes = request.classes,
                        requireCompatibleModLoader = true
                    ).also { compatible ->
                        if (!compatible) reportIncompatible(request, target)
                    }
                }
                if (compatibleTargets.isEmpty()) return emptyList()
                return listOf(ResolvedVersion(request, version, compatibleTargets))
            }

            // 项目级依赖从按日期倒序的列表中选择首个严格兼容文件

            val allVersions = projectVersions(request)
            val groups = LinkedHashMap<QuickItemKey, ResolvedVersion>()
            targets.forEach { target ->
                val version = allVersions.firstOrNull { candidate ->
                    candidate.isCompatibleWith(
                        gameVersion = target.minecraftVersion,
                        targetLoaders = target.loaders,
                        classes = request.classes,
                        requireCompatibleModLoader = true
                    )
                }
                if (version == null) {
                    reportIncompatible(request, target)
                    return@forEach
                }
                val key = request.key(version)
                val old = groups[key]
                groups[key] = old?.copy(targets = old.targets + target)
                    ?: ResolvedVersion(request, version, listOf(target))
            }
            return groups.values.toList()
        }

        private fun reportIncompatible(request: DependencyRequest, target: QuickGameTarget) {
            val issue = QuickPlanningIssue(
                platform = request.platform,
                projectId = request.projectId,
                projectTitle = request.projectTitle,
                versionName = target.versionName
            )
            if (reportedIssues.add(issue)) {
                Logger.warning(
                    TAG,
                    "No compatible version of ${request.projectTitle} was found for ${target.versionName}; skipping this target."
                )
                synchronized(onIssue) { onIssue(issue) }
            }
        }

        private suspend fun projectVersions(request: DependencyRequest): List<PlatformVersion> {
            val key = "${request.platform.name}/${request.projectId}"
            versionsCache[key]?.let { return it } // 已有缓存

            return versionsLocks.computeIfAbsent(key) { Mutex() }.withLock {
                versionsCache[key] ?: run {
                    onProgress(request.projectTitle)
                    // 无缓存可用，加载项目所有发布的版本
                    val versions = try {
                        semaphore.withPermit {
                            loadVersions(request.projectId, request.platform)
                                .initAllStrict(request.projectId)
                        }
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: Throwable) {
                        throw QuickPlanningException(
                            "Failed to resolve versions of ${request.projectTitle}.", e
                        )
                    }
                    versionsCache[key] = versions
                    versions
                }
            }
        }

        private suspend fun exactVersion(
            platform: Platform,
            versionId: String,
            currentProjectId: String?,
            initialize: Boolean
        ): PlatformVersion {
            val key = "${platform.name}/$versionId"
            if (!initialize) exactCache[key]?.let { return it }

            return exactLocks.computeIfAbsent(key) { Mutex() }.withLock {
                val version = exactCache[key] ?: try {
                    semaphore.withPermit {
                        loadVersion(versionId, platform)
                    }.also {
                        exactCache[key] = it
                    }
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Throwable) {
                    throw QuickPlanningException("Failed to resolve dependency version $versionId.", e)
                }

                if (initialize) {
                    val initialized = try {
                        semaphore.withPermit {
                            version.initFileStrict(currentProjectId ?: version.platformProjectId())
                        }
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: Throwable) {
                        throw QuickPlanningException("Failed to initialize dependency version $versionId.", e)
                    }
                    if (!initialized) {
                        throw QuickPlanningException("Dependency version $versionId has no downloadable primary file.")
                    }
                }
                version
            }
        }

        private suspend fun project(platform: Platform, projectId: String): PlatformProject {
            val key = "${platform.name}/$projectId"
            projectCache[key]?.let { return it }
            return projectLocks.computeIfAbsent(key) { Mutex() }.withLock {
                projectCache[key] ?: try {
                    semaphore.withPermit {
                        loadProject(projectId, platform)
                    }.also {
                        projectCache[key] = it
                    }
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Throwable) {
                    throw QuickPlanningException("Failed to resolve required project $projectId.", e)
                }
            }
        }

        private fun register(resolved: ResolvedVersion) {
            val request = resolved.request
            val key = request.key(resolved.version)
            groups.computeIfAbsent(key) {
                DownloadGroup(
                    key = key,
                    projectTitle = request.projectTitle,
                    file = resolved.version.toQuickResolvedFile()
                )
            }.addTargets(resolved.targets.map { it.installTo(request.classes) })
        }

        /** 合并共享安装目录，并检查同项目多版本及不同文件写入同一路径的冲突 */
        fun finish(): List<QuickDownloadItem> {
            val destinationOwners = mutableMapOf<String, QuickItemKey>()
            val projectDirectories = mutableMapOf<ProjectDirectoryKey, String>()
            return groups.values.sortedWith(
                compareBy({ it.key.platform.name }, { it.key.projectId }, { it.key.versionId })
            ).map { group ->
                val targets = group.targetList().distinctBy { it.installDir.path }
                targets.forEach { target ->
                    val directoryKey = ProjectDirectoryKey(
                        platform = group.key.platform,
                        projectId = group.key.projectId,
                        classes = group.key.classes,
                        installDir = target.installDir.path
                    )
                    val previousVersion = projectDirectories.putIfAbsent(directoryKey, group.key.versionId)
                    if (previousVersion != null && previousVersion != group.key.versionId) {
                        throw QuickPlanningException(
                            "Multiple versions of ${group.projectTitle} target ${target.installDir.path}."
                        )
                    }

                    val destination = quickDestination(group.key, group.file, target)
                    val previous = destinationOwners.putIfAbsent(destination.path, group.key)
                    if (previous != null && previous != group.key) {
                        throw QuickPlanningException(
                            "Multiple planned files conflict at ${destination.path}."
                        )
                    }
                }
                QuickDownloadItem(
                    key = group.key,
                    projectTitle = group.projectTitle,
                    file = group.file,
                    targets = targets
                )
            }
        }
    }

    private data class ProjectDirectoryKey(
        val platform: Platform,
        val projectId: String,
        val classes: PlatformClasses,
        val installDir: String
    )

    private data class DependencyClaim(val versionId: String)

    private data class ResolvedVersion(
        val request: DependencyRequest,
        val version: PlatformVersion,
        val targets: List<QuickGameTarget>
    )

    private class DownloadGroup(
        val key: QuickItemKey,
        val projectTitle: String,
        val file: QuickResolvedFile
    ) {
        private val targets = mutableListOf<QuickInstallTarget>()

        fun addTargets(newTargets: List<QuickInstallTarget>) = synchronized(targets) {
            targets.addAll(newTargets)
        }

        fun targetList(): List<QuickInstallTarget> = synchronized(targets) { targets.toList() }
    }
}

private fun DependencyRequest.key(version: PlatformVersion) = QuickItemKey(
    platform = platform,
    projectId = projectId,
    versionId = version.platformId(),
    classes = classes
)

/**
 * 快捷下载的严格初始化：异常直接传播，永久无有效主文件的条目则被忽略。
 * 原始列表非空但全部不可下载属于规划错误，不能伪装成“没有兼容版本”。
 */
private suspend fun List<PlatformVersion>.initAllStrict(
    currentProjectId: String
): List<PlatformVersion> {
    if (isEmpty()) return emptyList()
    val initialized = mapNotNull { version ->
        if (version.initFileStrict(currentProjectId)) {
            version
        } else {
            Logger.warning(TAG, "Skipped ${version.platformId()} of $currentProjectId: no downloadable primary file.")
            null
        }
    }.sortedByDescending { it.platformDatePublished() }
    if (initialized.isEmpty()) {
        throw QuickPlanningException("Project $currentProjectId has no downloadable primary files.")
    }
    return initialized
}

/** 拒绝绝对路径、路径分隔符及相对导航，确保远端文件名不能逃出安装目录。 */
private fun requireSafeQuickFileName(fileName: String) {
    require(
        fileName.isNotBlank() && fileName != "." && fileName != ".." &&
                File(fileName).name == fileName &&
                !fileName.contains('/') && !fileName.contains('\\')
    ) { "Invalid remote file name: $fileName" }
}

private fun PlatformVersion.toQuickResolvedFile(): QuickResolvedFile {
    val fileName = platformFileName()
    requireSafeQuickFileName(fileName)
    val downloadUrl = platformDownloadUrl()
    require(downloadUrl.isNotBlank()) { "The remote file $fileName has no download URL." }
    return QuickResolvedFile(
        versionId = platformId(),
        displayName = platformDisplayName(),
        fileName = fileName,
        downloadUrl = downloadUrl,
        sha1 = platformSha1(),
        size = platformFileSize().takeIf { it > 0L } ?: -1L
    )
}


/** 按平台和真实游戏目录缓存一次本地模组指纹匹配，避免每个依赖重复扫描。 */
private class InstalledModIndex {
    private data class Matches(
        val projects: Set<String>,
        val versions: Set<String>
    )

    private val matches = ConcurrentHashMap<String, Matches>()
    private val locks = ConcurrentHashMap<String, Mutex>()

    /** 项目级依赖按项目 ID 检查；指定 [versionId] 时必须命中同一个平台文件版本。 */
    suspend fun isInstalled(
        platform: Platform,
        projectId: String,
        versionId: String?,
        target: QuickGameTarget
    ): Boolean {
        val key = "${platform.name}/${target.gameDir.path}"
        val installed = matches[key] ?: locks.computeIfAbsent(key) { Mutex() }.withLock {
            matches[key] ?: run {
                val modsDir = VersionFolders.MOD.getDir(target.gameDir)
                val matched = matchInstalledMods(
                    fingerprints = scanModFingerprints(modsDir),
                    platform = platform
                )
                Matches(
                    projects = matched.byProject.keys.toSet(),
                    versions = matched.byVersion.keys.toSet()
                ).also { matches[key] = it }
            }
        }
        return if (versionId == null) {
            projectId in installed.projects
        } else {
            versionId in installed.versions
        }
    }
}
