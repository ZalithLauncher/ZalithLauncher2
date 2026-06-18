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

import androidx.compose.ui.input.pointer.PointerId

/**
 * 触控会话状态持有者
 */
class TouchSession {
    /**
     * 指针 → 当前按压中的所有控件（按加入顺序）
     */
    private val _activeWidgets = mutableMapOf<PointerId, MutableList<ObservableWidget>>()

    /**
     * 处于滑动链中的指针集合
     */
    private val _swipeChainPointers = mutableSetOf<PointerId>()

    // ────────────────────────────────────────────────────────
    // 生命周期
    // ────────────────────────────────────────────────────────

    /**
     * 手指抬起：清理该指针的所有状态，返回需要释放的控件列表。
     */
    fun endPointer(pointerId: PointerId): List<ObservableWidget> {
        _swipeChainPointers.remove(pointerId)
        return _activeWidgets.remove(pointerId)?.toList() ?: emptyList()
    }

    // ────────────────────────────────────────────────────────
    // 活跃控件管理
    // ────────────────────────────────────────────────────────

    /**
     * 获取指定指针的当前活跃控件列表（不可变副本）。
     */
    fun activeWidgets(pointerId: PointerId): List<ObservableWidget> {
        return _activeWidgets[pointerId]?.toList() ?: emptyList()
    }

    /**
     * 替换指定指针的活跃控件列表。
     */
    fun setActiveWidgets(pointerId: PointerId, widgets: List<ObservableWidget>) {
        if (widgets.isEmpty()) {
            _activeWidgets.remove(pointerId)
        } else {
            _activeWidgets[pointerId] = widgets.toMutableList()
        }
    }

    /**
     * 将控件加入指定指针的活跃列表。
     */
    fun addActiveWidget(pointerId: PointerId, widget: ObservableWidget) {
        val list = _activeWidgets.getOrPut(pointerId) { mutableListOf() }
        if (!list.contains(widget)) {
            list.add(widget)
        }
    }

    /**
     * 获取活跃控件快照（用于越界检测前的状态保存）。
     */
    fun snapshot(pointerId: PointerId): List<ObservableWidget> = activeWidgets(pointerId)

    // ────────────────────────────────────────────────────────
    // 滑动链管理
    // ────────────────────────────────────────────────────────

    /** 指定指针是否处于滑动链中 */
    fun isInSwipeChain(pointerId: PointerId): Boolean = pointerId in _swipeChainPointers

    /** 标记指针进入滑动链 */
    fun enterSwipeChain(pointerId: PointerId) {
        _swipeChainPointers.add(pointerId)
    }

    /** 将指针移出滑动链 */
    fun exitSwipeChain(pointerId: PointerId) {
        _swipeChainPointers.remove(pointerId)
    }
}
