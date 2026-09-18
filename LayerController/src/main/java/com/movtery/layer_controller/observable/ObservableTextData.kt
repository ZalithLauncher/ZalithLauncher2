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

package com.movtery.layer_controller.observable

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.movtery.layer_controller.data.ButtonPosition
import com.movtery.layer_controller.data.ButtonSize
import com.movtery.layer_controller.data.TextData
import com.movtery.layer_controller.data.VisibilityType
import com.movtery.layer_controller.data.cloneNew
import com.movtery.layer_controller.event.EventHandler

/**
 * 可观察的TextData包装类
 */
open class ObservableTextData(data: TextData) : ObservableWidget() {
    val text = ObservableTranslatableString(data.text)
    val uuid: String = data.uuid
    var position by mutableStateOf(data.position)
    var buttonSize by mutableStateOf(data.buttonSize)
    var buttonStyle by mutableStateOf(data.buttonStyle)
    var textAlignment by mutableStateOf(data.textAlignment)
    var textBold by mutableStateOf(data.textBold)
    var textItalic by mutableStateOf(data.textItalic)
    var textUnderline by mutableStateOf(data.textUnderline)
    var visibilityType by mutableStateOf(data.visibilityType)

    override val behavior: InteractionBehavior
        get() = InteractionBehavior.Press //展示控件不参与触控

    override val internalRenderPosition: ButtonPosition
        get() = position

    override fun putRenderPosition(position: ButtonPosition) {
        this.position = position
    }

    override fun putWidgetSize(size: ButtonSize) {
        this.buttonSize = size
    }

    override val styleId: String?
        get() = buttonStyle

    override val widgetSize: ButtonSize
        get() = buttonSize

    override fun canTouch(): Boolean = false //不处理触摸事件

    override fun onCompositionStart(eventHandler: EventHandler?) {

    }

    override fun onCompositionDispose(eventHandler: EventHandler?) {

    }

    override fun onCheckVisibilityType(): VisibilityType {
        return visibilityType
    }

    override fun supportsDeepTouchDetection(): Boolean {
        return false //不处理触摸事件
    }

    override fun canProcess(): Boolean {
        return false //不处理触摸事件
    }

    override fun onTouchEvent(
        eventHandler: EventHandler,
        allLayers: List<ObservableControlLayer>,
        activeWidgets: List<ObservableWidget>,
        addThis: () -> Unit,
        consumeEvent: (Boolean) -> Unit
    ) {
        //不处理触摸事件
    }

    override fun isReleaseOnOutOfBounds(): Boolean {
        return false //不处理触摸事件
    }

    override fun onPointerBackInBounds(
        eventHandler: EventHandler,
        allLayers: List<ObservableControlLayer>
    ) {
        //不处理触摸事件
    }

    override fun onReleaseEvent(
        eventHandler: EventHandler,
        allLayers: List<ObservableControlLayer>
    ) {
        //不处理触摸事件
    }

    fun packText(): TextData {
        return TextData(
            text = text.pack(),
            uuid = uuid,
            position = position,
            buttonSize = buttonSize,
            buttonStyle = buttonStyle,
            textAlignment = textAlignment,
            textBold = textBold,
            textItalic = textItalic,
            textUnderline = textUnderline,
            visibilityType = visibilityType
        )
    }
}

fun ObservableTextData.cloneText(): ObservableTextData {
    return ObservableTextData(packText().cloneNew())
}