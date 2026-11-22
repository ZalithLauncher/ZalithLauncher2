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

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.util.zip.ZipFile

/**
 * 校验游戏 JVM 实际加载的 LWJGL 组件（assets/app_runtime/lwjgl/<版本>/
 * lwjgl-<版本>-merged-modules.jar）里打包的 org.lwjgl.glfw.GLFW
 * 已包含阻塞事件等待实现。
 *
 * 3.3.3 与 3.4.1 两个组件都会按版本清单被真实加载
 * （见 LaunchArgs.lwjglVersionDir），所以两份都必须带上新实现；
 * 该测试防止只改了源码却忘记重新生成组件 jar 的回归。
 */
class PackagedGlfwEventWaitTest {

    private fun moduleDir(): File {
        var dir = File(System.getProperty("user.dir") ?: ".").absoluteFile
        repeat(5) {
            if (File(dir, "src/main/assets/app_runtime").isDirectory) return dir
            dir = dir.parentFile ?: return@repeat
        }
        throw IllegalStateException("Cannot locate ZalithLauncher module dir from ${System.getProperty("user.dir")}")
    }

    private fun glfwClassBytes(version: String): ByteArray {
        val jar = File(moduleDir(), "src/main/assets/app_runtime/lwjgl/$version/lwjgl-$version-merged-modules.jar")
        assertTrue("LWJGL $version merged component jar is missing: ${jar.path} (run :LWJGL:lwjgl-$version:jar)", jar.isFile)
        ZipFile(jar).use { zip ->
            val entry = zip.getEntry("org/lwjgl/glfw/GLFW.class")
            assertTrue("org/lwjgl/glfw/GLFW.class missing from $version merged jar", entry != null)
            return zip.getInputStream(entry).readBytes()
        }
    }

    private fun assertThatClassContains(version: String, bytes: ByteArray, symbol: String) {
        // 常量池中的 UTF-8 条目是明文，直接扫描即可覆盖“源码改了但 jar 没重打包”的回归
        val text = bytes.toString(Charsets.ISO_8859_1)
        assertTrue("LWJGL $version packaged GLFW.class lacks '$symbol' — component jar is stale", symbol in text)
    }

    @Test
    fun `glfw 3_3_3 component ships the blocking event wait`() {
        val bytes = glfwClassBytes("3.3.3")
        listOf(
            "gwResetEventWait", "gwStopEventWait", "gwPostEmptyEvent",
            "gwWaitEvents", "(DZ)I", "waitEvents", "eventError"
        ).forEach { assertThatClassContains("3.3.3", bytes, it) }
    }

    @Test
    fun `glfw 3_4_1 component ships the blocking event wait`() {
        val bytes = glfwClassBytes("3.4.1")
        listOf(
            "gwResetEventWait", "gwStopEventWait", "gwPostEmptyEvent",
            "gwWaitEvents", "(DZ)I", "waitEvents", "eventError"
        ).forEach { assertThatClassContains("3.4.1", bytes, it) }
    }

    @Test
    fun `both components agree with lwjglVersionDir selection`() {
        // LaunchArgs.lwjglVersionDir：>=3.4.1 走 3.4.1 组件，其余走 3.3.3 组件。
        // 两个分支都必须存在组件 jar，保证任意版本清单都有阻塞等待可用。
        listOf("3.3.3", "3.4.1").forEach { version ->
            val jar = File(moduleDir(), "src/main/assets/app_runtime/lwjgl/$version/lwjgl-$version-merged-modules.jar")
            assertTrue("component jar for $version must be committed with the launcher", jar.isFile)
        }
    }
}
