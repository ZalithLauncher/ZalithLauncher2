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

package com.movtery.zalithlauncher.keepalive

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.movtery.zalithlauncher.utils.logging.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicInteger
import kotlin.time.Duration.Companion.milliseconds

private const val TAG = "TaskKeepAlive"

/**
 * 任务保活控制器
 */
object TaskKeepAlive {
    /**
     * 所有任务结束后的延迟停止时间，避免连续任务之间出现空隙导致服务频繁启停
     */
    private const val STOP_DELAY_MS = 1_500L

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val activeCount = AtomicInteger(0)
    private var stopJob: Job? = null

    /** 应用级 Context，在 Application 启动时赋值，可能被其他线程读取 */
    @Volatile
    private var appContext: Context? = null

    /**
     * 当前持有保活的任务数量
     */
    val count: Int get() = activeCount.get()

    @JvmStatic
    fun initialize(context: Context) {
        appContext = context.applicationContext
    }

    /**
     * 申请保活，每调用一次 [acquire]，都必须对应调用一次 [release]
     * 只要任务被提交/任务流开始执行就会立即拉起前台服务，与任务是否真的在下载无关
     */
    @JvmStatic
    @Synchronized
    fun acquire() {
        val count = activeCount.incrementAndGet()
        //有新任务加入，取消待执行的停止
        stopJob?.cancel()
        stopJob = null
        //首个任务立即拉起服务，确保应用即将退至后台时也能拿到前台身份
        if (count == 1) startService()
    }

    /**
     * 释放保活
     */
    @JvmStatic
    @Synchronized
    fun release() {
        val count = activeCount.updateAndGet { if (it > 0) it - 1 else 0 }
        if (count == 0) scheduleStop()
    }

    /**
     * 立即停止保活并清空计数，用于停止所有任务、应用崩溃等场景
     */
    @JvmStatic
    @Synchronized
    fun reset() {
        stopJob?.cancel()
        stopJob = null
        activeCount.set(0)
        stopService()
    }

    private fun scheduleStop() {
        stopJob?.cancel()
        stopJob = scope.launch {
            delay(STOP_DELAY_MS.milliseconds)
            //延迟期间可能又有新任务加入
            if (activeCount.get() == 0) stopService()
        }
    }

    private fun startService() {
        val context = appContext ?: run {
            Logger.warning(TAG, "The application context is not initialized, skip starting the task keep-alive service.")
            return
        }

        runCatching {
            ContextCompat.startForegroundService(
                context,
                Intent(context, TaskKeepAliveService::class.java)
            )
        }.onFailure { e ->
            //应用处于后台等受限状态时系统会拒绝启动前台服务，此时忽略即可
            Logger.error(TAG, "Failed to start the task keep-alive service", e)
        }
    }

    private fun stopService() {
        val context = appContext ?: return
        runCatching {
            context.stopService(Intent(context, TaskKeepAliveService::class.java))
        }.onFailure { e ->
            Logger.error(TAG, "Failed to stop the task keep-alive service", e)
        }
    }
}
