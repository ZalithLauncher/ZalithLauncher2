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

package com.movtery.zalithlauncher.ui.screens.content.versions.elements

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.movtery.zalithlauncher.game.version.installed.VersionConfig
import java.io.File

class StatefulVersionConfig(private val config: VersionConfig) {
    val versionPath: File = config.getVersionPath()

    var isolationType by mutableStateOf(config.isolationType)
    var skipGameIntegrityCheck by mutableStateOf(config.skipGameIntegrityCheck)
    var javaRuntime by mutableStateOf(config.javaRuntime)
    var jvmArgs by mutableStateOf(config.jvmArgs)
    var gameArgs by mutableStateOf(config.gameArgs)
    var renderer by mutableStateOf(config.renderer)
    var sfpewPluginLayer by mutableStateOf(config.sfpewPluginLayer)
    var sfpewRelaxedContext by mutableStateOf(config.sfpewRelaxedContext)
    var driver by mutableStateOf(config.driver)
    var graphicsApi by mutableStateOf(config.graphicsApi)
    var control by mutableStateOf(config.control)
    var customInfo by mutableStateOf(config.customInfo)
    var serverIp by mutableStateOf(config.serverIp)
    var ramAllocation by mutableIntStateOf(config.ramAllocation)
    var touchVibrateKind by mutableStateOf(config.touchVibrateKind)
    var touchVibrateDuration by mutableIntStateOf(config.touchVibrateDuration)

    /**
     * 将当前状态重新打包为新的 [VersionConfig] 对象
     */
    fun pack(): VersionConfig = VersionConfig(versionPath).also { packed ->
        packInto(packed)
        packed.customPath = config.customPath
        packed.versionSummary = config.versionSummary
    }

    /**
     * 将当前状态写入已有的 [VersionConfig] 对象
     */
    fun packInto(target: VersionConfig) {
        target.isolationType = isolationType
        target.skipGameIntegrityCheck = skipGameIntegrityCheck
        target.javaRuntime = javaRuntime
        target.jvmArgs = jvmArgs
        target.gameArgs = gameArgs
        target.renderer = renderer
        target.sfpewPluginLayer = sfpewPluginLayer
        target.sfpewRelaxedContext = sfpewRelaxedContext
        target.driver = driver
        target.graphicsApi = graphicsApi
        target.control = control
        target.customInfo = customInfo
        target.serverIp = serverIp
        target.ramAllocation = ramAllocation
        target.touchVibrateKind = touchVibrateKind
        target.touchVibrateDuration = touchVibrateDuration
    }

    /**
     * @return 托管字段自创建以来是否被修改过
     */
    fun isModified(): Boolean = config.isModified(pack())
}
