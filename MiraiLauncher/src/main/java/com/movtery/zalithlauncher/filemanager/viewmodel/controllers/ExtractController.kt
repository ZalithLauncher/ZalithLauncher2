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

package com.movtery.zalithlauncher.filemanager.viewmodel.controllers

import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.filemanager.events.FileManagerEvent
import com.movtery.zalithlauncher.filemanager.logic.AccessScope
import com.movtery.zalithlauncher.filemanager.logic.FileManagerLogic
import com.movtery.zalithlauncher.filemanager.logic.FmResult
import com.movtery.zalithlauncher.filemanager.logic.entry.ArchiveType
import com.movtery.zalithlauncher.filemanager.logic.entry.FmEntry
import com.movtery.zalithlauncher.filemanager.logic.extract.ArchivePasswordException
import com.movtery.zalithlauncher.filemanager.logic.extract.ExtractOptions
import com.movtery.zalithlauncher.filemanager.logic.extract.Extractor
import com.movtery.zalithlauncher.filemanager.logic.ops.ConflictResolution
import com.movtery.zalithlauncher.filemanager.logic.ops.FileOps
import com.movtery.zalithlauncher.filemanager.logic.ops.FilePermissions
import com.movtery.zalithlauncher.filemanager.os.FmLog
import com.movtery.zalithlauncher.filemanager.viewmodel.DialogIntent
import com.movtery.zalithlauncher.filemanager.viewmodel.FmSnackbar
import com.movtery.zalithlauncher.filemanager.viewmodel.FmStateStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.apache.commons.compress.PasswordRequiredException
import java.nio.file.Files
import java.nio.file.LinkOption
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.nio.file.attribute.BasicFileAttributes

private const val TAG = "FmExtract"

