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
package com.movtery.zalithlauncher.game.download.modpack.install

/**
 * 整合包不受支持，无法导入时，抛出这个异常
 * @param reason 不受支持的原因
 */
class PackNotSupportedException(
    val reason: UnsupportedPackReason
) : RuntimeException(
    reason.reasonText
)

/**
 * 导致启动器判断整合包不受支持的原因
 */
enum class UnsupportedPackReason(
    val reasonText: String
) {
    /**
     * 压缩包损坏或解压失败
     */
    CorruptedArchive("The archive is corrupted or failed to extract."),

    /**
     * 不支持的整合包格式
     */
    UnsupportedFormat("The modpack format is not supported.")
}