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

import com.movtery.zalithlauncher.BuildKeys

object GameSettings : SettingsRegistry() {
    /**
     * 版本隔离
     */
    val versionIsolation = boolSetting("versionIsolation", true)

    /**
     * 不检查游戏完整性
     */
    val skipGameIntegrityCheck = boolSetting("skipGameIntegrityCheck", false)

    /**
     * 版本自定义信息
     */
    val versionCustomInfo = stringSetting("versionCustomInfo", "${BuildKeys.LAUNCHER_IDENTIFIER}[zl_version]")

    /**
     * 启动器的Java环境
     */
    val javaRuntime = stringSetting("javaRuntime", "")

    /**
     * 自动选择Java环境
     */
    val autoPickJavaRuntime = boolSetting("autoPickJavaRuntime", true)

    /**
     * 游戏内存分配大小
     */
    val ramAllocation = intSetting("ramAllocation", null, min = 256)

    /**
     * 自定义Jvm启动参数
     */
    val jvmArgs = stringSetting("jvmArgs", "")

    /**
     * 已禁用的原生库插件列表
     */
    val disableNativeLibPlugins = stringListSetting("nativeLibPlugins", emptyList())

    /**
     * 启动游戏时自动展示日志，直到游戏开始渲染
     */
    val showLogAutomatic = boolSetting("showLogAutomatic", false)

    /**
     * 日志字体大小
     */
    val logTextSize = intSetting("logTextSize", 15, 5..20)

    /**
     * 日志缓冲区刷新时间
     */
    val logBufferFlushInterval = intSetting("logBufferFlushInterval", 200, 100..1000)
}
