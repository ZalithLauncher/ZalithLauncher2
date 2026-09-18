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

package com.movtery.layer_controller

import com.movtery.layer_controller.data.TextAlignment
import com.movtery.layer_controller.layout.ControlLayout

/**
 * 控件编辑器的版本号
 */
internal const val EDITOR_VERSION = 12

/**
 * 自动处理并逐步更新控制布局到新版编辑器
 */
internal fun updateLayoutToNew(
    layout: ControlLayout
): ControlLayout {
    return when (layout.editorVersion) {
        1 -> updateLayoutToNew(update1To2(layout))
        2 -> updateLayoutToNew(update2To3(layout))
        3 -> updateLayoutToNew(update3To4(layout))
        4, 5, 6, 7, 8, 9 -> updateLayoutToNew(update4To10(layout))
        10 -> updateLayoutToNew(update10To11(layout))
        11 -> updateLayoutToNew(update11To12(layout))
        else -> layout
    }
}

/**
 * 1 -> 2: 控件位置、控件大小 数值精确到小数点后2位
 */
private fun update1To2(
    layout: ControlLayout
): ControlLayout = layout.copy(
    editorVersion = 2,
    layers = layout.layers.map { layer ->
        layer.copy(
            normalButtons = layer.normalButtons.map { data ->
                data.copy(
                    position = data.position.copy(
                        x = data.position.x * 10,
                        y = data.position.y * 10
                    ),
                    buttonSize = data.buttonSize.copy(
                        widthPercentage = data.buttonSize.widthPercentage * 10,
                        heightPercentage = data.buttonSize.heightPercentage * 10
                    )
                )
            },
            textBoxes = layer.textBoxes.map { data ->
                data.copy(
                    position = data.position.copy(
                        x = data.position.x * 10,
                        y = data.position.y * 10
                    ),
                    buttonSize = data.buttonSize.copy(
                        widthPercentage = data.buttonSize.widthPercentage * 10,
                        heightPercentage = data.buttonSize.heightPercentage * 10
                    )
                )
            }
        )
    }
)

/**
 * 2 -> 3: 支持在实体鼠标、手柄操控后，隐藏控件层
 */
private fun update2To3(
    layout: ControlLayout
): ControlLayout = layout.copy(
    editorVersion = 3,
    layers = layout.layers.map { layer ->
        layer.copy(
            hideWhenMouse = true,
            hideWhenGamepad = true
        )
    }
)

/**
 * 3 -> 4: 支持为文本设置文本对齐、粗体、斜体、下划线
 */
private fun update3To4(
    layout: ControlLayout
): ControlLayout = layout.copy(
    editorVersion = 4,
    layers = layout.layers.map { layer ->
        layer.copy(
            normalButtons = layer.normalButtons.map { data ->
                data.copy(
                    textAlignment = TextAlignment.Left,
                    textBold = false,
                    textItalic = false,
                    textUnderline = false
                )
            },
            textBoxes = layer.textBoxes.map { data ->
                data.copy(
                    textAlignment = TextAlignment.Left,
                    textBold = false,
                    textItalic = false,
                    textUnderline = false
                )
            }
        )
    }
)

/**
 * 4 -> 5: 支持设置点击事件: `强制显示控件层`，`强制隐藏控件层`
 * 5 -> 6: 支持设置点击事件: `发送消息`
 * 6 -> 7: 支持设置文本大小
 * 7 -> 8: 支持扩展设定: 可设定启动器层的摇杆的样式
 * 8 -> 9: 按钮大小最小可调至 1%
 * 9 -> 10: 在使用摇杆时，支持隐藏控件层
 *
 * 不需要变更控制布局文件，仅提升版本号
 */
private fun update4To10(
    layout: ControlLayout
): ControlLayout = layout.copy(
    editorVersion = 10
)

/**
 * 10 -> 11: 控件外观支持不区分系统主题
 */
private fun update10To11(
    layout: ControlLayout
): ControlLayout = layout.copy(
    editorVersion = 11,
    styles = layout.styles.map { style ->
        style.copy(
            //兼容旧布局，启用主题区分
            commonStyle = false
        )
    }
)

/**
 * 11 -> 12: 控件层支持摇杆控件
 */
private fun update11To12(
    layout: ControlLayout
): ControlLayout = layout.copy(
    editorVersion = 12
)