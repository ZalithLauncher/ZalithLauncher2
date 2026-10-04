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

import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.filemanager.events.FileManagerEvent
import com.movtery.zalithlauncher.filemanager.logic.FileManagerLogic
import com.movtery.zalithlauncher.filemanager.logic.FmResult
import com.movtery.zalithlauncher.filemanager.logic.ops.ConflictResolution
import com.movtery.zalithlauncher.filemanager.logic.ops.PasteMode
import com.movtery.zalithlauncher.filemanager.logic.ops.PasteRequest
import com.movtery.zalithlauncher.filemanager.logic.ops.PasteSummary
import com.movtery.zalithlauncher.filemanager.viewmodel.DialogIntent
import com.movtery.zalithlauncher.filemanager.viewmodel.FmSnackbar
import com.movtery.zalithlauncher.filemanager.viewmodel.FmStateStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.nio.file.Path

/** 粘贴控制器 */
class PasteController(
    private val logic: FileManagerLogic,
    private val store: FmStateStore,
    private val browse: BrowseController,
    private val coroutineScope: CoroutineScope
) {
    /** 触发粘贴流程 */
    fun requestPaste() {
        val cb = store.clipboard ?: return
        val target = store.history.currentPath
        val mode = if (cb.isCut) PasteMode.MOVE else PasteMode.COPY
        coroutineScope.launch(Dispatchers.IO) {
            val request = runCatching {
                logic.buildPasteRequest(cb.sources, target, mode)
            }.getOrElse {
                store.emitSnackbar(FmSnackbar(it.message ?: store.stringResolver(R.string.fm_error_paste_invalid)))
                browse.refreshCurrentDir()
                return@launch
            }
            when (request) {
                is PasteRequest.Ready -> {
                    executePasteInternal(request.sources, request.targetDir, request.mode,
                        resolutions = request.sources.map { ConflictResolution.SKIP }
                    ) { /* 结果已通过 snackbar 上报 */ }
                }
                is PasteRequest.ResolveRequest -> {
                    // 初始索引指向首个非 null 冲突项（null 为非冲突项，无需决策）
                    val firstConflict = request.conflicts.indexOfFirst { it != null }
                    if (firstConflict < 0) {
                        executePasteInternal(request.sources, request.targetDir, request.mode,
                            resolutions = request.sources.map { ConflictResolution.SKIP }
                        ) { }
                        return@launch
                    }
                    store.updateState {
                        it.copy(
                            dialogIntent = DialogIntent.PasteConflict(
                                request = request,
                                decidedResolutions = emptyList(),
                                currentIndex = firstConflict
                            )
                        )
                    }
                }
            }
        }
    }

    /** 粘贴冲突决策 */
    fun resolvePasteConflict(resolution: ConflictResolution) {
        val cur = store.stateValue().dialogIntent as? DialogIntent.PasteConflict ?: return
        val req = cur.request
        val total = req.conflicts.size
        // 把当前 resolution 写入与 currentIndex 关联的“项位置”，并寻找下一个冲突 index
        val updated = cur.decidedResolutions.toMutableList()
        while (updated.size < total) updated += ConflictResolution.SKIP
        updated[cur.currentIndex] = resolution
        // 找到下一个非 null 冲突项 index
        var next = cur.currentIndex + 1
        while (next < total && req.conflicts[next] == null) next++
        if (next < total) {
            store.updateState {
                it.copy(dialogIntent = DialogIntent.PasteConflict(req, updated, next))
            }
        } else {
            // 全部决策完成
            store.dismissDialog()
            coroutineScope.launch(Dispatchers.IO) {
                // 若为导入触发的冲突流程，粘贴完成后需清理临时目录（异步，不可在 finally 中清理）
                val tempDir = store.pendingImportTempDir
                if (tempDir != null) {
                    executeImportPaste(req.sources, req.targetDir, req.mode, updated, tempDir)
                } else {
                    executePasteInternal(req.sources, req.targetDir, req.mode, updated) { }
                }
            }
        }
    }

    /** 执行粘贴。 */
    suspend fun executePasteInternal(
        sources: List<Path>,
        targetDir: Path,
        mode: PasteMode,
        resolutions: List<ConflictResolution>,
        onResult: suspend (PasteSummary?) -> Unit
    ) {
        when (val r = logic.executePaste(sources, targetDir, mode, resolutions)) {
            is FmResult.Ok -> {
                // 剪贴板任务被取消时保留，任务完成后清空
                store.setClipboard(null)
                browse.notifyFileChanged(
                    FileManagerEvent(
                        FileManagerEvent.Type.COPY_PASTE,
                        listOf(targetDir.toString())
                    )
                )
                onResult(r.value)
            }
            is FmResult.Failed -> {
                store.emitSnackbar(FmSnackbar(r.error.message ?: store.stringResolver(R.string.fm_error_paste_failed)))
                browse.refreshCurrentDir()
                onResult(null)
            }
            FmResult.Cancelled -> {
                // 任务被取消时剪贴板保留，便于用户再次粘贴
                onResult(null)
            }
            FmResult.Rejected -> {
                store.emitSnackbar(FmSnackbar(store.stringResolver(R.string.fm_task_busy)))
                onResult(null)
            }
        }
    }

    /** 执行导入触发的粘贴 */
    suspend fun executeImportPaste(
        sources: List<Path>,
        targetDir: Path,
        mode: PasteMode,
        resolutions: List<ConflictResolution>,
        tempDir: Path
    ) {
        executePasteInternal(sources, targetDir, mode, resolutions) {
            finishImportPaste(targetDir.toString(), tempDir)
        }
    }

    private suspend fun finishImportPaste(targetPath: String, tempDir: Path) {
        logic.tempWorkspace.delete(tempDir)
        store.pendingImportTempDir = null
        browse.notifyFileChanged(
            FileManagerEvent(FileManagerEvent.Type.IMPORT, listOf(targetPath))
        )
    }
}
