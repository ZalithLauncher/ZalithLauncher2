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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.unit.IntSize
import com.movtery.layer_controller.data.ButtonPosition
import com.movtery.layer_controller.data.ButtonSize
import com.movtery.layer_controller.data.VisibilityType
import com.movtery.layer_controller.event.EventHandler

/**
 * 可观察的BaseData包装类
 */
abstract class ObservableWidget {
    /**
     * 编辑模式中，是否正在编辑位置
     */
    var isEditingPos by mutableStateOf(false)

    /**
     * 编辑模式中，记录实时偏移量
     */
    var movingOffset by mutableStateOf(Offset.Zero)

    /**
     * 控件的内部渲染位置（相对于画布的像素偏移量）
     */
    internal var internalRenderOffsetPx by mutableStateOf(Offset.Zero)

    /**
     * 控件的内部渲染大小
     */
    internal var internalRenderSize by mutableStateOf(IntSize.Zero)

    /**
     * 组件的内部渲染位置属性
     */
    internal abstract val internalRenderPosition: ButtonPosition

    /**
     * 存入内部渲染位置属性
     */
    internal abstract fun putRenderPosition(position: ButtonPosition)

    /**
     * 组件的样式 ID
     */
    internal abstract val styleId: String?

    /**
     * 组件的大小属性
     */
    internal abstract val widgetSize: ButtonSize

    /**
     * 存入组件的大小属性
     */
    internal abstract fun putWidgetSize(size: ButtonSize)

    /**
     * 控件的交互行为模型
     */
    abstract val behavior: InteractionBehavior

    /**
     * 确认该组件是否可以响应触摸事件
     */
    open fun canTouch(): Boolean = true

    /**
     * Compose 树开始布局时
     */
    abstract fun onCompositionStart(eventHandler: EventHandler?)

    /**
     * Compose 树结束布局时
     */
    abstract fun onCompositionDispose(eventHandler: EventHandler?)

    /**
     * 获取该组件可见类型
     */
    abstract fun onCheckVisibilityType(): VisibilityType

    /**
     * 判断该组件是否支持深度触摸检测和取最深操作
     */
    abstract fun supportsDeepTouchDetection(): Boolean

    /**
     * 检查是否要处理这个触摸事件
     * @return 是否可以处理
     */
    abstract fun canProcess(): Boolean

    /**
     * 响应触摸事件
     * @param allLayers 当前所有可观察控制层
     * @param activeWidgets 当前指针活动中的组件
     * @param addThis 标记组件在该指针活动
     * @param consumeEvent 是否要求标记消费事件
     */
    abstract fun onTouchEvent(
        eventHandler: EventHandler,
        allLayers: List<ObservableControlLayer>,
        change: PointerInputChange,
        activeWidgets: List<ObservableWidget>,
        addThis: () -> Unit,
        consumeEvent: (Boolean) -> Unit
    )

    /**
     * 用于判断组件是否支持 移出边界即视为松开 的交互行为
     */
    abstract fun isReleaseOnOutOfBounds(): Boolean

    /**
     * 手指回到组件内
     * @param allLayers 当前所有可观察控制层
     */
    abstract fun onPointerBackInBounds(
        eventHandler: EventHandler,
        allLayers: List<ObservableControlLayer>
    )

    /**
     * 响应松开触摸事件
     * @param allLayers 当前所有可观察控制层
     */
    abstract fun onReleaseEvent(
        eventHandler: EventHandler,
        allLayers: List<ObservableControlLayer>
    )
}