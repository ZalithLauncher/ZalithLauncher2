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

package com.movtery.zalithlauncher.filemanager.logic.task

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex

/**
 * 长任务进度
 */
data class TaskProgress(
    val kind: TaskKind,
    val total: Int = 0,
    val completed: Int = 0,
    val currentName: String? = null,
    val bytesTotal: Long = 0L,
    val bytesDone: Long = 0L,
    val bytesPerSecond: Long = 0L,
    val message: String? = null
) {
    val ratio: Float get() = if (total <= 0) 0f else completed.toFloat() / total
}

/**
 * 任务状态
 */
sealed interface TaskState {
    data object Idle : TaskState
    data class Busy(val kind: TaskKind) : TaskState
}

/**
 * 任务被拒绝的原因
 */
sealed interface TaskReject {
    data object Busy : TaskReject
}

class TaskManager {
    private val mutex = Mutex()
    private val _state = MutableStateFlow<TaskState>(TaskState.Idle)
    val state: StateFlow<TaskState> = _state.asStateFlow()

    private val _progress = MutableStateFlow<TaskProgress?>(null)
    val progress: StateFlow<TaskProgress?> = _progress.asStateFlow()

    @Volatile
    private var currentJob: Job? = null

    /**
     * 尝试运行任务，已有任务进行中时返回 [TaskReject.Busy]
     * @param kind 任务类型
     * @param block 任务体，可在 [TaskProgressScope] 中上报进度
     * @return 任务执行结果
     */
    suspend fun <T> run(
        kind: TaskKind,
        block: suspend TaskProgressScope.() -> T
    ): RunResult<T> {
        if (!mutex.tryLock()) {
            return RunResult.Rejected(TaskReject.Busy)
        }

        try {
            _state.value = TaskState.Busy(kind)
            _progress.value = TaskProgress(kind = kind)
            val scope = TaskProgressScopeImpl(kind, _progress)
            // 使用 coroutineScope + async 创建可独立取消的子任务
            // 任务块强制派发到 Dispatchers.IO，避免阻塞调用线程
            return coroutineScope {
                val deferred = async(Dispatchers.IO) { scope.block() }
                currentJob = deferred
                try {
                    RunResult.Ok(deferred.await())
                } catch (_: CancellationException) {
                    RunResult.Cancelled
                } catch (e: Throwable) {
                    RunResult.Failed(e)
                } finally {
                    currentJob = null
                    _progress.value = null
                    _state.value = TaskState.Idle
                }
            }
        } finally {
            runCatching { mutex.unlock() }
        }
    }

    /**
     * 取消当前正在执行的任务
     */
    fun cancel() {
        runCatching { currentJob?.cancel() }
    }
}

/** [TaskManager.run] 的结果 */
sealed interface RunResult<out T> {
    data class Ok<T>(val value: T) : RunResult<T>
    data class Failed(val error: Throwable) : RunResult<Nothing>
    data object Cancelled : RunResult<Nothing>
    data class Rejected(val reason: TaskReject) : RunResult<Nothing>
}

/** 任务进度上报接口 */
interface TaskProgressScope {
    val kind: TaskKind

    /** 整体进度更新 */
    fun report(
        total: Int? = null,
        completed: Int? = null,
        currentName: String? = null,
        bytesTotal: Long? = null,
        bytesDone: Long? = null,
        bytesPerSecond: Long? = null,
        message: String? = null
    )
}

private class TaskProgressScopeImpl(
    override val kind: TaskKind,
    private val flow: MutableStateFlow<TaskProgress?>
) : TaskProgressScope {
    override fun report(
        total: Int?,
        completed: Int?,
        currentName: String?,
        bytesTotal: Long?,
        bytesDone: Long?,
        bytesPerSecond: Long?,
        message: String?
    ) {
        val cur = flow.value ?: TaskProgress(kind = kind)
        flow.value = cur.copy(
            total = total ?: cur.total,
            completed = completed ?: cur.completed,
            currentName = currentName ?: cur.currentName,
            bytesTotal = bytesTotal ?: cur.bytesTotal,
            bytesDone = bytesDone ?: cur.bytesDone,
            bytesPerSecond = bytesPerSecond ?: cur.bytesPerSecond,
            message = message ?: cur.message
        )
    }
}