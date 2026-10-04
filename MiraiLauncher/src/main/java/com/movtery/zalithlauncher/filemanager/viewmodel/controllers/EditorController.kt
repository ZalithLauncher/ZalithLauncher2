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
import com.movtery.zalithlauncher.filemanager.logic.editor.MAX_EDIT_SIZE
import com.movtery.zalithlauncher.filemanager.os.FmLog
import com.movtery.zalithlauncher.filemanager.viewmodel.EditorUiState
import com.movtery.zalithlauncher.filemanager.viewmodel.FmSnackbar
import com.movtery.zalithlauncher.filemanager.viewmodel.FmStateStore
import com.movtery.zalithlauncher.ui.code_editor.EditorState
import io.github.rosemoe.sora.text.Content
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.nio.ByteBuffer
import java.nio.charset.Charset
import java.nio.charset.CodingErrorAction
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path

/**
 * 文本编辑器控制器
 */
class EditorController(
    private val store: FmStateStore,
    private val browse: BrowseController,
    private val coroutineScope: CoroutineScope
) {
    /** 当前文件使用的编码，保存时按原编码写回 */
    private var charset: Charset = StandardCharsets.UTF_8

    /** 进行中的保存任务 */
    private var saveJob: Job? = null

    /** 打开文件并异步加载内容 */
    fun open(path: Path) {
        store.updateEditorUi { EditorUiState(path = path) }
        coroutineScope.launch(Dispatchers.IO) {
            val result = runCatching { loadFile(path) }
            result.onSuccess { content ->
                store.updateEditorUi {
                    it.copy(
                        state = EditorState.Success(content),
                        writable = Files.isWritable(path),
                        error = null
                    )
                }
            }.onFailure { e ->
                if (e is CancellationException) return@launch
                FmLog.error(TAG, "Open editor file failed: ${path}", e)
                store.updateEditorUi {
                    it.copy(error = store.fileOpErrorText(e, R.string.fm_editor_open_failed))
                }
            }
        }
    }

    /** 内容被修改（供编辑器事件回调） */
    fun onTextChanged() {
        store.updateEditorUi { it.copy(dirty = true) }
    }

    /**
     * 保存当前内容到文件
     */
    fun save(onDone: (Boolean) -> Unit = {}) {
        if (store.editorUiValue().saving) return
        val state = store.editorUiValue()
        val path = state.path ?: return
        val content = (state.state as? EditorState.Success)?.content ?: return
        store.updateEditorUi { it.copy(saving = true) }
        saveJob = coroutineScope.launch {
            try {
                val success = withContext(Dispatchers.IO) {
                    runCatching {
                        Files.write(path, content.toString().toByteArray(charset))
                    }.isSuccess
                }
                // 写入完成后若用户已取消（Files.write 为阻塞调用，无法中途中断），
                // 保持未保存状态，不更新 dirty、不提示结果
                ensureActive()
                if (!success) {
                    FmLog.error(TAG, "Save editor file failed: $path")
                }
                store.updateEditorUi { it.copy(dirty = false) }
                if (success) {
                    store.emitSnackbar(FmSnackbar(store.stringResolver(R.string.generic_saved), long = false))
                    // 文件大小 / 修改时间已变化，刷新列表展示
                    browse.refreshDir()
                } else {
                    store.emitSnackbar(FmSnackbar(store.stringResolver(R.string.fm_editor_save_failed)))
                }
                onDone(success)
            } catch (e: CancellationException) {
                // 用户取消保存：不更新 dirty、不提示结果
                onDone(false)
                throw e
            } finally {
                store.updateEditorUi { it.copy(saving = false) }
            }
        }
    }

    /**
     * 取消进行中的保存
     */
    fun cancelSave() {
        saveJob?.cancel()
        saveJob = null
        store.updateEditorUi { it.copy(saving = false) }
    }

    /** 请求显示退出确认弹窗 */
    fun requestExitConfirm() {
        store.updateEditorUi { it.copy(exitConfirm = true) }
    }

    /** 取消退出确认弹窗 */
    fun cancelExitConfirm() {
        store.updateEditorUi { it.copy(exitConfirm = false) }
    }

    /** 是否存在未保存的修改（供系统返回键判定） */
    fun hasDirty(): Boolean = store.editorUiValue().dirty

    private fun loadFile(path: Path): Content {
        val size = Files.size(path)
        if (size > MAX_EDIT_SIZE || !hasEnoughMemoryFor(size)) {
            throw EditorFileTooLargeException(
                store.stringResolver(
                    if (size > MAX_EDIT_SIZE) R.string.fm_editor_file_too_large
                    else R.string.fm_editor_out_of_memory
                )
            )
        }
        val bytes = Files.readAllBytes(path)
        val text = decode(bytes)
        return Content(text)
    }

    /**
     * 判断剩余堆内存能否容纳该文件的加载峰值，内存不足时直接拒绝加载
     */
    private fun hasEnoughMemoryFor(fileSize: Long): Boolean {
        val runtime = Runtime.getRuntime()
        val available = runtime.maxMemory() - (runtime.totalMemory() - runtime.freeMemory())
        val estimated = fileSize * MEMORY_MULTIPLIER + MEMORY_OVERHEAD
        return estimated <= available * MEMORY_SAFE_RATIO
    }

    /**
     * 解码文件内容：优先识别 BOM，否则按严格 UTF-8 解码，
     * 解码失败回退 GBK（兼容旧编码的中文文本文件）。
     */
    private fun decode(bytes: ByteArray): String {
        val bom = detectBom(bytes)
        val contentBytes = bytes.copyOfRange(bom.length, bytes.size)
        if (bom.charset != null) {
            charset = bom.charset
            return String(contentBytes, bom.charset)
        }
        val decoder = StandardCharsets.UTF_8.newDecoder()
            .onMalformedInput(CodingErrorAction.REPORT)
            .onUnmappableCharacter(CodingErrorAction.REPORT)
        return try {
            charset = StandardCharsets.UTF_8
            decoder.decode(ByteBuffer.wrap(contentBytes)).toString()
        } catch (e: CharacterCodingException) {
            charset = Charset.forName("GBK")
            String(contentBytes, charset)
        }
    }

    private fun detectBom(bytes: ByteArray): BomResult = when {
        bytes.size >= 3 && bytes[0] == 0xEF.toByte() &&
            bytes[1] == 0xBB.toByte() && bytes[2] == 0xBF.toByte() ->
            BomResult(3, StandardCharsets.UTF_8)

        bytes.size >= 2 && bytes[0] == 0xFF.toByte() && bytes[1] == 0xFE.toByte() ->
            BomResult(2, StandardCharsets.UTF_16LE)

        bytes.size >= 2 && bytes[0] == 0xFE.toByte() && bytes[1] == 0xFF.toByte() ->
            BomResult(2, StandardCharsets.UTF_16BE)

        else -> BomResult(0, null)
    }

    private data class BomResult(
        val length: Int,
        val charset: Charset?
    )

    private class EditorFileTooLargeException(message: String) : Exception(message)

    companion object {
        private const val TAG = "EditorController"
        /** 加载文本文件时的内存放大系数（字节数组 + UTF-16 字符串 + 编辑器行结构等） */
        private const val MEMORY_MULTIPLIER: Long = 6
        /** 加载过程中的固定内存开销（语法分析等其他分配） */
        private const val MEMORY_OVERHEAD: Long = 16L * 1024 * 1024
        /** 允许占用的可用堆内存比例，保留余量给应用其他部分 */
        private const val MEMORY_SAFE_RATIO = 0.75
    }
}
