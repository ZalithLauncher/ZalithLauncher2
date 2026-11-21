/*
 * Zalith Launcher 2
 * Copyright (C) 2025-2026 MovTery <movtery228@qq.com> and contributors.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/gpl-3.0.txt>.
 */

package com.movtery.zalithlauncher.game.input

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Test
import java.io.File
import java.util.concurrent.TimeUnit

/**
 * 编译并运行 jni/glfw/tests/event_wait_test.c，链接的是真实的
 * 生产源码 jni/glfw/event_wait.c（libpojavexec 内的 GLFW 等待原语）。
 *
 * 覆盖：超时截止时间、已有事件立即返回、跨线程唤醒、事件泵期间
 * 到达的事件保持待处理、重复 reset/stop、信号中断沿用同一截止时间、
 * 多轮等待/发布竞争不丢唤醒、等待线程 CPU 时间远小于墙钟时间。
 *
 * 仅在存在宿主 C 编译器的类 Unix 环境执行；CI（Linux）必须能跑，
 * 缺编译器时在 Linux 上直接失败而不是跳过。
 */
class EventWaitNativeTest {

    private fun moduleDir(): File {
        var dir = File(System.getProperty("user.dir") ?: ".").absoluteFile
        repeat(5) {
            if (File(dir, "src/main/jni/glfw/event_wait.c").isFile) return dir
            dir = dir.parentFile ?: return@repeat
        }
        throw IllegalStateException("Cannot locate ZalithLauncher module dir from ${System.getProperty("user.dir")}")
    }

    private fun findCompiler(): String? =
        listOf("cc", "gcc", "clang").firstOrNull { cmd ->
            try {
                ProcessBuilder(cmd, "--version")
                    .redirectErrorStream(true)
                    .start()
                    .waitFor(15, TimeUnit.SECONDS)
            } catch (_: Exception) {
                false
            }
        }

    @Test
    fun `blocking glfw event wait passes native regression suite`() {
        val isLinux = System.getProperty("os.name").lowercase().contains("linux")
        val compiler = findCompiler()
        assumeTrue(
            "No host C compiler available; native event-wait suite skipped",
            compiler != null
        )
        if (isLinux) {
            assertTrue("Linux CI must provide a host C compiler (cc/gcc/clang)", compiler != null)
        }

        val module = moduleDir()
        val glfwDir = File(module, "src/main/jni/glfw")
        val workDir = File.createTempFile("event_wait_test", ".dir").let {
            it.delete(); it.mkdirs(); it
        }
        try {
            val binary = File(workDir, "event_wait_test")
            val compile = ProcessBuilder(
                compiler,
                "-O2", "-Wall", "-Wextra", "-std=gnu11", "-pthread",
                "-I", glfwDir.absolutePath,
                File(glfwDir, "event_wait.c").absolutePath,
                File(glfwDir, "tests/event_wait_test.c").absolutePath,
                "-o", binary.absolutePath
            ).redirectErrorStream(true).start()
            val compileOutput = compile.inputStream.bufferedReader().readText()
            assertEquals("event_wait_test compile failed:\n$compileOutput", 0, compile.waitFor())

            val run = ProcessBuilder(binary.absolutePath)
                .redirectErrorStream(true)
                .start()
            val output = StringBuilder()
            val reader = Thread {
                run.inputStream.bufferedReader().forEachLine { output.appendLine(it) }
            }
            reader.start()
            // 信号中断用例最长 ~0.4s，压力用例 ~2s 上限，整体给足余量
            val finished = run.waitFor(60, TimeUnit.SECONDS)
            assertTrue("event_wait_test timed out:\n$output", finished)
            reader.join(5000)

            assertEquals("native event wait failures:\n$output", 0, run.exitValue().toLong())
            assertTrue(
                "suite must report a clean summary",
                output.toString().contains("all passed")
            )
        } finally {
            workDir.deleteRecursively()
        }
    }
}
