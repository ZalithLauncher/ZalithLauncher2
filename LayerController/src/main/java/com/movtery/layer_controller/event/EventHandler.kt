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

package com.movtery.layer_controller.event

import com.movtery.layer_controller.observable.ObservableControlLayer

/**
 * 专用于处理控件触发事件的处理器
 * @param handle 处理事件
 */
class EventHandler(
    private val handle: (event: ClickEvent, pressed: Boolean) -> Unit = { _, _ -> }
) {
    /**
     * 普通的按钮按下事件
     * @param handle 决定是否处理该事件
     */
    internal fun onKeyPressed(
        clickEvents: List<ClickEvent>,
        isPressed: Boolean,
        handle: (ClickEvent) -> Boolean = { true }
    ) {
        for (event in clickEvents) {
            if (handle(event)) handle(event, isPressed)
        }
    }

    /**
     * 处理切换布局隐藏显示
     */
    internal fun onSwitchLayer(
        clickEvent: ClickEvent,
        allLayers: List<ObservableControlLayer>,
        switch: (ObservableControlLayer) -> Unit,
        show: (ObservableControlLayer) -> Unit,
        hide: (ObservableControlLayer) -> Unit
    ) {
        fun findLayer() = allLayers.find { it.uuid == clickEvent.key }

        when (clickEvent.type) {
            ClickEvent.Type.SwitchLayer -> findLayer()?.let { switch(it) }
            ClickEvent.Type.ShowLayer -> findLayer()?.let { show(it) }
            ClickEvent.Type.HideLayer -> findLayer()?.let { hide(it) }
            else -> {}
        }
    }
}