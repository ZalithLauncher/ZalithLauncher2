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

package com.movtery.zalithlauncher.filemanager.logic.extract

/**
 * 解压过程中检测到压缩包需要密码、或密码错误时抛出的异常
 * @param type 错误类型（需要密码 / 密码错误）
 */
class ArchivePasswordException(
    val type: Type,
    message: String = type.message,
    cause: Throwable? = null
) : Exception(message, cause) {
    /** 密码异常类型 */
    enum class Type(val message: String) {
        /** 压缩包已加密，尚未提供密码 */
        REQUIRED("Archive requires a password"),
        /** 提供的密码错误 */
        WRONG("Archive password is incorrect"),
    }
}
