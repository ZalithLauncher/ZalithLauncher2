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

import com.movtery.zalithlauncher.utils.file.InvalidFilenameException
import com.movtery.zalithlauncher.utils.file.checkFilenameValidity

object FilenameValidator {
    /**
     * 校验文件名合法性
     * @throws FmFilenameException 非法时抛出，携带错误类别与细节
     */
    @Throws(FmFilenameException::class)
    fun check(name: String) {
        try {
            checkFilenameValidity(name)
        } catch (e: InvalidFilenameException) {
            throw FmFilenameException(
                message = e.message ?: "Invalid filename",
                type = when {
                    e.isLeadingOrTrailingSpace -> FmFilenameError.LEADING_OR_TRAILING_SPACE
                    e.isInvalidLength -> FmFilenameError.INVALID_LENGTH
                    else -> FmFilenameError.ILLEGAL_CHARACTERS
                },
                invalidLength = if (e.isInvalidLength) e.invalidLength else -1,
                illegalCharacters = if (e.containsIllegalCharacters()) e.illegalCharacters else null
            )
        }
    }

    /**
     * 验证文件名合法性
     * @return 校验失败时的错误异常，合法时为 null
     */
    fun verify(name: String): FmFilenameException? {
        return try {
            check(name)
            null
        } catch (e: FmFilenameException) {
            e
        }
    }
}
