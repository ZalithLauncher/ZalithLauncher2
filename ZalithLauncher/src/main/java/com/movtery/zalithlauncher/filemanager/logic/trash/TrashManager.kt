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

package com.movtery.zalithlauncher.filemanager.logic.trash

import com.movtery.zalithlauncher.filemanager.logic.AccessScope
import com.movtery.zalithlauncher.filemanager.logic.ops.ConflictResolution
import com.movtery.zalithlauncher.filemanager.logic.ops.FileOps
import com.movtery.zalithlauncher.filemanager.os.FmLog
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import java.io.IOException
import java.nio.file.FileVisitResult
import java.nio.file.Files
import java.nio.file.LinkOption
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.SimpleFileVisitor
import java.nio.file.StandardCopyOption
import java.nio.file.attribute.BasicFileAttributes
import java.util.UUID

private const val TAG = "FmTrash"
private const val CONTENT_DIR = "content"
private const val META_FILE = "meta.json"

/**
 * 回收站管理器
 * @param trashRoot 回收站根目录
 */
class TrashManager(
    val trashRoot: Path,
    private val scope: AccessScope,
    private val fileOps: FileOps = FileOps(scope)
) {
    init {
        Files.createDirectories(trashRoot)
    }

    /**
     * 将一项移入回收站
     * @param original 原始条目的绝对路径
     * @return 成功时返回新生成的 UUID
     */
    suspend fun moveIn(
        original: Path,
        onProgress: (name: String?) -> Unit
    ): String = withContext(Dispatchers.IO) {
        val checkCancel = { coroutineContext.ensureActive() }

        val source = scope.guard(original)
        if (!Files.exists(source, LinkOption.NOFOLLOW_LINKS)) {
            throw IOException("Source does not exist: $source")
        }
        val name = source.fileName?.toString() ?: throw IOException("Source has no filename")
        val isFolder = Files.readAttributes(
            source,
            BasicFileAttributes::class.java,
            LinkOption.NOFOLLOW_LINKS
        ).isDirectory

        // UUID 目录规避同名条目多次删除
        val uuid = generateSequence { UUID.randomUUID().toString() }
            .onEach { checkCancel() }
            .first { !Files.exists(trashRoot.resolve(it), LinkOption.NOFOLLOW_LINKS) }
        val entryDir = trashRoot.resolve(uuid)
        val contentDir = entryDir.resolve(CONTENT_DIR)
        Files.createDirectories(contentDir)
        onProgress(name)

        moveAcrossFs(
            source = source,
            target = contentDir.resolve(name),
            checkCancel = checkCancel
        )

        // 写 meta.json
        writeMeta(entryDir, TrashMeta(
            originalPath = source.normalize().toAbsolutePath().toString(),
            deletedAt = System.currentTimeMillis(),
            name = name,
            isFolder = isFolder
        ))
        uuid
    }

    /**
     * 列出回收站全部条目
     */
    suspend fun list(): List<TrashItem> = withContext(Dispatchers.IO) {
        Files.newDirectoryStream(trashRoot).use { stream ->
            stream.mapNotNull { entryDir ->
                if (Files.isDirectory(entryDir, LinkOption.NOFOLLOW_LINKS)) readItem(entryDir) else null
            }
        }
    }

    /**
     * 恢复一项到原始位置
     * @return 目标路径（成功时）或失败原因
     */
    suspend fun restore(
        item: TrashItem,
        resolution: ConflictResolution,
        onProgress: (name: String?) -> Unit
    ): RestoreResult = withContext(Dispatchers.IO) {
        val checkCancel = { coroutineContext.ensureActive() }
        if (item.meta.corrupted) {
            return@withContext RestoreResult.Failed(item, "Corrupted trash entry, cannot restore")
        }
        val original = runCatching {
            scope.guard(Paths.get(item.meta.originalPath)).let { it.parent ?: it }
        }.getOrNull() ?: return@withContext RestoreResult.Failed(item, "Original path out of scope")
        if (!Files.isDirectory(original)) Files.createDirectories(original)

        val name = item.meta.name
        onProgress(name)
        val initialTarget = original.resolve(name)
        val target = when {
            !Files.exists(initialTarget, LinkOption.NOFOLLOW_LINKS) -> initialTarget
            resolution == ConflictResolution.SKIP -> return@withContext RestoreResult.Skipped(item, initialTarget)
            resolution == ConflictResolution.OVERWRITE -> initialTarget.also { fileOps.deleteRecursive(it, checkCancel) }
            else -> original.resolve(fileOps.nextKeepBothName(original, name))
        }
        try {
            moveAcrossFs(item.contentDir.resolve(name), target, checkCancel)
            // 清理 UUID 目录
            fileOps.deleteRecursive(item.contentDir.parent ?: trashRoot, checkCancel)
            RestoreResult.Ok(item, target)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            FmLog.warn(TAG, "Restore failed: ${item.uuid}", e)
            RestoreResult.Failed(item, e.message ?: "Restore failed")
        }
    }

    /**
     * 彻底删除一项
     */
    suspend fun purge(
        item: TrashItem,
        onProgress: (name: String?) -> Unit
    ) = withContext(Dispatchers.IO) {
        val checkCancel = { coroutineContext.ensureActive() }
        onProgress(item.meta.name)
        val entryDir = item.contentDir.parent ?: return@withContext
        // 先删 content 再删 UUID 目录
        fileOps.deleteRecursive(item.contentDir, checkCancel)
        fileOps.deleteRecursive(entryDir, checkCancel)
    }

    /**
     * 清空整个回收站。
     */
    suspend fun clear(
        onProgress: (current: Int, total: Int, name: String?) -> Unit
    ) = withContext(Dispatchers.IO) {
        val checkCancel = { coroutineContext.ensureActive() }
        val items = Files.newDirectoryStream(trashRoot).use { it.toList() }
        items.forEachIndexed { i, entry ->
            checkCancel()
            onProgress(i, items.size, entry.fileName?.toString())
            fileOps.deleteRecursive(entry, checkCancel)
        }
    }

    private fun readItem(entryDir: Path): TrashItem? {
        val uuid = entryDir.fileName?.toString() ?: return null

        val contentDir = entryDir.resolve(CONTENT_DIR)
        val meta = readMeta(entryDir)
        if (meta != null && Files.isDirectory(contentDir, LinkOption.NOFOLLOW_LINKS) && !isDirEmpty(contentDir)) {
            return TrashItem(uuid, meta, contentDir, computeSize(contentDir))
        }

        // 损坏条目
        val fallback = meta ?: TrashMeta(
            originalPath = entryDir.toString(),
            deletedAt = runCatching { Files.getLastModifiedTime(entryDir).toMillis() }.getOrDefault(0L),
            name = pickFirstChildName(contentDir) ?: uuid,
            isFolder = false
        )
        return TrashItem(uuid, fallback.copy(corrupted = true), contentDir, 0L)
    }

    private fun pickFirstChildName(contentDir: Path): String? = runCatching {
        Files.newDirectoryStream(contentDir).use { stream ->
            stream.firstOrNull()?.fileName?.toString()
        }
    }.getOrNull()

    private fun isDirEmpty(dir: Path): Boolean = runCatching {
        Files.newDirectoryStream(dir).use { stream -> !stream.iterator().hasNext() }
    }.getOrDefault(true)

    private fun computeSize(contentDir: Path): Long {
        var total = 0L
        Files.walkFileTree(contentDir, object : SimpleFileVisitor<Path>() {
            override fun visitFile(file: Path, attrs: BasicFileAttributes) = FileVisitResult.CONTINUE.also {
                if (attrs.isRegularFile) total += attrs.size()
            }
        })
        return total
    }

    private fun writeMeta(entryDir: Path, meta: TrashMeta) {
        Files.write(entryDir.resolve(META_FILE), meta.toJson().toString().toByteArray(Charsets.UTF_8))
    }

    private fun readMeta(entryDir: Path): TrashMeta? {
        val file = entryDir.resolve(META_FILE)
        if (!Files.exists(file)) return null
        return TrashMeta.fromJson(Files.readAllBytes(file).toString(Charsets.UTF_8))
    }

    private suspend fun moveAcrossFs(
        source: Path,
        target: Path,
        checkCancel: () -> Unit
    ) = withContext(Dispatchers.IO) {
        // 同分区优先原子移动；失败回退为复制 + 删除
        if (
            runCatching {
                Files.move(source, target, StandardCopyOption.ATOMIC_MOVE)
            }.isSuccess
        ) return@withContext

        Files.createDirectories(target.parent ?: trashRoot)
        fileOps.copyTree(source, target, checkCancel, applyPermissions = false)
        fileOps.deleteRecursive(source, checkCancel)
    }
}

/** 恢复操作的执行结果 */
sealed interface RestoreResult {
    data class Ok(val item: TrashItem, val target: Path) : RestoreResult
    data class Skipped(val item: TrashItem, val target: Path) : RestoreResult
    data class Failed(val item: TrashItem, val reason: String) : RestoreResult
}
