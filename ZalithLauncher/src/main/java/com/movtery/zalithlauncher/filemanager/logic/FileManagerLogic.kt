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

package com.movtery.zalithlauncher.filemanager.logic

import com.movtery.zalithlauncher.filemanager.logic.compress.CompressOptions
import com.movtery.zalithlauncher.filemanager.logic.compress.CompressSummary
import com.movtery.zalithlauncher.filemanager.logic.compress.Compressor
import com.movtery.zalithlauncher.filemanager.logic.entry.BrowseException
import com.movtery.zalithlauncher.filemanager.logic.entry.FmListResult
import com.movtery.zalithlauncher.filemanager.logic.entry.toFmEntry
import com.movtery.zalithlauncher.filemanager.logic.extract.ArchivePasswordException
import com.movtery.zalithlauncher.filemanager.logic.extract.ExtractOptions
import com.movtery.zalithlauncher.filemanager.logic.extract.ExtractSummary
import com.movtery.zalithlauncher.filemanager.logic.extract.Extractor
import com.movtery.zalithlauncher.filemanager.logic.ops.ByteRateTracker
import com.movtery.zalithlauncher.filemanager.logic.ops.ConflictResolution
import com.movtery.zalithlauncher.filemanager.logic.ops.FileOps
import com.movtery.zalithlauncher.filemanager.logic.ops.PasteMode
import com.movtery.zalithlauncher.filemanager.logic.ops.PasteRequest
import com.movtery.zalithlauncher.filemanager.logic.ops.PasteSummary
import com.movtery.zalithlauncher.filemanager.logic.ops.RenameCreateOps
import com.movtery.zalithlauncher.filemanager.logic.task.RunResult
import com.movtery.zalithlauncher.filemanager.logic.task.TaskKind
import com.movtery.zalithlauncher.filemanager.logic.task.TaskManager
import com.movtery.zalithlauncher.filemanager.logic.trash.RestoreResult
import com.movtery.zalithlauncher.filemanager.logic.trash.TrashItem
import com.movtery.zalithlauncher.filemanager.logic.trash.TrashManager
import com.movtery.zalithlauncher.filemanager.os.FmLog
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import java.nio.file.Files
import java.nio.file.LinkOption
import java.nio.file.NoSuchFileException
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.attribute.BasicFileAttributes
import java.util.ArrayDeque

/**
 * 文件管理器统一任务结果
 */
sealed interface FmResult<out T> {
    data class Ok<T>(val value: T) : FmResult<T>
    data class Failed(val error: Throwable) : FmResult<Nothing>
    data object Cancelled : FmResult<Nothing>
    data object Rejected : FmResult<Nothing>
}

private fun <T> RunResult<T>.toFmResult(): FmResult<T> = when (this) {
    is RunResult.Ok -> FmResult.Ok(value)
    is RunResult.Failed -> FmResult.Failed(error)
    RunResult.Cancelled -> FmResult.Cancelled
    is RunResult.Rejected -> FmResult.Rejected
}

private const val TAG_BROWSE = "FmBrowse"
private const val TAG_DELETE = "FmDelete"
private const val TAG_SEARCH = "FmSearch"

