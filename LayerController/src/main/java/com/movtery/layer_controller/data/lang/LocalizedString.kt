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
import com.movtery.layer_controller.utils.compareLangTag
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.Locale

/**
 * 本地化显示字符串
 * @param languageTag 语言标签
 */
@Serializable
data class LocalizedString(
    @SerialName("language_tag")
    val languageTag: String,
    @SerialName("value")
    val value: String
): Modifiable<LocalizedString> {
    override fun isModified(other: LocalizedString): Boolean {
        return this.languageTag != other.languageTag ||
                this.value != other.value
    }
}

val EmptyLocalizedString = LocalizedString(languageTag = "", value = "")

/**
 * 尝试检查语言是否匹配
 */
fun LocalizedString.check(
    locale: Locale = Locale.getDefault()
): String? = value.takeIf {
    locale.compareLangTag(languageTag)
}