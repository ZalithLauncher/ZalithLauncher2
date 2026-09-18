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

/** 文件名错误类别 */
enum class FmFilenameError {
    /** 包含非法字符 / 路径穿越序列（如 `..`、开头 `/` 或 `\`） */
    ILLEGAL_CHARACTERS,

    /** 名称过长（超过 255 字符） */
    INVALID_LENGTH,

    /** 以空格开头或结尾 */
    LEADING_OR_TRAILING_SPACE,

    /** 与同目录下已有条目重名 */
    NAME_CONFLICT
}

/**
 * 文件名校验异常
 * @param message 错误描述
 * @param type 错误类别
 * @param invalidLength 非法名称长度（[FmFilenameError.INVALID_LENGTH] 时有效，否则为 -1）
 * @param illegalCharacters 命中的非法字符（[FmFilenameError.ILLEGAL_CHARACTERS] 时有效，否则为 null）
 */
class FmFilenameException(
    message: String,
    val type: FmFilenameError,
    val invalidLength: Int = -1,
    val illegalCharacters: String? = null
) : Exception(message)
