/*
 * Zalith Launcher 2
 * Copyright (C) 2025-2026 MovTery <movtery228@qq.com> and contributors
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

package com.movtery.layer_controller.observable

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.movtery.layer_controller.data.lang.LocalizedString
import com.movtery.layer_controller.utils.compareLangTag
import java.util.Locale

class ObservableLocalizedString(
    private val string: LocalizedString
): Packable<LocalizedString?> {
    var languageTag by mutableStateOf(string.languageTag)
    var value by mutableStateOf(string.value)

    override fun pack(): LocalizedString? {
        if (languageTag.isEmpty() || languageTag.isBlank()) return null
        if (value.isEmpty() || value.isBlank()) return null
        return LocalizedString(
            languageTag = languageTag,
            value = value
        )
    }

    override fun isModified(): Boolean {
        val packedNew = pack() ?: return true
        return this.string.isModified(packedNew)
    }
}

/**
 * 尝试检查语言是否匹配
 */
fun ObservableLocalizedString.check(
    locale: Locale = Locale.getDefault()
): String? = value.takeIf {
    locale.compareLangTag(languageTag)
}