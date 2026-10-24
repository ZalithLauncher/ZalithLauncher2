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

package com.movtery.zalithlauncher.ui.screens.main.control_editor

import com.movtery.layer_controller.event.ClickEvent
import com.movtery.layer_controller.observable.ObservableButtonStyle
import com.movtery.layer_controller.observable.ObservableClickEventsProvider
import com.movtery.layer_controller.observable.ObservableControlLayer
import com.movtery.layer_controller.observable.ObservableJoystickStyle
import com.movtery.layer_controller.observable.ObservableTranslatableString
import com.movtery.layer_controller.observable.ObservableWidget
import com.movtery.zalithlauncher.game.control.CabinetStyleItem
import com.movtery.zalithlauncher.ui.screens.main.control_editor.edit_widget.SelectedWidgetData
import com.movtery.zalithlauncher.ui.screens.main.control_editor.style_cabinet.OpenCabinetSource
import com.movtery.zalithlauncher.viewmodel.EditorViewModel

/**
 * 控制布局编辑器主操作状态机
 */
sealed interface EditorOperation {
    /** 空闲 */
    data object None : EditorOperation

    /** 打开控件编辑器（编辑 [SelectedWidgetData] 指向的控件） */
    data object SelectButton : EditorOperation

    /** 编辑控件层属性，进入自 [EditorViewModel.editLayer] */
    data class EditLayer(val layer: ObservableControlLayer) : EditorOperation

    /** 请求删除控件层的对话框 */
    data class DeleteLayer(val layer: ObservableControlLayer) : EditorOperation

    /** 打开控件外观列表对话框 */
    data object OpenStyleList : EditorOperation

    /** 创建控件外观对话框 */
    data object CreateStyle : EditorOperation

    /** 编辑布局内的控件外观样式 */
    data class EditButtonStyle(val style: ObservableButtonStyle) : EditorOperation

    /** 请求删除控件外观样式对话框 */
    data class DeleteButtonStyle(val style: ObservableButtonStyle) : EditorOperation

    /** 编辑布局内的摇杆样式 */
    data class EditJoystickStyle(val style: ObservableJoystickStyle) : EditorOperation

    /** 请求删除摇杆样式对话框 */
    data class DeleteJoystickStyle(val style: ObservableJoystickStyle) : EditorOperation

    /** 打开摇杆样式列表对话框 */
    data object OpenJoystickStyleList : EditorOperation

    /** 创建摇杆样式对话框 */
    data object CreateJoystickStyle : EditorOperation

    /** 打开样式收藏柜，[source] 记录来源 */
    data class OpenStyleCabinet(val source: OpenCabinetSource) : EditorOperation

    /** 收藏控件外观样式时，收藏柜中已存在同 uuid 样式的处理方式对话框 */
    data class FavoriteButtonStyleConflict(val style: ObservableButtonStyle) : EditorOperation

    /** 收藏摇杆样式时，收藏柜中已存在同 uuid 样式的处理方式对话框 */
    data class FavoriteJoystickStyleConflict(val style: ObservableJoystickStyle) : EditorOperation

    /** 柜内控件外观样式加入布局时，布局中已存在同 uuid 样式的处理方式对话框 */
    data class AddToLayoutButtonStyleConflict(
        val item: CabinetStyleItem.Button,
        val source: OpenCabinetSource
    ) : EditorOperation

    /** 柜内摇杆样式加入布局时，布局中已存在同 uuid 样式的处理方式对话框 */
    data class AddToLayoutJoystickStyleConflict(
        val item: CabinetStyleItem.Joystick,
        val source: OpenCabinetSource
    ) : EditorOperation

    /** 编辑收藏柜中的控件外观样式 */
    data class EditCabinetButtonStyle(
        val item: CabinetStyleItem.Button,
        val source: OpenCabinetSource
    ) : EditorOperation

    /** 编辑收藏柜中的摇杆样式 */
    data class EditCabinetJoystickStyle(
        val item: CabinetStyleItem.Joystick,
        val source: OpenCabinetSource
    ) : EditorOperation

    /** 控制布局正在保存中 */
    data object Saving : EditorOperation

    /** 控制布局保存失败对话框 */
    data class SaveFailed(val error: Throwable) : EditorOperation
}

/**
 * 控制布局编辑器对控件的操作状态机
 */
sealed interface EditorWidgetOperation {
    /** 空闲 */
    data object None : EditorWidgetOperation

    /** 选择了一个控件, 并询问用户将其复制到哪些控制层 */
    data class CloneButton(
        val data: ObservableWidget,
        val layer: ObservableControlLayer
    ) : EditorWidgetOperation

    /** 删除一个控件的警告对话框 */
    data class DeleteButton(
        val data: ObservableWidget,
        val layer: ObservableControlLayer
    ) : EditorWidgetOperation

    /** 编辑控件的显示文本 */
    data class EditWidgetText(val string: ObservableTranslatableString) : EditorWidgetOperation

    /** 编辑切换控件层可见性事件 */
    data class SwitchLayersVisibility(
        val data: ObservableClickEventsProvider,
        val type: ClickEvent.Type
    ) : EditorWidgetOperation

    /** 编辑发送的文本 */
    data class SendText(val data: ObservableClickEventsProvider) : EditorWidgetOperation
}

/**
 * 控制布局编辑器的警告操作状态机
 */
sealed interface EditorWarningOperation {
    /** 空闲 */
    data object None : EditorWarningOperation
    /** 没有控件层，提醒用户添加 */
    data object WarningNoLayers : EditorWarningOperation
    /** 没有选择控件层，提醒用户选择 */
    data object WarningNoSelectLayer : EditorWarningOperation
}
