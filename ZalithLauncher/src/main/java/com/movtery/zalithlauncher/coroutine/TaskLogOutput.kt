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

package com.movtery.zalithlauncher.coroutine

import androidx.annotation.Keep
import com.movtery.zalithlauncher.ui.AndroidStringText
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * 任务流的日志输出接口，持有待展示的日志行状态
 * @param title 日志标题
 * @param maxLines 日志行数上限，超出后丢弃最旧的日志行
 */
@Keep
class TaskLogOutput(
    val title: AndroidStringText,
    private val maxLines: Int = DEFAULT_MAX_LINES
) {
    private val _lines = MutableStateFlow<List<String>>(emptyList())

    /** 当前已展示的日志行 */
    val lines: StateFlow<List<String>> = _lines.asStateFlow()

    private val _active = MutableStateFlow(false)

    /** 是否处于日志输出会话中 */
    val active: StateFlow<Boolean> = _active.asStateFlow()

    /**
     * 发起一次日志输出会话：清空已有内容并进入活跃状态
     */
    fun start() {
        _lines.update { emptyList() }
        _active.update { true }
    }

    /**
     * 停止日志输出会话，保留已展示的内容
     */
    fun stop() {
        _active.update { false }
    }

    /**
     * 增量追加单行日志
     */
    fun appendLine(line: String) {
        appendLines(listOf(line))
    }

    /**
     * 增量追加多行日志
     */
    fun appendLines(lines: List<String>) {
        if (lines.isEmpty()) return
        _lines.update { current -> (current + lines).takeLast(maxLines) }
    }

    companion object {
        /** 默认日志行数上限 */
        const val DEFAULT_MAX_LINES = 1000
    }
}

/**
 * 创建并发起一次日志输出会话：立即创建 [TaskLogOutput] 并写入 [holder]，
 * [block] 结束（含异常、取消）后停止会话并将 [holder] 置空
 */
suspend fun <R> withTaskLogOutput(
    holder: MutableStateFlow<TaskLogOutput?>,
    title: AndroidStringText,
    block: suspend (TaskLogOutput) -> R
): R {
    val output = TaskLogOutput(title).also { it.start() }
    holder.value = output
    try {
        return block(output)
    } finally {
        output.stop()
        holder.value = null
    }
}
