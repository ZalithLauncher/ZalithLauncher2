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

package com.movtery.zalithlauncher.game.version.export

import android.content.Context
import com.movtery.zalithlauncher.coroutine.TitledTask
import com.movtery.zalithlauncher.game.version.installed.Version
import java.io.File

abstract class AbstractExporter(
    val type: PackType
) {
    /**
     * 构建所需的导出任务
     */
    abstract fun MutableList<TitledTask>.buildTasks(
        context: Context,
        version: Version,
        info: ExportInfo,
        tempPath: File
    )

    /**
     * 整合包文件后缀
     */
    abstract val fileSuffix: String

    protected fun generateTargetRoot(
        file: File,
        rootPath: String,
        targetPath: String
    ): File {
        return File(targetPath, relativePath(file, rootPath))
    }

    /**
     * 获取一个文件的相对路径
     */
    protected fun relativePath(
        file: File,
        rootPath: String
    ): String {
        return file.absolutePath
            .removePrefix(rootPath)
            .removePrefix("/")
    }
}