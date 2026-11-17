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

import com.movtery.zalithlauncher.game.version.installed.GraphicsApi
import com.movtery.zalithlauncher.setting.enums.ResolutionRule

object RendererSettings : SettingsRegistry() {
    /**
     * 全局渲染器
     */
    val renderer = stringSetting("renderer", "")

    /**
     * 为渲染器插件叠加 SFPEW 兼容层
     */
    val sfpewPluginLayer = boolSetting("sfpewPluginLayer", false)

    /**
     * SFPEW 宽松上下文模式
     */
    val sfpewRelaxedContext = boolSetting("sfpewRelaxedContext", false)

    /**
     * Vulkan 驱动器
     */
    val vulkanDriver = stringSetting("vulkanDriver", "default turnip")

    /**
     * 图形 API（Minecraft 26.2+）
     */
    val graphicsApi = enumSetting("graphicsApi", GraphicsApi.DEFAULT_OPENGL)

    /**
     * 分辨率
     */
    val resolutionRatio = intSetting("resolutionRatio", 100, 25..300)

    /**
     * 分辨率规则
     */
    val resolutionRule = enumSetting("resolutionRule", ResolutionRule.PERCENTAGE)

    /**
     * 自定义分辨率宽度，0 表示尚未初始化
     */
    val customResolutionWidth = intSetting("customResolutionWidth", 0)

    /**
     * 自定义分辨率高度，0 表示尚未初始化
     */
    val customResolutionHeight = intSetting("customResolutionHeight", 0)

    /**
     * 游戏页面全屏化
     */
    val gameFullScreen = boolSetting("gameFullScreen", true)

    /**
     * 持续性能模式
     */
    val sustainedPerformance = boolSetting("sustainedPerformance", false)

    /**
     * 游戏运行时请求系统切换到同分辨率最高刷新率档位（关闭则回落系统自适应刷新）
     */
    val requestMaxRefreshRate = boolSetting("requestMaxRefreshRate", true)

    /**
     * 使用系统的 Vulkan 驱动
     */
    val zinkPreferSystemDriver = boolSetting("zinkPreferSystemDriver", false)

    /**
     * Zink 垂直同步
     */
    val vsyncInZink = boolSetting("vsyncInZink", false)

    /**
     * 启用着色器日志输出
     */
    val dumpShaders = boolSetting("dumpShaders", false)
}
