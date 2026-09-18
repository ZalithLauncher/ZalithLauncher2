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

package com.movtery.zalithlauncher.filemanager.logic

import com.movtery.zalithlauncher.filemanager.logic.ops.deleteRecursivePath
import java.nio.file.Files
import java.nio.file.Path

private const val COMPRESS_SUBDIR = "fileManagerCompress"
private const val EXTRACT_SUBDIR = "fileManagerExtract"
private const val IMPORT_SUBDIR = "fileManagerImport"

/** 临时工作区 */
class TempWorkspace(private val cacheRoot: Path) {
    /** 生成压缩用的临时文件路径 */
    fun compressTempFile(extension: String): Path {
        val dir = cacheRoot.resolve(COMPRESS_SUBDIR)
        runCatching {
            Files.createDirectories(dir)
        }
        return dir.resolve("tmp_${System.nanoTime()}.$extension")
    }

    /** 生成解压用的临时目录路径 */
    fun extractTempDir(): Path = uniqueDir(EXTRACT_SUBDIR)

    /** 生成导入用的临时目录路径 */
    fun importTempDir(): Path = uniqueDir(IMPORT_SUBDIR)

    /** 递归删除临时文件或目录 */
    suspend fun delete(path: Path) {
        deleteRecursivePath(path)
    }

    /** 判断路径是否位于临时工作区内 */
    fun isInside(path: Path): Boolean {
        val normalized = path.normalize().toAbsolutePath()
        return normalized.startsWith(cacheRoot)
    }

    private fun uniqueDir(sub: String): Path {
        val base = cacheRoot.resolve(sub)
        runCatching {
            Files.createDirectories(base)
        }
        return base.resolve("tmp_${System.nanoTime()}")
    }
}
