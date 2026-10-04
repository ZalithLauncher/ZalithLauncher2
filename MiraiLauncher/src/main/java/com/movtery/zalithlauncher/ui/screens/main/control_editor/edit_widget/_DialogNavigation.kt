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

package com.movtery.zalithlauncher.ui.screens.main.control_editor.edit_widget

import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.ui.screens.TitledNavKey
import com.movtery.zalithlauncher.ui.screens.content.elements.CategoryIcon
import com.movtery.zalithlauncher.ui.screens.content.elements.CategoryItem
import kotlinx.serialization.Serializable

sealed interface EditWidgetCategory : TitledNavKey {
    /** 基本信息 */
    @Serializable data object Info : EditWidgetCategory
    /** 文本样式 */
    @Serializable data object TextStyle : EditWidgetCategory
    /** 点击事件 */
    @Serializable data object ClickEvent : EditWidgetCategory
    /** 控件样式 */
    @Serializable data object Style : EditWidgetCategory
    /** 摇杆配置 */
    @Serializable data object JoystickConfig : EditWidgetCategory
    /** 方向事件 */
    @Serializable data object DirectionEvents : EditWidgetCategory
    /** 摇杆样式 */
    @Serializable data object JoystickStyle : EditWidgetCategory
}

/**
 * 编辑普通控件标签页
 */
val editWidgetCategories = listOf(
    CategoryItem(EditWidgetCategory.Info, { CategoryIcon(R.drawable.ic_info_outlined, R.string.control_editor_edit_category_info) }, R.string.control_editor_edit_category_info),
    CategoryItem(EditWidgetCategory.TextStyle, { CategoryIcon(R.drawable.ic_text_format, R.string.control_editor_edit_text) }, R.string.control_editor_edit_text),
    CategoryItem(EditWidgetCategory.ClickEvent, { CategoryIcon(R.drawable.ic_touch_app_outlined, R.string.control_editor_edit_category_event) }, R.string.control_editor_edit_category_event),
    CategoryItem(EditWidgetCategory.Style, { CategoryIcon(R.drawable.ic_style_outlined, R.string.control_editor_edit_category_style) }, R.string.control_editor_edit_category_style)
)

/**
 * 编辑摇杆控件标签页
 */
val editJoystickCategories = listOf(
    CategoryItem(EditWidgetCategory.Info, { CategoryIcon(R.drawable.ic_info_outlined, R.string.control_editor_edit_category_info) }, R.string.control_editor_edit_category_info),
    CategoryItem(EditWidgetCategory.JoystickConfig, { CategoryIcon(R.drawable.ic_settings_filled, R.string.control_editor_edit_category_joystick_config) }, R.string.control_editor_edit_category_joystick_config),
    CategoryItem(EditWidgetCategory.DirectionEvents, { CategoryIcon(R.drawable.ic_touch_app_outlined, R.string.control_editor_edit_category_joystick_events) }, R.string.control_editor_edit_category_joystick_events),
    CategoryItem(EditWidgetCategory.JoystickStyle, { CategoryIcon(R.drawable.ic_style_outlined, R.string.control_editor_edit_category_joystick_style) }, R.string.control_editor_edit_category_joystick_style)
)