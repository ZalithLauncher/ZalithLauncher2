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

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerId
import androidx.compose.ui.input.pointer.PointerInputChange
import com.movtery.layer_controller.event.EventHandler

/**
 * 帧级触控事件处理器
 */
class TouchProcessor(
    private val eventHandler: EventHandler,
    private val widgetPosition: (ObservableWidget) -> Offset,
) {
    /**
     * 判断指针坐标是否在控件矩形区域内
     */
    private val hitTest: (widget: ObservableWidget, position: Offset) -> Boolean = { widget, position ->
        val size = widget.internalRenderSize
        val offset = widgetPosition(widget)
        position.x in offset.x..(offset.x + size.width) &&
                position.y in offset.y..(offset.y + size.height)
    }

    /**
     * 处理单帧指针事件
     */
    fun processFrame(
        session: TouchSession,
        change: PointerInputChange,
        visibleWidgets: List<ObservableWidget>,
        allLayers: List<ObservableControlLayer>,
        consumeEvent: (PointerInputChange) -> Unit,
        markPointerAsMoveOnly: (PointerId) -> Unit,
    ) {
        val pointerId = change.id
        val position = change.position

        //获取指针命中的目标控件
        val targets = findTargets(visibleWidgets, position)
        handleOutOfBounds(session, pointerId, position, allLayers)

        routeToTargets(
            session = session,
            change = change,
            pointerId = pointerId,
            targets = targets,
            allLayers = allLayers,
            consumeEvent = consumeEvent,
            markPointerAsMoveOnly = markPointerAsMoveOnly,
        )
    }

    private fun findTargets(
        visibleWidgets: List<ObservableWidget>,
        position: Offset,
    ): List<ObservableWidget> {
        val hitList = visibleWidgets.filter { widget ->
            widget.canTouch() && hitTest(widget, position)
        }
        if (hitList.isEmpty()) return emptyList()

        // 找到第一个支持深度检测的控件（通常是拦截后续分发的终点，如摇杆）
        val firstDeepWidget = hitList.firstOrNull { it.supportsDeepTouchDetection() }
            ?: return hitList

        val topIndex = hitList.indexOf(firstDeepWidget)
        // 返回从顶层到该深层控件之间的所有控件
        return hitList.subList(0, topIndex + 1)
    }

    private fun handleOutOfBounds(
        session: TouchSession,
        pointerId: PointerId,
        position: Offset,
        allLayers: List<ObservableControlLayer>,
    ) {
        val widgets = session.activeWidgets(pointerId)
        if (widgets.isEmpty()) return

        val preSnapshot = session.snapshot(pointerId)
        val backInBounds = mutableListOf<ObservableWidget>()
        val removed = mutableListOf<ObservableWidget>()

        for (widget in widgets) {
            if (!widget.behavior.releaseOnOutOfBounds) continue

            if (!hitTest(widget, position)) {
                widget.onReleaseEvent(eventHandler, allLayers)
                removed.add(widget)
            } else {
                backInBounds.add(widget)
            }
        }

        if (removed.isNotEmpty()) {
            session.setActiveWidgets(pointerId, widgets - removed)
        }

        for (widget in backInBounds) {
            widget.onPointerBackInBounds(eventHandler, allLayers)
        }

        val currentWidgets = session.activeWidgets(pointerId)
        if (currentWidgets.isEmpty() && preSnapshot.any { it.behavior is InteractionBehavior.Swipable }) {
            session.enterSwipeChain(pointerId)
        }

        if (currentWidgets.isNotEmpty() && session.isInSwipeChain(pointerId)) {
            session.exitSwipeChain(pointerId)
        }
    }

    private fun routeToTargets(
        session: TouchSession,
        change: PointerInputChange,
        pointerId: PointerId,
        targets: List<ObservableWidget>,
        allLayers: List<ObservableControlLayer>,
        consumeEvent: (PointerInputChange) -> Unit,
        markPointerAsMoveOnly: (PointerId) -> Unit,
    ) {
        val activeWidgets = session.activeWidgets(pointerId)
        
        // 优先处理已激活的控件（捕获模式），确保在拖动过程中不被其他控件拦截
        // 其次处理新命中的目标
        val combinedTargets = (activeWidgets + targets).distinct()

        if (combinedTargets.isEmpty()) return

        for (target in combinedTargets) {
            if (session.isInSwipeChain(pointerId) && !target.behavior.canBeSwipedTo) {
                continue
            }

            target.onTouchEvent(
                eventHandler = eventHandler,
                allLayers = allLayers,
                change = change,
                activeWidgets = activeWidgets,
                addThis = {
                    session.addActiveWidget(pointerId, target)
                },
                consumeEvent = { shouldConsume ->
                    if (shouldConsume) {
                        consumeEvent(change)
                    } else {
                        markPointerAsMoveOnly(pointerId)
                    }
                },
            )

            if (target.canProcess()) break
        }
    }
}
