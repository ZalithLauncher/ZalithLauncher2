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

package com.movtery.layer_controller.data

import com.movtery.layer_controller.data.lang.TranslatableString
import com.movtery.layer_controller.observable.Modifiable
import com.movtery.layer_controller.utils.getAButtonUUID
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * @param text 按钮显示的文本
 */
@Serializable
data class TextData(
    @SerialName("text")
    val text: TranslatableString,
    @SerialName("uuid")
    val uuid: String,
    @SerialName("position")
    val position: ButtonPosition,
    @SerialName("buttonSize")
    val buttonSize: ButtonSize,
    @SerialName("buttonStyle")
    val buttonStyle: String? = null,
    @SerialName("textAlignment")
    val textAlignment: TextAlignment = TextAlignment.Left,
    @SerialName("textBold")
    val textBold: Boolean = false,
    @SerialName("textItalic")
    val textItalic: Boolean = false,
    @SerialName("textUnderline")
    val textUnderline: Boolean = false,
    @SerialName("visibilityType")
    val visibilityType: VisibilityType
): Widget, Modifiable<TextData> {
    override fun isModified(other: TextData): Boolean {
        return this.text.isModified(other.text) ||
                this.uuid != other.uuid ||
                this.position.isModified(other.position) ||
                this.buttonSize.isModified(other.buttonSize) ||
                this.buttonStyle != other.buttonStyle ||
                this.textAlignment != other.textAlignment ||
                this.textBold != other.textBold ||
                this.textItalic != other.textItalic ||
                this.textUnderline != other.textUnderline ||
                this.visibilityType != other.visibilityType
    }
}

/**
 * 克隆一个新的TextData对象（UUID、位置不同）
 */
fun TextData.cloneNew(): TextData = TextData(
    text = this.text,
    uuid = getAButtonUUID(),
    position = CenterPosition,
    buttonSize = buttonSize,
    buttonStyle = buttonStyle,
    textAlignment = textAlignment,
    textBold = textBold,
    textItalic = textItalic,
    textUnderline = textUnderline,
    visibilityType = visibilityType
)