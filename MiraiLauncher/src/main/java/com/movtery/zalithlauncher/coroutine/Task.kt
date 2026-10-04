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

import com.movtery.zalithlauncher.ui.AndroidStringText
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID

class Task private constructor(
    val id: String,
    val dispatcher: CoroutineDispatcher = Dispatchers.Default,
    val task: suspend CoroutineScope.(Task) -> Unit,
    val onError: suspend (Throwable) -> Unit = {},
    val onFinally: () -> Unit = {},
    val onCancel: () -> Unit = {}
) {
    private val _stage = MutableStateFlow(TaskStage.PREPARING)
    /**
     * 任务阶段（TaskSystem可能用不到，主要服务于GameInstaller）
     */
    val stage = _stage.asStateFlow()

    private val _progress = MutableStateFlow(-1f)
    /** 任务进度状态 */
    val progress = _progress.asStateFlow()

    private val _message = MutableStateFlow<AndroidStringText?>(null)
    /** 任务消息状态 */
    val message = _message.asStateFlow()

    private val _rateBytesPerSec = MutableStateFlow<Long?>(null)
    /** 当前速率 Bytes */
    val rateBytesPerSec = _rateBytesPerSec.asStateFlow()

    /**
     * 更新任务阶段
     */
    fun updateStage(state: TaskStage) {
        this._stage.update { state }
    }

    /**
     * 更新进度，自动处理 NaN、isInfinite 的这种错误情况
     * @param percentage 进度百分比，-1f代表进度不确定
     */
    fun updateProgress(percentage: Float) {
        this._progress.update {
            (percentage.takeIf { it.isFinite() } ?: 0f).coerceIn(-1f, 1f)
        }
    }

    /**
     * 更新任务描述消息
     * @param text 任务描述消息
     */
    fun updateMessage(text: AndroidStringText?) {
        this._message.update { text }
    }

    /**
     * 更新任务比特速率
     */
    fun updateSpeed(bytes: Long) {
        this._rateBytesPerSec.update { bytes.takeIf { it >= 0L } }
    }

    /**
     * 清除任务比特速率
     */
    fun clearSpeed() {
        this._rateBytesPerSec.update { null }
    }

    override fun equals(other: Any?): Boolean = other is Task && other.id == this.id

    override fun hashCode(): Int = id.hashCode()

    companion object {
        fun runTask(
            id: String? = null,
            dispatcher: CoroutineDispatcher = Dispatchers.Default,
            task: suspend CoroutineScope.(Task) -> Unit,
            onError: suspend (Throwable) -> Unit = {},
            onFinally: () -> Unit = {},
            onCancel: () -> Unit = {}
        ): Task =
            Task(
                id = id ?: getRandomID(),
                dispatcher = dispatcher,
                task = task,
                onError = onError,
                onFinally = onFinally,
                onCancel = onCancel
            )

        private fun getRandomID(): String = UUID.randomUUID().toString()
    }
}