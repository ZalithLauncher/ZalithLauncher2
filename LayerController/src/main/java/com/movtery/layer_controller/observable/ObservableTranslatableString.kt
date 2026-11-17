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
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.movtery.layer_controller.data.lang.EmptyLocalizedString
import com.movtery.layer_controller.data.lang.LocalizedString
import com.movtery.layer_controller.data.lang.TranslatableString
import java.util.Locale

class ObservableTranslatableString(
    private val text: TranslatableString
): Packable<TranslatableString> {
    var default by mutableStateOf(text.default)
    var matchQueue = mutableStateListOf<ObservableLocalizedString>()
        .apply { addAll(getMatchQueues()) }
        private set

    private fun getMatchQueues() = text.matchQueue.map { ObservableLocalizedString(it) }

    /**
     * 重置状态
     */
    fun reset() {
        default = text.default
        matchQueue.clear()
        matchQueue.addAll(getMatchQueues())
    }

    fun translate(locale: Locale = Locale.getDefault()): String {
        matchQueue.sortedByDescending {
            it.languageTag.contains("-")
        }.forEach { ls ->
            val value = ls.check(locale)
            if (value != null) return value
        }
        return default
    }

    /**
     * 移除可翻译的字符串
     */
    fun deleteLocalizedString(string: ObservableLocalizedString) {
        matchQueue.removeIf {
            string.languageTag == it.languageTag && string.value == it.value
        }
    }

    /**
     * 添加可翻译的字符串
     */
    fun addLocalizedString(string: LocalizedString = EmptyLocalizedString) {
        if (matchQueue.any { it.languageTag == string.languageTag && it.value == string.value }) return
        matchQueue.add(ObservableLocalizedString(string))
    }

    override fun pack(): TranslatableString {
        return TranslatableString(
            default = default,
            matchQueue = matchQueue.mapNotNull { it.pack() }
        )
    }

    override fun isModified(): Boolean {
        return this.text.isModified(pack())
    }
}