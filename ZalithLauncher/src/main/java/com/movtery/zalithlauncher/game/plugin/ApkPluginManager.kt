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

package com.movtery.zalithlauncher.game.plugin

import android.content.Context
import android.content.pm.ApplicationInfo
import android.os.Bundle
import com.movtery.zalithlauncher.utils.string.isNotEmptyOrBlank

abstract class ApkPluginManager {
    abstract fun parseApkPlugin(
        context: Context,
        info: ApplicationInfo,
        loaded: (ApkPlugin) -> Unit = {}
    )

    protected fun Bundle.getVersionString(key: String): String? {
        return if (containsKey(key)) {
            runCatching {
                when (val o = get(key)) {
                    is String -> o
                    is Number -> o.toString()
                    else -> null
                }
            }.getOrNull()?.takeIf { it.isNotEmptyOrBlank() }
        } else null
    }
}