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

import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.coroutine.Task
import com.movtery.zalithlauncher.coroutine.TaskSystem
import com.movtery.zalithlauncher.game.download.addon.mapExceptionToMessage
import com.movtery.zalithlauncher.game.download.addon.platform.PlatformClasses
import com.movtery.zalithlauncher.game.download.addon.platform.mcim.mapMCIMMirrorUrls
import com.movtery.zalithlauncher.game.version.download.DEFAULT_DOWNLOAD_THREADS
import com.movtery.zalithlauncher.game.version.download.DownloadTask
import com.movtery.zalithlauncher.game.version.download.runBatchDownloads
import com.movtery.zalithlauncher.game.version.installed.Version
import com.movtery.zalithlauncher.game.version.saves.unpackSaveZip
import com.movtery.zalithlauncher.path.PathManager
import com.movtery.zalithlauncher.ui.androidText
import com.movtery.zalithlauncher.utils.file.ensureParentDirectory
import com.movtery.zalithlauncher.utils.file.formatFileSize
import com.movtery.zalithlauncher.utils.logging.Logger
import com.movtery.zalithlauncher.viewmodel.ErrorViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import org.apache.commons.io.FileUtils
import java.io.File
import java.io.IOException
import java.util.*

private const val TAG = "QuickDownloadTask"
private const val QUICK_FILE_TIMEOUT_MILLIS = 5 * 60 * 1000L

/** 快捷下载 Addon 的缓存目录名称 */
const val ADDON_CACHE_DIR_NAME = "quick-addon"

/**
 * 为一次版本选择提交唯一的聚合任务
 */
fun downloadQuickForVersions(
    request: QuickDownloadRequest,
    versions: List<Version>,
    submitError: (ErrorViewModel.ThrowableMessage) -> Unit
) {
    require(request.classes != PlatformClasses.MOD_PACK) { "Mod packs do not support quick download." }
    if (versions.isEmpty()) return

    val operationId = UUID.randomUUID().toString()
    val stagingDir = File(PathManager.DIR_CACHE, "$ADDON_CACHE_DIR_NAME/$operationId")
    val committer = QuickDownloadCommitter(
        backupDir = File(stagingDir, "backups"),
        operationId = operationId
    )

    TaskSystem.submitTask(
        Task.runTask(
            id = "$ADDON_CACHE_DIR_NAME/${request.platform.name}/${request.projectId}/$operationId",
            task = { task ->
                var lease: QuickInFlightLease? = null
                val planningIssues = mutableListOf<QuickPlanningIssue>()

                try {
                    task.updateProgress(-1f)

                    // 准备快捷下载任务阶段
                    task.updateMessage(androidText(R.string.download_addon_quick_planning, request.title))
                    val planned = planQuickDownload(
                        request = request,
                        versions = versions,
                        onProgress = { project ->
                            task.updateMessage(androidText(R.string.download_addon_quick_planning, project))
                        },
                        onIssue = planningIssues::add
                    )

                    lease = QuickDownloadInFlight.acquire(planned).also { acquired ->
                        if (acquired.skippedTargets > 0) {
                            Logger.info(TAG, "Skipped ${acquired.skippedTargets} in-flight install targets for ${request.projectId}.")
                            task.updateMessage(
                                androidText(R.string.download_addon_quick_skipped, acquired.skippedTargets)
                            )
                        }
                    }
                    val items = lease.items
                    if (items.isEmpty()) {
                        task.updateProgress(1f)
                        task.updateMessage(
                            androidText(
                                if (planned.isEmpty()) R.string.download_addon_quick_no_files
                                else R.string.download_addon_quick_all_skipped
                            )
                        )
                        submitPlanningIssues(planningIssues, submitError)
                        return@runTask
                    }

                    val staged = items.mapIndexed { index, item ->
                        StagedQuickItem(
                            item = item,
                            source = File(stagingDir, "files/$index/${item.file.fileName}").ensureParentDirectory(),
                            unpackDir = File(stagingDir, "unpacked/$index")
                        )
                    }
                    val downloads = staged.map { entry ->
                        DownloadTask(
                            urls = entry.item.file.downloadUrl.mapMCIMMirrorUrls(),
                            verifyIntegrity = true,
                            targetFile = entry.source,
                            sha1 = entry.item.file.sha1,
                            size = entry.item.file.size
                        )
                    }

                    task.runBatchDownloads(
                        tasks = downloads,
                        maxConnections = DEFAULT_DOWNLOAD_THREADS,
                        retryRounds = 1,
                        fileTimeoutMillis = QUICK_FILE_TIMEOUT_MILLIS,
                        onSnapshot = { snapshot ->
                            task.updateSpeed(snapshot.speedBytesPerSec)
                            task.updateMessage(
                                androidText(
                                    R.string.download_addon_quick_downloading,
                                    snapshot.downloadedFiles,
                                    snapshot.totalFiles,
                                    formatFileSize(snapshot.downloadedBytes)
                                )
                            )
                        }
                    )

                    task.updateProgress(-1f)
                    task.updateMessage(androidText(R.string.download_addon_quick_installing))
                    val payloads = staged.flatMap { entry -> entry.preparePayload() }
                    committer.commit(payloads)

                    task.updateProgress(1f)
                    task.updateMessage(androidText(R.string.download_addon_quick_complete))
                    submitPlanningIssues(planningIssues, submitError)
                } finally {
                    lease?.release()
                    cleanupQuickStaging(stagingDir, committer.recoveryRequired)
                }
            },
            onError = { error ->
                Logger.warning(TAG, "Quick download failed for ${request.projectId}.", error)
                submitError(
                    ErrorViewModel.ThrowableMessage(
                        title = androidText(R.string.download_addon_install_failed),
                        message = mapExceptionToMessage(error)
                    )
                )
            },
            onFinally = {
                cleanupQuickStaging(stagingDir, committer.recoveryRequired)
            }
        )
    )
}

