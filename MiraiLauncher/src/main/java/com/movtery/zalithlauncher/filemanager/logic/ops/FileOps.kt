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

package com.movtery.zalithlauncher.filemanager.logic.ops

import com.movtery.zalithlauncher.filemanager.logic.AccessScope
import com.movtery.zalithlauncher.filemanager.logic.OutOfScopeException
import com.movtery.zalithlauncher.filemanager.os.FmLog
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.withContext
import java.io.IOException
import java.nio.file.FileVisitResult
import java.nio.file.Files
import java.nio.file.LinkOption
import java.nio.file.Path
import java.nio.file.SimpleFileVisitor
import java.nio.file.StandardCopyOption
import java.nio.file.attribute.BasicFileAttributes

private const val TAG = "FmFileOps"
private const val BUFFER_SIZE = 64 * 1024

/** 文件操作逻辑 */
class FileOps(private val scope: AccessScope) {
    /** 构建粘贴请求，并分析顶层项是否冲突 */
    fun buildPasteRequest(
        sources: List<Path>,
        targetDir: Path,
        mode: PasteMode
    ): PasteRequest {
        val safeTarget = scope.guard(targetDir)
        if (!Files.isDirectory(safeTarget)) {
            throw OutOfScopeException("Target is not a directory")
        }
        if (!Files.isWritable(safeTarget)) {
            throw IllegalStateException("Target dir is not writable")
        }
        // 源路径与目标路径相同、或目标位于源目录内部时视为无效操作
        for (src in sources) {
            val safeSource = src.normalize().toAbsolutePath()
            if (safeSource == safeTarget || safeTarget.startsWith(safeSource)) {
                throw IllegalStateException("Source and target overlap")
            }
        }
        val conflicts = sources.map { src ->
            val existing = safeTarget.resolve(src.fileName.toString())
            if (Files.exists(existing, LinkOption.NOFOLLOW_LINKS)) {
                ConflictItem(src.normalize().toAbsolutePath(), existing)
            } else null
        }
        return if (conflicts.any { it != null }) {
            PasteRequest.ResolveRequest(sources, safeTarget, mode, conflicts)
        } else {
            PasteRequest.Ready(sources, safeTarget, mode)
        }
    }

    suspend fun executePaste(
        sources: List<Path>,
        targetDir: Path,
        mode: PasteMode,
        resolutions: List<ConflictResolution>,
        onProgress: (completed: Int, total: Int, currentName: String?) -> Unit,
        onBytes: (bytesDone: Long, bytesTotal: Long) -> Unit = { _, _ -> }
    ): PasteSummary = withContext(Dispatchers.IO) {
        require(sources.size == resolutions.size) { "resolutions must align with sources" }

        val job = coroutineContext[Job]
        fun checkCancel() {
            if (job?.isActive == false) throw CancellationException()
        }

        val safeTarget = scope.guard(targetDir)
        val results = ArrayList<ItemResult>(sources.size)
        val total = sources.size
        val bytesTotal = totalBytes(sources)
        var completed = 0

        val reportBytes = { done: Long ->
            onBytes(done, bytesTotal)
        }

        sources.forEachIndexed { index, rawSource ->
            checkCancel()
            // 源不做可访问范围限制，仅归一化绝对路径
            val source = rawSource.normalize().toAbsolutePath()
            val name = source.fileName?.toString()
            if (name == null) {
                results += ItemResult(source, null, false, "Invalid source name")
                completed++
                onProgress(completed, total, null)
                return@forEachIndexed
            }

            //禁止把目录复制到自身或自身的子目录内，源与目标相同同样视为无效
            if (source == safeTarget || safeTarget.startsWith(source)) {
                results += ItemResult(source, null, false, "Cannot copy/move into itself or its own subdirectory")
                completed++
                onProgress(completed, total, name)
                return@forEachIndexed
            }

            val resolution = resolutions[index]
            val existing = safeTarget.resolve(name)

            try {
                val finalTarget: Path? = when {
                    !Files.exists(existing, LinkOption.NOFOLLOW_LINKS) -> {
                        copyOrMove(source, existing, mode, ::checkCancel, reportBytes)
                        existing
                    }
                    resolution == ConflictResolution.SKIP -> null
                    resolution == ConflictResolution.OVERWRITE -> {
                        overwriteOrMerge(source, existing, mode, ::checkCancel, reportBytes)
                        existing
                    }
                    resolution == ConflictResolution.KEEP_BOTH -> {
                        val keepName = nextKeepBothName(safeTarget, name)
                        val keepTarget = safeTarget.resolve(keepName)
                        copyOrMove(source, keepTarget, mode, ::checkCancel, reportBytes)
                        keepTarget
                    }
                    else -> error("Unexpected resolution")
                }
                results += ItemResult(source, finalTarget, true, null)
            } catch (e: Exception) {
                FmLog.warn(TAG, "Paste item failed: $name", e)
                results += ItemResult(source, null, false, e.message ?: "Failed")
            }

            completed++
            onProgress(completed, total, name)
        }

        PasteSummary(mode, safeTarget, results)
    }

