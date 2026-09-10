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

package com.movtery.zalithlauncher.game.download.engine

import com.movtery.zalithlauncher.utils.network.isInterruptedIOException
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import java.io.InterruptedIOException
import java.net.SocketTimeoutException
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

/**
 * 跨文件共享的主机级健康登记
 */
class HostHealth(
    private val tripThreshold: Int = TRIP_THRESHOLD,
    private val baseCooldownNanos: Long = BASE_COOLDOWN_NANOS,
    private val windowNanos: Long = FAILURE_WINDOW_NANOS,
    private val escalationLookbackNanos: Long = ESCALATION_LOOKBACK_NANOS
) {
    private class Host {
        val recentFailures = ArrayDeque<Long>()
        val trips = ArrayDeque<Long>()
        val trippedUntilNanos = AtomicLong(0L)
    }

    private val hosts = ConcurrentHashMap<String, Host>()

    fun recordFailure(url: String, error: Throwable) {
        if (error.isMissingResourceError()) return
        record(url)
    }

    /** 下载完成但内容校验不过：源在返回 200 的同时给出了坏数据，同样属于主机问题 */
    fun recordCorruption(url: String) {
        record(url)
    }

    /** 冷却期内返回 false；从未见过失败的主机恒为 true */
    fun isViable(url: String): Boolean {
        val host = hosts[hostOf(url)] ?: return true
        return System.nanoTime() >= host.trippedUntilNanos.get()
    }

    /** 距冷却到期的剩余时间，未熔断或从未见过的主机为 0 */
    fun remainingCooldownNanos(url: String): Long {
        val host = hosts[hostOf(url)] ?: return 0L
        return (host.trippedUntilNanos.get() - System.nanoTime()).coerceAtLeast(0L)
    }

    /** 当前失败窗口内的计数，供调用方在多个健康主机间排序 */
    fun recentFailures(url: String): Int {
        val host = hosts[hostOf(url)] ?: return 0
        synchronized(host) {
            prune(host.recentFailures, System.nanoTime(), windowNanos)
            return host.recentFailures.size
        }
    }

    private fun record(url: String) {
        val host = hosts.computeIfAbsent(hostOf(url)) { Host() }
        synchronized(host) {
            val now = System.nanoTime()
            host.recentFailures.addLast(now)
            prune(host.recentFailures, now, windowNanos)
            //同一场失败风暴只计一次熔断：冷却期内的后续失败不再累加退避档位
            if (host.recentFailures.size >= tripThreshold && now >= host.trippedUntilNanos.get()) {
                host.trips.addLast(now)
                prune(host.trips, now, escalationLookbackNanos)
                val shifts = (host.trips.size - 1).coerceAtMost(MAX_COOLDOWN_SHIFTS)
                host.trippedUntilNanos.set(now + (baseCooldownNanos shl shifts))
            }
        }
    }

    private fun prune(deque: ArrayDeque<Long>, now: Long, window: Long) {
        while (deque.isNotEmpty() && now - deque.first() > window) {
            deque.removeFirst()
        }
    }

    private fun hostOf(url: String): String =
        //host:port 作为分键：同一主机的不同端口通常对应不同的服务
        url.toHttpUrlOrNull()?.let { "${it.host}:${it.port}" } ?: url

    companion object {
        const val TRIP_THRESHOLD = 4
        val FAILURE_WINDOW_NANOS = 10_000_000_000L
        val BASE_COOLDOWN_NANOS = 15_000_000_000L
        val ESCALATION_LOOKBACK_NANOS = 300_000_000_000L
        private const val MAX_COOLDOWN_SHIFTS = 3
    }
}

private fun Throwable.isMissingResourceError(): Boolean =
    generateSequence(this) { it.cause }
        .filterIsInstance<HttpResultException>()
        .any { it.code == 404 || it.code == 410 }

/** 沿因果链识别读/连接超时 */
fun Throwable.isTimeoutError(): Boolean =
    generateSequence(this) { it.cause }.any {
        it is SocketTimeoutException ||
                (it is InterruptedIOException && it.message == TIMEOUT_MESSAGE)
    }

/** 线程级中断（协程取消），而非网络超时；超时的 InterruptedIOException 不属此类 */
fun Throwable.isInterruptedByCancellation(): Boolean =
    this.isInterruptedIOException() && !this.isTimeoutError()

private const val TIMEOUT_MESSAGE = "timeout"
