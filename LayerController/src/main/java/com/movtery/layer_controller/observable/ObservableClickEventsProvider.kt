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

import com.movtery.layer_controller.data.JoystickDirection
import com.movtery.layer_controller.event.ClickEvent

/**
 * 可观察控件的点击事件编辑提供器
 */
abstract class ObservableClickEventsProvider {
    abstract val clickEvents: List<ClickEvent>
    abstract fun onRemoveAllEvents(events: List<ClickEvent>)
    abstract fun onRemoveAllEvents(type: ClickEvent.Type)
    abstract fun onAddEvent(event: ClickEvent)
    abstract fun onRemoveEvent(event: ClickEvent)
}

/**
 * 为普通按钮控件创建点击事件编辑提供器
 */
fun clickEventsProvider(data: ObservableNormalData): ObservableClickEventsProvider {
    return object : ObservableClickEventsProvider() {
        override val clickEvents: List<ClickEvent>
            get() = data.clickEvents
        override fun onRemoveAllEvents(events: List<ClickEvent>) {
            data.removeAllEvent(events)
        }
        override fun onRemoveAllEvents(type: ClickEvent.Type) {
            data.removeAllEvent(type)
        }
        override fun onAddEvent(event: ClickEvent) {
            data.addEvent(event)
        }
        override fun onRemoveEvent(event: ClickEvent) {
            data.removeEvent(event)
        }
    }
}

/**
 * 为摇杆控件创建锁定状态触发事件编辑提供其
 */
fun joystickLockEventsProvider(data: ObservableJoystickData): ObservableClickEventsProvider {
    return object : ObservableClickEventsProvider() {
        override val clickEvents: List<ClickEvent>
            get() = data.lockEvents
        override fun onRemoveAllEvents(events: List<ClickEvent>) {
            events.forEach { event ->
                data.removeLockEvent { it == event }
            }
        }
        override fun onRemoveAllEvents(type: ClickEvent.Type) {
            data.removeLockEvent { it.type == type }
        }
        override fun onAddEvent(event: ClickEvent) {
            data.addLockEvent(event)
        }
        override fun onRemoveEvent(event: ClickEvent) {
            data.removeLockEvent { it == event }
        }
    }
}

/**
 * 为摇杆控件创建方向触发事件编辑提供其
 */
fun joystickDirectionEventsProvider(
    data: ObservableJoystickData,
    direction: JoystickDirection?,
): ObservableClickEventsProvider {
    return object : ObservableClickEventsProvider() {
        override val clickEvents: List<ClickEvent>
            get() = direction?.let { data.directionEvents[it] } ?: emptyList()
        override fun onRemoveAllEvents(events: List<ClickEvent>) {
            events.forEach { event ->
                data.removeDirectionEvent(direction) { it == event }
            }
        }
        override fun onRemoveAllEvents(type: ClickEvent.Type) {
            data.removeDirectionEvent(direction) { it.type == type }
        }
        override fun onAddEvent(event: ClickEvent) {
            data.addDirectionEvent(direction, event)
        }
        override fun onRemoveEvent(event: ClickEvent) {
            data.removeDirectionEvent(direction) { it == event }
        }
    }
}