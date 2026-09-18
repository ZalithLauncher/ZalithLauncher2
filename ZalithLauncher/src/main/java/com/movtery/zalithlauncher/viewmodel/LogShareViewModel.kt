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

package com.movtery.zalithlauncher.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import java.io.File

/**
 * 游戏日志分享菜单状态管理 ViewModel
 */
class LogShareViewModel : ViewModel() {
    /** 当前待分享的日志文件 */
    var currentLogFile by mutableStateOf<File?>(null)
        private set

    /** 是否显示日志操作菜单 */
    var showMenu by mutableStateOf(false)
        private set

    /**
     * 打开日志分享菜单
     */
    fun openMenu(logFile: File) {
        currentLogFile = logFile
        showMenu = true
    }

    /**
     * 关闭日志分享菜单
     */
    fun closeMenu() {
        showMenu = false
    }

    /**
     * 重置状态
     */
    fun reset() {
        showMenu = false
        currentLogFile = null
    }
}
