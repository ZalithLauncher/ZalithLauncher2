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
     * @param visibleWidgets 预过滤后的可见控件列表
     * @param allLayers 所有控件层
     * @param consumeEvent 消费事件的回调
     * @param markPointerAsMoveOnly 标记指针为仅移动的回调
     */
    fun processFrame(
        session: PointerEventBus,
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

    /**
     * 从可见控件中找出当前指针命中的目标控件
     */
    private fun findTargets(
        visibleWidgets: List<ObservableWidget>,
        position: Offset,
    ): List<ObservableWidget> {
        val hitList = visibleWidgets.filter { widget ->
            widget.canTouch() && hitTest(widget, position)
        }
        if (hitList.isEmpty()) return emptyList()

        //找到第一个支持深度检测的控件
        val firstDeepWidget = hitList
            .firstOrNull { it.supportsDeepTouchDetection() }
            ?: return hitList

        val topIndex = hitList.indexOf(firstDeepWidget)
        //只保留该控件及其上方的可穿透控件
        return hitList.subList(0, topIndex + 1)
            .filter { !it.canProcess() }
    }

    /**
     * 处理活跃控件的越界释放
     */
    private fun handleOutOfBounds(
        session: PointerEventBus,
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
                //指针重新回到控件边界内
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
        if (
            currentWidgets.isEmpty() &&
            //越界前存在可滑动控件
            preSnapshot.any { it.behavior is InteractionBehavior.Swipable }
        ) {
            session.enterSwipeChain(pointerId)
        }

        //指针回到了控件上，退出滑动链
        if (currentWidgets.isNotEmpty() && session.isInSwipeChain(pointerId)) {
            session.exitSwipeChain(pointerId)
        }
    }


    private fun routeToTargets(
        session: PointerEventBus,
        change: PointerInputChange,
        pointerId: PointerId,
        targets: List<ObservableWidget>,
        allLayers: List<ObservableControlLayer>,
        consumeEvent: (PointerInputChange) -> Unit,
        markPointerAsMoveOnly: (PointerId) -> Unit,
    ) {
        if (targets.isEmpty()) return

        val activeWidgets = session.activeWidgets(pointerId)

        for (target in targets) {
            if (target.canProcess()) return

            //只允许可滑动且非可切换的控件通过
            if (
                session.isInSwipeChain(pointerId) &&
                !target.behavior.canBeSwipedTo
            ) {
                continue
            }

            target.onTouchEvent(
                eventHandler = eventHandler,
                allLayers = allLayers,
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
        }
    }
}