/** 清理不再需要的暂存区 */
internal fun cleanupQuickStaging(stagingDir: File, recoveryRequired: Boolean) {
    if (recoveryRequired) {
        // 回滚失败后，暂存区可能保有原文件的唯一备份，绝不能再按普通缓存清理。
        Logger.error(TAG, "Rollback was incomplete; recovery files were kept at ${stagingDir.absolutePath}")
    } else {
        FileUtils.deleteQuietly(stagingDir)
    }
}

/** 将兼容性跳过项按项目汇总，避免同一项目重复提示多个游戏版本。 */
private fun submitPlanningIssues(
    issues: List<QuickPlanningIssue>,
    submitError: (ErrorViewModel.ThrowableMessage) -> Unit
) {
    val projects = issues.distinct()
        .groupBy { Triple(it.platform, it.projectId, it.projectTitle) }
        .entries
        .sortedBy { it.key.third }
    if (projects.isEmpty()) return

    val texts = projects.flatMapIndexed { index, (key, projectIssues) ->
        val versionNames = projectIssues.map { it.versionName }.distinct().sorted().joinToString()
        buildList {
            if (index > 0) add(androidText("\n"))
            add(
                androidText(
                    R.string.download_addon_quick_no_compatible,
                    key.third,
                    versionNames
                )
            )
        }
    }
    submitError(
        ErrorViewModel.ThrowableMessage(
            title = androidText(R.string.download_addon_quick_incomplete),
            message = androidText(*texts.toTypedArray())
        )
    )
}

/** 一份暂存文件及其存档解压目录；同一份文件可生成多个安装目标。 */
private class StagedQuickItem(
    val item: QuickDownloadItem,
    val source: File,
    val unpackDir: File
) {
    /** 将暂存文件或已解压存档转换为提交负载，此阶段仍不修改游戏目录。 */
    suspend fun preparePayload(): List<QuickInstallPayload> {
        if (item.key.classes == PlatformClasses.SAVES) {
            unpackSaveZip(zipFile = source, targetPath = unpackDir)
            val saveDir = File(unpackDir, source.nameWithoutExtension)
            if (!saveDir.isDirectory) throw IOException("The unpacked save is missing: ${saveDir.path}")
            return item.targets.map { target ->
                QuickInstallPayload(
                    source = saveDir,
                    destination = item.destinationFor(target)
                )
            }
        }
        if (!source.isFile) throw IOException("The downloaded file is missing: ${source.path}")
        return item.targets.map { target ->
            QuickInstallPayload(
                source = source,
                destination = item.destinationFor(target)
            )
        }
    }
}

/** 单个文件或目录从暂存区到最终目标的复制单元。 */
internal data class QuickInstallPayload(
    val source: File,
    val destination: File
)

private data class CopyAction(
    val source: File,
    val destination: File,
    val backup: File?
)

/**
 * 在所有下载与解压完成后提交文件。覆盖前统一备份，失败时按逆序尽力恢复。
 */