    private suspend fun copyOrMove(
        source: Path,
        target: Path,
        mode: PasteMode,
        checkCancel: () -> Unit,
        onBytes: (Long) -> Unit = {}
    ) {
        if (Files.isSymbolicLink(source)) {
            FmLog.info(TAG, "Skip symlink during copy/move: $source")
            return
        }
        when (mode) {
            PasteMode.COPY -> copyTree(source, target, checkCancel, onBytes = onBytes)
            PasteMode.MOVE -> moveTree(source, target, checkCancel, onBytes)
        }
    }

    /** 复制目录树 */
    suspend fun copyTree(
        source: Path,
        target: Path,
        checkCancel: () -> Unit = {},
        applyPermissions: Boolean = true,
        onBytes: (Long) -> Unit = {}
    ) = withContext(Dispatchers.IO) {
        val attrs = Files.readAttributes(
            source,
            BasicFileAttributes::class.java,
            LinkOption.NOFOLLOW_LINKS
        )

        if (attrs.isRegularFile) {
            copyFileWithProgress(
                source = source,
                target = target,
                checkCancel = checkCancel,
                onBytes = onBytes
            )
            if (applyPermissions) FilePermissions.apply(target)
        } else if (attrs.isDirectory) {
            Files.walkFileTree(source, emptySet(), Int.MAX_VALUE, object : SimpleFileVisitor<Path>() {
                override fun preVisitDirectory(dir: Path, attrs: BasicFileAttributes): FileVisitResult {
                    checkCancel()
                    if (attrs.isSymbolicLink) return FileVisitResult.SKIP_SUBTREE
                    Files.createDirectories(target.resolve(source.relativize(dir)))
                    return FileVisitResult.CONTINUE
                }

                override fun visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult {
                    checkCancel()
                    if (attrs.isSymbolicLink) {
                        FmLog.info(TAG, "Skip symlink in dir copy: $file")
                        return FileVisitResult.CONTINUE
                    }
                    copyFileWithProgress(
                        source = file,
                        target = target.resolve(source.relativize(file)),
                        checkCancel = checkCancel,
                        onBytes = onBytes
                    )
                    return FileVisitResult.CONTINUE
                }
            })

            if (applyPermissions) {
                FilePermissions.apply(target)
            }
        }
    }

    private fun copyFileWithProgress(
        source: Path,
        target: Path,
        checkCancel: () -> Unit,
        onBytes: (Long) -> Unit
    ) {
        var done = 0L
        Files.newInputStream(source).use { input ->
            Files.newOutputStream(target).use { output ->
                val buffer = ByteArray(BUFFER_SIZE)
                while (true) {
                    checkCancel()
                    val n = input.read(buffer)
                    if (n < 0) break
                    output.write(buffer, 0, n)
                    done += n
                    onBytes(done)
                }
            }
        }
        runCatching {
            Files.setLastModifiedTime(target, Files.getLastModifiedTime(source))
        }
    }

    private suspend fun moveTree(
        source: Path,
        target: Path,
        checkCancel: () -> Unit = {},
        onBytes: (Long) -> Unit = {}
    ) {
        try {
            withContext(Dispatchers.IO) {
                Files.move(source, target, StandardCopyOption.ATOMIC_MOVE)
            }
            return
        } catch (_: Exception) {
        }

        copyTree(source, target, checkCancel, onBytes = onBytes)
        if (Files.exists(target, LinkOption.NOFOLLOW_LINKS)) deleteRecursive(source, checkCancel)
    }

