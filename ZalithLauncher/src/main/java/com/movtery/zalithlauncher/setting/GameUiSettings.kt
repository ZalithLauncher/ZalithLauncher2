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

package com.movtery.zalithlauncher.setting

import androidx.compose.ui.geometry.Offset
import com.movtery.layer_controller.utils.snap.SnapMode
import com.movtery.zalithlauncher.setting.enums.FpsDisplayMode
import com.movtery.zalithlauncher.ui.control.HotbarRule

object GameUiSettings : SettingsRegistry() {
    /**
     * 在游戏菜单悬浮窗上显示帧率
     */
    val showFPS = boolSetting("showFPS", true)

    /**
     * 游戏内帧率的展示模式
     */
    val fpsDisplayMode = enumSetting("fpsDisplayMode", FpsDisplayMode.NUMBER)

    /**
     * 在游戏菜单悬浮窗上显示内存
     */
    val showMemory = boolSetting("showMemory", false)

    /**
     * 在游戏画面上展示菜单悬浮窗
     */
    val showMenuBall = boolSetting("showMenuBall", true)

    /**
     * 游戏菜单悬浮窗位置
     */
    val menuBallPos = offsetSetting("menuBallPos", Offset.Zero)

    /**
     * 游戏菜单悬浮窗不透明度
     */
    val menuBallOpacity = intSetting("menuBallOpacity", 100, 20..100)

    /**
     * 快捷栏判定箱计算规则
     */
    val hotbarRule = enumSetting("hotbarRule", HotbarRule.Auto)

    /**
     * 快捷栏宽度百分比
     */
    val hotbarWidth = intSetting("hotbarWidth", 500, 0..1000)

    /**
     * 快捷栏高度百分比
     */
    val hotbarHeight = intSetting("hotbarHeight", 100, 0..1000)

    /**
     * 快捷栏双击与副手交换物品
     */
    val hotbarDoubleClick = boolSetting("hotbarDoubleClick", true)

    /**
     * 快捷栏长按丢弃所选物品
     */
    val hotbarLongClick = boolSetting("hotbarLongClick", true)

    /**
     * 快捷栏长按快捷栏触发延迟
     */
    val hotbarLongClickDelay = intSetting("hotbarLongClickDelay", 300, 100..1000)

    /**
     * 游戏内控制布局的整体不透明度
     */
    val controlsOpacity = intSetting("controlsOpacity", 100, 0..100)

    /**
     * 控制布局编辑器：是否开启控件吸附功能
     */
    val editorEnableWidgetSnap = boolSetting("editorEnableWidgetSnap", true)

    /**
     * 控制布局编辑器：是否在所有控件层范围内吸附
     */
    val editorSnapInAllLayers = boolSetting("editorSnapInAllLayers", false)

    /**
     * 控制布局编辑器：控件吸附模式
     */
    val editorWidgetSnapMode = enumSetting("editorWidgetSnapMode", SnapMode.FullScreen)
}
