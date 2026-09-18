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
import com.movtery.zalithlauncher.filemanager.events.FileManagerEventBus
import com.movtery.zalithlauncher.filemanager.logic.FileManagerLogic
import com.movtery.zalithlauncher.filemanager.logic.FilenameValidator
import com.movtery.zalithlauncher.filemanager.logic.FmResult
import com.movtery.zalithlauncher.filemanager.logic.compress.CompressFormat
import com.movtery.zalithlauncher.filemanager.logic.compress.CompressOptions
import com.movtery.zalithlauncher.filemanager.logic.entry.FmEntry
import com.movtery.zalithlauncher.filemanager.logic.ops.ConflictResolution
import com.movtery.zalithlauncher.filemanager.logic.ops.FilePermissions
import com.movtery.zalithlauncher.filemanager.os.FmLog
import com.movtery.zalithlauncher.filemanager.viewmodel.DialogIntent
import com.movtery.zalithlauncher.filemanager.viewmodel.FmSnackbar
import com.movtery.zalithlauncher.filemanager.viewmodel.FmStateStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.nio.file.Files
import java.nio.file.LinkOption
import java.nio.file.Path
import java.nio.file.StandardCopyOption

private const val TAG = "FmCompress"

/** 压缩控制器 */
class CompressController(
    private val context: Context,
    private val logic: FileManagerLogic,
    private val store: FmStateStore,
    private val browse: BrowseController,
    private val coroutineScope: CoroutineScope
) {
    /** 暂存待执行的压缩请求 */
    private var pendingCompress: PendingCompress? = null
    /** 压缩冲突流程暂存目标 */
    private var pendingCompressOutputTarget: OutputTarget? = null

    fun bulkCompress() {
        store.dismissDialog()
        val paths = store.selectedEntries().map { it.path }
        if (paths.isEmpty()) return
        val defaultName = defaultCompressName(paths)
        store.updateState {
            it.copy(
                dialogIntent = DialogIntent.CompressSetup(
                    defaultName = defaultName,
                    sources = paths
                )
            )
        }
    }

    /** 对单个目录 / 文件发起压缩 */
    fun compressEntry(entry: FmEntry) {
        store.dismissDialog()
        val defaultName = defaultCompressName(listOf(entry.path))
        store.updateState {
            it.copy(
                dialogIntent = DialogIntent.CompressSetup(
                    defaultName = defaultName,
                    sources = listOf(entry.path)
                )
            )
        }
    }

    /** 压缩设置确认 */
    fun onCompressSetupConfirmed(
        name: String,
        sources: List<Path>,
        options: CompressOptions
    ) {
        if (name.isBlank()) return
        // 压缩包命名同样通过文件名校验，非法时给出原因
        val err = FilenameValidator.verify(name)
        if (err != null) {
            store.emitError(store.filenameErrorText(err))
            return
        }
        // 若用户手动输入了与所选格式相同的后缀，避免重复拼接（foo.zip + .zip）
        val cleanName = stripCompressSuffix(name, options.format)
        pendingCompress = PendingCompress(cleanName, sources, options)
        store.updateState {
            it.copy(dialogIntent = DialogIntent.CompressOutputChoice)
        }
    }

    /** 在当前目录生成压缩包 */
    fun onCompressOutputChoiceCurrent() {
        val pending = pendingCompress ?: run {
            store.dismissDialog()
            return
        }

        val target = OutputTarget.Local(store.history.currentPath)
        val fileName = pending.name + pending.options.format.suffix
        coroutineScope.launch(Dispatchers.IO) {
            if (existsInTarget(target, fileName)) {
                stageConflict(target, fileName)
            } else {
                executeCompress(target, fileName, overwrite = false)
            }
        }
    }

    /** 通过 SAF 选择输出目录 */
    fun onCompressOutputChoiceSaf() {
        store.updateState {
            it.copy(dialogIntent = DialogIntent.CompressOutputPick)
        }
    }

    /** SAF 输出目录选择取消，清空暂存，关闭流程。 */
    fun onCompressOutputPickedCancelled() {
        pendingCompress = null
        store.dismissDialog()
    }

    /** SAF 选定输出目录，存在同名文件时弹冲突对话框，否则直接执行压缩。 */
    fun onCompressOutputPicked(treeUri: Uri) {
        val pending = pendingCompress ?: run {
            store.dismissDialog()
            return
        }

        val fileName = pending.name + pending.options.format.suffix
        coroutineScope.launch(Dispatchers.IO) {
            if (findChildDocument(context, treeUri, fileName) != null) {
                pendingCompressOutputTarget = OutputTarget.Saf(treeUri)
                store.updateState {
                    it.copy(dialogIntent = DialogIntent.CompressConflict(fileName))
                }
            } else {
                executeCompress(OutputTarget.Saf(treeUri), fileName, overwrite = false)
            }
        }
    }

    /** 压缩冲突决策：按 SKIP/OVERWRITE/KEEP_BOTH 执行压缩。 */
    fun resolveCompressConflict(resolution: ConflictResolution) {
        val target = pendingCompressOutputTarget ?: run {
            store.dismissDialog()
            return
        }

        val pending = pendingCompress ?: run {
            store.dismissDialog()
            return
        }

        store.dismissDialog()
        when (resolution) {
            ConflictResolution.SKIP -> {
                pendingCompress = null
                pendingCompressOutputTarget = null
            }
            ConflictResolution.OVERWRITE -> coroutineScope.launch(Dispatchers.IO) {
                executeCompress(
                    target = target,
                    fileName = pending.name + pending.options.format.suffix,
                    overwrite = true
                )
            }
            ConflictResolution.KEEP_BOTH -> coroutineScope.launch(Dispatchers.IO) {
                executeCompress(
                    target = target,
                    fileName = pending.name + pending.options.format.suffix,
                    overwrite = false,
                    keepBoth = true
                )
            }
        }
    }

    private suspend fun executeCompress(
        target: OutputTarget,
        fileName: String,
        overwrite: Boolean,
        keepBoth: Boolean = false
    ) {
        val pending = pendingCompress ?: return
        store.dismissDialog()
        // 临时文件创建与半成品清理统一由逻辑层 TempWorkspace 负责
        val tempFile = logic.tempWorkspace.compressTempFile(pending.options.format.extension)
        try {
            when (val r = logic.compress(pending.sources, tempFile, pending.options)) {
                is FmResult.Ok -> {
                    val targetName = if (keepBoth) {
                        nextKeepBothName(target, fileName)
                    } else fileName

                    when (target) {
                        is OutputTarget.Local -> {
                            val dest = target.dir.resolve(targetName)
                            if (overwrite) runCatching { Files.deleteIfExists(dest) }
                            withContext(Dispatchers.IO) {
                                Files.move(tempFile, dest, StandardCopyOption.REPLACE_EXISTING)
                                // 跨文件系统移动可能丢失权限，落地后再确保 664
                                FilePermissions.apply(dest)
                            }
                            browse.notifyFileChanged(FileManagerEvent(FileManagerEvent.Type.ARCHIVE, listOf(target.dir.toString())))
                        }
                        is OutputTarget.Saf -> {
                            copyTempToSaf(
                                treeUri = target.treeUri,
                                file = tempFile.toFile(),
                                name = targetName,
                                mimeType = pending.options.format.mimeType,
                                overwrite = overwrite
                            )
                            FileManagerEventBus.dispatch(FileManagerEvent(FileManagerEvent.Type.ARCHIVE, listOf(target.treeUri.toString())))
                        }
                        else -> { /* SafDir 不适用于压缩流程 */ }
                    }
                }
                is FmResult.Failed -> {
                    store.emitSnackbar(FmSnackbar(store.fileOpErrorText(r.error, R.string.fm_error_compress_failed)))
                    browse.refreshCurrentDir()
                }
                FmResult.Rejected -> {
                    store.emitSnackbar(FmSnackbar(store.stringResolver(R.string.fm_task_busy)))
                }
                FmResult.Cancelled -> {}
            }
        } catch (e: Exception) {
            FmLog.warn(TAG, "Compress failed", e)
            store.emitSnackbar(FmSnackbar(store.fileOpErrorText(e, R.string.fm_error_compress_failed)))
            browse.refreshCurrentDir()
        } finally {
            // 半成品清理（含取消 / 失败场景）统一由逻辑层完成
            logic.tempWorkspace.delete(tempFile)
            pendingCompress = null
            pendingCompressOutputTarget = null
        }
    }

    private fun stripCompressSuffix(name: String, format: CompressFormat): String {
        val lower = name.lowercase()
        if (lower.endsWith(format.suffix)) {
            return name.substring(0, name.length - format.suffix.length)
        }
        return name
    }

    private fun stageConflict(target: OutputTarget, fileName: String) {
        pendingCompressOutputTarget = target
        store.updateState {
            it.copy(dialogIntent = DialogIntent.CompressConflict(fileName))
        }
    }

    private fun existsInTarget(target: OutputTarget, fileName: String): Boolean {
        return when (target) {
            is OutputTarget.Local -> Files.exists(target.dir.resolve(fileName), LinkOption.NOFOLLOW_LINKS)
            is OutputTarget.Saf -> findChildDocument(context, target.treeUri, fileName) != null
            is OutputTarget.SafDir -> findChildDocument(context, target.treeUri, fileName) != null
        }
    }

    private fun nextKeepBothName(target: OutputTarget, name: String): String {
        val dot = name.lastIndexOf('.')
        val base = if (dot > 0) name.substring(0, dot) else name
        val ext = if (dot > 0) name.substring(dot) else ""
        var n = 1
        while (true) {
            val candidate = "$base ($n)$ext"
            if (!existsInTarget(target, candidate)) return candidate
            n++
        }
    }

    private suspend fun copyTempToSaf(
        treeUri: Uri,
        file: File,
        name: String,
        mimeType: String,
        overwrite: Boolean
    ) = withContext(Dispatchers.IO) {
        val docId = DocumentsContract.getTreeDocumentId(treeUri)
        val parent = DocumentsContract.buildDocumentUriUsingTree(treeUri, docId)
        if (overwrite) {
            val existing = findChildDocument(context, treeUri, name)
            if (existing != null) {
                runCatching {
                    DocumentsContract.deleteDocument(context.contentResolver, existing)
                }
            }
        }
        val newDoc = DocumentsContract.createDocument(context.contentResolver, parent, mimeType, name)
            ?: throw IllegalStateException("Failed to create document")
        val out = context.contentResolver.openOutputStream(newDoc, "wt")
            ?: throw IllegalStateException("Failed to open output stream")
        out.use { output ->
            file.inputStream().use { input -> input.copyTo(output) }
        }
    }

    private fun defaultCompressName(sources: List<Path>): String {
        if (sources.size == 1) {
            val src = sources[0]
            if (Files.isDirectory(src)) return src.fileName?.toString() ?: "archive"
            val name = src.fileName?.toString() ?: "archive"
            val dot = name.lastIndexOf('.')
            return if (dot > 0) name.substring(0, dot) else name
        }
        return store.history.currentPath.fileName?.toString() ?: "archive"
    }

    private data class PendingCompress(
        val name: String,
        val sources: List<Path>,
        val options: CompressOptions
    )
}
