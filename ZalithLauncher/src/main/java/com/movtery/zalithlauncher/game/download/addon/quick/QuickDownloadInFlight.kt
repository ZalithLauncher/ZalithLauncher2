/*
 * Zalith Launcher 2
 * Copyright (C) 2025-2026 MovTery <movtery228@qq.com> and contributors
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

package com.movtery.zalithlauncher.game.download.addon.quick

import com.movtery.zalithlauncher.game.download.addon.platform.Platform

/**
 * 唯一标识一项在途安装工作：将指定平台项目的某个文件版本安装到指定目录。
 *
 * 该键仅用于在途去重。[installDir] 是模组、资源包等内容的实际安装目录，
 * 不是游戏版本根目录，也不包含最终文件名。
 *
 * @property platform 资源所属平台。
 * @property projectId 平台内的项目 ID。
 * @property versionId 平台内的文件版本 ID，而非 Minecraft 游戏版本。
 * @property installDir 规范化后的实际安装目录路径。
 */
internal data class QuickInFlightKey(
    val platform: Platform,
    val projectId: String,
    val versionId: String,
    val installDir: String
)

/**
 * 一次在途登记的所有权凭证，包含过滤后的可执行计划及释放登记所需的记录。
 *
 * 调用方只能使用 [items] 执行后续下载与安装，并必须在任务完成、失败或取消时调用 [release]。
 *
 * @property registry 创建该租约的注册表，释放登记时必须归还原注册表。
 * @property items 已排除在途重复目标的下载计划；空列表表示本次没有可执行目标。
 * @property keys 本次登记新增的工作键，用于在释放时精确移除对应记录。
 * @property destinations 本次登记的最终目标路径及其工作键，用于释放路径所有权。
 * @property skippedTargets 因相同工作已在当前批次或其他任务中登记而被排除的安装目标数量。
 */
internal class QuickInFlightLease internal constructor(
    private val registry: QuickInFlightRegistry,
    val items: List<QuickDownloadItem>,
    internal val keys: List<QuickInFlightKey>,
    internal val destinations: Map<String, QuickInFlightKey>,
    val skippedTargets: Int
) {
    /** 登记是否已释放；仅由所属注册表在同步临界区内修改。 */
    internal var released = false
        private set

    /** 将本租约持有的全部登记归还所属注册表；重复调用不会再次释放。 */
    fun release() = registry.release(this)

    /** 标记登记已释放，仅供所属注册表完成释放流程时调用。 */
    internal fun markReleased() {
        released = true
    }
}

/**
 * 管理进程内快捷下载任务的工作键和最终目标路径所有权。
 *
 * 工作键用于过滤重复安装，最终目标路径用于阻止不同文件并发写入同一位置。
 * 每次登记都会返回独立的 [QuickInFlightLease]，由持有方负责释放。
 */
internal class QuickInFlightRegistry {
    /** 保护工作键、目标路径及租约释放状态的监视器。 */
    private val lock = Any()

    /** 已登记且尚未释放的工作键。 */
    private val active = mutableSetOf<QuickInFlightKey>()

    /** 已登记且尚未释放的最终目标路径，值为负责该路径的工作键。 */
    private val activeDestinations = mutableMapOf<String, QuickInFlightKey>()

    /**
     * 在一个同步临界区内过滤并登记下载计划。
     *
     * 对每个安装目标生成 [QuickInFlightKey]。若相同键已处于在途状态，或已在本批次中登记，
     * 则排除该目标；其余目标会登记到当前注册表，并记录在返回的租约中。
     * 若另一个工作键已经占用同一最终目标路径，则抛出 [QuickPlanningException]。
     *
     * 该函数是非挂起的同步操作，只在进入短临界区时可能短暂阻塞；
     * 不会等待其他任务完成下载，也不会复用其下载结果。检查全部完成后才更新注册表，
     * 因此路径冲突不会留下部分登记。
     *
     * @param items 已完成规划、安装目录与最终路径已规范化的下载项。
     * @return 本次登记的所有权凭证；其 [QuickInFlightLease.items] 仅包含可执行目标，可能为空。
     * @throws QuickPlanningException 如果不同工作键需要写入同一最终目标路径。
     */
    fun acquire(items: List<QuickDownloadItem>): QuickInFlightLease = synchronized(lock) {
        val acquired = mutableListOf<QuickInFlightKey>()
        val acquiredDestinations = mutableMapOf<String, QuickInFlightKey>()
        var skipped = 0
        val filtered = items.mapNotNull { item ->
            val targets = item.targets.filter { target ->
                val key = QuickInFlightKey(
                    platform = item.key.platform,
                    projectId = item.key.projectId,
                    versionId = item.file.versionId,
                    installDir = target.installDir.path
                )
                if (key in active || key in acquired) {
                    skipped++
                    return@filter false
                }

                val destination = item.destinationFor(target).path
                val owner = activeDestinations[destination] ?: acquiredDestinations[destination]
                if (owner != null && owner != key) {
                    throw QuickPlanningException(
                        "Another quick download is installing a different file to $destination."
                    )
                }

                acquired += key
                acquiredDestinations[destination] = key
                true
            }
            item.copy(targets = targets).takeIf { targets.isNotEmpty() }
        }
        active += acquired
        activeDestinations.putAll(acquiredDestinations)
        QuickInFlightLease(this, filtered, acquired, acquiredDestinations, skipped)
    }

    /**
     * 释放租约持有的工作键及最终目标路径。
     *
     * 只有当前记录仍属于该租约时才会移除路径所有权；已释放的租约会被直接忽略。
     * 该操作不挂起，调用方应在任务的 `finally` 块中执行。
     *
     * @param lease 由当前注册表签发的租约；重复释放会被忽略。
     */
    internal fun release(lease: QuickInFlightLease) = synchronized(lock) {
        if (lease.released) return@synchronized
        lease.keys.forEach(active::remove)
        lease.destinations.forEach { (path, key) ->
            if (activeDestinations[path] == key) activeDestinations.remove(path)
        }
        lease.markReleased()
    }
}

/**
 * 所有快捷下载任务共用的进程级在途注册表。
 * 该对象只协调当前进程内的任务，不提供跨进程互斥。
 */
internal object QuickDownloadInFlight {
    /** 当前进程共享的注册表实例。 */
    private val registry = QuickInFlightRegistry()

    /**
     * 在进程级注册表中过滤并登记下载计划。
     * 相同工作键的目标会被排除，不同工作键写入同一最终路径时抛出 [QuickPlanningException]。
     *
     * @param items 已完成规划的下载项。
     * @return 包含本次可执行计划及登记所有权的租约。
     */
    fun acquire(items: List<QuickDownloadItem>): QuickInFlightLease = registry.acquire(items)
}