class FileManagerLogic(
    val scope: AccessScope,
    trashRoot: Path,
    cacheRoot: Path,
    private val taskManager: TaskManager
) {
    val tempWorkspace = TempWorkspace(cacheRoot)

    private val fileOps = FileOps(scope)
    private val trash = TrashManager(trashRoot, scope, fileOps)
    private val renameCreate = RenameCreateOps(scope)

    /**
     * 浏览 [currentDir] 目录内容，返回 [FmListResult]
     */
    suspend fun browse(currentDir: Path): FmResult<FmListResult> = taskManager.run(TaskKind.LIST) {
        doBrowse(currentDir)
    }.toFmResult()

    private suspend fun doBrowse(currentDir: Path): FmListResult = withContext(Dispatchers.IO) {
        val safeDir = try {
            scope.guard(currentDir)
        } catch (_: OutOfScopeException) {
            throw BrowseException("Current dir is out of scope")
        }
        if (!Files.isDirectory(safeDir)) throw BrowseException("Current dir is not a directory")
        if (!Files.isReadable(safeDir)) throw BrowseException("Current dir is not readable")

        val rootAbs = scope.rootAbs
        val entries = safeDir.toFile().listFiles()
            ?.mapNotNull { child ->
                val childPath = child.toPath()
                val attrs = runCatching {
                    Files.readAttributes(childPath, BasicFileAttributes::class.java, LinkOption.NOFOLLOW_LINKS)
                }.onFailure { e ->
                    FmLog.warn(TAG_BROWSE, "Failed to stat entry: ${child.name}", e)
                }.getOrNull() ?: return@mapNotNull null

                if (attrs.isSymbolicLink) {
                    FmLog.info(TAG_BROWSE, "Skip symlink in listing: ${child.name}")
                    return@mapNotNull null
                }
                childPath.toFmEntry(attrs)
            }
            ?: throw BrowseException("Failed to list directory")

        FmListResult(
            rootDir = rootAbs,
            currentDir = safeDir,
            ancestors = buildAncestors(rootAbs, safeDir),
            entries = entries,
            hasSubdirectory = entries.any { it.isDirectory },
            writable = Files.isWritable(safeDir)
        )
    }

    /**
     * 校验 [candidate] 是否为可访问范围内的可读目录
     * @return 归一化后的目录路径；不合法或越界时返回 null
     */
    fun validateTarget(candidate: Path): Path? = runCatching {
        scope.guard(candidate).takeIf { Files.isDirectory(it) && Files.isReadable(it) }
    }.getOrNull()

    private fun requireWritableOutput(target: Path): Path {
        val normalized = target.normalize().toAbsolutePath()
        if (scope.isUnder(normalized) || tempWorkspace.isInside(normalized)) return normalized
        FmLog.warn(TAG_BROWSE, "Denied write outside accessible scope or cache workspace.")
        throw OutOfScopeException("Output path is outside the accessible scope.")
    }

    /**
     * 解析初始访问目录
     * 目录合法且在根目录内时返回，否则回退到根目录
     */
    fun resolveInitialCurrent(hintPath: Path?): Path {
        val root = scope.rootAbs
        val normal = hintPath?.normalize()?.toAbsolutePath()
        return normal?.takeIf {
            scope.isUnder(it) && Files.isDirectory(it) && Files.isReadable(it)
        } ?: root
    }

    /**
     * 构建粘贴请求
     */
    fun buildPasteRequest(
        sources: List<Path>,
        targetDir: Path,
        mode: PasteMode
    ): PasteRequest = fileOps.buildPasteRequest(sources, targetDir, mode)

    /**
     * 执行粘贴
     */
    suspend fun executePaste(
        sources: List<Path>,
        targetDir: Path,
        mode: PasteMode,
        resolutions: List<ConflictResolution>
    ): FmResult<PasteSummary> =
        taskManager.run(TaskKind.valueOf(mode.name)) {
            val tracker = ByteRateTracker()
            tracker.start()
            fileOps.executePaste(sources, targetDir, mode, resolutions,
                onProgress = { c, t, n -> report(completed = c, total = t, currentName = n) },
                onBytes = { done, total ->
                    report(
                        bytesDone = done,
                        bytesTotal = total,
                        bytesPerSecond = if (total > 0) tracker.rate(done) else 0L
                    )
                }
            )
        }.toFmResult()

    /**
     * 删除一组条目
     * @param toTrash 是否移入回收站
     */
    suspend fun delete(
        targets: List<Path>,
        toTrash: Boolean
    ): FmResult<DeleteSummary> = taskManager.run(TaskKind.DELETE) {
        val ctx = currentCoroutineContext()
        val total = targets.size
        var completed = 0
        val results = mutableListOf<SingleDeleteResult>()
        for (raw in targets) {
            val target = raw.normalize().toAbsolutePath()
            val name = target.fileName?.toString()
            val result = try {
                scope.guardAbsolute(target)
                when {
                    !Files.exists(target, LinkOption.NOFOLLOW_LINKS) -> SingleDeleteResult(target, false, "Not exist")
                    toTrash -> {
                        val uuid = trash.moveIn(target) {
                            report(
                                completed = completed,
                                total = total,
                                currentName = it
                            )
                        }
                        FmLog.info(TAG_DELETE, "Moved to trash uuid=$uuid")
                        SingleDeleteResult(target, true, null)
                    }
                    else -> {
                        fileOps.deleteRecursive(target) { ctx.ensureActive() }
                        SingleDeleteResult(target, true, null)
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                FmLog.warn(TAG_DELETE, "Delete failed: $name", e)
                SingleDeleteResult(target, false, e.message ?: "Failed")
            }
            results += result
            completed++
            report(completed = completed, total = total, currentName = name)
        }
        DeleteSummary(toTrash, results)
    }.toFmResult()

    /**
     * 执行压缩
     * @param sources 待压缩条目
     * @param output 输出压缩包路径
     * @param options 压缩参数
     */
    suspend fun compress(
        sources: List<Path>,
        output: Path,
        options: CompressOptions
    ): FmResult<CompressSummary> = taskManager.run(TaskKind.COMPRESS) {
        val ctx = currentCoroutineContext()
        val tracker = ByteRateTracker().also { it.start() }
        // 待压缩条目必须位于可访问范围内
        // 输出允许范围内或文件管理器缓存临时区
        val safeSources = sources.map { scope.guardAbsolute(it) }
        safeSources.firstOrNull { !Files.exists(it, LinkOption.NOFOLLOW_LINKS) }
            ?.let { throw NoSuchFileException(it.toString()) }

        val safeOutput = requireWritableOutput(output)
        Compressor.compress(
            sources = safeSources,
            output = safeOutput,
            options = options,
            onProgress = { completed, total, currentName ->
                report(
                    completed = completed,
                    total = total,
                    currentName = currentName
                )
            },
            checkCancel = { !ctx.isActive },
            onBytes = { done, total ->
                report(
                    bytesDone = done,
                    bytesTotal = total,
                    bytesPerSecond = if (total > 0) {
                        tracker.rate(done)
                    } else {
                        0L
                    }
                )
            }
        )
    }.toFmResult()

    /**
     * 执行解压
     * @param archive 压缩包路径
     * @param outputDir 目标目录
     * @param options 解压选项
     */
    suspend fun extract(
        archive: Path,
        outputDir: Path,
        options: ExtractOptions
    ): FmResult<ExtractSummary> {
        val result = taskManager.run(TaskKind.EXTRACT) {
            val ctx = currentCoroutineContext()
            val tracker = ByteRateTracker().also { it.start() }
            // 压缩包必须位于可访问范围内
            // 输出允许范围内或文件管理器缓存临时区
            val safeArchive = scope.guardAbsolute(archive)
            if (!Files.exists(safeArchive, LinkOption.NOFOLLOW_LINKS)) {
                throw NoSuchFileException(safeArchive.toString())
            }

            val safeOutputDir = requireWritableOutput(outputDir)
            Extractor.extract(
                archive = safeArchive,
                outputDir = safeOutputDir,
                options = options,
                onProgress = { completed, total, currentName ->
                    report(
                        completed = completed,
                        total = total,
                        currentName = currentName
                    )
                },
                checkCancel = { !ctx.isActive },
                onBytes = { done, total ->
                    report(
                        bytesDone = done,
                        bytesTotal = total,
                        bytesPerSecond = if (total > 0) tracker.rate(done) else 0L
                    )
                }
            )
        }.toFmResult()
        // 密码缺失或错误时不包装进 FmResult.Failed，直接抛出
        val error = (result as? FmResult.Failed)?.error
        if (error is ArchivePasswordException) throw error
        return result
    }

    suspend fun trashList(): FmResult<List<TrashItem>> =
        taskManager.run(TaskKind.LIST) { trash.list() }.toFmResult()

    /**
     * 预检回收站恢复冲突
     * @return 冲突项列表，元素为 (item, 在 [items] 中的下标)
     */
    suspend fun trashRestoreConflicts(items: List<TrashItem>): List<Pair<TrashItem, Int>> =
        withContext(Dispatchers.IO) {
            val planned = HashSet<Path>()
            items.mapIndexedNotNull { index, item ->
                if (item.meta.corrupted) return@mapIndexedNotNull null
                val target = trashRestoreTarget(item) ?: return@mapIndexedNotNull null
                val exists = Files.exists(target, LinkOption.NOFOLLOW_LINKS) ||
                    !planned.add(target.normalize().toAbsolutePath())
                if (exists) item to index else null
            }
        }

    private fun trashRestoreTarget(item: TrashItem): Path? = runCatching {
        val original = scope.guard(Paths.get(item.meta.originalPath)).let { it.parent ?: it }
        original.resolve(item.meta.name)
    }.getOrNull()

    suspend fun trashRestore(
        items: List<TrashItem>,
        resolutions: Map<String, ConflictResolution>
    ): FmResult<List<RestoreResult>> = taskManager.run(TaskKind.TRASH_RESTORE) {
        val total = items.size
        var completed = 0
        val results = mutableListOf<RestoreResult>()
        for (item in items) {
            val rr = trash.restore(item, resolutions[item.uuid] ?: ConflictResolution.SKIP) {
                report(
                    completed = completed,
                    total = total,
                    currentName = it
                )
            }
            results += rr
            completed++
            report(
                completed = completed,
                total = total,
                currentName = item.meta.name
            )
        }
        results
    }.toFmResult()

    suspend fun trashPurge(items: List<TrashItem>): FmResult<Unit> = taskManager.run(TaskKind.TRASH_PURGE) {
        val total = items.size
        var completed = 0
        for (item in items) {
            trash.purge(item) {
                report(
                    completed = completed,
                    total = total,
                    currentName = it
                )
            }
            completed++
            report(
                completed = completed,
                total = total,
                currentName = item.meta.name
            )
        }
    }.toFmResult()

    suspend fun trashClear(): FmResult<Unit> = taskManager.run(TaskKind.TRASH_CLEAR) {
        trash.clear { completed, total, currentName ->
            report(
                completed = completed,
                total = total,
                currentName = currentName
            )
        }
    }.toFmResult()


    suspend fun rename(target: Path, newName: String): Path = renameCreate.rename(target, newName)
    suspend fun createFolder(parent: Path, name: String): Path = renameCreate.createFolder(parent, name)
    suspend fun createFile(parent: Path, name: String): Path = renameCreate.createFile(parent, name)


    /**
     * 搜索匹配项
     * @param startDir 起始目录
     * @param keyword 匹配关键词
     * @param caseSensitive 是否区分大小写
     * @param onProgress 进度回调（当前搜索到的目录）
     */
    suspend fun search(
        startDir: Path,
        keyword: String,
        caseSensitive: Boolean,
        onProgress: (currentDir: Path) -> Unit = {}
    ): SearchResult {
        val safeStart = try {
            scope.guard(startDir)
        } catch (_: OutOfScopeException) {
            return SearchResult.Failed(BrowseException("Start dir is out of scope"))
        }
        if (!Files.isDirectory(safeStart)) {
            return SearchResult.Failed(BrowseException("Start dir is not a directory"))
        }

        val results = mutableListOf<SearchHit>()
        val stack = ArrayDeque<Path>()
        stack.push(safeStart)

        while (stack.isNotEmpty()) {
            currentCoroutineContext().ensureActive()
            val dir = stack.pop()
            onProgress(dir)
            val children = runCatching {
                Files.newDirectoryStream(dir).use { stream -> stream.toList() }
            }.getOrElse { emptyList() }
            for (child in children) {
                currentCoroutineContext().ensureActive()
                val attrs = runCatching {
                    Files.readAttributes(child, BasicFileAttributes::class.java, LinkOption.NOFOLLOW_LINKS)
                }.getOrNull() ?: continue
                if (attrs.isSymbolicLink) {
                    FmLog.info(TAG_SEARCH, "Skip symlink during search: $child")
                    continue
                }
                val name = child.fileName?.toString() ?: continue
                if (name.contains(keyword, ignoreCase = !caseSensitive)) {
                    results += SearchHit(
                        path = child,
                        name = name,
                        isDirectory = attrs.isDirectory,
                        size = if (attrs.isRegularFile) attrs.size() else 0L,
                        modifiedMs = attrs.lastModifiedTime().toMillis(),
                        hidden = name.startsWith(".")
                    )
                }
                if (attrs.isDirectory) stack.push(child)
            }
        }
        return SearchResult.Ok(results)
    }

    private fun buildAncestors(root: Path, current: Path): List<Path> {
        if (current == root) return emptyList()
        return buildList {
            add(root)
            root.relativize(current).fold(root) { acc, seg ->
                acc.resolve(seg).also { if (it != current) add(it) }
            }
        }
    }
}

/**
 * 删除一项的结果
 */
data class SingleDeleteResult(
    val path: Path,
    val success: Boolean,
    val reason: String?
)

/**
 * 删除结果汇总
 */
data class DeleteSummary(
    val toTrash: Boolean,
    val results: List<SingleDeleteResult>
)

/**
 * 搜索结果命中条目
 */
data class SearchHit(
    val path: Path,
    val name: String,
    val isDirectory: Boolean,
    val size: Long,
    val modifiedMs: Long,
    val hidden: Boolean
)

/**
 * 搜索结果
 */
sealed interface SearchResult {
    data class Ok(val hits: List<SearchHit>) : SearchResult
    data class Failed(val error: Throwable) : SearchResult
}