/** 解压控制器：解压设置、密码、输出位置选择、冲突决策与执行。 */
class ExtractController(
    private val context: Context,
    private val logic: FileManagerLogic,
    private val scope: AccessScope,
    private val store: FmStateStore,
    private val browse: BrowseController,
    private val coroutineScope: CoroutineScope
) {
    /** 暂存待执行的解压请求（缓存） */
    private var pendingExtract: PendingExtract? = null
    /** 解压目标（缓存） */
    private var pendingExtractOutputTarget: OutputTarget? = null

    fun showExtract(entry: FmEntry) {
        store.dismissDialog()
        pendingExtract = PendingExtract(
            archivePath = entry.path,
            archiveName = entry.name,
            options = ExtractOptions()
        )
        store.updateState {
            it.copy(
                dialogIntent = DialogIntent.ExtractSetup(archivePath = entry.path, archiveName = entry.name)
            )
        }
    }

    /** 解压设置确认 */
    fun onExtractSetupConfirmed(independentFolder: Boolean) {
        val pending = pendingExtract ?: return
        pendingExtract = pending.copy(options = pending.options.copy(independentFolder = independentFolder))
        store.updateState { it.copy(dialogIntent = DialogIntent.ExtractOutputChoice) }
    }

    /** 解压密码确认 */
    fun onExtractPasswordConfirmed(password: String) {
        val target = pendingExtractOutputTarget ?: run {
            store.dismissDialog()
            return
        }

        val pending = pendingExtract ?: run {
            store.dismissDialog()
            return
        }

        store.dismissDialog()
        val withPassword = pending.copy(
            options = pending.options.copy(password = password.takeIf { it.isNotEmpty() })
        )
        pendingExtract = withPassword
        coroutineScope.launch(Dispatchers.IO) { executeExtract(target, withPassword) }
    }

    /** 在当前目录解压 */
    fun onExtractOutputChoiceCurrent() {
        val pending = pendingExtract ?: run {
            store.dismissDialog()
            return
        }

        val target = OutputTarget.Local(store.history.currentPath)
        coroutineScope.launch(Dispatchers.IO) {
            stageExtractOrExecute(target, pending)
        }
    }

    /** 通过 SAF 选择输出目录 */
    fun onExtractOutputChoiceSaf() {
        store.updateState {
            it.copy(dialogIntent = DialogIntent.ExtractOutputPick)
        }
    }

    /** SAF 选定输出目录 */
    fun onExtractOutputPicked(treeUri: Uri) {
        val pending = pendingExtract ?: run {
            store.dismissDialog()
            return
        }

        coroutineScope.launch(Dispatchers.IO) {
            stageExtractOrExecute(OutputTarget.Saf(treeUri), pending)
        }
    }

    /** 解压输出目录选择取消 */
    fun onExtractOutputPickedCancelled() {
        pendingExtract = null
        store.dismissDialog()
    }

    /** 解压冲突决策 */
    fun resolveExtractConflict(resolution: ConflictResolution) {
        val target = pendingExtractOutputTarget ?: run {
            store.dismissDialog()
            return
        }

        val pending = pendingExtract ?: run {
            store.dismissDialog()
            return
        }

        store.dismissDialog()
        when (resolution) {
            ConflictResolution.SKIP -> {
                pendingExtract = null
                pendingExtractOutputTarget = null
            }
            ConflictResolution.OVERWRITE -> {
                val updated = pending.copy(overwrite = true, keepBoth = false)
                pendingExtract = updated
                coroutineScope.launch(Dispatchers.IO) {
                    executeExtract(target, updated)
                }
            }
            ConflictResolution.KEEP_BOTH -> {
                val updated = pending.copy(overwrite = false, keepBoth = true)
                pendingExtract = updated
                coroutineScope.launch(Dispatchers.IO) {
                    executeExtract(target, updated)
                }
            }
        }
    }

    private suspend fun executeExtract(target: OutputTarget, pending: PendingExtract) {
        store.dismissDialog()

        val tempDir = logic.tempWorkspace.extractTempDir()
        var keepPendingForPassword = false
        try {
            when (val r = logic.extract(pending.archivePath, tempDir, pending.options)) {
                is FmResult.Ok -> {
                    val finalTarget = if (pending.keepBoth && pending.options.independentFolder) {
                        nextKeepBothExtractTarget(target)
                    } else {
                        // 直接解压：KEEP_BOTH 在写入时对冲突顶层条目逐个改名，不触碰目标目录本身
                        target
                    }
                    writeExtractToTarget(
                        target = finalTarget,
                        tempDir = tempDir,
                        overwrite = pending.overwrite,
                        keepBoth = pending.keepBoth,
                        independentFolder = pending.options.independentFolder
                    )
                    if (finalTarget is OutputTarget.Local) {
                        browse.notifyFileChanged(
                            FileManagerEvent(
                                FileManagerEvent.Type.EXTRACT,
                                listOf(finalTarget.dir.toString())
                            )
                        )
                    }
                }
                is FmResult.Failed -> {
                    store.emitSnackbar(FmSnackbar(store.fileOpErrorText(r.error, R.string.fm_error_extract_failed)))
                    browse.refreshCurrentDir()
                }
                FmResult.Rejected -> store.emitSnackbar(FmSnackbar(store.stringResolver(R.string.fm_task_busy)))
                FmResult.Cancelled -> {}
            }
        } catch (e: Exception) {
            FmLog.warn(TAG, "Extract failed", e)
            if (isPasswordError(e)) {
                // 密码错误或缺失
                // 保留目标与请求，弹密码输入对话框
                keepPendingForPassword = true
                pendingExtract = pending
                pendingExtractOutputTarget = target
                val errorText = when ((e as? ArchivePasswordException)?.type) {
                    ArchivePasswordException.Type.REQUIRED -> store.stringResolver(R.string.fm_extract_password_required)
                    ArchivePasswordException.Type.WRONG -> store.stringResolver(R.string.fm_extract_password_wrong)
                    null -> store.stringResolver(R.string.fm_extract_password_error)
                }
                store.updateState {
                    it.copy(dialogIntent = DialogIntent.ExtractPassword(errorText = errorText))
                }
            } else {
                store.emitSnackbar(FmSnackbar(store.fileOpErrorText(e, R.string.fm_error_extract_failed)))
                browse.refreshCurrentDir()
            }
        } finally {
            // 半成品清理
            logic.tempWorkspace.delete(tempDir)
            if (!keepPendingForPassword) {
                pendingExtract = null
                pendingExtractOutputTarget = null
            }
        }
    }

    private fun extractFinalDir(base: OutputTarget, pending: PendingExtract): OutputTarget {
        return if (pending.options.independentFolder) {
            val folderName = stripArchiveSuffix(pending.archiveName)
            when (base) {
                is OutputTarget.Local -> OutputTarget.Local(base.dir.resolve(folderName))
                is OutputTarget.Saf -> OutputTarget.SafDir(base.treeUri, folderName)
                is OutputTarget.SafDir -> base
            }
        } else {
            base
        }
    }

    private fun stripArchiveSuffix(name: String): String {
        for (ext in ArchiveType.knownSuffix()) {
            if (name.lowercase().endsWith(ext)) {
                return name.substring(0, name.length - ext.length)
            }
        }
        return name
    }

    /** 校验目标冲突（无冲突则直接执行解压） */
    private suspend fun stageExtractOrExecute(targetBase: OutputTarget, pending: PendingExtract) {
        val finalTarget = extractFinalDir(targetBase, pending)
        val conflictName = findExtractConflict(finalTarget, pending)
        if (conflictName != null) {
            pendingExtractOutputTarget = finalTarget
            store.updateState {
                it.copy(dialogIntent = DialogIntent.ExtractConflict(conflictName))
            }
        } else {
            pendingExtract = pending
            executeExtract(finalTarget, pending)
        }
    }

    private suspend fun findExtractConflict(target: OutputTarget, pending: PendingExtract): String? {
        if (pending.options.independentFolder) {
            return when (target) {
                is OutputTarget.Local ->
                    if (Files.exists(target.dir, LinkOption.NOFOLLOW_LINKS)) {
                        target.dir.fileName?.toString()
                    } else null
                is OutputTarget.SafDir ->
                    if (findChildDocument(context, target.treeUri, target.name) != null) {
                        target.name
                    } else null
                else -> null
            }
        }
        // 直接解压到目标目录
        // 检查压缩包顶层条目是否与目标目录已有内容冲突
        val topLevels = Extractor.topLevelNames(pending.archivePath)
        return topLevels.firstOrNull { name ->
            when (target) {
                is OutputTarget.Local -> {
                    Files.exists(target.dir.resolve(name), LinkOption.NOFOLLOW_LINKS)
                }
                is OutputTarget.Saf -> {
                    findChildDocument(context, target.treeUri, name) != null
                }
                else -> false
            }
        }
    }

    private fun nextKeepBothExtractTarget(target: OutputTarget): OutputTarget {
        if (target is OutputTarget.Local) {
            val parent = target.dir.parent ?: return target
            val base = target.dir.fileName?.toString() ?: return target
            var n = 1
            while (true) {
                val candidate = parent.resolve("$base ($n)")
                if (!Files.exists(candidate, LinkOption.NOFOLLOW_LINKS)) return OutputTarget.Local(candidate)
                n++
            }
        }
        if (target is OutputTarget.SafDir) {
            var n = 1
            while (true) {
                val candidate = "${target.name} ($n)"
                if (findChildDocument(context, target.treeUri, candidate) == null) return OutputTarget.SafDir(target.treeUri, candidate)
                n++
            }
        }
        return target
    }

    private suspend fun writeExtractToTarget(
        target: OutputTarget,
        tempDir: Path,
        overwrite: Boolean,
        keepBoth: Boolean,
        independentFolder: Boolean
    ) {
        withContext(Dispatchers.IO) {
            Files.createDirectories(tempDir)
        }
        when (target) {
            is OutputTarget.Local -> {
                if (overwrite && independentFolder) {
                    // 独立文件夹：覆盖时整体删除该文件夹
                    runCatching { Files.deleteIfExists(target.dir) }
                } // 直接解压：目标为当前目录，仅按条目覆盖
                withContext(Dispatchers.IO) {
                    Files.createDirectories(target.dir)
                }
                moveChildren(tempDir, target.dir, keepBoth)
                FilePermissions.apply(target.dir)
            }
            is OutputTarget.Saf -> {
                runCatching {
                    withContext(Dispatchers.IO) {
                        Files.newDirectoryStream(tempDir).use {
                            for (child in it) {
                                copyTreeToSaf(target.treeUri, child, overwrite, keepBoth)
                            }
                        }
                    }
                }
            }
            is OutputTarget.SafDir -> {
                val parentDocId = DocumentsContract.getTreeDocumentId(target.treeUri)
                val parentUri = DocumentsContract.buildDocumentUriUsingTree(target.treeUri, parentDocId)
                if (overwrite) {
                    findChildDocument(context, target.treeUri, target.name)?.let {
                        runCatching {
                            DocumentsContract.deleteDocument(context.contentResolver, it)
                        }
                    }
                }
                val dirDoc = DocumentsContract.createDocument(
                    context.contentResolver, parentUri, "vnd.android.document/directory", target.name
                ) ?: throw IllegalStateException("Failed to create directory")

                runCatching {
                    withContext(Dispatchers.IO) {
                        Files.newDirectoryStream(tempDir).use {
                            for (child in it) {
                                copyTreeToSafDir(dirDoc, child)
                            }
                        }
                    }
                }
            }
        }
    }

    private suspend fun moveChildren(src: Path, dest: Path, keepBoth: Boolean) = withContext(Dispatchers.IO) {
        Files.newDirectoryStream(src).use { stream ->
            for (child in stream) {
                val name = child.fileName.toString()
                val plainTarget = dest.resolve(name)
                val target = if (keepBoth && Files.exists(plainTarget, LinkOption.NOFOLLOW_LINKS)) {
                    dest.resolve(nextKeepBothEntryName(dest, name))
                } else {
                    plainTarget
                }
                runCatching { Files.move(child, target, StandardCopyOption.REPLACE_EXISTING) }
                    .getOrElse { e ->
                        FmLog.warn(TAG, "Move failed, fallback copy: $child", e)
                        runCatching {
                            copyTreeLocal(child, target)
                            FileOps(scope).deleteRecursive(child)
                        }
                    }
            }
        }
    }

    private fun nextKeepBothEntryName(dir: Path, name: String): String {
        val dot = name.lastIndexOf('.')
        val base = if (dot > 0) name.substring(0, dot) else name
        val ext = if (dot > 0) name.substring(dot) else ""
        var n = 1
        while (true) {
            val candidate = if (ext.isEmpty()) "$base ($n)" else "$base ($n)$ext"
            if (!Files.exists(dir.resolve(candidate), LinkOption.NOFOLLOW_LINKS)) return candidate
            n++
        }
    }

    private suspend fun copyTreeLocal(src: Path, dest: Path) {
        withContext(Dispatchers.IO) {
            val attrs = Files.readAttributes(src, BasicFileAttributes::class.java, LinkOption.NOFOLLOW_LINKS)
            if (attrs.isDirectory) {
                Files.createDirectories(dest)
                Files.newDirectoryStream(src).use { stream ->
                    for (child in stream) {
                        copyTreeLocal(child, dest.resolve(child.fileName.toString()))
                    }
                }
            } else {
                Files.copy(src, dest, StandardCopyOption.REPLACE_EXISTING)
                FilePermissions.apply(dest)
            }
        }
    }

    private suspend fun copyTreeToSaf(
        treeUri: Uri,
        src: Path,
        overwrite: Boolean,
        keepBoth: Boolean
    ) = withContext(Dispatchers.IO) {
        val docId = DocumentsContract.getTreeDocumentId(treeUri)
        val parentUri = DocumentsContract.buildDocumentUriUsingTree(treeUri, docId)
        val attrs = Files.readAttributes(src, BasicFileAttributes::class.java, LinkOption.NOFOLLOW_LINKS)
        val rawName = src.fileName?.toString() ?: return@withContext
        val name = if (keepBoth && findChildDocument(context, treeUri, rawName) != null) {
            nextKeepBothSafName(treeUri, rawName)
        } else {
            rawName
        }
        if (attrs.isDirectory) {
            val existing = findChildDocument(context, treeUri, name)
            if (overwrite && existing != null) {
                runCatching {
                    DocumentsContract.deleteDocument(context.contentResolver, existing)
                }
            }
            val dirDoc = DocumentsContract.createDocument(
                context.contentResolver, parentUri, "vnd.android.document/directory", name
            ) ?: return@withContext
            Files.newDirectoryStream(src).use { stream ->
                for (child in stream) copyTreeToSafDir(dirDoc, child)
            }
        } else {
            if (overwrite) {
                findChildDocument(context, treeUri, name)?.let {
                    runCatching {
                        DocumentsContract.deleteDocument(context.contentResolver, it)
                    }
                }
            }
            val newDoc = DocumentsContract.createDocument(
                context.contentResolver, parentUri, guessMime(name), name
            ) ?: return@withContext
            val out = context.contentResolver.openOutputStream(newDoc, "wt") ?: return@withContext
            out.use { output ->
                Files.newInputStream(src).use { input ->
                    input.copyTo(output)
                }
            }
        }
    }

    private fun nextKeepBothSafName(treeUri: Uri, name: String): String {
        val dot = name.lastIndexOf('.')
        val base = if (dot > 0) name.substring(0, dot) else name
        val ext = if (dot > 0) name.substring(dot) else ""
        var n = 1
        while (true) {
            val candidate = if (ext.isEmpty()) "$base ($n)" else "$base ($n)$ext"
            if (findChildDocument(context, treeUri, candidate) == null) return candidate
            n++
        }
    }

    private suspend fun copyTreeToSafDir(dirDoc: Uri, src: Path) {
        withContext(Dispatchers.IO) {
            val attrs = Files.readAttributes(src, BasicFileAttributes::class.java, LinkOption.NOFOLLOW_LINKS)
            val name = src.fileName?.toString() ?: return@withContext
            if (attrs.isDirectory) {
                val childDir = DocumentsContract.createDocument(
                    context.contentResolver, dirDoc, "vnd.android.document/directory", name
                ) ?: return@withContext
                Files.newDirectoryStream(src).use { stream ->
                    for (child in stream) copyTreeToSafDir(childDir, child)
                }
            } else {
                val newDoc = DocumentsContract.createDocument(
                    context.contentResolver, dirDoc, guessMime(name), name
                ) ?: return@withContext
                val out = context.contentResolver.openOutputStream(newDoc, "wt") ?: return@withContext
                out.use { output ->
                    Files.newInputStream(src).use { input ->
                        input.copyTo(output)
                    }
                }
            }
        }
    }

    private fun isPasswordError(e: Exception): Boolean {
        if (e is ArchivePasswordException) return true
        if (e is PasswordRequiredException) return true
        if (e is net.lingala.zip4j.exception.ZipException) {
            if (e.type == net.lingala.zip4j.exception.ZipException.Type.WRONG_PASSWORD) return true
        }
        val message = e.message ?: ""
        return message.contains("password", ignoreCase = true) ||
            message.contains("wrong password", ignoreCase = true)
    }

    private data class PendingExtract(
        val archivePath: Path,
        val archiveName: String,
        val options: ExtractOptions,
        val overwrite: Boolean = false,
        val keepBoth: Boolean = false
    )
}
