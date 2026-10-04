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

package com.movtery.zalithlauncher.filemanager.logic.compress

import java.nio.file.Path

/**
 * 压缩格式
 */
enum class CompressFormat(val extension: String, val mimeType: String) {
    ZIP("zip", "application/zip"),
    SEVEN_Z("7z", "application/x-7z-compressed"),
    TAR("tar", "application/x-tar");

    /** 输出文件名的标准后缀 */
    val suffix: String get() = ".$extension"

    /** 该格式的默认压缩方法 */
    val defaultMethod: CompressMethod
        get() = when (this) {
            ZIP -> CompressMethod.DEFLATE
            SEVEN_Z -> CompressMethod.LZMA2
            TAR -> CompressMethod.TAR_POSIX
        }
}

/**
 * 压缩方法
 */
enum class CompressMethod(val displayName: String) {
    /** Zip：不压缩直接存储；7Z：COPY 不压缩 */
    STORE("Store"),
    /** Zip 默认 */
    DEFLATE("Deflate"),

    /** 7Z 默认 */
    LZMA2("LZMA2"),
    BZIP2("BZIP2"),

    /** TAR 长文件名处理：GNU 格式 */
    TAR_GNU("GNU"),
    /** TAR 长文件名处理：POSIX（PAX）格式 */
    TAR_POSIX("POSIX");

    companion object {
        /** ZIP 支持的方法 */
        val zipMethods = listOf(STORE, DEFLATE)
        /** 7Z 支持的方法 */
        val sevenZMethods = listOf(LZMA2, BZIP2)
        /** TAR 支持的方法（长文件名处理格式） */
        val tarMethods = listOf(TAR_GNU, TAR_POSIX)
    }
}

/**
 * 压缩参数。
 * @param format 压缩格式
 * @param method 压缩方法，null 表示使用格式默认
 * @param level 压缩等级（1-9），null 表示格式默认
 * @param password 密码，TAR 不支持
 */
data class CompressOptions(
    val format: CompressFormat,
    val method: CompressMethod? = null,
    val level: Int? = null,
    val password: String? = null
)

/**
 * 压缩结果汇总。
 */
data class CompressSummary(
    val outputPath: Path,
    val entryCount: Int
)
