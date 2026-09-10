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

import java.net.UnknownHostException
import java.util.concurrent.atomic.AtomicInteger

/**
 * 一个下载任务的候选源集合
 * 选源按主机健康度排序：近期失败少的主机优先，平级时回到候选列表的静态偏好顺序，
 * 因此全部源健康时行为与用户设置一致，某源病态时整批自动把流量让给其他源。
 * 全部失效后由 [FileDownloader] 进入降级阶段逐源单流重试。
 */
class SourceSet(
    urls: List<String>,
    private val health: HostHealth = HostHealth()
) {
    inner class Source(
        @JvmField val url: String,
        @JvmField val index: Int
    ) {
        private val failCount = AtomicInteger(0)

        @Volatile
        var disabled = false

        /** 404/502/DNS 解析失败等，本轮直接出局 */
        @Volatile
        var fatal = false

        /** 该源对 Range 请求返回了无范围应答 */
        @Volatile
        var noRange = false

        @Volatile
        var lastReason: String? = null

        val supportsRange: Boolean get() = !noRange

        fun recordSuccess() {
            failCount.set(0)
            lastReason = null
        }

        /** 记录一次失败；返回该源是否仍然可用 */
        fun recordFailure(error: Throwable): Boolean {
            lastReason = error.message ?: error.toString()
            health.recordFailure(url, error)
            if (disableImmediately(error)) {
                fatal = true
                disabled = true
                return false
            }
            if (failCount.incrementAndGet() >= SOFT_FAILURE_LIMIT) {
                disabled = true
                return false
            }
            return true
        }

        fun markNoRangeSupport() {
            noRange = true
        }
    }

    private val sources: List<Source> = urls.distinct().mapIndexed { index, url -> Source(url, index) }
    private val degradedCursor = AtomicInteger(0)

    /**
     * 挑选下一个源：跳过禁用、致命、不支持 Range 与处于熔断冷却的候选，
     * 在余下的候选中按（主机近期失败数, 静态偏好顺序）取最优。
     * 全部源都在冷却中时返回 null，调用方按 [cooldownRemainingMillis] 等待后重试
     */
    fun acquire(requireRange: Boolean): Source? =
        sources.asSequence()
            .filter { isUsable(it, requireRange) }
            .filter { health.isViable(it.url) }
            .minWithOrNull(compareBy({ health.recentFailures(it.url) }, { it.index }))

    /**
     * 无视熔断冷却，按轮转保证末路阶段仍会真实尝试每一个候选源
     */
    fun acquireDegraded(requireRange: Boolean): Source? {
        val size = sources.size
        repeat(size) {
            val candidate = sources[Math.floorMod(degradedCursor.getAndAdd(1), size)]
            if (isUsable(candidate, requireRange)) {
                return candidate
            }
        }
        return null
    }

    /**
     * 全部可用候选源都处于熔断冷却时，返回最早的到期剩余毫秒
     */
    fun cooldownRemainingMillis(): Long {
        val remaining = sources.asSequence()
            .filter { !it.disabled && !it.fatal }
            .minOfOrNull { health.remainingCooldownNanos(it.url) }
            ?: return 0L
        return (remaining / 1_000_000L).coerceAtLeast(1L)
    }

    private fun isUsable(source: Source, requireRange: Boolean): Boolean =
        !source.disabled && !source.fatal && !(requireRange && source.noRange)

    val hasUsable: Boolean get() = sources.any { !it.disabled && !it.fatal }

    /**
     * 进入降级阶段：清空非致命源的失败名单，允许它们以单流模式再轮一遍。
     * 致命源（如确切的 404）不会复活。
     */
    fun degrade() {
        sources.forEach { source ->
            if (!source.fatal) {
                source.disabled = false
                source.lastReason = null
            }
        }
    }

    fun describe(): String =
        sources.joinToString("\n") { source ->
            "- ${source.url}${source.lastReason?.let { reason -> ": $reason" } ?: ""}"
        }

    companion object {
        const val SOFT_FAILURE_LIMIT = 3

        fun disableImmediately(error: Throwable): Boolean = when (error) {
            is HttpResultException -> error.code == 404 || error.code == 410
            is UnknownHostException -> true
            else -> false
        }
    }
}
