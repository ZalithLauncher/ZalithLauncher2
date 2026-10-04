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

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * 互斥进程名单匹配回归：
 * :filemanager 被主界面以 BIND_AUTO_CREATE 常驻绑定、杀后必被系统重建，
 * 一旦误入"必须消失"名单，安装前的等待将永远无法满足（Forge 安装卡死的根因）。
 */
class JvmExclusiveProcessTest {

    private val main = "com.movtery.zalithlauncher.v2.debug"

    @Test
    fun `jvm and game processes are exclusive`() {
        assertTrue(isJvmExclusiveProcess("$main:jvm", main))
        assertTrue(isJvmExclusiveProcess("$main:game", main))
    }

    @Test
    fun `filemanager and other sub processes are not exclusive`() {
        assertFalse(isJvmExclusiveProcess("$main:filemanager", main))
        assertFalse(isJvmExclusiveProcess(main, main))
        assertFalse(isJvmExclusiveProcess("$main:other", main))
    }

    @Test
    fun `foreign packages never match`() {
        //精确匹配主进程名+后缀，不能是 startsWith 前缀匹配
        assertFalse(isJvmExclusiveProcess("com.other.app:jvm", main))
        assertFalse(isJvmExclusiveProcess("${main}evil:jvm", main))
    }
}
