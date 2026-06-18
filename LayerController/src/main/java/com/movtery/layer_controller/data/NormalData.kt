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

package com.movtery.layer_controller.data

import com.movtery.layer_controller.data.lang.TranslatableString
import com.movtery.layer_controller.event.ClickEvent
import com.movtery.layer_controller.observable.Modifiable
import com.movtery.layer_controller.observable.isModified
import com.movtery.layer_controller.utils.getAButtonUUID
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * @param clickEvents 点击事件组
 * @param isSwipple 滑动可与周围的按钮联动操作
 * @param isPenetrable 是否允许将触摸事件向下穿透
 * @param isToggleable 是否用开关的形式切换按下状态
 */
@Serializable
data class NormalData(
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
    val visibilityType: VisibilityType,
    @SerialName("clickEvents")
    private var _clickEvents: List<ClickEvent> = emptyList(),
    @SerialName("isSwipple")
    val isSwipple: Boolean,
    @SerialName("isPenetrable")
    val isPenetrable: Boolean,
    @SerialName("isToggleable")
    val isToggleable: Boolean
): Widget, Modifiable<NormalData> {
    val clickEvents: List<ClickEvent> get() = _clickEvents

    init {
        _clickEvents = _clickEvents.filterValidEvent()
    }

    override fun isModified(other: NormalData): Boolean {
        return this.text.isModified(other.text) ||
                this.uuid != other.uuid ||
                this.position.isModified(other.position) ||
                this.buttonSize.isModified(other.buttonSize) ||
                this.buttonStyle != other.buttonStyle ||
                this.textAlignment != other.textAlignment ||
                this.textBold != other.textBold ||
                this.textItalic != other.textItalic ||
                this.textUnderline != other.textUnderline ||
                this.visibilityType != other.visibilityType ||
                this._clickEvents.isModified(other._clickEvents) ||
                this.isSwipple != other.isSwipple ||
                this.isPenetrable != other.isPenetrable ||
                this.isToggleable != other.isToggleable
    }
}

/**
 * 过滤出有效的点击事件
 */
internal fun List<ClickEvent>.filterValidEvent(): List<ClickEvent> {
    var foundValidSendText = false
    return filter { event ->
        if (event.type == ClickEvent.Type.SendText) {
            if (!foundValidSendText && event.key.isNotEmpty()) {
                foundValidSendText = true
                true
            } else {
                false
            }
        } else {
            true
        }
    }
}

/**
 * 克隆一个新的NormalData对象（UUID、位置不同）
 */
fun NormalData.cloneNew(): NormalData = NormalData(
    text = this.text,
    uuid = getAButtonUUID(),
    position = CenterPosition,
    buttonSize = buttonSize,
    buttonStyle = buttonStyle,
    textAlignment = textAlignment,
    textBold = textBold,
    textItalic = textItalic,
    textUnderline = textUnderline,
    visibilityType = visibilityType,
    _clickEvents = clickEvents,
    isSwipple = isSwipple,
    isPenetrable = isPenetrable,
    isToggleable = isToggleable
)