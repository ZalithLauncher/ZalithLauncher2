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
 * 共享的多指针状态管理器
 * 管理每个指针的活跃控件列表和滑动链状态
 */
class PointerEventBus {
    /**
     * 指针 → 当前按压中的所有控件（按加入顺序）
     */
    private val _activeWidgets = mutableMapOf<PointerId, MutableList<ObservableWidget>>()

    /**
     * 处于滑动链中的指针集合
     */
    private val _swipeChainPointers = mutableSetOf<PointerId>()

    /**
     * 检查已占用的指针（来自 MouseControlLayout / Hotbar 等外部层）
     */
    var checkOccupiedPointers: (PointerId) -> Boolean = { false }

    /**
     * 标记指针为仅移动（不消费事件）
     */
    var markPointerAsMoveOnly: (PointerId) -> Unit = {}

    // ────────────────────────────────────────────────────────
    // 生命周期
    // ────────────────────────────────────────────────────────

    /**
     * 手指抬起：清理该指针的所有状态，返回需要释放的控件列表
     */
    fun endPointer(pointerId: PointerId): List<ObservableWidget> {
        _swipeChainPointers.remove(pointerId)
        return _activeWidgets.remove(pointerId)?.toList() ?: emptyList()
    }

    // ────────────────────────────────────────────────────────
    // 活跃控件管理
    // ────────────────────────────────────────────────────────

    /**
     * 获取指定指针的当前活跃控件列表
     */
    fun activeWidgets(pointerId: PointerId): List<ObservableWidget> {
        return _activeWidgets[pointerId]?.toList() ?: emptyList()
    }

    /**
     * 将控件加入指定指针的活跃列表
     */
    fun addActiveWidget(pointerId: PointerId, widget: ObservableWidget) {
        _activeWidgets.getOrPut(pointerId) { mutableListOf() }.add(widget)
    }

    /**
     * 获取活跃控件快照
     */
    fun snapshot(pointerId: PointerId): List<ObservableWidget> = activeWidgets(pointerId)

    /**
     * 替换指定指针的活跃控件列表
     */
    fun setActiveWidgets(pointerId: PointerId, widgets: List<ObservableWidget>) {
        _activeWidgets[pointerId] = widgets.toMutableList()
    }

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
