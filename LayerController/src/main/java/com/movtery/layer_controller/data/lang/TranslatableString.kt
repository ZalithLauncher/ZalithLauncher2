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

package com.movtery.layer_controller.data.lang

import com.movtery.layer_controller.observable.Modifiable
import com.movtery.layer_controller.observable.isModified
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.Locale

/**
 * 多语言集合，可根据当前系统语言匹配指定的字符串
 * @param default 默认字符串，如果未找到匹配项，则使用它
 */
@Serializable
data class TranslatableString(
    @SerialName("default")
    val default: String,
    @SerialName("matchQueue")
    val matchQueue: List<LocalizedString>
): Modifiable<TranslatableString> {
    fun translate(locale: Locale = Locale.getDefault()): String {
        matchQueue.forEach { ls ->
            val value = ls.check(locale)
            if (value != null) return value
        }
        return default
    }

    override fun isModified(other: TranslatableString): Boolean {
        return this.default != other.default ||
                this.matchQueue.isModified(other.matchQueue)
    }
}

val EmptyTranslatableString = TranslatableString("", emptyList())

fun createTranslatable(default: String, vararg matchQueue: LocalizedString): TranslatableString {
    return TranslatableString(default = default, matchQueue = matchQueue.toList())
}