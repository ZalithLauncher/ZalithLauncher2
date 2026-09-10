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

import java.util.concurrent.atomic.AtomicLong

/**
 * 单个文件内部的分段链表：每段的右边界由后继段的起点隐式定义，
 * 运行中可在任意段的剩余区间中部插入新段（动态分块）。
 */
class SegmentChain(initialSize: Long) {
    inner class Segment(
        @JvmField val start: Long,
        next: Segment?
    ) {
        /** 本段已持久化的字节数；写盘成功后才累加，崩溃后可从断点续传 */
        val done = AtomicLong(0L)

        @Volatile
        var next: Segment? = next

        fun position(): Long = start + done.get()

        fun endOr(tailEnd: Long): Long =
            next?.start ?: tailEnd

        fun remaining(tailEnd: Long): Long = (endOr(tailEnd) - position()).coerceAtLeast(0L)
    }

    /** 文件总大小；从响应头得知后由 [-1] 升级为确定值 */
    @Volatile
    var totalSize: Long = initialSize
        private set

    private val head = Segment(0L, null)

    /** 总大小未知时由读到 EOF 的那一端置位 */
    @Volatile
    var eof = false

    val first: Segment get() = head

    /** 末尾分段的右边界：大小未知时视为无穷远 */
    fun tailEnd(): Long = if (totalSize < 0) Long.MAX_VALUE else totalSize

    fun snapshot(): List<Segment> {
        val result = ArrayList<Segment>(4)
        var current: Segment? = head
        while (current != null) {
            result.add(current)
            current = current.next
        }
        return result
    }

    /**
     * 在 [prev] 段的剩余区间的 [offset] 处切出新的尾段；
     * 写入顺序保证并发读取者最多短暂漏看新段，不会读到越界数据。
     */
    fun split(prev: Segment, offset: Long): Segment {
        val created = Segment(offset, prev.next)
        prev.next = created
        return created
    }

    fun totalDone(): Long = snapshot().sumOf { it.done.get() }

    /** 尚有未落盘区间的分段列表；无界尾段以 EOF 为完成标志 */
    fun pendingSegments(): List<Segment> {
        val tail = tailEnd()
        return snapshot().filter {
            val end = it.endOr(tail)
            if (end == Long.MAX_VALUE) !eof else it.position() < end
        }
    }

    fun isComplete(): Boolean {
        if (totalSize < 0) return eof
        return snapshot().all { it.remaining(totalSize) == 0L }
    }

    /**
     * 以响应头声明的总大小为准刷新边界；
     * 声明值优先于调用方注册的期望值——期望值可能过时
     * （例如启动器替换库版本后未同步大小的场景），最终完整性由 sha1 兜底。
     */
    fun adoptTotal(total: Long) {
        if (total > 0 && total != totalSize) totalSize = total
    }

    fun eofReached() {
        eof = true
    }

    /**
     * 摊平为单段并清零进度；用于降级阶段以无 Range 的完整响应重新消费。
     * 仅允许在没有存活工作线程时调用。
     */
    fun resetForSingleStream() {
        head.next = null
        snapshot().forEach { it.done.set(0L) }
        eof = false
    }

    companion object {
        /** 分裂时新段拿走原段剩余量的比例，其余留给持有连接的旧段 */
        const val TAIL_FRACTION = 0.4

        /**
         * 计算分裂点：给定段内已下载位置 [position]、段右边界 [endExclusive]、
         * 新段至少要保证的字节数 [minTailSize]，返回新段起点；不足以拆出合规新段时返回 null。
         */
        fun splitOffset(position: Long, endExclusive: Long, minTailSize: Long): Long? {
            val remaining = endExclusive - position
            if (remaining <= minTailSize) return null
            val tailStart = endExclusive - (remaining * TAIL_FRACTION).toLong().coerceAtLeast(minTailSize)
            return tailStart.takeIf { it > position }
        }
    }
}
