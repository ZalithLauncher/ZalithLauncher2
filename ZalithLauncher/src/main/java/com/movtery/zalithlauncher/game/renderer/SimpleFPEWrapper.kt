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

package com.movtery.zalithlauncher.game.renderer

import com.movtery.zalithlauncher.game.plugin.Plugin
import com.movtery.zalithlauncher.game.version.installed.Version
import com.movtery.zalithlauncher.game.version.mod.LocalMod
import com.movtery.zalithlauncher.game.version.mod.isEnabled
import com.movtery.zalithlauncher.path.PathManager
import com.movtery.zalithlauncher.utils.logging.Logger

private const val TAG = "SimpleFPEWrapper"

/**
 * @return 获取 SimpleFPEWrapper 原生库的绝对路径
 */
fun getSfpewLibraryPath(): String = "${PathManager.DIR_NATIVE_LIB}/libSimpleFPEWrapper.so"

/**
 * @return 判断启动 [version] 时是否已为 [renderer] 配置 SFPEW
 */
fun isSfpewConfiguredFor(renderer: RendererInterface, version: Version): Boolean {
    return if (renderer is Plugin) {
        version.isSfpewPluginLayerEnabled()
    } else {
        renderer.supportsSfpewByDefault()
    }
}

/**
 * 判断启动 [version] 时是否应启用 SFPEW
 *
 * [launchConfig] 为启动前解析好的渲染器配置，其开关声明优先于渲染器默认
 * 版本中存在 FPE 模拟模组时禁用兼容层，避免功能冲突
 */
fun isSfpewEnabledFor(
    renderer: RendererInterface,
    version: Version,
    launchConfig: RendererLaunchConfig? = null
): Boolean =
    !version.fpeEmulatorModDetected &&
        (launchConfig?.sfpewLayerEnabled ?: isSfpewConfiguredFor(renderer, version))

/**
 * 检测 [mods] 中已启用的 FPE 模拟模组
 * @return 检测到至少一个已启用的 FPE 模拟模组时返回 `true`
 */
fun detectFpeEmulatorMods(mods: List<LocalMod>): Boolean {
    val fpeEmulatorModIds = setOf("angelica", "actinium")

    val detectedIds = mods.filter {
        it.file.isEnabled() && it.id in fpeEmulatorModIds
    }.mapTo(mutableSetOf()) { it.id }

    if (detectedIds.isNotEmpty()) {
        Logger.info(
            TAG,
            "FPE emulator mod detected (${detectedIds.joinToString()}), compatibility layer disabled"
        )
    }
    return detectedIds.isNotEmpty()
}