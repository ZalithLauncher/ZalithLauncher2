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

package com.movtery.zalithlauncher.filemanager.logic.entry

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.attribute.BasicFileAttributes

/**
 * 文件 / 目录条目
 * @param path 归一化后的绝对路径
 * @param archiveType 可解压压缩包类型，非压缩包为 null
 */
data class FmEntry(
    val path: Path,
    val name: String,
    val isDirectory: Boolean,
    val size: Long,
    val modifiedMs: Long,
    val hidden: Boolean,
    val archiveType: ArchiveType?,
    val writable: Boolean
) {
    val isFile: Boolean get() = !isDirectory
}

/** 压缩包后缀类型 */
enum class ArchiveType {
    ZIP,
    SEVEN_Z,
    TAR;

    companion object {
        fun knownSuffix() = listOf(".tar.gz", ".tar.bz2", ".tar.xz", ".zip", ".7z", ".tar")

        fun of(name: String): ArchiveType? {
            val lower = name.lowercase()
            return when {
                lower.endsWith(".zip") -> ZIP
                lower.endsWith(".7z") -> SEVEN_Z
                lower.endsWith(".tar.gz") || lower.endsWith(".tgz") -> TAR
                lower.endsWith(".tar.bz2") || lower.endsWith(".tbz2") -> TAR
                lower.endsWith(".tar.xz") || lower.endsWith(".txz") -> TAR
                lower.endsWith(".tar") -> TAR
                else -> null
            }
        }
    }
}

/**
 * 单次浏览结果
 * @param ancestors 从根目录到当前目录的祖先链（含根，不含当前）
 * @param entries 当前目录内全部条目（含隐藏条目）
 * @param writable 当前目录是否可写
 */
data class FmListResult(
    val rootDir: Path,
    val currentDir: Path,
    val ancestors: List<Path>,
    val entries: List<FmEntry>,
    val hasSubdirectory: Boolean,
    val writable: Boolean
)

/** 浏览失败时抛出的异常 */
class BrowseException(message: String, cause: Throwable? = null) : Exception(message, cause)


internal fun Path.toFmEntry(
    attrs: BasicFileAttributes,
): FmEntry {
    val name = fileName?.toString() ?: toString()
    return FmEntry(
        path = this,
        name = name,
        isDirectory = attrs.isDirectory,
        size = if (attrs.isRegularFile) attrs.size() else 0L,
        modifiedMs = attrs.lastModifiedTime().toMillis(),
        hidden = name.startsWith("."),
        archiveType = if (attrs.isRegularFile) ArchiveType.of(name) else null,
        writable = Files.isWritable(this)
    )
}