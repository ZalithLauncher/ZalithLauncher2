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

package com.movtery.zalithlauncher.filemanager

import android.content.Context
import android.content.Intent
import com.movtery.zalithlauncher.filemanager.ui.FileManagerActivity

/**
 * 文件管理器启动接口
 */
object FileManagerLauncher {
    /**
     * “可访问范围目录”绝对路径
     */
    const val EXTRA_ROOT_PATH = "fm.extra.ROOT_PATH"
    /**
     * “当前访问目录”，需位于“可访问范围目录”之下
     */
    const val EXTRA_CURRENT_PATH = "fm.extra.CURRENT_PATH"
    /**
     * 日志根目录
     */
    const val EXTRA_LOGS_DIR = "fm.extra.LOGS_DIR"

    /**
     * 启动文件管理器
     * @param rootPath “可访问范围目录”绝对路径
     * @param currentPath “当前访问目录”，非法时回退根目录
     * @param logsDir 日志根目录
     */
    fun launch(
        context: Context,
        rootPath: String,
        currentPath: String? = null,
        logsDir: String? = null
    ) {
        require(rootPath.isNotBlank()) { "rootPath must not be blank" }

        val intent = Intent(context, FileManagerActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            putExtra(EXTRA_ROOT_PATH, rootPath)
            currentPath?.let { putExtra(EXTRA_CURRENT_PATH, it) }
            logsDir?.let { putExtra(EXTRA_LOGS_DIR, it) }
        }
        context.startActivity(intent)
    }
}