    private suspend fun overwriteOrMerge(
        source: Path,
        existing: Path,
        mode: PasteMode,
        checkCancel: () -> Unit,
        onBytes: (Long) -> Unit = {}
    ) = withContext(Dispatchers.IO) {
        val srcAttrs = Files.readAttributes(
            source,
            BasicFileAttributes::class.java,
            LinkOption.NOFOLLOW_LINKS
        )
        if (srcAttrs.isRegularFile) {
            Files.copy(source, existing, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES)
            FilePermissions.apply(existing)
            if (mode == PasteMode.MOVE) Files.delete(source)
            return@withContext
        }
        if (srcAttrs.isDirectory) {
            Files.createDirectories(existing)
            FilePermissions.apply(existing)
            copyTree(source, existing, checkCancel, onBytes = onBytes)
            if (mode == PasteMode.MOVE) deleteRecursive(source, checkCancel)
        }
    }

    fun nextKeepBothName(targetDir: Path, originalName: String): String {
        val dot = originalName.lastIndexOf('.')
        val base: String
        val ext: String
        if (dot > 0 && dot != originalName.length - 1) {
            base = originalName.substring(0, dot)
            ext = originalName.substring(dot)
        } else {
            base = originalName
            ext = ""
        }
        var n = 1
        while (true) {
            val candidate = if (ext.isEmpty()) {
                "$base ($n)"
            } else {
                "$base ($n)$ext"
            }

            if (!Files.exists(targetDir.resolve(candidate), LinkOption.NOFOLLOW_LINKS)) {
                return candidate
            }
            n++
        }
    }

    suspend fun totalBytes(sources: List<Path>): Long {
        var total = 0L
        for (source in sources) {
            total += sizeOf(source)
        }
        return total
    }

    private suspend fun sizeOf(path: Path): Long = withContext(Dispatchers.IO) {
        val attrs = runCatching {
            Files.readAttributes(
                path,
                BasicFileAttributes::class.java,
                LinkOption.NOFOLLOW_LINKS
            )
        }.getOrNull() ?: return@withContext 0L

        if (attrs.isSymbolicLink) return@withContext 0L
        if (!attrs.isDirectory) return@withContext attrs.size()

        var size = 0L
        runCatching {
            Files.walkFileTree(path, emptySet(), Int.MAX_VALUE, object : SimpleFileVisitor<Path>() {
                override fun visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult {
                    if (attrs.isRegularFile) size += attrs.size()
                    return FileVisitResult.CONTINUE
                }
            })
        }
        size
    }

    suspend fun deleteRecursive(
        path: Path,
        checkCancel: () -> Unit = {}
    ) {
        deleteRecursivePath(path, checkCancel)
    }
}

/** 递归删除路径 */
suspend fun deleteRecursivePath(path: Path, checkCancel: () -> Unit = {}) = withContext(Dispatchers.IO) {
    if (Files.isSymbolicLink(path)) {
        runCatching {
            Files.delete(path)
        }
        return@withContext
    }

    val attrs = runCatching {
        Files.readAttributes(path, BasicFileAttributes::class.java, LinkOption.NOFOLLOW_LINKS)
    }.getOrNull() ?: return@withContext

    if (attrs.isDirectory) {
        Files.walkFileTree(path, emptySet(), Int.MAX_VALUE, object : SimpleFileVisitor<Path>() {
            override fun visitFileFailed(file: Path, exc: IOException): FileVisitResult {
                runCatching { Files.delete(file) }
                return FileVisitResult.CONTINUE
            }

            override fun visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult {
                checkCancel()
                runCatching { Files.delete(file) }
                return FileVisitResult.CONTINUE
            }

            override fun postVisitDirectory(dir: Path, exc: IOException?): FileVisitResult {
                runCatching { Files.delete(dir) }
                return FileVisitResult.CONTINUE
            }
        })
    } else {
        runCatching {
            Files.delete(path)
        }
    }
}