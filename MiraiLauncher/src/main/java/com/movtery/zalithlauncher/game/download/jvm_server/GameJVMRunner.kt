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

package com.movtery.zalithlauncher.game.download.jvm_server

import android.content.Intent
import com.movtery.zalithlauncher.components.jre.Jre
import com.movtery.zalithlauncher.context.GlobalContext
import com.movtery.zalithlauncher.coroutine.TaskLogOutput
import com.movtery.zalithlauncher.notification.NoticeProgress
import com.movtery.zalithlauncher.utils.logging.Logger
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.io.IOException
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.nanoseconds
import kotlin.time.Duration.Companion.seconds

private const val TAG = "GameJVMRunner"

/** 等待互斥进程自行退出的时长，超过后按 pid 强杀 */
private val WAIT_BEFORE_FORCE_KILL: Duration = 15.seconds
/** 强杀后再宽限一轮的时长，仍无法清场则以可见错误结束 */
private val KILL_GRACE: Duration = 5.seconds
/** 等待日志的打印间隔 */
private val WAIT_LOG_INTERVAL: Duration = 2.seconds
/** 单次 JVM 运行等待退出码的总时限 */
private val JVM_EXIT_TIMEOUT: Duration = 15.minutes
/** 前台服务启动失败后的最大重试次数 */
private const val SERVICE_START_RETRY_MAX = 4
/** 前台服务启动失败重试的退避基准间隔 */
private val SERVICE_START_RETRY_DELAY: Duration = 500.milliseconds
/** 互斥进程消失后、重新拉起前的冷却时长 */
private val POST_PROCESS_EXIT_COOLDOWN: Duration = 1.seconds

/**
 * 运行一个简易的JVM环境，安装ModLoader，同时在jvm退出时，尝试使用其他的Java环境重试
 * @param logId 记录日志的 tag
 * @param start 刚开始启动会调用的回调
 * @param logOutput 安装 JVM 的实时日志输出
 */
suspend fun runJvmRetryRuntimes(
    logId: String,
    jvmArgs: String,
    prefixArgs: (Jre) -> String?,
    jre: Jre,
    userHome: String,
    postSummary: String? = null,
    postProgress: NoticeProgress? = null,
    start: () -> Unit = {},
    logOutput: TaskLogOutput? = null
): Unit = withContext(Dispatchers.Default) {
    waitForJvmExclusiveProcessesStopped(logId)

    start()

    val finalArgs = prefixArgs(jre)?.let {
        "$it $jvmArgs"
    } ?: jvmArgs

    val exitCode = startJvmServiceAndWaitExit(
        jvmArgs = finalArgs,
        jreName = jre.jreName,
        userHome = userHome,
        postSummary = postSummary,
        postProgress = postProgress,
        logOutput = logOutput
    )

    if (exitCode != 0) {
        val nextJava: Jre? = when (jre) {
            Jre.JRE_8 -> Jre.JRE_17
            Jre.JRE_17 -> Jre.JRE_21
            else -> null
        }

        nextJava?.let { jre ->
            Logger.info(TAG, "Retry with jre ${jre.name}...")
            runJvmRetryRuntimes(
                logId = logId,
                jvmArgs = jvmArgs,
                prefixArgs = prefixArgs,
                jre = jre,
                userHome = userHome,
                postSummary = postSummary,
                postProgress = postProgress,
                logOutput = logOutput
            )
        } ?: throw JvmCrashException(exitCode)
    }
}

/**
 * 等待互斥进程（:jvm、:game）退出后再开跑
 */
private suspend fun waitForJvmExclusiveProcessesStopped(logId: String) {
    val startNanos = System.nanoTime()
    var lastLog = -WAIT_LOG_INTERVAL
    var forceKilled = false
    var sawBlocking = false

    while (true) {
        val blocking = listBlockingProcesses(GlobalContext)
        if (blocking.isEmpty()) {
            if (!sawBlocking) {
                // 从未见过互斥进程则无需等待，直接放行
                return
            }
            // 刚等走过互斥进程，给 system_server 一点处理死亡事件的时间
            delay(POST_PROCESS_EXIT_COOLDOWN)
            if (listBlockingProcesses(GlobalContext).isEmpty()) return
            continue
        }

        sawBlocking = true

        val elapsed = (System.nanoTime() - startNanos).nanoseconds
        when {
            elapsed >= WAIT_BEFORE_FORCE_KILL + KILL_GRACE ->
                throw IOException(
                    "Timed out waiting for exclusive processes to stop: ${blocking.joinToString()}"
                )

            elapsed >= WAIT_BEFORE_FORCE_KILL -> {
                if (!forceKilled) {
                    forceKilled = true
                    Logger.warning(TAG, "$logId Force stopping blocking processes: $blocking")
                }
                stopAllNonMainProcesses(GlobalContext)
            }

            elapsed - lastLog >= WAIT_LOG_INTERVAL -> {
                lastLog = elapsed
                Logger.info(TAG, "$logId Waiting for other processes stop... [$blocking]")
            }
        }
        delay(100L.milliseconds)
    }
}

suspend fun startJvmServiceAndWaitExit(
    jvmArgs: String,
    jreName: String? = null,
    userHome: String? = null,
    postSummary: String? = null,
    postProgress: NoticeProgress? = null,
    logOutput: TaskLogOutput? = null,
): Int = withContext(Dispatchers.IO) {
    val doneSignal = CompletableDeferred<Unit>()

    val tailerJob = logOutput?.let {
        ProcessLogTailer(LATEST_PROCESS_LOG_FILE, it).launch(this)
    }

    try {
        // 先起接收端再拉起服务
        // JVM 秒退时退出码不会因 socket 尚未绑定而丢失
        JVMSocketServer.start { receiveMsg ->
            Logger.info(TAG, "receive msg: $receiveMsg, stopping server...")
            if (!doneSignal.isCompleted) {
                doneSignal.complete(Unit)
            }
            JVMSocketServer.stop()
        }

        var attempt = 0
        while (true) {
            try {
                startJvmService(
                    context = GlobalContext,
                    jvmArgs = jvmArgs,
                    userHome = userHome,
                    jreName = jreName,
                    postSummary = postSummary,
                    postProgress = postProgress
                )
                break
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                attempt++
                if (attempt > SERVICE_START_RETRY_MAX) {
                    Logger.error(TAG, "Failed to start JvmService after $attempt attempts", e)
                    throw e
                }
                val retryDelay = SERVICE_START_RETRY_DELAY * (1 shl (attempt - 1))
                Logger.warning(
                    TAG,
                    "Failed to start JvmService (attempt $attempt/$SERVICE_START_RETRY_MAX): " +
                        "${e.message}. Retrying in $retryDelay..."
                )
                delay(retryDelay)
            }
        }

        if (withTimeoutOrNull(JVM_EXIT_TIMEOUT) { doneSignal.await() } == null) {
            // 超时，停掉服务与接收端，让安装以可见错误结束而不是永久挂起
            Logger.error(TAG, "Timed out ($JVM_EXIT_TIMEOUT) waiting for JVM exit code, stopping service...")
            runCatching {
                GlobalContext.stopService(Intent(GlobalContext, JvmService::class.java))
            }
            throw IOException("Timed out waiting for the JVM process to exit.")
        }
    } finally {
        // 无论成败、取消还是超时都收掉接收端
        // 单例状态跨轮残留会毒化下一次运行
        JVMSocketServer.stop()
        tailerJob?.cancel()
    }

    val code = JVMSocketServer.receiveMsg?.toIntOrNull()
    Logger.info(TAG, "receive exit code: ${code ?: "unknown, default 0"}")
    code ?: 0
}