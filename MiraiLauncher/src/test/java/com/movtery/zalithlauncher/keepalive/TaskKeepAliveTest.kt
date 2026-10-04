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

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

/**
 * 保活计数回归：acquire/release 必须严格配对。
 * 若计数下溢会出现"任务仍在运行但前台服务被停掉"，若计数泄漏则会出现"任务已结束但服务无法停止"。
 */
class TaskKeepAliveTest {

    @Before
    fun setUp() = TaskKeepAlive.reset()

    @After
    fun tearDown() = TaskKeepAlive.reset()

    @Test
    fun `acquire and release keep the count balanced`() {
        assertEquals(0, TaskKeepAlive.count)

        TaskKeepAlive.acquire()
        assertEquals(1, TaskKeepAlive.count)
        TaskKeepAlive.acquire()
        assertEquals(2, TaskKeepAlive.count)

        TaskKeepAlive.release()
        assertEquals(1, TaskKeepAlive.count)
        TaskKeepAlive.release()
        assertEquals(0, TaskKeepAlive.count)
    }

    @Test
    fun `extra release never underflows`() {
        TaskKeepAlive.release()
        assertEquals(0, TaskKeepAlive.count)

        TaskKeepAlive.acquire()
        TaskKeepAlive.release()
        //多减一次也不能变成负数
        TaskKeepAlive.release()
        assertEquals(0, TaskKeepAlive.count)
    }

    @Test
    fun `reset clears every acquired reference`() {
        repeat(5) { TaskKeepAlive.acquire() }
        assertEquals(5, TaskKeepAlive.count)

        TaskKeepAlive.reset()
        assertEquals(0, TaskKeepAlive.count)
    }

    @Test
    fun `concurrent acquire and release conserve the count`() = runBlocking<Unit> {
        val workers = 32

        coroutineScope {
            repeat(workers) {
                launch(Dispatchers.Default) {
                    TaskKeepAlive.acquire()
                }
            }
        }
        assertEquals(workers, TaskKeepAlive.count)

        coroutineScope {
            repeat(workers) {
                launch(Dispatchers.Default) {
                    TaskKeepAlive.release()
                }
            }
        }
        assertEquals(0, TaskKeepAlive.count)
    }
}
