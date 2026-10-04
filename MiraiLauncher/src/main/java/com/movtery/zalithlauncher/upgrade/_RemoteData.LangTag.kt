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

package com.movtery.zalithlauncher.upgrade

import com.movtery.zalithlauncher.utils.compareLangTag
import java.util.Locale

/**
 * 根据当前系统语言寻找合适的Body
 */
fun RemoteData.findCurrentBody(
    locale: Locale
): RemoteData.RemoteBody? {
    return bodies.sortedByDescending {
        it.language.contains("_")
    }.find { body ->
        locale.compareLangTag(body.language)
    }
}

/**
 * 根据当前系统语言寻找合适的网盘链接，若未找到则尝试匹配默认网盘链接
 */
fun RemoteData.getCurrentCouldDrive(
    locale: Locale
): RemoteData.CloudDrive? {
    return cloudDrives.sortedByDescending {
        it.language.contains("_")
    }.find { drive ->
        locale.compareLangTag(drive.language)
    } ?: defaultCloudDrive?.takeIf {
        //如果是 NULL，则是全区域可用
        //否则根据语言决定是否可用
        it.language == "NULL" || locale.compareLangTag(it.language)
    }
}