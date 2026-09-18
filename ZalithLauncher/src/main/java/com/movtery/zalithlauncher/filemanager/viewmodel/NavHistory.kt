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

package com.movtery.zalithlauncher.filemanager.viewmodel

import java.nio.file.Path

/** 当前目录的后退/前进历史 */
class NavHistory(initialCurrent: Path) {

    private val backStack = ArrayDeque<Path>()
    private val forwardStack = ArrayDeque<Path>()
    private var current: Path = initialCurrent

    val canBack: Boolean get() = backStack.isNotEmpty()
    val canForward: Boolean get() = forwardStack.isNotEmpty()
    val currentPath: Path get() = current

    /** 进入一个新目录，清空前进栈 */
    fun navigate(to: Path) {
        if (to == current) return
        backStack.addLast(current)
        forwardStack.clear()
        current = to
    }

    /**
     * 后退
     * @return 上一个目录，或 null 表示无历史
     */
    fun back(): Path? {
        if (backStack.isEmpty()) return null
        val prev = backStack.removeLast()
        forwardStack.addLast(current)
        current = prev
        return prev
    }

    /**
     * 前进
     * @return 撤销上一次后退，或 null 表示无可前进
     */
    fun forward(): Path? {
        if (forwardStack.isEmpty()) return null
        val next = forwardStack.removeLast()
        backStack.addLast(current)
        current = next
        return next
    }

    /**
     * 目录被删除后清理历史
     */
    fun pruneDeleted(deleted: Path) {
        fun inside(p: Path): Boolean {
            return p == deleted || p.startsWith(deleted)
        }
        backStack.removeAll { inside(it) }
        forwardStack.removeAll { inside(it) }
    }
}