internal class QuickDownloadCommitter(
    private val backupDir: File,
    private val operationId: String = UUID.randomUUID().toString(),
    private val restoreBackup: (File, File) -> Unit = { backup, destination ->
        backup.copyTo(destination, overwrite = false)
    }
) {
    /** 回滚未完整成功时，调用方必须保留暂存区中的恢复文件。 */
    @Volatile
    var recoveryRequired: Boolean = false
        private set

    /**
     * 预检全部目标并备份已有文件后再提交；异常或取消时在 [NonCancellable] 中回滚。
     * 原始异常始终重新抛出，回滚失败通过 [recoveryRequired] 要求保留恢复文件。
     */
    suspend fun commit(payloads: List<QuickInstallPayload>) {
        val touched = mutableListOf<CopyAction>()
        val createdDirectories = mutableListOf<File>()
        try {
            withContext(Dispatchers.IO) {
                val destinations = HashSet<String>()
                payloads.forEach { payload ->
                    val path = payload.destination.canonicalPath
                    if (!destinations.add(path)) {
                        throw IOException("Multiple install payloads target the same path: $path")
                    }
                }

                val copyActions = mutableListOf<CopyAction>()
                val requiredDirectories = linkedSetOf<File>()
                payloads.forEach { payload -> flatten(payload, copyActions, requiredDirectories) }

                val actionDestinations = HashSet<String>()
                copyActions.forEach { action ->
                    if (!actionDestinations.add(action.destination.canonicalPath)) {
                        throw IOException("Multiple files target ${action.destination.path}")
                    }
                }

                //先在暂存区完成全部备份；此阶段失败不会修改任何目标文件。
                copyActions.forEachIndexed { index, action ->
                    coroutineContext.ensureActive()
                    val destination = action.destination
                    if (destination.exists() && !destination.isFile) {
                        throw IOException("Install target is not a file: ${destination.path}")
                    }
                    if (destination.isFile) {
                        val backup = File(backupDir, "$index.file").ensureParentDirectory()
                        action.source.inputStream().close() //提前确认源文件可读
                        destination.copyTo(backup, overwrite = false)
                        copyActions[index] = action.copy(backup = backup)
                    }
                }

                requiredDirectories.forEach { directory ->
                    coroutineContext.ensureActive()
                    ensureDirectory(directory, createdDirectories)
                }
                copyActions.forEach { action ->
                    coroutineContext.ensureActive()
                    ensureDirectory(action.destination.parentFile, createdDirectories)
                    touched += action
                    replaceFile(action)
                }
            }
        } catch (error: Throwable) {
            // withContext 在边界上也可能交付取消；统一在外层回滚，覆盖最后一次复制完成后的取消。
            withContext(NonCancellable + Dispatchers.IO) {
                rollback(touched, createdDirectories, error)
            }
            throw error
        }
    }

    private fun flatten(
        payload: QuickInstallPayload,
        actions: MutableList<CopyAction>,
        directories: MutableSet<File>
    ) {
        val source = payload.source
        val destination = payload.destination
        when {
            source.isFile -> actions += CopyAction(source, destination, backup = null)
            source.isDirectory -> {
                directories += destination
                source.walkTopDown().forEach { entry ->
                    if (entry == source) return@forEach
                    val relative = entry.relativeTo(source)
                    val target = File(destination, relative.path)
                    when {
                        entry.isDirectory -> directories += target
                        entry.isFile -> actions += CopyAction(entry, target, backup = null)
                        else -> throw IOException("Unsupported save entry: ${entry.path}")
                    }
                }
            }
            else -> throw IOException("Install payload does not exist: ${source.path}")
        }
    }

    private fun ensureDirectory(directory: File?, created: MutableList<File>) {
        if (directory == null || directory.isDirectory) return
        if (directory.exists()) throw IOException("Install parent is not a directory: ${directory.path}")
        ensureDirectory(directory.parentFile, created)
        if (!directory.mkdir() && !directory.isDirectory) {
            throw IOException("Unable to create install directory: ${directory.path}")
        }
        created += directory
    }

    /** 优先通过同目录临时文件替换目标；任何中途失败都由外层回滚处理。 */
    private fun replaceFile(action: CopyAction) {
        val destination = action.destination
        val temporary = File(destination.parentFile, ".${destination.name}.$operationId.tmp")
        if (temporary.exists() && !temporary.delete()) {
            throw IOException("Unable to clear temporary install file: ${temporary.path}")
        }
        try {
            action.source.copyTo(temporary, overwrite = false)
            if (destination.exists() && !destination.delete()) {
                throw IOException("Unable to replace install target: ${destination.path}")
            }
            if (!temporary.renameTo(destination)) {
                action.source.copyTo(destination, overwrite = true)
            }
        } finally {
            FileUtils.deleteQuietly(temporary)
        }
    }

    /** 逆序恢复已触及目标；恢复错误附加到原异常，并设置 [recoveryRequired]。 */
    private fun rollback(
        touched: List<CopyAction>,
        createdDirectories: List<File>,
        original: Throwable
    ) {
        touched.asReversed().forEach { action ->
            try {
                if (action.backup != null) {
                    if (action.destination.exists() && !action.destination.delete()) {
                        throw IOException("Unable to remove partial target: ${action.destination.path}")
                    }
                    restoreBackup(action.backup, action.destination)
                } else if (!FileUtils.deleteQuietly(action.destination)) {
                    throw IOException("Unable to remove new install target: ${action.destination.path}")
                }
            } catch (rollbackError: Throwable) {
                recoveryRequired = true
                Logger.error(TAG, "Failed to roll back ${action.destination.path}", rollbackError)
                original.addSuppressed(rollbackError)
            }
        }
        createdDirectories.asReversed().forEach { directory ->
            if (!directory.delete() && directory.isDirectory && directory.listFiles()?.isEmpty() == true) {
                Logger.warning(TAG, "Unable to remove empty install directory: ${directory.path}")
            }
        }
    }
}
